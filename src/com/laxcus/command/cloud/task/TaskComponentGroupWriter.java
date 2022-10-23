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

import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 组件集输出流
 * 将多个以".dtc"为后续的组件打在一个包里面，并且加上标签输出到磁盘！
 * 
 * @author scott.liang
 * @version 1.0 4/4/2020
 * @since laxcus 1.0
 */
public class TaskComponentGroupWriter {

	/** 统计 **/
	private int count;
	
	/** 已经保存的名称 **/
	private TreeSet<String> array = new TreeSet<String>();
	
	/** 缓存流数据 **/
	private ByteArrayOutputStream buff;
	
	/** 压缩文件流 **/
	private ZipOutputStream zos;
	

	/**
	 * 组件集输出流，指定缓存长度
	 * @param bufflen 字节缓存长度
	 */
	public TaskComponentGroupWriter(int bufflen) {
		super();
		init(bufflen);
	}
	
	/**
	 * 构造默认的组件集输出流
	 */
	public TaskComponentGroupWriter() {
		this(10240);
	}
	
	/**
	 * 初始化缓存
	 */
	private void init(int len) {
		if (len < 1024) {
			len = 1024;
		}
		count = 0;
		buff = new ByteArrayOutputStream(len);
		zos = new ZipOutputStream(buff);
	}
	
	/**
	 * 判断存在这个名称的文件
	 * @param name 名称
	 * @return 返回真或者假
	 */
	public boolean exists(String name) {
		return array.contains(name);
	}
	
	/**
	 * 字节内容输出到压缩包
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 长度
	 * @param selfly 是否自有组件集合
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	public int write(byte[] b, int off, int len) throws IOException {
		MD5Hash hash = Laxkit.doMD5Hash(b);
		
		//	String name = (selfly ? "selfly" : hash.toString()) + TaskArchive.DTC_SUFFIX;

		// 检查
		String name = hash.toString() + TF.DTC_SUFFIX;
//		if (selfly) {
//			name = TF.SELFLY_FILE;
//		}

		// 判断名称存在
		if (exists(name)) {
			throw new IOException("file existed! " + name);
		}
		
		// 只在内容到压缩缓存
		ZipEntry entry = new ZipEntry(name);
		entry.setTime(System.currentTimeMillis());
		zos.putNextEntry(entry);
		zos.write(b, off, len);
		zos.closeEntry();
		zos.flush();
		
		// 统计值增1
		count++;
		
		// 保存名称
		array.add(name);
		
		return len;
	}

	/**
	 * 字节内容输出到压缩包
	 * @param b 字节数组
	 * @param selfly 是否自有组件集合
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	public int write(byte[] b) throws IOException {
		return write(b, 0, b.length);
	}
	
	/**
	 * 文件内容输出到压缩包
	 * @param file 磁盘文件
	 * @param selfly 是否自有组件集合
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	public int write(File file) throws IOException {
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		int len = in.read(b);
		in.close();
		// 保存到压缩
		return write(b, 0, len);
	}

	/**
	 * 生成XML标签
	 * @param part
	 * @return 输出UTF8格式的字节数组
	 */
	public byte[] buildXMLTag(TaskPart part) {
		// 内部标签
		StringBuilder sub = new StringBuilder();
		// 用户签名
		if (part.getIssuer() != null) {
			String s = String.format("<sign>%s</sign>", part.getIssuer().toString());
			sub.append(s);
		}
		// 阶段命名
		String family = PhaseTag.translate(part.getFamily());
		String s = String.format("<phase>%s</phase>", family);
		sub.append(s);
		// 单元统计值
		s = String.format("<count>%d</count>", count);
		sub.append(s);

		// 输出实例
		StringBuilder all = new StringBuilder();
		all.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		s = String.format("<root>%s</root>", sub.toString());
		all.append(s);

		// 以UTF-8格式输出！
		return new UTF8().encode(all.toString());
	}

	/**
	 * 输出带标签的字节内容
	 * @param part 工作部件
	 * @return 字节数组
	 * @throws IOException
	 */
	public byte[] flush(TaskPart part) throws IOException {
		byte[] b = buildXMLTag(part);
		
		// 固定名称
//		String name = TaskBoot.TAG; // "GROUP-INF/group.xml";
		String name = TF.GROUP_INF; // "GROUP-INF/group.xml";
		ZipEntry entry = new ZipEntry(name);
		entry.setTime(System.currentTimeMillis());
		zos.putNextEntry(entry);
		zos.write(b, 0, b.length);
		zos.closeEntry();
		// 输出内容和关闭底层输出流
		zos.flush();
		zos.finish();
		
		// 输出字节数组
		return buff.toByteArray();
	}
	
	/**
	 * 输出到磁盘文件
	 * @param part 工作部件
	 * @param file 磁盘文件名
	 * @throws IOException
	 */
	public void flush(TaskPart part, File file) throws IOException {
		byte[] b = flush(part);
		
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
	
	private static void create() {
		TaskComponentGroupWriter writer = new TaskComponentGroupWriter();
		SHA256Hash issuer = Laxkit.doSHA256Hash("AXIBIT");
		TaskPart part = new TaskPart(issuer, PhaseTag.ISSUE);
		
		File file = new File("e:/aixbit/flush.dtg");
		
		try {
			writer.write(new File("e:/aixbit/DEMO.txt"));
//			writer.write(new File("e:/aixbit/DEMO.txt"), true);
			writer.write(new File("e:/aixbit/licence.txt"));
			writer.flush(part, file);
			
//			int len = writer.toBytes().length;
//			System.out.printf("len is %d\n", len);
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		create();
		System.out.println("Okay!!!");
	}
}
