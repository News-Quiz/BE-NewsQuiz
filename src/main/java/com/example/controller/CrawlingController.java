package com.example.controller;

import com.example.entity.News;
import com.example.service.CrawlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CrawlingController {

    private final CrawlingService crawlingService;

    @Autowired
    public CrawlingController(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }

    // 크롤링된 기사를 데이터베이스에 저장하고, 결과를 반환하는 엔드포인트
    @GetMapping("/crawl")
    public ResponseEntity<List<News>> crawlAndSaveArticles() {
        try {
            // 크롤링 및 저장 로직 호출
            List<News> crawledArticles = crawlingService.fetchAndSaveNewsByCategory();

            // 성공적으로 저장된 기사를 응답으로 반환
            return ResponseEntity.ok(crawledArticles);
        } catch (Exception e) {
            // 에러 발생 시 에러 메시지를 반환
            return ResponseEntity.status(500).body(null);
        }
    }
}