/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.watch;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;

import com.laxcus.access.diagram.*;
import com.laxcus.log.client.*;
import com.laxcus.site.watch.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;
import com.laxcus.xml.*;

/**
 * WATCH登录账号管理器
 * 
 * @author scott.liang
 * @version 1.0 6/12/2015
 * @since laxcus 1.0
 */
public final class WatchManager {
	
	class FileTag implements Comparable<FileTag> {
		/** 文件长度 **/
		long length;

		/** 文件时间 **/
		long time;
		
		public FileTag(long len, long tm) {
			super();
			length = len;
			time = tm;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(FileTag that) {
			int ret = Laxkit.compareTo(length, that.length);
			if(ret == 0) {
				ret = Laxkit.compareTo(time, that.time);
			}
			return ret;
		}
	}

	/** 本地文件标识 **/
	private FileTag current;

	/** 本地文件名 **/
	private String filename;

	/** 注册用户 **/
	private TreeSet<WatchUser> array = new TreeSet<WatchUser>();

	/**
	 * 构造WATCH登录账号管理器
	 */
	public WatchManager() {
		super();
	}

	/**
	 * 设置文件名
	 * @param path 文件路径
	 */
	public void setFile(String path) {
		filename = ConfigParser.splitPath(path);
	}

	/**
	 * 返回文件名
	 * @return 字符串文件名
	 */
	public String getFile() {
		return filename;
	}
	
	/**
	 * 判断文件已经更新
	 * @return 返回真或者假
	 */
	public boolean hasRefresh() {
		if (filename == null) {
			return false;
		}

		File file = new File(filename);
		if (!file.exists()) {
			return false;
		}

		// 不存在，返回TRUE
		if (current == null) {
			current = new FileTag(file.length(), file.lastModified());
			return true;
		}

		// 比较前后参数一致
		FileTag next = new FileTag(file.length(), file.lastModified());
		boolean refresh = (current.compareTo(next) != 0);
		if (refresh) {
			current = next;
		}

		return refresh;
	}

	/**
	 * 判断包含
	 * @param user
	 * @return 返回真或者假
	 */
	public boolean contains(WatchUser user) {
		return array.contains(user);
	}

	/**
	 * 统计WATCH用户数目
	 * @return 整数值
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * 保存WATCH站点账号
	 * @param user WATCH账号
	 * @return 保存成功返回真，否则假
	 */
	private boolean add(WatchUser user) {
		Laxkit.nullabled(user);
		return array.add(user);
	}
	
	/**
	 * 重新加载配置
	 * 账号有两种格式：
	 * 1. 完全的16进制字符串
	 * 2. 字符串明文文本
	 */
	public boolean reload() {
		// 判断是空
		if (filename == null) {
			return false;
		}
		// 文件不存在
		File file = new File(filename);
		if (!file.exists()) {
			return false;
		}
		
		// 读XML内容
		Document document = XMLocal.loadXMLSource(file);
		if (document == null) {
			return false;
		}
		
		// 清除旧记录
		array.clear();
		
		// 解析新的参数
		NodeList list = document.getElementsByTagName("account");
		int size = list.getLength();
		
		for (int i = 0; i < size; i++) {
			Element element = (Element) list.item(i);
			String username = XMLocal.getValue(element, "username");
			String password = XMLocal.getValue(element, "password");
			
			boolean success = false;
			// 两种：<1> 16进制字符串 <2> 明文文本
			if (Siger.validate(username) && SHA512Hash.validate(password)) {
				Siger siger = new Siger(username);
				SHA512Hash pwd = new SHA512Hash(password);
				WatchUser user = new WatchUser(siger, pwd);
				success = add(user);
			} else {
				Siger siger = SHAUser.doUsername(username);
				SHA512Hash pwd = SHAUser.doPassword(password);
				WatchUser user = new WatchUser(siger, pwd);
				success = add(user);
			}
			
			Logger.debug(this, "reload", success, "%s # %s", username, password);
		}

		return true;
	}
	
//	public static void main(String args[]) {
//		String filename = "E:/parallel/top/conf/watches.xml";
//		WatchManager u = new WatchManager();
//		u.setFile(filename);
//		System.out.printf("has refresh is %s\n", u.hasRefresh());
//		System.out.printf("has refresh is %s\n", u.hasRefresh());
//		u.reload();
//	}
}
