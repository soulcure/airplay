package swaiotos.runtime.h5.core.os.webview;

import android.webkit.WebViewClient;

public class LoadingStateWebViewClient extends WebViewClient {
    protected boolean isLoadOk = false;

    public boolean isLoadOk() {
        return isLoadOk;
    }

    public void resetLoadState(){
        
    }
}
