package com.swiftseek.searchengine.controller;

import com.swiftseek.searchengine.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SearchController {

    private final SearchService searchService;

    // Constructor injection (recommended)
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Search API
     *
     * Example:
     * /search?q=java
     * /search?q=java&domain=oracle.com
     * /search?q=java&days=7
     * /search?q=java&domain=spring.io&days=3&page=0&size=5
     */
    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam("q") String query,
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size
    ) throws Exception {

        return searchService.search(query, domain, days, page, size);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public String health() {
        return "Search Engine Backend is RUNNING 🚀";
    }
}
