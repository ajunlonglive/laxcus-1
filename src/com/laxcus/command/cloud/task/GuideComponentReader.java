/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.task;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;

/**
 * 引导任务组件读取器。<br>
 * 引导任务负责启动，包括提供参数和分配分布命令，是单个“.gtc”后缀的文件。
 * 
 * @author scott.liang
 * @version 1.0 4/14/2020
 * @since laxcus 1.0
 */
public class GuideComponentReader {

	/** 数据内容，以字节形式保存 **/
	private byte[] content;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		// 释放内存！
		content = null;
	}

	/**
	 * 构造分布组件集读取器，指定内容
	 * @param b 字节内容
	 * @param off 下标位置
	 * @param len 指定度
	 */
	public GuideComponentReader(byte[] b , int off, int len) {
		super();
		setContent(b, off, len);
	}

	/**
	 * 构造分布组件集读取器，指定内容
	 * @param b 字节内容
	 */
	public GuideComponentReader(byte[] b) {
		super();
		setContent(b);
	}

	/**
	 * 构造分布组件集读取器，读取磁盘文件内容
	 * @param file 文件实例
	 * @throws IOException
	 */
	public GuideComponentReader(File file) throws IOException {
		super();
		readContent(file);
	}

	/**
	 * 设置字节内容
	 * @param b
	 */
	private void setContent(byte[] b) {
		// 判断是空指针
		if (Laxkit.isEmpty(b)) {
			throw new NullPointerException();
		}
		setContent(b, 0, b.length);
	}

	/**
	 * 设置字节内容
	 * @param b
	 */
	private void setContent(byte[] b, int off, int len) {
		// 判断是空指针
		if (Laxkit.isEmpty(b) || len == 0) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || off > b.length
				|| (off + len > b.length) || (off + len < 0)) {
			throw new IndexOutOfBoundsException();
		}
		// 复制内容
		content = Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 输出字节内容
	 * @return 返回字节数组
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * 从磁盘读取字节内容
	 * @param file
	 * @throws IOException
	 */
	private void readContent(File file) throws IOException {
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			throw new FileNotFoundException(file.toString());
		}
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();
		setContent(b);
	}

	/**
	 * 检测和读取内容
	 * @return 返回计取的单元数目。成功是大于等于0，否则是-1。
	 */
	public int check() {
		try {
			int count = 0;
			// 读内容
			ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
			ZipInputStream jin = new ZipInputStream(bin);
			while (true) {
				ZipEntry entry = jin.getNextEntry();
				if (entry == null) {
					break;
				}

				// 只读文件，如果是目录，忽略！
				if (entry.isDirectory()) {
					continue;
				}

				// 读取字节数组
				byte[] b = new byte[1024];
				while (true) {
					int len = jin.read(b, 0, b.length);
					if (len == -1) break;
				}

				// 成功，统计加1
				count++;
			}

			jin.close();
			bin.close();
			return count;
		} catch (IOException e) {
			Logger.error(e);
		}

		return -1;
	}

	/**
	 * 读取包里面的“GUIDE-INF/guides.xml”配置文本
	 * @return 字节数组
	 */
	public byte[] readGuideText() throws IOException {
		byte[] configure = null;

		// 读内容
		ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
		ZipInputStream jin = new ZipInputStream(bin);
		while (true) {
			ZipEntry entry = jin.getNextEntry();
			if (entry == null) {
				break;
			}

			// 只读文件，如果是目录，忽略！
			if (entry.isDirectory()) {
				continue;
			}

			// 判断匹配
			String name = entry.getName();
			if(!name.equalsIgnoreCase(TF.GUIDE_INF)) { 
				continue;
			}

			// 读取字节数组
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			while (true) {
				int len = jin.read(b, 0, b.length);
				if (len == -1) break;
				out.write(b, 0, len);
			}
			// 输出为字节
			configure = out.toByteArray();

			break;
		}
		jin.close();
		bin.close();

		return configure;
	}
	
	/**
	 * 解析版本标签 
	 * @return 返回有效标识
	 */
	public WareTag readWareTag() {
		byte[] configure = null;
		try {
			configure = readGuideText();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 没有找到，警告！忽略！
		if (Laxkit.isEmpty(configure)) {
			Logger.debug(this, "readWareTag", "null pointer");
			return null;
		}

		GuideConfigReader reader = new GuideConfigReader(configure);
		return reader.readWareTag();
	}

//	public static void main(String[] args) {
//		File file = new File("E:/laxcus/backup/demo_fork.dtc");
//		try {
//			GuideComponentReader reader = new GuideComponentReader(file);
//			WareTag tag = reader.readWareTag();
//			System.out.println(tag);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

}