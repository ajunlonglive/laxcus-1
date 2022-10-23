/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.establish.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.distribute.establish.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * FRONT站点的数据调整命令的调用器 <br><br>
 * 
 * 这是REGULATE命令在BUILD站点的操作，它将转换为“establish modulate scan [schema.table] ”，以ESTABLISH的命令形式去分布执行。
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopModulateInvoker extends DesktopInvoker {

	/**
	 * 构造数据调整命令的调用器
	 * @param cmd 数据调整命令
	 */
	public DesktopModulateInvoker(Modulate cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Modulate getCommand() {
		return (Modulate) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Modulate cmd = getCommand();
		Dock dock = cmd.getDock();

		// 系统命名，这个命名是固定的，在组件配置中存在！
		final String rootText = "MODULATE"; 
		Sock root = Sock.doSystemSock(rootText);
		// 生成ESTABLISH命令
		Establish establish = new Establish(root);

		// 以排它方式独享一个表的全部资源
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(dock.getSpace());

		// 分配SCAN阶段对象（必须！在ModulateIssueTask中检查）
		Phase scanPhase = new Phase(getUsername(), PhaseTag.SCAN, root);
		ScanObject scanObject = new ScanObject(scanPhase);

		ScanInputter scanInputter = new ScanInputter(scanPhase);
		scanInputter.addSpace(dock.getSpace());
		scanInputter.addRule(rule);
		scanObject.addInputter(scanInputter);

		// 分配SIFT阶段对象（必须！在ModulateIssueTask中检查）
		Phase siftPhase = new Phase(getUsername(), PhaseTag.SIFT, root);
		SiftObject siftObject = new SiftObject(siftPhase);

		SiftInputter siftInputter = new SiftInputter(siftPhase);
		siftInputter.addDock(dock);
		siftObject.setInputter(siftInputter);
		
		// 分配RISE阶段对象
		Phase risePhase = new Phase(getUsername(), PhaseTag.RISE, root);
		RiseObject riseObject = new RiseObject(risePhase);

		// 设置阶段对象
		establish.setScanObject(scanObject);
		establish.setSiftObject(siftObject);
		establish.setRiseObject(riseObject);

		// 保存MODULATE命令原语
		establish.setPrimitive(cmd.getPrimitive());

		// 转发给FRONT命令管理池，按照ESTABLISH命令去处理
		boolean success = getCommandPool().press(establish, getDisplay());

		// 不成功时
		if (!success) {
			super.faultX(FaultTip.FAILED_X, cmd.getPrimitive());
		}
		
		// 无论成功或者失败，都退出
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}