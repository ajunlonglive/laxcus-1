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
import com.laxcus.command.task.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 检索分布任务组件解析器。<br><br>
 * 
 * 语法格式：<br>
 * 1. SEEK TASK [ALL] ON [SIGN ...] <br>
 * 2. SEEK TASK [ALL] ON [....] <br>
 * 3. SEEK TASK [TASK NAME] ON [SIGN ...] <br>
 * 4. SEEK TASK [TASK NAME] ON [...] <br>
 * 5. SEEK TASK [TASK NAME] <br><br>
 * 
 * 如：SEEK TASK SYSTEM.SELECT, CLIENT_SOFTWARE.MINE ON TINY, VICTOR
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public class SeekTaskParser extends SyntaxParser {

	/** 检索分布任务组件命令集合 **/
	private final static String REGEX1 = "^\\s*(?i)(?:SEEK\\s+TASK\\s+ALL)\\s+(?i)(?:ON\\s+SIGN)\\s+([0-9a-fA-F]{64})\\s*$";

	private final static String REGEX2 = "^\\s*(?i)(?:SEEK\\s+TASK\\s+ALL)\\s+(?i)(?:ON)\\s+([\\w\\W]+?)\\s*$";

	private final static String REGEX3 = "^\\s*(?i)(?:SEEK\\s+TASK)\\s+([\\w\\W]+)\\s+(?i)(?:ON\\s+SIGN)\\s+([0-9a-fA-F]{64})\\s*$";

	private final static String REGEX4 = "^\\s*(?i)(?:SEEK\\s+TASK)\\s+([\\w\\W]+)\\s+(?i)(?:ON)\\s+([\\w\\W]+?)\\s*$";

	private final static String REGEX5 = "^\\s*(?i)(?:SEEK\\s+TASK)\\s+([\\w\\W]+)\\s*$";

	/** 阶段命名根名称 **/
	private final static String ROOT = "^\\s*(?i)([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+)\\s*$";
	
	/**
	 * 构造检索分布任务组件解析器
	 */
	public SeekTaskParser() {
		super();
	}

	/**
	 * 判断语句匹配“SEEK TASK”
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SEEK TASK", input);
		}
		// 1. 用户的全部（SHA256）
		Pattern pattern = Pattern.compile(SeekTaskParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		// 2. 用户的全部
		if (!success) {
			pattern = Pattern.compile(SeekTaskParser.REGEX2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		// 3. 用户的某个组件（SHA256）
		if (!success) {
			pattern = Pattern.compile(SeekTaskParser.REGEX3);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		// 4. 用户的某个组件
		if (!success) {
			pattern = Pattern.compile(SeekTaskParser.REGEX4);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		// 5. 某个组件
		if (!success) {
			pattern = Pattern.compile(SeekTaskParser.REGEX5);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}
	
	/**
	 * 解析分布任务组件名称，包括软件名称和根命名
	 * @param input 输入参数
	 * @return 列表
	 */
	private List<Sock> splitSocks(String input) {
		TreeSet<Sock> array = new TreeSet<Sock>();
		String[] items = splitCommaSymbol(input);
		for (String item : items) {
			Pattern pattern = Pattern.compile(SeekTaskParser.ROOT);
			Matcher matcher = pattern.matcher(item);
			// 不匹配，弹出错误
			if (!matcher.matches()) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
			}
			// 保存阶段命名
			array.add(new Sock(item));
		}
		return new ArrayList<Sock>(array);
	}

	/**
	 * 解析“SEEK TASK ...”命令
	 * @param input 输入语句
	 * @return 返回SeekTask命令
	 */
	public SeekTask split(String input) {
		SeekTask cmd = null;
		// 1. 第一种情况
		Pattern pattern = Pattern.compile(SeekTaskParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String hex = matcher.group(1);
			cmd = new SeekTask(new Siger(hex));
		}
		// 2. 第二种情况
		if (cmd == null) {
			pattern = Pattern.compile(SeekTaskParser.REGEX2);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				Siger siger = SHAUser.doUsername(text);
				cmd = new SeekTask(siger);
			}
		}
		// 3. 第3种情况
		if (cmd == null) {
			pattern = Pattern.compile(SeekTaskParser.REGEX3);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				List<Sock> roots = splitSocks(matcher.group(1));
				String hex = matcher.group(2);
				cmd = new SeekTask(new Siger(hex), roots);
			}
		}
		// 4. 第4种情况
		if (cmd == null) {
			pattern = Pattern.compile(SeekTaskParser.REGEX4);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				List<Sock> roots = splitSocks(matcher.group(1));
				String username = matcher.group(2);
				Siger siger = SHAUser.doUsername(username);
				cmd = new SeekTask(siger, roots);
			}
		}
		// 5. 第5种情况
		if (cmd == null) {
			pattern = Pattern.compile(SeekTaskParser.REGEX5);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				List<Sock> roots = splitSocks(matcher.group(1));
				cmd = new SeekTask(roots);
			}
		}
		// 以上不成功，弹出异常
		if (cmd == null) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}