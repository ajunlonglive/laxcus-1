/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import com.laxcus.command.cloud.task.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.naming.*;

/**
 * 分布任务组件管理池。包括ESTABLISH和CONDUCT两组命令的阶段任务。<br>
 * 任务管理池会定时检查指定目录下的JAR包，如果发现有新发布的组件，会即时更新内存中的记录。<br><br>
 * 
 * 每个任务管理池只监视一个发布目录，只管理一种类型的发布任务。如果在运行过程中改变发布目录，
 * 旧的目录将被丢弃，修改接口是: setRoot。<br>
 * 后续将支持网络更新，即分布组件部署在其它服务器上，
 * 任务管理池通过网络去定时检查它的更新状态。这样有助于统一管理任务组件。<br><br>
 * 分布任务组件后缀名：dtc(distributed task component)，采用小写字母。如”laxcus_system.dtc“。
 * 
 * TASK内部参数命名规则<br>
 * 1. 分布任务组件包下必须有一个TASK-INF目录，这个目录下放一个tasks.xml，做为配置文件<br>
 * 2. "TASK-INF"必须全大写<br>
 * 3. "tasks.xml"必须全小写 <br>
 * 4. tasks.xml里面的配置规则见"task"标准(在DistributeTask)<br>
 * 5. 与task中的类相关的类文件可以放在不同包中,但是必须保证能够被找到。 <br>
 * 6. 如果在启动目录下定义"task"目录,运行时自动加载里面的配置文件 <br><br>
 * 
 * 包规则：<br>
 * 1. 用户以“.dtc”为后缀的分布任务组件包，只能有一个。<br>
 * 2. 与分布任务组件包配套的JAR包，允许任意多个，后缀固定是“.jar”，忽略大小写。<br>
 * 3. 每个阶段中的分布计算类实例，打包在一个“.dtc”文件里（方便管理）。建议“.dtc”文件只放分布计算类实例，辅助类放入JAR包。<br><br>
 * 
 * 这个类同时加载JAR包和动态链接库（.so/.dll），但是在释放HotClassLoader的时候，<br>
 * 不要使用"LibraryClassLoader.loader.freeLoadLibrary"去主动释放动态链接库。<br>
 * 释放动态链接库的工作留给垃圾回收去处理，若使用"LibraryClassLoader.loader.freeLoadLibrary"主动释放，<br>
 * 会出现JVM崩溃的可能（已经多次验证过）！！！<br><br>
 * 
 * 
 * @author scott.liang 
 * @version 1.3 10/19/2013
 * @since laxcus 1.0
 */
public abstract class TaskPool extends DiskPool implements TaskReader {
	
	/**
	 * 更新但是没有部署的动态链接库
	 *
	 * @author scott.liang
	 * @version 1.0 7/11/2021
	 * @since laxcus 1.0
	 */
	public class RefreshLibrary implements Comparable<RefreshLibrary> {

		TaskPart part;

		ArrayList<File> array = new ArrayList<File>();

		public RefreshLibrary(TaskPart part) {
			super();
			setPart(part);
		}

		public void setPart(TaskPart e) {
			part = e;
		}

		public TaskPart getPart() {
			return part;
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(RefreshLibrary that) {
			return Laxkit.compareTo(part, that.part);
		}
	}
	
	/**
	 * 已经成为垃圾的动态链接库
	 *
	 * @author scott.liang
	 * @version 1.0 7/11/2021
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

	/** 阶段类型，每个组件管理池只接受一个阶段。见PhaseTag中的定义。 **/
	private int family;

	/** 分布任务组件监听器  */
	private TaskListener taskListener;

	/** 组件工作区(对应一个分布应用) -> 分布应用对标的类加载器 **/
	private TreeMap<TaskSection, HotClassLoader> loaders = new TreeMap<TaskSection, HotClassLoader>();

	/** 阶段命名 -> 项目配置 **/
	private TreeMap<Phase, TaskProject> projects = new TreeMap<Phase, TaskProject>();
	
	/** 待删除和回收的垃圾文件 **/
	private ArrayList<RubbishLibrary> rubbishLibraries = new ArrayList<RubbishLibrary>();

	/**
	 * 构造私有的分布任务组件管理池
	 */
	private TaskPool() {
		super();
		setSleepTime(60); // 默认延时是60秒
		family = 0;
	}

	/**
	 * 构造分布任务组件管理池，指定阶段类型
	 * @param family CONDUCT/ESTABLISH中的任何一种阶段
	 */
	protected TaskPool(int family) {
		this();
		setFamily(family);
	}

	/**
	 * 设置分布任务组件的阶段类型
	 * @param who CONDUCT/ESTABLISH/CONTACT阶段
	 */
	private void setFamily(int who) {
		if (!PhaseTag.isPhase(who)) {
			throw new IllegalPhaseException("illega %d", who);
		}
		family = who;
	}

	/**
	 * 返回分布任务组件的阶段类型
	 * @return CONDUCT/ESTABLISH阶段类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 判断阶段类型一致
	 * @param phaseFamily 阶段类型
	 * @return 返回是或者否
	 */
	public boolean match(int phaseFamily) {
		return phaseFamily == family;
	}

	/**
	 * 设置分布任务组件监听接口
	 * @param e 分布任务组件监听接口实例
	 */
	public void setTaskListener(TaskListener e) {
		taskListener = e;
	}

	/**
	 * 返回分布任务组件监听接口
	 * @return 分布任务组件监听接口实例
	 */
	public TaskListener getTaskListener() {
		return taskListener;
	}

	/**
	 * 判断用户的分布任务组件已经加载
	 * @param section 任务区
	 * @return 返回真或者假
	 */
	public boolean hasTask(TaskSection section) {
		HotClassLoader sub = findClassLoader(section);
		return sub != null; // 判断有效！
	}

	/**
	 * 判断用户的分布任务组件已经加载。如果是空指针，表示是系统级分布任务组件
	 * @param issuer 用户签名，或者空指针
	 * @param ware 软件包名称
	 * @return 返回真或者假
	 */
	public boolean hasTask(Siger issuer, Naming ware) {
		TaskSection section = new TaskSection(issuer, getFamily(), ware);
		return hasTask(section);
	}

	/**
	 * 统计类加载器
	 * @param part 工作部件
	 * @return HotClasLoader实例数组
	 */
	private int countClassLoaders(TaskPart part) {
		int count = 0;
		// 锁定，逐个检查
		super.lockMulti();
		try {
			Iterator<Map.Entry<TaskSection, HotClassLoader>> iterator = loaders.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<TaskSection, HotClassLoader> entry = iterator.next();
				TaskSection key = entry.getKey();
				// 判断一致！
				boolean success = (Laxkit.compareTo(key.getTaskPart(), part) == 0);
				if (success) {
					count++;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "countClassLoaders", "ClassLoader elements: %d", count);

		return count;
	}

	/**
	 * 判断用户的分布任务组件已经加载。如果是空指针，表示是系统级分布任务组件
	 * @param issuer 用户签名，或者空指针
	 * @return 返回真或者假
	 */
	public boolean hasTask(Siger issuer) {
		TaskPart part = new TaskPart(issuer, getFamily());
		int count = countClassLoaders(part);
		return count > 0;
	}

	/**
	 * 判断已经加载系统级分布任务组件
	 * @return 返回真或者假
	 */
	public boolean hasSystemTask() {
		return hasTask(null, Sock.SYSTEM_WARE);
	}

	/**
	 * 输出管理池中的全部阶段命名副本
	 * @return 阶段命名列表
	 */
	public List<Phase> getPhases() {
		ArrayList<Phase> array = new ArrayList<Phase>();
		// 锁定
		super.lockMulti();
		try {
			for (Phase e : projects.keySet()) {
				array.add(e.duplicate());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 取全部用户签名。
	 * 这些签名与所在站点的资源引用对应！
	 * 
	 * @return 用户签名集合
	 */
	public List<Siger> getIssuers() {
		TreeSet<Siger> a = new TreeSet<Siger>();
		// 锁定
		super.lockMulti();
		try {
			for (Phase e : projects.keySet()) {
				if (e.isUserLevel()) {
					a.add(e.getIssuer());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return new ArrayList<Siger>(a);
	}

	/**
	 * 查找匹配的阶段命名
	 * @param issuer 用户签名
	 * @return 阶段命名数组，没有是空集合
	 */
	public List<Phase> findPhases(Siger issuer) {
		ArrayList<Phase> array = new ArrayList<Phase>();
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<Phase, TaskProject>> iterator = projects.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Phase, TaskProject> entry = iterator.next();
				Phase e = entry.getKey();
				// 判断是用户组件，且签名一致
				if (e.isUserLevel()) {
					if (Laxkit.compareTo(issuer, e.getIssuer()) == 0) {
						array.add(e.duplicate());
					}
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
	 * 删除已经成为过期的动态链接库和它的父目录
	 * @param filename 链接库全路径名称
	 * @return 删除成功返回真，否则假
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

		// 删除父目录
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
	 * 释放动态链接库。<br><br>
	 * 
	 * 分两种：<br>
	 * 1. 如果是WINDOWS版本，调用“freeLoadLibrary”方法去操作ClassLoader内部方法释放，这时会调用内部本地函数<br>
	 * 2. 如果是LINUX，调用“freeLoadLibrary”会造成JVM崩溃，所以做个变通，使用“unregisterNativeLibrary”，只释放ClassLoader注册的库路径（注意！库路径只是字符串）<br>
	 * 
	 * @param loader 类加载器
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
			
			// 删除已经成为垃圾的动态链接库文件
			boolean success = deleteRubbishLibrary(path);

			// 如果不成功，保存，后面由线程来重新尝试清除
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
	 * 删除一个签名成员的全部记录
	 * 注意！这是一个本地内部调用方法，不要加锁！！！
	 * 
	 * @param issuer 发布者用户签名
	 * @return 返回删除的类加载器数目
	 */
	private int remove(Siger issuer) {
		// 工作组件部件
		TaskPart part = new TaskPart(issuer, getFamily());

		TreeSet<Phase> phases = new TreeSet<Phase>();
		TreeSet<TaskSection> sections = new TreeSet<TaskSection>();
		int count = 0;

		// 不论系统组件/用户组件，只比较签名，找到一致的
		for (Phase phase : projects.keySet()) {
			boolean success = (Laxkit.compareTo(issuer, phase.getIssuer()) == 0);
			if (success) {
				phases.add(phase);
				// 工作区
				TaskSection e = new TaskSection(part, phase.getSock().getWare());
				sections.add(e);
			}
		}
		// 删除阶段命名
		for (Phase phase : phases) {
			projects.remove(phase);
		}
		// 删除类加载器
		for (TaskSection section : sections) {
			HotClassLoader e = loaders.remove(section);
			// 类加载器存在，加1
			if (e != null) {
				freeRubbishLibraries(e);
				count++;
			}
		}

		Logger.debug(this, "remove", count > 0, "phase size: %d, task-section: %d, class-loader size: %d",
				phases.size(), sections.size(), count);

		return count;
	}

	/**
	 * 删除指定账号下的全部阶段命名和类加载器
	 * @param issuer 组件发布者的账号签名
	 * @return 返回删除的类加载器数目（也是应用软件数目）
	 */
	public boolean drop(Siger issuer) {
		int count = 0;

		// 锁定后删除
		super.lockSingle();
		try {
			count = remove(issuer);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return count>0;
	}

	/**
	 * 删除一个用户的某一个分布应用
	 * @param issuer 用户签名
	 * @param ware 软件名称
	 * @return 返回被删除的类加载器数目（也是分布应用的数目）
	 */
	public boolean drop(Siger issuer, Naming ware) {
		// 软件名不允许空指针！
		Laxkit.nullabled(ware);

		TreeSet<Phase> phases = new TreeSet<Phase>();
		TreeSet<TaskSection> sections = new TreeSet<TaskSection>();
		int count = 0;

		// 锁定
		super.lockSingle();
		try {
			for (Phase phase : projects.keySet()) {
				// 签名和软件名一致!
				boolean success = (Laxkit.compareTo(issuer, phase.getIssuer()) == 0 && 
						Laxkit.compareTo(phase.getWare(), ware) == 0);
				// 以上条件成立，保存它
				if (success) {
					phases.add(phase);
					// 工作区
					TaskSection e = new TaskSection(issuer, phase.getFamily(), ware);
					sections.add(e);
				}
			}
			// 删除阶段命名
			for (Phase phase : phases) {
				projects.remove(phase);
			}
			// 删除类加载器
			for (TaskSection section : sections) {
				HotClassLoader e = loaders.remove(section);
				if (e != null) {
					freeRubbishLibraries(e);
					count++;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		boolean success = (count>0);

		Logger.note(this, "drop", success, "phase size: %d, task-section size: %d, class loader size: %d",
				phases.size(), sections.size(), count);

		// 删除的类加载器数目
		return success;
	}

	/**
	 * 根据阶段命名查询项目配置
	 * @param phase 阶段命名
	 * @return 返回Project实例
	 */
	private TaskProject findPrivate(Phase phase) {
		super.lockMulti();
		try {
			if (phase != null) {
				return projects.get(phase);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 在忽略安全检查前提下，根据项目命名判断项目存在且是系统级
	 * @param root 项目命名
	 * @return 条件成功返回“真”，否则“假”。 
	 */
	public boolean isSystemLevel(Sock root) {
		Phase phase = new Phase(family, root);
		TaskProject e = findPrivate(phase);
		return (e != null && e.isSystemLevel());
	}

	/**
	 * 在忽略安全检查前提下，根据项目命名判断项目存在且是用户级
	 * @param root 项目命名
	 * @return 条件成功返回“真”，否则“假”。 
	 */
	public boolean isUserLevel(Sock root) {
		Phase phase = new Phase(family, root);
		TaskProject e = findPrivate(phase);
		return (e != null && e.isUserLevel());
	}

	/**
	 * 根据阶段命名返回它的项目配置。
	 * 阶段命名进行阶段类型和持有人安全检查，如果阶段类型不一致时，弹出IllegalPhaseException异常；如果用户名不一致，返回空值
	 * @param phase 阶段命名
	 * @return 成功返回项目配置，否则是空指针
	 * @throws NullPointerException, IllegalPhaseException
	 */
	public TaskProject findProject(Phase phase) {
		// 判断是空指针
		Laxkit.nullabled(phase);

		// 判断阶段类型与管理池类型不匹配，弹出异常
		if (phase.getFamily() != getFamily()) {
			throw new IllegalPhaseException("illegal %s", phase);
		}

		TaskProject project = findPrivate(phase);
		// 判断项目存在 
		boolean success = (project != null);
		// 阶段命名安全检查
		if (success) {
			success = validate(project, phase);
		}
		return (success ? project : null);
	}

	/**
	 * 判断阶段命名存在，判断过程包括签名安全检查。<br>
	 * 如果阶段类型不一致，弹出IllegalPhaseException异常。如果签名不同，返回“假”。
	 * @param phase 阶段命名
	 * @return 匹配返回“真”，否则“假”。
	 * @throws NullPointerException, IllegalPhaseException
	 */
	public boolean contains(Phase phase) {
		return findProject(phase) != null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TaskReader#readResource(long, com.laxcus.util.naming.Naming, java.lang.String)
	 */
	@Override
	public byte[] readResource(long invokerId, Naming ware, String name) throws TaskException {
		//		System.out.printf("invokerId:%d, ware:%s, class-name:%s\n",  invokerId, ware, name);

		// 1. 找到异常调用器
		InvokerPool pool = getLauncher().getInvokerPool();
		EchoInvoker invoker = pool.findInvoker(invokerId);
		if (invoker == null) {
			throw new TaskSecurityException("cannot be find invoker, %d", invokerId);
		}
		// 2. 找到签名
		Siger issuer = invoker.getIssuer();
		// System.out.printf("issuer %s\n", issuer);
		if (issuer == null) {
			throw new TaskSecurityException("cannot be find issuer, %d", invokerId);
		}

		// 任务区
		TaskSection section = new TaskSection(issuer, getFamily(), ware);
		// 查找类加载器
		HotClassLoader loader = findClassLoader(section);
		if (loader != null) {
			try {
				return loader.readResource(issuer, name);
				//				byte[] b = loader.readResource(issuer, name);
				//				System.out.printf("read stream:%d\n" , (b == null ? -1 : b.length));
				//				return b;
			} catch (Throwable e) {
				throw new TaskException(e);
			}
		} else {
			throw new TaskNotFoundException("not found HotClassLoader! %s # %s", section, name);
		}
	}

	/**
	 * 项目和阶段命名的签名安全检查。<br>
	 * 如果项目是系统级组件，适合所有请求人，返回“真”；<br>
	 * 如果项目是用户级组件，判断签名一致。<br>
	 * 
	 * @param project 项目
	 * @param phase 阶段命名
	 * @return 有效返回“真”，否则“假”。
	 */
	private boolean validate(TaskProject project, Phase phase) {
		// 系统级组件适合所有人
		if (project.isSystemLevel()) {
			return true;
		}
		// 判断签名，一致有效，否则无效
		Siger issuer = project.getIssuer();
		return Laxkit.compareTo(issuer, phase.getIssuer()) == 0;
	}

	/**
	 * 根据工作部件查找类加载器
	 * @param section 工作部件
	 * @return HotClasLoader实例
	 */
	private HotClassLoader findClassLoader(TaskSection section) {
		super.lockMulti();
		try {
			return loaders.get(section);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据阶段命名建立一个任务实例。<br>
	 * 每个进入的阶段命名都进行阶段类型和持有人安全检查。
	 * 阶段类型不匹配的签名将弹出IllegalPhaseException异常。持有人检查中，系统级组件被忽略，不匹配将返回空指针。
	 * 
	 * @param phase 阶段命名
	 * @return 成功返回任务实例，没有返回空指针。
	 * @throws NullPointerException, IllegalPhaseException
	 */
	protected DistributedTask createTask(Phase phase) {
		// 判断是空指针
		Laxkit.nullabled(phase);

		// 判断阶段类型与管理池类型不匹配，弹出异常
		if (phase.getFamily() != getFamily()) {
			throw new IllegalPhaseException("illegal phase: %s", phase);
		}

		// 查找项目
		TaskProject project = findPrivate(phase);
		// 判断项目有效
		if (project == null) {
			Logger.error(this, "createTask", "cannot be find TaskProject! %s", phase);
			return null;
		}
		// 判断签名一致的安全检查
		boolean success = validate(project, phase);
		if (!success) {
			Logger.error(this, "createTask", "cannot be accepted! %s != %s", project, phase);
			return null;
		}

		// 找到关联的类加载器
		TaskSection section = new TaskSection(project.getIssuer(), getFamily(), phase.getWare());
		HotClassLoader loader = findClassLoader(section);
		if (loader == null) {
			Logger.error(this, "createTask", "cannot be find HotClassLoader! %s", section);
			return null;
		}

		try {
			// 取出类实例，生成分布组件实例输出
			String clazzName = project.getTaskClass();
			Logger.debug(this, "createTask", "class is %s", clazzName);

			// 找到类定义
			Class<?> clazz = Class.forName(clazzName, true, loader);
			// 生成类实例
			DistributedTask task = (DistributedTask) clazz.newInstance();
			// 设置任务管理项目
			task.setProject(project);
			// 设置资源读取接口
			task.setPrivateReader(this);
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
	 * 读取DTC/JAR文件内容，生成应用条目。<br>
	 * 条目内容：用户签名、磁盘文件路径、磁盘文件URL、磁盘内容。<br><br>
	 * 
	 * @param issuer 发布者签名
	 * @param key 文件指针
	 * @return 返回实例
	 */
	private HotClassEntry createEntry(Siger issuer, FileKey key) {
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
		return new HotClassEntry(issuer, path, url, b);
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
	
	/**
	 * 清除全部记录，包括动态链接库
	 * 注意！不加锁处理！
	 */
	protected void clear() {
		// 清除动态链接库
		Iterator<Map.Entry<TaskSection, HotClassLoader>> iterator = loaders.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<TaskSection, HotClassLoader> entry = iterator.next();
			HotClassLoader value = entry.getValue();
			freeRubbishLibraries(value);
		}
		
		// 释放
		loaders.clear();
		// 清除项目
		projects.clear();
	}

	/**
	 * 从输入参数中解析命名，建立阶段命名
	 * @param input 命名描述
	 * @return Phase实例
	 */
	private Phase createPhase(Naming ware, String input) {
		try {
			Tock tock = new Tock(ware, input);
			return new Phase(getFamily(), tock.getSock(), tock.getSub());
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return null;
	}

	/**
	 * 生成一个新的类加载器
	 * @param group 分布任务组件集
	 * @return  返回HotClassLoader实例
	 */
	private HotClassLoader createClassLoader(TaskElement element) {
		Siger issuer = element.getIssuer();

		ArrayList<HotClassEntry> array = new ArrayList<HotClassEntry>();
		// 生成辅助参数
		TaskElementBoot boot = element.getBoot();
		String path = boot.getPath();

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
		HotClassEntry entry = new HotClassEntry(issuer, path, bootURL, boot.getContent());
		array.add(entry);

		Logger.debug(this, "createClassLoader", "%s packet element count %d", path, array.size());

		// 加载JAR辅助文件
		List<FileKey> jars = element.getJARs();
		for (int index = 0; index < jars.size(); index++) {
			// 生成JAR辅助条目
			FileKey key = jars.get(index);
			HotClassEntry sub = createEntry(issuer, key);
			if (sub == null) {
				Logger.error(this, "createClassLoader", "cannot be load %s", key.getPath());
				return null;
			}
			array.add(sub);

			Logger.info(this, "createClassLoader", "load jar %s", key.getPath());
		}

		Logger.debug(this, "createClassLoader", "dtc and jar packets size %d", array.size());

		// 返回一个新的类加载器，包括组件包和J辅助JAR包
		HotClassLoader loader = new HotClassLoader(array);

		// 保存动态链接库到类加载器
		List<FileKey> libraries = element.getLibraries();
		for (int index = 0; index < libraries.size(); index++) {
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
	private void loadLibrary(TaskProject project, HotClassLoader loader)
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		// 取出动态链接库
		String[] paths = loader.getLibraries();
		int size = (paths == null ? 0 : paths.length);
		if (size < 1) {
			return;
		}
		
		// 注意，这里清除掉垃圾文件

		// 取出类实例，生成分布组件实例输出
		String clazzName = project.getTaskClass();

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
	 * @param boot 分布任务组件档案文件
	 * @param loader 类加载器
	 * @return 返回任务管理项目集合，失败是空指针
	 */
	private Map<Phase, TaskProject> createProjects(TaskElement parent, HotClassLoader loader) {
		// 组件实例
		TreeMap<Phase, TaskProject> array = new TreeMap<Phase, TaskProject>();

		// 取出包中的“TASK-INF/tasks.xml”标签
		TaskElementBoot item = parent.getBoot();
		byte[] taskText = item.getTaskText();
		// 没有找到，警告！忽略！
		if (Laxkit.isEmpty(taskText)) {
			Logger.error(this, "createProjects", "cannot be find %s", TF.TASK_INF); // TaskBootItem.TAG);
			return null;
		}

		TaskConfigReader reader = new TaskConfigReader(taskText);
		TaskPart part = reader.readTaskPart();
		WareTag wareTag = reader.readWareTag();
		if (part == null || wareTag == null) {
			Logger.error(this, "createProjects", "cannot be find %s", TF.TASK_INF);
			return null;
		}
		// 改成拥有人的签名
		part.setIssuer(parent.getIssuer());

		List<TaskToken> tokens = reader.readTaskTokens();
		// 判断失败!
		if (tokens == null || tokens.isEmpty()) {
			return null;
		}

		boolean loadLibrary = false;
		for(TaskToken token : tokens) {
			// 解析命名参数，建立阶段命名
			Phase phase = createPhase(wareTag.getNaming(), token.getNaming());
			// 不成立，忽略
			if (phase == null) {
				continue;
			}
			// 项目持有人
			phase.setIssuer(part.getIssuer());

			// 正确情况:
			// 1. 用户组件，一定有用户签名
			// 2. 系统组件，一定不能有用户签名
			// 错误情况：
			// 1. 用户组件，没有定义用户签名
			// 2. 系统组件，定义了不应该定义的用户签名
			// 基于以上述情况，判断合法
			boolean allow = ((phase.isUserLevel() && phase.getIssuer() != null)
					|| (phase.isSystemLevel() && phase.getIssuer() == null));
			if (!allow) {
				Logger.error(this, "createProjects", "illegal phase: %s", phase);
				continue;
			}

			try {
				Class<?> clazz = Class.forName(token.getProjectClass(), true, loader);
				TaskProject jet = (TaskProject) clazz.newInstance();
				// 项目持有人
				jet.setIssuer(part.getIssuer());
				// 阶段命名
				jet.setPhase(phase);
				// 版本号
				jet.setVersion(wareTag.getVersion());
				// 项目类实例
				jet.setTaskClass(token.getBootClass());
				// 资源
				jet.setResource(token.getResource());

				// 加载动态链接库，只加载一次。注意，这个很重要！！！
				if (!loadLibrary) {
					loadLibrary(jet, loader);
					loadLibrary = true;
				}

				// 保存命名项目
				array.put(phase, jet);

				Logger.info(this, "createProjects", "%s", jet);
			} catch (ClassNotFoundException e) {
				Logger.error(e);
			} catch (InstantiationException e) {
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
			} catch (Exception e) {
				Logger.error(e);
			}
		}

		return array;
	}

	/**
	 * 保留一批库文件
	 */
	private void keepTaskLibrary(TaskGroup group) {
		for (TaskElement element : group.list()) {
			TaskSection section = element.getSection();
			HotClassLoader loader = loaders.get(section);
			if (loader == null) {
				continue;
			}
			
			List<FileKey> libraries = element.getLibraries();
			for (int index = 0; index < libraries.size(); index++) {
				FileKey key = libraries.get(index);
				boolean success = loader.removeLibrary(key.getPath());

				Logger.debug(this, "keepTaskLibrary", success, "keep link library %s", key.getPath());
			}
		}
	}

	/**
	 * 加载一个注册用户在本地的全部应用软件包。<br>
	 * 一个注册用户允许有任意多个应用软件，相对的是多个类加载器（HotClassLoader）和多个阶段命名。
	 * 解析阶段命名过程，如果有任意的配置错误，将忽略这个故障应用。
	 * 
	 * @param documents 分布任务组件组集
	 * @return 任何一个应用包加载成功返回真，全部失败返回假。
	 */
	protected boolean loadTask(TaskGroup group) {
		// 保留库
		keepTaskLibrary(group);

		// 清除旧的记录
		Siger issuer = group.getIssuer();
		remove(issuer);

		// 应用软件逐一解析和加载
		int count = 0;
		for (TaskElement element : group.list()) {
			// 1. 给组件分配类加载器，不成功忽略!
			HotClassLoader loader = createClassLoader(element);
			if (loader == null) {
				Logger.error(this, "loadTask", "cannot be create HotClassLoader! %s", element.getSection());
				continue;
			}
			// 2. 生成分布任务项目，不成功忽略！
			Map<Phase, TaskProject> jets = createProjects(element, loader);
			if (jets == null || jets.isEmpty()) {
				Logger.error(this, "loadTask", "cannot be create TaskProject! %s", element.getSection());
				continue;
			}

			// 保存两个实例
			loaders.put(element.getSection(), loader);
			projects.putAll(jets);

			Logger.debug(this, "loadTask", "Load %s, HotClassEntry count %d", element.getSection(), loader.size());
			// 成功，统计数加1
			count++;
		}

		// 判断成功，打印结果
		if (group.size() == count) {
			Logger.info(this, "loadTask", "load %s - %d, all successful!", issuer, count);
		} else {
			Logger.warning(this, "loadTask", "load %s, successful %d, failed %d", 
					issuer, count, group.size() - count);
		}

		return count > 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 释放
		loaders.clear();
		// 清除项目
		projects.clear();
	}
}


///**
// * 释放动态链接库
// * @param loader
// */
//private void freeLibraries(HotClassLoader loader) {
//	try {
//		String[] paths = loader.getLibraries();
//		int size = (paths == null ? 0 : paths.length);
//		for (int i = 0; i < size; i++) {
//			String path = paths[i];
//			Logger.info(this, "freeLibraries", "release %s", path);
//			loader.freeLoadLibrary(path);
//		}
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
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	}
//}

///**
// * 释放动态链接库
// * @param loader
// * @param delete 是不是删除
// */
//private void freeLibraries(HotClassLoader loader, boolean delete) {
//	String[] paths = loader.getLibraries();
//	int size = (paths == null ? 0 : paths.length);
//	for (int i = 0; i < size; i++) {
//		String path = paths[i];
//		
////		// 先删除库...
////		try {
////			loader.freeLoadLibrary(path);
////		} catch (SecurityException e) {
////			Logger.error(e);
////		} catch (IllegalArgumentException e) {
////			Logger.error(e);
////		} catch (NoSuchFieldException e) {
////			Logger.error(e);
////		} catch (IllegalAccessException e) {
////			Logger.error(e);
////		} catch (NoSuchMethodException e) {
////			Logger.error(e);
////		} catch (InvocationTargetException e) {
////			Logger.error(e);
////		} catch (Throwable e) {
////			Logger.fatal(e);
////		}
//		
//		// 删除磁盘文件
//		if (delete) {
//			boolean success = deleteLibrary(path);
//			Logger.note(this, "freeLibraries", success, "delete %s", path);
//			
//			// 不成功，继续删除
//			if (!success) {
//				rubbishs.add(path);
//			}
//		}
//		Logger.info(this, "freeLibraries", "release %s", path);
//	}
//}


///**
// * 获得释放垃圾链接库的超时时间
// * @return 返回以毫秒为单位的时间
// */
//protected long getFreeLibraryTimeout() {
//	String input = System.getProperty("laxcus.timeout.free.tasklib");
//	return ConfigParser.splitTime(input, 30000L);
//}


//	/**
//	 * 删除指定账号下的全部阶段命名和类加载器
//	 * @param issuer 组件发布者的账号签名
//	 * @return 返回删除的类加载器数目（也是应用软件数目）
//	 */
//	public int drop(Siger issuer) {
//		// 工作组件部件
//		TaskPart part = new TaskPart(issuer, getFamily());
//
//		TreeSet<Phase> phases = new TreeSet<Phase>();
//		TreeSet<TaskSection> sections = new TreeSet<TaskSection>();
//		int count = 0;
//
//		// 锁定
//		super.lockSingle();
//		try {
//			for (Phase phase : projects.keySet()) {
//				// 不论系统组件/用户组件，只比较签名
//				boolean success = (Laxkit.compareTo(issuer, phase.getIssuer()) == 0);
//				if (success) {
//					phases.add(phase);
//
//					// 工作区
//					TaskSection e = new TaskSection(part, phase.getSock().getWare());
//					sections.add(e);
//				}
//			}
//			// 删除阶段命名
//			for (Phase phase : phases) {
//				projects.remove(phase);
//			}
//			// 删除类加载器
//			for (TaskSection section : sections) {
//				HotClassLoader e = loaders.remove(section);
//				// 类加载器存在，加1
//				if(e != null) {
//					count++;
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		Logger.note(this, "drop", count > 0, "phase size: %d, task-section: %d, class-loader size: %d",
//				phases.size(), sections.size(), count);
//
//		return count;
//	}

//		Document document = XMLocal.loadXMLSource(taskText);
//		if (document == null) {
//			return null;
//		}
//
//		// 解析单项
//		NodeList nodes = document.getElementsByTagName(TaskMark.TASK);
//		int size = nodes.getLength();
//		for (int i = 0; i < size; i++) {
//			Element element = (Element) nodes.item(i);
//
//			// 任务命名，包括根命名和子命名两部分，中间由逗号隔开
//			String naming = XMLocal.getValue(element, TaskMark.TASK_NAMING); // "naming");
//			// 任务类路径
//			String task_class = XMLocal.getValue(element, TaskMark.TASK_CLASS); // "class");
//			// 资源配置(任意字符串格式.具体由用户的Project子类解释)
//			String resource = XMLocal.getValue(element, TaskMark.TASK_RESOURCE); // "resource");
//			// 项目类路径(从Project.class派生)
//			String project_class = XMLocal.getValue(element, TaskMark.TASK_PROJECT_CLASS); // "project-class");
//			// 解析命名参数，建立阶段命名
//			Phase phase = createPhase(tag.getNaming(), naming);
//			// 项目持有人
//			phase.setIssuer(part.getIssuer());
//
//			// 正确情况:
//			// 1. 用户组件，一定有用户签名
//			// 2. 系统组件，一定不能有用户签名
//			// 错误情况：
//			// 1. 用户组件，没有定义用户签名
//			// 2. 系统组件，定义了不应该定义的用户签名
//			// 基于以上述情况，判断合法
//			boolean allow = ((phase.isUserLevel() && phase.getIssuer() != null)
//					|| (phase.isSystemLevel() && phase.getIssuer() == null));
//			if (!allow) {
//				Logger.error(this, "createProjects", "illegal phase: %s", phase);
//				continue;
//			}
//
//			try {
//				Class<?> clazz = Class.forName(project_class, true, loader);
//				TaskProject jet = (TaskProject) clazz.newInstance();
//				// 项目持有人
//				jet.setIssuer(part.getIssuer());
//				// 阶段命名
//				jet.setPhase(phase);
//				// 版本号
//				jet.setVersion(tag.getVersion());
//				// 项目类实例
//				jet.setTaskClass(task_class);
//				// 资源
//				jet.setResource(resource);
//				// 保存命名项目
//				array.put(phase, jet);
//
//				Logger.info(this, "createProjects", "%s", jet);
//			} catch (ClassNotFoundException e) {
//				Logger.error(e);
//			} catch (InstantiationException e) {
//				Logger.error(e);
//			} catch (IllegalAccessException e) {
//				Logger.error(e);
//			}
//		}


//	/**
//	 * 生成关联的任务管理项目
//	 * @param boot 分布任务组件档案文件
//	 * @param loader 类加载器
//	 * @return 返回任务管理项目集合，失败是空指针
//	 */
//	private Map<Phase, TaskProject> createProjects(TaskElement parent, HotClassLoader loader) {
//		// 组件实例
//		TreeMap<Phase, TaskProject> array = new TreeMap<Phase, TaskProject>();
//
//		// 取出包中的“TASK-INF/tasks.xml”标签
//		TaskElementBoot item = parent.getBoot();
//		byte[] taskText = item.getTaskText();
//		// 没有找到，警告！忽略！
//		if (Laxkit.isEmpty(taskText)) {
//			Logger.error(this, "createProjects", "cannot be find %s", TF.TASK_INF); // TaskBootItem.TAG);
//			return null;
//		}
//
//		TaskConfigReader reader = new TaskConfigReader(taskText);
//		TaskPart part = reader.readTaskPart();
//		WareTag tag = reader.readWareTag();
//		if(part == null || tag == null) {
//			Logger.error(this, "createProjects", "cannot be find %s", TF.TASK_INF); // TaskBootItem.TAG);
//			return null;
//		}
//		// 改成拥有人的签名
//		part.setIssuer(parent.getIssuer());
//
//		Document document = XMLocal.loadXMLSource(taskText);
//		if (document == null) {
//			return null;
//		}
//
//		// 解析单项
//		NodeList nodes = document.getElementsByTagName(TaskMark.TASK);
//		int size = nodes.getLength();
//		for (int i = 0; i < size; i++) {
//			Element element = (Element) nodes.item(i);
//
//			// 任务命名，包括根命名和子命名两部分，中间由逗号隔开
//			String naming = XMLocal.getValue(element, TaskMark.TASK_NAMING); // "naming");
//			// 任务类路径
//			String task_class = XMLocal.getValue(element, TaskMark.TASK_CLASS); // "class");
//			// 资源配置(任意字符串格式.具体由用户的Project子类解释)
//			String resource = XMLocal.getValue(element, TaskMark.TASK_RESOURCE); // "resource");
//			// 项目类路径(从Project.class派生)
//			String project_class = XMLocal.getValue(element, TaskMark.TASK_PROJECT_CLASS); // "project-class");
//			// 解析命名参数，建立阶段命名
//			Phase phase = createPhase(tag.getNaming(), naming);
//			// 项目持有人
//			phase.setIssuer(part.getIssuer());
//
//			// 正确情况:
//			// 1. 用户组件，一定有用户签名
//			// 2. 系统组件，一定不能有用户签名
//			// 错误情况：
//			// 1. 用户组件，没有定义用户签名
//			// 2. 系统组件，定义了不应该定义的用户签名
//			// 基于以上述情况，判断合法
//			boolean allow = ((phase.isUserLevel() && phase.getIssuer() != null)
//					|| (phase.isSystemLevel() && phase.getIssuer() == null));
//			if (!allow) {
//				Logger.error(this, "createProjects", "illegal phase: %s", phase);
//				continue;
//			}
//
//			try {
//				Class<?> clazz = Class.forName(project_class, true, loader);
//				TaskProject jet = (TaskProject) clazz.newInstance();
//				// 项目持有人
//				jet.setIssuer(part.getIssuer());
//				// 阶段命名
//				jet.setPhase(phase);
//				// 版本号
//				jet.setVersion(tag.getVersion());
//				// 项目类实例
//				jet.setTaskClass(task_class);
//				// 资源
//				jet.setResource(resource);
//				// 保存命名项目
//				array.put(phase, jet);
//
//				Logger.info(this, "createProjects", "%s", jet);
//			} catch (ClassNotFoundException e) {
//				Logger.error(e);
//			} catch (InstantiationException e) {
//				Logger.error(e);
//			} catch (IllegalAccessException e) {
//				Logger.error(e);
//			}
//		}
//
//		return array;
//	}

//	/**
//	 * 使用传入的分布组件组集合，更新类加载器、阶段命名映像。<br>
//	 * 每个账号有一个对应的类加载器（HotClassLoader），阶段命名映像集中在一起
//	 * 在解析阶段命名过程中，如果因为“tasks.xml”配置存在错误，将忽略这个阶段命名。
//	 * 
//	 * @param documents 分布任务组件组集
//	 */
//	protected void updateTasks(Collection<TaskGroup> documents) {
//		// 清除全部旧记录
//		clear();
//
//		// 如果发生错误，忽略整个分布任务组件群（就是说，忽略这个用户的全部应用！）
//		for (TaskGroup document : documents) {
//			for(TaskElement element: document.list()) {
//				// 1. 给组件分配类加载器
//				HotClassLoader loader = createClassLoader(element);
//				if (loader == null) {
//					Logger.error(this, "updateTasks", "cannot be create HotClassLoader! %s", element.getSection());
//					continue;
//				}
//				// 2. 生成分布任务项目
//				Map<Phase, TaskProject> jets = createProjects(element, loader);
//				if (jets == null || jets.isEmpty()) {
//					Logger.error(this, "updateTasks", "cannot be create TaskProject! %s", element.getSection());
//					continue;
//				}
//				// 保存两个实例
//				loaders.put(element.getSection(), loader);
//				projects.putAll(jets);
//
//				Logger.debug(this, "updateTasks", "Load %s, HotClassEntry count %d", element.getSection(), loader.size());
//			}
//		}
//
//		Logger.debug(this, "updateTasks", "all ClassLoader size:%d, all TaskProject size:%d", 
//				loaders.size(), projects.size());
//	}
