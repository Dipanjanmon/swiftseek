package com.swiftseek.searchengine.crawler;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class RobotsTxtUtil {

    public static boolean isAllowed(String targetUrl) {

        try {
            URL url = new URL(targetUrl);
            String robotsUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";

            String robotsTxt = Jsoup.connect(robotsUrl)
                    .userAgent("SwiftSeekBot/1.0")
                    .timeout(5000)
                    .ignoreHttpErrors(true)
                    .execute()
                    .body();

            if (robotsTxt == null || robotsTxt.isEmpty()) {
                return true; // no robots.txt → allowed
            }

            String[] lines = robotsTxt.split("\n");
            boolean appliesToUs = false;

            for (String line : lines) {
                line = line.trim().toLowerCase();

                if (line.startsWith("user-agent:")) {
                    appliesToUs = line.contains("*") || line.contains("swiftseekbot");
                }

                if (appliesToUs && line.startsWith("disallow:")) {
                    String path = line.replace("disallow:", "").trim();
                    if (path.equals("/") || targetUrl.contains(path)) {
                        return false;
                    }
                }
            }

        } catch (IOException e) {
            return true; // fail-open (industry practice)
        }

        return true;
    }
}
