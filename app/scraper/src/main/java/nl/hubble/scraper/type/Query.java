package nl.hubble.scraper.type;

import android.content.Context;

import org.json.JSONArray;
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

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;

public class Query implements BaseScraper {
    protected static final String SPLIT_REGEX = "(, | ; | : | - )";
    protected Context context;
    protected int timeout;
    protected Document doc;
    protected String titleQuery;
    protected String statusQuery;
    protected String descQuery;
    protected String coverQuery;
    protected String coverAttrQuery;
    protected String authorsQuery;
    protected String altTitlesQuery;
    protected String genresQuery;
    protected String chapterRowQuery;
    protected String chapterTitleQuery;
    protected String chapterNumberQuery;
    protected String chapterPostedQuery;
    protected String chapterPostedAttrQuery;
    protected String chapterHrefQuery;
    protected String imageQuery;
    protected String imageAttrQuery;

    public Query(Context context) {
        this.context = context;
    }

    private void initQueries(String hostname_) throws Exception {
        JSONObject object = Utils.Parse.readQueryFile(context);

        StringBuilder titleQuery = new StringBuilder();
        StringBuilder statusQuery = new StringBuilder();
        StringBuilder descQuery = new StringBuilder();
        StringBuilder coverQuery = new StringBuilder();
        StringBuilder coverAttrQuery = new StringBuilder();
        StringBuilder authorsQuery = new StringBuilder();
        StringBuilder altTitlesQuery = new StringBuilder();
        StringBuilder genresQuery = new StringBuilder();
        StringBuilder chapterRowQuery = new StringBuilder();
        StringBuilder chapterTitleQuery = new StringBuilder();
        StringBuilder chapterNumberQuery = new StringBuilder();
        StringBuilder chapterPostedQuery = new StringBuilder();
        StringBuilder chapterPostedAttrQuery = new StringBuilder();
        StringBuilder chapterHrefQuery = new StringBuilder();

        boolean lockTitle = false;
        boolean lockStatus = false;
        boolean lockDesc = false;
        boolean lockCover = false;
        boolean lockCoverAttr = false;
        boolean lockAuthors = false;
        boolean lockAlts = false;
        boolean lockGenres = false;
        boolean lockRow = false;
        boolean lockCTitle = false;
        boolean lockNumber = false;
        boolean lockPosted = false;
        boolean lockPostedAttr = false;
        boolean lockHref = false;

        JSONArray names = object.names();
        if (names == null) return;
        for (int i = 0; i < names.length(); i++) {
            String hostname = names.getString(i);
            JSONObject config = object.getJSONObject(hostname);

            if (hostname_.contains(hostname)) {
                if (config.has("title")) {
                    lockTitle = true;
                    String title = config.getString("title");
                    if (!title.isEmpty()) {
                        titleQuery = new StringBuilder(title);
                    }
                }
                if (config.has("status")) {
                    lockStatus = true;
                    String status = config.getString("status");
                    if (!status.isEmpty()) {
                        statusQuery = new StringBuilder(status);
                    }
                }
                if (config.has("description")) {
                    lockDesc = true;
                    String desc = config.getString("description");
                    if (!desc.isEmpty()) {
                        descQuery = new StringBuilder(desc);
                    }
                }
                if (config.has("cover")) {
                    lockCover = true;
                    String cover = config.getString("cover");
                    if (!cover.isEmpty()) {
                        coverQuery = new StringBuilder(cover);
                    }
                }
                if (config.has("cover_attr")) {
                    lockCoverAttr = true;
                    String coverAttr = config.getString("cover_attr");
                    if (!coverAttr.isEmpty()) {
                        coverAttrQuery = new StringBuilder(coverAttr);
                    }
                }
                if (config.has("authors")) {
                    lockAuthors = true;
                    String authors = config.getString("authors");
                    if (!authors.isEmpty()) {
                        authorsQuery = new StringBuilder(authors);
                    }
                }
                if (config.has("alternative_titles")) {
                    lockAlts = true;
                    String alts = config.getString("alternative_titles");
                    if (!alts.isEmpty()) {
                        altTitlesQuery = new StringBuilder(alts);
                    }
                }
                if (config.has("genres")) {
                    lockGenres = true;
                    String genres = config.getString("genres");
                    if (!genres.isEmpty()) {
                        genresQuery = new StringBuilder(genres);
                    }
                }
                if (config.has("chapter_row")) {
                    lockRow = true;
                    String row = config.getString("chapter_row");
                    if (!row.isEmpty()) {
                        chapterRowQuery = new StringBuilder(row);
                    }
                }
                if (config.has("chapter_title")) {
                    lockCTitle = true;
                    String cTitle = config.getString("chapter_title");
                    if (!cTitle.isEmpty()) {
                        chapterTitleQuery = new StringBuilder(cTitle);
                    }
                }
                if (config.has("chapter_number")) {
                    lockNumber = true;
                    String number = config.getString("chapter_number");
                    if (!number.isEmpty()) {
                        chapterNumberQuery = new StringBuilder(number);
                    }
                }
                if (config.has("chapter_posted")) {
                    lockPosted = true;
                    String posted = config.getString("chapter_posted");
                    if (!posted.isEmpty()) {
                        chapterPostedQuery = new StringBuilder(posted);
                    }
                }
                if (config.has("chapter_posted_attr")) {
                    lockPostedAttr = true;
                    String attr = config.getString("chapter_posted_attr");
                    if (!attr.isEmpty()) {
                        chapterPostedAttrQuery = new StringBuilder(attr);
                    }
                }
                if (config.has("chapter_href")) {
                    lockHref = true;
                    String href = config.getString("chapter_href");
                    if (!href.isEmpty()) {
                        chapterHrefQuery = new StringBuilder(href);
                    }
                }
            } else {
                if (!lockTitle && config.has("title")) {
                    String title = config.getString("title");
                    if (!title.isEmpty()) {
                        if (titleQuery.length() != 0) {
                            titleQuery.append(", ");
                        }
                        titleQuery.append(title);
                    }
                }
                if (!lockStatus && config.has("status")) {
                    String status = config.getString("status");
                    if (!status.isEmpty()) {
                        if (statusQuery.length() != 0) {
                            statusQuery.append(", ");
                        }
                        statusQuery.append(status);
                    }
                }
                if (!lockDesc && config.has("description")) {
                    String desc = config.getString("description");
                    if (!desc.isEmpty()) {
                        if (descQuery.length() != 0) {
                            descQuery.append(", ");
                        }
                        descQuery.append(desc);
                    }
                }
                if (!lockCover && config.has("cover")) {
                    String cover = config.getString("cover");
                    if (!cover.isEmpty()) {
                        if (coverQuery.length() != 0) {
                            coverQuery.append(", ");
                        }
                        coverQuery.append(cover);
                    }
                }
                if (!lockCoverAttr && config.has("cover_attr")) {
                    String coverAttr = config.getString("cover_attr");
                    if (!coverAttr.isEmpty()) {
                        if (coverAttrQuery.length() != 0) {
                            coverAttrQuery.append(", ");
                        }
                        coverAttrQuery.append(coverAttr);
                    }
                }
                if (!lockAuthors && config.has("authors")) {
                    String authors = config.getString("authors");
                    if (!authors.isEmpty()) {
                        if (authorsQuery.length() != 0) {
                            authorsQuery.append(", ");
                        }
                        authorsQuery.append(authors);
                    }
                }
                if (!lockAlts && config.has("alternative_titles")) {
                    String alts = config.getString("alternative_titles");
                    if (!alts.isEmpty()) {
                        if (altTitlesQuery.length() != 0) {
                            altTitlesQuery.append(", ");
                        }
                        altTitlesQuery.append(alts);
                    }
                }
                if (!lockGenres && config.has("genres")) {
                    String genres = config.getString("genres");
                    if (!genres.isEmpty()) {
                        if (genresQuery.length() != 0) {
                            genresQuery.append(", ");
                        }
                        genresQuery.append(genres);
                    }
                }
                if (!lockRow && config.has("chapter_row")) {
                    String row = config.getString("chapter_row");
                    if (!row.isEmpty()) {
                        if (chapterRowQuery.length() != 0) {
                            chapterRowQuery.append(", ");
                        }
                        chapterRowQuery.append(row);
                    }
                }
                if (!lockCTitle && config.has("chapter_title")) {
                    String cTitle = config.getString("chapter_title");
                    if (!cTitle.isEmpty()) {
                        if (chapterTitleQuery.length() != 0) {
                            chapterTitleQuery.append(", ");
                        }
                        chapterTitleQuery.append(cTitle);
                    }
                }
                if (!lockNumber && config.has("chapter_number")) {
                    String number = config.getString("chapter_number");
                    if (!number.isEmpty()) {
                        if (chapterNumberQuery.length() != 0) {
                            chapterNumberQuery.append(", ");
                        }
                        chapterNumberQuery.append(number);
                    }
                }
                if (!lockPosted && config.has("chapter_posted")) {
                    String posted = config.getString("chapter_posted");
                    if (!posted.isEmpty()) {
                        if (chapterPostedQuery.length() != 0) {
                            chapterPostedQuery.append(", ");
                        }
                        chapterPostedQuery.append(posted);
                    }
                }
                if (!lockPostedAttr && config.has("chapter_posted_attr")) {
                    String attr = config.getString("chapter_posted_attr");
                    if (!attr.isEmpty()) {
                        if (chapterPostedAttrQuery.length() != 0) {
                            chapterPostedAttrQuery.append(", ");
                        }
                        chapterPostedAttrQuery.append(attr);
                    }
                }
                if (!lockHref && config.has("chapter_href")) {
                    String href = config.getString("chapter_href");
                    if (!href.isEmpty()) {
                        if (chapterHrefQuery.length() != 0) {
                            chapterHrefQuery.append(", ");
                        }
                        chapterHrefQuery.append(href);
                    }
                }
            }
        }

        this.titleQuery = titleQuery.toString();
        this.statusQuery = statusQuery.toString();
        this.descQuery = descQuery.toString();
        this.coverQuery = coverQuery.toString();
        this.coverAttrQuery = coverAttrQuery.toString();
        this.authorsQuery = authorsQuery.toString();
        this.altTitlesQuery = altTitlesQuery.toString();
        this.genresQuery = genresQuery.toString();
        this.chapterRowQuery = chapterRowQuery.toString();
        this.chapterTitleQuery = chapterTitleQuery.toString();
        this.chapterNumberQuery = chapterNumberQuery.toString();
        this.chapterPostedQuery = chapterPostedQuery.toString();
        this.chapterPostedAttrQuery = chapterPostedAttrQuery.toString();
        this.chapterHrefQuery = chapterHrefQuery.toString();
    }

    private void initImageQuery(String hostname_) throws Exception {
        JSONObject object = Utils.Parse.readQueryFile(context);

        StringBuilder imageQuery = new StringBuilder();
        StringBuilder imageAttrQuery = new StringBuilder();

        boolean lockImage = false;
        boolean lockImageAttr = false;

        JSONArray names = object.names();
        if (names == null) return;
        for (int i = 0; i < names.length(); i++) {
            String hostname = names.getString(i);
            JSONObject config = object.getJSONObject(hostname);

            if (hostname_.contains(hostname)) {
                if (config.has("image")) {
                    lockImage = true;
                    String image = config.getString("image");
                    if (!image.isEmpty()) {
                        imageQuery = new StringBuilder(image);
                    }
                }
                if (config.has("image_attr")) {
                    lockImageAttr = true;
                    String imageAttr = config.getString("image_attr");
                    if (!imageAttr.isEmpty()) {
                        imageAttrQuery = new StringBuilder(imageAttr);
                    }
                }
            } else {
                if (!lockImage && config.has("image")) {
                    String image = config.getString("image");
                    if (!image.isEmpty()) {
                        if (imageQuery.length() != 0) {
                            imageQuery.append(", ");
                        }
                        imageQuery.append(image);
                    }
                }
                if (!lockImageAttr && config.has("image_attr")) {
                    String imageAttr = config.getString("image_attr");
                    if (!imageAttr.isEmpty()) {
                        if (imageAttrQuery.length() != 0) {
                            imageAttrQuery.append(", ");
                        }
                        imageAttrQuery.append(imageAttr);
                    }
                }
            }
        }

        this.imageQuery = imageQuery.toString();
        this.imageAttrQuery = imageAttrQuery.toString();
    }

    protected void getDocument(URL url) throws IOException {
        doc = Jsoup.connect(url.toExternalForm())
                .timeout(timeout)
                .followRedirects(true)
                .get();
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
        manga.hostname = url.getHost();
        manga.href = url.toString();

        initQueries(manga.hostname);

        manga.title = title();
        manga.status = status();
        manga.description = description();
        manga.cover = cover();
        manga.authors = authors();
        manga.altTitles = altTitles(manga.title);
        manga.genres = genres();
        manga.chapters = chapters();

        return manga;
    }

    @Override
    public List<Manga> search(String query, int timeout) throws Exception {
        return null;
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
        doc = Jsoup.parse(url, timeout);

        initImageQuery(url.getHost());

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
    public boolean accepts(URL url) {
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
            return first.ownText();
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
                        chapter.title = title.ownText();
                    } else {
                        continue;
                    }
                    chapter.number = -1;
                    if (!chapterNumberQuery.isEmpty()) {
                        chapter.number = convertToNumber(chapElement.selectFirst(chapterNumberQuery));
                    }
                    if (chapter.number == -1) {
                        chapter.number = Utils.Parse.convertToNumber(chapter.title);
                    }
                    if (!chapterPostedQuery.isEmpty()) {
                        chapter.posted = toTime(chapElement.selectFirst(chapterPostedQuery));
                    } else {
                        chapter.posted = -1;
                    }
                    chapter.href = chapElement.selectFirst(chapterHrefQuery).absUrl("href");
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
}
