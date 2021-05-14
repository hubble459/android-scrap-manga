package nl.hubble.scrapmanga.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadTask extends Thread implements Runnable {
    private final String fileIn;
    private final String fileOut;
    private final DownloadListener listener;
    private boolean cancelled;

    public DownloadTask(String fileIn, String fileOut, DownloadListener listener) {
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
                if (cancelled) {
                    input.close();
                    output.close();
                    listener.cancelled();
                    return;
                }
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            listener.onError(e);
            return;
        }
        listener.onFinish();
    }

    public void cancel() {
        this.cancelled = true;
    }

    public interface DownloadListener {
        void onFinish();
        void onError(Exception e);
        void cancelled();
    }
}

