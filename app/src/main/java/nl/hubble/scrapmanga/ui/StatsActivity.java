package nl.hubble.scrapmanga.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Locale;

import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.util.DatabaseHelper;
import nl.hubble.scrapmanga.view.StatBarView;

public class StatsActivity extends CustomActivity {
    private TextView time;
    private int totalChapsRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        setStats();

        initCalculator();
    }

    private void initCalculator() {
        NumberPicker np = findViewById(R.id.avgTimePicker);
        NumberPicker.Formatter formatter = value -> {
            if (value == 1) {
                return value + " min";
            } else {
                return value + " mins";
            }
        };
        np.setFormatter(formatter);
        np.setMaxValue(60);
        np.setMinValue(1);
        np.setValue(3);

        formatTime(3);
        np.setOnValueChangedListener((picker, oldVal, newVal) -> formatTime(newVal));
    }

    private void formatTime(int value) {
        if (time == null) {
            time = findViewById(R.id.time);
        }

        int totalMin = totalChapsRead * value;
        int totalHour = totalMin / 60;
        int day = totalHour / 24;
        int hour = totalHour % 24;
        int min = totalMin % 60;
        time.setText(String.format(Locale.ENGLISH, "%dd %dh %dm", day, hour, min));
    }

    private void setStats() {
        SQLiteDatabase db = DatabaseHelper.getDatabase(this);
        // Queries
        Cursor totalCursor = db.rawQuery("SELECT count() FROM reading", null);
        totalCursor.moveToNext();
        Cursor readCursor = db.rawQuery("SELECT count() FROM reading WHERE chapter IS total_chapters", null);
        readCursor.moveToNext();

        // Vars
        int total = totalCursor.getInt(0);
        int totalRead = readCursor.getInt(0);
        int unread = total - totalRead;

        // Close
        totalCursor.close();
        readCursor.close();

        // Use
        StatBarView mangaStats = findViewById(R.id.mangaStats);
        mangaStats.setTotal(total);
        mangaStats.setRead(totalRead);
        mangaStats.setUnread(unread);

        // Queries
        totalCursor = db.rawQuery("SELECT sum(total_chapters) FROM reading", null);
        totalCursor.moveToNext();
        readCursor = db.rawQuery("SELECT sum(chapter) FROM reading", null);
        readCursor.moveToNext();

        // Vars
        total = totalCursor.getInt(0);
        totalChapsRead = readCursor.getInt(0);
        unread = total - totalChapsRead;

        // Close
        totalCursor.close();
        readCursor.close();

        // Use
        StatBarView chaptersStats = findViewById(R.id.chaptersStats);
        chaptersStats.setTotal(total);
        chaptersStats.setRead(totalChapsRead);
        chaptersStats.setUnread(unread);

        // Close db
        db.close();
    }
}