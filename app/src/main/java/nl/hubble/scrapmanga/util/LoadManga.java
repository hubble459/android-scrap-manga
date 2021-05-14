package nl.hubble.scrapmanga.util;

import android.content.Context;

import java.net.MalformedURLException;
import java.net.URL;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.model.Manga;

public class LoadManga extends Thread implements Runnable {
    private final MangaScraper ms;
    private final URL url;
    private final OnFinishedListener listener;
    private final int timeout;

    public LoadManga(Context context, String href, OnFinishedListener listener) throws MalformedURLException {
        this(new MangaScraper(context), href, listener);
    }

    public LoadManga(MangaScraper ms, String href, OnFinishedListener listener) throws MalformedURLException {
        this(ms, href, listener, 20000);
    }

    public LoadManga(MangaScraper ms, String href, OnFinishedListener listener, int timeout) throws MalformedURLException {
        this.ms = ms;
        this.url = new URL(href);
        this.listener = listener;
        this.timeout = timeout;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            listener.finished(ms.parse(url, timeout));
        } catch (Exception e) {
//            e.printStackTrace();
            listener.error(e);
        }
    }

    public interface OnFinishedListener {
        void finished(Manga manga);

        void error(Exception e);
    }
}
