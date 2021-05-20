package nl.hubble.scraper.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import nl.hubble.scraper.MangaScraper;

import static android.content.Context.MODE_PRIVATE;

public class Utils {
    enum DateFormat {
        MANGADEX("yy-MM-dd k:m:s"),
        MANGAKAKALOT_1("MMM-dd-yy k:m"),
        MANGAKAKALOT_2("MMM-dd-yy"),
        MANGANELO_1("MMM dd,yy - k:m a"),
        MANGANELO_2("MMM dd,yy k:m"),
        WEBTOONS("MMM dd, yy"),
        WHIMSUBS("yy.MM.dd"),
        MANGAFOX("MMM dd,yy");

        private final String format;

        DateFormat(String format) {
            this.format = format;
        }

        @NonNull
        @Override
        public String toString() {
            return format;
        }
    }

    public static class Parse {
        private static final String TAG = "ParseUtil";

        public static long toTime(String text) {
            for (DateFormat format : DateFormat.values()) {
                try {
                    Date date = new SimpleDateFormat(format.toString(), Locale.getDefault()).parse(text);
                    if (date == null) continue;
                    return date.getTime();
                } catch (ParseException ignored) {
                }
            }

            long ms = 0;
            // get number from string
            int n = (int) convertToNumber(text);
            if (text.matches("(\\W|\\d)+y.*")) {
                ms = (long) (n * 3.154e+10);
            } else if (text.matches("(\\W|\\d)+mo.*")) {
                ms = (long) (n * 2.628e+9);
            } else if (text.matches("(\\W|\\d)+w.*")) {
                ms = (long) (n * 6.048e+8);
            } else if (text.matches("(\\W|\\d)+d.*")) {
                ms = (long) (n * 8.64e+7);
            } else if (text.matches("(\\W|\\d)+h.*")) {
                ms = (long) (n * 3.6e+6);
            } else if (text.matches("(\\W|\\d)+m.*")) {
                ms = (long) n * 60000;
            } else if (text.matches("(\\W|\\d)+s.*")) {
                ms = (long) n * 1000;
            }

            return System.currentTimeMillis() - ms;
        }

        public static double convertToNumber(String text) {
            if (text.isEmpty()) return -1;

            text = text
                    .trim()
                    .replace(" ", "")
                    .replaceAll("Vol\\w*\\.\\d+", "")
                    .replaceAll("[,-]", ".")
                    .replaceAll("[^\\d.]", "");

            boolean dotted = false;
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (c == '.') {
                    if (!dotted) {
                        dotted = true;
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            }
            double d = -1;
            try {
                d = Double.parseDouble(sb.toString());
            } catch (NumberFormatException e) {
                System.err.println("Double Parse: " + e.getMessage());
            }
            return d;
        }

        public static JSONObject getJSON(URL url, int timeout) throws IOException, JSONException {
            return getJSON(url, timeout, "https://google.com/");
        }

        public static JSONObject getJSON(URL url, int timeout, String referer) throws IOException, JSONException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeout);
            conn.setRequestMethod("GET");
            if (referer != null && !referer.isEmpty()) {
                conn.addRequestProperty("Referer", referer);
            }
            conn.addRequestProperty("User-Agent", MangaScraper.USER_AGENT);
            conn.setRequestProperty("Content-Type", "application/json");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder sb = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                }
                try {
                    return new JSONObject(sb.toString());
                } catch (JSONException e) {
                    return new JSONArray(sb.toString()).getJSONObject(0);
                }
            } else {
                throw new IOException("Got response code: " + responseCode);
            }
        }

        public static void saveAsHTML(Context context, List<String> images, String filename) throws IOException {
            String html = toHTML(images, true, 500, 500);

            OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(filename, MODE_PRIVATE));
            out.write(html);
            out.close();
            Log.i(TAG, "readQueryFile: html file created");
        }

        public static String toHTML(List<String> images, boolean lazy, int width, int height) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title></title>\n");
            sb.append("<script>window.lazyLoadOptions={};</script>\n");
            sb.append("<script async src=\"https://cdn.jsdelivr.net/npm/vanilla-lazyload@17.3.0/dist/lazyload.min.js\"></script>\n");
            sb.append("</head>\n");
            sb.append("<body style=\"padding: 0; margin: 0;\">\n");
            sb.append("<noscript>JavaScript is disabled so unable to load images properly</noscript>");
            for (String image : images) {
                if (lazy) {
                    sb.append(String.format(Locale.UK,
                            "\t<img style=\"%s\" src=\"https://via.placeholder.com/%dx%d/89ff89/000000?text=Loading...\" class=\"lazy\" alt=\"uwu\" data-src=\"%s\"/>%n",
                            "display: block; max-width: 100%; margin-left: auto; margin-right: auto;",
                            width,
                            height,
                            image));
                } else {
                    sb.append(String.format(Locale.UK,
                            "\t<img style=\"%s\" src=\"%s\" loading=\"lazy\" alt=\"uwu\"/>%n",
                            "display: block; max-width: 100vw; text-align: center; padding: 0;",
                            image));
                }
            }
            sb.append("</body>\n");
            sb.append("</html>");
            return sb.toString();
        }

        public static JSONObject readQueryFile(Context context) throws IOException, JSONException {
            File file = new File(context.getExternalFilesDir(null) + "/queries.json");
            String jsonString;
            if (file.exists()) {
                StringBuilder sb = new StringBuilder();
                Scanner sc = new Scanner(file);
                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine());
                }
                jsonString = sb.toString();
            } else {
                jsonString = resetQueries(context);
            }

            return new JSONObject(jsonString);
        }

        public static String resetQueries(Context context) throws IOException {
            StringBuilder sb = new StringBuilder();
            Scanner sc = new Scanner(context.getAssets().open("queries.json"));
            while (sc.hasNextLine()) sb.append(sc.nextLine());

            String jsonString;
            try {
                jsonString = new JSONObject(sb.toString()).toString();
            } catch (JSONException e) {
                e.printStackTrace();
                jsonString = sb.toString();
            }
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(context.getExternalFilesDir(null), "queries.json")));
            out.write(jsonString);
            out.close();
            Log.i(TAG, "resetQueries: Query file created");
            return jsonString;
        }

        public static String toString(long updated) {
            if (updated <= 0) return "";
            SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy k:mm a", Locale.US);
            return dtf.format(new Date(updated));
        }
    }

    public static class RateLimit {
        private final long perMS;
        private final int amount;
        private long start;
        private int count;

        public RateLimit(int amount, long perMS) {
            this.amount = amount;
            this.perMS = perMS;
            this.start = System.currentTimeMillis();
        }

        public synchronized void call() {
            long diff = System.currentTimeMillis() - start;
            if (diff < perMS) {
                if (++count == amount) {
                    try {
                        wait(perMS - diff);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                start = System.currentTimeMillis();
                count = 0;
            }
        }
    }
}
