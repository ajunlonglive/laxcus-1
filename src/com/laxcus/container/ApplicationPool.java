/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.container;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.util.*;

/**
 * 容器池。
 * 保存运行中的应用
 * 
 * @author scott.liang
 * @version 1.0 6/28/2021
 * @since laxcus 1.0
 */
public class ApplicationPool extends VirtualPool {

	/** 容器池句柄 **/
	private static ApplicationPool selfHandle = new ApplicationPool();
	
	/** 系统应用运行目录 */
	private File systemRoot;
	
	/** 用户应用运行目录 **/
	private File userRoot;

	/** 产生一个编号 **/
	private SerialGenerator generator = new SerialGenerator(1, Integer.MAX_VALUE);

	/** 容器 **/
	private TreeMap<Long, Container> containers = new TreeMap<Long, Container>();

	/** 删除资源 **/
	private ArrayList<Long> deletes = new ArrayList<Long>();

	/**
	 * 构造默认和私有的容器池
	 */
	private ApplicationPool() {
		super();
	}

	/**
	 * 返回容器池句柄
	 * @return 容器池
	 */
	public static ApplicationPool getInstance() {
		return ApplicationPool.selfHandle;
	}

	/**
	 * 设置系统应用运行根目录。要求目录存在。如果不存在，建立一个新的目录。
	 * @param path 系统应用运行根目录
	 * @return 目录存在，或者新建成功，返回真；否则假。
	 */
	public boolean setSystemRoot(File path) {
		// 检查目录
		boolean success = (path.exists() && path.isDirectory());

		// 如果目录不存在时，建立它
		if (!success) {
			success = path.mkdirs();
		}
		// 如果成功，保存为根目录（规范格式！）
		if (success) {
			try {
				systemRoot = path.getCanonicalFile();
			} catch (IOException e) {
				Logger.error(e);
				success = false;
			}
		}
		
		Logger.debug(this, "setSystemRoot", success, "%s", path);
		
		// 返回结果
		return success;
	}

	/**
	 * 设置系统应用运行根目录
	 * @param path 磁盘目录
	 * @return 成功返回真，否则假
	 */
	public boolean setSystemRoot(String path) {
		path = ConfigParser.splitPath(path);
		return setSystemRoot(new File(path));
	}

	/**
	 * 在指定运行根目录之下，通过后缀目录建立一个新目录，做为系统应用的运行根目录
	 * @param path 主目录
	 * @param subpath 子目录
	 * @return 成功返回<b>真</b>，否则<b>假</b>。
	 */
	public boolean setSystemRoot(String path, String subpath) {
		path = ConfigParser.splitPath(path);
		return setSystemRoot(new File(path, subpath));
	}

	/**
	 * 返回系统应用运行根目录
	 * @return File实例
	 */
	public final File getSystemRoot() {
		return systemRoot;
	}

	/**
	 * 设置用户应用运行根目录。要求目录存在。如果不存在，建立一个新的目录。
	 * @param path 用户应用运行根目录
	 * @return 目录存在，或者新建成功，返回真；否则假。
	 */
	public boolean setUserRoot(File path) {
		// 检查目录
		boolean success = (path.exists() && path.isDirectory());

		// 如果目录不存在时，建立它
		if (!success) {
			success = path.mkdirs();
		}
		// 如果成功，保存为根目录（规范格式！）
		if (success) {
			try {
				userRoot = path.getCanonicalFile();
			} catch (IOException e) {
				Logger.error(e);
				success = false;
			}
		}
		
		Logger.debug(this, "setUserRoot", success, "%s", path);
		
		// 返回结果
		return success;
	}

	/**
	 * 设置用户应用运行根目录
	 * @param path 磁盘目录
	 * @return 成功返回真，否则假
	 */
	public boolean setUserRoot(String path) {
		path = ConfigParser.splitPath(path);
		return setUserRoot(new File(path));
	}

	/**
	 * 在指定运行根目录之下，通过后缀目录建立一个新目录，做为用户应用的运行根目录
	 * @param path 主目录
	 * @param subpath 子目录
	 * @return 成功返回<b>真</b>，否则<b>假</b>。
	 */
	public boolean setUserRoot(String path, String subpath) {
		path = ConfigParser.splitPath(path);
		return setUserRoot(new File(path, subpath));
	}

	/**
	 * 返回用户应用运行根目录
	 * @return File实例
	 */
	public final File getUserRoot() {
		return userRoot;
	}

	
	/**
	 * 返回一个序列编号，在一段时间内唯一。
	 * 序列编号在 0 - Integer.MAX_VALUE之间循环。
	 * @return 长整型正数
	 */
	public long nextPID() {
		return generator.nextSerial();
	}
	
	/**
	 * 删除目录
	 * @param root
	 */
	private void deleteDirectory(File root) {
		File[] files = root.listFiles();
		int size = (files == null ? 0 : files.length);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				deleteDirectory(file);
			} else if (file.isFile()) {
				file.delete();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 目录必须设置！
		if (systemRoot == null) {
			Logger.error(this, "init", "cannot be set system directory!");
			return false;
		}
		if (userRoot == null) {
			Logger.error(this, "init", "cannot be set user directory!");
			return false;
		}
		
		// 在启动前删除目录，这是垃圾文件
		deleteDirectory(systemRoot);
		deleteDirectory(userRoot);
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		while (!isInterrupted()) {
			// 检查分布任务组件包更新
			if (hasExit()) {
				release();
			}
			// 线程进行等待状态
			sleep();
		}
		// 清除
		release();

		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		containers.clear();
		deletes.clear();
		
		// 退出前，删除目录下的文件
		deleteDirectory(systemRoot);
		deleteDirectory(userRoot);
	}

	/**
	 * 保存
	 * @param container
	 */
	public void add(Container container) {
		super.lockSingle();
		try {
			containers.put(container.getPID(), container);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 判断有退出
	 * @return 返回真或者假
	 */
	private boolean hasExit() {
		// 锁定
		super.lockMulti();
		try {
			return (deletes.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return false;
	}

	/**
	 * 根据PID删除容器
	 * @param pid 进程编号
	 */
	private void delete(long pid) {
		Container container = containers.remove(pid);
		if (container == null) {
			return;
		}

		// 释放所有类定义，首先释放动态链接库，再清除文件
		int count =	container.release();
		Logger.info(this, "delete", "release count %d", count);
	}

	/**
	 * 释放被删除的容器
	 */
	private void release() {
		super.lockSingle();
		try {
			int size = deletes.size();
			for(int i =0; i < size; i++) {
				long pid = deletes.get(i);
				delete(pid);
			}
			deletes.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 释放资源，收集进程号之后，交给线程释放和JVM回收
	 * 
	 * @param pid 进程编号
	 * @param status 状态码
	 * @return 返回结束码，见ContainerShutdown的定义
	 */
	public int exit(long pid, int status) {
		// 锁定
		super.lockSingle();
		try {
			// 判断存在
			boolean success = containers.containsKey(pid);
			if (success) {
				deletes.add(pid);
				return ContainerShutdown.SUCCESSFUL;
			} else {
				return ContainerShutdown.NOTFOUND;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return ContainerShutdown.FAILED;
	}

}