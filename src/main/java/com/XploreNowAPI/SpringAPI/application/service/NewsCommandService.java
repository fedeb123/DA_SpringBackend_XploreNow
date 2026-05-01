package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.domain.model.entity.News;
import com.XploreNowAPI.SpringAPI.domain.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NewsCommandService {

    private final NewsRepository newsRepository;

    @Transactional
    public void updateStatus(Long newsId, boolean active) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found"));

        news.setActive(active);
        newsRepository.save(news);
    }
}
