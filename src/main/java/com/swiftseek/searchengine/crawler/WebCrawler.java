package com.swiftseek.searchengine.crawler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebCrawler {

    private static final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

    public static CrawledPage crawl(String url) {

        // Prevent duplicate crawling
        if (!visitedUrls.add(url)) {
            return null;
        }

        // Robots.txt check
        if (!RobotsTxtUtil.isAllowed(url)) {
            System.out.println("🚫 Blocked by robots.txt: " + url);
            return null;
        }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("SwiftSeekBot/1.0 (+https://swiftseek.dev)")
                    .timeout(10_000)
                    .ignoreHttpErrors(true)
                    .get();

            String title = doc.title();
            String content = doc.body().text();
            String domain = doc.location()
                    .replaceFirst("https?://", "")
                    .split("/")[0];

            long crawlTime = System.currentTimeMillis();

            System.out.println("✅ Crawled: " + url);

            return new CrawledPage(
                    title,
                    url,
                    content,
                    domain,
                    crawlTime);

        } catch (Exception e) {
            System.err.println("❌ Failed to crawl: " + url);
            return null;
        }
    }
}
