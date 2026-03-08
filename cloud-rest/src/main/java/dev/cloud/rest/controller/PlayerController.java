package dev.cloud.rest.controller;

import dev.cloud.master.MasterCloudAPI;
import dev.cloud.rest.dto.PlayerDto;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.util.List;

/**
 * REST controller for online player information.
 *
 * <pre>
 * GET /api/players         — list all online players
 * GET /api/players/:name   — get a player by name
 * </pre>
 */
public class PlayerController {

    private final MasterCloudAPI api;

    public PlayerController(MasterCloudAPI api) {
        this.api = api;
    }

    public void registerRoutes() {
        ApiBuilder.path("/players", () -> {
            ApiBuilder.get(this::listPlayers);
            ApiBuilder.path("/{name}", () ->
                    ApiBuilder.get(this::getPlayer)
            );
        });
    }

    private void listPlayers(Context ctx) {
        List<PlayerDto> dtos = api.playerRegistry().allPlayers().stream()
                .map(p -> new PlayerDto(
                        p.getUuid().toString(),
                        p.getName(),
                        p.getCurrentService()))
                .toList();
        ctx.json(dtos);
    }

    private void getPlayer(Context ctx) {
        String name = ctx.pathParam("name");
        api.playerRegistry().findByName(name)
                .map(p -> new PlayerDto(
                        p.getUuid().toString(),
                        p.getName(),
                        p.getCurrentService()))
                .ifPresentOrElse(ctx::json,
                        () -> {
                            throw new NotFoundResponse("Player not found: " + name);
                        });
    }
}