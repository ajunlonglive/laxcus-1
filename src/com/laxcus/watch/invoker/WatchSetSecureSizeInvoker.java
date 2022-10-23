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
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置对称密钥长度调用器
 * 
 * @author scott.liang
 * @version 1.0 2/27/2021
 * @since laxcus 1.0
 */
public class WatchSetSecureSizeInvoker extends WatchInvoker {

	/**
	 * 构造设置对称密钥长度调用器，指定命令
	 * @param cmd 设置对称密钥长度命令
	 */
	public WatchSetSecureSizeInvoker(SetSecureSize cmd) {
		super(cmd);
		// 指定快速处理
		cmd.setQuick(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetSecureSize getCommand() {
		return (SetSecureSize) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetSecureSize cmd = getCommand();
		// 本地执行
		if (cmd.isLocal()) {
			pickup(cmd);
			return useful();
		} else {
			// 发送到注册站点，再由注册站点分发命令
			return fireToHub();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		SetSecureSizeProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(SetSecureSizeProduct.class, index);
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
		String keyCount = getXMLContent("SET-SECURE-SIZE/COUNT");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, keyCount));
		item.add(new ShowIntegerCell(1, count));
		item.add(new ShowStringCell(2, ""));
		item.add(new ShowStringCell(3, ""));
		addShowItem(item);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(SetSecureSizeProduct product) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "SET-SECURE-SIZE/STATUS",
				"SET-SECURE-SIZE/SITE", "SET-SECURE-SIZE/CLIENT", "SET-SECURE-SIZE/SERVER" });

		// 集群节点
		printCount(product.size());
		printGap(4);

		ArrayList<Node> array = new ArrayList<Node>();
		SetSecureSize cmd = getCommand();
		// 输出全部...
		if (cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}

		// 输出...
		for (Node node : array) {
			SetSecureSizeItem slice = product.find(node);
			if (slice == null) {
				continue;
			}

			// 判断成功
			boolean success = slice.isSuccessful();

			// 写入记录
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, success)); // 图标
			item.add(new ShowStringCell(1, slice.getSite())); // 节点
			if (success) {
				item.add(new ShowIntegerCell(2, slice.getClientBits()));
				item.add(new ShowIntegerCell(3, slice.getServerBits()));
			} else {
				item.add(new ShowStringCell(2, ""));
				item.add(new ShowStringCell(3, ""));
			}
			addShowItem(item);
		}
		// 删除全部记录
		flushTable();
	}
	
	/**
	 * 本地执行
	 * @param cmd
	 */
	private void pickup(SetSecureSize cmd) {
		Cipher.setClientWidthWithBits(cmd.getClientBits());
		Cipher.setServerWidthWithBits(cmd.getServerBits());

		// 生成标题
		createShowTitle(new String[] { "SET-SECURE-SIZE/STATUS",
				"SET-SECURE-SIZE/CLIENT", "SET-SECURE-SIZE/SERVER" });

		// 写入记录
		ShowItem item = new ShowItem();
		item.add(createConfirmTableCell(0, true)); // 图标
		item.add(new ShowIntegerCell(1, Cipher.getClientWidthWithBits()));
		item.add(new ShowIntegerCell(2, Cipher.getServerWidthWithBits()));
		addShowItem(item);

		// 删除全部记录
		flushTable();
	}

}