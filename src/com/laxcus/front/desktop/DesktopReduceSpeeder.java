/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.io.*;

import javax.swing.filechooser.*;

import com.laxcus.thread.*;

/**
 * 冗余资源加速器
 * 
 * @author scott.liang
 * @version 1.0 3/20/2022
 * @since laxcus 1.0
 */
final class DesktopReduceSpeeder extends VirtualThread {

	/**
	 * 构造默认的冗余资源加速器
	 */
	public DesktopReduceSpeeder() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		processFileSystem();
		processSystemHome();
		
		// 退出
		setInterrupted(true);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
//		System.out.println("release reduce resource!");
	}

	/**
	 * 处理本地文件资源，第一次读取，加速处理，以后就不用再处理了。
	 */
	private void processFileSystem() {
		String bin = System.getProperty("user.dir");
		bin += "/..";
		File root = new File(bin);
		// 生成规范目录，读它
		try {
			root = root.getCanonicalFile();
			processFileSystem(root);
		} catch (IOException e) {

		}
	}
	
	/**
	 * 处理文件系统
	 * @param root
	 */
	private void processFileSystem(File root) {
		boolean success = (root.exists() && root.isDirectory());
		if (!success) {
			return;
		}

		// 初始它
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File[] files = root.listFiles();
		int size = (files != null ? files.length : 0);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			// 区分文件和目录
			if (file.isFile()) {
				fsv.getSystemIcon(file);
				fsv.getSystemDisplayName(file);
				fsv.getSystemTypeDescription(file);
			} else if (file.isDirectory()) {
				processFileSystem(file);
			}
		}
	}

	/**
	 * 处理系统桌面
	 */
	private void processSystemHome() {
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File home = fsv.getHomeDirectory();
		if (home == null) {
			return;
		}
		// 桌面
		fsv.getSystemIcon(home);
		fsv.getSystemDisplayName(home);
		fsv.getSystemTypeDescription(home);

		// 取出下属目标
		File[] files = fsv.getFiles(home, false);
		int size = (files != null ? files.length : 0);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			fsv.getSystemIcon(file);
			fsv.getSystemDisplayName(file);
			fsv.getSystemTypeDescription(file);
		}
	}

}
