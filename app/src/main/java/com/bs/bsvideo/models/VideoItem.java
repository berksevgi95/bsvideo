package com.bs.bsvideo.models;

import android.net.Uri;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VideoItem {

    String fileName;
    long fileSize;
    Uri contentUri;
    Consumer<Long> encryptionCallback;

}
