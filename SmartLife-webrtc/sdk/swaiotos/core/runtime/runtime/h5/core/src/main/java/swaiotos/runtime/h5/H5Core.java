package swaiotos.runtime.h5;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;

import java.util.Map;

/**
 * @ClassName: H5Core
 * @Author: lu
 * @CreateDate: 2020/10/25 4:28 PM
 * @Description:
 */
public interface H5Core {
    View create(Context context, Map<String, H5CoreExt> extension);

    void load(String url);

    String curUrl();

    void setExtJS(String js);

    boolean onBackPressed();

    void destroy();

    void onLeftBtnClick();

    void onShareBtnClick();

    void setBackgroundColor(int color);
    void evaluateJavascript(String data);
    void setH5Style(H5Style style);


    void onResume();
    void onPause();
    void onStop();

    WebView getWebView();
}
