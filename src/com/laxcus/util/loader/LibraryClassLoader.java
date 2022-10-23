/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.loader;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import com.laxcus.util.*;

/**
 * 动态链接库类加载器。<br><br>
 * 
 * 继承自“URLClassLoader”，支持动态链接库的加载和释放，从“LibraryClassLoader”加载的动态链接库被拥有者独有。<br>
 * 基于这个“拥有者独有动态链接库”的特点，可以构造成容器的基础底层。<br>
 * 动态链接库，需要用户使用“System.load”方法来加载。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/5/2021
 * @since laxcus 1.0
 */
public class LibraryClassLoader extends URLClassLoader {
	
//	/**
//	 * 动态链接库单元
//	 *
//	 * @author scott.liang
//	 * @version 1.0 7/5/2021
//	 * @since laxcus 1.0
//	 */
//	class Slice implements Comparable<Slice> {
//
//		/** 动态链接库路径 **/
//		String path;
//
////		/** 判断是不是加载了 **/
////		boolean loaded;
//
//		/**
//		 * 构造动态链接库单元，指定路径
//		 * @param path
//		 */
//		Slice(String path) {
//			super();
//			setPath(path);
////			loaded = false;
//		}
//
//		void setPath(String s) {
//			path = s;
//		}
//
//		String getPath() {
//			return path;
//		}
//
////		public boolean load() {
////			if (!loaded) {
////				System.load(path);
////				loaded = true;
////			}
////			return loaded;
////		}
//
//		/* (non-Javadoc)
//		 * @see java.lang.Comparable#compareTo(java.lang.Object)
//		 */
//		@Override
//		public int compareTo(Slice that) {
//			return path.compareTo(that.path);
//		}
//	}
	
//	/** 动态链接库 **/
//	private ArrayList<Slice> slices = new ArrayList<Slice>();

	/** 动态链接库全路径名称 **/
	private ArrayList<String> paths = new ArrayList<String>();
	
	/**
	 * @param urls
	 */
	public LibraryClassLoader(URL[] urls) {
		super(urls);
	}

	/**
	 * @param urls
	 * @param parent
	 */
	public LibraryClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	/**
	 * @param urls
	 * @param parent
	 * @param factory
	 */
	public LibraryClassLoader(URL[] urls, ClassLoader parent,
			URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}
	
	/**
	 * 添加动态链接库
	 * @param path 库路径
	 * @return 成功返回真，否则假
	 */
	public boolean addLibrary(String path) {
		// 如果存在，忽略它！
		if (paths.contains(path)) {
			return false;
		}
		return paths.add(path);
	}
	
	/**
	 * 移出一个动态链接库
	 * @param path 库路径
	 * @return 成功返回真，否则假
	 */
	public boolean removeLibrary(String path) {
		return paths.remove(path);
	}

	/**
	 * 增加动态链接库
	 * @param a
	 * @return 返回新增的库成员数
	 */
	public int addLibraries(Collection<String> a) {
		int size = paths.size();
		for (String path : a) {
			addLibrary(path);
		}
		return paths.size() - size;
	}
	
	/**
	 * 判断预存了动态链接库
	 * @return 返回真或者假
	 */
	public boolean hasLibraries() {
		return paths.size() > 0;
	}

	/**
	 * 返回动态链接库文件路径
	 * @return 字符串数组
	 */
	public String[] getLibraries() {
		if (paths.isEmpty()) {
			return new String[0];
		}
		String[] a = new String[paths.size()];
		return paths.toArray(a);
	}

	/**
	 * 判断加载全部动态链接库
	 * @return 返回真或者假
	 * @throws IllegalAccessException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	public boolean isLoadedLibraries() throws SecurityException, 
			IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		
		// 返回已经加载的动态链接库
		int count = 0;
		String[] array = getLoadedLibraries();
		// 找到它，如果一致返回真，否则假
		for (String source : array) {
			for (String path : paths) {
				boolean success = (Laxkit.compareTo(source, path) == 0);
				if (success) {
					count++;
					break;
				}
			}
		}

		return count == paths.size();
	}

	/**
	 * 判断一个动态链接库已经加载
	 * @param soName 绝对路径名
	 * @return 返回真或者假
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public boolean isLoadedLibrary(String soName) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		
		// 返回已经加载的动态链接库
		String[] array = getLoadedLibraries();
		// 找到它，如果一致返回真，否则假
		for (String source : array) {
			if (source.equals(soName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 取出加载的动态链接库
	 * 
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public String[] getLoadedLibraries() throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		// 变量"nativeLibraries"在ClassLoader里面
		Field field = ClassLoader.class.getDeclaredField("nativeLibraries");
		field.setAccessible(true);

		ArrayList<String> array = new ArrayList<String>();

		Vector<Object> objects = (Vector<Object>) field.get(this);
		Iterator<Object> iterator = objects.iterator();
		Object object;
		
		while (iterator.hasNext()) {
			object = iterator.next(); // "object"是NativeLibrary类，在ClassLoader里面
			Field[] fields = object.getClass().getDeclaredFields(); // 声明的变量

			int size = (fields == null ? 0 : fields.length);
			for (int i = 0; i < size; i++) {
				Field element = fields[i];
				// "name"是NativeLibrary里面的String变量名称
				if (element.getName().equals("name")) {
					element.setAccessible(true);
					String soPath = element.get(object).toString();
					array.add(soPath);
				}
			}
		}

		if (array.isEmpty()) {
			return new String[0];
		}
		// 输出全部
		String[] a = new String[array.size()];
		return array.toArray(a);
	}
	
//	/**
//	 * 释放动态链接库
//	 * 在ClassLoader类中，有一个“NativeLibrary”类。调用它的“finalize”方法，释放动态链接库。具体见ClassLoader中的定义。
//	 * 
//	 * @param soName 动态链接库名称
//	 * @throws SecurityException
//	 * @throws NoSuchFieldException
//	 * @throws IllegalArgumentException
//	 * @throws IllegalAccessException
//	 * @throws NoSuchMethodException
//	 * @throws InvocationTargetException
//	 */
//	@SuppressWarnings("unchecked")
//	private boolean freeLoadLibrary(String soName) throws SecurityException,
//			NoSuchFieldException, IllegalArgumentException,
//			IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//
//		Field field = ClassLoader.class.getDeclaredField("nativeLibraries");
//		field.setAccessible(true);
//
//		Vector<Object> objects = (Vector<Object>) field.get(this);
//		Iterator<Object> iterator = objects.iterator();
//
//		while (iterator.hasNext()) {
//			Object object = iterator.next();
//			Field[] fields = object.getClass().getDeclaredFields();
//
//			// 判断成员数
//			int size = (fields == null ? 0 : fields.length);
//			for (int i = 0; i < size; i++) {
//				Field element = fields[i];
//
//				//				// 变量名“fromClass”是调用者类
//				//				if (element.getName().equals("fromClass")) {
//				//					element.setAccessible(true);
//				//					Object obj = element.get(object);
//				//					System.out.printf("fromClass is %s\n", obj.toString());
//				//				}
//				
//				// 只有变量名“name”，其它忽略
//				if (!element.getName().equals("name")) {
//					continue;
//				}
//				
//				// 取动态链接库名全路径名
//				element.setAccessible(true);
//				String soPath = element.get(object).toString();
//				boolean match = soPath.equals(soName);
//				if (!match) {
//					continue;
//				}
//
//				// 关键！找到“finalize”方法，调用它释放动态链接库！
//				Method finalize = object.getClass().getDeclaredMethod("finalize", new Class[0]);
//				finalize.setAccessible(true);
//				finalize.invoke(object, new Object[0]);
//				iterator.remove();
//				objects.remove(object);
//				return true;
//			}
//		}
//		return false;
//	}
	
//	/**
//	 * 释放全部动态链接库
//	 * 这个方法由外部接口调用
//	 * @return 返回被释放的动态库
//	 * @throws IllegalAccessException 
//	 * @throws NoSuchFieldException 
//	 * @throws IllegalArgumentException 
//	 * @throws SecurityException 
//	 * @throws InvocationTargetException 
//	 * @throws NoSuchMethodException 
//	 */
//	public int freeAllLibraries() throws SecurityException,
//			IllegalArgumentException, NoSuchFieldException, IllegalAccessException, 
//			NoSuchMethodException, InvocationTargetException {
//
//		String[] soPaths = getLoadedLibraries();
//		int size = (soPaths == null ? 0 : soPaths.length);
////		System.out.printf("全部库文件：%d\n", soPaths.length);
//		int count = 0;
//		for (int i = 0; i < size; i++) {
//			String path = soPaths[i];
//			boolean success = freeLoadLibrary(path);
//			if (success) {
//				count++;
//			}
////			System.out.printf("释放 %s %s \n", soPaths[i], (success ? "成功" : "失败"));
//		}
//		return count;
//	}
	
//	/**
//	 * 释放动态链接库
//	 * 在ClassLoader类中，有一个“NativeLibrary”类。调用它的“finalize”方法，释放动态链接库。具体见ClassLoader中的定义。
//	 * 
//	 * @param soName 动态链接库名称
//	 * @throws SecurityException
//	 * @throws NoSuchFieldException
//	 * @throws IllegalArgumentException
//	 * @throws IllegalAccessException
//	 * @throws NoSuchMethodException
//	 * @throws InvocationTargetException
//	 */
//	@SuppressWarnings("unchecked")
//	public boolean freeLoadLibrary(String soName) throws SecurityException,
//			NoSuchFieldException, IllegalArgumentException,
//			IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//
//		/**
//		 * 变量"nativeLibraries"在ClassLoader中的声明是：
//		 * 
//		 *  // Native libraries associated with the class loader.
//		 *  private Vector nativeLibraries = new Vector();
//		 *  
//		 *  nativeLibraries是基于私有向量类变量
//		 */
//		
//		Field field = ClassLoader.class.getDeclaredField("nativeLibraries");
//		field.setAccessible(true);
//
//		Vector<Object> objects = (Vector<Object>) field.get(this);
//		Iterator<Object> iterator = objects.iterator();
//		Object object;
//		int count = 0;
//		
//		while (iterator.hasNext()) {
//			object = iterator.next(); // object是“NativeLibrary”对象
//			Field[] fields = object.getClass().getDeclaredFields();
//
//			// 判断成员数
//			int size = (fields == null ? 0 : fields.length);
//			for (int i = 0; i < size; i++) {
//				Field element = fields[i];
//
//				//				// 变量名“fromClass”是调用者类
//				//				if (element.getName().equals("fromClass")) {
//				//					element.setAccessible(true);
//				//					Object obj = element.get(object);
//				//					System.out.printf("fromClass is %s\n", obj.toString());
//				//				}
//				
//				// 只有变量名“name”，其它忽略
//				if (!element.getName().equals("name")) {
//					continue;
//				}
//				
//				// 取动态链接库名全路径名
//				element.setAccessible(true);
//				String soPath = element.get(object).toString();
//				boolean match = soPath.equals(soName);
//				if (!match) {
//					continue;
//				}
//
//				// 关键！找到“finalize”方法，调用它释放动态链接库！
//				Method finalize = object.getClass().getDeclaredMethod("finalize", new Class[0]);
//				finalize.setAccessible(true);
//				finalize.invoke(object, new Object[0]);
//				// 删除“nativeLibraries”里的动态链接库文件
//				iterator.remove();
//				objects.remove(object);
//				count++;
//			}
//		}
//		// 大于0是成功
//		return count > 0;
//	}
	
	/**
	 * 释放动态链接库
	 * 在ClassLoader类中，有一个“NativeLibrary”类。调用它的“finalize”方法，释放动态链接库。具体见ClassLoader中的定义。
	 * 
	 * @param soName 动态链接库名称
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public boolean freeLoadLibrary(String soName) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException, InvocationTargetException {

		/**
		 * 变量"nativeLibraries"在ClassLoader中的声明是：
		 * 
		 *  // Native libraries associated with the class loader.
		 *  private Vector nativeLibraries = new Vector();
		 *  
		 *  nativeLibraries是基于私有向量类变量
		 */
		
		Field field = ClassLoader.class.getDeclaredField("nativeLibraries");
		field.setAccessible(true);

		// 先记录，再删除！
		ArrayList<Object> array = new ArrayList<Object>();
		
		Vector<Object> objects = (Vector<Object>) field.get(this);
		
		// 同步
		synchronized(	objects) {
			Iterator<Object> iterator = objects.iterator();

			while (iterator.hasNext()) {
				Object object = iterator.next(); // object是“NativeLibrary”对象
				Field[] fields = object.getClass().getDeclaredFields();

				// 判断成员数
				int size = (fields == null ? 0 : fields.length);
				for (int i = 0; i < size; i++) {
					Field element = fields[i];

					//				// 变量名“fromClass”是调用者类
					//				if (element.getName().equals("fromClass")) {
					//					element.setAccessible(true);
					//					Object obj = element.get(object);
					//					System.out.printf("fromClass is %s\n", obj.toString());
					//				}

					// 只有变量名“name”，其它忽略
					if (!element.getName().equals("name")) {
						continue;
					}

					// 取动态链接库名全路径名
					element.setAccessible(true);
					String soPath = element.get(object).toString();
					boolean match = soPath.equals(soName);
					if (match) {
						array.add(object);
					}
				}
			}

			// 调用NativeLibrary类
			for (Object object : array) {
				// 关键！找到“finalize”方法，调用它释放动态链接库！
				Method finalize = object.getClass().getDeclaredMethod("finalize", new Class[0]);
				finalize.setAccessible(true);
				finalize.invoke(object, new Object[0]);
				// 删除“nativeLibraries”里的动态链接库文件
				objects.remove(object);
			}
		}
		
		// 大于0是成功
		return array.size() > 0;
	}
	
	/**
	 * 输出已经注册的本地库文件
	 * @return 返回字符串数组
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public String[] getRegisterNativeLibraries() throws SecurityException,
		NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		ArrayList<String> a = new ArrayList<String>();
		
		Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
		field.setAccessible(true);
		
		Vector<Object> objects = (Vector<Object>) field.get(this);
		// 同步锁定
		synchronized (objects) {
			Iterator<Object> iterator = objects.iterator();
			// 循环，找到对应的
			while (iterator.hasNext()) {
				Object object = iterator.next();
				// 保存...
				if (object.getClass() == String.class) {
					String str = (String) object;
					a.add(str);
				}
			}
		}
		// 输出字符串
		if (a.isEmpty()) {
			return new String[0];
		}
		String[] strs = new String[a.size()];
		return a.toArray(strs);
	}
	
	/**
	 * 判断在"ClassLoader"有注册的动态链接库
	 * @param soName 库路径
	 * @return 返回真或者假
	 * 
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public boolean hasRegisterNativeLibrary(String soName) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		/**
		 *  在ClassLoader的声明是：
		 *  // All native library names we've loaded.
		 *  private static Vector loadedLibraryNames = new Vector();
		 *  
		 *  loadedLibraryNames是一个静态的向量类对量
		 */
		Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
		field.setAccessible(true);
		int count = 0;
		
		Vector<Object> objects = (Vector<Object>) field.get(this);
		// 同步锁定
		synchronized (objects) {
			Iterator<Object> iterator = objects.iterator();
			// 循环，找到对应的
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object.getClass() != String.class) {
					continue;
				}
				// 比较一致时...
				String str = (String) object;
				boolean success = str.equals(soName);
				if (success) {
					count++;
				}
			}
		}
		// 返回结果
		return (count > 0);
	}
	
	/***
	 * 撤销在"ClassLoader"中的注册
	 * @param soName 动态链接库全路径名称
	 * @return 成功返回真，否则假
	 * 
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public boolean unregisterNativeLibrary(String soName) throws SecurityException, 
		NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		
		/**
		 *  在ClassLoader的声明是：
		 *  // All native library names we've loaded.
		 *  private static Vector loadedLibraryNames = new Vector();
		 *  
		 *  loadedLibraryNames是一个静态的对象数组
		 */
		Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
		field.setAccessible(true);
		
		ArrayList<Object> a = new ArrayList<Object>();
		
		Vector<Object> objects = (Vector<Object>) field.get(this);
		// 同步锁定
		synchronized (objects) {
			Iterator<Object> iterator = objects.iterator();

			// 循环，找到对应的
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object.getClass() != String.class) {
					continue;
				}
				// 比较一致，保存它！
				String str = (String) object;
				boolean success = str.equals(soName);
				if (success) {
					a.add(str);
				}
			}

			// 删除
			if (a.size() > 0) {
				for (Object o : a) {
					objects.removeElement(o);
				}
			}
		}
		// 返回结果
		return (a.size() > 0);
	}

	/**
	 * 释放全部动态链接库
	 * 这个方法由外部接口调用
	 * @return 返回被释放的动态库
	 * @throws IllegalAccessException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	public int freeAllLibraries() throws SecurityException,
			IllegalArgumentException, NoSuchFieldException, IllegalAccessException, 
			NoSuchMethodException, InvocationTargetException {

		String[] soPaths = getLoadedLibraries();
		int size = (soPaths == null ? 0 : soPaths.length);
//		System.out.printf("全部库文件：%d\n", soPaths.length);
		int count = 0;
		for (int i = 0; i < size; i++) {
			String path = soPaths[i];
			boolean success = freeLoadLibrary(path);
			if (success) {
				count++;
			}
//			System.out.printf("释放 %s %s \n", soPaths[i], (success ? "成功" : "失败"));
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		
//		freeAllLibraries();
		
		paths.clear(); // 清除动态链接库
	}
}


///***
// * 撤销在"ClassLoader"中的注册
// * @param soName 动态链接库全路径名称
// * @return 成功返回真，否则假
// * 
// * @throws SecurityException
// * @throws NoSuchFieldException
// * @throws IllegalArgumentException
// * @throws IllegalAccessException
// */
//@SuppressWarnings("unchecked")
//public boolean unregisterNativeLibrary(String soName) throws SecurityException, 
//	NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//	
//	/**
//	 *  在ClassLoader的声明是：
//	 *  // All native library names we've loaded.
//	 *  private static Vector loadedLibraryNames = new Vector();
//	 *  
//	 *  loadedLibraryNames是一个静态的对象数组
//	 */
//	Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
//	field.setAccessible(true);
//	
//	int count = 0;
//	
//	Vector<Object> objects = (Vector<Object>) field.get(this);
//	// 同步锁定
//	synchronized (objects) {
//		Iterator<Object> iterator = objects.iterator();
//		Object object;
//
//		// 循环，找到对应的
//		while (iterator.hasNext()) {
//			object = iterator.next();
//			if (object.getClass() != String.class) {
//				continue;
//			}
//			// 比较一致，删除！
//			String str = (String) object;
//			boolean success = str.equals(soName);
//			if (success) {
//				iterator.remove();
//				objects.remove(object);
//				count++;
//			}
//		}
//	}
//	// 返回结果
//	return (count > 0);
//}