import java.net.*;
import java.io.*;

/**
 * SocketHandler — Servidor principal.
 *
 * Implementa el patrón listenfd / connfd del profesor:
 *   - listenfd : socket del servidor padre, solo escucha nuevas conexiones
 *   - connfd   : socket de la conexión con el cliente, se pasa al thread hijo
 *
 * Flujo:
 *   1. Padre acepta conexión → obtiene connfd
 *   2. Padre encola un RequestJob que recibe connfd
 *   3. Padre cierra su referencia a connfd (ya no la necesita)
 *   4. El thread hijo procesa connfd (fetch → parse → compress → send)
 *   5. El thread hijo cierra connfd al terminar
 *
 * El cliente manda la parte del URL → servidor → thread.
 * El thread hace chdir al directorio www y sirve el archivo.
 */
public class SocketHandler {

    private final ThreadPool pool;
    private final SessionStore sessions; // guarda estado entre requests

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
}
