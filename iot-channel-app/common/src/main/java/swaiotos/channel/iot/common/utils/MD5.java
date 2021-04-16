package swaiotos.channel.iot.common.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * ****************************************************************************
 * md5 类实现了RSA Data Security, Inc.在提交给IETF 的RFC1321中的MD5 message-digest 算法。
 * ****************************************************************************
 */
public class MD5 {

	/**
	 * 用MD5算法加密
	 *
	 * @param in
	 *            String : 待加密的原文
	 * @return String : 加密后的密文，如果原文为空，则返回null;
	 */
	public static String encode(final String in) {
		return encode(in, "");
	}

	/**
	 * 用MD5算法加密
	 *
	 * @param in
	 *            String : 待加密的原文
	 * @param charset
	 *            String : 加密算法字符集
	 * @return String : 加密后的密文，若出错或原文为null，则返回null
	 */
	public static String encode(final String in, final String charset) {
		if (in == null)
			return null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (charset == null || "".equals(charset)) {
				md.update(in.getBytes());
			} else {
				try {
					md.update(in.getBytes(charset));
				} catch (Exception e) {
					md.update(in.getBytes());
				}
			}
			byte[] digesta = md.digest();
			return byte2hex(digesta);
		} catch (NoSuchAlgorithmException ex) {
			Log.e("MD5", "encode(" + in + "," + charset
					+ "):NoSuchAlgorithmException -->" + ex.getMessage());
			return null;
		}
	}


	private static String byte2hex(final byte[] b) {
		if (b == null)
			return null;
		StringBuilder output = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int current = b[i] & 0xff;
			if (current < 16)
				output.append("0");
			output.append(Integer.toString(current, 16));
		}

		return output.toString();
	}

	public static String getMd5(String inStr, String charset)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		if (!StringUtils.isEmpty(inStr)) {
			byte[] byteArray = inStr.getBytes(charset);
			byte[] md5Bytes = md5.digest(byteArray);
			StringBuffer hexValue = new StringBuffer();
			int val;
			for (int i = 0; i < md5Bytes.length; i++) {
				val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16)
					hexValue.append("0");
				hexValue.append(Integer.toHexString(val));
			}
			return hexValue.toString();
		}
		return "";
	}

	public static String getMd5(String inStr)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		return getMd5(inStr, "UTF-8");
	}

	/**
	 * 签名字符串
	 * @param text 需要签名的字符串
	 * @param key 密钥
	 * @param input_charset 编码格式
	 * @return 签名结果
	 */
	public static String sign(String text, String key, String input_charset) {
		text = text + key;
		try {
			return getMd5(text,input_charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}


//	/**
//	 * 签名字符串
//	 * @param text 需要签名的字符串
//	 * @param sign 签名结果
//	 * @param key 密钥
//	 * @param input_charset 编码格式
//	 * @return 签名结果
//	 */
//	public static boolean verify(String text, String sign, String key, String input_charset) {
//		text = text + key;
//		String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset));
//		if(mysign.equals(sign)) {
//			return true;
//		}
//		else {
//			return false;
//		}
//	}

	protected static char[] encodeHex(final byte[] data, final char[] toDigits) {
		final int l = data.length;
		final char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
			out[j++] = toDigits[0x0F & data[i]];
		}
		return out;
	}


	/**
	 * @param content
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static byte[] getContentBytes(String content, String charset) {
		if (charset == null || "".equals(charset)) {
			return content.getBytes();
		}
		try {
			return content.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
		}
	}


	//普通方式
	public static String MD5(String key) {
		char hexDigits[] = {
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
		};
		try {
			byte[] btInput = key.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}


}
