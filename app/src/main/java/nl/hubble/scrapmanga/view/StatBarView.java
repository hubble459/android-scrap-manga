package nl.hubble.scrapmanga.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Locale;

import nl.hubble.scrapmanga.R;


public class StatBarView extends LinearLayout {
    private TextView read, unread, total;
    private ProgressBar statBar;

    public StatBarView(Context context) {
        super(context);
        init();
    }

    public StatBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_stat_bar, this);

        read = view.findViewById(R.id.numberStart);
        unread = view.findViewById(R.id.numberEnd);
        total = view.findViewById(R.id.total);
        statBar = view.findViewById(R.id.statBar);
    }

    private String totalFormat(int total) {
        return String.format(Locale.ENGLISH, "Total: %d", total);
    }

    private String readFormat(int read) {
        return String.format(Locale.ENGLISH, "Read: %d", read);
    }

    private String unreadFormat(int unread) {
        return String.format(Locale.ENGLISH, "Unread: %d", unread);
    }

    public void setRead(int number) {
        this.statBar.setProgress(number);
        this.read.setText(readFormat(number));
    }

    public void setUnread(int number) {
        this.unread.setText(unreadFormat(number));
    }

    public void setTotal(int total) {
        this.statBar.setMax(total);
        this.total.setText(totalFormat(total));
    }
}
