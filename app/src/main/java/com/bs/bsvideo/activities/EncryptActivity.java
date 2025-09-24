package com.bs.bsvideo.activities;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bs.bsvideo.R;
import com.bs.bsvideo.models.DecisionItem;
import com.bs.bsvideo.models.VideoItem;
import com.bs.bsvideo.services.CryptoService;
import com.bs.bsvideo.utils.CallbackList;
import com.bs.bsvideo.utils.FileSizeUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EncryptActivity extends AppCompatActivity {

    CallbackList<VideoItem> fileList = new CallbackList<>();

    CallbackList.Callback<VideoItem> listCallback = new CallbackList.Callback<VideoItem>() {

        public void change() {
            ListView fileListView = findViewById(R.id.fileList);
            BaseAdapter baseAdapter = (BaseAdapter) fileListView.getAdapter();
            baseAdapter.notifyDataSetChanged();
            FloatingActionButton addButton = findViewById(R.id.addButton);
            FloatingActionButton startButton = findViewById(R.id.startButton);
            if (fileList.isEmpty()) {
                addButton.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.INVISIBLE);
            } else {
                addButton.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAdd(VideoItem videoItem) {
            change();
        }

        @Override
        public void onRemove(VideoItem videoItem) {
            change();
        }
    };

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CryptoService.LocalBinder binder = (CryptoService.LocalBinder) service;
            CryptoService taskService = binder.getService();
            taskService.runEncryption(fileList);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt);

        fileList.addCallback(listCallback);

        setAddButton(R.id.addButton);
        setStartButton(R.id.startButton);
        setGoBackButton(R.id.goBackButton);
        setFileList(R.id.fileList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        fileList.removeCallback(listCallback);
    }

    private void setAddButton(int addButtonId) {
        FloatingActionButton addButton = findViewById(addButtonId);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, 101);
            }
        });
    }

    private void setStartButton(int startButtonId) {
        FloatingActionButton startButton = findViewById(startButtonId);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton goBackButton = findViewById(R.id.goBackButton);
                FloatingActionButton addButton = findViewById(R.id.addButton);
                FloatingActionButton startButton = findViewById(R.id.startButton);

                goBackButton.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.INVISIBLE);

                Intent intent = new Intent(EncryptActivity.this, CryptoService.class);
                bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        });
    }

    private void setGoBackButton(int goBackButtonId) {
        FloatingActionButton goBackButton = findViewById(goBackButtonId);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setFileList(int fileListId) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ListView fileListView = findViewById(fileListId);
        if (fileListView != null) {
            fileListView.setEmptyView(findViewById(R.id.emptyView));
            fileListView.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return fileList.size();
                }

                @Override
                public Object getItem(int i) {
                    return fileList.get(i);
                }

                @Override
                public long getItemId(int i) {
                    return i;
                }

                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    VideoItem videoItem = fileList.get(i);

                    View videoItemLayout = layoutInflater.inflate(R.layout.videoitem_layout, null);
                    ImageView thumbnail = videoItemLayout.findViewById(R.id.thumbnail);
                    try {
                        thumbnail.setImageBitmap(getThumbnail(videoItem.getContentUri()));
                    } catch (Exception ignored) {

                    }

                    TextView filename = videoItemLayout.findViewById(R.id.filename);
                    filename.setText(videoItem.getFileName());

                    TextView subTitle = videoItemLayout.findViewById(R.id.description);
                    subTitle.setText(FileSizeUtil.getSize(videoItem.getFileSize()));

                    ImageView removeItemButton = videoItemLayout.findViewById(R.id.removeItem);
                    removeItemButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fileList.remove(i);
                        }
                    });

                    return videoItemLayout;
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            List<Uri> uriList = getUriList(data);
            for (int i = 0; i < uriList.size(); i++) {
                Uri videoUri = uriList.get(i);
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

                        ListView fileListView = findViewById(R.id.fileList);

                        int index = i;
                        fileList.add(VideoItem
                                .builder()
                                .fileName(fileName)
                                .fileSize(fileSize)
                                .contentUri(videoUri)
                                .encryptionCallback((bytesRead) -> {
                                    ProgressBar progressBar = fileListView.getChildAt(index).findViewById(R.id.progressBar);
                                    progressBar.setProgress((int) ((bytesRead * 100) / fileSize));
                                })
                                .build()
                        );

                    }
                }
            }
        }
    }

    private List<Uri> getUriList(Intent data) {
        ClipData clipData = data.getClipData();
        if (clipData == null) {
            Uri videoUri = data.getData();
            if (videoUri == null) {
                return List.of();
            }
            return List.of(videoUri);
        }
        List<Uri> uriList = new ArrayList<>();
        for (int i = 0; i < clipData.getItemCount(); i++) {
            Uri videoUri = clipData.getItemAt(i).getUri();
            uriList.add(videoUri);
        }
        return uriList;
    }

    private Bitmap getThumbnail(Uri uri) throws IOException {
        Bitmap thumbnail = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            thumbnail = getContentResolver().loadThumbnail(uri, new Size(200, 200), null);
        } else {
            long id = ContentUris.parseId(uri);
            if (uri.toString().contains("video")) {
                thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                        getContentResolver(),
                        id,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null
                );
            }
        }
        return thumbnail;
    }
}