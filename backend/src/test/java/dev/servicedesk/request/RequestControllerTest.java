package dev.servicedesk.request;

import dev.servicedesk.config.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {
    @Mock RequestService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new RequestController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void creationReturns201AndLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.create(any())).thenReturn(new RequestResponse(id, "Printer", "Paper jam",
                Priority.HIGH, RequestStatus.NEW, Instant.EPOCH, Instant.EPOCH));
        mvc.perform(post("/api/requests").contentType("application/json")
                .content("""
                {"title":"Printer","description":"Paper jam","priority":"HIGH"}"""))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/requests/" + id))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void rejectsBlankTitleWithoutCallingService() throws Exception {
        mvc.perform(post("/api/requests").contentType("application/json")
                .content("""
                {"title":" ","description":"Paper jam","priority":"HIGH"}"""))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsUnknownPriority() throws Exception {
        mvc.perform(post("/api/requests").contentType("application/json")
                .content("""
                {"title":"Printer","description":"Paper jam","priority":"URGENT"}"""))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void missingIdReturnsProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenThrow(new RequestNotFoundException(id));
        mvc.perform(get("/api/requests/" + id)).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void forwardsFiltersAndPagination() throws Exception {
        when(service.list(RequestStatus.NEW, "printer", 0, 10))
                .thenReturn(new RequestPage(List.of(), 0, 10, 0, 0));
        mvc.perform(get("/api/requests").param("status", "NEW").param("search", "printer"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void rejectsNullStatus() throws Exception {
        mvc.perform(patch("/api/requests/" + UUID.randomUUID() + "/status")
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
