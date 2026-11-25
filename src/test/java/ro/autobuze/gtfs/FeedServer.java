package ro.autobuze.gtfs;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Server HTTP local care imită adresa Tursib: {@code /gtfs} redirecționează (301) spre
 * {@code /files/tursib-mini.zip}, ca adresa oficială. Servește arhiva setată de test și poate
 * ține descărcarea în așteptare, ca testele să prindă importul în timp ce rulează.
 */
public final class FeedServer {

    private static FeedServer instance;

    private final HttpServer server;
    private volatile byte[] zip = MiniFeed.zip();
    private volatile CountDownLatch gate;

    private FeedServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/gtfs", exchange -> {
            exchange.getResponseHeaders().add("Location", fileUrl());
            exchange.sendResponseHeaders(301, -1);
            exchange.close();
        });
        server.createContext("/files/tursib-mini.zip", exchange -> {
            CountDownLatch waitFor = gate;
            try {
                if (waitFor != null && !waitFor.await(30, TimeUnit.SECONDS)) {
                    exchange.sendResponseHeaders(504, -1);
                    exchange.close();
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = zip;
            exchange.getResponseHeaders().add("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    /** Un singur server pentru toate testele din JVM. */
    public static synchronized FeedServer get() {
        if (instance == null) {
            try {
                instance = new FeedServer();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return instance;
    }

    /** Adresa configurată în aplicație (cea cu redirect). */
    public String feedUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/gtfs";
    }

    /** Adresa finală, după redirect: devine {@code source_url}. */
    public String fileUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/files/tursib-mini.zip";
    }

    public void serve(byte[] zip) {
        this.zip = zip;
    }

    /** Descărcarea așteaptă până la {@link #release()}. */
    public void hold() {
        gate = new CountDownLatch(1);
    }

    public void release() {
        CountDownLatch g = gate;
        gate = null;
        if (g != null) {
            g.countDown();
        }
    }
}
