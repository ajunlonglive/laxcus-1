/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.util.*;

import com.laxcus.util.naming.*;

/**
 * 附件投递坐标
 * 
 * @author scott.liang
 * @version 1.0 10/7/2019
 * @since laxcus 1.0
 */
public class PublishTaskAxes {
	
	/**
	 * CONTACT枚举值
	 * @return
	 */
	public static int[] contact() {
		return new int[] { PhaseTag.DISTANT, PhaseTag.NEAR , PhaseTag.FORK, PhaseTag.MERGE };
	}

	/**
	 * ESTABLISH枚举值
	 * @return 阶段命名类型数组
	 */
	public static int[] establish() {
		return new int[] { PhaseTag.ISSUE, PhaseTag.SCAN, PhaseTag.SIFT,
				PhaseTag.RISE, PhaseTag.ASSIGN, PhaseTag.END };
	}
	
	/**
	 * CONDUCT枚举值
	 * @return 阶段命名类型数组
	 */
	public static int[] conduct() {
		return new int[] { PhaseTag.INIT, PhaseTag.FROM, PhaseTag.TO,
				PhaseTag.BALANCE, PhaseTag.PUT };
	}


	/**
	 * 返回CONTACT的字符串描述
	 * @return String数组
	 */
	public static String[] getSwiftFrontString() {
		int[] all = PublishTaskAxes.contact();
		ArrayList<String> a = new ArrayList<String>();
		// 转成字符串
		for (int who : all) {
			a.add(PublishTaskAxes.translate(who));
		}
		
		String[] str = new String[a.size()];
		return a.toArray(str);
	}

	/**
	 * 返回CONTACT的字符串描述
	 * @return String数组
	 */
	public static String[] getSwiftWatchString() {
		int[] all = PublishTaskAxes.contact();
		ArrayList<String> a = new ArrayList<String>();
		// 转成字符串
		for (int who : all) {
			// 忽略FRONT节点
			if (PhaseTag.onFrontSite(who)) {
				continue;
			}
			a.add(PublishTaskAxes.translate(who));
		}
		
		String[] str = new String[a.size()];
		return a.toArray(str);
	}
	
	/**
	 * 返回ESTABLISH的字符串描述
	 * @return String数组
	 */
	public static String[] getEstablishFrontString() {
		int[] all = PublishTaskAxes.establish();
		ArrayList<String> a = new ArrayList<String>();
		// 转成字符串
		for (int who : all) {
			a.add(PublishTaskAxes.translate(who));
		}
		
		String[] str = new String[a.size()];
		return a.toArray(str);
	}
	
	/**
	 * 返回ESTABLISH的字符串描述
	 * @return String数组
	 */
	public static String[] getEstablishWatchString() {
		int[] all = PublishTaskAxes.establish();
		ArrayList<String> a = new ArrayList<String>();
		// 转成字符串
		for (int who : all) {
			// 忽略FRONT节点
			if (PhaseTag.onFrontSite(who)) {
				continue;
			}
			a.add(PublishTaskAxes.translate(who));
		}
		
		String[] str = new String[a.size()];
		return a.toArray(str);
	}
	

	/**
	 * 返回CONDUCT的字符串描述
	 * @return String数组
	 */
	public static String[] getConductFrontString() {
		int[] all = PublishTaskAxes.conduct();
		ArrayList<String> a = new ArrayList<String>();
		// 转成字符串
		for (int who : all) {
			a.add(PublishTaskAxes.translate(who));
		}
		
		String[] str = new String[a.size()];
		return a.toArray(str);
	}
	
	/**
	 * 返回CONDUCT的字符串描述
	 * @return String数组
	 */
	public static String[] getConductWatchString() {
		int[] all = PublishTaskAxes.conduct();
		ArrayList<String> a = new ArrayList<String>();
		// 转成字符串
		for (int who : all) {
			// 忽略！
			if (PhaseTag.onFrontSite(who)) {
				continue;
			}
			a.add(PublishTaskAxes.translate(who));
		}
		
		String[] str = new String[a.size()];
		return a.toArray(str);
	}

	/**
	 * 判断阶段执行阶段有效，允许是CONDUCT/ESTABLISH/CONTACT中的任意一个
	 * @param who 阶段类型
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isPhase(int who) {
		return PublishTaskAxes.isConduct(who) || PublishTaskAxes.isEstablish(who) || PublishTaskAxes.isSwift(who);
	}

	/**
	 * 判断是否CONDUCT阶段类型
	 * @param who 阶段类型编号
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public static boolean isConduct(int who) {
		return PhaseTag.isConduct(who);
	}

	/**
	 * 判断是ESTABLISH阶段类型
	 * @param who 阶段类型编号
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public static boolean isEstablish(int who) {
		return PhaseTag.isEstablish(who);
	}
	
	/**
	 * 判断是CONTACT阶段类型
	 * @param who 阶段类型编号
	 * @return 返回真或者假
	 */
	public static boolean isSwift(int who) {
		return PhaseTag.isContact(who);
	}
	
	/**
	 * 根据字符串，返回它的阶段标识号的数字格式
	 * @param input 阶段类型的字符串描述，忽略大小写。
	 * @return 返回对应的阶段类型编号。不匹配返回-1。
	 */
	public static int translate(String input) {
		// CONDUCT命令
		if (input.matches("^\\s*(?i)(CONDUCT.INIT)\\s*$")) {
			return PhaseTag.INIT;
		} else if (input.matches("^\\s*(?i)(CONDUCT.FROM)\\s*$")) {
			return PhaseTag.FROM;
		} else if (input.matches("^\\s*(?i)(CONDUCT.TO)\\s*$")) {
			return PhaseTag.TO;
		} else if (input.matches("^\\s*(?i)(CONDUCT.BALANCE)\\s*$")) {
			return PhaseTag.BALANCE;
		} else if (input.matches("^\\s*(?i)(CONDUCT.PUT)\\s*$")) {
			return PhaseTag.PUT;
		}
		// ESTABLISH命令
		else if (input.matches("^\\s*(?i)(ESTABLISH.ISSUE)\\s*$")) {
			return PhaseTag.ISSUE;
		} else if (input.matches("^\\s*(?i)(ESTABLISH.ASSIGN)\\s*$")) {
			return PhaseTag.ASSIGN;
		} else if (input.matches("^\\s*(?i)(ESTABLISH.SCAN)\\s*$")) {
			return PhaseTag.SCAN;
		} else if (input.matches("^\\s*(?i)(ESTABLISH.SIFT)\\s*$")) {
			return PhaseTag.SIFT;
		} else if (input.matches("^\\s*(?i)(ESTABLISH.RISE)\\s*$")) {
			return PhaseTag.RISE;
		} else if (input.matches("^\\s*(?i)(ESTABLISH.END)\\s*$")) {
			return PhaseTag.END;
		}
		// CONTACT
		else if (input.matches("^\\s*(?i)(CONTACT.DISTANT)\\s*$")) {
			return PhaseTag.DISTANT;
		}else if (input.matches("^\\s*(?i)(CONTACT.NEAR)\\s*$")) {
			return PhaseTag.NEAR;
		} else if (input.matches("^\\s*(?i)(CONTACT.FORK)\\s*$")) {
			return PhaseTag.FORK;
		}else if (input.matches("^\\s*(?i)(CONTACT.MERGE)\\s*$")) {
			return PhaseTag.MERGE;
		}
		return -1;
	}

	/**
	 * 根据标识号，返回它的字符串描述
	 * @param who 阶段类型编号
	 * @return 返回字符串描述
	 */
	public static String translate(int who) {
		switch (who) {
		// 下述是CONDUCT命令 
		case PhaseTag.INIT:
			return "CONDUCT.INIT";
		case PhaseTag.FROM:
			return "CONDUCT.FROM";
		case PhaseTag.TO:
			return "CONDUCT.TO";
		case PhaseTag.BALANCE:
			return "CONDUCT.BALANCE";
		case PhaseTag.PUT:
			return "CONDUCT.PUT";
		// 下述是ESTABLISH命令 
		case PhaseTag.ISSUE:
			return "ESTABLISH.ISSUE";
		case PhaseTag.SCAN:
			return "ESTABLISH.SCAN";
		case PhaseTag.SIFT:
			return "ESTABLISH.SIFT";
		case PhaseTag.RISE:
			return "ESTABLISH.RISE";
		case PhaseTag.ASSIGN:
			return "ESTABLISH.ASSIGN";
		case PhaseTag.END:
			return "ESTABLISH.END";
		// 以下是CONTACT命令
		case PhaseTag.DISTANT:
			return "CONTACT.DISTANT";
		case PhaseTag.NEAR:
			return "CONTACT.NEAR";
		case PhaseTag.FORK:
			return "CONTACT.FORK";
		case PhaseTag.MERGE:
			return "CONTACT.MERGE";
		}
		return "INVALID";
	}

}