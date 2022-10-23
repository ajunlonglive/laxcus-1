/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.*;
import java.util.regex.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.permit.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 授权解析器。<br><br>
 * 
 * 对应SQL的“GRANT”语法。对用户、数据库、数据表进行授权。
 * 权限表文本参数见Control类中定义。<br><br>
 * 
 * @author scott.liang
 * @version 1.2 7/16/2012
 * @since laxcus 1.0
 */
public class GrantParser extends PermitParser {
	
	/** 简单语法 **/
	private final static String GRANT_TITLE = "^\\s*(?i)(?:GRANT)\\s+(?i)([\\w\\W]+)\\s*$";

	/** 用户权限定义，格式: GRANT 权限表  TO 用户名（允许多个） **/
	private final static String GRANT_USER   = "^\\s*(?i)(?:GRANT)\\s+(\\p{Print}+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/** 数据库权限定义，格式: GRANT 权限表  ON SCHEMA|ON DATABASE 数据库名   TO 用户名(只能有一个) **/
	private final static String GRANT_SCHEMA = "^\\s*(?i)(?:GRANT)\\s+(\\p{Print}+?)\\s+(?i)(?:ON\\s+SCHEMA|ON\\s+DATABASE)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$"; 

	/** 数据库表权限定义，格式: GRANT 权限表 ON TABLE 库名.表名, 库名.表名 TO 用户名 (只能有一个) */
	private final static String GRANT_TABLE  = "^\\s*(?i)(?:GRANT)\\s+(\\p{Print}+?)\\s+(?i)(?:ON\\s+TABLE)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";
	
	// "^\\s*(?i)(?:GRANT)\\s+(\\p{Print}+?)\\s+(?i)(?:ON\\s+TABLE)\\s+(\\w+)\\.(\\w+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$"; 

	/**
	 * 构造授权解析器
	 */
	public GrantParser() {
		super();
	}

	/**
	 * 检查传入语句匹配权限语法
	 * @param simple
	 * @param input 输入语句
	 * @return 匹配返回“真“，否则”假“。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			Pattern pattern = Pattern.compile(GrantParser.GRANT_TITLE);
			Matcher matcher = pattern.matcher(input);
			return matcher.matches();
		}
		
		Pattern pattern = Pattern.compile(GrantParser.GRANT_TABLE);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(GrantParser.GRANT_SCHEMA);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			pattern = Pattern.compile(GrantParser.GRANT_USER);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析授权配置
	 * @param input 输入语句
	 * @return 返回Grant实例
	 */
	public Grant split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		// 只有管理员或者等同于管理身份的用户才能操作
		if (online) {
			boolean success = (isAdministrator() || isSameAdministrator());
			if (!success) {
				throwableNo(FaultTip.PERMISSION_MISSING);
			}
		}
		
		// 1. 对数据表授权
		Grant grant = splitTable(input, online);
		// 2. 对数据库授权
		if (grant == null) {
			grant = splitSchema(input, online);
		}
		// 3. 对用户授权
		if (grant == null) {
			grant = splitUser(input, online);
		}
		// 出错
		if (grant == null) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		grant.setPrimitive(input);
		return grant;
	}
	
	/**
	 * 解析表名
	 * @param input 输入字符串
	 * @return 任意多个表名
	 */
	private Space[] splitSpaces(String input) {
		String[] texts = splitCommaSymbol(input);
		ArrayList<Space> array = new ArrayList<Space>();

		for (String text : texts) {
			// 如果不符合语法
			if (!Space.validate(text)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, text);
			}
			// 保存表名
			array.add(new Space(text));
		}

		Space[] spaces = new Space[array.size()];
		return array.toArray(spaces);
	}
	
	/**
	 * 解析数据表授权语句
	 * @param input 格式: GRANT [operator] ON schema.table TO username
	 * @param online 在线模式
	 * @return 返回权限配置
	 */
	private Grant splitTable(String input, boolean online) {
		Pattern pattern = Pattern.compile(GrantParser.GRANT_TABLE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}
		
		// 操作权限
		String[] options = splitCommaSymbol(matcher.group(1));
		// 数据表
		Space[] spaces = splitSpaces(matcher.group(2));
		// 用户账号名
		String[] users = splitCommaSymbol(matcher.group(3));

		// 只允许一个，否则拒绝
		int size = (users != null ? users.length : 0);
		if (size != 1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX);
		}
		
		String username = users[0];
		// 检查用户
		if (online) {
			if (!hasUser(username)) {
				throwableNo(FaultTip.NOTFOUND_X, username);
			}
		}
		// 检查数据表
		if (online) {
			// 找到账号
			Siger siger = SHAUser.doUsername(username);
			Account account = findAccount(siger);
			if (account == null) {
				throwableNo(FaultTip.NOTFOUND_X, username);
			}
			// 判断表存在
			for (Space space : spaces) {
				if (!account.hasTable(space)) {
					throwableNo(FaultTip.NOTFOUND_X, space);
				}
			}
		}

		TablePermit permit = new TablePermit();
		short[] symbols = splitControlTags(options);

		Control control = new Control();
		boolean success = control.setOption(PermitTag.TABLE_PERMIT, symbols);
		if (!success) {
			throwableNo(FaultTip.ILLEGAL_VALUE_X, matcher.group(1));
		}
		// 输入多个
		for (Space space : spaces) {
			permit.add(space, control);
		}

		Grant grant = new Grant();
		grant.addUser(username);
		grant.setPermit(permit);
		return grant;
	}

//	/**
//	 * 解析数据表授权语句
//	 * @param input 格式: GRANT [operator] ON schema.table TO username
//	 * @param online 在线模式
//	 * @return 返回权限配置
//	 */
//	private Grant splitTable(String input, boolean online) {
//		Pattern pattern = Pattern.compile(GrantParser.GRANT_TABLE);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			return null;
//		}
//
//		// 数据库表
//		Space space = new Space(matcher.group(2), matcher.group(3));
//		// 操作权限
//		String[] options = splitCommaSymbol(matcher.group(1));
//		// 用户账号名
//		String[] users = splitCommaSymbol(matcher.group(4));
//
//		// 检查数据库表
//		if (online) {
//			if (!hasTable(space)) {
//				throwableNo(FaultTip.NOTFOUND_X, space);
//			}
//		}
//		// 检查用户
//		for (int i = 0; i < users.length; i++) {
//			if (online) {
//				if (!hasUser(users[i])) {
//					throwableNo(FaultTip.NOTFOUND_X, users[i]);
//				}
//			}
//		}
//
//		TablePermit permit = new TablePermit();
//		short[] symbols = splitControlTags(options);
//
//		Control control = new Control();
//		boolean success = control.setOption(PermitTag.TABLE_PERMIT, symbols);
//		if (!success) {
//			// throwable("illegal option %s", matcher.group(1));
//			throwableNo(FaultTip.ILLEGAL_VALUE_X, matcher.group(1));
//		}
//		permit.add(space, control);
//
//		Grant grant = new Grant();
//		grant.setUsers(users);
//		grant.setPermit(permit);
//		return grant;
//	}

	/**
	 * 解析数据库权限
	 * @param input 输入语句。格式：GRANT [operator] ON SCHEMA schemaname TO username
	 * @param online 在线模式
	 * @return 用户授权命令
	 */
	private Grant splitSchema(String input, boolean online) {
		Pattern pattern = Pattern.compile(GrantParser.GRANT_SCHEMA);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}

		String[] options = splitCommaSymbol(matcher.group(1));
		String[] schemas = splitCommaSymbol(matcher.group(2));
		String[] users = splitCommaSymbol(matcher.group(3));

		// 只允许一个用户账号，否则拒绝
		int size = (users != null ? users.length : 0);
		if (size != 1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX);
		}
		
		String username  = users[0];
		// 检查用户
		if (online) {
			if (!hasUser(username)) {
				throwableNo(FaultTip.NOTFOUND_X, username);
			}
		}
		if (online) {
			// 找到账号
			Siger siger = SHAUser.doUsername(username);
			Account account = findAccount(siger);
			if (account == null) {
				throwableNo(FaultTip.NOTFOUND_X, username);
			}

			// 检查数据库
			for (int i = 0; i < schemas.length; i++) {
				Fame fame = new Fame(schemas[i]);
				if (!account.hasSchema(fame)) {
					throwableNo(FaultTip.NOTFOUND_X, fame);
				}
			}
		}

		SchemaPermit permit = new SchemaPermit();
		short[] symbols = splitControlTags(options);

		Control control = new Control();
		boolean success = control.setOption(PermitTag.SCHEMA_PERMIT, symbols);
		if (!success) {
			throwableNo(FaultTip.ILLEGAL_VALUE_X, matcher.group(1));
		}
		for (String schema : schemas) {
			permit.add(new Fame(schema), control);
		}

		Grant grant = new Grant();
		grant.addUser(username);
		grant.setPermit(permit);
		return grant;
	}

	/**
	 * 解析授权用户的配置
	 * @param input GRANT [options] TO username
	 * @param online 在线模式
	 * @return 返回GRANT命令实例
	 */
	private Grant splitUser(String input, boolean online) {
		Pattern pattern = Pattern.compile(GrantParser.GRANT_USER);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}

		String[] options = splitCommaSymbol(matcher.group(1));
		String[] users = splitCommaSymbol(matcher.group(2));

		// 在线检查注册用户
		for (int i = 0; i < users.length; i++) {
			if (online) {
				if (!hasUser(users[i])) {
					throwableNo(FaultTip.NOTFOUND_X, users[i]);
				}
			}
		}

		// 解析用户权限
		UserPermit permit = new UserPermit();
		short[] symbols = splitControlTags(options);

		Control control = new Control();
		boolean success = control.setOption(PermitTag.USER_PERMIT, symbols);
		// 如果设置有错误，弹出异常
		if (!success) {
			throwableNo(FaultTip.ILLEGAL_VALUE_X, matcher.group(1));
		}
		permit.add(control);

		// 设置授权参数
		Grant grant = new Grant();
		grant.setUsers(users);
		grant.setPermit(permit);

		return grant;
	}

}