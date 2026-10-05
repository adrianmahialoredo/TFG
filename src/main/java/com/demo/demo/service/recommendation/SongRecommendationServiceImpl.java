package com.demo.demo.service.recommendation;

import com.demo.demo.model.*;
import com.demo.demo.repository.SongRepository;
import com.demo.demo.utils.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.demo.demo.utils.Utils.resolvePageable;
import static com.demo.demo.utils.EntityResolver.resolveItemList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongRecommendationServiceImpl implements SongRecommendationService {

    private final SongRepository songRepository;

    @Override
    public SequencedSet<Song> findForItemList(Long id, int limit) {
        ItemList itemList = resolveItemList(id);
        Set<Song> excluding = itemList.getSongs();
        SequencedSet<Song> songs = new LinkedHashSet<>();

        if (limit <= 0) {
            return songs;
        }

        //Nivel 1 - canciones del que pertenezcan al genero y tengan top 3 instrumentos de la playlist
        Optional<Genre> mainGenre = itemList.getOnlyGenre().or(itemList::getMainGenreFromSongs);
        List<Instrument> topInstruments = itemList.getTopInstrumentsFromSongs(RecommendationConstants.NUMBER_OF_MATCHING_INSTRUMENTS_IN_SONG_RECOMMENDATION);

        if (!topInstruments.isEmpty()) {
            mainGenre.ifPresent(genre -> songs.addAll(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(
                    genre.getId(),
                    topInstruments,
                    idsOf(excluding),
                    resolvePageable(limit)
            )));
            excluding.addAll(songs);
        }

        int missing = limit - songs.size();
        if (missing <= 0) {
            return songs;
        }

        //Nivel 2 - mitad canciones del que pertenezcan al genero y mitad que tengan top 3 instrumentos de la playlist
        int byGenreCount = missing % 2 == 0 ? missing / 2 : (missing / 2) + 1;
        int byInstrumentsCount = missing / 2;

        mainGenre.ifPresent(genre -> {
            List<Song> byGenre = songRepository.findDistinctByGenresIdAndIdNotIn(
                    genre.getId(),
                    idsOf(excluding),
                    resolvePageable(byGenreCount)
            );
            songs.addAll(byGenre);
            excluding.addAll(byGenre);
        });

        if (byInstrumentsCount > 0 && !topInstruments.isEmpty()) {
            List<Song> byInstrument = songRepository.findDistinctByInstrumentsInAndIdNotIn(
                    topInstruments,
                    idsOf(excluding),
                    resolvePageable(byInstrumentsCount)
            );
            songs.addAll(byInstrument);
            excluding.addAll(byInstrument);
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

    /**
     * Nivel 3a - canciones del genero e instrumento del unico InstrumentGenre de la playlist.
     */
    private void addByInstrumentGenre(Set<Song> songs, InstrumentGenre instrumentGenre, Set<Song> excluding, int missing) {
        songs.addAll(songRepository.findDistinctByGenresIdAndInstrumentsInAndIdNotIn(
                instrumentGenre.getGenre().getId(),
                Set.of(instrumentGenre.getInstrument()),
                idsOf(excluding),
                resolvePageable(missing)
        ));
    }

    /**
     * Nivel 3b - canciones de una seleccion aleatoria de los generos e instrumentos favoritos del usuario.
     */
    private void addByUserPreferences(Set<Song> songs, User user, Set<Song> excluding, int missing) {
        Set<Genre> favouriteGenres = user.getPreferences().getFavoriteGenres();
        Set<Instrument> favouriteInstruments = user.getPreferences().getFavoriteInstruments();

        ArrayList<Genre> favouriteGenresCopy = new ArrayList<>(favouriteGenres);
        ArrayList<Instrument> favouriteInstrumentsCopy = new ArrayList<>(favouriteInstruments);

        Collections.shuffle(favouriteGenresCopy);
        Collections.shuffle(favouriteInstrumentsCopy);

        if (!favouriteGenresCopy.isEmpty() && !favouriteInstrumentsCopy.isEmpty()) {
            songs.addAll(songRepository.findDistinctByGenresInAndInstrumentsInAndIdNotIn(
                    favouriteGenresCopy.stream().limit(Preferences.MIN_NUMBER_OF_FAVOURITE_GENRES).collect(Collectors.toSet()),
                    favouriteInstrumentsCopy.stream().limit(Preferences.MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS).collect(Collectors.toSet()),
                    idsOf(excluding),
                    resolvePageable(missing)
            ));
        }
    }

    private static Set<Long> idsOf(Set<Song> songs) {
        return songs.stream().map(Song::getId).collect(Collectors.toSet());
    }
}
