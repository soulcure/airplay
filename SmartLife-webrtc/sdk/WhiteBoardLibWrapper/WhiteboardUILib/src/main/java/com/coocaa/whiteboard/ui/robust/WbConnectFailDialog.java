package com.coocaa.whiteboard.ui.robust;

import android.app.Dialog;
import android.content.Context;

import com.coocaa.whiteboard.ui.R;

/**
 * 连接断开页面
 */
public class WbConnectFailDialog extends Dialog {


    public WbConnectFailDialog(Context context) {
        super(context, R.style.base_dialog);
        setContentView(R.layout.whiteboard_connect_fail_layout);
    }
}
