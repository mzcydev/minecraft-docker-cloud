package dev.cloud.rest.controller;

import dev.cloud.master.MasterCloudAPI;
import dev.cloud.rest.dto.ServiceDto;
import dev.cloud.rest.mapper.ServiceMapper;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * REST controller for service management.
 *
 * <pre>
 * GET    /api/services              — list all services
 * GET    /api/services/:id          — get a service by ID
 * POST   /api/services/:group/start — start a service for a group
 * DELETE /api/services/:id          — stop a service
 * </pre>
 */
public class ServiceController {

    private static final Logger log = LoggerFactory.getLogger(ServiceController.class);

    private final MasterCloudAPI api;
    private final ServiceMapper mapper = new ServiceMapper();

    public ServiceController(MasterCloudAPI api) {
        this.api = api;
    }

    public void registerRoutes() {
        ApiBuilder.path("/services", () -> {
            ApiBuilder.get(this::listServices);
            ApiBuilder.path("/{id}", () -> {
                ApiBuilder.get(this::getService);
                ApiBuilder.delete(this::stopService);
            });
            ApiBuilder.path("/{group}/start", () ->
                    ApiBuilder.post(this::startService)
            );
        });
    }

    private void listServices(Context ctx) {
        List<ServiceDto> dtos = api.serviceRegistry().allServices().stream()
                .map(mapper::toDto)
                .toList();
        ctx.json(dtos);
    }

    private void getService(Context ctx) {
        String id = ctx.pathParam("id");
        api.serviceRegistry().findById(id)
                .map(mapper::toDto)
                .ifPresentOrElse(ctx::json,
                        () -> { throw new NotFoundResponse("Service not found: " + id); });
    }

    private void startService(Context ctx) {
        String groupName = ctx.pathParam("group");
        api.groupManager().getGroup(groupName).ifPresentOrElse(group -> {
            api.serviceManager().startService(group).ifPresentOrElse(
                    service -> {
                        ctx.status(201).json(mapper.toDto(
                                api.serviceRegistry().findById(
                                        service.getId().toString()).orElseThrow()));
                        log.info("REST: service started for group '{}'.", groupName);
                    },
                    () -> ctx.status(503).result("No node available.")
            );
        }, () -> { throw new NotFoundResponse("Group not found: " + groupName); });
    }

    private void stopService(Context ctx) {
        String id = ctx.pathParam("id");
        api.serviceRegistry().findById(id).ifPresentOrElse(service -> {
            api.serviceManager().stopService(id);
            ctx.status(204);
            log.info("REST: service '{}' stopped.", service.getName());
        }, () -> { throw new NotFoundResponse("Service not found: " + id); });
    }
}