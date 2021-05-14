package nl.hubble.scraper.type;

import java.net.URL;
import java.util.List;

import nl.hubble.scraper.model.Manga;

public interface BaseScraper {
    Manga parse(URL url, int timeout) throws Exception;

    List<Manga> search(String query, int timeout) throws Exception;

    List<String> images(URL url, int timeout) throws Exception;

    boolean accepts(URL url);
}
