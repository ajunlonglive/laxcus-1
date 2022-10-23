/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.awt.Color;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 检索在线资源调用器
 * 
 * @author scott.liang
 * @version 1.0 4/22/2018
 * @since laxcus 1.0
 */
public class RaySeekOnlineResourceInvoker extends RayInvoker {

	/**
	 * 构造检索在线资源调用器，指定命令
	 * @param cmd 检索在线资源
	 */
	public RaySeekOnlineResourceInvoker(SeekOnlineResource cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekOnlineResource getCommand() {
		return (SeekOnlineResource) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekOnlineResource cmd = getCommand();
		Node site = cmd.getSite();

		// 不允许发送的地址
		if (site.isTop() || site.isFront() || site.isFront() || site.isLog()) {
			faultX(FaultTip.ILLEGAL_SITE_X, site);
			return false;
		}

		return fireToHub(site, cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekOnlineResourceProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SeekOnlineResourceProduct.class, index);
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
	private void print(SeekOnlineResourceProduct product) {
		// 打印消耗的时间
		printRuntime();

		createShowTitle(new String[] { "SEEK-ONLINE-RESOURCE/T1",
				"SEEK-ONLINE-RESOURCE/T2" });

		String txtSiger = getXMLContent("SEEK-ONLINE-RESOURCE/SIGER");
		Color colorSiger = findXMLForeground("SEEK-ONLINE-RESOURCE/SIGER",Color.BLACK);
		String txtTable = getXMLContent("SEEK-ONLINE-RESOURCE/TABLE");
		Color colorTable = findXMLForeground("SEEK-ONLINE-RESOURCE/TABLE",Color.BLACK);
		String txtPhase = getXMLContent("SEEK-ONLINE-RESOURCE/PHASE");
		Color colorPhase = findXMLForeground("SEEK-ONLINE-RESOURCE/PHASE",Color.BLACK);
		String systemTask = getXMLContent("SEEK-ONLINE-RESOURCE/SYSTEM-TASK");
		Color colorTask = findXMLForeground("SEEK-ONLINE-RESOURCE/SYSTEM-TASK", Color.BLACK);

		List<SeekOnlineResourceItem> items = product.getItems();
		
		for (int i = 0; i < items.size(); i++) {
			if (i > 0) printGap();
			
			SeekOnlineResourceItem item = items.get(i);
			Siger siger = item.getSiger();
			
			ShowItem showItem = new ShowItem();
			showItem.add(new ShowStringCell(0, txtSiger, colorSiger));
			showItem.add(new ShowStringCell(1, siger));
			addShowItem(showItem);
			
			// 输出数据表
			Refer refer = item.getRefer();
			if (refer != null) {
				List<Space> spaces = refer.getTables();
				for (Space space : spaces) {
					showItem = new ShowItem();
					showItem.add(new ShowStringCell(0, txtTable, colorTable));
					showItem.add(new ShowStringCell(1, space));
					addShowItem(showItem);
				}
			}

			// 输出阶段命名
			List<Phase> phases = item.getPhases();
			for (Phase phase : phases) {
				showItem = new ShowItem();
				showItem.add(new ShowStringCell(0, txtPhase, colorPhase));
				showItem.add(new ShowStringCell(1, phase));
				addShowItem(showItem);
			}
		}
		
		// 系统层阶段命名
		List<Phase> phases = product.getSystemPhases();
		if (phases.size() > 0) {
			// 显示空格
			if (items.size() > 0) printGap();
			
			ShowItem showItem = new ShowItem();
			showItem.add(new ShowStringCell(0, txtSiger, colorTask));
			showItem.add(new ShowStringCell(1, systemTask));
			addShowItem(showItem);
			
			for (Phase phase : phases) {
				showItem = new ShowItem();
				showItem.add(new ShowStringCell(0, txtPhase, colorPhase));
				showItem.add(new ShowStringCell(1, phase));
				addShowItem(showItem);
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
		showItem.add(new ShowStringCell(0, ""));
		showItem.add(new ShowStringCell(1, ""));
		// 增加一行记录
		addShowItem(showItem);
	}
	
}