/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.site.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 从指定节点清除用户调用器
 * 
 * @author scott.liang
 * @version 1.0 6/2/2019
 * @since laxcus 1.0
 */
public class RayEraseUserInvoker extends RayInvoker {

	/**
	 * 构造从指定节点清除用户调用器，指定命令
	 * @param cmd 从指定节点清除用户
	 */
	public RayEraseUserInvoker(EraseUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public EraseUser getCommand() {
		return (EraseUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果是BANK站点，拒绝发送
		if (isBankHub()) {
			faultX(FaultTip.BANK_RETRY);
			return useful(false);
		}

		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		EraseUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(EraseUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product);
		} else {
			printFault();
		}

		return useful(success);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(EraseUserProduct product) {
		// 打印消耗的时间
		printRuntime();

		// 显示标题
		createShowTitle(new String[] { "ERASE-USER/STATUS", "ERASE-USER/USERNAME", "ERASE-USER/SITE" });

		EraseUser cmd = getCommand();
		List<Siger> sigers = cmd.getUsers();
		List<Node> sites = cmd.getSites();

		for (int n = 0; n < sigers.size(); n++) {
			if (n > 0) printGap();
			// 用户签名
			Siger siger = sigers.get(n);
			// 找明文
			String plainText = cmd.findPlainText(siger);
			// 匹配的记录
			List<EraseUserItem> a = product.find(siger);
			
			// 逐一排查比较
			for (Node node : sites) {
				boolean success = false;
				// 比较每一个站点
				for (EraseUserItem e : a) {
					success = (Laxkit.compareTo(e.getSite(), node) == 0 && e.isSuccessful());
					if (success) {
						break;
					}
				}
				ShowItem item = new ShowItem();
				item.add(createConfirmTableCell(0, success));
				item.add(new ShowStringCell(1, plainText));
				item.add(new ShowStringCell(2, node));
				addShowItem(item);
			}
		}

		// 输出全部记录
		flushTable();

	}

	/**
	 * 打印空格
	 */
	private void printGap() {
		ShowItem showItem = new ShowItem();
		for (int i = 0; i < 3; i++) {
			showItem.add(new ShowStringCell(i, ""));
		}
		// 增加一行记录
		addShowItem(showItem);
	}

}