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
import com.laxcus.util.each.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 分布任务组件工具管理池
 * 提供基础参数
 * 
 * @author scott.liang
 * @version 1.0 10/4/2019
 * @since laxcus 1.0
 */
public abstract class TaskToolPool extends TaskPool {

	/** 库ID生成器 **/
	private SerialGenerator lid = new SerialGenerator(999, Integer.MAX_VALUE - 9999); 

	/** 新部署但是没有发布的动态链接库 **/
	private Map<TaskPart, RefreshLibrary> refreshLibraries = new TreeMap<TaskPart, RefreshLibrary>();

	/** 收集被删除但是不成功的垃圾目录 **/
	private TreeSet<File> rubbishDirectoires = new TreeSet<File>();

	/**
	 * 构造组件工作管理池
	 * @param family 阶段类型
	 */
	protected TaskToolPool(int family) {
		super(family);
	}

	/**
	 * 返回下一个库文件ID号
	 * @return 整数码
	 */
	protected int nextLibraryID() {
		return (int) lid.nextSerial();
	}

	/**
	 * 判断磁盘和传入内容匹配
	 * @param file 磁盘文件
	 * @param content 准备写入磁盘的数据
	 */
	protected boolean isMatching(File file , byte[] content) {
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

			// name = issuer.toString().toUpperCase();
		}

		return new File(getRoot(), name);
	}

	/**
	 * 根据用户SHA256签名，建立专属他的子目录，如果是系统组件，目录是“system”。
	 * @param issuer 用户签名，或者空指针
	 * @return 成功返回真的目录名，否则是空指针
	 */
	protected File createSubRoot(Siger issuer) {
		File dir = buildSubRoot(issuer);
		// 判断目录存在且是“目录”属性
		boolean success = (dir.exists() && dir.isDirectory());
		// 不存在，建立一个新的目录
		if (!success) {
			success = dir.mkdirs();
		}
		return (success ? dir : null);
	}

	/**
	 * 根据用户SHA256签名，建立专属他的子目录，如果是系统组件，目录是“system”。
	 * @param issuer 用户签名，或者空指针
	 * @param ware 软件名称
	 * @return 成功返回真的目录名，否则是空指针
	 */
	protected File createSubRoot(Siger issuer, Naming ware) {
		// 生成用户目录
		File dir = createSubRoot(issuer);
		if (dir == null) {
			return null;
		}

		// 以上成立，生成次级目录

		// 1. 生成半截符码
		String wareName = ware.toString();
		wareName = wareName.trim().toLowerCase();
		wareName = Halffer.encode(wareName);

		// 2. 生成次级目录
		dir = new File(dir, wareName);
		boolean success = (dir.exists() && dir.isDirectory());
		// 不存在，建立一个新的目录
		if (!success) {
			success = dir.mkdirs();
		}

		return (success ? dir : null);
	}

	/**
	 * 生成动态链接库文件
	 * @param root 根目录
	 * @param name 动态链接库文件名称
	 * @return 返回目录
	 */
	protected File buildLibraryFile(File root, String name) {
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
	 * 生成以EACH用户签名为标准的文件名称（只是文件名称，不包含目录）
	 * @return 返回文件名称的字符串
	 */
	protected String buildTaskBootGroup(TaskPart part) {
		String prefix = "SYSTEM";
		// 用户组件
		if (part.isUserLevel()) {
			long hash = EachTrustor.sign(part.getIssuer().binary());
			prefix = Long.toHexString(hash).toUpperCase();
		}
		String suffix = PhaseTag.translate(part.getFamily());
		return prefix + "_" + suffix + TF.DTG_SUFFIX;
	}

	/**
	 * 生成以EACH用户签名为标准的引导单元文件名称（只是文件名称，不包含目录）
	 * @return 返回文件名称的字符串
	 */
	protected String buildTaskBootItem(TaskPart part) {
		String prefix = "SYSTEM";
		// 用户组件
		if (part.isUserLevel()) {
			long hash = EachTrustor.sign(part.getIssuer().binary());
			prefix = Long.toHexString(hash).toUpperCase();
		}
		String suffix = PhaseTag.translate(part.getFamily());
		return prefix + "_" + suffix + TF.DTC_SUFFIX;
	}

	/**
	 * 判断是分布计算组件包文件
	 * @param file 磁盘文件名
	 * @return 返回是或者否
	 */
	protected boolean isBootGroup(File file) {
		String path = canonical(file);
		return path.matches(TF.DTG_REGEX);
	}

	/**
	 * 判断是分布计算组件包文件
	 * @param file 磁盘文件名
	 * @return 返回是或者否
	 */
	protected boolean isTaskElementBoot(File file) {
		String path = canonical(file);
		return path.matches(TF.DTC_REGEX);
	}

	/**
	 * 保存子文件到组件单元，包括JAR文件和动态链接库
	 * @param element 组件单元
	 * @param dir 目录
	 * 
	 * @return 返回新增加成员数目
	 */
	protected int scanSubUnit(TaskElement element, File dir){
		Logger.debug(this, "scanSubUnit", "scan %s", dir);

		int count = 0;
		File[] files = dir.listFiles();
		int size = (files == null ? 0 : files.length);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			// 如果是目录，继续追踪
			if (file.isDirectory()) {
				scanSubUnit(element, file);
				continue;
			}
			// 只保存附件和动态链接库

			// 判断是JAR文件，保存它，任意多个！
			if (isJAR(file)) {
				FileKey key = createJAR(file);
				boolean success = element.addJAR(key);
				if (success) {
					count++;
				}
				//				Logger.debug(this, "scanSubFiles", success, "add %s", key);
			}
			// 判断是动态链接库文件，任意多个
			else if (isLinkLibrary(file)) {
				// 1. 如果这个链接库存在于垃圾目录中，忽略它
				if (isRubbishLibrary(file)) {
					continue;
				}
				//				// 会出现多个动态链接库分属不同ClassLoader的情况，这里做过滤。如果记录中不存在链接库文件，忽略它
				//				boolean first = (lid.getLoop() == 0 && lid.getCurrentSerial() == lid.getMinSerial());
				//				// 2. 不是第一次了，找它的同类值
				//				if (!first) {
				//					if (!hasRefreshLibrary(element.getTaskPart(), file)) {
				//						continue;
				//					}
				//				}


				//				if(first) {
				//					update = true;
				//				} else {
				//					update = hasRefreshLibrary(element.getTaskPart(), file);
				//				}

				// 2. 如果是第一次，或者更新链接库记录集中有这个动态链接库，记录它
				boolean first = (lid.getLoop() == 0 && lid.getCurrentSerial() == lid.getMinSerial());
				boolean update = (first || hasRefreshLibrary(element.getTaskPart(), file));
				// 可以记录，保存链接库参数
				if (update) {
					FileKey key = createLibraryKey(file);
					boolean success = element.addLibrary(key);
					if (success) {
						count++;
					}
				}
				
				//				Logger.debug(this, "scanSubFiles", success, "add %s", key);
			}
		}

		Logger.debug(this, "scanSubUnit", "scan %s, sub member: %d", element.getSection(), count);

		// 返回统计数
		return count;
	}

	/**
	 * 扫描一个应用软件的组件单元。每个组件单元包括它的全部内容。
	 * 
	 * @param owner 软件拥有人
	 * @param item 包成员
	 * @return 返回TaskElement实例，失败是空指针
	 */
	private TaskElement scanTaskSoftware(Siger owner, File dir) {
		Logger.debug(this, "scanTaskSoftware", "scan %s", dir);

		ArrayList<File> files = new ArrayList<File>();
		ArrayList<File> dirs = new ArrayList<File>();

		File[] list = dir.listFiles();
		int size = (list == null ? 0 : list.length);
		for (int i = 0; i < size; i++) {
			File file = list[i];
			// 是目录，先保存
			if (file.isDirectory()) {
				dirs.add(file);
				continue;
			}
			// 是组件单元引导文件，总是只能有一个
			else if (isTaskElementBoot(file)) {
				files.add(file);
			}
		}
		// 空记录，忽略退出！
		if (files.isEmpty()) {
			return null;
		}
		// 只能有一个引导文件，否则是错误!
		if (files.size() != 1) {
			Logger.error(this, "scanTaskSoftware",
					"directory %s fatal! boot file too multi! %d != 1", dir, files.size());
			return null;
		}

		// 读组件包内容
		File file = files.get(0);
		byte[] content = null;
		TaskSection section = null;
		try {
			content = readContent(file);
			if (content != null) {
				TaskComponentReader reader = new TaskComponentReader(content);
				section = reader.readTaskSection();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 空指针，失败！
		if (content == null || section == null) {
			Logger.error(this, "scanTaskSoftware", "cannot be resolve %s", file);
			return null;
		}
		// 修改拥有人
		section.setIssuer(owner);

		// 生成引导类，解析内容参数
		String path = Laxkit.canonical(file);
		TaskElementBoot boot = new TaskElementBoot(path, content);
		boolean success = boot.suckup();
		if (!success) {
			Logger.error(this, "scanTaskSoftware", "cannot be resolve %s", file);
			return null;
		}

		// 生成实例
		TaskElement element = new TaskElement(dir, section);
		element.setBoot(boot);
		// 提取子目录下的全部附件文件和动态链接库文件
		for (File sub : dirs) {
			scanSubUnit(element, sub);
		}
		// scanSubFiles(element, dir);

		return element;
	}

	/**
	 * 从指定目录中取出任务组件群
	 * @param dir 指定的目录
	 * @return 返回任务组件群，或者空指针
	 */
	protected TaskGroup scanGroup(File dir) {
		Logger.debug(this, "scanGroup", "scan %s", dir);

		ArrayList<File> files = new ArrayList<File>();
		ArrayList<File> dirs = new ArrayList<File>();

		File[] list = dir.listFiles();
		int size = (list == null ? 0 : list.length);
		for (int i = 0; i < size; i++) {
			File file = list[i];
			// 保存目录
			if (file.isDirectory()) {
				dirs.add(file);
			}
			// 是组件集引导文件，总是只能有一个
			else if (isBootGroup(file)) {
				files.add(file);
			}
		}
		// 空记录，忽略退出！
		if (files.isEmpty()) {
			return null;
		}
		// 只能有一个引导文件，否则是错误!
		if (files.size() != 1) {
			Logger.error(this, "scanGroup",
					"directory %s fatal! boot file too multi! %d != 1", dir, files.size());
			return null;
		}

		// 取出这个文件
		File file = files.get(0);

		// 生成签名、读工作部件!
		MD5Hash sign = null;
		TaskPart part = null;
		try {
			sign = Laxkit.doMD5Hash(file);
			TaskComponentGroupReader reader = new TaskComponentGroupReader(file);
			part = reader.readTaskPart();
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 判断，空指针是错误；
		if (sign == null || part == null) {
			Logger.error(this, "scanGroup", "task-tag missing!");
			return null;
		}

		TaskGroup group = new TaskGroup(part, sign, dir);
		// 逐个扫描每个应用
		for (File sub : dirs) {
			TaskElement element = scanTaskSoftware(part.getIssuer(), sub);
			if(element != null) {
				group.add(element);
			}
		}

		return group;
	}

	//	/**
	//	 * 从指定的目录，扫描相关的分布计算组件和附属的JAR包、动态链接库
	//	 * @param dir 根目录
	//	 * @return 返回全部的组件包集合
	//	 */
	//	private List<TaskGroup> scanDisk(File dir) {
	//		ArrayList<TaskGroup> array = new ArrayList<TaskGroup>();
	//
	//		// 判断目录存在且有效！
	//		boolean success = (dir.exists() && dir.isDirectory());
	//		if (!success) {
	//			Logger.error(this, "scanDisk", "illegal directory %s", dir);
	//			return array;
	//		}
	//
	//		// 扫描引导文件、JAR、SO文件，生成TaskGroup
	//		TaskGroup group = scanGroup(dir);
	//		if (group != null) {
	//			array.add(group);
	//		}
	//
	//		// 判断有子目录，提取里面的文件
	//		File[] files = dir.listFiles();
	//		int size = (files == null ? 0 : files.length);
	//		for (int i = 0; i < size; i++) {
	//			File file = files[i];
	//			// 是目录，扫描它
	//			if (file.isDirectory()) {
	//				List<TaskGroup> a = scanDisk(file);
	//				array.addAll(a);
	//			}
	//		}
	//
	//		Logger.debug(this, "scanDisk", "\'%s\' task group size %d", dir, array.size());
	//
	//		return array;
	//	}

	/**
	 * 从当前节点的任务根目录开始，扫描全部分布应用群。<br>
	 * 说明，一个应用群包含多个子应用，一个子应用就是一个应用软件。
	 * @param dir 任务根目录
	 * @return 返回TaskGroup数组
	 */
	private List<TaskGroup> scanRoot(File dir) {
		ArrayList<TaskGroup> array = new ArrayList<TaskGroup>();

		// 判断目录存在且有效！
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			Logger.error(this, "scanRoot", "illegal directory %s", dir);
			return array;
		}

		ArrayList<File> roots = new ArrayList<File>();

		// 判断有子目录，提取里面的文件
		File[] files = dir.listFiles();
		int size = (files == null ? 0 : files.length);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			// 是目录，保存和准备扫描它
			if (file.isDirectory()) {
				roots.add(file);
			}
		}

		// 扫描引导文件、JAR、SO文件，生成TaskGroup
		for (File subRoot : roots) {
			TaskGroup group = scanGroup(subRoot);
			if (group != null) {
				array.add(group);
			}
		}

		Logger.debug(this, "scanRoot", "\'%s\' task group size %d", dir, array.size());

		return array;
	}

	/**
	 * 从目录中提取分布组件集合，集合中的组件必须有效
	 * @return TaskDocument列表
	 */
	protected List<TaskGroup> detect() {
		List<TaskGroup> array = new ArrayList<TaskGroup>();

		// 扫描文件
		//		List<TaskGroup> groups = scanDisk(getRoot());

		List<TaskGroup> groups = scanRoot(getRoot());
		// 检查和保存它们
		for (TaskGroup group : groups) {
			// 空集，或者没有工作部件，属于无效！忽略它
			if (group.isEmpty() || group.getTag() == null) {
				continue;
			}
			// 保存
			array.add(group);
		}

		Logger.debug(this, "detect", "\'%s\' task group size %d", getRoot(), array.size());

		return array;
	}

	protected boolean addSoftwareDirectory(File file) {
		if (file != null) {
			return rubbishDirectoires.add(file);
		}
		return false;
	}

	protected boolean removeSoftwareDirectory(File file) {
		if (file != null) {
			return rubbishDirectoires.remove(file);
		}
		return false;
	}

	/**
	 * 粉碎垃圾目录
	 * @return 返回成功数目
	 */
	protected int shredSoftwareDirectories() {
		int count = 0;
		for (File dir : rubbishDirectoires) {
			boolean b = shredDirectory(dir);
			if (b) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 粉碎删除软件存储目录，以及目录下的全部文件
	 * @param dir 磁盘目录
	 * @return 成功返回真，否则假
	 */
	protected boolean shredWareDirectory(File dir) {
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			return false;
		}
		// 枚举全部
		File[] lists = dir.listFiles();
		int size = (lists == null ? 0 : lists.length);
		for (int i = 0; i < size; i++) {
			File file = lists[i];
			if (file.isDirectory()) {
				shredWareDirectory(file);
			} else if (file.isFile()) {
				file.delete();
			}
		}
		// 最后，删除空目录
		success = dir.delete();
		// 不成功，记录这个目录
		if (!success) {
			addSoftwareDirectory(dir);
		}
		Logger.note(this, "shredWareDirectory", success, "delete directory %s", dir);
		return success;
	}

	/**
	 * 粉碎删除目录，以及目录下的全部文件
	 * @param dir 磁盘目录
	 * @return 成功返回真，否则假
	 */
	protected boolean shredDirectory(File dir) {
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			return false;
		}
		// 枚举全部
		File[] lists = dir.listFiles();
		int size = (lists == null ? 0 : lists.length);
		for (int i = 0; i < size; i++) {
			File file = lists[i];
			if (file.isDirectory()) {
				boolean b = shredDirectory(file);
				if (!b) return false;
			} else if (file.isFile()) {
				boolean b = file.delete();
				Logger.note(this, "shredDirectory", b, "delete file %s", file);
				if (!b) return false;
			}
		}
		// 最后，删除空目录
		success = dir.delete();
		Logger.note(this, "shredDirectory", success, "delete directory %s", dir);
		return success;
	}

	/**
	 * 增加库文件
	 * @param part
	 * @param file
	 */
	protected void addRefreshLibrary(TaskPart part, File file) {
		RefreshLibrary lib = refreshLibraries.get(part);
		if (lib == null) {
			lib = new RefreshLibrary(part);
			refreshLibraries.put(lib.getPart(), lib);
		}
		lib.add(file);
	}

	/**
	 * 删除库文件
	 * @param part 单元部件
	 * @return 删除成功返回真，否则假
	 */
	protected boolean removeRefreshLibrary(TaskPart part) {
		return refreshLibraries.remove(part) != null;
		
//		boolean success = refreshLibraries.remove(part) != null;
//		Logger.note(this, "removeRefreshLibrary", success, "release refresh library! %s", part);
//		return success;
	}

	//	protected boolean hashasRefreshLibrary() {
	//		return refreshLibraries.size()>0;
	//	}

	/**
	 * 判断链接库存在于记录中
	 * @param part
	 * @param file
	 * @return
	 */
	private boolean hasRefreshLibrary(TaskPart part, File file) {
		RefreshLibrary lib = refreshLibraries.get(part);
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
	 * 判断有匹配的库文件
	 * @param part
	 * @param content
	 * @return
	 */
	protected boolean hasRefreshLibrary(TaskPart part, byte[] content) {
		RefreshLibrary lib = refreshLibraries.get(part);
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


//	/**
//	 * 从目录中提取分布组件集合，集合中的组件必须有效
//	 * @return TaskDocument列表
//	 */
//	protected List<TaskGroup> detect() {
//		List<TaskGroup> array = new ArrayList<TaskGroup>();
//
//		// 扫描文件
//		List<TaskGroup> documents = scanDisk(getRoot());
//		// 检查和保存它们
//		for (TaskGroup document : documents) {
//			// 空集，或者没有工作部件，属于无效！忽略它
//			if (document.isEmpty() || document.getTag() == null) {
//				continue;
//			}
//
////			// 加载动态链接库
////			for (TaskElement element : document.list()) {
////				for (FileKey key : element.getLibraries()) {
////					File file = new File(key.getPath());
////					boolean success = JNILoader.loadSingleLibrary(file);
////					Logger.debug(this, "detect", success,
////							"load link library %s", key.getPath());
////				}
////			}
//			
//			// 保存
//			array.add(document);
//		}
//
//		Logger.debug(this, "detect", "\'%s\' task group size %d", getRoot(), array.size());
//
//		return array;
//	}
