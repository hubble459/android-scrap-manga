package nl.hubble.scrapmanga.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import nl.hubble.scraper.model.Chapter;

public class FileUtil {
    private static boolean cancelled;

    public static synchronized void cancelDownloadChapter() {
        cancelled = true;
    }

    public static synchronized void downloadChapter(Context context, String path, Chapter chapter, DownloadListener downloadListener) {
        try {
            FileUtil.mkdir(path);
            URL url = new URL(chapter.getHref());

            new LoadChapter(context, url, new LoadChapter.OnFinishedListener() {
                @Override
                public synchronized void finished(List<String> images) {
                    Object lock = new Object();
                    for (int i = 0; i < images.size(); i++) {
                        try {
                            URL image = new URL(images.get(i));

                            String extension = image.getFile().substring(image.getFile().lastIndexOf("."));
                            new Web(image, path + File.separator + i + extension, url.getHost(), new DownloadListener() {
                                @Override
                                public void onFinish() {
                                    synchronized (lock) {
                                        lock.notify();
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    downloadListener.onError(e);
                                }

                                @Override
                                public void progress(String file, int percent) {

                                }

                                @Override
                                public void cancelled() {

                                }
                            }).start();

                            synchronized (lock) {
                                lock.wait();
                            }

                            if (cancelled) {
                                break;
                            }

                            downloadListener.progress(image.getFile(), images.size() / 100 * i);
                        } catch (MalformedURLException | InterruptedException e) {
                            downloadListener.onError(e);
                        }
                    }
                    downloadListener.onFinish();
                }

                @Override
                public void error(Exception e) {
                    downloadListener.onError(e);
                }
            });
        } catch (MalformedURLException e) {
            downloadListener.onError(e);
        }
    }

    public static boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean remove(String path) {
        File file = new File(path);
        if (file.exists()) {
            boolean success = true;
            if (file.isDirectory()) {
                for (File listFile : Objects.requireNonNull(file.listFiles())) {
                    if (!listFile.delete() && success) {
                        success = false;
                    }
                }
            }
            return file.delete() && success;
        }
        return false;
    }

    public static boolean mkdir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return false;
    }

    public static void copy(String fileIn, String fileOut, DownloadListener listener) {
        new Copy(fileIn, fileOut, listener).start();
    }

    public interface DownloadListener {
        void onFinish();

        void onError(Exception e);

        void progress(String file, int percent);

        void cancelled();
    }

    public static class DownloadAdapter implements DownloadListener {

        @Override
        public void onFinish() {

        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void progress(String file, int percent) {

        }

        @Override
        public void cancelled() {

        }
    }

    public static class Copy extends Thread implements Runnable {
        private final String fileIn;
        private final String fileOut;
        private final DownloadListener listener;

        public Copy(String fileIn, String fileOut, DownloadListener listener) {
            this.fileIn = fileIn;
            this.fileOut = fileOut;
            this.listener = listener;
        }

        @Override
        public void run() {
            try (InputStream input = new FileInputStream(fileIn); OutputStream output = new FileOutputStream(fileOut)) {
                byte[] data = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                listener.onError(e);
                return;
            }
            listener.onFinish();
        }
    }

    public static class Web extends Thread implements Runnable {
        private final URL url;
        private final String fileOut;
        private final String referer;
        private final DownloadListener listener;

        public Web(URL url, String fileOut, String referer, DownloadListener listener) {
            this.url = url;
            this.fileOut = fileOut;
            this.referer = referer;
            this.listener = listener;
        }

        @Override
        public void run() {
            HttpURLConnection connection1 = null;
            HttpsURLConnection connection2 = null;
            try {
                if (url.getProtocol().equals("http")) {
                    connection1 = (HttpURLConnection) url.openConnection();
                    connection1.addRequestProperty("referer", referer);
                    connection1.connect();
                } else {
                    connection2 = (HttpsURLConnection) url.openConnection();
                    connection2.addRequestProperty("referer", referer);
                    connection2.connect();
                }

                try (InputStream input = (connection1 == null ? connection2 : connection1).getInputStream(); OutputStream output = new FileOutputStream(fileOut)) {
                    byte[] data = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    listener.onError(e);
                    return;
                }
            } catch (IOException e) {
                listener.onError(e);
                return;
            } finally {
                if (connection1 != null) {
                    connection1.disconnect();
                } else if (connection2 != null) {
                    connection2.disconnect();
                }
            }
            listener.onFinish();
        }
    }
}
