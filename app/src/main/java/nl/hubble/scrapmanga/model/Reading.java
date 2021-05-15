package nl.hubble.scrapmanga.model;

import java.io.Serializable;

public class Reading implements Serializable {
    private long id;
    private String href;
    private String title;
    private String hostname;
    private int totalChapters;
    private int chapter;
    private boolean autoRefresh;
    private long refreshed;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getTotalChapters() {
        return totalChapters;
    }

    public void setTotalChapters(int totalChapters) {
        this.totalChapters = totalChapters;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public long getRefreshed() {
        return refreshed;
    }

    public void setRefreshed(long refreshed) {
        this.refreshed = refreshed;
    }
}
