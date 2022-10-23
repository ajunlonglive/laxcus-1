/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.pool;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.task.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.guide.*;
import com.laxcus.task.guide.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.each.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.naming.*;

/**
 * 隐形引导池
 * 
 * @author scott.liang
 * @version 1.0 7/26/2020
 * @since laxcus 1.0
 */
abstract class GuidePool extends DiskPool {

	/**
	 * 已经成为垃圾的动态链接库
	 *
	 * @author scott.liang
	 * @version 1.0 7/12/2021
	 * @since laxcus 1.0
	 */
	class RubbishLibrary implements Comparable<RubbishLibrary> {

		/** 文件路径 **/
		String path;

		/** 时间 **/
		long time;

		RubbishLibrary(String filename) {
			super();
			path = filename;
			time = System.currentTimeMillis();
		}

		/**
		 * 判断超时
		 * 
		 * @param timeout 超时时间
		 * @return 真或者假
		 */
		boolean isTimeout(long timeout) {
			return System.currentTimeMillis() - time >= timeout;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(RubbishLibrary that) {
			return Laxkit.compareTo(path, that.path);
		}
	}

	/** 根命名 -> 引导备注 **/
	private TreeMap<Sock, GuideRemark> remarks = new TreeMap<Sock, GuideRemark>();

	/** 根命名 -> 分布应用对标的类加载器 **/
	private TreeMap<Sock, HotClassLoader> loaders = new TreeMap<Sock, HotClassLoader>();

	/** 根命名 -> 项目配置  **/
	private Map<Sock, GuideProject> projects = new TreeMap<Sock, GuideProject>();

	/** 待删除和回收的垃圾文件 **/
	private ArrayList<RubbishLibrary> rubbishLibraries = new ArrayList<RubbishLibrary>();

	/** 用户签名接口 **/
	protected UserListener listener;

	/**
	 * 构造默认的隐形引导池
	 */
	protected GuidePool() {
		super();
	}

	/**
	 * 设置用户签名接口
	 * @param e 用户签名接口实例
	 */
	public void setUserListener(UserListener e) {
		listener = e;
	}

	/**
	 * 返回用户签名接口
	 * @return 用户签名接口实例
	 */
	protected UserListener getUserListener() {
		return listener;
	}

	/**
	 * 尝试删除单个垃圾文件
	 * @param filename
	 * @return 成功返回真，否则假
	 */
	private boolean deleteRubbishLibrary(String filename) {
		boolean b1 = false;
		boolean b2 = false;

		// 删除文件
		File file = new File(filename);
		boolean success = (file.exists() && file.isFile());
		if (success) {
			b1 = file.delete();
		} else {
			b1 = true;
		}

		// 在删除文件基础上，删除目录
		if (b1) {
			File parent = file.getParentFile();
			success = (parent != null && parent.exists() && parent.isDirectory());
			if (success) {
				b2 = parent.delete();
			} else {
				b2 = true;
			}
		}

		success = (b1 && b2);
		// 重新判断文件和目录，物理不存在了...
		if (success) {
			File f = new File(filename);
			File p = f.getParentFile();
			// 不存在...
			success = (!f.exists() && !p.exists());
		}
		return success;
	}

	/**
	 * 释放动态链接库
	 * @param loader
	 * @param delete 是不是删除
	 */
	private void freeRubbishLibraries(HotClassLoader loader) {
		String[] paths = loader.getLibraries();
		int size = (paths == null ? 0 : paths.length);
		for (int i = 0; i < size; i++) {
			String path = paths[i];
			
			// 如果是Windows系统，删除库记录
			if (isWindows()) {
				try {
					loader.freeLoadLibrary(path);
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
			}
			// 是Linux，尝试删除记录，其它不动
			else if (isLinux()) {
				try {
					loader.unregisterNativeLibrary(path);
				} catch (SecurityException e) {
					Logger.error(e);
				} catch (IllegalArgumentException e) {
					Logger.error(e);
				} catch (NoSuchFieldException e) {
					Logger.error(e);
				} catch (IllegalAccessException e) {
					Logger.error(e);
				} catch (Throwable e) {
					Logger.fatal(e);
				}
			}

			// 删除已经是垃圾的动态链接库文件
			boolean success = deleteRubbishLibrary(path);

			// 如果不成功，保存，再由线程尝试重新删除
			if (!success) {
				RubbishLibrary lib = new RubbishLibrary(path);
				// 不存在时，保存它
				if (!rubbishLibraries.contains(lib)) {
					rubbishLibraries.add(lib);
				}
			}
		}
	}
	
	/**
	 * 释放过期的垃圾动态链接库文件
	 * 这个方法被子级调用，会定时清除超时库文件
	 */
	protected void releaseRubbishLibraries(long timeout) {
		// 没有，忽略它
		if (rubbishLibraries.isEmpty()) {
			return;
		}
		// 锁定
		super.lockSingle();
		try {
			Iterator<RubbishLibrary> iterator = rubbishLibraries.iterator();
			while (iterator.hasNext()) {
				RubbishLibrary lib = iterator.next();
				// 没有超时，忽略它
				if (!lib.isTimeout(timeout)) {
					continue;
				}
				// 删除垃圾库文件
				boolean success = deleteRubbishLibrary(lib.path);
				if (success) {
					iterator.remove();
					rubbishLibraries.remove(lib); // 移除库记录
				}
				Logger.note(this, "releaseRubbishLibraries", success, "delete library %s", lib.path);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 这个方法由子类线程调用，从系统配置中取出参数，定时删除
	 */
	protected void releaseRubbishLibraries(){
		String input = System.getProperty("laxcus.timeout.free.tasklib");
		long ms = ConfigParser.splitTime(input, 30000L);
		releaseRubbishLibraries(ms);
	}
	
	/**
	 * 判断这是一个过期的垃圾库文件
	 * @param file
	 * @return
	 */
	protected boolean isRubbishLibrary(File file) {
		for (RubbishLibrary e : this.rubbishLibraries) {
			File other = new File(e.path);
			if (other.compareTo(file) == 0) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 写入磁盘
		writeRemarks();
		remarks.clear();
		// 清除内存记录
		loaders.clear();
		projects.clear();
	}

	/**
	 * 注册注释
	 * @param ware 软件名称
	 * @return 返回删除的单元数目
	 */
	private int dropRemarks(Naming ware) {
		ArrayList<Sock> array = new ArrayList<Sock>();

		// 锁定
		super.lockSingle();
		try {
			for (Sock sock : remarks.keySet()) {
				// 判断匹配
				if (Laxkit.compareTo(sock.getWare(), ware) == 0) {
					array.add(sock);
				}
			}
			// 逐个删除
			for (Sock sock : array) {
				remarks.remove(sock);
			}
		} catch(Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 重新写数据
		int size = array.size();
		if (size > 0) {
			writeRemarks();
		}

		Logger.info(this, "dropRemarks", "delete remarks, count %d", size);

		return size;
	}
	
	/**
	 * 删除类加载器
	 * @param ware 软件名称
	 * @return Sock列表
	 */
	private int dropClassLoaders(Naming ware) {
		ArrayList<Sock> array = new ArrayList<Sock>();
		// 锁定
		super.lockSingle();
		try {
			for (Sock sock : loaders.keySet()) {
				if (Laxkit.compareTo(sock.getWare(), ware) == 0) {
					array.add(sock);
				}
			}
			for (Sock sock : array) {
				HotClassLoader c = loaders.remove(sock);
				if (c != null) {
					freeRubbishLibraries(c);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		Logger.info(this, "dropClassLoaders", "delete class-loaders, count: %d", array.size());

		return array.size();
	}

	/**
	 * 找到与软件名称匹配的项目根名称
	 * @param ware 软件名称
	 * @return Sock列表
	 */
	private int dropProjectss(Naming ware) {
		TreeSet<Sock> array = new TreeSet<Sock>();
		// 锁定
		super.lockSingle();
		try {
			for (Sock sock : projects.keySet()) {
				if (Laxkit.compareTo(sock.getWare(), ware) == 0) {
					array.add(sock);
				}
			}
			for (Sock sock : array) {
				projects.remove(sock);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.info(this, "dropProjectss", "delete projects, count: %d", array.size());
		return array.size();
	}
	
	/**
	 * 删除基于软件名称的全部内存记录
	 * @param ware 软件名称
	 * @return 返回删除内存成员数目
	 */
	public int drop(Naming ware) {
		// 删除注释
		int remarks = dropRemarks(ware);
		int loaders = dropClassLoaders(ware);
		int projects = dropProjectss(ware);

		return remarks + loaders + projects;
	}
	
	/**
	 * 产生但是不建立一个磁盘子目录
	 * @param issuer 用户签名
	 * @return 返回File实例
	 */
	protected File buildSubRoot(Siger issuer) {
		// 名称，保证唯一！
		String name = "system";
		if (issuer != null) {

			// 大写的用户签名
			long sign = EachTrustor.sign(issuer.toString().toUpperCase());
			name = String.format("%x", sign);

			// // 大写的用户签名
			// name = issuer.toString().toUpperCase();
		}

		return new File(getRoot(), name);
	}

	/**
	 * 生成“remarks”文件
	 * @param dir 目录
	 * @return 返回文件实例
	 */
	private File buildRemarks(File dir) {
		return new File(dir, "remarks");
	}

	/**
	 * 判断用于用户状态
	 * @return 成功返回真，否则假
	 */
	protected boolean isUser() {
		if (listener == null) {
			return false;
		}
		if (listener.isOffline()) {
			return false;
		}
		if (listener.isAdministrator()) {
			return false;
		}
		Siger username = listener.getIssuer();
		return listener.isUser() && username != null;
	}

	/**
	 * 清除标记
	 */
	protected void clearRemarks() {
		remarks.clear();
	}

	/**
	 * 加载备注
	 * @return 没有或者加载成功，返回真；否则假。
	 */
	protected boolean readRemarks() {
		// 判断是用户状态
		if (!isUser()) {
			return true;
		}

		// 生成目录
		File dir = buildSubRoot(listener.getIssuer());
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			success = dir.mkdirs();
			if (!success) return false;
		}

		// 判断文件存在！
		File file = buildRemarks(dir);
		success = (file.exists() && file.isFile());
		if (!success) {
			return true;
		}

		int len = (int) file.length();
		try {
			byte[] b = new byte[len];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			// 解析保存
			ClassReader reader = new ClassReader(b);
			int size = reader.readInt();
			for (int i = 0; i < size; i++) {
				GuideRemark element = new GuideRemark(reader);
				remarks.put(element.getSock(), element);
			}
			return true;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return false;
	}

	/**
	 * 保存备注，必须是用户在线状态下才处理
	 * 
	 * @return 成功返回真，失败假
	 */
	private boolean writeRemarks() {
		// 判断处于用户状态
		if (!isUser()) {
			return false;
		}

		// 判断目录存在
		File dir = buildSubRoot(listener.getIssuer());
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			success = dir.mkdirs();
			if (!success) {
				return false;
			}
		}

		// 生成文件
		File file = buildRemarks(dir);
		success = false;
		// 锁定
		super.lockSingle();
		try {
			int size = remarks.size();
			
			// 判断是空，删除磁盘文件
			if (size == 0) {
				boolean exists = (file.exists() && file.isFile());
				if (exists) {
					success = file.delete();
				} else {
					success = true;
				}
			} else {
				ClassWriter writer = new ClassWriter(102400);
				writer.writeInt(size);
				for (GuideRemark e : remarks.values()) {
					writer.writeObject(e);
				}
				byte[] b = writer.effuse();
				// 写入磁盘
				FileOutputStream out = new FileOutputStream(file);
				out.write(b);
				out.close();
				success = true;
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 根据根命名查询项目配置
	 * @param sock 根命名
	 * @return 返回GuideProject实例
	 */
	private GuideRemark findRemark(Sock sock) {
		super.lockMulti();
		try {
			if (sock != null) {
				return remarks.get(sock);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据根命名查询项目配置
	 * @param sock 根命名
	 * @return 返回GuideProject实例
	 */
	private GuideProject findPrivate(Sock sock) {
		super.lockMulti();
		try {
			if (sock != null) {
				return projects.get(sock);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据根命名查找类加载器
	 * @param sock 根命名
	 * @return HotClasLoader实例
	 */
	private HotClassLoader findClassLoader(Sock sock) {
		super.lockMulti();
		try {
			return loaders.get(sock);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 读软件的标题
	 * @param sock 根命名
	 * @return 返回字符串
	 */
	public String readTooltip(Sock sock) {
		// 查找项目
		GuideProject project = findPrivate(sock);
		// 判断项目有效
		if (project == null) {
			return null;
		}
		return project.getTooltip();
	}

	/**
	 * 设置标题
	 * @param sock
	 * @param caption
	 */
	public void setCaption(Sock sock, String caption) {
		GuideRemark remark = findRemark(sock);
		// 锁定!
		super.lockSingle();
		try {
			if (remark == null) {
				remark = new GuideRemark(sock);
				remark.setCaption(caption);
				remarks.put(sock, remark);
			} else {
				remark.setCaption(caption);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 保存
		writeRemarks();
	}

	/**
	 * 设置图标备注！
	 * @param sock
	 * @param icon
	 */
	public void setIcon(Sock sock, byte[] icon) {
		GuideRemark remark = findRemark(sock);
		// 锁定!
		super.lockSingle();
		try {
			if (remark == null) {
				remark = new GuideRemark(sock);
				remark.setIcon(icon);
				remarks.put(sock, remark);
			} else {
				remark.setIcon(icon);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 保存!
		writeRemarks();
	}

	/**
	 * 读软件的标题
	 * @param sock 根命名
	 * @return 返回字符串
	 */
	public String readCaption(Sock sock) {
		GuideRemark remark = findRemark(sock);
		if (remark != null) {
			String caption = remark.getCaption();
			if (caption != null) {
				return caption;
			}
		}

		// 查找项目
		GuideProject project = findPrivate(sock);
		// 判断项目有效
		if (project == null) {
			return null;
		}
		return project.getCaption();
	}

	/**
	 * 读图标字节流
	 * @param sock
	 * @return
	 */
	public byte[] readIcon(Sock sock) {
		GuideRemark remark = findRemark(sock);
		if (remark != null) {
			byte[] icon = remark.getIcon();
			if (icon != null && icon.length > 0) {
				return icon;
			}
		}

		// 查找项目
		GuideProject project = findPrivate(sock);
		// 判断项目有效
		if (project == null) {
			return null;
		}

		// 查找类加载器
		HotClassLoader loader = findClassLoader(sock);
		if (loader == null) {
			return null;
		}
		// 读图标!
		try {
			String path = project.getIconPath();
			if (path != null) {
				return loader.readResource(null, path);
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return null;
	}

	/**
	 * 生成引导任务类
	 * @param sock 根命名
	 * @return 成功返回类实例，否则是空指针
	 */
	public GuideTask createTask(Sock sock) {
		// 判断是空指针
		Laxkit.nullabled(sock);

		// 查找项目
		GuideProject project = findPrivate(sock);
		// 判断项目有效
		if (project == null) {
			Logger.error(this, "createTask", "cannot be find GuideProject! %s", sock);
			return null;
		}

		// 找到关联的类加载器
		HotClassLoader loader = findClassLoader(sock);
		if (loader == null) {
			Logger.error(this, "createTask", "cannot be find HotClassLoader! %s", sock);
			return null;
		}

		try {
			// 取出类实例，生成分布组件实例输出
			String clazzName = project.getGuideClass();
			
			Logger.debug(this, "createTask", "class is %s", clazzName);
			
			// 找到类定义...
			Class<?> clazz = Class.forName(clazzName, true, loader);
			// 生成类实例
			GuideTask task = (GuideTask) clazz.newInstance();
			// 返回实例
			return task;
		} catch (InstantiationException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (ClassNotFoundException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return null;
	}

	/**
	 * 读取GTC/JAR文件内容，生成应用条目。<br>
	 * 条目内容：用户签名、磁盘文件路径、磁盘文件URL、磁盘内容。<br><br>
	 * 
	 * @param issuer 发布者签名
	 * @param key 文件指针
	 * @return 返回实例
	 */
	private HotClassEntry createEntry( FileKey key) {
		String path = key.getPath();
		File file = new File(path);
		// 读文件内容
		byte[] b = readContent(file);
		if (Laxkit.isEmpty(b)) {
			Logger.error(this, "createEntry", "cannot be read content");
			return null;
		}
		// 类路径的URL
		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			Logger.error(e);
		}
		if (url == null) {
			return null;
		}
		// 输出结果
		return new HotClassEntry(null, path, url, b);
	}

	/**
	 * 生成一个新的类加载器
	 * @param element 引导任务组件
	 * @return  返回HotClassLoader实例
	 */
	private HotClassLoader createClassLoader(GuideElement element) {
		ArrayList<HotClassEntry> array = new ArrayList<HotClassEntry>();

		FileKey boot = element.getBoot();
		String path = boot.getPath();
		// 读引导包内容
		byte[] content = readContent(new File(path));

		// 引导包路径的URL
		URL bootURL = null;
		try {
			bootURL = new File(path).toURI().toURL();
		} catch (MalformedURLException e) {
			Logger.error(e);
		}
		if (bootURL == null) {
			Logger.error(this, "createClassLoader", "cannot be resolve %s", path);
			return null;
		}

		// 生成引导类包
		HotClassEntry entry = new HotClassEntry(null, path, bootURL, content);
		array.add(entry);

		Logger.debug(this, "createClassLoader", "%s packet element count %d", path, array.size());

		// 加载JAR辅助文件
		List<FileKey> keys = element.getJARs();
		for (int index = 0; index < keys.size(); index++) {
			// 生成JAR辅助条目
			FileKey key = keys.get(index);
			HotClassEntry sub = createEntry(key);
			if (sub == null) {
				Logger.error(this, "createClassLoader", "cannot be load %s", key.getPath());
				return null;
			}
			array.add(sub);

			Logger.info(this, "createClassLoader", "load jar %s", key.getPath());
		}

		Logger.debug(this, "createClassLoader", "gtc and jar packets size %d", array.size());

		// 返回一个新的类加载器，包括组件包和J辅助JAR包
		HotClassLoader loader = new HotClassLoader(array);
		
		List<FileKey> libraries = element.getLibraries();
		for(int index =0; index < libraries.size(); index++) {
			FileKey key = libraries.get(index);
			loader.addLibrary(key.getPath());
			Logger.debug(this, "createClassLoader", "add library %s", key.getPath());
		}
		Logger.debug(this, "createClassLoader", "add libraries %d", libraries.size());
		
		return loader;
	}

	/**
	 * 加载动态链接库。流程：<br>
	 * 1. 找到引导类 <br>
	 * 2. 基于引导类，加载动态链接库<br><br>
	 * 
	 * @param project 任务管理项目
	 * @param loader 类加载器
	 * 
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void loadLibrary(GuideProject project, HotClassLoader loader) throws ClassNotFoundException, 
		SecurityException, NoSuchMethodException, IllegalArgumentException, 
		IllegalAccessException, InvocationTargetException {

		// 取出动态链接库
		String[] paths = loader.getLibraries();
		int size = (paths == null ? 0 : paths.length);
		if (size < 1) {
			return;
		}

		// 取出类实例，生成分布组件实例输出
		String clazzName = project.getGuideClass();

		// 找到类定义，这个最关键！！！
		// 如果用System.load(String filename)，System会调用getCallerClass()方法，可能会指向其他ClassLoader，导致加载动态链接库出错
		// 而用引导类，它会指向当前的类加载器
		Class<?> clazz = Class.forName(clazzName, true, loader);

		// 如果这个类的类加载器，不是当前的类加载器，这是错误的！
		if (clazz.getClassLoader() != loader) {
			Logger.error(this, "loadLibrary", "wrong class loader! %s != %s",
					clazz.getClassLoader().getClass().getName(), loader.getClass().getName());
			return;
		}

		// 3. 调用Runtime.load0方法，去加载动态链接库
		Runtime rt = Runtime.getRuntime();
		Method load0 = rt.getClass().getDeclaredMethod("load0", new Class[] { Class.class, String.class });
		load0.setAccessible(true);
		// 4. 采用引用方式调用
		for (int i = 0; i < size; i++) {
			String filename = paths[i];
			Logger.debug(this, "loadLibrary", "load %s %s", clazzName, filename);
			
			Object[] objects = new Object[] { clazz, filename };
			load0.invoke(rt, objects);
		}
	}
	
	/**
	 * 生成关联的任务管理项目
	 * @param boot 引导任务组件档案文件
	 * @param loader 类加载器
	 * @return 返回任务管理项目集合，失败是空指针
	 */
	private Map<Sock, GuideProject> createProjects(GuideElement parent, HotClassLoader loader) {
		// 组件实例
		TreeMap<Sock, GuideProject> array = new TreeMap<Sock, GuideProject>();

		// 取出包中的“GUIDE-INF/guides.xml”标签
		byte[] configure = parent.getConfigure();
		// 没有找到，警告！忽略！
		if (Laxkit.isEmpty(configure)) {
			Logger.error(this, "createProjects", "cannot be find %s", TF.TASK_INF); // TaskBootItem.TAG);
			return null;
		}

		// 调用配置读取器，取出组件标记
		GuideConfigReader reader = new GuideConfigReader(configure);
		WareTag wareTag = reader.readWareTag();
		if( wareTag == null) {
			Logger.error(this, "createProjects", "cannot be find %s", TF.TASK_INF); // TaskBootItem.TAG);
			return null;
		}

		List<GuideToken> tokens = reader.readGuideTokens();
		// 判断失败
		if (tokens == null || tokens.isEmpty()) {
			return null;
		}

		boolean loadLibrary = false;
		// 逐个处理输出
		for(GuideToken token : tokens) {
			Sock sock = new Sock(wareTag.getNaming(), token.getNaming());
			GuideProject project = new GuideProject(sock);
			project.setVersion(wareTag.getVersion());
			project.setGuideClass(token.getBootClass());
			project.setCaption(token.getCaption());
			project.setIconPath(token.getIcon());
			project.setTooltip(token.getTooltip());
			
			// 加载动态链接库，只做一次，这个很重要！
			try {
				if (!loadLibrary) {
					loadLibrary(project, loader);
					loadLibrary = true;
				}
			} catch (ClassNotFoundException e) {
				Logger.error(e);
			} catch (IllegalAccessException e) {
				Logger.error(e);
			} catch (SecurityException e) {
				Logger.error(e);
			} catch (IllegalArgumentException e) {
				Logger.error(e);
			} catch (NoSuchMethodException e) {
				Logger.error(e);
			} catch (InvocationTargetException e) {
				Logger.error(e);
			}
			
			// 保存！
			array.put(sock, project);
		}

		return array;
	}
	
	/**
	 * 保留动态链接库
	 * @param elements
	 */
	private void keepGuideLibrary(Collection<GuideElement> elements) {
		Iterator<Map.Entry<Sock, HotClassLoader>> iterator = loaders.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Sock, HotClassLoader> entry = iterator.next();
			HotClassLoader loader = entry.getValue();

			// 从旧的HotClassLoader中保留
			for (GuideElement element : elements) {
				List<FileKey> libraries = element.getLibraries();
				for (int index = 0; index < libraries.size(); index++) {
					FileKey key = libraries.get(index);
					// 从旧的类加载器中删除仍然有用的库
					boolean success = loader.removeLibrary(key.getPath());

					Logger.debug(this, "keepGuideLibrary", success, "keep link library %s", key.getPath());
				}
			}
		}
	}

	/**
	 * 清除内存记录
	 */
	private void clearMemory() {
		// 释放动态链接库
		Iterator<Map.Entry<Sock, HotClassLoader>> iterator = loaders.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Sock, HotClassLoader> entry = iterator.next();
			HotClassLoader c = entry.getValue();
			freeRubbishLibraries(c);
		}

		// 清除
		loaders.clear();
		projects.clear();
	}
	
	/**
	 * 更新引导成员
	 * @param elements
	 */
	protected void updateGuides(Collection<GuideElement> elements) {
		// 保留仍然有用的库
		keepGuideLibrary(elements);
		// 清除全部旧记录
		clearMemory();

		// 输出
		for(GuideElement element : elements) {
			// 1. 给组件分配类加载器
			HotClassLoader loader = createClassLoader(element);
			if (loader == null) {
				Logger.error(this, "updateGuides", "cannot be create HotClassLoader! from %s", element.getRoot());
				continue;
			}
			// 2. 生成引导任务项目
			Map<Sock, GuideProject> jets = createProjects(element, loader);
			if (jets == null || jets.isEmpty()) {
				Logger.error(this, "updateGuides", "cannot be create TaskProject! from %s", element.getRoot());
				continue;
			}
			// 保存两个实例
			for(Sock sock : jets.keySet()) {
				loaders.put(sock, loader);
			}
			projects.putAll(jets);

			Logger.debug(this, "updateGuides", "Load %s, HotClassEntry count %d", element.getRoot(), loader.size());
		}

		Logger.debug(this, "updateGuides", "all ClassLoader size:%d, all TaskProject size:%d", 
				loaders.size(), projects.size());
	}
}


///**
// * 释放动态链接库
// * @param loader
// */
//private void freeLibraries(HotClassLoader loader) {
//	try {
//		loader.freeAllLibraries();
//	} catch (SecurityException e) {
//		Logger.error(e);
//	} catch (IllegalArgumentException e) {
//		Logger.error(e);
//	} catch (NoSuchFieldException e) {
//		Logger.error(e);
//	} catch (IllegalAccessException e) {
//		Logger.error(e);
//	} catch (NoSuchMethodException e) {
//		Logger.error(e);
//	} catch (InvocationTargetException e) {
//		Logger.error(e);
//	}
//}



///**
// * 找到与软件名匹配的根命名
// * @param ware 软件名称
// * @return Sock列表
// */
//private List<Sock> findRemarkSocks(Naming ware) {
//	TreeSet<Sock> array = new TreeSet<Sock>();
//	// 锁定
//	super.lockSingle();
//	try {
//		for (Sock sock : remarks.keySet()) {
//			if (Laxkit.compareTo(sock.getWare(), ware) == 0) {
//				array.add(sock);
//			}
//		}
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockSingle();
//	}
//	return new ArrayList<Sock>(array);
//}

///**
// * 找到与软件名称匹配的类加载器根名称
// * @param ware 软件名称
// * @return Sock列表
// */
//private List<Sock> findClassLoaderSocks(Naming ware) {
//	TreeSet<Sock> array = new TreeSet<Sock>();
//	// 锁定
//	super.lockSingle();
//	try {
//		for (Sock sock : loaders.keySet()) {
//			if (Laxkit.compareTo(sock.getWare(), ware) == 0) {
//				array.add(sock);
//			}
//		}
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockSingle();
//	}
//	return new ArrayList<Sock>(array);
//}

///**
// * 找到与软件名称匹配的项目根名称
// * @param ware 软件名称
// * @return Sock列表
// */
//private List<Sock> findProjectsSocks(Naming ware) {
//	TreeSet<Sock> array = new TreeSet<Sock>();
//	// 锁定
//	super.lockSingle();
//	try {
//		for (Sock sock : projects.keySet()) {
//			if (Laxkit.compareTo(sock.getWare(), ware) == 0) {
//				array.add(sock);
//			}
//		}
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockSingle();
//	}
//	return new ArrayList<Sock>(array);
//}


///**
// * 删除基于软件名称的全部内存记录
// * @param software 软件名称
// * @return 返回删除内存成员数目
// */
//public int drop(Naming software) {
//	// 删除注释
//	int count = dropRemark(software);
//	
//	TreeSet<Sock> array = new TreeSet<Sock>();
//	// 提到全部
//	array.addAll(findClassLoaderSocks(software));
//	array.addAll(findProjectsSocks(software));
//	// 锁定
//	super.lockSingle();
//	try {
//		// 逐一删除
//		for (Sock sock : array) {
//			boolean success = (loaders.remove(sock) != null);
//			if (success) {
//				count++;
//			}
//			success = (projects.remove(sock) != null);
//			if (success) {
//				count++;
//			}
//		}
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockSingle();
//	}
//
//	Logger.debug(this, "drop", "delete class-loaders and projects,  count:%d", count);
//	return count;
//}


//	/**
//	 * 生成关联的任务管理项目
//	 * @param boot 引导任务组件档案文件
//	 * @param loader 类加载器
//	 * @return 返回任务管理项目集合，失败是空指针
//	 */
//	private Map<Sock, GuideProject> createProjects(GuideElement parent, HotClassLoader loader) {
//		// 组件实例
//		TreeMap<Sock, GuideProject> array = new TreeMap<Sock, GuideProject>();
//
//		// 取出包中的“GUIDE-INF/guides.xml”标签
//		byte[] configure = parent.getConfigure();
//		// 没有找到，警告！忽略！
//		if (Laxkit.isEmpty(configure)) {
//			Logger.error(this, "createProjects", "cannot be find %s", TF.TASK_INF); // TaskBootItem.TAG);
//			return null;
//		}
//
//		// 调用配置读取器，取出组件标记
//		GuideConfigReader reader = new GuideConfigReader(configure);
//		WareTag tag = reader.readWareTag();
//		if( tag == null) {
//			Logger.error(this, "createProjects", "cannot be find %s", TF.TASK_INF); // TaskBootItem.TAG);
//			return null;
//		}
//
//		Document document = XMLocal.loadXMLSource(configure);
//		if (document == null) {
//			return null;
//		}
//
//		// 解析单项
//		NodeList nodes = document.getElementsByTagName("guide");
//		int size = nodes.getLength();
//		for (int i = 0; i < size; i++) {
//			Element element = (Element) nodes.item(i);
//
//			// 组件命名
//			String naming = XMLocal.getValue(element, "naming");
//			// 任务类路径
//			String clazz = XMLocal.getValue(element, "boot-class"); 
//			// 资源配置(任意字符串格式.具体由用户的Project子类解释)
//			String icon = XMLocal.getValue(element, "icon"); 
//			// 应用组件标题，展示给用户使用的，类似“product-name”
//			String caption = XMLocal.getValue(element, "caption"); 
//			// 工具提示，展示给用户使用的
//			String tooltip = XMLocal.getValue(element, "tooltip"); 
//
//			Sock sock = new Sock(tag.getNaming(), naming);
//			GuideProject project = new GuideProject(sock);
//			project.setVersion(tag.getVersion());
//			project.setCaption(caption);
//			project.setIconPath(icon);
//			project.setGuideClass(clazz);
//			project.setTooltip(tooltip);
//			// 保存！
//			array.put(sock, project);
//		}
//
//		return array;
//	}


//		Document document = XMLocal.loadXMLSource(configure);
//		if (document == null) {
//			return null;
//		}
//
//		// 解析单项
//		NodeList nodes = document.getElementsByTagName("guide");
//		int size = nodes.getLength();
//		for (int i = 0; i < size; i++) {
//			Element element = (Element) nodes.item(i);
//
//			// 组件命名
//			String naming = XMLocal.getValue(element, "naming");
//			// 任务类路径
//			String clazz = XMLocal.getValue(element, "boot-class"); 
//			// 资源配置(任意字符串格式.具体由用户的Project子类解释)
//			String icon = XMLocal.getValue(element, "icon"); 
//			// 应用组件标题，展示给用户使用的，类似“product-name”
//			String caption = XMLocal.getValue(element, "caption"); 
//			// 工具提示，展示给用户使用的
//			String tooltip = XMLocal.getValue(element, "tooltip"); 
//
//			Sock sock = new Sock(wareTag.getNaming(), naming);
//			GuideProject project = new GuideProject(sock);
//			project.setVersion(wareTag.getVersion());
//			project.setCaption(caption);
//			project.setIconPath(icon);
//			project.setGuideClass(clazz);
//			project.setTooltip(tooltip);
//			// 保存！
//			array.put(sock, project);
//		}
