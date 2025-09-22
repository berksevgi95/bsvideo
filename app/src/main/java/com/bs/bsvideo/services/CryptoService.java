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
import com.bs.bsvideo.utils.CryptoUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.function.Consumer;

import javax.crypto.SecretKey;

public class CryptoService extends Service {

    public class LocalBinder extends Binder {

        public CryptoService getService() {
            return CryptoService.this;
        }

    }

    private final IBinder binder = new LocalBinder();

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
        new Thread(() -> {
            try {
                if (secretKey != null) {
                    for (VideoItem videoItem : videoItemList) {

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "my_file.bin");
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS); // Documents folder

                        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

                        CryptoUtils.encryptFileWithKey(secretKey, getContentResolver().openInputStream(videoItem.getContentUri()), getContentResolver().openOutputStream(uri));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void runDecryption(VideoItem videoItem, Consumer<Uri> outFile) {
        new Thread(() -> {
            try {
                if (secretKey != null) {
                    File tempFile = File.createTempFile("temp_", ".mp4", getCacheDir());
                    CryptoUtils.decryptFileWithKey(secretKey, getContentResolver().openInputStream(videoItem.getContentUri()), new FileOutputStream(tempFile));
                    outFile.accept(Uri.fromFile(tempFile));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}