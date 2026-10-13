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

import com.uo289390.toco.controller.TabSectionController;
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
import com.uo289390.toco.dto.TabSectionDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.TabSectionService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link TabSectionController}.
 * Verifies routing, status codes, JSON serialization and request validation
 * of every tab section endpoint, with the service layer mocked.
 */
@WebMvcTest(TabSectionController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class TabSectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TabSectionService tabSectionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private TabSectionDTO tab;

    @BeforeEach
    void setUp() {
        tab = validRequest();
        tab.setId(8L);
    }

    private TabSectionDTO validRequest() {
        TabSectionDTO dto = new TabSectionDTO();
        dto.setIdentifier("Intro");
        dto.setContent("e|---0---3---|");
        dto.setSongLessonId(5L);
        return dto;
    }

    private void expectCreateValidationError(TabSectionDTO dto, String field, String message) throws Exception {
        mockMvc.perform(post("/api/tab-sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + field).value(message));
        verify(tabSectionService, never()).create(any());
    }

    @Test
    void getAll_returnsAllTabSections() throws Exception {
        when(tabSectionService.findAll()).thenReturn(List.of(tab));

        mockMvc.perform(get("/api/tab-sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].identifier").value("Intro"))
                .andExpect(jsonPath("$[0].content").value("e|---0---3---|"))
                .andExpect(jsonPath("$[0].songLessonId").value(5));
    }

    @Test
    void getByIds_returnsRequestedTabSections() throws Exception {
        when(tabSectionService.findByIds(List.of(8L))).thenReturn(List.of(tab));

        mockMvc.perform(post("/api/tab-sections/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[8]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(8));

        verify(tabSectionService).findByIds(List.of(8L));
    }

    @Test
    void getById_whenFound_returnsTabSection() throws Exception {
        when(tabSectionService.findById(8L)).thenReturn(Optional.of(tab));

        mockMvc.perform(get("/api/tab-sections/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identifier").value("Intro"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        when(tabSectionService.findById(8L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tab-sections/8"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getForSongLesson_returnsTabSectionsOfSongLesson() throws Exception {
        when(tabSectionService.findForSongLesson(5L)).thenReturn(List.of(tab));

        mockMvc.perform(get("/api/tab-sections/by-song-lesson/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].songLessonId").value(5));

        verify(tabSectionService).findForSongLesson(5L);
    }

    @Test
    void getForSongLesson_whenNone_returnsEmptyList() throws Exception {
        when(tabSectionService.findForSongLesson(5L)).thenReturn(List.of());

        mockMvc.perform(get("/api/tab-sections/by-song-lesson/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        when(tabSectionService.create(any(TabSectionDTO.class))).thenReturn(tab);

        mockMvc.perform(post("/api/tab-sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8));

        verify(tabSectionService).create(argThat(dto -> "Intro".equals(dto.getIdentifier())
                && dto.getSongLessonId() == 5L));
    }

    @Test
    void create_withBlankIdentifier_returnsValidationError() throws Exception {
        TabSectionDTO dto = validRequest();
        // Whitespace only: violates @NotBlank but not @Size(min = 1), so the reported message is deterministic
        dto.setIdentifier("   ");
        expectCreateValidationError(dto, "identifier", "Tab section identifier is required");
    }

    @Test
    void create_withTooLongIdentifier_returnsValidationError() throws Exception {
        TabSectionDTO dto = validRequest();
        dto.setIdentifier("i".repeat(51));
        expectCreateValidationError(dto, "identifier", "Identifier must be between 1 and 50 characters");
    }

    @Test
    void create_withBlankContent_returnsValidationError() throws Exception {
        TabSectionDTO dto = validRequest();
        dto.setContent(null);
        expectCreateValidationError(dto, "content", "Tab section content is required");
    }

    @Test
    void create_withTooLongContent_returnsValidationError() throws Exception {
        TabSectionDTO dto = validRequest();
        dto.setContent("c".repeat(10001));
        expectCreateValidationError(dto, "content", "Content must be between 1 and 10000 characters");
    }

    @Test
    void create_withoutSongLessonId_returnsValidationError() throws Exception {
        TabSectionDTO dto = validRequest();
        dto.setSongLessonId(null);
        expectCreateValidationError(dto, "songLessonId", "Song lesson ID is required");
    }

    @Test
    void update_whenFound_returnsUpdatedTabSection() throws Exception {
        when(tabSectionService.update(eq(8L), any(TabSectionDTO.class))).thenReturn(Optional.of(tab));

        mockMvc.perform(put("/api/tab-sections/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));
    }

    @Test
    void update_whenMissing_returnsNotFound() throws Exception {
        when(tabSectionService.update(eq(8L), any(TabSectionDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tab-sections/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withInvalidBody_returnsValidationError() throws Exception {
        TabSectionDTO dto = validRequest();
        dto.setSongLessonId(null);

        mockMvc.perform(put("/api/tab-sections/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.songLessonId").exists());

        verify(tabSectionService, never()).update(anyLong(), any());
    }

    @Test
    void delete_whenFound_returnsNoContent() throws Exception {
        when(tabSectionService.delete(8L)).thenReturn(true);

        mockMvc.perform(delete("/api/tab-sections/8"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMissing_returnsNotFound() throws Exception {
        when(tabSectionService.delete(8L)).thenReturn(false);

        mockMvc.perform(delete("/api/tab-sections/8"))
                .andExpect(status().isNotFound());
    }
}
