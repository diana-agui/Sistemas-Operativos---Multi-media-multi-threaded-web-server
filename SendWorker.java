import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.Locale;

/**
 * SendWorker
 * Responsabilidad: construir y enviar la respuesta HTTP al cliente.
 *
 * Escribe:
 *   - Status line   (HTTP/1.1 200 OK)
 *   - Headers       (Content-Type, Content-Length, Content-Encoding, etc.)
 *   - Línea vacía
 *   - Body (bytes)
 */
public class SendWorker {

    private final Socket socket;
    private final byte[] body;
    private final boolean gzipEncoded;
    private final String sessionCookie;

    // Formateador para el header "Date" según RFC 7231
    private static final DateTimeFormatter HTTP_DATE =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
                    .withZone(ZoneOffset.UTC);

    public SendWorker(Socket socket, byte[] body, boolean gzipEncoded, String sessionCookie) {
        this.socket      = socket;
        this.body        = body;
        this.gzipEncoded = gzipEncoded;
        this.sessionCookie = sessionCookie;
    }

    /**
     * Envía la respuesta HTTP con el Content-Type dado.
     * Por defecto retorna 200 OK.
     */
    public void send(String contentType) throws IOException {
        send(200, "OK", contentType);
    }

    /**
     * Envía la respuesta HTTP con status personalizado.
     *
     * IMPORTANTE: usamos un solo BufferedOutputStream para todo (headers + body).
     * Mezclar PrintWriter con OutputStream sobre el mismo socket causaba corrupción
     * en binarios (imágenes) porque el PrintWriter tiene buffer interno propio.
     */
    public void send(int statusCode, String statusText, String contentType) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        // Construir headers y convertir a bytes ASCII puro
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");
        sb.append("Date: ").append(HTTP_DATE.format(Instant.now())).append("\r\n");
        sb.append("Server: RaspberryServer/1.0\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n");
        sb.append("Content-Length: ").append(body.length).append("\r\n");
        sb.append("Connection: close\r\n");
        if (gzipEncoded) {
            sb.append("Content-Encoding: gzip\r\n");
        }
        sb.append("Access-Control-Allow-Origin: *\r\n");
        if (sessionCookie != null) {
            sb.append(sessionCookie);    // already ends in \r\n from SessionStore.setCookieHeader()
        }
        sb.append("\r\n"); // línea vacía — fin de headers

        // Headers (ASCII) + body (puede ser binario) en un solo stream
        out.write(sb.toString().getBytes("US-ASCII"));
        out.write(body);
        out.flush();
    }
}
