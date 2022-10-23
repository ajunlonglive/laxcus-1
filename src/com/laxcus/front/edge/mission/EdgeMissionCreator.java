/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.edge.mission;

import com.laxcus.access.parse.*;
import com.laxcus.command.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.edge.*;
import com.laxcus.front.edge.invoker.*;
import com.laxcus.mission.*;
import com.laxcus.util.*;

/**
 * 边缘计算任务生成器 <br>
 * 
 * 边缘计算任务生成器将命令文本语句转换成类实例边缘计算任务，或者将命令包装到边缘计算任务的服务。
 * 
 * @author scott.liang
 * @version 1.0 7/30/2019
 * @since laxcus 1.0
 */
public class EdgeMissionCreator extends MissionCreator {

	/**
	 * 构造默认的任务生成器
	 */
	public EdgeMissionCreator() {
		super();
	}

	/**
	 * 返回当前用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return EdgeLauncher.getInstance().getUsername();
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
	public EdgeMission create(Command cmd) throws MissionException {
		Laxkit.nullabled(cmd);
		
		return new EdgeMission(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.mission.MissionCreator#createInvoker(com.laxcus.mission.Mission)
	 */
	@Override
	public EdgeInvoker createInvoker(Mission m) throws MissionException {
		Laxkit.nullabled(m);
		// 类实例检测
		if (!Laxkit.isClassFrom(m, EdgeMission.class)) {
			throw new MissionException("cannot be cast! %s", m.getClass().getName());
		}

		EdgeMission mission = (EdgeMission) m;
		// 取出命令
		Command cmd = mission.getCommand();

		EdgeInvoker invoker = null;

		// 本地工具命令（散列命令、半截符命令、设置密文超时、命令模式、命令超时）
		if (cmd.getClass() == BuildHash.class) {
			invoker = new EdgeBuildHashInvoker(mission);
		} else if (cmd.getClass() == BuildHalf.class) {
			invoker = new EdgeBuildHalfInvoker(mission);
		} else if (cmd.getClass() == CipherTimeout.class) {
			invoker = new EdgeCipherTimeoutInvoker(mission);
		} else if (cmd.getClass() == CommandTimeout.class) {
			invoker = new EdgeCommandTimeoutInvoker(mission);
		} else if (cmd.getClass() == CommandMode.class) {
			invoker = new EdgeCommandModeInvoker(mission);
		}
		// UDP数据流测试
		else if (cmd.getClass() == Swarm.class) {
			invoker = new EdgeSwarmInvoker(mission);
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
		if (checker.isCipherTimeout(input)) return doSetCipherTimeout(input);
		if (checker.isCommandMode(input)) return doSetCommandMode(input);
		if (checker.isCommandTimeout(input)) return doSetCommandTimeout(input);
		
		// UDP数据包测试
		if (checker.isSwarm(input)) return doSwarm(input);

		// 判断是自定义命令和解析命令（放在最后）
		if (CustomCreator.isCommand(input)) {
			return CustomCreator.split(input);
		}

		// 返回命令
		return null;
	}
	
	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private Swarm doSwarm(String input) {
		SwarmParser parser = new SwarmParser();
		return parser.split(input, true); // 解析命令
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
	 * 设置客户端密文
	 * @param input 输入语句
	 * @return 返回CipherTimeout命令
	 */
	private CipherTimeout doSetCipherTimeout(String input) {
		CipherTimeoutParser parser = new CipherTimeoutParser();
		return parser.split(input);
	}

	/**
	 * 设置命令模式
	 * @param input 输入语句
	 * @return 返回CommandMode命令
	 */
	private CommandMode doSetCommandMode(String input) {
		CommandModeParser parser = new CommandModeParser();
		return parser.split(input);
	}

	/**
	 * 设置命令超时
	 * @param input 输入语句
	 * @return 返回CommandTimeout命令
	 */
	private CommandTimeout doSetCommandTimeout(String input) {
		CommandTimeoutParser parser = new CommandTimeoutParser();
		return parser.split(input);
	}
}