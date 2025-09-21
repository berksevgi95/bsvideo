package com.bs.bsvideo.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bs.bsvideo.R;
import com.bs.bsvideo.models.DecisionItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDecisionList(R.id.decisionList);
    }

    public void setDecisionList(Integer decisionListId) {
        List<DecisionItem> decisions = List.of(DecisionItem.ENCRYPT, DecisionItem.DECRYPT);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ListView decisionList = findViewById(decisionListId);
        if (decisionList != null) {
            decisionList.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return decisions.size();
                }

                @Override
                public Object getItem(int i) {
                    return decisions.get(i);
                }

                @Override
                public long getItemId(int i) {
                    return i;
                }

                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    DecisionItem decisionItem = decisions.get(i);

                    View decisionLayout = layoutInflater.inflate(R.layout.decision_layout, null);
                    ImageView icon = decisionLayout.findViewById(R.id.icon);

                    TextView title = decisionLayout.findViewById(R.id.title);
                    title.setText(decisionItem.getTitle());

                    TextView subTitle = decisionLayout.findViewById(R.id.subTitle);
                    subTitle.setText(decisionItem.getSubTitle());

                    return decisionLayout;
                }
            });
            decisionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i == DecisionItem.ENCRYPT.ordinal()) {
                        startActivity(new Intent(MainActivity.this, EncryptActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, DecryptActivity.class));
                    }
                }
            });
        }
    }


//    // initialize player view from layout
//    private PlayerView playerView;
//    // initialize player from ExoPlayer
//    private ExoPlayer player;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("video/*"); // only show videos
//        startActivityForResult(intent, 101);
//
//        // define player view from layout
//        playerView = findViewById(R.id.player_view);
////        initializePlayer();
//    }
//
//    // define a function to initialize player
//    private void initializePlayer(Uri uri) {
//        // initialize player
//        player = new ExoPlayer.Builder(this).build();
//        // set player to player view
//        playerView.setPlayer(player);
//
//        // set first video
////        setVideo(videoUrls.get(currentVideoIndex));
//
//        // add listener to player to handle playback end
////        player.addListener(new Player.Listener() {
////            @Override
////            public void onPlaybackStateChanged(int playbackState) {
////                if (playbackState == Player.STATE_ENDED) {
////                    showCompletionDialog();
////                }
////            }
////        });
//
//        MediaItem mediaItem = MediaItem.fromUri(uri);
//        player.setMediaItem(mediaItem);
//        player.prepare();
//        player.play();
//
//
//    }
//
//    // define a function to show a dialog box after video completion
//    private void showCompletionDialog() {
//        // show a dialog box with two options - replay or play next video
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle("Playback Finished!")
//                .setMessage("Want to replay or play next video?")
//                .setIcon(R.mipmap.ic_launcher)
//                // set two buttons
//                // replay button will set video to start from beginning
//                .setPositiveButton("Replay", (dialogInterface, i) -> {
//                    player.seekTo(0);
//                    player.play();
//                })
//                // next video button will play next video
//                .setNegativeButton("Next", (dialogInterface, i) -> {
////                    currentVideoIndex = (currentVideoIndex + 1) % videoUrls.size();
////                    setVideo(videoUrls.get(currentVideoIndex));
//                })
//                // cancel button will dismiss the dialog
//                .create();
//
//        // show the dialog box
//        dialog.show();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
//            Uri videoUri = data.getData();
//
//            initializePlayer(videoUri);
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        // release player when app is stopped
//        if (player != null) {
//            player.release();
//        }
//    }
}