/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.command.secure.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 删除密钥令牌调用器
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class WatchDropSecureTokenInvoker extends WatchInvoker {

	/**
	 * 构造删除密钥令牌调用器，指定命令
	 * @param cmd 删除密钥令牌命令
	 */
	public WatchDropSecureTokenInvoker(DropSecureToken cmd) {
		super(cmd);
		// 指定快速处理
		cmd.setQuick(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropSecureToken getCommand() {
		return (DropSecureToken) super.getCommand();
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
		DropSecureTokenProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(DropSecureTokenProduct.class, index);
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
		String keyCount = getXMLContent("DROP-SECURE-TOKEN/COUNT");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, keyCount));
		item.add(new ShowIntegerCell(1, count));
		item.add(new ShowStringCell(2, ""));
		addShowItem(item);
	}

	/**
	 * 显示节点
	 * @param site
	 */
	private void printSite(Node site) {
		String keySite = getXMLContent("DROP-SECURE-TOKEN/SITE");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, keySite));
		item.add(new ShowStringCell(1, site.toString()));
		item.add(new ShowStringCell(2, ""));
		addShowItem(item);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(DropSecureTokenProduct product){
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "DROP-SECURE-TOKEN/T1",
				"DROP-SECURE-TOKEN/T2", "DROP-SECURE-TOKEN/T3" });

		ArrayList<Node> array = new ArrayList<Node>();
		DropSecureToken cmd = getCommand();
		// 输出全部...
		if (cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}

		// 集群节点
		printCount(product.size());

		// 显示结果
		String name = getXMLContent("DROP-SECURE-TOKEN/NAME");
		for (Node node : array) {
			DropSecureTokenItem sub = product.find(node);
			if (sub == null) {
				continue;
			}

			printGap(3);
			printSite(sub.getSite());
			// 命名成功或者否
			for (DropSecureTokenSlice slice : sub.list()) {
				ShowItem item = new ShowItem();
				item.add(new ShowStringCell(0, name));
				item.add(new ShowStringCell(1, slice.getNaming())); // 命名
				item.add(createConfirmTableCell(2, slice.isSuccessful())); // 图标
				// 保存单元
				addShowItem(item);
			}
		}
		// 删除全部记录
		flushTable();
	}

}