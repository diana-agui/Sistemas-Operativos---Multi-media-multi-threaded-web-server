import java.net.*;
import java.io.*;

public class SocketHandler {

    private final ThreadPool    pool;
    private final SessionStore  sessions;

    public SocketHandler(int threadCount, int queueCapacity) {
        this.pool     = new ThreadPool(threadCount, queueCapacity);
        this.sessions = new SessionStore();
    }

    public void start() throws Exception {
        // listenfd — solo escucha, nunca lee ni escribe datos
        ServerSocket listenfd = new ServerSocket(8080);
        System.out.println("Servidor listo en http://localhost:8080");

        while (true) {
            // Padre acepta → connfd es la conexión con el cliente
            Socket connfd = listenfd.accept();
            connfd.setSoTimeout(5000);

            // Encolar el job; el thread hijo se encarga de connfd
            boolean accepted = pool.submit(new RequestJob(connfd, sessions));
            if (!accepted) {
                // Cola llena — rechazar y cerrar connfd en el padre
                System.err.println("[Server] Cola llena, rechazando conexión.");
                connfd.close();
            }
            // Padre ya no usa connfd — el hijo es el dueño desde aquí
        }
    }

    // Punto de entrada si corres SocketHandler directamente
    public static void main(String[] args) throws Exception {
        new SocketHandler(4, 10).start();
    }
}
