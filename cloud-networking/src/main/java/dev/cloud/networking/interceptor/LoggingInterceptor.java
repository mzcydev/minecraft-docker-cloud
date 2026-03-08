package dev.cloud.networking.interceptor;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC interceptor that logs every incoming and outgoing RPC call at DEBUG level.
 * Logs the method name and final status code on completion.
 * Works on both server and client side.
 */
public class LoggingInterceptor implements ServerInterceptor, ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    // ── Server side ──────────────────────────────────────────────────────────

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(
            ServerCall<Q, R> call,
            Metadata headers,
            ServerCallHandler<Q, R> next
    ) {
        String method = call.getMethodDescriptor().getFullMethodName();
        log.debug("[gRPC SERVER] Incoming call: {}", method);

        ServerCall<Q, R> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                if (!status.isOk()) {
                    log.warn("[gRPC SERVER] Call {} failed with status: {}", method, status);
                } else {
                    log.debug("[gRPC SERVER] Call {} completed OK", method);
                }
                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }

    // ── Client side ──────────────────────────────────────────────────────────

    @Override
    public <Q, R> ClientCall<Q, R> interceptCall(
            MethodDescriptor<Q, R> method,
            CallOptions callOptions,
            Channel next
    ) {
        String methodName = method.getFullMethodName();
        log.debug("[gRPC CLIENT] Outgoing call: {}", methodName);

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<R> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (!status.isOk()) {
                            log.warn("[gRPC CLIENT] Call {} failed: {}", methodName, status);
                        } else {
                            log.debug("[gRPC CLIENT] Call {} completed OK", methodName);
                        }
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }
}