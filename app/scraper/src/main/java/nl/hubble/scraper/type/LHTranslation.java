package nl.hubble.scraper.type;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;

public class LHTranslation extends QueryScraper {
    public LHTranslation(Context context) {
        super(context);
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        Manga manga = super.parse(url, timeout);
        if (!manga.getAuthors().isEmpty()) {
            manga.getAuthors().set(0, manga.getAuthors().get(0).replace("Author(s): ", ""));
        }
        if (!manga.getAltTitles().isEmpty()) {
            manga.getAltTitles().set(0, manga.getAltTitles().get(0).replace("Other names: ", ""));
        }
        return manga;
    }

    @Override
    public List<Manga> search(String hostname, String query, int timeout) throws Exception {
        URL url = new URL("https://lhtranslation.net/app/manga/controllers/search.single.php?q=" + query);
        hostname = url.getHost();
        JSONObject object = Utils.Parse.getJSON(url, timeout, hostname);
        JSONArray data = object.getJSONArray("data");
        ArrayList<Manga> results = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject result = data.getJSONObject(i);
            Manga manga = new Manga();
            manga.setHostname(hostname);
            manga.setTitle(Utils.Parse.toNormalCase(result.getString("primary")));
            manga.setCover(result.getString("image"));
            String script = result.getString("onclick");
            String href = "https://lhtranslation.net/" + script.substring("window.location='".length(), script.length() - 1);
            manga.setHref(href);
            results.add(manga);
        }
        return results;
    }

    @Override
    public String[] hostnames() {
        return new String[]{"lhtranslation.net"};
    }
}
