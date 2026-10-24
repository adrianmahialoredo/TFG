package com.uo289390.toco.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.controller.ItemController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.dto.ItemSummaryDTO;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.ItemService;

/**
 * Web layer tests for {@link ItemController}.
 * Covers listing, lookup by id(s), search and item-list based recommendations.
 */
@WebMvcTest(ItemController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private ItemSummaryDTO summary(Long id, String title, Item.Type type) {
        ItemSummaryDTO dto = new ItemSummaryDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setType(type);
        dto.setLevel(2);
        dto.setImageUrl("https://example.com/" + id + ".png");
        dto.setArtist(type == Item.Type.Song ? "Queen" : null);
        return dto;
    }

    @Test
    void getAll_returnsSummaries() throws Exception {
        when(itemService.findAll()).thenReturn(List.of(
                summary(1L, "Bohemian Rhapsody", Item.Type.Song),
                summary(2L, "Intro to chords", Item.Type.Lesson)));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Bohemian Rhapsody"))
                .andExpect(jsonPath("$[0].type").value("Song"))
                .andExpect(jsonPath("$[0].artist").value("Queen"))
                .andExpect(jsonPath("$[0].level").value(2))
                .andExpect(jsonPath("$[1].type").value("Lesson"));
    }

    @Test
    void getAll_empty_returnsEmptyArray() throws Exception {
        when(itemService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getByIds_returnsMatchingSummaries() throws Exception {
        when(itemService.findByIds(List.of(4L, 5L))).thenReturn(List.of(
                summary(4L, "Rock", Item.Type.Genre),
                summary(5L, "Rock guitar", Item.Type.InstrumentGenre)));

        mockMvc.perform(post("/api/items/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[4,5]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("Genre"))
                .andExpect(jsonPath("$[1].type").value("InstrumentGenre"));
    }

    @Test
    void getById_existing_returnsSummary() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(summary(1L, "Bohemian Rhapsody", Item.Type.Song)));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/1.png"));
    }

    @Test
    void getById_missing_returnsNotFound() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_passesQueryToService() throws Exception {
        when(itemService.search("queen")).thenReturn(List.of(summary(1L, "Bohemian Rhapsody", Item.Type.Song)));

        mockMvc.perform(get("/api/items/search").param("q", "queen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Bohemian Rhapsody"));

        verify(itemService).search("queen");
    }

    @Test
    void search_emptyQuery_isForwarded() throws Exception {
        when(itemService.search("")).thenReturn(List.of());

        mockMvc.perform(get("/api/items/search").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_queryWithSpacesAndAccents_isForwardedDecoded() throws Exception {
        when(itemService.search("canción rock")).thenReturn(List.of());

        mockMvc.perform(get("/api/items/search").param("q", "canción rock"))
                .andExpect(status().isOk());

        verify(itemService).search("canción rock");
    }

    @Test
    void search_missingQueryParam_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/items/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(itemService, org.mockito.Mockito.never()).search(anyString());
    }



    @Test
    void unknownSubPath_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/items/1/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
