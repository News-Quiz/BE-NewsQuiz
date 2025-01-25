package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.json.JSONObject;

@Service
public class GenerateQuizService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    public String generateQuiz(String title, String summary) {
        try {
            // 1. 프롬프트 구성
            String prompt = buildPrompt(title, summary);

            // 2. HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            headers.set("Content-Type", "application/json");

            // 3. 요청 바디 구성
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo"); // 저렴한 모델 사용
            requestBody.put("messages", new JSONObject[]{
                    new JSONObject().put("role", "system").put("content", "You are a helpful assistant that generates quiz questions."),
                    new JSONObject().put("role", "user").put("content", prompt)
            });
            requestBody.put("max_tokens", 200); // 응답 길이 제한
            requestBody.put("temperature", 0.7); // 창의성 설정

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

            // 4. API 호출
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, request, String.class);

            // 5. 응답 파싱
            JSONObject responseObject = new JSONObject(response.getBody());
            String generatedQuiz = responseObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

            return generatedQuiz;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to generate quiz: " + e.getMessage();
        }
    }

    private String buildPrompt(String title, String summary) {
        return String.format(
                "Create a quiz question based on the following news article:\n" +
                        "Title: %s\n" +
                        "Summary: %s\n" +
                        "Generate 2 multiple-choice questions, each with 4 options, and provide the correct answer for each.",
                title, summary
        );
    }
}