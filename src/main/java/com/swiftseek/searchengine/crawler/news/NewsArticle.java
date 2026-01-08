package com.swiftseek.searchengine.crawler.news;

public record NewsArticle(
        String title,
        String url,
        String content,
        String source,
        long publishedTime) {
}
