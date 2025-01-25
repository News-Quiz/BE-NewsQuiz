package com.example.controller;

import com.example.service.CrawlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/crawl", produces = MediaType.TEXT_HTML_VALUE)
    public String getNewsByCategory() {
        List<String> news = crawlingService.fetchNewsByCategory();
        return String.join("", news); // HTML 문자열로 합치기
    }
}