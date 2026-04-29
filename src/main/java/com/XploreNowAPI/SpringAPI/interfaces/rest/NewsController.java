package com.XploreNowAPI.SpringAPI.interfaces.rest;

import com.XploreNowAPI.SpringAPI.application.dto.news.NewsDetailDto;
import com.XploreNowAPI.SpringAPI.application.dto.news.NewsSummaryDto;
import com.XploreNowAPI.SpringAPI.application.service.NewsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "News", description = "Listado y detalle de noticias")
public class NewsController {

    private final NewsQueryService newsQueryService;

    @GetMapping
    @Operation(summary = "Listado de noticias", description = "Retorna todas las noticias activas ordenadas por fecha de creacion")
    public ResponseEntity<List<NewsSummaryDto>> getCatalog() {
        return ResponseEntity.ok(newsQueryService.getCatalog());
    }

    @GetMapping("/{newsId}")
    @Operation(summary = "Detalle de noticia", description = "Retorna el contenido completo de una noticia activa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle obtenido"),
            @ApiResponse(responseCode = "404", description = "Noticia no encontrada")
    })
    public ResponseEntity<NewsDetailDto> getDetail(
            @Parameter(description = "ID de la noticia")
            @PathVariable Long newsId
    ) {
        return ResponseEntity.ok(newsQueryService.getNewsDetail(newsId));
    }
}
