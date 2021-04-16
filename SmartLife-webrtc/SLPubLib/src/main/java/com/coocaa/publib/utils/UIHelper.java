package com.coocaa.publib.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;

import androidx.core.app.ActivityCompat;

import com.coocaa.publib.R;
import com.coocaa.publib.common.Constants;
import com.coocaa.publib.views.SDialog;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

/**
 * 类描述：<br>
 * UI操作工具类
 *
 * @author gmf
 * @version v1.0
 * @date 2015年9月16日
 */
public class UIHelper {

    private static final String TAG = "UIHelper";

    /**
     * 方法说明：<br>
     * 打开apk文件(进行安装apk程序)
     *
     * @param context
     * @param filePath 文件路径
     * @return
     */
    public static boolean openApkFile(Context context, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.isFile() || !file.exists()) {
                return false;
            }
            Uri uri = Uri.fromFile(new File(filePath)); // 获取文件的Uri
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * 方法说明：<br>
     * 打开浏览器
     *
     * @param context
     * @param url
     */
    public static void openBrowesr(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri content_uri_browsers = Uri.parse(url);
            intent.setData(content_uri_browsers);
            // intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }

    /**
     * 方法说明：<br>
     * 提示信息  电视派中尽量使用toastutils，因为区分背景色的设计要求
     *
     * @param context
     * @param message
     */
    public static void showMessage(Context context, String message) {
        if (context != null) {
            try {
                // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                ToastUtils.getInstance().showGlobalShort(message);
            } catch (Exception e) {
//                IRLog.e(TAG, e.getMessage());
            }
        }
    }

    // 电视派中尽量使用toastutils，因为区分背景色的设计要求
    public static void showMessage(Context context, int resourceId) {
        if (context != null) {
            try {
                // Toast.makeText(context, context.getString(resourceId),
                // Toast.LENGTH_SHORT).show();
                ToastUtils.getInstance().showGlobalShort(context.getString(resourceId));
            } catch (Exception e) {
                IRLog.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * 方法说明：<br>
     * 跳转到目标界面
     *
     * @param context        当前界面
     * @param targetActivity 目标界面
     * @param data           传递数据
     * @param finish         如果为true，关闭当前界面。否则不关闭
     */
    public static void forwardTargetActivity(Context context, Class<?> targetActivity, Bundle data, boolean finish) {
        try {
            Intent intent = new Intent();
            intent.setClass(context, targetActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            if (data != null) {
                intent.putExtras(data);
            }
            context.startActivity(intent);
            if (finish && context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            IRLog.e(TAG, e.getMessage());
        }
    }

    /**
     * 方法说明：<br>
     * 跳转到目标界面
     *
     * @param context        当前界面
     * @param targetActivity 目标界面
     * @param data           传递数据
     *                       如果为true，关闭当前界面。否则不关闭
     * @param requestCode    参数代码
     */
    public static void forwardTargetActivityForResult(Context context, Class<?> targetActivity, Bundle data,
                                                      int requestCode) {
        try {
            Intent intent = new Intent();
            intent.setClass(context, targetActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            if (data != null) {
                intent.putExtras(data);
            }

            ((Activity) context).startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            IRLog.e(TAG, e.getMessage());
        }
    }


    /**
     * 根据链接地址，找到对应的activity配置，并打开。
     *
     * @param context
     * @param url
     */
    public static void startActivityByURL(Context context, String url) {
        url = url.replace("app.tvpi.com", "app.smart_screen.com");
        IRLog.d(TAG, "startActivityByURL,url:" + url);
        try {
            String strUrlParam = StringUtils.truncateUrlPage(url);
            url = url.substring(0, url.indexOf("?") != -1 ? url.indexOf("?") : url.length());
            //浏览器打开
            if (Constants.OPENBROWESR_ROUTERS.equals(url)) {
                //大于1
                if (strUrlParam.split("[=]").length > 1) {
                    String str = new String(strUrlParam.split("[=]")[1].getBytes(), "UTF-8");
                    str = URLDecoder.decode(str, "UTF-8");
                    openBrowesr(context, str);
                }
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //判断？后面的参数
            if (strUrlParam != null) {
                for (String strSplit : strUrlParam.split("[&]")) {
                    String[] arrSplitEqual = null;
                    arrSplitEqual = strSplit.split("[=]");
                    // 解析出键值
                    if (arrSplitEqual.length > 1) {
                        // 正确解析
                        String str = new String(arrSplitEqual[1].getBytes(), "UTF-8");
                        str = URLDecoder.decode(str, "UTF-8");

                        //设置参数
                        intent.putExtra(arrSplitEqual[0], str);
                        IRLog.d(TAG, "startActivityByUrl,key:" + arrSplitEqual[0] + ",value:" + str);
                    }
                }
            }

            try {
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.s_msg_intent_failed);
                builder.setPositiveButton(R.string.OK, null);
                builder.show();*/
                // 在前台时，使用applicationContext调用AlertDialog会导致调用失败，换成toast
                ToastUtils.getInstance().showGlobalShort(R.string.s_msg_intent_failed);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 方法说明：<br>
     * 获取手机IMEI
     *
     * @param context
     * @return
     */
    public static String getPhoneIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        return telephonyManager.getDeviceId();
    }

    /**
     * 方法说明：<br>
     * 获取当前软件版本号
     *
     * @param ctx
     * @return
     */
    public static int getVersionCode(Context ctx) {
        int version = 0;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
            version = packInfo.versionCode;
        } catch (Exception e) {
        }
        return version;
    }

    /**
     * 弹出提示框
     *
     * @param titleText 标题显示的String id值
     * @param type      类型，1为登录提示
     */
    public static void showMyDialog(final Context context, String titleText, final int type) {
        //去登录
        if (type == Constants.MYDIALOG_TYPE_LOGIN) {
            showLogin(context);
            return;
        }
        SDialog dialog = new SDialog(context, context.getResources().getString(R.string.str_learn_know2), null, new SDialog.SDialogListener() {
            @Override
            public void onOK() {

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 是否登陆的判断
     * @param
     * @return
     */
    public static boolean isLogin() {
        //对接酷开账号 只需判断token是否存在
//        return !TextUtils.isEmpty(UserInfoCenter.getInstance().getAccessToken());

        String tokenStr = SharedData.getInstance().getString(SharedData.Keys.COOCAA_TOKEN, "");
        return !TextUtils.isEmpty(tokenStr);
    }

    /**
     * 跳转到登录页面
     *
     * @param context
     */
    public static void showLogin(final Context context) {
        SDialog dialog = new SDialog(context, context.getResources().getString(R.string.user_token_failure),
                context.getResources().getString(R.string.to_login), new SDialog.SDialogListener() {
            @Override
            public void onOK() {
                // 跳转到登录页面
                toLogin(context);
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    /**
     * 跳转到登录页面
     */
    public static void toLogin(Context context) {
        // ToDo
//        Intent intent = new Intent(context, LoginActivity.class);
//        context.startActivity(intent);
    }

    /**
     * 判断是否已经安装了应用包
     *
     * @param context
     * @param packageName
     * @return
     */
    public static int isInstallByread(Context context, String packageName) {
        // 判断是否安装
        // if (new File("/data/data/" + packageName).exists()) {
        // 获取系统中安装的所有应用包名集合
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            // 找出指定的应用
            if (packageName.equals(packageInfo.packageName)) {
                return Constants.INSTALLED;
            }
        }
        // }
        return Constants.NOTINSTALL;
    }

    /**
     * @param context
     * @param name    数据库的dat名称
     * @param sod     需要保存的对象
     * @return void
     * @function 将一个对象保存到本地
     * @author 高明峰
     */
    public static void saveObject(Context context, String name, Object sod) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(name, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(sod);
        } catch (Exception e) {
            e.printStackTrace();
            // 这里是保存文件产生异常
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // fos流关闭异常
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    // oos流关闭异常
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param context activity
     * @param name    数据库的dat名称
     * @return Object
     * @function 从本地读取保存的对象
     * @author 高明峰
     */
    public static Object getObject(Context context, String name) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(name);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            // e.printStackTrace();
            Log.d(TAG, "getObject '" + name + "' error. " + e.toString());
            // 这里是读取文件产生异常
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // fis流关闭异常
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // ois流关闭异常
                    e.printStackTrace();
                }
            }
        }
        // 读取产生异常，返回null
        return null;
    }

    /**
     * 根据uri的地址,转换base64的数据
     *
     * @param path
     * @return
     */
    public static String imageUriToBase64(String path) {
        byte[] input = null;
        FileInputStream in = null;
        try {
            File file = new File(path);
            in = new FileInputStream(file);
            input = new byte[in.available()];
            in.read(input);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Base64.encodeToString(input, Base64.DEFAULT);
    }

    /**
     * 加载本地图片
     *
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据接口返回的code，获取错误信息
     *
     * @param context
     * @param code
     * @return
     */
    public static String getErrorMsg(Context context, String code) {
        if (TextUtils.isEmpty(code)) {
            return context.getResources().getString(R.string.server_error_tip);
        }
        String msg = (String) setErrorMsg(context).get(Integer.parseInt(code));
        if (TextUtils.isEmpty(msg)) {
            msg = context.getResources().getString(R.string.server_error_tip);
        }
        return msg;

    }

    /**
     * 获取电视派的推送接口
     *
     * @param context
     */
    /*public static int getHttpServicePort(Context context) {
        return MonitorUtil.getInstance().getHttpServerPort();
    }*/

    /**
     * 创建一个简单的确认对话框
     *
     * @param context
     * @param message        提示消息
     * @param positiveButton 确定按钮
     * @param negativeButton 取消按钮
     * @param callback       返回事件
     */
    public static void confirmMyDialog(Context context, String message, String positiveButton, String negativeButton, final DialogCallback callback) {

        /*MyDialog dialog = new MyDialog(context, R.style.MyDialog) {
            @Override
            public void Yesclick() {
                callback.onClick(this, DialogInterface.BUTTON_POSITIVE);
                this.dismiss();
            }

            @Override
            public void Noclick() {
                callback.onClick(this, DialogInterface.BUTTON_NEGATIVE);
                this.dismiss();
            }
        };
        dialog.setOnCancelListener(callback);
        dialog.setdialog((int) context.getResources().getDimension(R.dimen.DIMEN_900PX), 1.0f);
        dialog.setTitleText(message);
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        dialog.setTitleSize((int) (context.getResources().getDimension(R.dimen.DIMEN_55PX) / dm.density));
        dialog.setInfoSize((int) (context.getResources().getDimension(R.dimen.DIMEN_45PX) / dm.density));
        dialog.setBtnYesSize((int) (context.getResources().getDimension(R.dimen.DIMEN_60PX) / dm.density));
        dialog.setBtnNoSize((int) (context.getResources().getDimension(R.dimen.DIMEN_60PX) / dm.density));
        dialog.setInfoVisible(View.GONE);
        //确定按钮
        if (positiveButton != null) {
            dialog.setBtnYesText(positiveButton);
            dialog.setBtnYesVisible(View.VISIBLE);
        } else {
            dialog.setBtnYesVisible(View.GONE);
        }

        //取消按钮
        if (negativeButton != null) {
            dialog.setBtnNoText(negativeButton);
            dialog.setBtnNoVisible(View.VISIBLE);
        } else {
            dialog.setBtnNoVisible(View.GONE);
        }

        //如果都为空，则显示确定按钮
        if (TextUtils.isEmpty(positiveButton) && TextUtils.isEmpty(negativeButton)) {
            dialog.setBtnYesVisible(View.VISIBLE);
        }
        dialog.show();*/
    }

    /**
     * 接口
     */
    public interface DialogCallback extends DialogInterface.OnClickListener,
            DialogInterface.OnDismissListener,
            DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialog);

        @Override
        public void onDismiss(DialogInterface dialog);

        @Override
        public void onClick(DialogInterface dialog, int which);

    }

    /**
     * DialogCallback的实现
     */
    public static class DialogCallbackSimpleImpl implements DialogCallback {

        @Override
        public void onCancel(DialogInterface dialog) {

        }

        @Override
        public void onDismiss(DialogInterface dialog) {

        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE://左边按钮点击返回
                    onPositiveButtonClick(dialog);
                    break;
                case DialogInterface.BUTTON_NEGATIVE://右边按钮点击返回
                    onNegativeButtonClick(dialog);
                    break;
            }
        }

        public void onPositiveButtonClick(DialogInterface dialog) {
        }

        public void onNegativeButtonClick(DialogInterface dialog) {
        }

    }

    /**
     * 封装 错误码信息
     *
     * @param context
     * @return
     */
    public static HashMap<Integer, String> setErrorMsg(final Context context) {

        HashMap<Integer, String> errorMsgMap = new HashMap<Integer, String>() {
            {
                put(201001, context.getResources().getString(R.string.code201001));
                put(201002, context.getResources().getString(R.string.code201002));
                put(201003, context.getResources().getString(R.string.code201003));
                put(201004, context.getResources().getString(R.string.code201004));
                put(201005, context.getResources().getString(R.string.code201005));
                put(202001, context.getResources().getString(R.string.code202001));
                put(202002, context.getResources().getString(R.string.code202002));
                put(202003, context.getResources().getString(R.string.code202003));
                put(202004, context.getResources().getString(R.string.code202004));
                put(202005, context.getResources().getString(R.string.code202005));
                put(203001, context.getResources().getString(R.string.code203001));
                put(203002, context.getResources().getString(R.string.code203002));
                put(203003, context.getResources().getString(R.string.code203003));
                put(204001, context.getResources().getString(R.string.code204001));
                put(204002, context.getResources().getString(R.string.code204002));
                put(204003, context.getResources().getString(R.string.code204003));
                put(204004, context.getResources().getString(R.string.code204004));
                put(204005, context.getResources().getString(R.string.code204005));
                put(205001, context.getResources().getString(R.string.code205001));
                put(205002, context.getResources().getString(R.string.code205002));
                put(205003, context.getResources().getString(R.string.code205003));
                put(205004, context.getResources().getString(R.string.code205004));
                put(206001, context.getResources().getString(R.string.code206001));
                put(206002, context.getResources().getString(R.string.code206002));
                put(206003, context.getResources().getString(R.string.code206003));
                put(206004, context.getResources().getString(R.string.code206004));
                put(206005, context.getResources().getString(R.string.code206005));
                put(206006, context.getResources().getString(R.string.code206006));
                put(206007, context.getResources().getString(R.string.code206007));
                put(206008, context.getResources().getString(R.string.code206008));
                put(206009, context.getResources().getString(R.string.code206009));
                put(206010, context.getResources().getString(R.string.code206010));
                put(207001, context.getResources().getString(R.string.code207001));
                put(207002, context.getResources().getString(R.string.code207002));
                put(207003, context.getResources().getString(R.string.code207003));
                put(207004, context.getResources().getString(R.string.code207004));
                put(207005, context.getResources().getString(R.string.code207005));
                put(207006, context.getResources().getString(R.string.code207006));
                put(207007, context.getResources().getString(R.string.code207007));
                put(207008, context.getResources().getString(R.string.code207008));
                put(207009, context.getResources().getString(R.string.code207009));
                put(208001, context.getResources().getString(R.string.code208001));
                put(208002, context.getResources().getString(R.string.code208002));
                put(208003, context.getResources().getString(R.string.code208003));
                put(208004, context.getResources().getString(R.string.code208004));

                put(403001, context.getResources().getString(R.string.code403001));
                put(403002, context.getResources().getString(R.string.code403002));
                put(403003, context.getResources().getString(R.string.code403003));
                put(403004, context.getResources().getString(R.string.code403004));
                put(403005, context.getResources().getString(R.string.code403005));
                put(503001, context.getResources().getString(R.string.code503001));
            }
        };
        return errorMsgMap;
    }

}
