/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 扫描数据块命令的调用器。<br>
 * 
 * 流程：FRONT -> CALL -> DATA。 
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopScanEntityInvoker extends DesktopInvoker {

	/**
	 * 构造扫描数据块命令的调用器，指定命令
	 * @param cmd 扫描数据块命令
	 */
	public DesktopScanEntityInvoker(ScanEntity cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanEntity getCommand() {
		return (ScanEntity) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanEntity cmd = getCommand();
		Space space = cmd.getSpace();

		NodeSet set = getStaffPool().findTableSites(space);
		Node hub = (set != null ? set.next() : null);
		// 没有站点
		if (hub == null) {
			faultX(FaultTip.ILLEGAL_SITE_X, space);
			return false;
		}
		// 投递到CALL站点
		return fireToHub(hub, cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ScanEntityProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ScanEntityProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}
	
	/**
	 * 打印结果
	 * @param array
	 */
	private void print(List<ScanEntityItem> array) {
		// 显示运行时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SCAN-ENTITY/SITE", "SCAN-ENTITY/DATABASE", "SCAN-ENTITY/TABLE",
				"SCAN-ENTITY/STUBS", "SCAN-ENTITY/SIZE" });

		for (ScanEntityItem item : array) {
			ShowItem e = new ShowItem();
			Space space = item.getSpace();
			e.add(new ShowStringCell(0, item.getSite()));
			e.add(new ShowStringCell(1, space.getSchemaText()));
			e.add(new ShowStringCell(2, space.getTableText()));
			e.add(new ShowLongCell(3, item.getStubs()));
			e.add(new ShowStringCell(4, ConfigParser.splitCapacity(item.getLength()) ));
			
			addShowItem(e);
		}
		
		// 输出全部记录
		flushTable();
	}

}