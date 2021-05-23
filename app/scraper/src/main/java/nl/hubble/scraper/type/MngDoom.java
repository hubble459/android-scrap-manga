package nl.hubble.scraper.type;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MngDoom extends QueryScraper {
    public MngDoom(Context context) {
        super(context);
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
        url = new URL(url.toExternalForm() + "/all-pages");
        return super.images(url, timeout);
    }

    @Override
    protected void getDocument() throws IOException {
        doc = Jsoup.connect(url.toExternalForm())
                .timeout(timeout)
                .header("origin", url.getHost())
                .header("referer", url.getHost())
                .followRedirects(true)
                .get();
    }

    @Override
    public String[] hostnames() {
        return new String[]{"www.mngdoom.com", "www.mangainn.net"};
    }
}
