package com.swiftseek.searchengine;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class SearchengineApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchengineApplication.class, args);
    }
}
