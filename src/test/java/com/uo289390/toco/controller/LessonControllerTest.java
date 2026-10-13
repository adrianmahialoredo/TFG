package com.uo289390.toco.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.controller.LessonController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.LessonService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link LessonController}.
 * Covers lesson CRUD, validation and the id-listing endpoints (popular, new, by instrument, by genre).
 */
@WebMvcTest(LessonController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LessonService lessonService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private LessonDTO lesson(Long id) {
        LessonDTO dto = new LessonDTO();
        dto.setId(id);
        dto.setTitle("Lesson " + id);
        dto.setLevel(1);
        dto.setImageUrl("https://example.com/lesson.png");
        dto.setBelongsToGenre(true);
        dto.setGenreId(3L);
        dto.setInstrumentId(4L);
        dto.setSectionIds(List.of(100L, 101L));
        dto.setPopular(true);
        return dto;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void getAll_returnsLessonsWithLessonType() throws Exception {
        when(lessonService.findAll()).thenReturn(List.of(lesson(1L), lesson(2L)));

        mockMvc.perform(get("/api/lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("Lesson"))
                .andExpect(jsonPath("$[0].title").value("Lesson 1"))
                .andExpect(jsonPath("$[0].genreId").value(3))
                .andExpect(jsonPath("$[0].belongsToGenre").value(true))
                .andExpect(jsonPath("$[0].sectionIds.length()").value(2))
                .andExpect(jsonPath("$[0].popular").value(true));
    }

    @Test
    void getByIds_returnsMatchingLessons() throws Exception {
        when(lessonService.findByIds(List.of(1L))).thenReturn(List.of(lesson(1L)));

        mockMvc.perform(post("/api/lessons/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getById_existing_returnsLesson() throws Exception {
        when(lessonService.findById(1L)).thenReturn(Optional.of(lesson(1L)));

        mockMvc.perform(get("/api/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.instrumentId").value(4));
    }

    @Test
    void getById_missing_returnsNotFound() throws Exception {
        when(lessonService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/lessons/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByGenre_returnsLessons() throws Exception {
        when(lessonService.findForGenre(3L)).thenReturn(List.of(lesson(1L)));

        mockMvc.perform(get("/api/lessons/by-genre/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getIdsInstrumentPopular_returnsIds() throws Exception {
        when(lessonService.findIdsInstrumentPopular(4L)).thenReturn(List.of(1L, 2L));

        mockMvc.perform(get("/api/lessons/by-instrument/4/popular/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(2));
    }

    @Test
    void getIdsPopular_returnsIds() throws Exception {
        when(lessonService.findIdsPopular()).thenReturn(List.of(5L));

        mockMvc.perform(get("/api/lessons/popular/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(5));
    }

    @Test
    void getIdsNew_returnsIds() throws Exception {
        when(lessonService.findIdsNew()).thenReturn(List.of(9L, 8L));

        mockMvc.perform(get("/api/lessons/new/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value(9));
    }

    @Test
    void getIdsNew_empty_returnsEmptyArray() throws Exception {
        when(lessonService.findIdsNew()).thenReturn(List.of());

        mockMvc.perform(get("/api/lessons/new/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_validLesson_returnsCreated() throws Exception {
        when(lessonService.create(any(LessonDTO.class))).thenReturn(lesson(30L));

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lesson(null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.type").value("Lesson"));
    }

    @Test
    void create_blankTitle_returnsBadRequest() throws Exception {
        LessonDTO dto = lesson(null);
        dto.setTitle(" ");

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Item title is required"));

        verifyNoInteractions(lessonService);
    }

    @Test
    void create_levelAboveMaximum_returnsBadRequest() throws Exception {
        LessonDTO dto = lesson(null);
        dto.setLevel(6);

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.level").value("Level must be at most 5"));
    }

    @Test
    void create_negativeLevel_returnsBadRequest() throws Exception {
        LessonDTO dto = lesson(null);
        dto.setLevel(-1);

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.level").value("Level must be at least 0"));
    }

    @Test
    void create_boundaryLevels_areAccepted() throws Exception {
        when(lessonService.create(any(LessonDTO.class))).thenReturn(lesson(30L));
        for (int level : new int[] {0, 5}) {
            LessonDTO dto = lesson(null);
            dto.setLevel(level);
            mockMvc.perform(post("/api/lessons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(dto)))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void create_unknownGenre_returnsNotFound() throws Exception {
        when(lessonService.create(any(LessonDTO.class)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Genre not found"));

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lesson(null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Genre not found"));
    }

    @Test
    void update_existing_returnsUpdatedLesson() throws Exception {
        LessonDTO updated = lesson(1L);
        updated.setTitle("Updated");
        when(lessonService.update(eq(1L), any(LessonDTO.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/lessons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void update_missing_returnsNotFound() throws Exception {
        when(lessonService.update(eq(1L), any(LessonDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/lessons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lesson(1L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_blankTitle_returnsBadRequest() throws Exception {
        LessonDTO dto = lesson(1L);
        dto.setTitle("");

        mockMvc.perform(put("/api/lessons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());

        verifyNoInteractions(lessonService);
    }

    @Test
    void delete_existing_returnsNoContent() throws Exception {
        when(lessonService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/lessons/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_missing_returnsNotFound() throws Exception {
        when(lessonService.delete(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/lessons/1"))
                .andExpect(status().isNotFound());
    }
}
