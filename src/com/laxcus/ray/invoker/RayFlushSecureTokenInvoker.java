/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.secure.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 输出密钥令牌调用器
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class RayFlushSecureTokenInvoker extends RayInvoker {

	/**
	 * 构造输出密钥令牌调用器，指定命令
	 * @param cmd 输出密钥令牌命令
	 */
	public RayFlushSecureTokenInvoker(FlushSecureToken cmd) {
		super(cmd);
		// 指定快速处理
		cmd.setQuick(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FlushSecureToken getCommand() {
		return (FlushSecureToken) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送到注册站点，再由注册站点分发命令
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		FlushSecureTokenProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(FlushSecureTokenProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		return useful(success);
	}

	/**
	 * 显示节点数目
	 * @param count
	 */
	private void printCount(int count) {
		String key = getXMLContent("FLUSH-SECURE-TOKEN/COUNT");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowIntegerCell(1, count));
		addShowItem(item);
	}
	
	/**
	 * 打印结果
	 * @param product
	 */
	private void print(FlushSecureTokenProduct product) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "FLUSH-SECURE-TOKEN/STATUS", "FLUSH-SECURE-TOKEN/SITE" });

		// 集群节点
		printCount(product.size());
		if (product.size() > 0) {
			printGap(2);
		}
		
		ArrayList<Node> array = new ArrayList<Node>();
		FlushSecureToken cmd = getCommand();
		// 输出全部...
		if (cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}

		for (Node node : array) {
			FlushSecureTokenItem e = product.find(node);
			if (e == null) {
				continue;
			}
			// 输出结果
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowStringCell(1, e.getSite()));
			// 保存单元
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}