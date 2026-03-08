package dev.cloud.rest.controller;

import dev.cloud.master.MasterCloudAPI;
import dev.cloud.rest.dto.NodeDto;
import dev.cloud.rest.mapper.NodeMapper;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.util.List;

/**
 * REST controller for node status.
 *
 * <pre>
 * GET /api/nodes         — list all connected nodes
 * GET /api/nodes/:name   — get a node by name
 * </pre>
 */
public class NodeController {

    private final MasterCloudAPI api;
    private final NodeMapper mapper = new NodeMapper();

    public NodeController(MasterCloudAPI api) {
        this.api = api;
    }

    public void registerRoutes() {
        ApiBuilder.path("/nodes", () -> {
            ApiBuilder.get(this::listNodes);
            ApiBuilder.path("/{name}", () ->
                    ApiBuilder.get(this::getNode)
            );
        });
    }

    private void listNodes(Context ctx) {
        List<NodeDto> dtos = api.nodeRegistry().allNodes().stream()
                .map(mapper::toDto)
                .toList();
        ctx.json(dtos);
    }

    private void getNode(Context ctx) {
        String name = ctx.pathParam("name");
        api.nodeRegistry().findNode(name)
                .map(mapper::toDto)
                .ifPresentOrElse(ctx::json,
                        () -> { throw new NotFoundResponse("Node not found: " + name); });
    }
}