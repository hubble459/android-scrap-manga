package nl.hubble.scraper.type;

import android.content.Context;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import nl.hubble.scraper.model.Manga;

public class Webtoon extends QueryScraper {
    public Webtoon(Context context) {
        super(context);
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        if (!url.getHost().contains("m.webtoons")) {
            url = new URL(url.toString().replace("webtoons.com", "m.webtoons.com"));
        }

        return super.parse(url, timeout);
    }

    @Override
    protected void getDocument(URL url) throws IOException {
        doc = Jsoup.connect(url.toString())
                .timeout(timeout)
                .header("Cookie", "pagGDPR=true;")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Moto G (4)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Mobile Safari/537.36")
                .get();
    }

    @Override
    public String[] hostnames() {
        return new String[]{"webtoon"};
    }

    @Override
    protected List<String> authors() {
        List<String> authors = super.authors();
        if (!authors.isEmpty()) {
            authors.set(0, authors.get(0).replace(" author info", ""));
        }
        return authors;
    }
}
