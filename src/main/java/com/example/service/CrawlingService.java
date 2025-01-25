package com.example.service;

import com.example.entity.News;
import com.example.repository.NewsRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CrawlingService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    // 카테고리 URL 매핑
    private static final Map<String, String> CATEGORY_URLS = Map.of(
            "정치", "https://news.naver.com/section/100",
            "경제", "https://news.naver.com/section/101",
            "사회", "https://news.naver.com/section/102",
            "생활/문화", "https://news.naver.com/section/103",
            "IT/과학", "https://news.naver.com/section/105",
            "세계", "https://news.naver.com/section/104"
    );

    @Autowired
    private NewsRepository newsRepository;

    // 모든 카테고리 뉴스 가져오기
    public void fetchAndSaveNewsByCategory() {
        for (Map.Entry<String, String> entry : CATEGORY_URLS.entrySet()) {
            String category = entry.getKey();
            String url = entry.getValue();
            List<News> newsList = fetchNewsFromUrl(url, category);
            saveNewsToDatabase(newsList);
        }
    }

    // 특정 URL에서 뉴스 가져오기
    private List<News> fetchNewsFromUrl(String url, String category) {
        List<News> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(5000) // 타임아웃 설정
                    .get();

            Elements articles = doc.select("li.sa_item._SECTION_HEADLINE"); // 기사 블록 선택
            int count = 0;

            for (Element article : articles) {
                if (count >= 5) break; // 최대 5개 제한

                // 제목과 링크 추출
                Element titleElement = article.selectFirst("a.sa_text_title");
                String title = (titleElement != null) ? titleElement.text() : "No title";
                String link = (titleElement != null) ? titleElement.absUrl("href") : "No link";

                // 요약 본문 추출
                Element summaryElement = article.selectFirst("div.sa_text_lede");
                String summary = (summaryElement != null) ? summaryElement.text() : "No summary available";

                // News 엔티티 생성
                News news = new News();
                news.setTitle(title);
                news.setSource(link);
                news.setContent(summary);
                news.setCategory(category);

                newsList.add(news);
                count++;
            }
        } catch (Exception e) {
            System.err.println("Error fetching news from " + url + ": " + e.getMessage());
        }
        return newsList;
    }

    // 데이터베이스에 저장
    private void saveNewsToDatabase(List<News> newsList) {
        newsRepository.saveAll(newsList);
    }


}