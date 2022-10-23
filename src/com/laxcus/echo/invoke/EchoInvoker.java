/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.io.*;
import java.util.*;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.echo.product.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;

/**
 * 异步命令调用器（回显调用器）。依据“INVOKE/PRODUCE”机制运行。<br><br>
 * 
 * 
 * LAUNCH工作流程：<br>
 * <1> 客户机发出命令，服务器收到命令后，判断是否受理，在确认有效后，保存到本地命令管理池（CommandPool）。<br>
 * <2> CommandPool检查当前计算机的资源状态，在资源允许的状态下（线程/内存/CPU的指标符合要求），根据命令建立对应的异步调用器，交给调用器管理池（InvokerPool）处理。<br>
 * <3> InvokerPool给异步调用器分配运行参数，启动LaunchTrustor执行异步操作。<br>
 * 
 * <br>
 * 
 * ENDING工作流程：<br>
 * <1> InvokerPool收到EchoInvoker发出全部的反馈，在资源允许情况下，启动EndingTrustor，执行数据处理。<br><br>
 * 
 * 
 * 异步操作完成后，服务器根据命令中提供的回显地址（Cabin），把数据返回给客户机，这样就实现了一个完整的异步操作过程。<br>
 * 
 * INVOKE/PRODUCE是异步处理过程，客户机发完命令后即关闭网络连接，转入等待状态。
 * 同样的，服务器在发送完数据处理结果后，也要关闭连接。
 * 这种数据处理方案对节约网络资源、增加多任务并行处理能力，提高数据处理效率意义重大。<br>
 * 
 * 在LAXCUS集群的定义里，所有站点同时客户机和服务器两种身份。<br><br>
 * 
 * 
 * 命令管理池受理客户机命令的接口是“CommandPool.accept”方法，检查和分配异步命令调用器的接口是“CommandPool.dispatch”方法。
 * 转发异步命令调用器给异步调用器管理池的接口是“InvokerPool.launch”方法，“InvokerPool.launch”将分配异步调用器运行所必须的参数，启动“LaunchTrustor”代理。
 * 如果异步命令调用器是迭代处理的，将通过“InvokerPool.dispatch”方法启动“EndingTrustor”代理处理。<br><br>
 * 
 * 
 * 每个具体的异步调用器都有一个关联命令，和根据所在的站点，只执行属于自己范围内的任务。<br>
 * 
 * 异步调用器有两个抽象方法：“launch”和“ending”，由子类根据所属站点和要求去实现。
 * “launch”是负责启动工作，“ending”是启动之后工作。返回值为“真”时，表示它们处理成功，不成功“假”。<br><br>
 * 
 * “launch”方法是数据处理的开始，是必选项，只执行一次；“ending”是“launch”之后的操作，允许多次迭代执行，也可以不执行，是可选项。
 * 大多数简单的异步调用器只用到“launch”，不需要“ending”。复杂的如：CONDUCT/ESTABLISH/SQL INSERT/DROP xxx等操作，
 * 需要多次迭代执行。在调用“launch”之后，往复多次调用“ending”。<br><br>
 * 
 * 判断一个异步调用器已经完成全部工作的方法是：“isUseless”。“真”表示已经完成，“假”表示仍在进行中。
 * 这个参数由各个具体的异步调用器来设置，要在“launch”和“ending”之后检查，再采取需要的措施。<br><br>
 * 
 * 判断一个异步调用器处理运行中的方法是：“isRunning”，真表示在运行中，否则不是。<br><br>
 * 
 * 四个时间：<br>
 * 1. 初始化时间，在构造时生成，不允许改变 <br>
 * 2. 启动时间，在调用器构造时生成，可以重置 <br>
 * 3. 线程启动时间，被InvokerTrustor调用，进行运行状态的时间 <br>
 * 4. 有效运行时间，只在线程中运行的时间，不包括堆栈中等待的时间 <br>
 * 
 * @author scott.liang
 * @version 1.36 01/08/2015
 * @since laxcus 1.0
 */
public abstract class EchoInvoker extends SiteInvoker {

	/** 请求命令 **/
	private Command command;

	/** 回显地址，接收返回数据的通讯端口。在InvokerPool.launch中分配，保证唯一 **/
	private Cabin listener;

	/** 进入运行状态，默认是“假” **/
	private boolean running;

	/** 退出状态，是调用器完成标记，默认是“假”，当“is true”时，调用器管理池将释放它 **/
	private boolean quit;

	/** 最后一次调用成功或者失败 **/
	private boolean perfectly;

	/** 强制退出！由系统调用强制执行，默认是假 **/
	private boolean forceExit;

	/**
	 * 构造默认和私有的异步命令调用器
	 */
	protected EchoInvoker() {
		super();
		setQuit(false);
		setPerfectly(false);
		setRunning(false);
		// 非强行退出
		setForceExit(false);
	}

	/**
	 * 构造异步命令调用器，指定操作命令
	 * @param cmd 操作命令
	 */
	protected EchoInvoker(Command cmd) {
		this();
		setCommand(cmd);
	}

	/**
	 * 返回站点启动器
	 * @return SiteLauncher实例实例
	 */
	public SiteLauncher getLauncher() {
		return InvokerPool.getLauncher();
	}

	/**
	 * 判断支持跨网段通信（此参数由许可证定义）
	 * @return 返回真或者假
	 */
	public boolean isSkipcast() {
		return getLauncher().isSkipcast();
	}

	/**
	 * 判断已经登录
	 * @return 返回真或者假
	 */
	public boolean isLogined() {
		return getLauncher().isLogined();
	}

	/**
	 * 判断已经注销
	 * @return 返回真或者假
	 */
	public boolean isLogout() {
		return getLauncher().isLogout();
	}

	/**
	 * 判断是WINDOWS平台
	 * @return 返回真或者假
	 */
	public boolean isWindows() {
		return getLauncher().isWindows();
	}

	/**
	 * 判断是LINUX平台
	 * @return 返回真或者假
	 */
	public boolean isLinux() {
		return getLauncher().isLinux();
	}

	/**
	 * 返回节点使用者签名。签名在启动时由用户输入
	 * @return 字符串或者空指针
	 */
	public String getSignature() {
		return getLauncher().getSignature();
	}

	/**
	 * 判断记录命令和调用器操作
	 * @return 返回真或者假
	 */
	public boolean isTigger() {
		return command != null && command.isTigger();
	}
	
	/**
	 * 当前调用器是分布式，即需要发送命令给其它节点去执行，默认是“真”。
	 * 子类如果是本地执行，可以重写覆盖这个方法。
	 * @return
	 */
	public boolean isDistributed() {
		return true;
	}

	/**
	 * 记录命令或者否
	 * @param b 是或者否
	 */
	public void setTigger(boolean b) {
		if (command != null) {
			command.setTigger(b);
		}
	}

	/**
	 * 判断本地内存不足，分别检查LINUX/WINDOWS环境。
	 * @return 返回真或者假
	 */
	public boolean isLocalMemoryMissing() {
		if (isLinux()) {
			return LinuxDevice.getInstance().isMemoryMissing();
		} else if (isWindows()) {
			return WindowsDevice.getInstance().isMemoryMissing();
		}
		return false;
	}

	/**
	 * 判断本地磁盘空间不足，分别检查LINUX/WINDOWS环境。
	 * @return 返回真或者假
	 */
	public boolean isLocalDiskMissing() {
		if (isLinux()) {
			return LinuxDevice.getInstance().isDiskMissing();
		} else if (isWindows()) {
			return WindowsDevice.getInstance().isDiskMissing();
		}
		return false;
	}

	/**
	 * 返回当前站点的类型
	 * @return 站点类型
	 */
	public byte getSiteFamily() {
		return getLauncher().getFamily();
	}

	/**
	 * 判断（节点）处于在线状态（即节点已经登录注册成功）
	 * @return 返回真或者假
	 */
	public boolean isOnline() {
		return getLauncher().isLogined();
	}

	/**
	 * 返回命令管理池
	 * @return 命令管理池实例
	 */
	public CommandPool getCommandPool() {
		return getLauncher().getCommandPool();
	}

	/**
	 * 返回调用器管理池
	 * @return 调用器管理池实例
	 */
	public InvokerPool getInvokerPool() {
		return getLauncher().getInvokerPool();
	}

	/**
	 * 统计数据流量，包括接收和发送的数据流
	 * @param client RemoteClient实例
	 */
	protected void addFlow(RemoteClient client) {
		if (client != null) {
			addReceiveFlowSize(client.getReceiveFlowSize());
			addSendFlowSize(client.getSendFlowSize());
		}
	}

	/**
	 * 统计数据流量，包括接收和发送的数据流
	 * @param client EchoClient实例
	 */
	protected void addFlow(EchoClient client) {
		if (client != null) {
			addReceiveFlowSize(client.getReceiveFlowSize());
			addSendFlowSize(client.getSendFlowSize());
		}
	}

	/**
	 * 返回注册的服务器站点地址。
	 * @return Node实例。只有TOP站点是空指针。
	 */
	public Node getHub() {
		return getLauncher().getHub();
	}

	/**
	 * 判断某个站点是管理站点
	 * @param node 站点地址
	 * @return 返回真或者假
	 */
	public boolean isHub(Node node) {
		return Laxkit.compareTo(getHub(), node) == 0;
	}

	/**
	 * 返回本地站点监听地址。如果当前属于网关，返回是内网地址。
	 * @return Node实例
	 */
	public Node getLocal() {
		return getLauncher().getListener();
	}

	/**
	 * 返回外网地址，如果当前节点属于网关时
	 * @return 返回外网地址，或者空指针
	 */
	public Node getPublicListener() {
		return getLauncher().getPublicListener();
	}

	/**
	 * 判断当前节点属于网关类型
	 * @return 返回真或者假
	 */
	public boolean isGateway() {
		return getLauncher().isGateway();
	}

	/**
	 * 判断当前节点运行在内网，通过NAT设备与外网连接。<br>
	 * @return 返回真或者假
	 */
	public boolean isPock() {
		return getLauncher().isPock();
	}

	/**
	 * 设置异步命令调用器进入线程运行状态。<br>
	 * 运行状态是指调用“launch/ending”方法，而在内存中的等待不是运行状态。
	 * @param b 是运行状态
	 */
	public void setRunning(boolean b) {
		running = b;
	}

	/**
	 * 判断异步调用器处于线程运行状态
	 * 
	 * @return 返回真或者假
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 判断异步调用器处于退出线程运行状态
	 * 
	 * @return 返回真或者假
	 */
	public boolean isStopped() {
		return !isRunning();
	}

	/**
	 * 设置退出状态。<br>
	 * 退出状态由子类设置，当调用器的工作全部完成后，这个参数被置“真”。
	 * @param b 是退出状态
	 */
	protected final void setQuit(boolean b) {
		quit = b;
	}

	/**
	 * 判断调用器要求退出。退出后管理池将释放调用器。
	 * @return 返回真或者假
	 */
	public final boolean isQuit() {
		return quit;
	}

	/**
	 * 最后一次调用成功/失败
	 * @param b 成功/失败
	 */
	public void setPerfectly(boolean b) {
		perfectly = b;
	}

	/**
	 * 判断最后调用是成功或者失败
	 * @return 成功/失败
	 */
	public boolean isPerfectly() {
		return perfectly;
	}

	/**
	 * 全部操作流程处理完成，异步调用器退出。
	 * 当“launch/ending”迭代流程全部完成，这个方法被调用，异步调用器将完全退出运行状态，随后管理线程销毁它。
	 * @param success 最后操作结果
	 * @return 操作成功返回“真”，或者失败返回“假”。
	 */
	protected final boolean useful(boolean success) {
		setQuit(true);
		return success;
	}

	/**
	 * 默认是操作成功，异步调用器退出运行状态。
	 * 这个方法实质是“useful(true)”方法
	 * @return 固定返回“真”。
	 */
	protected final boolean useful() {
		return useful(true);
	}

	/**
	 * 设置强行退出
	 * @param b 是或者否
	 */
	public void setForceExit(boolean b) {
		forceExit = b;
		// 如果在运行中，且强行退出
		boolean success = (isRunning() && forceExit);
		if (success) {
			// 如果是转发命令，拿到命令钩子，强制退出！
			if (isShiftCommand()) {
				ShiftCommand cmd = (ShiftCommand) command;
				CommandHook hook = cmd.getHook();
				if (hook != null) {
					hook.done();
				}
				//	cmd.getHook().done();
			} else {
				// 普通的命令，在执行业务中... 如何处理？需要子类去实时判断...

			}
		}
	}

	/**
	 * 判断是强行退出，或者否
	 * @return 返回真或者假
	 */
	public boolean isForceExit() {
		return forceExit;
	}

	/**
	 * 设置异步操作命令（是原始命令！）
	 * @param e 操作命令句柄
	 * @throws NullPointerException - 如果是空值
	 */
	public final void setCommand(Command e) {
		Laxkit.nullabled(e);

		command = e;
	}

	/**
	 * 返回异步操作命令（原始命令！） <br>
	 * 这个异步操作命令是xxxCommandPool分配各站点的xxxInvoker时设置，在整个异步调用器生存期存在和有效。
	 * 所以，这个异步操作命令区别与异步调用器之后执行过程中，再次收到的命令。这个命令是原始命令。
	 * 
	 * @return Command实例
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 返回当前调用器的剩余处理时间
	 * @return 返回调用器剩余可用时间
	 */
	public long getLeftTime() {
		// 没有定义命令，默认无限超时
		if (command == null) {
			return -1;
		}
		// 如果没有超时限制，返回-1
		if (command.isInfinite()) {
			return -1;
		}
		// 命令超时
		long timeout = command.getTimeout();
		// 已经运行时间
		long runtime = getRunTime();
		// 已经超时，返回0
		if (timeout < runtime) {
			return 0;
		}
		// 计算剩余运行时间
		return timeout - runtime;
	}

	/**
	 * 判断需要大块传输。超过10K，默认是采用大块传输
	 * 
	 * @param size 被判断的尺寸
	 * @return 返回真或者假
	 */
	public boolean isBigTransfer(long size) {
		return size >= 10240;
	}

	/**
	 * 判断对象是某个类的子类（从某个类派生出来）
	 * @param that 实例对象
	 * @param clazz 指定类定义
	 * @return 返回真或者假
	 */
	protected boolean isFrom(Object that, Class<?> clazz) {
		if (that != null) {
			return Laxkit.isClassFrom(that.getClass(), clazz);
		}
		return false;
	}

	/**
	 * 判断当前命令是ShiftCommand的子类
	 * @return 返回真或者假
	 */
	public boolean isShiftCommand() {
		return isFrom(command, ShiftCommand.class);
	}

	/**
	 * 判断是TouchCommand命令
	 * @return 返回真或者假
	 */
	public boolean isTouchCommand() {
		return isFrom(command, TouchCommand.class);
	}

	/**
	 * 判断是BatchCommand命令
	 * @return 返回真或者假
	 */
	public boolean isBatchCommand() {
		return isFrom(command, BatchCommand.class);
	}

	/**
	 * 去掉外层包装，返回实际命令
	 * @return Command子类
	 */
	private Command getRealCommand() {
		if (command == null) {
			return null;
		} else if (isShiftCommand()) {
			return ((ShiftCommand) command).getCommand();
		} else if (isTouchCommand()) {
			return ((TouchCommand) command).getCommand();
		}
		return command;
	}

	/**
	 * 设置命令为内存存取模式
	 * @param b 内存存取模式
	 */
	public void setMemory(boolean b) {
		if (command != null) {
			command.setMemory(b);
		}
	}

	/**
	 * 设置命令为磁盘存取模式
	 * @param b 磁盘存取模式
	 */
	public void setDisk(boolean b) {
		if (command != null) {
			command.setDisk(b);
		}
	}

	/**
	 * 判断命令处理以磁盘为中间存取介质
	 * @return 返回真或者假
	 */
	public final boolean isDisk() {
		Command cmd = getRealCommand();
		if(cmd != null) {
			return cmd.isDisk();
		} else {
			return command.isDisk();
		}
	}

	/**
	 * 判断命令处理以内存为中间存取介质（内存计算）
	 * @return 返回真或者假
	 */
	public final boolean isMemory() {
		Command cmd = getRealCommand();
		if (cmd != null) {
			return cmd.isMemory();
		} else {
			return command.isMemory();
		}
	}

	/**
	 * 返回命令的优先级
	 * @return
	 */
	public byte getPriority() {
		Command cmd = getRealCommand();
		if (cmd != null) {
			return cmd.getPriority();
		} else {
			return command.getPriority();
		}
	}

	/**
	 * 判断是闪速处理调用器
	 * @return 返回真或者假
	 */
	public boolean isFast() {
		Command cmd = getRealCommand();
		if (cmd != null) {
			return cmd.isFast();
		} else {
			return command.isFast();
		}
	}

	/**
	 * 判断是快速处理调用器
	 * @return 返回真或者假
	 */
	public boolean isQuick() {
		Command cmd = getRealCommand();
		if (cmd != null) {
			return cmd.isQuick();
		} else {
			return command.isQuick();
		}
	}

	/**
	 * 返回命令的操作人签名，系统操作是空值！
	 * @return Siger实例或者空指针
	 */
	public final Siger getIssuer() {
		Command cmd = getRealCommand();
		if (cmd != null) {
			return cmd.getIssuer();
		} else {
			return null;
		}
	}

	/**
	 * 返回命令超时
	 * @return 毫秒为单位的时间，小于等于0是不限制超时时间 
	 */
	public long getCommandTimeout() {
		Command cmd = getRealCommand();
		if (cmd != null) {
			return cmd.getTimeout();
		} else {
			return -1;
		}
	}

	/**
	 * 返回命令来源的监听地址
	 * @return Cabin实例
	 */
	public final Cabin getCommandSource() {
		Command cmd = getRealCommand();
		if (cmd != null) {
			return cmd.getSource();
		} else {
			return command.getSource();
		}
	}

	/**
	 * 返回命令来源的站点地址
	 * @return Node实例
	 */
	public Node getCommandSite() {
		Cabin cabin = getCommandSource();
		if (cabin != null) {
			return cabin.getNode();
		}
		return null;
	}

	/**
	 * 判断是来自哪类节点
	 * @param siteFamily 节点类型
	 * @return 匹配返回真，否则假
	 */
	public boolean isFromSite(byte siteFamily) {
		Node node =	getCommandSite();
		if(node != null) {
			return (node.getFamily() == siteFamily);
		}
		return false;
	}

	/**
	 * 判断命令来自TOP节点
	 * @return 返回真或者假
	 */
	public boolean isFromTop() {
		return isFromSite(SiteTag.TOP_SITE);
	}

	/**
	 * 判断命令来自HOME节点
	 * @return 返回真或者假
	 */
	public boolean isFromLog() {
		return isFromSite(SiteTag.LOG_SITE);
	}

	/**
	 * 判断命令来自HOME站点
	 * @return 返回真或者假
	 */
	public boolean isFromHome() {
		return isFromSite(SiteTag.HOME_SITE);
	}

	/**
	 * 判断命令来自BUILD站点
	 * @return 返回真或者假
	 */
	public boolean isFromBuild() {
		return isFromSite(SiteTag.BUILD_SITE);
	}

	/**
	 * 判断命令来自CALL站点
	 * @return 返回真或者假
	 */
	public boolean isFromCall() {
		return isFromSite(SiteTag.CALL_SITE);
	}

	/**
	 * 判断命令来自DATA站点
	 * @return 返回真或者假
	 */
	public boolean isFromData() {
		return isFromSite(SiteTag.DATA_SITE);
	}

	/**
	 * 判断命令来自WORK站点
	 * @return 返回真或者假
	 */
	public boolean isFromWork() {
		return isFromSite(SiteTag.WORK_SITE);
	}

	/**
	 * 判断命令来自BANK站点
	 * @return 返回真或者假
	 */
	public boolean isFromBank() {
		return isFromSite(SiteTag.BANK_SITE);
	}

	/**
	 * 判断命令来自ACCOUNT站点
	 * @return 返回真或者假
	 */
	public boolean isFromAccount() {
		return isFromSite(SiteTag.ACCOUNT_SITE);
	}

	/**
	 * 判断命令来自HASH站点
	 * @return 返回真或者假
	 */
	public boolean isFromHash() {
		return isFromSite(SiteTag.HASH_SITE);
	}

	/**
	 * 判断命令来自GATE站点
	 * @return 返回真或者假
	 */
	public boolean isFromGate() {
		return isFromSite(SiteTag.GATE_SITE);
	}

	/**
	 * 判断命令来自ENTRANCE站点
	 * @return 返回真或者假
	 */
	public boolean isFromEntrance() {
		return isFromSite(SiteTag.ENTRANCE_SITE);
	}

	/**
	 * 判断命令来自WATCH站点
	 * @return 返回真或者假
	 */
	public boolean isFromWatch() {
		return isFromSite(SiteTag.WATCH_SITE);
	}

	/**
	 * 判断命令来自FRONT站点
	 * @return 返回真或者假
	 */
	public boolean isFromFront() {
		return isFromSite(SiteTag.FRONT_SITE);
	}

	/**
	 * 判断本地是哪种类型的节点
	 * @param siteFamily 节点类型
	 * @return 匹配返回真，否则假
	 */
	public boolean isSite(byte siteFamily) {
		Node node =	getLocal();
		if(node != null) {
			return (node.getFamily() == siteFamily);
		}
		return false;
	}

	/**
	 * 判断本地是FRONT节点
	 * @return 返回真或者假
	 */
	public boolean isFront() {
		return isSite(SiteTag.FRONT_SITE);
	}

	/**
	 * 判断本地是WATCH节点
	 * @return 返回真或者假
	 */
	public boolean isWatch() {
		return isSite(SiteTag.WATCH_SITE);
	}

//	/**
//	 * 判断命令是来自公网IP的FRONT站点，且当前站点是网关。
//	 * @return 返回真或者假
//	 */
//	private boolean isFrontFromWide() {
//		// 判断命令来自FRONT站点，并且当前站点是网关
//		boolean success = (isFromFront() && isGateway());
//		// 判断FRONT站点来自公网
//		if (success) {
//			Node from = getCommandSite();
//			success = from.getAddress().isWideAddress();
//		}
//		return success;
//	}

	/**
	 * 设置异步调用器在本地的“回显地址”<br><br>
	 * 
	 * 异步调用器的回显地址由“InvokerPool.launch”方法产生和设置，它的“工作编号”与上级的“invokerId”一致。
	 * 
	 * 异步命令发送前，回显地址被赋值到异步命令中，传输到目标站点。服务器完成处理后，根据回显地址向请求端发送异步应答数据。
	 * @param e 回显地址
	 * @throws NullPointerException - 回显地址不允许是空指针 
	 */
	public final void setListener(Cabin e) {
		// 不允许空指针
		Laxkit.nullabled(e);

		// 克隆回显地址
		listener = e;
		// 设置调用器编号
		setInvokerId(listener.getInvokerId());
		// 命令有效，设置调用器编号
		if (command != null) {
			command.setLocalId(listener.getInvokerId());
		}
	}

	/**
	 * 返回当前调用器的“回显地址”
	 * @return 回显地址实例
	 */
	public final Cabin getListener() {
		return listener;
	}

	/**
	 * 返回回显地址中的“回显标识”
	 * @return EchoFlag
	 */
	public final EchoFlag getFlag() {
		return listener.getFlag();
	}

	/**
	 * 在“本地回显地址”基础上，根据索引号，产生一个新的本地回显地址。
	 * @param hub 命令发向的节点
	 * @param index 索引号，不允许负数。
	 * @return Cabin实例
	 * @throws InvokerException, NullPointerException，本地回显地址没有地址，弹出空指针异常。如果索引号小于0，弹出调用异常。
	 */
	protected Cabin createLocalCabin(Node hub, int index) {
		if (listener == null) {
			throw new NullPointerException();
		} else if (index < 0) {
			throw new IllegalValueException("illegal index: %d", index);
		}

		// 当前节点位于NAT内网（目前只有FRONT），这时使用NAT的出口地址与外界通信
		if (isPock()) {
			// 根据目标UDP主机地址，取出当前节点在NAT网关的地址
			SocketHost host = hub.getPacketHost();
			SocketHost nat = getLauncher().getPacketHelper().findPockLocal(host);
			if (nat == null) {
				throw new NullPointerException("cannon be find nat host! " + hub);
			}

			// 生成副本，把节点的地址改成NAT出口地址
			Node node = listener.getNode().duplicate();
			node.getHost().setAddress(nat.getAddress());
			node.getHost().setUDPort(nat.getPort());
			
			// 返回结果
			return new Cabin(node, listener.getInvokerId(), index, true);
		}

		//		// 判断发往FRONT节点，并且命令来自公网的FRONT站点，当前站点是网关。
		//		if (hub.isFront()) {
		//			if (isFrontFromWide()) {
		//				Node outer = getLauncher().getPublicListener();
		//				return new Cabin(outer, listener.getInvokerId(), index);
		//			}
		//		}
		
		// 判断来自公网的FRONT节点，当前节点是网关（GATE/TRANCE/CALL），这时要返回网关节点的公网地址
		if (hub.isFront()) {
			Address address = hub.getAddress();
			if (address.isWideAddress() && isGateway()) {
				Node node = getLauncher().getPublicListener();
				return new Cabin(node, listener.getInvokerId(), index);
			}
		}

		// 属于其它情况，采用本地内网地址
		return new Cabin(listener.getNode(), listener.getInvokerId(), index);
	}

	/**
	 * 在不建立回显缓存情况下，向不同的目标地址发送命令。
	 * @param items 命令处理单元
	 * @param complete 非容错模式（容错是允许发送失败，否则是不失败）
	 * @return 返回发送成功的统计数
	 */
	protected int directTo(CommandItem[] items, boolean complete) {
		int count = 0;
		for (int index = 0; index < items.length; index++) {
			// 服务器地址
			Node hub = items[index].getHub();
			// 生成命令副本，做为发送命令传递
			Command sub = items[index].getCommand().duplicate();

			// 设置回显地址（让服务器判断来源）
			Cabin cabin = createLocalCabin(hub, index);
			sub.setSource(cabin);
			// 根据当前命令，确认子命令是内存或者硬盘处理模式
			sub.setMemory(isMemory());
			// 设置用户签名（如果子类中没有定义，系统来设置）
			if (!sub.hasIssuer()) {
				sub.setIssuer(getIssuer());
			}
			// 直接投递，不用反馈
			sub.setDirect(true);

			// 提交给服务器
			boolean success = submit(hub, sub);
			// 设置执行结果
			items[index].setCompleted(success);

			Logger.debug(this, "directTo", success, "send [%s] to '%s'", sub, hub);

			if (success) {
				count++;
			} else {
				// 如果是非容错模式，后续不再发送，退出！
				if (complete) break;
			}
		}
		// 返回发送统计
		return count;
	}

	/**
	 * 在不建立回显缓存情况下，向不同的目标地址发送任意命令
	 * @param array 命令处理单元数据
	 * @param complete 非容错模式（容错是允许发送失败，否则是不失败）
	 * @return 返回发送成功的统计数
	 */
	protected int directTo(Collection<CommandItem> array, boolean complete) {
		int size = array.size();
		if (size == 0) {
			return 0;
		}
		CommandItem[] items = new CommandItem[size];
		items = array.toArray(items);
		// 投递到指定站点
		return directTo(items, complete);
	}

	/**
	 * 在不建立回显缓存的情况下，向不同的目标地址发送一个相同的命令。
	 * @param hubs 目标站点
	 * @param cmd 异步命令
	 * @param complete 非容错模式（容错是允许发送失败，否则是不失败）
	 * @return 返回发送成功的统计数
	 */
	protected int directTo(Node[] hubs, Command cmd, boolean complete) {
		// 建立命令处理单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (int index = 0; index < hubs.length; index++) {
			CommandItem item = new CommandItem(hubs[index], cmd);
			array.add(item);
		}
		// 发送命令
		return directTo(array, complete);
	}

	/**
	 * 在不建立回显缓存情况下，向不同的目标站点发送一个命令
	 * @param hubs 目标站点地址数组
	 * @param cmd 命令
	 * @param complete 非容错模式（容错是允许发送失败，否则是不失败）
	 * @return 返回发送成功的统计数
	 */
	protected int directTo(Collection<Node> hubs, Command cmd, boolean complete) {
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (Node hub : hubs) {
			CommandItem item = new CommandItem(hub, cmd);
			array.add(item);
		}
		return directTo(array, complete);
	}

	/**
	 * 在不建立回显缓存的情况下，向一批目标地址发送命令。默认是容错模式
	 * @param hubs 目标站点数组
	 * @param cmd 异步命令
	 * @return 返回发送成功的统计数
	 */
	protected int directTo(Node[] hubs, Command cmd) {
		return directTo(hubs, cmd, false);
	}

	/**
	 * 在不建立回显缓存的情况下，向一批目标地址发送命令。默认是容错模式
	 * @param hubs 目标地址数组
	 * @param cmd 命令
	 * @return 返回发送成功的统计数
	 */
	protected int directTo(Collection<Node> hubs, Command cmd) {
		return directTo(hubs, cmd, false);
	}

	/**
	 * 发送多个命令到目标站点
	 * 
	 * @param hub 目标站点
	 * @param cmds 命令集合
	 * @return 返回发送成功数目
	 */
	protected int directTo(Node hub, Collection<Command> cmds) {
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (Command cmd : cmds) {
			CommandItem item = new CommandItem(hub, cmd);
			array.add(item);
		}
		return directTo(array, false);
	}

	/**
	 * 向一个指定的地址发送命令
	 * @param hub 目标地址
	 * @param cmd 异步命令
	 * @return 发送成功返回“真”，否则“假”。 
	 */
	protected boolean directTo(Node hub, Command cmd) {
		Node[] hubs = new Node[] { hub };
		return directTo(hubs, cmd) == 1;
	}

	/**
	 * 在不建立回显缓存情况下，向注册服务器发送一个命令。
	 * 
	 * @param cmd 异步命令
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean directToHub(Command cmd) {
		Node hub = getHub();
		return directTo(new Node[] { hub }, cmd) == 1;
	}

	/**
	 * 在不建立回显缓存情况下，向注册服务器发送一组命令。
	 * @param cmds 异步集合集合
	 * @return 返回发送成功的命令数目
	 */
	protected int directToHub(Collection<Command> cmds) {
		Node hub = getHub();
		return directTo(hub, cmds);
	}

	/**
	 * 客户机向服务器提交一个异步命令。
	 * @param hub 目标站点地址
	 * @param cmd 异步命令
	 * @return 服务器受理返回“真”，否则“假”。
	 */
	private boolean submitTo(Node hub, Command cmd) {
		CommandClient client = ClientCreator.create(CommandClient.class, hub);
		if (client == null) {
			Logger.error(this, "submitTo", "cannot be find \"%s\"", hub);
			return false;
		}

		// 向服务器提交命令
		boolean success = false;
		try {
			// 提交命令到服务器，服务器接受返回真，否则假。
			success = client.submit(cmd);

			// 关闭socket连接，调用“exit”命令，优雅关闭
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 统计数据流量
		addFlow(client);

		// 强制关闭。如果上面已经执行优雅关闭，这行代码不会起作用。
		client.destroy();

		// 返回异步受理结果
		return success;
	}
	
	/**
	 * 客户机向服务器提交一个异步命令。
	 * 发送三次，如果仍然失败，退出！
	 * @param hub 目标站点地址
	 * @param cmd 异步命令
	 * @return 服务器受理返回“真”，否则“假”。
	 */
	protected boolean submit(Node hub, Command cmd) {
		for (int index = 0; index < 3; index++) {
			// 延时3秒再试
			if (index > 0) {
				delay(3000);
			}
			boolean success = submitTo(hub, cmd);
			if (success) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 以“容错”方式，向目标地址发送命令。允许任意发送命令不成功
	 * 
	 * @param items 命令处理单元数组
	 * @return 返回命令发送成功的数目。
	 */
	protected int incompleteTo(CommandItem[] items) {
		// 如果是空值
		if (Laxkit.isEmpty(items)) {
			throw new NullPointerException();
		}
		// 回显地址必须在启动分布命令前已经分配
		if (listener == null) {
			throw new InvokerException("cannot be null");
		}

		Logger.debug(this, "incompleteTo", "send size is %d", items.length);

		// 预定义回显缓存数目
		resetAllBuffers(items.length);

		// 向目标站点发送异步命令
		int count = 0; // 统计成功的数目
		for (int index = 0; index < items.length; index++) {
			// 服务器地址
			Node hub = items[index].getHub();
			// 当前命令不变，克隆一个数据副本
			Command sub = items[index].getCommand().duplicate();
			// 在基础回显地址基础上，分配一个新索引号的回显地址
			Cabin cabin = createLocalCabin(hub, index);
			// 将新的回显地址放到克隆命令中
			sub.setSource(cabin);
			// 基于当前命令模式，确定子命令是内存或者硬盘模式
			sub.setMemory(isMemory());
			// 设置用户签名
			if (!sub.hasIssuer()) {
				sub.setIssuer(getIssuer());
			}

			// 建立回显缓存，同时绑定到异步数据接收代理器。
			boolean success = createBuffer(cabin.getFlag(), isDisk(), sub, hub);
			// 不能建立回显缓存，这是一个严重错误
			if (!success) {
				throw new EchoException("cannot be create buffer");
			}
			// 发送命令，返回服务端受理结果。
			success = submit(hub, sub);
			// 设置发送结果
			items[index].setCompleted(success);

			// 发送成功，统计值加1；否则删除这个缓存
			if (success) {
				count++;
			} else {
				removeBuffer(index);
			}

			Logger.debug(this, "incompleteTo", success, "send [%s] to '%s', save to %s", sub, hub, (isDisk() ? "DISK" : "MEMORY"));
		}

		// 不完成成功，重置缓存数目
		if (count > 0 && count < items.length) {
			setDefaultSize(count);
		}

		Logger.debug(this, "incompleteTo", 
				"send finished! success size:%d, all size:%d", count, items.length);

		return count;
	}

	/**
	 * 以“容错”方式，向目标地址发送命令。允许某些站点发送不成功的现象存在
	 * @param array 命令处理单元数组
	 * @return 返回发送成功的数目
	 */
	protected int incompleteTo(Collection<CommandItem> array) {
		int size = array.size();
		if (size == 0) {
			return 0;
		}
		CommandItem[] items = new CommandItem[size];
		items = array.toArray(items);
		return incompleteTo(items);
	}

	/**
	 * 以“容错”方式，向目标地址发送命令。允许任意发送命令不成功
	 * @param hubs 目标站点地址
	 * @param cmd 发送的命令
	 * @return 返回命令发送成功的数目。
	 */
	protected int incompleteTo(Node[] hubs, Command cmd) {
		int size = (hubs != null ? hubs.length : 0);
		CommandItem[] items = new CommandItem[size];
		for (int i = 0; i < size; i++) {
			items[i] = new CommandItem(hubs[i], cmd);
		}
		// 发送命令
		return incompleteTo(items);
	}

	/**
	 * 以“容错”方式，向目标地址发送命令。允许任意发送命令不成功
	 * @param hub 目标站点地址
	 * @param cmds 发送的命令数组
	 * @return 返回命令发送成功的数目。
	 */
	protected int incompleteTo(Node hub, Command[] cmds) {
		int size = (cmds != null ? cmds.length : 0);
		CommandItem[] items = new CommandItem[size];
		for (int i = 0; i < size; i++) {
			items[i] = new CommandItem(hub, cmds[i]);
		}
		// 发送命令
		return incompleteTo(items);
	}

	/**
	 * 以“容错”方式，向目标地址发送命令。允许任意发送命令不成功
	 * @param hubs 目标站点地址
	 * @param cmd 发送的命令
	 * @return 返回命令发送成功的数目。
	 */
	protected int incompleteTo(Collection<Node> hubs, Command cmd) {
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (Node node : hubs) {
			CommandItem item = new CommandItem(node, cmd);
			array.add(item);
		}
		// 向目标站点发送命令处理单元
		return incompleteTo(array);
	}

	/**
	 * 根据索引编号，找到对应的异步缓存，从EchoHead中取出监听地址。
	 * 在“EchoInvoker.replyTo”方法中设置ItemCabin。
	 * @param index 索引编号
	 * @return 返回Cabin或者空值
	 */
	protected Cabin findItemCabin(int index) {
		EchoBuffer buf = super.findBuffer(index);
		if (buf == null) {
			return null;
		}

		EchoHead head = buf.getHead();

		if (head.isHelp(ItemCabin.class)) {
			ItemCabin that = head.getHelp(ItemCabin.class);
			return that.getListener();
		} else if (head.isCastHelp(ItemCabin.class)) {
			ItemCabin that = head.getCastHelp(ItemCabin.class);
			return that.getListener();
		}
		// 返回空值
		return null;
	}
	
//	private Cabin createLocalReplyCabin(Cabin source, int index) {
//		if (listener == null) {
//			throw new NullPointerException();
//		} else if (index < 0) {
//			throw new IllegalValueException("illegal index: %d", index);
//		}
//		
//		Node hub = source.getNode();
//
//		// 判断当前节点位于内网，连接网关节点
//		if (isPock()) {
//			// 根据目标UDP主机地址，取出当前节点在NAT网关的地址
//			SocketHost host = hub.getPacketHost();
////			SocketHost nat = getLauncher().getReplyHelper().findPockLocal(host);
//			
//			SocketHost nat = getLauncher().getPacketHelper().findPockLocal(host);
//			if (nat == null) {
//				throw new NullPointerException("cannon be find nat host! " + hub);
//			}
//
//			// 把节点的地址改成NAT出口地址
//			Node node = listener.getNode().duplicate();
//			node.getHost().setAddress(nat.getAddress());
//			node.getHost().setUDPort(nat.getPort());
//			
//			// 返回结果
//			return new Cabin(node, listener.getInvokerId(), index, true);
//		}
//
////		// 判断发往FRONT节点，并且命令来自公网的FRONT站点，当前站点是网关。
////		if (hub.isFront() && isFrontFromWide()) {
//////			Node outer = getLauncher().getPublicListener();
//////			return new Cabin(outer, listener.getInvokerId(), index);
////			
////			Node outer = getLauncher().getPublicListener();
////			return new Cabin(outer, listener.getInvokerId(), index);
////		}
////		// 判断发往FRONT节点，并且FRONT节点位于NAT后面，这里启动SUCKER端口，让FRONT向这里发送数据包
////		else if(hub.isFront() && source.isPock()) {
////			Node sucker = listener.getNode().duplicate();
////			// 取SUCKER地址，向这个地址发送
////			sucker.getHost().setUDPort(getLauncher().getReflectSuckerPort());
////			return new Cabin(sucker, listener.getInvokerId(), index);
////		}
//
//		// 来自FRONT节点，FRONT是公网，或者是
//		if (hub.isFront() ) { 
//			if (isFrontFromWide()) {
//				Node outer = getLauncher().getPublicListener();
//				return new Cabin(outer, listener.getInvokerId(), index);
//			} else if (source.isPock()) {
//
//			}
//
//			//			Node sucker = listener.getNode().duplicate();
//			//			// 取SUCKER地址，向这个地址发送
//			//			sucker.getHost().setUDPort(getLauncher().getReflectSuckerPort());
//			//			return new Cabin(sucker, listener.getInvokerId(), index);
//		}
//		
//		// 属于其它情况，采用本地内网地址
//		return new Cabin(listener.getNode(), listener.getInvokerId(), index);
//	}

//	/**
//	 * 建立异步缓存，发送一批命令处理结果到其它节点的异步调用器。这些异步调用器都处于监听状态。
//	 * @param items 异步回应单元数组
//	 * @return 全部成功返回真，否则假。
//	 */
//	protected boolean replyTo(ReplyItem[] items) {
//		// 如果是空值
//		if (Laxkit.isEmpty(items)) {
//			throw new NullPointerException("ReplyItem array is null!");
//		}
//		// 回显地址必须在启动分布命令前已经分配
//		if (listener == null) {
//			throw new InvokerException("listen is null!");
//		}
//
//		Logger.debug(this, "replyTo", "send size is %d", items.length);
//
//		// 预定义缓存数目
//		resetAllBuffers(items.length);
//
//		// 向目标站点发送异步命令
//		int index = 0; // 索引号，从0开始。
//		for (; index < items.length; index++) {
//			// 异步调用器监听地址（因为是应答，所以这个监听地址一定存在！）
//			Cabin remote = items[index].getSource();
//
//			// 从0开始，依次建立一个本地回显地址
//			Cabin cabin = createLocalCabin(remote.getNode(), index);
//			Logger.debug(this, "replyTo", "local cabin: %s", cabin);
//			
////			// 生成回显地址，向接收端接下来向这里发送数据包
////			Cabin cabin = createReplyCabin(remote, index);
////			Logger.debug(this, "replyTo", "数据接收位置：%s", cabin);
//
//			// 建立回显缓存，同时绑定到异步数据接收代理器。
//			boolean success = createBuffer(cabin.getFlag(), isDisk());
//			// 不能建立回显缓存，这是错误
//			if (!success) {
//				throw new EchoException("cannot be create buffer");
//			}
//
//			// 应答对象
//			Object object = items[index].getObject();
//			// 如果传输对象是命令，设置它的监听地址、内存模式、用户签名
//			if (items[index].isCommand()) {
//				Command cmd = (Command) object;
//				cmd.setSource(cabin);
//				cmd.setMemory(isMemory());
//				if (!cmd.hasIssuer()) {
//					cmd.setIssuer(getIssuer());
//				}
//			}
//			// 如果是异步应答，设置它的监听地址、用户签名
//			else if (items[index].isEchoProduct()) {
//				EchoProduct product = (EchoProduct) object;
//				product.setSource(cabin);
//				product.setIssuer(getIssuer());
//			}
//
//			CastFlag flag = createCastFlag(remote);
//			// 生成监听地址（辅助信息）
//			flag.setHelp(new ItemCabin(cabin));
//
//			// 发送对象到命令来源监听端口
//			// 传输对象有4种情况：1.原始字节数组。2.磁盘文件。3. 命令。4.java.lang.Object子类。它们分别以各自的模式发送
//			if (items[index].isPrimitive()) {
//				success = replyPrimitive(remote, flag, (byte[]) object);
//			} else if (items[index].isFile()) {
//				success = replyFile(remote, flag, (File) object);
//			} else if (items[index].isCommand()) {
//				success = replyCommand(remote, flag, (Command) object);
//			} else {
//				success = replyObject(remote, flag, object);
//			}
//
//			Logger.debug(this, "replyTo", success, "send [%s] to '%s', save to %s", 
//					object.getClass().getSimpleName(), remote, (isDisk() ? "DISK" : "MEMORY"));
//
//			// 发送命令不成功，释放回显缓存和退出！
//			if (!success) {
//				removeBuffer(index);
//				break;
//			}
//		}
//
//		boolean success = (index == items.length);
//
//		Logger.debug(this, "replyTo", success,
//				"send finished! current index:%d, size:%d", index, items.length);
//
//		// 返回处理结果
//		return success;
//	}

	/**
	 * 建立异步缓存，发送一批命令处理结果到其它节点的异步调用器。这些异步调用器都处于监听状态。
	 * @param items 异步回应单元数组
	 * @return 全部成功返回真，否则假。
	 */
	protected boolean replyTo(ReplyItem[] items) {
		// 如果是空值
		if (Laxkit.isEmpty(items)) {
			throw new NullPointerException("ReplyItem array is null!");
		}
		// 回显地址必须在启动分布命令前已经分配
		if (listener == null) {
			throw new InvokerException("listen is null!");
		}

		Logger.debug(this, "replyTo", "send size is %d", items.length);

		// 预定义缓存数目
		resetAllBuffers(items.length);

		// 向目标站点发送异步命令
		int index = 0; // 索引号，从0开始。
		for (; index < items.length; index++) {
			ReplyItem element = items[index];
			// 异步调用器监听地址（因为是应答，所以这个监听地址一定存在！）
			Cabin remote = element.getSource();

			// 从0开始，依次建立一个本地回显地址
			Cabin cabin = createLocalCabin(remote.getNode(), index);
//			Logger.debug(this, "replyTo", "local cabin: %s", cabin);

			// 建立回显缓存，同时绑定到异步数据接收代理器。
			boolean success = createBuffer(cabin.getFlag(), isDisk());
			// 不能建立回显缓存，这是错误
			if (!success) {
				throw new EchoException("cannot be create buffer");
			}

			// 应答对象
			Object object = element.getObject();
			// 如果传输对象是命令，设置它的监听地址、内存模式、用户签名
			if (element.isCommand()) {
				Command cmd = (Command) object;
				cmd.setSource(cabin);
				cmd.setMemory(isMemory());
				if (!cmd.hasIssuer()) {
					cmd.setIssuer(getIssuer());
				}
			}
			// 如果是异步应答，设置它的监听地址、用户签名
			else if (element.isEchoProduct()) {
				EchoProduct product = (EchoProduct) object;
				product.setSource(cabin);
				product.setIssuer(getIssuer());
			}

			CastFlag flag = createCastFlag(remote);
			// 生成监听地址（辅助信息）
			flag.setHelp(new ItemCabin(cabin));

			// 发送对象到命令来源监听端口
			// 传输对象有4种情况：1.原始字节数组。2.磁盘文件。3. 命令。4.java.lang.Object子类。它们分别以各自的模式发送
			if (element.isPrimitive()) {
				success = replyPrimitive(remote, flag, (byte[]) object);
			} else if (element.isFile()) {
				success = replyFile(remote, flag, (File) object);
			} else if (element.isCommand()) {
				success = replyCommand(remote, flag, (Command) object);
			} else {
				success = replyObject(remote, flag, object);
			}

			Logger.debug(this, "replyTo", success, "send [%s] to '%s', save to %s", 
					object.getClass().getSimpleName(), remote, (isDisk() ? "DISK" : "MEMORY"));

			// 发送命令不成功，释放回显缓存和退出！
			if (!success) {
				removeBuffer(index);
				break;
			}
		}

		boolean success = (index == items.length);

		Logger.debug(this, "replyTo", success,
				"send finished! current index:%d, size:%d", index, items.length);

		// 返回处理结果
		return success;
	}
	
	/**
	 * 建立异步缓存，发送一批命令处理结果到其它节点的异步调用器。这些异步调用器都处于监听状态。
	 * @param array 异步回应单元数组
	 * @return 全部成功返回真，否则假
	 */
	protected boolean replyTo(List<ReplyItem> array) {
		int size = array.size();
		if (size == 0) {
			return false;
		}
		ReplyItem[] items = new ReplyItem[size];
		items = array.toArray(items);
		return replyTo(items);
	}

	/**
	 * 建立异步缓存，并发送命令处理结果到另一个节点的异步调用器，这个异步调用器处于数据接收状态。
	 * @param item 异步应答单元
	 * @return 成功返回真，否则假
	 */
	protected boolean replyTo(ReplyItem item) {
		ReplyItem[] items = new ReplyItem[] { item };
		return replyTo(items);
	}

	/**
	 * 建立异步缓存，并发送命令处理结果到另一个节点的异步调用器，这个异步调用器处于数据接收状态。
	 * @param source 异步调用器监听地址
	 * @param object 反馈的对象
	 * @return 成功返回真，否则假
	 */
	protected boolean replyTo(Cabin source, Object object) {
		ReplyItem item = new ReplyItem(source, object);
		return replyTo(item);
	}

	/**
	 * 以“完全”模式，向每一个目标地址发送一个命令。发送的命令必须全部成功。
	 * 
	 * @param items 命令处理单元数组
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean completeTo(CommandItem[] items) {
		// 如果是空值
		if (Laxkit.isEmpty(items)) {
			throw new NullPointerException("EchoItem array is null!");
		}
		// 回显地址必须在启动分布命令前已经分配
		if (listener == null) {
			throw new InvokerException("listen is null!");
		}

		Logger.debug(this, "completeTo", "send count: %d", items.length);

		// 预定义缓存数目
		resetAllBuffers(items.length);

		// 向目标站点发送异步命令
		int index = 0; // 索引号，从0开始。
		for (; index < items.length; index++) {
			// 服务器地址
			Node hub = items[index].getHub();
			// 不改变当前命令，克隆一个副本
			Command sub = items[index].getCommand().duplicate();

			// 在基本回显地址上，分配一个新索引号的回显地址
			Cabin cabin = createLocalCabin(hub, index);
			// 将新的回显地址放到克隆命令中
			sub.setSource(cabin);
			// 基于当前命令模式，确认它的子命令是内存或者硬盘处理
			sub.setMemory(isMemory());
			// 命令副本如果没有定义用户签名，设置它
			if (!sub.hasIssuer()) {
				sub.setIssuer(getIssuer());
			}

			// 建立回显缓存，同时绑定到异步数据接收代理器。
			boolean success = createBuffer(cabin.getFlag(), isDisk(), sub, hub);
			// 不能建立回显缓存，这是错误
			if (!success) {
				throw new EchoException("cannot be create echo-buffer!");
			}
			// 向目标地址发送命令，返回服务端受理结果。
			success = submit(hub, sub);
			// 设置发送结果
			items[index].setCompleted(success);

			Logger.debug(this, "completeTo", success, "send [%s] to [%s], save to %s.",
					sub, hub, (isDisk() ? "DISK" : "MEMORY"));

			// 发送命令不成功，释放回显缓存和退出！
			if (!success) {
				removeBuffer(index);
				break;
			}
		}

		boolean success = (index == items.length);

		Logger.debug(this, "completeTo", success,
				"send finished! current index:%d, size:%d.", index, items.length);

		// 返回处理结果
		return success;
	}

	/**
	 * 以“完全”模式，向指定目标地址发送命令
	 * @param array 命令处理单元数组
	 * @return 全部发送成功返回“真”，否则“假”。
	 */
	protected boolean completeTo(Collection<CommandItem> array) {
		int size = array.size();
		if (size == 0) {
			return false;
		}
		CommandItem[] items = new CommandItem[size];
		items = array.toArray(items);
		return completeTo(items);
	}

	/**
	 * 以“完全”模式，向指定目标地址发送命令
	 * @param item 命令处理单元
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean completeTo(CommandItem item) {
		CommandItem[] array = new CommandItem[] { item };
		return completeTo(array);
	}

	/**
	 * 以“完全”模式，向一组目标地址发送命令。发送的命令必须全部成功。
	 * @param hubs 一组目标站点地址
	 * @param cmd 发送的命令
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean completeTo(Node[] hubs, Command cmd) {
		int size = (hubs != null ? hubs.length : 0);
		// 建立命令处理单元数组
		CommandItem[] items = new CommandItem[size];
		for (int i = 0; i < hubs.length; i++) {
			items[i] = new CommandItem(hubs[i], cmd);
		}
		// 发送命令
		return completeTo(items);
	}

	/**
	 * 以“完全”模式，向一个目标地址发送一组命令。发送的命令必须全部成功。
	 * @param hub 目标站点地址
	 * @param cmds 一组命令
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean completeTo(Node hub, Command[] cmds) {
		int size = (cmds != null ? cmds.length : 0);
		CommandItem[] items = new CommandItem[size];
		for (int i = 0; i < size; i++) {
			items[i] = new CommandItem(hub, cmds[i]);
		}
		return completeTo(items);
	}

	/**
	 * 以“完全”模式，向目标地址发送命令。发送的命令必须全部成功。
	 * @param hub 目标站点地址
	 * @param cmd 异步命令
	 * @return 建立缓存和发送命令成功返回“真”，否则“假”。
	 */
	protected boolean completeTo(Node hub, Command cmd) {
		CommandItem item = new CommandItem(hub, cmd);
		return completeTo(item);
	}

	/**
	 * 以“完全”模式，向目标地址发送命令。发送的命令必须全部成功
	 * @param hubs 目标站点地址数组
	 * @param cmd 异步命令
	 * @return 建立全部缓存和发送命令成功返回“真”，任何出错都中止，返回“假”。
	 */
	protected boolean completeTo(Collection<Node> hubs, Command cmd) {
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for(Node node : hubs) {
			CommandItem item = new CommandItem(node, cmd);
			array.add(item);
		}
		// 发送命令
		return completeTo(array);
	}

	/**
	 * 发送命令到注册站点
	 * @param cmd 被发送的异步命令
	 * @return 建立回显缓存和发送命令成功返回“真”，否则“假”。
	 */
	protected boolean launchToHub(Command cmd) {
		Node hub = getHub();
		return completeTo(hub, cmd);
	}

	/**
	 * 生成默认命令的数据副本，发送给注册站点。返回结果默认保存到内存。
	 * 
	 * @return 建立回显缓存和发送命令成功返回“真”，否则“假”。
	 */
	protected boolean launchToHub() {
		Node hub = getHub();
		Command sub = command.duplicate();
		return completeTo(hub, sub);
	}

	/**
	 * 向目标站点发送一个命令
	 * @param hub 服务器地址
	 * @param cmd 命令
	 * @return 成功返回“真”，否则“假”
	 */
	protected boolean launchTo(Node hub, Command cmd) {
		CommandItem item = new CommandItem(hub, cmd);
		return completeTo(item);
	}

	/**
	 * 向一批目标站点发送一个同质命令
	 * @param hubs 一批目标站点
	 * @param cmd 命令
	 * @return 成功返回“真”，否则“假”
	 */
	protected boolean launchTo(Node[] hubs, Command cmd) {
		return completeTo(hubs, cmd);
	}

	/**
	 * 向一批目标站点发送一个同质命令
	 * @param hubs 一批目标站点
	 * @param cmd 命令
	 * @return 成功返回“真”，否则“假”
	 */
	protected boolean launchTo(Collection<Node> hubs, Command cmd) {
		int size = hubs.size();
		if (size == 0) {
			return false;
		}
		Node[] sites = new Node[size];
		sites = hubs.toArray(sites);
		return completeTo(sites, cmd);
	}

	/**
	 * 向一个目标站点发送一批命令
	 * @param hub 目标站点
	 * @param cmds 一批命令
	 * @return 成功返回“真”，否则“假”
	 */
	protected boolean launchTo(Node hub, Command[] cmds) {
		return completeTo(hub, cmds);
	}

	/**
	 * 向一组指定的地址发送异步命令，选择内存或者磁盘做为接收缓存<br>
	 * @param hubs 目标地址数组
	 * @return 全部站点受理返回“真”，否则返回“假”。
	 */
	protected boolean launchTo(Node[] hubs) {
		return completeTo(hubs, command);
	}

	/**
	 * 向一组指定的站点发送异步命令，选择内存或者硬盘做为接收缓存。
	 * @param hubs 目标地址集合
	 * @return 全部站点受理返回“真”，否则返回“假”。
	 */
	protected boolean launchTo(Collection<Node> hubs) {
		int size = hubs.size();
		if (size < 1) {
			return false;
		}
		Node[] nodes = new Node[hubs.size()];
		nodes = hubs.toArray(nodes);
		return launchTo(nodes);
	}

	/**
	 * 向一个指定的站点分派异步命令。
	 * @param hub 目标地址
	 * @return 站点受理返回“真”，否则返回“假”。
	 */
	protected boolean launchTo(Node hub) {
		Laxkit.nullabled(hub);

		Node[] hubs = new Node[] { hub };
		return launchTo(hubs);
	}

	/**
	 * 建立MD5的异步通信码
	 * @return 返回异步通信码（MD5的封装）
	 */
	private CastCode createCastCode() {
		ClassWriter writer = new ClassWriter();
		writer.writeObject(listener);
		writer.writeInt(doCastIndex());
		byte[] b = writer.effuse();
		// 把上述参数散列成MD5码
		MD5Hash hash = Laxkit.doMD5Hash(b);
		return new CastCode(hash);
	}

	/**
	 * 当前节点是以客户端的身份，生成异步通信标识。注意！！！是客户端！！！<br><br>
	 * 
	 * 异步通信标识在EchoBuffer.cast、DoubleClient.cast中使用。<br>
	 * 
	 * @param remote 远程目标地址（此时的远程目标地址属于服务器身份）
	 * @return 返回异步通信标识
	 */
	protected CastFlag createCastFlag(Cabin remote) {
		// 如果没有定义目标地址，取命令来源地址
		if (remote == null) {
			remote = getCommandSource();
		}
		// 命令来源地址，目标服务器地址
		Address server = remote.getAddress();

		SiteLauncher launcher = getLauncher();

		// 默认是异步发送器的内网主机地址
		SocketHost local = launcher.getReplyWorker().getDefinePrivateHost();
		// 当前站点是网关，做为客户端的身份，准备发送命令给来源的服务端
		// 服务器来自有NAT的内网或者公网，取异步发送器（ReplyDispatcher）的公网IP地址。
		if (launcher.isGateway()) {
			// 服务端来自公网
			if (server.isWideAddress()) {
				local = launcher.getReplyWorker().getDefinePublicHost();
			}
			// 服务端来自有NAT的内网
			else if (remote.isPock()) {
				local = launcher.getReplyWorker().getDefinePublicHost();
			}
		}

//		Logger.debug(this, "createCastFlag", "from [%s], requestor is: %s , current is: %s#%s",
//				remote, remote.getNode(), SiteTag.translate(launcher.getFamily()), local);

		Logger.debug(this, "createCastFlag", "requestor is: %s , current is: %s#%s",
				remote.getNode(), SiteTag.translate(launcher.getFamily()), local);
		
		// 建立一个异步通信码
		CastCode code = createCastCode();
		// 异步通信标识
		return new CastFlag(server, launcher.getFamily(), local, code);
	}

	/**
	 * 生成快速异步通信标识
	 * @return CastFlag实例
	 */
	protected CastFlag createCastFlag() {
		return createCastFlag(null);
	}

	/**
	 * 指定目标地址，回应故障失败应答。
	 * 只调用EchoVisit.start, EchoVisit.stop两个方法，忽略EchoVisit.push方法。
	 * 
	 * @param endpoint 目标站点地址
	 * @param code 应答码
	 * @param help 辅助应答信息
	 * @return 发送成功返回真，否则假。
	 */
	protected boolean replyFault(Cabin endpoint, EchoCode code, EchoHelp help) {
		if (endpoint == null) {
			Logger.warning(this, "replyFault", "null address!");
			return false;
		}

		// 生成回显报头
		EchoHead head = new EchoHead(code, 0, help);

		// 建立连接
		EchoClient client = createEchoClient(endpoint);
		// 发送错误通知
		boolean success = false;
		if (client != null) {
			success = client.shoot(head);
		}

		Logger.debug(this, "replyFault", success, "Reply To %s", endpoint);

		// 统计数据流量（接收和发送）
		addFlow(client);

		// 释放SOCKET。如果已经关闭，这里是冗余操作。
		if (client != null) {
			client.destroy();
		}

		return success;
	}

	/**
	 * 发送错误到请求端
	 * @param code 回显码
	 * @param help 回显帮助信息
	 * @return 发送成功返回真，否则假。
	 */
	protected boolean replyFault(EchoCode code, EchoHelp help) {
		Cabin endpoint = getCommandSource();
		return replyFault(endpoint, code, help);
	}

	/**
	 * 指定请求端回显地址和错误编码，发送到请求端
	 * @param endpoint 请求端回显地址
	 * @param code 回显码
	 * @return 发送成功返回真，否则假。
	 */
	protected boolean replyFault(Cabin endpoint, EchoCode code) {
		return replyFault(endpoint, code, null);
	}

	/**
	 * 指定请求端回显地址和错误编码，发送到请求端
	 * @param endpoint 请求端回显地址
	 * @param major 错误主码
	 * @param minor 错误次码
	 * @return 发送成功返回真，否则假。
	 */
	protected boolean replyFault(Cabin endpoint, short major, short minor) {
		EchoCode code = new EchoCode(major, minor);
		return replyFault(endpoint, code);
	}

	/**
	 * 指定请求端的回显地址和应答编号，发送错误通知到请求端
	 * @param endpoint 请求端回显地址
	 * @param major 错误码，见Major定义
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyFault(Cabin endpoint, short major) {
		EchoCode code = new EchoCode(major, Minor.SYSTEM_FAILED);
		return replyFault(endpoint, code);
	}

	/**
	 * 指定目标回显地址，发送错误通知到目标站点上。
	 * @param endpoint 请求端回显地址
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyFault(Cabin endpoint) {
		return replyFault(endpoint, Major.FAULTED);
	}

	/**
	 * 向请求端发送一个错误应答。见Major/Minor中的定义
	 * @param major 主码
	 * @param minor 从码
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyFault(short major, short minor) {
		Cabin endpoint = getCommandSource();
		// 如果没有回显地址，这个命令是本地发出，不需要回答。
		if (endpoint == null) {
			return false;
		}
		EchoCode code = new EchoCode(major, minor);
		return replyFault(endpoint, code);
	}

	/**
	 * 向请求端发送一个错误应答
	 * @param major 主码
	 * @param minor 从码
	 * @param help 回显辅助信息
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyFault(short major, short minor, EchoHelp help) {
		Cabin endpoint = getCommandSource();
		// 如果没有回显地址，这个命令是本地发出，不需要回答。
		if (endpoint == null) {
			return false;
		}
		EchoCode code = new EchoCode(major, minor);
		return replyFault(endpoint, code, help);
	}

	/**
	 * 发送错误通知到请求端
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyFault() {
		Cabin endpoint = getCommandSource();
		// 如果没有回显地址，这个命令是本地发出，不需要回答。
		if (endpoint == null) {
			return false;
		}
		return replyFault(endpoint);
	}

	/**
	 * 指定目标地址和文件数组，向这个地址发送文件数据
	 * @param endpoint 目标地址
	 * @param flag 快速异步通信标识
	 * @param object java.lang.Object子类对象
	 * @return 全部发送成功返回“真”，否则“假”。
	 */
	protected boolean replyObject(Cabin endpoint, CastFlag flag, Object object) {
		Laxkit.nullabled(endpoint);

		// 如果是异步调用器，记录它的使用时间
		if (Laxkit.isClassFrom(object, EchoProduct.class)) {
			long ms = getProcessTime() + getThreadUsedTime();
			((EchoProduct) object).setProcessTime(ms);
		}

		// 建立联接和发送
		EchoClient client = createEchoClient(endpoint);
		boolean success = false;
		if (client != null) {
			success = client.post(true, flag, object);
		}

		// 统计数据流量（接收和发送）
		addFlow(client);

		Logger.debug(this, "replyObject", success, "[%s] reply to [%s]", 
				object.getClass().getSimpleName(), endpoint);

		// 销毁
		if (client != null) {
			client.destroy();
		}
		return success;
	}

	/**
	 * 指定目标的回显地址和对象，向目标地址发送对象数据
	 * @param endpoint 目标回显地址
	 * @param object java.lang.Object子类对象
	 * @return 发布成功返回“真”，否则“假”。
	 */
	protected boolean replyObject(Cabin endpoint, Object object) {
		CastFlag flag = createCastFlag();
		return replyObject(endpoint, flag, object);
	}

	/**
	 * 向指定的目标地址发送一个对象。
	 * @param object 基于java.lang.Object的任何实例
	 * @return 发布成功返回“真”，否则“假”。
	 */
	protected boolean replyObject(Object object) {
		Cabin endpoint = getCommandSource();
		// 生成通信标识
		CastFlag flag = createCastFlag();
		// 发送对象
		return replyObject(endpoint, flag, object);
	}

	/**
	 * 发送异步应答报告
	 * @param product 异步应答报告
	 * @return 发布成功返回“真”，否则“假”
	 */
	protected boolean replyProduct(EchoProduct product) {
		return replyObject(product);
	}

	/**
	 * 向指定的监听地址发送异步应答报告
	 * @param endpoint 目标地址
	 * @param product 异步应答报告 
	 * @return 发送成功返回真，否则假
	 */
	protected boolean replyProduct(Cabin endpoint, EchoProduct product) {
		CastFlag flag = createCastFlag();
		return replyObject(endpoint, flag, product);
	}

	/**
	 * 发送命令
	 * @param endpoint 目标站点
	 * @param flag 快速异步通信标识
	 * @param cmd 异步命令
	 * @return 发送成功返回真，否则假
	 */
	protected boolean replyCommand(Cabin endpoint, CastFlag flag, Command cmd) {
		return replyObject(endpoint, flag, cmd);
	}

	/**
	 * 发送命令
	 * @param endpoint 目标站点
	 * @param cmd 异步命令
	 * @return 发送成功返回真，否则假
	 */
	protected boolean replyCommand(Cabin endpoint, Command cmd) {
		return replyObject(endpoint, cmd);
	}

	/**
	 * 发送命令
	 * @param cmd 异步命令
	 * @return 发送成功返回真，否则假
	 */
	protected boolean replyCommand(Command cmd) {
		return replyObject(cmd);
	}

	/**
	 * 指定目标地址和文件数组，向这个地址发送文件数据
	 * @param endpoint 目标地址
	 * @param flag 快速异步通信标识
	 * @param files 文件数组
	 * @return 全部发送成功返回“真”，否则“假”。
	 */
	protected boolean replyFile(Cabin endpoint, CastFlag flag, File[] files) {
		Laxkit.nullabled(endpoint);

		//		Logger.debug(this, "replyFile", "%d 的文件准备投递到：%s", 
		//				getInvokerId(),  endpoint);

		// 建立联接和发送
		EchoClient client = createEchoClient(endpoint);
		boolean success = false;
		if (client != null) {
			success = client.post(true, flag, files);
		}

		// 统计数据流量（接收和发送）
		addFlow(client);

		Logger.debug(this, "replyFile", success, "reply to: %s, send size:%d",
				endpoint, client.getSendFlowSize());

		// 销毁
		if (client != null) {
			client.destroy();
		}
		return success;
	}

	/**
	 * 指定目标地址和文件，向这个地址发送文件数据
	 * @param endpoint 目标地址
	 * @param flag 快速异步通信标识
	 * @param file 磁盘文件
	 * @return 发送成功返回真，否则假
	 */
	protected boolean replyFile(Cabin endpoint, CastFlag flag, File file) {
		return replyFile(endpoint, flag, new File[] { file });
	}

	/**
	 * 指定目标地址和文件，向这个地址发送文件数据
	 * @param endpoint 目标地址
	 * @param file 磁盘文件
	 * @return 发送成功返回真，否则假
	 */
	protected boolean replyFile(Cabin endpoint, File file) {
		CastFlag flag = createCastFlag();
		File[] files = new File[] { file };
		return replyFile(endpoint, flag, files);
	}

	/**
	 * 指定一组文件，向请求端发送文件数据
	 * @param files 文件数组
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyFile(File[] files) {
		Cabin endpoint = getCommandSource();
		CastFlag flag = createCastFlag();
		return replyFile(endpoint, flag, files);
	}

	/**
	 * 向请求端发送一组文件
	 * @param files 文件数组
	 * @return 发送成功返回真，否则假
	 */
	protected boolean replyFile(Collection<File> files){
		File[] array = new File[files.size()];
		array = files.toArray(array);
		return replyFile(array);
	}

	/**
	 * 指定文件，向请求端发送文件数据
	 * @param file 文件
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyFile(File file) {
		return replyFile(new File[] { file });
	}

	/**
	 * 指定请求端的回显地址，发送原始字节数组数据。
	 * @param endpoint 请求端回显地址
	 * @param flag 快速异步通信标识
	 * @param primitive 字节数组
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyPrimitive(Cabin endpoint, CastFlag flag, byte[] primitive) {
		Laxkit.nullabled(endpoint);

		// 建立联接和发送
		EchoClient client = createEchoClient(endpoint);
		boolean success = false;
		if (client != null) {
			success = client.post(true, flag, primitive);
		}

		// 统计数据流量（接收和发送）
		addFlow(client);

		Logger.debug(this, "replyPrimitive", success, "reply to %s, data size:%d, send size:%d", 
				endpoint, primitive.length, client.getSendFlowSize());

		// 销毁
		if (client != null) {
			client.destroy();
		}
		return success;
	}

	/**
	 * 指定请求端的回显地址，发送原始字节数组数据。
	 * @param endpoint 请求端回显地址
	 * @param primitive 字节数组
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyPrimitive(Cabin endpoint, byte[] primitive) {
		CastFlag flag = createCastFlag();
		return replyPrimitive(endpoint, flag, primitive);
	}

	/**
	 * 指定请求端的回显地址，发送原始字节数组数据。
	 * @param primitive 字节数组
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyPrimitive(byte[] primitive) {
		Cabin endpoint = getCommandSource();
		return replyPrimitive(endpoint, primitive);
	}

	/**
	 * 指定请求端的回显地址，发送原始字节数组数据。
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 发送成功返回“真”，否则“假”。
	 */
	protected boolean replyPrimitive(byte[] b, int off, int len) {
		byte[] primitive = Arrays.copyOfRange(b, off, off + len);
		return replyPrimitive(primitive);
	}

	/**
	 * 建立基于数据包或者数据流的异步应答客户端
	 * @param hub 目标服务器地址
	 * @param stream 数据流模式
	 * @return 成功返回EchoClient实例，否则是空指针
	 */
	protected EchoClient createEchoClient(Cabin hub, boolean stream) {
		return ClientCreator.createEchoClient(hub, stream);
	}

	/**
	 * 判断系统要求是流连接或者包连接模式，选择建立基于数据流或者数据包的异步应答客户端
	 * @param hub 目标地址
	 * @return 成功返回EchoClient实例，否则是空指针
	 */
	protected EchoClient createEchoClient(Cabin hub) {
		boolean stream = EchoTransfer.isStreamTransfer();
		return createEchoClient(hub, stream);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.SiteInvoker#destroy()
	 */
	@Override
	public void destroy() {
		// 释放父类资源数据
		super.destroy();

		// 如果命令有效且是ShiftCommand，判断钩子是否释放
		if (command != null && isShiftCommand()) {
			// 拿到命令钩子
			CommandHook hook = ((ShiftCommand) command).getHook();
			// 如果钩子有效且处于等待状态时，唤醒它
			if (hook != null && hook.isAwaiting()) {
				hook.done();
			}
		}

		// 释放命令和监听地址
		if (command != null) {
			command = null;
		}
		if (listener != null) {
			listener = null;
		}
	}

	/**
	 * 当前节点做为中继节点，把请求端的命令原样转发给服务器端。
	 * @return 成功返回真，否则假。
	 */
	protected final boolean transmit() {
		// 回显地址必须分配
		if (getListener() == null) {
			throw new InvokerException("cannot be null");
		}

		Command cmd = getCommand();
		Node hub = getHub();
		if (hub == null) {
			replyFault(Major.FAULTED, Minor.CLIENT_ERROR);
			Logger.error(this, "transmit", "cannot be transmit! hub is null pointer!");
			return false;
		}

		// 向注册的服务器提交命令
		boolean success = launchTo(hub, cmd);
		// 失败，反馈结果
		if (!success) {
			replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
		}
		return success;
	}

	/**
	 * 当前节点做为中继节点，把服务端的处理结果数据原样反馈给请求端。
	 * @return 向请求端反馈BANK结果成功返回真，否则假
	 */
	protected final boolean reflect() {
		// 没有完成不处理
		if (!isCompleted()) {
			return true;
		}

		// 取出源地址
		Cabin cabin = getCommandSource();
		EchoClient client = createEchoClient(cabin);
		if (client == null) {
			Logger.error(this, "reflect", "cannot be send to %s", cabin);
			return false;
		}

		CastFlag flag = createCastFlag();
		// 找到0下标的回显缓存
		EchoBuffer buf = findBuffer(0);
		EchoHead head = buf.getHead();
		EchoTail tail = buf.getTail();
		// 替换CAST标记
		if (head.getCastFlag() == null) {
			head.setCastFlag(flag);
		}
		// 更换关键参数，包括：服务端IP地址、客户端站点类型、客户端的ReplyWorker绑定的地址
		else {
			head.getCastFlag().setServer(flag.getServer());
			head.getCastFlag().setClientFamily(flag.getClientFamily());
			head.getCastFlag().setClientHost(flag.getClientHost());
		}

		// 生成异步发送器，当前站点以客户机的身份，向请求端（服务器的身份）发送反馈结果。
		ReplySender sender = new ReplySender();
		// 固定从内存取数据
		sender.setData(buf.getMemory());
		// 快速投递给调用端
		boolean success = client.post(head, tail.getHelp(), sender);

		// 销毁客户端
		client.destroy();

		Logger.debug(this, "reflect", success, "send [%s] to %s", head, cabin);

		// 回传完成
		return useful(success);
	}

	/**
	 * 返回自定义资源代理
	 * @return 自定义资源代理
	 */
	public CustomTrustor getCustomTrustor() {
		return getLauncher().getCustomTrustor();
	}

	/**
	 * 调用当前命令管理池的“press”方法，投递一个新的命令
	 * @param next 转发命令
	 * @return CommandPool接受返回真，否则假
	 */
	protected boolean press(Command next) {
		// 设置命令超时和当前用户的签名
		next.setTimeout(getCommandTimeout());
		next.setIssuer(getIssuer());

		return getCommandPool().press(next);
	}

	/**
	 * 启动异步处理工作。<br>
	 * 此方法是异步调用的必选项，只执行一次。
	 * @return 异步启动成功返回“真”，否则“假”。
	 */
	public abstract boolean launch();

	/**
	 * 执行异步应答操作。<br>
	 * 此方法是异步处理的可选项，允许任意多次迭代的。依据不同命令和业务需求，执行一次或者多次。<br>
	 * 
	 * 此方法承接上一阶段（“launch”方法或者上一次“ending”方法）的操作，
	 * 分析上个阶段的数据处理结果，执行本次数据处理，
	 * 以及对“本阶段是否结束退出和下一阶段处理范围”做出判断和预定义。
	 * 
	 * 当isQuit结果是“真”或者返回“假”时退出。
	 * 
	 * @return 处理成功返回“真”，否则“假”。
	 */
	public abstract boolean ending();

}


///**
// * 客户机向服务器提交一个异步命令。
// * @param hub 目标站点地址
// * @param cmd 异步命令
// * @return 服务器受理返回“真”，否则“假”。
// */
//protected boolean submit(Node hub, Command cmd) {
//	CommandClient client = ClientCreator.create(CommandClient.class, hub);
//	if (client == null) {
//		Logger.error(this, "submit", "cannot be find \"%s\"", hub);
//		return false;
//	}
//
//	boolean exit = false;
//
//	// 向服务器提交命令
//	boolean success = false;
//	try {
//		// 提交命令到服务器，服务器接受返回真，否则假。
//		success = client.submit(cmd);
//
//		//	// 优雅关闭
//		//	client.close();
//
//		exit = true;
//	} catch (VisitException e) {
//		Logger.error(e);
//	}
//
//	// 统计数据流量
//	addFlow(client);
//
//	// 优雅关闭
//	client.close(exit);
//
//	// 强制关闭。如果上面已经执行优雅关闭，这行代码不会起作用。
//	client.destroy();
//
//	// 返回异步受理结果
//	return success;
//}

///**
// * 客户机向服务器提交一个异步命令。
// * @param hub 目标站点地址
// * @param cmd 异步命令
// * @return 服务器受理返回“真”，否则“假”。
// */
//protected boolean submit(Node hub, Command cmd) {
//	CommandClient client = ClientCreator.create(CommandClient.class, hub);
//	if (client == null) {
//		Logger.error(this, "submit", "cannot be find \"%s\"", hub);
//		return false;
//	}
//
//	// 向服务器提交命令
//	boolean success = false;
//	try {
//		// 提交命令到服务器，服务器接受返回真，否则假。
//		success = client.submit(cmd);
//
//		// 关闭socket连接，调用“exit”命令，优雅关闭
//		client.close();
//	} catch (VisitException e) {
//		Logger.error(e);
//	}
//
//	// 统计数据流量
//	addFlow(client);
//
//	// 强制关闭。如果上面已经执行优雅关闭，这行代码不会起作用。
//	client.destroy();
//
//	// 返回异步受理结果
//	return success;
//}
