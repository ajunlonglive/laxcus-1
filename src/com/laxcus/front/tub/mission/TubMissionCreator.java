/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.tub.mission;

import com.laxcus.access.parse.*;
import com.laxcus.command.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.contact.*;
import com.laxcus.command.establish.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.traffic.*;
import com.laxcus.front.tub.invoker.*;
import com.laxcus.mission.*;
import com.laxcus.util.*;

/**
 * 边缘计算容器任务生成器
 * 
 * @author scott.liang
 * @version 1.0 7/4/2019
 * @since laxcus 1.0
 */
public class TubMissionCreator extends MissionCreator {

	/**
	 * 构造默认的边缘容器任务生成器
	 */
	public TubMissionCreator() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.mission.MissionCreator#create(java.lang.String)
	 */
	@Override
	public Command create(String input) throws MissionException {
		Laxkit.nullabled(input);

		Command cmd = null;
		try {
			cmd = split(input);
		} catch (Throwable e) {
			throw new MissionException(e);
		}

		return cmd;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.mission.MissionCreator#create(com.laxcus.command.Command)
	 */
	@Override
	public TubMission create(Command cmd) throws MissionException {
		Laxkit.nullabled(cmd);
		// 返回结果
		return new TubMission(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.mission.MissionCreator#createInvoker(com.laxcus.mission.Mission)
	 */
	@Override
	public TubInvoker createInvoker(Mission m) throws MissionException {
		// 空指针检查
		Laxkit.nullabled(m);
		// 如果不是从TubMission派生，不支持它！
		if (!Laxkit.isClassFrom(m, TubMission.class)) {
			throw new MissionException("cannot be cast! %s", m.getClass().getName());
		}
		TubMission mission = (TubMission) m;

		TubInvoker invoker = null;
		// 取出命令
		Command cmd = mission.getCommand();

		// 判断参数，返回结果
		if (cmd.getClass() == Swarm.class) {
			invoker = new TubSwarmInvoker(mission);
		} else if (cmd.getClass() == BuildHash.class) {
			invoker = new TubBuildHashInvoker(mission);
		} else if(cmd.getClass() == BuildHalf.class) {
			invoker = new TubBuildHalfInvoker(mission);
		}
		// 分布计算/数据重组/迭代计算
		else if (cmd.getClass() == Conduct.class) {
			invoker = new TubConductInvoker(mission);
		} else if (cmd.getClass() == Establish.class) {
			invoker = new TubEstablishInvoker(mission);
		} else if (cmd.getClass() == Contact.class) {
			invoker = new TubContactInvoker(mission);
		}

		return invoker;
	}

	/**
	 * 解析语句，转成命令
	 * @param input LAXCUS分布描述语句
	 * @return 命令实例
	 */
	private Command split(String input) {
		// 本地命令（生成散列码、半截符、设置密文超时、命令模式、命令超时）
		if (checker.isBuildHash(input)) return doHashCommand(input); 
		if (checker.isBuildHalf(input)) return doHalfCommand(input);
		// UDP数据包测试
		if (checker.isSwarm(input)) return doSwarm(input);
		
		// 分布计算和数据重组
		if (checker.isConduct(input)) return doConduct(input);
		if (checker.isEstablish(input)) return doEstablish(input);
		if(checker.isContact(input)) return doContact(input);

		// 不成立，返回空指针
		return null;
	}

	/**
	 * 计算散列码（在本地进行）
	 * @param input 输入语句
	 * @return 返回BuildHash命令
	 */
	private BuildHash doHashCommand(String input) {
		BuildHashParser parser = new BuildHashParser();
		return parser.split(input);
	}

	/**
	 * 计算散列码（在本地进行）
	 * @param input 输入语句
	 * @return 返回BuildHalf命令
	 */
	private BuildHalf doHalfCommand(String input) {
		BuildHalfParser parser = new BuildHalfParser();
		return parser.split(input);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 * @return Swarm实例
	 */
	private Swarm doSwarm(String input) {
		SwarmParser parser = new SwarmParser();
		return parser.split(input, true); // 解析命令
	}

	/**
	 * 解析分布计算
	 * @param input 输入语句
	 * @return Conduct实例
	 */
	private Conduct doConduct(String input) {
		ConductParser parser = new ConductParser();
		return parser.split(input, true);
	}

	/**
	 * 解析数据重组
	 * @param input 输入语句
	 * @return Establish实例
	 */
	private Establish doEstablish(String input) {
		EstablishParser parser = new EstablishParser();
		return parser.split(input, true);
	}
	
	/**
	 * 解析迭代计算
	 * @param input 输入语句
	 * @return Contact实例
	 */
	private Contact doContact(String input) {
		ContactParser parser = new ContactParser();
		return parser.split(input, true);
	}

}