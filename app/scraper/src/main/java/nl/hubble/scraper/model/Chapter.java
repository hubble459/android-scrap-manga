package nl.hubble.scraper.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Chapter implements Serializable {
    private long id;
    private String title;
    private String href;
    private double number;
    private long posted;
    private boolean downloaded;

    @NonNull
    @Override
    public String toString() {
        return "Chapter{" +
                "title='" + getTitle() + '\'' +
                ", href='" + getHref() + '\'' +
                ", number=" + getNumber() +
                ", posted=" + getPosted() +
                ", downloaded=" + isDownloaded() +
                '}';
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        this.number = number;
    }

    public long getPosted() {
        return posted;
    }

    public void setPosted(long posted) {
        this.posted = posted;
    }
}
