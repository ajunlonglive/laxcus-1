/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.site.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.util.*;

/**
 * 命令管理池。<br><br>
 * 
 * 命令管理池遵循“INVOKE/PRODUCE”异步原则处理命令。<br><br>
 * 
 * 命令按照优先级为两种：<1> 普通命令 <2> 快速处理命令（快速处理命令按照等级排列）。管理池首先处理快速处理命令。<br>
 * 
 * 命令管理池方法说明：<br>
 * 1. “accept”方法接受远程异步命令（其它节点发来的异步命令），做出判断后选择是否接受。新命令被保管在运行队列中，在硬件环境许可的情况下按照顺序依次启动。<br>
 * 2. “dispatch”方法在运行环境的硬件条件许可情况下被启动，检查命令和分配对应的异步调用器（EchoInvoker），然后交给InvokerPool处理。<br>
 * 3. “admit”方法受理本地异步命令（区别与“accept”方法），再使用“dispatch”方法分配异步调用器。<br>
 * 4. “press”方法用于本地加急情况，跳过“accept/admit/dispatch”环节，根据命令马上建立一个异步调用器，交给InvokerPool处理。<br><br>
 * 
 * InvokerPool收到异步调用器后，将启动InvokeTrustor，以代理线程方式处理异步调用器。
 * 
 * 删除超时命令，达到这两个条件：
 * 1. 达到系统规定的超时时间，判断是invokerTimeout > 0，这个参数在“conf/local.xml”定义。
 * 2. 达到用户规定的超时时间，参数是Command.getTimeout > 0。
 * 存在一个问题，如果两个判断条件都不成立，那么命令将“死”在管理池中。所以invokerTimeout必须大于0。
 * 
 * @author scott.liang
 * @version 1.3 01/08/2015
 * @since laxcus 1.0
 */
public abstract class CommandPool extends EchoPool {

	/** 来自请求端，普通的异步命令 **/
	private ArrayList<Command> slacks = new ArrayList<Command>(1000);

	/** 优先级大于0的命令，优先普通的异步命令获得处理 **/
	private ArrayList<Command> quicks = new ArrayList<Command>(200);

	/** 极速处理命令，只限系统命令 **/
	private ArrayList<Command> fasts = new ArrayList<Command>(100);

	/**
	 * 构造默认的命令管理池
	 */
	protected CommandPool() {
		super();
		InvokerPool.setCommandPool(this);
	}

	/**
	 * 这个方法在InvokerPool成功释放异步调用器后调用。
	 * 如果CommandPool有处于等待命令，将唤醒线程，启动新任务检查和分配工作。
	 */
	protected void taste() {
		boolean success = (hasQuick() || hasSlack());
		if (success) {
			wakeup();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoPool#init()
	 */
	@Override
	public boolean init() {
		// 启动上级资源
		boolean success = super.init();
		// 启动本地资源
		if (success) {

		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into ...");

		// 超时计时器，以外部规定的时间为准，默认是1分钟
		EchoTimer timer = new EchoTimer(getDisableCheckInterval());

		while (!isInterrupted()) {
			// 必须登录!
			if (isLogined()) {
				// 1. 首先执行极速处理
				boolean success = doFast();
				// 2. 执行快速处理命令
				if (!success) {
					success = doQuick();
				}
				// 3. 执行普通处理命令
				if (!success) {
					success = doSlack();
				}
				// 成功，继续检测下一个
				if (success) {
					continue;
				}
			}

			// 延时
			delay(getSilentInterval());

			// 超时，释放命令
			if (timer.isTimeout()) {
				// 刷新，时间移到下一阶段
				timer.refresh();
				// 释放超时命令，两个条件：1. 达到命令自己规定的时间；2. 达到系统规定的时间
				doReleaseCommand();
			}
		}
		Logger.info(this, "process", "exit");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoPool#finish()
	 */
	@Override
	public void finish() {
		super.finish();

		slacks.clear();
		quicks.clear();
		fasts.clear();
	}
	
	/**
	 * 提取达到超时要求的回显地址
	 * @param array 命令数组
	 * @return 返回回显地址列表
	 */
	private List<Cabin> doReleaseCommand(ArrayList<Command> array) {
		int size = array.size();
		if (size == 0) {
			return null;
		}

		// 系统规定的调用器超时时间
		long sysTimeout = EchoTransfer.getCommandTimeout(); // getMemberTimeout();

		ArrayList<Cabin> a = new ArrayList<Cabin>();
		// 逐一排查
		for (int index = 0; index < array.size(); index++) {
			Command cmd = array.get(index);
			// 删除和忽略可能存在的空指针，然后退出！
			if (cmd == null) {
				array.remove(index);
				break;
			}
			// 判断命令超时
			boolean success = (cmd.isCreateTimeout() || cmd.isCreateTimeout(sysTimeout));
			if (!success) {
				continue;
			}
			// 回显地址有效，保存它
			Cabin cabin = cmd.getSource();
			if (cabin != null) {
				a.add(cabin);
			}
			// 删除指定下标的命令
			array.remove(index);
			// 命令下标前移
			index--;
		}

		return a;
	}

	/**
	 * 释放超时的命令
	 */
	private void doReleaseCommand() {
		int size = size();
		if (size == 0) {
			return;
		}

		ArrayList<Cabin> array = new ArrayList<Cabin>(size);

		// 锁定
		super.lockSingle();
		try {
			// 检查超时的普通命令
			List<Cabin> cabins = doReleaseCommand(slacks);
			if (cabins != null && cabins.size() > 0) {
				array.addAll(cabins);
			}
			// 快速处理命令
			cabins = doReleaseCommand(quicks);
			if (cabins != null && cabins.size() > 0) {
				array.addAll(cabins);
			}
			// 极速处理命令
			cabins = doReleaseCommand(fasts);
			if (cabins != null && cabins.size() > 0) {
				array.addAll(cabins);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 向命令来源发送超时通知
		if (array.size() > 0) {
			for (Cabin source : array) {
				replyTimeout(source);
			}
		}
	}

	/**
	 * 执行极速处理命令。
	 * 极速处理跳过CPU/内存的检查，直接生成调用器
	 * @return 成功返回真，否则假
	 */
	private boolean doFast() {
		boolean success = hasFast();
		if (success) {
			Command cmd = pollFast();
			success = (cmd != null);
			if (success) {
				dispatch(cmd); // 交给子类处理
			}
		}
		return success;
	}

	/**
	 * 执行快速处理命令
	 * @return 成功返回真，否则假
	 */
	private boolean doQuick() {
		// 不成立，退出
		if (!hasQuick()) {
			return false;
		}
		// 检查资源
		boolean success = checkPower();
		// 取出命令
		if (success) {
			Command cmd = pollQuick();
			success = (cmd != null);
			if (success) {
				dispatch(cmd); // 交给子类处理
			}
		}
		return success;
	}

	/**
	 * 执行普通处理命令
	 * @return 成功返回真，否则假
	 */
	private boolean doSlack() {
		// 不成立，退出！
		if (!hasSlack()) {
			return false;
		}
		// 检查负载（剩余线程数、CPU载荷、内存余量、磁盘IO四项工作）
		boolean success = checkPower();
		// 以上通过分派异步命令，或者进入延时
		if (success) {
			Command cmd = pollSlack();
			success = (cmd != null); // 如果弹出空值，这里队列中只有TouchCommand
			if (success) {
				dispatch(cmd); // 派发给子类去处理
			}
		}
		// 返回处理结果
		return success;
	}

	/**
	 * 判断传入的命令与指定类匹配，或者命令是指定类的子类
	 * @param cmd 命令
	 * @param clazz 指定类
	 * @return 匹配成功返回“真”，否则“假”。
	 */
	protected boolean isFrom(Command cmd, Class<?> clazz) {
		if (cmd != null) {
			return super.isFrom(cmd.getClass(), clazz);
		}
		return false;
	}

	/**
	 * 判断是“SWITCH HUB”命令。切换管理站点
	 * 
	 * @param cmd 命令实例
	 * @return 返回“是”或者“否”。
	 */
	protected boolean isSwitchHub(Command cmd) {
		return isFrom(cmd, SwitchHub.class);
	}

	/**
	 * 增加一个本地命令，允许不定义回显地址。<br>
	 * 这个方法只给本地使用，与“accept”不同的是，它跳过命令队列数目检查，直接保存。
	 * @param cmd 命令实例
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean admit(Command cmd) {
		// 忽略空指针
		if (cmd == null) {
			return false;
		}

		// 对极速处理、快速处理、普通命令进行锁定保存
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			if (cmd.isSystemFast()) {
				success = fasts.add(cmd);
				if (success) Collections.sort(fasts);
			} else if (cmd.isQuick()) {
				success = quicks.add(cmd);
				if (success) Collections.sort(quicks); // 调整排序位置
			} else {
				success = slacks.add(cmd);
				// 如果是用户命令，调整命令优先级
				if (success && cmd.isUser()) {
					Collections.sort(slacks); // 调整排序位置
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 唤醒
		if (success) {
			wakeup();
		}
		return success;
	}

	/**
	 * 保存命令到管理池队列。<br><br>
	 * 
	 * 这个方法接受子类“accept”的调用，判断命令“SWIFT”属性，保存到SWIFT/SLACK其中一个命令队列中。<br>
	 * 如果队列中的命令数目已经超过限制值时，将拒绝保存。<br>
	 * 
	 * @param cmd 异步命令
	 * @param check 检查资源许可
	 * @return 添加成功返回“真“，否则”假“。
	 */
	protected boolean add(Command cmd, boolean check) {
		// 忽略空指针
		if (cmd == null) {
			return false;
		}

		// 统计当前队列中的命令数目
		if (check) {
			int size = size();
			// 当前队列数目在允许范围内
			boolean success = (EchoTransfer.getMaxCommands() < 1 || size < EchoTransfer.getMaxCommands());
			if (!success) {
				Logger.warning(this, "add", "command sizeout! %d - %d", EchoTransfer.getMaxCommands(), size);
				return false;
			}
		}

		// 判断极速/快速/普通处理命令，分别保存。极速处理只限系统命令
		boolean success = false;
		// 锁定！
		super.lockSingle();
		try {
			if (cmd.isSystemFast()) {
				success = fasts.add(cmd);
				if (success) Collections.sort(fasts);
			} else if (cmd.isQuick()) {
				success = quicks.add(cmd);
				if (success) Collections.sort(quicks); // 调整排序位置
			} else {
				success = slacks.add(cmd);
				// 如果是用户命令，调整命令优先级
				if (success && cmd.isUser()) {
					Collections.sort(slacks); // 调整排序位置
				}
			}
		} catch (Throwable e) {
			Logger.error(this, "add", "command class is %s", (cmd != null ? cmd.getClass().getName() : "Null!"));
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 唤醒线程
		if (success) {
			wakeup();
		}
		// 返回结果
		return success;
	}

	/**
	 * 保存命令到管理池队列，默认要检查资源。<br>
	 * 
	 * 这个方法接受子类“accept”的调用，判断命令“SWIFT”属性，保存到SWIFT/SLACK其中一个命令队列中。<br>
	 * 如果队列中的命令数目已经超过限制值时，将拒绝保存。<br>
	 * 
	 * @param cmd 异步命令
	 * @return 成功返回真，否则假
	 */
	protected boolean add(Command cmd) {
		return add(cmd, true);
	}

	/**
	 * 弹出命令队列中的一个命令。<br>
	 * 首先判断是定时触发命令（TouchCommand），当它存在时，检查它的触发时间，决定是否弹出里面的实际命令。否则弹出一般命令。
	 * 
	 * @return 返回命令实例或者null
	 */
	private Command pollSlack() {
		Command sub = null;
		// 锁定
		super.lockSingle();
		try {
			int size = slacks.size();
			for (int index = 0; index < size; index++) {
				Command cmd = slacks.get(index);
				// 删除和忽略可能存在的空指针，返回空指针
				if (cmd == null) {
					slacks.remove(index);
					break;
				}
				// 判断是定时触发命令，当达到定时触发时间后，才启动这个命令
				if (cmd.getClass() == TouchCommand.class) {
					TouchCommand touch = (TouchCommand) cmd;
					if (touch.isTouchTimeout()) {
						slacks.remove(index); // 删除内存中的记录
						sub = touch.getCommand(); // 返回实例要传递的命令
						break;
					}
				} else {
					slacks.remove(index); // 删除内存中的记录
					sub = cmd;
					break;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 返回结果
		return sub;
	}

	/**
	 * 判断队列中有命令存在。
	 * @return 返回真或者假。
	 */
	private boolean hasSlack() {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			success = (slacks.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 弹出一个快速处理命令
	 * @return 弹出的快速处理命令，没有返回空指针
	 */
	private Command pollQuick() {
		Command sub = null;
		// 锁定处理
		super.lockSingle();
		try {
			int size = quicks.size();
			for (int index = 0; index < size; index++) {
				Command cmd = quicks.get(index);
				// 删除和忽略可能存在的空指针，返回空指针
				if (cmd == null) {
					quicks.remove(index);
					break;
				}
				if (cmd.getClass() == TouchCommand.class) {
					TouchCommand touch = (TouchCommand) cmd;
					if (touch.isTouchTimeout()) {
						quicks.remove(index); // 删除内存中的记录
						sub = touch.getCommand(); // 返回实例要传递的命令
						break;
					}
				} else {
					quicks.remove(index); // 删除内存中的记录
					sub = cmd;
					break;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 返回结果
		return sub;
	}

	/**
	 * 判断有快速处理命令
	 * @return 返回真或者假
	 */
	private boolean hasQuick() {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			success = (quicks.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 弹出一个极速处理命令
	 * @return 弹出的极速处理命令，没有返回空指针
	 */
	private Command pollFast() {
		Command sub = null;
		// 锁定处理
		super.lockSingle();
		try {
			int size = fasts.size();
			for (int index = 0; index < size; index++) {
				Command cmd = fasts.get(index);
				// 删除和忽略可能存在的空指针，返回空指针
				if (cmd == null) {
					fasts.remove(index);
					break;
				}
				if (cmd.getClass() == TouchCommand.class) {
					TouchCommand touch = (TouchCommand) cmd;
					if (touch.isTouchTimeout()) {
						fasts.remove(index); // 删除内存中的记录
						sub = touch.getCommand(); // 返回实例要传递的命令
						break;
					}
				} else {
					fasts.remove(index); // 删除内存中的记录
					sub = cmd;
					break;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 返回结果
		return sub;
	}

	/**
	 * 判断有极速处理命令
	 * 
	 * @return 返回真或者假
	 */
	private boolean hasFast() {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			success = (fasts.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 向命令来源发送不支持的操作
	 * @param cmd 来源命令
	 */
	protected boolean unsupport(Command cmd) {
		// 如果是本地转发命令，但是没有对应调用器，这是开发者错误
		if (Laxkit.isClassFrom(cmd, ShiftCommand.class)) {
			ShiftCommand shift = (ShiftCommand) cmd;
			if (shift.getHook() != null) {
				shift.getHook().done(); // 唤醒线程
			}
			Logger.error(this, "unsupport", "cannot be find invoker for '%s'",
					cmd.getClass().getSimpleName());
			return false;
		}

		// 如果不需要反馈，直接退出
		if (cmd.isDirect()) {
			Logger.error(this, "unsupport", "cannot be support '%s'", cmd);
			return false;
		}
		Cabin endpoint = cmd.getSource();
		// 没有定义回显地址，这是一个本地命令，不需要处理
		if (endpoint == null) {
			return false;
		}
		// 发送错误提示到目标站点
		EchoClient client = createClient(endpoint);
		if (client == null) {
			Logger.error(this, "unsupport", "cannot be create client");
			return false;
		}
		// 生成报头，发送给请求端
		EchoHead head = new EchoHead(Major.FAULTED, Minor.UNSUPPORT, 0);
		boolean success = client.shoot(head);
		// 关闭连接
		client.destroy();
		return success;
	}

	/**
	 * 统计命令成员数目
	 * @return 命令数目
	 */
	public int size() {
		int count = 0;
		// 锁定
		super.lockSingle();
		try {
			count = (fasts.size() + quicks.size() + slacks.size());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return count;
	}

	/**
	 * 输出全部命令的数据副本
	 * @return 命令实例
	 */
	public List<Command> getCommands() {
		ArrayList<Command> array = new ArrayList<Command>();
		// 锁定！
		super.lockSingle();
		try {
			for (Command e : slacks) {
				array.add(e.duplicate());
			}
			for (Command e : quicks) {
				array.add(e.duplicate());
			}
			for(Command e : fasts) {
				array.add(e.duplicate());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return array;
	}

	/**
	 * 输出对列中的命令
	 * @return 字符串集合
	 */
	public List<String> printCommands() {
		int capacity = size();
		ArrayList<String> array = new ArrayList<String>(capacity + 10);

		super.lockSingle();
		try {
			for (Command cmd : slacks) {
				String s = String.format("%s - %d", cmd.getClass().getName(),cmd.getRunTime());
				array.add(s);
			}
			for (Command cmd : quicks) {
				String s = String.format("%s - %d", cmd.getClass().getName(),cmd.getRunTime());
				array.add(s);
			}
			for (Command cmd : fasts) {
				String s = String.format("%s - %d", cmd.getClass().getName(), cmd.getRunTime());
				array.add(s);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return array;
	}

	/**
	 * 查找指定的命令
	 * @param clazz 命令类
	 * @return 返回关联的命令数组
	 */
	public List<Command> findCommands(Class<?> clazz) {
		ArrayList<Command> array = new ArrayList<Command>();
		// 锁定
		super.lockSingle();
		try {
			for (Command e : slacks) {
				if (Laxkit.isClassFrom(e, clazz)) {
					array.add(e.duplicate());
				}
			}
			for (Command e : quicks) {
				if (Laxkit.isClassFrom(e, clazz)) {
					array.add(e.duplicate());
				}
			}
			for (Command e : fasts) {
				if (Laxkit.isClassFrom(e, clazz)) {
					array.add(e.duplicate());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return array;
	}

	/**
	 * 受理来自其它站点的异步命令。在命令中必须含有回显地址，否则不接受。
	 * @param cmd 异步操作命令
	 * @return 接受返回“真”，否则“假”。
	 */
	public abstract boolean accept(Command cmd);

	/**
	 * 检查一个命令和分配它对应的命令调用器。<br><br>
	 * 命令的来源是“accept”和“admit”方法。成功将产生命令对应的异步调用器，
	 * 然后转交给InvokerPool.launch分配运行参数和启动。否则，将向客户机发送一个“系统不支持”的应答。
	 * 
	 * @param cmd 异步操作命令
	 * @return 分派成功返回“真”，否则“假”。
	 */
	protected abstract boolean dispatch(Command cmd);

	/**
	 * 快速提交和执行命令。<br>
	 * 
	 * 这个方法被本地站点使用，是启动命令调用器的快车道。
	 * 它跳过“accpet/admit”与“dispatch”方法之间的硬件资源判断，不需要等待
	 * 判断命令后启动对应的异步调用器，交给InvokerPool处理。
	 * 
	 * “press”方法只在加急的情况下使用，本地非加急处理仍然需要走“admit/dispatch”道路。
	 * 
	 * @param cmd 异步操作命令
	 * @return 启动成功返回”真“，否则”假“。
	 */
	public abstract boolean press(Command cmd);
}


///*
// * (non-Javadoc)
// * @see com.laxcus.thread.VirtualThread#process()
// */
//@Override
//public void process() {
//	Logger.info(this, "process", "into ...");
//
//	// 超时计时器，以外部规定的时间为准，默认是1分钟
//	EchoTimer timer = new EchoTimer(getDisableCheckInterval());
//
//	while (!isInterrupted()) {
//		// 必须登录!
//		if (isLogined()) {
//			// 1. 首先执行极速处理
//			boolean success = doFast();
//			// 2. 执行快速处理命令
//			if (!success) {
//				success = doQuick();
//			}
//			// 3. 执行普通处理命令
//			if (!success) {
//				success = doSlack();
//			}
//			// 成功，继续检测下一个
//			if (success) {
//				downSilentInterval(); // 连续处理发生，降低时延1秒钟
//				continue;
//			} else {
//				upSilentInterval(); // 没有任务发生，降低时延1秒钟
//			}
//		} else {
//			// 没有登录，增加时延
//			upSilentInterval();
//		}
//
//		// 延时，时间由系统动态调整
//		delay(getSilentInterval());
//
//		// 超时，释放命令
//		if (timer.isTimeout()) {
//			// 刷新，时间移到下一阶段
//			timer.refresh();
//			// 释放超时命令，两个条件：1. 达到命令自己规定的时间；2. 达到系统规定的时间
//			doReleaseCommand();
//		}
//	}
//	Logger.info(this, "process", "exit");
//}