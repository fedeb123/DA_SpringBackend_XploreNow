package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.domain.model.entity.News;
import com.XploreNowAPI.SpringAPI.domain.repository.NewsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsCommandServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsCommandService newsCommandService;

    @Test
    void updateStatus_WhenNewsNotFound_ThrowsNotFound() {
        when(newsRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> newsCommandService.updateStatus(99L, false));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("News not found", ex.getReason());
    }

    @Test
    void updateStatus_WhenSuccessPersistsNewState() {
        News news = News.builder()
                .id(10L)
                .title("Test")
                .shortDescription("Short")
                .fullDescription("Full")
                .active(true)
                .build();

        when(newsRepository.findById(10L)).thenReturn(Optional.of(news));
        when(newsRepository.save(any(News.class))).thenReturn(news);

        newsCommandService.updateStatus(10L, false);

        assertFalse(news.isActive());
        verify(newsRepository).save(news);
    }
}
