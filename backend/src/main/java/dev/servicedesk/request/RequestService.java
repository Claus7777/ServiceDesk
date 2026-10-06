package dev.servicedesk.request;

import java.time.Clock;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RequestService {
    private final RequestRepository repository;
    private final Clock clock;

    public RequestService(RequestRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public RequestResponse create(CreateRequest input) {
        return RequestResponse.from(repository.save(new ServiceRequest(
                input.title().strip(), input.description().strip(), input.priority(), clock.instant())));
    }

    public RequestPage list(RequestStatus status, String search, int page, int size) {
        Specification<ServiceRequest> specification = (root, query, cb) -> cb.conjunction();
        if (status != null) specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (search != null && !search.isBlank()) {
            String escaped = search.strip().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            specification = specification.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + escaped + "%", '\\'));
        }
        var result = repository.findAll(specification, PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        return new RequestPage(result.getContent().stream().map(RequestResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public RequestResponse get(UUID id) { return RequestResponse.from(find(id)); }

    @Transactional
    public RequestResponse updateStatus(UUID id, RequestStatus status) {
        var request = find(id);
        if (request.getStatus() != status) request.changeStatus(status, clock.instant());
        return RequestResponse.from(request);
    }

    private ServiceRequest find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }
}
