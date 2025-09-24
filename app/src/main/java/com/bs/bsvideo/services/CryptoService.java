package com.bs.bsvideo.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;

import com.bs.bsvideo.models.VideoItem;
import com.bs.bsvideo.utils.BSInputStream;
import com.bs.bsvideo.utils.CryptoUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Date;
import java.util.List;
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

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, new Date() + ".bin");
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

                        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                        InputStream is = new BSInputStream(getContentResolver().openInputStream(videoItem.getContentUri()), videoItem);
                        OutputStream os = getContentResolver().openOutputStream(uri);

                        CryptoUtils.encryptFileWithKey(secretKey, is, os);
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

}