/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

/**
 * 命令优先级排序器。
 * 数值越大，优先级最高。
 * 
 * @author scott.liang
 * @version 1.0 5/09/2012
 * @since laxcus 1.0
 */
public final class CommandPriority {

	/** 无定义优先级 **/
	public static final byte NONE = 0;

	/** 属于用户的最小优先级 **/
	public static final byte MIN = 0x10;

	/** 属于用户的普通优先级 **/
	public static final byte NORMAL = 0x20;

	/** 属于用户的最大优先级 **/
	public static final byte MAX = 0x30;

	/** 快速处理 **/
	public static final byte QUICK = 0x40;
	
	/** 极速处理。只允许系统内部使用，是占用资源少、耗时短、必须马上处理的命令。**/
	public static final byte FAST = 0x50;

	/**
	 * 判断是规定的优先级
	 * @param no 优先级编号
	 * @return 返回真或者假
	 */
	public static boolean isPriority(byte no) {
		return no >= CommandPriority.NONE
				&& no <= CommandPriority.FAST;
	}

	/**
	 * 判断是规定的优先级
	 * @param who 字符串
	 * @return 返回真或者假
	 */
	public static boolean isPriority(String who) {
		byte no = CommandPriority.translate(who);
		return CommandPriority.isPriority(no);
	}
	
	/**
	 * 判断是规定的用户层面的优先级
	 * @param who 字符串
	 * @return 返回真或者假
	 */
	public static boolean isUserPriority(byte no) {
		return no >= CommandPriority.NONE && no <= CommandPriority.MAX;
	}

	/**
	 * 判断是快速处理，编号大于最小优先级就属于快速处理
	 * 
	 * @param no 优先级编号
	 * @return 返回真或者假
	 */
	public static boolean isQuick(byte no) {
		return no == CommandPriority.QUICK;
	}

	/**
	 * 判断是闪速处理
	 * 
	 * @param no 优先级编号
	 * @return 返回真或者假
	 */
	public static boolean isFast(byte no) {
		return no == CommandPriority.FAST;
	}
	
	/**
	 * 解析命令优先级类型
	 * @param who 
	 * @return 返回字符串描述
	 */
	public static String translate(byte who) {
		switch (who) {
		case CommandPriority.FAST:
			return "FAST";
		case CommandPriority.QUICK:
			return "QUICK";
		case CommandPriority.MAX:
			return "MAX";
		case CommandPriority.NORMAL:
			return "NORMAL";
		case CommandPriority.MIN:
			return "MIN";
		case CommandPriority.NONE:
			return "NONE";
		}
		return "UNKNOWN";
	}

	/**
	 * 命令优先级类型
	 * @param who
	 * @return 返回数字描述
	 */
	public static byte translate(String who) {
		if (who.matches("^\\s*(?i)(FAST)\\s*$")) {
			return CommandPriority.FAST;
		} else if (who.matches("^\\s*(?i)(QUICK)\\s*$")) {
			return CommandPriority.QUICK;
		} else if (who.matches("^\\s*(?i)(MAX)\\s*$")) {
			return CommandPriority.MAX;
		} else if (who.matches("^\\s*(?i)(NORMAL)\\s*$")) {
			return CommandPriority.NORMAL;
		} else if (who.matches("^\\s*(?i)(MIN)\\s*$")) {
			return CommandPriority.MIN;
		} else if (who.matches("^\\s*(?i)(NONE)\\s*$")) {
			return CommandPriority.NONE;
		}
		return -1;
	}

}