package nl.hubble.scraper.type;

import android.content.Context;

public class MangaNelo extends QueryScraper {
    public MangaNelo(Context context) {
        super(context);
    }

    @Override
    public String refactorQuery(String query) {
        return query.replaceAll("[\\W\\s]", "_");
    }

    @Override
    public String[] hostnames() {
        return new String[]{"manganelo.com", "m.mangabat.com", "readmanganato.com", "manganato.com"};
    }
}
