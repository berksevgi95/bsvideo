package com.bs.bsvideo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // initialize player view from layout
    private PlayerView playerView;
    // initialize player from ExoPlayer
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*"); // only show videos
        startActivityForResult(intent, 101);

        // define player view from layout
        playerView = findViewById(R.id.player_view);
//        initializePlayer();
    }

    // define a function to initialize player
    private void initializePlayer(Uri uri) {
        // initialize player
        player = new ExoPlayer.Builder(this).build();
        // set player to player view
        playerView.setPlayer(player);

        // set first video
//        setVideo(videoUrls.get(currentVideoIndex));

        // add listener to player to handle playback end
//        player.addListener(new Player.Listener() {
//            @Override
//            public void onPlaybackStateChanged(int playbackState) {
//                if (playbackState == Player.STATE_ENDED) {
//                    showCompletionDialog();
//                }
//            }
//        });

        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();


    }

    // define a function to show a dialog box after video completion
    private void showCompletionDialog() {
        // show a dialog box with two options - replay or play next video
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Playback Finished!")
                .setMessage("Want to replay or play next video?")
                .setIcon(R.mipmap.ic_launcher)
                // set two buttons
                // replay button will set video to start from beginning
                .setPositiveButton("Replay", (dialogInterface, i) -> {
                    player.seekTo(0);
                    player.play();
                })
                // next video button will play next video
                .setNegativeButton("Next", (dialogInterface, i) -> {
//                    currentVideoIndex = (currentVideoIndex + 1) % videoUrls.size();
//                    setVideo(videoUrls.get(currentVideoIndex));
                })
                // cancel button will dismiss the dialog
                .create();

        // show the dialog box
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();

            initializePlayer(videoUri);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // release player when app is stopped
        if (player != null) {
            player.release();
        }
    }
}