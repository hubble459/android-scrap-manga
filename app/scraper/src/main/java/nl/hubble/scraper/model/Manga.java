package nl.hubble.scraper.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Manga implements Serializable {
    public long id;
    public String hostname;
    public String title;
    public String description;
    public String href;
    public String cover;
    public boolean status;
    public long updated;
    public List<String> altTitles;
    public List<String> authors;
    public List<String> genres;
    public List<Chapter> chapters;

    @NonNull
    @Override
    public String toString() {
        return "Manga{" +
                "hostname='" + hostname + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", href='" + href + '\'' +
                ", img='" + cover + '\'' +
                ", status=" + status +
                ", updated=" + updated +
                ", altTitles=" + altTitles +
                ", authors=" + authors +
                ", genres=" + genres +
                ", chapters=" + chapters +
                '}';
    }
}
