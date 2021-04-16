package swaiotos.channel.iot.tv.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import swaiotos.channel.iot.tv.R;

/**
 * @ProjectName: panel_update
 * @Package: swaiotos.panel_update.view
 * @ClassName: SystemUpdateDialog
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/30 17:58
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/30 17:58
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class DialogTools {

    /**
     *
     * 升级（下载）对话框
     *
     * */
    public static Dialog showDialogByXml(Context context) {

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_waiting, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0+
            dialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }else {
            dialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        int width = (int)context.getResources().getDimension(R.dimen.px_60);
        int height = (int)context.getResources().getDimension(R.dimen.px_60);

        params.height = height;
        params.width = width;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        window.setContentView(view);

        return dialog;
    }
}
