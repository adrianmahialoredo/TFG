package com.uo289390.toco.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.controller.AuthController;
import com.uo289390.toco.controller.GenreController;
import com.uo289390.toco.controller.InstrumentController;
import com.uo289390.toco.controller.ItemController;
import com.uo289390.toco.controller.ItemListController;
import com.uo289390.toco.controller.RecommendationController;
import com.uo289390.toco.controller.SpotifyRecommendationController;
import com.uo289390.toco.controller.TuneController;
import com.uo289390.toco.controller.UserController;
import com.uo289390.toco.model.ItemList;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.ChordRepository;
import com.uo289390.toco.repository.GenreRepository;
import com.uo289390.toco.repository.InstrumentGenreRepository;
import com.uo289390.toco.repository.InstrumentRepository;
import com.uo289390.toco.repository.ItemListRepository;
import com.uo289390.toco.repository.ItemRepository;
import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.repository.LessonSectionRepository;
import com.uo289390.toco.repository.SongLessonRepository;
import com.uo289390.toco.repository.SongRepository;
import com.uo289390.toco.repository.SpotifyUserRepository;
import com.uo289390.toco.repository.TabSectionRepository;
import com.uo289390.toco.repository.TuneRepository;
import com.uo289390.toco.repository.UserRepository;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import com.uo289390.toco.service.EmailVerificationService;
import com.uo289390.toco.service.GenreService;
import com.uo289390.toco.service.InstrumentService;
import com.uo289390.toco.service.ItemListService;
import com.uo289390.toco.service.ItemService;
import com.uo289390.toco.service.recommendation.GenreRecommendationService;
import com.uo289390.toco.service.recommendation.InstrumentGenreRecommendationService;
import com.uo289390.toco.service.recommendation.ItemRecommendationService;
import com.uo289390.toco.service.recommendation.LessonRecommendationService;
import com.uo289390.toco.service.recommendation.SongRecommendationService;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.service.recommendation.SpotifyRecommendationService;
import com.uo289390.toco.service.TuneService;
import com.uo289390.toco.service.UserService;
import com.uo289390.toco.utils.EntityResolver;

/**
 * Access rules of {@link SecurityConfig} and the ownership checks ({@link OwnershipChecker})
 * on real controllers with their services mocked.
 *
 * "Allowed" means the request got past security (any status other than 401/403): what the
 * mocked service answers afterwards is covered by each controller's own test.
 */
@WebMvcTest(controllers = {AuthController.class, GenreController.class, UserController.class,
        ItemListController.class, SpotifyRecommendationController.class, RecommendationController.class,
        ItemController.class, InstrumentController.class, TuneController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, OwnershipChecker.class, GlobalExceptionHandler.class})
class AccessRulesTest {

    private static final long OWNER_ID = 7L;
    private static final long OTHER_ID = 8L;
    private static final long LIST_ID = 100L;
    private static final long MISSING_LIST_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private InstrumentService instrumentService;

    @MockitoBean
    private ItemListService itemListService;

    @MockitoBean
    private SpotifyRecommendationService spotifyRecommendationService;

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
    private ItemService itemService;

    @MockitoBean
    private TuneService tuneService;

    private ItemListRepository itemListRepository;

    @BeforeEach
    void setUp() {
        // OwnershipChecker reads the list through the static EntityResolver
        itemListRepository = mock(ItemListRepository.class);
        new EntityResolver(mock(ChordRepository.class), mock(GenreRepository.class), mock(InstrumentRepository.class),
                mock(InstrumentGenreRepository.class), mock(ItemRepository.class), itemListRepository,
                mock(LessonRepository.class), mock(LessonSectionRepository.class), mock(SongRepository.class),
                mock(SongLessonRepository.class), mock(SpotifyUserRepository.class), mock(TabSectionRepository.class),
                mock(TuneRepository.class), mock(UserRepository.class));

        User owner = new User();
        owner.setId(OWNER_ID);
        ItemList list = new ItemList();
        list.setId(LIST_ID);
        list.setUser(owner);
        when(itemListRepository.findById(LIST_ID)).thenReturn(Optional.of(list));
        when(itemListRepository.findById(MISSING_LIST_ID)).thenReturn(Optional.empty());
    }

    private static RequestPostProcessor asUser(long id) {
        return user(String.valueOf(id)).roles("USER");
    }

    private static RequestPostProcessor asAdmin() {
        return user("1").roles("ADMIN");
    }

    private void expectAllowed(RequestBuilder request) throws Exception {
        int status = mockMvc.perform(request).andReturn().getResponse().getStatus();
        assertTrue(status != 401 && status != 403, "Expected the request to pass security, got " + status);
    }

    private static String itemListBody(long userId) {
        return "{\"name\":\"Favoritas\",\"userId\":" + userId + ",\"itemIds\":[]}";
    }

    @Nested
    class PublicRoutes {

        @Test
        void login_withoutToken_isAllowed() throws Exception {
            expectAllowed(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"a@b.com\",\"password\":\"x\"}"));
        }

        @Test
        void verificationCode_withoutToken_isAllowed() throws Exception {
            expectAllowed(post("/api/auth/verification-code").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"a@b.com\"}"));
        }

        @Test
        void instrumentList_withoutToken_isAllowed() throws Exception {
            // Needed by the registration form, before the user has a token
            expectAllowed(get("/api/instruments"));
        }

        @Test
        void instrumentById_withoutToken_isUnauthorized() throws Exception {
            mockMvc.perform(get("/api/instruments/1")).andExpect(status().isUnauthorized());
        }

        @Test
        void genreList_withoutToken_isAllowed() throws Exception {
            // Needed by the registration form, before the user has a token
            expectAllowed(get("/api/genres"));
        }

        @Test
        void catalogRead_withoutToken_isUnauthorized() throws Exception {
            mockMvc.perform(get("/api/genres/1")).andExpect(status().isUnauthorized());
        }

        @Test
        void userData_withoutToken_isUnauthorized() throws Exception {
            mockMvc.perform(get("/api/users/" + OWNER_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class Catalog {

        @Test
        void read_asUser_isAllowed() throws Exception {
            expectAllowed(get("/api/genres").with(asUser(OWNER_ID)));
        }

        @Test
        void batchRead_asUser_isAllowed() throws Exception {
            // POST /by-ids is a read even though it uses POST
            expectAllowed(post("/api/genres/by-ids").with(asUser(OWNER_ID))
                    .contentType(MediaType.APPLICATION_JSON).content("[1,2]"));
        }

        @Test
        void create_asUser_isForbidden() throws Exception {
            mockMvc.perform(post("/api/genres").with(asUser(OWNER_ID))
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void update_asUser_isForbidden() throws Exception {
            mockMvc.perform(put("/api/genres/1").with(asUser(OWNER_ID))
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void delete_asUser_isForbidden() throws Exception {
            mockMvc.perform(delete("/api/genres/1").with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void delete_asAdmin_isAllowed() throws Exception {
            expectAllowed(delete("/api/genres/1").with(asAdmin()));
        }

        @Test
        void tunesRead_asUser_isAllowed() throws Exception {
            // The tuner reads the tunings with the user's token
            expectAllowed(get("/api/tunes").with(asUser(OWNER_ID)));
            expectAllowed(get("/api/tunes/by-instrument/1").with(asUser(OWNER_ID)));
        }

        @Test
        void tunesRead_withoutToken_isUnauthorized() throws Exception {
            mockMvc.perform(get("/api/tunes")).andExpect(status().isUnauthorized());
        }

        @Test
        void tunesCreate_asUser_isForbidden() throws Exception {
            mockMvc.perform(post("/api/tunes").with(asUser(OWNER_ID))
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void tunesDelete_asAdmin_isAllowed() throws Exception {
            expectAllowed(delete("/api/tunes/1").with(asAdmin()));
        }
    }

    @Nested
    class Users {

        @Test
        void topArtists_ofOwnUser_isAllowed() throws Exception {
            expectAllowed(get("/api/users/" + OWNER_ID + "/top-artists").with(asUser(OWNER_ID)));
        }

        @Test
        void topArtists_ofAnotherUser_isForbidden() throws Exception {
            mockMvc.perform(get("/api/users/" + OTHER_ID + "/top-artists").with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void listAll_asUser_isForbidden() throws Exception {
            mockMvc.perform(get("/api/users").with(asUser(OWNER_ID))).andExpect(status().isForbidden());
        }

        @Test
        void listAll_asAdmin_isAllowed() throws Exception {
            expectAllowed(get("/api/users").with(asAdmin()));
        }

        @Test
        void readSelf_isAllowed() throws Exception {
            expectAllowed(get("/api/users/" + OWNER_ID).with(asUser(OWNER_ID)));
        }

        @Test
        void readOther_asUser_isForbidden() throws Exception {
            mockMvc.perform(get("/api/users/" + OTHER_ID).with(asUser(OWNER_ID))).andExpect(status().isForbidden());
        }

        @Test
        void readOther_asAdmin_isAllowed() throws Exception {
            expectAllowed(get("/api/users/" + OTHER_ID).with(asAdmin()));
        }

        @Test
        void batchRead_onlySelf_isAllowed() throws Exception {
            expectAllowed(post("/api/users/by-ids").with(asUser(OWNER_ID))
                    .contentType(MediaType.APPLICATION_JSON).content("[" + OWNER_ID + "]"));
        }

        @Test
        void batchRead_includingOther_isForbidden() throws Exception {
            mockMvc.perform(post("/api/users/by-ids").with(asUser(OWNER_ID))
                            .contentType(MediaType.APPLICATION_JSON).content("[" + OWNER_ID + "," + OTHER_ID + "]"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void favoriteGenresOfOther_isForbidden() throws Exception {
            mockMvc.perform(get("/api/users/" + OTHER_ID + "/favorite-genres").with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void favoriteInstrumentsOfSelf_isAllowed() throws Exception {
            expectAllowed(get("/api/users/" + OWNER_ID + "/favorite-instruments").with(asUser(OWNER_ID)));
        }

        @Test
        void addFavoriteGenre_onSelf_isAllowed() throws Exception {
            expectAllowed(post("/api/users/" + OWNER_ID + "/favorite-genres/3").with(asUser(OWNER_ID)));
        }

        @Test
        void addFavoriteGenre_onOther_isForbiddenWithJsonBody() throws Exception {
            mockMvc.perform(post("/api/users/" + OTHER_ID + "/favorite-genres/3").with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        void removeFavoriteInstrument_onOther_isForbidden() throws Exception {
            mockMvc.perform(delete("/api/users/" + OTHER_ID + "/favorite-instruments/2").with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void delete_onOther_isForbidden() throws Exception {
            mockMvc.perform(delete("/api/users/" + OTHER_ID).with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void delete_onOther_asAdmin_isAllowed() throws Exception {
            expectAllowed(delete("/api/users/" + OTHER_ID).with(asAdmin()));
        }
    }

    @Nested
    class ItemLists {

        @Test
        void listAll_asUser_isForbidden() throws Exception {
            mockMvc.perform(get("/api/item-lists").with(asUser(OWNER_ID))).andExpect(status().isForbidden());
        }

        @Test
        void listAll_asAdmin_isAllowed() throws Exception {
            expectAllowed(get("/api/item-lists").with(asAdmin()));
        }

        @Test
        void read_ownList_isAllowed() throws Exception {
            expectAllowed(get("/api/item-lists/" + LIST_ID).with(asUser(OWNER_ID)));
        }

        @Test
        void read_otherUsersList_isForbidden() throws Exception {
            mockMvc.perform(get("/api/item-lists/" + LIST_ID).with(asUser(OTHER_ID))).andExpect(status().isForbidden());
        }

        @Test
        void batchRead_ownLists_isAllowed() throws Exception {
            expectAllowed(post("/api/item-lists/by-ids").with(asUser(OWNER_ID))
                    .contentType(MediaType.APPLICATION_JSON).content("[" + LIST_ID + "," + MISSING_LIST_ID + "]"));
        }

        @Test
        void batchRead_otherUsersList_isForbidden() throws Exception {
            mockMvc.perform(post("/api/item-lists/by-ids").with(asUser(OTHER_ID))
                            .contentType(MediaType.APPLICATION_JSON).content("[" + LIST_ID + "]"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void idsForSelf_isAllowed() throws Exception {
            expectAllowed(get("/api/item-lists/by-user/" + OWNER_ID + "/ids").with(asUser(OWNER_ID)));
        }

        @Test
        void idsForOther_isForbidden() throws Exception {
            mockMvc.perform(get("/api/item-lists/by-user/" + OTHER_ID + "/ids").with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void create_forSelf_isAllowed() throws Exception {
            expectAllowed(post("/api/item-lists").with(asUser(OWNER_ID))
                    .contentType(MediaType.APPLICATION_JSON).content(itemListBody(OWNER_ID)));
        }

        @Test
        void create_forOther_isForbidden() throws Exception {
            mockMvc.perform(post("/api/item-lists").with(asUser(OWNER_ID))
                            .contentType(MediaType.APPLICATION_JSON).content(itemListBody(OTHER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void update_ownList_isAllowed() throws Exception {
            expectAllowed(put("/api/item-lists/" + LIST_ID).with(asUser(OWNER_ID))
                    .contentType(MediaType.APPLICATION_JSON).content(itemListBody(OWNER_ID)));
        }

        @Test
        void update_ownList_handingItToOther_isForbidden() throws Exception {
            mockMvc.perform(put("/api/item-lists/" + LIST_ID).with(asUser(OWNER_ID))
                            .contentType(MediaType.APPLICATION_JSON).content(itemListBody(OTHER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void update_otherUsersList_isForbidden() throws Exception {
            mockMvc.perform(put("/api/item-lists/" + LIST_ID).with(asUser(OTHER_ID))
                            .contentType(MediaType.APPLICATION_JSON).content(itemListBody(OTHER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void delete_otherUsersList_isForbidden() throws Exception {
            mockMvc.perform(delete("/api/item-lists/" + LIST_ID).with(asUser(OTHER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void addItem_ownList_isAllowed() throws Exception {
            expectAllowed(post("/api/item-lists/" + LIST_ID + "/items/5").with(asUser(OWNER_ID)));
        }

        @Test
        void removeItem_otherUsersList_isForbidden() throws Exception {
            mockMvc.perform(delete("/api/item-lists/" + LIST_ID + "/items/5").with(asUser(OTHER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void delete_missingList_reachesControllerForNotFound() throws Exception {
            mockMvc.perform(delete("/api/item-lists/" + MISSING_LIST_ID).with(asUser(OTHER_ID)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void delete_otherUsersList_asAdmin_isAllowed() throws Exception {
            expectAllowed(delete("/api/item-lists/" + LIST_ID).with(asAdmin()));
        }
    }

    @Nested
    class Recommendations {

        @Test
        void forOwnList_isAllowed() throws Exception {
            expectAllowed(get("/api/recommendations/items/by-item-list/" + LIST_ID).with(asUser(OWNER_ID)));
        }

        @Test
        void forOtherUsersList_isForbidden() throws Exception {
            mockMvc.perform(get("/api/recommendations/items/by-item-list/" + LIST_ID).with(asUser(OTHER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void itemsForOther_isForbidden() throws Exception {
            mockMvc.perform(get("/api/recommendations/items/by-user/" + OTHER_ID).with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void lessonsForSelf_isAllowed() throws Exception {
            expectAllowed(get("/api/recommendations/lessons/by-user/" + OWNER_ID).with(asUser(OWNER_ID)));
        }

        @Test
        void lessonsForOther_isForbidden() throws Exception {
            mockMvc.perform(get("/api/recommendations/lessons/by-user/" + OTHER_ID).with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void songsForOther_isForbidden() throws Exception {
            mockMvc.perform(get("/api/recommendations/songs/by-user/" + OTHER_ID).param("limit", "5")
                            .with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void favouriteCombinationsForSelf_isAllowed() throws Exception {
            expectAllowed(get("/api/recommendations/instrument-genres/favourite-combinations/by-user/" + OWNER_ID + "/ids")
                    .with(asUser(OWNER_ID)));
        }

        @Test
        void favouriteCombinationsForOther_isForbidden() throws Exception {
            mockMvc.perform(get("/api/recommendations/instrument-genres/favourite-combinations/by-user/" + OTHER_ID + "/ids")
                            .with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void mostUsedGenresForSelf_isAllowed() throws Exception {
            expectAllowed(get("/api/recommendations/genres/most-used-in-item-lists/by-user/" + OWNER_ID + "/ids")
                    .with(asUser(OWNER_ID)));
        }

        @Test
        void mostUsedGenresForOther_isForbidden() throws Exception {
            mockMvc.perform(get("/api/recommendations/genres/most-used-in-item-lists/by-user/" + OTHER_ID + "/ids")
                            .with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void lessonsForGenre_isCatalogAndAllowed() throws Exception {
            expectAllowed(get("/api/recommendations/lessons/by-genre/3").with(asUser(OTHER_ID)));
        }




    }

    @Nested
    class SpotifyRecommendations {

        @Test
        void songs_forSelf_isAllowed() throws Exception {
            expectAllowed(get("/api/spotify/recommendations/songs/" + OWNER_ID).param("limit", "5")
                    .with(asUser(OWNER_ID)));
        }

        @Test
        void songs_forOther_isForbidden() throws Exception {
            mockMvc.perform(get("/api/spotify/recommendations/songs/" + OTHER_ID).param("limit", "5")
                            .with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void items_forOther_isForbidden() throws Exception {
            mockMvc.perform(get("/api/spotify/recommendations/items/" + OTHER_ID).with(asUser(OWNER_ID)))
                    .andExpect(status().isForbidden());
        }
    }
}
