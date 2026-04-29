package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.news.NewsDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.news.NewsSummaryDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.News;
import com.XploreNowAPI.SpringAPI.domain.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsQueryService {

    private final NewsRepository newsRepository;

    @Transactional(readOnly = true)
    public List<NewsSummaryDto> getCatalog() {
        return newsRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public NewsDetailDto getNewsDetail(Long newsId) {
        News news = newsRepository.findByIdAndActiveTrue(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found"));

        return toDetail(news);
    }

    private NewsSummaryDto toSummary(News news) {
        return new NewsSummaryDto(
                news.getId(),
                news.getTitle(),
                news.getShortDescription(),
                news.getImageUrl(),
                news.getCreatedAt()
        );
    }

    private NewsDetailDto toDetail(News news) {
        return new NewsDetailDto(
                news.getId(),
                news.getTitle(),
                news.getShortDescription(),
                news.getFullDescription(),
                news.getImageUrl(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }
}
