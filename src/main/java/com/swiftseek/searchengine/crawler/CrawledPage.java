package com.swiftseek.searchengine.crawler;

public record CrawledPage(
        String title,
        String url,
        String content,
        String domain,
        long crawlTime) {
}
