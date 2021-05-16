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
import nl.hubble.scraper.type.MangaNelo;
import nl.hubble.scraper.type.MangaStream;
import nl.hubble.scraper.type.MngDoom;
import nl.hubble.scraper.type.QueryScraper;
import nl.hubble.scraper.type.Webtoon;
import nl.hubble.scraper.type.WhimSubs;

/**
 * - MangaSushi
 * - bato.to
 * - niadd.com
 * - mangafreak.net
 * - mangainn.net
 * - honto.jp
 * - holymanga.net
 * - manytoon.com
 * - mangapark.net <- CLOUDFLARE
 * - fanfox.net <- CLOUDFLARE
 */
public class MangaScraper {
    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";
    private final ArrayList<BaseScraper> scrapers = new ArrayList<>();

    public MangaScraper(Context context) {
        scrapers.add(new ArangScans(context));
        scrapers.add(new MangaDex(context));
        scrapers.add(new MangaStream(context));
        scrapers.add(new MngDoom(context));
        scrapers.add(new Leviatan(context));
        scrapers.add(new LHTranslation(context));
        scrapers.add(new MangaKakalot(context));
        scrapers.add(new MangaNelo(context));
        scrapers.add(new Webtoon(context));
        scrapers.add(new WhimSubs(context));
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
            throw new Exception(String.format("Website '%s' not supported (yet)", url.getHost()));
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
            if (scraper.accepts(hostname) || hostname == null) {
                List<Manga> res = scraper.search(hostname, query, timeout);
                if (res != null) {
                    results.addAll(res);
                }
                if (hostname != null) {
                    break;
                }
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
