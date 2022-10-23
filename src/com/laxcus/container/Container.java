/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.container;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.loader.*;

/**
 * 应用容器 <br><br>
 * 
 * 一个软件包括的：<br>
 * 1. 软件包 (.jar后缀格式） <br>
 * 2. 动态链接库（.dll / .so格式） <br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/28/2021
 * @since laxcus 1.0
 */
class Container {

	/**
	 * 保存临时的包资源
	 *
	 * @author scott.liang
	 * @version 1.0 7/3/2021
	 * @since laxcus 1.0
	 */
	class Basket {

		/** JAR包 **/
		ArrayList<HotClassEntry> entries = new ArrayList<HotClassEntry>();

		/** 动态链接库文件名 **/
		ArrayList<String> libraries = new ArrayList<String>();

		public Basket() {
			super();
		}

		void addEntry(HotClassEntry e) {
			entries.add(e);
		}

		void addLibrary(String filename) {
			libraries.add(filename);
		}

		void add(Basket e) {
			entries.addAll(e.entries);
			libraries.addAll(e.libraries);
		}

		void addAll(Collection<Basket> a) {
			for (Basket e : a) {
				add(e);
			}
		}
	}

	/** WINDOWS动态链接库后缀 **/
	private static String DLL = "^\\s*([\\w\\W]+)(?i)(\\.so)\\s*$";

	/** LINUX动态链接库后缀 **/
	private static String SO = "^\\s*([\\w\\W]+)(?i)(\\.dll)\\s*$";

	/** JAR包 **/
	private static String JAR = "^\\s*([\\w\\W]+)(?i)(\\.jar)\\s*$";
	
	/** 软件名称 **/
	private String name;

	/** 根目录 **/
	private File root;

	/** 加载器 **/
	private ApplicationClassLoader loader;

	/**
	 * 构造默认的应用容器
	 */
	public Container() {
		super();
	}

	/**
	 * 返回根目录
	 * @return
	 */
	public File getRoot() {
		return root;
	}
	
	/**
	 * 返回应用软件名称
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 设置应用软件名称
	 * @param s 名称
	 */
	private void setName(String s) {
		name = s;
	}

	/**
	 * 返回进程号
	 * @return 进程号
	 */
	public long getPID() {
		if (loader == null) {
			return -1;
		}
		return loader.getPID();
	}

	/**
	 * 读取磁盘内容
	 * @param file 磁盘
	 * @return 返回字节数组
	 * @throws IOException
	 */
	private byte[] readContent(File file) throws IOException {
		byte[] b = new byte[(int) file.length()];
		// 读取磁盘文件
		FileInputStream in = new FileInputStream(file);
		int len = in.read(b);
		in.close();

		if (len < 1 || len != file.length()) {
			throw new IOException("illegal byte array!");
		}
		return b;
	}

	/**
	 * 是JAR文件
	 * @param file
	 * @return
	 */
	private boolean isJar(File file) {
		String filename = Laxkit.canonical(file);
		return filename.matches(JAR);
	}

	/**
	 * 判断是动态链接库文件，分为WINDOWS/LINUX两种
	 * @param filename 文件名
	 * @return 返回真或者假
	 */
	private boolean isLibrary(File file) {
		String filename = Laxkit.canonical(file);
		return (filename.matches(Container.DLL) || filename.matches(Container.SO));
	}

	/**
	 * 读取一个JAR包
	 * @param file 磁盘文件
	 * @return 返回JAR包的字节数组
	 * @throws IOException
	 */
	private HotClassEntry readJar(File file) throws IOException {
		byte[] b = readContent(file);

		URL bootURL = file.toURI().toURL();
		String filename = Laxkit.canonical(file);

		return new HotClassEntry(null, filename, bootURL, b);
	}

	//	private List<HotClassEntry> loadDirectory(File file) throws IOException {
	//		ArrayList<HotClassEntry> array = new ArrayList<HotClassEntry>();
	//		File[] subs = file.listFiles();
	//		for (File sub : subs) {
	//			if (sub.isDirectory()) {
	//				List<HotClassEntry> a = loadDirectory(sub);
	//				if (a != null) {
	//					array.addAll(a);
	//				}
	//			} else if (isJar(sub)) {
	//				HotClassEntry e = readJar(sub);
	//				array.add(e);
	//			} else if (isLibrary(sub)) {
	//				// 加载动态链接库
	//				String filename = Laxkit.canonical(sub);
	////				System.load(filename);
	//				libraries.add(filename);
	//			}
	//		}
	//		return array;
	//	}

	private Basket loadDirectory(File file) throws IOException {
		Basket entry = new Basket();
		File[] subs = file.listFiles();
		for (File sub : subs) {
			if (sub.isDirectory()) {
				Basket that = loadDirectory(sub);
				entry.add(that);
			} else if (isJar(sub)) {
				HotClassEntry e = readJar(sub);
				entry.addEntry(e);
			} else if (isLibrary(sub)) {
				// 保存动态链接库文件
				String filename = Laxkit.canonical(sub);
				entry.addLibrary(filename);
			}
		}
		return entry;
	}

	/**
	 * 加载目录下的全部文件
	 * @param path
	 * @param pid
	 * @throws IOException
	 */
	private void load(File path, long pid) throws IOException {
		// 如果不是目录
		if (!path.isDirectory()) {
			throw new IOException("illegal directory "+path.toString());
		}

		Basket entry = new Basket();

		File[] files = path.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				Basket that = loadDirectory(file);
				entry.add(that);
			} else if (isJar(file)) {
				HotClassEntry e = readJar(file);
				entry.addEntry(e);
			} else if (isLibrary(file)) {
				// 保存动态链接库文件
				String filename = Laxkit.canonical(file);
				entry.addLibrary(filename);
			}
		}

		// 加载到类加载器
		loader = new ApplicationClassLoader(entry.entries);
		loader.setPID(pid);
		loader.addLibraries(entry.libraries);
	}

//	/**
//	 * 把"DAS"包解压
//	 * @param source DAS源文件
//	 * @throws IOException
//	 */
//	private void decompress(File source) throws IOException {
//		// 读文件
//		byte[] content = readContent(source);
//		// 逐个读取单元
//		ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
//		ZipInputStream jin = new ZipInputStream(bin);
//		while (true) {
//			ZipEntry entry = jin.getNextEntry();
//			if (entry == null) {
//				break;
//			}
//
//			// 只读文件，如果是目录，忽略！
//			if (entry.isDirectory()) {
//				continue;
//			}
//
//			// 文件内容
//			String name = entry.getName();
////			System.out.println(name);
//
//			File temp = root;
//			int last = name.lastIndexOf("/");
//			if (last > -1) {
//				String prefix = name.substring(0, last);
//				temp = new File(temp, prefix);
//
//				// 判断目录存在，没有新建一个
//				boolean success = (temp.exists() && temp.isDirectory());
//				if (!success) {
//					success = temp.mkdirs();
//					if (!success) {
//						String fault = String.format("cannot be create %s", Laxkit.canonical(temp));
//						throw new IOException(fault);
//					}
//				}
//				name = name.substring(last + 1);
//			}
//
//			// 读一个文件
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			byte[] b = new byte[1024];
//			do {
//				int len = jin.read(b, 0, b.length);
//				if (len == -1) {
//					break;
//				}
//				out.write(b, 0, len);
//			} while (true);
//			b = out.toByteArray();
//
//			// 写入磁盘
//			File w = new File(temp, name);
//			FileOutputStream os = new FileOutputStream(w);
//			os.write(b);
//			os.close();
//		}
//
//		jin.close();
//		bin.close();
//	}

	
	/**
	 * 把应用软件包解压
	 * @param content 字节数组
	 * @throws IOException 异常
	 */
	private void decompress(byte[] content) throws IOException {
		// 逐个读取单元
		ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
		ZipInputStream jin = new ZipInputStream(bin);
		while (true) {
			ZipEntry entry = jin.getNextEntry();
			if (entry == null) {
				break;
			}

			// 只读文件，如果是目录，忽略！
			if (entry.isDirectory()) {
				continue;
			}

			// 文件内容
			String name = entry.getName();

			File temp = root;
			int last = name.lastIndexOf("/");
			if (last > -1) {
				String prefix = name.substring(0, last);
				temp = new File(temp, prefix);

				// 判断目录存在，没有新建一个
				boolean success = (temp.exists() && temp.isDirectory());
				if (!success) {
					success = temp.mkdirs();
					if (!success) {
						String fault = String.format("cannot be create %s", Laxkit.canonical(temp));
						throw new IOException(fault);
					}
				}
				name = name.substring(last + 1);
			}

			// 读一个文件
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			do {
				int len = jin.read(b, 0, b.length);
				if (len == -1) {
					break;
				}
				out.write(b, 0, len);
			} while (true);
			b = out.toByteArray();

			// 写入磁盘
			File w = new File(temp, name);
			FileOutputStream os = new FileOutputStream(w);
			os.write(b);
			os.close();
		}

		jin.close();
		bin.close();
	}
	
	/**
	 * 把"DAS"包解压
	 * @param source DAS源文件
	 * @throws IOException
	 */
	private void decompress(File source) throws IOException {
		// 读文件
		byte[] content = readContent(source);
		// 解压到指定目录下面
		this.decompress(content);
	}
	
	/**
	 * 生成运行目录
	 * 
	 * @param runDir
	 * @param software
	 * @param pid
	 * @throws IOException
	 */
	private void doRunDirectory(File rootDir, String software, long pid) throws IOException {
		setName(software);
		software = software.toLowerCase();
		// 生成目录
		root = new File(rootDir, String.format("%s%d", software, pid));
		boolean success = (root.exists() && root.isDirectory());
		if (!success) {
			success = root.mkdirs();
			if (!success) {
				String fault = String.format("cannot be create %s", Laxkit.canonical(root));
				throw new IOException(fault);
			}
		}
	}
	
//	/**
//	 * 加载一个文件
//	 * @param runDir 临时运行目录
//	 * @param software 软件名称
//	 * @param pid 进程编号
//	 * @param source 源文件
//	 * @throws IOException
//	 */
//	public void load(File runDir, String software, long pid, File source) throws IOException {
////		setName(software);
////		software  = software.toLowerCase();
////		// 生成目录
////		root = new File(runDir, String.format("%s%d", software, pid));
////		boolean success = (root.exists() && root.isDirectory());
////		if (!success) {
////			success = root.mkdirs();
////			if (!success) {
////				String fault = String.format("cannot be create %s", Laxkit.canonical(root));
////				throw new IOException(fault);
////			}
////		}
//		
//		// 生成运行目录
//		doRunDirectory(runDir, software, pid);
//
//		// 解压保存到磁盘上
//		decompress(source);
//
//		//		// 读文件
//		//		byte[] content = readContent(source);
//		//		ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
//		//		ZipInputStream jin = new ZipInputStream(bin);
//		//		while (true) {
//		//			ZipEntry entry = jin.getNextEntry();
//		//			if (entry == null) {
//		//				break;
//		//			}
//		//
//		//			// 只读文件，如果是目录，忽略！
//		//			if (entry.isDirectory()) {
//		//				continue;
//		//			}
//		//
//		//			// 文件内容
//		//			String name = entry.getName();
//		//			System.out.println(name);
//		//			
//		//			File temp = root;
//		//			int last = name.lastIndexOf("/");
//		//			if (last > -1) {
//		//				String prefix = name.substring(0, last);
//		//				temp = new File(temp, prefix);
//		//				
//		//				// 判断目录存在，没有新建一个
//		//				success = (temp.exists() && temp.isDirectory());
//		//				if (!success) {
//		//					success = temp.mkdirs();
//		//					if (!success) {
//		//						throw new IOException("cannot be create " + temp.toString());
//		//					}
//		//				}
//		//				name = name.substring(last + 1);
//		//			}
//		//
//		//			// 读一个文件
//		//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//		//			byte[] b = new byte[1024];
//		//			do {
//		//				int len = jin.read(b, 0, b.length);
//		//				if (len == -1) {
//		//					break;
//		//				}
//		//				out.write(b, 0, len);
//		//			} while (true);
//		//			b = out.toByteArray();
//		//			
//		//			// 写入磁盘
//		//			File w = new File(temp, name);
//		//			FileOutputStream os = new FileOutputStream(w);
//		//			os.write(b);
//		//			os.close();
//		//		}
//		//		
//		//		jin.close();
//		//		bin.close();
//
//		// 加载目录
//		load(root, pid);
//	}

	/**
	 * 加载一个文件
	 * @param runDir 临时运行目录
	 * @param software 软件名称
	 * @param pid 进程编号
	 * @param source 源文件
	 * @throws IOException
	 */
	public void load(File runDir, String software, long pid, File source) throws IOException {
		// 生成运行目录
		doRunDirectory(runDir, software, pid);

		// 解压保存到磁盘上
		decompress(source);

		// 加载目录
		load(root, pid);
	}
	
	/**
	 * 加载一个文件
	 * @param runDir 临时运行目录
	 * @param software 软件名称
	 * @param pid 进程编号
	 * @param content 源文件
	 * @throws IOException
	 */
	public void load(File runDir, String software, long pid, byte[] content) throws IOException {
		// 生成运行目录
		doRunDirectory(runDir, software, pid);

		// 解压保存到磁盘上
		decompress(content);

		// 加载目录
		load(root, pid);
	}
	
	/**
	 * 删除目录
	 * @param dir
	 * @return
	 */
	private int deleteDirectory(File dir) {
		int count = 0;
		File[] files = dir.listFiles();
		int size = (files == null ? 0 : files.length);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				int ret = deleteDirectory(file);
				count += ret;
			} else if (file.isFile()) {
				boolean success = file.delete();
				if (success) {
					count++;
				} else {

				}
			}
		}

		// 删除目录
		boolean success = dir.delete();
		if (success) {
			count++;
		}
		return count;
	}

	/**
	 * 释放全部资源，包括目录和文件
	 * @return 成功返回真，否则假
	 */
	protected int release() {
		if (root == null) {
			return -1;
		}
		// 判断存在且是目录
		boolean success = (root.exists() && root.isDirectory());
		if (!success) {
			return 0;
		}

		int count = 0;

		// 释放动态链接库
		int libs = 0;
		try {
			libs = loader.freeAllLibraries();
		} catch (SecurityException e) {
			Logger.error(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (NoSuchFieldException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		
//		System.out.printf("释放了 %d 个链接库\n", libs);

		Logger.debug(this, "release", "free libraries %d", libs);

		if (libs > 0) {
			count += libs;
		}

		File[] files = root.listFiles();
		int size = (files == null ?0 : files.length);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				int ret = deleteDirectory(file);
				count += ret;
			} else if (file.isFile()) {
				boolean b = file.delete();
				if (b) {
					count++;
				} else {
					return -1;
				}
			}
		}

		// 删除根目录
		success = root.delete();
		if (success) {
			count++;
		}
		return count;
	}

	/**
	 * 生成一个类实例
	 * @param clazzName
	 * @return 返回类
	 */
	public Class<?> createClass(String clazzName) throws ContainerException {
		if (loader == null) {
			throw new NullPointerException();
		}

		try {
			return Class.forName(clazzName, true, loader);
		} catch (ClassNotFoundException e) {
			throw new ContainerException(e);
		} catch (Throwable e) {
			throw new ContainerException(e);
		}
	}

	/**
	 * 读资源数据
	 * @param name jar文件路径
	 * @return 返回解析的字节数组
	 * @throws ContainerException
	 */
	public byte[] readResource(String name) throws ContainerException {
		try {
			return loader.readResource(name);
		} catch (IOException e) {
			throw new ContainerException(e);
		} catch (Throwable e) {
			throw new ContainerException(e);
		}
	}

}