package com.uo289390.toco.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

import com.uo289390.toco.controller.SongController;
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
import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.dto.SongLessonDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link SongController}.
 * Verifies routing, status codes, JSON serialization, query parameters and
 * request validation of every song endpoint, with the service layer mocked.
 */
@WebMvcTest(SongController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SongService songService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private SongDTO song;

    @BeforeEach
    void setUp() {
        song = validRequest();
        song.setId(1L);
        SongLessonDTO lesson = new SongLessonDTO();
        lesson.setId(11L);
        lesson.setDifficulty("EASY");
        lesson.setSongId(1L);
        lesson.setInstrumentId(2L);
        song.setLessons(List.of(lesson));
        song.setLessonIds(List.of(11L));
    }

    private SongDTO validRequest() {
        SongDTO dto = new SongDTO();
        dto.setTitle("Bohemian Rhapsody");
        dto.setArtist("Queen");
        dto.setImageUrl("https://example.com/bohemian.png");
        dto.setLevel(3);
        dto.setGenreIds(List.of(1L));
        dto.setInstrumentIds(List.of(2L));
        return dto;
    }

    @Test
    void getAll_returnsAllSongs() throws Exception {
        when(songService.findAll()).thenReturn(List.of(song));

        mockMvc.perform(get("/api/songs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Bohemian Rhapsody"))
                .andExpect(jsonPath("$[0].artist").value("Queen"))
                .andExpect(jsonPath("$[0].type").value("Song"))
                .andExpect(jsonPath("$[0].level").value(3))
                .andExpect(jsonPath("$[0].genreIds[0]").value(1))
                .andExpect(jsonPath("$[0].instrumentIds[0]").value(2))
                .andExpect(jsonPath("$[0].lessonIds[0]").value(11))
                .andExpect(jsonPath("$[0].lessons[0].difficulty").value("EASY"));
    }

    @Test
    void getAll_returnsEmptyList() throws Exception {
        when(songService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/songs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getByIds_returnsRequestedSongs() throws Exception {
        when(songService.findByIds(List.of(1L, 2L))).thenReturn(List.of(song));

        mockMvc.perform(post("/api/songs/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(songService).findByIds(List.of(1L, 2L));
    }

    @Test
    void getById_whenFound_returnsSong() throws Exception {
        when(songService.findById(1L)).thenReturn(Optional.of(song));

        mockMvc.perform(get("/api/songs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artist").value("Queen"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        when(songService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/songs/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByDecade_passesDecadeToService() throws Exception {
        when(songService.findByDecade("1970s")).thenReturn(List.of(song));

        mockMvc.perform(get("/api/songs/by-decade/1970s"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(songService).findByDecade("1970s");
    }

    @Test
    void getByDecade_whenNone_returnsEmptyList() throws Exception {
        when(songService.findByDecade("2020s")).thenReturn(List.of());

        mockMvc.perform(get("/api/songs/by-decade/2020s"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getPopularForGenre_returnsPopularSongs() throws Exception {
        when(songService.findPopularForGenre(4L)).thenReturn(List.of(song));

        mockMvc.perform(get("/api/songs/by-genre/4/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(songService).findPopularForGenre(4L);
    }

    @Test
    void getClassicForGenre_returnsClassicSongs() throws Exception {
        when(songService.findClassicForGenre(4L)).thenReturn(List.of(song));

        mockMvc.perform(get("/api/songs/by-genre/4/classic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(songService).findClassicForGenre(4L);
    }

    @Test
    void getNewForGenre_returnsNewSongs() throws Exception {
        when(songService.findNewForGenre(4L)).thenReturn(List.of());

        mockMvc.perform(get("/api/songs/by-genre/4/new"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(songService).findNewForGenre(4L);
    }

    @Test
    void getPopular_withLimit_passesLimit() throws Exception {
        when(songService.findPopular(5)).thenReturn(List.of(song));

        mockMvc.perform(get("/api/songs/popular").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(songService).findPopular(5);
    }

    @Test
    void getPopular_withoutLimit_passesNull() throws Exception {
        when(songService.findPopular(null)).thenReturn(List.of(song));

        mockMvc.perform(get("/api/songs/popular"))
                .andExpect(status().isOk());

        verify(songService).findPopular(isNull());
    }

    @Test
    void getNew_withLimit_passesLimit() throws Exception {
        when(songService.findNew(3)).thenReturn(List.of(song));

        mockMvc.perform(get("/api/songs/new").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(songService).findNew(3);
    }

    @Test
    void getNew_withoutLimit_passesNull() throws Exception {
        when(songService.findNew(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/songs/new"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(songService).findNew(isNull());
    }

    @Test
    void literalPathsTakePrecedenceOverIdPath() throws Exception {
        when(songService.findPopular(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/songs/popular"))
                .andExpect(status().isOk());

        verify(songService, never()).findById(anyLong());
    }

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        when(songService.create(any(SongDTO.class))).thenReturn(song);

        mockMvc.perform(post("/api/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(songService).create(argThat(dto -> "Queen".equals(dto.getArtist())
                && dto.getGenreIds().equals(List.of(1L))
                && dto.getInstrumentIds().equals(List.of(2L))));
    }

    @Test
    void create_withBlankTitle_returnsValidationError() throws Exception {
        SongDTO dto = validRequest();
        dto.setTitle(" ");

        mockMvc.perform(post("/api/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Item title is required"));

        verify(songService, never()).create(any());
    }

    @Test
    void create_withLevelOutOfRange_returnsValidationError() throws Exception {
        SongDTO dto = validRequest();
        dto.setLevel(6);

        mockMvc.perform(post("/api/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.level").value("Level must be at most 5"));
    }

    @Test
    void update_whenFound_returnsUpdatedSong() throws Exception {
        when(songService.update(eq(1L), any(SongDTO.class))).thenReturn(Optional.of(song));

        mockMvc.perform(put("/api/songs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_whenMissing_returnsNotFound() throws Exception {
        when(songService.update(eq(1L), any(SongDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/songs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withInvalidBody_returnsValidationError() throws Exception {
        SongDTO dto = validRequest();
        dto.setLevel(-2);

        mockMvc.perform(put("/api/songs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.level").value("Level must be at least 0"));

        verify(songService, never()).update(anyLong(), any());
    }

    @Test
    void delete_whenFound_returnsNoContent() throws Exception {
        when(songService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/songs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMissing_returnsNotFound() throws Exception {
        when(songService.delete(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/songs/1"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/songs/by-artists/new | popular ----------

    @Test
    void getNewForArtists_readsTheNumberedArtistsInOrder() throws Exception {
        when(songService.findNewForArtists(List.of("Queen", "Bowie", "Oasis"), 12)).thenReturn(List.of(song));

        mockMvc.perform(get("/api/songs/by-artists/new")
                        .param("limit", "12")
                        .param("artist3", "Oasis")
                        .param("artist1", "Queen")
                        .param("artist2", "Bowie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(songService).findNewForArtists(List.of("Queen", "Bowie", "Oasis"), 12);
    }

    @Test
    void getPopularForArtists_withoutLimit_passesNull() throws Exception {
        when(songService.findPopularForArtists(List.of("Queen"), null)).thenReturn(List.of());

        mockMvc.perform(get("/api/songs/by-artists/popular").param("artist1", "Queen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(songService).findPopularForArtists(eq(List.of("Queen")), isNull());
    }

    @Test
    void getNewForArtists_artistNumberOutOfRange_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/songs/by-artists/new").param("artist1", "Queen").param("artist11", "Bowie"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Artist parameters go from artist1 to artist10: artist11"));

        mockMvc.perform(get("/api/songs/by-artists/popular").param("artist0", "Queen"))
                .andExpect(status().isBadRequest());

        verify(songService, never()).findNewForArtists(any(), any());
        verify(songService, never()).findPopularForArtists(any(), any());
    }

    @Test
    void getNewForArtists_serviceRejection_returnsBadRequest() throws Exception {
        when(songService.findNewForArtists(List.of(), null))
                .thenThrow(new IllegalArgumentException("At least one artist is required"));

        mockMvc.perform(get("/api/songs/by-artists/new"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least one artist is required"));
    }
}
