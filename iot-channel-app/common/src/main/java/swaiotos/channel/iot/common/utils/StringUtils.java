package swaiotos.channel.iot.common.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.utils
 * @ClassName: StringUtils
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/9 12:38
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/9 12:38
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class StringUtils {
    private static String TAG = StringUtils.class.getSimpleName();

    public StringUtils() {
    }

    @SuppressLint({"DefaultLocale"})
    public static String parseStringBrand(String str) throws PatternSyntaxException {
        if (!isEmpty(str)) {
            String brand = StringFilterEg(str);
            return brand.replaceAll("\\s*", "");
        } else {
            return "";
        }
    }

    @SuppressLint({"DefaultLocale"})
    public static String parseStringModel(String str) throws PatternSyntaxException {
        if (!isEmpty(str)) {
            try {
                String strFilterString = StringFilter(str);
                strFilterString.replace("//", "");
                String strChinese = StringChinese(strFilterString);
                return strChinese.replaceAll("\\s*", "");
            } catch (Exception var3) {
                var3.printStackTrace();
                Log.d(TAG, "" + var3.toString());
                return "";
            }
        } else {
            return "";
        }
    }

    public static String StringFilter(String str) {
        try {
            String regEx = "[^1234567890qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM_-]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(str);
            return m.replaceAll("").trim();
        } catch (Exception var4) {
            var4.printStackTrace();
            Log.d(TAG, "" + var4.toString());
            return "";
        }
    }

    public static String StringFilterEg(String str) {
        try {
            String reg = "[^a-zA-Z0-9]";
            Pattern pat = Pattern.compile(reg);
            Matcher mat = pat.matcher(str);
            return mat.replaceAll("");
        } catch (Exception var4) {
            var4.printStackTrace();
            Log.d(TAG, "" + var4.toString());
            return "";
        }
    }

    public static String StringChinese(String str) {
        try {
            String reg = "[一-龥]";
            Pattern pat = Pattern.compile(reg);
            Matcher mat = pat.matcher(str);
            return mat.replaceAll("");
        } catch (Exception var4) {
            var4.printStackTrace();
            Log.d(TAG, "" + var4.toString());
            return "";
        }
    }

    public static String parseEmpty(String str) {
        if (str == null || "null".equals(str.trim())) {
            str = "";
        }

        return str.trim();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0 || str.equals("null");
    }

    public static boolean isMacLegal(String mac) {
        if (!TextUtils.isEmpty(mac) && mac.length() == 12) {
            try {
                String reg = "[a-fA-F0-9]+";
                Pattern pat = Pattern.compile(reg);
                Matcher mat = pat.matcher(mac);
                return mat.matches();
            } catch (Exception var4) {
                var4.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
}