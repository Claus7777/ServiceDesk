package dev.servicedesk.request;

import jakarta.validation.constraints.NotNull;

public record UpdateStatus(@NotNull RequestStatus status) {}
