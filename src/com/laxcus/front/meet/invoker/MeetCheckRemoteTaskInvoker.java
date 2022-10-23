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
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.site.*;

/**
 * 检测注册组件异步调用器。
 * 
 * @author scott.liang
 * @version 1.0 11/07/2018
 * @since laxcus 1.0
 */
public class MeetCheckRemoteTaskInvoker extends MeetInvoker {

	/**
	 * 构造检测注册组件异步调用器，指定命令
	 * @param cmd 显示数据表命令
	 */
	public MeetCheckRemoteTaskInvoker(CheckRemoteTask cmd) {
		super(cmd);
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
	public CheckRemoteTask getCommand() {
		return (CheckRemoteTask) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#launch()
	 */
	public boolean launch() {
		// 如果是系统管理员，不能操作！
		if (isAdministrator()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return useful();
		}
		
		TreeSet<Sock> array = new TreeSet<Sock>();

		CheckRemoteTask cmd = getCommand();
		if (cmd.isAll()) {
			for (Phase e : getStaffPool().getPhases()) {
				array.add(e.getSock());
			}
		} else {
			array.addAll(cmd.list());
		}

		// 显示标题
		printTitle();

		// 显示
		for(Sock root: array) {
			// 找组件
			Phase phase = findPhase(root);
			if (phase == null) {
				Object[] a = new Object[] { false, root, "" };
				printRow(a);
				continue;
			}

			// 组件存在，找到匹配的
			NodeSet set = getStaffPool().findTaskSites(phase);
			if (set == null || set.isEmpty()) {
				String str = phase.toString(cmd.isSimple());
				Object[] a = new Object[] { false, str, " " };
				printRow(a);
			} else {
				for (Node hub : set.show()) {
					String str = phase.toString(cmd.isSimple());
					Object[] a = new Object[] { true, str, hub };
					printRow(a);
				}
			}
		}

		// 输出全部记录
		flushTable();

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 找到组件
	 * @param root
	 * @return
	 */
	private Phase findPhase(Sock root) {
		for (Phase e : getStaffPool().getPhases()) {
			if (root.compareTo(e.getSock()) == 0) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 输出标题单元
	 * @return
	 */
	private String[] getTitleCells() {
		// 生成表格标题
		String[] cells = new String[] { "CHECK-REMOTE-TASK/STATUS",
				"CHECK-REMOTE-TASK/TASK", "CHECK-REMOTE-TASK/SITE" };
		return cells;
	}

	/**
	 * 生成标题单元
	 */
	private void printTitle() {
		String[] cells = getTitleCells();
		createShowTitle(cells);
	}

}