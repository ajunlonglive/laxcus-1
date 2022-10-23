/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.pool;

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
 * 分布任务组件资源管理池。<br><br>
 * 
 * 管理池中包含系统级和用户级组件两种。系统级组件由管理员发布，用户级组件由用户发布，管理员负责检查和上传到指定位置。<br>
 * 
 * 当管理池检测到分布计算组件被删除、新增、更新时，ACCOUNT站点将重新注册到TOP站点。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class TaskOnAccountPool extends ComponentPool {

	/** 资源管理池句柄 **/
	private static TaskOnAccountPool selfHandle = new TaskOnAccountPool();

	/** 文件名 -> 部件文件 **/
	private Map<String, TaskFile> mapFiles = new TreeMap<String, TaskFile>();

	/** 阶段部件 -> 部件文件 **/
	private Map<TaskPart, TaskFile> mapTasks = new TreeMap<TaskPart, TaskFile>();

	/** 新加入的私属文件，保存它的文件路径！**/
	private ArrayList<FileTuple> tuples = new ArrayList<FileTuple>();

	/**
	 * 构造分布任务组件资源管理池
	 */
	private TaskOnAccountPool() {
		super();
	}

	/**
	 * 返回分布任务组件资源管理池
	 * @return
	 */
	public static TaskOnAccountPool getInstance() {
		return TaskOnAccountPool.selfHandle;
	}

	/**
	 * 查找匹配的分布任务组件
	 * @param part
	 * @param root
	 * @return
	 */
	public List<Phase> findPhases(TaskPart part, Naming root) {
		ArrayList<Phase> array = new ArrayList<Phase>();
		// 锁定!
		super.lockMulti();
		try {
			TaskFile file = mapTasks.get(part);
			if (file != null) {
				List<Tock> ticks = file.findTocks(root);
				for (Tock tick : ticks) {
					Phase phase = new Phase(part.getIssuer(), part.getFamily(), tick.getSock(), tick.getSub());
					array.add(phase);
//					array.add(new Phase(part, tick));
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 查找匹配的分布任务组件
	 * @param part 工作部件
	 * @return 阶段命名
	 */
	public List<Phase> findPhases(TaskPart part) {
		ArrayList<Phase> array = new ArrayList<Phase>();

		super.lockMulti();
		try {
			TaskFile file = mapTasks.get(part);
			if (file != null) {
				for (Tock tick : file.getTocks()) {
					Phase phase = new Phase(part.getIssuer(), part.getFamily(), tick.getSock(), tick.getSub());
					array.add(phase);
//					array.add(new Phase(part, tick));
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 查找匹配的分布任务组件
	 * @param root
	 * @return
	 */
	public List<Phase> findPhases(Naming root) {
		ArrayList<Phase> array = new ArrayList<Phase>();

		super.lockMulti();
		try {
			Iterator<Map.Entry<TaskPart, TaskFile>> iterator = mapTasks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<TaskPart, TaskFile> entry = iterator.next();
				TaskPart part = entry.getKey();
				TaskFile file = entry.getValue();
				List<Tock> ticks = file.findTocks(root);
				for (Tock tick : ticks) {
					Phase phase = new Phase(part.getIssuer(), part.getFamily(),
							tick.getSock(), tick.getSub());
					array.add(phase);
					// array.add(new Phase(entry.getKey(), tick));
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 输出全部工作部件
	 * @return
	 */
	public List<TaskPart> getTaskParts() {
		super.lockMulti();
		try {
			return new ArrayList<TaskPart>(mapTasks.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 输出全部用户签名
	 * @return Siger列表
	 */
	public List<Siger> getSigers() {
		TreeSet<Siger> array = new TreeSet<Siger>();
		// 保存签名
		super.lockMulti();
		try {
			for (TaskPart part : mapTasks.keySet()) {
				Siger issuer = part.getIssuer();
				// 是注册用户签名
				if (issuer != null) {
					array.add(issuer);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		// 输出
		return new ArrayList<Siger>(array);
	}

	/**
	 * 返回分布任务组件的磁盘文件数目
	 * @return 磁盘文件数目
	 */
	public int size() {
		return mapFiles.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 判断目录有效
		boolean success = (getRoot() != null);
		// 加载磁盘记录
		if (success) {
			int count = reload();
			success = (count >= 0);
		}

		Logger.debug(this, "init", success, "init resource");

		// 返回结果
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into ...");

		// 触发时间
		long touchTime = System.currentTimeMillis() + getInterval();
		// 循环检查
		while (!isInterrupted()) {
			// 判断有私属文件
			if (hasTuple()) {
				loadTuple();
				continue;
			}

			// 抵达时间
			if (System.currentTimeMillis() >= touchTime) {
				// 重新分析一次
				int count = reload();

				Logger.debug(this, "process", "reload count:%d", count);

				//如果发生更新，以异步方式重新注册到TOP站点
				if (count > 0) {
					getLauncher().checkin(false);
				}
				// 下一次触发时间
				touchTime = System.currentTimeMillis() + getInterval();
			}
			sleep();
		}

		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapTasks.clear();
		mapFiles.clear();
		tuples.clear();
	}

	/**
	 * 根据签名，生成全部分布任务组件部件
	 * @param siger 用户签名
	 * @return 返回组件部件
	 */
	private List<TaskPart> createParts(Siger siger) {
		ArrayList<TaskPart> a = new ArrayList<TaskPart>();
		int[] families = PhaseTag.enumlate();
		for (int family : families) {
			a.add(new TaskPart(siger, family));
		}
		return a;
	}

	/**
	 * 删除内存和磁盘上的相关分布任务组件
	 * @param siger 用户签名
	 * @return 返回删除的组件文件数目
	 */
	public int drop(Siger siger) {
		int count = 0;

		// 生成全部组件部件
		List<TaskPart> parts = createParts(siger);

		// 锁定，检索和删除
		super.lockSingle();
		try {
			// 删除组件
			for (TaskPart part : parts) {
				TaskFile element = mapTasks.remove(part);
				// 没有，忽略它
				if (element == null) {
					continue;
				}
				String path = element.getPath();
				mapFiles.remove(path);
				// 判断文件存在，删除磁盘文件！
				File file = new File(path);
				boolean success = (file.exists() && file.isFile());
				if (success) {
					success = file.delete();
					if (success) count++;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.info(this, "drop", "%s delete count %d", siger, count);

		return count;
	}

	/**
	 * 建立一个分布任务组件
	 * @param file 任务组件属性
	 * @return 返回组件实例
	 */
	private TaskComponent create(TaskFile file) {
		File that = new File(file.getPath());
		try {
			byte[] b = new byte[(int) that.length()];

			FileInputStream fin = new FileInputStream(that);
			fin.read(b);
			fin.close();

			return new TaskComponent(file.getTag(), b);
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 根据阶段部件，查找对应的标识
	 * @param part 阶段部件
	 * @return 返回阶段标识，或者空值
	 */
	public TaskTag findTag(TaskPart part) {
		if (part == null) {
			Logger.error(this, "findTag", "null pointer");
			return null;
		}

		TaskTag tag = null;
		super.lockMulti();
		try {
			TaskFile file = mapTasks.get(part);
			if (file != null) {
				tag = file.getTag();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "findTag", "find %s is %s", part, tag);

		return tag;
	}

	/**
	 * 查找匹配的文件路径
	 * @param part 工作部件
	 * @return 返回字符串的文件名
	 */
	public String findPath(TaskPart part) {
		Laxkit.nullabled(part);

		String path = null;
		// 锁定
		super.lockMulti();
		try {
			TaskFile file = mapTasks.get(part);
			if (file != null) {
				path = file.getPath();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "findPath", "find %s is %s", part, path);
		return path;
	}

	/**
	 * 根据组件标识，生成一个分布任务组件
	 * @param tag 组件标识
	 * @return 返回分布任务组件实例
	 */
	public TaskComponent doComponent(TaskTag tag) {
		TaskPart part = tag.getPart();

		// 单向锁定
		super.lockSingle();
		try {
			TaskFile file = mapTasks.get(part);
			if (file == null) {
				return null;
			}

			// 生成一个分布任务组件包
			TaskComponent component = create(file);
			// 输出分布任务组件的字节数组
			return component;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}


	//	/**
	//	 * 扫描磁盘文件，返回文件列表
	//	 * @return
	//	 */
	//	private List<TaskFile> scan(File path) {
	//		ArrayList<TaskFile> array = new ArrayList<TaskFile>();
	//		// 从目录中读取“.dtc”后缀文件
	//		File[] files = path.listFiles();
	//
	//		// 空集合，返回
	//		if (files == null || files.length == 0) {
	//			return array;
	//		}
	//
	//		for (File file : files) {
	//			// 如果是隐藏文件，不处理
	//			if(file.isHidden()) {
	//				continue;
	//			}
	//			// 如果是目录，读这个目录下的文件
	//			if (file.isDirectory()) {
	//				List<TaskFile> sub = scan(file);
	//				array.addAll(sub);
	//				continue;
	//			}
	//
	//			String name = file.getAbsolutePath();
	//			// 不是DTC文件不处理
	//			if (!name.matches(TaskAccount.DTC_REGEX)) {
	//				continue;
	//			}
	//			TaskFile e = new TaskFile(name, file.length(), file.lastModified());
	//			array.add(e);
	//		}
	//		return array;		
	//	}

	//	/**
	//	 * 重新加载分布任务组件，发现更新通知目标节点
	//	 */
	//	private void reload1() {
	//		// 1.本次读取文件
	//		List<TaskFile> array = scan(super.getRoot());
	//
	//		// 2. 判断这是一个新的（本地没有保存，两个条件，1. 文件名没有. 2.签名不一致）
	//		ArrayList<TaskFile> scales = new ArrayList<TaskFile>(array.size());
	//		for (TaskFile file : array) {
	//			boolean success = isNew(file);
	//			// 分析阶段命名和签名
	//			if(success) {
	//				success = file.analyse();
	//			}
	//			if(success) {
	//				scales.add(file);
	//			}
	//
	//			Logger.debug(this, "reload", success, "file is '%s'", file.getName());
	//		}
	//
	//		// 3. 根据新文件的PART，删除磁盘上的旧文件
	//		super.lockSingle();
	//		try {
	//			for (TaskFile file : scales) {
	//				// 根据PART找到旧文件
	//				TaskPart part = file.getPart();
	//
	//				Logger.debug(this, "reload", "check %s %s", file.getName(), part);
	//
	//				TaskFile discard = mapTasks.remove(part);
	//				// 如果过期文件存在，删除磁盘上的文件和内存记录
	//				if (discard != null) {
	//					delete(discard);
	//					mapFiles.remove(discard.getName());
	//				}
	//
	//				// 保存新文件
	//				mapTasks.put(file.getPart(), file);
	//				mapFiles.put(file.getName(), file);
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//
	//		// 4. 即有文件做一次检查，如果不存在就从内存中删除它
	//		ArrayList<TaskPart> checks = new ArrayList<TaskPart>(mapTasks.size());
	//		super.lockSingle();
	//		try {
	//			checks.addAll(mapTasks.keySet());
	//			for (TaskPart part : checks) {
	//				TaskFile file = mapTasks.get(part);
	//				File f = new File(file.getName());
	//				if (!f.exists()) {
	//					mapTasks.remove(part);
	//					mapFiles.remove(file.getName());
	//				}
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//
	//		// 发生更新
	//		boolean updated = (scales.size() > 0);
	//
	//		Logger.debug(this, "reload", updated, "task size: %d - file size: %d", mapTasks.size(), mapFiles.size());
	//
	//		// 通知重新注册
	//		if (updated) {
	//			AccountLauncher.getInstance().hug();
	//		}
	//	}

	//	/**
	//	 * 删除本地的磁盘文件
	//	 * @param file
	//	 * @return
	//	 */
	//	private boolean delete(TaskFile file) {
	//		File f = new File(file.getName());
	//		boolean success = (f.exists() && f.isFile());
	//		if (success) {
	//			success = f.delete();
	//		}
	//		return success;
	//	}

	//	/**
	//	 * 根据文件更新时间和长度，比较旧文件，判断是新文件
	//	 * @param file
	//	 * @return
	//	 */
	//	private boolean isNew(TaskFile file) {
	//		super.lockMulti();
	//		try {
	//			// 根据文件名找文件
	//			TaskFile old = mapFiles.get(file.getName());
	//			// 不存在是新文件
	//			boolean isnew = (old == null);
	//			// 存在，根据属性判断是新文件
	//			if (!isnew) {
	//				isnew = (file.getTime() != old.getTime() || file.getLength() != old.getLength());
	//			}
	//			return isnew;
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//		return false;
	//	}


	/////////////////////////////
	// NEW CODE

	/**
	 * 扫描和检查磁盘资源文件
	 * @param path 文件目录
	 * @return 返回扫描结果
	 */
	private Map<String, TaskFile> scan(File path) {
		TreeMap<String, TaskFile> array = new TreeMap<String, TaskFile>();
		// 从目录中读取“.dtg”后缀文件
		File[] files = path.listFiles();

		// 空集合，返回
		if (files == null || files.length == 0) {
			return array;
		}

		for (File file : files) {
			// 如果是隐藏文件，不处理
			if (file.isHidden()) {
				continue;
			}
			// 如果是目录，读这个目录下的文件
			if (file.isDirectory()) {
				Map<String, TaskFile> sub = scan(file);
				array.putAll(sub);
				continue;
			}

			String name = Laxkit.canonical(file);
			// 不是DTG文件不处理
			if (!name.matches(TF.DTG_REGEX)) {
				continue;
			}
			TaskFile e = new TaskFile(name, file.length(), file.lastModified());
			array.put(e.getPath(), e);
		}
		return array;
	}

	/**
	 * 旧文件有，新文件没有，删除旧文件
	 * @param files 磁盘扫描文件集合
	 * @return 返回删除的成员数目
	 */
	private int delete(Map<String, TaskFile> files) {
		ArrayList<String> array = new ArrayList<String>();

		for (String filename : mapFiles.keySet()) {
			boolean notfound = (files.get(filename) == null);
			if (notfound) {
				array.add(filename);
			}
		}

		for (String filename : array) {
			TaskFile file = mapFiles.remove(filename);
			mapTasks.remove(file.getPart());

			Logger.debug(this, "delete", "file is %s", filename);
		}

		return array.size();
	}

	/**
	 * 扫描群组文件中包含的每个组件
	 * @param file 组件群组文件
	 * @return 成功返回真，否则假
	 * @throws IOException
	 */
	private boolean analyse(TaskFile file) throws IOException {
		TaskComponentGroupReader reader = new TaskComponentGroupReader(file.getFile());
		// 检查
		int count = reader.check();
		if (count < 1) {
			Logger.error(this, "analyse", "cannot be analyse %s", file.getPath());
			return false;
		}

		// 清除旧的
		file.clearTocks();

		// 读取组件部件
		TaskPart part = reader.readTaskPart();
		if (part == null) {
			Logger.error(this, "analyse", "cannot be git TaskPart! from %s", file.getPath());
			return false;
		}
		file.setPart(part);
		// MD5签名
		MD5Hash sign = Laxkit.doMD5Hash(reader.getContent());
		file.setSign(sign);

		// 读取单个文件
		List<CloudPackageItem> items = reader.readTaskComponents();
		for (CloudPackageItem e : items) {
			String name = e.getName();
			// 如果是"GROUP-INF/group.xml"文件，忽略它！
			if (name.equalsIgnoreCase(TF.GROUP_INF)) {
				continue;
			}
			// 取出单个DTC组件内容!
			byte[] content = e.getContent();
			// 解析内容
			boolean success = splitTocket(file, content);
			if (!success) {
				Logger.error(this, "analyse", "cannot be analyse %s & %s", part, name);
				return false;
			}
		}

		Logger.info(this, "analyse", "scan %s okay!", file.getPath());

		return true;
	}

	//	/**
	//	 * 解析组件文件中的引导信息
	//	 * @param file 磁盘组件文件
	//	 * @param content DTC文件内容！
	//	 * @return 成功返回真，否则假
	//	 * @throws IOException
	//	 */
	//	private boolean splitTaskSegment(TaskFile file, byte[] content) throws IOException {
	//		boolean success = false;
	//
	//		// 解析tasks.xml
	//		ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
	//		JarInputStream jin = new JarInputStream(bin);
	//		while (true) {
	//			JarEntry entry = jin.getNextJarEntry();
	//			if (entry == null) {
	//				break;
	//			}
	//
	//			String name = entry.getName();
	//			// 找到组件标记
	//			if (!TaskBootItem.TAG.equals(name)) { // tag file (tasks.xml)
	//				continue;
	//			}
	//
	//			// 取出"TASK-INF/tasks.xml"文件中的内容
	//			ByteArrayOutputStream buff = new ByteArrayOutputStream();
	//			byte[] b = new byte[1024];
	//			while(true) {
	//				int len = jin.read(b, 0, b.length);
	//				if(len == -1) {
	//					break;
	//				}
	//				buff.write(b, 0, len);
	//			}
	//			b = buff.toByteArray();
	//			success = (b != null && b.length > 0);
	//			if (success) {
	//				success = splitSubTaskSegment(file, b);
	//				break;
	//			}
	//		}
	//		jin.close();
	//		bin.close();
	//		
	//		Logger.note(this, "splitTaskSegment", success, "scan dtc content");
	//
	//		return success;
	//	}

	//	/**
	//	 * 解析每个DTC文件中的"TASK-INF/tasks.xml"文件的内容
	//	 * @param file 磁盘组件文件
	//	 * @param taskText "TASK-INF/tasks.xml"内容
	//	 * @return 成功返回真，否则假
	//	 * @throws IOException
	//	 */
	//	private boolean splitSubTaskSegment(TaskFile file , byte[] taskText) throws IOException {
	//		XMLocal xml = new XMLocal();
	//		Document document = xml.loadXMLSource(taskText);
	//		if (document == null) {
	//			return false;
	//		}
	//		
	//		// 取出软件标签
	//		TaskConfigReader reader = new TaskConfigReader(taskText);
	//		WareTag tag = reader.readWareTag();
	//		Logger.note(this, "splitSubTaskSegment", tag != null, "tag is %s", tag);
	//
	//		// 解析命名（分为系统命名和用户命名）
	//		NodeList list = document.getElementsByTagName("task");
	//		int size = list.getLength();
	//		for (int i = 0; i < size; i++) {
	//			Element element = (Element) list.item(i);
	//
	//			// 任务命名，包括根命名和子命名两部分，中间由逗号隔开
	//			String naming = xml.getValue(element, "naming");
	//			TaskSegment segment = new TaskSegment(naming);
	//			boolean success = file.addTaskSegment(segment);
	//			
	//			Logger.note(this, "splitSubTaskSegment", success, "save %s", segment);
	//		}
	//		return true;
	//	}

//	/**
//	 * 解析组件文件中的引导信息
//	 * @param file 磁盘组件文件
//	 * @param content DTC文件内容！
//	 * @return 成功返回真，否则假
//	 * @throws IOException
//	 */
//	private boolean splitTaskTick(TaskFile file, byte[] content) throws IOException {
//		TaskComponentReader reader = new TaskComponentReader(content);
////		byte[] taskText = reader.readTaskText();
////		boolean success = (!Laxkit.isEmpty(taskText));
////		if (success) {
////			success = splitSubTaskTick(file, taskText);
////		}
//		
//		List<TaskTick> ticks = reader.readTickTicks();
//		int count = 0;
//		if (ticks != null) {
//			count = file.addTaskTicks(ticks);
//		}
//		boolean success = (count > 0);
//
//		Logger.note(this, "splitTaskTick", success, "save %d", count);
////		return success;
////
////		Logger.note(this, "splitTaskTick", success, "scan dtc content");
//
//		return success;
//	}

//	/**
//	 * 解析每个DTC文件中的"TASK-INF/tasks.xml"文件的内容
//	 * @param file 磁盘组件文件
//	 * @param taskText "TASK-INF/tasks.xml"内容
//	 * @return 成功返回真，否则假
//	 * @throws IOException
//	 */
//	private boolean splitSubTaskTick(TaskFile file , byte[] taskText) throws IOException {
//		// 取出软件标签
//		TaskConfigReader reader = new TaskConfigReader(taskText);
//		List<TaskTick> ticks = reader.readTaskTicks();
//		int count = file.addTaskTicks(ticks);
//		boolean success = (count > 0);
//
//		Logger.note(this, "splitSubTaskTick", success, "save %d", count);
//		return success;
//	}

	/**
	 * 解析组件文件中的引导信息
	 * @param file 磁盘组件文件
	 * @param content DTC文件内容！
	 * @return 成功返回真，否则假
	 * @throws IOException
	 */
	private boolean splitTocket(TaskFile file, byte[] content) throws IOException {
		TaskComponentReader reader = new TaskComponentReader(content);
		List<Tock> ticks = reader.readTocks();
		int count = 0;
		if (ticks != null && ticks.size() > 0) {
			count = file.addTocks(ticks);
		}
		boolean success = (count > 0);

		Logger.note(this, "splitTocket", success, "save count %d", count);

		return success;
	}
	/**
	 * 追加。新文件中有，旧文件没有，解析追加
	 * @param files 
	 * @return
	 */
	private int append(Map<String, TaskFile> files) {
		ArrayList<String> array = new ArrayList<String>();

		// 新文件有，旧文件没有，保存新文件名
		for (String filename : files.keySet()) {
			boolean notfound = (mapFiles.get(filename) == null);
			if (notfound) {
				array.add(filename);
			}
		}

		// 扫描磁盘文件，追加到运行文件集合中
		int count = 0;
		for (String filename : array) {
			TaskFile file = files.get(filename);
			// 解析文件
			boolean success = false;  //file.analyse();
			try {
				success = analyse(file);
			} catch (IOException e) {
				Logger.error(e);
			}
			// 不成功，忽略它
			if (!success) {
				Logger.warning(this, "append", "analyse failed! %s", filename);
				continue;
			}

			TaskFile old = mapTasks.get(file.getPart());
			// 有相同阶段部件文件存在，先删除它再保存
			if (old != null) {
				mapTasks.remove(old.getPart());
				mapFiles.remove(old.getPath());

				// 删除磁盘上重叠的文件
				File that = new File(old.getPath());
				if (that.exists()) {
					boolean b = that.delete();
					Logger.note(this, "append", b, "delete overlap file %s", old.getPath());
				}
			}

			// 保存新文件
			mapTasks.put(file.getPart(), file);
			mapFiles.put(file.getPath(), file);
			// 从扫描文件中删除它
			files.remove(filename);
			// 统计
			count++;

			Logger.debug(this, "append", "%s is %s", filename, file.getPart());
		}

		return count;
	}

	/**
	 * 替换
	 * @param files
	 * @return
	 */
	private int replace(Map<String, TaskFile> files) {
		ArrayList<String> array = new ArrayList<String>();

		// 检查文件属性，如果不一致，进行替换
		for (String name : files.keySet()) {
			TaskFile file = files.get(name);
			TaskFile old = mapFiles.get(name);
			boolean match = (file != null && old != null);
			// 两个文件不一致，忽略
			if (!match) {
				continue;
			}
			// 属性匹配
			match = (file.getModified() == old.getModified() && file.getLength() == old.getLength());
			// 文件属性匹配，忽略
			if (match) {
				continue;
			}
			// 文件属性不匹配，删除旧的，保存新的
			mapFiles.remove(old.getPath());
			mapTasks.remove(old.getPart());
			// 解析文件
			boolean success = false; // file.analyse();
			try {
				success = analyse(file);
			} catch (IOException e) {
				Logger.error(e);
			}
			if (success) {
				// 保存新文件
				mapTasks.put(file.getPart(), file);
				mapFiles.put(file.getPath(), file);
				// 记录更新的文件
				array.add(name);
			}
		}

		// 删除更新文件
		for(String name : array) {
			files.remove(name);

			Logger.debug(this, "replace", "file is %s", name);
		}

		// 返回更新的数目
		return array.size();
	}

	/**
	 * 重新加载分布任务组件
	 * 
	 * @return 成功，返回更新后的组件数目（大于等于0）；否则是-1，失败!
	 */
	private int reload() {
		// 从磁盘上获得分布任务组件文件
		Map<String, TaskFile> files = scan(getRoot());

		// 以锁定方式进行检查
		super.lockSingle();
		try {
			// 删除旧文件
			int count = delete(files);
			// 追加新文件
			count += append(files);
			// 替换更新后的文件
			count += replace(files);
			// 返回重新加载后更新的组件数目
			return count;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return -1;
	}
	
	/**
	 * 删除一个磁盘文件
	 * @param file 文件实例
	 * @return 成功返回真，否则假
	 */
	public boolean delete(File file) {
		boolean exists = (file.exists() && file.isFile());
		if (!exists) {
			return false;
		}

		TaskFile taskFile = new TaskFile(file);
		boolean success = false;
		// 锁定，解析/删除
		super.lockSingle();
		try {
			// 解析
			boolean okay = analyse(taskFile);
			// 删除
			if (okay) {
				// 删除内存记录
				mapTasks.remove(taskFile.getPart());
				mapFiles.remove(taskFile.getPath());
				// 删除磁盘文件，关键！
				success = file.delete(); 
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "delete", success, "delete %s", Laxkit.canonical(file));

		return success;
	}

	/**
	 * 加载一个新的文件，立即生效！
	 * @param file 磁盘文件
	 */
	public boolean load(File file) {
		// 生成实例
		FileTuple element = new FileTuple(file);

		boolean success = false;
		// 锁定保存!
		super.lockSingle();
		try {
			// 判断文件不存在才保存！
			if (!tuples.contains(element)) {
				tuples.add(element);
				success = true;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 等待直到完成
		if (success) {
			wakeup();
			element.await();
		}

		Logger.debug(this, "load", element.isSuccessful(), "read %s", Laxkit.canonical(file));

		return element.isSuccessful();
	}

	/**
	 * 判断有缓存文件
	 * @return 真或者假
	 */
	private boolean hasTuple() {
		return tuples.size() > 0;
	}

	/**
	 * 弹出一个私属文件
	 * @return 文件路径
	 */
	private FileTuple popupTuple() {
		// 锁定
		super.lockSingle();
		try {
			if (tuples.size() > 0) {
				return tuples.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 加载私属文件
	 */
	private void loadTuple() {
		FileTuple tuple = popupTuple();
		if (tuple != null) {
			loadTuple(tuple);
		}
	}

	/**
	 * 加载文件
	 * @param tuple
	 */
	private void loadTuple(FileTuple tuple) {
		TaskFile taskFile = new TaskFile(tuple.getFile());
		// 解析文件
		boolean success = false;
		try {
			success = analyse(taskFile);
		} catch (IOException e) {
			Logger.error(e);
		}

		// 解析成功
		if (success) {
			success = false;
			// 锁定
			super.lockSingle();
			try {
				mapTasks.put(taskFile.getPart(), taskFile);
				mapFiles.put(taskFile.getPath(), taskFile);
				success = true;
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}
		}

		// 设置结果
		tuple.setSuccessful(success);
	}
}