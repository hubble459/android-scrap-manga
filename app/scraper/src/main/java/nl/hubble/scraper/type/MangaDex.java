package nl.hubble.scraper.type;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;

public class MangaDex implements BaseScraper {
    private final String[] genres;
    private final Context context;

    public MangaDex(Context context) {
        this.context = context;
        this.genres = new String[86];
        fillGenres();
    }

    @Override
    public String[] hostnames() {
        return new String[]{"mangadex"};
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        url = parseURL(url);

        JSONObject object = Utils.Parse.getJSON(url, timeout);

        String status = object.getString("status");
        if (status.equals("OK")) {
            Manga manga = new Manga();

            JSONObject mangaJSON = object.getJSONObject("manga");
            manga.setTitle(Jsoup.parse(mangaJSON.getString("title")).text());
            manga.setCover("https://mangadex.org" + mangaJSON.getString("cover_url"));

            String description = mangaJSON.getString("description");
            description = Jsoup.parse(description).wholeText();
            manga.setDescription(parseDesc(description));

            manga.setUpdated(mangaJSON.getLong("last_updated") * 1000);
            manga.setStatus(toStatus(mangaJSON.getInt("status")));

            String[] authors = mangaJSON.getString("author").split(", ");
            manga.setAuthors(Arrays.asList(authors));

            JSONArray altNames = mangaJSON.getJSONArray("alt_names");
            ArrayList<String> altTitles = new ArrayList<>();
            for (int i = 0; i < altNames.length(); i++) {
                String title = altNames.getString(i);
                altTitles.add(title);
            }
            manga.setAltTitles(altTitles);

            JSONArray genreNumbers = mangaJSON.getJSONArray("genres");
            ArrayList<String> genreList = new ArrayList<>();
            for (int i = 0; i < genreNumbers.length(); i++) {
                String title = genres[genreNumbers.getInt(i)];
                genreList.add(title);
            }
            manga.setGenres(genreList);

            JSONObject chaptersObject = object.getJSONObject("chapter");
            ArrayList<Chapter> chapters = new ArrayList<>();
            Iterator<?> iterator = chaptersObject.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();

                JSONObject chObject = chaptersObject.getJSONObject(key);
                String lang = chObject.getString("lang_name").toLowerCase();
                if (lang.equals("english")) {
                    Chapter chapter = new Chapter();
                    chapter.setNumber(Utils.Parse.convertToNumber(chObject.getString("chapter")));
                    String title = chObject.getString("title");
                    if (title.isEmpty()) {
                        title = "Chapter " + chapter.getNumber();
                    }
                    chapter.setTitle(title);
                    chapter.setPosted(chObject.getLong("timestamp") * 1000);
                    chapter.setHref("https://mangadex.org/api/?type=chapter&id=" + key);

                    chapters.add(chapter);
                }
            }
            manga.setChapters(chapters);
            return manga;
        }

        throw new Exception("Failed to read from website");
    }

    @Override
    public List<Manga> search(String hostname, String query, int timeout) {
        return null;
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
        url = parseURL(url);

        String path = url.getPath();
        if (path.contains("/api/")) {
            JSONObject object = Utils.Parse.getJSON(url, timeout);

            List<String> images = new ArrayList<>();
            String hash = object.getString("hash");
            String server = object.getString("server");
            JSONArray array = object.getJSONArray("page_array");

            for (int i = 0; i < array.length(); i++) {
                String filename = array.getString(i);
                String link = server + hash + '/' + filename;
                images.add(link);
            }

            return images;
        }
        throw new Exception("Failed to read from website");
    }

    @Override
    public boolean canSearch() {
        return false;
    }

    private URL parseURL(URL url) throws MalformedURLException {
        String path = url.getPath() + '?' + url.getQuery();
        if (path.matches(".*/api/\\?((type|id)=(\\d+|chapter|manga)(&)?){2}")
                || path.matches("api\\.mangadex.org/.*")) return url;

        if (path.matches(".*/(title|chapter)/.*")) {
            String id = path.split("/(title|chapter)/")[1].split("/")[0];
            String type = path.contains("chapter") ? "chapter" : "manga";
            url = new URL(String.format("https://api.mangadex.org/v2/%s/%s", type, id));
        } else {
            throw new MalformedURLException("Failed to parse from this URL");
        }
        return url;
    }

    private String parseDesc(String desc) {
        return desc
                .replaceAll("\\[\\*]", "* ")
                .replaceAll("\\[hr]", "------------\n\n")
                .replaceAll("\\[/?u]", "*")
                .replaceAll("\\[/?b]", "**")
                .replaceAll("\\[li]", " - ")
                .replaceAll("\\[url=", "[")
                .replaceAll("].*\\[/url]", "]");
    }

    private boolean toStatus(int status) {
        switch (status) {
            case 0:
            case 2:
                return false;
            default:
                return true;
        }
    }

    private void fillGenres() {
        genres[1] = "4-Koma";
        genres[2] = "Action";
        genres[3] = "Adventure";
        genres[4] = "Award Winning";
        genres[5] = "Comedy";
        genres[6] = "Cooking";
        genres[7] = "Doujinshi";
        genres[8] = "Drama";
        genres[9] = "Ecchi";
        genres[10] = "Fantasy";
        genres[11] = "Gyaru";
        genres[12] = "Harem";
        genres[13] = "Historical";
        genres[14] = "Horror";
        genres[15] = "";
        genres[16] = "Martial Arts";
        genres[17] = "Mecha";
        genres[18] = "Medical";
        genres[19] = "Music";
        genres[20] = "Mystery";
        genres[21] = "Oneshot";
        genres[22] = "Psychological";
        genres[23] = "Romance";
        genres[24] = "School Life";
        genres[25] = "Sci-Fi";
        genres[26] = "";
        genres[27] = "";
        genres[28] = "Shoujo Ai";
        genres[29] = "";
        genres[30] = "Shounen Ai";
        genres[31] = "Slice of Life";
        genres[32] = "Smut";
        genres[33] = "Sports";
        genres[34] = "Supernatural";
        genres[35] = "Tragedy";
        genres[36] = "Long Strip";
        genres[37] = "Yaoi";
        genres[38] = "Yuri";
        genres[39] = "";
        genres[40] = "Video Games";
        genres[41] = "Isekai";
        genres[42] = "Adaptation";
        genres[43] = "Anthology";
        genres[44] = "Web Comic";
        genres[45] = "Full Color";
        genres[46] = "User Created";
        genres[47] = "Official Colored";
        genres[48] = "Fan Colored";
        genres[49] = "Gore";
        genres[50] = "Sexual Violence";
        genres[51] = "Crime";
        genres[52] = "Magical Girls";
        genres[53] = "Philosophical";
        genres[54] = "Superhero";
        genres[55] = "Thriller";
        genres[56] = "Wuxia";
        genres[57] = "Aliens";
        genres[58] = "Animals";
        genres[59] = "Crossdressing";
        genres[60] = "Demons";
        genres[61] = "Delinquents";
        genres[62] = "Genderswap";
        genres[63] = "Ghosts";
        genres[64] = "Monster Girls";
        genres[65] = "Loli";
        genres[66] = "Magic";
        genres[67] = "Military";
        genres[68] = "Monsters";
        genres[69] = "Ninja";
        genres[70] = "Office Workers";
        genres[71] = "Police";
        genres[72] = "Post-Apocalyptic";
        genres[73] = "Reincarnation";
        genres[74] = "Reverse Harem";
        genres[75] = "Samurai";
        genres[76] = "Shota";
        genres[77] = "Survival";
        genres[78] = "Time Travel";
        genres[79] = "Vampires";
        genres[80] = "Traditional Games";
        genres[81] = "Virtual Reality";
        genres[82] = "Zombies";
        genres[83] = "Incest";
        genres[84] = "Mafia";
        genres[85] = "Villainess";
    }
}
