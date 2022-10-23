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

/**
 * 访问权限集合
 * 
 * @author scott.liang
 * @version 1.0 7/23/2014
 * @since laxcus 1.0
 */
final class AccessPermissionCollection extends PermissionCollection {

	private static final long serialVersionUID = -3104708089571368659L;

	/** 日志权限存取数组 **/
	@SuppressWarnings("rawtypes")
	private transient ArrayList array;

	/**
	 * 构造默认的日志权限集合
	 */
	@SuppressWarnings("rawtypes")
	public AccessPermissionCollection() {
		array = new ArrayList();
	}

	/* (non-Javadoc)
	 * @see java.security.PermissionCollection#add(java.security.Permission)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void add(Permission permission) {
		if (!(permission instanceof AccessPermission)) {
			throw new IllegalArgumentException("invalid permission: "
					+ permission);
		}

		// 保存它！
		synchronized (this) {
			array.add((AccessPermission) permission);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.PermissionCollection#implies(java.security.Permission)
	 */
	@Override
	public boolean implies(Permission permission) {
		if (!(permission instanceof AccessPermission)) {
			return false;
		}

		AccessPermission that = (AccessPermission) permission;
		synchronized (this) {
			int size = array.size();
			for (int i = 0; i < size; i++) {
				AccessPermission e = (AccessPermission) array.get(i);
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