/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.task.guide.parameter.*;
import com.laxcus.util.naming.*;

/**
 * 引导任务 <br><br>
 * 
 * 根据基础字和录入参数产生分布计算命令。<br>
 * 由用户派生实现。
 * 
 * @author scott.liang
 * @version 1.0 7/25/2012
 * @since laxcus 1.0
 */
public abstract class GuideTask {

	/**
	 * 构造默认的引导任务
	 */
	public GuideTask() {
		super();
	}

	/**
	 * 返回支持的基础字
	 * @return Sock列表
	 */
	public abstract List<Sock> getSocks();

	/**
	 * 判断支持某个基础字
	 * 
	 * @param sock 基础字
	 * @return 返回真或者假
	 */
	public abstract boolean isSupport(Sock sock);

	/**
	 * 产生标注参数
	 * 
	 * 实际类输出接口，由用户输入后，调用“create”方法产生分布计算命令
	 * @param sock 基础字
	 * @return 启动参数列表
	 * @throws GuideTaskException
	 */
	public abstract InputParameterList markup(Sock sock) throws GuideTaskException;

	/**
	 * 根据基础字和录入参数，产生分布命令
	 * @param sock 基础字
	 * @param list 已经录入的参数
	 * @return 返回生成的DistributeCommand分布命令，包括有“Conduct/Contact/Establish”
	 * @throws GuideTaskException
	 */
	public abstract DistributedCommand create(Sock sock, InputParameterList list) throws GuideTaskException;

	/**
	 * 在前次计算结果上，根据前次计算结果和基础字，产生新分布计算命令，形成迭代。是在“create”方法之后调用。
	 * 
	 * @param predata 上个阶段产生的结果数据，需要开发者自行解析
	 * @param sock 基础字
	 * @return 返回生成的DistributeCommand分布命令，包括有“Conduct/Contact/Establish”
	 * @throws GuideTaskException
	 */
	public abstract DistributedCommand next(byte[] predata, Sock sock) throws GuideTaskException;
}

//public abstract class GuideTask {
//	/**
//	 * 输出支持的基础字
//	 * @return Sock列表
//	 */
//	public abstract List<Sock> getSocks();
//	/**
//	 * 判断支持某个基础字
//	 * @param sock 基础字
//	 * @return 返回真或者假
//	 */
//	public abstract boolean isSupport(Sock sock);
//	/**
//	 * 根据基础字生成录入参数
//	 * @param sock 基础字
//	 * @return 录入参数列表
//	 * @throws GuideTaskException
//	 */
//	public abstract InputParameterList markup(Sock sock) throws GuideTaskException;
//	/**
//	 * 根据基础字和录入参数，产生分布计算命令
//	 * @param sock 基础字
//	 * @param list 录入参数列表
//	 * @return 分布计算命令
//	 * @throws GuideTaskException
//	 */
//	public abstract DistributedCommand create(Sock sock, InputParameterList list) throws GuideTaskException;
//	/**
//	 * 在前次计算结果上，根据计算结果和基础字，再次产生新分布计算命令，在“create”方法之后调用
//	 * @param predata 上次计算结果
//	 * @param sock 基础字
//	 * @return 分布计算命令。
//	 * @throws GuideTaskException
//	 */
//	public abstract DistributedCommand next(byte[] predata, Sock sock) throws GuideTaskException;
//}

//		// 检查和加载动态链接库
//		checkAndLoadLibraries0();


//	/**
//	 * 检查和安装动态链接库
//	 */
//	protected void checkAndLoadLibraries0() {
//		try {
//			loadLibraries();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		} catch (UnsatisfiedLinkError e) {
//			e.printStackTrace();
//		} catch (NoSuchFieldException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * 调用方法："Runtime.load0"，采用引用方式加载全部动态链接库
//	 * 
//	 * @throws SecurityException
//	 * @throws IllegalArgumentException
//	 * @throws NoSuchFieldException
//	 * @throws IllegalAccessException
//	 * @throws NoSuchMethodException
//	 * @throws InvocationTargetException
//	 */
//	protected void loadLibraries() throws SecurityException,
//			IllegalArgumentException, NoSuchFieldException,
//			IllegalAccessException, NoSuchMethodException,
//			InvocationTargetException {
//
//		ClassLoader c = getClass().getClassLoader();
//		// 判断是动态库类加载器
//		if (!Laxkit.isClassFrom(c, LibraryClassLoader.class)) {
//			return;
//		}
//
//		LibraryClassLoader loader = (LibraryClassLoader) c;
//		// 1. 没有预存储动态链接库，不用加载
//		String[] paths = loader.getLibraries();
//		int size = (paths == null ? 0 : paths.length);
//		if (size < 1) {
//			return;
//		}
//		// 2. 如果动态库已经全部加载，忽略它！
//		if (loader.isLoadedLibraries()) {
//			return;
//		}
//		// 3. 调用Runtime.load0方法，去加载动态链接库
//		Runtime rt = Runtime.getRuntime();
//		Method load0 = rt.getClass().getDeclaredMethod("load0", new Class[] { Class.class, String.class });
//		load0.setAccessible(true);
//		// 4. 采用引用方式调用
//		// 这个最关键!!!是子类实例。如果用System.load(String filename),System调用getCallerClass()方法，会指向其它类，导致类加载器位置出错。
//		Class<?> clazz = getClass(); 
//		for (int i = 0; i < size; i++) {
//			String filename = paths[i];
//			Object[] objects = new Object[] { clazz, filename };
//			load0.invoke(rt, objects);
//		}
//	}

//	/**
//	 * 加载全部动态链接库
//	 * @throws IllegalAccessException 
//	 * @throws NoSuchFieldException 
//	 * @throws IllegalArgumentException 
//	 * @throws SecurityException 
//	 */
//	protected void loadLibraries() throws SecurityException,
//			IllegalArgumentException, NoSuchFieldException,
//			IllegalAccessException, NullPointerException, UnsatisfiedLinkError {
//
//		ClassLoader c = getClass().getClassLoader();
//		// 判断是动态库类加载器
//		if (!Laxkit.isClassFrom(c, LibraryClassLoader.class)) {
//			return;
//		}
//
//		LibraryClassLoader loader = (LibraryClassLoader) c;
//		// 1. 没有预存储动态链接库，不用加载
//		if (!loader.hasLibraries()) {
//			return;
//		}
//		// 2. 如果动态库已经全部加载，忽略它！
//		if (loader.isLoadedLibraries()) {
//			return;
//		}
//		// 2. 取出动态链接库，加载它！注意：只能在用户对象中使用“System.load”加载动态链接库，而不是在LibraryClassLoader中加载！
//		String[] paths = loader.getLibraries();
//		for (String path : paths) {
//			// 以全路径格式加载动态链接库到“LibraryClassLoader”
//			System.load(path);
//		}
//	}
