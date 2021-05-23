package nl.hubble.scrapmanga.ui;

import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.util.DatabaseHelper;

public class QueryActivity extends CustomActivity {
    private ListView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        EditText sqlQuery = findViewById(R.id.sql_query);
        sqlQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                syntaxHighlighting(s);
            }
        });

        findViewById(R.id.execute).setOnClickListener(v -> {
            try {
                runQuery(sqlQuery.getText().toString());
            } catch (SQLiteException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        output = findViewById(R.id.output);
    }

    private void runQuery(String query) {
        SQLiteDatabase db = DatabaseHelper.getDatabase(this);
        Cursor cursor = db.rawQuery(query, null);

        String[] columnNames = cursor.getColumnNames();
        int columns = cursor.getColumnCount();
        cursor.moveToNext();
        ArrayList<String> results = new ArrayList<>();
        results.add(arrayToString(columnNames));
        int count = cursor.getCount();
        for (int row = 0; row < count; row++) {
            String[] rowData = new String[columns];
            for (int column = 0; column < columns; column++) {
                CharArrayBuffer cab = new CharArrayBuffer(0);
                cursor.copyStringToBuffer(column, cab);
                String s = new String(cab.data);
                rowData[column] = s;
            }
            results.add(arrayToString(rowData));
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        output.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, results));
    }

    private <T> String arrayToString(T[] array) {
        StringBuilder sb = new StringBuilder();
        for (T t : array) {
            sb.append(t).append("; ");
        }
        return sb.substring(0, Math.max(0, sb.lastIndexOf("; ")));
    }

    private final ArrayList<ForegroundColorSpan> spans = new ArrayList<>();
    private final String[] statements = {
            ";",
            "add",
            "alter",
            "and",
            "as",
            "asc",
            "avg",
            "between",
            "count",
            "create",
            "delete",
            "desc",
            "distinct",
            "drop",
            "from",
            "group",
            "having",
            "insert",
            "inner",
            "into",
            "is",
            "join",
            "like",
            "limit",
            "max",
            "min",
            "on",
            "or",
            "order by",
            "round",
            "select",
            "sum",
            "table",
            "update",
            "values",
            "where",
            "with",
    };

    private final String[] tables = {
            "reading",
            "manga",
            "chapters",
            "null",
            "\\(",
            "\\)"
    };

    private void syntaxHighlighting(Editable s) {
        for (ForegroundColorSpan span : spans) {
            s.removeSpan(span);
        }
        spans.clear();

        String text = s.toString().toLowerCase();
        for (String statement : statements) {
            highlight(Color.parseColor("#ffa500"), s, text, Pattern.compile(' ' + statement + '|' + statement + ' '), 0);
        }

        for (String table : tables) {
            highlight(Color.parseColor("#B00B69"), s, text, Pattern.compile(' ' + table + '|' + table + ' '), 0);
        }

        highlight(Color.parseColor("green"), s, text, Pattern.compile("('.*')|(\".*\")"), 0);
        highlight(Color.parseColor("cyan"), s, text, Pattern.compile("\\d+"), 0);
    }

    private void highlight(int color, Editable e, String text, Pattern p, int offset) {
        Matcher m = p.matcher(text);
        if (m.find(offset)) {
            int index = m.start();
            ForegroundColorSpan span = new ForegroundColorSpan(color);
            spans.add(span);
            int end = m.end();
            e.setSpan(
                    span,
                    index,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            highlight(color, e, text, p, end);
        }
    }
}