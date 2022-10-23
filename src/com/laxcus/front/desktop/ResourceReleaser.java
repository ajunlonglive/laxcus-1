/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.io.*;

/**
 * 资源释放器
 * 包括动态生成的脚本和应用程序
 * 
 * @author scott.liang
 * @version 1.0 2/13/2022
 * @since laxcus 1.0
 */
final class ResourceReleaser {

	public ResourceReleaser() {
		super();
	}
	
	/**
	 * 删除动态脚本
	 */
	public void deleteResource() {
		// 删除脚本
		deleteScripts();
		// 删除应用程序目录
		deleteApplicationFolders();
	}
	
	/**
	 * 删除动态脚本
	 */
	private void deleteScripts() {
		deleteConfigureFile("applications.conf");
		deleteConfigureFile("buttons.conf");
		deleteConfigureFile("environment.conf");
		deleteConfigureFile("docks.conf");
	}

	/**
	 * 删除应用程序目录
	 */
	private void deleteApplicationFolders() {
		deleteApplicationFolder("laxcus.store.system");
		deleteApplicationFolder("laxcus.store.user");
		deleteApplicationFolder("laxcus.store.temp");
		deleteApplicationFolder("laxcus.run.system");
		deleteApplicationFolder("laxcus.run.user");
	}
	
	/**
	 *  删除一个配置文件
	 *  delete [E:\os\client\x86\win32\desktop\bin\..\conf\applications.conf] is true
	 *  delete [E:\os\client\x86\win32\desktop\bin\..\conf\buttons.conf] is true
	 *  delete [E:\os\client\x86\win32\desktop\bin\..\conf\environment.conf] is true
	 *  delete [E:\os\client\x86\win32\desktop\bin\..\conf\docks.conf] is true
	 * @param name
	 * @return
	 */
	private boolean deleteConfigureFile(String name) {
		File dir = DesktopSystem.createRuntimeDirectory();
		if (dir == null) {
			return false;
		}
		File file = new File(dir, name);
		// 判断文件存在
		boolean success = (file.exists() && file.isFile());
		if (success) {
			success = file.delete();
		}
		return success;
	}
	
	/**
	 * 删除目录及下属所有文件
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
				boolean success = file.delete();
				if (success) count++;
			}
		}
		// 删除根目录
		boolean success = dir.delete();
		if (success) {
			count++;
		}
		return count;
	}
	
	/**
	 * 根据KEY找到对应的目录，删除这个目录及下属的所有文件和子目录
	 * 注意！根目录本身不要删除！
	 * @param key
	 * @return 返回删除记录数
	 */
	private int deleteApplicationFolder(String key) {
		String value = System.getProperty(key);
		if (value == null) {
			return 0;
		}
		File dir = new File(value);
		// 判断存在
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			return 0;
		}
		File[] files = dir.listFiles();
		int count = 0;
		int size = (files == null ? 0 : files.length);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			if (file.isFile()) {
				success = file.delete();
				if (success) {
					count++;
				}
			} else if (file.isDirectory()) {
				count += deleteFolder(file);
			}
		}
		return count;
	}
	
//	/**
//	 * 根据KEY找到对应的目录，删除这个目录及下属的所有文件和子目录
//	 * 注意！根目录本身不要删除！
//	 * @param key
//	 * @return
//	 */
//	private int deleteApplicationFolder(String key) {
//		String value = System.getProperty(key);
//		if (value == null) {
//			return 0;
//		}
//		File dir = new File(value);
//		File[] files = dir.listFiles();
//		int count = 0;
//		for (int i = 0; i < files.length; i++) {
//			File file = files[i];
//			if (file.isFile()) {
//				boolean success = file.delete();
//				if (success) {
//					count++;
//				}
//			} else if (file.isDirectory()) {
//				count += deleteFolder(file);
//			}
//		}
//		return count;
//	}
	
}
