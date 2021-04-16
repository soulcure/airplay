package com.coocaa.tvpi.module.share.intent;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.coocaa.tvpi.module.local.document.DocumentConfig;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.document.FormatEnum;
import com.coocaa.tvpi.module.web.SmartBrowserSearchActivity;

/**
 * @Author: yuzhan
 */
public class TextIntent {

    private final static String TAG = "IntentActivity";

    public static boolean handleTextIntent(Context context, Intent intent) {
        ClipData clipData = intent.getClipData();
        if(clipData != null) {
            int size = clipData.getItemCount();
            for(int i=0; i<size; i++) {
                Log.d(TAG, "handleTextIntent, clipData=" + clipData.getItemAt(i));
            }
        }
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        Log.d(TAG, "handleTextIntent, EXTRA_TEXT=" + text);
        if(isHtml(text)) {
            Log.d(TAG, "this is html intent.");
            SmartBrowserSearchActivity.start(context, text);
            return true;
        }
//        if (isSupportFile(filePath)) {
//            Intent intent = new Intent(context, DocumentPlayerActivity.class);
//            intent.putExtra(DocumentUtil.KEY_FILE_PATH, filePath);
//            intent.putExtra(DocumentUtil.KEY_SOURCE_APP, getSourceAppName(filePath));
//            intent.putExtra(DocumentUtil.KEY_SOURCE_PAGE, DocumentUtil.SOURCE_PAGE_OTHER_APP);
//            if (!(context instanceof Activity)) {
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            }
//            return intent;
//        }
        return false;
    }

    private static boolean isHtml(String text) {
        return text != null && (text.toLowerCase().startsWith("http://") || text.toLowerCase().startsWith("https://"));
    }

    //支持的文件类型
    private static boolean isSupportFile(String filePath) {
        String suffix = DocumentUtil.getFileType(filePath);
        return DocumentConfig.SUPPORT_FORMATS.contains(FormatEnum.getFormat(suffix));
    }

    private static String getSourceAppName(String filePath) {
        String appName = "unknown";
        if (filePath.contains("com.tencent.mm") || filePath.contains("MicroMsg")) {
            appName = "微信";
        } else if (filePath.contains("com.tencent.mobileqq")) {
            appName = "QQ";
        } else if (filePath.contains("WeixinWork")) {
            appName = "企业微信";
        } else if (filePath.contains("DingTalk")) {
            appName = "钉钉";
        }
        return appName;
    }
}
