/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.naming;

/**
 * 任务阶段命名的执行阶段定义。用于CONDUCT和ESTABLISH两组命令中。<br><br>
 * 
 * CONDUCT是大规模的分布式数据计算。
 * ESTABLISH是大规模的分布式数据构建，目的是辅助CONDUCT，实现CONDUCT更多更快的数据处理。
 * 它们的工作目标和处理内容不同。<br><br>
 * 
 * CONDUCT命令说明：<br>
 * <1> CONDUCT是DIFFUSE/CONVERGE分布算法的语句化实现，为分布网络环境下的大规模数据计算设计，数据计算包括依赖于存储数据（DATA站点的数据）和不依赖存储数据（自己产生数据）的两种。<br>
 * <2> CONDUCT任务有五个阶段，见下表，阶段之间存在顺序和迭代两种现象。<br>
 * <3> 工作位置：INIT、BALANCE在CALL站点，FROM在DATA站点，TO(SUBTO)在WORK站点，PUT在FRONT站点。<br>
 * <4> 工作性质：INIT、BALANCE、PUT是单线程。FROM、TO(SUBTO)是同时多线程处理。 <br>
 * 
 * <br><br>
 * 
 * ESTABLISH命令说明：<br>
 * <1> ESTABLISH是为分布网络环境下的大批量数据构建设计的，数据构建包括数据优化和数据重组两种处理。
 * “数据优化”操作的实质是将DATA站点的regulate操作移到BUILD站点进行，它遵守原来表的数据结构，清除过期数据，目的是让数据更紧凑，执行更有效。
 * “数据重组”也可以称为“洗牌reshuffle”操作，是将原来数据的基础上，按照自种自定义规则，生成新的数据，原来的数据不改变。<br>
 * <2> ESTABLISH任务有六个阶段，见下表，阶段之间整个流程是顺序进行的，依次是：ISSUE、SCAN、SIFT（SUBSIFT）、RISE、ASSIGN、END。<br>
 * <3> 工作位置：ISSUE、ASSIGN在CALLL站点、END在FRONT站点，SCAN、RISE在DATA站点，SIFT（SUBSIFT）在BUILD站点。<br>
 * <4> 工作性质：ISSUE、ASSIGN、END是单线程的，即顺序串行的处理。SCAN、SIFT（SUBSIFT）、RISE是并行的，即同时有多个SCAN/RISE线程作用到DATA主站点，多个SIFT线程作用到BUILD站点上。<br>
 * 
 * <br><br>
 * 
 * 要点说明：<br>
 * <1> 两种操作都能够使用自定义参数。自定义参数的格式由系统规定，参数的内容信心含义和计算规则由用户去自己解释和执行，LAXCUS负责保存和传递。<br>
 * <2> ESTABLISH的阶段处理过程是完全顺序的，即上一阶段完成，处理数据交给下一阶段处理，直到最后一步。<br>
 * <3> CONDUCT的阶段处理在INIT/FROM/BALANCE是顺序的，BALANCE/TO之间是迭代重复的。原因是TO根阶段有SUBTO子阶段，SUBTO的结果要返回给BALANCE处理，BALANCE又要提交给下一个SUBTO处理，TO/PUT是顺序的。<br>
 * <4> 两组任务都有“会话”阶段。所谓“会话”是指CALL站点与其它站点，或者其它站点之间的数据交互过程。ESTABLISH的会话阶段是SCAN、SIFT，CONDUCT的会话阶段是FROM、TO。<br>
 * <5> CONDUCT和ESTABLISH的阶段命名没有关联关系。<br>
 * 
 * @author scott.liang
 * @version 1.2 01/23/2015
 * @since laxcus 1.0
 */
public final class PhaseTag {

	/** ESTABLISH命令的阶段命名（对应SCAN/SIFT算法） **/
	public final static int ISSUE = 0x1;
	public final static int SCAN = 0x2;
	public final static int SIFT = 0x3;
	public final static int RISE = 0x4;
	public final static int ASSIGN = 0x5;
	public final static int END = 0x6;

	/** CONDUCT命令的阶段命名（对应DIFFUSE/CONVERGE分布算法）**/
	public final static int INIT = 0x11;
	public final static int FROM = 0x12;
	public final static int TO = 0x13;
	public final static int BALANCE = 0x14;
	public final static int PUT = 0x15;
	
	/** CONTACT命令的阶段命名（对应CONTACT分布架构，远端位于WORK节点,近地位于FRONT节点, FORK/MERGE位于CALL节点进行分布调度） **/
	public final static int FORK = 0x21;
	public final static int MERGE = 0x22;
	public final static int DISTANT = 0x23;
	public final static int NEAR = 0x24;

	/**
	 * 枚举值
	 * @return 阶段命名类型数组
	 */
	public static int[] enumlate() {
		return new int[] { PhaseTag.ISSUE, PhaseTag.SCAN, PhaseTag.SIFT,
				PhaseTag.RISE, PhaseTag.ASSIGN, PhaseTag.END,
				PhaseTag.INIT, PhaseTag.FROM, PhaseTag.TO, PhaseTag.BALANCE,
				PhaseTag.PUT, 
				PhaseTag.FORK, PhaseTag.MERGE, PhaseTag.DISTANT, PhaseTag.NEAR };
	}
	
	/**
	 * 输出CONDUCT阶段类型
	 * @return 整数数组
	 */
	public static int[] conduct() {
		return new int[] { PhaseTag.INIT, PhaseTag.FROM, PhaseTag.TO,
				PhaseTag.BALANCE, PhaseTag.PUT };
	}

	/**
	 * 输出ESTABLISH阶段类型
	 * @return 整数数组
	 */
	public static int[] establish() {
		return new int[] { PhaseTag.ISSUE, PhaseTag.SCAN, PhaseTag.SIFT,
				PhaseTag.RISE, PhaseTag.ASSIGN, PhaseTag.END, };
	}

	/**
	 * 输出CONTACT阶段类型
	 * @return 整数数组
	 */
	public static int[] contact() {
		return new int[] { PhaseTag.FORK, PhaseTag.MERGE, PhaseTag.DISTANT,
				PhaseTag.NEAR };
	}

	/**
	 * 判断阶段执行阶段有效，允许是CONDUCT/ESTABLISH/CONTACT中的任意一个
	 * @param who 阶段类型
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isPhase(int who) {
		return PhaseTag.isConduct(who) || PhaseTag.isEstablish(who) || PhaseTag.isContact(who);
	}

	/**
	 * 判断阶段执行阶段有效，允许是CONDUCT/ESTABLISH中的任意一个
	 * @param input 阶段类型的字符串描述
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isPhase(String input) {
		if (input == null) {
			return false;
		}
		int who = PhaseTag.translate(input);
		return PhaseTag.isPhase(who);
	}

	/**
	 * 判断是否CONDUCT阶段类型
	 * @param who 阶段类型编号
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public static boolean isConduct(int who) {
		switch (who) {
		case PhaseTag.INIT:
		case PhaseTag.FROM:
		case PhaseTag.TO:
		case PhaseTag.BALANCE:
		case PhaseTag.PUT:
			return true;
		}
		return false;
	}

	/**
	 * 判断是ESTABLISH阶段类型
	 * @param who 阶段类型编号
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public static boolean isEstablish(int who) {
		switch (who) {
		case PhaseTag.ISSUE:
		case PhaseTag.SCAN:
		case PhaseTag.SIFT:
		case PhaseTag.RISE:
		case PhaseTag.ASSIGN:
		case PhaseTag.END:
			return true;
		}
		return false;
	}

	/**
	 * 判断是否CONTACT阶段类型
	 * @param who 阶段类型编号
	 * @return 返回真或者假
	 */
	public static boolean isContact(int who) {
		switch (who) {
		case PhaseTag.FORK:
		case PhaseTag.MERGE:
		case PhaseTag.DISTANT:
		case PhaseTag.NEAR:
			return true;
		}
		return false;
	}
	
	/**
	 * 根据字符串，返回它的阶段标识号的数字格式
	 * @param input 阶段类型的字符串描述，忽略大小写。
	 * @return 返回对应的阶段类型编号。不匹配返回-1。
	 */
	public static int translate(String input) {
		// CONDUCT分布计算
		if (input.matches("^\\s*(?i)(INIT)\\s*$")) {
			return PhaseTag.INIT;
		} else if (input.matches("^\\s*(?i)(FROM|DIFFUSE)\\s*$")) {
			return PhaseTag.FROM;
		} else if (input.matches("^\\s*(?i)(TO|CONVERGE|NEXTO|SUBTO)\\s*$")) {
			return PhaseTag.TO;
		} else if (input.matches("^\\s*(?i)(BALANCE)\\s*$")) {
			return PhaseTag.BALANCE;
		} else if (input.matches("^\\s*(?i)(PUT)\\s*$")) {
			return PhaseTag.PUT;
		}
		// ESTABLISH分布数据构建
		else if (input.matches("^\\s*(?i)(ISSUE)\\s*$")) {
			return PhaseTag.ISSUE;
		} else if (input.matches("^\\s*(?i)(ASSIGN)\\s*$")) {
			return PhaseTag.ASSIGN;
		} else if (input.matches("^\\s*(?i)(SCAN)\\s*$")) {
			return PhaseTag.SCAN;
		} else if (input.matches("^\\s*(?i)(SIFT)\\s*$")) {
			return PhaseTag.SIFT;
		} else if (input.matches("^\\s*(?i)(RISE)\\s*$")) {
			return PhaseTag.RISE;
		} else if (input.matches("^\\s*(?i)(END)\\s*$")) {
			return PhaseTag.END;
		}
		// CONTACT的CS计算
		else if (input.matches("^\\s*(?i)(DISTANT)\\s*$")) {
			return PhaseTag.DISTANT;
		} else if (input.matches("^\\s*(?i)(NEAR)\\s*$")) {
			return PhaseTag.NEAR;
		} else if (input.matches("^\\s*(?i)(FORK)\\s*$")) {
			return PhaseTag.FORK;
		} else if (input.matches("^\\s*(?i)(MERGE)\\s*$")) {
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
			return "INIT";
		case PhaseTag.FROM:
			return "FROM";
		case PhaseTag.TO:
			return "TO";
		case PhaseTag.BALANCE:
			return "BALANCE";
		case PhaseTag.PUT:
			return "PUT";
		// 下述是ESTABLISH命令 
		case PhaseTag.ISSUE:
			return "ISSUE";
		case PhaseTag.SCAN:
			return "SCAN";
		case PhaseTag.SIFT:
			return "SIFT";
		case PhaseTag.RISE:
			return "RISE";
		case PhaseTag.ASSIGN:
			return "ASSIGN";
		case PhaseTag.END:
			return "END";
		// 以下是CONTACT计算
		case PhaseTag.DISTANT:
			return "DISTANT";
		case PhaseTag.NEAR:
			return "NEAR";
		case PhaseTag.FORK:
			return "FORK";
		case PhaseTag.MERGE:
			return "MERGE";
		}
		return "NONE";
	}

	/**
	 * 判断一个阶段标识是否属于“会话”阶段，包括CONDUCT/ESTABLISH/CONTACT的会话。
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isSession(int who) {
		return PhaseTag.isConductSession(who)
				|| PhaseTag.isEstablishSession(who)
				|| PhaseTag.isContactSession(who);
	}

	/**
	 * 检查一个阶段标识是否属于CONDUCT任务的“会话”阶段
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isConductSession(int who) {
		switch (who) {
		case PhaseTag.FROM:
		case PhaseTag.TO:
			return true;
		}
		return false;
	}

	/**
	 * 检查一个阶段标识是否属于ESTABLISH任务的“会话”阶段
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isEstablishSession(int who) {
		switch (who) {
		case PhaseTag.SCAN:
		case PhaseTag.SIFT:
		case PhaseTag.RISE:
			return true;
		}
		return false;
	}

	/**
	 * 检查一个阶段标识是否属于CONTACT任务的“会话”阶段
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isContactSession(int who) {
		switch (who) {
		case PhaseTag.DISTANT:
			return true;
		}
		return false;
	}
	
	/**
	 * 判断是CONDUCT.INIT阶段类型
	 * @param who 阶段类型编号 
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isInit(int who) {
		return who == PhaseTag.INIT;
	}

	/**
	 * 判断是CONDUCT.INIT阶段命名
	 * @param phase 阶段命名 
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isInit(Phase phase) {
		return phase != null && isInit(phase.getFamily());
	}

	/**
	 * 判断是CONDUCT.FROM阶段类型
	 * @param who 阶段类型编号 
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isFrom(int who) {
		return who == PhaseTag.FROM;
	}

	/**
	 * 判断是CONDUCT.FROM阶段命名
	 * @param phase 阶段命名 
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isFrom(Phase phase) {
		return phase != null && isFrom(phase.getFamily());
	}

	/**
	 * 判断是CONDUCT.TO阶段类型
	 * @param who 阶段类型编号
	 * @return 判断条件成立返回“真”，否则“假”
	 */
	public static boolean isTo(int who) {
		return who == PhaseTag.TO;
	}

	/**
	 * 判断是CONDUCT.TO阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isTo(Phase phase) {
		return phase != null && isTo(phase.getFamily());
	}

	/**
	 * 判断是CONDUCT.BALANCE阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isBalance(int who) {
		return who == PhaseTag.BALANCE;
	}

	/**
	 * 判断是CONDUCT.BALANCE阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isBalance(Phase phase) {
		return phase != null && isBalance(phase.getFamily());
	}

	/**
	 * 判断是CONDUCT.PUT阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isPut(int who) {
		return who == PhaseTag.PUT;
	}

	/**
	 * 判断是CONDUCT.PUT阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isPut(Phase phase) {
		return phase != null && isPut(phase.getFamily());
	}

	/**
	 * 判断是ESTABLISH.ISSUE阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isIssue(int who) {
		return who == PhaseTag.ISSUE;
	}

	/**
	 * 判断是ESTABLISH.ISSUE阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isIssue(Phase phase) {
		return phase != null && isIssue(phase.getFamily());
	}

	/**
	 * 判断是ESTABLISH.SCAN阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isScan(int who) {
		return who == PhaseTag.SCAN;
	}

	/**
	 * 判断是ESTABLISH.SCAN阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isScan(Phase phase) {
		return phase != null && isScan(phase.getFamily());
	}

	/**
	 * 判断是ESTABLISH.SIFT阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isSift(int who) {
		return who == PhaseTag.SIFT;
	}

	/**
	 * 判断是ESTABLISH.SIFT阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isSift(Phase phase) {
		return phase != null && isSift(phase.getFamily());
	}

	/**
	 * 判断是ESTABLISH.RISE阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isRise(int who) {
		return who == PhaseTag.RISE;
	}

	/**
	 * 判断是ESTABLISH.RISE阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isRise(Phase phase) {
		return phase != null && isRise(phase.getFamily());
	}

	/**
	 * 判断是ESTABLISH.ASSIGN阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isAssign(int who) {
		return who == PhaseTag.ASSIGN;
	}

	/**
	 * 判断是ESTABLISH.ASSIGN阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isAssign(Phase phase) {
		return phase != null && isAssign(phase.getFamily());
	}

	/**
	 * 判断是ESTABLISH.END阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isEnd(int who) {
		return who == PhaseTag.END;
	}

	/**
	 * 判断是ESTABLISH.END阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isEnd(Phase phase) {
		return phase != null && isEnd(phase.getFamily());
	}
	
	/**
	 * 判断是CONTACT.DISTANT阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isDistant(int who) {
		return who == PhaseTag.DISTANT;
	}

	/**
	 * 判断是CONTACT.DISTANT阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isDistant(Phase phase) {
		return phase != null && isDistant(phase.getFamily());
	}
	
	/**
	 * 判断是CONTACT.NEAR阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isNear(int who) {
		return who == PhaseTag.NEAR;
	}

	/**
	 * 判断是CONTACT.NEAR阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isNear(Phase phase) {
		return phase != null && isNear(phase.getFamily());
	}
	
	/**
	 * 判断是CONTACT.MERGE阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isMerge(int who) {
		return who == PhaseTag.MERGE;
	}

	/**
	 * 判断是CONTACT.MERGE阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isMerge(Phase phase) {
		return phase != null && isMerge(phase.getFamily());
	}

	/**
	 * 判断是CONTACT.FORK阶段类型
	 * @param who 阶段类型编号
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isFork(int who) {
		return who == PhaseTag.FORK;
	}

	/**
	 * 判断是CONTACT.FORK阶段命名
	 * @param phase 阶段命名
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isFork(Phase phase) {
		return phase != null && isFork(phase.getFamily());
	}
	
	/**
	 * 判断阶段类型位于FRONT站点
	 * @param who 阶段类型
	 * @return 返回真或者假
	 */
	public static boolean onFrontSite(int who) {
		return PhaseTag.isEnd(who) || PhaseTag.isPut(who) || PhaseTag.isNear(who);
	}
	
	/**
	 * 判断阶段类型位于CALL站点
	 * @param who 阶段类型
	 * @return 返回真或者假
	 */
	public static boolean onCallSite(int who) {
		return PhaseTag.isInit(who) || PhaseTag.isBalance(who)
				|| PhaseTag.isIssue(who) || PhaseTag.isAssign(who) 
				|| PhaseTag.isFork(who) || PhaseTag.isMerge(who);
	}

	/**
	 * 判断阶段类型位于DATA站点
	 * @param who 阶段类型
	 * @return 返回真或者假
	 */
	public static boolean onDataSite(int who) {
		return PhaseTag.isFrom(who) || PhaseTag.isScan(who)
				|| PhaseTag.isRise(who);
	}
	
	/**
	 * 判断阶段位于DATA主站点
	 * @param who 阶段类型
	 * @return 返回真或者假
	 */
	public static boolean onDataMasterSite(int who) {
		return PhaseTag.isFrom(who) || PhaseTag.isScan(who)
				|| PhaseTag.isRise(who);
	}

	/**
	 * 判断阶段位于DATA从站点，忽略SCAN类型
	 * @param who 阶段类型
	 * @return 返回真或者假
	 */
	public static boolean onDataSlaveSite(int who) {
		return PhaseTag.isFrom(who) || PhaseTag.isRise(who);
	}
	
	/**
	 * 判断阶段类型位于WORK站点
	 * @param who 阶段类型
	 * @return 返回真或者假
	 */
	public static boolean onWorkSite(int who) {
		return PhaseTag.isTo(who) || PhaseTag.isDistant(who);
	}

	/**
	 * 判断阶段类型位于BUILD站点
	 * @param who 阶段类型
	 * @return 返回真或者假
	 */
	public static boolean onBuildSite(int who) {
		return PhaseTag.isSift(who);
	}

	//	public static void main(String[] args) {
	//		Phase phase = new Phase(PhaseTag.FROM, "helo");
	//		System.out.printf("match is %s\n", PhaseTag.isFrom(phase));
	//	}
}