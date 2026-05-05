/**
 * ResponseData — Objeto inmutable que agrupa la respuesta HTTP a enviar.
 * Lo produce WorkerThread y lo consume SendWorker.
 */
public record ResponseData(
        int statusCode,
        String statusText,
        String contentType,
        byte[] body
) {}
