import java.net.*;

public class SocketHandler {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(8080);
        ThreadPool pool = new ThreadPool(4, 10);

        while (true) {
            Socket connection = server.accept();
            pool.submit(() -> {
                try {
                    connection.getOutputStream().write(
                        "HTTP/1.1 200 OK\r\nContent-Length: 5\r\n\r\nhello"
                        .getBytes()
                    );
                    connection.close();
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            });
        }
    }
}
