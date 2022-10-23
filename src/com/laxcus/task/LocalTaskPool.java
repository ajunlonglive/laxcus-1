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

import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 管理保存在本地磁盘的分布任务组件池。<br><br>
 * 
 * 分布任务组件文件被放在计算机磁盘上，文件以“.dtc”后缀命名。
 * 目前只用于FRONT站点。
 * 
 * @author scott.liang 
 * @version 1.1 11/12/2012
 * @since laxcus 1.0
 */
public class LocalTaskPool extends TaskToolPool {

	/** 用户签名接口 **/
	private UserListener listener;

	/** 阶段签名 -> 组件档案，包括DTC文件、JAR辅助文件、动态链接库 **/
	private TreeMap<TaskPart, TaskGroup> groups = new TreeMap<TaskPart, TaskGroup>();
	
	/**
	 * 构造分布组件管理池，指定任务命名类型
	 * @param family CONDUCT/ESTABLISH阶段类型
	 */
	protected LocalTaskPool(int family) {
		super(family);
		// 60秒检查一次磁盘
		setSleepTime(60);
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
	 * 返回用户签名。<br>
	 * 如果离线或者管理员状态时，返回空指针，否则是SHA256签名。<br><br>
	 * 
	 * @return Siger实例或者空指针
	 */
	public Siger getIssuer() {
		return listener.getIssuer() ;
	}

	/**
	 * 判断是系统管理员
	 * @return 返回真或者假
	 */
	public boolean isAdministrator() {
		return listener.isAdministrator();
	}

	/**
	 * 判断是注册用户
	 * @return 返回真或者假
	 */
	public boolean isUser() {
		return listener.isUser();
	}

	/**
	 * 判断是离线状态
	 * @return 返回真或者假
	 */
	public boolean isOffline() {
		return listener.isOffline();
	}

	/**
	 * 根据签名，判断它们匹配
	 * @param siger 签名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger) {
		Siger issuer = getIssuer();
		return (Laxkit.compareTo(issuer, siger) == 0);
	}

	/*
	 * 线程启动前的初始化
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 如果目录没有定义，默认为用户目录下的"deploy"目录
		if (getRoot() == null) {
			// 用户目录
			String bin = System.getProperty("user.dir");
			// 假定一个发布目录(实际应用中，全部发布任务都定义在"deploy"目录)
			boolean success = super.setRoot(bin, "deploy");
			if (!success) {
				Logger.error(this, "init", "cannot be create task directory");
				return false;
			}
		}

		Logger.info(this, "init", "'%s' directory is '%s'",
				PhaseTag.translate(getFamily()), getRoot());

		// 加载任务命名
		reload();

		return true;
	}
	
//	/**
//	 * 判断处于登录状态!
//	 * @return 返回真或者假
//	 */
//	private boolean isLogined() {
//		return getLauncher().isLogined();
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		while (!isInterrupted()) {
			// 在登录状态下，定时检查JAR包更新
			if (isLogined()) {
				reload();
			}
			// 线程进行等待状态
			sleep();
		}

		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
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
	 * 唤醒线程，检测和更新分布任务组件
	 */
	public void update() {
		wakeup();
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
	 * @return 存在返回真，或者假
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
	
//	/**
//	 * 粉碎性删除某个账号的全部文件，包括内存记录和磁盘上的全部文件
//	 * @param section 
//	 * @return 返回删除的磁盘文件数目
//	 */
//	private int shred(TaskSection section) {
//		TaskPart part = section.getTaskPart();
//		int count = 0;
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
//						// 删除磁盘目录和下面的全部文件
//						File root = element.getRoot();
//						success = shredDirectory(root);
//						if (success) {
//							array.add(element);
//							// 统计文件
//							count += element.size();
//						}
//					}
//				}
//				// 从内存中清除
//				for (TaskElement e : array) {
//					document.remove(e);
//				}
//				// 如果已经没有，删除整个目录和文档
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
	 * 粉碎性删除某个账号的全部文件，包括内存记录和磁盘上的全部文件
	 * @param section 
	 * @return 返回删除的磁盘文件数目
	 */
	private boolean shredWare(TaskSection section) {
		TaskPart part = section.getTaskPart();
		int failures = 0;
		int successfully = 0;
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
				boolean success = (Laxkit.compareTo(element.getSection(), section) == 0);
				if (success) {
					array.add(element);
				}

				//					if (success) {
				//						// 删除磁盘目录和下面的全部文件
				//						File root = element.getRoot();
				//						success = shredDirectory(root);
				//						if (success) {
				//							array.add(element);
				//							// 统计文件
				//							failures += element.size();
				//						}
				//					}
			}
			// 从内存中清除
			for (TaskElement e : array) {
				// 删除内存记录
				document.remove(e);
				// 删除对应目录的数据
				boolean b = shredWareDirectory(e.getRoot());
				if (b) {
					successfully++;
				} else {
					failures++;
				}
			}
			// 如果已经没有，删除整个目录和文档
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
	 * @see com.laxcus.task.TaskPool#drop(com.laxcus.util.Siger, com.laxcus.util.naming.Naming)
	 */
	@Override
	public boolean drop(Siger issuer, Naming ware) {
		// 组件分区
		TaskSection section = new TaskSection(issuer, getFamily(), ware);

		// 调用上极方法，删除内存中的分布任务组件的类加载器和项目
		super.drop(issuer, ware);
		// 删除磁盘文件
		boolean success = shredWare(section);
		
		//		// 成功，通知更新
		//		if (memory > 0 || disk > 0) {
		//			update();
		//		}

		// 更新
		update();

		return success;
	}
	
	/**
	 * 检查和更新目录下的分布组件包。
	 * @return 如果文件发生变化和更新成功，返回“真”，否则“假”。
	 */
	private boolean reload() {
		// 清除旧的垃圾动态链接库，注意！这个方法本身是在锁定环境中，所以不能在锁定环境中执行。
		releaseRubbishLibraries(); 
		
		// 扫描磁盘
		List<TaskGroup> array = detect();
		
		int size = array.size();
		int count = 0;
		
		// 锁定
		super.lockSingle();
		try {
			// 空记录，删除全部，忽略!
			if (size == 0) {
				clear(); // 清除上级的内存记录，包括动态键接库
				groups.clear(); // 清除组件！
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
						removeRefreshLibrary(part); // 删除记录
						count++;
					}
				} 
				// 如果不一致，可以加载
				else if (!source.match(dest)) {
					boolean success = loadTask(source);
					if (success) {
						groups.put(part, source);
						removeRefreshLibrary(part);
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

		// 统计新成员
		boolean success = (count > 0);
		Logger.debug(this, "reload", success, "load new application:%d, all:%d", count, groups.size());

		return success;
	}
	
//	/**
//	 * 检查和更新目录下的分布组件包。
//	 * @return 如果文件发生变化和更新成功，返回“真”，否则“假”。
//	 */
//	private boolean reload() {
//		//		// 扫描磁盘
//		//		List<TaskGroup> array = detect();
//		//		
//		//		int size = array.size();
//
//		int count = 0;
//
//		// 锁定
//		super.lockSingle();
//		try {
//			//			// 空记录，删除全部，忽略!
//			//			if (size == 0) {
//			//				clear(); // 清除上级的内存记录
//			//				groups.clear(); // 清除组件！
//			//				return true;
//			//			}
//
//			// 1. 清除
//			clear();
//			groups.clear();
//			
//			// 2. 扫描磁盘
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
//			}
//			
////			int size = array.size();
////			if (size == 0) {
////				groups.clear(); // 清除组件！
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
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		// 统计新成员
//		boolean success = (count > 0);
//		Logger.debug(this, "reload", success, "load new application:%d, all:%d", count, groups.size());
//
//		return success;
//	}
	
//	/**
//	 * 检查和更新目录下的分布组件包。
//	 * @return 如果文件发生变化和更新成功，返回“真”，否则“假”。
//	 */
//	private boolean reload() {
//		// 扫描磁盘
//		List<TaskGroup> array = detect();
//		
//		int size = array.size();
//		int count = 0;
//		
//		// 锁定
//		super.lockSingle();
//		try {
//			// 空记录，删除全部，忽略!
//			if (size == 0) {
//				clear(); // 清除上级的内存记录
//				groups.clear(); // 清除组件！
//				return true;
//			}
//
//			// 逐个判断，加载云端应用
//			for (int index = 0; index < size; index++) {
//				TaskGroup source = array.get(index);
//				TaskPart part = source.getPart();
//				TaskGroup dest = groups.get(part);
//				// 两个条件加载：1. 没有旧的对象；2. 两个对象比较不一致！
//				if (dest == null) {
//					boolean success = loadTask(source);
//					if (success) {
//						groups.put(part, source);
//						count++;
//					}
//				} else if (!source.match(dest)) {
//					boolean success = loadTask(source);
//					if (success) {
//						groups.put(part, source);
//						count++;
//					} else {
//						groups.remove(part); // 不成功，删除这个它
//					}
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		// 统计新成员
//		boolean success = (count > 0);
//		Logger.debug(this, "reload", success, "load new application:%d, all:%d", count, groups.size());
//
//		return success;
//	}
	
//	/**
//	 * 检查和更新目录下的分布组件包。
//	 * @return 如果文件发生变化和更新成功，返回“真”，否则“假”。
//	 */
//	private boolean reload() {
//		// 扫描磁盘
//		List<TaskGroup> array = detect();
//		// 空记录
//		if (array.isEmpty() && groups.isEmpty()) {
//			return false;
//		}
//
//		// 根据成员尺寸判断匹配
//		boolean match = (array.size() == groups.size());
//		// 进一步判断匹配
//		if (match) {
//			int index = 0;
//			for (; index < array.size(); index++) {
//				TaskGroup e1 = array.get(index);
//				TaskGroup e2 = groups.get(e1.getPart());
//				// 没有找到，或者组件不一致时，更新它！
//				if (e2 == null) {
//					break;
//				} else if (!e1.match(e2)) {
//					break;
//				}
//			}
//			// 全部比较完全，匹配！
//			match = (index == array.size());
//		}
//		// 匹配，不更新
//		if (match) {
//			Logger.info(this, "reload", "all matched! ignore!");
//			return false;
//		}
//
//		// 不匹配时，更新
//		Logger.info(this, "reload", "deploy task group ...");
//
//		// 更新类加载器和阶段命名
//		boolean success = false;
//		// 锁定!
//		super.lockSingle();
//		try {
//			// 清除旧的记录
//			groups.clear();
//			// 形成映射
//			for(TaskGroup e : array) {
//				groups.put(e.getPart(), e);
//			}
//			// 通知上级更新
//			updateTasks(groups.values());
//			success = true;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//
//		return success;
//	}

	/**
	 * 部署组件，加入到自己的集合
	 * 这时传入的是DTC组件包，合并成DTG包
	 * 
	 * @param component 组件
	 * @return 成功返回真，否则假
	 */
	private boolean writeTaskBootGroup(TaskComponent component) {
		TaskPart part = component.getPart();

		// 根据用户签名，建立子目录且成功
		File root = createSubRoot(part.getIssuer());
		if (root == null) {
			Logger.error(this, "writeTaskBootGroup", "cannot be create sub-directory! %s", part.getIssuer());
			return false;
		}

		// 建立文件名，判断文件存在
		String name = buildTaskBootGroup(part);
		File file = new File(root, name);
		boolean exists = (file.exists() && file.isFile());

		// 锁定资源，合并操作！
		boolean success = false;
		super.lockSingle();
		try {
			TaskComponentCombiner combiner = new TaskComponentCombiner();
			combiner.write(component.getContent());
			if (exists) {
				combiner.writeGroup(file, false);
			}
			// 输入到磁盘上
			combiner.flush(part, file);
			combiner.close();
			// 成功
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "writeTaskBootGroup", success, "write %s", file.toString());

		return success;
	}

	/**
	 * 部署组件，加入到自己的集合
	 * 这时是DTC组件包
	 * 
	 * @param component 组件
	 * @return 成功返回真，否则假
	 */
	private boolean writeTaskBootItem(TaskComponent component) {
		// 读组件包内容
		TaskComponentReader reader = new TaskComponentReader(component.getContent());
		TaskSection section = reader.readTaskSection();
		// 空指针，失败！
		if (section == null) {
			Logger.error(this, "writeTaskBootItem", "cannot be resolve %s", component.getPart());
			return false;
		}

		// 修改为发布者的签名
		section.setIssuer(component.getIssuer());

		// 根据用户签名，建立子目录且成功
		File root = createSubRoot(section.getIssuer(), section.getWare());
		if (root == null) {
			Logger.error(this, "writeTaskBootItem", "cannot be create sub-directory! %s", section);
			return false;
		}
		
		// 删除可能存在的垃圾
		removeSoftwareDirectory(root);

		// 建立文件名，判断文件存在
		String name = buildTaskBootItem(section.getTaskPart());
		File file = new File(root, name);

		// 保存内容
		boolean success = writeContent(file, component.getContent());

		Logger.note(this, "writeTaskBootItem", success, "write %s", file.toString());

		return success;
	}

	/**
	 * 部署组件，加入到自己的集合
	 * 这时是DTC组件包
	 * 
	 * @param component 组件
	 * @return 成功返回真，否则假
	 */
	public boolean deploy(TaskComponent component) {
		TaskPart part = component.getPart();
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

		// 保存成集合和单个文件
		boolean success = writeTaskBootGroup(component);
		if (success) {
			success = writeTaskBootItem(component);
		}

//		// 唤醒更新
//		if (success) {
//			wakeup();
//		}

		Logger.debug(this, "deploy", success, "deploy task boot %s", part);

		return success;
	}

	/**
	 * 发布JAR辅助文件
	 * @param component
	 * @return
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

		// 组件部件
		TaskSection section = component.getSection(); 
		// 建立目录
		File root = createSubRoot(section.getIssuer(), section.getWare());
		if (root == null) {
			Logger.error(this, "deploy", "cannot be create sub-directory! %s", section);
			return false;
		}
		
		// 根据传入的文件名，生成一个本地文件！
		File file = new File(root, component.getName());
		if (isMatching(file, content)) {
			return true;
		}

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			// 先删除可能存在的垃圾
			removeSoftwareDirectory(file);
			
			// JAR文件保存到本地磁盘
			success = writeContent(file, content);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

//		// 唤醒更新
//		if (success) {
//			wakeup();
//		}

		Logger.debug(this, "deploy", success, "deploy task assist %s", section);

		return success;
	}

	/**
	 * 部署动态链接库组件
	 * @param component
	 * @return
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

		boolean success = false;
		TaskSection section = component.getSection();
		// 锁定！
		super.lockSingle();
		try {
			// 判断有同质文件存在
			if (hasRefreshLibrary(component.getPart(), content)) {
				Logger.warning(this, "deploy", "library file exists!");
				return true;
			}
			
			// 建立目录
			File root = createSubRoot(section.getIssuer(), section.getWare());
			if (root == null) {
				Logger.error(this, "deploy", "cannot be create sub-directory! %s", section);
				return false;
			}
			
			// 先删除可能存在的垃圾，这个目录要被使用了
			removeSoftwareDirectory(root);

			// 构造一个新的库文件名称
			File file = buildLibraryFile(root, component.getName());
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
			
			Logger.debug(this, "deploy", success, "deploy link library %s # %s", section, file);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return success;
	}	
	
}



///**
// * 部署动态链接库组件
// * @param component
// * @return
// */
//public boolean deploy(TaskLibraryComponent component) {
//	// 判断内容签名一致
//	byte[] content = component.getContent();
//	MD5Hash sign = Laxkit.doMD5Hash(content);
//	boolean match = (Laxkit.compareTo(component.getSign(), sign) == 0);
//	if (!match) {
//		Logger.error(this, "deploy", "library sign error! %s - %s", component.getSign(), sign);
//		return false;
//	}
//
//	boolean success = false;
//	TaskSection section = component.getSection();
//	// 锁定！
//	super.lockSingle();
//	try {
//		// 建立目录
//		File root = createSubRoot(section.getIssuer(), section.getWare());
//		if (root == null) {
//			Logger.error(this, "deploy", "cannot be create sub-directory! %s", section);
//			return false;
//		}
//
//		// 根据传入的文件名，生成一个本地文件！
//		File file = new File(root, component.getName());
//		// 判断一致!
//		if (isMatching(file, content)) {
//			return true;
//		}
//
//		// 文件存在
//		boolean exists = (file.exists() && file.isFile());
//		// 如果文件存在，先删除，再追加！
//		if (exists) {
//			success = file.delete();
//			if (success) {
//				success = writeContent(file, content);
//			}
//		} else {
//			// 动态链接库文件保存到本地磁盘
//			success = writeContent(file, content);
//		}
////		// 加载动态链接库
////		if (success) {
////			success = JNILoader.loadSingleLibrary(file);
////		}
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockSingle();
//	}
//
//	Logger.debug(this, "deploy", success, "deploy link library %s", section);
//
//	return success;
//}


///**
// * 部署动态链接库组件
// * @param component
// * @return
// */
//public boolean deploy(TaskLibraryComponent component) {
//	// 判断内容签名一致
//	byte[] content = component.getContent();
//	MD5Hash sign = Laxkit.doMD5Hash(content);
//	boolean match = (Laxkit.compareTo(component.getSign(), sign) == 0);
//	if (!match) {
//		Logger.error(this, "deploy", "library sign error! %s - %s", component.getSign(), sign);
//		return false;
//	}
//
//	boolean success = false;
//	TaskSection section = component.getSection();
//	// 锁定！
//	super.lockSingle();
//	try {
//		// 建立目录
//		File root = createSubRoot(section.getIssuer(), section.getWare());
//		if (root == null) {
//			Logger.error(this, "deploy", "cannot be create sub-directory! %s", section);
//			return false;
//		}
//
//		// 构造一个新的库文件名称
//		File file = buildLibraryFile(root, component.getName());
//		if (file == null) {
//			Logger.error(this, "deploy", "cannot be create library directory! %s", section);
//			return false;
//		}
//
//		// 动态链接库文件保存到本地磁盘
//		success = writeContent(file, content);
//
//
//		//			// 根据传入的文件名，生成一个本地文件！
//		//			File file = new File(root, component.getName());
//		//			// 判断一致!
//		//			if (isMatching(file, content)) {
//		//				return true;
//		//			}
//		//
//		//			// 文件存在
//		//			boolean exists = (file.exists() && file.isFile());
//		//			// 如果文件存在，先删除，再追加！
//		//			if (exists) {
//		//				success = file.delete();
//		//				if (success) {
//		//					success = writeContent(file, content);
//		//				}
//		//			} else {
//		//				// 动态链接库文件保存到本地磁盘
//		//				success = writeContent(file, content);
//		//			}
//		////			// 加载动态链接库
//		////			if (success) {
//		////				success = JNILoader.loadSingleLibrary(file);
//		////			}
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockSingle();
//	}
//
//	Logger.debug(this, "deploy", success, "deploy link library %s", section);
//
//	return success;
//}	
