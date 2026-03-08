package dev.cloud.networking.interceptor;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC interceptor that enforces token-based authentication on every call.
 *
 * <p>On the <b>server side</b> (master): validates the {@code Authorization} header
 * against the configured shared secret and rejects calls with {@code UNAUTHENTICATED}
 * if the token is missing or wrong.
 *
 * <p>On the <b>client side</b> (node/plugin): attaches the token to every outgoing call.
 */
public class AuthInterceptor implements ServerInterceptor, ClientInterceptor {

    /**
     * The metadata key used to carry the auth token.
     */
    public static final Metadata.Key<String> AUTH_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private final String expectedToken;

    /**
     * @param token the shared secret. On the server side this is the value to validate against;
     *              on the client side this is the value to attach to outgoing calls.
     */
    public AuthInterceptor(String token) {
        this.expectedToken = "Bearer " + token;
    }

    // ── Server side ──────────────────────────────────────────────────────────

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(
            ServerCall<Q, R> call,
            Metadata headers,
            ServerCallHandler<Q, R> next
    ) {
        String token = headers.get(AUTH_KEY);

        if (!expectedToken.equals(token)) {
            log.warn("Rejected unauthenticated call to {}", call.getMethodDescriptor().getFullMethodName());
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid or missing auth token"), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }

        return next.startCall(call, headers);
    }

    // ── Client side ──────────────────────────────────────────────────────────

    @Override
    public <Q, R> ClientCall<Q, R> interceptCall(
            MethodDescriptor<Q, R> method,
            CallOptions callOptions,
            Channel next
    ) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<R> responseListener, Metadata headers) {
                headers.put(AUTH_KEY, expectedToken);
                super.start(responseListener, headers);
            }
        };
    }
}