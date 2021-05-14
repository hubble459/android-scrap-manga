package nl.hubble.scrapmanga.filter;

import android.widget.BaseAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import nl.hubble.scrapmanga.model.Reading;

@SuppressWarnings("unchecked")
public class ReadingFilter extends Filter {
    private final List<Reading> filtered;
    private final BaseAdapter adapter;
    private List<Reading> original;

    public ReadingFilter(List<Reading> reading, BaseAdapter adapter) {
        this.filtered = reading;
        this.original = new ArrayList<>(reading);
        this.adapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults fr = new FilterResults();

        if (constraint == null || constraint.length() == 0) {
            fr.values = new ArrayList<>(original);
            fr.count = original.size();
        } else {
            String constraintString = constraint.toString().toLowerCase();

            ArrayList<Reading> newValues = new ArrayList<>();

            for (Reading value : original) {
                String valueText = value.title.toLowerCase() + ' ' + value.hostname.toLowerCase();

                // First match against the whole, non-splitted value
                if (matches(valueText, constraintString)) {
                    newValues.add(value);
                } else {
                    String[] words = constraintString.split(" ");
                    for (String word : words) {
                        if (("unread".contains(word) || word.contains("unread"))
                                && value.totalChapters - value.chapter > 0) {
                            newValues.add(value);
                            break;
                        }
                    }
                }
            }

            fr.values = newValues;
            fr.count = newValues.size();
        }

        return fr;
    }

    private boolean matches(String valueText, String constraint) {
        int count = 0;

        String[] constraints = constraint.split(" ");
        String[] valueWords = valueText.split(" ");
        for (String c : constraints) {
            for (String val : valueWords) {
                if (val.contains(c)) {
                    ++count;
                    break;
                }
            }
        }
        return count >= constraints.length;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        filtered.clear();
        filtered.addAll((List<Reading>) results.values);
        if (results.count > 0) {
            adapter.notifyDataSetChanged();
        } else {
            adapter.notifyDataSetInvalidated();
        }
    }

    public void notifyDataSetChanged(List<Reading> original) {
        this.original = new ArrayList<>(original);
    }

    public List<Reading> getList() {
        return original;
    }
}
