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

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "manga.db";
    private static final int VERSION = 1;
    private static final String TAG = "DatabaseHelper";
    private final String path;

    public DatabaseHelper(@NonNull Context context) {
        super(new DatabaseContext(context), DB_NAME, null, VERSION);
        Log.i(TAG, "onCreate: " + getFilePath(context));
        Log.i(TAG, "onCreate: " + getDatabaseDir(context));
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

    public static Reading getReading(SQLiteDatabase db, long readingId) {
        Reading reading = null;
        Cursor c = db.rawQuery("SELECT * FROM reading WHERE reading_id IS ?", new String[]{String.valueOf(readingId)});
        c.moveToNext();
        if (c.getCount() == 1) {
            reading = new Reading();
            reading.id = c.getInt(c.getColumnIndex("reading_id"));
            reading.href = c.getString(c.getColumnIndex("href"));
            reading.title = c.getString(c.getColumnIndex("title"));
            reading.hostname = c.getString(c.getColumnIndex("hostname"));
            reading.totalChapters = c.getInt(c.getColumnIndex("total_chapters"));
            reading.chapter = c.getInt(c.getColumnIndex("chapter"));
            reading.refreshed = c.getLong(c.getColumnIndex("refreshed"));
            reading.autoRefresh = c.getInt(c.getColumnIndex("auto_refresh")) == 1;
        }
        c.close();
        return reading;
    }

    public static void reset(SQLiteDatabase db, Reading reading) {
        db.delete("reading", "reading_id IS ?", new String[]{String.valueOf(reading.id)});
    }

    public static Manga getManga(SQLiteDatabase db, Reading reading) {
        Manga manga = null;
        Cursor c = db.rawQuery("SELECT * FROM manga WHERE reading_id IS ?;", new String[]{String.valueOf(reading.id)});
        c.moveToNext();
        if (c.getCount() == 1) {
            manga = new Manga();
            manga.id = c.getInt(c.getColumnIndex("manga_id"));
            manga.href = reading.href;
            manga.title = reading.title;
            manga.hostname = reading.hostname;
            manga.authors = Arrays.asList(c.getString(c.getColumnIndex("authors")).split("&;"));
            manga.genres = Arrays.asList(c.getString(c.getColumnIndex("genres")).split("&;"));
            manga.altTitles = Arrays.asList(c.getString(c.getColumnIndex("alt_titles")).split("&;"));
            manga.description = c.getString(c.getColumnIndex("description"));
            manga.cover = c.getString(c.getColumnIndex("cover_url"));
            manga.status = c.getInt(c.getColumnIndex("status")) == 1;
            manga.updated = c.getLong(c.getColumnIndex("updated"));
            manga.chapters = getChapters(db, manga);
        }
        c.close();
        return manga;
    }

    public static boolean exists(SQLiteDatabase db, String url) {
        Cursor c = db.rawQuery("SELECT href FROM reading WHERE href IS ?;", new String[]{url});
        int count = c.getCount();
        c.close();
        return count == 1;
    }

    public static void insertManga(SQLiteDatabase db, Reading reading, Manga manga) {
        ContentValues cv = new ContentValues();
        cv.put("reading_id", reading.id);
        cv.put("description", manga.description);
        cv.put("authors", listAsString(manga.authors));
        cv.put("genres", listAsString(manga.genres));
        cv.put("alt_titles", listAsString(manga.altTitles));
        cv.put("cover_url", manga.cover);
        cv.put("status", manga.status ? 1 : 0);
        cv.put("updated", manga.updated);
        manga.id = db.insert("manga", null, cv);

        for (Chapter chapter : manga.chapters) {
            cv = new ContentValues();
            cv.put("manga_id", manga.id);
            cv.put("title", chapter.title);
            cv.put("href", chapter.href);
            cv.put("number", chapter.number);
            cv.put("posted", chapter.posted);
            chapter.id = db.insert("chapters", null, cv);
        }
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

    public static void updateManga(SQLiteDatabase db, Manga manga) {
        ContentValues cv = new ContentValues();
        cv.put("description", manga.description);
        cv.put("authors", listAsString(manga.authors));
        cv.put("genres", listAsString(manga.genres));
        cv.put("alt_titles", listAsString(manga.altTitles));
        cv.put("cover_url", manga.cover);
        cv.put("status", manga.status ? 1 : 0);
        cv.put("updated", manga.updated);
        db.update("manga", cv, "manga_id IS ?", new String[]{String.valueOf(manga.id)});

        db.delete("chapters", "manga_id IS ?", new String[]{String.valueOf(manga.id)});
        for (Chapter chapter : manga.chapters) {
            cv = new ContentValues();
            cv.put("manga_id", manga.id);
            cv.put("title", chapter.title);
            cv.put("href", chapter.href);
            cv.put("number", chapter.number);
            cv.put("posted", chapter.posted);
            chapter.id = db.insert("chapters", null, cv);
        }
    }

    public static void updateManga(SQLiteDatabase db, Manga manga, Reading reading) {
        ContentValues cv = new ContentValues();
        cv.put("description", manga.description);
        cv.put("authors", listAsString(manga.authors));
        cv.put("genres", listAsString(manga.genres));
        cv.put("alt_titles", listAsString(manga.altTitles));
        cv.put("cover_url", manga.cover);
        cv.put("status", manga.status ? 1 : 0);
        cv.put("updated", manga.updated);
        db.update("manga", cv, "reading_id IS ?", new String[]{String.valueOf(reading.id)});

        db.delete("chapters", "manga_id IS ?", new String[]{String.valueOf(manga.id)});
        for (Chapter chapter : manga.chapters) {
            cv = new ContentValues();
            cv.put("manga_id", manga.id);
            cv.put("title", chapter.title);
            cv.put("href", chapter.href);
            cv.put("number", chapter.number);
            cv.put("posted", chapter.posted);
            chapter.id = db.insert("chapters", null, cv);
        }
    }

    public static List<Chapter> getChapters(SQLiteDatabase db, Manga manga) {
        List<Chapter> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM chapters WHERE manga_id IS ?", new String[]{String.valueOf(manga.id)});
        int chapter_id = c.getColumnIndex("chapter_id");
        int title = c.getColumnIndex("title");
        int href = c.getColumnIndex("href");
        int number = c.getColumnIndex("number");
        int posted = c.getColumnIndex("posted");

        while (c.moveToNext()) {
            Chapter chapter = new Chapter();
            chapter.id = c.getInt(chapter_id);
            chapter.title = c.getString(title);
            chapter.href = c.getString(href);
            chapter.number = c.getInt(number);
            chapter.posted = c.getLong(posted);
            list.add(chapter);
        }
        c.close();
        return list;
    }

    public static List<Reading> getAllReading(SQLiteDatabase db) {
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
            r.id = cursor.getInt(idIndex);
            r.href = cursor.getString(hrefIndex);
            r.title = cursor.getString(titleIndex);
            r.hostname = cursor.getString(hostIndex);
            r.totalChapters = cursor.getInt(totalIndex);
            r.chapter = cursor.getInt(chapterIndex);
            r.refreshed = cursor.getLong(refreshedIndex);
            r.autoRefresh = cursor.getInt(autoRIndex) == 1;
            list.add(r);
        }
        cursor.close();
        return list;
    }

    public static void insertReading(SQLiteDatabase db, Reading reading) {
        ContentValues cv = new ContentValues();
        cv.put("href", reading.href);
        cv.put("title", reading.title);
        cv.put("hostname", reading.hostname);
        cv.put("total_chapters", reading.totalChapters);
        cv.put("chapter", reading.chapter);
        cv.put("refreshed", reading.refreshed = System.currentTimeMillis());
        reading.id = db.insert("reading", null, cv);
    }

    public static void updateReading(SQLiteDatabase db, Reading reading) {
        ContentValues cv = new ContentValues();
        cv.put("title", reading.title);
        cv.put("hostname", reading.hostname);
        cv.put("chapter", reading.chapter);
        cv.put("total_chapters", reading.totalChapters);
        cv.put("refreshed", reading.refreshed);
        db.update("reading", cv, "href IS ?", new String[]{reading.href});
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
                    "status INTEGER DEFAULT 0, " +
                    "updated INTEGER DEFAULT 0, " +
                    "FOREIGN KEY (reading_id) " +
                    "REFERENCES reading (reading_id) " +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE);");

            db.execSQL("CREATE TABLE chapters (" +
                    "chapter_id INTEGER PRIMARY KEY, " +
                    "manga_id INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "href TEXT NOT NULL, " +
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
//            if (!db.isOpen()) {
//                db = SQLiteDatabase.openOrCreateDatabase(path, null);
//            }
//            db.rawQuery("ALTER TABLE reading ADD hostname TEXT", null)
//                    .close();
//            db.rawQuery("ALTER TABLE reading ADD glide_mode INTEGER", null)
//                    .close();
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        super.onOpen(db);
    }
}