package nl.hubble.scrapmanga.util;

import java.util.List;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.model.Manga;

public class SearchManga extends Thread implements Runnable {
    private final MangaScraper scraper;
    private final String hostname;
    private final String query;
    private final OnFinishedListener listener;

    public SearchManga(MangaScraper scraper, String hostname, String query, OnFinishedListener listener) {
        this.scraper = scraper;
        this.hostname = hostname;
        this.query = query;
        this.listener = listener;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            listener.finished(scraper.search(hostname, query, 20000));
        } catch (Exception e) {
            e.printStackTrace();
            listener.error(e);
        }
    }

    public interface OnFinishedListener {
        void finished(List<Manga> manga);

        void error(Exception e);
    }
}
