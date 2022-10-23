/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;

/**
 * 检查目标目录，再从源目录找到匹配的，更新它!
 * 
 * @author scott.liang
 * @version 1.0 12/17/2019
 * @since laxcus 1.0
 */
public class Updater {
	
	/** 目标目录 **/
	private File destRoot;
	
	/** 源目录 **/
	private File sourceRoot;
	
	/** 统计更新成员数目 **/
	private int count;

	/**
	 * 构造实例
	 */
	public Updater() {
		super();
		count = 0;
	}
	
	/**
	 * 检查目录
	 * @param dest
	 * @param source
	 * @return
	 */
	private boolean check(String dest, String source) {
		destRoot = new File(dest);
		sourceRoot = new File(source);
		// 检查目录目录
		boolean success = (destRoot.exists() && destRoot.isDirectory());
		if (!success) {
			System.out.printf("not found %s\n", dest);
			return false;
		}
		// 检查源目录
		success = (sourceRoot.exists() && sourceRoot.isDirectory());
		if (!success) {
			System.out.printf("not found %s\n", dest);
			return false;
		}
		return true;
	}
	
	/**
	 * 移动文件
	 * @param dest 目标文件
	 * @param source 源头文件
	 * @return 成功返回真，否则假
	 */
	private boolean move(File dest, File source) {
		boolean success = true;
		
		try {
			byte[] b = new byte[(int)source.length()];
			FileInputStream in = new FileInputStream(source);
			in.read(b);
			in.close();
			
			FileOutputStream out = new FileOutputStream(dest);
			out.write(b);
			out.flush();
			out.close();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	/** 正则表达式 **/
	private final String REGEX = "^\\s*(.+)(\\.)(?i)(SO|DLL|JAR|DTC|CSC|SPC)\\s*$";
	
	/**
	 * 判断文件类型匹配
	 * @param name
	 * @return
	 */
	private boolean matchs(String name) {
		return name.matches(REGEX);
	}
	
	/**
	 * 从指定目录更新
	 * @param dest
	 */
	private void update(File dest) {
		int all = 0;
		File[] files = dest.listFiles();
		for (int i = 0; files != null && i < files.length; i++) {
			File file = files[i];
			// 是目录，检查它
			if (file.isDirectory()) {
				update(file);
			}
			// 不是文件，忽略它！
			if (!file.isFile()) {
				continue;
			}
			
			// 判断匹配
			String name = file.getName();
			boolean success = matchs(name);
			if (!success) {
				continue;
			}
			File source = new File(sourceRoot, name);
			success = (source.exists() && source.isFile());
			if (!success) {
				System.out.printf("ERROR! source:%s -> dest:%s!\n", source.toString(), file.toString());
				continue;
			}
			// 移动文件
			success = move(file, source);
			if (success) {
				System.out.printf("%s -> %s\n", source.toString(), file.toString());
				all++;
			} else {
				System.out.printf("FAILED! source:%s -> dest:%s!\n", source.toString(), file.toString());
			}
		}
		count += all;
	}

	private boolean isSource(String name) {
		return name.matches("^\\s*(?i)(?:-SOURCE)\\s*$");
	}

	private boolean isDest(String name) {
		return name.matches("^\\s*(?i)(?:-DEST)\\s*$");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int size = args.length;
		if (size != 4) {
			System.out.println("param missing!");
			System.exit(0);
			return;
		}
		
		Updater e = new Updater();

		// 检查参数
		boolean success = false;
		if (e.isDest(args[0]) && e.isSource(args[2])) {
			success = e.check(args[1], args[3]);
		} else if (e.isSource(args[0]) && e.isDest(args[2])) {
			success = e.check(args[3], args[3]);
		}
		// 参数错误！
		if (!success) {
			System.out.println("ERROR! param error!");
			System.exit(0);
			return;
		}

		System.out.printf("source %s to dest %s\n", e.sourceRoot, e.destRoot);
		
		// 更新
		e.update(e.destRoot);
		System.out.printf("update count %d\n", e.count);
		System.exit(0);
	}

}
