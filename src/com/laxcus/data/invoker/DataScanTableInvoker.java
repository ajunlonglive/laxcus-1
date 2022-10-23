/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;

/**
 * 扫描数据表命令调用器
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class DataScanTableInvoker extends DataInvoker {

	/**
	 * 构造扫描数据表命令调用器，指定命令
	 * @param cmd 扫描数据表命令
	 */
	public DataScanTableInvoker(ScanTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanTable getCommand() {
		return (ScanTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanTable cmd = getCommand();
		
		ScanTableProduct product = new ScanTableProduct();
		
		for (Space space : cmd.list()) {
			// 如果数据表不存在，忽略它
			if(!StaffOnDataPool.getInstance().hasTable(space)) {
				continue;
			}
			
			ScanTableItem item = new ScanTableItem(getLocal(), space);

			// 总行数、有效行数，数据块总数
			long rows = 0L;
			long avaiableRows = 0L;
			int count = 0;

			// CACHE状态数据块
			long stub = AccessTrustor.getCacheStub(space);
			if (stub != 0L) {
				long size = AccessTrustor.getRows(space, stub);
				if (size >= 0) rows += size;
				size = AccessTrustor.getAvailableRows(space, stub);
				if (size > 0) avaiableRows += size;
				count++;
			}
			
			// CHUNK状态数据块
			long[] stubs = AccessTrustor.getChunkStubs(space);
			for (int i = 0; stubs != null && i < stubs.length; i++) {
				long size = AccessTrustor.getRows(space, stubs[i]);
				if (size >= 0) rows += size;
				size = AccessTrustor.getAvailableRows(space, stubs[i]);
				if (size > 0) avaiableRows += size;
				count++;
			}
			
			// 保存参数
			item.setRows(rows);
			item.setAvailableRows(avaiableRows);
			item.setStubs(count);
			// 保存单元
			product.add(item);
		}
		
		// 反馈处理结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch",  success, "item size %d", product.size());

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
