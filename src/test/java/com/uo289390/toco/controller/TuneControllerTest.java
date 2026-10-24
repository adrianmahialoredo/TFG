package com.uo289390.toco.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.dto.TuneDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.TuneService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Web layer tests for {@link TuneController}.
 * Verifies routing, status codes, JSON serialization and request validation
 * of every tuning endpoint, with the service layer mocked.
 */
@WebMvcTest(TuneController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class TuneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TuneService tuneService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private TuneDTO standard;

    @BeforeEach
    void setUp() {
        standard = new TuneDTO();
        standard.setId(1L);
        standard.setTitle("Estándar");
        standard.setInstrumentId(1L);
        standard.setNotes(List.of("E2", "A2", "D3", "G3", "B3", "E4"));
    }

    private TuneDTO validRequest() {
        TuneDTO dto = new TuneDTO();
        dto.setId(2L);
        dto.setTitle("Drop D");
        dto.setInstrumentId(1L);
        dto.setNotes(List.of("D2", "A2", "D3", "G3", "B3", "E4"));
        return dto;
    }

    // ---------- GET /api/tunes ----------

    @Test
    void getAll_returnsAllTunes() throws Exception {
        when(tuneService.findAll()).thenReturn(List.of(standard));

        mockMvc.perform(get("/api/tunes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Estándar"))
                .andExpect(jsonPath("$[0].instrumentId").value(1))
                .andExpect(jsonPath("$[0].notes", hasSize(6)))
                .andExpect(jsonPath("$[0].notes[0]").value("E2"))
                .andExpect(jsonPath("$[0].notes[5]").value("E4"));
    }

    @Test
    void getAll_returnsEmptyListWhenNoTunes() throws Exception {
        when(tuneService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/tunes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- POST /api/tunes/by-ids ----------

    @Test
    void getByIds_returnsRequestedTunes() throws Exception {
        when(tuneService.findByIds(List.of(1L, 2L))).thenReturn(List.of(standard));

        mockMvc.perform(post("/api/tunes/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Estándar"));
    }

    // ---------- GET /api/tunes/{id} ----------

    @Test
    void getById_whenFound_returnsTune() throws Exception {
        when(tuneService.findById(1L)).thenReturn(Optional.of(standard));

        mockMvc.perform(get("/api/tunes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Estándar"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        when(tuneService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tunes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_nonNumericId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/tunes/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tuneService);
    }

    // ---------- GET /api/tunes/by-instrument/{instrumentId} ----------

    @Test
    void getForInstrument_returnsTheTuningsOfTheInstrument() throws Exception {
        when(tuneService.findForInstrument(1L)).thenReturn(List.of(standard, validRequest()));

        mockMvc.perform(get("/api/tunes/by-instrument/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Estándar"))
                .andExpect(jsonPath("$[1].title").value("Drop D"));
    }

    @Test
    void getForInstrument_whenNone_returnsEmptyList() throws Exception {
        when(tuneService.findForInstrument(5L)).thenReturn(List.of());

        mockMvc.perform(get("/api/tunes/by-instrument/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- POST /api/tunes ----------

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        when(tuneService.create(any(TuneDTO.class))).thenReturn(validRequest());

        mockMvc.perform(post("/api/tunes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Drop D"))
                .andExpect(jsonPath("$.notes[0]").value("D2"));
    }

    @Test
    void create_withBlankTitle_returnsValidationError() throws Exception {
        TuneDTO dto = validRequest();
        dto.setTitle(" ");

        mockMvc.perform(post("/api/tunes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists());

        verify(tuneService, never()).create(any());
    }

    @Test
    void create_withoutInstrument_returnsValidationError() throws Exception {
        TuneDTO dto = validRequest();
        dto.setInstrumentId(null);

        mockMvc.perform(post("/api/tunes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.instrumentId").value("Instrument ID is required"));

        verify(tuneService, never()).create(any());
    }

    @Test
    void create_withoutNotes_returnsValidationError() throws Exception {
        TuneDTO dto = validRequest();
        dto.setNotes(List.of());

        mockMvc.perform(post("/api/tunes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.notes").value("Tune must have at least one note"));
    }

    @Test
    void create_withMalformedNote_returnsValidationError() throws Exception {
        TuneDTO dto = validRequest();
        // Flats and notes without octave are not accepted: E2, F#3...
        dto.setNotes(List.of("D2", "Bb2", "E"));

        mockMvc.perform(post("/api/tunes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(tuneService, never()).create(any());
    }

    @Test
    void create_withMissingInstrument_returnsNotFound() throws Exception {
        when(tuneService.create(any(TuneDTO.class))).thenThrow(new EntityNotFoundException("Instrument not found: 1"));

        mockMvc.perform(post("/api/tunes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /api/tunes/{id} ----------

    @Test
    void update_whenFound_returnsUpdatedTune() throws Exception {
        when(tuneService.update(eq(2L), any(TuneDTO.class))).thenReturn(Optional.of(validRequest()));

        mockMvc.perform(put("/api/tunes/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Drop D"));
    }

    @Test
    void update_whenMissing_returnsNotFound() throws Exception {
        when(tuneService.update(eq(99L), any(TuneDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tunes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withInvalidBody_returnsValidationError() throws Exception {
        TuneDTO dto = validRequest();
        dto.setNotes(List.of("H2"));

        mockMvc.perform(put("/api/tunes/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(tuneService, never()).update(anyLong(), any());
    }

    // ---------- DELETE /api/tunes/{id} ----------

    @Test
    void delete_whenFound_returnsNoContent() throws Exception {
        when(tuneService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/tunes/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void delete_whenMissing_returnsNotFound() throws Exception {
        when(tuneService.delete(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/tunes/99"))
                .andExpect(status().isNotFound());
    }
}
