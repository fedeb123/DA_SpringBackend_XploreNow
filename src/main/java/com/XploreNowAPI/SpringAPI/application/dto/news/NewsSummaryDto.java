package com.XploreNowAPI.SpringAPI.application.dto.news;

import java.time.LocalDateTime;

public record NewsSummaryDto(
        Long newsId,
        String title,
        String shortDescription,
        String imageUrl,
        LocalDateTime publishedAt
) {
}
