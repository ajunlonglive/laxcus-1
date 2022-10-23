/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.casket;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 数据存取封装包 <BR>
 * 
 * 数据存取封装包的子类包含一个数据处理命令，它将转换成字节流，通过JNI接口传递给存储层，并由存储层来解析和执行。<br>
 * 
 * 设计说明：<br>
 * 在数据存取层，为防止单个任务长时间独占数据资源，保证多任务同时执行数据处理，体现数据存取的公平性，以及减少单个任务对内存和磁盘的战胜，避免机器资源造成内存溢出等几个原因，每个任务每次只能操作一个数据块。<br><br>
 * 
 * 表现：<br>
 * 数据处理结果可以写入内存或者指定的磁盘文件。如果数据写入磁盘文件，而且文件已经存在时，数据以追加方式写在文件末尾。<br><br>
 * 
 * 单次小批量但是连续的数据操作，是保证数据存取层稳定运行的基础。本质上，这实际仍然是一种以时间换空间的行为。
 * 
 * @author scott.liang
 * @version 1.12 8/31/2015
 * @since laxcus 1.0
 */
public abstract class AccessCasket { 

	/** 存储数据的文件名。全ASCII编码 **/
	private String file;

	/**
	 * 构造默认的SQL数据存取封装包
	 */
	protected AccessCasket() {
		super();
	}

	/**
	 * 判断数据写入磁盘
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return file != null;
	}

	/**
	 * 设置数据存储的文件名。与本地系统关联，目前是LINUX系统格式。
	 * @param e File实例
	 */
	public void setFile(File e) {
		try {
			setFile(e.getCanonicalPath()); // 规范格式
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 设置数据存储文件名
	 * @param e 字符串文件名称
	 */
	public void setFile(String e) {
		file = e;
	}

	/**
	 * 返回数据存储文件名
	 * @return 字符串文件名称
	 */
	public String getFile() {
		return file;
	}

	/**
	 * 生成数据流
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 将数据存取封装包写入可类化数据存储器。
	 * @param writer 可类化数据存储器
	 * @return 写入的字节尺寸
	 */
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 磁盘文件
		byte[] b = (file == null ? null : file.getBytes());
		int len = (b == null ? 0 : b.length);
		// 文件长度
		writer.writeInt(len);
		// 文件字节
		if (len > 0) {
			writer.write(b);
		}

		// 写入子类参数
		buildSuffix(writer);
		// 返回写入字节数目
		return writer.size() - size;
	}

	/**
	 * 将子类参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

}