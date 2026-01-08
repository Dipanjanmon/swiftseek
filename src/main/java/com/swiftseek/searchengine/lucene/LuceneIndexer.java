package com.swiftseek.searchengine.lucene;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneIndexer {

    private static final String INDEX_PATH = "D:/searchengine/index";
    private static Directory directory;
    private static final StandardAnalyzer analyzer = new StandardAnalyzer();

    static {
        try {
            directory = FSDirectory.open(Paths.get(INDEX_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open Lucene index", e);
        }
    }

    public static Directory getDirectory() {
        return directory;
    }

    /* ---------------------------------------------------
       SAMPLE DATA (SAFE & IDEMPOTENT)
    --------------------------------------------------- */

    public static void indexSampleData() throws IOException {

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        try (IndexWriter writer = new IndexWriter(directory, config)) {

            Document doc1 = createDoc(
                    "Java Basics Tutorial",
                    "https://example.com/java-basics",
                    "Learn Java from scratch with examples."
            );
            writer.updateDocument(
                    new Term("id", "https://example.com/java-basics"),
                    doc1
            );

            Document doc2 = createDoc(
                    "Spring Boot Search Engine",
                    "https://example.com/spring-search",
                    "Build a fast search engine using Spring Boot and Lucene."
            );
            writer.updateDocument(
                    new Term("id", "https://example.com/spring-search"),
                    doc2
            );
        }
    }

    /* ---------------------------------------------------
       SHARED DOCUMENT BUILDER
    --------------------------------------------------- */

    private static Document createDoc(String title, String url, String content) {

        Document doc = new Document();

        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));

        doc.add(new StringField("id", url, Field.Store.NO));
        doc.add(new StringField("url", url, Field.Store.YES));

        String domain = URI.create(url).getHost();
        doc.add(new StringField("domain", domain, Field.Store.YES));

        long now = System.currentTimeMillis();
        doc.add(new LongPoint("timestamp", now));
        doc.add(new StoredField("timestamp_store", now));

        return doc;
    }

    /* ---------------------------------------------------
       BACKWARD COMPATIBLE METHOD
    --------------------------------------------------- */

    public static void indexPage(String title, String url, String content)
            throws Exception {

        indexPage(
                title,
                url,
                content,
                URI.create(url).getHost(),
                System.currentTimeMillis()
        );
    }

    /* ---------------------------------------------------
       REAL CRAWLER INDEXING METHOD
    --------------------------------------------------- */

    public static void indexPage(
            String title,
            String url,
            String content,
            String domain,
            long crawlTime
    ) throws Exception {

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        try (IndexWriter writer = new IndexWriter(directory, config)) {

            Document doc = new Document();

            doc.add(new TextField("title", title, Field.Store.YES));
            doc.add(new TextField("content", content, Field.Store.YES));

            doc.add(new StringField("id", url, Field.Store.NO));
            doc.add(new StringField("url", url, Field.Store.YES));

            doc.add(new StringField("domain", domain, Field.Store.YES));
            doc.add(new LongPoint("timestamp", crawlTime));
            doc.add(new StoredField("timestamp_store", crawlTime));

            writer.updateDocument(new Term("id", url), doc);
        }
    }
}
