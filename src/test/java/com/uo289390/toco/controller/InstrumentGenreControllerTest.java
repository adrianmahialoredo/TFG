package com.uo289390.toco.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
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

import com.uo289390.toco.controller.InstrumentGenreController;
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
import com.uo289390.toco.dto.InstrumentGenreDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.InstrumentGenreService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link InstrumentGenreController}.
 * Verifies routing, status codes, JSON serialization and request validation
 * of every instrument-genre endpoint, with the service layer mocked.
 */
@WebMvcTest(InstrumentGenreController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class InstrumentGenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InstrumentGenreService instrumentGenreService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private InstrumentGenreDTO instrumentGenre;

    @BeforeEach
    void setUp() {
        instrumentGenre = validRequest();
        instrumentGenre.setId(7L);
    }

    private InstrumentGenreDTO validRequest() {
        InstrumentGenreDTO dto = new InstrumentGenreDTO();
        dto.setTitle("Rock Guitar");
        dto.setLevel(1);
        dto.setInstrumentId(2L);
        dto.setGenreId(3L);
        dto.setLessonIds(List.of(10L));
        return dto;
    }

    @Test
    void getAll_returnsAllInstrumentGenres() throws Exception {
        when(instrumentGenreService.findAll()).thenReturn(List.of(instrumentGenre));

        mockMvc.perform(get("/api/instrument-genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].type").value("InstrumentGenre"))
                .andExpect(jsonPath("$[0].instrumentId").value(2))
                .andExpect(jsonPath("$[0].genreId").value(3))
                .andExpect(jsonPath("$[0].lessonIds[0]").value(10));
    }

    @Test
    void getByIds_returnsRequestedInstrumentGenres() throws Exception {
        when(instrumentGenreService.findByIds(List.of(7L, 8L))).thenReturn(List.of(instrumentGenre));

        mockMvc.perform(post("/api/instrument-genres/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[7,8]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(instrumentGenreService).findByIds(List.of(7L, 8L));
    }

    @Test
    void getById_whenFound_returnsInstrumentGenre() throws Exception {
        when(instrumentGenreService.findById(7L)).thenReturn(Optional.of(instrumentGenre));

        mockMvc.perform(get("/api/instrument-genres/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Rock Guitar"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        when(instrumentGenreService.findById(7L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/instrument-genres/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getForGenre_returnsInstrumentGenresOfGenre() throws Exception {
        when(instrumentGenreService.findForGenre(3L)).thenReturn(List.of(instrumentGenre));

        mockMvc.perform(get("/api/instrument-genres/by-genre/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].genreId").value(3));

        verify(instrumentGenreService).findForGenre(3L);
    }

    @Test
    void getForGenre_whenNone_returnsEmptyList() throws Exception {
        when(instrumentGenreService.findForGenre(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/instrument-genres/by-genre/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getForInstrument_returnsInstrumentGenresOfInstrument() throws Exception {
        when(instrumentGenreService.findForInstrument(2L)).thenReturn(List.of(instrumentGenre));

        mockMvc.perform(get("/api/instrument-genres/by-instrument/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].instrumentId").value(2));

        verify(instrumentGenreService).findForInstrument(2L);
    }

    @Test
    void getForInstrument_whenNone_returnsEmptyList() throws Exception {
        when(instrumentGenreService.findForInstrument(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/instrument-genres/by-instrument/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        when(instrumentGenreService.create(any(InstrumentGenreDTO.class))).thenReturn(instrumentGenre);

        mockMvc.perform(post("/api/instrument-genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7));

        verify(instrumentGenreService).create(argThat(dto ->
                dto.getInstrumentId() == 2L && dto.getGenreId() == 3L));
    }

    @Test
    void create_withoutInstrumentId_returnsValidationError() throws Exception {
        InstrumentGenreDTO dto = validRequest();
        dto.setInstrumentId(null);

        mockMvc.perform(post("/api/instrument-genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.instrumentId").value("Instrument ID is required"));

        verify(instrumentGenreService, never()).create(any());
    }

    @Test
    void create_withoutGenreId_returnsValidationError() throws Exception {
        InstrumentGenreDTO dto = validRequest();
        dto.setGenreId(null);

        mockMvc.perform(post("/api/instrument-genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.genreId").value("Genre ID is required"));
    }

    @Test
    void create_withMultipleViolations_reportsEveryField() throws Exception {
        InstrumentGenreDTO dto = new InstrumentGenreDTO();
        dto.setLevel(9);

        mockMvc.perform(post("/api/instrument-genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.level").exists())
                .andExpect(jsonPath("$.errors.instrumentId").exists())
                .andExpect(jsonPath("$.errors.genreId").exists());
    }

    @Test
    void update_whenFound_returnsUpdatedInstrumentGenre() throws Exception {
        when(instrumentGenreService.update(eq(7L), any(InstrumentGenreDTO.class)))
                .thenReturn(Optional.of(instrumentGenre));

        mockMvc.perform(put("/api/instrument-genres/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void update_whenMissing_returnsNotFound() throws Exception {
        when(instrumentGenreService.update(eq(7L), any(InstrumentGenreDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/instrument-genres/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withInvalidBody_returnsValidationError() throws Exception {
        InstrumentGenreDTO dto = validRequest();
        dto.setGenreId(null);

        mockMvc.perform(put("/api/instrument-genres/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.genreId").exists());

        verify(instrumentGenreService, never()).update(anyLong(), any());
    }

    @Test
    void delete_whenFound_returnsNoContent() throws Exception {
        when(instrumentGenreService.delete(7L)).thenReturn(true);

        mockMvc.perform(delete("/api/instrument-genres/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMissing_returnsNotFound() throws Exception {
        when(instrumentGenreService.delete(7L)).thenReturn(false);

        mockMvc.perform(delete("/api/instrument-genres/7"))
                .andExpect(status().isNotFound());
    }
}
