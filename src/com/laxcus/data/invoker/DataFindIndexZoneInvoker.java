/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.index.zone.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.zone.*;
import com.laxcus.data.pool.*;
import com.laxcus.data.slider.*;
import com.laxcus.log.client.*;

/**
 * 查询索引分区调用器。<br>
 * 命令从CALL/WORK站点发出，作用到DATA站点。DATA站点检查本地的索引分区。
 * 
 * @author scott.liang
 * @version 1.0 5/21/2012
 * @since laxcus 1.0
 */
public class DataFindIndexZoneInvoker extends DataInvoker {

	/**
	 * 构造查询索引分区调用器，指定命令
	 * @param cmd 查询索引分区命令
	 */
	public DataFindIndexZoneInvoker(FindIndexZone cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindIndexZone getCommand() {
		return (FindIndexZone) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindIndexZone cmd = getCommand();

		Dock dock = cmd.getDock();
		Table table = StaffOnDataPool.getInstance().findTable(dock.getSpace());
		// 没有找到表
		if (table == null) {
			Logger.debug(this, "launch", "cannot find %s", dock.getSpace());
			super.replyFault();
			return false;
		}

		// 查询索引
		IndexZone[] zones = DataSliderPool.getInstance().findIndexZones(dock);
		// 保存数据
		IndexZoneProduct product = new IndexZoneProduct();
		for (int i = 0; zones != null && i < zones.length; i++) {
			product.add(zones[i]);
		}

		// 发送到目标地址
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "%s index zone size is:%d", dock, product.size());

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
