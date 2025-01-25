package com.example.controller;

import com.example.service.CrawlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrawlingController {

    private final CrawlingService crawlingService;

    @Autowired
    public CrawlingController(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }

    // 크롤링된 기사를 데이터베이스에 저장하는 엔드포인트
    @GetMapping("/crawl")
    public ResponseEntity<String> crawlAndSaveArticles() {
        try {
            crawlingService.fetchAndSaveNewsByCategory(); // 크롤링 및 저장 로직 호출
            return ResponseEntity.ok("Articles have been successfully crawled and saved to the database.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while crawling and saving articles: " + e.getMessage());
        }
    }
}