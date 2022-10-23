/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

/**
 * 节点级别标记。<br><br>
 * 
 * 节点级别是节点类型之下的子属性，包括：<br>
 * 1. TOP/BANK/HOME节点的“管理节点/监视器节点”。<br>
 * 2. DATA节点的“主节点/从节点”。“主/从”只适用于DATA节点，BUILD固定为主节点。<br>
 * 3. FRONT节点的几种类型：驱动、边缘节点、控制台、图形终端、桌面、应用程序。<br>
 * <br>
 * 
 * @author scott.liang
 * @version 1.0 5/20/2009
 * @since laxcus 1.0
 */
public final class RankTag {

	/** 无定义 **/
	public final static byte NONE = 0;

	/** TOP/BANK/HOME节点，注意！不要改变它！**/
	
	/** 交换中心管理节点，负责所在集群的状态 **/
	public final static byte MANAGER = 1;

	/** 交换中心监视器节点，监视管理节点，发生故障时切换成管理节点！**/
	public final static byte MONITOR = 2;

	/** DATA节点，注意！不要改变它，关联着底层的动态链接库！**/
	
	/** DATA主节点 **/
	public final static byte MASTER = 3;

	/** DATA从节点 **/
	public final static byte SLAVE = 4;
	
	/** 以下是FRONT节点的类型 **/
	
	/** 字符控制台 **/
	public final static byte CONSOLE = 11;

	/** 图形终端 **/
	public final static byte TERMINAL = 12;

	/** 驱动接口 **/
	public final static byte DRIVER = 13;

	/** 边缘节点 **/
	public final static byte EDGE = 14;

	/** 可视化虚拟桌面，类似于WINDOWS/MACINTOSH的桌面，桌面上显示应用图标，双点启动图标 **/
	public final static byte DESKTOP = 15;

	/** 应用终端，独立的应用软件 **/
	public final static byte APPLICATION = 16;
	
	/** 代理，把第三方转换成LAXCUS本地指令的应用。未使用 **/
	public final static byte PROXY = 17;
	
	/** 以下是WATCH节点的类型 **/
	
	/** WATCH管理员，应用软件的界面 **/
	public final static byte ADMINISTRATOR = 21;

	/** 可视化虚拟工作台，界面同DESKTOP **/
	public final static byte BENCH = 22;

	/**
	 * 判断当前是不是一个级别标记
	 * @param who 节点级别标记
	 * @return 返回真或者假
	 */
	public static boolean isRank(byte who) {
		switch (who) {
		// TOP/HOME/BANK节点
		case RankTag.MANAGER:
		case RankTag.MONITOR:
		// DATA节点
		case RankTag.MASTER:
		case RankTag.SLAVE:
		// FRONT节点
		case RankTag.CONSOLE:
		case RankTag.TERMINAL:
		case RankTag.DRIVER:
		case RankTag.EDGE:
		case RankTag.DESKTOP:
		case RankTag.APPLICATION:
		// WATCH节点
		case RankTag.ADMINISTRATOR:
		case RankTag.BENCH:
			return true;
		}
		return false;
	}

	/**
	 * 根据字符串描述，翻译成对应的节点级别定义。字符串在DATA节点的local.xml配置中。
	 * @param input 输入语句
	 * @return 匹配返回节点级别编号，否则是 -1
	 */
	public static byte translate(String input) {
		// DATA节点
		if (input.matches("^\\s*(?i)(?:MASTER|PRIMARY|MAIN)\\s*$")) {
			return RankTag.MASTER;
		} else if (input.matches("^\\s*(?i)(?:SLAVE|SECONDARY|SUB)\\s*$")) {
			return RankTag.SLAVE;
		} 
		// TOP/HOME/BANK
		else if (input.matches("^\\s*(?i)(?:MANAGER)\\s*$")) {
			return RankTag.MANAGER;
		} else if (input.matches("^\\s*(?i)(?:MONITOR)\\s*$")) {
			return RankTag.MONITOR;
		} 
		// FRONT节点
		else if (input.matches("^\\s*(?i)(?:DRIVER)\\s*$")) {
			return RankTag.DRIVER;
		} else if (input.matches("^\\s*(?i)(?:EDGE)\\s*$")) {
			return RankTag.EDGE;
		} else if (input.matches("^\\s*(?i)(?:CONSOLE)\\s*$")) {
			return RankTag.CONSOLE;
		} else if (input.matches("^\\s*(?i)(?:TERMINAL)\\s*$")) {
			return RankTag.TERMINAL;
		} else if (input.matches("^\\s*(?i)(?:DESKTOP)\\s*$")) {
			return RankTag.DESKTOP;
		} else if (input.matches("^\\s*(?i)(?:APPLICATION)\\s*$")) {
			return RankTag.APPLICATION;
		}
		// WATCH节点
		else if (input.matches("^\\s*(?i)(?:ADMINISTRATOR)\\s*$")) {
			return RankTag.ADMINISTRATOR;
		} else if (input.matches("^\\s*(?i)(?:BENCH)\\s*$")) {
			return RankTag.BENCH;
		} 
		
		return -1;
	}

	/**
	 * 将数据的节点级别转成文本描述
	 * @param who  节点级别
	 * @return 返回字符串描述
	 */
	public static String translate(byte who) {
		switch (who) {
		case RankTag.MASTER:
			// return "master";
			// return "main";
			return "primary";
		case RankTag.SLAVE:
			// return "slave";
			// return "sub";
			return "secondary";
		case RankTag.MANAGER:
			return "manager";
		case RankTag.MONITOR:
			return "monitor";
		case RankTag.DRIVER:
			return "driver";
		case RankTag.EDGE:
			return "edge";
		case RankTag.CONSOLE:
			return "console";
		case RankTag.TERMINAL:
			return "terminal";
		case RankTag.DESKTOP:
			return "desktop";
		case RankTag.APPLICATION:
			return "application";
		case RankTag.ADMINISTRATOR:
			return "administrator";
		case RankTag.BENCH:
			return "bench";
		}
		return "none";
	}

	/**
	 * 判断无定义
	 * @param who 节点级别
	 * @return 返回真或者假
	 */
	public static boolean isNone(byte who) {
		return who == RankTag.NONE;
	}

	/**
	 * 判断是主节点
	 * @param who 节点级别
	 * @return 返回真或者假
	 */
	public static boolean isMaster(byte who) {
		return who == RankTag.MASTER;
	}

	/**
	 * 判断是从节点
	 * @param who 节点级别
	 * @return 返回真或者假
	 */
	public static boolean isSlave(byte who) {
		return who == RankTag.SLAVE;
	}

	/**
	 * 判断是交换中心的管理节点
	 * @param who 节点级别
	 * @return 返回真或者假
	 */
	public static boolean isManager(byte who) {
		return who == RankTag.MANAGER;
	}

	/**
	 * 判断是交换中心的监视器节点。监视器节点关联同级管理节点
	 * @param who 节点级别
	 * @return 返回真或者假
	 */
	public static boolean isMonitor(byte who) {
		return who == RankTag.MONITOR;
	}
	
	/**
	 * 判断是驱动
	 * @param who FRONT节点类型
	 * @return 返回真或者假
	 */
	public static boolean isDriver(byte who) {
		return who == RankTag.DRIVER;
	}

	/**
	 * 判断是边缘节点
	 * @param who FRONT节点类型
	 * @return 返回真或者假
	 */
	public static boolean isEdge(byte who) {
		return who == RankTag.EDGE;
	}

	/**
	 * 判断是字符控制台
	 * @param who FRONT节点类型
	 * @return 返回真或者假
	 */
	public static boolean isConsole(byte who) {
		return who == RankTag.CONSOLE;
	}

	/**
	 * 判断是图形终端
	 * @param who FRONT节点类型
	 * @return 返回真或者假
	 */
	public static boolean isTerminal(byte who) {
		return who == RankTag.TERMINAL;
	}

	/**
	 * 判断是虚拟桌面 
	 * @param who FRONT节点类型
	 * @return 返回真或者假
	 */
	public static boolean isDesktop(byte who) {
		return who == RankTag.DESKTOP;
	}

	/**
	 * 判断是客户端应用软件
	 * @param who FRONT节点类型
	 * @return 返回真或者假
	 */
	public static boolean isApplication(byte who) {
		return who == RankTag.APPLICATION;
	}

	/**
	 * 判断是WATCH节点管理员
	 * @param who
	 * @return
	 */
	public static boolean isAdministrator(byte who) {
		return who == RankTag.ADMINISTRATOR;
	}

	/**
	 * 判断是WATCH节点工作台，以桌面形式出现
	 * @param who
	 * @return
	 */
	public static boolean isBench(byte who) {
		return who == RankTag.BENCH;
	}
}