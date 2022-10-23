/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.sign.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;

/**
 * 获取数据块分布图谱调用器
 * 
 * @author scott.liang
 * @version 1.0 11/11/2020
 * @since laxcus 1.0
 */
public class DataPrintStubsDiagramInvoker extends DataInvoker {

	/**
	 * 构造获取数据块分布图谱调用器，指定命令
	 * @param cmd 获取数据块分布图谱
	 */
	public DataPrintStubsDiagramInvoker(PrintStubsDiagram cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PrintStubsDiagram getCommand() {
		return (PrintStubsDiagram) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PrintStubsDiagram cmd = getCommand();
		Space space = cmd.getSpace();

		List<StubSign> list = scanDisk(space);
		if (list == null || list.isEmpty()) {
			replyProduct(new PrintStubsDiagramProduct(space));
			return useful(false);
		}

		// 打印结果
		PrintStubsDiagramItem item = new PrintStubsDiagramItem(getLocal());
		for (StubSign sign : list) {
			long stub = sign.getStub();
			if (sign.isCache()) {
				String filename = AccessTrustor.findCachePath(space, stub);
				long length = AccessTrustor.length(filename);
				item.addCacheStub(new PrintStubsDiagramCell(stub, length));
			} else if (sign.isCacheReflex()) {
				String filename = AccessTrustor.findCacheReflexPath(space, stub);
				long length = AccessTrustor.length(filename);
				item.addReflexStub(new PrintStubsDiagramCell(stub, length));
			} else if (sign.isChunk()) {
				String filename = AccessTrustor.findChunkPath(space, stub);
				long length = AccessTrustor.length(filename);
				item.addChunkStub(new PrintStubsDiagramCell(stub, length));
			}
		}

		PrintStubsDiagramProduct product = new PrintStubsDiagramProduct(space);
		product.add(item);

		boolean success = replyProduct(product);

		return useful(success);
	}
	
	/**
	 * 检索磁盘
	 * @param space
	 * @return
	 */
	private List<StubSign> scanDisk(Space space) {
		List<StubSign> list = null;
		try {
			list = AccessTrustor.sign(space);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return list;
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
