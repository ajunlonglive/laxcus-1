/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

/**
 * 资源控制选项 <br><br>
 * 
 * 说明：<br>
 * 1. 资源控制从高到低，分为“用户、数据库、表”三个级别。<br>
 * 2. 同一个控制选项，上一级高于下一级。如用户级的SELECT高于数据库级的SELECT，数据库级的SELECT高于表级的SELECT。<br>
 * 3. “DBA/ALL/MEMBER”是三个个特殊的关键字。DBA属于用户级，拥有所有控制选项。ALL分别针对“用户、数据库、表”三个级别，每个级别的“ALL”有不同的定义。<br>
 * 4. “DBA/ALL/MEMBER”与其它控制选项是一对多关系。<br>
 * 5. “GRANT/REVOKE/CREATE USER/DROP USER”是“DBA”专属选项。<br><br>
 * 
 * 资源控制举例：<br>
 * “GRANT SELECT TO [user]”，“GRANT SELECT TO [schema]”，“GRANT SELECT to [schema.table]” <br>
 * 用户级SELECT操作所有表，数据库级SELECT能操作某个数据库下属所有表，表级SELECT只能针对一个表进行操作。其它选项类似。<br>
 * 
 * 
 * @author scott.liang
 * @version 1.05 11/03/2009
 * @since laxcus 1.0
 */
public final class ControlTag {

	/** 表级资源控制 **/
	public final static short SELECT = 1;
	public final static short INSERT = 2;
	public final static short DELETE = 3;
	public final static short UPDATE = 4;
	public final static short CONDUCT = 5;
	public final static short CONTACT = 6; // CS架构计算，或者集群的小规模快速计算
	public final static short ESTABLISH = 7;
	public final static short EXPORT_ENTITY = 8;	// 导出数据块
	public final static short IMPORT_ENTITY = 9;	// 导入数据块
	

	/** 以下是用户级资源控制 **/

	/** 授权/撤销授权 **/
	public final static short GRANT = 10;
	public final static short REVOKE = 11;

	/** 建立/删除账号，修改账号密码 **/
	public final static short CREATE_USER = 20;
	public final static short DROP_USER = 21;
	public final static short ALTER_USER = 22;

	/** 建立/删除数据库 **/
	public final static short CREATE_SCHEMA = 23;
	public final static short DROP_SCHEMA = 24;

	/** 数据库级资源控制 **/
	public final static short CREATE_TABLE = 25;
	public final static short DROP_TABLE = 26;

	/** 开放/关闭共享资源（包括共享数据库和共享数据表两个层级，实际最终都指向数据表） **/
	public final static short OPEN_RESOURCE = 30;
	public final static short CLOSE_RESOURCE = 31;

	/** 建立/撤销故障规则 **/
	public final static short CREATE_FAULT = 32;
	public final static short DROP_FAULT = 33;

	/** 建立/撤销限制操作规则（用于可调CAP策略） **/
	public final static short CREATE_LIMIT = 34;
	public final static short DROP_LIMIT = 35;

	/** 发布分布任务组件 **/
	public final static short PUBLISH_TASK = 61;
	public final static short PUBLISH_TASK_LIBRARY = 62;

	/** 加载索引/数据块到内存 **/
	public final static short LOAD_INDEX = 81;
	public final static short LOAD_ENTITY = 82;
	
	/** 独享单个用户计算机机资源 **/
	public final static short EXCLUSIVE = 91;

	/** 成员控制选项，低于“ALL”选项 **/
	public final static short MEMBER = 3000;

	/** 用户的全部控制选项，针对“用户、数据库、表”三个级别，分别有不同的资源控制。 **/
	public final static short ALL = 3001;

	/** 管理员权限，允许所有操作，属于“用户”级别 **/
	public final static short DBA = 3002;
	
	
	/** 用户级“MEMBER” 控制选项(允许修改账号密码和处理数据库、快捷组件操作，不能操作动态库等) **/
	public final static short[] MEMBER_OPTIONS = { ControlTag.ALTER_USER,
		ControlTag.PUBLISH_TASK,  
		ControlTag.CREATE_FAULT, ControlTag.DROP_FAULT,
		ControlTag.CREATE_LIMIT, ControlTag.DROP_LIMIT,
		ControlTag.CREATE_SCHEMA, ControlTag.DROP_SCHEMA, 
		ControlTag.CREATE_TABLE, ControlTag.DROP_TABLE, 
		ControlTag.SELECT, ControlTag.INSERT, ControlTag.DELETE, ControlTag.UPDATE, 
		ControlTag.CONDUCT, ControlTag.CONTACT, ControlTag.ESTABLISH,
		ControlTag.EXPORT_ENTITY, ControlTag.IMPORT_ENTITY};
	
	/** 用户级“ALL” 控制选项(允许修改账号密码和处理数据库、快捷组件操作) **/
	public final static short[] ALL_OPTIONS = { ControlTag.ALTER_USER,
		ControlTag.PUBLISH_TASK, ControlTag.PUBLISH_TASK_LIBRARY,
		ControlTag.OPEN_RESOURCE, ControlTag.CLOSE_RESOURCE,
		ControlTag.CREATE_FAULT, ControlTag.DROP_FAULT,
		ControlTag.CREATE_LIMIT, ControlTag.DROP_LIMIT,
		ControlTag.CREATE_SCHEMA, ControlTag.DROP_SCHEMA, 
		ControlTag.CREATE_TABLE, ControlTag.DROP_TABLE, 
		ControlTag.SELECT, ControlTag.INSERT, ControlTag.DELETE, ControlTag.UPDATE, 
		ControlTag.CONDUCT, ControlTag.CONTACT, ControlTag.ESTABLISH,
		ControlTag.EXPORT_ENTITY, ControlTag.IMPORT_ENTITY, 
		ControlTag.LOAD_INDEX, ControlTag.LOAD_ENTITY, ControlTag.EXCLUSIVE};

	/** 管理员控制选项(允许所有操作，属于用户级) */
	public final static short[] DBA_OPTIONS = { ControlTag.GRANT, ControlTag.REVOKE,
		ControlTag.CREATE_USER, ControlTag.DROP_USER, ControlTag.ALTER_USER,
		ControlTag.PUBLISH_TASK, ControlTag.PUBLISH_TASK_LIBRARY,
		ControlTag.OPEN_RESOURCE, ControlTag.CLOSE_RESOURCE,
		ControlTag.CREATE_FAULT, ControlTag.DROP_FAULT,
		ControlTag.CREATE_LIMIT, ControlTag.DROP_LIMIT,
		ControlTag.CREATE_SCHEMA, ControlTag.DROP_SCHEMA,
		ControlTag.CREATE_TABLE, ControlTag.DROP_TABLE, 
		ControlTag.SELECT, ControlTag.DELETE, ControlTag.INSERT, ControlTag.UPDATE, 
		ControlTag.CONDUCT, ControlTag.CONTACT, ControlTag.ESTABLISH,
		ControlTag.EXPORT_ENTITY, ControlTag.IMPORT_ENTITY,
		ControlTag.LOAD_INDEX, ControlTag.LOAD_ENTITY, ControlTag.EXCLUSIVE};

	
	/** 数据库级“ALL”控制选项 **/
	public final static short[] SCHEMA_OPTIONS = new short[] {
		ControlTag.CREATE_TABLE, ControlTag.DROP_TABLE,
		ControlTag.SELECT, ControlTag.INSERT, ControlTag.DELETE,
		ControlTag.UPDATE, ControlTag.CONDUCT, ControlTag.CONTACT, ControlTag.ESTABLISH ,
		ControlTag.EXPORT_ENTITY, ControlTag.IMPORT_ENTITY};

	/** 表级“ALL”控制选项 */
	public final static short[] TABLE_OPTIONS = new short[] { 
		ControlTag.SELECT, ControlTag.INSERT, ControlTag.DELETE, 
		ControlTag.UPDATE, ControlTag.CONDUCT, ControlTag.CONTACT, ControlTag.ESTABLISH,
		ControlTag.EXPORT_ENTITY, ControlTag.IMPORT_ENTITY};


	/**
	 * 翻译标识
	 * @param who
	 * @return
	 */
	public static String translate(short who) {
		switch(who){
		/** 表级资源控制 **/
		case ControlTag.SELECT:
			return "SELECT";
		case ControlTag.INSERT:
			return "INSERT INTO";
		case ControlTag.DELETE:
			return "DELETE";
		case ControlTag.UPDATE:
			return "UPDATE";
		case ControlTag.CONDUCT:
			return "CONDUCT";
		case ControlTag.CONTACT:
			return "CONTACT";
		case ControlTag.ESTABLISH:
			return "ESTABLISH";
		case ControlTag.IMPORT_ENTITY:
			return "IMPORT ENTITY";
		case ControlTag.EXPORT_ENTITY:
			return "EXPORT ENTITY";
			/** 数据库级资源控制 **/
		case ControlTag.CREATE_TABLE:
			return "CREATE TABLE";
		case ControlTag.DROP_TABLE:
			return "DROP TABLE";
			/** 授权/撤销授权 **/
		case ControlTag.GRANT:
			return "GRANT";
		case ControlTag.REVOKE:
			return "REVOKE";
			/** 建立/删除账号，修改账号密码 **/
		case ControlTag.CREATE_USER:
			return "CREATE USER";
		case ControlTag.DROP_USER:
			return "DROP USER";
		case ControlTag.ALTER_USER:
			return "ALTER USER";
			/** 建立/删除数据库 **/
		case ControlTag.CREATE_SCHEMA:
			return "CREATE DATABASE";
		case ControlTag.DROP_SCHEMA:
			return "DROP DATABASE";

			// 发布任务组件
		case ControlTag.PUBLISH_TASK:
			return "PUBLISH TASK COMPONENT";
		case ControlTag.PUBLISH_TASK_LIBRARY:
			return "PUBLISH TASK LIBRARY COMPONENT";
		// 加载索引/数据到内存
		case ControlTag.LOAD_INDEX:
			return "LOAD INDEX";
		case ControlTag.LOAD_ENTITY:
			return "LOAD ENTITY";
		case ControlTag.EXCLUSIVE:
			return "EXCLUSIVE";

		/** 开放/关闭共享资源（包括共享数据库和共享数据表两个层级，实际最终都指向数据表） **/
		case ControlTag.OPEN_RESOURCE:
			return "OPEN RESOURCE";
		case ControlTag.CLOSE_RESOURCE:
			return "CLOSE RESOURCE";
			/** 建立/撤销故障 **/
		case ControlTag.CREATE_FAULT:
			return "CREATE FAULT";
		case ControlTag.DROP_FAULT:
			return "DROP FAULT";
			/** 建立/撤销限制 **/
		case ControlTag.CREATE_LIMIT:
			return "CREATE LIMIT";
		case ControlTag.DROP_LIMIT:
			return "DROP LIMIT";
		}
		return "NONE";
	}

	/**
	 * 将文本翻译成资源控制选项
	 * 
	 * @param input 文本描述
	 * @return 资源控制选项
	 */
	public static short translate(String input) {
		// 成员、全部、数据管理员，三个选项
		if (input.matches("^\\s*(?i)MEMBER\\s*$")) {
			return ControlTag.MEMBER;
		} else if (input.matches("^\\s*(?i)ALL\\s*$")) {
			return ControlTag.ALL;
		} else if (input.matches("^\\s*(?i)DBA\\s*$")) {
			return ControlTag.DBA;
		}
		// 操作命令
		if (input.matches("^\\s*(?i)SELECT\\s*$")) {
			return ControlTag.SELECT;
		} else if (input.matches("^\\s*(?i)(?:INSERT\\s+INTO|INJECT\\s+INTO)\\s*$") ) {
			return ControlTag.INSERT;
		} else if (input.matches("^\\s*(?i)DELETE\\s*$")) {
			return ControlTag.DELETE;
		} else if (input.matches("^\\s*(?i)UPDATE\\s*$")) {
			return ControlTag.UPDATE;
		} else if (input.matches("^\\s*(?i)CONDUCT\\s*$")) {
			return ControlTag.CONDUCT;
		} else if (input.matches("^\\s*(?i)CONTACT\\s*$")) {
			return ControlTag.CONTACT;
		} else if (input.matches("^\\s*(?i)ESTABLISH\\s*$")) {
			return ControlTag.ESTABLISH;
		} else if (input.matches("^\\s*(?i)(?:EXPORT\\s+ENTITY)\\s*$")) {
			return ControlTag.EXPORT_ENTITY;
		} else if (input.matches("^\\s*(?i)(?:IMPORT\\s+ENTITY)\\s*$")) {
			return ControlTag.IMPORT_ENTITY;
		}
		// 管理命令
		else if (input.matches("^\\s*(?i)GRANT\\s*$")) {
			return ControlTag.GRANT;
		} else if (input.matches("^\\s*(?i)REVOKE\\s*$")) {
			return ControlTag.REVOKE;
		}
		// 资源命令
		else if (input.matches("^\\s*(?i)(?:CREATE\\s+USER)\\s*$")) {
			return ControlTag.CREATE_USER;
		} else if (input.matches("^\\s*(?i)(?:DROP\\s+USER)\\s*$")) {
			return ControlTag.DROP_USER;
		} else if (input.matches("^\\s*(?i)(?:ALTER\\s+USER)\\s*$")) {
			return ControlTag.ALTER_USER;
		} else if (input.matches("^\\s*(?i)(?:CREATE\\s+DATABASE)\\s*$")) {
			return ControlTag.CREATE_SCHEMA;
		} else if (input.matches("^\\s*(?i)(?:DROP\\s+DATABASE)\\s*$")) {
			return ControlTag.DROP_SCHEMA;
		} else if (input.matches("^\\s*(?i)(?:CREATE\\s+TABLE)\\s*$")) {
			return ControlTag.CREATE_TABLE;
		} else if (input.matches("^\\s*(?i)(?:DROP\\s+TABLE)\\s*$")) {
			return ControlTag.DROP_TABLE;
		}
		// 发布分布任务组件和动态链接库
		else if (input.matches("^\\s*(?i)(?:PUBLISH\\s+TASK\\+COMPONENT)\\s*$")) {
			return ControlTag.PUBLISH_TASK;
		} else if (input.matches("^\\s*(?i)(?:PUBLISH\\s+TASK\\s+LIBRARY\\s+COMPONENT)\\s*$")) {
			return ControlTag.PUBLISH_TASK_LIBRARY;
		}
		// 加载索引/数据数据、独序计算机资源
		else if (input.matches("^\\s*(?i)(?:LOAD\\s+INDEX)\\s*$")) {
			return ControlTag.LOAD_INDEX;
		} else if (input.matches("^\\s*(?i)(?:LOAD\\s+ENTITY)\\s*$")) {
			return ControlTag.LOAD_ENTITY;
		} else if (input.matches("^\\s*(?i)(?:EXCLUSIVE)\\s*$")) {
			return ControlTag.EXCLUSIVE;
		}
		// 资源共享
		else if (input.matches("^\\s*(?i)(?:OPEN\\s+RESOURCE)\\s*$")) {
			return ControlTag.OPEN_RESOURCE;
		} else if (input.matches("^\\s*(?i)(?:CLOSE\\s+RESOURCE)\\s*$")) {
			return ControlTag.CLOSE_RESOURCE;
		}
		// 故障规则
		else if (input.matches("^\\s*(?i)(?:CREATE\\s+FAULT)\\s*$")) {
			return ControlTag.CREATE_FAULT;
		} else if (input.matches("^\\s*(?i)(?:DROP\\s+FAULT)\\s*$")) {
			return ControlTag.DROP_FAULT;
		}
		// 可调CAP策略
		else if (input.matches("^\\s*(?i)(?:CREATE\\s+LIMIT)\\s*$")) {
			return ControlTag.CREATE_LIMIT;
		} else if (input.matches("^\\s*(?i)(?:DROP\\s+LIMIT)\\s*$")) {
			return ControlTag.DROP_LIMIT;
		}

		return -1;
	}
}
