/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application;

import java.lang.reflect.*;

import com.laxcus.container.*;
import com.laxcus.util.*;
import com.laxcus.util.event.*;

/**
 * 客户端桌面应用 <br><br>
 * 
 * 两个要求：<br>
 * 1. 用户在构造函数的同时或者之后加载动态链接库，加载方式可调用"loadLibraries"方法加载，或者自行处理，具体根据用户需要来处理。<br>
 * 2. 应用在退出前，必须要调用 "exit"方法，通知容器卸载应用。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/3/2021
 * @since laxcus 1.0
 */
public class Application {

	/**
	 * 构造客户端桌面应用
	 */
	protected Application() {
		super();
	}

	/**
	 * 构造客户端桌面应用，且加载动态链接库
	 * 
	 * @param loadlib 加载库
	 * 
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected Application(boolean loadlib) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		this();
		// 加载动态链接库
		if (loadlib) {
			loadLibraries();
		}
	}

	/**
	 * 返回动态全部的链接库
	 * 
	 * @return 动态链接库的磁盘路径，没有返回零数组
	 */
	public String[] getLibraries() {
		// 指向当前类的类加载器
		ClassLoader loader = getClass().getClassLoader();
		if (Laxkit.isClassFrom(loader, ApplicationClassLoader.class)) {
			return ((ApplicationClassLoader) loader).getLibraries();
		}
		return new String[0];
	}

	/**
	 * 加载动态链接库
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void loadLibraries() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		// 取得动态链接库所在路径
		String[] paths = getLibraries();
		if (paths.length == 0) {
			return;
		}

		// 加载动态链接库，System.load也是通过调用"Runtime.load0"方法来实现
		Runtime rt = Runtime.getRuntime();
		Method load0 = rt.getClass().getDeclaredMethod("load0", new Class[] { Class.class, String.class });
		load0.setAccessible(true);

		// 下面这行最关键!!! 如果用System.load(String filename),System调用getCallerClass()方法，会指向其它类，导致类加载器位置出错。
		Class<?> clazz = getClass(); 

		// 逐个加载
		for (int i = 0; i < paths.length; i++) {
			String filename = paths[i];
			// System.out.printf("在loadLibraries方法，类 %s, 加载库 %s \n", clazz.getName(), filename);
			Object[] objects = new Object[] { clazz, filename };
			load0.invoke(rt, objects);
		}
	}

	/**
	 * 释放资源，由应用在结束后调用
	 * @param status
	 * @return 
	 */
	public int exit(int status) {
		// 实例
		ClassLoader loader = getClass().getClassLoader();
		if (loader == null) {
			return ContainerShutdown.FAILED;
		}
		// 是容器类加载器
		if (Laxkit.isClassFrom(loader, ApplicationClassLoader.class)) {
			return ((ApplicationClassLoader) loader).exit(status);
		}
		return ContainerShutdown.NOTFOUND;
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	protected void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 延时等待
	 * @param ms 超时时间，单位：毫秒。
	 */
	public synchronized void delay(long ms) {
		try {
			if (ms > 0L) {
				wait(ms);
			}
		} catch (InterruptedException e) {
			
		}
	}

	/**
	 * 唤醒线程
	 */
	public synchronized void wakeup() {
		try {
			notify();
		}catch(IllegalMonitorStateException e) {
			
		}
	}

	/**
	 * 唤醒全部
	 */
	public synchronized void wakeupAll() {
		try {
			notifyAll();
		}catch(IllegalMonitorStateException e) {
			
		}
	}
}

//	/**
//	 * 返回调用者的类。
//	 * 这个里面的函数是私有的，随JRE版本不同而有变化。我们要做的工作是：把JRE封装在一起，避免因为外带JRE版本不同出现意外情况！
//	 * @return
//	 */
//	private static Class<?> getCallerClass() {
//		for(int i =0; true; i++) {
//			Class<?> clazz = Reflection.getCallerClass(i);
//			if(clazz ==null){
//				break;
//			}
//			System.out.printf("%d %s\n", i, clazz.getName());
//		}
//		System.out.println();
//		
//		return Reflection.getCallerClass(3);
//	}
//	
//	@SuppressWarnings("unchecked")
//	public void printClassFrom(ClassLoader loader) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException  {
//
////		ClassLoader c = getClass().getClassLoader();
//		System.out.printf("这个类加载器他是： %s\n", loader.getClass().getName());
//		Field field = loader.getClass().getDeclaredField("nativeLibraries");
//		field.setAccessible(true);
//
//
//		Vector<Object> objects = (Vector<Object>) field.get(this);
//		Iterator<Object> iterator = objects.iterator();
//
//		while (iterator.hasNext()) {
//			Object object = iterator.next();
//			Field[] fields = object.getClass().getDeclaredFields();
//
//			for (int i = 0; fields != null && i < fields.length; i++) {
//				Field element = fields[i];
//				// 只有变量名“name”，其它忽略
//				if (!element.getName().equals("fromClass")) {
//					continue;
//				}
//				
//				element.setAccessible(true);
//				Object obj = element.get(object);
//				System.out.printf("fromClass is %s\n", obj.toString() );
//
//				// 取动态链接库名全路径名
//				//		element.setAccessible(true);
//				//		String soPath = element.get(object).toString();
//				//		boolean match = soPath.equals(soName);
//				//		if (!match) {
//				//			continue;
//				//		}
//				//
//				//		// 关键！找到“finalize”方法，调用它释放动态链接库！
//				//		Method finalize = object.getClass().getDeclaredMethod("finalize", new Class[0]);
//				//		finalize.setAccessible(true);
//				//		finalize.invoke(object, new Object[0]);
//				//		iterator.remove();
//				//		objects.remove(object);
//				//		return true;
//			}
//		}
//
//	}
//	
////	/**
////	 * 释放资源，由应用在结束后调用
////	 * @param status
////	 * @return 
////	 */
////	public int exit(int status) {
////		//		//		ClassLoader loader = getClass().getClassLoader();
////		//		ClassLoader la = BasketSystem.class.getClassLoader();
////		//		System.out.printf("BasketSystem ClassLoader is %s\n", la.getClass().getName());
////
////		Class<?> clazz = ContainerApplication.getCallerClass();
////		ClassLoader loader = clazz.getClassLoader();
////		if (loader == null) {
////			return ContainerShutdown.FAILED;
////		}
////
////		//		System.out.printf("caller clazz is %s, class loader is %s\n", clazz.getName(), loader.getClass().getName());
////
////		//		boolean success = (loader != null && Laxkit.isClassFrom(loader, ApplicationClassLoader.class));
////
////		if (Laxkit.isClassFrom(loader, ApplicationClassLoader.class)) {
////			return ((ApplicationClassLoader) loader).exit(status);
////		}
////		return ContainerShutdown.NOTFOUND;
////	}
//
//	
////	/**
////	 * 加载全部动态链接库
////	 */
////	protected void loadLibraries() {
////		ClassLoader c = getClass().getClassLoader();
////		System.out.printf("类加载器是 %s\n", c.getClass().getName());
////		
////		// boolean success = (Laxkit.isClassFrom(loader,
////		// ContainerClassLoader.class));
////
////		// 判断是容器类加载器
////		if (Laxkit.isClassFrom(c, ApplicationClassLoader.class)) {
////			ApplicationClassLoader loader = (ApplicationClassLoader) c;
////			String[] libraries = loader.getLibraries();
////			System.out.printf("libraries size is %d\n", libraries.length);
////			for (String path : libraries) {
////				// 以全路径格式加载动态链接库
////				System.load(path);
////				System.out.printf("load library %s\n", path);
////			}
////		}
////	}
//
//	/**
//	 * 加载全部动态链接库
//	 */
//	protected void loadLibrariesx() {
//		ClassLoader c = getClass().getClassLoader();
//		System.out.printf("类加载器: %s\n", c.getClass().getName());
//		
//		// boolean success = (Laxkit.isClassFrom(loader,
//		// ContainerClassLoader.class));
//
//		// 判断是容器类加载器
//		if (!Laxkit.isClassFrom(c, ApplicationClassLoader.class)) {
//			return;
//		}
//
//		ApplicationClassLoader loader = (ApplicationClassLoader) c;
//		// 没有动态链接库，不用加载
//		if (!loader.hasLibraries()) {
//			System.out.println("没有预存储动态链接库");
//			return;
//		}
//		
//		// 2. 取出动态链接库，加载它！注意：只能在用户对象中使用“System.load”加载动态链接库，而不是在LibraryClassLoader中加载！
//		String[] libraries = loader.getLibraries();
//		System.out.printf("libraries size is %d\n", libraries.length);
//		for (String path : libraries) {
//			// 以全路径格式加载动态链接库
//			System.load(path);
//			System.out.printf("load library %s\n", path);
//		}
//		
//		try {
//			System.out.printf("动态链接库全部加载：%s\n", (loader.isLoadedLibraries() ? "Yes" : "No" ));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//
//	public void testAddAllLibraries() throws Throwable {
////		System.out.printf("T1, 这个调用者来自：%s\n", getCallerClass().getName());
//
//		String[] paths = getLibraries();
//		Runtime rt = Runtime.getRuntime();
//		Method load0 = rt.getClass().getDeclaredMethod("load0", new Class[] { Class.class, String.class });
//		load0.setAccessible(true);
//		Class<?> clazz = getClass(); // 这个最关键。如果用System.load(String filename),System调用getCallerClass()方法，会指向其它类，导致类加载器位置出错。
//		
////		Class<?> clazz = Application.class; // 这个最关键
////		Class<?> clazz = NotepadInternalFrame.class;
//		
//		for (int i = 0; i < paths.length; i++) {
//			String filename = paths[i];
//			System.out.printf("类 %s, 加载库 %s \n", clazz.getName(), filename);
//			Object[] objects = new Object[] { clazz, filename };
//			load0.invoke(rt, objects);
//		}
//		
////		printClassFrom();
//	}
//
//	public void testAddAllLibraries2() throws Throwable {
////		System.out.printf("T2, 这个调用者来自：%s\n", getCallerClass().getName());
//
//		String[] paths = getLibraries();
//		for (String path : paths) {
//			System.out.printf("用\"System.load\"加载动态链接库 %s\n", path);
//			// 以全路径格式加载动态链接库
//			System.load(path);
//		}
////		printClassFrom();
//		
////		Runtime rt = Runtime.getRuntime();
////		Method load0 = rt.getClass().getDeclaredMethod("load0", new Class[] { Class.class, String.class });
////		load0.setAccessible(true);
////		Class<?> clazz = getClass(); // 这个最关键
////		for (int i = 0; i < paths.length; i++) {
////			String filename = paths[i];
////			System.out.printf("class is %s, LOAD LIBRARY %s \n", clazz.getName(), filename);
////			Object[] objects = new Object[] { clazz, filename };
////			load0.invoke(rt, objects);
////		}
//	}