/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import java.io.*;
import java.util.*;

import com.laxcus.command.cloud.*;
import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 异地分布任务组件池 <br>
 * <br>
 * 
 * 接收来自ACCOUNT节点的分布任务组件，在本地发布、检查、分配。
 * 
 * @author scott.liang
 * @version 1.1 2/12/2015
 * @since laxcus 1.0
 */
public class RemoteTaskPool extends TaskToolPool {

	/** 阶段签名 -> 组件档案，包括DTC文件、JAR辅助文件、动态链接库文件 **/
	private TreeMap<TaskPart, TaskGroup> groups = new TreeMap<TaskPart, TaskGroup>();

	/** 刷新标记 **/
	private volatile boolean refresh;
	
	
	/**
	 * 构造分布组件管理池，指定任务命名类型
	 * 
	 * @param family CONDUCT/ESTABLISH/CONTACT阶段类型
	 */
	protected RemoteTaskPool(int family) {
		super(family);
		// 30秒启动一次判断刷新，如果发生重新加载
		setSleepTime(30);
		// 默认没有刷新
		setRefresh(false);
	}

	/**
	 * 检查是一个许可的分布任务组件引导文件
	 * 
	 * @param boot
	 *            引导文件
	 * @return 返回真或者假
	 */
	private boolean allow(TaskPart part) {
		// 判断符合定义
		boolean success = match(part.getFamily());
		if (!success) {
			Logger.warning(this, "allow", "illegal pahse family %s",
					PhaseTag.translate(part.getFamily()));
			return false;
		}

		Siger issuer = part.getIssuer();
		// 如果是注册用户的分布任务组件，判断签名存在。系统组件默认是允许！
		if (issuer != null) {
			TaskListener listener = getTaskListener();
			success = listener.hasTaskUser(issuer);

			// 用户签名不存在，忽略它
			if (!success) {
				Logger.warning(this, "allow", "refuse %s", part);
				return false;
			}
		}
		// 允许！
		return true;
	}

	/**
	 * 执行刷新
	 */
	private void doRefresh() {
		setRefresh(true);
	}

	/**
	 * 设置刷新标记
	 * 
	 * @param b
	 *            布尔值
	 */
	private void setRefresh(boolean b) {
		refresh = b;
	}

	/**
	 * 判断要求刷新
	 * 
	 * @return
	 */
	private boolean isRefresh() {
		return refresh;
	}

	//	/**
	//	 * 判断有更新
	//	 * @return
	//	 */
	//	private boolean hasRefresh() {
	//		// 锁定
	//		super.lockMulti();
	//		try {
	//			return (updates.size() > 0);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}
	//	
	//	/**
	//	 * 更新一个用户账号下的全部应用
	//	 * @param issuer 用户签名
	//	 */
	//	private void doRefresh(TaskPart part) {
	//		boolean success = false;
	//		// 锁定
	//		super.lockSingle();
	//		try {
	//			updates.add(part);
	//			success = true;
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//		// 更新
	//		if (success) {
	//			wakeup();
	//		}
	//	}
	//	
	//	/**
	//	 * 弹出一个组件
	//	 * @return
	//	 */
	//	private TaskPart popup() {
	//		// 锁定
	//		super.lockSingle();
	//		try {
	//			if (updates.size() > 0) {
	//				return updates.remove(0);
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//		return null;
	//	}


	/*
	 * 线程启动前的初始化
	 * 
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 如果没有定义根目录，这是错误
		if (getRoot() == null) {
			throw new NullPointerException("cannot be define task directory");
		}

		Logger.info(this, "init", "'%s' directory is '%s'",
				PhaseTag.translate(getFamily()), getRoot());

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		while (!isInterrupted()) {
			// 检查分布任务组件包更新
			if (isRefresh()) {
				// 收到通知，重新加载
				reload();
				// 重置!
				setRefresh(false);
			} else {
				// 删除垃圾库，默认30秒超时，或者写在系统配置中“laxcus.timeout.freelibrary”
				releaseRubbishLibraries();
			}
			// 线程进行等待状态
			sleep();
		}

		//		while (!isInterrupted()) {
		//			// 检查分布任务组件包更新
		//			if (hasRefresh()) {
		//				reload();
		//			} else {
		//				// 删除垃圾，默认30秒超时，或者写在系统配置中“laxcus.timeout.freelibrary”
		//				long ts = getFreeLibraryTimeout();
		//				releaseLibraries(ts);
		//			}
		//			// 线程进行等待状态
		//			sleep();
		//		}

		Logger.info(this, "process", "exit");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		super.finish();
		groups.clear();

		// 全部清除
		releaseRubbishLibraries(0);
		// 释放全部垃圾目录
		shredSoftwareDirectories();
	}

	/**
	 * 获取磁盘上的用户签名。 这些签名从磁盘上读取出来，不需要匹配所在节点的账号。
	 * 
	 * @return 全部用户签名
	 */
	public List<Siger> scanIssuers() {
		TreeSet<Siger> a = new TreeSet<Siger>();
		List<TaskGroup> elements = detect();
		for (TaskGroup e : elements) {
			TaskPart part = e.getPart();
			Siger issuer = part.getIssuer();
			// 有签名，非系统组件，保存它！
			if (issuer != null) {
				a.add(issuer);
			}
		}
		return new ArrayList<Siger>(a);
	}

	/**
	 * 查找分布任务组件群
	 * 
	 * @param part 分布任务组件工作部件
	 * @return 返回TaskGroup实例，或者空指针
	 */
	public TaskGroup findGroup(TaskPart part) {
		Laxkit.nullabled(part);
		// 锁定
		super.lockMulti();
		try {
			TaskGroup group = groups.get(part);
			if (group != null) {
				return group.duplicate();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 找到所属成员
	 * 
	 * @param section 组件任务区
	 * @return 返回实例，没有是空指针
	 */
	private TaskElement findTaskElement(TaskSection section) {
		Laxkit.nullabled(section);
		TaskPart part = section.getTaskPart();
		// 锁定
		super.lockMulti();
		try {
			TaskGroup document = groups.get(part);
			if (document != null) {
				for (TaskElement e : document.list()) {
					boolean match = (Laxkit.compareTo(e.getSection(), section) == 0);
					if (match) {
						return e;
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 找到所属成员
	 * 
	 * @param section 组件任务区
	 * @return 返回实例，没有是空指针
	 */
	public boolean hasTaskElement(TaskSection section) {
		TaskElement element = findTaskElement(section);
		return element != null;
	}

	/**
	 * 找到所属成员
	 * 
	 * @param issuer 用户签名
	 * @param software 软件名称
	 * @return 存在返回真，否则假
	 */
	public boolean hasTaskElement(Siger issuer, Naming software) {
		TaskSection section = new TaskSection(issuer, getFamily(), software);
		return hasTask(section);
	}

	/**
	 * 查找分布任务组件群
	 * 
	 * @param issuer 用户签名
	 * @return 返回TaskGroup实例，或者空指针
	 */
	public TaskGroup findGroup(Siger issuer) {
		TaskPart part = new TaskPart(getFamily(), issuer);
		return findGroup(part);
	}
	
	/**
	 * 判断有这个用户
	 * @param issuer
	 * @return
	 */
	public boolean hasGroup(Siger issuer) {
		return findGroup(issuer)!=null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TaskPool#clear(boolean)
	 */
	@Override
	protected void clear() {
		// 注意！不要加锁，因为调用方法已经加锁！
		super.clear();
		groups.clear();
	}

	/**
	 * 通知外部接口，本地组件已经更新
	 */
	private void refreshTasks() {
		// 通知宿主站点更新分布组件和重新注册
		TaskListener listener = getTaskListener();
		if (listener != null) {
			listener.refreshTask(getFamily());
		}
	}

	/**
	 * 内部调用，重新加载新应用软件包
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean reload() {
		// 扫描磁盘上的分布任务组件
		List<TaskGroup> array = detect();
		int size = array.size();

		Logger.debug(this, "reload", "task group count: %d", size);

		int count = 0;

		// 锁定
		super.lockSingle();
		try {
			// 空记录，删除全部，忽略!
			if (size == 0) {
				clear(); // 清除上级和本地的内存记录
				refreshTasks(); // 通知更新
				return true;
			}

			// 逐个判断，加载云端应用
			for (int index = 0; index < size; index++) {
				TaskGroup source = array.get(index);
				TaskPart part = source.getPart();
				TaskGroup dest = groups.get(part);
				// 两个条件加载：1. 没有旧的对象；2. 两个对象比较不一致！
				if (dest == null) {
					boolean success = loadTask(source);
					if (success) {
						groups.put(part, source);
						removeRefreshLibrary(part); // 删除库
						count++;
					}
				}
				// 不一致时，再次加载
				else if (!source.match(dest)) {
					boolean success = loadTask(source);
					if (success) {
						groups.put(part, source);
						removeRefreshLibrary(part); //删除库
						count++;
					} else {
						groups.remove(part); // 不成功，删除这个它
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 统计新增加成员
		boolean success = (count > 0);
		Logger.debug(this, "reload", success, "load new application:%d, all:%d", count, groups.size());

		// 通知线程更新
		refreshTasks();

		return success;
	}

	//	/**
	//	 * 加载全部本地的分布任务组件
	//	 * 
	//	 * @return 成功返回“真”，否则“假”。
	//	 */
	//	public boolean load() {
	//		// 扫描磁盘上的分布任务组件
	//		List<TaskGroup> array = detect();
	//		// 锁定
	//		super.lockSingle();
	//		try {
	//			// 清除上级和本地内存的全部记录
	//			clear();
	//
	//			// 逐个加载，成功形成映射
	//			for (TaskGroup group : array) {
	//				boolean success = loadTask(group);
	//				if (success) {
	//					TaskPart part = group.getPart();
	//					groups.put(part, group);
	//				}
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//
	//		// 通知线程更新
	//		refreshTasks();
	//		return true;
	//	}

	/**
	 * 加载全部本地的分布任务组件
	 * 
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean load() {
		// 锁定
		super.lockSingle();
		try {
			// 清除上级和本地内存的全部记录
			clear();

			// 扫描磁盘上的分布任务组件
			List<TaskGroup> array = detect();

			// 逐个加载，成功形成映射
			for (TaskGroup group : array) {
				boolean success = loadTask(group);
				if (success) {
					TaskPart part = group.getPart();
					groups.put(part, group);
					// 删除库记录
					removeRefreshLibrary(part);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 通知线程更新
		refreshTasks();
		return true;
	}

	//	/**
	//	 * 内部调用，重新加载新应用软件包
	//	 * 
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean reload() {
	////		int count = 0;
	////
	////		// 锁定
	////		super.lockSingle();
	////		try {
	//////			// 空记录，删除全部，忽略!
	//////			if (size == 0) {
	//////				clear(); // 清除上级和本地的内存记录
	//////				refreshTasks(); // 通知更新
	//////				return true;
	//////			}
	////			
	////			// 1. 先清除旧的
	////			clear(true);
	////
	////			// 2. 扫描磁盘上的分布任务组件
	////			List<TaskGroup> array = detect();
	////			int size = array.size();
	////
	////			// 空记录，删除全部，忽略!
	////			if (size == 0) {
	////				refreshTasks(); // 通知更新
	////				return true;
	////			}
	////
	////			// 3. 逐个判断，加载云端应用
	////			for (int index = 0; index < size; index++) {
	////				TaskGroup source = array.get(index);
	////				TaskPart part = source.getPart();
	////				TaskGroup dest = groups.get(part);
	////				// 两个条件加载：1. 没有旧的对象；2. 两个对象比较不一致！
	////				if (dest == null) {
	////					boolean success = loadTask(source);
	////					if (success) {
	////						groups.put(part, source);
	////						count++;
	////					}
	////				} else if (!source.match(dest)) {
	////					boolean success = loadTask(source);
	////					if (success) {
	////						groups.put(part, source);
	////						count++;
	////					} else {
	////						groups.remove(part); // 不成功，删除这个它
	////					}
	////				}
	////			}
	////		} catch (Throwable e) {
	////			Logger.fatal(e);
	////		} finally {
	////			super.unlockSingle();
	////		}
	//		
	//		int count = 0;
	//		
	//		// 锁定
	//		super.lockSingle();
	//		try {
	//			// 1. 清除旧的
	//			clear(true);
	//
	//			// 2. 扫描磁盘上的分布任务组件
	//			List<TaskGroup> array = detect();
	//
	//			// 3. 逐个加载，成功形成映射
	//			for (TaskGroup group : array) {
	//				boolean success = loadTask(group);
	//				if (success) {
	//					TaskPart part = group.getPart();
	//					groups.put(part, group);
	//					count++;
	//				}
	//				// 删除库记录
	//				removeLibrary(group.getPart());
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//
	//		// 统计新增加成员
	//		boolean success = (count > 0);
	//		Logger.debug(this, "reload", success, "load new application:%d, all groups:%d", count, groups.size());
	//
	//		// 通知线程更新
	//		refreshTasks();
	//
	//		return success;
	//	}

	//	/**
	//	 * 加载新的应用软件
	//	 */
	//	private void reload() {
	//		int count = 0;
	//		do {
	//			TaskPart part = popup();
	//			if (part == null) {
	//				break;
	//			}
	//			// 收到通知，重新加载
	//			boolean success = reload(part);
	//			if (success) {
	//				count++;
	//			}
	//		} while (true);
	//
	//		// 成功，通知线程更新
	//		if (count > 0) {
	//			refreshTasks();
	//		}
	//	}

	//	/**
	//	 * 重新加载一个用户的应用组件群
	//	 * @param part 工作部件
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean reload(TaskPart part) {
	//		boolean success = false;
	//
	////		// 注意，必须是调用上级的drop方法
	////		super.drop(part.getIssuer());
	//		
	//		// 锁定
	//		super.lockSingle();
	//		try {
	//			// 删除本地
	//			groups.remove(part);
	//
	//			// 找到目录和判断存在
	//			File root = buildSubRoot(part.getIssuer());
	//			boolean exists = (root.exists() && root.isDirectory());
	//			// 扫描
	//			if (exists) {
	//				// 扫描磁盘
	//				TaskGroup group = super.scanGroup(root);
	//				
	//				exists = (group.size() > 0 && group.getTag() != null);
	//				
	//				Logger.note(this, "reload", exists, "加载 %s", root);
	//				
	//				if (exists) {
	//					success = loadTask(group);
	//				}
	//				if (success) {
	//					groups.put(group.getPart(), group);
	//					// 删除库记录
	//					removeLibrary(group.getPart());
	//				}
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//
	//		Logger.note(this, "reload", success, "load new application %s", part);
	//
	//		return success;
	//	}

	/**
	 * 根据阶段标识判断分布任务组件已经存在且一致。<BR>
	 * 必须满足两个条件：1.组件存在，2.MD5签名一致
	 * 
	 * @param tag
	 *            分布任务组件标记
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean match(TaskTag tag) {
		boolean success = false;
		TaskPart part = tag.getPart();
		// 锁定!
		super.lockMulti();
		try {
			// 1. 判断组件在内存中存在
			TaskGroup document = groups.get(part);
			// 2. 判断参数一致
			if (document != null) {
				success = (Laxkit.compareTo(document.getTag(), tag) == 0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "match", success, "%s", tag);

		return success;
	}

	/**
	 * 粉碎性删除某个账号的全部文件，包括内存和磁盘上的全部
	 * 
	 * @param part
	 * @return
	 */
	private boolean shredAll(TaskPart part) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			// 1. 从内存中删除它
			TaskGroup document = groups.remove(part);
			File root = (document != null ? document.getRoot() : null);
			success = (root != null);
			// 2. 删除磁盘的全部文件
			if (success) {
				File[] list = root.listFiles();
				int size = (list == null ? 0 : list.length);
				for (int i = 0; i < size; i++) {
					File waredir = list[i];
					shredWareDirectory(waredir); // 删除软件包目录
				}
				// 最后删除根目录
				success = shredDirectory(root);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "shareAll", success, "shred %s", part);

		return success;
	}

//	/**
//	 * 粉碎性删除某个账号的全部文件，包括内存和磁盘上的全部
//	 * 
//	 * @param section 组件分区
//	 * @return 返回删除的文件数目（不包括目录）
//	 */
//	private int shred(TaskSection section) {
//		int count = 0;
//		TaskPart part = section.getTaskPart();
//		// 锁定
//		super.lockSingle();
//		try {
//			// 1. 从内存中找到对象
//			TaskGroup document = groups.get(part);
//			// 2. 逐个判断和删除
//			if (document != null) {
//				ArrayList<TaskElement> array = new ArrayList<TaskElement>();
//				for (TaskElement element : document.list()) {
//					boolean success = (Laxkit.compareTo(element.getSection(), section) == 0);
//					if (success) {
//						// 删除磁盘的全部文件
//						File root = element.getRoot();
//						success = shredDirectory(root);
//						if (success) {
//							array.add(element);
//							// 统计删除的成员数目
//							count += element.size();
//						} else {
//							// 不成功，记录这个目录
//						}
//					}
//				}
//				// 从内存中清除
//				for (TaskElement e : array) {
//					document.remove(e);
//				}
//				// 如果已经没有，删除整个文档
//				if (document.isEmpty()) {
//					shredDirectory(document.getRoot());
//					groups.remove(part);
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		Logger.note(this, "shred", count > 0, "shred %s, count %d", section, count);
//
//		return count;
//	}

	/**
	 * 粉碎性删除某个账号的一个软件，包括内存和磁盘上的
	 * 
	 * @param section 组件分区
	 * @return 返回删除的文件数目（不包括目录）
	 */
	private boolean shredWare(TaskSection section) {
		int failures = 0;
		int successfully =0;
		
		TaskPart part = section.getTaskPart();
		// 锁定
		super.lockSingle();
		try {
			// 1. 从内存中找到对象
			TaskGroup document = groups.get(part);
			// 2. 逐个判断和删除
			if (document == null) {
				return false;
			}
			
				ArrayList<TaskElement> array = new ArrayList<TaskElement>();
				for (TaskElement element : document.list()) {
					boolean success = (Laxkit.compareTo(element.getSection(),section) == 0);
					if (success) {
						array.add(element);
					}

					//					if (success) {
					//						// 删除磁盘的全部文件
					//						File root = element.getRoot();
					//						success = shredDirectory(root);
					//						if (success) {
					//							array.add(element);
					//							// 统计删除的成员数目
					//							failures += element.size();
					//						} else {
					//							// 不成功，记录这个目录
					//						}
					//					}
				}
				
			// 从内存中清除
			for (TaskElement e : array) {
				// 删除内存记录
				document.remove(e);
				// 粉碎目录
				boolean b = shredWareDirectory(e.getRoot());
				if (b) {
					successfully++;
				} else {
					failures++;
				}
			}
				// 如果已经没有，删除整个文档
				if (document.isEmpty()) {
					shredDirectory(document.getRoot());
					groups.remove(part);
				}
			
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		boolean success = (failures == 0 && successfully > 0);

		Logger.note(this, "shredWare", success, "shred %s, failures %d, successfully %d",
				section, failures, successfully);

		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.task.TaskPool#drop(com.laxcus.util.Siger)
	 */
	@Override
	public boolean drop(Siger issuer) {
		// 拒绝空指针，这是系统级组件，不允许删除
		if (issuer == null) {
			return false;
		}

		// 分布任务部件
		TaskPart part = new TaskPart(issuer, getFamily());

		// 调用上极方法，删除内存中的分布任务组件的类加载器和项目
		super.drop(issuer);
		// 删除磁盘文件
		boolean success = shredAll(part);

		// 更新
		refreshTasks();

		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.task.TaskPool#drop(com.laxcus.util.Siger,
	 * com.laxcus.util.naming.Naming)
	 */
	@Override
	public boolean drop(Siger issuer, Naming ware) {
		// 分布任务部件
		TaskSection section = new TaskSection(issuer, getFamily(), ware);

		// 调用上极方法，删除内存中的分布任务组件的类加载器和项目
		super.drop(issuer, ware);
		// 删除磁盘文件
		boolean success = shredWare(section);
//		// 刷新记录
//		if (memory > 0 || disk > 0) {
//			// doRefresh();
//			refreshTasks();
//		}
		
		// 更新记录
		refreshTasks();

		return success;
	}

	/**
	 * 生成一个组件单元，组件单元包括它的全部内容
	 * 
	 * @param owner 软件包实际拥有人（不是开发者）
	 * @param item 包成员
	 * @return 成功返回真，否则假
	 */
	private boolean createTaskElement(Siger owner, CloudPackageItem item) {
		// 读组件包内容
		TaskComponentReader reader = new TaskComponentReader(item.getContent());
		TaskSection section = reader.readTaskSection();
		// 空指针，失败！
		if (section == null) {
			Logger.error(this, "createTaskElement", "cannot be resolve %s", item.getName());
			return false;
		}
		// 修改拥有人
		section.setIssuer(owner);

		// 生成子级目录
		File root = createSubRoot(section.getIssuer(), section.getWare());
		if (root == null) {
			Logger.error(this, "createTaskElement", "cannot be create directory!");
			return false;
		}
		
		// 删除可能存在的垃圾目录
		removeSoftwareDirectory(root);
		
		// 组件集名称，是用户SHA256签名和阶段名称的组合
		String name = buildTaskBootItem(section.getTaskPart());
		File file = new File(root, name);
		// 把引导文件写入磁盘
		boolean success = writeContent(file, item.getContent());

		Logger.note(this, "createTaskElement", success, "write %s # %s", section, file);

		return success;
	}

	/**
	 * 判断是空集合。<br>
	 * 1. 检测有错误，是空<br>
	 * 2. 只有“GROUP-INF/group.xml”，没有其它内容，是空<br>
	 * <br>
	 * 
	 * @param content
	 *            内容
	 * @return 返回真或者假
	 */
	private boolean isEmptyTaskComponent(byte[] content) {
		int count = 0;
		try {
			TaskComponentGroupReader reader = new TaskComponentGroupReader(
					content);
			count = reader.getAvailableItems();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 小于等于0是空集合
		return count <= 0;
	}

	/**
	 * 在本地保存和发布分布任务组件。
	 * 
	 * @param component 分布任务组件文件包，以“.dtg”为文件后缀。
	 * @return 发送成功返回真，否则假
	 */
	public boolean deploy(TaskComponent component) {
		TaskPart part = component.getPart();
		Logger.debug(this, "deploy", "component is %s", part);

		// 阶段类型必须匹配，否则不接受
		boolean match = match(part.getFamily());
		if (!match) {
			Logger.error(this, "deploy", "family error! %d - %d", part.getFamily(), getFamily());
			return false;
		}

		// 判断内容签名一致
		byte[] content = component.getContent();
		MD5Hash sign = Laxkit.doMD5Hash(content);
		match = (Laxkit.compareTo(component.getSign(), sign) == 0);
		if (!match) {
			Logger.error(this, "deploy", "md5 sign error! %s - %s", component.getSign(), sign);
			return false;
		}

		// 判断是集合
		if (isEmptyTaskComponent(content)) {
			Logger.warning(this, "deploy", "%s is empty!", part);
			return true;
		}

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			// 根据用户签名，建立子目录且成功
			File root = createSubRoot(part.getIssuer());
			if (root == null) {
				Logger.error(this, "deploy", "cannot be create sub-directory! %s", part.getIssuer());
				return false;
			}

			// 组件集名称，是用户SHA256签名和阶段名称的组合
			String name = buildTaskBootGroup(part);
			File file = new File(root, name);

			// 判断一致则忽略
			if (isMatching(file, content)) {
				Logger.info(this, "deploy", "dtg file %s is exists!", file);
				return true;
			}

			// 分布组件集写入磁盘。也许有旧文件，覆盖它
			success = writeContent(file, content);
			Logger.note(this, "deploy", success, "write %s", file);

			// 解析和保存
			if (success) {
				// 读参数
				TaskPart taskPart = null;
				TaskComponentGroupReader reader = new TaskComponentGroupReader(content);
				List<CloudPackageItem> items = null;
				try {
					taskPart = reader.readTaskPart();
					items = reader.readTaskComponents();
				} catch (Throwable e) {
					Logger.fatal(e);
				}

				// 判断，空指针是错误；
				if (items == null || taskPart == null) {
					Logger.error(this, "deploy", "param missing!");
					return false;
				}
				// 判断是允许的部件，否则拒绝执行
				if (!allow(taskPart)) {
					Logger.error(this, "deploy", "refuse %s!", taskPart);
					return false;
				}
				// 判断一致
				if (Laxkit.compareTo(part, taskPart) != 0) {
					Logger.error(this, "deploy", "%s != %s", part, taskPart);
					return false;
				}
				// 允许空集合，提出警告！
				if (items.isEmpty()) {
					Logger.warning(this, "deploy", "%s is empty set!", part);
				}

				// 成员参数
				for (CloudPackageItem item : items) {
					// 如果是“GROUP-INF/group.xml”，则忽略
					if (item.getName().matches(TF.GROUP_INF)) {
						continue;
					}
					// 解析生成实例
					boolean b = createTaskElement(taskPart.getIssuer(), item);
					// 不成功退出
					if (!b) return false;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 成功，定时唤醒线程，要求重新加载
		if (success) {
			doRefresh();
		}

		Logger.debug(this, "deploy", success, "deploy dtg %s", component.getTag());

		return success;
	}

	/**
	 * 删除应用附件
	 * 
	 * @param element
	 *            软件包单元
	 * @param filename
	 *            文件名称，不包括路径
	 * @return 被删除的数目，包括磁盘和内存！
	 */
	private int deleteJAR(TaskElement element, String filename) {
		int count = 0;
		// 删除磁盘上的记录
		File root = element.getRoot();
		File[] files = root.listFiles();
		for (int i = 0; files != null && i < files.length; i++) {
			File file = files[i];
			String sub = file.getName(); // 去掉路径，只取文件名
			// 名称一致，忽略大小写
			if (filename.equalsIgnoreCase(sub)) {
				boolean success = file.delete();
				Logger.note(this, "deleteJAR", success, "delete jar: %s", filename);
				if (success) count++;
			}
		}

		// 删除内存记录
		ArrayList<FileKey> keys = new ArrayList<FileKey>(element.getJARs());
		for (FileKey key : keys) {
			File file = new File(key.getPath());
			String sub = file.getName(); // 去掉路径，只取文件名
			// 只要名称相同就删除!
			if (filename.equalsIgnoreCase(sub)) {
				boolean success = element.removeJAR(key);
				if (success) count++;
			}
		}

		Logger.info(this, "deleteJAR", "delete %s count %d", filename, count);

		return count;
	}

	/**
	 * 在本地保存和发布分布任务组件的应用附件。成功发布的前提是账号和分布任务组件已经存在！<br>
	 * 应用附件是一个“.jar”后缀的文件。<br>
	 * <br>
	 * 
	 * 处理流程：<br>
	 * 1. 判断账号和分布任务组件已经存在。<br>
	 * 2. 删除本地旧记录（包括内存和磁盘文件）<br>
	 * 3. 新的JAR文件保存到磁盘。<br>
	 * 4. 生成新的记录，发布它。<br>
	 * 
	 * @param component 应用附件
	 * @return 部署成功返回真，否则假
	 */
	public boolean deploy(TaskAssistComponent component) {
		// 判断内容签名一致
		byte[] content = component.getContent();
		MD5Hash sign = Laxkit.doMD5Hash(content);
		boolean match = (Laxkit.compareTo(component.getSign(), sign) == 0);
		if (!match) {
			Logger.error(this, "deploy", "assist sign error! %s - %s", component.getSign(), sign);
			return false;
		}

		// 判断可以允许执行
		TaskPart part = component.getPart();
		if (!allow(part)) {
			Logger.error(this, "deploy", "refuse %s", part);
			return false;
		}

		// 找到应用单元
		TaskSection section = component.getSection();
		TaskElement element = findTaskElement(section);
		if (element == null) {
			Logger.error(this, "deploy", "not found TaskElement! %s", section);
			return false;
		}

		boolean success = false;
		// 锁定！
		super.lockSingle();
		try {
			// 建立目录
			File path = createSubRoot(section.getIssuer(), component.getWare());
			if (path == null) {
				Logger.error(this, "deploy", "cannot be create sub-directory! %s", section.getIssuer());
				return false;
			}
			
			// 删除可能存在的垃圾目录
			removeSoftwareDirectory(path);
			
			// 根据传入的文件名，生成一个本地文件！
			File file = new File(path, component.getName());

			// 判断文件一致，不处理退出
			if (isMatching(file, content)) {
				Logger.info(this, "deploy", "jar file: %s is exists!", file);
				return true;
			}

			// 删除JAR文件
			deleteJAR(element, component.getName());
			// JAR文件保存到本地磁盘
			success = writeContent(file, content);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 成功，通知定时更新！
		if (success) {
			doRefresh();
		}

		Logger.debug(this, "deploy", success, "deploy jar %s", section);

		return success;
	}

	/**
	 * 先于引导文件保存到磁盘，不做加载到内存处理！
	 * 
	 * @param component
	 *            组件
	 * @return 成功返回真，否则假
	 */
	public boolean direct(TaskAssistComponent component) {
		// 判断内容签名一致
		byte[] content = component.getContent();
		MD5Hash sign = Laxkit.doMD5Hash(content);
		boolean match = (Laxkit.compareTo(component.getSign(), sign) == 0);
		if (!match) {
			Logger.error(this, "direct", "assist sign error! %s - %s",
					component.getSign(), sign);
			return false;
		}

		boolean success = false;
		TaskPart part = component.getPart();
		// 锁定！
		super.lockSingle();
		try {
			// 建立目录
			File path = createSubRoot(part.getIssuer(), component.getWare());
			if (path == null) {
				Logger.error(this, "direct",
						"cannot be create sub-directory! %s", part.getIssuer());
				return false;
			}
			
			// 删除可能存在的垃圾目录
			removeSoftwareDirectory(path);

			// 根据传入的文件名，生成一个本地文件！
			File file = new File(path, component.getName());
			// JAR文件保存到本地磁盘
			success = writeContent(file, content);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "direct", success, "direct jar %s", part);

		return success;
	}

	//	/**
	//	 * 删除动态链接库
	//	 * 
	//	 * @param element
	//	 *            任务组件单元
	//	 * @param filename
	//	 *            文件名称，不包括路径
	//	 * @return 被删除的数目，包括磁盘和内存！
	//	 */
	//	private int deleteLibrary(TaskElement element, String filename) {
	//		int count = 0;
	//		// 删除磁盘上的记录
	//		File root = element.getRoot();
	//		File[] files = root.listFiles();
	//		for (int i = 0; files != null && i < files.length; i++) {
	//			File file = files[i];
	//			String subname = file.getName(); // 去掉路径，只取文件名
	//			// 名称一致，忽略大小写
	//			if (filename.equalsIgnoreCase(subname)) {
	//				boolean success = file.delete();
	//				Logger.note(this, "deleteLibrary", success, "delete so: %s", filename);
	//				if (success) count++;
	//			}
	//		}
	//
	//		// 删除内存记录
	//		ArrayList<FileKey> keys = new ArrayList<FileKey>(element.getLibraries());
	//		for (FileKey key : keys) {
	//			File file = new File(key.getPath());
	//			String subname = file.getName(); // 去掉路径，只取文件名
	//			// 只要名称相同就删除
	//			if (filename.equalsIgnoreCase(subname)) {
	//				boolean success = element.removeLibrary(key);
	//				if (success) count++;
	//			}
	//		}
	//
	//		Logger.info(this, "deleteLibrary", "delete %s count %d", filename, count);
	//
	//		return count;
	//	}

	//	/**
	//	 * 在本地保存和发布分布任务组件的动态链接库附件。成功发布的前提是账号和分布任务组件已经存在！<br>
	//	 * 动态链接库附件根据所属操作系统，是“*.so”或者“*.dll”后缀的文件。<br>
	//	 * <br>
	//	 * 
	//	 * 处理流程：<br>
	//	 * 1. 判断账号和分布任务组件已经存在。<br>
	//	 * 2. 删除本地旧记录（包括内存和磁盘文件）<br>
	//	 * 3. 新的动态链接库文件保存到磁盘。<br>
	//	 * 4. 加载新的动态链接库。<br>
	//	 * 
	//	 * @param component 动态链接库附件
	//	 * @return 部署成功返回真，否则假
	//	 */
	//	public boolean deploy(TaskLibraryComponent component) {
	//		// 判断内容签名一致
	//		byte[] content = component.getContent();
	//		MD5Hash sign = Laxkit.doMD5Hash(content);
	//		boolean match = (Laxkit.compareTo(component.getSign(), sign) == 0);
	//		if (!match) {
	//			Logger.error(this, "deploy", "library sign error! %s - %s", component.getSign(), sign);
	//			return false;
	//		}
	//		// 判断可以允许执行
	//		if (!allow(component.getPart())) {
	//			Logger.error(this, "deploy", "refuse %s", component.getPart());
	//			return false;
	//		}
	//
	//		// 查找...
	//		TaskSection section = component.getSection();
	//		TaskElement element = findTaskElement(section);
	//		if (element == null) {
	//			Logger.error(this, "deploy", "not found TaskElement! %s", section);
	//			return false;
	//		}
	//
	//		boolean success = false;
	//		// 锁定！
	//		super.lockSingle();
	//		try {
	//			// 建立目录
	//			File path = createSubRoot(section.getIssuer(), component.getWare());
	//			if (path == null) {
	//				Logger.error(this, "deploy", "cannot be create sub-directory! %s", section);
	//				return false;
	//			}
	//
	//			// 根据传入的文件名，生成一个本地文件！
	//			File file = new File(path, component.getName());
	//			// 判断文件存在且匹配
	//			if (isMatching(file, content)) {
	//				Logger.info(this, "deploy", "library file: %s is exists!", file);
	//				return true;
	//			}
	//
	//			// 删除动态库文件
	//			deleteLibrary(element, component.getName());
	//			// 动态链接库文件保存到本地磁盘
	//			success = writeContent(file, content);
	////			// 加载动态链接库
	////			if (success) {
	////				success = JNILoader.loadSingleLibrary(file);
	////			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//
	//		Logger.debug(this, "deploy", success, "deploy link library %s", section);
	//
	//		return success;
	//	}

	//	/**
	//	 * 先于引导文件部署动态链接库！ 做保存到磁盘和加载处理！
	//	 * 
	//	 * @param component
	//	 *            库组件
	//	 * @return 成功返回真，否则假
	//	 */
	//	public boolean direct(TaskLibraryComponent component) {
	//		// 判断内容签名一致
	//		byte[] content = component.getContent();
	//		MD5Hash sign = Laxkit.doMD5Hash(content);
	//		boolean match = (Laxkit.compareTo(component.getSign(), sign) == 0);
	//		if (!match) {
	//			Logger.error(this, "direct", "library sign error! %s - %s",
	//					component.getSign(), sign);
	//			return false;
	//		}
	//
	//		boolean success = false;
	//		TaskPart part = component.getPart();
	//		// 锁定！
	//		super.lockSingle();
	//		try {
	//			// 建立目录
	//			File path = createSubRoot(part.getIssuer(), component.getWare());
	//			if (path == null) {
	//				Logger.error(this, "direct",
	//						"cannot be create sub-directory! %s", part.getIssuer());
	//				return false;
	//			}
	//
	//			// 根据传入的文件名，生成一个本地文件！
	//			File file = new File(path, component.getName());
	//			// 动态链接库文件保存到本地磁盘
	//			success = writeContent(file, content);
	//			//			// 加载动态链接库
	//			//			if (success) {
	//			//				success = JNILoader.loadSingleLibrary(file);
	//			//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//
	//		Logger.debug(this, "direct", success, "deploy link library %s", part);
	//
	//		return success;
	//	}


	/**
	 * 在本地保存和发布分布任务组件的动态链接库附件。成功发布的前提是账号和分布任务组件已经存在！<br>
	 * 动态链接库附件根据所属操作系统，是“*.so”或者“*.dll”后缀的文件。<br>
	 * <br>
	 * 
	 * 处理流程：<br>
	 * 1. 判断账号和分布任务组件已经存在。<br>
	 * 2. 删除本地旧记录（包括内存和磁盘文件）<br>
	 * 3. 新的动态链接库文件保存到磁盘。<br>
	 * 4. 加载新的动态链接库。<br>
	 * 
	 * @param component 动态链接库附件
	 * @return 部署成功返回真，否则假
	 */
	public boolean deploy(TaskLibraryComponent component) {
		// 判断内容签名一致
		byte[] content = component.getContent();
		MD5Hash sign = Laxkit.doMD5Hash(content);
		boolean match = (Laxkit.compareTo(component.getSign(), sign) == 0);
		if (!match) {
			Logger.error(this, "deploy", "library sign error! %s - %s", component.getSign(), sign);
			return false;
		}
		// 判断可以允许执行
		if (!allow(component.getPart())) {
			Logger.error(this, "deploy", "refuse %s", component.getPart());
			return false;
		}

		// 查找...
		TaskSection section = component.getSection();
		TaskElement element = findTaskElement(section);
		if (element == null) {
			Logger.error(this, "deploy", "not found TaskElement! %s", section);
			return false;
		}

		boolean success = false;
		// 锁定！
		super.lockSingle();
		try {
			// 判断有同质文件存在
			if (hasRefreshLibrary(component.getPart(), content)) {
				Logger.warning(this, "deploy", "library file exists!");
				return true;
			}

			// 建立目录
			File path = createSubRoot(section.getIssuer(), component.getWare());
			if (path == null) {
				Logger.error(this, "deploy", "cannot be create sub-directory! %s", section);
				return false;
			}
			
			// 删除可能存在的垃圾目录
			removeSoftwareDirectory(path);

			// 构造一个库文件名称
			File file = buildLibraryFile(path, component.getName());
			if (file == null) {
				Logger.error(this, "deploy", "cannot be create library directory! %s", section);
				return false;
			}

			// 动态链接库文件保存到本地磁盘
			success = writeContent(file, content);
			// 若成功，保存
			if (success) {
				addRefreshLibrary(component.getPart(), file);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "deploy", success, "deploy link library %s", section);

		return success;
	}

	/**
	 * 先于引导文件部署动态链接库！ 做保存到磁盘和加载处理！
	 * 
	 * @param component 库组件
	 * @return 成功返回真，否则假
	 */
	public boolean direct(TaskLibraryComponent component) {
		// 判断内容签名一致
		byte[] content = component.getContent();
		MD5Hash sign = Laxkit.doMD5Hash(content);
		boolean match = (Laxkit.compareTo(component.getSign(), sign) == 0);
		if (!match) {
			Logger.error(this, "direct", "library sign error! %s - %s",
					component.getSign(), sign);
			return false;
		}

		boolean success = false;
		TaskPart part = component.getPart();
		// 锁定！
		super.lockSingle();
		try {
			// 判断有同质文件存在
			if (hasRefreshLibrary(part, content)) {
				Logger.warning(this, "direct", "library file exists!");
				return true;
			}

			// 建立目录
			File path = createSubRoot(part.getIssuer(), component.getWare());
			if (path == null) {
				Logger.error(this, "direct", "cannot be create sub-directory! %s", part.getIssuer());
				return false;
			}
			
			// 删除可能存在的垃圾目录
			removeSoftwareDirectory(path);

			// 构造一个库文件名称
			File file = buildLibraryFile(path, component.getName());
			if (file == null) {
				Logger.error(this, "direct", "cannot be create library directory! %s", part.getIssuer());
				return false;
			}

			// 动态链接库文件保存到本地磁盘
			success = writeContent(file, content);
			// 成功，保存它
			if (success) {
				addRefreshLibrary(part, file);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "direct", success, "deploy link library %s", part);

		return success;
	}

}