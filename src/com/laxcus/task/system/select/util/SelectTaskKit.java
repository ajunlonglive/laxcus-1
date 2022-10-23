/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.util;

import com.laxcus.util.naming.*;

/**
 * 定义SELECT检索的"SELECT FROM WHERE GROUP BY|ORDER BY"中的参数，命名字符串在tasks.xml中定义。<br>
 * 
 * @author scott.liang
 * @version 1.0 9/23/2011
 * @since laxcus 1.0
 */
public class SelectTaskKit extends SQLTaskKit {

	/** 检索根命名 **/
	public final static String SELECT = "SELECT";

	/** 检索子命名 **/
	public final static String GROUPBY = "GROUPBY";
	public final static String ORDERBY = "ORDERBY";
	public final static String DISTINCT = "DISTINCT";

	public final static String STANDARD = "STANDARD";

	/** INIT阶段命名 **/
	public final static Phase INIT = new Phase(PhaseTag.INIT,
			Sock.doSystemSock(SelectTaskKit.SELECT));

	/** BALANCE阶段命名 **/
	public final static Phase BALANCE = new Phase(PhaseTag.BALANCE,
			Sock.doSystemSock(SelectTaskKit.SELECT));

	/** FROM阶段命名 **/
	public final static Phase FROM_GROUPBY = new Phase(PhaseTag.FROM,
			Sock.doSystemSock(SelectTaskKit.SELECT), SelectTaskKit.GROUPBY);
	public final static Phase FROM_ORDERBY = new Phase(PhaseTag.FROM,
			Sock.doSystemSock(SelectTaskKit.SELECT), SelectTaskKit.ORDERBY);
	public final static Phase FROM_DISTINCT = new Phase(PhaseTag.FROM,
			Sock.doSystemSock(SelectTaskKit.SELECT), SelectTaskKit.DISTINCT);

	public final static Phase FROM_STANDARD = new Phase(PhaseTag.FROM,
			Sock.doSystemSock(SelectTaskKit.SELECT), SelectTaskKit.STANDARD);

	/** TO阶段命名 **/
	public final static Phase TO_GROUPBY = new Phase(PhaseTag.TO,
			Sock.doSystemSock(SelectTaskKit.SELECT), SelectTaskKit.GROUPBY);
	public final static Phase TO_ORDERBY = new Phase(PhaseTag.TO,
			Sock.doSystemSock(SelectTaskKit.SELECT), SelectTaskKit.ORDERBY);
	public final static Phase TO_DISTINCT = new Phase(PhaseTag.TO,
			Sock.doSystemSock(SelectTaskKit.SELECT), SelectTaskKit.DISTINCT);

	//	/** 执行分割的列标识号 **/
	//	public final static String SPLIT_COLUMNID = "SPLIT_COLUMNID";

	//	/** 标准检索的自定义参数标题 **/	
	//	/** 检索的自定义参数，用于标准检索(只用GROUP BY和ORDER BY)，和嵌套检索中 **/
	//	public final static String SELECT_OBJECT = "SELECT_OBJECT";
	//	public final static String DOCK_OBJECT = "SELECT_DOCK";

	//	/** 嵌套SELECT检索，以下任务命名在tasks.xml中定义 **/
	//
	//	/** 嵌套SELECT检索根命名 **/
	//	public final static String SUBSELECT = "SUBSELECT";
	//
	//	/** 嵌套SELECT检索子命名 **/
	//	public final static String SUBSELECT_IN = "IN";
	//	public final static String SUBSELECT_NOTEQUALS = "NOT_EQUALS";
	//	public final static String SUBSELECT_GREATER = "GREATER";
	//	public final static String SUBSELECT_LESS = "LESS";
	//
	//	/** 嵌套SELECT检索FROM阶段命名 **/
	//	public final static Phase SUBSELECT_FROM_IN = new Phase(Phase.FROM, SUBSELECT, SUBSELECT_IN);
	//	public final static Phase SUBSELECT_FROM_NOTEQUALS = new Phase(Phase.FROM, SUBSELECT, SUBSELECT_IN);
	//	public final static Phase SUBSELECT_FROM_GREATER = new Phase(Phase.FROM, SUBSELECT, SUBSELECT_GREATER);
	//	public final static Phase SUBSELECT_FROM_LESS = new Phase(Phase.FROM, SUBSELECT, SUBSELECT_LESS);
	//
	//	/** 嵌套SELECT检索TO阶段命名 **/
	//	public final static Phase SUBSELECT_TO_IN = new Phase(Phase.TO, SUBSELECT, SUBSELECT_IN);
	//	public final static Phase SUBSELECT_TO_NOTEQUALS = new Phase(Phase.TO, SUBSELECT, SUBSELECT_IN);
	//	public final static Phase SUBSELECT_TO_GREATER = new Phase(Phase.TO, SUBSELECT, SUBSELECT_GREATER);
	//	public final static Phase SUBSELECT_TO_LESS = new Phase(Phase.TO, SUBSELECT, SUBSELECT_LESS);
	//
	//	/** 嵌套SELECT的BALANCE阶段命名 **/
	//	public final static	Phase SUBSELECT_BALANCE = new Phase(Phase.BALANCE, SUBSELECT, null);



	//	/** 嵌套检索的自定义参数 **/
	//	public final static String DISPATCH_SELECT = "DISPATCH_SELECT";	// 在complete方法中使用的SELECT

	//	//// JOIN 分布计算参数  ////
	//	
	//	public final static String JOINSELECT = "JOINSELECT"; // JOIN基础命名
	//	public final static String INNERJOIN = "INNERJOIN"; // 子命名，内联卡笛尔
	//
	//	// JOIN阶段命名
	//	public final static Phase SELECT_FROM_INNERJOIN = new Phase(Phase.FROM, JOINSELECT, INNERJOIN);
	//	public final static Phase SELECT_TO_INNERJOING = new Phase(Phase.TO, JOINSELECT, INNERJOIN);
	//	public final static Phase JOIN_BALANCE = new Phase(Phase.BALANCE, JOINSELECT, null);
	//
	//	// 变量
	//	public final static String JOIN_OBJECT = "JOIN_OBJECT";

	/**
	 * 
	 */
	public SelectTaskKit() {
		super();
	}

}