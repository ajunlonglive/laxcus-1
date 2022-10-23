/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.loader.*;

/**
 * 扩展JAR包管理池，定时检测、更新、加载。<br>
 * 扩展包固定是节点目录下的“ext”目录。
 * 
 * @author scott.liang
 * @version 1.0 10/2/2019
 * @since laxcus 1.0
 */
public final class ExtClassPool extends VirtualPool {

	/** 管理池句柄 **/
	private static ExtClassPool selfHandle = new ExtClassPool();
	
	/** 根目录 **/
	private File root;

	/** JAR文件后缀 **/
	private final String suffix = ".jar";

	/** 保存的JAR包记录 **/
	private TreeSet<FileKey> records = new TreeSet<FileKey>();

	/** JAR资源包加载器 **/
	private HotClassLoader loader;

	/**
	 * 构造默认和私有的自定义类资源管理池
	 */
	private ExtClassPool() {
		super();
		// 1分钟检查一次
		setSleepTime(60);
	}

	/**
	 * 唤醒线程，检测目录上的JAR包，重新加载
	 */
	public void reload() {
		wakeup();
	}

	/**
	 * 返回管理池句柄
	 * @return ExtClassPool实例
	 */
	public static ExtClassPool getInstance() {
		return ExtClassPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		String key = "java.system.class.loader";
		String value = System.getProperty(key);
		Logger.debug(this, "init", "%s | %s", key, value);
		Logger.debug(this, "init", "System Class Loader is %s", ClassLoader.getSystemClassLoader().getClass().getName());
		Logger.debug(this, "init", "Parent Class Loader is %s", ClassLoader.getSystemClassLoader().getParent().getClass().getName());

		boolean success = createRoot();
		if (success) {
			success = loadJar();
		}
		
		Logger.debug(this, "init", success, "result is");
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		while (!isInterrupted()) {
			// 判断发生更新，如果是，加载新的
			boolean success = isUpdate();
			if (success) {
				loadJar();
			}
			sleep();
		}
		Logger.info(this, "process", "exit!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		records.clear();
		loader = null;
	}
	
	/**
	 * 返回类加载器
	 * @return
	 */
	public ClassLoader getClassLoader() {
		return loader;
	}
	
	/**
	 * 建立基于系统配置下的目录
	 * @return 成功返回目录实例，否则空指针
	 */
	private File createSystemRoot() {
		String path = System.getProperty("laxcus.default.site");
		if (path == null) {
			return null;
		}
		File dir = new File(path, "ext");
		// 转成标准化的目录
		try {
			dir = dir.getCanonicalFile();
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			success = dir.mkdirs();
			return (success ? dir : null);
		}
		return dir;
	}

	/**
	 * 建立默认配置下的扩展目录
	 * @return 成功返回目录实例，否则是空指针
	 */
	private File createDefaultRoot() {
		String path = System.getProperty("user.dir");
		if (path == null) {
			return null;
		}
		File dir = new File(path, "../ext");
		try {
			dir = dir.getCanonicalFile();
		} catch (IOException ex) {
			Logger.error(ex);
			return null;
		}
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			success = dir.mkdirs();
			return (success ? dir : null);
		}
		return dir;
	}

	/**
	 * 建立根目录
	 * @return 返回真或者假
	 */
	private boolean createRoot() {
		File dir = createSystemRoot();
		if (dir == null) {
			dir = createDefaultRoot();
		}
		if (dir == null) {
			return false;
		}
		root = dir;
		return true;
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
		// 扫描本地文件
		ArrayList<File> files = new ArrayList<File>();
		scanDisk(files, root);

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
	 * 加载指定目录下的JAR文件
	 * @return 成功返回真，否则假
	 */
	private boolean loadJar() {
		// 扫描本地文件
		ArrayList<File> files = new ArrayList<File>();
		scanDisk(files, root);

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
}
