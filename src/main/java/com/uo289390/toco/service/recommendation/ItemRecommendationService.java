package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.dto.ItemDTO;

import java.util.List;

public interface ItemRecommendationService {
    List<ItemDTO> findForItemList(Long id);

    List<ItemDTO> findForUser(Long id);
}
