/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.util;

import java.io.*;
import java.util.zip.*;

/**
 * 数据解压缩处理器，包括GZIP、ZIP、deflate。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/19/2009
 * @since laxcus 1.0
 */
public class Deflator {

	/**
	 * 以32为单位，规范内存长度
	 * @param len 输入长度
	 * @return 返回长度
	 */
	private static int fmsize(int len) {
		int left = len % 32;
		if (left != 0) len = len - left + 32;
		return len;
	}

	/**
	 * 使用GZIP算法解压数据
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解压后的字节数组
	 */
	public static byte[] gzip(byte[] b, int off, int len) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(Deflator.fmsize(len));
		byte[] data = new byte[1024];

		ByteArrayInputStream in = new ByteArrayInputStream(b, off, len);
		GZIPInputStream gzip = new GZIPInputStream(in);
		do {
			int size = gzip.read(data, 0, data.length);
			if (size < 1) break;
			buff.write(data, 0, size);
		} while (true);
		gzip.close();
		in.close();

		return buff.toByteArray();
	}
	
	/**
	 * 使用ZIP算法解压数据
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解压后的字节数组
	 */
	public static byte[] zip(byte[] b, int off, int len) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(Deflator.fmsize(len));
		byte[] data = new byte[1024];

		ByteArrayInputStream in = new ByteArrayInputStream(b, off, len);
		ZipInputStream zip = new ZipInputStream(in);

		while (zip.getNextEntry() != null) {
			while (true) {
				int size = zip.read(data, 0, data.length);
				if (size <= 0) break;
				buff.write(data, 0, size);
			}
		}

		zip.close();
		in.close();
		return buff.toByteArray();
	}

	/**
	 * 使用INFLATE算法解压数据
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解压后的字节数组
	 **/
	public static byte[] deflate(byte[] b, int off, int len) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(Deflator.fmsize(len));
		byte[] data = new byte[1024];

		ByteArrayInputStream in = new ByteArrayInputStream(b, off, len);
		InflaterInputStream inflate = new InflaterInputStream(in);
		do {
			int size = inflate.read(data, 0, data.length);
			if (size <= 0) break;
			buff.write(data, 0, size);
		} while (true);
		inflate.close();
		in.close();

		return buff.toByteArray();
	}

}