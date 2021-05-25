package nl.hubble.scraper.type;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;

public class Madara extends QueryScraper {
    public Madara(Context context) {
        super(context);
    }

    public Manga parse(URL url, int timeout, Document doc) throws Exception {
        this.doc = doc;
        return super.parse(url, timeout);
    }

    @Override
    public String[] hostnames() {
        return new String[]{
                "mangakik.com",
                "manhuaus.com",
                "mangaweebs.in",
                "isekaiscanmanga.com",
                "manhuaplus.com",
                "mangasushi.net",
                "1stkissmanga.com",
                "mangafoxfull.com",
                "s2manga.com",
                "manhwatop.com",
                "manga68.com",
                "manga347.com",
                "mixedmanga.com",
                "manhuadex.com",
                "mangachill.com",
                "mangarockteam.com",
                "mangazukiteam.com",
                "azmanhwa.net",
                "mangafunny.com",
                "mangatx.com",
                "yaoi.mobi",
        };
    }

    @Override
    protected List<Chapter> chapters() {
        try {
            Element script = doc.selectFirst("input.rating-post-id, #wp-manga-js-extra");
            String id;
            if (script.tagName().equals("script")) {
                String data = script.data().split("\"manga_id\":\"", 2)[1];
                id = data.substring(0, data.indexOf("\"}"));
            } else {
                id = script.attr("value");
            }

            doc = Jsoup
                    .connect(url.getProtocol() + "://" + url.getHost() + "/wp-admin/admin-ajax.php")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .data("action", "manga_get_chapters")
                    .data("manga", id)
                    .post();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.chapters();
    }
}
