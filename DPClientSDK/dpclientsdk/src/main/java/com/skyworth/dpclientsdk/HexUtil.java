package com.skyworth.dpclientsdk;

public class HexUtil {


    /**
     * 二进制数组转十六进制，使用逗号隔开
     *
     * @param b
     * @return
     */
    public static String bytes2HexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        int length = b.length;
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append("0x").append(hex.toUpperCase());
            if (i < length - 1) {
                sb.append(',');
            }
        }
        return sb.toString();
    }


    /**
     * 二进制数组转十六进制,不使用逗号隔开
     *
     * @param src
     * @return
     */
    public static String bytesToHex(byte[] src) {
        StringBuilder sb = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                sb.append(0);
            }
            sb.append(hv);
        }
        return sb.toString();
    }


}
