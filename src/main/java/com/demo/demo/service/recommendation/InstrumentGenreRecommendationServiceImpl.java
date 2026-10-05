package com.demo.demo.service.recommendation;

import com.demo.demo.model.Genre;
import com.demo.demo.model.Instrument;
import com.demo.demo.model.InstrumentGenre;
import com.demo.demo.model.ItemList;
import com.demo.demo.repository.InstrumentGenreRepository;
import com.demo.demo.utils.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;

import static com.demo.demo.utils.EntityResolver.resolveItemList;

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

    /**
     * Adds the InstrumentGenre of the given genre and instrument if it exists and is not already in the playlist.
     */
    private void addCandidate(SequencedSet<InstrumentGenre> instrumentGenres, Genre genre, Instrument instrument, ItemList itemList) {
        instrumentGenreRepository.findByGenreIdAndInstrumentId(genre.getId(), instrument.getId())
                .filter(instrumentGenre -> !itemList.containsItem(instrumentGenre))
                .ifPresent(instrumentGenres::add);
    }
}
