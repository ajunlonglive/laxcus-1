/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

/**
 * LAXCUS集群和站点设计定义。<br><br>
 * 
 * LAXCUS由多个子域集群组成，分为BANK集群和HOME集群，BANK和HOME组成LAXCUS TOP集群。其中BANK集群负责账号和业务管理工作，HOME集群负责数据管理和计算工作。BANK集群在系统中允许有一个，HOME集群可以是任意多个。它们共同接受TOP站点管理调度。<br><br>
 * 
 * BANK站点是BANK集群的管理站点，它下面子站点包括：<br>
 * 1. ACCOUNT站点，负责账号管理工作。<br>
 * 2. HASH站点，账号用户签名的散列和快速定位。<br>
 * 3. GATE站点，网关节点，管理登录的FRONT站点和分配账号工作。<br>
 * 4. ENTRANCE站点，网关节点，FRONT站点首先登录站点，根据用户签名，重定向到匹配的GATE站点。<br><br>
 * 
 * HOME站点是HOME集群的管理站点，它下属子站点包括：<br>
 * 1. DATA站点，负责数据存储和初始计算工作。<br>
 * 2. WORK站点，负责数据计算工作。<br>
 * 3. BUILD站点，负责数据优化工作。<br>
 * 4. CALL站点，网关节点，接受FRONT登录和分配数据计算/优化。CALL站点可以注册到多个HOME站点并发送命令。<br><br>
 * 
 * 跨子域集群节点：<br>
 * 1. LOG站点，保存所属子域集群任意多个站点的日志。<br>
 * 2. WATCH站点，登录到管理站点，监视所属集群的运行状态，这些站点包括：TOP/BANK/HOME。<br><br>
 * 
 * 集群外节点：<br>
 * FRONT节点，由注册用户使用和管理，登录到ENTRANCE/GATE/CALL节点，发送数据计算命令。<br><br>
 * 
 * 节点部署位置：<br>
 * 
 * 
 * 除FRONT站点由用户自由部署外，其他站点的部署由集群设施建设者统一规划。<br>
 * 
 * 
 * 站点类型包括：TOP、ARCHIVE、AID、HOME、WATCH、LOG、CALL、DATA、WORK、BUILD、FRONT。<br><br>
 * 
 * 站点服务属性：<br>
 * 1. 管理站点：TOP、HOME<br>
 * 2. 网关站点：GATE、AID、CALL<br>
 * 3. 工作站点：LOG、DATA、WORK、BUILD<br>
 * 4. 账号和账号资源（分布任务组件、码位计算器、快捷组件）节点：ACCOUNT
 * 5. 账号定位节点：HASH
 * 6. 网关重定向节点：ENTRANCE
 * 7. 账号及相关管理节点：BANK
 * 
 * 4. 分布任务组件保存/管理站点：ARCHIVE<br>
 * 5. 前端站点：FRONT<br>
 * 6. 集群监视站点：WATCH <br><br> 
 * 
 * 前端站点（FRONT SITE）的部署：<br>
 * 1. 做为管理员时，它的运行位置在集群内，直接接入TOP站点。<br>
 * 2. 做为普通注册用户，它的部署地点是INTERNET/INTRANET的任何位置，只要能够接入LAXCUS集群。<br>
 * 
 * 其他站点的部署：<br>
 * 1. TOP/HOME/WATCH/LOG/DATA/WORK/BUILD部署在内网中。<br>
 * 2. AID/CALL是网关站点。网关站点绑定通配符地址，分别是对内和对外。<br>
 * 3. FRONT是外部站点，由用户部署和使用，可以连接AID/CALL站点，其它均不可见并且不可连接。<br><br>
 * 
 * 除FRONT站点由用户自由部署外，其他站点的部署由集群设施建设者统一规划。<br><br>
 * 
 * 网关站点:<br>
 * 1. AID注册到TOP站点，为FRONT提供账号管理服务。建立这个站点的目的是出入安全考虑，不允许FRONT直接接入TOP，需要中转。<br>
 * 2. CALL注册到多个HOME站点，实现多域并行操作。其中一个是它的主站点，其他是从站。CALL为FRONT提供数据处理业务，这些业务包括SQL/CONDUCT/ESTABLISH/OTHER。<br>
 * 3. 网关站点用通配符地址监听网络数据，有两个注册地址：内网/公网地址。内网地址与集群内的站点通信，公网地址为FRONT接入时使用。
 * 如果需要接受来自INTERNET的数据请求，公网地址是必须的。如果只限INTRANET中使用，公网/内网地址一致。<br>
 * 
 * @author scott.liang
 * @version 1.1 12/11/2011
 * @since laxcus 1.0
 */
public final class SiteTag {

	/** 无定义 **/
	public final static byte NONE = 0;

	/** LAXCUS集群站点类型 **/
	
	/** TOP主域集群 **/
	public final static byte TOP_SITE = 1;
	
	/** BANK子域集群 **/
	public final static byte BANK_SITE = 2;
	public final static byte ACCOUNT_SITE = 3;
	public final static byte HASH_SITE = 4;
	public final static byte GATE_SITE = 5;
	public final static byte ENTRANCE_SITE = 6;

	/** HOME子域集群 **/
	public final static byte HOME_SITE = 7;
	public final static byte DATA_SITE = 8;
	public final static byte WORK_SITE = 9;
	public final static byte BUILD_SITE = 10;
	public final static byte CALL_SITE = 11;

	/** 管理站点，跨集群存在，监视集群全部站点的运行，包括节点运行状态、报警、一般性提示、远程操控管理。**/
	public final static byte WATCH_SITE = 12;

	/** 日志站点，跨集群存在，记录所属集群子站点日志 **/
	public final static byte LOG_SITE = 13;

	/** 用户登录站点 **/
	public final static byte FRONT_SITE = 15;

	/**
	 * 将站点类型数字描述翻译为字符串描述 
	 * @param who 站点类型
	 * @return 站点字符串描述
	 */
	public static String translate(byte who) {
		switch (who) {
		case SiteTag.TOP_SITE:
			return "top";
		case SiteTag.LOG_SITE:
			return "log";
		// bank cluster
		case SiteTag.BANK_SITE:
			return "bank";
		case SiteTag.ACCOUNT_SITE:
			return "account";
		case SiteTag.HASH_SITE:
			return "hash";
		case SiteTag.GATE_SITE:
			return "gate";
		case SiteTag.ENTRANCE_SITE:
			return "entrance";
		// home cluster
		case SiteTag.HOME_SITE:
			return "home";
		case SiteTag.CALL_SITE:
			return "call";
		case SiteTag.DATA_SITE:
			return "data";
		case SiteTag.WORK_SITE:
			return "work";			
		case SiteTag.BUILD_SITE:
			return "build";
		// watch site
		case SiteTag.WATCH_SITE:
			return "watch";
		// front site
		case SiteTag.FRONT_SITE:
			return "front";
		}
		return "none";
	}

	/**
	 * 将站点类型的字符串描述翻译为数字描述
	 * @param who 字符串描述
	 * @return 站点类型，无效返回0。
	 */
	public static byte translate(String who) {
		// TOP集群
		if (who.matches("^\\s*(?i)(TOP)\\s*$")) {
			return SiteTag.TOP_SITE;
		} else if (who.matches("^\\s*(?i)(BANK)\\s*$")) {
			return SiteTag.BANK_SITE;
		} else if (who.matches("^\\s*(?i)(HOME)\\s*$")) {
			return SiteTag.HOME_SITE;
		}
		// BANK集群
		else if (who.matches("^\\s*(?i)(ACCOUNT)\\s*$")) {
			return SiteTag.ACCOUNT_SITE;
		} else if (who.matches("^\\s*(?i)(HASH)\\s*$")) {
			return SiteTag.HASH_SITE;
		} else if (who.matches("^\\s*(?i)(GATE)\\s*$")) {
			return SiteTag.GATE_SITE;
		} else if (who.matches("^\\s*(?i)(ENTRANCE)\\s*$")) {
			return SiteTag.ENTRANCE_SITE;
		}
		// HOME集群
		else if (who.matches("^\\s*(?i)(CALL)\\s*$")) {
			return SiteTag.CALL_SITE;
		} else if (who.matches("^\\s*(?i)(DATA)\\s*$")) {
			return SiteTag.DATA_SITE;
		} else if (who.matches("^\\s*(?i)(WORK)\\s*$")) {
			return SiteTag.WORK_SITE;
		} else if (who.matches("^\\s*(?i)(BUILD)\\s*$")) {
			return SiteTag.BUILD_SITE;
		}
		// 跨集群
		else if (who.matches("^\\s*(?i)(LOG)\\s*$")) {
			return SiteTag.LOG_SITE;
		} else if (who.matches("^\\s*(?i)(WATCH)\\s*$")) {
			return SiteTag.WATCH_SITE;
		}
		// 外网
		else if (who.matches("^\\s*(?i)(FRONT)\\s*$")) {
			return SiteTag.FRONT_SITE;
		}
		
		// 无效值
		return 0;
	}

	/**
	 * 判断是合法的站点类型
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isSite(byte who) {
		switch(who) {
		// 顶级节点
		case SiteTag.TOP_SITE:
			// BANK子集群
		case SiteTag.BANK_SITE:
		case SiteTag.ACCOUNT_SITE:
		case SiteTag.HASH_SITE:
		case SiteTag.GATE_SITE:
		case SiteTag.ENTRANCE_SITE:
			// HOME子集群
		case SiteTag.HOME_SITE:
		case SiteTag.CALL_SITE:
		case SiteTag.DATA_SITE:
		case SiteTag.WORK_SITE:
		case SiteTag.BUILD_SITE:
			// 跨集群节点
		case SiteTag.LOG_SITE:
		case SiteTag.WATCH_SITE:
		case SiteTag.FRONT_SITE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 判断是TOP站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isTop(byte who) {
		return who == SiteTag.TOP_SITE;
	}

	/**
	 * 判断是ACCOUNT站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isAccount(byte who) {
		return who == SiteTag.ACCOUNT_SITE;
	}

	/**
	 * 判断是HASH站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isHash(byte who) {
		return who == SiteTag.HASH_SITE;
	}

	/**
	 * 判断是GATE站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isGate(byte who) {
		return who == SiteTag.GATE_SITE;
	}

	/**
	 * 判断是ENTRANCE站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isEntrance(byte who) {
		return who == SiteTag.ENTRANCE_SITE;
	}

	/**
	 * 判断是BANK站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isBank(byte who) {
		return who == SiteTag.BANK_SITE;
	}

	/**
	 * 判断是HOME站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isHome(byte who) {
		return who == SiteTag.HOME_SITE;
	}

	/**
	 * 判断是WATCH站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isWatch(byte who) {
		return who == SiteTag.WATCH_SITE;
	}

	/**
	 * 判断是LOG站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isLog(byte who) {
		return who == SiteTag.LOG_SITE;
	}

	/**
	 * 判断是是FRONT站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isFront(byte who) {
		return who == SiteTag.FRONT_SITE;
	}

	/**
	 * 判断是DATA站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isData(byte who) {
		return who == SiteTag.DATA_SITE;
	}

	/**
	 * 判断是WORK站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isWork(byte who) {
		return who == SiteTag.WORK_SITE;
	}

	/**
	 * 判断是BUILD站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isBuild(byte who) {
		return who == SiteTag.BUILD_SITE;
	}

	/**
	 * 判断是CALL站点
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isCall(byte who) {
		return who == SiteTag.CALL_SITE;
	}
	
	/**
	 * 判断是网关节点。网关节点包括：GATE、ENTRANCE、CALL
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isGateway(byte who) {
		switch (who) {
		case SiteTag.GATE_SITE:
		case SiteTag.ENTRANCE_SITE:
		case SiteTag.CALL_SITE:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是枢纽(交换中心)节点，包括TOP/HOME/BANK
	 * @param who 站点类型
	 * @return 返回真或者假
	 */
	public static boolean isHub(byte who) {
		switch (who) {
		case SiteTag.TOP_SITE:
		case SiteTag.HOME_SITE:
		case SiteTag.BANK_SITE:
			return true;
		default:
			return false;
		}
	}
}