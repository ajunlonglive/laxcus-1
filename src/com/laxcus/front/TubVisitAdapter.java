/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

import java.lang.reflect.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.tub.invoke.*;
import com.laxcus.tub.turn.*;
import com.laxcus.tub.visit.*;
import com.laxcus.util.lock.*;

/**
 * 远程过程调用适配器（TubVisit接口适配器）。<br><br>
 * 
 * TubVisit适配器位于服务端，它集中管理所有TubVisit接口，为FIXP服务器提供执行RPC的能力。<br><br>
 * 
 * FIXP服务器在站点启动时，将所有接口注册到TubVisit适配器里。在运行过程中，当FIXP服务器收到RPC任务时，
 * 会调用它的“invoke”方法，它通过传入的接口名称，找到对应的类实例，然后调用类实例的对应方法，进行方法调用，
 * 和产生调用结果（以Reply类形式）。从而实现一个RPC操作。
 * 
 * @author scott.liang
 * @version 1.0 10/11/2020
 * @since laxcus 1.0
 */
public class TubVisitAdapter implements TubVisitInvoker {

	/** 多向锁实例 **/
	private MutexLock lock = new MutexLock();

	/** 接口类名称 -> 对象实例。在锁定状态下工作 **/
	private TreeMap<String, Object> tokens = new TreeMap<String, Object>();

	/**
	 * 构造一个TubVisit集合适配器
	 */
	public TubVisitAdapter() {
		super();
	}

	/**
	 * 保存接口类名称和对象实例
	 * @param name 接口名称
	 * @param instance 对象实例
	 */
	private void put(String name, Object instance) {
		lock.lockSingle();
		try {
			tokens.put(name, instance);
		} finally {
			lock.unlockSingle();
		}
	}

	/**
	 * 根据接口名称删除对象实例
	 * @param name 接口名称
	 * @return 返回对象实例或者空指针
	 */
	private Object remove(String name) {
		lock.lockSingle();
		try {
			return tokens.remove(name);
		} finally {
			lock.unlockSingle();
		}
	}

	/**
	 * 根据接口名称返回对象实例
	 * @param name 接口名称
	 * @return 返回对象实例或者空指针
	 */
	private Object get(String name) {
		lock.lockMulti();
		try {
			return tokens.get(name);
		} finally {
			lock.unlockMulti();
		}
	}

	/**
	 * 检查传入的类是继承自TubVisit接口
	 * @param clazz 类定义
	 * @return 如果实现TubVisit接口，返回真，否则返回假。
	 */
	private boolean isTubVisit(Class<?> clazz) {
		if (clazz == TubVisit.class) {
			return true;
		}
		// 枚举它的接口
		Class<?>[] array = clazz.getInterfaces();
		int size = (array == null ? 0 : array.length);
		for (int i = 0; i < size; i++) {
			if (isTubVisit(array[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 注册TubVisit接口实例
	 * @param clazz TubVisit接口类
	 * @return 如果成功返回true，否则返回false。
	 */
	public boolean addTubVisit(Class<?> clazz) {
		int count = 0;
		try {
			// 生成实例
			Object instance = clazz.newInstance();
			// 枚举它的接口
			Class<?>[] array = clazz.getInterfaces();
			for (int i = 0; array != null && i < array.length; i++) {
				// 判断是否源自TubVisit接口，忽略非TubVisit接口
				if (!isTubVisit(array[i])) {
					continue;
				}
				// 类的全路径名称
				String name = array[i].getName();
				put(name, instance);
				count++;
			}
		} catch (InstantiationException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		}
		return count > 0;
	}

	/**
	 * 加载一个继承了TubVisit接口的实例
	 * @param that TubVisit实例
	 * @return 接受返回true，否则返回false。
	 */
	public boolean addInstance(Object that) {
		int count = 0;
		Class<?> clazz = that.getClass();
		// 检举它的接口
		Class<?>[] array = clazz.getInterfaces();
		for (int i = 0; array != null && i < array.length; i++) {
			// 判断当前接口是否源自TubVisit，忽略非TubVisit
			if (!isTubVisit(array[i])) {
				continue;
			}
			// 类的全路径名称
			String name = array[i].getName();
			put(name, that);
			count++;
		}
		return count > 0;
	}

	/**
	 * 根据类名称，注销TubVisit接口实例
	 * @param className 类全路径名称
	 * @return 成功返回true，失败返回false。
	 */
	public boolean deleteTubVisit(String className) {
		return remove(className) != null;
	}

	/**
	 * 根据类定义查找类实例
	 * @param clazz 类定义
	 * @return 返回类对象
	 */
	public Object findTubVisit(Class<?> clazz) {
		return findTubVisit(clazz.getName());
	}

	/**
	 * 根据类名称查找类实例
	 * @param className 类名
	 * @return 返回类对象
	 */
	@Override
	public Object findTubVisit(String className) {
		return get(className);
	}

	/**
	 * 执行RPC调用
	 * @param apply TubCreator请求实例
	 * @return 返回经过RPC处理后的TubExtractor应答实例
	 */
	@Override
	public TubExtractor invoke(TubConstructor apply) {
		if (apply == null) {
			return new TubExtractor(null, new NullPointerException("null point request!"));
		}
		// TubVisit接口名称
		String interface_name = apply.getInterfaceName();
		// TubVisit里面的方法名
		String method_name = apply.getMethodName();
		// 方法里面的参数类型
		Class<?>[] types = apply.getParameterTypes();
		// 方法中的参数值
		Object[] params = apply.getParameters();

		// RPC应答
		TubExtractor reply = new TubExtractor();

		// 找到对应的实例
		Object instance = get(interface_name);
		if (instance == null) {
			Logger.error(this, "invoke", "cannot be find [%s]", interface_name);
			reply.setThrowable(new ClassNotFoundException("cannot be find class: " + interface_name));
			return reply;
		}

		try {
			// 根据方法名称和方法中的类型，找到一个方法实例
			Method method = instance.getClass().getMethod(method_name, types);
			// 调用方法
			Object res = method.invoke(instance, params);
			reply.setObject(res);
		} catch (IllegalAccessException e) {
			Logger.error(e);
			reply.setThrowable(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
			reply.setThrowable(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
			reply.setThrowable(e);
		} catch (SecurityException e) {
			Logger.error(e);
			reply.setThrowable(e);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
			reply.setThrowable(e);
		} catch (Throwable e) {
			Logger.fatal(e);
			reply.setThrowable(e);
		}
		// 返回应答
		return reply;
	}

}