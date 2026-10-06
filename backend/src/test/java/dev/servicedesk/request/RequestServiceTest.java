package dev.servicedesk.request;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {
    @Mock RequestRepository repository;
    private RequestService service;
    private final Instant now = Instant.parse("2026-10-06T12:00:00Z");

    @BeforeEach
    void setUp() { service = new RequestService(repository, Clock.fixed(now, ZoneOffset.UTC)); }

    @Test
    void createsTrimmedRequestWithNewStatusAndUtcTimestamps() {
        when(repository.save(any(ServiceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.create(new CreateRequest("  Broken printer  ", "  Paper jam  ", Priority.HIGH));
        assertThat(result.id()).isNotNull();
        assertThat(result.title()).isEqualTo("Broken printer");
        assertThat(result.description()).isEqualTo("Paper jam");
        assertThat(result.priority()).isEqualTo(Priority.HIGH);
        assertThat(result.status()).isEqualTo(RequestStatus.NEW);
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
        verify(repository).save(any(ServiceRequest.class));
    }

    @Test
    void missingRequestIsReported() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(id)).isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    void changesStatusAndPreservesCreationTime() {
        var request = new ServiceRequest("Printer", "Paper jam", Priority.MEDIUM, now.minusSeconds(60));
        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        var result = service.updateStatus(request.getId(), RequestStatus.IN_PROGRESS);
        assertThat(result.status()).isEqualTo(RequestStatus.IN_PROGRESS);
        assertThat(result.createdAt()).isEqualTo(now.minusSeconds(60));
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void repeatedStatusUpdateDoesNotChangeTimestamp() {
        var request = new ServiceRequest("Printer", "Paper jam", Priority.LOW, now.minusSeconds(60));
        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        assertThat(service.updateStatus(request.getId(), RequestStatus.NEW).updatedAt())
                .isEqualTo(now.minusSeconds(60));
    }

    @Test
    void cannotUpdateMissingRequest() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateStatus(id, RequestStatus.RESOLVED))
                .isInstanceOf(RequestNotFoundException.class);
    }
}
