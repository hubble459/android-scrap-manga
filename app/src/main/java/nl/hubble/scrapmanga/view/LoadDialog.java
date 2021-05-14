package nl.hubble.scrapmanga.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import nl.hubble.scrapmanga.R;

public class LoadDialog extends AlertDialog.Builder {
    private final ProgressBar pb;
    private final TextView count;
    private final AlertDialog ad;
    private final List<Error> errors;
    private final Adapter adapter;
    private final OnRetryListener retryListener;

    public LoadDialog(Context context, int max, DialogInterface.OnCancelListener listener, OnRetryListener retryListener) {
        super(context);

        errors = new ArrayList<>();

        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.view_dialog_load, null);

        ListView lv = view.findViewById(R.id.error_list);
        lv.setAdapter(adapter = new Adapter(getContext(), errors));
        pb = view.findViewById(R.id.progress_bar);
        count = view.findViewById(R.id.count);

        pb.setMax(max);
        pb.setProgress(0);

        setTitle(R.string.progress);
        setView(view);
        setCancelable(false);
        setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        setPositiveButton(R.string.done, (dialog, which) -> dialog.dismiss());
        ad = show();

        Button positive = ad.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setVisibility(View.GONE);

        this.retryListener = retryListener;
    }

    public synchronized void increaseCount() {
        pb.incrementProgressBy(1);
        count.setText(String.valueOf(pb.getProgress()));
    }

    public synchronized void addError(String title, String message, Object tag) {
        errors.add(0, new Error(title, message, tag));
        adapter.notifyDataSetChanged();
    }

    public synchronized void done() {
        Button positive = ad.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setVisibility(View.VISIBLE);

        ad.setCancelable(true);
        Button negative = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (errors.isEmpty()) {
            negative.setEnabled(false);
        } else {
            negative.setEnabled(true);
            negative.setText(R.string.retry);
            negative.setOnClickListener(v -> {
                ad.dismiss();
                retryListener.onRetry(errors);
            });
        }
    }

    public interface OnRetryListener {
        void onRetry(List<Error> errors);
    }

    public static class Error {
        private final String title;
        private final String message;
        private final Object tag;

        public Error(String title, String message, Object tag) {
            this.title = title;
            this.message = message;
            this.tag = tag;
        }

        public Object getTag() {
            return tag;
        }
    }

    private static class Adapter extends ArrayAdapter<Error> {

        public Adapter(@NonNull Context context, List<Error> errors) {
            super(context, 0, errors);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Error e = getItem(position);

            TextView title = convertView.findViewById(android.R.id.text1);
            title.setText(e.title);

            TextView message = convertView.findViewById(android.R.id.text2);
            message.setText(e.message);

            return convertView;
        }
    }
}