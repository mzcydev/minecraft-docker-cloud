package dev.cloud.rest.middleware;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Javalin before-handler that validates the {@code Authorization: Bearer <token>} header.
 * Rejects requests without a valid token with HTTP 401.
 */
public class AuthMiddleware implements Handler {

    private final String expectedToken;

    public AuthMiddleware(String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Missing Authorization header.");
        }
        String token = header.substring("Bearer ".length()).trim();
        if (!expectedToken.equals(token)) {
            throw new UnauthorizedResponse("Invalid token.");
        }
    }
}