package nl.hubble.scraper;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.type.BaseScraper;
import nl.hubble.scraper.type.KissManga;
import nl.hubble.scraper.type.LHTranslation;
import nl.hubble.scraper.type.Madara;
import nl.hubble.scraper.type.MangaDex5;
import nl.hubble.scraper.type.MangaFreak;
import nl.hubble.scraper.type.MangaKakalot;
import nl.hubble.scraper.type.MangaNelo;
import nl.hubble.scraper.type.MangaStream;
import nl.hubble.scraper.type.MngDoom;
import nl.hubble.scraper.type.QueryScraper;
import nl.hubble.scraper.type.WhimSubs;
import nl.hubble.scraper.type.ZeroScans;
import nl.hubble.scraper.util.Utils;

/**
 * - MADARA -> USE API /wp-json/wp/v2/search | posts
 * - bato.to
 * - niadd.com
 * - honto.jp
 * - holymanga.net
 * - manytoon.com
 * - mangapark.net <- CLOUDFLARE
 * - fanfox.net <- CLOUDFLARE
 */
public class MangaScraper {
    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36";
    private final ArrayList<BaseScraper> scrapers = new ArrayList<>();
    private final Madara madara;

    public MangaScraper(Context context) {
        scrapers.add(new MangaKakalot(context));
        scrapers.add(new MangaNelo(context));
        scrapers.add(new KissManga(context));
        this.madara = new Madara(context) {
            @Override
            protected void getDocument() {
            }

            @Override
            protected void initQueries(String hostname) throws Exception {
                super.initQueries("mangakik.com");
            }
        };
        scrapers.add(new Madara(context));
        scrapers.add(new MangaDex5(context));
        scrapers.add(new MangaStream(context));
        scrapers.add(new MangaFreak(context));
        scrapers.add(new MngDoom(context));
        scrapers.add(new LHTranslation(context));
        scrapers.add(new WhimSubs(context));
        scrapers.add(new ZeroScans(context));
        scrapers.add(new QueryScraper(context));
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
            Document doc = Jsoup.connect(url.toExternalForm())
                    .timeout(timeout)
                    .userAgent(MangaScraper.USER_AGENT)
                    .followRedirects(true)
                    .get();
            boolean isMadara = doc.getElementById("madara-comments") != null;
            if (isMadara) {
                manga = madara.parse(url, timeout, doc);
            }

            if (manga == null) {
                throw new Exception(String.format("Website '%s' not supported (yet)", url.getHost()));
            }
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
        return images;
    }

    public List<Manga> search(String hostname, String query, int timeout) throws Exception {
        List<Manga> results = new ArrayList<>();
        for (BaseScraper scraper : scrapers) {
            if (scraper.accepts(hostname)) {
                results = scraper.search(hostname, query, timeout);
                break;
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

    public ArrayList<String> getEngineNames() {
        ArrayList<String> engines = new ArrayList<>();
        for (BaseScraper scraper : scrapers) {
            for (String hostname : scraper.hostnames()) {
                if (!engines.contains(hostname)) {
                    engines.add(hostname);
                }
            }
        }
        return engines;
    }

    public ArrayList<BaseScraper> getSearchEngines() {
        ArrayList<BaseScraper> searchEngines = new ArrayList<>();
        for (BaseScraper scraper : scrapers) {
            if (scraper.canSearch()) {
                searchEngines.add(scraper);
            }
        }
        return searchEngines;
    }

    public ArrayList<String> getSearchEnginesNames() {
        ArrayList<String> searchEngineNames = new ArrayList<>();
        ArrayList<BaseScraper> searchEngines = getSearchEngines();
        for (BaseScraper searchEngine : searchEngines) {
            if (searchEngine instanceof QueryScraper) {
                ArrayList<String> searchableHostnames = ((QueryScraper) searchEngine).searchableHostnames();
                for (String shn : searchableHostnames) {
                    if (!searchEngineNames.contains(shn)) {
                        searchEngineNames.add(shn);
                    }
                }
            } else {
                for (String hostname : searchEngine.hostnames()) {
                    if (!searchEngineNames.contains(hostname)) {
                        searchEngineNames.add(hostname);
                    }
                }
            }
        }
        return searchEngineNames;
    }
}
