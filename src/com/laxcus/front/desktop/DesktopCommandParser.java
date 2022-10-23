/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import com.laxcus.access.parse.*;
import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.contact.*;
import com.laxcus.command.establish.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.listener.*;

/**
 * 分布式命令解析器
 * 
 * @author scott.liang
 * @version 1.0 4/10/2022
 * @since laxcus 1.0
 */
public class DesktopCommandParser implements CommandParser {

	/** 语法检查器 **/
	private SyntaxChecker checker = new SyntaxChecker();

	/**
	 * 构造默认的分布式命令解析器
	 */
	public DesktopCommandParser() {
		super();
	}
	
	private CreateSchema doCreateSchema(String input) {
		CreateSchemaParser parser = new CreateSchemaParser();
		return parser.split(input, true);
	}

	private CreateTable doCreateTable(String input) {
		CreateTableParser parser = new CreateTableParser();
		return parser.split(input, true);
	}

	private DropSchema doDropSchema(String input) {
		DropSchemaParser parser = new DropSchemaParser();
		return parser.split(input, true);
	}

	private DropTable doDropTable(String input) {
		DropTableParser parser = new DropTableParser();
		return parser.split(input, true);
	}
	
	private Select doSelect(String input) {
		SelectParser parser = new SelectParser();
		return parser.split(input, true);
	}
	
	private Delete doDelete(String input) {
		DeleteParser parser = new DeleteParser();
		return parser.split(input, true);
	}
	private Insert doInsert(String input) {
		InsertParser parser = new InsertParser();
		return parser.splitInsert(input, true);
	}
	
	private Insert doInject(String input) {
		InsertParser parser = new InsertParser();
		return parser.splitInject(input, true);
	}
	
	private Update doUpdate(String input) {
		UpdateParser parser = new UpdateParser();
		return parser.split(input, true);
	}
	
	private Conduct doConduct(String input) {
		ConductParser parser = new ConductParser();
		return parser.split(input, true);
	}
	
	private Establish doEstablish(String input) {
		EstablishParser parser = new EstablishParser();
		return parser.split(input, true);
	}
	
	private Contact doContact(String input) {
		ContactParser parser = new ContactParser();
		return parser.split(input, true);
	}
	
	/**
	 * 解析字符串命令
	 * @param input 字符串
	 * @return
	 */
	private Command implement(String input) {
		// 建立/删除数据库
		if (checker.isCreateSchema(input)) {
			return doCreateSchema(input);
		}
		if (checker.isDropSchema(input)) {
			return doDropSchema(input);
		}
		// 建立/删除数据表
		if (checker.isCreateTable(input)) {
			return doCreateTable(input);
		}
		if (checker.isDropTable(input)) {
			return doDropTable(input);
		}
		// 判断是SELECT命令
		if (checker.isSelect(input, false)) {
			return doSelect(input);
		}
		// 判断SQL "DELETE FROM"语句
		if (checker.isDelete(input, false)) {
			return doDelete(input);
		}
		// 判断SQL "INSERT INTO"
		if (checker.isInsert(input, false)) {
			return doInsert(input);
		}
		// 判断SQL "INJECT INTO"
		if (checker.isInject(input, false)) {
			return doInject(input);
		}
		// 判断SQL "UPDATE ... SET ..."
		if (checker.isUpdate(input, false)) {
			return doUpdate(input);
		}
		// 判断分布计算 "CONDUCT ..."
		if (checker.isConduct(input)) {
			return doConduct(input);
		}
		// 判断分布数据构建
		if (checker.isEstablish(input)) {
			return doEstablish(input);
		}
		// 分布迭代计算
		if (checker.isContact(input)) {
			return doContact(input);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.CommandParser#split(java.lang.String)
	 */
	@Override
	public Command split(String input) {
		// 解析
		try {
			return implement(input);
		} catch (SyntaxException e) {
			Logger.error(input, e);
		} catch (Throwable e) {
			Logger.fatal(input, e);
		}
		return null;
	}

}
