/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.command.forbid.*;
import com.laxcus.law.*;
import com.laxcus.law.forbid.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示禁止操作单元命令调用器
 * 
 * @author scott.liang
 * @version 1.0 3/28/2017
 * @since laxcus 1.0
 */
public class MeetShowForbidInvoker extends MeetInvoker {

	/**
	 * 构造默认的显示禁止操作单元命令调用器
	 * @param cmd 显示禁止操作命令
	 */
	public MeetShowForbidInvoker(ShowForbid cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowForbid getCommand() {
		return (ShowForbid) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ShowForbidProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShowForbidProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShowForbid cmd = getCommand();
		// 判断成功或者失败
		boolean success = (product != null);
		if (success) {
			print(product.list());
		} else {
			faultX(FaultTip.FAILED_X, cmd);
		}
		return useful(success);
	}

	/**
	 * 在窗口上显示禁止操作单元
	 * @param array
	 */
	protected void print(List<ForbidItem> array) {	
		// 生成标题
		createShowTitle(new String[] { "FORBID-ITEM/RANK", "FORBID-ITEM/RESOURCE" });

		for (ForbidItem e : array) {
			// 级别
			String rank = "";
			switch(e.getRank()) {
			case LawRank.USER:
				rank = getXMLContent("FORBID-ITEM/RANK/USER");
				break;
			case LawRank.SCHEMA:
				rank = getXMLContent("FORBID-ITEM/RANK/SCHEMA");
				break;
			case LawRank.TABLE:
				rank = getXMLContent("FORBID-ITEM/RANK/TABLE");
				break;
			case LawRank.ROW:
				rank = getXMLContent("FORBID-ITEM/RANK/ROW");
				break;
			}

			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, rank));

			if (e.isUserRank()) {
				item.add(new ShowStringCell(2, ""));
			} else if (e.isSchemaRank()) {
				SchemaForbidItem that = (SchemaForbidItem) e;
				item.add(new ShowStringCell(2, that.getFame().toString()));
			} else if (e.isTableRank()) {
				TableForbidItem that = (TableForbidItem) e;
				item.add(new ShowStringCell(2, that.getSpace().toString()));
			} else if(e.isRowRank()){
				RowForbidItem that = (RowForbidItem) e;
				item.add(new ShowStringCell(2, that.getFeature().toString()));
			}

			// 增加一行
			addShowItem(item);
		}

		// 输出全部记录
		flushTable();
	}


}