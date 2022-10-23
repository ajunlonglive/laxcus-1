/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.establish.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.distribute.establish.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * MODULATE命令转义器
 * 
 * @author scott.liang
 * @version 1.0 2/8/2018
 * @since laxcus 1.0
 */
public class ModulateShifter extends DriverShifter {
	
	/**
	 * 构造默认的MODULATE命令转义器
	 */
	public ModulateShifter(){
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.driver.invoker.DriverShifter#createInvoker(com.laxcus.front.driver.mission.DriverMission)
	 */
	@Override
	public DriverInvoker createInvoker(DriverMission mission) {
		// 必须是指定命令
		if (!Laxkit.isClassFrom(mission.getCommand(), Modulate.class)) {
			mission.setException("cannot be cast!");
			return null;
		}

		Modulate modulate = (Modulate) mission.getCommand();
		Dock dock = modulate.getDock();
		
		// 系统命名，这个命名是固定的，在组件配置中存在！
		final String rootText = "MODULATE"; 
		Sock root = Sock.doSystemSock(rootText);
		// 生成ESTABLISH命令
		Establish cmd = new Establish(root);

		// 以排它方式独享一个表的全部资源
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(dock.getSpace());

		// 分配SCAN阶段对象（必须！在ModulateIssueTask中检查）
		Phase scanPhase = new Phase(getUsername(), PhaseTag.SCAN, root);
		ScanObject scanObject = new ScanObject(scanPhase);
		// SCAN参数输入器
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
		cmd.setScanObject(scanObject);
		cmd.setSiftObject(siftObject);
		cmd.setRiseObject(riseObject);

		// 保存MODULATE命令原语
		cmd.setPrimitive(modulate.getPrimitive());
		// 更改命令
		mission.setCommand(cmd);

		// 生成数据构建任务调用器
		return new DriverEstablishInvoker(mission); 
	}

}
