package dev.cloud.rest;

import dev.cloud.master.MasterCloudAPI;
import dev.cloud.rest.controller.*;
import dev.cloud.rest.middleware.AuthMiddleware;
import dev.cloud.rest.middleware.CorsMiddleware;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstraps the Javalin REST API server.
 * Registers all middleware, controllers and routes.
 */
public class RestServer {

    private static final Logger log = LoggerFactory.getLogger(RestServer.class);

    private final RestConfig config;
    private final MasterCloudAPI cloudAPI;
    private Javalin app;

    public RestServer(RestConfig config, MasterCloudAPI cloudAPI) {
        this.config   = config;
        this.cloudAPI = cloudAPI;
    }

    /**
     * Starts the REST server and registers all routes.
     */
    public void start() {
        GroupController   groupController   = new GroupController(cloudAPI);
        NodeController    nodeController    = new NodeController(cloudAPI);
        ServiceController serviceController = new ServiceController(cloudAPI);
        PlayerController  playerController  = new PlayerController(cloudAPI);

        app = Javalin.create(cfg -> {
            cfg.showJavalinBanner = false;
            cfg.jsonMapper(new io.javalin.json.JavalinJackson());
        });

        // Middleware
        if (config.corsEnabled()) {
            app.before(new CorsMiddleware(config.corsOrigin()));
        }
        app.before("/api/*", new AuthMiddleware(config.authToken()));

        // Routes
        app.routes(() -> ApiBuilder.path("/api", () -> {
            groupController.registerRoutes();
            nodeController.registerRoutes();
            serviceController.registerRoutes();
            playerController.registerRoutes();
        }));

        // Global exception handlers
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unhandled exception in REST handler: {}", e.getMessage(), e);
            ctx.status(500).json(new ErrorResponse("Internal server error."));
        });

        app.start(config.port());
        log.info("REST API started on port {}.", config.port());
    }

    /**
     * Stops the REST server.
     */
    public void stop() {
        if (app != null) {
            app.stop();
            log.info("REST API stopped.");
        }
    }

    /**
     * Simple error response body.
     */
    public record ErrorResponse(String message) {}
}