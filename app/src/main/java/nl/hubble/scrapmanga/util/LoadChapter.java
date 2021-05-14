package nl.hubble.scrapmanga.util;

import android.content.Context;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import nl.hubble.scraper.MangaScraper;

public class LoadChapter extends Thread implements Runnable {
    private final MangaScraper ms;
    private final URL url;
    private final OnFinishedListener listener;
    private final int timeout;

    public LoadChapter(Context context, String href, OnFinishedListener listener) throws MalformedURLException {
        this(new MangaScraper(context), href, listener);
    }

    public LoadChapter(MangaScraper ms, String href, OnFinishedListener listener) throws MalformedURLException {
        this(ms, href, listener, 20000);
    }

    public LoadChapter(MangaScraper ms, String href, OnFinishedListener listener, int timeout) throws MalformedURLException {
        this.ms = ms;
        this.url = new URL(href);
        this.listener = listener;
        this.timeout = timeout;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            listener.finished(ms.images(url, timeout));
        } catch (Exception e) {
            listener.error(e);
        }
    }

    public interface OnFinishedListener {
        void finished(List<String> images);

        void error(Exception e);
    }
}
