package com.uo289390.toco.service;

import static org.mockito.Mockito.mock;

import java.util.Arrays;

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
 * Test helper that wires the static {@link EntityResolver} with repository mocks.
 *
 * The services resolve related entities through the static methods of
 * {@link EntityResolver}, which read the repositories injected in its
 * constructor. This helper builds it with the mocks provided by the test and
 * fills the remaining repositories with fresh, unstubbed mocks.
 */
final class EntityResolverTestSupport {

    private EntityResolverTestSupport() {
    }

    /**
     * Initialises {@link EntityResolver} with the given repository mocks.
     *
     * @param repositories repository mocks used by the test; any repository type
     *                     not provided is replaced with a new mock
     */
    static void initResolver(Object... repositories) {
        new EntityResolver(
                pick(ChordRepository.class, repositories),
                pick(GenreRepository.class, repositories),
                pick(InstrumentRepository.class, repositories),
                pick(InstrumentGenreRepository.class, repositories),
                pick(ItemRepository.class, repositories),
                pick(ItemListRepository.class, repositories),
                pick(LessonRepository.class, repositories),
                pick(LessonSectionRepository.class, repositories),
                pick(SongRepository.class, repositories),
                pick(SongLessonRepository.class, repositories),
                pick(SpotifyUserRepository.class, repositories),
                pick(TabSectionRepository.class, repositories),
                pick(TuneRepository.class, repositories),
                pick(UserRepository.class, repositories));
    }

    private static <T> T pick(Class<T> type, Object[] repositories) {
        return Arrays.stream(repositories)
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst()
                .orElseGet(() -> mock(type));
    }
}
