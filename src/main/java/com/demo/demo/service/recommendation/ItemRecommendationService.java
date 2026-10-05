package com.demo.demo.service.recommendation;

import com.demo.demo.dto.ItemDTO;

import java.util.List;

public interface ItemRecommendationService {
    List<ItemDTO> findForItemList(Long id);
}
