import java.net.*;
import java.io.*;

/**
 * RequestJob — implementa job (interfaz de tu equipo).
 *
 * Es el "servidor hijo": recibe connfd del padre y ejecuta
 * el pipeline completo fetch → parse → compress → send.
 *
 * El thread hace chdir implícito al directorio www buscando
 * los archivos relativos a esa carpeta, igual que haría un
 * proceso hijo que cambia su directorio de trabajo.
 */
public class RequestJob implements job {

    private static final String WWW_ROOT = "/var/www/html"; // directorio base de archivos

    private final Socket       connfd;   // conexión heredada del padre
    private final SessionStore sessions;

    public RequestJob(Socket connfd, SessionStore sessions) {
        this.connfd   = connfd;
        this.sessions = sessions;
    }

    @Override
    public void execute() {
        // try-with-resources: connfd se cierra al terminar (hijo cierra su copia)
        try (connfd) {
            String workerName = Thread.currentThread().getName();

            // ── 1. FETCH — leer request del socket ────────────
            FetchWorker fetcher = new FetchWorker(connfd);
            String rawHeaders;
            try {
                rawHeaders = fetcher.fetch();
            } catch (SocketTimeoutException e) {
                // Conexión vacía del navegador — ignorar silenciosamente
                return;
            }
            if (rawHeaders == null || rawHeaders.isBlank()) return;

            // ── 2. PARSE — interpretar HTTP ───────────────────
            ParseWorker parser   = new ParseWorker(rawHeaders);
            HttpRequest request  = parser.parse();
            System.out.printf("[%s] → %s %s%n", workerName, request.method(), request.path());

            // Leer body si existe (POST)
            int contentLength = 0;
            try { contentLength = Integer.parseInt(request.header("content-length")); }
            catch (NumberFormatException ignored) {}
            byte[] requestBody = fetcher.fetchBody(contentLength);

            // ── Sesión — recordar cliente entre requests ──────
            String clientIp = request.header("x-real-ip");
            if (clientIp == null || clientIp.isBlank())
                clientIp = connfd.getInetAddress().toString(); // fallback if no proxy
            String sessionId = sessions.getOrCreate(request.header("cookie"), clientIp);
            sessions.recordRequest(sessionId, request.path());
            String sessionCookie = sessions.setCookieHeader(sessionId);

            // ── 3. Resolver archivo (chdir implícito a www) ───
            // El thread busca el archivo dentro de WWW_ROOT,
            // equivalente a hacer chdir("www") antes de open()
            ResponseData response = resolveFile(request.path(), WWW_ROOT);

            // ── 4. COMPRESS — gzip solo para texto ────────────
            boolean compressible  = isCompressible(request.path());
            boolean useGzip       = request.acceptsGzip() && compressible;
            CompressWorker comp   = new CompressWorker(response.body());
            byte[] finalBody      = useGzip ? comp.compress() : response.body();

            // ── 5. SEND — escribir respuesta al cliente ────────
            SendWorker sender = new SendWorker(connfd, finalBody, useGzip, sessionCookie);
            sender.send(response.statusCode(), response.statusText(), response.contentType());

            System.out.printf("[%s] ← %d %s — %d bytes%s%n",
                    workerName, response.statusCode(), response.statusText(),
                    finalBody.length, useGzip ? " (gzip)" : "");

        } catch (SocketException e) {
            // Cliente cerró la conexión — normal al navegar o recargar
        } catch (Exception e) {
            System.err.printf("[%s] Error inesperado: %s%n",
                    Thread.currentThread().getName(), e.getMessage());
        }
    }

    /**
     * Resuelve el archivo pedido dentro de wwwRoot.
     * Equivale al chdir que mencionó el profesor:
     * el thread trabaja relativo a "www/" para encontrar el recurso.
     */
    private ResponseData resolveFile(String path, String wwwRoot) throws IOException {
        // Seguridad: bloquear path traversal (../../etc/passwd)
        if (path.contains("..")) {
            byte[] body = "<html><body><h1>403 Forbidden</h1></body></html>".getBytes("UTF-8");
            return new ResponseData(403, "Forbidden", "text/html; charset=UTF-8", body);
        }

        String filePath = path.equals("/") ? "index.html" : path.substring(1);

        // chdir implícito: buscar el archivo dentro de wwwRoot
        File file = new File(wwwRoot, filePath);

        if (file.exists() && file.isFile()) {
            byte[] body = java.nio.file.Files.readAllBytes(file.toPath());
            return new ResponseData(200, "OK", contentTypeFor(filePath), body);
        }

        byte[] body = ("<html><body><h1>404 - No encontrado</h1><p>"
                + filePath + "</p></body></html>").getBytes("UTF-8");
        return new ResponseData(404, "Not Found", "text/html; charset=UTF-8", body);
    }

    private boolean isCompressible(String path) {
        String p = path.toLowerCase();
        return p.endsWith(".html") || p.endsWith(".css") || p.endsWith(".js")
                || p.endsWith(".json") || p.endsWith(".svg") || p.equals("/");
    }

    private String contentTypeFor(String path) {
        String p = path.toLowerCase();
        if (p.endsWith(".txt"))              return "text/plain";
        if (p.endsWith(".css"))              return "text/css";
        if (p.endsWith(".js"))               return "application/javascript";
        if (p.endsWith(".json"))             return "application/json";
        if (p.endsWith(".svg"))              return "image/svg+xml";
        if (p.endsWith(".png"))              return "image/png";
        if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return "image/jpeg";
        if (p.endsWith(".gif"))              return "image/gif";
        if (p.endsWith(".webp"))             return "image/webp";
        if (p.endsWith(".ico"))              return "image/x-icon";
        if (p.endsWith(".woff2"))            return "font/woff2";
        if (p.endsWith(".woff"))             return "font/woff";
        return "text/html; charset=UTF-8";
    }
}
