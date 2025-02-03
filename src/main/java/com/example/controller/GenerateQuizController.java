package com.example.controller;

import com.example.service.GenerateQuizService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GenerateQuizController {

    private final GenerateQuizService generateQuizService;

    @Autowired
    public GenerateQuizController(GenerateQuizService generateQuizService) {
        this.generateQuizService = generateQuizService;
    }

    @PostMapping("/generatequiz")
    public ResponseEntity<Object> generateQuiz() {
        JSONObject quiz = generateQuizService.generateQuizzesFromLatestNews();
        return ResponseEntity.ok(quiz.toMap());
    }
}