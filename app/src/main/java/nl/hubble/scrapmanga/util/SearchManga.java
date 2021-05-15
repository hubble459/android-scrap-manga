package nl.hubble.scrapmanga.util;

import java.util.List;

import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.type.BaseScraper;

public class SearchManga extends Thread implements Runnable {
    private final BaseScraper scraper;
    private final String query;
    private final OnFinishedListener listener;

    public SearchManga(BaseScraper scraper, String query, OnFinishedListener listener) {
        this.scraper = scraper;
        this.query = scraper.refactorQuery(query);
        this.listener = listener;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            listener.finished(scraper.search(query, 20000));
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
