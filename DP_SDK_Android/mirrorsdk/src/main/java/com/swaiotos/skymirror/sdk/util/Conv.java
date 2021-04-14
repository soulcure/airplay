package com.swaiotos.skymirror.sdk.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

public final class Conv {
	
	public static int booleanToInt(boolean flag) {
		return flag ? 1 : 0;
	}
	
	public static boolean intToBoolean(int flag) {
		return flag != 0 ? true : false;
	}
	
	public static int[] arrayToPrimitive(ArrayList<Integer> array) {
		int[] ret = new int[array.size()];
		for (int i = 0; i < ret.length; i++) {
	        ret[i] = array.get(i).intValue();
	    }
		return ret;
	}
	
	public static int[] arrayToPrimitive(LinkedList<Integer> array) {
		int[] ret = new int[array.size()];
		int retIndex = 0;
		for (Integer val: array) {
	        ret[retIndex++] = val.intValue();
	    }
		return ret;
	}
	
	/** 
	 * Build char array with specified length,<br/>
	 * any unused space is zero.<br/>
	 * NOTE: sizeof(char) = 2
	 * HHNote: length is the number of characters, so bytes count is length*sizeof(char)
	 */
	public static char[] strToFixedLengthChars(String str, int length) {
		char[] fixedLengthChars = new char[length];
		Arrays.fill(fixedLengthChars, '\0');
		int relCharLen = Math.min(str.length(), length);	
		str.getChars(0, relCharLen, fixedLengthChars, 0);		
		return fixedLengthChars;
	}
	
	/**
	 *  signed byte to unsigned */
	public static int byteToUByte(int value) {
		return (value >= 0) ? value : (value+256); // (int)Math.pow(2, 8)
	}
	
	/**
	 *  signed int to unsigned */
	public static int shortToUShort(int value) {
		return (value >= 0) ? value : (value+65536); // (int)Math.pow(2, 16)
	}
	
	/**
	 *  signed long to unsigned */
	public static long intToUInt(long value) {
		return (value >= 0) ? value : (value+4294967296l); //(long)Math.pow(2, 32)
	}
	
	//0x01020304(DWORD)
	//MemAddress 	4000 	4001 	4002 	4003
	//LE 			04 		03 		02 		01
	//BE 			01 		02		03 		04
	
	/**
	 *  little endian int -> string ipv4 */
	public static String ipv4IntLEToStr(int ip) {
		int a = Conv.byteToUByte((ip<<24)>>24);
		int b = Conv.byteToUByte((ip<<16)>>16>>8);
		int c = Conv.byteToUByte((ip<<8)>>8>>16);
		int d = Conv.byteToUByte(ip>>24);
		StringBuilder builder = new StringBuilder(7);
		return builder.append(a).append('.').append(b).append('.').append(c).append('.').append(d).toString();
	}
	
	/**
	 *  big endian int -> string ipv4 */
//	public static String ipv4IntBEToStr(int ip) {
//		int a = Conv.byteToUByte(ip>>24);
//		int b = Conv.byteToUByte((ip<<8)>>8>>16);
//		int c = Conv.byteToUByte((ip<<16)>>16>>8);
//		int d = Conv.byteToUByte((ip<<24)>>24);
//		StringBuilder builder = new StringBuilder(7);
//		return builder.append(a).append('.').append(b).append('.').append(c).append('.').append(d).toString();
//	}	
	

	/**
	 *  string ipv4 -> little endian int */
	public static int ipv4StrToIntLE(String ip) {
		String[] section = ip.split("\\.");
		int numIp = 0;
		int valIndex = 3;
		for (String val: section) {
			numIp += Integer.parseInt(val) << ((3-valIndex)<<3);
			--valIndex;
		}
		return numIp;
	}
	
//	/**
//	 *  string ipv4 -> big endian int */
//	public static int ipv4StrToIntBE(String ip) {
//		String[] section = ip.split("\\.");
//		int numIp = 0;
//		int valIndex = 0;
//		for (String val: section) {
//			numIp += Integer.parseInt(val) << ((3-valIndex)<<3);
//			++valIndex;
//		}
//		return numIp;
//	}
	
	
	/**
	 *  string MAC -> 6 bytes hex */
	public static byte[] macStrToBytes(String mac) {
		String[] section = mac.split("\\:");
		byte[] hexMac = new byte[6];
		int valIndex = 0;
		for (String val: section) {
			hexMac[valIndex++] = (byte) Integer.parseInt(val, 16);
		}
		return hexMac;
	}
	
	public static String macBytesToStr(byte[] hexMac)
	  {
	    if ((hexMac == null) || (hexMac.length != 6))
	      return null;

	    return String.format(Locale.US, "%02X:%02X:%02X:%02X:%02X:%02X", 
	    		hexMac[0], hexMac[1], hexMac[2], hexMac[3], hexMac[4], hexMac[5]);
	  }
	
	/**
	 *  little endian signed int <=> string magic */
	public static String intToMagic(int magic) {
		int a = Conv.byteToUByte(magic>>24);
		int b = Conv.byteToUByte((magic<<8)>>8>>16);
		int c = Conv.byteToUByte((magic<<16)>>16>>8);
		int d = Conv.byteToUByte((magic<<24)>>24);
		StringBuilder builder = new StringBuilder(4);
		return builder.append((char)a).append((char)b).append((char)c).append((char)d).toString();
	}
	
	public static int magicToInt(String magic) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.put(magic.getBytes(), 0, 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(0);
		return buffer.getInt();
	}
	
	/**
	 * Convert Java UTF-8 string to Windows UTF-16 c-style string.
	 * @author YanhuiShen , Change by Radin.Yang
	 */
	public static byte[] stringToUtf16CStr(String str) {
		byte[] cstr = null;
		try {
			byte[] bstr = (str+'\0').getBytes("UnicodeLittle");
			cstr = new byte[bstr.length - 2];// "UnicodeLittle" Head 0xFF0xFE
			for (int i = 0; i < cstr.length; i++) 
			{
				cstr[i] = bstr[i+2];
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return cstr;
	}
	
	public static byte[] stringToUtf16CStrNoTerminator(String str) {
		byte[] cstr = null;
		try {
			byte[] bstr = (str).getBytes("UnicodeLittle");
			cstr = new byte[bstr.length - 2];// "UnicodeLittle" Head 0xFF0xFE
			for (int i = 0; i < cstr.length; i++) 
			{
				cstr[i] = bstr[i+2];
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return cstr;
	}
	
	public static byte[] stringToUtf16CStrFixLength(String str, int nLength)
	{
		byte[] byteTemp = new byte[nLength];
		ByteBuffer bfTemp = ByteBuffer.wrap(byteTemp);

		byte[] byteFaceName = Conv.stringToUtf16CStr(str);
		bfTemp.put(byteFaceName);
		bfTemp.rewind();
		
		return byteTemp;
	}
	
	//
	public static byte[] stringToUtf8CStrFixLength(String str, int nLength)
	{
		byte[] byteTemp = new byte[nLength];
		ByteBuffer bfTemp = ByteBuffer.wrap(byteTemp);

//		byte[] byteFaceName = Conv.stringToUtf16CStr(str);
		byte[] byteFaceName;
		try {
			byteFaceName = str.getBytes("UTF-8");
			bfTemp.put(byteFaceName);
			bfTemp.rewind();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return byteTemp;
	}
	
	public static byte[] stringToUtf8CStr(String str) {
		byte[] cstr = null;
		try {
			byte[] bstr = (str+'\0').getBytes("UnicodeLittle");
			cstr = new byte[bstr.length - 2];// "UnicodeLittle" Head 0xFF0xFE
			for (int i = 0; i < cstr.length; i++) 
			{
				cstr[i] = bstr[i+2];
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return cstr;
	}

	
	/* ascii byte stream to string */
	public static String asciiBytesToString(byte[] bytes) {
		StringBuilder builder = new StringBuilder(bytes.length);		
		for (byte val: bytes) {
			builder.append((char)val);
		}
		return builder.toString();
	}
	
	public static String asciiBytesToString(byte[] bytes, int length) {
		StringBuilder builder = new StringBuilder(length);		
		for (byte val: bytes) {
			builder.append((char)val);
		}
		return builder.toString();
	}
	
	// TODO:
	// if b.length < 4, it will crash!
	public static int bytesToInt(byte[] b) {
		//byte[] b=new byte[]{1,2,3,4};
		int mask = 0xff;
	    int temp = 0;
	    int res = 0;
	    for (int i=0; i<4; i++){
	        res <<= 8;
	        temp = b[i]&mask;
	        res |= temp;
	    }
	    return res;
	}

	public static byte[] intToBytes(int num) {
		byte[] b = new byte[4];
	    // int mask = 0xff;
	    for (int i = 0; i<4; i++)
	    {
	    	b[i] = (byte)(num>>>(24-i*8));
	    }
	    return b;
	}		
}
