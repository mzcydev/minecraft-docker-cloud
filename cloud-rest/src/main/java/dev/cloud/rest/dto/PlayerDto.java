package dev.cloud.rest.dto;

/**
 * Data Transfer Object for online player responses.
 */
public record PlayerDto(
        String uuid,
        String name,
        String currentService
) {}