package at.technikum.mrp.util;

/**
 * Eigene Exception für API-Fehler mit HTTP-Statuscode.
 * Controller fängt das ab und gibt saubere JSON-Fehlermeldungen zurück (statt Stacktrace).
 */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * HTTP Statuscode, der an den Client zurückgegeben werden soll.
     */
    public int getStatus() {
        return status;
    }

    // Convenience-Methoden, damit man im Code kurz und sauber bleibt
    public static ApiException badRequest(String msg) { return new ApiException(400, msg); }
    public static ApiException unauthorized(String msg) { return new ApiException(401, msg); }
    public static ApiException forbidden(String msg) { return new ApiException(403, msg); }
    public static ApiException notFound(String msg) { return new ApiException(404, msg); }
    public static ApiException conflict(String msg) { return new ApiException(409, msg); }
}
