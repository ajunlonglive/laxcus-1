/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.pool;

import java.io.*;
import java.util.*;

import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.guide.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 启动任务引导池。<br>
 * 部署在FRONT节点，负责启动时提出输入参数和分配命令。
 * 
 * @author scott.liang
 * @version 1.0 7/26/2020
 * @since laxcus 1.0
 */
public class GuideTaskPool extends GuidePool {

	/**
	 * 更新但是没有部署的动态链接库
	 *
	 * @author scott.liang
	 * @version 1.0 7/11/2021
	 * @since laxcus 1.0
	 */
	public class RefreshLibrary implements Comparable<RefreshLibrary> {

		/** 根目录 **/
		File root;

		/** 库文件 **/
		ArrayList<File> array = new ArrayList<File>();

		public RefreshLibrary(File root) {
			super();
			setRoot(root);
		}

		public void setRoot(File e) {
			root = e;
		}

		public File getRoot() {
			return root;
		}

		public boolean add(File file) {
			if (!array.contains(file)) {
				return array.add(file);
			}
			return false;
		}

		public boolean remove(File file) {
			return array.remove(file);
		}

		public List<File> list() {
			return new ArrayList<File>(array);
		}

		public boolean isEmpty() {
			return array.isEmpty();
		}

		@Override
		public int compareTo(RefreshLibrary that) {
			return Laxkit.compareTo(root, that.root);
		}
	}

	/** 实例 **/
	private static GuideTaskPool selfHandle = new GuideTaskPool();

	/** 库ID生成器 **/
	private SerialGenerator lid = new SerialGenerator(999, Integer.MAX_VALUE - 9999); 

	/** 软件信息 **/
	private Map<Sock, WareTag> tags = new TreeMap<Sock, WareTag>();

	/** 刷新标记 **/
	private volatile boolean refresh;

	/** 新部署但是没有发布的动态链接库 **/
	private Map<File, RefreshLibrary> refreshLibraries = new TreeMap<File, RefreshLibrary>();

	/**
	 * 构造实例
	 */
	private GuideTaskPool() {
		super();
		refresh = false;
	}

	/**
	 * 返回实例
	 * @return
	 */
	public static GuideTaskPool getInstance() {
		return GuideTaskPool.selfHandle;
	}

	/**
	 * 返回下一个库文件ID号
	 * @return 整数码
	 */
	protected int nextLibraryID() {
		return (int) lid.nextSerial();
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.guide.pool.GuideHiddlePool#clear()
	//	 */
	//	@Override
	//	protected void clear() {
	//		super.clear();
	//		tags.clear();
	//	}

	/**
	 * 刷新
	 */
	public void refresh() {
		refresh = true;
		// 唤醒
		wakeup();
		// 判断和等待结果
		while (refresh) {
			delay(500);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 如果目录没有定义，默认为用户目录下的"deploy"目录
		if (getRoot() == null) {
			// 用户目录
			String bin = System.getProperty("user.dir");
			// 假定一个发布目录(实际应用中，全部发布任务都定义在"deploy"目录)
			boolean success = super.setRoot(bin, "boot");
			if (!success) {
				Logger.error(this, "init", "cannot be create task directory");
				return false;
			}
		}

		Logger.info(this, "init", "directory is '%s'", getRoot());

		//		// 加载本地的备注
		//		readRemarks();
		//
		//		// 加载引导应用
		//		loadTasks();

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		while (!isInterrupted()) {
			// 定时检查JAR包更新
			if (refresh) {
				reload();
				refresh = false;
			} else {
				// 定时调用，释放已经成为垃圾的动态链接库文件
				releaseRubbishLibraries();
			}
			// 线程进行等待状态
			sleep();
		}

		Logger.info(this, "process", "exit");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.guide.pool.GuideHiddlePool#finish()
	 */
	@Override
	public void finish() {
		super.finish();
		tags.clear();

		// 释放全部过期库文件
		releaseRubbishLibraries(0);
	}

	/**
	 * 查找软件标记
	 * @param sock
	 * @return
	 */
	public WareTag findWare(Sock sock) {
		super.lockMulti();
		try {
			return tags.get(sock);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断是引导文件
	 * @param file 磁盘文件名
	 * @return 返回是或者否
	 */
	private boolean isGuideTask(File file) {
		String path = canonical(file);
		return path.matches(TF.GTC_REGEX);
	}

	//	/**
	//	 * 从指定目录中取出引导文件
	//	 * @param dir 指定的目录
	//	 * @return 返回引导文件，或者空指针
	//	 */
	//	private GuideElement scanElement(File dir){
	//		// 判断是目录且存在
	//		boolean success = (dir.exists() && dir.isDirectory());
	//		if (!success) {
	//			return null;
	//		}
	//
	//		GuideElement element = new GuideElement(dir);
	//
	//		File[] list = dir.listFiles();
	//		ArrayList<File> files = new ArrayList<File>();
	//		for (int i = 0; i < list.length; i++) {
	//			File sub = list[i];
	//
	//			// 是目录，忽略它!
	//			if (sub.isDirectory()) {
	////				continue;
	//				
	//				scanElement(dir);
	//			}
	//
	//			// 是组件集引导文件，总是只能有一个
	//			if (isGuideTask(sub)) {
	//				files.add(sub);
	//			} else if (isJAR(sub)) {
	//				element.addJAR(new FileKey(sub));
	//			} else if (isLinkLibrary(sub)) {
	//				element.addLibrary(new FileKey(sub));
	//			}
	//		}
	//		// 空记录，忽略退出！
	//		if (files.isEmpty()) {
	//			return null;
	//		}
	//
	//		// 只能有一个引导文件，否则是错误!
	//		if (files.size() != 1) {
	//			Logger.error(this, "scanElement",
	//					"%s fatal! guide task too multi! %d != 1", dir.toString(), files.size());
	//			return null;
	//		}
	//
	//		// 取出这个文件
	//		File file = files.get(0);
	//		element.setBoot(new FileKey(file));
	//
	//		// 提取配置文件
	//		try {
	//			GuideComponentReader reader = new GuideComponentReader(file);
	//			byte[] content = reader.readGuideText();
	//			if (content != null) {
	//				element.setConfigure(content);
	//				return element;
	//			}
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		}
	//		// 不成功，返回真
	//		return null;
	//	}

	/**
	 * 从指定目录中取出引导文件
	 * @param dir 指定的目录
	 * @return 返回引导文件，或者空指针
	 */
	private int scanSubUnit(GuideElement element, File dir) {
		Logger.debug(this, "scanSubUnit", "scan %s", dir);

		// 判断是目录且存在
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			return 0;
		}

		int count = 0;
		File[] list = dir.listFiles();
		for (int i = 0; i < list.length; i++) {
			File file = list[i];

			// 是目录，忽略它!
			if (file.isDirectory()) {
				count += scanSubUnit(element, file);
				continue;
			}

			if (isJAR(file)) {
				element.addJAR(new FileKey(file));
				count++;
			} else if (isLinkLibrary(file)) {
				//				// 不是更新库，忽略它
				//				boolean first = (lid.getLoop() == 0 && lid
				//						.getCurrentSerial() == lid.getMinSerial());
				//				// 不是第一次了，找它的同类值
				//				if (!first) {
				//					if (!hasRefreshLibrary(element.getRoot(), file)) {
				//						continue;
				//					}
				//				}
				//				element.addLibrary(new FileKey(file));
				//				count++;

				// 1. 如果这个链接库存在于垃圾目录中，忽略它
				if (isRubbishLibrary(file)) {
					continue;
				}
				// 2. 如果是第一次，或者更新链接库记录集中有这个动态链接库，记录它
				boolean first = (lid.getLoop() == 0 && lid.getCurrentSerial() == lid.getMinSerial());
				boolean update = (first || hasRefreshLibrary(element.getRoot(), file));
				// 可以记录，保存链接库参数
				if (update) {
					element.addLibrary(new FileKey(file));
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * 从指定目录中取出引导文件
	 * @param dir 指定的目录
	 * @return 返回引导文件，或者空指针
	 */
	private GuideElement scanGuideSoftware(File dir) {
		// 判断是目录且存在
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			return null;
		}

		GuideElement element = new GuideElement(dir);

		ArrayList<File> files = new ArrayList<File>();
		ArrayList<File> dirs = new ArrayList<File>();

		File[] list = dir.listFiles();
		for (int i = 0; i < list.length; i++) {
			File file = list[i];

			// 是目录，记录，忽略它!
			if (file.isDirectory()) {
				dirs.add(file);
				continue;
			}

			// 是组件集引导文件，总是只能有一个
			if (isGuideTask(file)) {
				files.add(file);
			} else if (isJAR(file)) {
				element.addJAR(new FileKey(file));
			} else if (isLinkLibrary(file)) {
				// 1. 如果这个链接库存在于垃圾目录中，忽略它
				if (isRubbishLibrary(file)) {
					continue;
				}
				// 2. 如果是第一次，或者更新链接库记录集中有这个动态链接库，记录它
				boolean first = (lid.getLoop() == 0 && lid.getCurrentSerial() == lid.getMinSerial());
				boolean update = (first || hasRefreshLibrary(element.getRoot(), file));
				// 可以记录，保存链接库参数
				if (update) {
					element.addLibrary(new FileKey(file));
				}

				//				// 不是更新库，忽略它
				//				if (!hasRefreshLibrary(element.getRoot(), file)) {
				//					continue;
				//				}
				//				element.addLibrary(new FileKey(file));
			}
		}
		// 空记录，忽略退出！
		if (files.isEmpty()) {
			return null;
		}

		// 只能有一个引导文件，否则是错误!
		if (files.size() != 1) {
			Logger.error(this, "scanGuideSoftware",
					"%s fatal! guide task too multi! %d != 1", dir.toString(), files.size());
			return null;
		}

		// 取出这个文件
		File file = files.get(0);
		element.setBoot(new FileKey(file));

		// 提取配置文件
		byte[] content = null;
		try {
			GuideComponentReader reader = new GuideComponentReader(file);
			content = reader.readGuideText();
		} catch (IOException e) {
			Logger.error(e);
		}

		if (content == null) {
			Logger.error(this, "scanGuideSoftware", "cannot be find guide text");
			return null;
		}

		// 设置配置
		element.setConfigure(content);
		// 解析子级
		for (File sub : dirs) {
			scanSubUnit(element, sub);
		}

		// 不成功，返回真
		return element;
	}

	/**
	 * 从指定的目录，扫描相关的分布计算组件和附属的JAR包、动态链接库
	 * @param root 根目录
	 * @return 返回全部的组件包集合
	 */
	private List<GuideElement> scanRoot(File root) {
		ArrayList<GuideElement> array = new ArrayList<GuideElement>();

		// 判断目录存在且有效！
		boolean success = (root.exists() && root.isDirectory());
		if (!success) {
			return array;
		}

		// 判断有子目录，提取里面的文件
		File[] files = root.listFiles();
		for (int i = 0; i < files.length; i++) {
			File sub = files[i];
			// 是目录，扫描它下面的全部文件
			if (sub.isDirectory()) {
				//				GuideElement element = scanElement(sub);
				//				if (element != null) {
				//					array.add(element);
				//				}

				GuideElement element = scanGuideSoftware(sub);
				if (element != null) {
					array.add(element);
				}
			}
		}

		//		// 加载动态链接库
		//		for (GuideElement element : array) {
		//			for (FileKey key : element.getLibraries()) {
		//				File file = new File(key.getPath());
		//				success = JNILoader.loadSingleLibrary(file);
		//				Logger.debug(this, "scanDisk", success, "load link library %s", key.getPath());
		//			}
		//		}

		Logger.debug(this, "scanRoot", "\'%s\' guide elements size %d", root, array.size());

		return array;
	}

	//	/**
	//	 * 检测磁盘
	//	 * @return 返回引导成员
	//	 */
	//	private List<GuideElement> scanDisk() {
	//		// 扫描文件
	//		List<GuideElement> elements = scanDisk(getRoot());
	//		// 加载动态链接库
	//		for (GuideElement element : elements) {
	//			for (FileKey key : element.getLibraries()) {
	//				File file = new File(key.getPath());
	//				boolean success = JNILoader.loadSingleLibrary(file);
	//				Logger.debug(this, "scanDisk", success, "load link library %s", key.getPath());
	//			}
	//		}
	//
	//		Logger.debug(this, "scanDisk", "\'%s\' guide elements size %d", getRoot(), elements.size());
	//
	//		return elements;
	//	}

	/**
	 * 更新软件标记
	 * @param elements
	 */
	private void updateWareTags(List<GuideElement> elements) {
		// 锁定!
		super.lockSingle();
		try {
			tags.clear();
			for (GuideElement e : elements) {
				byte[] config = e.getConfigure();
				GuideConfigReader reader = new GuideConfigReader(config);
				WareTag tag = reader.readWareTag();
				List<Sock> socks = reader.readSocks();
				if (socks == null || socks.isEmpty()) {
					Logger.error(this, "updateWareTags", "cannot be resolve Sock!");
					return;
				}
				for (Sock sock : socks) {
					tags.put(sock, tag);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "updateWareTags", "all elements:%d", tags.size());
	}

	/**
	 * 构造一个目录，但是不在磁盘上建立它
	 * @param software 软件名称
	 * @return 返回目录
	 */
	private File buildSubRoot(Siger issuer, Naming software) {
		// 基于用户签名的根目录
		File dir = buildSubRoot(issuer);

		// 1. 生成半截符码
		String wareName = software.toString();
		wareName = wareName.trim().toLowerCase();
		wareName = Halffer.encode(wareName);
		// 次级目录
		return new File(dir, wareName);
	}

	/**
	 * 根据用户SHA256签名，建立专属他的子目录，如果是系统组件，目录是“system”。
	 * @param issuer 用户签名，或者空指针
	 * @return 成功返回真的目录名，否则是空指针
	 */
	private File createSubRoot(Siger issuer, Naming software) {
		// 生成目录
		File dir = buildSubRoot(issuer, software);
		// 判断目录存在且是“目录”属性
		boolean success = (dir.exists() && dir.isDirectory());
		// 不存在，建立一个新的目录
		if (!success) {
			success = dir.mkdirs();
		}
		return (success ? dir : null);
	}

	/**
	 * 重置全部数据
	 */
	public void reset() {
		// 锁定重置
		super.lockSingle();
		try {
			//			clear();
			clearRemarks();
			tags.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 加载磁盘上的引导任务包
	 * @return 成功返回真，否则假
	 */
	private boolean reload() {
		// 重置全部参数
		reset();

		// 不是有效，退出
		if (!isUser()) {
			return false;
		}

		// 生成目录
		File dir = buildSubRoot(listener.getIssuer());
		// 扫描磁盘，提取参数
		List<GuideElement> elements = scanRoot(dir);
		updateWareTags(elements);

		// 更新类加载器和阶段命名
		boolean success = false;
		// 锁定!
		super.lockSingle();
		try {
			// 加载本地的备注
			readRemarks();

			// 通知上级更新
			// 注意！！！不论是否有成员都要更新！
			updateGuides(elements);

			// 发布完成后，清除更新的记录
			for (GuideElement e : elements) {
				removeRefreshLibrary(e.getRoot());
			}

			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}


	//	/**
	//	 * 生成子目录
	//	 * @param software 软件名称
	//	 * @return 返回软件匹配目录
	//	 */
	//	private File createSubRoot(Naming software) {
	//		// 构造目录
	//		File dir = buildSubRoot(software);
	//		// 判断目录存在且是“目录”属性
	//		boolean success = (dir.exists() && dir.isDirectory());
	//		// 不存在，建立一个新的目录
	//		if (!success) {
	//			success = dir.mkdirs();
	//		}
	//		return (success ? dir : null);
	//	}

	/**
	 * 部署应用引导包
	 * @param component 组件包
	 * @return 成功返回真，否则假
	 */
	public boolean deploy(BootComponent component) {
		// 非用户状态，忽略它
		if (!isUser()) {
			return false;
		}

		Naming software = component.getWare();
		File dir = createSubRoot(listener.getIssuer(), software);
		if (dir == null) {
			return false;
		}
		File file = new File(dir, "guide.gtc");
		// 写入磁盘
		boolean success = writeContent(file, component.getContent());
		Logger.debug(this, "deploy", success, "write %s", file);
		return success;
	}

	/**
	 * 部署JAR附件包
	 * @param component 组件包
	 * @return 成功返回真，否则假
	 */
	public boolean deploy(BootAssistComponent component) {
		// 非用户状态，忽略它
		if (!isUser()) {
			return false;
		}

		Naming software = component.getWare();
		File dir = createSubRoot(listener.getIssuer(), software);
		if (dir == null) {
			return false;
		}
		// 写磁盘...
		File file = new File(dir, component.getName());
		boolean success = writeContent(file, component.getContent());

		Logger.debug(this, "deploy", success, "write %s", file);
		return success;
	}

	/**
	 * 生成动态链接库文件
	 * @param root 根目录
	 * @param name 动态链接库文件名称
	 * @return 返回目录
	 */
	private File buildLibraryFile(File root, String name) {
		File dir = null;
		// 生成库目录，找到一个实际的目录
		do {
			String id = String.format("%d", nextLibraryID());
			dir = new File(root, id);
			boolean exists = (dir.exists() && dir.isDirectory());
			// 不存在，退出执行它!
			if (!exists) {
				break;
			}
		} while (true);
		// 建立目录
		boolean success = dir.mkdirs();
		// 返回文件名
		return (success ? new File(dir, name) : null);
	}

	/**
	 * 部署动态链接库
	 * @param component 组件包
	 * @return 成功返回真，否则假
	 */
	public boolean deploy(BootLibraryComponent component) {
		// 非用户状态，忽略它
		if (!isUser()) {
			return false;
		}

		Naming software = component.getWare();
		File dir = createSubRoot(listener.getIssuer(), software);
		if (dir == null) {
			return false;
		}

		// 内容
		byte[] content = component.getContent();

		// 判断有同质文件存在
		if (hasRefreshLibrary(dir, content)) {
			Logger.warning(this, "deploy", "library file exists!");
			return true;
		}

		// 生成文件名，写磁盘...
		//		File file = new File(dir, component.getName());
		File file = buildLibraryFile(dir, component.getName());
		if (file == null) {
			Logger.error(this, "deploy", "cannot be create library directory! %s", dir);
			return false;
		}

		boolean success = writeContent(file, content);

		// 若成功，保存
		if (success) {
			addRefreshLibrary(dir, file);
		}

		//		// 加载动态链接库
		//		if (success) {
		//			success = JNILoader.loadSingleLibrary(file);
		//		}

		Logger.debug(this, "deploy", success, "write to %s", file);
		return success;
	}

	/**
	 * 删除指定的磁盘
	 * @param dir
	 * @return
	 */
	private int shredDirectory(File dir) {
		// 判断存在，不存在返回0
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			return 0;
		}

		int count = 0;
		// 枚举全部
		File[] lists = dir.listFiles();
		for (int i = 0; lists != null && i < lists.length; i++) {
			File sub = lists[i];
			if (sub.isDirectory()) {
				int ret = shredDirectory(sub);
				if(ret < 0) return -1;// 删除错误，返回负数
				// 增加删除统计
				count += ret;
			} else if (sub.isFile()) {
				boolean b = sub.delete();
				if (!b) return -1; // 删除错误，返回负数
				// 增加删除统计
				count += 1;
			}
		}
		// 最后，删除空目录
		success = dir.delete();
		return (success ? count : -1);
	}

	/**
	 * 删除内存和磁盘上的引导文件
	 * @param software 软件名称
	 * @return 返回删除的内存成员和磁盘文件数目，负数是删除错误！
	 */
	@Override
	public int drop(Naming software) {
		// 非用户状态，忽略它
		if (!isUser()) {
			return -1;
		}

		// 删除内存记录
		int memory = super.drop(software);

		// 构造目录，删除磁盘文件
		File dir = buildSubRoot(listener.getIssuer(), software);
		int disks = shredDirectory(dir);
		// 小于0时，返回负值
		if (disks < 0) {
			return -1;
		}

		// 统计根目录下面的成员数目，为0时，删除这个目录
		File root = buildSubRoot(listener.getIssuer());
		boolean success = isEmptyDirectory(root);
		if (success) {
			shredDirectory(root);
		}

		return memory + disks;
	}

	/**
	 * 增加库文件
	 * @param root
	 * @param file
	 */
	private void addRefreshLibrary(File root, File file) {
		RefreshLibrary lib = refreshLibraries.get(root);
		if (lib == null) {
			lib = new RefreshLibrary(root);
			refreshLibraries.put(lib.getRoot(), lib);
		}
		lib.add(file);
	}

	/**
	 * 删除库文件
	 * @param root 单元部件
	 * @return 删除成功返回真，否则假
	 */
	private boolean removeRefreshLibrary(File root) {
		return refreshLibraries.remove(root) != null;
		//		boolean success = refreshLibraries.remove(root) != null;
		//		Logger.note(this, "removeRefreshLibrary", success, "release refresh library! %s", root);
		//		return success;
	}

	/**
	 * 判断链接库存在于记录中
	 * @param root
	 * @param file
	 * @return
	 */
	private boolean hasRefreshLibrary(File root, File file) {
		RefreshLibrary lib = refreshLibraries.get(root);
		if (lib == null) {
			return false;
		}
		// 逐个查找匹配
		for (File temp : lib.list()) {
			if (temp.compareTo(file) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断磁盘和传入内容匹配
	 * @param file 磁盘文件
	 * @param content 准备写入磁盘的数据
	 */
	private boolean isMatching(File file , byte[] content) {
		// 1. 判断文件存在
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			Logger.info(this, "isMatching", "%s is new file!", file);
			return false;
		}

		// 2. 生成MD5编码
		MD5Hash source = null;
		try {
			source = Laxkit.doMD5Hash(file);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		// 3. 内容生成MD5
		MD5Hash dest = Laxkit.doMD5Hash(content);
		// 判断一致
		success = (source != null && dest != null);
		if (success) {
			success = (Laxkit.compareTo(source, dest) == 0);
		}
		Logger.debug(this, "isMatching", success, "%s %s == %s", file, source, dest);
		return success;
	}

	/**
	 * 判断有匹配的库文件
	 * @param root
	 * @param content
	 * @return
	 */
	private boolean hasRefreshLibrary(File root, byte[] content) {
		RefreshLibrary lib = refreshLibraries.get(root);
		if (lib == null) {
			return false;
		}
		// 逐个查找匹配
		for (File file : lib.list()) {
			if (isMatching(file, content)) {
				return true;
			}
		}
		return false;
	}
}