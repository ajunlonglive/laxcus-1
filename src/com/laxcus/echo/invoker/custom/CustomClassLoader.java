/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 自定义JAR包加载器。<br><br>
 * 
 * 这个加载器是把JAR加入到系统的JAR列表。系统运行过程中，将根据类定义，去JAR列表查找指定名称的类和资源。<br>
 * JAR包加载器不能支持象分布任务组件的分布式热发布和即时更新。它只在节点启动时加载JAR包，加载后即退出。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/2/2017
 * @since laxcus 1.0
 */
public class CustomClassLoader {

	/**
	 * 构造默认的自定义JAR包加载器
	 */
	public CustomClassLoader(){
		super();
	}

	/**
	 * 解析配置文件中的COMMAND/INVOKER声明对
	 * @return 成功返回真，否则假
	 */
	private boolean splitStatement() {
		String filename = CustomConfig.getStatement();
		// 清空COMMAND/INVOKER对
		CustomCreator.clear();
		// 交给解析器解析COMMAND/INVOKER对
		CustomTokenParser parser = new CustomTokenParser();
		return parser.split(filename);
	}

	/**
	 * 加载自定义目录上的JAR文件，追加到系统的资源环境中。<br>
	 * 本处的加载JAR文件是追加到系统环境，加载后不能动态更新，有别于CustomClassPool可动态更新。
	 * 
	 * @return 加载成功返回真，否则假
	 */
	private boolean loadJar() {
		String path = CustomConfig.getDirectory();
		// 不允许空目录
		if (path ==null ||path.isEmpty()) {
			return false;
		}

		// 目录
		File dir = new File(path);
		// 扫描本地文件
		ArrayList<File> array = new ArrayList<File>();
		scanDisk(array, dir);

		// 加载JAR文件
		for (File file : array) {
			boolean success = loadJAR(file);
			// 任何一个加载失败，返回假
			if (!success) {
				return false;
			}
		}

		// 类加载器是空值。调用Class.forName方法时，将选用系统类加载器
		CustomCreator.setClassLoader(null);

		return true;
	}

	/**
	 * 加载本地自定义资源。<br><br>
	 * 操作流程：<br>
	 * 1. 将指定目录下的JAR文件追加到系统中。<br>
	 * 2. 解析COMMAND/INVOKER配置对。<br>
	 * 这个顺序不能乱！！！<br>
	 * 
	 * @return 成功返回真，否则假
	 */
	public boolean load() {
		// 1. 加载JAR文件到系统环境
		boolean success = loadJar();
		// 2. 解析COMMAND/INVOKER配置对
		if (success) {
			success = splitStatement();
		}
		return success;
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
			} else if (name.toLowerCase().endsWith(".jar")) {
				array.add(file);
			}
		}
	}

	/**
	 * 将一个JAR文件追加到系统环境。<br>
	 * 这个追加方案不允许动态更新，区别于CustomClassPool的动态更新的加载。
	 * 
	 * @param file 磁盘文件
	 * @return 成功返回真，否则假
	 */
	private boolean loadJAR(File file) {
		// 判断是继承自URLClassLoader，否则返回假
		ClassLoader clazzLoader = ClassLoader.getSystemClassLoader();
		if (!Laxkit.isClassFrom(clazzLoader, URLClassLoader.class)) {
			Logger.error(this, "loadJAR", "must be from URLClassLoader, %s",
					clazzLoader.getClass().getName());
			return false;
		}

		Method method = null;
		boolean accessible = false;

		boolean success = false;
		try {
			URLClassLoader loader = (URLClassLoader) clazzLoader;
			// 取出方法
			method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			// 判断是可访问，如果不行，设置为可访问。
			accessible = method.isAccessible();
			if (!accessible) {
				method.setAccessible(true);
			}
			// 加载JAR文件到运行环境列表，之后系统将去URL列表查找对应类和资源。
			URL url = file.toURI().toURL();
			method.invoke(loader, url);
			
			Logger.debug(this, "loadJAR", "load \'%s\' to %s", url, loader.getClass().getName());
			// 成功
			success = true;
		} catch (Throwable e) {
			e.printStackTrace();
			Logger.fatal(e);
		} finally {
			// 恢复
			if (method != null) {
				method.setAccessible(accessible);
			}
		}

		//		printClass(ClassLoader.getSystemClassLoader().getClass());

		//		printURLs();

		return success;
	}

	//	private void printClass(Class<?> clazz) {
	//		System.out.printf("class is %s\n", clazz.getName());
	//		Class<?> parent = clazz.getSuperclass();
	//		if(parent == null) {
	//			return;
	//		}
	//		printClass(parent);
	//	}

	//	private void printURLs() {
	//		URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
	//		URL[] tokens = loader.getURLs();
	//		for(URL e : tokens) {
	//			System.out.println(e);
	//		}
	//	}

	//	/**
	//	 * 从指定目录加载JAR文件
	//	 * @param path 磁盘目录
	//	 * @return 全部JAR文件加载成功返回真，否则假
	//	 */
	//	public boolean load(String path) {
	//		Laxkit.nullabled(path);
	//		path = ConfigParser.splitPath(path);
	//		// 不允许空目录
	//		if (path.isEmpty()) {
	//			return false;
	//		}
	//		
	//		// 目录
	//		File dir = new File(path);
	//		// 扫描本地文件
	//		ArrayList<File> array = new ArrayList<File>();
	//		scanDisk(array, dir);
	//
	//		// 加载JAR文件
	//		for (File file : array) {
	//			boolean success = loadJAR(file);
	//			// 任何一个加载失败，返回假
	//			if (!success) {
	//				return false;
	//			}
	//		}
	//
	//		return true;
	//	}

}