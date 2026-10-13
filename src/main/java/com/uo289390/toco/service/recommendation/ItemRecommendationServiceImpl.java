package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRecommendationServiceImpl implements ItemRecommendationService {

    private final SongRecommendationService songRecommendationService;
    private final GenreRecommendationService genreRecommendationService;
    private final InstrumentGenreRecommendationService instrumentGenreRecommendationService;
    private final LessonRecommendationService lessonRecommendationService;
    private final DtoMapper dtoMapper;

    @Override
    public List<ItemDTO> findForItemList(Long id) {
        SequencedSet<Item> items = new LinkedHashSet<>();

        items.addAll(songRecommendationService.findForItemList(id, RecommendationConstants.RECOMMENDED_SONGS_COUNT));
        items.addAll(genreRecommendationService.findForItemList(id, RecommendationConstants.RECOMMENDED_GENRES_COUNT));
        items.addAll(instrumentGenreRecommendationService.findForItemList(id, RecommendationConstants.RECOMMENDED_INSTRUMENT_GENRES_COUNT));
        items.addAll(lessonRecommendationService.findForItemList(id, RecommendationConstants.RECOMMENDED_LESSONS_COUNT));

        return dtoMapper.itemsToItemDTOs(items);
    }

    @Override
    public List<ItemDTO> findForUser(Long id) {
        SequencedSet<Item> items = new LinkedHashSet<>();

        items.addAll(songRecommendationService.findForUser(id, RecommendationConstants.HOME_RECOMMENDED_SONGS_COUNT));
        items.addAll(genreRecommendationService.findForUser(id, RecommendationConstants.HOME_RECOMMENDED_GENRES_COUNT));
        items.addAll(instrumentGenreRecommendationService.findForUser(id, RecommendationConstants.HOME_RECOMMENDED_INSTRUMENT_GENRES_COUNT));
        items.addAll(lessonRecommendationService.findForUser(id, RecommendationConstants.HOME_RECOMMENDED_LESSONS_COUNT));

        return dtoMapper.itemsToItemDTOs(items);
    }
}
