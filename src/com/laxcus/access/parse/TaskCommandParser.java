/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import com.laxcus.command.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.util.tip.*;

/**
 * 用户自定义命令解析器
 * 
 * @author scott.liang
 * @version 1.0 5/23/2014
 * @since laxcus 1.0
 */
public class TaskCommandParser extends SyntaxParser {

	/**
	 * 构造用户命令解析器
	 */
	public TaskCommandParser() {
		super();
	}
	
	/**
	 * 解析命令，生成返回结果
	 * @param input 输入命令
	 * @param online 在线模式
	 * @return 返回TaskCommand实例
	 */
	public TaskCommand split(String input, boolean online) {
		SyntaxChecker checker = new SyntaxChecker();

		Command cmd = null;
		if (checker.isSelect(input, online)) {
			SelectParser parser = new SelectParser();
			cmd = parser.split(input, online);
		} else if (checker.isDelete(input, online)) {
			DeleteParser parser = new DeleteParser();
			cmd = parser.split(input, online);
		} else if (checker.isUpdate(input, online)) {
			UpdateParser parser = new UpdateParser();
			cmd = parser.split(input, online);
		} else if (checker.isInject(input, online)) {
			InsertParser parser = new InsertParser();
			cmd = parser.splitInject(input, online);
		} else if (checker.isInsert(input, online)) {
			InsertParser parser = new InsertParser();
			cmd = parser.splitInsert(input, online);
		}

		// 弹出不支持命令异常
		if (cmd == null) {
			throwableNo(FaultTip.NOTSUPPORT_X, input);
		}

		// 返回命令
		TaskCommand value = new TaskCommand(); 
		value.setValue(cmd);

		return value;
	}

}
