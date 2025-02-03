package com.example.repository;

import com.example.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepository extends JpaRepository<News,Long> {
    boolean existsByTitleAndSource(String title, String source);

    // 특정 카테고리에서 최신 5개 기사 가져오기
    List<News> findTop5ByCategoryOrderByIdDesc(String category);

    // 모든 카테고리 리스트 가져오기 (중복 없이)
    @Query("SELECT DISTINCT n.category FROM News n")
    List<String> findDistinctCategories();
}
