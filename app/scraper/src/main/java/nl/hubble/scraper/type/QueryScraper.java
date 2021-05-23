package nl.hubble.scraper.type;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;

public class QueryScraper implements BaseScraper {
    protected static final String SPLIT_REGEX = "(, | ?; | : | - )";
    protected Context context;
    protected int timeout;
    protected Document doc;
    protected String titleQuery = "";
    protected String statusQuery = "";
    protected String descQuery = "";
    protected String coverQuery = "";
    protected String coverAttrQuery = "";
    protected String authorsQuery = "";
    protected String altTitlesQuery = "";
    protected String genresQuery = "";
    protected String chapterRowQuery = "";
    protected String chapterTitleQuery = "";
    protected String chapterNumberQuery = "";
    protected String chapterPostedQuery = "";
    protected String chapterPostedAttrQuery = "";
    protected String chapterHrefQuery = "";
    protected String imageQuery = "";
    protected String imageAttrQuery = "";
    protected String searchHrefQuery = "";
    protected String searchLinkQuery = "";
    protected String searchLinkAttrQuery = "";
    protected String searchTitleQuery = "";
    protected String searchImageQuery = "";
    protected String searchImageAttrQuery = "";

    public QueryScraper(Context context) {
        this.context = context;
    }

    private void initQueries(String hostname) throws Exception {
        JSONObject config = getConfig(hostname);
        if (config.has("inherits")) {
            initQueries(config.getString("inherits"));
        }
        this.titleQuery = config.optString("title", titleQuery);
        this.statusQuery = config.optString("status", statusQuery);
        this.descQuery = config.optString("description", descQuery);
        this.coverQuery = config.optString("cover", coverQuery);
        this.coverAttrQuery = config.optString("cover_attr", coverAttrQuery.isEmpty() ? "src" : coverAttrQuery);
        this.authorsQuery = config.optString("authors", authorsQuery);
        this.altTitlesQuery = config.optString("alternative_titles", altTitlesQuery);
        this.genresQuery = config.optString("genres", genresQuery);
        this.chapterRowQuery = config.optString("chapter_row", chapterRowQuery);
        this.chapterTitleQuery = config.optString("chapter_title", chapterTitleQuery);
        this.chapterNumberQuery = config.optString("chapter_number", chapterNumberQuery.isEmpty() ? chapterTitleQuery : "");
        this.chapterPostedQuery = config.optString("chapter_posted", chapterPostedQuery);
        this.chapterPostedAttrQuery = config.optString("chapter_posted_attr", chapterPostedAttrQuery);
        this.chapterHrefQuery = config.optString("chapter_href", chapterHrefQuery);

        if (titleQuery.isEmpty() || chapterTitleQuery.isEmpty() || chapterHrefQuery.isEmpty()) {
            throw new Exception("Bad query file");
        }
    }

    private void initImageQueries(String hostname) throws Exception {
        JSONObject config = getConfig(hostname);
        if (config.has("inherits")) {
            initImageQueries(config.getString("inherits"));
        }
        this.imageQuery = config.optString("image", imageQuery);
        this.imageAttrQuery = config.optString("image_attr", imageAttrQuery.isEmpty() ? "src" : imageAttrQuery);
    }

    private void initSearchQueries(String hostname) throws Exception {
        JSONObject config = getConfig(hostname);
        if (config.has("inherits")) {
            initSearchQueries(config.getString("inherits"));
        }
        this.searchHrefQuery = config.optString("search_href", searchHrefQuery);
        this.searchLinkQuery = config.optString("search_link", searchLinkQuery);
        this.searchLinkAttrQuery = config.optString("search_link_attr", searchLinkAttrQuery.isEmpty() ? "href" : searchLinkAttrQuery);
        this.searchTitleQuery = config.optString("search_title", searchTitleQuery);
        this.searchImageQuery = config.optString("search_image", searchImageQuery);
        this.searchImageAttrQuery = config.optString("search_image_attr", searchImageAttrQuery.isEmpty() ? "src" : searchImageAttrQuery);
    }

    protected void getDocument(URL url) throws IOException {
        doc = Jsoup.connect(url.toExternalForm())
                .timeout(timeout)
                .userAgent(MangaScraper.USER_AGENT)
                .followRedirects(true)
                .get();
    }

    private JSONObject getConfig(String hostname) throws Exception {
        JSONObject object = Utils.Parse.readQueryFile(context);
        JSONArray configs = object.names();
        if (configs == null) throw new Exception(String.format("Can not parse '%s'", hostname));
        for (int i = 0; i < configs.length(); i++) {
            String name = configs.getString(i);
            if (hostname.contains(name)) {
                return object.getJSONObject(name);
            }
        }
        throw new Exception(String.format("Can not parse '%s'", hostname));
    }

    @Override
    public String[] hostnames() {
        try {
            JSONObject object = Utils.Parse.readQueryFile(context);
            JSONArray names = object.names();
            if (names != null) {
                ArrayList<String> hostnames = new ArrayList<>();
                for (int i = 0; i < names.length(); i++) {
                    hostnames.add(names.getString(i));
                }
                return hostnames.toArray(new String[0]);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        this.timeout = timeout;

        getDocument(url);

        Manga manga = new Manga();
        try {
            url = new URL(doc.location());
        } catch (MalformedURLException ignored) {
        }
        manga.setHostname(url.getHost());
        manga.setHref(url.toString());

        initQueries(manga.getHostname());

        manga.setTitle(title());
        manga.setStatus(status());
        manga.setDescription(description());
        manga.setCover(cover());
        manga.setAuthors(authors());
        manga.setAltTitles(altTitles(manga.getTitle()));
        manga.setGenres(genres());
        manga.setChapters(chapters());

        return manga;
    }

    @Override
    public List<Manga> search(String hostname, String query, int timeout) throws Exception {
        initSearchQueries(hostname);

        getDocument(new URL(String.format(searchHrefQuery, refactorQuery(query))));

        ArrayList<Manga> manga = new ArrayList<>();

        Elements links = doc.select(searchLinkQuery);
        Elements titles = doc.select(searchTitleQuery);
        Elements images = doc.select(searchImageQuery);

        for (int i = 0; i < links.size(); i++) {
            Element hrf = links.get(i);
            String link = hrf.absUrl(searchLinkAttrQuery);
            if (link.isEmpty()) {
                link = hrf.attr(searchLinkAttrQuery);
            }
            Element img = images.get(i);
            String image = img.absUrl(searchImageAttrQuery);
            if (image.isEmpty()) {
                image = img.attr(searchImageAttrQuery);
            }
            String title = titles.get(i).ownText();
            Manga m = new Manga();
            m.setHref(link);
            m.setHostname(hostname);
            m.setTitle(title);
            m.setCover(image);
            manga.add(m);
        }

        return manga;
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
        getDocument(url);

        initImageQueries(url.getHost());

        List<String> images = new ArrayList<>();
        Elements elements = doc.select(imageQuery);
        for (Element element : elements) {
            String href = "";
            if (!imageAttrQuery.isEmpty()) {
                String[] split = imageAttrQuery.split(" ");
                for (String query : split) {
                    href = element.absUrl(query);
                    if (href.isEmpty()) {
                        href = element.attr(query);
                    }
                    if (!href.isEmpty()) break;
                }
            }
            if (href.isEmpty()) {
                href = element.absUrl("src");
            }
            images.add(href);
        }
        return images;
    }

    @Override
    public boolean canSearch() {
        return true;
    }

    protected String title() {
        if (titleQuery.isEmpty()) return null;

        Element first = doc.selectFirst(titleQuery);
        if (first != null) {
            return first.ownText();
        } else {
            return null;
        }
    }

    protected boolean status() {
        if (statusQuery.isEmpty()) return true;

        Element first = doc.selectFirst(statusQuery);
        if (first != null) {
            return first.ownText().toLowerCase().contains("ongoing");
        } else {
            return true;
        }
    }

    protected String description() {
        if (descQuery.isEmpty()) return null;

        Element first = doc.selectFirst(descQuery);
        if (first != null) {
            String text = first.ownText();
            if (text.isEmpty()) {
                text = first.wholeText();
            }
            return text;
        } else {
            return null;
        }
    }

    protected String cover() {
        if (coverQuery.isEmpty()) return null;

        Element first = doc.selectFirst(coverQuery);
        if (first != null) {
            String src = "";
            if (!coverAttrQuery.isEmpty()) {
                String[] split = coverAttrQuery.split(", ");
                for (String query : split) {
                    src = first.absUrl(query);
                    if (src.isEmpty()) {
                        src = first.attr(query);
                    }
                    if (!src.isEmpty()) break;
                }
            }

            if (src.isEmpty()) {
                src = first.absUrl("src");
            }

            return src;
        } else {
            return null;
        }
    }

    protected List<String> altTitles(String title) {
        if (altTitlesQuery.isEmpty()) return new ArrayList<>();

        Element first = doc.selectFirst(altTitlesQuery);
        if (first != null) {
            List<String> list = new ArrayList<>(Arrays.asList(first.text().split(SPLIT_REGEX)));
            list.remove(title);
            return list;
        } else {
            return new ArrayList<>();
        }
    }

    protected List<String> authors() {
        if (authorsQuery.isEmpty()) return new ArrayList<>();

        Elements elements = doc.select(authorsQuery);
        StringBuilder text = new StringBuilder();
        if (elements.size() == 1) {
            text.append(elements.first().text());
        } else {
            collect(elements, text);
        }
        return new ArrayList<>(Arrays.asList(text.toString().split(SPLIT_REGEX)));
    }

    protected List<String> genres() {
        if (genresQuery.isEmpty()) return new ArrayList<>();

        Elements elements = doc.select(genresQuery);
        StringBuilder text = new StringBuilder();

        collect(elements, text);
        return new ArrayList<>(Arrays.asList(text.toString().split(SPLIT_REGEX)));
    }

    protected void collect(Elements elements, StringBuilder text) {
        String t;
        for (Element element : elements) {
            if (element.childrenSize() > 0) {
                for (Element child : element.children()) {
                    if (child.childrenSize() > 0) {
                        for (Element c : child.children()) {
                            t = c.text();
                            if (!t.isEmpty()) {
                                text.append(t).append(", ");
                            }
                        }
                    } else {
                        t = child.text();
                        if (!t.isEmpty()) {
                            text.append(t).append(", ");
                        }
                    }
                }
            } else {
                t = element.text();
                if (!t.isEmpty()) {
                    text.append(t).append(", ");
                }
            }
        }
        text.substring(0, Math.max(0, text.length() - 2));
    }

    protected List<Chapter> chapters() {
        if (chapterTitleQuery.isEmpty()
                || chapterHrefQuery.isEmpty()) return null;

        List<Chapter> chapters = new ArrayList<>();

        if (!chapterRowQuery.isEmpty()) {
            Elements elements = doc.select(chapterRowQuery);

            if (elements != null) {
                for (Element chapElement : elements) {
                    Chapter chapter = new Chapter();
                    Element title = chapElement.selectFirst(chapterTitleQuery);
                    if (title != null) {
                        chapter.setTitle(title.ownText());
                    } else {
                        continue;
                    }
                    chapter.setNumber(-1);
                    if (!chapterNumberQuery.isEmpty()) {
                        chapter.setNumber(convertToNumber(chapElement.selectFirst(chapterNumberQuery)));
                    }
                    if (chapter.getNumber() == -1) {
                        chapter.setNumber(Utils.Parse.convertToNumber(chapter.getTitle()));
                    }
                    if (!chapterPostedQuery.isEmpty()) {
                        chapter.setPosted(toTime(chapElement.selectFirst(chapterPostedQuery)));
                    } else {
                        chapter.setPosted(-1);
                    }
                    String href;
                    if (chapterHrefQuery.equals("$row")) {
                        href = chapElement.absUrl("href");
                    } else {
                        href = chapElement.selectFirst(chapterHrefQuery).absUrl("href");
                    }
                    chapter.setHref(href);
                    chapters.add(chapter);
                }
            }
        }

        return chapters;
    }

    protected double convertToNumber(Element element) {
        if (element == null) return -1;
        return Utils.Parse.convertToNumber(element.ownText());
    }

    protected long toTime(Element element) {
        if (element != null) {
            String text = "";
            if (!chapterPostedAttrQuery.isEmpty()) {
                String[] split = chapterPostedAttrQuery.split(", ");
                for (String query : split) {
                    text = element.attr(query);
                    if (!text.isEmpty()) break;
                }
            }
            if (text.isEmpty()) {
                text = element.ownText();
            }

            return Utils.Parse.toTime(text);
        }
        return System.currentTimeMillis();
    }

    public ArrayList<String> searchableHostnames() {
        ArrayList<String> hostnames = new ArrayList<>();
        try {
            JSONObject object = Utils.Parse.readQueryFile(context);

            JSONArray names = object.names();
            if (names == null) return hostnames;
            for (int i = 0; i < names.length(); i++) {
                String hostname = names.getString(i);
                JSONObject config = object.getJSONObject(hostname);
                if (config.has("search_href")) {
                    hostnames.add(hostname);
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return hostnames;
    }
}
