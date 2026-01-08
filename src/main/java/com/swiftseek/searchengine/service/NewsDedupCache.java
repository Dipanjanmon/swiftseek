package com.swiftseek.searchengine.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class NewsDedupCache {

    private final Set<String> indexedUrls = ConcurrentHashMap.newKeySet();

    public boolean isAlreadyIndexed(String url) {
        return indexedUrls.contains(url);
    }

    public void markIndexed(String url) {
        indexedUrls.add(url);
    }
}
