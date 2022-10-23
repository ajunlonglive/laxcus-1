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

import com.laxcus.command.site.watch.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.site.*;
import com.laxcus.ray.runtime.*;

/**
 * 检查集群成员分布调用器
 * 
 * @author scott.liang
 * @version 1.0 2/8/2020
 * @since laxcus 1.0
 */
public class RayCheckDistributedMemberInvoker extends RayInvoker {

	/**
	 * 构造检查集群成员分布调用器，指定命令
	 * @param cmd 检查集群成员分布
	 */
	public RayCheckDistributedMemberInvoker(CheckDistributedMember cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckDistributedMember getCommand() {
		return (CheckDistributedMember) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckDistributedMember cmd = getCommand();
		print(cmd);
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
	 * 显示结果
	 * @param cmd
	 */
	private void print(CheckDistributedMember cmd) {
		List<Siger> sigers = cmd.getUsers();

		// 显示全部！
		if (cmd.isAll()) {
			sigers = RayRegisterMemberBasket.getInstance().getUsers();
		}

		// 标题
		// 显示标题
		createShowTitle(new String[] { "CHECK-DISTRIBUTED-MEMBER/T1", "CHECK-DISTRIBUTED-MEMBER/T2" });

		// 用户账号
		String txtUser = getXMLContent("CHECK-DISTRIBUTED-MEMBER/USER");
		Color colorUser = findXMLForeground("CHECK-DISTRIBUTED-MEMBER/USER",Color.BLACK);

		// 注册地址
		String txtRegister = getXMLContent("CHECK-DISTRIBUTED-MEMBER/REGISTER");
		Color colorRegister = findXMLForeground("CHECK-DISTRIBUTED-MEMBER/REGISTER",Color.BLACK);

		// 在线地址
		String txtOnline = getXMLContent("CHECK-DISTRIBUTED-MEMBER/ONLINE");
		Color colorOnline = findXMLForeground("CHECK-DISTRIBUTED-MEMBER/ONLINE",Color.BLACK);

		// 注册在线地址
		String txtRegisterOnline = getXMLContent("CHECK-DISTRIBUTED-MEMBER/REGISTER-OLINE");
		Color colorRegisterOnline = findXMLForeground("CHECK-DISTRIBUTED-MEMBER/REGISTER-OLINE",Color.BLACK);

		// 在线地址
		String txtFront = getXMLContent("CHECK-DISTRIBUTED-MEMBER/FRONT");
		Color colorFront = findXMLForeground("CHECK-DISTRIBUTED-MEMBER/FRONT",Color.BLACK);

		// 显示成员
		int size = sigers.size();
		for (int index = 0; index < size; index++) {
			if (index > 0) {
				printGap(2);
			}

			Siger siger = sigers.get(index);
			// 找到明文
			String username = cmd.findPlainText(siger);
			// 如果是签名，查找注册池
			if (username != null && Siger.validate(username)) {
				boolean success = RayRegisterMemberBasket.getInstance().hasPlainText(siger);
				if (success) {
					username = RayRegisterMemberBasket.getInstance().findPlainText(siger);
				}
			}

			ShowItem item = new ShowItem();
			// 站点地址
			item.add(new ShowStringCell(0, txtUser, colorUser));
			item.add(new ShowStringCell(1, username));
			// 保存单元
			addShowItem(item);

			// 注册成员
			List<Node> registerSites = RayRegisterMemberBasket.getInstance().find(siger);
			for (Node node : registerSites) {
				String type = txtRegister;
				Color color = colorRegister;

				// 在线
				boolean success = node.isCall();
				if (success) {
					success = RayFrontMemberBasket.getInstance().contains(siger, node);
				}
				if (success) {
					type = txtRegisterOnline;
					color = colorRegisterOnline;
				}

				item = new ShowItem();
				// 站点地址
				item.add(new ShowStringCell(0, type, color));
				item.add(new ShowStringCell(1, node.toString()));
				// 保存单元
				addShowItem(item);
			}
			
			TreeSet<Node> gates = new TreeSet<Node>();
			TreeSet<Node> fronts = new TreeSet<Node>();

			// 在线成员
			List<FrontSeat> onlineSeats = RayFrontMemberBasket.getInstance().find(siger);
			for (FrontSeat seat : onlineSeats) {
				// 如果是CALL节点，忽略他，前面已经包括！
				if (seat.getGateway().isCall()) {
					continue;
				}
				// 保存!
				gates.add(seat.getGateway());
				fronts.add(seat.getFront());
			}
			// GATE节点
			for (Node node : gates) {
				item = new ShowItem();
				// 站点地址
				item.add(new ShowStringCell(0, txtOnline, colorOnline));
				item.add(new ShowStringCell(1, node));
				// 保存单元
				addShowItem(item);
			}
			// FRONT节点
			for (Node node : fronts) {
				item = new ShowItem();
				// 站点地址
				item.add(new ShowStringCell(0, txtFront, colorFront));
				item.add(new ShowStringCell(1, node));
				// 保存单元
				addShowItem(item);
			}
		}

		// 输出全部记录
		flushTable();
	}


}