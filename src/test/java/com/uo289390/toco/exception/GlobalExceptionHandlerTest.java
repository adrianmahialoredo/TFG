package com.uo289390.toco.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.uo289390.toco.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Tests for {@link GlobalExceptionHandler}.
 * Each handler is exercised both directly and through a MockMvc pipeline backed by a
 * test-only controller that throws the corresponding exception, checking the HTTP status
 * and the JSON error body (timestamp, status, error, message and validation errors).
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockMvc mockMvc;

    /**
     * Request body used to trigger bean validation failures.
     */
    static class SampleRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @Min(value = 1, message = "Quantity must be positive")
        private int quantity;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    /**
     * Test-only controller that throws the exceptions handled by the advice.
     */
    @RestController
    @RequestMapping("/test")
    static class ThrowingController {

        @GetMapping("/bad-credentials")
        String badCredentials() {
            throw new BadCredentialsException("Invalid credentials");
        }

        @GetMapping("/illegal-argument")
        String illegalArgument() {
            throw new IllegalArgumentException("Bad argument");
        }

        @GetMapping("/not-found")
        String notFound() {
            throw new EntityNotFoundException("Entity 5 not found");
        }

        @GetMapping("/unexpected")
        String unexpected() {
            throw new IllegalStateException("secret internal detail");
        }

        @GetMapping("/checked")
        String checked() throws Exception {
            throw new Exception("checked failure");
        }

        @GetMapping("/conflict")
        String conflict() {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already exists");
        }

        @GetMapping("/teapot")
        String teapot() {
            throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT);
        }

        @GetMapping("/param")
        String param(@RequestParam int limit) {
            return "limit=" + limit;
        }

        @GetMapping("/items/{id}")
        String item(@PathVariable Long id) {
            return "item=" + id;
        }

        @PostMapping("/validate")
        String validate(@Valid @RequestBody SampleRequest request) {
            return "ok";
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- direct handler invocations ----------

    @Test
    void handleBadCredentials_buildsUnauthorizedBody() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleBadCredentials(new BadCredentialsException("Invalid credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(401, body.get("status"));
        assertEquals("Unauthorized", body.get("error"));
        assertEquals("Invalid credentials", body.get("message"));
        assertNotNull(Instant.parse((String) body.get("timestamp")));
    }

    @Test
    void handleAccessDenied_buildsForbiddenBodyWithoutInternalDetail() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleAccessDenied(new AccessDeniedException("Access Denied by expression XYZ"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().get("status"));
        assertEquals("Forbidden", response.getBody().get("error"));
        assertEquals("Access denied", response.getBody().get("message"));
    }

    @Test
    void handleIllegalArgument_buildsBadRequestBody() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Bad argument"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad argument", response.getBody().get("message"));
        assertEquals("Bad Request", response.getBody().get("error"));
    }

    @Test
    void handleEntityNotFound_buildsNotFoundBody() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleEntityNotFound(new EntityNotFoundException("Missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Missing", response.getBody().get("message"));
    }

    @Test
    void responseBody_keepsFieldOrder() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("x"));

        assertEquals(List.of("timestamp", "status", "error", "message"),
                List.copyOf(response.getBody().keySet()));
    }

    @Test
    void handleValidation_collectsFieldErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new SampleRequest(), "request");
        bindingResult.addError(new FieldError("request", "name", "Name is required"));
        bindingResult.addError(new FieldError("request", "quantity", "Quantity must be positive"));
        MethodParameter parameter = new MethodParameter(
                ThrowingController.class.getDeclaredMethod("validate", SampleRequest.class), 0);

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidation(new MethodArgumentNotValidException(parameter, bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertEquals("Validation failed", body.get("message"));
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals("Name is required", errors.get("name"));
        assertEquals("Quantity must be positive", errors.get("quantity"));
        assertEquals(List.of("name", "quantity"), List.copyOf(errors.keySet()));
    }

    @Test
    void handleValidation_withoutFieldErrors_returnsEmptyErrorsMap() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new SampleRequest(), "request");
        MethodParameter parameter = new MethodParameter(
                ThrowingController.class.getDeclaredMethod("validate", SampleRequest.class), 0);

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidation(new MethodArgumentNotValidException(parameter, bindingResult));

        assertTrue(((Map<?, ?>) response.getBody().get("errors")).isEmpty());
    }

    @Test
    void handleGeneric_plainException_hidesMessageBehind500() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new RuntimeException("database password leaked"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().get("message"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
    }

    @Test
    void handleGeneric_errorResponseException_keepsItsStatus() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new HttpRequestMethodNotSupportedException("PATCH"));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(405, response.getBody().get("status"));
    }

    @Test
    void handleGeneric_responseStatusException_keepsItsStatus() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new ResponseStatusException(HttpStatus.CONFLICT, "Already exists"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(((String) response.getBody().get("message")).contains("Already exists"));
    }

    // ---------- through the MVC pipeline ----------

    @Test
    void badCredentials_returns401() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void illegalArgument_returns400() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad argument"));
    }

    @Test
    void entityNotFound_returns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Entity 5 not found"));
    }

    @Test
    void unexpectedRuntimeException_returns500WithoutLeakingDetails() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    void checkedException_returns500() throws Exception {
        mockMvc.perform(get("/test/checked"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    void responseStatusException_returnsItsStatus() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("Already exists")));
    }

    @Test
    void responseStatusExceptionWithoutReason_returnsItsStatus() throws Exception {
        mockMvc.perform(get("/test/teapot"))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.status").value(418));
    }

    @Test
    void missingRequestParameter_returns400() throws Exception {
        mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("limit")));
    }

    @Test
    void unsupportedMethod_returns405() throws Exception {
        mockMvc.perform(delete("/test/illegal-argument"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    @Test
    void unsupportedMediaType_returns415() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void invalidBody_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").value("Name is required"))
                .andExpect(jsonPath("$.errors.quantity").value("Quantity must be positive"));
    }

    @Test
    void validBody_passesThrough() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ok\",\"quantity\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void malformedJsonBody_returns400() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed or missing request body"));
    }

    @Test
    void missingBody_returns400() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or missing request body"));
    }

    @Test
    void bodyFieldWithWrongType_returns400() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ok\",\"quantity\":\"many\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or missing request body"));
    }

    @Test
    void nonNumericPathVariable_returns400NamingTheParameter() throws Exception {
        mockMvc.perform(get("/test/items/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }

    @Test
    void nonNumericRequestParam_returns400NamingTheParameter() throws Exception {
        mockMvc.perform(get("/test/param").param("limit", "ten"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'limit'"));
    }
}
