/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.naming.*;

/**
 * 系统注册环境
 * 
 * 保存所有的注册信息。
 * 注册表实例只能由系统来定义，用户在注册表下增加、删除信息。
 * 
 * @author scott.liang
 * @version 1.0 7/15/2021
 * @since laxcus 1.0
 */
public final class RTEnvironment extends MutexHandler {

	/** 根定义 **/
	public final static Naming CLASSES_TYPE = new Naming("CLASSES_TYPE");

	public final static Naming ENVIRONMENT_USER = new Naming("ENVIRONMENT_USER");

	public final static Naming ENVIRONMENT_SYSTEM = new Naming("ENVIRONMENT_SYSTEM");

	public final static Naming ENVIRONMENT_CONFIG = new Naming("ENVIRONMENT_CONFIG");

	/** 句柄实例 **/
	private static RTEnvironment selfHandle = new RTEnvironment();

	/** 名称 -> 根实例 **/
	private Map<Naming, RTable> tables = new TreeMap<Naming, RTable>();

	/** 判断为可用 **/
	private volatile boolean usabled = false;

	/** 发生更新或者否 **/
	private volatile boolean updated;
	
	/**
	 * 构造系统注册表
	 */
	private RTEnvironment() {
		super();
		usabled = false;
		updated = false;
	}

	/**
	 * 返回实例
	 * @return RTEnvironment实例
	 */
	public static RTEnvironment getInstance() {
		return RTEnvironment.selfHandle;
	}

	/**
	 * 判断为可用
	 * @return
	 */
	public boolean isUsabled() {
		return usabled;
	}

	/**
	 * 设置为可用或者否
	 * @param b
	 */
	public void setUsabled(boolean b) {
		usabled = b;
	}

	/**
	 * 判断发生更新
	 * @param b
	 */
	public void setUpdated(boolean b) {
		updated = b;
	}
	
	/**
	 * 判断已经更新
	 * @return
	 */
	public boolean isUpdated() {
		return updated;
	}

	/**
	 * 追加
	 * @param table
	 */
	private void addTable(RTable table) {
		// 锁定
		super.lockSingle();
		try {
			tables.put(table.getName(), table);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 返回全部名称
	 * @return
	 */
	public List<Naming> getTableNames() {
		super.lockMulti();
		try {
			return new ArrayList<Naming>(tables.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 找到匹配的注册表
	 * @param root 根命名
	 * @return 返回实例，或者是空指针
	 */
	public RTable findTable(Naming root) {
		super.lockMulti();
		try {
			return tables.get(root);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 查找一个成员
	 * @param root 根命名
	 * @param elementName 成员命名
	 * @return 返回RElement
	 */
	public RElement findElement(Naming root, Naming elementName) {
		RTable table = findTable(root);
		if (table != null) {
			return table.find(elementName);
		}
		return null;
	}
	
	/**
	 * 删除完整的成员
	 * @param root
	 * @param elementName
	 * @return
	 */
	public boolean removeElement(Naming root, Naming elementName) {
		boolean success = false;
		RTable table = findTable(root);
		if (table != null) {
			success = table.remove(elementName);
		}
		// 成功 
		if (success) {
			setUpdated(true);
		}
		return success;
	}

	/**
	 * 查找一个成员
	 * @param root 命名
	 * @param elementName 命名
	 * @return 返回RElement
	 */
	public RElement findElement(Naming root, String elementName) {
		return findElement(root, new Naming(elementName));
	}
	
	/**
	 * 查找标记
	 * @param root
	 * @param elementName
	 * @param tokenNames
	 * @param defaultToken
	 * @return
	 */
	public RToken findToken(Naming root, Naming elementName,
			Naming[] tokenNames, byte attribute, RToken defaultToken) {
		// 找到成员
		RElement element = findElement(root, elementName);
		if (element == null) {
			return defaultToken;
		}
		// 返回查找结果
		return element.find(tokenNames, attribute, defaultToken);
	}
	
	/**
	 * 采用全路径格式，查找标记值。路径以“/”符号分隔。
	 * @param paths 路径
	 * @param defaultToken 默认参量
	 * @return 返回查找结果
	 */
	public RToken findToken(String paths, byte attribute, RToken defaultToken) {
		String[] texts = paths.split("/");
		if (texts.length < 3) {
			return defaultToken;
		}

		// 根
		Naming root = new Naming(texts[0]);
		// element name
		Naming elementName = new Naming(texts[1]);

		Naming[] tokenNames = new Naming[texts.length - 2];
		for (int i = 2; i < texts.length; i++) {
			tokenNames[i - 2] = new Naming(texts[i]);
		}
		return findToken(root, elementName, tokenNames, attribute, defaultToken);
	}
	
	/**
	 * 采用全路径格式，查找标记值。路径以“/”符号分隔。
	 * @param root 根命名
	 * @param paths 成员路径
	 * @param attribute 属性（目录或者参数）
	 * @param defaultToken 默认参量
	 * @return 返回查找结果
	 */
	public RToken findToken(Naming root, String paths, byte attribute, RToken defaultToken) {
		paths = root.toString() + "/" + paths;
		return findToken(paths, attribute, defaultToken);
	}
	
	/**
	 * 找到目录
	 * @param root
	 * @param paths
	 * @return
	 */
	public RFolder findFolder(Naming root, String paths) {
		return (RFolder) findToken(root, paths, RTokenAttribute.FOLDER, null);
	}

	/**
	 * 找到参量
	 * @param root
	 * @param paths
	 * @return
	 */
	public RParameter findParameter(Naming root, String paths) {
		return (RParameter) findToken(root, paths, RTokenAttribute.PARAMETER, null);
	}

	/**
	 * 增加一个成员
	 * @param root
	 * @param element
	 * @return
	 */
	public boolean addElement(Naming root, RElement element) {
		boolean success = false;
		RTable table = findTable(root);
		if (table != null) {
			success = table.add(element);
		}
		// 成功，记录更新
		if (success) {
			setUpdated(true);
		}
		return false;
	}

	/**
	 * 判断存在
	 * @param name
	 * @return
	 */
	public boolean contains(Naming name) {
		return findTable(name) != null;
	}

	/**
	 * 解析全部的信息，只能执行一次
	 * @param b
	 */
	public void infuse(byte[] b) {
		if (tables.size() > 0) {
			throw new IllegalStateException("multi split!");
		}

		// 从可类化读取器中解析
		ClassReader reader = new ClassReader(b);

		// 读取参数
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RTable table = new RTable(reader);
			addTable(table);
		}

		// 如果参数不足时，追加
		if (!contains(RTEnvironment.CLASSES_TYPE)) {
			addTable(new RTable(RTEnvironment.CLASSES_TYPE));
		}
		// 如果参数不足时，追加
		if (!contains(RTEnvironment.ENVIRONMENT_CONFIG)) {
			addTable(new RTable(RTEnvironment.ENVIRONMENT_CONFIG));
		}
		// 如果参数不足时，追加
		if (!contains(RTEnvironment.ENVIRONMENT_USER)) {
			addTable(new RTable(RTEnvironment.ENVIRONMENT_USER));
		}
		// 如果参数不足时，追加
		if (!contains(RTEnvironment.ENVIRONMENT_SYSTEM)) {
			addTable(new RTable(RTEnvironment.ENVIRONMENT_SYSTEM));
		}
	}

	/**
	 * 解析全部信息
	 * @param file 磁盘文件
	 * @throws IOException
	 */
	public void infuse(File file) throws IOException {
		int len = (int) file.length();

		// 从磁盘读取
		FileInputStream in = new FileInputStream(file);
		byte[] b = new byte[len];
		in.read(b);
		in.close();
		// 解析
		infuse(b);
	}

	/**
	 * 输出到磁盘
	 * @param file
	 * @throws IOException
	 */
	public void effuse(File file) throws IOException {
		byte[] stream = effuse();
		FileOutputStream out = new FileOutputStream(file);
		out.write(stream);
		out.flush();
		out.close();
	}

	/**
	 * 输出到磁盘
	 * @return 字节数组
	 */
	public byte[] effuse() {
		ClassWriter writer = new ClassWriter();
		// 锁定
		super.lockSingle();
		try {
			writer.writeInt(tables.size());
			for (RTable table : tables.values()) {
				writer.writeObject(table);
				// System.out.printf("write root %s\n", table.getName());
			}
		} catch (Throwable e) {

		} finally {
			super.unlockSingle();
		}
		// 输出为字节数组
		return writer.effuse();
	}

	/**
	 * 生成一个默认的运行时环境
	 */
	public void createDefault() {
		if (tables.size() > 0) {
			throw new IllegalStateException("wrong!");
		}

		addTable(new RTable(RTEnvironment.CLASSES_TYPE));
		addTable(new RTable(RTEnvironment.ENVIRONMENT_CONFIG));
		addTable(new RTable(RTEnvironment.ENVIRONMENT_USER));
		addTable(new RTable(RTEnvironment.ENVIRONMENT_SYSTEM));
	}

}