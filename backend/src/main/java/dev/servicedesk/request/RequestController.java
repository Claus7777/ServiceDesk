package dev.servicedesk.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final RequestService service;

    public RequestController(RequestService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<RequestResponse> create(@Valid @RequestBody CreateRequest input) {
        var response = service.create(input);
        return ResponseEntity.created(URI.create("/api/requests/" + response.id())).body(response);
    }

    @GetMapping
    public RequestPage list(@RequestParam(required = false) RequestStatus status,
                            @RequestParam(required = false) @Size(max = 120) String search,
                            @RequestParam(defaultValue = "0") @Min(0) int page,
                            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return service.list(status, search, page, size);
    }

    @GetMapping("/{id}")
    public RequestResponse get(@PathVariable UUID id) { return service.get(id); }

    @PatchMapping("/{id}/status")
    public RequestResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatus input) {
        return service.updateStatus(id, input.status());
    }
}
