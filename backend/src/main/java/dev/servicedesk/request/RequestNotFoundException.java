package dev.servicedesk.request;

import java.util.UUID;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(UUID id) { super("Request " + id + " was not found."); }
}
