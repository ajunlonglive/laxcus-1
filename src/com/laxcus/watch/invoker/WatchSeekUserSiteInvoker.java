/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检索用户站点分布调用器
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class WatchSeekUserSiteInvoker extends WatchInvoker {

	/**
	 * 构造检索用户站点分布，指定命令
	 * @param cmd 检索用户站点分布命令
	 */
	public WatchSeekUserSiteInvoker(SeekUserSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserSite getCommand() {
		return (SeekUserSite) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 只能在登录HOME或者TOP集群后才能操作
		boolean success = (isTopHub() || isHomeHub());
		if (!success) {
			faultX(FaultTip.TOP_HOME_RETRY);
			return false;
		}
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekUserSiteProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SeekUserSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null);
		if (success) {
			print(product.list());
		}

		return useful(success);
	}

	/**
	 * 打印空格
	 */
	private void printGap() {
		ShowItem showItem = new ShowItem();
		for (int i = 0; i < 2; i++) {
			showItem.add(new ShowStringCell(i, ""));
		}
		// 增加一行记录
		addShowItem(showItem);
	}
	
	/**
	 * 在屏幕上打印结果
	 * @param array
	 */
	private void print(List<SeekUserSiteItem> array) {
		// 打印消耗的时间
		printRuntime();
		
		createShowTitle(new String[] { "SEEK-USERSITE/USERNAME", "SEEK-USERSITE/SITE" });

		SeekUserSite cmd = getCommand();
		
		// 用户签名
		ArrayList<Siger> sigers = new ArrayList<Siger>();
		
		// 如果指定全部用户账号时，把用户签名取出
		if (cmd.isAllUser()) {
			// 遍历，不存在，保存它！
			for (SeekUserSiteItem e : array) {
				if (!sigers.contains(e.getUsername())) {
					sigers.add(e.getUsername());
				}
			}
		} else {
			// 逐一处理
			sigers.addAll(cmd.getUsers());
		}
		
		for (int index = 0; index < sigers.size(); index++) {
			if (index > 0) printGap();

			// 签名
			Siger siger = sigers.get(index);
			// 文本名称
			String username = cmd.findPlainText(siger);

			// 逐一检查，排序显示
			int count = 0;
			for (SeekUserSiteItem e : array) {
				// 不匹配，忽略它
				if (Laxkit.compareTo(siger, e.getUsername()) != 0) {
					continue;
				}
				// 显示单元
				ShowItem item = new ShowItem();
				item.add(new ShowStringCell(0, username));
				item.add(new ShowStringCell(1, e.getSite()));
				addShowItem(item);
				count++;
			}
			// 没有找到，显示一个空行
			if (count == 0) {
				ShowItem item = new ShowItem();
				item.add(new ShowStringCell(0, username));
				item.add(new ShowStringCell(1, ""));
				addShowItem(item);
			}
		}

		// 输出全部记录
		flushTable();
	}
	
//	/**
//	 * 在屏幕上打印结果
//	 * @param array
//	 */
//	private void print2(List<SeekUserSiteItem> array) {
//		createShowTitle(new String[] { "SEEK-USERSITE/USERNAME", "SEEK-USERSITE/SITE" });
//
//		SeekUserSite cmd = getCommand();
//		for (SeekUserSiteItem e : array) {
//			ShowItem item = new ShowItem();
//			String username = cmd.findText(e.getUsername());
//			item.add(new ShowStringCell(0, username));
//			item.add(new ShowStringCell(1, e.getSite()));
//			addShowItem(item);
//		}
//		// 输出全部记录
//		flushTable();
//	}

}