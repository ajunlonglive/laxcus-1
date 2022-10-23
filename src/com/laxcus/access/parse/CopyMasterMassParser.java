/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 复制DATA主节点数据块解析器
 * 
 * @author scott.liang
 * @version 1.0 2019年6月15日
 * @since laxcus 1.0
 */
public class CopyMasterMassParser extends SyntaxParser {

	/** 带数据块编码的正则表达式 **/
	private static final String REGEX1 = "^\\s*(?i)(?:COPY\\s+MASTER\\s+MASS)\\s+([\\w\\W]+)\\s+(.+?)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";
	
	/** 不带数据块编号的正则表达式  **/
	private static final String REGEX2 = "^\\s*(?i)(?:COPY\\s+MASTER\\s+MASS)\\s+([\\w\\W]+)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的复制DATA主节点数据块解析器
	 */
	public CopyMasterMassParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("COPY MASTER MASS", input);
		}
		Pattern pattern = Pattern.compile(CopyMasterMassParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(CopyMasterMassParser.REGEX2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}
	
	/**
	 * 解析语句
	 * @param input 输入语句
	 * @return 返回CopyMasterMass命令
	 */
	public CopyMasterMass split(String input) {
		String spaceText = null;
		String stubsText = null;
		String masterText  = null;
		String slaveText = null;
		
		// 判断匹配
		Pattern pattern = Pattern.compile(CopyMasterMassParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			spaceText = matcher.group(1);
			stubsText = matcher.group(2);
			masterText = matcher.group(3);
			slaveText = matcher.group(4);
		}
		if (!success) {
			pattern = Pattern.compile(CopyMasterMassParser.REGEX2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				spaceText = matcher.group(1);
				masterText = matcher.group(2);
				slaveText = matcher.group(3);
			}
		}
		// 以上不成功时...
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 检查表名正确
		if (!Space.validate(spaceText)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, spaceText);
		}

		// 表名
		Space space = new Space(spaceText);
		
		// 命令！
		CopyMasterMass cmd = new CopyMasterMass(space);
		// 解析数据块编码
		if (stubsText != null) {
			String[] stubs = splitCommaSymbol(stubsText);
			for (String stub : stubs) {
				// 解析数据块编号
				long id = ConfigParser.splitLong(stub, 0);
				// 0值是无效的数据块编号
				if (id == 0) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, stub);
				}
				cmd.addStub(id);
			}
		}

		// 解析MASTER地址
		Node master = splitSite(masterText, SiteTag.DATA_SITE);
		if (master == null) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, stubsText);
		}
		// 解析SLAVE地址
		java.util.List<Node> slaves = splitSites(slaveText, SiteTag.DATA_SITE);
		if (slaves == null || slaves.isEmpty()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, stubsText);
		}
		// 保存地址
		cmd.setMaster(master);
		cmd.addSlaves(slaves);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
	
//	public static void main(String[] args) {
//		String input = "COPY MASTER MASS MEDIA.MUSIC FROM data://localhost:6500_6500 TO data://localhost:7600_8700";
//		CopyMasterMassParser e = new CopyMasterMassParser();
//		boolean success = e.matches(false, input);
//		System.out.printf("result is %s\n", success);
//	}
}
