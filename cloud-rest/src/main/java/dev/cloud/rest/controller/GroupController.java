package dev.cloud.rest.controller;

import dev.cloud.master.MasterCloudAPI;
import dev.cloud.rest.dto.GroupDto;
import dev.cloud.rest.mapper.GroupMapper;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * REST controller for service group management.
 *
 * <pre>
 * GET    /api/groups          — list all groups
 * GET    /api/groups/:name    — get a group by name
 * POST   /api/groups          — create a group
 * PUT    /api/groups/:name    — update a group
 * DELETE /api/groups/:name    — delete a group
 * </pre>
 */
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    private final MasterCloudAPI api;
    private final GroupMapper mapper = new GroupMapper();

    public GroupController(MasterCloudAPI api) {
        this.api = api;
    }

    public void registerRoutes() {
        ApiBuilder.path("/groups", () -> {
            ApiBuilder.get(this::listGroups);
            ApiBuilder.post(this::createGroup);
            ApiBuilder.path("/{name}", () -> {
                ApiBuilder.get(this::getGroup);
                ApiBuilder.put(this::updateGroup);
                ApiBuilder.delete(this::deleteGroup);
            });
        });
    }

    private void listGroups(Context ctx) {
        List<GroupDto> dtos = api.groupManager().getAllGroups().stream()
                .map(mapper::toDto)
                .toList();
        ctx.json(dtos);
    }

    private void getGroup(Context ctx) {
        String name = ctx.pathParam("name");
        api.groupManager().getGroup(name)
                .map(mapper::toDto)
                .ifPresentOrElse(ctx::json,
                        () -> { throw new NotFoundResponse("Group not found: " + name); });
    }

    private void createGroup(Context ctx) {
        GroupDto dto = ctx.bodyAsClass(GroupDto.class);
        api.groupManager().createGroup(mapper.toDomain(dto));
        ctx.status(201).json(dto);
        log.info("REST: group '{}' created.", dto.name());
    }

    private void updateGroup(Context ctx) {
        GroupDto dto = ctx.bodyAsClass(GroupDto.class);
        api.groupManager().updateGroup(mapper.toDomain(dto));
        ctx.json(dto);
        log.info("REST: group '{}' updated.", dto.name());
    }

    private void deleteGroup(Context ctx) {
        String name = ctx.pathParam("name");
        api.groupManager().deleteGroup(name);
        ctx.status(204);
        log.info("REST: group '{}' deleted.", name);
    }
}