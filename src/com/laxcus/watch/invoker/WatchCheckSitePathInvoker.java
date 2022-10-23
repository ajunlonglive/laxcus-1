/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.awt.Color;
import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.disk.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 节点最小磁盘空间限制调用器。
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.0
 */
public class WatchCheckSitePathInvoker extends WatchInvoker {

	/**
	 * 构造节点最小磁盘空间限制调用器，指定命令
	 * @param cmd 打印站点检测目录
	 */
	public WatchCheckSitePathInvoker(CheckSitePath cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckSitePath getCommand() {
		return (CheckSitePath) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckSitePath cmd = getCommand();
		// 本地节点
		if (cmd.isLocal()) {
			pickup();
			return useful();
		} else {
			// 投递到HUB站点，分别处理
			return fireToHub();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CheckSitePathProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CheckSitePathProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(false, product);
		} else {
			printFault();
		}

		return useful(success);
	}

	/**
	 * 提取本地检测目录
	 */
	private void pickup() {
		CheckSitePathItem item = new CheckSitePathItem(getLocal());

		// 判断操作系统，保存参数
		if (isLinux()) {
			// OS
			String name = System.getProperty("os.name");
			String version = LinuxEffector.getInstance().getVersion();
			if (version != null) {
				name = String.format("%s/%s", name, version);
			}
			item.setOS(name);

			// 保存全部目录
			item.addAll(LinuxDevice.getInstance().getPathTabs());
		} else if (isWindows()) {
			// OS
			item.setOS(System.getProperty("os.name") + "/" + System.getProperty("os.version"));

			// 保存全部目录
			item.addAll(WindowsDevice.getInstance().getPathTabs());
		} 

		// 打印结果
		print(true, new CheckSitePathProduct(item));
	}

	/**
	 * 打印空格
	 */
	private void printGap() {
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, " "));
		item.add(new ShowStringCell(1, " "));
		// 增加一行记录
		addShowItem(item);
	}

	/**
	 * 显示统计值
	 * @param key
	 * @param count
	 */
	private void printCount(String key, int count) {
		// 节点地址
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowIntegerCell(1, count));
		addShowItem(item);
	}

	/**
	 * 打印本地结果
	 * @param product 结果
	 */
	private void print(boolean local, CheckSitePathProduct product) {
		ArrayList<Node> array = new ArrayList<Node>();
		
		CheckSitePath cmd = getCommand();
		if (cmd.isLocal() || cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}
		
		// 显示处理结果
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "CHECK-SITE-PATH/T1", "CHECK-SITE-PATH/T2" });

		String count = findXMLTitle("CHECK-SITE-PATH/ITEM/COUNT");

		String site = findXMLTitle("CHECK-SITE-PATH/ITEM/SITE");
		Color colorSite = findXMLForeground("CHECK-SITE-PATH/ITEM/SITE");
		String os = findXMLTitle("CHECK-SITE-PATH/ITEM/OS");
		Color colorOS = findXMLForeground("CHECK-SITE-PATH/ITEM/OS");
		String path = findXMLTitle("CHECK-SITE-PATH/ITEM/PATH");
		Color colorPath = findXMLForeground("CHECK-SITE-PATH/ITEM/PATH");

		// 统计值
		if (!local) {
			printCount(count, product.size());
		}

		// 逐一打印
		for (Node node : array) {
			CheckSitePathItem e = product.find(node);
			// 没有找到，忽略！
			if (e == null) {
				continue;
			}
			// 非本地的检索
			if (!local) {
				printGap();
			}

			boolean success = e.isSuccessful();
			// 节点地址
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, site, colorSite));
			item.add(new ShowStringCell(1, e.getSite(), (success ? null : Color.RED)));
			addShowItem(item);

			// 不成功，忽略它，继续下一个
			if (!success) {
				continue;
			}

			// 操作系统
			item = new ShowItem();
			item.add(new ShowStringCell(0, os, colorOS));
			item.add(new ShowStringCell(1, e.getOS()));
			addShowItem(item);

			// 目录
			for (PathTab dir : e.list()) {
				ShowItem sub = new ShowItem();
				sub.add(new ShowStringCell(0, path, colorPath));
				sub.add(new ShowStringCell(1, dir));
				addShowItem(sub);
			}
		}

		// 输出全部记录
		flushTable();
	}

}