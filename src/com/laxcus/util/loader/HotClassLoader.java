/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.loader;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.jar.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * LAXCUS类加载器。<br><br>
 * 
 * 类加载器在内存中保存任意数量的JAR包，这些JAR包以文件的磁盘路径为唯一标识。<br>
 * 重新构造LAXCUS类加载器，原有记录将从内存中清除，当再次查找类时，系统将重新调用“findClass”和“findResource”方法查找相关类和资源。根据这个特点，我们可以实现JAR数据包的热发布。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/3/2017
 * @since laxcus 1.0
 */
public class HotClassLoader extends LibraryClassLoader { // URLClassLoader {

	/** 类文件后缀 **/
	private final static String CLASS_SUFFIX = ".class";

	/** JAR档案文件数组 **/
	private TreeSet<HotClassEntry> array = new TreeSet<HotClassEntry>();

	/**
	 * 构造默认的类加载器。<br>
	 * 
	 * 发布类加载器和绑定系统加载器，当在本地找不到关连类的时候，再提交给系统类加载器去查找。
	 */
	public HotClassLoader() {
		super(new URL[0], ClassLoader.getSystemClassLoader());
	}

	/**
	 * 构造默认的类加载器，保存发布的JAR档案文件。
	 * @param array JAR档案文件数组
	 */
	public HotClassLoader(Collection<HotClassEntry> array) {
		this();
		addAll(array);
	}

	/**
	 * 保存JAR档案文件。不允许空指针
	 * @param e JAR档案文件实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(HotClassEntry e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批JAR档案文件
	 * @param a JAR档案文件数组
	 * @return 返回新增条目数目
	 */
	public int addAll(Collection<HotClassEntry> a) {
		int size = array.size();
		for (HotClassEntry e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存JAR档案文件
	 * @param filename 磁盘文件名
	 * @param content JAR数据内容
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(String filename, byte[] content) {
		HotClassEntry e = new HotClassEntry(filename, content);
		return add(e);
	}

//	/**
//	 * 保存一个文件
//	 * @param file 磁盘文件
//	 * @return 保存成功返回真，否则假
//	 * @throws IOException
//	 */
//	public boolean add(File file) throws IOException {
//		int len = (int) file.length();
//		byte[] b = new byte[len];
//		FileInputStream in = new FileInputStream(file);
//		in.read(b);
//		in.close();
//		// 生成JAR条目。文件采用规范路径，即过滤“.”和“..”路径符的完全格式
//		HotClassEntry e = new HotClassEntry(file.getCanonicalPath(), b);
//		return add(e);
//	}

	/**
	 * 保存一个文件
	 * @param file 磁盘文件
	 * @return 保存成功返回真，否则假
	 * @throws IOException
	 */
	public boolean add(File file) throws IOException {
		// 生成JAR条目。文件采用规范路径，即过滤“.”和“..”路径符的完全格式
		boolean success = (file.exists() && file.isFile());
		if (success) {
			String path = Laxkit.canonical(file);
			HotClassEntry e = new HotClassEntry(path);
			return add(e);
		}
		return false;
	}
	
	/**
	 * 保存一个文件
	 * @param filename 磁盘文件名称
	 * @return 保存成功返回真，否则假
	 * @throws IOException
	 */
	public boolean add(String filename) throws IOException {
		File file = new File(filename);
		return add(file);
	}

	/**
	 * 清除全部
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 返回JAR文件数目
	 * @return JAR文件数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 从JAR流中读类文件
	 * @param reader JAR读取流
	 * @return 返回解码后的数据流
	 * @throws IOException
	 */
	private byte[] readEntry(JarInputStream reader) throws IOException {
		// 读数据流
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		while(true) {
			int len = reader.read(b, 0, b.length);
			if(len == -1) break;
			writer.write(b, 0, len);
		}
		return writer.toByteArray();
	}

	/**
	 * 从JAR文档发中读取指定的资源内容
	 * @param entry JAR档案文件的内存格式
	 * @param resourceName 资源文件名
	 * @return 返回读取的字节数组，没有返回空指针。
	 * @throws IOException
	 */
	private byte[] readEntry(HotClassEntry entry, String resourceName) throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(entry.getContent());
		JarInputStream reader = new JarInputStream(bin);

		// 原始数据流
		byte[] primitive = null;

		while (true) {
			JarEntry element = reader.getNextJarEntry();
			if (element == null) {
				break;
			}
			// 忽略目录
			if (element.isDirectory()) {
				continue;
			}
			// 文件名
			String name = element.getName();

			// 文件名不匹配，忽略下一个
			if (!name.equals(resourceName)) {
				continue;
			}

			// 解压读出数据流，然后退出
			primitive = readEntry(reader);
			break;
		}
		// 关闭IO
		reader.close();
		bin.close();

		// 返回结果
		return primitive;
	}

	/**
	 * 从JAR档案内容中找到关连类
	 * @param entry JAR档案内容
	 * @param className 类的二进制名称，以“.”号为分隔符，如：com.laxcus.command.xxx
	 * @return 新生成的Class对象
	 * @throws IOException
	 */
	private Class<?> findClass(HotClassEntry entry, String className) throws IOException {
		ByteArrayInputStream raw = new ByteArrayInputStream(entry.getContent());
		JarInputStream reader = new JarInputStream(raw);

		Class<?> clazz = null;

		while (true) {
			// 下一个JAR条目
			JarEntry element = reader.getNextJarEntry();
			// 如果没有，退出
			if (element == null) {
				break;
			}
			// 如果是目录，忽略它
			if (element.isDirectory()) {
				continue;
			}

			// 文件名
			String name = element.getName();

			// 不是类文件，忽略它
			if (!name.endsWith(CLASS_SUFFIX)) {
				continue;
			}
			// 去掉后缀，将以“/”符号为分隔符的字符串，改为以“.”号为分隔符的二进制字符串格式
			name = name.substring(0, name.length() - CLASS_SUFFIX.length());
			name = name.replace('/', '.');
			// 文件名不匹配，忽略下一个
			if (!name.equals(className)) {
				continue;
			}

			// 读一个类文件
			byte[] b = readEntry(reader);

			// 生成辅助参数
			URL url = entry.getURL();
			CodeSigner[] signers = element.getCodeSigners();
			CodeSource codeSource = new CodeSource(url, signers);
			// 交给系统去生成类，然后退出
			clazz = super.defineClass(name, b, 0, b.length, codeSource);
			break;
		}
		// 关闭IO
		reader.close();
		raw.close();

		return clazz;
	}

	/*
	 * (non-Javadoc)
	 * @see java.net.URLClassLoader#findClass(java.lang.String)
	 */
	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException {
//		System.out.printf("find class %s\n", className);
		
		// 从内存中保存的JAR文件中找到关联类文件
		try {
			for (HotClassEntry entry : array) {
				Class<?> clazz = findClass(entry, className);
				if (clazz != null) {
					return clazz;
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}

		// 交给系统去查找
		return super.findClass(className);
	}

	/**
	 * 查找指定格式的URL资源。<br>
	 * URL资源格式：jar:<url>!/{entry}
	 * 
	 * @param entry JAR档案内容
	 * @param resourceName 资源名称。资源名称是以 '/' 分隔的标识资源的路径名称。
	 * 
	 * @return 返回URL，或者空指针
	 * @throws IOException
	 */
	private URL findResource(HotClassEntry entry, String resourceName) throws IOException {
		ByteArrayInputStream raw = new ByteArrayInputStream(entry.getContent());
		JarInputStream reader = new JarInputStream(raw);

		// URL路径
		URL path = null;

		while (true) {
			JarEntry element = reader.getNextJarEntry();
			if (element == null) {
				break;
			}
			// 文件名
			String name = element.getName();

			// 文件名不匹配，忽略，下一个
			if(!name.equals(resourceName)) {
				continue;
			}

			// 文件URL
			URL file = entry.getURL();
			// 文件URL
			String jar = "jar:" + file.toExternalForm() + "!/" + name;
			// 生成以“jar”为前缀的URL，退出
			path = new URL(jar);
			break;
		}
		// 关闭IO
		reader.close();
		raw.close();

		return path;
	}

	/*
	 * (non-Javadoc)
	 * @see java.net.URLClassLoader#findResource(java.lang.String)
	 */
	@Override
	public URL findResource(String name) {
		// 从内存中保存的JAR文件中找到关联类文件
		try {
			for (HotClassEntry entry : array) {
				URL url = findResource(entry, name);
				if (url != null) {
					return url;
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}

		// 以上没有找到，交给系统去处理
		return super.findResource(name);
	}

	/**
	 * 从JAR档案内容中读原始文件的字节流
	 * 
	 * @param entry JAR文件内容
	 * @param name 资源名称。资源名称是以 '/' 分隔的标识资源的路径名称。
	 * 
	 * @return 返回资源文件输入流，没有返回空指针
	 * @throws IOException
	 */
	private InputStream getResourceAsStream(HotClassEntry entry, String name) throws IOException {
		// 读指定的内容
		byte[] b = readEntry(entry, name);
		if (b == null) {
			return null;
		}
		return new ByteArrayInputStream(b);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
	 */
	@Override
	public InputStream getResourceAsStream(String name) {
		// 从内存中保存的JAR文件中找到关联类文件
		try {
			for (HotClassEntry entry : array) {
				InputStream in = getResourceAsStream(entry, name);
				if (in != null) {
					return in;
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}

		// 交给系统去读取
		return super.getResourceAsStream(name);
	}

	/**
	 * 找到与用户签名匹配的JAR档案文件，从中读出指定的资源。
	 * 
	 * @param siger 用户签名
	 * @param name 资源名称。是以“/”号分隔的路径名，存在于JAR文件中
	 * @return 返回资源的字节数组，没有找到返回空指针。
	 * @throws IOException - 执行过程中发生IO异常
	 */
	public byte[] readResource(Siger siger, String name) throws IOException {
		for (HotClassEntry entry : array) {
			// 不匹配，忽略，继续下一个
			if (Laxkit.compareTo(siger, entry.getIssuer()) != 0) {
				continue;
			}
			// 读一个JAR条目。如果有返回，如果没有继续找下一个
			byte[] b = readEntry(entry, name);
			if (b != null) {
				return b;
			}
		}
		// 没有返回空指针
		return null;
	}

	/**
	 * 从JAR档案文件中读取指定的资源，不指定用户签名
	 * @param name 资源名称。
	 * @return 返回资源的字节数组，没有返回空指针
	 * @throws IOException
	 */
	public byte[] readResource(String name) throws IOException {
		return readResource(null, name);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		array.clear(); // 清除全部内存记录
	}

}