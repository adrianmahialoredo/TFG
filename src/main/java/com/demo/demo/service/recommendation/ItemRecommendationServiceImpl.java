package com.demo.demo.service.recommendation;

import com.demo.demo.dto.ItemDTO;
import com.demo.demo.model.Item;
import com.demo.demo.utils.mapper.DtoMapper;
import com.demo.demo.utils.RecommendationConstants;
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
}
