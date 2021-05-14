package nl.hubble.scraper.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Chapter implements Serializable {
    public long id;
    public String title;
    public String href;
    public double number;
    public long posted;

    @NonNull
    @Override
    public String toString() {
        return "Chapter{" +
                "title='" + title + '\'' +
                ", href='" + href + '\'' +
                ", number=" + number +
                ", posted=" + posted +
                '}';
    }
}
