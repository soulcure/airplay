package swaiotos.channel.iot.utils;

public class HexUtil {

    /**
     * 将指定字节数组转化为long
     *
     * @param array byte[]
     * @param bool  true高位在前，false高位在后
     * @return long
     */
    public static long getLongByBytes(byte[] array, boolean bool) {
        if (array == null) {
            return 0;
        }
        long tmp = 0;
        for (int i = 0; i < array.length; i++) {
            tmp = (tmp | ((long) ((array[bool ? array.length - 1 - i : i]) & 0xff) << (i * 8))); // 其他字节转化
        }
        return tmp;
    }


    /**
     * 将指定的long数据提取全部8位到byte数组，高位在前，依次类推
     *
     * @param num long
     * @return byte[]
     */
    public static byte[] longToBytes(long num, boolean bool) {
        byte[] array = new byte[8];
        longToBytes(num, array, bool);
        return array;
    }


    /**
     * 将指定的long数据内的array个数据写入array，从低位开始
     *
     * @param num   要提取的数据
     * @param array 缓存数组
     * @param bool  true高位在前，false低位在前
     */
    private static void longToBytes(long num, byte[] array, boolean bool) {
        if (array == null) {
            return;
        }
        for (int i = 0; i < array.length; ++i) {
            if (i == array.length - 1) {
                array[bool ? array.length - 1 - i : i] = (byte) ((num >> (i * 8)) & 0xff); // 最高位
            }
            array[bool ? array.length - 1 - i : i] = (byte) ((num >>> (i * 8)) & 0xff); // 最低位
        }
    }


    /**
     * 将指定字节数组转化为字符串
     *
     * @param array       byte[]
     * @param charsetName "US-ASCII" "UTF-8"
     * @return enc 指定编码
     */
    public static String getString(byte[] array, String charsetName) {
        if (array == null || array.length <= 0) {
            return null;
        }
        try {
            int bitLen = array.length; // 字符串的实际长度
            for (int i = 0; i < array.length; i++) { // 字符串遇到0将自动结束
                if (array[i] == 0) {
                    bitLen = i;
                    break;
                }
            }
            return new String(array, 0, bitLen, charsetName).trim();
        } catch (Exception e) {
            return new String(array).trim();
        }
    }


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

    public static String bytes2HexString1(byte[] b) {
        StringBuilder sb = new StringBuilder();
        int length = b.length;
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            /*sb.append("0x").append(hex.toUpperCase());
            if (i < length - 1) {
                sb.append(',');
            }*/
            sb.append(hex.toUpperCase());
            if (i < length - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }


    public static String byte2HexString(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return "0x" + hex.toUpperCase();
    }


    public static int convertHexStringToInt(String hexStr) {
        int res;

        String arrHexStr[] = hexStr.toLowerCase().split("x");
        if (arrHexStr.length == 2) {
            hexStr = arrHexStr[1];
        }

        try {
            res = Integer.parseInt(hexStr, 16); //base 16 for converting hexadecimal string
        } catch (NumberFormatException e) {
            res = 0;
        }

        return res;
    }
}
