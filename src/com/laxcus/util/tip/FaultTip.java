/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.tip;

/**
 * 错误提示标签
 * 
 * @author scott.liang
 * @version 1.0 11/25/2013
 * @since laxcus 1.0
 */
public class FaultTip {

	/** 语法错误 **/
	public static final int INCORRECT_SYNTAX = 1;

	/** 无效命令 **/
	public static final int ILLEGAL_COMMAND = 2;

	/** 命令被拒绝 **/
	public static final int COMMAND_REFUSED = 3;

	/** 系统故障 **/
	public static final int SYSTEM_FAULT = 4;

	/** 参数不足，拒绝执行 **/
	public static final int PARAMETER_MISSING = 5;
	
	/** 分布计算失败 **/
	public static final int CONDUCT_FAULT = 6;

	/** 数据构建失败 **/
	public static final int ESTABLISH_FAULED = 7;
	
	/** 空记录 **/
	public static final int EMPRY_LIST = 8;
	
	/** 权限不足 **/
	public static final int PERMISSION_MISSING = 9; 
	
	/** 系统拒绝 **/
	public static final int SYSTEM_DENIED = 10; 
	
	/** 网络故障，连接故障 **/
	public static final int NETWORK_FAILURE = 11; 
	
	/** 投递失败 **/
	public static final int CANNOT_SUBMIT = 12; 
	
	/** 请求的站点不足 **/
	public static final int SITE_MISSING = 13;
	
	/** 执行失败 **/
	public static final int IMPLEMENT_FAULT = 14;
	
	/** 参数不足，拒绝执行 **/
	public static final int ILLEGAL_PARAMETER = 15;
	
	/** 业务超时 **/
	public static final int JOB_TIMEOUT = 16;

	/** 分布锁事务执行故障 **/
	public static final int RULE_ATTACH_FAULT = 17;

	public static final int RULE_ATTACH_RETURN_FAULT = 18;

	public static final int RULE_DETACH_FAULT = 19;

	public static final int RULE_DETACH_RETURN_FAULT = 20;

	/** 节点没有注册 **/
	public static final int SITE_NOT_LOGING = 21;
	
	/** 队列中有分布任务 **/
	public static final int REFUSE_REFRESH_CYBER = 22;
	
	/** 加载用户资源失败！ **/
	public static final int LOAD_USER_RESOURCE_FAILED = 23;
	
	/** 登录以下集群重试 **/
	public static final int TOP_RETRY = 24;

	public static final int HOME_RETRY = 25;

	public static final int BANK_RETRY = 26;

	public static final int TOP_HOME_RETRY = 27;

	public static final int TOP_BANK_RETRY = 28;

	public static final int HOME_BANK_RETRY = 29;
	
	/** 离线状态，非在线状态，拒绝执行任何网络处理！ **/
	public static final int OFFLINE_REFUSE = 30;
	
	
	/** 以下是带X后缀的标签，从10000开始 **/

	/** ... 失败 **/
	public static final int FAILED_X = 10000;

	/** ... 没有找到 **/
	public static final int NOTFOUND_X = 10001;
	
	/** 不能提交到 ... **/
	public static final int CANNOT_SUBMIT_X = 10002;

	/** 语法错误 ... **/
	public static final int INCORRECT_SYNTAX_X = 10003;

	/** 不支持 ... **/
	public static final int NOTSUPPORT_X = 10004;

	/** 不能解析 ... **/
	public static final int NOTRESOLVE_X = 10005;

	/** 参数不足 ... **/
	public static final int PARAM_MISSING_X = 10006;

	/** 无效站点 ... **/
	public static final int ILLEGAL_SITE_X = 10007;

	/** ... 已经存在 **/
	public static final int EXISTED_X = 10008;
	
	/** ... 发生冲突 **/
	public static final int OCCURED_CONFLICT = 10009;
	
	/** ... 不匹配 **/
	public static final int NOTMATCH_X = 10010;
	
	/** 销毁站点 **/
	public static final int DESTROY_NODE_X = 10011;
	
	/** 没有找到站点 **/
	public static final int NOTFOUND_SITE_X = 10012;
	
	/** 无效的SQL WHERE比较符 **/
	public static final int ILLEGAL_WHERE_OPERATOR = 10013;
	
	/** 无效的字符集 **/
	public static final int ILLEGAL_CHARSET_X = 10014;
	
	/** 权限不足 **/
	public static final int PERMISSION_MISSING_X = 10015;
	
	/** 许可证超时 **/
	public static final int LICENCE_TIMEOUT_X = 10016;
	
	/** 禁用关键字 **/
	public static final int FORBID_KEYWORD_X = 10017;
	
	/** 无效值 **/
	public static final int ILLEGAL_VALUE_X = 10018;
	
	/** 没有找到应用组件... **/
	public static final int NOTFOUND_TASK_X = 10019;

	/** 插入、删除、更新失败 **/
	public final static int INSERT_FAILED_X = 10020;
	public final static int DELETE_FAILED_X = 10021;
	public final static int UPDATE_FAILED_X = 10022;
	
	/** SQL错误 **/
	public final static int SQL_ILLEGAL_COLUMN_X = 10030; // "无效列：%s"
	public final static int SQL_OVERLAP_COLUMN_X = 10031; // "重复列：%s"
	public final static int SQL_ILLEGAL_KEY_X = 10032; // "无效索引键：%s"
	public final static int SQL_ILLEGAL_FUNCTION_X = 10033; // "无效函数：%s"
	public final static int SQL_ILLEGAL_VALUE_X = 10034; // "无效数值：%s"
	public final static int SQL_INCORRECT_SYNTAX_X = 10035; // "无效语句"
	public final static int SQL_ILLEGAL_ATTRIBUTE_X = 10036; // "无效列属性：%s"
	
	public static final int SQL_TYPE_NOTMATCH_X = 10037; // 类型不匹配
	public static final int SQL_CANNOTSUPPORT_X = 10038;
	public static final int SQL_NOT_INDEXKEY_X = 10039;
	public static final int SQL_KEY_MISSING_X = 10040;
	public static final int SQL_PRIMEKEY_MEMBEROUT_X = 10041;
	public static final int SQL_DATABASE_NAME_SIZEOUT_X = 10042;
	public static final int SQL_TABLE_NAME_SIZEOUT_X = 10043;
	public static final int SQL_ILLEGAL_STYLE_X = 10044;
	public final static int SQL_ILLEGAL_AGGREGATE_FUNCTION_X = 10045; // "无效的聚合函数：%s"
	
	public final static int SQL_GROUPBY_ELEMENT_MISSING_X = 10046; // "%s 不在GROUP BY列表中"
	public final static int SQL_ORDERBY_INCORRECT_SYNTAX_X = 10047; // "ORDER BY语句错误！%s"
	

}