/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.command.site.front.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 打印本地任务组件调用器。<br>
 * 
 * 只在FRONT节点有效。
 * 
 * @author scott.liang
 * @version 1.0 9/20/2019
 * @since laxcus 1.0
 */
public class MeetCheckLocalTaskInvoker extends MeetInvoker {

	/**
	 * 构造打印本地任务组件调用器，指定命令
	 * @param cmd 打印本地任务组件
	 */
	public MeetCheckLocalTaskInvoker(CheckLocalTask cmd) {
		super(cmd);
		
//		// 测试
//		test();
//		test2();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#isDistributed()
	 */
	@Override
	public boolean isDistributed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckLocalTask getCommand() {
		return (CheckLocalTask) super.getCommand();
	}
	
	/**
	 * 判断是私有组件，包括系统组件和自己发布的
	 * @param phase 阶段命名
	 * @return 返回真或者假
	 */
	private boolean isPrivate(Phase phase) {
		boolean success = phase.isSystemLevel();
		if (!success) {
			Siger siger = getUsername();
			success = (Laxkit.compareTo(phase.getIssuer(), siger) == 0);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果是系统管理员，不能操作！
		if (isAdministrator()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return useful();
		}
		
		CheckLocalTask cmd = getCommand();

		// 保存！
		ArrayList<Phase> array = new ArrayList<Phase>();
		array.addAll(PutTaskPool.getInstance().getPhases());
		array.addAll(EndTaskPool.getInstance().getPhases());
		array.addAll(NearTaskPool.getInstance().getPhases());

		createShowTitle(new String[] { "CHECK-LOCAL-TASK/STATUS", "CHECK-LOCAL-TASK/TASK" });
		
		if (cmd.isAll()) {
			for (Phase e : array) {
				// 判断是私有组件
				if (!isPrivate(e)) {
					continue;
				}
				String str = e.toString(cmd.isSimple());
				printRow(new Object[] { true, str });
			}
		} else {
			List<Sock> roots = cmd.list();
			for (Sock root : roots) {
				int count = 0;
				// 逐一检查
				for (Phase e : array) {
					if (Laxkit.compareTo(e.getSock(), root) == 0) {
						if (isPrivate(e)) {
							String str = e.toString(cmd.isSimple());
							printRow(new Object[] { true, str });
							count++;
						}
					}
				}
				// 没找到，显示错误
				if (count == 0) {
					printRow(new Object[] { false, root });
				}
			}
		}

		// 输出全部记录
		flushTable();

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

//	/**
//	 * 测试类加载
//	 */
//	private void test() {
//		String name = "org.aixbit.ext.print.TubPrinter";
//
//		Class<?> clazz = null;
//		try {
//			clazz = Class.forName(name);
//		} catch (ClassNotFoundException e) {
//			Logger.error(e);
//		}
//
//		boolean success = (clazz != null);
//		Logger.note(this, "test", success, "class forname %s", name);
//		if (success) {
//			Logger.info(this, "test", "class name is %s", clazz.getName());
//		}
//	}
//	
//	/**
//	 * 测试类加载
//	 */
//	private void test2() {
//		String name = "org.aixbit.ext.print.TubPrinter";
//
//		Class<?> clazz = null;
//		try {
//			clazz = Class.forName(name, true, ExtClassPool.getInstance().getClassLoader() );
//		} catch (ClassNotFoundException e) {
//			Logger.error(e);
//		}
//
//		boolean success = (clazz != null);
//		Logger.note(this, "test2", success, "class forname %s", name);
//		if (success) {
//			Logger.info(this, "test2", "class name is %s", clazz.getName());
//		}
//	}
//	
//	private void testThread() {
//		Thread e = Thread.currentThread();
//		Logger.debug(this, "testThread", "thread id:%d - %d", e.getId(), super.getThreadId());
//		Logger.debug(this, "testThread", "class loader is %s", e.getContextClassLoader().getClass().getName() );
//	}
}