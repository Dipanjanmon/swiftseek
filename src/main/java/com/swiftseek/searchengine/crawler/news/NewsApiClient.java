package com.swiftseek.searchengine.crawler.news;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NewsApiClient {

    private static final String API_KEY = "ac33f47adb724785bb1dcc4d03a5a686";
    private static final String API_URL = "https://newsapi.org/v2/everything?q=%s&language=en&pageSize=10&apiKey=%s";

    private final RestTemplate restTemplate = new RestTemplate();

    public List<NewsArticle> fetchNews(String query) {

        String url = String.format(API_URL, query, API_KEY);

        NewsApiResponse response = restTemplate.getForObject(url, NewsApiResponse.class);

        List<NewsArticle> articles = new ArrayList<>();

        if (response == null || response.articles() == null)
            return articles;

        for (NewsApiResponse.Article a : response.articles()) {

            long time = Instant.parse(a.publishedAt()).toEpochMilli();

            articles.add(new NewsArticle(
                    a.title(),
                    a.url(),
                    a.content() != null ? a.content() : "",
                    a.source().name(),
                    time));
        }

        return articles;
    }
}
