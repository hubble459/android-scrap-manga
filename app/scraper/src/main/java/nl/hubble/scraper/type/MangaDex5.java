package nl.hubble.scraper.type;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import nl.hubble.scraper.exceptions.MangaDexException;
import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;

public class MangaDex5 implements BaseScraper {
    private final Utils.RateLimit rateLimit;

    public MangaDex5(Context unused) {
        rateLimit = new Utils.RateLimit(5, 1000);
    }

    private JSONObject getJSON(String url, int timeout) throws Exception {
        return getJSON(new URL(url), timeout);
    }

    private JSONObject getJSON(URL url, int timeout) throws Exception {
        JSONObject object = Utils.Parse.getJSON(url, timeout);
        boolean ok = object.optString("result", "ok").equals("ok");
        if (ok) {
            return object;
        } else {
            throw new Exception("Response not OK");
        }
    }

    @Override
    public String[] hostnames() {
        return new String[]{"api.mangadex.org"};
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        checkUrl(url);

        rateLimit.call();

        JSONObject object = getJSON(url, timeout);
        JSONObject data = object.getJSONObject("data");
        String mangaId = data.getString("id");
        JSONObject attributes = data.getJSONObject("attributes");
        Manga manga = new Manga();

        // Title
        JSONObject titleObj = attributes.getJSONObject("title");
        String title = titleObj.optString("en");
        if (title.isEmpty()) {
            throw new MangaDexException("Could not get title from manga");
        }
        manga.setTitle(title);

        // Authors (cba to fetch from another online api link)
        manga.setAuthors(new ArrayList<>());

        // Alt titles
        List<String> altTitles = new ArrayList<>();
        JSONArray altTitlesArr = attributes.getJSONArray("altTitles");
        for (int i = 0; i < altTitlesArr.length(); i++) {
            JSONObject alt = altTitlesArr.getJSONObject(i);
            String altTitle = alt.optString("en");
            if (!altTitle.isEmpty()) {
                altTitles.add(altTitle);
            }
        }
        manga.setAltTitles(altTitles);

        // Description
        String description = attributes.getJSONObject("description").getString("en");
        manga.setDescription(parseDescription(description));

        // Status
        manga.setStatus(attributes.getString("status").equals("ongoing"));

        // Genres
        ArrayList<String> genres = new ArrayList<>();
        JSONArray tags = attributes.getJSONArray("tags");
        for (int i = 0; i < tags.length(); i++) {
            JSONObject tag = tags.getJSONObject(i);
            if (tag.getString("type").equals("tag")) {
                genres.add(tag.getJSONObject("attributes").getJSONObject("name").getString("en"));
            }
        }
        manga.setGenres(genres);

        // Updated
        manga.setUpdated(toTime(attributes.getString("updatedAt")));

        // Chapters
        ArrayList<Chapter> chapters = new ArrayList<>();

        int offset = 0;
        int limit = 500;
        int total;
        Utils.RateLimit rateLimit = new Utils.RateLimit(5, 1000);
        do {
            JSONObject mangaFeed = getJSON(String.format(Locale.getDefault(), "https://api.mangadex.org/manga/%s/feed?limit=%d&offset=%d", mangaId, limit, offset), timeout);
            JSONArray chaptersArray = mangaFeed.getJSONArray("results");
            total = mangaFeed.getInt("total");

            rateLimit.call();

            for (int i = 0; i < chaptersArray.length(); i++) {
                JSONObject chObj = chaptersArray.getJSONObject(i).getJSONObject("data");
                if (chObj.getString("type").equals("chapter")) {
                    JSONObject chAttrs = chObj.getJSONObject("attributes");
                    if (chAttrs.optString("translatedLanguage", "en").equals("en")) {
                        Chapter chapter = new Chapter();
                        chapter.setNumber(Utils.Parse.convertToNumber(chAttrs.getString("chapter")));
                        String chTitle = chAttrs.getString("title");
                        if (chTitle.isEmpty()) {
                            chTitle = String.format(Locale.getDefault(), "Ch. %s", chapter.getNumber());
                            String volume = chAttrs.optString("volume");
                            if (!volume.isEmpty() && !volume.equals("null")) {
                                chTitle = String.format(Locale.getDefault(), "V. %s %s", volume, chTitle);
                            }
                        }
                        chapter.setTitle(chTitle);
                        chapter.setPosted(toTime(chAttrs.getString("updatedAt")));
                        String chapterId = chObj.getString("id");
                        chapter.setHref("https://api.mangadex.org/chapter/" + chapterId);
                        chapters.add(chapter);
                    }
                }
            }

        } while (offset + 500 < total);

        sortChapters(chapters);

        manga.setChapters(chapters);
        return manga;
    }

    private void checkUrl(URL url) throws MangaDexException {
        if (!url.toExternalForm().matches("^https://api\\.mangadex\\.org/manga/([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})/?$")) {
            throw new MangaDexException("Incorrect mangadex.api url");
        }
    }

    private void sortChapters(ArrayList<Chapter> chapters) {
        Collections.sort(chapters, (o1, o2) -> Double.compare(o2.getNumber(), o1.getNumber()));
    }

    private long toTime(String updatedAt) {
        return Utils.Parse.toTime(updatedAt.substring(0, updatedAt.indexOf("+")).replace('T', ' '));
    }

    @Override
    public List<Manga> search(String hostname, String query, int timeout) throws Exception {
        List<Manga> mangaList = new ArrayList<>();
        JSONArray results = getJSON("https://api.mangadex.org/manga?title=" + query, timeout).getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            JSONObject data = results.getJSONObject(i).getJSONObject("data");
            JSONObject attrs = data.getJSONObject("attributes");
            String id = data.getString("id");
            Manga manga = new Manga();
            manga.setTitle(attrs.getJSONObject("title").getString("en"));
            manga.setHref("https://api.mangadex.org/manga/" + id);
            manga.setHostname(hostname);
            mangaList.add(manga);
        }

        return mangaList;
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
        JSONObject chapter = getJSON(url, timeout).getJSONObject("data");
        String id = chapter.getString("id");
        JSONObject attributes = chapter.getJSONObject("attributes");
        String baseUrl = getJSON("https://api.mangadex.org/at-home/server/" + id, timeout).getString("baseUrl");
        String hash = attributes.getString("hash");
        String qualityMode = "data"; // "data-saver"
        JSONArray data = attributes.getJSONArray(qualityMode);

        List<String> images = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            String filename = data.getString(i);
            String link = baseUrl + '/' + qualityMode + '/' + hash + '/' + filename;
            images.add(link);
        }

        return images;
    }

    @Override
    public boolean canSearch() {
        return true;
    }

    private String parseDescription(String desc) {
        return desc
                .replaceAll("\\[\\*]", "* ")
                .replaceAll("\\[hr]", "------------\n\n")
                .replaceAll("\\[/?u]", "*")
                .replaceAll("\\[/?b]", "**")
                .replaceAll("\\[li]", " - ")
                .replaceAll("\\[url=", "[")
                .replaceAll("].*\\[/url]", "]");
    }
}
