package com.uo289390.toco.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.recommendation.SpotifyRecommendationService;

/**
 * Web layer tests for {@link SpotifyRecommendationController}.
 * Covers Spotify-based item and song recommendation endpoints.
 */
@WebMvcTest(SpotifyRecommendationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class SpotifyRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpotifyRecommendationService spotifyRecommendationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private SongDTO song(Long id) {
        SongDTO dto = new SongDTO();
        dto.setId(id);
        dto.setTitle("Song " + id);
        dto.setArtist("Artist");
        dto.setGenreIds(List.of(1L));
        return dto;
    }

    @Test
    void getItemsForUser_returnsItems() throws Exception {
        List<ItemDTO> items = List.of(song(1L), song(2L));
        when(spotifyRecommendationService.findItemsForUser(7L)).thenReturn(items);

        mockMvc.perform(get("/api/spotify/recommendations/items/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("Song"))
                .andExpect(jsonPath("$[0].genreIds[0]").value(1));
    }

    @Test
    void getItemsForUser_noSpotifyData_returnsEmptyArray() throws Exception {
        when(spotifyRecommendationService.findItemsForUser(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/spotify/recommendations/items/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getItemsForUser_spotifyNotConnected_returnsBadRequest() throws Exception {
        when(spotifyRecommendationService.findItemsForUser(7L))
                .thenThrow(new IllegalArgumentException("Spotify account not connected"));

        mockMvc.perform(get("/api/spotify/recommendations/items/7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Spotify account not connected"));
    }

    @Test
    void getSongsForUser_passesLimit() throws Exception {
        when(spotifyRecommendationService.findSongsForUser(7L, 3)).thenReturn(List.of(song(1L)));

        mockMvc.perform(get("/api/spotify/recommendations/songs/7").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].artist").value("Artist"));

        verify(spotifyRecommendationService).findSongsForUser(7L, 3);
    }

    @Test
    void getSongsForUser_missingLimit_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/spotify/recommendations/songs/7"))
                .andExpect(status().isBadRequest());

        verify(spotifyRecommendationService, never()).findSongsForUser(anyLong(), anyInt());
    }

    @Test
    void getSongsForUser_unexpectedError_returnsInternalServerError() throws Exception {
        when(spotifyRecommendationService.findSongsForUser(7L, 3)).thenThrow(new RuntimeException("Spotify down"));

        mockMvc.perform(get("/api/spotify/recommendations/songs/7").param("limit", "3"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }
}
