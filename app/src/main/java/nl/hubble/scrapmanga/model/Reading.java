package nl.hubble.scrapmanga.model;

import java.io.Serializable;

public class Reading implements Serializable {
    public long id;
    public String href;
    public String title;
    public String hostname;
    public int totalChapters;
    public int chapter;
    public boolean autoRefresh;
    public long refreshed;
}
