package nl.hubble.scraper.type;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

import nl.hubble.scraper.model.Chapter;

public class Madara extends QueryScraper {
    public Madara(Context context) {
        super(context);
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
        };
    }

    @Override
    protected List<Chapter> chapters() {
        try {
            Element script = doc.selectFirst("#wp-manga-js-extra, input.rating-post-id");
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

    private boolean isDoubleImages(String hostname) {
        final String[] doubleImages = {
                "mangahz.com",
                "mangakik.com"
        };
        for (String doubleImage : doubleImages) {
            if (hostname.equals(doubleImage)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Elements searchImages() {
        Elements elements = super.searchImages();
        Elements filtered = new Elements();
        if (isDoubleImages(url.getHost())) {
            for (int i = 0; i < elements.size(); i++) {
                if (i % 2 == 0) {
                    filtered.add(elements.get(i));
                }
            }
        } else {
            filtered = elements;
        }
        return filtered;
    }
}
