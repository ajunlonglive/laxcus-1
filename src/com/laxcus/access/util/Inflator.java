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
 * 数据压缩生成器，包括GZIP、ZIP、deflate。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/19/2009
 * @since laxcus 1.0
 */
public class Inflator {

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
	 * GZIP算法压缩数据
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 压缩后的字节数组
	 */
	public static byte[] gzip(byte[] b, int off, int len) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(Inflator.fmsize(len));
		GZIPOutputStream gzip = new GZIPOutputStream(buff);
		gzip.write(b, off, len);
		gzip.finish();
		gzip.close();

		return buff.toByteArray();
	}

	/**
	 * ZIP算法压缩数据
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 压缩后的字节数组
	 */
	public static byte[] zip(byte[] b, int off, int len) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(Inflator.fmsize(len));
		ZipOutputStream zip = new ZipOutputStream(buff);
		ZipEntry entry = new ZipEntry("default");
		zip.putNextEntry(entry);

		zip.write(b, off, len);
		zip.finish();
		zip.close();

		return buff.toByteArray();
	}

	/**
	 * 使用INFLATE压缩数据
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 压缩后的字节数组
	 */
	public static byte[] inflate(byte[] b, int off, int len) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(Inflator.fmsize(len));

		DeflaterOutputStream zip = new DeflaterOutputStream(buff);
		zip.write(b, off, len);
		zip.finish();
		zip.close();

		return buff.toByteArray();
	}

}