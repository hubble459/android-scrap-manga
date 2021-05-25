package nl.hubble.scrapmanga.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scrapmanga.model.DatabaseContext;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.ui.QueryActivity;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "manga.db";
    private static final String TAG = "DatabaseHelper";
    private static final int VERSION = 1;
    private final String path;

    public DatabaseHelper(@NonNull Context context) {
        super(new DatabaseContext(context), DB_NAME, null, VERSION);
        this.path = getFilePath(context);
    }

    public static String getFilePath(Context context) {
        char s = File.separatorChar;
        return getDatabaseDir(context) + s + DB_NAME;
    }

    public static String getDatabaseDir(Context context) {
        return context.getExternalFilesDir(null).getAbsolutePath();
    }

    public static SQLiteDatabase getDatabase(Context context) {
        return new DatabaseHelper(context).getWritableDatabase();
    }

    public static Reading getReading(Context context, long readingId) {
        SQLiteDatabase db = getDatabase(context);
        Reading reading = null;
        Cursor c = db.rawQuery("SELECT * FROM reading WHERE reading_id IS ?", new String[]{String.valueOf(readingId)});
        c.moveToNext();
        if (c.getCount() == 1) {
            reading = new Reading();
            reading.setId(c.getInt(c.getColumnIndex("reading_id")));
            reading.setHref(c.getString(c.getColumnIndex("href")));
            reading.setTitle(c.getString(c.getColumnIndex("title")));
            reading.setHostname(c.getString(c.getColumnIndex("hostname")));
            reading.setTotalChapters(c.getInt(c.getColumnIndex("total_chapters")));
            reading.setChapter(c.getInt(c.getColumnIndex("chapter")));
            reading.setRefreshed(c.getLong(c.getColumnIndex("refreshed")));
            reading.setAutoRefresh(c.getInt(c.getColumnIndex("auto_refresh")) == 1);
        }
        c.close();
        db.close();
        return reading;
    }

    public static void remove(Context context, Reading reading) {
        SQLiteDatabase db = getDatabase(context);

        Manga manga = getManga(context, db, reading);
        String basePath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator + "chapters" + File.separator;
        for (Chapter chapter : manga.getChapters()) {
            if (chapter.isDownloaded()) {
                FileUtil.remove(basePath + chapter);
            }
        }
        db.delete("reading", "reading_id IS ?", new String[]{String.valueOf(reading.getId())});

        db.close();
    }

    private static Manga getManga(Context context, SQLiteDatabase db, Reading reading) {
        Manga manga = null;
        Cursor c = db.rawQuery("SELECT * FROM manga WHERE reading_id IS ?;", new String[]{String.valueOf(reading.getId())});
        c.moveToNext();
        if (c.getCount() == 1) {
            manga = new Manga();
            manga.setId(c.getInt(c.getColumnIndex("manga_id")));
            manga.setHref(reading.getHref());
            manga.setTitle(reading.getTitle());
            manga.setHostname(reading.getHostname());
            manga.setAuthors(Arrays.asList(c.getString(c.getColumnIndex("authors")).split("&;")));
            manga.setGenres(Arrays.asList(c.getString(c.getColumnIndex("genres")).split("&;")));
            manga.setAltTitles(Arrays.asList(c.getString(c.getColumnIndex("alt_titles")).split("&;")));
            manga.setDescription(c.getString(c.getColumnIndex("description")));
            manga.setCover(c.getString(c.getColumnIndex("cover_url")));
            manga.setStatus(c.getInt(c.getColumnIndex("status")) == 1);
            manga.setInterval(c.getString(c.getColumnIndex("interval")));
            manga.setUpdated(c.getLong(c.getColumnIndex("updated")));
            manga.setChapters(getChapters(context, db, manga));
        }
        c.close();
        return manga;
    }

    public static Manga getManga(Context context, Reading reading) {
        SQLiteDatabase db = getDatabase(context);
        Manga manga = getManga(context, db, reading);
        db.close();
        return manga;
    }

    public static int exists(Context context, String url) {
        SQLiteDatabase db = getDatabase(context);
        Cursor c = db.rawQuery("SELECT * FROM reading WHERE href IS ?;", new String[]{url});
        int count = c.getCount();
        int readingId = -1;
        if (count == 1) {
            c.moveToNext();
            readingId = c.getInt(c.getColumnIndex("reading_id"));
        }
        c.close();
        db.close();
        return readingId;
    }

    public static void insertManga(Context context, Reading reading, Manga manga) {
        SQLiteDatabase db = getDatabase(context);
        ContentValues cv = new ContentValues();
        cv.put("reading_id", reading.getId());
        cv.put("description", manga.getDescription());
        cv.put("authors", listAsString(manga.getAuthors()));
        cv.put("genres", listAsString(manga.getGenres()));
        cv.put("alt_titles", listAsString(manga.getAltTitles()));
        cv.put("cover_url", manga.getCover());
        cv.put("status", manga.getStatus() ? 1 : 0);
        cv.put("interval", manga.getInterval());
        cv.put("updated", manga.getUpdated());
        manga.setId(db.insert("manga", null, cv));

        for (Chapter chapter : manga.getChapters()) {
            cv = new ContentValues();
            cv.put("manga_id", manga.getId());
            cv.put("title", chapter.getTitle());
            cv.put("href", chapter.getHref());
            cv.put("page", chapter.getPage());
            cv.put("number", chapter.getNumber());
            cv.put("posted", chapter.getPosted());
            chapter.setId(db.insert("chapters", null, cv));
        }
        db.close();
    }

    private static String listAsString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append("&;");
            }
        }
        return sb.toString();
    }

    public static void updateManga(Context context, Manga manga) {
        SQLiteDatabase db = getDatabase(context);
        ContentValues cv = new ContentValues();
        cv.put("description", manga.getDescription());
        cv.put("authors", listAsString(manga.getAuthors()));
        cv.put("genres", listAsString(manga.getGenres()));
        cv.put("alt_titles", listAsString(manga.getAltTitles()));
        cv.put("cover_url", manga.getCover());
        cv.put("status", manga.getStatus() ? 1 : 0);
        cv.put("interval", manga.getInterval());
        cv.put("updated", manga.getUpdated());
        db.update("manga", cv, "manga_id IS ?", new String[]{String.valueOf(manga.getId())});

        db.delete("chapters", "manga_id IS ?", new String[]{String.valueOf(manga.getId())});
        for (Chapter chapter : manga.getChapters()) {
            cv = new ContentValues();
            cv.put("manga_id", manga.getId());
            cv.put("title", chapter.getTitle());
            cv.put("href", chapter.getHref());
            cv.put("page", chapter.getPage());
            cv.put("number", chapter.getNumber());
            cv.put("posted", chapter.getPosted());
            chapter.setId(db.insert("chapters", null, cv));
        }
        db.close();
    }

    public static void updateManga(Context context, Manga manga, long readingId) {
        SQLiteDatabase db = getDatabase(context);
        ContentValues cv = new ContentValues();
        cv.put("description", manga.getDescription());
        cv.put("authors", listAsString(manga.getAuthors()));
        cv.put("genres", listAsString(manga.getGenres()));
        cv.put("alt_titles", listAsString(manga.getAltTitles()));
        cv.put("cover_url", manga.getCover());
        cv.put("status", manga.getStatus() ? 1 : 0);
        cv.put("interval", manga.getInterval());
        cv.put("updated", manga.getUpdated());
        db.update("manga", cv, "reading_id IS ?", new String[]{String.valueOf(readingId)});

        int mangaId = 0;
        Cursor c = db.rawQuery("SELECT manga_id FROM MANGA WHERE reading_id IS ?", new String[]{String.valueOf(readingId)});
        while (c.moveToNext()) {
            mangaId = c.getInt(0);
        }
        c.close();
        manga.setId(mangaId);

        db.delete("chapters", "manga_id IS ?", new String[]{String.valueOf(manga.getId())});
        for (Chapter chapter : manga.getChapters()) {
            cv = new ContentValues();
            cv.put("manga_id", manga.getId());
            cv.put("title", chapter.getTitle());
            cv.put("href", chapter.getHref());
            cv.put("page", chapter.getPage());
            cv.put("number", chapter.getNumber());
            cv.put("posted", chapter.getPosted());
            chapter.setId(db.insert("chapters", null, cv));
        }
        db.close();
    }

    private static List<Chapter> getChapters(Context context, SQLiteDatabase db, Manga manga) {
        List<Chapter> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM chapters WHERE manga_id IS ?", new String[]{String.valueOf(manga.getId())});
        int chapter_id = c.getColumnIndex("chapter_id");
        int title = c.getColumnIndex("title");
        int href = c.getColumnIndex("href");
        int page = c.getColumnIndex("page");
        int number = c.getColumnIndex("number");
        int posted = c.getColumnIndex("posted");

        String basePath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator + "chapters" + File.separator;

        while (c.moveToNext()) {
            Chapter chapter = new Chapter();
            chapter.setId(c.getInt(chapter_id));
            chapter.setTitle(c.getString(title));
            chapter.setHref(c.getString(href));
            chapter.setPage(c.getInt(page));
            chapter.setNumber(c.getInt(number));
            chapter.setPosted(c.getLong(posted));
            chapter.setDownloaded(FileUtil.exists(basePath + chapter.getId()));
            list.add(chapter);
        }
        c.close();
        return list;
    }

    public static List<Reading> getAllReading(Context context) {
        SQLiteDatabase db = getDatabase(context);
        Cursor cursor = db.rawQuery("SELECT * FROM reading ORDER BY title ASC;", null);
        int idIndex = cursor.getColumnIndex("reading_id");
        int hrefIndex = cursor.getColumnIndex("href");
        int titleIndex = cursor.getColumnIndex("title");
        int hostIndex = cursor.getColumnIndex("hostname");
        int totalIndex = cursor.getColumnIndex("total_chapters");
        int chapterIndex = cursor.getColumnIndex("chapter");
        int refreshedIndex = cursor.getColumnIndex("refreshed");
        int autoRIndex = cursor.getColumnIndex("auto_refresh");

        List<Reading> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            Reading r = new Reading();
            r.setId(cursor.getInt(idIndex));
            r.setHref(cursor.getString(hrefIndex));
            r.setTitle(cursor.getString(titleIndex));
            r.setHostname(cursor.getString(hostIndex));
            r.setTotalChapters(cursor.getInt(totalIndex));
            r.setChapter(cursor.getInt(chapterIndex));
            r.setRefreshed(cursor.getLong(refreshedIndex));
            r.setAutoRefresh(cursor.getInt(autoRIndex) == 1);
            list.add(r);
        }
        cursor.close();
        db.close();
        return list;
    }

    public static void insertReading(Context context, Reading reading) {
        SQLiteDatabase db = getDatabase(context);
        ContentValues cv = new ContentValues();
        cv.put("href", reading.getHref());
        cv.put("title", reading.getTitle());
        cv.put("hostname", reading.getHostname());
        cv.put("total_chapters", reading.getTotalChapters());
        cv.put("chapter", reading.getChapter());
        reading.setRefreshed(System.currentTimeMillis());
        cv.put("refreshed", reading.getRefreshed());
        reading.setId(db.insert("reading", null, cv));
        db.close();
    }

    public static void updateReading(Context context, Reading reading) {
        ContentValues cv = new ContentValues();
        cv.put("title", reading.getTitle());
        cv.put("hostname", reading.getHostname());
        cv.put("chapter", reading.getChapter());
        cv.put("total_chapters", reading.getTotalChapters());
        cv.put("refreshed", reading.getRefreshed());
        updateReading(context, reading, cv);
    }

    public static void updateReading(Context context, Reading reading, ContentValues cv) {
        SQLiteDatabase db = getDatabase(context);
        db.update("reading", cv, "reading_id IS ?", new String[]{String.valueOf(reading.getId())});
        db.close();
    }

    public static void updatePage(Context context, Chapter chapter, int page) {
        ContentValues cv = new ContentValues();
        chapter.setPage(page);
        cv.put("page", chapter.getPage());
        SQLiteDatabase db = getDatabase(context);
        db.update("chapters", cv, "chapter_id IS ?", new String[]{String.valueOf(chapter.getId())});
        db.close();
    }

    public static boolean notEmpty(Object object) {
        boolean res = false;
        if (object != null) {
            res = true;
            if (object instanceof String) {
                res = !((String) object).isEmpty();
            } else if (object instanceof Integer) {
                res = ((Integer) object) != -1;
            }
        }
        return res;
    }

    public static String arrayAsString(List<?> list) {
        return list.toString().replaceAll("[\\[\\]]", "").replace(",", ";");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (!db.isOpen()) {
            db = SQLiteDatabase.openOrCreateDatabase(path, null);
        }

        if (db.isOpen()) {
            db.execSQL("CREATE TABLE reading (" +
                    "reading_id INTEGER PRIMARY KEY, " +
                    "href TEXT NOT NULL UNIQUE, " +
                    "title TEXT NOT NULL, " +
                    "hostname TEXT NOT NULL, " +
                    "refreshed INTEGER DEFAULT 0, " +
                    "auto_refresh INTEGER DEFAULT 1, " +
                    "total_chapters INTEGER NOT NULL DEFAULT 0, " +
                    "chapter INTEGER NOT NULL DEFAULT 0);");

            db.execSQL("CREATE TABLE manga (" +
                    "manga_id INTEGER PRIMARY KEY, " +
                    "reading_id INTEGER NOT NULL, " +
                    "description TEXT DEFAULT '', " +
                    "authors TEXT DEFAULT '', " +
                    "genres TEXT DEFAULT '', " +
                    "alt_titles TEXT DEFAULT '', " +
                    "cover_url TEXT DEFAULT '', " +
                    "interval TEXT DEFAULT '', " +
                    "updated INTEGER DEFAULT 0, " +
                    "status INTEGER DEFAULT 0, " +
                    "FOREIGN KEY (reading_id) " +
                    "REFERENCES reading (reading_id) " +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE);");

            db.execSQL("CREATE TABLE chapters (" +
                    "chapter_id INTEGER PRIMARY KEY, " +
                    "manga_id INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "href TEXT NOT NULL, " +
                    "page INTEGER DEFAULT 0, " +
                    "number INTEGER NOT NULL, " +
                    "posted INTEGER NOT NULL, " +
                    "FOREIGN KEY (manga_id) " +
                    "REFERENCES manga (manga_id) " +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE);");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            Log.i(TAG, "onUpgrade: updating");
        }
        db.close();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        super.onOpen(db);
    }
}