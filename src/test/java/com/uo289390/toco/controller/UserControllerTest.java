package com.uo289390.toco.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.uo289390.toco.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.dto.UserDto;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Web layer tests for {@link UserController}.
 * Covers user CRUD and favourite genre/instrument management endpoints.
 */
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private UserDto user(Long id) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setEmail("user" + id + "@example.com");
        dto.setName("User " + id);
        dto.setUsername("user" + id);
        dto.setFavouriteGenresIds(List.of(1L, 2L));
        dto.setFavouriteInstrumentsIds(List.of(3L, 4L));
        return dto;
    }

    private UserDto validCreateRequest() {
        UserDto dto = user(null);
        dto.setPassword("Password1!");
        return dto;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    // ---------- reads ----------

    @Test
    void getAll_returnsAllUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of(user(1L), user(2L)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    void getAll_noUsers_returnsEmptyArray() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAll_doesNotExposePassword() throws Exception {
        UserDto withPassword = user(1L);
        withPassword.setPassword("hashed");
        when(userService.findAll()).thenReturn(List.of(withPassword));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spotifyConnected").value(false));
    }

    @Test
    void getByIds_returnsRequestedUsers() throws Exception {
        when(userService.findByIds(List.of(1L, 3L))).thenReturn(List.of(user(1L), user(3L)));

        mockMvc.perform(post("/api/users/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].id").value(3));
    }

    @Test
    void getByIds_emptyList_returnsEmptyArray() throws Exception {
        when(userService.findByIds(List.of())).thenReturn(List.of());

        mockMvc.perform(post("/api/users/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getById_existing_returnsUser() throws Exception {
        when(userService.findById(5L)).thenReturn(Optional.of(user(5L)));

        mockMvc.perform(get("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("user5@example.com"))
                .andExpect(jsonPath("$.favouriteGenresIds.length()").value(2));
    }

    @Test
    void getById_missing_returnsNotFound() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_serviceThrowsEntityNotFound_returnsNotFoundBody() throws Exception {
        when(userService.findById(99L)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getById_unexpectedError_returnsInternalServerError() throws Exception {
        when(userService.findById(1L)).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    // ---------- favourite genres ----------

    @Test
    void getFavoriteGenres_existingUser_returnsIds() throws Exception {
        when(userService.findFavoriteGenreIdsForUser(1L)).thenReturn(Optional.of(List.of(10L, 20L)));

        mockMvc.perform(get("/api/users/1/favorite-genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(10))
                .andExpect(jsonPath("$[1]").value(20));
    }

    @Test
    void getFavoriteGenres_missingUser_returnsNotFound() throws Exception {
        when(userService.findFavoriteGenreIdsForUser(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1/favorite-genres"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFavoriteGenre_existing_returnsUpdatedUser() throws Exception {
        when(userService.addFavoriteGenre(1L, 30L)).thenReturn(Optional.of(user(1L)));

        mockMvc.perform(post("/api/users/1/favorite-genres/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService).addFavoriteGenre(1L, 30L);
    }

    @Test
    void addFavoriteGenre_missing_returnsNotFound() throws Exception {
        when(userService.addFavoriteGenre(1L, 30L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/1/favorite-genres/30"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFavoriteGenre_existing_returnsUpdatedUser() throws Exception {
        when(userService.removeFavoriteGenre(1L, 10L)).thenReturn(Optional.of(user(1L)));

        mockMvc.perform(delete("/api/users/1/favorite-genres/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void removeFavoriteGenre_missing_returnsNotFound() throws Exception {
        when(userService.removeFavoriteGenre(1L, 10L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/1/favorite-genres/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFavoriteGenre_belowMinimum_returnsBadRequest() throws Exception {
        when(userService.removeFavoriteGenre(1L, 10L))
                .thenThrow(new IllegalArgumentException("At least 2 favourite genres are required"));

        mockMvc.perform(delete("/api/users/1/favorite-genres/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least 2 favourite genres are required"));
    }

    // ---------- favourite instruments ----------

    @Test
    void getFavoriteInstruments_existingUser_returnsIds() throws Exception {
        when(userService.findFavoriteInstrumentIdsForUser(2L)).thenReturn(Optional.of(List.of(3L)));

        mockMvc.perform(get("/api/users/2/favorite-instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value(3));
    }

    @Test
    void getFavoriteInstruments_missingUser_returnsNotFound() throws Exception {
        when(userService.findFavoriteInstrumentIdsForUser(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/2/favorite-instruments"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFavoriteInstrument_existing_returnsUpdatedUser() throws Exception {
        when(userService.addFavoriteInstrument(2L, 8L)).thenReturn(Optional.of(user(2L)));

        mockMvc.perform(post("/api/users/2/favorite-instruments/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(userService).addFavoriteInstrument(2L, 8L);
    }

    @Test
    void addFavoriteInstrument_missing_returnsNotFound() throws Exception {
        when(userService.addFavoriteInstrument(2L, 8L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/2/favorite-instruments/8"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFavoriteInstrument_existing_returnsUpdatedUser() throws Exception {
        when(userService.removeFavoriteInstrument(2L, 3L)).thenReturn(Optional.of(user(2L)));

        mockMvc.perform(delete("/api/users/2/favorite-instruments/3"))
                .andExpect(status().isOk());
    }

    @Test
    void removeFavoriteInstrument_missing_returnsNotFound() throws Exception {
        when(userService.removeFavoriteInstrument(2L, 3L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/2/favorite-instruments/3"))
                .andExpect(status().isNotFound());
    }

    // ---------- create ----------

    @Test
    void create_validUser_returnsCreated() throws Exception {
        when(userService.create(any(UserDto.class))).thenReturn(user(11L));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    void create_ignoresClientSuppliedSpotifyConnectedFlag() throws Exception {
        when(userService.create(any(UserDto.class))).thenReturn(user(11L));
        String body = json(validCreateRequest()).replace("\"spotifyConnected\":false", "\"spotifyConnected\":true");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(userService).create(org.mockito.ArgumentMatchers.argThat(dto -> !dto.isSpotifyConnected()));
    }

    @Test
    void create_withoutPassword_returnsBadRequest() throws Exception {
        UserDto dto = validCreateRequest();
        dto.setPassword(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));

        verifyNoInteractions(userService);
    }

    @Test
    void create_multipleInvalidFields_reportsAllFieldErrors() throws Exception {
        UserDto dto = new UserDto();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.favouriteGenresIds").exists())
                .andExpect(jsonPath("$.errors.favouriteInstrumentsIds").exists());
    }

    @Test
    void create_passwordWithSpaces_returnsBadRequest() throws Exception {
        UserDto dto = validCreateRequest();
        dto.setPassword("Pass word1!");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    // ---------- update ----------

    @Test
    void update_validWithoutPassword_returnsUpdatedUser() throws Exception {
        UserDto updated = user(4L);
        updated.setName("Renamed");
        when(userService.update(eq(4L), any(UserDto.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/users/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(user(4L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"));
    }

    @Test
    void update_missingUser_returnsNotFound() throws Exception {
        when(userService.update(eq(4L), any(UserDto.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(user(4L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_invalidPassword_returnsBadRequest() throws Exception {
        UserDto dto = user(4L);
        dto.setPassword("short");

        mockMvc.perform(put("/api/users/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());

        verifyNoInteractions(userService);
    }

    @Test
    void update_invalidEmail_returnsBadRequest() throws Exception {
        UserDto dto = user(4L);
        dto.setEmail("bad");

        mockMvc.perform(put("/api/users/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"));
    }

    @Test
    void update_duplicateUsername_returnsBadRequest() throws Exception {
        when(userService.update(eq(4L), any(UserDto.class)))
                .thenThrow(new IllegalArgumentException("Username already in use"));

        mockMvc.perform(put("/api/users/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(user(4L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already in use"));
    }

    // ---------- delete ----------

    @Test
    void delete_existing_returnsNoContent() throws Exception {
        when(userService.delete(3L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/3"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void delete_missing_returnsNotFound() throws Exception {
        when(userService.delete(3L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unsupportedMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(patch("/api/users/3"))
                .andExpect(status().isMethodNotAllowed());
    }
}
