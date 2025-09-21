package com.bs.bsvideo.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum DecisionItem {

    ENCRYPT("icon", "Encrypt", "Encrypt video files securily"),
    DECRYPT("icon", "Decrypt", "Decrypt encrypted video files");

    String icon;
    String title;
    String subTitle;

}
