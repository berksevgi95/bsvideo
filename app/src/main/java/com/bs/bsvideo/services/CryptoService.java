package com.bs.bsvideo.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.bs.bsvideo.models.VideoItem;
import com.bs.bsvideo.utils.BSInputStream;
import com.bs.bsvideo.utils.CryptoUtils;

import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.crypto.SecretKey;

public class CryptoService extends Service {

    public class LocalBinder extends Binder {

        public CryptoService getService() {
            return CryptoService.this;
        }

    }

    final IBinder binder = new LocalBinder();

    SecretKey secretKey = null;

    @Override
    public IBinder onBind(Intent intent) {
        try {
            secretKey = CryptoUtils.deriveKeyFromPassword(new char[] {'a', 'b', 'c'},"abc".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return binder;
    }

    public void runEncryption(List<VideoItem> videoItemList) {
        Executor executor = Executors.newFixedThreadPool(videoItemList.size());
        executor.execute(() -> {
            try {
                if (secretKey != null) {
                    for (VideoItem videoItem : videoItemList) {

                        InputStream inputStream = new BSInputStream(getContentResolver().openInputStream(videoItem.getContentUri()), videoItem);

                        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".mp4", getCacheDir());
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = inputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }

//                        Movie movie = MovieCreator.build(tempFile.getAbsolutePath());
//                        DefaultMp4Builder builder = new DefaultMp4Builder();
//                        builder.setFragmenter(track -> {
//                            long[] samples = new long[track.getSampleDurations().length];
//                            for (int i = 0; i < samples.length; i++) samples[i] = i + 1;
//                            return samples;
//                        });
//                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
//                            WritableByteChannel channel = Channels.newChannel(fos);
//                            builder.build(movie).writeContainer(channel);
//                        }

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, new Date() + ".bin");
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

                        InputStream is = new BufferedInputStream(new FileInputStream(tempFile));

                        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                        OutputStream os = getContentResolver().openOutputStream(uri);

                        CryptoUtils.encryptFileWithKey(secretKey, is, os);

                        tempFile.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void runDecryption(VideoItem videoItem, Consumer<Uri> outFile) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                if (secretKey != null) {
                    File tempFile = File.createTempFile("temp_", ".mp4", getCacheDir());
                    CryptoUtils.decryptFileWithKey(secretKey, getContentResolver().openInputStream(videoItem.getContentUri()), new FileOutputStream(tempFile));
                    outFile.accept(Uri.fromFile(tempFile));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }




//    public void runDecryption(VideoItem videoItem, Consumer<byte[]> outSink) {
//        Executor executor = Executors.newSingleThreadExecutor();
//        executor.execute(() -> {
//            try {
//                if (secretKey != null) {
//                    CryptoUtils.decryptFileWithKey(secretKey, getContentResolver().openInputStream(videoItem.getContentUri()), new OutputStream() {
//                        @Override
//                        public void write(int i) throws IOException {
//
//                        }
//
//                        @Override
//                        public void write(byte[] b, int off, int len) throws IOException {
//                            outSink.accept(b);
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }

    


}