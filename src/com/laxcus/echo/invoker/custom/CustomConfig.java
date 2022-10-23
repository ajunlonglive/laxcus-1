/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

import java.io.*;

import com.laxcus.util.*;

/**
 * 自定义资源配置。<br>
 * 
 * 包括JAR文件存储目录和COMMAND/INVOKER配置声明文件，两者缺一不可。
 * 
 * @author scott.liang
 * @version 1.0 11/4/2017
 * @since laxcus 1.0
 */
public final class CustomConfig {
	
	/** 自动更新磁盘JAR文件，默认是真 **/
	private static boolean autoUpdate = true;

	/** 自定义JAR文件存储目录 **/
	private static String directory;
	
	/** COMMAND/INVOKER配置声明文件 **/
	private static String statement;
	
	/** 自定义标签文件路径，标签文件在JAR包里 **/
	private static String tokenPath;
	
	/**
	 * 判断参数有效
	 * @return 返回真或者假
	 */
	public static boolean isValidate() {
		// 判断不是空指针
		boolean success = (CustomConfig.directory != null && CustomConfig.statement != null);
		// 判断目录存在
		if (success) {
			File file = new File(CustomConfig.directory);
			success = (file.exists() && file.isDirectory());
		}
		// 判断配置文件存在
		if (success) {
			File file = new File(CustomConfig.statement);
			success = (file.exists() && file.isFile());
		}
		return success;
	}
	
	/**
	 * 设置自动更新J磁盘AR包
	 * @param b 自动更新
	 */
	public static void setAutoUpdate(boolean b) {
		CustomConfig.autoUpdate = b;
	}

	/**
	 * 判断是自动更新J磁盘AR包
	 * @return 返回真或者假
	 */
	public static boolean isAutoUpdate() {
		return CustomConfig.autoUpdate;
	}

	/**
	 * 设置自定义JAR文件存储目录
	 * @param path 文件目录
	 */
	public static void setDirectory(String path) {
		Laxkit.nullabled(path);
		directory = ConfigParser.splitPath(path);
	}

	/**
	 * 返回自定义JAR文件存储目录
	 * @return 文件目录
	 */
	public static String getDirectory() {
		return directory;
	}

	/**
	 * 设置COMMAND/INVOKER配置声明文件
	 * @param path 声明文件
	 */
	public static void setStatement(String path) {
		Laxkit.nullabled(path);
		CustomConfig.statement = ConfigParser.splitPath(path);
	}

	/**
	 * 返回COMMAND/INVOKER配置声明文件
	 * @return 声明文件
	 */
	public static String getStatement() {
		return CustomConfig.statement;
	}
	
	/**
	 * 设置自定义标签文件路径
	 * @param e 自定义标签文件路径
	 */
	public static void setTokenPath(String e) {
		Laxkit.nullabled(e);
		CustomConfig.tokenPath = ConfigParser.splitPath(e);
	}

	/**
	 * 返回自定义标签文件路径
	 * @return 自定义标签文件路径
	 */
	public static String getTokenPath() {
		return CustomConfig.tokenPath;
	}

}