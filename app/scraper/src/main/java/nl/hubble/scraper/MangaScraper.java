package nl.hubble.scraper;

import android.content.Context;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.type.ArangScans;
import nl.hubble.scraper.type.BaseScraper;
import nl.hubble.scraper.type.LHTranslation;
import nl.hubble.scraper.type.Leviatan;
import nl.hubble.scraper.type.MangaDex;
import nl.hubble.scraper.type.MangaKakalot;
import nl.hubble.scraper.type.MangaStream;
import nl.hubble.scraper.type.Query;
import nl.hubble.scraper.type.Webtoon;
import nl.hubble.scraper.type.WhimSubs;

/**
 * - MangaSushi
 * - bato.to
 * - fanfox.net
 * - mngdoom.com
 * - niadd.com
 * - mangafreak.net
 * - mangapark.net
 * - mangainn.net
 * - mngdoom.com
 * - honto.jp
 * - holymanga.net
 * - manytoon.com
 */
public class MangaScraper {
    private final ArrayList<BaseScraper> scrapers = new ArrayList<>();
    private final Query queryScraper;

    public MangaScraper(Context context) {
        this.queryScraper = new Query(context);
        scrapers.add(new ArangScans(context));
        scrapers.add(new MangaDex(context));
        scrapers.add(new MangaStream(context));
        scrapers.add(new Leviatan(context));
        scrapers.add(new LHTranslation(context));
        scrapers.add(new MangaKakalot(context));
        scrapers.add(new Webtoon(context));
        scrapers.add(new WhimSubs(context));
    }

    public Manga parse(URL url, int timeout) throws Exception {
        Manga manga = null;
        for (BaseScraper scraper : scrapers) {
            if (scraper.accepts(url)) {
                manga = scraper.parse(url, timeout);
                break;
            }
        }
        if (manga == null) {
            manga = queryScraper.parse(url, timeout);
        }
        if (manga.getHostname() == null || manga.getHostname().isEmpty()) {
            manga.setHostname(url.getHost());
        }
        if (manga.getHref() == null || manga.getHref().isEmpty()) {
            manga.setHref(url.toString());
        }
        return manga;
    }

    public List<String> images(URL url, int timeout) throws Exception {
        List<String> images = null;
        for (BaseScraper scraper : scrapers) {
            if (scraper.accepts(url)) {
                images = scraper.images(url, timeout);
                break;
            }
        }
        if (images == null) {
            images = queryScraper.images(url, timeout);
        }
        return images;
    }

    public List<Manga> search(String query, int timeout) throws Exception {
        List<Manga> results = new ArrayList<>();
        for (BaseScraper scraper : scrapers) {
            List<Manga> res = scraper.search(query, timeout);
            if (res != null) {
                results.addAll(res);
            }
        }
        return results;
    }

    public Manga parse(URL url) throws Exception {
        return parse(url, 20000);
    }

    public List<String> images(URL url) throws Exception {
        return images(url, 20000);
    }
}
