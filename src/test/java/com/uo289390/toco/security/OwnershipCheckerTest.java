package com.uo289390.toco.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

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
import com.uo289390.toco.utils.EntityResolver;

/**
 * Unit tests for {@link OwnershipChecker}: self checks, batch self checks and item list
 * ownership (including the rule that a missing list is let through so the controller answers 404).
 */
@ExtendWith(MockitoExtension.class)
class OwnershipCheckerTest {

    private static final Long USER_ID = 7L;
    private static final Long OTHER_USER_ID = 8L;

    @Mock
    private ItemListRepository itemListRepository;

    private final OwnershipChecker ownership = new OwnershipChecker();

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // OwnershipChecker reads the lists through the static EntityResolver
        new EntityResolver(mock(ChordRepository.class), mock(GenreRepository.class),
                mock(InstrumentRepository.class), mock(InstrumentGenreRepository.class), mock(ItemRepository.class),
                itemListRepository, mock(LessonRepository.class), mock(LessonSectionRepository.class),
                mock(SongRepository.class), mock(SongLessonRepository.class), mock(SpotifyUserRepository.class),
                mock(TabSectionRepository.class), mock(TuneRepository.class), mock(UserRepository.class));
        authentication = new UsernamePasswordAuthenticationToken(String.valueOf(USER_ID), null, List.of());
    }

    private static ItemList listOwnedBy(Long ownerId) {
        ItemList itemList = new ItemList();
        if (ownerId != null) {
            User owner = new User();
            owner.setId(ownerId);
            itemList.setUser(owner);
        }
        return itemList;
    }

    // ---- isSelf ----

    @Test
    void isSelf_sameUser_returnsTrue() {
        assertTrue(ownership.isSelf(USER_ID, authentication));
    }

    @Test
    void isSelf_otherUser_returnsFalse() {
        assertFalse(ownership.isSelf(OTHER_USER_ID, authentication));
    }

    @Test
    void isSelf_nullUserId_returnsFalse() {
        assertFalse(ownership.isSelf(null, authentication));
    }

    @Test
    void isSelf_nullAuthentication_returnsFalse() {
        assertFalse(ownership.isSelf(USER_ID, null));
    }

    @Test
    void isSelf_principalNameIsNotNumeric_returnsFalse() {
        Authentication named = new UsernamePasswordAuthenticationToken("john", null, List.of());

        assertFalse(ownership.isSelf(USER_ID, named));
    }

    // ---- isOnlySelf ----

    @Test
    void isOnlySelf_nullCollection_returnsTrue() {
        assertTrue(ownership.isOnlySelf(null, authentication));
    }

    @Test
    void isOnlySelf_emptyCollection_returnsTrue() {
        assertTrue(ownership.isOnlySelf(List.of(), authentication));
    }

    @Test
    void isOnlySelf_onlySelf_returnsTrue() {
        assertTrue(ownership.isOnlySelf(List.of(USER_ID, USER_ID), authentication));
    }

    @Test
    void isOnlySelf_containsOtherUser_returnsFalse() {
        assertFalse(ownership.isOnlySelf(List.of(USER_ID, OTHER_USER_ID), authentication));
    }

    @Test
    void isOnlySelf_containsNullId_returnsFalse() {
        assertFalse(ownership.isOnlySelf(Arrays.asList(USER_ID, null), authentication));
    }

    @Test
    void isOnlySelf_nullAuthenticationWithIds_returnsFalse() {
        assertFalse(ownership.isOnlySelf(Set.of(USER_ID), null));
    }

    // ---- ownsItemList ----

    @Test
    void ownsItemList_ownList_returnsTrue() {
        when(itemListRepository.findById(1L)).thenReturn(Optional.of(listOwnedBy(USER_ID)));

        assertTrue(ownership.ownsItemList(1L, authentication));
    }

    @Test
    void ownsItemList_listOfOtherUser_returnsFalse() {
        when(itemListRepository.findById(1L)).thenReturn(Optional.of(listOwnedBy(OTHER_USER_ID)));

        assertFalse(ownership.ownsItemList(1L, authentication));
    }

    @Test
    void ownsItemList_listWithoutUser_returnsFalse() {
        when(itemListRepository.findById(1L)).thenReturn(Optional.of(listOwnedBy(null)));

        assertFalse(ownership.ownsItemList(1L, authentication));
    }

    @Test
    void ownsItemList_missingList_returnsTrueSoTheControllerAnswers404() {
        when(itemListRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(ownership.ownsItemList(99L, authentication));
    }

    @Test
    void ownsItemList_nullId_returnsTrueWithoutQuerying() {
        assertTrue(ownership.ownsItemList(null, authentication));

        verify(itemListRepository, never()).findById(any());
    }

    @Test
    void ownsItemList_nullAuthentication_returnsFalseForExistingList() {
        when(itemListRepository.findById(1L)).thenReturn(Optional.of(listOwnedBy(USER_ID)));

        assertFalse(ownership.ownsItemList(1L, null));
    }

    // ---- ownsAllItemLists ----

    @Test
    void ownsAllItemLists_nullCollection_returnsTrue() {
        assertTrue(ownership.ownsAllItemLists(null, authentication));
    }

    @Test
    void ownsAllItemLists_emptyCollection_returnsTrue() {
        assertTrue(ownership.ownsAllItemLists(List.of(), authentication));
    }

    @Test
    void ownsAllItemLists_allOwnOrMissing_returnsTrue() {
        when(itemListRepository.findById(1L)).thenReturn(Optional.of(listOwnedBy(USER_ID)));
        when(itemListRepository.findById(2L)).thenReturn(Optional.of(listOwnedBy(USER_ID)));
        when(itemListRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(ownership.ownsAllItemLists(List.of(1L, 2L, 99L), authentication));
    }

    @Test
    void ownsAllItemLists_oneListOfOtherUser_returnsFalse() {
        when(itemListRepository.findById(1L)).thenReturn(Optional.of(listOwnedBy(USER_ID)));
        when(itemListRepository.findById(2L)).thenReturn(Optional.of(listOwnedBy(OTHER_USER_ID)));

        assertFalse(ownership.ownsAllItemLists(List.of(1L, 2L), authentication));
    }

    @Test
    void ownsAllItemLists_stopsAtFirstForeignList() {
        when(itemListRepository.findById(2L)).thenReturn(Optional.of(listOwnedBy(OTHER_USER_ID)));

        assertFalse(ownership.ownsAllItemLists(List.of(2L, 3L), authentication));

        verify(itemListRepository, never()).findById(3L);
    }
}
