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

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 数据优化命令解析器。<br>
 * 指定表和重新定义主排序键，同时可以指向某个DATA主节点地址<br><br>
 * 
 * 语法:REGULATE schema.table [ORDER BY column] [TO address, address...]<br>
 * 
 * @author scott.liang
 * @version 1.1 12/28/2009
 * @since laxcus 1.0
 */
public final class RegulateParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:REGULATE)\\s+([\\w\\W]+)\\s*$";

	/** 优化表的四种语句定义 */
	private final static String REGULATE1 = "^\\s*(?i)(?:REGULATE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:ORDER\\s+BY)\\s+(\\w+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)$";
	private final static String REGULATE2 = "^\\s*(?i)(?:REGULATE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:ORDER\\s+BY)\\s+(\\w+)\\s*$";
	private final static String REGULATE3 = "^\\s*(?i)(?:REGULATE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)$";
	private final static String REGULATE4 = "^\\s*(?i)(?:REGULATE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";

	/**
	 * 构造数据优化命令解析器
	 */
	public RegulateParser() {
		super();
	}

	/**
	 * 检查数据表的优化语句是否匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("REGULATE", input);
		}
		Pattern pattern = Pattern.compile(RegulateParser.REGULATE1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(RegulateParser.REGULATE2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			pattern = Pattern.compile(RegulateParser.REGULATE3);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			pattern = Pattern.compile(RegulateParser.REGULATE4);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析优化语句
	 * @param input 数据优化语句
	 * @return 返回"REGULATE"命令实例
	 */
	public Regulate split(String input) {
		Space space = null;
		String columnName = null;
		String hosts = null;

		Pattern pattern = Pattern.compile(RegulateParser.REGULATE1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			space = new Space(matcher.group(1), matcher.group(2));
			columnName = matcher.group(3);
			hosts = matcher.group(4);
		}
		if(!match) {
			pattern = Pattern.compile(RegulateParser.REGULATE2);
			matcher = pattern.matcher(input);
			if(match = matcher.matches()) {
				space = new Space(matcher.group(1), matcher.group(2));
				columnName = matcher.group(3);
			}
		}
		if(!match) {
			pattern = Pattern.compile(RegulateParser.REGULATE3);
			matcher = pattern.matcher(input);
			if(match = matcher.matches()) {
				space = new Space(matcher.group(1), matcher.group(2));
				hosts = matcher.group(3);
			}
		}
		if(!match) {
			pattern = Pattern.compile(RegulateParser.REGULATE4);
			matcher = pattern.matcher(input);
			if(match = matcher.matches()) {
				space = new Space(matcher.group(1), matcher.group(2));
			}
		}
		if (!match) {
			// throw new SyntaxException("syntax error or missing!");
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		// 查找对应的数据库表配置
		ResourceChooser chooser = SyntaxParser.getResourceChooser();
		Table table = chooser.findTable(space);
		if (table == null) {
			throwableNo(FaultTip.NOTFOUND_X, space);
		}

		Regulate cmd = new Regulate(space);
		cmd.setPrimitive(input); //原语

		// 解析索引
		if (columnName != null) {
			ColumnAttribute attribute = table.find(columnName);
			if (attribute == null) {
				throwableNo(FaultTip.NOTFOUND_X, columnName);
			}
			// 如果是行存储模型，必须是索引键
			if (table.isNSM() && !attribute.isKey()) {
				//	throw new SyntaxException("invalid key:%s", columnName);
				throwableNo(FaultTip.SQL_ILLEGAL_KEY_X, columnName);
			}
			cmd.setColumnId(attribute.getColumnId());
		}
		// 解析指定的DATA节点地址
		if (hosts != null) {
			List<Node> sites = splitSites(hosts, SiteTag.DATA_SITE);
			cmd.addSites(sites);
		}

		return cmd;
	}

}