package com.swiftseek.searchengine.crawler.news;

import java.util.List;

public record NewsApiResponse(List<Article> articles) {

    public record Article(
            String title,
            String url,
            String content,
            Source source,
            String publishedAt) {
    }

    public record Source(String name) {
    }
}
