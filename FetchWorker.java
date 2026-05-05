import java.io.*;
import java.net.*;

public class FetchWorker {

    private final Socket socket;

    public FetchWorker(Socket socket) {
        this.socket = socket;
    }

    public String fetch() throws IOException {
        InputStream in = socket.getInputStream();
        StringBuilder sb = new StringBuilder();
        // Ventana de 4 bytes para detectar \r\n\r\n (fin de headers HTTP)
        int[] w = {0, 0, 0, 0};
        int b;
        while ((b = in.read()) != -1) {
            sb.append((char) b);
            w[0] = w[1]; w[1] = w[2]; w[2] = w[3]; w[3] = b;
            if (w[0] == '\r' && w[1] == '\n' && w[2] == '\r' && w[3] == '\n') {
                break;
            }
        }
        return sb.toString();
    }

    public byte[] fetchBody(int contentLength) throws IOException {
        if (contentLength <= 0) return new byte[0];
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = in.read(buffer, totalRead, contentLength - totalRead);
            if (read == -1) break;
            totalRead += read;
        }
        return buffer;
    }
}
