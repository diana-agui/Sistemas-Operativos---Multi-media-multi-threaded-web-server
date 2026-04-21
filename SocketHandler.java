import java.net.*;

public class SocketHandler {
    
    void main() throws Exception {
        ServerSocket server = new ServerSocket(8080);
        ThreadPool pool = new ThreadPool(4, 10);
        
        while (true) {
            Socket connection = server.accept();
            pool.submit(() -> {
                try {
                    connection.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\ndaoms world".getBytes());
                    connection.close();
                } catch (Exception e) {
                    System.err.println("error x_X " );
                }
            });
        }
    }
}
