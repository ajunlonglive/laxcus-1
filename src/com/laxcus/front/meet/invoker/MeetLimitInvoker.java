/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.law.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.display.show.*;

/**
 * 限制操作命令调用器。
 * 
 * @author scott.liang
 * @version 3/23/2017
 * @since laxcus 1.0
 */
abstract class MeetLimitInvoker extends MeetInvoker {

	/**
	 * 建立限制操作命令调用器，指定命令
	 * @param cmd - 限制操作命令
	 */
	protected MeetLimitInvoker(Command cmd) {
		super(cmd);
	}

	/**
	 * 在窗口上限制操作单元
	 * @param array
	 */
	protected void print(List<LimitItem> array) {
		// 生成标题
		createShowTitle(new String[] { "LIMIT-ITEM/RANK",
				"LIMIT-ITEM/OPERATOR", "LIMIT-ITEM/RESOURCE" });

		for (LimitItem e : array) {
			LimitFlag flag = e.getFlag();
			// 级别
			String rank = "";
			switch(flag.getRank()) {
			case LawRank.USER:
				rank = getXMLContent("LIMIT-ITEM/RANK/USER");
				break;
			case LawRank.SCHEMA:
				rank = getXMLContent("LIMIT-ITEM/RANK/SCHEMA");
				break;
			case LawRank.TABLE:
				rank = getXMLContent("LIMIT-ITEM/RANK/TABLE");
				break;
			}
			// 操作符
			String operator = "";
			switch (flag.getOperator()) {
			case LimitOperator.READ:
				operator = getXMLContent("LIMIT-ITEM/OPERATOR/READ");
				break;
			case LimitOperator.WRITE:
				operator = getXMLContent("LIMIT-ITEM/OPERATOR/WRITE");
				break;
			}

			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, rank));
			item.add(new ShowStringCell(1, operator));

			if (flag.isUserRank()) {
				item.add(new ShowStringCell(2, ""));
			} else if (flag.isSchemaRank()) {
				SchemaLimitItem that = (SchemaLimitItem) e;
				item.add(new ShowStringCell(2, that.getFame().toString()));
			} else if (flag.isTableRank()) {
				TableLimitItem that = (TableLimitItem) e;
				item.add(new ShowStringCell(2, that.getSpace().toString()));
			}
			// 增加一行记录
			addShowItem(item);
		}

		// 输出全部记录
		flushTable();
	}
}
