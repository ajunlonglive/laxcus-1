/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

//import java.awt.*;
import java.io.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.access.diagram.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.each.*;
import com.laxcus.util.hash.*;

/**
 * LAXCUS基础工具包。<br>
 * 包括：<br>
 * 1. 数值、字符、字节数组的转换，数值/字符数组转换采用小字头(little-endian)编码格式。<br>
 * 2. 数值、字符串的大小比较/排序比较。<br>
 * 3. 修订文本中带系统标记的参数。<br>
 * 4. 生成数字签名（MD5、SHA1、SHA256、SHA512）。<br>
 * 5. 其它公共杂项。<br>
 * 
 * @author scott.liang 
 * @version 1.5 8/6/2016
 * @since laxcus 1.0
 */
public final class Laxkit {

	/**
	 * 判断当前是简体中文环境
	 * @return 返回真或者假
	 */
	public static boolean isSimplfiedChinese() {
		Locale local = Locale.getDefault();
		String language = local.getLanguage();
		String country = local.getCountry();

		// 判断一致
		boolean success = language.equalsIgnoreCase(Locale.SIMPLIFIED_CHINESE.getLanguage());
		if (success) {
			success = country.equalsIgnoreCase(Locale.SIMPLIFIED_CHINESE.getCountry());
		}
		return success;
	}

	/**
	 * 判断是LINUX系统
	 * @return 返回真或者假
	 */
	public static boolean isLinux() {
		String os = System.getProperty("os.name");
		if (os == null) {
			return false;
		}
		return os.matches("^(.*?)(?i)(LINUX)(.*)$");
	}

	/**
	 * 判断是WINDOWS系统
	 * @return 返回真或者假
	 */
	public static boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os == null) {
			return false;
		}
		return os.matches("^(.*?)(?i)(WINDOWS)(.*)$");
	}

	/** 数据尺寸计量单位，从KB到EB（长整型） **/
	public static final long KB = 1024;

	public static final long MB = Laxkit.KB * 1024L;

	public static final long GB = Laxkit.MB * 1024L;

	public static final long TB = Laxkit.GB * 1024L;

	public static final long PB = Laxkit.TB * 1024L;
	
	public static final long EB = Laxkit.PB * 1024L;
	
	/** 数据尺寸计量单位，整型 **/
	public final static int kb = 1024;

	public final static int mb = Laxkit.kb * 1024;
	
	/** 为毫秒为单位的系统时间 **/
//	public final static long MILLIS_SECOND = 1;

	public final static long SECOND = 1000;

	public final static long MINUTE = SECOND * 60L;

	public final static long HOUR = MINUTE * 60;

	public final static long DAY = HOUR * 24;
	
	/**
	 * 判断对象类是从某个父类派生。即对象类是指定类的子类。
	 * @param o 对象类
	 * @param clazz 指定类
	 * @return 匹配成功返回“真”，否则“假”。
	 */
	public static boolean isClassFrom(Class<?> o, Class<?> clazz) {
		if (o == null || clazz == null) {
			return false;
		}
		// 比较一致，返回真
		if (o == clazz) {
			return true;
		}
		// 取父类
		Class<?> parent = o.getSuperclass();
		if (parent == null) {
			return false;
		}
		// 递归再比较
		return Laxkit.isClassFrom(parent, clazz);
	}

	/**
	 * 判断对象类是与指定类匹配，或者是它的子类
	 * @param object 对象实例
	 * @param clazz 指定类类型
	 * @return 匹配成功返回“真”，否则“假”。
	 */
	public static boolean isClassFrom(Object object, Class<?> clazz) {
		if (object == null || clazz == null) {
			return false;
		}
		return Laxkit.isClassFrom(object.getClass(), clazz);
	}
	
	/**
	 * 判断类定义实现某个接口。
	 * @param o 类定义
	 * @param clazz 指定接口
	 * @return 匹配成功返回“真”，否则“假”。
	 */
	public static boolean isInterfaceFrom(Class<?> o, Class<?> clazz) {
		if (o == null || clazz == null) {
			return false;
		}
		// 比较一致，返回真
		if (o == clazz) {
			return true;
		}
		// 取接口
		Class<?>[] vs = o.getInterfaces();
		int size = (vs != null ? vs.length : 0);
		// 逐一判断匹配
		for (int i = 0; i < size; i++) {
			if (vs[i] == clazz) {
				return true;
			}
		}
		// 取父类
		Class<?> parent = o.getSuperclass();
		if (parent == null) {
			return false;
		}
		// 递归继续判断
		return Laxkit.isInterfaceFrom(parent, clazz);
	}

	/**
	 * 判断对象类实现某个接口。
	 * @param object 对象实例
	 * @param clazz 指定接口
	 * @return 匹配成功返回“真”，否则“假”。
	 */
	public static boolean isInterfaceFrom(Object object, Class<?> clazz) {
		return Laxkit.isInterfaceFrom(object.getClass(), clazz);
	}
	
	/**
	 * 在一个指定的整形范围内，产生一个随机数
	 * @param from 开始数字
	 * @param to 结尾数字（包括这个数字）
	 * @return 返回指定范围内的随机数
	 */
	public static int random(int from, int to) {
		Rand rnd = new Rand();
		return rnd.nextInt(from, to);
	}

	/**
	 * 在一个指定的长整型范围内，产生一个随机数
	 * @param from 开始数字
	 * @param to 结尾数字（包括这个数字）
	 * @return 返回指定范围内的随机数
	 */
	public static long random(long from, long to) {
		Rand rnd = new Rand();
		return rnd.nextLong(from, to);
	}

	/**
	 * 打印错误堆栈
	 * @param fatal 异常
	 * @return 返回输出文本
	 */
	public static String printThrowable(Throwable fatal) {
		if (fatal == null) {
			return "";
		}

		CharArrayWriter buf = new CharArrayWriter(1024);
		PrintWriter print = new PrintWriter(buf, true);
		fatal.printStackTrace(print);
		return buf.toString();
	}

	/**
	 * 返回一个数据块编号的16进制字符串描述
	 * @param stub 数据块编号
	 * @return 16个字符的字符串
	 */
	public static String toStubString(long stub) {
		StringBuilder buf = new StringBuilder(String.format("%X", stub));
		while (buf.length() < 16) {
			buf.insert(0, '0');
		}
		return buf.toString();
	}
	
	/**
	 * 生成EACH签名
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 返回EACH签名（长整数）
	 */
	public static long doEach(byte[] b, int off, int len) {
		return EachTrustor.sign(b, off, len);
	}

	/**
	 * 生成EACH签名
	 * @param b 字节数组
	 * @return 返回EACH签名（长整数）
	 */
	public static long doEach(byte[] b) {
		return EachTrustor.sign(b, 0, b.length);
	}
	
	/**
	 * 生成文件的EACH签名
	 * @param file 磁盘文件
	 * @return 返回长整数
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static long doEach(File file) throws IOException, NoSuchAlgorithmException {
		byte[] b = new byte[(int) file.length()];

		FileInputStream in = new FileInputStream(file);
		in.read(b, 0, b.length);
		in.close();

		return Laxkit.doEach(b, 0, b.length);
	}
	
	/**
	 * 生成MD5签名
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return MD5散列码实例
	 */
	public static MD5Hash doMD5Hash(byte[] b, int off, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(b, off, len);
			byte[] sign = md.digest();
			return new MD5Hash(sign);
		} catch (NoSuchAlgorithmException e) {
			com.laxcus.log.client.Logger.error(e);
		}
		return null;
	}

	/**
	 * 生成MD5签名
	 * @param b 字节数组
	 * @return MD5散列码实例
	 */
	public static MD5Hash doMD5Hash(byte[] b) {
		return Laxkit.doMD5Hash(b, 0, b.length);
	}
	
	/**
	 * 将字符串转成UTF8编码，生成MD5签名
	 * @param text 字符串
	 * @return MD5散列码实例
	 */
	public static MD5Hash doMD5Hash(String text) {
		byte[] b = new UTF8().encode(text);
		return Laxkit.doMD5Hash(b);
	}

	/**
	 * 生成一个文件的MD5散列码
	 * @param file 磁盘文件
	 * @return 返回文件的MD5散列码
	 * @throws IOException 读写异常
	 * @throws NoSuchAlgorithmException 无效算法异常
	 */
	public static MD5Hash doMD5Hash(File file) throws IOException, NoSuchAlgorithmException {
		FileInputStream in = new FileInputStream(file);
		MessageDigest md = MessageDigest.getInstance("MD5");

		byte[] b = new byte[10240];
		do {
			int len = in.read(b, 0, b.length);
			if (len < 1) break;
			md.update(b, 0, len);
		} while (true);
		
		in.close();

		return new MD5Hash(md.digest());
	}

	/**
	 * 生成SHA1签名
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return SHA1散列码实例
	 */
	public static SHA1Hash doSHA1Hash(byte[] b, int off, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(b, off, len);
			byte[] sign = md.digest();
			return new SHA1Hash(sign);
		} catch (NoSuchAlgorithmException e) {
			com.laxcus.log.client.Logger.error(e);
		}
		return null;
	}

	/**
	 * 生成SHA1签名
	 * @param b 字节数组
	 * @return SHA1散列码实例
	 */
	public static SHA1Hash doSHA1Hash(byte[] b) {
		return Laxkit.doSHA1Hash(b, 0, b.length);
	}

	/**
	 * 将字符串转成UTF8编码，生成SHA1签名
	 * @param text 字符串
	 * @return SHA1散列码实例
	 */
	public static SHA1Hash doSHA1Hash(String text) {
		byte[] b = new UTF8().encode(text);
		return Laxkit.doSHA1Hash(b);
	}

	/**
	 * 生成一个文件的SHA1散列码
	 * @param file 磁盘文件
	 * @return 返回文件的SHA1散列码
	 * @throws IOException 读写异常
	 * @throws NoSuchAlgorithmException 无效算法异常
	 */
	public static SHA1Hash doSHA1Hash(File file) throws IOException, NoSuchAlgorithmException {
		FileInputStream in = new FileInputStream(file);
		MessageDigest md = MessageDigest.getInstance("SHA-1");

		byte[] b = new byte[10240];
		do {
			int len = in.read(b, 0, b.length);
			if (len < 1) break;
			md.update(b, 0, len);
		} while (true);
		
		in.close();

		return new SHA1Hash(md.digest());
	}
	
	/**
	 * 生成SHA256签名
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return SHA256散列码实例
	 */
	public static SHA256Hash doSHA256Hash(byte[] b, int off, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(b, off, len);
			byte[] sign = md.digest();
			return new SHA256Hash(sign);
		} catch (NoSuchAlgorithmException e) {
			com.laxcus.log.client.Logger.error(e);
		}
		return null;
	}

	/**
	 * 生成SHA256签名
	 * @param b 字节数组
	 * @return SHA256散列码实例
	 */
	public static SHA256Hash doSHA256Hash(byte[] b) {
		return Laxkit.doSHA256Hash(b, 0, b.length);
	}

	/**
	 * 将字符串转成UTF8编码，生成SHA256签名
	 * @param text 字符串
	 * @return SHA256散列码实例
	 */
	public static SHA256Hash doSHA256Hash(String text) {
		byte[] b = new UTF8().encode(text);
		return Laxkit.doSHA256Hash(b);
	}

	/**
	 * 生成一个文件的SHA256散列码
	 * @param file 磁盘文件
	 * @return 返回文件的SHA256散列码
	 * @throws IOException 读写异常
	 * @throws NoSuchAlgorithmException 无效算法异常
	 */
	public static SHA256Hash doSHA256Hash(File file) throws IOException, NoSuchAlgorithmException {
		FileInputStream in = new FileInputStream(file);
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		byte[] b = new byte[10240];
		do {
			int len = in.read(b, 0, b.length);
			if (len < 1) break;
			md.update(b, 0, len);
		} while (true);
		
		in.close();

		return new SHA256Hash(md.digest());
	}
	
	/**
	 * 生成SHA384签名。SHA384是SHA512的简版。
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return SHA384Hash实例
	 */
	public static SHA384Hash doSHA384Hash(byte[] b, int off, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-384");
			md.update(b, off, len);
			byte[] sign = md.digest();
			return new SHA384Hash(sign);
		} catch (NoSuchAlgorithmException e) {
			com.laxcus.log.client.Logger.error(e);
		}
		return null;
	}

	/**
	 * 生成SHA384签名。SHA384是SHA512的简版。
	 * @param b 字节数组
	 * @return SHA384Hash实例
	 */
	public static SHA384Hash doSHA384Hash(byte[] b) {
		return Laxkit.doSHA384Hash(b, 0, b.length);
	}

	/**
	 * 将字符串转成UTF8编码，生成SHA384签名
	 * @param text 字符串
	 * @return SHA384散列码实例
	 */
	public static SHA384Hash doSHA384Hash(String text) {
		byte[] b = new UTF8().encode(text);
		return Laxkit.doSHA384Hash(b);
	}

	/**
	 * 生成SHA512签名
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return SHA512Hash实例
	 */
	public static SHA512Hash doSHA512Hash(byte[] b, int off, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(b, off, len);
			byte[] sign = md.digest();
			return new SHA512Hash(sign);
		} catch (NoSuchAlgorithmException e) {
			com.laxcus.log.client.Logger.error(e);
		}
		return null;
	}

	/**
	 * 生成SHA512签名
	 * @param b 字节数组
	 * @return SHA512Hash实例
	 */
	public static SHA512Hash doSHA512Hash(byte[] b) {
		return Laxkit.doSHA512Hash(b, 0, b.length);
	}

	/**
	 * 将字符串转成UTF8编码，生成SHA512签名
	 * @param text 字符串
	 * @return SHA512散列码实例
	 */
	public static SHA512Hash doSHA512Hash(String text) {
		byte[] b = new UTF8().encode(text);
		return Laxkit.doSHA512Hash(b);
	}

	/**
	 * 生成一个文件的SHA512散列码
	 * @param file 磁盘文件
	 * @return 返回文件的SHA512散列码
	 * @throws IOException 读写异常
	 * @throws NoSuchAlgorithmException 无效算法异常
	 */
	public static SHA512Hash doSHA512Hash(File file) throws IOException, NoSuchAlgorithmException {
		FileInputStream in = new FileInputStream(file);
		MessageDigest md = MessageDigest.getInstance("SHA-512");

		byte[] b = new byte[10240];
		do {
			int len = in.read(b, 0, b.length);
			if (len < 1) break;
			md.update(b, 0, len);
		} while (true);
		
		in.close();

		return new SHA512Hash(md.digest());
	}

	/**
	 * 生成用户签名，判断条件：
	 * 1. 如果字符串空，返回空指针
	 * 2. 如果是16进制的SHA256签名（64个字符串明文），生成签名
	 * 3. 以上不成立，认为是明文，对明文签名
	 * @param input 输入参数
	 * @return 返回签名，或者空指针
	 */
	public static Siger doSiger(String input) {
		if (input == null || input.trim().length() == 0) {
			return null;
		}
		// 判断是16进制的SHA256签名（64个字符串），否则认为是明文
		if (Siger.validate(input)) {
			return new Siger(input);
		} else {
			return SHAUser.doUsername(input);
		}
	}

	/**
	 * 将指定位置的字节数组转为16进制字符串
	 * @param b 字节数组
	 * @param off 指定下标位置
	 * @param len 指字长度
	 * @return 描述16进制数字的字符串
	 */
	public static String itoh(byte[] b, int off, int len) {
		int end = off + len;
		if (off < 0 || len < 1 || off >= b.length || end > b.length) {
			throw new IndexOutOfBoundsException();
		}
		StringBuilder hex = new StringBuilder();
		for (int i = off; i < end; i++) {
			String s = String.format("%x", b[i] & 0xFF);
			if (s.length() == 1) hex.append('0');
			hex.append(s);
		}
		return hex.toString();
	}
	
	/**
	 * 返回16进制字符串
	 * @param b 字节数组
	 * @return 字符串
	 */
	public static String itoh(byte[] b) {
		int len = (b != null ? b.length : 0);
		return itoh(b, 0, len);
	}

//	/**
//	 * 将十六进制字符串转为字节数组。如果存在非法字符，将弹出异常。
//	 * @param hex 16进制字节串
//	 * @return 字节数组
//	 */
//	public static byte[] htoi(String hex) {
//		if (hex == null || hex.length() % 2 != 0) {
//			throw new IllegalArgumentException("invalid hex value");
//		} else if (!hex.matches("^([0-9a-fA-F]{2,})$")) {
//			throw new IllegalArgumentException("invalid hex value");
//		}
//
//		int seek = 0;
//		byte[] b = new byte[hex.length() / 2];
//		for (int i = 0; i < b.length; i++) {
//			String s = hex.substring(seek, seek + 2);
//			b[i] = (byte) Integer.parseInt(s, 16);
//			seek += 2;
//		}
//		return b;
//	}

	/**
	 * 将十六进制字符串转为字节数组。如果存在非法字符，将弹出异常。
	 * @param hex 16进制字节串
	 * @return 字节数组
	 */
	public static byte[] htoi(String hex) {
		if (hex == null || hex.length() < 1) {
			throw new IllegalArgumentException("invalid hex value");
		} else if (!hex.matches("^([0-9a-fA-F]{2,})$")) {
			throw new IllegalArgumentException("invalid hex value");
		}

		// 不足，填充！
		if (hex.length() % 2 != 0) {
			hex = "0" + hex;
		}

		int seek = 0;
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0; i < b.length; i++) {
			String s = hex.substring(seek, seek + 2);
			b[i] = (byte) Integer.parseInt(s, 16);
			seek += 2;
		}
		return b;
	}
	
	/**
	 * 根据传入的三个长整型，计算最大限制长度。<br>
	 * 
	 * @param begin 数据开始下标
	 * @param end 数据结束下标
	 * @param unit 单位尺寸
	 * @return 返回最大限制长度
	 */
	public static long limit(long begin, long end, long unit) {
		return (end - begin > unit ? unit : end - begin);
	}

	/**
	 * 根据传入的三个整型，计算它的最大限制长度。 <br>
	 * 使用这个方法时，整形数值范围要在2G以内，否则会有精度损失。<br>
	 * 
	 * @param begin 数据开始下标
	 * @param end 数据结束下标
	 * @param unit 单位尺寸
	 * @return 返回最大限制长度
	 */
	public static int limit(int begin, int end, int unit) {
		return (end - begin > unit ? unit : end - begin);
	}

	/**
	 * 过滤0字节
	 * @param value 传入的字节数组
	 * @return 过滤后的字节数组
	 */
	private static byte[] compress(byte[] value) {
		int end = value.length - 1;
		for (; end >= 0; end--) {
			if (value[end] != 0) break;
		}
		if (end < 0) end = 0;
		byte[] b = new byte[end + 1];
		System.arraycopy(value, 0, b, 0, b.length);
		return b;
	}

	/**
	 * 把一个短整型转为字节数组输出，占用2个字节，字节数组采用小字头编码（LITTLE-ENDIAN）
	 * 
	 * @param value 短整型
	 * @return 转换后的字节数组
	 */
	public static byte[] toBytes(short value) {
		byte[] b = new byte[2];
		b[0] = (byte) (value & 0xFF);
		b[1] = (byte) ((value >>> 8) & 0xFF);
		return b;
	}

	/**
	 * 把一个短整型转为字节数组输出，转换过程考虑是否采用压缩格式，过滤开始的0字节。字节数组采用小字头编码（LITTLE-ENDIAN）
	 * 
	 * @param value 短整型
	 * @param compress 压缩标记
	 * @return 转换后的字节数组
	 */
	public static byte[] toBytes(short value, boolean compress) {
		byte[] b = toBytes(value);
		if (compress) {
			return Laxkit.compress(b);
		}
		return b;
	}

	/**
	 * 将一个字符转为字节数组输出
	 * 
	 * @param ch 字符
	 * @return 字节数组
	 */
	public static byte[] toBytes(char ch) {
		byte[] b = new byte[2];
		b[0] = (byte) (ch & 0xFF);
		b[1] = (byte) ((ch >>> 8) & 0xFF);
		return b;
	}

	/**
	 * 将一个字节转为字节数组，同时选择是否采用压缩模式
	 * @param ch 字符
	 * @param compress 压缩标记
	 * @return 转换后的字节数组
	 */
	public static byte[] toBytes(char ch, boolean compress) {
		byte[] b = toBytes(ch);
		if (compress) {
			return Laxkit.compress(b);
		}
		return b;
	}

	/**
	 * 从字节数组的指定下标位置读取一个字符
	 * 
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度（大于或者等于2）
	 * @return 一个字符
	 */
	public static char toChar(byte[] b, int off, int len) {
		int end = off + len;
		char ch = (char) (b[off++] & 0xFF);
		if (off < end) ch |= ((b[off++] & 0xFF) << 8);
		return ch;
	}

	/**
	 * 从字节数组的开始位置读取一个字符
	 * 
	 * @param b 字节数组
	 * @return 一个字符
	 */
	public static char toChar(byte[] b) {
		int len = (b.length < 2 ? b.length : 2);
		return Laxkit.toChar(b, 0, len);
	}

	/**
	 * 从字节数组的指定下标位读取一个字符，默认是2个字节
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @return 一个字符
	 */
	public static char toChar(byte[] b, int off) {
		int len = (b.length - off < 2 ? b.length - off : 2);
		return Laxkit.toChar(b, off, len);
	}

	/**
	 * 从字节数组的指定下标位置读取一个短整型值
	 * 
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 短整型
	 */
	public static short toShort(byte[] b, int off, int len) {
		int end = off + len;
		short value = (short) (b[off++] & 0xff);
		if (off < end) value |= ((b[off++] & 0xff) << 8);
		return value;
	}

	/**
	 * 将一个字节数组输出为短整型值
	 * 
	 * @param b 字节数组
	 * @return 短整型
	 */
	public static short toShort(byte[] b) {
		int len = (b.length < 2 ? b.length : 2);
		return Laxkit.toShort(b, 0, len);
	}

	/**
	 * 从字节数组的指定下标位置，把一个字节数组输出为短整型值
	 * 
	 * @param b 字节数组
	 * @param off 下标位置
	 * @return 短整型值
	 */
	public static short toShort(byte[] b, int off) {
		int len = (b.length - off < 2 ? b.length - off : 2);
		return Laxkit.toShort(b, off, len);
	}

	/**
	 * 把一个整型值输出出字节数组，共4个字节。输出采用小字头编码。
	 * 
	 * @param value 整形值
	 * @return 字节数组
	 */
	public static byte[] toBytes(int value) {
		byte[] b = new byte[4];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) ((value >>> 8) & 0xff);
		b[2] = (byte) ((value >>> 16) & 0xff);
		b[3] = (byte) ((value >>> 24) & 0xff);
		return b;
	}

	/**
	 * 把一个整形值输出为字节数组，同时判断采用压缩模式
	 * 
	 * @param value 整形值
	 * @param compress 压缩模式
	 * @return 字节数组
	 */
	public static byte[] toBytes(int value, boolean compress) {
		byte[] b = toBytes(value);
		if (compress) {
			return Laxkit.compress(b);
		}
		return b;
	}

	/**
	 * 将一个字节数组转换为整形值，转换过程采用小字头编码
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 整型值
	 */
	public static int toInteger(byte[] b, int off, int len) {
		int end = off + len;
		int value = b[off++] & 0xFF;
		if (off < end) value |= ((b[off++] & 0xFF) << 8);
		if (off < end) value |= ((b[off++] & 0xFF) << 16);
		if (off < end) value |= ((b[off++] & 0xFF) << 24);
		return value;
	}

	/**
	 * 将字节数组转换为整型值，转换过程采用小字头编码
	 * 
	 * @param b 字节数组
	 * @return 整形值
	 */
	public static int toInteger(byte[] b) {
		int len = (b.length < 4 ? b.length : 4);
		return Laxkit.toInteger(b, 0, len);
	}

	/**
	 * 将指定位置的字节数组转换为整形值，转换过程采用小字头编码
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @return 整形值
	 */
	public static int toInteger(byte[] b, int off) {
		int len = (b.length - off < 4 ? b.length - off : 4);
		return Laxkit.toInteger(b, off, len);
	}

	/**
	 * 将一个长整型值转换为字节数组，转换过程采用小字头编码
	 * 
	 * @param value 长整型
	 * @return 字节数组
	 */
	public static byte[] toBytes(long value) {
		byte[] b = new byte[8];
		b[0] = (byte) (value & 0xFF);
		b[1] = (byte) ((value >>> 8) & 0xFF);
		b[2] = (byte) ((value >>> 16) & 0xFF);
		b[3] = (byte) ((value >>> 24) & 0xFF);
		b[4] = (byte) ((value >>> 32) & 0xFF);
		b[5] = (byte) ((value >>> 40) & 0xFF);
		b[6] = (byte) ((value >>> 48) & 0xFF);
		b[7] = (byte) ((value >>> 56) & 0xFF);
		return b;
	}

	/**
	 * 将长整型值转换为字节数组，转换过程考虑采用压缩格式，输出过程采用小字头编码
	 * 
	 * @param value 长整型
	 * @return 字节数组
	 */
	public static byte[] toBytes(long value, boolean compress) {
		byte[] b = toBytes(value);
		if (compress) {
			return Laxkit.compress(b);
		}
		return b;
	}

	/**
	 * 将字节数组转换为长整型值，转换过程采用小字头编码
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 长整型值
	 */
	public static long toLong(byte[] b, int off, int len) {
		int end = off + len;
		long value = b[off++] & 0xFF;
		if (off < end) value |= ((long) ((b[off++] & 0xFF)) << 8);
		if (off < end) value |= ((long) ((b[off++] & 0xFF)) << 16);
		if (off < end) value |= ((long) ((b[off++] & 0xFF)) << 24);
		if (off < end) value |= ((long) ((b[off++] & 0xFF)) << 32);
		if (off < end) value |= ((long) ((b[off++] & 0xFF)) << 40);
		if (off < end) value |= ((long) ((b[off++] & 0xFF)) << 48);
		if (off < end) value |= ((long) ((b[off++] & 0xFF)) << 56);
		return value;
	}

	/**
	 * 将字节数组转换为长整型值
	 * 
	 * @param b 字节数组
	 * @return 长整型值
	 */
	public static long toLong(byte[] b) {
		int len = (b.length < 8 ? b.length : 8);
		return Laxkit.toLong(b, 0, len);
	}

	/**
	 * 将字节转换为长整数
	 * 
	 * @param b 字节数组
	 * @param off 开始下标
	 * @return 长整型值
	 */
	public static long toLong(byte[] b, int off) {
		int len = (b.length - off < 8 ? b.length - off : 8);
		return Laxkit.toLong(b, off, len);
	}

	/**
	 * 比较字节数组的排列顺序
	 * @param b1 字节数组1
	 * @param b2 字节数组2
	 * @return 字典排列值
	 */
	public static int compareTo(byte[] b1, byte[] b2) {
		if (b1 == null && b2 == null) {
			return 0;
		} else if (b1 == null) {
			return -1;
		} else if (b2 == null) {
			return 1;
		}

		for (int i = 0; i < b1.length && i < b2.length; i++) {
			int ret = Laxkit.compareTo(b1[i], b2[i]);
			if (ret != 0) return ret;
		}
		return b1.length - b2.length;
	}

	/**
	 * 按照升序比较两个字节的排列顺序
	 * @param b1 字节1
	 * @param b2 字节2
	 * @return 字典排序值
	 */
	public static int compareTo(byte b1, byte b2) {
		return b1 - b2;
	}

	/**
	 * 按照升序比较两个布尔值的排列顺序
	 * @param b1 布尔值2
	 * @param b2 布尔值2
	 * @return 排序值
	 */
	public static int compareTo(boolean b1, boolean b2) {
		return (b1 == b2 ? 0 : (b2 ? 1 : -1));
	}

	/**
	 * 按照升序比较两个短整型的排列顺序
	 * @param b1 短整型值1
	 * @param b2 短整型值2
	 * @return 排序值
	 */
	public static int compareTo(short b1, short b2) {
		return (b1 < b2 ? -1 : (b1 > b2 ? 1 : 0));
	}

	/**
	 * 按照升序比较两个整型的排列顺序
	 * @param b1 整型值1
	 * @param b2 整型值2
	 * @return 排序值
	 */
	public static int compareTo(int b1, int b2) {
		return (b1 < b2 ? -1 : (b1 > b2 ? 1 : 0));
	}

	/**
	 * 按照升序比较两个长整型的排列顺序
	 * @param b1 长整型值1
	 * @param b2 长整型值2
	 * @return 排序值
	 */
	public static int compareTo(long b1, long b2) {
		return (b1 < b2 ? -1 : (b1 > b2 ? 1 : 0));
	}

	/**
	 * 按照升序比较两个单浮点数的排列顺序
	 * @param b1 单浮点值1
	 * @param b2 单浮点值2
	 * @return 排序值
	 */
	public static int compareTo(float b1, float b2) {
		return (b1 < b2 ? -1 : (b1 > b2 ? 1 : 0));
	}

	/**
	 * 按照升序比较两个双浮点数的排列顺序
	 * @param b1 双浮点值1
	 * @param b2 双浮点值1
	 * @return 排序值
	 */
	public static int compareTo(double b1, double b2) {
		return (b1 < b2 ? -1 : (b1 > b2 ? 1 : 0));
	}

	/**
	 * 按照对象排序要求比较两个对象的排序
	 * 
	 * @param <T> 类类型
	 * @param b1 对象1
	 * @param b2 对象2
	 * @return 返回对象排序值
	 */
	@SuppressWarnings("unchecked")
	public static <T> int compareTo(Comparable<? super T> b1, Comparable<? super T> b2) {
		if (b1 == b2) { // 同一个地址，包括空指针时...
			return 0;
		} else if (b1 == null) {
			return -1;
		} else if (b2 == null) {
			return 1;
		} else {
			return b1.compareTo((T) b2);
		}
	}

	/**
	 * 以大小敏感可选的方式，对两个字符串按照字典序列进行排序
	 * @param s1 字符串1
	 * @param s2 字符串2
	 * @param sentient 大小敏感
	 * @return 返回字符串的字典排序值
	 */
	public static int compareTo(String s1, String s2, boolean sentient) {
		if (s1 == s2) { // 同一个地址，包括空指针时...
			return 0;
		} else if (s1 == null) {
			return -1;
		} else if (s2 == null) {
			return 1;
		} else {
			// 区分大小写敏感
			return (sentient ? s1.compareTo(s2) : s1.compareToIgnoreCase(s2));
		}
	}

	/**
	 * 以大小写敏感的方式，对两个字符串排序
	 * @param b1 字符串1
	 * @param b2 字符串2
	 * @return 返回字符串的字典排序值
	 */
	public static int compareTo(String b1, String b2) {
		return Laxkit.compareTo(b1, b2, true);
	}

	/**
	 * 判断字节数组是空值（两个条件：空指针或者数组0长度）
	 * @param b 字节数组
	 * @return 返回真或者假
	 */
	public static boolean isEmpty(byte[] b) {
		return b == null || b.length == 0;
	}

	/**
	 * 判断字符数组是空值（两个条件：空指针或者数组0长度）
	 * @param b 字符数组
	 * @return 返回真或者假
	 */
	public static boolean isEmpty(char[] b) {
		return b == null || b.length == 0;
	}

	/**
	 * 判断短整型数组是空值（两个条件：空指针或者数组0长度）
	 * @param b 短整型数组
	 * @return 返回真或者假
	 */
	public static boolean isEmpty(short[] b) {
		return b == null || b.length == 0;
	}

	/**
	 * 判断整型数组是空值（两个条件：空指针或者数组0长度）
	 * @param b 整形数组
	 * @return 返回真或者假
	 */
	public static boolean isEmpty(int[] b) {
		return b == null || b.length == 0;
	}

	/**
	 * 判断长整型数组是空值（两个条件：空指针或者数组0长度）
	 * @param b 长整型数组
	 * @return 返回真或者假
	 */
	public static boolean isEmpty(long[] b) {
		return b == null || b.length == 0;
	}

	/**
	 * 判断单浮点数组是空值（两个条件：空指针或者数组0长度）
	 * @param b 单浮点数组
	 * @return 返回真或者假
	 */
	public static boolean isEmpty(float[] b) {
		return b == null || b.length == 0;
	}

	/**
	 * 判断双浮点数组是空值（两个条件：空指针或者数组0长度）
	 * 
	 * @param b 双浮点数组
	 * @return 返回真或者假
	 */
	public static boolean isEmpty(double[] b) {
		return b == null || b.length == 0;
	}

	/**
	 * 判断对象数组是空值（两个条件：空指针或者数组0长度）
	 * 
	 * @param b 对象数组
	 * @return 返回真或者假
	 */
	public static boolean isEmpty(Object[] b) {
		return b == null || b.length == 0;
	}

	/**
	 * 判断对象是空指针。<br>
	 * 如果是，弹出NullPointerException异常
	 * 
	 * @param e 对象实例
	 */
	public static void nullabled(Object e) {
		if (e == null) {
			throw new NullPointerException();
		}
	}
	
	/**
	 * 取文件的规范路径，否则是绝对路径
	 * @param file File实例
	 * @return 字符串文件
	 */
	public static String canonical(File file) {
		try {
			return file.getCanonicalPath();
		} catch(IOException e) {
			return file.getAbsolutePath();
		}
	}
	
	/**
	 * 判断有这个文件
	 * @param filename 文件全路径
	 * @return 返回真或者假
	 */
	public static boolean hasFile(String filename) {
		File file = new File(filename);
		return (file.exists() && file.isFile());
	}
	
	/**
	 * 忽略两侧空格！
	 * @param input 输入文本
	 * @return 格式化文本
	 */
	public static String trim(String input) {
		final String regex = "^\\s*([\\w\\W]+)\\s*$";
		// 正则表达式语法
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		// 判断匹配
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return input;
	}
	
	/**
	 * 日期时间转换成字符串表述
	 * @param d Date实例
	 * @return 字符串
	 */
	public static String toString(java.util.Date d) {
		return DateFormat.getDateTimeInstance().format(d);
	}
	
//	/**
//	 * 从容器中查找指定的第一个组件！
//	 * 
//	 * @param <T>
//	 * @param container 容器实例
//	 * @param clazz 对象类
//	 * @param index 指定下标，从0开始
//	 * @return 关联对象类，或者空指针
//	 */
//	@SuppressWarnings("unchecked")
//	public static <T> T findComponent(Container container, Class<?> clazz, int index) {
//		if (container == null) {
//			return null;
//		}
//		// 逐个检测
//		Component[] objects = container.getComponents();
//		int size = (objects != null && objects.length > 0 ? objects.length : 0);
//		int count = 0;
//		for (int i = 0; i < size; i++) {
//			Component object = objects[i];
//			if (object.getClass() == clazz) {
//				if (count == index) {
//					return (T) object;
//				} else {
//					count++;
//				}
//			} else if (Laxkit.isClassFrom(object, Container.class)) {
//				Object o = Laxkit.findComponent((Container) object, clazz, index);
//				if (o != null) {
//					return (T) o;
//				}
//			}
//		}
//		return null;
//	}
//	
//	/**
//	 * 查找第一个下标组件
//	 * @param <T>
//	 * @param container 容器
//	 * @param clazz 对象类
//	 * @return 返回关联对象类，没有是空指针
//	 */
//	public static <T> T findComponent(Container container, Class<?> clazz) {
//		return Laxkit.findComponent(container, clazz, 0);
//	}
	
	
//	/**
//	 * 输出16进制字符串
//	 * @param b
//	 * @return
//	 */
//	public static String toHexString(byte[] b) {
//		StringBuilder bf = new StringBuilder();
//		int len = (b != null ? b.length : 0);
//		for (int i = 0; i < len; i++) {
//			String s = String.format("%X", b[i]);
//			if (s.length() == 1) {
//				bf.append("0" + s);
//			} else {
//				bf.append(s);
//			}
//		}
//		return bf.toString();
//	}
	

	
}