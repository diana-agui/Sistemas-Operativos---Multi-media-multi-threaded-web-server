import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class HandleRequestJob implements job {
    private final Socket clientSocket;
    private static final String ROOT_DIR = "C:/www/html";
    public HandleRequestJob(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void execute() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
        ) {
            // 1. Leer la primera línea del request: "GET /pagina.html HTTP/1.1"
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;


            System.out.println("[REQUEST] " + requestLine);

            // 2. Parsear la URL solicitada
            String[] parts = requestLine.split(" ");
            String method = parts[0];           // GET
            String path   = parts[1];           // /index.html  o  /img/foto.jpg

            // 3. Resolver el archivo en disco
            if (path.equals("/")) path = "/index.html"; // default
            File file = new File(ROOT_DIR + path);
            System.out.println("[FILE] Buscando: " + file.getAbsolutePath());
            System.out.println("[FILE] Existe: " + file.exists());
            // 4. Enviar respuesta
            if (!file.exists() || file.isDirectory()) {
                send404(out);
            } else {
                sendFile(out, file);
            }

        } catch (IOException e) {
            System.err.println("[ERROR] " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    // ── Enviar archivo (texto o imagen) ──────────────────────────────────────
    private void sendFile(OutputStream out, File file) throws IOException {
        byte[] body = Files.readAllBytes(file.toPath());
        String contentType = getContentType(file.getName());

        String headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: "   + contentType + "\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        out.write(headers.getBytes());
        out.write(body);          // funciona para texto E imágenes (bytes crudos)
        out.flush();
    }

    // ── 404 ──────────────────────────────────────────────────────────────────
    private void send404(OutputStream out) throws IOException {
        String body    = "<h1>404 Not Found</h1>";
        String headers = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(headers.getBytes());
        out.write(body.getBytes());
        out.flush();
    }

    // ── Detectar tipo de contenido por extensión ──────────────────────────────
    private String getContentType(String filename) {
        if (filename.endsWith(".html")) return "text/html";
        if (filename.endsWith(".css"))  return "text/css";
        if (filename.endsWith(".js"))   return "application/javascript";
        if (filename.endsWith(".png"))  return "image/png";
        if (filename.endsWith(".jpg") ||
                filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".gif"))  return "image/gif";
        if (filename.endsWith(".ico"))  return "image/x-icon";
        if (filename.endsWith(".txt"))  return "text/plain";
        return "application/octet-stream"; // fallback binario
    }
}