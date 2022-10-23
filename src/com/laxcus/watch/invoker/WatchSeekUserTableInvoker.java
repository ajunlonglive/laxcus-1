/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检索用户数据表分布调用器
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class WatchSeekUserTableInvoker extends WatchInvoker {

	/**
	 * 构造检索用户数据表分布，指定命令
	 * @param cmd 检索用户数据表分布命令
	 */
	public WatchSeekUserTableInvoker(SeekUserTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserTable getCommand() {
		return (SeekUserTable) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送到HOME/TOP站点的任意一个
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
		SeekUserTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SeekUserTableProduct.class, index);
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
	 * 基于用户账号，在屏幕上打印结果
	 * 
	 * @param array
	 */
	private void print(List<SeekUserTableItem> array) {
		// 打印消耗的时间
		printRuntime();
		
		createShowTitle(new String[] { "SEEK-USERTABLE/USERNAME",
				"SEEK-USERTABLE/SITE", "SEEK-USERTABLE/SPACE" });
		
		SeekUserTable cmd = getCommand();
		
		ArrayList<Siger> sigers = new ArrayList<Siger>();
		// 全部用户账号，或者部分
		if (cmd.isAllUser()) {
			for (SeekUserTableItem e : array) {
				if (!sigers.contains(e.getUsername())) {
					sigers.add(e.getUsername());
				}
			}
		} else {
			sigers.addAll(cmd.getUsers());
		}
		
		// 逐一查找
		for (int index = 0; index < sigers.size(); index++) {
			if (index > 0) printGap(3);
			Siger siger = sigers.get(index);

			int count = 0;
			for (SeekUserTableItem e : array) {
				// 忽略
				if (Laxkit.compareTo(e.getUsername(), siger) != 0) {
					continue;
				}

				// 找到匹配的文件显示
				String username = cmd.findPlainText(siger);
				List<Space> tables = e.getTables();
				if (tables.isEmpty()) {
					ShowItem item = new ShowItem();
					item.add(new ShowStringCell(0, username));
					item.add(new ShowStringCell(1, e.getSite()));
					item.add(new ShowStringCell(2, ""));
					addShowItem(item);
				} else {
					for (Space space : tables) {
						ShowItem item = new ShowItem();
						item.add(new ShowStringCell(0, username));
						item.add(new ShowStringCell(1, e.getSite()));
						item.add(new ShowStringCell(2, space));
						addShowItem(item);
					}
				}
				count++;
			}
			// 没有，显示一个空行
			if (count == 0) {
				String username = cmd.findPlainText(siger);
				ShowItem item = new ShowItem();
				item.add(new ShowStringCell(0, username));
				item.add(new ShowStringCell(1, ""));
				item.add(new ShowStringCell(2, ""));
				addShowItem(item);
			}
		}
		
		// 输出全部记录
		flushTable();
	}
	
//	/**
//	 * 在屏幕上打印结果
//	 * 
//	 * @param array
//	 */
//	private void print(List<SeekUserTableItem> array) {
//		createShowTitle(new String[] { "SEEK-USERTABLE/USERNAME",
//				"SEEK-USERTABLE/SITE", "SEEK-USERTABLE/SPACE" });
//
//		SeekUserTable cmd = getCommand();
//		for (SeekUserTableItem e : array) {
//			String username = cmd.findText(e.getUsername());
//			List<Space> tables = e.getTables();
//			if (tables.isEmpty()) {
//				ShowItem item = new ShowItem();
//				item.add(new ShowStringCell(0, username));
//				item.add(new ShowStringCell(1, e.getSite()));
//				item.add(new ShowStringCell(2, ""));
//				addShowItem(item);
//			} else {
//				for (Space space : tables) {
//					ShowItem item = new ShowItem();
//					item.add(new ShowStringCell(0, username));
//					item.add(new ShowStringCell(1, e.getSite()));
//					item.add(new ShowStringCell(2, space));
//					addShowItem(item);
//				}
//			}
//		}
//		// 输出全部记录
//		flushTable();
//	}

}