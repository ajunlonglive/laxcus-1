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
 * 嵌套检索中使用的参数，命名字符串在tasks.xml中定义。<br>
 * 
 * @author scott.liang
 * @version 1.2 8/27/2014
 * @since laxcus 1.0
 */
public class SubSelectTaskKit extends SQLTaskKit {
	
	
	
	
	public final static String PRESELECT = "PRE_SELECT";
	public final static String SELECT = "SELECT";
	

	/** 嵌套检索的自定义参数 **/
	public final static String NEXT_SELECT_OBJECT = "NEXT_SELECT";	// 在complete方法中使用的SELECT

	public final static String DISPATCH_DOCK = "SUBSELECT_DOCK";
	
	/** 嵌套检索根命名(对应tasks.xml中的定义) **/
	private final static String SUBSELECT = "SubSelect";

	/** BALANCE阶段命名 **/
	public final static Phase BALANCE = new Phase(PhaseTag.BALANCE, Sock.doSystemSock(SUBSELECT));

	/** 嵌套SELECT检索子命名(对应tasks.xml中的定义) **/
//	public final static String SUBSELECT_IN = "IN";
	public final static String SUBSELECT_IN = "EvaluateIn";
	public final static String SubSelect_EvaluateNotIn = "EvaluateNotIn";
	
	/** TO计算阶段，收集数据 **/
	public final static String SUBSELECT_EVALUATE_COLLECT = "COLLECT";
	
//	public final static String SUBSELECT_NOTEQUALS = "NOT_EQUALS";
	
	public final static String SUBSELECT_AGGREGATE_GREATER_ALL = "AGGREGATE_GREATER_ALL";
//	public final static String SUBSELECT_GERERATE_GREATER_ALL = "GERERATE_GREATER_ALL";
	
	public final static String SUBSELECT_EVALUATE_GREATER_ALL = "EVALUATE_GREATER_ALL";
	public final static String SUBSELECT_LESS_ALL = "LESS_ALL";

	/** 嵌套SELECT检索FROM阶段命名 **/
	public final static Phase SUBSELECT_FROM_IN = new Phase(PhaseTag.FROM, Sock.doSystemSock(SUBSELECT), SUBSELECT_IN);
	
//	public final static Phase SUBSELECT_FROM_NOTIN = new Phase(PhaseTag.FROM, Sock.doSystemSock(SUBSELECT), SUBSELECT_EvaluateNotIn);
//	public final static Phase SUBSELECT_FROM_NOTEQUALS = new Phase(Phase.FROM, SUBSELECT, SUBSELECT_NOTEQUALS);
//	public final static Phase SUBSELECT_FROM_GREATER = new Phase(Phase.FROM, SUBSELECT, SUBSELECT_GREATER);
//	public final static Phase SUBSELECT_FROM_LESS = new Phase(Phase.FROM, SUBSELECT, SUBSELECT_LESS);

	/** 嵌套SELECT检索TO阶段命名 **/
	public final static Phase TO_IN = new Phase(PhaseTag.TO, Sock.doSystemSock(SUBSELECT), SUBSELECT_IN);
	public final static Phase TO_NOTIN = new Phase(PhaseTag.TO, Sock.doSystemSock(SUBSELECT), SubSelect_EvaluateNotIn);
//	public final static Phase SUBSELECT_TO_NOTEQUALS = new Phase(Phase.TO, SUBSELECT, SUBSELECT_NOTEQUALS);
	
	// 大于等于计算时，对上次的结果进行聚合，取出最大值
	public final static Phase TO_ARRGERATE_GREATERALL = new Phase(PhaseTag.TO, Sock.doSystemSock(SUBSELECT), SUBSELECT_AGGREGATE_GREATER_ALL);
	
//	// 大于等于比较的产生数据阶段
//	public final static Phase TO_GERERATE_GREATERALL = new Phase(PhaseTag.TO, Sock.doSystemSock(SUBSELECT), SUBSELECT_GERERATE_GREATER_ALL);
	
	// 大于等于比较的计算数据阶段
	public final static Phase TO_EVALUATE_GREATERALL = new Phase(PhaseTag.TO, Sock.doSystemSock(SUBSELECT), SUBSELECT_EVALUATE_GREATER_ALL);
	
	
	public final static Phase TO_LESSALL = new Phase(PhaseTag.TO, Sock.doSystemSock(SUBSELECT), SUBSELECT_LESS_ALL);

//	public final static Phase TO_GENERATE_NOTIN = new Phase(PhaseTag.TO, Sock.doSystemSock(SUBSELECT), SUBSELECT_EvaluateNotIn);
	
	
	/** TO计算阶段，收集数据和返回 **/
	public final static Phase TO_EVALUATE_COLLECT = new Phase(PhaseTag.TO, Sock.doSystemSock(SUBSELECT), SUBSELECT_EVALUATE_COLLECT);
	
//	/**
//	 * 
//	 */
//	public SubSelectTaskKit() {
//		super();
//	}

}