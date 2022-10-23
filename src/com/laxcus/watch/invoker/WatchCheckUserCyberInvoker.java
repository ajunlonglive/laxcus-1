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

import com.laxcus.command.cyber.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 设置成员虚拟空间调用器
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class WatchCheckUserCyberInvoker extends WatchInvoker {

	/**
	 * 构造设置成员虚拟空间，指定命令
	 * @param cmd 设置成员虚拟空间
	 */
	public WatchCheckUserCyberInvoker(CheckUserCyber cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckUserCyber getCommand() {
		return (CheckUserCyber) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 投递到TOP/HOME/BANK节点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		UserCyberProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(UserCyberProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}

	/**
	 * 解析成字节串
	 * @param size
	 * @return
	 */
	private String capacity(long size) {
		return ConfigParser.splitCapacity(size);
	}

	/**
	 * 解析成字符串
	 * @param max
	 * @param used
	 * @return
	 */
	private String capacity(long used, long max) {
		double rate = ((double) used / (double) max) * 100.0f;

		//		System.out.printf("%d / %d = %.2f\n", used, max, rate);

		return String.format("%s / %s = %.2f", capacity(used), capacity(max), rate) + "%";
	}

	private String capacity(DeviceStamp deviceStamp) {
		return this.capacity(deviceStamp.getRealCapacity(), deviceStamp.getMaxCapacity());
	}
	
	private void printCount(int count) {
		String key = getXMLContent("CHECK-USER-CYBER/COUNT");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowIntegerCell(1, count));
		item.add(new ShowStringCell(2, ""));
		addShowItem(item);
	}

	/**
	 * 显示反馈结果
	 * @param array
	 */
	private void print(List<UserCyberItem> array) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "CHECK-USER-CYBER/T1", "CHECK-USER-CYBER/T2","CHECK-USER-CYBER/T3" });

		printCount(array.size());
		
		// 节点
		String txtSite = getXMLContent("CHECK-USER-CYBER/SITE");
		Color colorSite = findXMLForeground("CHECK-USER-CYBER/SITE");

		// 系统内存
		String txtSysMemory = getXMLContent("CHECK-USER-CYBER/SYS-MEMORY");
		Color colorSysMemory = findXMLForeground("CHECK-USER-CYBER/SYS-MEMORY");

		// 虚拟机内存
		String txtVmMemory = getXMLContent("CHECK-USER-CYBER/VM-MEMORY");
		Color colorVmMemory = findXMLForeground("CHECK-USER-CYBER/VM-MEMORY");

		// 系统磁盘
		String txtSysDisk = getXMLContent("CHECK-USER-CYBER/SYS-DISK");
		Color colorSysDisk = findXMLForeground("CHECK-USER-CYBER/SYS-DISK");

		// 成员规模
		String txtMemberCyber = getXMLContent("CHECK-USER-CYBER/MEMBER-CYBER");
		Color colorMemberCyber = findXMLForeground("CHECK-USER-CYBER/MEMBER-CYBER");

		// 在线用户
		String txtFrontCyber = getXMLContent("CHECK-USER-CYBER/FRONT-CYBER");
		Color colorFrontCyber = findXMLForeground("CHECK-USER-CYBER/FRONT-CYBER");

		// 满员
		String txtFull = getXMLContent("CHECK-USER-CYBER/FULL");
		Color colorFull = findXMLForeground("CHECK-USER-CYBER/FULL");

		// 空间不足
		String txtMissing = getXMLContent("CHECK-USER-CYBER/MISSING");
		Color colorMissing = findXMLForeground("CHECK-USER-CYBER/MISSING");

		// 空间足够
		String txtEnouth = getXMLContent("CHECK-USER-CYBER/ENOUTH");
		Color colorEnouth = findXMLForeground("CHECK-USER-CYBER/ENOUTH");

		// 处理单元
		for (int index = 0; index < array.size(); index++) {			
			 printGap(3);

			UserCyberItem e  = array.get(index);
			// 节点
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, txtSite, colorSite));
			item.add(new ShowStringCell(1, e.getSite()));
			item.add(new ShowStringCell(2, ""));
			addShowItem(item);

			Moment moment = e.getMoment();

			// JVM内存	
			DeviceStamp deviceStamp = moment.getVMMemory();
			String memory = capacity(deviceStamp);
			item = new ShowItem();
			item.add(new ShowStringCell(0, txtVmMemory, colorVmMemory));
			item.add(new ShowStringCell(1, memory));
			// 判断空间
			if (deviceStamp.isMissing()) {
				item.add(new ShowStringCell(2, txtMissing, colorMissing));
			} else {
				item.add(new ShowStringCell(2, txtEnouth, colorEnouth));
			}
			addShowItem(item);

			// 系统内存
			deviceStamp = moment.getSysMemory();
			memory = capacity(deviceStamp);
			item = new ShowItem();
			item.add(new ShowStringCell(0, txtSysMemory, colorSysMemory));
			item.add(new ShowStringCell(1, memory));
			// 判断空间
			if (deviceStamp.isMissing()) {
				item.add(new ShowStringCell(2, txtMissing, colorMissing));
			} else {
				item.add(new ShowStringCell(2, txtEnouth, colorEnouth));
			}
			addShowItem(item);

			// 磁盘空间
			deviceStamp = moment.getSysDisk();
			String disk = capacity(deviceStamp);
			item = new ShowItem();
			item.add(new ShowStringCell(0, txtSysDisk, colorSysDisk));
			item.add(new ShowStringCell(1, disk));
			// 判断空间
			if (deviceStamp.isMissing()) {
				item.add(new ShowStringCell(2, txtMissing, colorMissing));
			} else {
				item.add(new ShowStringCell(2, txtEnouth, colorEnouth));
			}
			addShowItem(item);

			// 成员规模
			PersonStamp kit = moment.getMember();
			if (kit != null) {
				String cyber = String.format("%d - %d", kit.getRealPersons(), kit.getMaxPersons());
				if (kit.getMaxPersons() < 1) {
					cyber = String.format("%d - ~", kit.getRealPersons());
				}
				item = new ShowItem();
				item.add(new ShowStringCell(0, txtMemberCyber, colorMemberCyber));
				item.add(new ShowStringCell(1, cyber));

				String text = txtEnouth;
				Color color = colorEnouth;
				if (kit.isFull()) {
					text = txtFull;
					color = colorFull;
				} else if (kit.isMissing()) {
					text = txtMissing;
					color = colorMissing;
				}
				item.add(new ShowStringCell(2, text, color));

				addShowItem(item);
			}

			// 在线用户
			kit = moment.getOnline();
			if (kit != null) {
				String cyber = String.format("%d - %d", kit.getRealPersons(), kit.getMaxPersons());
				if (kit.getMaxPersons() < 1) {
					cyber = String.format("%d - ~ ", kit.getRealPersons());
				}

				item = new ShowItem();
				item.add(new ShowStringCell(0, txtFrontCyber, colorFrontCyber));
				item.add(new ShowStringCell(1, cyber));

				String text = txtEnouth;
				Color color = colorEnouth;
				if (kit.isFull()) {
					text = txtFull;
					color = colorFull;
				} else if (kit.isMissing()) {
					text = txtMissing;
					color = colorMissing;
				}
				item.add(new ShowStringCell(2, text, color));

				addShowItem(item);
			}
		}

		// 输出全部记录
		flushTable();
	}
}