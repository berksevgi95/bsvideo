package com.bs.bsvideo.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.OpenableColumns;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bs.bsvideo.R;
import com.bs.bsvideo.models.VideoItem;
import com.bs.bsvideo.services.CryptoService;
import com.bs.bsvideo.utils.CryptoUtils;

import java.io.File;
import java.util.function.Consumer;

public class DecryptActivity extends AppCompatActivity {

    VideoItem videoItem = null;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CryptoService.LocalBinder binder = (CryptoService.LocalBinder) service;
            CryptoService taskService = binder.getService();
            taskService.runDecryption(videoItem, new Consumer<>() {
                @Override
                public void accept(Uri uri) {
                    playVideo(uri);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt);

        ExoPlayer player = new ExoPlayer.Builder(this).build();
        PlayerView playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();

            try (Cursor cursor = this.getContentResolver().query(
                    videoUri,
                    new String[]{
                            OpenableColumns.DISPLAY_NAME,
                            OpenableColumns.SIZE
                    },
                    null,
                    null,
                    null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(nameIndex);

                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    long fileSize = cursor.getLong(sizeIndex);

                    videoItem = VideoItem
                            .builder()
                            .fileName(fileName)
                            .fileSize(fileSize)
                            .contentUri(videoUri)
                            .build();
                    Intent intent = new Intent(DecryptActivity.this, CryptoService.class);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                }
            } catch (Exception e) {

            }
        }
    }

    public void playVideo(Uri uri) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PlayerView playerView = findViewById(R.id.player_view);
                Player player = playerView.getPlayer();

                MediaItem mediaItem = MediaItem.fromUri(uri);
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlayerView playerView = findViewById(R.id.player_view);
        Player player = playerView.getPlayer();
        player.release();
    }
}