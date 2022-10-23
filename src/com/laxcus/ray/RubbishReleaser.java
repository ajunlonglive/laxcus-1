/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import java.io.*;

/**
 * 删除运行时目录下过期的应用软件及附属目录
 * 
 * @author scott.liang
 * @version 1.0 2/28/2022
 * @since laxcus 1.0
 */
final class RubbishReleaser {

	public RubbishReleaser() {
		super();
	}
	
	/**
	 * 删除运行时目录下过期的垃圾文件
	 */
	public void deleteRubbishs() {
		// 删除应用程序目录
		deleteApplicationFolders();
	}
	
	/**
	 * 删除运行时应用程序目录
	 */
	private void deleteApplicationFolders() {
		deleteApplicationFolder("laxcus.run.system");
		deleteApplicationFolder("laxcus.run.user");
	}
	
	/**
	 * 根据KEY找到对应的目录，删除这个目录及下属的所有文件和子目录
	 * @param key
	 * @return
	 */
	private int deleteApplicationFolder(String key) {
		String value = System.getProperty(key);
		if (value == null) {
			return 0;
		}
		File dir = new File(value);
		boolean success = (dir.exists() && dir.isDirectory());
		if (success) {
			return deleteFolder(dir);
		}
		return 0;
	}
	
	/**
	 * 删除目录及下属所有文件。
	 * 注意！不要删除根目录
	 * 
	 * @param dir 目录
	 * @return 返回删除的成员数目
	 */
	private int deleteFolder(File dir) {
		int count = 0;
		File[] files = dir.listFiles();
		int size = (files != null ? files.length : 0);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				count += deleteFolder(file);
			} else if(file.isFile()) {
				boolean b = file.delete();
				if (b) count++;
			}
		}
		return count;
	}
	
}