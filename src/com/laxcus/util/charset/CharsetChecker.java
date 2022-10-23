/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.charset;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;

/**
 * 磁盘文件编码检测器  <br><br>
 * 
 * 启动时，检测JVM所在国家/地区，如果是东亚国家/地区，中日韩台湾香港，在读取字符集时，
 * 过滤非必要的的字符集，这些字符集是以“X-/IBM/WINDOWS/ISO”为前缀命名。
 * 
 * 判断依据：<br>
 * 1. 当从UNICODE编码向某个字符集转换时，如果在该字符集中没有对应的编码，则到0x3f（即问号字符?），或者不在Character规定字符集里 <br>
 * 2. 当其他字节集向UNICODE编码转换时，如果这个二进制数在该字符集中没有任何的字符，则得到的结果是 0XFFFD <br><br>
 * 
 * @author scott.liang
 * @version 1.0 5/17/2019
 * @since laxcus 1.0
 */
public class CharsetChecker {

	/**
	 * 乱码统计
	 *
	 * @author scott.liang
	 * @version 1.0 5/17/2019
	 * @since laxcus 1.0
	 */
	public class Messy implements Comparable<Messy> {
		String charset;

		int count;

		/**
		 * 构造乱码实例 
		 * @param w
		 * @param c
		 */
		public Messy(String w, int c) {
			charset = w;
			count = c;
		}

		public String getCharset() {
			return charset;
		}

		public int getCount() {
			return count;
		}

		/*
		 * 采用升序排序。小数在前，大数在后。
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Messy that) {
			return (count < that.count ? -1 : (count > that.count ? 1 : 0));
		}
	}
	
	/** 屏蔽 **/
	private boolean disabled;

	/**
	 * 构造默认的磁盘文件编码检测器
	 */
	public CharsetChecker() {
		super();
		checkDisabledCharset();
//		printEnv();
	}
	
//	private void printEnv() {
//		Map<String, String> es = System.getenv();
//		Iterator<Map.Entry<String, String>> ts = es.entrySet().iterator();
//		while(ts.hasNext()) {
//			Map.Entry<String, String> entry = ts.next();
//			System.out.printf("%s - %s\n",entry.getKey(),entry.getValue());
//		}
//		System.out.println("-------------");
//		
//		Properties ps = System.getProperties();
//		Iterator<Map.Entry<Object, Object>> ns = ps.entrySet().iterator();
//		while(ns.hasNext()) {
//			Map.Entry<Object, Object> entry = ns.next();
//			System.out.printf("%s - %s\n",entry.getKey(),entry.getValue());
//		}
//	}

	/**
	 * 判断国家，是否启动屏蔽
	 * 目前定义：CN/TW/HK/JP/KR，执行屏蔽处理
	 */
	private void checkDisabledCharset() {
		String value = System.getProperty("user.country");
		disabled = (value != null && value.matches("^\\s*(?i)(CN|TW|HK|JP|KR)\\s*$"));
	}
	
	/**
	 * 判断用户如果是东亚国家，检查需要屏蔽的字符集
	 * 以“IBM/ISO/WINDOWS/X-/TIS/JIS/KOI”为前缀的字符集进行屏蔽. KOI，西里尔; TIS，泰国
	 * @param charset 字符集
	 * @return 返回真或者否
	 */
	private boolean isDisabledCharset(String charset) {
		if (disabled && charset != null) {
			return charset.matches("^\\s*(?i)(IBM|ISO|X-|WINDOWS|TIS|JIS|KOI)(.*?)\\s*$");
		}
		return false;
	}

	/**
	 * 返回全部字符集，包括LAXCUS本地定义有限几个常用字符集，和系统提供的字节集
	 * @return 字符串数组
	 */
	private List<String> getCharsets(boolean full) {
		ArrayList<String> array = new ArrayList<String>();
		// 取本地定义的字符集
		String[] all = CharsetType.getStrings();
		for (String charset : all) {
			array.add(charset);
		}
		// 取系统中定义的字符集
		if (full) {
			SortedMap<String, java.nio.charset.Charset> map = java.nio.charset.Charset.availableCharsets();
			for (String charset : map.keySet()) {
				// 是需要屏蔽的字符集，忽略它！
				if (isDisabledCharset(charset)) {
					continue;
				}
				// 不保存，保存字符集
				if (!array.contains(charset)) {
					array.add(charset);
				}
			}
		}
		// 输出全部
		return array;
	}

	/**
	 * 用系统中全部字符集扫描磁盘文件。乱码最少的那个字符集即是与文件匹配的字符集。
	 * @param file 磁盘文件
	 * @return 返回最可能匹配的字符集
	 */
	public Messy[] calculate(File file) {
		// 判断文件有效
		if (file == null) {
			throw new NullPointerException("file is null pointer!");
		} else if (!(file.exists() && file.isFile())) {
			return null;
		}

		ArrayList<Messy> array = new ArrayList<Messy>();

		// 1. 取全部字符集
		List<String> set = getCharsets(true);
		// 2. 判断文件和每个字符集匹配程度
		for (String charset : set) {
			try {
				int count = check(file, charset);
				Messy e = new Messy(charset, count);
				array.add(e);
			} catch (IOException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
		}

		// 排序
		Collections.sort(array);

		// 输出全部结果
		Messy[] a = new Messy[array.size()];
		return array.toArray(a);
	}

	/**
	 * 用系统中全部字符集扫描磁盘文件。乱码最少的那个字符集即是与文件匹配的字符集。
	 * @param file 磁盘文件
	 * @return 返回最可能匹配的字符集
	 */
	public String check(File file) {
		// 判断文件有效
		if (file == null) {
			throw new NullPointerException("file is null pointer!");
		} else if (!(file.exists() && file.isFile())) {
			return null;
		}

		// 1. 取全部字符集
		List<String> set = getCharsets(false);

		// 2. 判断文件和每个字符集匹配程度
		for (String charset : set) {
			try {
				int count = check(file, charset);
//				Logger.warning(this, "check", "%s error count %d", charset, count);
				// 如果没有乱码，就认定是这个字符集
				if (count == 0) {
					return charset;
				}
			} catch (IOException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
		}

		// 否则返回空指针
		return null;
	}

	/**
	 * 判断有BOM且一致
	 * @param file 磁盘文件
	 * @param bom BOM符号
	 * @return 返回真或者假
	 * @throws IOException
	 */
	private boolean matchs(File file, byte[] bom) throws IOException {
		byte[] b = new byte[bom.length];
		if (file.length() < b.length) {
			return false;
		}

		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();

		for (int i = 0; i < b.length; i++) {
			if (b[i] != bom[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是错误码
	 * @param c 字符
	 * @return 返回真或者假
	 */
	private final boolean isFaultCode(char c) {
		return ((int) c) == 0xfffd;
	}

	/**
	 * 判断是乱码。<br><br>
	 * 
	 * 两个判断，任何一个成立就是乱码。<br>
	 * 1. 是规定的错误码 <br>
	 * 2. 不是UNICODE规定的编码<br><br>
	 * 
	 * @param c 字符
	 * @return  返回真或者假
	 */
	private final boolean isMessy(char c) {
		// 是错误码
		if (isFaultCode(c)) {
			return true;
		}
		// 不在UNICODE定义
		return !Character.isDefined(c);
	}

	/**
	 * 用指定的字符集打开文件
	 * @param file 磁盘文件
	 * @param charset 字符集
	 * @return 返回乱码统计数
	 * @throws IOException
	 */
	private int check(File file, String charset) throws IOException {
		int skip = 0;
		// 取出BOM字节
		byte[] bom = BOM.find(charset);
		if (bom != null) {
			boolean success = (matchs(file, bom));
			if (success) {
				skip = bom.length;
			}
		}

		// 打开文件
		FileInputStream in = new FileInputStream(file);
		// 跨过前面的字节
		if (skip > 0) {
			in.skip(skip);
		}
		InputStreamReader is = new InputStreamReader(in, charset);
		// 放进缓冲
		BufferedReader reader = new BufferedReader(is);

		int count = 0;
		do {
			int w = reader.read();
			// 达到结尾，退出！
			if(w == -1) {
				break;
			}
			char word = (char)w;
			// 判断是乱码
			if(isMessy(word)) {
				count++;
			}
		} while(true);

		reader.close();
		is.close();
		in.close();

		// 返回统计的乱码数
		return count;
	}

	/**
	 * 判断有BOM且一致
	 * @param file 磁盘文件
	 * @param bom BOM符号
	 * @return 返回真或者假
	 * @throws IOException
	 */
	private boolean matchs(byte[] content, byte[] bom) throws IOException {
		byte[] b = new byte[bom.length];
		if (content.length < b.length) {
			return false;
		}

		for (int i = 0; i < b.length; i++) {
			if (content[i] != bom[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 用指定的字符集打开文件
	 * @param file 磁盘文件
	 * @param charset 字符集
	 * @return 返回乱码统计数
	 * @throws IOException
	 */
	private int check(byte[] content, String charset) throws IOException {
		int skip = 0;
		// 取出BOM字节
		byte[] bom = BOM.find(charset);
		if (bom != null) {
			boolean success = (matchs(content, bom));
			if (success) {
				skip = bom.length;
			}
		}

		// 打开文件
//		FileInputStream in = new FileInputStream(file);
//		// 跨过前面的字节
//		if (skip > 0) {
//			in.skip(skip);
//		}
		
		// 跨过前面的字节
		ByteArrayInputStream in = new ByteArrayInputStream(content, skip, content.length - skip);
		InputStreamReader is = new InputStreamReader(in, charset);
		// 放进缓冲
		BufferedReader reader = new BufferedReader(is);

		int count = 0;
		do {
			int w = reader.read();
			// 达到结尾，退出！
			if(w == -1) {
				break;
			}
			char word = (char)w;
			// 判断是乱码
			if(isMessy(word)) {
				count++;
			}
		} while(true);

		reader.close();
		is.close();
		in.close();

		// 返回统计的乱码数
		return count;
	}
	
	/**
	 * 用系统中全部字符集扫描磁盘文件。乱码最少的那个字符集即是与文件匹配的字符集。
	 * @param content 磁盘文件
	 * @return 返回最可能匹配的字符集
	 */
	public String check(byte[] content) {
		// 判断是空集合
		if (content == null || content.length < 1) {
			throw new NullPointerException("empty content!");
		}

		// 1. 取全部字符集
		List<String> set = getCharsets(false);

		// 2. 判断文件和每个字符集匹配程度
		for (String charset : set) {
			try {
				int count = check(content, charset);
//				Logger.warning(this, "check", "%s error count %d", charset, count);
				// 如果没有乱码，就认定是这个字符集
				if (count == 0) {
					return charset;
				}
			} catch (IOException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
		}

		// 否则返回空指针
		return null;
	}

	//	public void test() {
	//		java.util.SortedMap<String, java.nio.charset.Charset> map =	java.nio.charset.Charset.availableCharsets();
	//		java.util.Set<String> names =	map.keySet();
	//		for (String name : names) {
	//			java.nio.charset.Charset value = map.get(name);
	//			System.out.printf("%s - %s\n", name, value.name());
	//		}
	//	}
	//
	//	public static void main(String[] args) {
	//		CharsetChecker e = new CharsetChecker();
	//		//			e.test();
	//
	//		//			File file = new File("d:\\downloads\\csv.csv");
	//		//			String charset = e.check(file);
	//		//			System.out.printf("result is %s\n", charset);
	//		//	
	//		//			file = new File("d:\\downloads\\utf8.csv");
	//		//			charset = e.check(file);
	//		//			System.out.printf("result is %s\n", charset);
	//
	//		File file = new File("d:\\downloads\\enterprise(1).csv");
	//		String charset = e.check(file);
	//		System.out.printf("result is %s\n", charset);
	//	}

}
