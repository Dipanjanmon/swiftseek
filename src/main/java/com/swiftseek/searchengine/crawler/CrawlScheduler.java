package com.swiftseek.searchengine.crawler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CrawlScheduler {

    @Scheduled(fixedDelay = 60000) // every 1 minute
    public void scheduledCrawl() {

        System.out.println("⏳ Scheduled crawl started");

        WebCrawler.crawl("https://spring.io");
        WebCrawler.crawl("https://www.oracle.com/java/");
    }
}
