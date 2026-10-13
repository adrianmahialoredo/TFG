package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.model.*;
import com.uo289390.toco.model.*;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.InstrumentGenreRepository;
import com.uo289390.toco.utils.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uo289390.toco.utils.Identifiable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;

import static com.uo289390.toco.utils.EntityResolver.resolveItemList;
import static com.uo289390.toco.utils.EntityResolver.resolveUser;
import static com.uo289390.toco.utils.Utils.idsToExclude;
import static com.uo289390.toco.utils.Utils.resolvePageable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstrumentGenreRecommendationServiceImpl implements InstrumentGenreRecommendationService {

    private final InstrumentGenreRepository instrumentGenreRepository;

    @Override
    public SequencedSet<InstrumentGenre> findForItemList(Long id, int limit) {
        ItemList itemList = resolveItemList(id);
        SequencedSet<InstrumentGenre> instrumentGenres = new LinkedHashSet<>();

        if (limit <= 0) {
            return instrumentGenres;
        }

        // Nivel 1 - Sacar el InstrumentGenre del genero y el instrumento mas top de la playlist
        Optional<Genre> mainGenre = itemList.getOnlyGenre().or(itemList::getMainGenreFromItems);
        Optional<Instrument> mainInstrument = itemList.getMainInstrumentFromItems();
        mainGenre.ifPresent(genre -> mainInstrument.ifPresent(
                instrument -> addCandidate(instrumentGenres, genre, instrument, itemList)));

        if (instrumentGenres.size() >= limit) {
            return instrumentGenres;
        }

        // Nivel 2 - Sacar los InstrumentGenres de combinar los generos y los instrumentos mas top de la playlist. No se añaden más si no se encuentran
        int topN = RecommendationConstants.NUMBER_OF_MAX_TOP_INSTRUMENTS_AND_GENRES_USED_IN_INSTRUMENT_GENRE_RECOMMENDATION;
        List<Genre> topGenres = itemList.getTopGenresFromItems(topN);
        List<Instrument> topInstruments = itemList.getTopInstrumentsFromItems(topN);

        for (Genre genre : topGenres) {
            for (Instrument instrument : topInstruments) {
                if (instrumentGenres.size() >= limit) {
                    return instrumentGenres;
                }
                addCandidate(instrumentGenres, genre, instrument, itemList);
            }
        }

        return instrumentGenres;
    }

    @Override
    public SequencedSet<InstrumentGenre> findForUser(Long id, int limit) {
        User user = resolveUser(id);
        SequencedSet<InstrumentGenre> instrumentGenres = new LinkedHashSet<>();

        if (limit <= 0) {
            return instrumentGenres;
        }

        // Candidatos: los generos/instrumentos mas usados en las listas del usuario, completados con sus favoritos
        Set<Long> genreIds = candidateIds(
                user.getTopGenresFromItemLists(RecommendationConstants.NUMBER_OF_ITEM_LISTS_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION),
                user.getPreferences().getFavoriteGenres(),
                RecommendationConstants.NUMBER_OF_FAVOURITE_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION
        );
        Set<Long> instrumentIds = candidateIds(
                user.getTopInstrumentsFromItemLists(RecommendationConstants.NUMBER_OF_ITEM_LISTS_INSTRUMENTS_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION),
                user.getPreferences().getFavoriteInstruments(),
                RecommendationConstants.NUMBER_OF_FAVOURITE_INSTRUMENTS_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION
        );

        // Sin generos o sin instrumentos no hay ninguna combinacion posible
        if (genreIds.isEmpty() || instrumentIds.isEmpty()) {
            return instrumentGenres;
        }

        // Se excluyen los que ya estan en las listas del usuario y los ya recomendados
        Set<Long> excluding = user.getIdsFromItemListsOfType(InstrumentGenre.class);

        // Nivel 1: todas las combinaciones de esos generos e instrumentos hasta el nivel del usuario
        List<InstrumentGenre> byLevel = instrumentGenreRepository.findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(
                genreIds,
                instrumentIds,
                user.getDifficultyLevel(),
                idsToExclude(excluding),
                resolvePageable(limit)
        );
        instrumentGenres.addAll(byLevel);
        byLevel.forEach(instrumentGenre -> excluding.add(instrumentGenre.getId()));

        int missing = limit - instrumentGenres.size();
        if (missing <= 0) {
            return instrumentGenres;
        }

        // Nivel 2: las mismas combinaciones sin filtrar por nivel
        instrumentGenres.addAll(instrumentGenreRepository.findByGenreIdInAndInstrumentIdInAndIdNotIn(
                genreIds,
                instrumentIds,
                idsToExclude(excluding),
                resolvePageable(missing)
        ));

        return instrumentGenres;
    }

    /**
     * IDs of the most used entities in the user's lists followed by up to {@code favouritesCount} of the
     * user's favourites, without duplicates and in that order.
     */
    private static <T extends Identifiable<Long>> Set<Long> candidateIds(List<T> fromItemLists, Collection<T> favourites, int favouritesCount) {
        Set<Long> ids = new LinkedHashSet<>();
        fromItemLists.forEach(entity -> ids.add(entity.getId()));
        favourites.stream().limit(favouritesCount).forEach(entity -> ids.add(entity.getId()));
        return ids;
    }

    /**
     * Adds the InstrumentGenre of the given genre and instrument if it exists and is not already in the playlist.
     */
    private void addCandidate(SequencedSet<InstrumentGenre> instrumentGenres, Genre genre, Instrument instrument, ItemList itemList) {
        instrumentGenreRepository.findByGenreIdAndInstrumentId(genre.getId(), instrument.getId())
                .filter(instrumentGenre -> !itemList.containsItem(instrumentGenre))
                .ifPresent(instrumentGenres::add);
    }
}
