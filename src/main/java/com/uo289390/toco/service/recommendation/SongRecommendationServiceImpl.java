package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.model.*;
import com.uo289390.toco.model.*;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.SongRepository;
import com.uo289390.toco.utils.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.uo289390.toco.utils.EntityResolver.resolveUser;
import static com.uo289390.toco.utils.Utils.resolvePageable;
import static com.uo289390.toco.utils.EntityResolver.resolveItemList;
import static com.uo289390.toco.utils.Utils.idsToExclude;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongRecommendationServiceImpl implements SongRecommendationService {

    private final SongRepository songRepository;

    @Override
    public SequencedSet<Song> findForItemList(Long id, int limit) {
        ItemList itemList = resolveItemList(id);
        Set<Long> excluding = idsOf(itemList.getSongs());
        SequencedSet<Song> songs = new LinkedHashSet<>();

        if (limit <= 0) {
            return songs;
        }

        //Nivel 1 - canciones del que pertenezcan al genero y tengan top 3 instrumentos de la playlist
        Optional<Genre> mainGenre = itemList.getOnlyGenre().or(itemList::getMainGenreFromSongs);
        List<Instrument> topInstruments = itemList.getTopInstrumentsFromSongs(RecommendationConstants.N_MATCHING_INSTRUMENTS_USED_IN_SONG_RECOMMENDATION);

        if (!topInstruments.isEmpty()) {
            mainGenre.ifPresent(genre -> songs.addAll(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(
                    genre.getId(),
                    topInstruments,
                    idsToExclude(excluding),
                    resolvePageable(limit)
            )));
            excluding.addAll(idsOf(songs));
        }

        int missing = limit - songs.size();
        if (missing <= 0) {
            return songs;
        }

        //Nivel 2 - mitad canciones del que pertenezcan al genero y mitad que tengan top 3 instrumentos de la playlist
        int byGenreCount = missing % 2 == 0 ? missing / 2 : (missing / 2) + 1;
        int byInstrumentsCount = missing / 2;

        mainGenre.ifPresent(genre -> {
            List<Song> byGenre = songRepository.findByGenresIdAndIdNotIn(
                    genre.getId(),
                    idsToExclude(excluding),
                    resolvePageable(byGenreCount)
            );
            songs.addAll(byGenre);
            excluding.addAll(idsOf(byGenre));
        });

        if (byInstrumentsCount > 0 && !topInstruments.isEmpty()) {
            List<Song> byInstrument = songRepository.findDistinctByInstrumentsInAndIdNotIn(
                    topInstruments,
                    idsToExclude(excluding),
                    resolvePageable(byInstrumentsCount)
            );
            songs.addAll(byInstrument);
            excluding.addAll(idsOf(byInstrument));
        }

        missing = limit - songs.size();
        if (missing <= 0) {
            return songs;
        }

        //Nivel 3a - por instrument_genre (si hay); Nivel 3b - por preferencias del usuario
        int remaining = missing;
        itemList.getOnlyInstrumentGenre()
                .filter(instrumentGenre -> instrumentGenre.getGenre() != null && instrumentGenre.getInstrument() != null)
                .ifPresentOrElse(
                        instrumentGenre -> addByInstrumentGenre(songs, instrumentGenre, excluding, remaining),
                        () -> addByUserPreferences(songs, itemList.getUser(), excluding, remaining)
                );

        return songs;
    }

    @Override
    public SequencedSet<Song> findForUser(Long id, int limit) {
        User user = resolveUser(id);
        Set<Long> excluding = user.getIdsExcludedFromRecommendationsOfType(Song.class);
        SequencedSet<Song> songs = new LinkedHashSet<>();

        if (limit <= 0) {
            return songs;
        }

        int halfLimit = limit / 2;

        // Nivel 1 - mitad del limite repartida entre los generos mas usados en las playlists, completados con los favoritos (minimo 1 favorito)
        List<Genre> genres = new ArrayList<>(user.getTopGenresFromItemLists(RecommendationConstants.N_GENRES_USED_IN_BY_USER_SONG_RECOMMENDATION - 1));
        for (Genre genre : user.getPreferences().getFavoriteGenres()) {
            if (!genres.contains(genre)) {
                genres.add(genre);
                if (genres.size() == RecommendationConstants.N_GENRES_USED_IN_BY_USER_SONG_RECOMMENDATION) {
                    break;
                }
            }
        }

        // El resto de la division se reparte, una cancion mas, entre los primeros generos.
        // Sin generos (ni listas ni favoritos) esta mitad se queda vacia y la completan los niveles siguientes.
        if (!genres.isEmpty()) {
            int byGenreCount = halfLimit / genres.size();
            int byGenreMissing = halfLimit % genres.size();
            for (int i = 0; i < genres.size(); i++) {
                int count = i < byGenreMissing ? byGenreCount + 1 : byGenreCount;
                if (count == 0) {
                    break;
                }
                List<Song> byGenre = songRepository.findByGenresIdAndLevelLessThanEqualAndIdNotIn(
                        genres.get(i).getId(),
                        user.getDifficultyLevel(),
                        idsToExclude(excluding),
                        resolvePageable(count)
                );
                songs.addAll(byGenre);
                excluding.addAll(idsOf(byGenre));
            }
        }

        // Nivel 2 - la otra mitad repartida entre los instrumentos mas usados en las playlists, completados con los favoritos (minimo 1 favorito)
        List<Instrument> instruments = new ArrayList<>(user.getTopInstrumentsFromItemLists(RecommendationConstants.N_INSTRUMENTS_USED_IN_BY_USER_SONG_RECOMMENDATION - 1));
        for (Instrument instrument : user.getPreferences().getFavoriteInstruments()) {
            if (!instruments.contains(instrument)) {
                instruments.add(instrument);
                if (instruments.size() == RecommendationConstants.N_INSTRUMENTS_USED_IN_BY_USER_SONG_RECOMMENDATION) {
                    break;
                }
            }
        }

        // El resto de la division se reparte, una cancion mas, entre los primeros instrumentos.
        // Sin instrumentos (ni listas ni favoritos) esta mitad se queda vacia y la completan los niveles siguientes.
        if (!instruments.isEmpty()) {
            int byInstrumentCount = halfLimit / instruments.size();
            int byInstrumentMissing = halfLimit % instruments.size();
            for (int i = 0; i < instruments.size(); i++) {
                int count = i < byInstrumentMissing ? byInstrumentCount + 1 : byInstrumentCount;
                if (count == 0) {
                    break;
                }
                List<Song> byInstrument = songRepository.findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(
                        instruments.get(i).getId(),
                        user.getDifficultyLevel(),
                        idsToExclude(excluding),
                        resolvePageable(count)
                );
                songs.addAll(byInstrument);
                excluding.addAll(idsOf(byInstrument));
            }
        }

        // Si el limite es impar sobra 1 de las dos mitades: se saca del genero principal (si lo hay)
        if (limit % 2 != 0 && !genres.isEmpty()) {
            List<Song> oddSong = songRepository.findByGenresIdAndIdNotIn(
                    genres.getFirst().getId(),
                    idsToExclude(excluding),
                    resolvePageable(1)
            );
            songs.addAll(oddSong);
            excluding.addAll(idsOf(oddSong));
        }

        int missing = limit - songs.size();
        if (missing <= 0) {
            return songs;
        }

        // Nivel 3 - por la lista de generos anterior, por orden de prioridad y sin chequear nivel:
        // se sacan todas las que falten de cada genero antes de pasar al siguiente
        for (Genre genre : genres) {
            List<Song> byGenre = songRepository.findByGenresIdAndIdNotIn(
                    genre.getId(),
                    idsToExclude(excluding),
                    resolvePageable(missing)
            );
            songs.addAll(byGenre);
            excluding.addAll(idsOf(byGenre));

            missing = limit - songs.size();
            if (missing <= 0) {
                return songs;
            }
        }

        // Nivel 4 - igual que el nivel 3 pero por la lista de instrumentos anterior
        for (Instrument instrument : instruments) {
            List<Song> byInstrument = songRepository.findByInstrumentsIdAndIdNotIn(
                    instrument.getId(),
                    idsToExclude(excluding),
                    resolvePageable(missing)
            );
            songs.addAll(byInstrument);
            excluding.addAll(idsOf(byInstrument));

            missing = limit - songs.size();
            if (missing <= 0) {
                return songs;
            }
        }

        return songs;
    }

    /**
     * Nivel 3a - canciones del genero e instrumento del unico InstrumentGenre de la playlist.
     */
    private void addByInstrumentGenre(Set<Song> songs, InstrumentGenre instrumentGenre, Set<Long> excluding, int missing) {
        songs.addAll(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(
                instrumentGenre.getGenre().getId(),
                Set.of(instrumentGenre.getInstrument()),
                idsToExclude(excluding),
                resolvePageable(missing)
        ));
    }

    /**
     * Nivel 3b - canciones de una seleccion aleatoria de los generos e instrumentos favoritos del usuario.
     */
    private void addByUserPreferences(Set<Song> songs, User user, Set<Long> excluding, int missing) {
        Set<Genre> favouriteGenres = user.getPreferences().getFavoriteGenres();
        Set<Instrument> favouriteInstruments = user.getPreferences().getFavoriteInstruments();

        ArrayList<Genre> favouriteGenresCopy = new ArrayList<>(favouriteGenres);
        ArrayList<Instrument> favouriteInstrumentsCopy = new ArrayList<>(favouriteInstruments);

        Collections.shuffle(favouriteGenresCopy);
        Collections.shuffle(favouriteInstrumentsCopy);

        if (!favouriteGenresCopy.isEmpty() && !favouriteInstrumentsCopy.isEmpty()) {
            songs.addAll(songRepository.findDistinctByGenresInAndInstrumentsInAndIdNotIn(
                    favouriteGenresCopy.stream().limit(Preferences.MIN_N_FAVOURITE_GENRES).collect(Collectors.toSet()),
                    favouriteInstrumentsCopy.stream().limit(Preferences.MIN_N_FAVOURITE_INSTRUMENTS).collect(Collectors.toSet()),
                    idsToExclude(excluding),
                    resolvePageable(missing)
            ));
        }
    }

    /**
     * The exclusions are accumulated as IDs (what the queries receive) instead of entities, so each
     * query does not have to map the whole set again.
     */
    private static Set<Long> idsOf(Collection<Song> songs) {
        return songs.stream().map(Song::getId).collect(Collectors.toCollection(HashSet::new));
    }
}
