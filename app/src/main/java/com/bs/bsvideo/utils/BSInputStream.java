package com.bs.bsvideo.utils;

import com.bs.bsvideo.models.VideoItem;

import java.io.IOException;
import java.io.InputStream;

public class BSInputStream extends InputStream {

    private final long threshold = 2048;

    private final InputStream in;
    private final VideoItem videoItem;

    private long bytesRead = 0;
    private long nextThreshold;

    public BSInputStream(InputStream in, VideoItem videoItem) {
        this.in = in;
        this.videoItem = videoItem;
        this.nextThreshold = threshold;
    }

    @Override
    public int read() throws IOException {
        int data = in.read();
        if (data != -1) {
            incrementCounter(1);
        }
        return data;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = in.read(b, off, len);
        if (count > 0) {
            incrementCounter(count);
        } else {
            incrementCounter(videoItem.getFileSize());
        }
        return count;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    private void incrementCounter(long count) {
        bytesRead += count;
        if (bytesRead >= nextThreshold) {
            videoItem.getEncryptionCallback().accept(bytesRead);
            nextThreshold = ((bytesRead / threshold) + 1) * threshold;
        }
    }

}
