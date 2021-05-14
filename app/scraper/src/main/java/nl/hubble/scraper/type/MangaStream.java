package nl.hubble.scraper.type;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MangaStream extends Query {
    private final String[] accepts = new String[]{"mangastream"};

    public MangaStream(Context context) {
        super(context);
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
        Document doc = Jsoup.parse(url, timeout);

        Element p = doc.selectFirst("#arraydata");
        String data = p.text();

        return new ArrayList<>(Arrays.asList(data.split(",")));
    }

    @Override
    public boolean accepts(URL url) {
        String hostname = url.getHost();
        for (String accept : accepts) {
            if (hostname.contains(accept)) {
                return true;
            }
        }
        return false;
    }
}
