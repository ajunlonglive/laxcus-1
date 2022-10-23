/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.dict;

import java.io.*;

import com.laxcus.util.*;

/**
 * 账号文件
 * 包括文件编号和文件两个参数
 * 
 * @author scott.liang
 * @version 1.0 7/4/2018
 * @since laxcus 1.0
 */
public class AccountFile {

	/** 文件编号 **/
	private int no;

	/** 文件实例 **/
	private File file;

	/**
	 * 构造账号文件，指定参数
	 * @param no 文件编号
	 * @param file 文件实例
	 */
	public AccountFile(int no, File file) {
		super();
		setNo(no);
		setFile(file);
	}

	/**
	 * 设置文件编号
	 * @param e int实例
	 */
	public void setNo(int e) {
		no = e;
	}

	/**
	 * 返回文件编号
	 * @return int实例
	 */
	public int getNo() {
		return no;
	}

	/**
	 * 返回磁盘文件
	 * @return 磁盘文件
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 设置磁盘文件
	 * @param e 磁盘文件
	 */
	public void setFile(File e) {
		Laxkit.nullabled(e);
		file = e;
	}

	/**
	 * 判断数据块最大长度溢出
	 * @param blockSize 数据块长度
	 * @return 返回真或者假
	 */
	public boolean isBlockout(int blockSize) {
		return file.length() >= blockSize;
	}

	/**
	 * 向文件尾部追加数据
	 * @param b 字节数组
	 * @return 返回磁盘坐标
	 * @throws IOException
	 */
	public DiskDock append(byte[] b) throws IOException {
		// 确定文件的下标
		int offset = (int) (file.exists() ? file.length() : 0);

		// 数据以追加方式写入磁盘
		FileOutputStream writer = new FileOutputStream(file, true);
		writer.write(b);
		writer.flush();
		writer.close();

		// 返回坐标位置
		return new DiskDock(no, offset, b.length);
	}
}