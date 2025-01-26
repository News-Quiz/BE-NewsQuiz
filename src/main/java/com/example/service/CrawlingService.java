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
    public List<News> fetchAndSaveNewsByCategory() {
        List<News> crawledArticles = performCrawling(); // 크롤링된 데이터를 가져옵니다.
        List<News> savedArticles = new ArrayList<>();

        for (News article : crawledArticles) {
            if (!newsRepository.existsByTitleAndSource(article.getTitle(), article.getSource())) {
                News savedArticle = newsRepository.save(article); // 저장 후 반환된 엔티티 추가
                savedArticles.add(savedArticle);
            }
        }

        return savedArticles; // 새로 저장된 기사만 반환
    }

    // 특정 URL에서 뉴스 가져오기
    private List<News> performCrawling() {
        List<News> articles = new ArrayList<>();

        try {
            for (Map.Entry<String, String> entry : CATEGORY_URLS.entrySet()) {
                String category = entry.getKey();
                String url = entry.getValue();

                System.out.println("Crawling category: " + category + ", URL: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(5000) // 타임아웃 설정
                        .get();

                Elements articleElements = doc.select("li.sa_item._SECTION_HEADLINE");
                int count = 0;

                for (Element articleElement : articleElements) {
                    if (count >= 5) break; // 각 카테고리에서 최대 5개의 기사만 가져오기

                    // 제목과 링크 추출
                    Element titleElement = articleElement.selectFirst("a.sa_text_title");
                    String title = (titleElement != null) ? titleElement.text() : "No title";
                    String link = (titleElement != null) ? titleElement.absUrl("href") : "No link";

                    // 요약 본문 추출
                    Element summaryElement = articleElement.selectFirst("div.sa_text_lede");
                    String summary = (summaryElement != null) ? summaryElement.text() : "No summary available";

                    // News 엔티티 생성 및 설정
                    News news = new News();
                    news.setTitle(title);
                    news.setSource(link);
                    news.setContent(summary);
                    news.setCategory(category);

                    articles.add(news);
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error during crawling: " + e.getMessage());
        }

        return articles;
    }

}