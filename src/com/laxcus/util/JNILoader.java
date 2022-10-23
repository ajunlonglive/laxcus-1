/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;

/**
 * WINDOWS/LINUX本地动态链接库加载器。<br>
 * 在节点启动或者更新动态链接库时调用。<br><br>
 * 
 * 把指定目录下的动态链接库（分为WINDOWS/LINUX）加载到JVM运行环境，这些目录在启动文件中指定，包括：<br>
 * 1. laxcus.library，系统目录 <br>
 * 2. laxcus.task.library，分布任务组件目录<br>
 * 3. laxcus.custom.library，管理员自定义目录<br><br>
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class JNILoader {

	/**
	 * 全部链接库文件目录。除"user.dir"，其他都在启动命令内定义。JVM规定配置格式：-Dlaxcus.library=...
	 * -Dlaxcus.task.library=...
	 **/
	private static String[] libTotalPaths = { "user.dir", "laxcus.library",
		"laxcus.task.library", "laxcus.custom.library" };

	/**
	 * 用户链接库
	 */
	private static String[] libUserPaths = { "laxcus.task.library", "laxcus.custom.library" };

	/** WINDOWS动态链接库后缀 **/
	private static String DLL = "^\\s*([\\w\\W]+)(?i)(\\.so)\\s*$";
	
	/** LINUX动态链接库后缀 **/
	private static String SO = "^\\s*([\\w\\W]+)(?i)(\\.dll)\\s*$";

	/**
	 * 加载一个全路径文件名的本地链接库
	 * @param filename 全路径文件名
	 */
	private static boolean loadLibrary(String filename) {
		try {
			System.load(filename);
			return true;
		} catch (UnsatisfiedLinkError e) {
			Logger.fatal(e);
		} catch (SecurityException e) {
			Logger.fatal(e);
		}
		return false;
	}
	
	/**
	 * 判断是动态链接库文件，分为WINDOWS/LINUX两种
	 * @param filename 文件名
	 * @return 返回真或者假
	 */
	private static boolean isLibrary(String filename) {
		return (filename.matches(JNILoader.DLL) || filename.matches(JNILoader.SO));
	}

	/**
	 * 加载一个动态库库文件
	 * @param file 文件名
	 * @return 成功返回真，否则假
	 */
	public static boolean loadSingleLibrary(File file) {
		// 存在，是文件，且没有隐藏
		boolean success = (file.exists() && file.isFile() && !file.isHidden());
		if (!success) {
			return false;
		}

		// 取标准格式
		String path = Laxkit.canonical(file);

		// 判断文件是DLL/SO文件
		success = JNILoader.isLibrary(path);
		// 加载本地库
		if (success) {
			success = JNILoader.loadLibrary(path);
			Logger.note("JNILoader.loadSingleLibrary, load native library:" + path, success);
		}
		
		return success;
	}
	
	/**
	 * 加载指定目录下的动态链接库文件
	 * @param path 文件目录
	 * @return 返回加载成功的磁盘文件
	 */
	private static List<String> loadLibraries(String path) {
		ArrayList<String> array = new ArrayList<String>();
		
		// 生成文件对象
		File root = new File(path);
		// 不存在，忽略它！
		if (!root.exists()) {
			return array;
		} 
		
		// 是文件，加载这个文件
		if (root.isFile()) {
			boolean success = JNILoader.loadSingleLibrary(root);
			if (success) array.add(Laxkit.canonical(root));
		}
		// 是目录，读取子目录下的内容
		else if (root.isDirectory()) {
			// 读取目录下的内容
			File[] files = root.listFiles();
			for (int i = 0; files != null && i < files.length; i++) {
				File file = files[i];

				// 如果是子目录，检查和加载库文件
				if (file.isDirectory()) {
					String sub = Laxkit.canonical(file);
					List<String> subs = JNILoader.loadLibraries(sub);
					array.addAll(subs);
				} else {
					boolean success = JNILoader.loadSingleLibrary(file);
					if (success) array.add(Laxkit.canonical(file));
				}
			}
		} 
		
		return array;
	}

	/**
	 * 加载全部本地的动态链接库文件
	 * “laxcus.library”做为一个“java.library.path”指定的路径之一存在，在启动时配置。
	 * 
	 * @return 返回加载的链接库名文件路径数组
	 */
	public static List<String> reloadTotalLibraries() {
		ArrayList<String> array = new ArrayList<String>();

		for (int i = 0; i < JNILoader.libTotalPaths.length; i++) {
			String path = System.getProperty(JNILoader.libTotalPaths[i]);
			if (path != null) {
				List<String> subs = JNILoader.loadLibraries(path);
				array.addAll(subs);
			}
		}

		return array;
	}

	/**
	 * 加载用户定义的动态链接库文件
	 * 
	 * @return 返回加载的链接库名文件路径数组
	 */
	public static List<String> reloadUserLibraries() {
		ArrayList<String> array = new ArrayList<String>();

		for (int i = 0; i < JNILoader.libUserPaths.length; i++) {
			String path = System.getProperty(JNILoader.libUserPaths[i]);
			if (path != null) {
				List<String> subs = JNILoader.loadLibraries(path);
				array.addAll(subs);
			}
		}

		return array;
	}

	/**
	 * 初始化和加载规定目录下的动态链接库
	 * 加载这些目录下的动态链接库
	 */
	public static void init() {
		// 加载动态链接库
		JNILoader.reloadTotalLibraries();
	}
	

//	/** WINDOWS动态链接库后缀 **/
//	private static String DLL = ".dll";
//
//	/** LINUX动态链接库后缀 **/
//	private static String SO = ".so";
	

//	/**
//	 * 加载指定目录下的动态链接库文件
//	 * @param path 文件目录
//	 * @return 返回加载成功的磁盘文件
//	 */
//	private static List<String> loadLibraries1(String path) {
//		ArrayList<String> array = new ArrayList<String>();
//		
//		// 生成文件对象
//		File dir = new File(path);
//		// 不存在，忽略它！
//		if (!dir.exists()) {
//			return array;
//		}
//		
//		// 如果是子目录，进一步检查
//		if (dir.isFile()) {
//			return array;
//		}
//
//		// 读取目录下的内容
//		File[] files = dir.listFiles();
//		for (int i = 0; files != null && i < files.length; i++) {
//			File file = files[i];
//
//			// 如果是子目录，检查和加载库文件
//			if (file.isDirectory()) {
//				String sub = Laxkit.canonical(file);
//				List<String> subs = JNILoader.loadLibraries(sub);
//				array.addAll(subs);
//				continue;
//			}
//			
//			// 取标准格式
//			String filename = Laxkit.canonical(file);
//
//			// 判断文件有效
//			boolean success = (file.isFile() && !file.isHidden() && 
//					JNILoader.isLibrary(filename));
//			if (!success) {
//				continue;
//			}
//
//			// 加载本地库
//			success = JNILoader.loadLibrary(filename);
//			if (success) {
//				array.add(filename);
//			}
//
//			Logger.note("JNILoader.loadLibraries, load native library:" + filename, success);
//		}
//		return array;
//	}
		
	
//	/**
//	 * 判断是动态链接库文件，分为WINDOWS/LINUX两种
//	 * @param filename 文件名
//	 * @return 返回真或者假
//	 */
//	private static boolean isLibrary(String filename) {
//		return (filename.endsWith(DLL) || filename.endsWith(SO));
//	}

//	/**
//	 * 加载指定目录下的动态链接库文件
//	 * @param path 文件目录
//	 * @return 返回加载成功的磁盘文件
//	 */
//	private static List<String> loadBatch(String path) {
//		ArrayList<String> array = new ArrayList<String>();
//		File dir = new File(path);
//		// 不是目录，忽略它
//		if (!dir.isDirectory()) {
//			return array;
//		}
//
//		File[] files = dir.listFiles();
//		for (File file : files) {
//			//			 取出绝对路径
//			//			String filename = file.getAbsolutePath();
//
//			// 取标准格式
//			String filename = Laxkit.canonical(file);
//
//			// 判断文件有效
//			boolean success = (file.isFile() && !file.isHidden() && 
//					JNILoader.isLibrary(filename));
//			if (!success) {
//				continue;
//			}
//
//			// 加载本地库
//			success = JNILoader.loadLibrary(filename);
//			if (success) {
//				array.add(filename);
//			}
//
//			Logger.note("JNILoader.loadBatch, load native library:" + filename, success);
//		}
//		return array;
//	}
	

	//	/** WINDOWS动态链接库后缀格式 **/
	//	private static String DLL = ".dll";
	//
	//	/** LINUX动态链接库前缀 **/
	//	private static String SO_PRE = "lib";
	//
	//	/** LINUX动态链接库后缀 **/
	//	private static String SO_SUF = ".so";

	//	/** 链接库目录，除"user.dir"，其他都在启动命令内定义，格式：-Dlaxcus.library=... -Dlaxcus.task.library=... **/
	//	private static String[] lib_paths = { "user.dir", "laxcus.library",
	//		"laxcus.task.library", "laxcus.scaler.library",
	//	"laxcus.swift.library" };

	//	/**
	//	 * 加载一个本地库文件
	//	 * @param name 文件名
	//	 */
	//	private static boolean loadLibrary(String name) {
	//		try {
	//			System.loadLibrary(name);
	//			return true;
	//		} catch (UnsatisfiedLinkError e) {
	//			//			e.printStackTrace();
	//			Logger.fatal(e);
	//		} catch (SecurityException e) {
	//			Logger.fatal(e);
	//			//			e.printStackTrace();
	//		}
	//		return false;
	//	}
	//
	//	/**
	//	 * 如果是动态连接库，返回它库文件名，否则是空指针
	//	 * @param file 磁盘文件
	//	 * @return 返回库文件名，否则空指针
	//	 */
	//	private static String split(File file) {
	//		String name = file.getName();
	//		if (name.endsWith(DLL)) {
	//			int end = name.length() - DLL.length();
	//			return name.substring(0, end);
	//		} else if (name.startsWith(SO_PRE) && name.endsWith(SO_SUF)) {
	//			int start = SO_PRE.length();
	//			int end = name.length() - SO_SUF.length();
	//			return name.substring(start, end);
	//		}
	//		return null;
	//	}
	//
	//	/**
	//	 * 加载动态链接库
	//	 * 返回加载的动态链接库文件路径
	//	 */
	//	private static List<String> load(String path) {
	//		ArrayList<String> array = new ArrayList<String>();
	//		File dir = new File(path);
	//		File[] files = dir.listFiles();
	//		for (File file : files) {
	//			String name = LibraryLoader.split(file);
	//			if (name == null) {
	//				continue;
	//			}
	//			String filename = file.getAbsolutePath();
	//			// 加载本地库
	//			boolean success = LibraryLoader.loadLibrary(name);
	//			if (success) {
	//				array.add(filename);
	//				Logger.info("Load Native library " + filename);
	//			} else {
	//				Logger.error("Load Native Library " + filename);
	//			}
	//		}
	//		return array;
	//	}

	//	/** 链接库目录，除"user.dir"，其他都在启动命令内定义，格式：-Dlaxcus.library=... -Dlaxcus.task.library=... **/
	//	private static String[] lib_paths = { "user.dir", "laxcus.library",
	//		"laxcus.task.library", "laxcus.scaler.library",
	//	"laxcus.swift.library" };
	//
	//	/**
	//	 * 加载本地的动态链接库文件
	//	 * “laxcus.library”做为一个“java.library.path”指定的路径之一存在，在启动时配置。
	//	 * 
	//	 * @return 返回加载的链接库名文件路径数组
	//	 */
	//	public static List<String> reload() {
	//		ArrayList<String> array = new ArrayList<String>();
	//
	//		for (int i = 0; i < lib_paths.length; i++) {
	//			String path = System.getProperty(lib_paths[i]);
	//			if (path != null) {
	//				List<String> subs = LibraryLoader.load(path);
	//				array.addAll(subs);
	//			}
	//		}
	//
	//		return array;
	//	}

	//	/**
	//	 * 使用JAVA提供的反射机制，增加一个动态链接库。<br>
	//	 * usr_paths 和 sys_paths ，都是ClassLoader中的变量
	//	 * 
	//	 * @param privatePath LAXCUS动态链接库的私有路径
	//	 * @throws NoSuchFieldException 
	//	 * @throws SecurityException 
	//	 * @throws IllegalAccessException 
	//	 * @throws IllegalArgumentException 
	//	 * @throws Exception
	//	 */
	//	public static void addLibraryPath(String privatePath) throws SecurityException,
	//	NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	//
	//		// 系统中还有的链接库目录
	//		Field field = ClassLoader.class.getDeclaredField("usr_paths");
	//		field.setAccessible(true);
	//		String[] paths = (String[]) field.get(null);
	//
	//		// 保存起来
	//		StringBuilder bf = new StringBuilder();
	//		for (int i = 0; i < paths.length; i++) {
	//			if (privatePath.equals(paths[i])) {
	//				continue;
	//			}
	//			bf.append(paths[i]);
	//			bf.append(File.pathSeparator);
	//		}
	//		// 增加LAXCUS动态链接库路径
	//		bf.append(privatePath);
	//
	//		// 重新设置动态链接类路径
	//		System.setProperty("java.library.path", bf.toString());
	//
	//		// 在ClassLoader中定义的系统参数
	//		Field sys = ClassLoader.class.getDeclaredField("sys_paths");
	//		sys.setAccessible(true);
	//		sys.set(null, null);
	//	}
	//
	//	/**
	//	 * 初始化动态链接库
	//	 * 1. 系统重新引导“java.library.path”目录
	//	 * 2. 加载这些目录下的动态链接库
	//	 */
	//	public static void init() {
	//		// 枚举LAXCUS链接库的目录
	//		for (int i = 0; i < lib_paths.length; i++) {
	//			String path = System.getProperty(lib_paths[i]);
	//			if (path == null) {
	//				continue;
	//			}
	//			// 动态增加链接库目录，增入到“java.library.path”中去
	//			try {
	//				LibraryLoader.addLibraryPath(path);
	//			} catch (SecurityException e) {
	//				e.printStackTrace();
	//				Logger.error(e);
	//			} catch (IllegalArgumentException e) {
	//				e.printStackTrace();
	//				Logger.error(e);
	//			} catch (NoSuchFieldException e) {
	//				e.printStackTrace();
	//				Logger.error(e);
	//			} catch (IllegalAccessException e) {
	//				e.printStackTrace();
	//				Logger.error(e);
	//			}
	//		}
	//
	//		// 加载动态链接库
	//		LibraryLoader.reload();
	//	}

	//	 * 两个功能：<br>
	//	 * 1. 利用反射机制，重新指定本地的动态链接库目录。取出ClassLoader的变量“usr_paths”，“sys_paths”参数，将本地私有链接库目录，导入系统的“java.library.path”队列。<br>
	//	 * 2. 重新加载规定动态链库目录下的链接库。<br><br><br><br>


}