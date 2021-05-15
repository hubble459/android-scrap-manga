package nl.hubble.scraper.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Manga implements Serializable {
    private long id;
    private String hostname;
    private String title;
    private String description;
    private String href;
    private String cover;
    private boolean status;
    private long updated;
    private List<String> altTitles;
    private List<String> authors;
    private List<String> genres;
    private List<Chapter> chapters;

    @NonNull
    @Override
    public String toString() {
        return "Manga{" +
                "hostname='" + getHostname() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", href='" + getHref() + '\'' +
                ", img='" + getCover() + '\'' +
                ", status=" + isStatus() +
                ", updated=" + getUpdated() +
                ", altTitles=" + getAltTitles() +
                ", authors=" + getAuthors() +
                ", genres=" + getGenres() +
                ", chapters=" + getChapters() +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public List<String> getAltTitles() {
        return altTitles;
    }

    public void setAltTitles(List<String> altTitles) {
        this.altTitles = altTitles;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
}
