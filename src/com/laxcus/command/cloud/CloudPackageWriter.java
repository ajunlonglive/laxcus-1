/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.io.*;
import java.util.zip.*;

import com.laxcus.util.*;

/**
 * 云应用包写入器。
 * 生成一个ZIP格式的数据包，包括了JAR和其它格式的文件
 * 
 * @author scott.liang
 * @version 1.0 4/5/2020
 * @since laxcus 1.0
 */
public class CloudPackageWriter {

	/** ZIP流 **/
	private ZipOutputStream zos;
	
	/** 缓存流 **/
	private ByteArrayOutputStream buff;
	
	/**
	 * 云应用包写入器，指定缓存长度
	 * @param bufflen 字节缓存长度
	 */
	public CloudPackageWriter(int bufflen) {
		super();
		init(bufflen);
	}
	
	/**
	 * 构造默认的云应用包写入器
	 */
	public CloudPackageWriter() {
		this(10240);
	}
	
	/**
	 * 初始化缓存
	 */
	private void init(int len) {
		if (len < 1024) {
			len = 1024;
		}
		buff = new ByteArrayOutputStream(len);
		zos = new ZipOutputStream(buff);
	}
	
	/**
	 * 写入成员中的全部文件
	 * @param zos
	 * @param element
	 * @throws IOException
	 */
	public void writeElement(CloudPackageElement element) throws IOException {
		// 写入文件到目录
		writeFile(element.getMark(), element.getBoot());
		for (FileKey key : element.getAssists()) {
			writeFile(element.getMark(), key);
		}
		for (FileKey key : element.getLibraries()) {
			writeFile(element.getMark(), key);
		}
	}
	
	/**
	 * LOGO图标是固定参数
	 * @param zos ZIP输出流
	 * @param tag 标签
	 * @param key 文件键值
	 * @throws IOException
	 */
	private void writeReadmeLogo(String tag, FileKey key) throws IOException {
		File file = new File(key.getPath());
		// 名称
		String name = file.getName();
		int last = name.lastIndexOf(".");
		String suffix = name.substring(last + 1);
		name = String.format("logo.%s", suffix);
		
		// 存在标记！
		if (tag != null) {
			name = String.format("%s/%s", tag, name);
		}

		ZipEntry entry = new ZipEntry(name);
		entry.setTime(System.currentTimeMillis());
		zos.putNextEntry(entry);
		byte[] b = new byte[10240];
		FileInputStream in = new FileInputStream(file);
		do {
			int len = in.read(b);
			if(len < 0) break;
			// 写入文件
			zos.write(b, 0, len);
		} while(true);

		in.close();
		zos.closeEntry();
	}
	
	/**
	 * LICENCE是可选参数
	 * 
	 * @param zos ZIP输出流
	 * @param tag 标签
	 * @param key 文件键值
	 * @throws IOException
	 */
	private void writeReadmeLicence(String tag, FileKey key) throws IOException {
		// 不存在，忽略它！
		if (key == null) {
			return;
		}
		File file = new File(key.getPath());
		// 名称
		String name = file.getName();
		int last = name.lastIndexOf(".");
		String suffix = name.substring(last + 1);
		name = String.format("licence.%s", suffix);
		
		// 存在标记！
		if (tag != null) {
			name = String.format("%s/%s", tag, name);
		}

		ZipEntry entry = new ZipEntry(name);
		entry.setTime(System.currentTimeMillis());
		zos.putNextEntry(entry);
		byte[] b = new byte[10240];
		FileInputStream in = new FileInputStream(file);
		do {
			int len = in.read(b);
			if(len < 0) break;
			// 写入文件
			zos.write(b, 0, len);
		} while(true);

		in.close();
		zos.closeEntry();
	}
	
	/**
	 * 写入“Readme”信息
	 * @param zos 
	 * @param element
	 * @throws IOException
	 */
	public void writeReadme(ReadmePackageElement element) throws IOException {
		if (element == null) {
			return;
		}
		// LOGO写入文件到目录
		writeReadmeLogo(element.getMark(), element.getLogo());
		// LICENCE文件写入目录
		writeReadmeLicence(element.getMark(), element.getLicence());

		// 附件文件
		for (FileKey key : element.getAssists()) {
			writeFile(element.getMark(), key);
		}
	}
	
	/**
	 * 定入单个文件
	 * @param zos
	 * @param tag
	 * @param key
	 * @throws IOException
	 */
	private void writeFile(String tag, FileKey key) throws IOException {
		File file = new File(key.getPath());
		// 名称
		String name = file.getName();
		if (tag != null) {
			name = String.format("%s/%s", tag, file.getName());
		}

		ZipEntry entry = new ZipEntry(name);
		entry.setTime(System.currentTimeMillis());
		zos.putNextEntry(entry);
		byte[] b = new byte[10240];
		FileInputStream in = new FileInputStream(file);
		do {
			int len = in.read(b);
			if(len < 0) break;
			// 写入文件
			zos.write(b, 0, len);
		} while(true);

		in.close();
		zos.closeEntry();
	}
	
	/**
	 * 输出到磁盘文件
	 * @param part 工作部件
	 * @param file 磁盘文件名
	 * @throws IOException
	 */
	public void flush(File file) throws IOException {
		zos.finish();
		byte[] b = buff.toByteArray();
		
		FileOutputStream out = new FileOutputStream(file);
		out.write(b, 0, b.length);
		out.close();
	}
	
	/**
	 * 关闭!
	 */
	public void close() throws IOException {
		if (zos != null) {
			zos.close();
			zos = null;
		}
		if (buff != null) {
			buff.close();
			buff = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		try {
			close();
		} catch (IOException e) {

		}
	}

}