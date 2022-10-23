/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 分布式任务组件 <br><br>
 * 
 * 分布式任务组件是“CONDUCT/ESTABLISH”阶段任务的基础类，它们的阶段任务都从这里派生。<br>
 * 系统按照分布任务不同的属性要求，定义了各自的规则。用户应该遵守接口规则，然后加入自己的业务代码，编译打包后生成程序文件，这个文件称为“分布式任务组件”。<br>
 * LAXCUS系统支持分布式任务组件的冷发布和热发布两个模式。<br><br>
 * 
 * 分布式任务组件分为系统级和用户级。它们部署在ARCHIVE服务器上，被统一分配和调用。 <br>
 * 分布式任务组件以文件的格式保存在ARCHIVE服务器上（PUT/END阶段组件除外），以“.DTC”后缀结尾。
 * 分布式任务组件文件必须包含一个账号下的某个阶段的全部组件。如“demo”账号下的“ISSUE”阶段组件，必须全部打包在一起。<br><br>
 * 
 * 每个文件包中必有一个“TASK-INF”目录，下面有一个“tasks.xml”文件，
 * “tasks.xml”文件注明这个包中的所有分布组件，所有对组件的判断和调用都从这个文件开始。<br><br>
 * 
 * tasks.xml文档格式：<br><br>
 * 
 * 题头（声明组件拥有人和阶段类型，每个文件只有一个）：<br>
 * &ltsign&gt 013271A04A1FDE715827FD702F252FF97C500CEC &lt/sign&gt <br>
 * &ltphase&gt ISSUE &lt/phase&gt <br>
 * &ltlogo&gt /com/laxcus/....../logo.png &lt/logo&gt <br>
 * <br>
 * 
 * 组件单元（文件中重复多组）：<br>
 * &lttask&gt  <br>
 * 	&ltnaming&gt any word (only one) &lt/naming&gt <br>
 *  &ltclass&gt org.xxx.xxx.EngineTask &lt/class&gt <br>
 *  &ltresource&gt schema or table or filename or other &lt/resource&gt  <br>
 * 	&ltproject-class&gt org.xxx.xxx.EngineProject &lt/project-class&gt  <br>
 *  &ltversion&gt 1.0 &lt/version&gt <br>
 * &lt/task&gt <br><br>
 * 
 * <br><br>
 * 
 * 说明：<br>
 * <1> sign，分布组件的持有人签名，是40个16进制字符的SHA256编码。如果是系统级组件，此项忽略。<br>
 * <2> phase，阶段类型关键字。文本描述见PhaseTag中的定义。<br>
 * <3> logo，标记图标。是一个资源路径名，图标固定保存在分布式任务组件指定的一个目录里。<br>
 * 
 * <4> naming，分布组件名称，在运行系统中唯一，且必须唯一，否则会引起调用混乱。格式由ASCII码的"字母、数字、下划线"组成，其它非法，忽略大小写，字符长度不限。<br>
 * <5> class，任务实现类，必须从DistributeTask类派生。<br> 
 * <6> resource，用户自定义的资源(文本格式)，具体由用户去解析。包含用户定义的任何参数。<br>
 * <7> project-class，项目管理类，必须从TaskProject类派生。<br>
 * <8> version, 版本号。不同的版本号有各自方法接口，方便外部调用判断。如果没有定义，默认是1.0版本。一个命名组件的版本号要保持一致。<br>
 * 
 * @author soctt.liang
 * @version 1.2 12/19/2012
 * @since laxcus 1.0
 */
public class DistributedTask {

	/** 任务所属项目，在TaskPool生成任务实例时指派 **/
	private TaskProject project;

	/** 分布式任务组件私有资源读取接口 **/
	private TaskReader reader;

	/** CONDUCT/ESTABLISH/CONTACT/其它命令，在任务接口实例生成后被调用端分配 **/
	private Command command;

	/** 异步调用器编号，通过编号，与异步调用器实现关联 **/
	private long invokerId;

	/**
	 * 构造任务基础接口
	 */
	protected DistributedTask() {
		super();
		// 默认是无效编号
		invokerId = InvokerIdentity.INVALID;

		//		// 检查和安装动态链接库
		//		checkAndLoadLibraries0();
	}

//	/**
//	 * 检查和安装动态链接库
//	 */
//	protected void checkAndLoadLibraries0() {
//		try {
//			loadLibraries();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		} catch (UnsatisfiedLinkError e) {
//			e.printStackTrace();
//		} catch (NoSuchFieldException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * 调用方法："Runtime.load0"，采用引用方式加载全部动态链接库
//	 * 
//	 * @throws SecurityException
//	 * @throws IllegalArgumentException
//	 * @throws NoSuchFieldException
//	 * @throws IllegalAccessException
//	 * @throws NoSuchMethodException
//	 * @throws InvocationTargetException
//	 */
//	protected void loadLibraries() throws SecurityException,
//			IllegalArgumentException, NoSuchFieldException,
//			IllegalAccessException, NoSuchMethodException,
//			InvocationTargetException {
//
//		ClassLoader c = getClass().getClassLoader();
//		// 判断是动态库类加载器
//		if (!Laxkit.isClassFrom(c, LibraryClassLoader.class)) {
//			return;
//		}
//
//		LibraryClassLoader loader = (LibraryClassLoader) c;
//		// 1. 没有预存储动态链接库，不用加载
//		String[] paths = loader.getLibraries();
//		int size = (paths == null ? 0 : paths.length);
//		if (size < 1) {
//			return;
//		}
//		// 2. 如果动态库已经全部加载，忽略它！
//		if (loader.isLoadedLibraries()) {
//			return;
//		}
//		// 3. 调用Runtime.load0方法，去加载动态链接库
//		Runtime rt = Runtime.getRuntime();
//		Method load0 = rt.getClass().getDeclaredMethod("load0", new Class[] { Class.class, String.class });
//		load0.setAccessible(true);
//		// 4. 采用引用方式调用
//		// 这个最关键!!!是子类实例。如果用System.load(String filename),System调用getCallerClass()方法，会指向其它类，导致类加载器位置出错。
//		Class<?> clazz = getClass(); 
//		for (int i = 0; i < size; i++) {
//			String filename = paths[i];
//			Object[] objects = new Object[] { clazz, filename };
//			load0.invoke(rt, objects);
//		}
//	}

	//	/**
	//	 * 加载全部动态链接库
	//	 * @throws IllegalAccessException 
	//	 * @throws NoSuchFieldException 
	//	 * @throws IllegalArgumentException 
	//	 * @throws SecurityException 
	//	 */
	//	protected void loadLibraries() throws SecurityException,
	//			IllegalArgumentException, NoSuchFieldException,
	//			IllegalAccessException, NullPointerException, UnsatisfiedLinkError {
	//
	//		ClassLoader c = getClass().getClassLoader();
	//		// 判断是动态库类加载器
	//		if (!Laxkit.isClassFrom(c, LibraryClassLoader.class)) {
	//			return;
	//		}
	//
	//		LibraryClassLoader loader = (LibraryClassLoader) c;
	//		// 1. 没有预存储动态链接库，不用加载
	//		if (!loader.hasLibraries()) {
	//			return;
	//		}
	//		// 2. 如果动态库已经全部加载，忽略它！
	//		if (loader.isLoadedLibraries()) {
	//			return;
	//		}
	//		// 2. 取出动态链接库，加载它！注意：只能在用户对象中使用“System.load”加载动态链接库，而不是在LibraryClassLoader中加载！
	//		String[] paths = loader.getLibraries();
	//		System.out.printf("HI, COUNT ALL LIBRARIES %d\n", paths.length);
	//		for (String path : paths) {
	//			// 以全路径格式加载动态链接库到“LibraryClassLoader”
	//			System.load(path);
	//			
	//			System.out.printf("HI, LOAD LIBRARY %s\n", path);
	//		}
	//	}


	/**
	 * 设置任务管理项目
	 * @param e 任务管理项目实例
	 */
	protected void setProject(TaskProject e) {
		project = e;
	}

	/**
	 * 返回任务项目配置
	 * @return 任务管理项目实例
	 */
	public TaskProject getProject() {
		return project;
	}

	/**
	 * 设置私有资源读取接口。<br>
	 * 私有资源读取提供给开发者，使开发者可以提取自己分布式任务组件包中的资源。
	 * 
	 * @param e 私有资源读取接口
	 */
	protected void setPrivateReader(TaskReader e) {
		reader = e;
	}

	/**
	 * 返回私有资源读取接口。<br>
	 * 私有资源读取提供给开发者，使开发者可以提取自己分布式任务组件包中的资源。
	 * 
	 * @return 私有资源读取接口
	 */
	protected TaskReader getPrivateReader() {
		return reader;
	}

	/**
	 * 读取用DTC引导包或者JAR附件包中的资源。这些资源包括图片、音频、视频、各类文档等，资源以字节数组方式输出。<br>
	 * 
	 * @param name 资源名称，以“/”为分隔符，保存在DTC引导包或者JAR附件包。
	 * @return 返回读取资源的字节数组
	 * @throws TaskException 如果资源不存在时，弹出异常
	 */
	protected byte[] readResource(String name) throws TaskException {
		Phase phase = getPhase();
		// 如果是空值，弹出错误！
		if (phase == null) {
			throw new TaskException("null pointer!");
		}
		// 读取资源
		Naming ware = phase.getWare();
		return reader.readResource(invokerId, ware, name);
	}

	/**
	 * 返回阶段命名
	 * @return Phase对象实例
	 */
	public final Phase getPhase() {
		if (project == null) {
			return null;
		}
		return project.getPhase();
	}

	/**
	 * 设置分布命令。系统每分配一个任务实例，必须调用这个接口设置命令。
	 * @param e 分布命令
	 */
	public void setCommand(Command e) {
		command = e;
	}

	/**
	 * 返回分布命令
	 * @return Command实例
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 设置异步调用器编号 <br>
	 * 异步调用器编号由CALL/DATA/WORK/BUILD站点在分配任务组件时设置，是SiteInvoker.invokeId的副本。
	 * 
	 * @param who 长整数
	 */
	public void setInvokerId(long who) {
		// 如果发生这个错误，是系统编程的问题，此外无它！
		if (InvokerIdentity.isInvalid(who)) {
			throw new IllegalValueException("illegal invoker identity %d", who);
		}
		invokerId = who;
	}

	/**
	 * 设置异步调用器编号
	 * @return 调用器编号（长整数）
	 */
	public long getInvokerId() {
		return invokerId;
	}

	/**
	 * 返回命令回显地址
	 * @return 返回Cabin实例，或者空指针
	 */
	public Cabin getListener() {
		// 如果命令为空指针时，是错误
		Laxkit.nullabled(command);

		return command.getSource();
	}

	/**
	 * 返回命令站点地址
	 * @return 返回Node实例，或者空指针
	 */
	public Node getSource() {
		// 是错误
		Laxkit.nullabled(command);

		return command.getSource().getNode();
	}

	/**
	 * 判断是内存处理模式（如果一次计算工作在所有任务组件上，都实现用内存做数据存取，这将成为一次流式处理）。 
	 * @return 返回真或者假
	 */
	public boolean isMemory() {
		Laxkit.nullabled(command);

		return command.isMemory();
	}

	/**
	 * 判断是磁盘处理模式
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return !isMemory();
	}

	/**
	 * 返回命令发起人签名。所有命令，都必须有签名！
	 * 
	 * @return 返回SHA256散列码
	 */
	public Siger getIssuer() {
		Laxkit.nullabled(command);

		return command.getIssuer();
	}

	/**
	 * 返回分布式任务组件最新的版本号。
	 * 这是一个固定方法，版本号在用户的“tasks.xml”中配置。
	 * 
	 * @return 系统最新版本号
	 */
	public final WareVersion getWareVersion() {
		return project.getVersion();
	}

	/**
	 * 返回分布式任务组件版本号。<br><br>
	 * 开发者可以在自己的实现类中重新定义，根据版本号，系统将调用与之对应的接口，优化处理。<br>
	 * 
	 * @return TaskVersion实例
	 */
	public TaskVersion getTaskVersion() {
		return new TaskVersion(TaskVersion.VERSION_1);
	}

	/**
	 * 对象延时
	 * @param timeout 等待时间，单位：毫秒
	 */
	protected synchronized void delay(long timeout) {
		try {
			if (timeout > 0L) {
				wait(timeout);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.error(getIssuer(), e);
		}
	}

	/**
	 * 唤醒延时等待
	 */
	protected synchronized void wakeup() {
		try {
			super.notify();
		} catch (IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.error(getIssuer(), e);
		}
	}

	/**
	 * 销毁任务组件保存的资源。<br>
	 * 用户在自己的任务组件中派生这个方法，释放自己的私有资源。销毁操作由JRE完成，无需用户调用。
	 */
	protected void destroy() {
		command = null;
		project = null;
		invokerId = InvokerIdentity.INVALID;
	}

	/**
	 * 释放分布式任务组件资源
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
	}
}