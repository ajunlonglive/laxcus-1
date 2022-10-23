/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

import java.security.*;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.util.*;

/**
 * 日志操作许可 <br>
 * 
 * 在运行过程中，对日志的方法调用时的检查<br><br>
 * 
 * 在各节点 site.policy 文件中的日志格式：<br>
 * <1> permission com.laxcus.log.client.LoggerPermission "using.*"; 允许所有用户使用日志 <br>
 * <2> permission com.laxcus.log.client.LoggerPermission "using.debug|using.info|using.warning|using.error|using.fatal"; 允许所有用户使用其中一种日志 <br>
 * <3> permission com.laxcus.log.client.LoggerPermission "using.*", "用户签名（64个16进制字符，忽略大小写）"; 允许某个用户使用全部日志 <br><br>
 * <4> permission com.laxcus.log.client.LoggerPermission "using.debug|using.info|using.warning|using.error|using.fatal", "用户签名（64个16进制字符，忽略大小写）,用户签名"; 允许某个用户使用其中一种或者几种日志，多个用户签名用逗号隔开。 <br><br> 
 * <5> permission com.laxcus.log.client.LoggerPermission "using.debug|using.info|using.warning|using.error|using.fatal", "用户明文名称,用户明文名称"; 允许多个用户名称一起，中间用逗号隔开 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 5/16/2018
 * @since laxcus 1.0
 */
public class LoggerPermission extends BasicPermission {

	private static final long serialVersionUID = -3185899727631655429L;

	/** 一组用户签名  **/
	private TreeSet<Siger> issuers = new TreeSet<Siger>();
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		if (issuers.size() > 0) {
			issuers.clear();
		}
	}

	/**
	 * 构造日志操作许可
	 * @param name 目标名称
	 */
	public LoggerPermission(String name) {
		super(name);
	}

	/**
	 * 构造日志操作许可
	 * @param name 目标名称
	 * @param actions 操作行为，在这里是用户签名，只是一个！
	 */
	public LoggerPermission(String name, String actions) {
		super(name, actions);
		setIssuers(actions);
	}

	/**
	 * 设置用户签名
	 * @param input 用户签名
	 */
	private void setIssuers(String input) {
		Laxkit.nullabled(input);
		// 如果是空字符串，弹出异常
		if (input.trim().isEmpty()) {
			throw new IllegalValueException("illegal signer:%s", input);
		}
		
		// 以逗号为分隔符，切开它们！
		String[] items = input.split(",");
		for (String item : items) {
			item = item.trim();
			if (item.isEmpty()) {
				continue;
			}
			// 生成一个签名
			Siger user = SHAUser.doSiger(item);
			if (user != null) {
				issuers.add(user);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.security.BasicPermission#getActions()
	 */
	@Override
	public String getActions() {
		if (issuers.isEmpty()) {
			return "";
		}
		// 数组
		StringBuilder buff = new StringBuilder();
		for (Siger siger : issuers) {
			if (buff.length() > 0) {
				buff.append(",");
			}
			buff.append(siger.toString());
		}
		return buff.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.security.BasicPermission#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = getName().hashCode();
		for (Siger e : issuers) {
			hash = hash ^ e.hashCode();
		}
		return hash;
	}
	
	/**
	 * 判断参数完全一致！
	 * @param array1 比较数组
	 * @param array2 比较数组
	 * @return 返回真或者假
	 */
	private boolean equals(Set<Siger> array1, Set<Siger> array2) {
		boolean success = (array1.size() == array2.size());
		if (success) {
			int count = 0;
			for (Siger siger : array1) {
				if (array2.contains(siger)) {
					count++;
				}
			}
			success = (count == array1.size());
		}
		return success;
	}
	
	/**
	 * 判断参数有匹配
	 * @param array1 签名数组
	 * @param array2 签名数组
	 * @return 返回真或者假
	 */
	private boolean matchs(Set<Siger> array1, Set<Siger> array2) {
		for (Siger siger : array1) {
			if (array2.contains(siger)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.security.BasicPermission#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object permission) {
		if (permission == this) {
			return true;
		}
		if (!(permission instanceof LoggerPermission)) {
			return false;
		}

		LoggerPermission that = (LoggerPermission) permission;

		// 判断一致
		boolean successful = super.equals(permission);
		if (successful) {
			successful = equals(issuers, that.issuers);
		}

		return successful;
	}

	/*
	 * (non-Javadoc)
	 * @see java.security.BasicPermission#implies(java.security.Permission)
	 */
	@Override
	public boolean implies(Permission permission) {
		if (!(permission instanceof LoggerPermission)) {
			return false;
		}

		LoggerPermission that = (LoggerPermission) permission;

		// 判断一致
		boolean match = false;
		if (issuers .isEmpty()) {
			match = true; // site.policy 没有定义用户，表示适合所有注册用户
		} else {
			match = matchs(issuers, that.issuers); // 判断其中有签名一致
		}
		
		return match && super.implies(permission);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.security.BasicPermission#newPermissionCollection()
	 */
	@Override
	public PermissionCollection newPermissionCollection() {
		return new LoggerPermissionCollection();
	}
}