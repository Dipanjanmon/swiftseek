package com.swiftseek.searchengine.service;
import com.swiftseek.searchengine.crawler.CrawledPage;
import com.swiftseek.searchengine.crawler.WebCrawler;
import com.swiftseek.searchengine.crawler.news.NewsApiClient;
import com.swiftseek.searchengine.crawler.news.NewsArticle;
import com.swiftseek.searchengine.lucene.LuceneIndexer;
import com.swiftseek.searchengine.lucene.LuceneSearcher;
import com.swiftseek.searchengine.service.NewsDedupCache;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    @Autowired
    private NewsDedupCache newsDedupCache;

    @Autowired
    private NewsQueryCache newsQueryCache;


    // 🔹 Inject News API client
    @Autowired
    private NewsApiClient newsApiClient;

    /**
     * Main search method used by SearchController
     */
    public Map<String, Object> search(
        String query,
        String domain,
        Integer days,
        int page,
        int size) throws Exception {

    // 1️⃣ Query-level cache
    List<NewsArticle> news = newsQueryCache.get(query);

    if (news == null) {
        news = newsApiClient.fetchNews(query);
        newsQueryCache.put(query, news);
    }

    // 2️⃣ Index safely (dedup + isolation)
    for (NewsArticle article : news) {
        try {
            if (newsDedupCache.isAlreadyIndexed(article.url())) continue;

            LuceneIndexer.indexPage(
                    article.title(),
                    article.url(),
                    article.content(),
                    article.source(),
                    article.publishedTime()
            );

            newsDedupCache.markIndexed(article.url());

        } catch (Exception e) {
            System.err.println("❌ Failed to index article: " + article.url());
        }
    }

    // 3️⃣ Search Lucene
    return LuceneSearcher.search(query, domain, days, page, size);
}


    /**
     * Initial indexing + crawling when application starts
     */
    @PostConstruct
    public void init() {
        try {
            System.out.println(">>> INDEXING INIT STARTED <<<");

            // Index static/sample data
            LuceneIndexer.indexSampleData();

            // Crawl + index real sites
            indexFromWeb("https://spring.io");
            indexFromWeb("https://www.oracle.com/java/");

            System.out.println(">>> SEARCH ENGINE READY 🚀 <<<");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method: crawl → index
     */
    private void indexFromWeb(String url) throws Exception {
        CrawledPage page = WebCrawler.crawl(url);

        if (page == null) return;

        LuceneIndexer.indexPage(
                page.title(),
                page.url(),
                page.content(),
                page.domain(),
                page.crawlTime()
        );
    }
}
