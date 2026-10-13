package com.uo289390.toco.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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

import com.uo289390.toco.controller.ItemListController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.dto.ItemListDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.ItemListService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Web layer tests for {@link ItemListController}.
 * Covers item list CRUD and adding/removing items from a list.
 */
@WebMvcTest(ItemListController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class ItemListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemListService itemListService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private ItemListDTO list(Long id, Long... itemIds) {
        ItemListDTO dto = new ItemListDTO();
        dto.setId(id);
        dto.setName("List " + id);
        dto.setImageUrl("https://example.com/list.png");
        dto.setUserId(7L);
        dto.setItemIds(List.of(itemIds));
        return dto;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void getAll_returnsLists() throws Exception {
        when(itemListService.findAll()).thenReturn(List.of(list(1L, 10L, 11L), list(2L)));

        mockMvc.perform(get("/api/item-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("List 1"))
                .andExpect(jsonPath("$[0].userId").value(7))
                .andExpect(jsonPath("$[0].itemIds.length()").value(2))
                .andExpect(jsonPath("$[1].itemIds.length()").value(0));
    }

    @Test
    void getByIds_returnsMatchingLists() throws Exception {
        when(itemListService.findByIds(List.of(1L, 2L))).thenReturn(List.of(list(1L), list(2L)));

        mockMvc.perform(post("/api/item-lists/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getById_existing_returnsList() throws Exception {
        when(itemListService.findById(1L)).thenReturn(Optional.of(list(1L, 5L)));

        mockMvc.perform(get("/api/item-lists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.itemIds[0]").value(5));
    }

    @Test
    void getById_missing_returnsNotFound() throws Exception {
        when(itemListService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/item-lists/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIdsForUser_returnsIds() throws Exception {
        when(itemListService.findIdsForUser(7L)).thenReturn(List.of(1L, 2L, 3L));

        mockMvc.perform(get("/api/item-lists/by-user/7/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[2]").value(3));
    }

    @Test
    void getIdsForUser_noLists_returnsEmptyArray() throws Exception {
        when(itemListService.findIdsForUser(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/item-lists/by-user/7/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_returnsCreatedList() throws Exception {
        ItemListDTO request = list(null);
        when(itemListService.create(any(ItemListDTO.class))).thenReturn(list(20L));

        mockMvc.perform(post("/api/item-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20));

        verify(itemListService).create(argThat(dto -> "List null".equals(dto.getName()) && dto.getUserId() == 7L));
    }

    @Test
    void create_withoutItemIds_defaultsToEmptyList() throws Exception {
        when(itemListService.create(any(ItemListDTO.class))).thenReturn(list(20L));

        mockMvc.perform(post("/api/item-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Favourites\",\"userId\":7}"))
                .andExpect(status().isCreated());

        verify(itemListService).create(argThat(dto -> dto.getItemIds() != null && dto.getItemIds().isEmpty()));
    }

    @Test
    void create_unknownUser_returnsNotFound() throws Exception {
        when(itemListService.create(any(ItemListDTO.class)))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(post("/api/item-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(list(null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void update_existing_returnsUpdatedList() throws Exception {
        ItemListDTO updated = list(1L);
        updated.setName("Renamed");
        when(itemListService.update(eq(1L), any(ItemListDTO.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/item-lists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"));
    }

    @Test
    void update_missing_returnsNotFound() throws Exception {
        when(itemListService.update(eq(1L), any(ItemListDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/item-lists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(list(1L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existing_returnsNoContent() throws Exception {
        when(itemListService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/item-lists/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void delete_missing_returnsNotFound() throws Exception {
        when(itemListService.delete(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/item-lists/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItem_existing_returnsUpdatedList() throws Exception {
        when(itemListService.addItem(1L, 50L)).thenReturn(Optional.of(list(1L, 50L)));

        mockMvc.perform(post("/api/item-lists/1/items/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemIds[0]").value(50));

        verify(itemListService).addItem(1L, 50L);
    }

    @Test
    void addItem_missing_returnsNotFound() throws Exception {
        when(itemListService.addItem(1L, 50L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/item-lists/1/items/50"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItem_invalidItem_returnsBadRequest() throws Exception {
        when(itemListService.addItem(1L, 50L)).thenThrow(new IllegalArgumentException("Item already in list"));

        mockMvc.perform(post("/api/item-lists/1/items/50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Item already in list"));
    }

    @Test
    void removeItem_existing_returnsUpdatedList() throws Exception {
        when(itemListService.removeItem(1L, 50L)).thenReturn(Optional.of(list(1L)));

        mockMvc.perform(delete("/api/item-lists/1/items/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemIds.length()").value(0));

        verify(itemListService).removeItem(1L, 50L);
    }

    @Test
    void removeItem_missing_returnsNotFound() throws Exception {
        when(itemListService.removeItem(1L, 50L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/item-lists/1/items/50"))
                .andExpect(status().isNotFound());
    }
}
