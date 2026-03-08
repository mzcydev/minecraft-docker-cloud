package dev.cloud.rest.middleware;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * Javalin before-handler that adds CORS headers to every response.
 * Allows the REST API to be called from browser-based dashboards.
 */
public class CorsMiddleware implements Handler {

    private final String allowedOrigin;

    public CorsMiddleware(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        ctx.header("Access-Control-Allow-Origin", allowedOrigin);
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Authorization, Content-Type");

        if (ctx.method().name().equals("OPTIONS")) {
            ctx.status(204).result("");
        }
    }
}