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
 * 操作操作权限集合
 * 
 * @author scott.liang
 * @version 1.0 1/6/2020
 * @since laxcus 1.0
 */
final class TiggerPermissionCollection extends PermissionCollection {

	private static final long serialVersionUID = -3104708089571368659L;

	/** 操作权限存取数组 **/
	@SuppressWarnings("rawtypes")
	private transient ArrayList array;

	/**
	 * 构造默认的操作权限集合
	 */
	@SuppressWarnings("rawtypes")
	public TiggerPermissionCollection() {
		array = new ArrayList();
	}

	/* (non-Javadoc)
	 * @see java.security.PermissionCollection#add(java.security.Permission)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void add(Permission permission) {
		if (!(permission instanceof TiggerPermission)) {
			throw new IllegalArgumentException("invalid permission: "
					+ permission);
		}

		// 保存它！
		synchronized (this) {
			array.add((TiggerPermission) permission);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.PermissionCollection#implies(java.security.Permission)
	 */
	@Override
	public boolean implies(Permission permission) {
		if (!(permission instanceof TiggerPermission)) {
			return false;
		}

		TiggerPermission that = (TiggerPermission) permission;
		synchronized (this) {
			int size = array.size();
			for (int i = 0; i < size; i++) {
				TiggerPermission e = (TiggerPermission) array.get(i);
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