package com.uo289390.toco.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashSet;
import java.util.List;

import com.uo289390.toco.controller.RecommendationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.recommendation.GenreRecommendationService;
import com.uo289390.toco.service.recommendation.InstrumentGenreRecommendationService;
import com.uo289390.toco.service.recommendation.ItemRecommendationService;
import com.uo289390.toco.service.recommendation.LessonRecommendationService;
import com.uo289390.toco.service.recommendation.SongRecommendationService;
import com.uo289390.toco.utils.RecommendationConstants;
import com.uo289390.toco.utils.mapper.DtoMapper;

/**
 * Web layer tests for {@link RecommendationController}.
 * Verifies that each endpoint delegates to the right recommendation service with the
 * configured limits and that entities are mapped to DTOs preserving their order.
 */
@WebMvcTest(RecommendationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
// Functional tests act as an admin; access rules are covered in security.AccessRulesTest
@WithMockUser(roles = "ADMIN")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemRecommendationService itemRecommendationService;

    @MockitoBean
    private LessonRecommendationService lessonRecommendationService;

    @MockitoBean
    private SongRecommendationService songRecommendationService;

    @MockitoBean
    private InstrumentGenreRecommendationService instrumentGenreRecommendationService;

    @MockitoBean
    private GenreRecommendationService genreRecommendationService;

    @MockitoBean
    private DtoMapper dtoMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    private LessonDTO lessonDto(Long id) {
        LessonDTO dto = new LessonDTO();
        dto.setId(id);
        dto.setTitle("Lesson " + id);
        return dto;
    }

    private SongDTO songDto(Long id) {
        SongDTO dto = new SongDTO();
        dto.setId(id);
        dto.setTitle("Song " + id);
        dto.setArtist("Artist " + id);
        return dto;
    }

    @Test
    void getForItemList_returnsMixedItemTypes() throws Exception {
        List<ItemDTO> items = List.of(songDto(1L), lessonDto(2L));
        when(itemRecommendationService.findForItemList(5L)).thenReturn(items);

        mockMvc.perform(get("/api/recommendations/items/by-item-list/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("Song"))
                .andExpect(jsonPath("$[0].artist").value("Artist 1"))
                .andExpect(jsonPath("$[1].type").value("Lesson"));
    }

    @Test
    void getForItemList_unknownList_returnsNotFound() throws Exception {
        when(itemRecommendationService.findForItemList(5L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Item list not found"));

        mockMvc.perform(get("/api/recommendations/items/by-item-list/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item list not found"));
    }

    @Test
    void getForUser_returnsItems() throws Exception {
        when(itemRecommendationService.findForUser(7L)).thenReturn(List.of(lessonDto(3L)));

        mockMvc.perform(get("/api/recommendations/items/by-user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void getForUser_noRecommendations_returnsEmptyArray() throws Exception {
        when(itemRecommendationService.findForUser(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/recommendations/items/by-user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getLessonsForUser_usesConfiguredCountAndMapsInOrder() throws Exception {
        Lesson first = new Lesson();
        Lesson second = new Lesson();
        LinkedHashSet<Lesson> lessons = new LinkedHashSet<>(List.of(first, second));
        when(lessonRecommendationService.findForUser(7L, RecommendationConstants.RECOMMENDED_LESSONS_COUNT))
                .thenReturn(lessons);
        when(dtoMapper.lessonToLessonDto(first)).thenReturn(lessonDto(10L));
        when(dtoMapper.lessonToLessonDto(second)).thenReturn(lessonDto(11L));

        mockMvc.perform(get("/api/recommendations/lessons/by-user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].id").value(11));

        verify(lessonRecommendationService).findForUser(7L, RecommendationConstants.RECOMMENDED_LESSONS_COUNT);
    }

    @Test
    void getLessonsForUser_unknownUser_returnsNotFound() throws Exception {
        when(lessonRecommendationService.findForUser(anyLong(), anyInt()))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/recommendations/lessons/by-user/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getForGenre_usesByGenreCount() throws Exception {
        when(lessonRecommendationService.findForGenre(3L, RecommendationConstants.BY_GENRE_RECOMMENDED_LESSONS_COUNT))
                .thenReturn(List.of(lessonDto(1L), lessonDto(2L)));

        mockMvc.perform(get("/api/recommendations/lessons/by-genre/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(lessonRecommendationService).findForGenre(3L, RecommendationConstants.BY_GENRE_RECOMMENDED_LESSONS_COUNT);
    }

    @Test
    void getSongsForUser_passesLimitAndMapsInOrder() throws Exception {
        Song first = new Song();
        Song second = new Song();
        LinkedHashSet<Song> songs = new LinkedHashSet<>(List.of(first, second));
        when(songRecommendationService.findForUser(7L, 4)).thenReturn(songs);
        when(dtoMapper.songToSongDto(first)).thenReturn(songDto(20L));
        when(dtoMapper.songToSongDto(second)).thenReturn(songDto(21L));

        mockMvc.perform(get("/api/recommendations/songs/by-user/7").param("limit", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].type").value("Song"))
                .andExpect(jsonPath("$[1].id").value(21));

        verify(songRecommendationService).findForUser(7L, 4);
    }

    @Test
    void getSongsForUser_emptyResult_returnsEmptyArray() throws Exception {
        when(songRecommendationService.findForUser(7L, 0)).thenReturn(new LinkedHashSet<>());

        mockMvc.perform(get("/api/recommendations/songs/by-user/7").param("limit", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getSongsForUser_missingLimit_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/recommendations/songs/by-user/7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(songRecommendationService, never()).findForUser(anyLong(), anyInt());
    }

    @Test
    void getFavouriteInstrumentGenreCombinationIds_usesConfiguredCountAndReturnsIdsInOrder() throws Exception {
        InstrumentGenre first = new InstrumentGenre();
        first.setId(30L);
        InstrumentGenre second = new InstrumentGenre();
        second.setId(31L);
        when(instrumentGenreRecommendationService.findFavouriteCombinationsForUser(7L, RecommendationConstants.FAVOURITE_INSTRUMENT_GENRE_COMBINATIONS_COUNT))
                .thenReturn(new LinkedHashSet<>(List.of(first, second)));

        mockMvc.perform(get("/api/recommendations/instrument-genres/favourite-combinations/by-user/7/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value(30))
                .andExpect(jsonPath("$[1]").value(31));
    }

    @Test
    void getFavouriteInstrumentGenreCombinationIds_unknownUser_returnsNotFound() throws Exception {
        when(instrumentGenreRecommendationService.findFavouriteCombinationsForUser(anyLong(), anyInt()))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/recommendations/instrument-genres/favourite-combinations/by-user/7/ids"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMostUsedGenreInItemListsIds_usesConfiguredCountAndReturnsIdsInOrder() throws Exception {
        Genre first = new Genre();
        first.setId(40L);
        Genre second = new Genre();
        second.setId(41L);
        when(genreRecommendationService.findMostUsedInItemListsForUser(7L, RecommendationConstants.MOST_USED_GENRES_IN_ITEM_LISTS_COUNT))
                .thenReturn(new LinkedHashSet<>(List.of(first, second)));

        mockMvc.perform(get("/api/recommendations/genres/most-used-in-item-lists/by-user/7/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value(40))
                .andExpect(jsonPath("$[1]").value(41));
    }

    @Test
    void getMostUsedGenreInItemListsIds_unknownUser_returnsNotFound() throws Exception {
        when(genreRecommendationService.findMostUsedInItemListsForUser(anyLong(), anyInt()))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/recommendations/genres/most-used-in-item-lists/by-user/7/ids"))
                .andExpect(status().isNotFound());
    }
}
