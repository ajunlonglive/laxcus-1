/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access;

import java.security.*;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.util.*;

/**
 * JNI方法访问许可。<br>
 * 防止分布任务组件对本地存取接口的非法调用。<br><br>
 * 
 * 在各节点data/build节点上的 site.policy 文件中的存取格式：<br>
 * <1> permission com.laxcus.access.AccessPermission "using.*"; 允许所有用户使用 <br>
 * <2> permission com.laxcus.access.AccessPermission "using.方法名称, using.方法名称"; 允许所有用户一个或者几个方法 <br>
 * <3> permission com.laxcus.access.AccessPermission "using.*", "用户签名（64个16进制字符，忽略大小写）"; 允许某个用户使用全部日志 <br>
 * <4> permission com.laxcus.access.AccessPermission "using.*", "用户签名（64个16进制字符，忽略大小写）,用户签名"; 允许某个用户使用其中一种或者几种日志，多个用户签名用逗号隔开。 <br> 
 * <5> permission com.laxcus.access.AccessPermission "using.*", "用户明文名称,用户明文名称"; 允许多个用户名称一起，中间用逗号隔开 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/23/2014
 * @since laxcus 1.0
 */
public class AccessPermission extends BasicPermission {

	private static final long serialVersionUID = 9093667824931807718L;

	/** 一组用户签名  **/
	private Siger[] sigers;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		sigers = null;
	}

	/**
	 * 构造访问操作许可
	 * @param name 目标名称
	 */
	public AccessPermission(String name) {
		super(name);
	}

	/**
	 * 构造访问操作许可
	 * @param name  目标名称
	 * @param actions 操作行为
	 */
	public AccessPermission(String name, String actions) {
		super(name, actions);
		setSiger(actions);
	}

	/**
	 * 设置用户签名
	 * @param input 用户签名
	 */
	public void setSiger(String input) {
		Laxkit.nullabled(input);
		// 如果是空字符串，弹出异常
		if (input.trim().isEmpty()) {
			throw new IllegalValueException("illegal signer:%s", input);
		}
		
		String[] items = input.split(",");
		TreeSet<Siger> array = new TreeSet<Siger>();
		for (String item : items) {
			item = item.trim();
			if (item.isEmpty()) {
				continue;
			}
			// 是16进制的SHA256签名，或者是普通的明文时...
			if (Siger.validate(item)) {
				array.add(new Siger(item));
			} else {
				array.add(SHAUser.doUsername(item));
			}
		}

		// 保存一批用户签名
		if (array.size() > 0) {
			sigers = new Siger[array.size()];
			array.toArray(sigers);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * @see java.security.BasicPermission#getActions()
	 */
	@Override
	public String getActions() {
		if (sigers == null) {
			return "";
		}
		// 数组
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < sigers.length; i++) {
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append(sigers[i].toString());
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.security.BasicPermission#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = getName().hashCode();
		if (sigers != null) {
			for (Siger e : sigers) {
				hash = hash ^ e.hashCode();
			}
		}
		return hash;
	}
	
	/**
	 * 判断参数完全一致！
	 * @param s1 比较数组
	 * @param s2 比较数组
	 * @return 返回真或者假
	 */
	private boolean equals(Siger[] s1, Siger[] s2) {
		boolean success = (s1 != null && s2 != null && s1.length == s2.length);
		if (success) {
			int count = 0;
			for (int n = 0; s1 != null && n < s1.length; n++) {
				int num = 0;
				for (int m = 0; s2 != null && m < s2.length; m++) {
					if (Laxkit.compareTo(s1[n], s2[m]) == 0) {
						num++; // 一致，统计值加1
					}
				}
				if (num == 1) {
					count++;
				}
			}
			success = (count == s1.length);
		}
		return success;
	}
	
	/**
	 * 判断参数有匹配
	 * @param s1 签名数组
	 * @param s2 签名数组
	 * @return 返回真或者假
	 */
	private boolean matchs(Siger[] s1, Siger[] s2) {
		for (int n = 0; s1 != null && n < s1.length; n++) {
			for (int m = 0; s2 != null && m < s2.length; m++) {
				if (Laxkit.compareTo(s1[n], s2[m]) == 0) {
					return true;
				}
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
		if (!(permission instanceof AccessPermission)) {
			return false;
		}

		AccessPermission that = (AccessPermission) permission;

		// 判断一致
		boolean successful = super.equals(permission);
		if (successful) {
			successful = equals(sigers, that.sigers);
		}

		return successful;
	}

	/*
	 * (non-Javadoc)
	 * @see java.security.BasicPermission#implies(java.security.Permission)
	 */
	@Override
	public boolean implies(Permission permission) {
		if (!(permission instanceof AccessPermission)) {
			return false;
		}

		AccessPermission that = (AccessPermission) permission;

		// 判断一致
		boolean match = false;
		if (sigers == null) {
			match = true; // site.policy 没有定义用户，表示适合所有注册用户
		} else if (sigers != null) { // site.policy定义了用户
			if (that.sigers != null) {
				match = matchs(sigers, that.sigers); // 判断其中有签名一致
			} else {
				match = false; // 传入参数没有定义用户，这种情况拒绝！
			}
		}

		return match && super.implies(permission);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.security.BasicPermission#newPermissionCollection()
	 */
	@Override
	public PermissionCollection newPermissionCollection() {
		return new AccessPermissionCollection();
	}

}