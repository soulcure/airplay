package com.coocaa.publib.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.EditText;


import com.coocaa.publib.PublibHelper;
import com.coocaa.publib.R;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FileName : StringUtils Description : string相关处理对象
 *
 * @author : 高明峰
 * @version : 1.0 Create Date : 2015-04-30 下午03:10:24
 **/
@SuppressLint("SimpleDateFormat")
public class StringUtils {

    /**
     * 生成几位数的随机数 <功能详细描述>
     *
     * @param num
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String getRandomStr(int num) {
        Random random = new Random();
        String result = "";
        for (int i = 0; i < num; i++) {
            result += random.nextInt(10);
        }
        return result;
    }

    /**
     * 把list数据转换为已&分隔的字符串
     *
     * @param stringList 需要转换的list
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String listToString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String string : stringList) {
            result.append(string);
            result.append("&");
        }
        return result.toString().substring(0, result.toString().length() - 1);
    }

    /**
     * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为1,英文字符长度为0.5
     *
     * @param s 需要得到长度的字符串
     * @return int 得到的字符串长度
     */
    public static int getStrLength(String s) {
        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        // 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
        for (int i = 0; i < s.length(); i++) {
            // 获取一个字符
            String temp = s.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
                // 中文字符长度为1
                valueLength += 2;
            } else {
                // 其他字符长度为0.5
                valueLength += 1;
            }
        }
        // 进位取整
        return (int) Math.ceil(valueLength);
    }

    /**
     * 获取sdk版本
     *
     * @return int [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static int getSDKVersionNumber() {
        int sdkVersion;
        try {
            sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            sdkVersion = 0;
        }
        return sdkVersion;
    }

    /**
     * 去除str里面的重复
     *
     * @param arr [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String judgeOnly(String arr) {
        String[] strs = arr.split("&");
        String result = "";

        Set<String> set = new HashSet<String>();

        for (String s : strs) {
            if (set.add(s)) // 如果能添加进去，说明没有重复

            {
                result += s + "&";

            }
        }
        return result = result.substring(0, result.length() - 1);
    }


    /**
     * 判断邮箱是否合法
     *
     * @param email
     * @return boolean false 验证不通过,true正确
     */
    public static boolean isEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        // Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p = Pattern
                .compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");// 复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 验证密码
     *
     * @param mContext
     * @param mPsw           密码的输入框
     * @param isShowErrorMsg 是否显示错误线下
     * @return boolean false 验证不通过,true正确
     */
    public static boolean validatePsw(Context mContext, EditText mPsw, boolean isShowErrorMsg) {
        // 成功和失败图片显示
        Drawable mErrDraw = mContext.getResources().getDrawable(
                R.drawable.validate_err);
        Drawable mSuccDraw = mContext.getResources().getDrawable(
                R.drawable.validate_succ);
        int size = DimensUtils.dp2Px(mContext, 20);
        Rect bounds = new Rect(0, 0, size, size);
        mErrDraw.setBounds(bounds);
        mSuccDraw.setBounds(bounds);
        // 判断密码是否为空
        String psw = mPsw.getText().toString();
        if (TextUtils.isEmpty(psw) || psw.length() < 6 || psw.length() > 25) {
            mPsw.setCompoundDrawables(null, null, mErrDraw, null);
            if (isShowErrorMsg) {
//                UIHelper.showMessage(mContext, R.string.psw_error);
                ToastUtils.getInstance().showGlobalShort(mContext.getString(R.string.psw_error));
            }
            return false;
        }
        mPsw.setCompoundDrawables(null, null, mSuccDraw, null);
        return true;
    }

    /**
     * 验证确定密码
     *
     * @param mContext
     * @param psw0           设置的第一个密码
     * @param mVerifyPsw     确定密码的输入框
     * @param isShowErrorMsg 是否提示错误信息
     * @return boolean false 验证不通过,true正确
     */
    public static boolean validateVerifyPsw(Context mContext, String psw0,
                                            EditText mVerifyPsw, boolean isShowErrorMsg) {
        // 成功和失败图片显示
        Drawable mErrDraw = mContext.getResources().getDrawable(
                R.drawable.validate_err);
        Drawable mSuccDraw = mContext.getResources().getDrawable(
                R.drawable.validate_succ);
        int size = DimensUtils.dp2Px(mContext, 20);
        Rect bounds = new Rect(0, 0, size, size);
        mErrDraw.setBounds(bounds);
        mSuccDraw.setBounds(bounds);
        // 判断确定密码是否为空
        String psw1 = mVerifyPsw.getText().toString();
        if (TextUtils.isEmpty(psw1) || psw1.length() < 6 || psw1.length() > 25) {
            mVerifyPsw.setCompoundDrawables(null, null, mErrDraw, null);
            // 需要的时候显示
            if (isShowErrorMsg) {
//                UIHelper.showMessage(mContext, R.string.verify_psw_error);
                ToastUtils.getInstance().showGlobalLong(mContext.getString(R.string.verify_psw_error));
            }
            return false;
        }
        // 判断确定密码跟密码是否一致
        if (!psw1.equals(psw0)) {
            mVerifyPsw.setCompoundDrawables(null, null, mErrDraw, null);
            // 需要的时候显示
            if (isShowErrorMsg) {
//                UIHelper.showMessage(mContext, R.string.verify_psw_notsame);
                ToastUtils.getInstance().showGlobalShort(mContext.getString(R.string.verify_psw_notsame));
            }
            return false;
        }
        mVerifyPsw.setCompoundDrawables(null, null, mSuccDraw, null);
        return true;
    }

    /**
     * 保留几位小数
     *
     * @param f 需要转换的数字
     * @param d 保留几位数字
     * @return
     */
    public static double retainDecimal(double f, int d) {
        BigDecimal b = new BigDecimal(f);// BigDecimal 类使用户能完全控制舍入行为
        double f1 = b.setScale(d, BigDecimal.ROUND_HALF_DOWN).doubleValue();
        return f1;
    }

    /**
     * 截取城市
     *
     * @return
     */
    public static String subCity(String address) {
        int startIndex = 0;
        //先判断自治区
        if (address.indexOf("自治区") != -1) {
            startIndex = address.indexOf("自治区") + 1;
        } else if (address.indexOf("省") != -1) {
            startIndex = address.indexOf("省") + 1;
        }

        //判断是否存在省
        int cityIndex = address.indexOf("市") + 1;
        if (cityIndex == 0) {
            cityIndex = 3;
        }
        String result = address.substring(startIndex, cityIndex);

        return result;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    public static String truncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

//        strURL = strURL.trim().toLowerCase();去掉小写转换 modified by wuhaiyuan

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    /**
     * 解析出url参数中的键值对 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> URLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = truncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        // 每个键值为一组 www.2cto.com
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            // 解析出键值
            if (arrSplitEqual.length > 1) {
                // 正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (arrSplitEqual[0] != "") {
                    // 只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    public static String buildString(Object... args) {
        StringBuilder build = new StringBuilder();
        for (Object item : args) {
            build.append(item);
        }

        return build.toString();
    }

    public static double String2Double(String val) {
        Double cny = Double.parseDouble(val);//6.2041
        return cny;
    }


    /**
     * 替换手机号码135****1234
     *
     * @param str
     * @return
     */
    public static String replaceMobile(String str) {
        if (!isMobileNO(str)) {
            return str;
        }

        return str.substring(0, str.length() - (str.substring(3)).length()) + "****" + str.substring(7);
    }


    /**
     * 判断是否是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    // 获取文字宽度,传入dp，内部转换为px尺寸给paint
    public static int getStringWidth(String text, float size) {
        if (null == text || "".equals(text)){
            return 0;
        }

        Paint paint = new Paint();
        paint.setTextSize(DimensUtils.dp2Px(PublibHelper.getContext(), size));
        int text_width = (int) paint.measureText(text);// 得到总体长度
        // int width = text_width/text.length();//每一个字符的长度
        return text_width;
    }

    /**
     * @param mobiles : 电话号码
     * @return boolean false 验证不通过,true正确
     */
    public static boolean isMobileNO(String mobiles) {
        if (TextUtils.isEmpty(mobiles)) {
            return false;
        }
        /*  *//*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188,147
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 网络:177,178 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         *//*
        String telRegex = "[1][345678]\\d{9}";// "[1]"代表第1位为数字1，"[3458]"代表第二位可以为3、4、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        return mobiles.matches(telRegex);*/

        //判断字符串是否是纯数字
        if(mobiles.length() == 11) {
            Pattern pattern = Pattern.compile("^[0-9]*$");
            Matcher isNum = pattern.matcher(mobiles);
            return isNum.matches();
        }else {
            return false;
        }
    }
}
