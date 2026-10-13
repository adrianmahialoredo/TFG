package com.uo289390.toco.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.controller.InstrumentController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.dto.InstrumentDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.InstrumentService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link InstrumentController}.
 * Verifies routing, status codes, JSON serialization and request validation
 * of every instrument endpoint, with the service layer mocked.
 */
@WebMvcTest(InstrumentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class InstrumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InstrumentService instrumentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private InstrumentDTO instrument;

    @BeforeEach
    void setUp() {
        instrument = validRequest();
        instrument.setId(1L);
    }

    private InstrumentDTO validRequest() {
        InstrumentDTO dto = new InstrumentDTO();
        dto.setName("Guitar");
        dto.setImageUrl("https://example.com/guitar.png");
        return dto;
    }

    @Test
    void getAll_returnsAllInstruments() throws Exception {
        InstrumentDTO bass = new InstrumentDTO();
        bass.setId(2L);
        bass.setName("Bass");
        when(instrumentService.findAll()).thenReturn(List.of(instrument, bass));

        mockMvc.perform(get("/api/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Guitar"))
                .andExpect(jsonPath("$[0].imageUrl").value("https://example.com/guitar.png"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Bass"));
    }

    @Test
    void getAll_returnsEmptyList() throws Exception {
        when(instrumentService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getByIds_returnsRequestedInstruments() throws Exception {
        when(instrumentService.findByIds(List.of(1L))).thenReturn(List.of(instrument));

        mockMvc.perform(post("/api/instruments/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(instrumentService).findByIds(List.of(1L));
    }

    @Test
    void getById_whenFound_returnsInstrument() throws Exception {
        when(instrumentService.findById(1L)).thenReturn(Optional.of(instrument));

        mockMvc.perform(get("/api/instruments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Guitar"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        when(instrumentService.findById(5L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/instruments/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        when(instrumentService.create(any(InstrumentDTO.class))).thenReturn(instrument);

        mockMvc.perform(post("/api/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Guitar"));
    }

    @Test
    void create_withoutImageUrl_isAccepted() throws Exception {
        InstrumentDTO dto = validRequest();
        dto.setImageUrl(null);
        when(instrumentService.create(any(InstrumentDTO.class))).thenReturn(instrument);

        mockMvc.perform(post("/api/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_withMissingName_returnsValidationError() throws Exception {
        InstrumentDTO dto = validRequest();
        dto.setName(null);

        mockMvc.perform(post("/api/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Instrument name is required"));

        verify(instrumentService, never()).create(any());
    }

    @Test
    void create_withTooLongName_returnsValidationError() throws Exception {
        InstrumentDTO dto = validRequest();
        dto.setName("n".repeat(101));

        mockMvc.perform(post("/api/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name must be between 1 and 100 characters"));
    }

    @Test
    void create_withInvalidImageUrl_returnsValidationError() throws Exception {
        InstrumentDTO dto = validRequest();
        dto.setImageUrl("guitar.png");

        mockMvc.perform(post("/api/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.imageUrl").value("Image URL must be a valid URL"));
    }

    @Test
    void update_whenFound_returnsUpdatedInstrument() throws Exception {
        when(instrumentService.update(eq(1L), any(InstrumentDTO.class))).thenReturn(Optional.of(instrument));

        mockMvc.perform(put("/api/instruments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_whenMissing_returnsNotFound() throws Exception {
        when(instrumentService.update(eq(1L), any(InstrumentDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/instruments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withInvalidBody_returnsValidationError() throws Exception {
        InstrumentDTO dto = validRequest();
        dto.setName(" ");

        mockMvc.perform(put("/api/instruments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verify(instrumentService, never()).update(anyLong(), any());
    }

    @Test
    void delete_whenFound_returnsNoContent() throws Exception {
        when(instrumentService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/instruments/1"))
                .andExpect(status().isNoContent());

        verify(instrumentService).delete(1L);
    }

    @Test
    void delete_whenMissing_returnsNotFound() throws Exception {
        when(instrumentService.delete(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/instruments/1"))
                .andExpect(status().isNotFound());
    }
}
