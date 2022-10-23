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

import com.laxcus.access.schema.*;
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检索用户在线注册的元数据调用器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public class WatchSeekRegisterMetadataInvoker extends WatchInvoker {

	/**
	 * 构造检索用户在线注册的元数据调用器，指定命令
	 * @param cmd 检索用户在线注册的元数据
	 */
	public WatchSeekRegisterMetadataInvoker(SeekRegisterMetadata cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekRegisterMetadata getCommand() {
		return (SeekRegisterMetadata) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须注册到TOP/HOME节点，否则拒绝
		boolean success = (isHomeHub() || isTopHub());
		if (!success) {
			faultX(FaultTip.TOP_HOME_RETRY);
			return useful(false);
		}
		// 发送给服务器
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekRegisterMetadataProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SeekRegisterMetadataProduct.class, index);
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
	private void print(SeekRegisterMetadataProduct product) {
		// 打印消耗的时间
		printRuntime();

		// 显示标题
		createShowTitle(new String[] { "SEEK-REGISTER-METADATA/T1",
				"SEEK-REGISTER-METADATA/T2","SEEK-REGISTER-METADATA/T3","SEEK-REGISTER-METADATA/T4" });

		String txtSiger = getXMLContent("SEEK-REGISTER-METADATA/SIGER");
		Color colorSiger = findXMLForeground("SEEK-REGISTER-METADATA/SIGER", Color.BLACK);

		String txtSite = getXMLContent("SEEK-REGISTER-METADATA/SITE");
		Color colorSite = findXMLForeground("SEEK-REGISTER-METADATA/SITE", Color.BLACK);
		String txtNotFound = getXMLAttribute("SEEK-REGISTER-METADATA/SITE/notfound");

		String txtTable = getXMLContent("SEEK-REGISTER-METADATA/TABLE");
		Color colorTable = findXMLForeground("SEEK-REGISTER-METADATA/TABLE",Color.BLACK);
		String txtPhase = getXMLContent("SEEK-REGISTER-METADATA/PHASE");
		Color colorPhase = findXMLForeground("SEEK-REGISTER-METADATA/PHASE",Color.BLACK);
		
		String txtRegTable = getXMLContent("SEEK-REGISTER-METADATA/REG-TABLE");
		Color colorRegTable = findXMLForeground("SEEK-REGISTER-METADATA/REG-TABLE", Color.BLACK);

		String txtRegPhase = getXMLContent("SEEK-REGISTER-METADATA/REG-PHASE");
		Color colorRegPhase = findXMLForeground("SEEK-REGISTER-METADATA/REG-PHASE", Color.BLACK);

		String txtRegSite = getXMLContent("SEEK-REGISTER-METADATA/REG-SITE");
		Color colorRegSite = findXMLForeground("SEEK-REGISTER-METADATA/REG-SITE", Color.BLACK);

		SeekRegisterMetadata cmd = getCommand();
		List<Siger> sigers = cmd.getUsers();

		for (int n = 0; n < sigers.size(); n++) {
			if (n > 0) printGap(4);

			// 用户签名
			Siger siger = sigers.get(n);
			// 找明文
			String plainText = cmd.findPlainText(siger);

			// 查找匹配的结果
			List<SeekRegisterMetadataItem> items = product.find(siger);
			if (items.isEmpty()) {
				ShowItem showItem = new ShowItem();
				showItem.add(new ShowStringCell(0, txtSiger, colorSiger));
				showItem.add(new ShowStringCell(1, plainText));
				showItem.add(new ShowStringCell(2, txtSite, colorSite));
				showItem.add(new ShowStringCell(3, txtNotFound));
				addShowItem(showItem);
				continue;
			}
			
			// 显示结果
			for(SeekRegisterMetadataItem item : items) {
				Seat seat = item.getSeat();

				ShowItem showItem = new ShowItem();
				showItem.add(new ShowStringCell(0, txtSiger, colorSiger));
				showItem.add(new ShowStringCell(1, plainText));
				showItem.add(new ShowStringCell(2, txtSite, colorSite));
				showItem.add(new ShowStringCell(3, seat.getSite()));
				addShowItem(showItem);

				// 输出数据表
				for (Space space : item.getLocalTables() ) {
					showItem = new ShowItem();
					showItem.add(new ShowStringCell(0, txtTable, colorTable));
					showItem.add(new ShowStringCell(1, space));
					showItem.add(new ShowStringCell(2, ""));
					showItem.add(new ShowStringCell(3, ""));
					addShowItem(showItem);
				}
				// 远程站点
				for (RemoteTableItem e : item.getRemoteTables()) {
					showItem = new ShowItem();
					showItem.add(new ShowStringCell(0, txtRegTable, colorRegTable));
					showItem.add(new ShowStringCell(1, e.getSpace()));
					showItem.add(new ShowStringCell(2, txtRegSite, colorRegSite));
					showItem.add(new ShowStringCell(3, e.getNode()));
					addShowItem(showItem);
				}
				
				// 本地阶段命名
				for (Phase phase :  item.getLocalPhases()) {
					showItem = new ShowItem();
					showItem.add(new ShowStringCell(0, txtPhase, colorPhase));
					showItem.add(new ShowStringCell(1, phase.toString(false))); // TRUE:简化模式输出，忽略用户签名，否则标准模式输出。
					showItem.add(new ShowStringCell(2, ""));
					showItem.add(new ShowStringCell(3, ""));
					addShowItem(showItem);
				}
				// 在本地注册的远端阶段命名
				for (RemotePhaseItem e : item.getRemotePhases()) {
					showItem = new ShowItem();
					showItem.add(new ShowStringCell(0, txtRegPhase, colorRegPhase));
					showItem.add(new ShowStringCell(1, e.getPhase().toString(false))); // TRUE:简化模式输出，忽略用户签名，否则标准模式输出。
					showItem.add(new ShowStringCell(2, txtRegSite, colorRegSite));
					showItem.add(new ShowStringCell(3, e.getNode()));
					addShowItem(showItem);
				}
			}
		}
		
		// 输出全部记录
		flushTable();
	}

}