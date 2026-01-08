package com.swiftseek.searchengine.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.swiftseek.searchengine.crawler.news.NewsArticle;

@Component
public class NewsQueryCache {

    private static final long TTL = 10 * 60 * 1000; // 10 minutes

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public List<NewsArticle> get(String query) {
        CacheEntry entry = cache.get(query);
        if (entry == null)
            return null;

        if (System.currentTimeMillis() - entry.timestamp > TTL) {
            cache.remove(query);
            return null;
        }
        return entry.articles;
    }

    public void put(String query, List<NewsArticle> articles) {
        cache.put(query, new CacheEntry(articles));
    }

    private static class CacheEntry {
        private final List<NewsArticle> articles;
        private final long timestamp;

        CacheEntry(List<NewsArticle> articles) {
            this.articles = articles;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
