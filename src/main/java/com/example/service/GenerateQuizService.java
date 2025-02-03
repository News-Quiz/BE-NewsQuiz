package com.example.service;

import com.example.entity.News;
import com.example.repository.NewsRepository;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.json.JSONObject;

import java.util.*;

@Service
public class GenerateQuizService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    @Autowired
    private NewsRepository newsRepository;

    // DB에서 카테고리별 최신 뉴스 5개씩 가져오기
    private Map<String, List<News>> getLatestNewsByCategory() {
        Map<String, List<News>> categoryNewsMap = new HashMap<>();
        List<String> categories = Arrays.asList("정치", "경제", "사회", "생활/문화", "IT/과학", "세계");

        for (String category : categories) {
            List<News> newsList = newsRepository.findTop5ByCategoryOrderByIdDesc(category);
            categoryNewsMap.put(category, newsList);
        }

        return categoryNewsMap;
    }

    // DB에서 뉴스 가져와서 OpenAI API 호출하여 퀴즈 생성
    public JSONObject generateQuizzesFromLatestNews() {
        // 1. DB에서 뉴스 가져오기
        Map<String, List<News>> newsByCategory = getLatestNewsByCategory();
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("다음 뉴스 기사들을 바탕으로 객관식 퀴즈를 생성하세요.\n\n");

        // 2. 뉴스 데이터를 프롬프트로 변환
        for (String category : newsByCategory.keySet()) {
            List<News> newsList = newsByCategory.get(category);
            Set<String> usedTitles = new HashSet<>();
            int quizCount = 0;

            promptBuilder.append("카테고리: ").append(category).append("\n");

            for (News news : newsList) {
                if (quizCount >= 2) break; // 각 카테고리별 2문제만 사용

                if (!usedTitles.contains(news.getTitle())) {
                    usedTitles.add(news.getTitle());
                    promptBuilder.append("제목: ").append(news.getTitle()).append("\n");
                    promptBuilder.append("요약: ").append(news.getContent()).append("\n\n");
                    quizCount++;
                }
            }
        }

        // 3. JSON 형식으로 퀴즈를 생성하도록 OpenAI API 요청
        promptBuilder.append("각 카테고리에서 2문제씩 총 12문제를 JSON 형식으로 생성하세요.\n");
        promptBuilder.append("반드시 JSON 형식만 출력하세요.\n\n");
        promptBuilder.append("{ \"quiz\": [ { \"category\": \"카테고리명\", \"question\": \"질문 내용\", ");
        promptBuilder.append("\"correct_answer\": \"정답\", \"incorrect_answers\": [ \"오답1\", \"오답2\", \"오답3\" ] } ] }");

        return callOpenAiApi(promptBuilder.toString());
    }

    // OpenAI API 호출 로직
    private JSONObject callOpenAiApi(String prompt) {
        try {
            // 1. HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            headers.set("Content-Type", "application/json");

            // 2. 요청 바디 구성
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", new JSONObject[]{
                    new JSONObject().put("role", "system").put("content", "You are a helpful assistant that generates quiz questions."),
                    new JSONObject().put("role", "user").put("content", prompt)
            });
            requestBody.put("max_tokens", 1500); // 응답 길이 증가
            requestBody.put("temperature", 0.7);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();

            // 3. OpenAI API 호출
            ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, request, String.class);

            // 4. API 응답을 JSON 객체로 변환
            JSONObject responseObject = new JSONObject(response.getBody());
            String responseText = responseObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

            return new JSONObject(responseText);

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("error", "Failed to generate quiz: " + e.getMessage());
        }
    }
}