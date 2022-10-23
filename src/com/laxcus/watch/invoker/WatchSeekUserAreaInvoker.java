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

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;
import com.laxcus.watch.pool.*;

/**
 * 检索用户分布区域调用器
 * 
 * @author scott.liang
 * @version 1.0 5/29/2019
 * @since laxcus 1.0
 */
public class WatchSeekUserAreaInvoker extends WatchInvoker {

	/**
	 * 构造检索用户分布区域调用器，指定命令
	 * @param cmd 检索用户分布区域
	 */
	public WatchSeekUserAreaInvoker(SeekUserArea cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserArea getCommand() {
		return (SeekUserArea) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekUserAreaProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SeekUserAreaProduct.class, index);
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
	private void print(SeekUserAreaProduct product) {
		// 打印消耗的时间
		printRuntime();

		// 显示标题
		createShowTitle(new String[] { "SEEK-USER-AREA/T1", "SEEK-USER-AREA/T2" });

		String txtSiger = getXMLContent("SEEK-USER-AREA/SIGER");
		Color colorSiger = findXMLForeground("SEEK-USER-AREA/SIGER", Color.BLACK);

		String txtSite = getXMLContent("SEEK-USER-AREA/SITE");
		Color colorSite = findXMLForeground("SEEK-USER-AREA/SITE", Color.BLACK);
		String txtNotFound = getXMLAttribute("SEEK-USER-AREA/SITE/notfound");
		
		String register = getXMLAttribute("SEEK-USER-AREA/SITE/register");
		String online = getXMLAttribute("SEEK-USER-AREA/SITE/online");
		String register_online = getXMLAttribute("SEEK-USER-AREA/SITE/register-online");

		SeekUserArea cmd = getCommand();
		List<Siger> sigers = cmd.getUsers();

		for (int index = 0; index < sigers.size(); index++) {
			if (index > 0) {
				printGap();
			}

			// 用户签名
			Siger siger = sigers.get(index);
			// 找明文
			String plainText = cmd.findPlainText(siger);

			// 没有找到匹配的结果
			List<Seat> seats = product.find(siger);
			if (seats.isEmpty()) {
				ShowItem showItem = new ShowItem();
				showItem.add(new ShowStringCell(0, txtSiger, colorSiger));
				showItem.add(new ShowStringCell(1, plainText, Color.RED));
				addShowItem(showItem);

				showItem = new ShowItem();
				showItem.add(new ShowStringCell(0, txtSite, colorSite));
				showItem.add(new ShowStringCell(1, txtNotFound, Color.RED));
				addShowItem(showItem);
				continue;
			}

			ShowItem showItem = new ShowItem();
			showItem.add(new ShowStringCell(0, txtSiger, colorSiger));
			showItem.add(new ShowStringCell(1, plainText));
			addShowItem(showItem);
			// 显示结果
			for (Seat seat : seats) {
				Node node = seat.getSite();
				String title = txtSite;

				// 判断类型。顺序：在线注册 -> 在线 -> 注册
				if (isRegisterOnline(node)) {
					title = register_online; // 注册在线
				} else if (isOnline(node)) {
					title = online; // 在线
				} else if (isRegister(node)) {
					title = register; // 注册
				}
				
				showItem = new ShowItem();
				showItem.add(new ShowStringCell(0, title, colorSite));
				showItem.add(new ShowStringCell(1, node, colorSite));
				addShowItem(showItem);
			}
		}

		// 输出全部记录
		flushTable();
	}

	/**
	 * 判断是注册在线
	 * @param node 节点
	 * @return 返回真或者假
	 */
	private boolean isRegisterOnline(Node node) {
		return node.isCall() && FrontMemberBasket.getInstance().contains(node);
	}

	/**
	 * 判断是在线
	 * @param node 节点
	 * @return 返回真或者假
	 */
	private boolean isOnline(Node node) {
		return node.isGate()
				|| (node.isCall() && FrontMemberBasket.getInstance().contains(node));
	}
	
	/**
	 * 判断是注册
	 * @param node 节点
	 * @return 返回真或者假
	 */
	private boolean isRegister(Node node) {
		return node.isAccount() || node.isHash() || node.isHome()
				|| node.isData() || node.isWork() || node.isBuild()
				|| node.isCall();
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

}