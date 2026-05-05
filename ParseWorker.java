import java.util.*;

/**
 * ParseWorker
 * Responsabilidad: interpretar el request HTTP crudo y extraer:
 *   - Método (GET, POST, ...)
 *   - Ruta (/index.html, /api/data, ...)
 *   - Versión HTTP
 *   - Headers como mapa clave→valor
 *   - Body (si existe)
 */
public class ParseWorker {

    private final String rawRequest;

    public ParseWorker(String rawRequest) {
        this.rawRequest = rawRequest;
    }

    /**
     * Parsea el request crudo y retorna un HttpRequest inmutable.
     */
    public HttpRequest parse() {
        if (rawRequest == null || rawRequest.isBlank()) {
            return new HttpRequest("GET", "/", "HTTP/1.1", Map.of(), "");
        }

        String[] lines = rawRequest.split("\r\n");

        // ── Request Line ──────────────────────────────────────
        // Ej: "GET /index.html HTTP/1.1"
        String[] requestLine = lines[0].split(" ");
        String method  = requestLine.length > 0 ? requestLine[0] : "GET";
        String path    = requestLine.length > 1 ? requestLine[1] : "/";
        String version = requestLine.length > 2 ? requestLine[2] : "HTTP/1.1";

        // Normalizar path (quitar query string)
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }

        // ── Headers ───────────────────────────────────────────
        Map<String, String> headers = new LinkedHashMap<>();
        int bodyStart = 1;

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                bodyStart = i + 1; // siguiente línea tras línea vacía = body
                break;
            }
            int colon = lines[i].indexOf(":");
            if (colon > 0) {
                String key   = lines[i].substring(0, colon).trim().toLowerCase();
                String value = lines[i].substring(colon + 1).trim();
                headers.put(key, value);
            }
        }

        // ── Body ──────────────────────────────────────────────
        StringBuilder body = new StringBuilder();
        for (int i = bodyStart; i < lines.length; i++) {
            body.append(lines[i]);
        }

        return new HttpRequest(method, path, version, headers, body.toString());
    }
}
