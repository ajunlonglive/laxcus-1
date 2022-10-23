/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.loader.*;

/**
 * 自定义类资源管理池。<br><br>
 * 
 * 管理基于COMMAND/INVOKER配置对，检查和热发布的JAR数据包。判断JAR包更新的条件是：文件名、时间、长度发生改变。<br>
 * 
 * 管理员发布、更新本地COMMAND/INVOKER的JAR包时，首先加载JAR包到内存，再更新声明文件。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/2/2017
 * @since laxcus 1.0
 */
public final class CustomClassPool extends VirtualPool {

	/** 管理池句柄 **/
	private static CustomClassPool selfHandle = new CustomClassPool();

	/** JAR文件后缀 **/
	private final String suffix = ".jar";

	/** 保存的JAR包记录 **/
	private TreeSet<FileKey> records = new TreeSet<FileKey>();

	/** JAR资源包加载器 **/
	private HotClassLoader loader;

	/** 强制更新JAR包 **/
	private boolean forceUpdate;

	/**
	 * 构造默认和私有的自定义类资源管理池
	 */
	private CustomClassPool() {
		super();
		// 1分钟检查一次
		setSleepTime(60);
		// 强制更新假
		setForceUpdate(false);
	}

	/**
	 * 设置为强制更新
	 * @param b 强制更新
	 */
	public void setForceUpdate(boolean b) {
		forceUpdate = b;
	}

	/**
	 * 判断是强制更新
	 * @return 返回真或者假
	 */
	public boolean isForceUpdate() {
		return forceUpdate;
	}

	/**
	 * 返回管理池句柄
	 * @return CustomClassPool实例
	 */
	public static CustomClassPool getInstance() {
		return CustomClassPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 加载配置
		boolean success = update();
		Logger.debug(this, "init", success, "load custom jar and split command/invoker");
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.debug(this, "process", "into...");
		// 循环判断直到退出
		while (!isInterrupted()) {
			sleep();
			// 如果允许自动检查更新时，定时检查更新
			if (CustomConfig.isAutoUpdate()) {
				boolean success = isUpdate();
				if (success) {
					update();
				}
			} else if (isForceUpdate()) {
				setForceUpdate(false);
				update();
			}
		}
		Logger.debug(this, "process", "exit!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		loader = null;
	}

	/**
	 * 转换参数
	 * @param file 磁盘文件
	 * @return FileKey实例
	 */
	private FileKey convert(File file) {
		return new FileKey(file.getAbsolutePath(), file.length(),
				file.lastModified());
	}

	/**
	 * 判断发生更新
	 * @return 发生更新返回真，否则假
	 */
	private boolean isUpdate() {
		String path = CustomConfig.getDirectory();
		// 如果目录是空值，不存在发生更新的可能
		if (path == null || path.isEmpty()) {
			return false;
		}

		// 目录
		File dir = new File(path);
		// 扫描本地文件
		ArrayList<File> files = new ArrayList<File>();
		scanDisk(files, dir);

		// 转换成自定义单元
		ArrayList<FileKey> array = new ArrayList<FileKey>();
		for (File file : files) {
			FileKey e = convert(file);
			array.add(e);
		}
		// 如果数目不匹配，存在更新
		if (array.size() != records.size()) {
			return true;
		}

		// 统计匹配的数目
		int count = 0;
		for (FileKey e : array) {
			if (records.contains(e)) {
				count++;
			}
		}
		// 如果不匹配，是发生更新
		return (count != records.size());
	}

	/**
	 * 扫描磁盘文件
	 * @param array 文件数组
	 * @param dir 磁盘目录
	 */
	private void scanDisk(List<File> array, File dir) {
		File[] subs = dir.listFiles();
		for (File file : subs) {
			String name = file.getAbsolutePath();
			if (file.isDirectory()) {
				scanDisk(array, file);
			} else if (name.toLowerCase().endsWith(suffix)) {
				array.add(file);
			}
		}
	}

	/**
	 * 读出JAR内容
	 * @param file 磁盘文件
	 * @return 返回JAR内容，错误返回空值
	 */
	private byte[] readContent(File file) {
		int len = (int) file.length();
		if (len < 1) {
			return null;
		}

		try {
			byte[] b = new byte[len];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return b;
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 热发布和更新自定义JAR文件和配置。如果没有定义自定义配置参数，或者参数不正确，返回“真”。<br><br>
	 * 
	 * 热发布和更新操作流程：<br>
	 * 1. 把自定义目录中的JAR读出来，保存到本地类加载器（注意，不是系统的类加载），以实现热发布和更新。<br>
	 * 2. 重新解析COMMAND/INVOKER配置对。<br>
	 * 这个顺序不能乱！！！
	 * 
	 * @return 成功返回真，否则假
	 */
	public boolean update() {
		// 如果参数无效，忽略它
		if (!CustomConfig.isValidate()) {
			Logger.warning(this, "update", "invalid custom configure!");
			return true;
		}

		// 先加载JAR文件，再解析配置文件
		boolean success = loadJar();
		if (success) {
			success = splitStatement();
		}
		Logger.note(this, "update", success, "load custom jar resource");

		return success;
	}

	/**
	 * 加载指定目录下的JAR文件
	 * @return 成功返回真，否则假
	 */
	private boolean loadJar() {
		String path = CustomConfig.getDirectory();
		// 不允许空目录
		if (path == null || path.isEmpty()) {
			Logger.error(this, "loadJar", "null path!");
			return false;
		}

		// 目录
		File dir = new File(path);
		// 扫描本地文件
		ArrayList<File> files = new ArrayList<File>();
		scanDisk(files, dir);

		// JAR档案条目数目
		ArrayList<HotClassEntry> array = new ArrayList<HotClassEntry>();

		// 加载JAR文件
		for (File file : files) {
			byte[] b = readContent(file);
			// 如果是空值，忽略它
			if(Laxkit.isEmpty(b)) {
				continue;
			}
			// 生成JAR档案条目
			HotClassEntry e = new HotClassEntry(file.getAbsolutePath(), b);
			array.add(e);

			Logger.info(this, "loadJar", "reload %s", e.getPath());
		}

		// 生成新的类加载器
		// 执行下面这段代码后，旧的类加载器被抛弃，调用Class.forName方法时，系统将重新调用ClassLoader.findClass方法，去找指定类
		loader = new HotClassLoader(array);
		// 提供给自定义资源生成器使用
		CustomCreator.setClassLoader(loader);

		// 重新保存参数
		records.clear();
		for(File file : files) {
			FileKey e = convert(file);
			records.add(e);
		}
		
		Logger.info(this, "loadJar", "jar files: %d", records.size());

		// 操作成功
		return true;
	}

	/**
	 * 解析配置文件中的COMMAND/INVOKER声明对
	 * @return 成功返回真，否则假
	 */
	private boolean splitStatement() {
		String filename = CustomConfig.getStatement();
		if (filename == null || filename.isEmpty()) {
			Logger.error(this, "splitStatement", "null statement file!");
			return false;
		}

		Logger.info(this, "splitStatement", "resolve %s", filename);

		// 清空
		CustomCreator.clear();
		// 交给解析器解析COMMAND/INVOKER对
		CustomTokenParser parser = new CustomTokenParser();
		boolean success = parser.split(filename);

		Logger.note(this, "splitStatement", success, "split statement file %s", filename);

		return success;
	}

}