package com.yatrasetu.tourism.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;

/**
 * Shared HTTP client for talking to the free, keyless OpenStreetMap services
 * (Nominatim, OSRM, Overpass) used by {@link PlacesService} and
 * {@link RouteService}.
 *
 * These are all public, best-effort community servers, so a single failed
 * request is normal and not necessarily a sign anything is broken. This
 * class exists to absorb that flakiness instead of surfacing a raw
 * "http connect failed" style exception straight to the user:
 *
 *   - follows redirects (some of these hosts issue one, and Java's
 *     HttpClient does NOT follow redirects by default — a request that
 *     "should" work can otherwise die with a connect-looking failure)
 *   - retries transient connection/timeout failures a couple of times
 *     with a short backoff before giving up
 *   - can fail over across a list of mirror base URLs (used for Overpass,
 *     which has several independent public mirrors)
 *   - turns the low-level exception into a clear, honest message so the
 *     real cause (DNS failure vs. connection refused vs. timeout) is easy
 *     to diagnose instead of a bare "http connect failed"
 */
@Component
public class ExternalApiClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiClient.class);

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration RETRY_BACKOFF = Duration.ofMillis(600);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            // Java's HttpClient does NOT follow redirects by default. Some
            // of these public endpoints (especially behind CDNs) can 30x —
            // without this, that shows up as a confusing connection failure.
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Sends a single request, retrying transient network failures.
     * Throws {@link ExternalApiException} with a clear, honest message if
     * every attempt fails.
     */
    public HttpResponse<String> send(HttpRequest.Builder requestBuilder, String what) {
        Exception last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            } catch (ConnectException | UnknownHostException | HttpTimeoutException e) {
                last = e;
                log.warn("{} attempt {}/{} failed: {}", what, attempt, MAX_ATTEMPTS, describe(e));
            } catch (IOException e) {
                last = e;
                log.warn("{} attempt {}/{} failed: {}", what, attempt, MAX_ATTEMPTS, describe(e));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalApiException(what + " was interrupted.", e);
            }
            if (attempt < MAX_ATTEMPTS) sleep(RETRY_BACKOFF.multipliedBy(attempt));
        }
        throw new ExternalApiException(diagnosticMessage(what, last), last);
    }

    /**
     * Same as {@link #send}, but tries each base URL in order (e.g. several
     * Overpass mirrors) before giving up, so one mirror being down or
     * rate-limiting us doesn't take the whole feature down.
     */
    public HttpResponse<String> sendWithFallback(List<String> baseUrls, RequestFactory factory, String what) {
        ExternalApiException last = null;
        for (String baseUrl : baseUrls) {
            try {
                return send(factory.build(baseUrl), what);
            } catch (ExternalApiException e) {
                last = e;
                log.warn("{} failed against {}, trying next mirror if any", what, baseUrl);
            }
        }
        // Every mirror failed the same way (usually outbound connectivity) —
        // the last mirror's message already explains why in plain terms.
        throw new ExternalApiException(what + " failed on every available mirror. " + last.getMessage(), last.getCause());
    }

    private void sleep(Duration d) {
        try {
            Thread.sleep(d.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String describe(Exception e) {
        return e.getClass().getSimpleName() + (e.getMessage() != null ? " — " + e.getMessage() : "");
    }

    /**
     * Turns the raw exception into a message that actually tells you what
     * to check, instead of just repeating the Java exception name.
     */
    private String diagnosticMessage(String what, Exception cause) {
        if (cause instanceof UnknownHostException) {
            return what + " failed: couldn't resolve the host (DNS lookup failed). "
                    + "This server likely has no outbound internet/DNS access — check its network/firewall settings.";
        }
        if (cause instanceof ConnectException) {
            return what + " failed: the connection was refused or blocked before it could be established. "
                    + "This usually means the server this backend runs on cannot make outbound HTTPS "
                    + "connections (corporate/campus firewall, or a proxy is required but not configured).";
        }
        if (cause instanceof HttpTimeoutException) {
            return what + " failed: the request timed out waiting for a response. "
                    + "The public OSM service may be slow/overloaded right now, or outbound traffic is being "
                    + "silently dropped by a firewall.";
        }
        String detail = cause != null ? describe(cause) : "unknown error";
        return what + " failed after " + MAX_ATTEMPTS + " attempts (" + detail + "). "
                + "Please try again in a moment.";
    }

    @FunctionalInterface
    public interface RequestFactory {
        HttpRequest.Builder build(String baseUrl);
    }

    public static final class ExternalApiException extends RuntimeException {
        public ExternalApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** Convenience for building a GET request with the standard User-Agent this project uses. */
    public static HttpRequest.Builder get(URI uri, Duration timeout) {
        return HttpRequest.newBuilder(uri)
                .header("User-Agent", "YatraSetu-CollegeProject/1.0 (student demo)")
                .timeout(timeout)
                .GET();
    }
}
