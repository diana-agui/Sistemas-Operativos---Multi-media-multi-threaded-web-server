import java.util.*;
import java.util.concurrent.*;

/**
 * SessionStore — guarda el estado del cliente en el servidor.
 *
 * Nota del profesor: "guardar conocimiento de peticiones previas
 * del cliente del lado del servidor porque en cuanto se entrega
 * la data al cliente se muere la conexión."
 *
 * Como HTTP es sin estado (cada conexión muere al terminar),
 * el servidor identifica al cliente por una cookie de sesión
 * y recuerda qué páginas ha visitado.
 *
 * Estructura: sessionId → Session (ip, historial de rutas, timestamp)
 */
public class SessionStore {

    // ConcurrentHashMap: acceso seguro desde múltiples threads sin lock manual
    private final ConcurrentHashMap<String, Session> store = new ConcurrentHashMap<>();

    public SessionStore() {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "session-cleaner");
            t.setDaemon(true);   // ← daemon so it doesn't block JVM shutdown
            return t;
        });
        cleaner.scheduleAtFixedRate(() ->
                        store.entrySet().removeIf(e ->
                                System.currentTimeMillis() - e.getValue().lastSeen > 30 * 60 * 1000L),
                5, 5, TimeUnit.MINUTES);
    }


    /**
     * Retorna el sessionId existente (de la cookie) o crea uno nuevo.
     * @param cookieHeader valor del header "Cookie" del request
     * @param clientIp     IP del cliente (para nuevas sesiones)
     */
    public String getOrCreate(String cookieHeader, String clientIp) {
        // Buscar sessionId en la cookie: "sessionId=abc123"
        String sessionId = extractSessionId(cookieHeader);

        if (sessionId != null && store.containsKey(sessionId)) {
            store.get(sessionId).lastSeen = System.currentTimeMillis();
            return sessionId; // sesión existente
        }

        // Nueva sesión
        sessionId = UUID.randomUUID().toString().substring(0, 8);
        store.put(sessionId, new Session(clientIp));
        System.out.printf("[Session] Nueva sesión: %s para %s%n", sessionId, clientIp);
        return sessionId;
    }

    /** Registra la ruta visitada en el historial de la sesión. */
    public void recordRequest(String sessionId, String path) {
        Session s = store.get(sessionId);
        if (s != null) s.history.add(path);
    }

    /** Retorna el historial de rutas visitadas por una sesión. */
    public List<String> getHistory(String sessionId) {
        Session s = store.get(sessionId);
        return s != null ? Collections.unmodifiableList(s.history) : List.of();
    }

    /**
     * Genera el header Set-Cookie para enviar al cliente.
     * El navegador lo guarda y lo manda en requests siguientes.
     */
    public String setCookieHeader(String sessionId) {
        return "Set-Cookie: sessionId=" + sessionId + "; Path=/; HttpOnly\r\n";
    }

    private String extractSessionId(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isBlank()) return null;
        for (String part : cookieHeader.split(";")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].trim().equals("sessionId")) {
                return kv[1].trim();
            }
        }
        return null;
    }

    // ── Clase interna Session ─────────────────────────────
    private static class Session {
        final String       clientIp;
        final List<String> history  = new ArrayList<>();
        long               lastSeen = System.currentTimeMillis();

        Session(String clientIp) { this.clientIp = clientIp; }
    }
}
