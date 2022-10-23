/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.boot;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import com.laxcus.util.loader.*;

/**
 * 读取一个“*.das、*.eas、*.cas”格式的文件，把解析结果保存在内存里
 * 
 * @author scott.liang
 * @version 1.0 7/4/2021
 * @since laxcus 1.0
 */
public class BasketBuffer {

	class Buffer {
		byte[] content;

		Buffer(byte[] b) {
			super();
			content = b;
		}
	}
	
	public static final String BOOTSTRAP = "BOOT-INF/boot.xml";

	/** JAR包 **/
	private static String JAR = "^\\s*([\\w\\W]+)(?i)(\\.jar)\\s*$";

	/** 文件名(除了JAR包之外的其它文件) -> 字节数组 **/
	private TreeMap<String, Buffer> buffers = new TreeMap<String, Buffer>();

	/** 类加载器 **/
	private HotClassLoader loader;
	
	public BasketBuffer() {
		super();
	}

	/**
	 * 是JAR文件
	 * @param file
	 * @return
	 */
	private boolean isJar(String filename) {
		return filename.matches(JAR);
	}

	/**
	 * 从磁盘读取字节内容
	 * @param file
	 * @throws IOException
	 */
	private byte[] readContent(File file) throws IOException {
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			throw new FileNotFoundException(file.toString());
		}
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();
		return b;
	}

	/**
	 * 全部加载到内存
	 * 
	 * @param tmp
	 * @param software
	 * @param pid
	 * @param file
	 * @throws IOException
	 */
	public void load(File file) throws IOException {
		URL bootURL = file.toURI().toURL();

		/** JAR单元 **/
		ArrayList<HotClassEntry> entries = new ArrayList<HotClassEntry>();

		// 读文件
		byte[] content = readContent(file);
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

			// 文件内容
			final String name = entry.getName();
//						System.out.println(name);

			// 读一个文件
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			do {
				int len = jin.read(b, 0, b.length);
				if (len == -1) {
					break;
				}
				out.write(b, 0, len);
			} while (true);
			b = out.toByteArray();

			// 判断是JAR文件
			if (isJar(name)) {
				HotClassEntry e = new HotClassEntry(null, name, bootURL, b);
				entries.add(e);
			} else {
				Buffer bf = new Buffer(b);
				buffers.put(name, bf);
			}
		}

		jin.close();
		bin.close();

		// 生成类加载器
		loader = new HotClassLoader(entries);
	}
	
	/**
	 * 全部加载到内存
	 * @param content
	 * @throws IOException
	 */
	public void load(byte[] content) throws IOException {
		/** JAR单元 **/
		ArrayList<HotClassEntry> entries = new ArrayList<HotClassEntry>();

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

			// 文件内容
			final String name = entry.getName();

			// 读一个文件
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			do {
				int len = jin.read(b, 0, b.length);
				if (len == -1) {
					break;
				}
				out.write(b, 0, len);
			} while (true);
			b = out.toByteArray();

			// 判断是JAR文件
			if (isJar(name)) {
				HotClassEntry e = new HotClassEntry(null, name, null, b);
				entries.add(e);
			} else {
				Buffer bf = new Buffer(b);
				buffers.put(name, bf);
			}
		}

		jin.close();
		bin.close();

		// 生成类加载器
		loader = new HotClassLoader(entries);
	}

	/**
	 * 读取在JAR包中的
	 * @param name
	 * @return
	 */
	public byte[] getJURI(String name) throws IOException {
		return loader.readResource(name);
	}

	/**
	 * 读取非JAR包，是在文件中独立存在的...
	 * @param name 包括路径名的名称
	 * @return 返回它的字节数组，没有是空指针
	 */
	public byte[] getURI(String name) {
		Buffer bf = buffers.get(name);
		if (bf != null) {
			return bf.content;
		}
		return null;
	}

	/**
	 * 读取引导配置信息
	 * @return 返回字节数组
	 */
	public byte[] readBootstrap() {
		return getURI(BOOTSTRAP);
	}
	
	public void test(){
		File file = new File("d:/notepad.das");
		try {
		load(file);
		byte[] b = this.readBootstrap();
		System.out.printf("byte len %d\n", b.length);
		
		BootSplitter bs = new BootSplitter();
		BootItem item =	bs.split(b);
		System.out.printf("title is %s\n", item.getTitle());
		System.out.printf("icon %s\n", 	item.getIcon());
		System.out.printf("application %s | %s\n", item.getApplication().getCommand(), 
				item.getApplication().getBootClass());
		
		b = this.getURI("notepad.png");
		System.out.printf("byte len %d\n", b.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		BasketBuffer e = new BasketBuffer ();
		e.test();
	}
}