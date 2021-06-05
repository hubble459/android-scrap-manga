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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;

import static android.content.Context.MODE_PRIVATE;

public class Utils {
    enum DateFormat {
        MANGAFAST("yy-MM-dd"),
        MANGADEX("yy-MM-dd k:m:s"),
        MANGAKAKALOT_1("MMM-dd-yy k:m"),
        MANGAKAKALOT_2("MMM-dd-yy"),
        TOPMANHUA("MM/dd/yy"),
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
            text = text.trim();
            for (DateFormat format : DateFormat.values()) {
                try {
                    Date date = new SimpleDateFormat(format.toString(), Locale.getDefault()).parse(text);
                    if (date == null) continue;
                    return date.getTime();
                } catch (ParseException ignored) {
                }
            }

            if (text.matches(".*(now|hot|new).*")) {
                return System.currentTimeMillis();
            }

            Pattern r = Pattern.compile("(\\d+)");
            Matcher m = r.matcher(text);
            if (m.find()) {
                String number = m.group(0);
                if (number != null) {
                    long ms = 0;
                    int n = Integer.parseInt(number);
                    if (text.matches("(\\W|\\d)+y.*")) {
                        ms = (long) (n * 3.154e10);
                    } else if (text.matches("(\\W|\\d)+mo.*")) {
                        ms = (long) (n * 2.628e9);
                    } else if (text.matches("(\\W|\\d)+w.*")) {
                        ms = (long) (n * 6.048e8);
                    } else if (text.matches("(\\W|\\d)+d.*")) {
                        ms = (long) (n * 8.64e7);
                    } else if (text.matches("(\\W|\\d)+h.*")) {
                        ms = (long) (n * 3.6e6);
                    } else if (text.matches("(\\W|\\d)+m.*")) {
                        ms = (long) n * 60000;
                    } else if (text.matches("(\\W|\\d)+s.*")) {
                        ms = (long) n * 1000;
                    }
                    if (ms != 0) {
                        return System.currentTimeMillis() - ms;
                    } else {
                        return 0;
                    }
                }
            }
            return 0;
        }

        public static double convertToNumber(String text) {
            if (text.isEmpty()) return -1;

            Pattern r = Pattern.compile("Ch(ap(ter)?)? ?(\\d+(\\.\\d+)?)");
            Matcher m = r.matcher(text);
            if (m.find()) {
                String number = m.group(3);
                if (number != null && number.matches("[\\d.]+")) {
                    return Double.parseDouble(number);
                }
            }

            text = text
                    .trim()
                    .replace(" ", "")
                    .replaceAll("Vol\\w*\\.?[\\d.]+", "")
                    .replaceAll("[,-]", ".")
                    .replaceAll("[^\\d.]", "");

            boolean dotted = false;
            boolean canDot = false;
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (c == '.') {
                    if (canDot && !dotted) {
                        dotted = true;
                        sb.append(c);
                    }
                } else {
                    canDot = true;
                    sb.append(c);
                }
            }
            double d = -1;
            try {
                d = Double.parseDouble(sb.toString());
            } catch (NumberFormatException e) {
                Log.i(TAG, "convertToNumber: Double Parse: " + e.getMessage());
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

        public static String toNormalCase(String sentence) {
            if (sentence == null || sentence.isEmpty()) return sentence;
            sentence = sentence.toLowerCase().trim();
            String[] words = sentence.split(" ");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (word.length() >= 1) {
                    char firstLetter = word.charAt(0);
                    if (i == 0 || needsUpperCase(word)) {
                        firstLetter = Character.toUpperCase(firstLetter);
                    }

                    String newWord = firstLetter + word.substring(1);
                    sb.append(newWord).append(" ");
                }
            }
            return sb.toString().trim();
        }

        private static boolean needsUpperCase(String word) {
            switch (word) {
                case "ni":
                case "o":
                case "wo":
                case "no":
                case "to":
                case "node":
                case "suru":
                case "ha":
                case "wa":
                case "ga":
                case "ka":
                case "the":
                case "de":
                case "mo":
                case "aru":
                case "ore":
                case "yori":
                case "of":
                case "deshita":
                case "kara":
                case "tte":
                    return false;
                default:
                    return true;
            }
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
            return jsonString;
        }

        public static String toTimeString(long updated) {
            if (updated <= 0) return "?";
            updated = (long) (Math.floor(updated / 1000D) * 1000);
            long diff = System.currentTimeMillis() - updated - 5000;
            if (diff <= 60000 /* 1 minute */) {
                return "Just now";
            } else if (diff <= 3.6e6 /* 1 hour */) {
                for (long i = 2000, j = 2; i <= 3.6e6; i += 1000, j++) {
                    if (diff <= i) {
                        return (int) (j / 60) + " minutes ago";
                    }
                }
            } else if (diff <= 8.64e7 /* 24 hours */) {
                for (long i = 7200000, j = 2; i <= 8.64e7; i += 3.6e6, j++) {
                    if (diff <= i) {
                        return j + " hours ago";
                    }
                }
            } else if (diff <= 6.048e+8 /* 1 week */) {
                for (long i = 172800000, j = 2; i <= 6.048e+8; i += 8.64e7, j++) {
                    if (diff <= i) {
                        return j + " days ago";
                    }
                }
            } else {
                SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy K:mm a", Locale.US);
                return dtf.format(new Date(updated));
            }
            return "?";
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

    public static class DifferenceCalculator {
        public static long avgDifference(List<Chapter> chapters) {
            if (chapters == null || chapters.size() != 1 && chapters.size() < 3) {
                return 0;
            } else {
                long total = 0;
                int size = chapters.size() - 1;
                for (int i = 0; i < size; i++) {
                    total += chapters.get(i).getPosted() - chapters.get(i + 1).getPosted();
                }
                return total / size;
            }
        }

        public static String prettyInterval(long average) {
            String[] numbers = new String[]{"two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};

            if (average <= 0) {
                return "Unknown";
            } else if (average <= 3.6e6) {
                return "Hourly";
            } else if (average <= 8.64e7) {
                return "Daily";
            } else if (average <= 1.728e8) {
                return "Every other day";
            } else if (average <= 5.184e8) {
                for (long i = 259200000L, j = 1; i <= 518400000L; i += 86400000L, j++) {
                    if (average <= i) {
                        return "Once every " + numbers[(int) j] + " days";
                    }
                }
            } else if (average <= 6.048e8) {
                return "Every week";
            } else {
                for (long i = 1209600000, j = 0; ; i += 6.048e8, j++) {
                    if (average <= i) {
                        String number;
                        if (j < numbers.length) {
                            number = numbers[(int) j];
                        } else {
                            number = String.valueOf(j + 1);
                        }
                        return "Every " + number + " months";
                    }
                }
            }

            return "Unknown";
        }

        public static String handle(Manga manga) {
            return prettyInterval(avgDifference(manga.getChapters()));
        }
    }
}
