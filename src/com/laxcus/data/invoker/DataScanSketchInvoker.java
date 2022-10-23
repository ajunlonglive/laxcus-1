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
import com.laxcus.log.client.*;

/**
 * 检测表分布数据容量调用器<br>
 * 
 * @author scott.liang
 * @version 1.0 9/25/2015
 * @since laxcus 1.0
 */
public class DataScanSketchInvoker extends DataInvoker {

	/**
	 * 构造检测表分布数据容量调用器，指定命令
	 * @param cmd 检测表分布数据容量命令
	 */
	public DataScanSketchInvoker(ScanSketch cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanSketch getCommand() {
		return (ScanSketch) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanSketch cmd = getCommand();
		Space space = cmd.getSpace();

		long length = 0;
		long rows = 0;
		long availables = 0;

		boolean prime = isMaster();
	
		// 取全部CHUNK数据块参数
		long[] stubs = AccessTrustor.getChunkStubs(space);
		int size = (stubs == null ? 0 : stubs.length);
		for (int i = 0; i < size; i++) {
			long stub = stubs[i];
			// 文件长度
			String filename = AccessTrustor.findChunkPath(space, stub);
			length += AccessTrustor.length(filename);
			// 行数/有效行数			
			rows += AccessTrustor.getRows(space, stub);
			availables += AccessTrustor.getAvailableRows(space, stub);
			
			// debug
			Logger.debug(this, "launch", "chunk, %x, %s length is %d, %d, %d",
					stub, filename, AccessTrustor.length(filename),
					AccessTrustor.getRows(space, stub),
					AccessTrustor.getAvailableRows(space, stub));
		}
		// 取CACHE数据块参数
		if (prime) {
			long stub = AccessTrustor.getCacheStub(space);
			// 文件长度
			String filename = AccessTrustor.findCachePath(space, stub);
			length += AccessTrustor.length(filename);
			// 行数/有效行数
			rows += AccessTrustor.getRows(space, stub);
			availables += AccessTrustor.getAvailableRows(space, stub);
			
			Logger.debug(this, "launch", "cache, %x, %s length is %d, %d, %d",
					stub, filename, AccessTrustor.length(filename),
					AccessTrustor.getRows(space, stub),
					AccessTrustor.getAvailableRows(space, stub));
		}
		
		ScanSketchItem item = (prime ? new MasterSketchItem() : new SlaveSketchItem());

		item.setStubs(size + (prime ? 1 : 0));
		item.setLength(length);
		item.setRows(rows);
		item.setAvaliableRows(availables);
		
		ScanSketchProduct product = new ScanSketchProduct(space);
		product.addCapacityItem(item);
		
		boolean success = replyProduct(product);
		
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
