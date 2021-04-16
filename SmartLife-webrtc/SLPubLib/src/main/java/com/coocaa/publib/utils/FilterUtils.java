package com.coocaa.publib.utils;

/**
 * <p>滤镜工具类</p>
 * <p/>
 * R' = a*R + b*G + c*B + d*A + e;<br>
 * G' = f*R + g*G + h*B + i*A + j;<br>
 * B' = k*R + l*G + m*B + n*A + o;<br>
 * A' = p*R + q*G + r*B + s*A + t;<br>
 */
public class FilterUtils {
    /**
     * RGBA蓝色滤镜
     */
    static final float arrayBlue[] = {
            1, 0, 0, 0, 50,
            0, 1, 0, 0, 80,
            0, 0, 1, 0, 255,
            0, 0, 0, 1, 0};
    /**
     * RGBA绿色滤镜
     */
    static final float arrayGreen[] = {
            1, 0, 0, 0, 0,
            0, 1, 0, 0, 100,
            0, 0, 1, 0, 0,
            0, 0, 0, 1, 0};
    /**
     * RGBA灰色滤镜
     */
    static final float arrayGray[] = {
            0, 0, 0, 0, 110,
            0, 0, 0, 0, 110,
            0, 0, 0, 0, 110,
            0, 0, 0, 1, 0};
    /**
     * RGBA深色滤镜
     */
    static final float arrayDark[] = {
            1, 0, 0, 0, -20,
            0, 1, 0, 0, -20,
            0, 0, 1, 0, -20,
            0, 0, 0, 1, 0};
    /**
     * RGBA浅色滤镜
     */
    static final float arrayLight[] = {
            1, 0, 0, 0, 20,
            0, 1, 0, 0, 20,
            0, 0, 1, 0, 20,
            0, 0, 0, 1, 0};

    /**
     * 背景颜色变换方式
     */
    public enum Type {
        /**
         * 变蓝
         */
        blue,
        /**
         * 变绿
         */
        green,
        /**
         * 变灰
         */
        gray,
        /**
         * 深色
         */
        dark,
        /**
         * 浅色
         */
        light,
        /**
         * 自定义滤镜
         */
        self
    }

    public static float[] getFilter(Type t) {
        switch (t) {
            case blue:
                return arrayBlue;
            case green:
                return arrayGreen;
            case light:
                return arrayLight;
            case dark:
                return arrayDark;
            default:
                return arrayDark;
        }
    }

    /**
     * 获取亮度滤镜，经验值-10变暗，10变亮
     *
     * @param brightness 负数：变暗 正数：变亮（绝对值通常不超过255）
     * @return
     */
    public static float[] getBrightFilter(int brightness) {
        return new float[]{
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0};
    }
}
