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
 * 回收权限解析器。<br><br>
 * 
 * 对应SQL的“REVOKE”语法。回收用户、数据库、数据表的权限。
 * 权限表文本参数见Control类中定义。<br><br>
 * 
 * 例如: REVOKE all ON DATABASE system_table FROM histes 
 * 
 * @author scott.liang
 * @version 1.2 7/28/2012
 * @since laxcus 1.0
 */
public class RevokeParser extends PermitParser {
	
	/** 标题 **/
	private final static String REVOKE_TITLE = "^\\s*(?i)(?:REVOKE)\\s+(?i)([\\w\\W]+)\\s*$";

	/** 回收用户权限，格式: REVOKE 权限表 FROM 用户名表 **/
	private final static String REVOKE_USER = "^\\s*(?i)(?:REVOKE)\\s+(\\p{Print}+?)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s*$";

	/** 回收数据库权限，格式: REVOKE 权限表 ON SCHEMA|ON DATABASE 数据库名 FROM 用户名 **/
	private final static String REVOKE_SCHEMA = "^\\s*(?i)(?:REVOKE)\\s+(\\p{Print}+?)\\s+(?i)(?:ON\\s+SCHEMA|ON\\s+DATABASE)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s*$";
	
	/** 回收数据库表权限，格式: REVOKE 权限表  ON TABLE 表名  FROM 用户名 **/
	private final static String REVOKE_TABLE = "^\\s*(?i)(?:REVOKE)\\s+(\\p{Print}+?)\\s+(?i)(?:ON\\s+TABLE)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s*$";
	
	// "^\\s*(?i)(?:REVOKE)\\s+(\\p{Print}+?)\\s+(?i)(?:ON\\s+TABLE)\\s+(\\w+)\\.(\\w+)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造回收权限解析器
	 */
	public RevokeParser() {
		super();
	}

	/**
	 * 检查传入语句匹配回收权限语法
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			Pattern pattern = Pattern.compile(RevokeParser.REVOKE_TITLE);
			Matcher matcher = pattern.matcher(input);
			return matcher.matches();
		}
		
		Pattern pattern = Pattern.compile(RevokeParser.REVOKE_TABLE);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(RevokeParser.REVOKE_SCHEMA);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			pattern = Pattern.compile(RevokeParser.REVOKE_USER);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}
	
	/**
	 * 解析回收权限语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回Revoke命令
	 */
	public Revoke split(String input, boolean online) {
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

		//1. 回收数据库表操作权限
		Revoke revoke = splitTable(input, online);
		//2. 回收数据库操作权限
		if (revoke == null) {
			revoke = splitSchema(input, online);
		}
		//3. 回收用户权限
		if (revoke == null) {
			revoke = splitUser(input, online);
		}
		// 出错
		if (revoke == null) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		revoke.setPrimitive(input);
		return revoke;
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
			// 如果不符合时...
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
	 * 解析回收数据表权限
	 * @param input  格式: REVOKE [options] ON schema.table FROM user1, user2,...
	 * @param online 在线模式
	 * @return 返回权限配置
	 */
	private Revoke splitTable(String input, boolean online) {
		Pattern pattern = Pattern.compile(RevokeParser.REVOKE_TABLE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}

		String[] options = splitCommaSymbol(matcher.group(1));
		Space[] spaces = splitSpaces(matcher.group(2));
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
		for (Space space : spaces) {
			permit.add(space, control);
		}
		
		Revoke revoke = new Revoke();
		revoke.addUser(username);
		revoke.setPermit(permit);

		return revoke;
	}

	
//	/**
//	 * 解析回收数据表权限
//	 * @param input  格式: REVOKE [options] ON schema.table FROM user1, user2,...
//	 * @param online 在线模式
//	 * @return 返回权限配置
//	 */
//	private Revoke splitTable(String input, boolean online) {
//		Pattern pattern = Pattern.compile(RevokeParser.REVOKE_TABLE);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			return null;
//		}
//
//		String[] options = splitCommaSymbol(matcher.group(1));
//		Space space = new Space(matcher.group(2), matcher.group(3));
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
//		Revoke revoke = new Revoke();
//		revoke.setUsers(users);
//		revoke.setPermit(permit);
//
//		return revoke;
//	}

	/**
	 * 回收数据库权限。<br>
	 * 命令格式: REVOKE [options] ON SCHEMA schema1,schema2,... FROM user1,user2,... <br>
	 * 
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return
	 */
	private Revoke splitSchema(String input, boolean online) {
		Pattern pattern = Pattern.compile(RevokeParser.REVOKE_SCHEMA);
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
		
		Revoke revoke = new Revoke();
		revoke.addUser(username);
		revoke.setPermit(permit);
		return revoke;
	}

	/**
	 * 回收用户权限。
	 * 命令格式： REVOKE [options] FROM username1,username2,...
	 * @param input 输入语句  
	 * @param online 在线模式
	 * @return 返回权限配置
	 */
	private Revoke splitUser(String input, boolean online) {
		Pattern pattern = Pattern.compile(RevokeParser.REVOKE_USER);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}
		
		String prefix = matcher.group(1);
		String suffix = matcher.group(2);
		String[] options = splitCommaSymbol(prefix);
		String[] users = splitCommaSymbol(suffix);

		// 检查用户
		for (int i = 0; i < users.length; i++) {
			if (online) {
				if (!hasUser(users[i])) {
					throwableNo(FaultTip.NOTFOUND_X, users[i]);
				}
			}
		}

		// 回收授权选项
		UserPermit permit = new UserPermit();
		short[] symbols = splitControlTags(options);
		
		Control control = new Control();
		boolean success = control.setOption(PermitTag.USER_PERMIT, symbols);
		if (!success) {
			// throwable("illegal option %s", matcher.group(1));
			throwableNo(FaultTip.ILLEGAL_VALUE_X, matcher.group(1));
		}
		permit.add(control);
		
		Revoke revoke = new Revoke();
		revoke.setUsers(users);
		revoke.setPermit(permit);

		return revoke;
	}
	
}