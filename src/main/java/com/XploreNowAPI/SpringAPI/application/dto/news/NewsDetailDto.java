package com.XploreNowAPI.SpringAPI.application.dto.news;

import java.time.LocalDateTime;

public record NewsDetailDto(
        Long newsId,
        String title,
        String shortDescription,
        String fullDescription,
        String imageUrl,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
