package nl.hubble.scraper.type;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KissManga extends MangaNelo {
    public KissManga(Context context) {
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
    public String[] hostnames() {
        return new String[]{"kissmanga.nl", "mangabat.best"};
    }
}
