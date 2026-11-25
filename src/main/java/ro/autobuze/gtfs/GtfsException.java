package ro.autobuze.gtfs;

/** Feed-ul GTFS nu se poate importa: lipsește un fișier sau o coloană, sau o valoare e greșită. */
public class GtfsException extends RuntimeException {

    public GtfsException(String message) {
        super(message);
    }

    public GtfsException(String message, Throwable cause) {
        super(message, cause);
    }
}
