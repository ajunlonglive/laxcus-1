/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.awt.*;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.util.disk.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 最少磁盘空间限制命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.0
 */
public class MeetCheckSitePathInvoker extends MeetInvoker {

	/**
	 * 构造最少磁盘空间限制命令调用器，指定命令
	 * @param cmd 打印站点检测目录
	 */
	public MeetCheckSitePathInvoker(CheckSitePath cmd) {
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
		// 本地磁盘空间限制
		if (cmd.isLocal()) {
			pickup();
		} else {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
		}
		// 投递到HUB站点
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
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
		print(new CheckSitePathProduct(item));
	}
	
	/**
	 * 打印空格
	 */
	private void printGap() {
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, ""));
		item.add(new ShowStringCell(1, ""));
		// 增加一行记录
		addShowItem(item);
	}

	/**
	 * 打印本地结果
	 * @param product 结果
	 */
	private void print(CheckSitePathProduct product) {
		// 显示处理结果
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "CHECK-SITE-PATH/T1", "CHECK-SITE-PATH/T2" });

		String site = findXMLTitle("CHECK-SITE-PATH/ITEM/SITE");
		String status = findXMLTitle("CHECK-SITE-PATH/ITEM/STATUS");
		
		Color colorSite = findXMLForeground("CHECK-SITE-PATH/ITEM/SITE",Color.BLACK);
		String os = findXMLTitle("CHECK-SITE-PATH/ITEM/OS");
		Color colorOS = findXMLForeground("CHECK-SITE-PATH/ITEM/OS",Color.BLACK);
		String path = findXMLTitle("CHECK-SITE-PATH/ITEM/PATH");
		Color colorPath = findXMLForeground("CHECK-SITE-PATH/ITEM/PATH",Color.BLACK);

		int index = 0;
		// 逐一打印
		for (CheckSitePathItem e : product.list()) {
			if (index > 0) {
				printGap();
			}
			index++;
			
			// 节点地址
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, site, colorSite));
			item.add(new ShowStringCell(1, e.getSite()));
			addShowItem(item);

			// 不成功，忽略
			if (!e.isSuccessful()) {
				item = new ShowItem();
				item.add(new ShowStringCell(0, status));
				item.add(createConfirmTableCell(1, false));
				addShowItem(item);
				return;
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