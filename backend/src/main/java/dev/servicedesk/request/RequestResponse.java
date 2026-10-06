package dev.servicedesk.request;

import java.time.Instant;
import java.util.UUID;

public record RequestResponse(UUID id, String title, String description, Priority priority,
                              RequestStatus status, Instant createdAt, Instant updatedAt) {
    public static RequestResponse from(ServiceRequest request) {
        return new RequestResponse(request.getId(), request.getTitle(), request.getDescription(),
                request.getPriority(), request.getStatus(), request.getCreatedAt(), request.getUpdatedAt());
    }
}
