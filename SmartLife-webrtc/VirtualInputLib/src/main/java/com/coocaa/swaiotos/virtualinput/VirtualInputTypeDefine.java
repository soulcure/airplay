package com.coocaa.swaiotos.virtualinput;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Author: yuzhan
 */
@Retention(RetentionPolicy.SOURCE)
//@IntDef({TYPE_DEFAULT, TYPE_ONLINE_VIDEO, TYPE_LOCAL_VIDEO, TYPE_PICTURE, TYPE_MUSIC})
public @interface VirtualInputTypeDefine {
    int TYPE_DEFAULT = 0;
    int TYPE_ONLINE_VIDEO = TYPE_DEFAULT+1;
    int TYPE_LOCAL_VIDEO = TYPE_DEFAULT+2;
    int TYPE_PICTURE = TYPE_DEFAULT+3;
    int TYPE_MUSIC = TYPE_DEFAULT+4;
    int TYPE_DOCUMENT = TYPE_DEFAULT+5;
    int TYPE_H5_ATMOSPHERE = TYPE_DEFAULT+6;
    int TYPE_LIVE = TYPE_DEFAULT+7;
    int TYPE_H5_GAME = TYPE_DEFAULT+8;

    String NAME_DEFAULT = "DEFAULT";
    String NAME_VIDEO = "VIDEO";
    String NAME_IMAGE = "IMAGE";
    String NAME_MUSIC = "AUDIO";
    String NAME_DOCUMENT = "DOC";
    String NAME_H5_ATMOSPHERE = "H5_ATMOSPHERE";
    String NAME_LIVE = "LIVE";
    String NAME_H5_GAME = "H5_PAGE_GAME";
}
