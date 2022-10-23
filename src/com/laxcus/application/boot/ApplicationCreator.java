/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.boot;

import java.io.*;
import java.util.zip.*;

import com.laxcus.util.*;

/**
 * 应用包生成器
 * 
 * @author scott.liang
 * @version 1.0 6/29/2021
 * @since laxcus 1.0
 */
public class ApplicationCreator {
	
	/**
	 * 读文件
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static byte[] readContent(File file) throws IOException {
		boolean success = (file.isFile() && file.exists());
		if (!success) {
			throw new FileNotFoundException("not found " + Laxkit.canonical(file));
		}
		
		byte[] b = new byte[(int) file.length()];
		// 读取磁盘文件
		FileInputStream in = new FileInputStream(file);
		int len = in.read(b);
		in.close();

		if (len < 1 || len != file.length()) {
			throw new IOException("illegal read!");
		}
		return b;
	}
	
	/**
	 * 构造应用容器
	 * @param bootstrap 引导配置文件
	 * @param jars 全部JAR文件
	 * @param libs 库文件
	 * @param others 其它文件，包括：图标、文档、配置文件等
	 * @param file 输出文件
	 * @throws IOException
	 */
	public static void createBasket(File bootstrap, File[] jars, File[] libs, File[] others, File dest) throws IOException {
		if (bootstrap == null || jars == null || jars.length == 0) {
			throw new IOException("empty file!");
		}

		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024 * 1024); // 10M缓存!
		ZipOutputStream zos = new ZipOutputStream(buff);

		// 引导配置文件
		ZipEntry entry = new ZipEntry(BasketBuffer.BOOTSTRAP);
		entry.setTime(System.currentTimeMillis());
		zos.putNextEntry(entry);
		byte[] b = readContent(bootstrap);
		zos.write(b, 0, b.length);
		zos.closeEntry();
		
		// JAR文件
		for(File file : jars) {
			String name = file.getName();
			entry = new ZipEntry(name);
			entry.setTime(System.currentTimeMillis());
			zos.putNextEntry(entry);
			b = readContent(file);
			zos.write(b, 0, b.length);
			zos.closeEntry();
		}
	
		// 库文件
		if (libs != null && libs.length > 0) {
			for (File file : libs) {
				String name = file.getName();
				entry = new ZipEntry(name);
				entry.setTime(System.currentTimeMillis());
				zos.putNextEntry(entry);
				b = readContent(file);
				zos.write(b, 0, b.length);
				zos.closeEntry();
			}
		}
		
		// 其他文件
		if (others != null && others.length > 0) {
			for (File file : others) {
				String name = file.getName();
				entry = new ZipEntry(name);
				entry.setTime(System.currentTimeMillis());
				zos.putNextEntry(entry);
				b = readContent(file);
				zos.write(b, 0, b.length);
				zos.closeEntry();
			}
		}
		// 输出和关闭，这个操作一起完成
		zos.flush();
		zos.close();
		
		// 写入磁盘
		FileOutputStream out = new FileOutputStream(dest);
		b = buff.toByteArray();
		out.write(b);
		out.flush();
		out.close();
	}
	
	/**
	 * 构造应用容器
	 * @param bootstrap 引导配置文件
	 * @param jars 全部JAR文件
	 * @param libs 库文件
	 * @param others 其它文件，包括：图标、文档、配置文件等
	 * @param file 输出文件
	 * @throws IOException
	 */
	public static void createBasket(File bootstrap, File[] jars, String libPrefix, File[] libs, File[] others, File dest) throws IOException {
		if (bootstrap == null || jars == null || jars.length == 0) {
			throw new IOException("empty file!");
		}

		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024 * 1024); // 10M缓存!
		ZipOutputStream zos = new ZipOutputStream(buff);

		// 引导配置文件
		ZipEntry entry = new ZipEntry(BasketBuffer.BOOTSTRAP);
		entry.setTime(System.currentTimeMillis());
		zos.putNextEntry(entry);
		byte[] b = readContent(bootstrap);
		zos.write(b, 0, b.length);
		zos.closeEntry();
		
		// JAR文件
		for(File file : jars) {
			String name = file.getName();
			entry = new ZipEntry(name);
			entry.setTime(System.currentTimeMillis());
			zos.putNextEntry(entry);
			b = readContent(file);
			zos.write(b, 0, b.length);
			zos.closeEntry();
		}
	
		// 库文件
		if (libs != null && libs.length > 0) {
			for (File file : libs) {
				String name = file.getName();
				if (libPrefix != null) {
					name = String.format("%s/%s", libPrefix, name);
				}
				entry = new ZipEntry(name);
				entry.setTime(System.currentTimeMillis());
				zos.putNextEntry(entry);
				b = readContent(file);
				zos.write(b, 0, b.length);
				zos.closeEntry();
			}
		}
		
		// 其他文件
		if (others != null && others.length > 0) {
			for (File file : others) {
				String name = file.getName();
				entry = new ZipEntry(name);
				entry.setTime(System.currentTimeMillis());
				zos.putNextEntry(entry);
				b = readContent(file);
				zos.write(b, 0, b.length);
				zos.closeEntry();
			}
		}
		// 输出和关闭，这个操作一起完成
		zos.flush();
		zos.close();
		
		// 写入磁盘
		FileOutputStream out = new FileOutputStream(dest);
		b = buff.toByteArray();
		out.write(b);
		out.flush();
		out.close();
	}

	public static void test() {
		File bt = new File("D:/laxcus/build/application/notepad/BOOT-INF/boot.xml");
		File icon0 = new File("D:/laxcus/build/application/notepad/app.png");
		File icon1 = new File("D:/laxcus/build/application/notepad/notepad.png");
		File icon2 = new File("D:/laxcus/build/application/notepad/edit.png");
		
		File jar = new File("D:/laxcus/build/application/notepad/notepad.jar");
//		File lib = new File("d:/laxtmp/scan.dll");
		File gpu = new File("d:/laxtmp/gpuminer.dll");
		File scan = new File("d:/laxtmp/scan.dll");
		File dest = new File("d:/notepad.das"); // das: distributed application software
		
		try {
			ApplicationCreator.createBasket(bt, new File[]{jar}, null, 
					new File[]{gpu, scan}, 
					new File[]{icon0, icon1, icon2}, dest);
//			BasketReader reader = new BasketReader();
//			reader.export(new File("d:/laxtmp"), "notepad", 666, dest);
			System.out.println("okay!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ApplicationCreator.test();
	}
}