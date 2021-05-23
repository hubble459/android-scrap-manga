package nl.hubble.scraper.type;

import java.net.URL;
import java.util.List;

import nl.hubble.scraper.model.Manga;

public interface BaseScraper {
    String[] hostnames();

    Manga parse(URL url, int timeout) throws Exception;

    List<Manga> search(String hostname, String query, int timeout) throws Exception;

    List<String> images(URL url, int timeout) throws Exception;

    default boolean accepts(URL url) {
        return accepts(url.getHost());
    }

    default boolean accepts(String hostname) {
        for (String accept : hostnames()) {
            if (hostname.equals(accept)) {
                return true;
            }
        }
        return false;
    }

    boolean canSearch();

    default String refactorQuery(String query) {
        return query;
    }
}
