package dev.servicedesk.request;

import java.util.List;

public record RequestPage(List<RequestResponse> content, int page, int size, long totalElements, int totalPages) {}
