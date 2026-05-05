public class Main {

    public static void main(String[] args) throws Exception {
        int threads = Runtime.getRuntime().availableProcessors();
        int queueCapacity = threads * 5;

        System.out.println("=== Multi-threaded Web Server ===");
        System.out.printf("Puerto: 8080 | Hilos: %d | Cola: %d%n%n", threads, queueCapacity);

        SocketHandler handler = new SocketHandler(threads, queueCapacity);
        handler.start();
    }
}
