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

import com.uo289390.toco.controller.LessonSectionController;
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
import com.uo289390.toco.dto.LessonSectionDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.LessonSectionService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link LessonSectionController}.
 * Verifies routing, status codes, JSON serialization and request validation
 * of every lesson section endpoint, with the service layer mocked.
 */
@WebMvcTest(LessonSectionController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class LessonSectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LessonSectionService lessonSectionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private LessonSectionDTO section;

    @BeforeEach
    void setUp() {
        section = validRequest();
        section.setId(3L);
    }

    private LessonSectionDTO validRequest() {
        LessonSectionDTO dto = new LessonSectionDTO();
        dto.setTitle("Introduction");
        dto.setHtmlContent("<p>Welcome</p>");
        dto.setLessonId(9L);
        dto.setOrderIndex(0);
        return dto;
    }

    private void expectCreateValidationError(LessonSectionDTO dto, String field, String message) throws Exception {
        mockMvc.perform(post("/api/lesson-sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + field).value(message));
        verify(lessonSectionService, never()).create(any());
    }

    @Test
    void getAll_returnsAllSections() throws Exception {
        when(lessonSectionService.findAll()).thenReturn(List.of(section));

        mockMvc.perform(get("/api/lesson-sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].title").value("Introduction"))
                .andExpect(jsonPath("$[0].htmlContent").value("<p>Welcome</p>"))
                .andExpect(jsonPath("$[0].lessonId").value(9))
                .andExpect(jsonPath("$[0].orderIndex").value(0));
    }

    @Test
    void getByIds_returnsRequestedSections() throws Exception {
        when(lessonSectionService.findByIds(List.of(3L, 4L))).thenReturn(List.of(section));

        mockMvc.perform(post("/api/lesson-sections/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[3,4]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(lessonSectionService).findByIds(List.of(3L, 4L));
    }

    @Test
    void getById_whenFound_returnsSection() throws Exception {
        when(lessonSectionService.findById(3L)).thenReturn(Optional.of(section));

        mockMvc.perform(get("/api/lesson-sections/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Introduction"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        when(lessonSectionService.findById(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/lesson-sections/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getForLesson_returnsSectionsOfLesson() throws Exception {
        LessonSectionDTO second = validRequest();
        second.setId(4L);
        second.setOrderIndex(1);
        when(lessonSectionService.findForLesson(9L)).thenReturn(List.of(section, second));

        mockMvc.perform(get("/api/lesson-sections/by-lesson/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderIndex").value(0))
                .andExpect(jsonPath("$[1].orderIndex").value(1));

        verify(lessonSectionService).findForLesson(9L);
    }

    @Test
    void getForLesson_whenNone_returnsEmptyList() throws Exception {
        when(lessonSectionService.findForLesson(9L)).thenReturn(List.of());

        mockMvc.perform(get("/api/lesson-sections/by-lesson/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        when(lessonSectionService.create(any(LessonSectionDTO.class))).thenReturn(section);

        mockMvc.perform(post("/api/lesson-sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));

        verify(lessonSectionService).create(argThat(dto -> "Introduction".equals(dto.getTitle())
                && dto.getLessonId() == 9L));
    }

    @Test
    void create_withBlankTitle_returnsValidationError() throws Exception {
        LessonSectionDTO dto = validRequest();
        dto.setTitle(null);
        expectCreateValidationError(dto, "title", "Section title is required");
    }

    @Test
    void create_withTooLongTitle_returnsValidationError() throws Exception {
        LessonSectionDTO dto = validRequest();
        dto.setTitle("t".repeat(201));
        expectCreateValidationError(dto, "title", "Section title must be between 1 and 200 characters");
    }

    @Test
    void create_withBlankContent_returnsValidationError() throws Exception {
        LessonSectionDTO dto = validRequest();
        dto.setHtmlContent("");
        expectCreateValidationError(dto, "htmlContent", "Section content is required");
    }

    @Test
    void create_withoutLessonId_returnsValidationError() throws Exception {
        LessonSectionDTO dto = validRequest();
        dto.setLessonId(null);
        expectCreateValidationError(dto, "lessonId", "Lesson ID is required");
    }

    @Test
    void create_withNegativeOrderIndex_returnsValidationError() throws Exception {
        LessonSectionDTO dto = validRequest();
        dto.setOrderIndex(-1);
        expectCreateValidationError(dto, "orderIndex", "Order index must be 0 or greater");
    }

    @Test
    void update_whenFound_returnsUpdatedSection() throws Exception {
        when(lessonSectionService.update(eq(3L), any(LessonSectionDTO.class))).thenReturn(Optional.of(section));

        mockMvc.perform(put("/api/lesson-sections/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void update_whenMissing_returnsNotFound() throws Exception {
        when(lessonSectionService.update(eq(3L), any(LessonSectionDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/lesson-sections/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withInvalidBody_returnsValidationError() throws Exception {
        LessonSectionDTO dto = validRequest();
        dto.setHtmlContent(" ");

        mockMvc.perform(put("/api/lesson-sections/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.htmlContent").exists());

        verify(lessonSectionService, never()).update(anyLong(), any());
    }

    @Test
    void delete_whenFound_returnsNoContent() throws Exception {
        when(lessonSectionService.delete(3L)).thenReturn(true);

        mockMvc.perform(delete("/api/lesson-sections/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMissing_returnsNotFound() throws Exception {
        when(lessonSectionService.delete(3L)).thenReturn(false);

        mockMvc.perform(delete("/api/lesson-sections/3"))
                .andExpect(status().isNotFound());
    }
}
