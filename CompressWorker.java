import java.io.*;
import java.util.zip.*;

/**
 * CompressWorker
 * Responsabilidad: comprimir el body de la respuesta con gzip.
 *
 * Solo se invoca si el cliente envía "Accept-Encoding: gzip".
 * Usa java.util.zip.GZIPOutputStream, disponible sin dependencias externas.
 *
 * En una Raspberry Pi 5 con recursos limitados, comprimir reduce
 * significativamente el ancho de banda a costo de CPU mínimo.
 */
public class CompressWorker {

    private final byte[] data;

    public CompressWorker(byte[] data) {
        this.data = data;
    }

    /**
     * Comprime los datos con gzip.
     * @return bytes comprimidos
     * @throws IOException si falla la compresión
     */
    public byte[] compress() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(data);
        } // flush + close automático del try-with-resources

        byte[] compressed = baos.toByteArray();

        // Log informativo (ratio de compresión)
        double ratio = data.length > 0
                ? (1.0 - (double) compressed.length / data.length) * 100
                : 0;
        System.out.printf("[CompressWorker] %d → %d bytes (%.1f%% reducción)%n",
                data.length, compressed.length, ratio);

        return compressed;
    }

    /**
     * Retorna los datos originales sin comprimir.
     * Útil para contenido binario o ya comprimido.
     */
    public byte[] raw() {
        return data;
    }
}
