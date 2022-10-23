/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform;

import java.lang.reflect.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.platform.listener.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 事件广播器
 * 
 * @author scott.liang
 * @version 1.0 3/5/2022
 * @since laxcus 1.0
 */
class PlatformMulticaster extends MutexHandler {
	
	/** 平台监听器 **/
	private ArrayList<PlatformListener> array = new ArrayList<PlatformListener>();

	/**
	 * 
	 */
	public PlatformMulticaster() {
		super();
	}

	/**
	 * 成员数
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 注册
	 * @param l
	 * @return
	 */
	public boolean register(PlatformListener l) {
		if (l != null) {
			super.lockSingle();
			try {
				return array.add(l);
			} finally {
				super.unlockSingle();
			}
		}
		return false;
	}

	/**
	 * 注销
	 * @param l
	 * @return
	 */
	public boolean unregister(PlatformListener l) {
		if (l != null) {
			super.lockSingle();
			try {
				return array.remove(l);
			} finally {
				super.unlockSingle();
			}
		}
		return false;
	}

	/**
	 * 返回全部匹配的监听接口
	 * @param <T> 类类型
	 * @param clazz PlatformListener接口类
	 * @return 基于PlatformListener的实例
	 */
	@SuppressWarnings("unchecked")
	public <T extends PlatformListener> T[] findListeners(Class<?> clazz) {
		// 锁定
		super.lockMulti();
		try {
			int count = 0;
			for (PlatformListener pl : array) {
				boolean success = Laxkit.isInterfaceFrom(pl, clazz);
				if (success) {
					count++;
				}
			}
			// 大于0时
			if (count > 0) {
				T[] res = (T[]) Array.newInstance(clazz, count);
				int index = 0;
				for (PlatformListener pl : array) {
					boolean success = Laxkit.isInterfaceFrom(pl, clazz);
					if (success) {
						res[index++] = (T) pl;
					}
				}
				return res;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		// 没有，返回0数组
		return (T[]) Array.newInstance(clazz, 0);
	}
	
	/**
	 * 返回记录中的监听器，这个方法对应系统级监听器。系统监听器在运行环境中只有一个
	 * @param <T> 类类型
	 * @param clazz PlatformListener接口类
	 * @return 返回对应实例 ，没有是空指针
	 */
	@SuppressWarnings("unchecked")
	public <T extends PlatformListener> T findListener(Class<?> clazz) {
		// 锁定
		super.lockMulti();
		try {
			for (PlatformListener pl : array) {
				boolean success = Laxkit.isInterfaceFrom(pl, clazz);
				if (success) {
					return (T) pl;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

}
