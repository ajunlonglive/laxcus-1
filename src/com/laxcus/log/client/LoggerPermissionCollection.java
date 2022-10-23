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

/**
 * 日志操作权限集合
 * 
 * @author scott.liang
 * @version 1.0 5/16/2018
 * @since laxcus 1.0
 */
final class LoggerPermissionCollection extends PermissionCollection {

	private static final long serialVersionUID = -3104708089571368659L;

	/** 日志权限存取数组 **/
	@SuppressWarnings("rawtypes")
	private transient ArrayList array;

	/**
	 * 构造默认的日志权限集合
	 */
	@SuppressWarnings("rawtypes")
	public LoggerPermissionCollection() {
		array = new ArrayList();
	}

	/* (non-Javadoc)
	 * @see java.security.PermissionCollection#add(java.security.Permission)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void add(Permission permission) {
		if (!(permission instanceof LoggerPermission)) {
			throw new IllegalArgumentException("invalid permission: "
					+ permission);
		}

		// 保存它！
		synchronized (this) {
			array.add((LoggerPermission) permission);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.PermissionCollection#implies(java.security.Permission)
	 */
	@Override
	public boolean implies(Permission permission) {
		if (!(permission instanceof LoggerPermission)) {
			return false;
		}

		LoggerPermission that = (LoggerPermission) permission;
		synchronized (this) {
			int size = array.size();
			for (int i = 0; i < size; i++) {
				LoggerPermission e = (LoggerPermission) array.get(i);
				if (e.implies(that)) {
					return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.security.PermissionCollection#elements()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<Permission> elements() {
		synchronized (this) {
		    return Collections.enumeration(array);
		}
	}

}