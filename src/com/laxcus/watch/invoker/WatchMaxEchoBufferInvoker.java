/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 设置FIXP本地密文超时调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class WatchMaxEchoBufferInvoker extends WatchInvoker {

	/**
	 * 构造设置FIXP本地密文超时调用器，指定命令
	 * @param cmd 设置FIXP本地密文超时
	 */
	public WatchMaxEchoBufferInvoker(MaxEchoBuffer cmd) {
		super(cmd);
		setFast(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MaxEchoBuffer getCommand() {
		return (MaxEchoBuffer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MaxEchoBuffer cmd = getCommand();

		// 如果是本地，在本地处理显示
		if (cmd.isLocal()) {
			long capacity = cmd.getCapacity();		
			// 设置本地密文超时时间
			EchoArchive.setMaxBufferSize(capacity);
			print(capacity);
			return useful();
		} else {
			// 投递到HUB站点，分别处理
			return fireToHub();
		}
	}

	/**
	 * 显示内存尺寸
	 * @param capacity
	 */
	private void print(long capacity) {
		createShowTitle(new String[] { "LOCAL-MAX-ECHO-BUFFER/CAPACITY" });
		
//		String text = ConfigParser.splitCapacity(capacity, 2);
//		
//		ShowItem item = new ShowItem();
//		item.add(new ShowStringCell(0, text));
//		addShowItem(item);
		
		ShowItem item = new ShowItem();
		if (capacity == 0) {
			String text = getXMLContent("LOCAL-MAX-ECHO-BUFFER/ANY");
			item.add(new ShowStringCell(0, text));
		} else {
			String text = ConfigParser.splitCapacity(capacity, 2);
			item.add(new ShowStringCell(0, text));
		}
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		MaxEchoBufferProduct product = null; 
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(MaxEchoBufferProduct.class, index);
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
	 * 显示反馈结果
	 * @param a
	 */
	private void print(List<MaxEchoBufferItem> a) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "MAX-ECHO-BUFFER-REMOTE/STATUS", "MAX-ECHO-BUFFER-REMOTE/SITE" });
		// 处理单元
		for (MaxEchoBufferItem e : a) {
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowStringCell(1, e.getSite()));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}
