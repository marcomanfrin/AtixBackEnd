package marcomanfrin.atixbackend.security;

import jakarta.servlet.http.HttpServletRequest;
import marcomanfrin.atixbackend.exceptions.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Rate limit a finestra fissa per IP sugli endpoint pubblici (in memoria: un'istanza di backend).
@Component
public class PublicRateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public PublicRateLimiter(@Value("${public.rate-limit.max-requests:30}") int maxRequests,
                             @Value("${public.rate-limit.window-seconds:600}") long windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowSeconds * 1000;
    }

    public void check(String clientIp) {
        long now = System.currentTimeMillis();
        if (windows.size() > 10_000) {
            windows.values().removeIf(w -> now - w.start > windowMillis);
        }
        Window window = windows.compute(clientIp, (ip, w) -> (w == null || now - w.start > windowMillis) ? new Window(now) : w);
        if (window.count.incrementAndGet() > maxRequests) {
            throw new TooManyRequestsException();
        }
    }

    /**
     * IP del client. X-Real-IP (impostato da nginx) e' considerato solo se la richiesta arriva
     * da un indirizzo privato/loopback, cioe' dal proxy: chi chiama la porta 3001 direttamente non puo' falsificarlo.
     */
    public static String clientIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Real-IP");
        if (forwarded != null && !forwarded.isBlank() && isInternal(remote)) {
            return forwarded.trim();
        }
        return remote;
    }

    private static boolean isInternal(String address) {
        try {
            InetAddress inet = InetAddress.getByName(address); // remoteAddr e' sempre un IP letterale: nessun lookup DNS
            return inet.isLoopbackAddress() || inet.isSiteLocalAddress() || inet.isLinkLocalAddress();
        } catch (Exception e) {
            return false;
        }
    }

    private static final class Window {
        final long start;
        final AtomicInteger count = new AtomicInteger();

        Window(long start) {
            this.start = start;
        }
    }
}
