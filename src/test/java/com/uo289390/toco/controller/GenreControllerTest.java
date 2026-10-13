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

import com.uo289390.toco.controller.GenreController;
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
import com.uo289390.toco.dto.GenreDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.GenreService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link GenreController}.
 * Verifies routing, status codes, JSON serialization and request validation
 * of every genre endpoint, with the service layer mocked.
 */
@WebMvcTest(GenreController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private GenreDTO genre;

    @BeforeEach
    void setUp() {
        genre = validRequest();
        genre.setId(1L);
    }

    private GenreDTO validRequest() {
        GenreDTO dto = new GenreDTO();
        dto.setTitle("Rock");
        dto.setImageUrl("https://example.com/rock.png");
        dto.setLevel(2);
        dto.setDescription("Loud guitars");
        dto.setBannerUrl("https://example.com/banner.png");
        dto.setWelcomeIntroVideoId("intro123");
        dto.setWelcomeSongVideoId("song123");
        dto.setLessonIds(List.of(4L, 5L));
        return dto;
    }

    private void expectCreateValidationError(GenreDTO dto, String field) throws Exception {
        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors." + field).exists());
    }

    @Test
    void getAll_returnsAllGenresWithItemType() throws Exception {
        when(genreService.findAll()).thenReturn(List.of(genre));

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Rock"))
                .andExpect(jsonPath("$[0].type").value("Genre"))
                .andExpect(jsonPath("$[0].level").value(2))
                .andExpect(jsonPath("$[0].description").value("Loud guitars"))
                .andExpect(jsonPath("$[0].bannerUrl").value("https://example.com/banner.png"))
                .andExpect(jsonPath("$[0].welcomeIntroVideoId").value("intro123"))
                .andExpect(jsonPath("$[0].welcomeSongVideoId").value("song123"))
                .andExpect(jsonPath("$[0].lessonIds", hasSize(2)));
    }

    @Test
    void getAll_returnsEmptyList() throws Exception {
        when(genreService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getByIds_returnsRequestedGenres() throws Exception {
        when(genreService.findByIds(List.of(1L, 3L))).thenReturn(List.of(genre));

        mockMvc.perform(post("/api/genres/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Rock"));

        verify(genreService).findByIds(List.of(1L, 3L));
    }

    @Test
    void getById_whenFound_returnsGenre() throws Exception {
        when(genreService.findById(1L)).thenReturn(Optional.of(genre));

        mockMvc.perform(get("/api/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("Genre"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        when(genreService.findById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/genres/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        when(genreService.create(any(GenreDTO.class))).thenReturn(genre);

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Rock"));

        verify(genreService).create(argThat(dto -> "Rock".equals(dto.getTitle())
                && "Loud guitars".equals(dto.getDescription())
                && dto.getLessonIds().equals(List.of(4L, 5L))));
    }

    @Test
    void create_withEmptyBannerUrl_isAccepted() throws Exception {
        GenreDTO dto = validRequest();
        dto.setBannerUrl("");
        when(genreService.create(any(GenreDTO.class))).thenReturn(genre);

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_withBlankTitle_returnsValidationError() throws Exception {
        GenreDTO dto = validRequest();
        dto.setTitle("");
        expectCreateValidationError(dto, "title");
        verify(genreService, never()).create(any());
    }

    @Test
    void create_withLevelOutOfRange_returnsValidationError() throws Exception {
        GenreDTO tooHigh = validRequest();
        tooHigh.setLevel(6);
        expectCreateValidationError(tooHigh, "level");

        GenreDTO negative = validRequest();
        negative.setLevel(-1);
        expectCreateValidationError(negative, "level");
    }

    @Test
    void create_withTooLongDescription_returnsValidationError() throws Exception {
        GenreDTO dto = validRequest();
        dto.setDescription("d".repeat(1001));
        expectCreateValidationError(dto, "description");
    }

    @Test
    void create_withInvalidBannerUrl_returnsValidationError() throws Exception {
        GenreDTO dto = validRequest();
        dto.setBannerUrl("ftp://example.com/banner.png");
        expectCreateValidationError(dto, "bannerUrl");
    }

    @Test
    void create_withTooLongVideoIds_returnsValidationError() throws Exception {
        GenreDTO intro = validRequest();
        intro.setWelcomeIntroVideoId("v".repeat(101));
        expectCreateValidationError(intro, "welcomeIntroVideoId");

        GenreDTO song = validRequest();
        song.setWelcomeSongVideoId("v".repeat(101));
        expectCreateValidationError(song, "welcomeSongVideoId");
    }

    @Test
    void update_whenFound_returnsUpdatedGenre() throws Exception {
        when(genreService.update(eq(1L), any(GenreDTO.class))).thenReturn(Optional.of(genre));

        mockMvc.perform(put("/api/genres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_whenMissing_returnsNotFound() throws Exception {
        when(genreService.update(eq(9L), any(GenreDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/genres/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withInvalidBody_returnsValidationError() throws Exception {
        GenreDTO dto = validRequest();
        dto.setBannerUrl("banner");

        mockMvc.perform(put("/api/genres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.bannerUrl").exists());

        verify(genreService, never()).update(anyLong(), any());
    }

    @Test
    void delete_whenFound_returnsNoContent() throws Exception {
        when(genreService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/genres/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMissing_returnsNotFound() throws Exception {
        when(genreService.delete(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/genres/1"))
                .andExpect(status().isNotFound());
    }
}
