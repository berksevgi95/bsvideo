package com.bs.bsvideo.utils;

import android.net.Uri;

import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.BaseDataSource;
import androidx.media3.datasource.DataSpec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@UnstableApi
public class ChunkedDataSource extends BaseDataSource {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private int readOffset = 0;

    public ChunkedDataSource() {
        super(false);
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        return C.LENGTH_UNSET;
    }

    @Override
    public int read(byte[] target, int offset, int length) throws IOException {
        synchronized (buffer) {
            byte[] data = buffer.toByteArray();
            if (readOffset >= data.length) {
                return C.RESULT_NOTHING_READ;
            }
            int bytesToRead = Math.min(length, data.length - readOffset);
            System.arraycopy(data, readOffset, target, offset, bytesToRead);
            readOffset += bytesToRead;
            return bytesToRead;
        }
    }

    @Override
    public Uri getUri() {
        return null;
    }

    @Override
    public void close() throws IOException {
        buffer.reset();
        readOffset = 0;
    }

    public void pushChunk(byte[] chunk) {
        synchronized (buffer) {
            try {
                buffer.write(chunk);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}