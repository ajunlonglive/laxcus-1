/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.command.rule.*;
import com.laxcus.law.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示事务规则命令调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopShowLockRuleInvoker extends DesktopInvoker {

	/**
	 * 构造默认的显示事务规则命令调用器
	 * @param cmd 显示事务规则命令
	 */
	public DesktopShowLockRuleInvoker(ShowLockRule cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowLockRule getCommand() {
		return (ShowLockRule) super.getCommand();
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
		ShowLockRuleProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShowLockRuleProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		ShowLockRule cmd = getCommand();
		// 判断成功或者失败
		boolean success = (product != null);
		if (success) {
			print(product);
		} else {
			faultX(FaultTip.FAILED_X, cmd);
		}
		return useful(success);
	}
	
	/**
	 * 显示事务规则
	 * @param product
	 */
	private void print(ShowLockRuleProduct product) {
		// 生成表格标题
		createShowTitle(new String[] { "RULE-ITEM/RANK",
				"RULE-ITEM/OPERATOR", "RULE-ITEM/RESOURCE", "RULE-ITEM/STATUS" });

		// 运行中的事务规则
		String status = getXMLContent("RULE-ITEM/STATUS/RUNNING");
		print(product.getRunRules(), status);
		// 等待中的事务规则
		status = getXMLContent("RULE-ITEM/STATUS/WAITING");
		print(product.getWaitRules(), status);
		
		// 输出全部记录
		flushTable();
	}
	
	/**
	 * 在窗口上显示事务规则
	 * @param array - 事务数组
	 * @param status - 状态
	 */
	protected void print(List<RuleItem> array, String status) {
		for (RuleItem rule : array) {
			// 级别
			String rank = "";
			switch (rule.getRank()) {
			case LawRank.USER:
				rank = getXMLContent("RULE-ITEM/RANK/USER");
				break;
			case LawRank.SCHEMA:
				rank = getXMLContent("RULE-ITEM/RANK/SCHEMA");
				break;
			case LawRank.TABLE:
				rank = getXMLContent("RULE-ITEM/RANK/TABLE");
				break;
			case LawRank.ROW:
				rank = getXMLContent("RULE-ITEM/RANK/ROW");
				break;
			}
			
			// 操作符
			String operator = "";
			switch (rule.getOperator()) {
			case RuleOperator.SHARE_READ:
				operator = getXMLContent("RULE-ITEM/OPERATOR/SHARE-READ");
				break;
			case RuleOperator.SHARE_WRITE:
				operator = getXMLContent("RULE-ITEM/OPERATOR/SHARE-WRITE");
				break;
			case RuleOperator.EXCLUSIVE_WRITE:
				operator = getXMLContent("RULE-ITEM/OPERATOR/EXCLUSIVE-WRITE");
				break;
			}

			ShowItem item = new ShowItem();
			// 级别和操作符
			item.add(new ShowStringCell(0, rank));
			item.add(new ShowStringCell(1, operator));

			// 显示关联参数
			if (rule.isUserRank()) {
				item.add(new ShowStringCell(2, ""));
			} else if (rule.isSchemaRank()) {
				SchemaRuleItem that = (SchemaRuleItem) rule;
				item.add(new ShowStringCell(2, that.getFame().toString()));
			} else if (rule.isTableRank()) {
				TableRuleItem that = (TableRuleItem) rule;
				item.add(new ShowStringCell(2, that.getSpace().toString()));
			}
			
			// 状态
			item.add(new ShowStringCell(3, status));

			// 在窗口显示
			addShowItem(item);
		}
	}

}
