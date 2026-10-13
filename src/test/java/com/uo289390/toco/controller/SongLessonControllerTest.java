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

import com.uo289390.toco.controller.SongLessonController;
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
import com.uo289390.toco.dto.SongLessonDTO;
import com.uo289390.toco.dto.TabSectionDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.SongLessonService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link SongLessonController}.
 * Verifies routing, status codes, JSON serialization and request validation
 * of every song lesson endpoint, with the service layer mocked.
 */
@WebMvcTest(SongLessonController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class SongLessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SongLessonService songLessonService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private SongLessonDTO songLesson;

    @BeforeEach
    void setUp() {
        songLesson = validRequest();
        songLesson.setId(5L);
        TabSectionDTO tab = new TabSectionDTO();
        tab.setId(8L);
        tab.setIdentifier("Intro");
        tab.setContent("e|---0---|");
        tab.setSongLessonId(5L);
        songLesson.setTabSections(List.of(tab));
        songLesson.setTabSectionsIds(List.of(8L));
    }

    private SongLessonDTO validRequest() {
        SongLessonDTO dto = new SongLessonDTO();
        dto.setDifficulty("MEDIUM");
        dto.setChordIds(List.of(1L, 2L));
        dto.setChordedLyrics("[Am]Hello [G]world");
        dto.setVideoUrl("https://youtube.com/watch?v=abc");
        dto.setSongId(3L);
        dto.setInstrumentId(4L);
        return dto;
    }

    private void expectCreateValidationError(SongLessonDTO dto, String field, String message) throws Exception {
        mockMvc.perform(post("/api/song-lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + field).value(message));
        verify(songLessonService, never()).create(any());
    }

    @Test
    void getAll_returnsAllSongLessons() throws Exception {
        when(songLessonService.findAll()).thenReturn(List.of(songLesson));

        mockMvc.perform(get("/api/song-lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$[0].chordIds", hasSize(2)))
                .andExpect(jsonPath("$[0].chordedLyrics").value("[Am]Hello [G]world"))
                .andExpect(jsonPath("$[0].videoUrl").value("https://youtube.com/watch?v=abc"))
                .andExpect(jsonPath("$[0].songId").value(3))
                .andExpect(jsonPath("$[0].instrumentId").value(4))
                .andExpect(jsonPath("$[0].tabSectionsIds[0]").value(8))
                .andExpect(jsonPath("$[0].tabSections[0].identifier").value("Intro"));
    }

    @Test
    void getByIds_returnsRequestedSongLessons() throws Exception {
        when(songLessonService.findByIds(List.of(5L))).thenReturn(List.of(songLesson));

        mockMvc.perform(post("/api/song-lessons/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[5]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));

        verify(songLessonService).findByIds(List.of(5L));
    }

    @Test
    void getById_whenFound_returnsSongLesson() throws Exception {
        when(songLessonService.findById(5L)).thenReturn(Optional.of(songLesson));

        mockMvc.perform(get("/api/song-lessons/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        when(songLessonService.findById(5L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/song-lessons/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getForSong_returnsLessonsOfSong() throws Exception {
        when(songLessonService.findForSong(3L)).thenReturn(List.of(songLesson));

        mockMvc.perform(get("/api/song-lessons/by-song/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].songId").value(3));

        verify(songLessonService).findForSong(3L);
    }

    @Test
    void getForSong_whenNone_returnsEmptyList() throws Exception {
        when(songLessonService.findForSong(3L)).thenReturn(List.of());

        mockMvc.perform(get("/api/song-lessons/by-song/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        when(songLessonService.create(any(SongLessonDTO.class))).thenReturn(songLesson);

        mockMvc.perform(post("/api/song-lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));

        verify(songLessonService).create(argThat(dto -> "MEDIUM".equals(dto.getDifficulty())
                && dto.getSongId() == 3L && dto.getInstrumentId() == 4L));
    }

    @Test
    void create_acceptsEveryValidDifficulty() throws Exception {
        when(songLessonService.create(any(SongLessonDTO.class))).thenReturn(songLesson);

        for (String difficulty : List.of("EASY", "MEDIUM", "ORIGINAL")) {
            SongLessonDTO dto = validRequest();
            dto.setDifficulty(difficulty);
            mockMvc.perform(post("/api/song-lessons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void create_withInvalidDifficulty_returnsValidationError() throws Exception {
        SongLessonDTO dto = validRequest();
        dto.setDifficulty("HARD");
        expectCreateValidationError(dto, "difficulty", "Difficulty must be EASY, MEDIUM, or ORIGINAL");
    }

    @Test
    void create_withLowercaseDifficulty_returnsValidationError() throws Exception {
        SongLessonDTO dto = validRequest();
        dto.setDifficulty("easy");
        expectCreateValidationError(dto, "difficulty", "Difficulty must be EASY, MEDIUM, or ORIGINAL");
    }

    @Test
    void create_withTooLongLyrics_returnsValidationError() throws Exception {
        SongLessonDTO dto = validRequest();
        dto.setChordedLyrics("l".repeat(5001));
        expectCreateValidationError(dto, "chordedLyrics", "Lyrics must not exceed 5000 characters");
    }

    @Test
    void create_withInvalidVideoUrl_returnsValidationError() throws Exception {
        SongLessonDTO dto = validRequest();
        dto.setVideoUrl("video");
        expectCreateValidationError(dto, "videoUrl", "Video URL must be a valid URL");
    }

    @Test
    void create_withoutSongId_returnsValidationError() throws Exception {
        SongLessonDTO dto = validRequest();
        dto.setSongId(null);
        expectCreateValidationError(dto, "songId", "Song ID is required");
    }

    @Test
    void create_withoutInstrumentId_returnsValidationError() throws Exception {
        SongLessonDTO dto = validRequest();
        dto.setInstrumentId(null);
        expectCreateValidationError(dto, "instrumentId", "Instrument ID is required");
    }

    @Test
    void update_whenFound_returnsUpdatedSongLesson() throws Exception {
        when(songLessonService.update(eq(5L), any(SongLessonDTO.class))).thenReturn(Optional.of(songLesson));

        mockMvc.perform(put("/api/song-lessons/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void update_whenMissing_returnsNotFound() throws Exception {
        when(songLessonService.update(eq(5L), any(SongLessonDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/song-lessons/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withInvalidBody_returnsValidationError() throws Exception {
        SongLessonDTO dto = validRequest();
        dto.setDifficulty("IMPOSSIBLE");

        mockMvc.perform(put("/api/song-lessons/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.difficulty").exists());

        verify(songLessonService, never()).update(anyLong(), any());
    }

    @Test
    void delete_whenFound_returnsNoContent() throws Exception {
        when(songLessonService.delete(5L)).thenReturn(true);

        mockMvc.perform(delete("/api/song-lessons/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMissing_returnsNotFound() throws Exception {
        when(songLessonService.delete(5L)).thenReturn(false);

        mockMvc.perform(delete("/api/song-lessons/5"))
                .andExpect(status().isNotFound());
    }
}
