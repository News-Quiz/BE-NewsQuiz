package com.example.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
            "IT/과학", "https://news.naver.com/section/104"
    );

    // 모든 카테고리 뉴스 가져오기
    public List<String> fetchNewsByCategory() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : CATEGORY_URLS.entrySet()) {
            String category = entry.getKey();
            String url = entry.getValue();
            result.add("Category: " + category + "\n");
            result.addAll(fetchNewsFromUrl(url));
        }
        return result;
    }

    // 개별 기사 본문 가져오기
    private String fetchArticleContent(String articleUrl) {
        try {
            Document articleDoc = Jsoup.connect(articleUrl)
                    .userAgent(USER_AGENT)
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .get();

            Element contentElement = articleDoc.selectFirst("#dic_area"); // 본문 ID
            return (contentElement != null) ? contentElement.text() : "No content available";
        } catch (Exception e) {
            System.err.println("Error fetching article content from " + articleUrl + ": " + e.getMessage());
            return "Error fetching content";
        }
    }

    // 특정 URL에서 뉴스 가져오기
    private List<String> fetchNewsFromUrl(String url) {
        List<String> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(5000) // 타임아웃 설정
                    .get();

            Elements articles = doc.select("li.sa_item._SECTION_HEADLINE"); // 기사 블록 선택
            int count = 0;

            for (Element article : articles) {
                if (count >= 10) break; // 최대 10개 제한

                // 제목과 링크 추출
                Element titleElement = article.selectFirst("a.sa_text_title");
                String title = (titleElement != null) ? titleElement.text() : "No title";
                String link = (titleElement != null) ? titleElement.absUrl("href") : "No link";

                // 요약 본문 추출
                Element summaryElement = article.selectFirst("div.sa_text_lede");
                String summary = (summaryElement != null) ? summaryElement.text() : "No summary available";

                // 기사 본문 내용 추출
                String content = fetchArticleContent(link);

                // 결과 리스트에 줄바꿈 추가
                newsList.add("Title: " + title + "<br>");
                newsList.add("Link: " + link + "<br>");
                newsList.add("Summary: " + summary + "<br>");
                newsList.add("Content: " + content + "<br>");
                newsList.add("-------------------------<br>");
                count++;
            }
        } catch (Exception e) {
            newsList.add("Error fetching news from " + url + ": " + e.getMessage() + "\n");
        }
        return newsList;
    }
}