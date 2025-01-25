package com.example.controller;

import com.example.service.GenerateQuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GenerateQuizController {

    private final GenerateQuizService generateQuizService;

    @Autowired
    public GenerateQuizController(GenerateQuizService generateQuizService) {
        this.generateQuizService = generateQuizService;
    }

    @PostMapping("/generatequiz")
    public ResponseEntity<String> generateQuiz(@RequestParam String title, @RequestParam String summary) {
        // 퀴즈 생성 요청을 서비스에 전달
        String generatedQuiz = generateQuizService.generateQuiz(title, summary);

        // 생성된 퀴즈를 응답
        return ResponseEntity.ok(generatedQuiz);
    }
}