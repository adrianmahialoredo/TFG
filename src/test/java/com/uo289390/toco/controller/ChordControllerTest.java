package com.uo289390.toco.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
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

import com.uo289390.toco.controller.ChordController;
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
import com.uo289390.toco.dto.ChordDto;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.ChordService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link ChordController}.
 * Verifies routing, status codes, JSON serialization and request validation
 * of every chord endpoint, with the service layer mocked.
 */
@WebMvcTest(ChordController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class ChordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChordService chordService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private ChordDto chord;

    @BeforeEach
    void setUp() {
        chord = new ChordDto();
        chord.setId(1L);
        chord.setName("Am");
        chord.setNotes(List.of("A", "C", "E"));
        chord.setImageUrl("https://example.com/am.png");
        chord.setOpen(true);
        chord.setMinor(true);
        chord.setCapoFret(0);
        chord.setInstrumentId(1L);
    }

    private ChordDto validRequest() {
        ChordDto dto = new ChordDto();
        dto.setName("G");
        dto.setNotes(List.of("G", "B", "D"));
        dto.setImageUrl("https://example.com/g.png");
        dto.setMajor(true);
        dto.setInstrumentId(1L);
        return dto;
    }

    // ---------- GET /api/chords ----------

    @Test
    void getAll_returnsAllChords() throws Exception {
        when(chordService.findAll()).thenReturn(List.of(chord));

        mockMvc.perform(get("/api/chords"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Am"))
                .andExpect(jsonPath("$[0].notes", hasSize(3)))
                .andExpect(jsonPath("$[0].open").value(true))
                .andExpect(jsonPath("$[0].minor").value(true))
                .andExpect(jsonPath("$[0].barre").value(false))
                .andExpect(jsonPath("$[0].capoFret").value(0));
    }

    @Test
    void getAll_returnsEmptyListWhenNoChords() throws Exception {
        when(chordService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/chords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- POST /api/chords/by-ids ----------

    @Test
    void getByIds_returnsRequestedChords() throws Exception {
        when(chordService.findByIds(List.of(1L, 2L))).thenReturn(List.of(chord));

        mockMvc.perform(post("/api/chords/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Am"));

        verify(chordService).findByIds(List.of(1L, 2L));
    }

    @Test
    void getByIds_withEmptyList_returnsEmptyList() throws Exception {
        when(chordService.findByIds(List.of())).thenReturn(List.of());

        mockMvc.perform(post("/api/chords/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    // ---------- GET /api/chords/{id} ----------

    @Test
    void getById_whenFound_returnsChord() throws Exception {
        when(chordService.findById(1L)).thenReturn(Optional.of(chord));

        mockMvc.perform(get("/api/chords/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Am"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/am.png"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        when(chordService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/chords/99"))
                .andExpect(status().isNotFound());
    }


    // ---------- GET /api/chords/by-song-lesson/{id}/ids ----------

    @Test
    void getIdsForSongLesson_returnsIds() throws Exception {
        when(chordService.findIdsForSongLesson(5L)).thenReturn(List.of(1L, 2L, 3L));

        mockMvc.perform(get("/api/chords/by-song-lesson/5/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[2]").value(3));
    }

    @Test
    void getIdsForSongLesson_whenNone_returnsEmptyList() throws Exception {
        when(chordService.findIdsForSongLesson(5L)).thenReturn(List.of());

        mockMvc.perform(get("/api/chords/by-song-lesson/5/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- POST /api/chords ----------

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        ChordDto saved = validRequest();
        saved.setId(10L);
        when(chordService.create(any(ChordDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/chords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("G"))
                .andExpect(jsonPath("$.major").value(true));
    }

    @Test
    void create_withBlankName_returnsValidationError() throws Exception {
        ChordDto dto = validRequest();
        dto.setName("  ");

        mockMvc.perform(post("/api/chords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());

        verify(chordService, never()).create(any());
    }

    @Test
    void create_withTooLongName_returnsValidationError() throws Exception {
        ChordDto dto = validRequest();
        dto.setName("x".repeat(51));

        mockMvc.perform(post("/api/chords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Chord name must be between 1 and 50 characters"));
    }

    @Test
    void create_withInvalidImageUrl_returnsValidationError() throws Exception {
        ChordDto dto = validRequest();
        dto.setImageUrl("not a url");

        mockMvc.perform(post("/api/chords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.imageUrl").value("Image URL must be a valid URL"));
    }

    @Test
    void create_withCapoFretOutOfRange_returnsValidationError() throws Exception {
        ChordDto tooHigh = validRequest();
        tooHigh.setCapoFret(13);
        ChordDto negative = validRequest();
        negative.setCapoFret(-1);

        mockMvc.perform(post("/api/chords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tooHigh)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capoFret").value("Capo fret must be at most 12"));

        mockMvc.perform(post("/api/chords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negative)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capoFret").value("Capo fret must be at least 0"));

        verify(chordService, never()).create(any());
    }

    @Test
    void create_withoutInstrument_returnsValidationError() throws Exception {
        ChordDto dto = validRequest();
        dto.setInstrumentId(null);

        mockMvc.perform(post("/api/chords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.instrumentId").value("Instrument ID is required"));

        verify(chordService, never()).create(any());
    }

    @Test
    void create_withBoundaryCapoFret_isAccepted() throws Exception {
        ChordDto dto = validRequest();
        dto.setCapo(true);
        dto.setCapoFret(12);
        when(chordService.create(any(ChordDto.class))).thenReturn(dto);

        mockMvc.perform(post("/api/chords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.capoFret").value(12))
                .andExpect(jsonPath("$.capo").value(true));
    }


    // ---------- PUT /api/chords/{id} ----------

    @Test
    void update_whenFound_returnsUpdatedChord() throws Exception {
        ChordDto updated = validRequest();
        updated.setId(1L);
        when(chordService.update(eq(1L), any(ChordDto.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/chords/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("G"));

        verify(chordService).update(eq(1L), any(ChordDto.class));
    }

    @Test
    void update_whenMissing_returnsNotFound() throws Exception {
        when(chordService.update(eq(99L), any(ChordDto.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/chords/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withInvalidBody_returnsValidationError() throws Exception {
        ChordDto dto = validRequest();
        dto.setName("");

        mockMvc.perform(put("/api/chords/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verify(chordService, never()).update(anyLong(), any());
    }

    // ---------- DELETE /api/chords/{id} ----------

    @Test
    void delete_whenFound_returnsNoContent() throws Exception {
        when(chordService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/chords/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(chordService).delete(1L);
    }

    @Test
    void delete_whenMissing_returnsNotFound() throws Exception {
        when(chordService.delete(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/chords/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unsupportedMethod_isRejected() throws Exception {
        mockMvc.perform(put("/api/chords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getById_nonNumericId_returnsBadRequest() throws Exception {
        // Regression: a type mismatch on the path variable used to fall into the generic 500 handler
        mockMvc.perform(get("/api/chords/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));

        verifyNoInteractions(chordService);
    }

    @Test
    void getByIds_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/chords/by-ids").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or missing request body"));

        verifyNoInteractions(chordService);
    }
}
