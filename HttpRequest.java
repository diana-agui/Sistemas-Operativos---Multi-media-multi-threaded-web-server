import java.util.Map;

/**
 * HttpRequest — Objeto inmutable que representa un request HTTP parseado.
 */
public record HttpRequest(
        String method,
        String path,
        String version,
        Map<String, String> headers,
        String body
) {
    /**
     * Indica si el cliente acepta compresión gzip.
     */
    public boolean acceptsGzip() {
        String ae = headers.getOrDefault("accept-encoding", "");
        return ae.toLowerCase().contains("gzip");
    }

    /**
     * Retorna el valor de un header (case-insensitive) o vacío.
     */
    public String header(String name) {
        return headers.getOrDefault(name.toLowerCase(), "");
    }
}
