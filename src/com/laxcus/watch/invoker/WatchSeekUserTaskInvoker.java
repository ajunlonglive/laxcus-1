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
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检索用户阶段命名分布调用器。
 * 
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class WatchSeekUserTaskInvoker extends WatchInvoker {

	/**
	 * 构造检索用户阶段命名分布，指定命令
	 * @param cmd 检索用户阶段命名分布命令
	 */
	public WatchSeekUserTaskInvoker(SeekUserTask cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserTask getCommand() {
		return (SeekUserTask) super.getCommand();
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
		SeekUserTaskProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SeekUserTaskProduct.class, index);
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
	 * 在屏幕上打印结果
	 * @param array
	 */
	private void print(List<SeekUserTaskItem> array) {
		// 打印消耗的时间
		printRuntime();
		
		createShowTitle(new String[] { "SEEK-USERPHASE/USERNAME",
				"SEEK-USERPHASE/SITE", "SEEK-USERPHASE/PHASE" });

		SeekUserTask cmd = getCommand();
		
		// 取出全部签名
		ArrayList<Siger> sigers = new ArrayList<Siger>();
		if (cmd.isAllUser()) {
			for (SeekUserTaskItem e : array) {
				if (!sigers.contains(e.getUsername())) {
					sigers.add(e.getUsername());
				}
			}
		} else {
			sigers.addAll(cmd.getUsers());
		}
		
		// 按照签名排列，显示！
		for (int index = 0; index < sigers.size(); index++) {
			if (index > 0) printGap(3);
			Siger siger = sigers.get(index);
			
			int count = 0;
			for (SeekUserTaskItem e : array) {
				// 忽略
				if (Laxkit.compareTo(e.getUsername(), siger) != 0) {
					continue;
				}

				String username = cmd.findPlainText(siger);
				List<Phase> phases = e.getPhases();
				// 阶段命名
				if (phases.isEmpty()) {
					ShowItem item = new ShowItem();
					
					item.add(new ShowStringCell(0, username));
					item.add(new ShowStringCell(1, e.getSite()));
					item.add(new ShowStringCell(2, ""));
					addShowItem(item);
				} else {
					for (Phase phase : phases) {
						ShowItem item = new ShowItem();
						item.add(new ShowStringCell(0, username));
						item.add(new ShowStringCell(1, e.getSite()));
						item.add(new ShowStringCell(2, phase));
						addShowItem(item);
					}
				}
				count++;
			}
			// 没有找到，显示一个空行
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
//	 * @param array
//	 */
//	private void print(List<SeekUserPhaseItem> array) {
//		createShowTitle(new String[] { "SEEK-USERPHASE/USERNAME",
//				"SEEK-USERPHASE/SITE", "SEEK-USERPHASE/PHASE" });
//
//		SeekUserPhase cmd = getCommand();
//		for (SeekUserPhaseItem e : array) {
//			String username = cmd.findText(e.getUsername());
//			List<Phase> phases = e.getPhases();
//			// 阶段命名
//			if (phases.isEmpty()) {
//				ShowItem item = new ShowItem();
//				item.add(new ShowStringCell(0, username));
//				item.add(new ShowStringCell(1, e.getSite()));
//				item.add(new ShowStringCell(2, ""));
//				addShowItem(item);
//			} else {
//				for (Phase phase : phases) {
//					ShowItem item = new ShowItem();
//					item.add(new ShowStringCell(0, username));
//					item.add(new ShowStringCell(1, e.getSite()));
//					item.add(new ShowStringCell(2, phase));
//					addShowItem(item);
//				}
//			}
//		}
//		
//		// 输出全部记录
//		flushTable();
//	}

}