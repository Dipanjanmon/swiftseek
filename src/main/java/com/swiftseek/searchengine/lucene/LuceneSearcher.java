package com.swiftseek.searchengine.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;

import java.util.*;

public class LuceneSearcher {

    public static Map<String, Object> search(
            String queryStr,
            String domain,
            Integer days,
            int page,
            int size) throws Exception {

        Directory directory = LuceneIndexer.getDirectory();
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        /* ---------- TEXT QUERY ---------- */
        String[] fields = { "title", "content" };
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());

        Query textQuery = parser.parse(queryStr);

        BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
        finalQuery.add(textQuery, BooleanClause.Occur.MUST);

        /* ---------- DOMAIN FILTER ---------- */
        if (domain != null && !domain.isEmpty()) {
            Query domainQuery = new TermQuery(new Term("domain", domain));
            finalQuery.add(domainQuery, BooleanClause.Occur.FILTER);
        }

        /* ---------- DATE FILTER ---------- */
        if (days != null && days > 0) {
            long now = System.currentTimeMillis();
            long past = now - (days * 24L * 60 * 60 * 1000);

            Query dateQuery = LongPoint.newRangeQuery(
                    "timestamp",
                    past,
                    now);
            finalQuery.add(dateQuery, BooleanClause.Occur.FILTER);
        }

        /* ---------- PAGINATION ---------- */
        int start = page * size;
        int end = start + size;

        TopDocs topDocs = searcher.search(finalQuery.build(), start + size);
        ScoreDoc[] hits = topDocs.scoreDocs;

        List<Map<String, String>> results = new ArrayList<>();

for (int i = start; i < Math.min(end, hits.length); i++) {

    ScoreDoc scoreDoc = hits[i];
    Document doc = searcher.doc(scoreDoc.doc);

    long timestamp = Long.parseLong(doc.get("timestamp_store"));
    double freshness = freshnessBoost(timestamp);

    double finalScore = scoreDoc.score * freshness;

    Map<String, String> result = new HashMap<>();
    result.put("title", doc.get("title"));
    result.put("url", doc.get("url"));
    result.put("snippet", buildSnippet(doc.get("content"), queryStr));
    result.put("domain", doc.get("domain"));
    result.put("crawlTime", doc.get("timestamp_store"));
    result.put("score", String.valueOf(finalScore));

    results.add(result);
}
    // 🔹 Sort by boosted (freshness-aware) score
results.sort((a, b) ->
        Double.compare(
                Double.parseDouble(b.get("score")),
                Double.parseDouble(a.get("score"))
        )
);



        reader.close();

        Map<String, Object> response = new HashMap<>();
        response.put("totalResults", topDocs.totalHits.value);
        response.put("results", results);

        return response;
    }
    private static String buildSnippet(String content, String query) {
    if (content == null || content.isEmpty()) return "";

    String lower = content.toLowerCase();
    String q = query.toLowerCase();

    int index = lower.indexOf(q);
    if (index == -1) {
        return content.substring(0, Math.min(160, content.length())) + "...";
    }

    int start = Math.max(0, index - 60);
    int end = Math.min(content.length(), index + q.length() + 100);

    return content.substring(start, end)
            .replaceAll("\\s+", " ")
            + "...";
}

private static double freshnessBoost(long timestamp) {
    long ageMillis = System.currentTimeMillis() - timestamp;
    long ageHours = ageMillis / (1000 * 60 * 60);

    if (ageHours < 1) return 2.0;        // last 1 hour
    if (ageHours < 6) return 1.7;
    if (ageHours < 24) return 1.4;
    if (ageHours < 72) return 1.1;

    return 1.0; // older news
}


}
