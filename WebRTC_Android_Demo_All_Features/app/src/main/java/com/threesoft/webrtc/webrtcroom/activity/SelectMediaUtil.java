package com.threesoft.webrtc.webrtcroom.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import static android.app.Activity.RESULT_OK;

public class SelectMediaUtil {

    private final int REQUEST_CODE_VIDEO = 2;
    private final int REQUEST_CODE_IMAGE = 3;

    private String path;
    private Activity activity;

    private SelectType selectType = SelectType.video;

    public enum SelectType {
        audio(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA),
        video(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA),
        image(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA);

        Uri externalContentUri;
        String data;

        SelectType(Uri externalContentUri, String data) {
            this.externalContentUri = externalContentUri;
            this.data = data;
        }
    }

    public void select(Activity activity, SelectType selectType) {
        this.activity = activity;
        this.selectType = selectType;
        Intent i = new Intent(Intent.ACTION_PICK, selectType.externalContentUri);
        if(selectType == SelectType.video){
            activity.startActivityForResult(i, REQUEST_CODE_VIDEO);
        }else if(selectType == SelectType.image){
            activity.startActivityForResult(i, REQUEST_CODE_IMAGE);
        }

    }

    public String onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && null != data) {
            if(requestCode ==REQUEST_CODE_VIDEO || requestCode ==REQUEST_CODE_IMAGE){
                Uri uri = data.getData();
                String[] filePathColumn = {selectType.data};

                Cursor cursor = activity.getContentResolver().query(uri,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                path = cursor.getString(columnIndex);
                cursor.close();
            }

        }

        return path;
    }

}

