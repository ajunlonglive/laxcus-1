/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.table.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 获取数据块分布图谱调用器
 * 
 * @author scott.liang
 * @version 1.0 11/11/2020
 * @since laxcus 1.0
 */
public class CallPrintStubsDiagramInvoker extends CallInvoker {

	/**
	 * 构造获取数据块分布图谱调用器，指定命令
	 * @param cmd 获取数据块分布图谱
	 */
	public CallPrintStubsDiagramInvoker(PrintStubsDiagram cmd) {
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

		// 找到全部主站点
		NodeSet set = DataOnCallPool.getInstance().findTableSites(space);
		ArrayList<Node> sites = new ArrayList<Node>();
		if (set != null) {
			sites.addAll(set.show());
		}

		// 空集合是错误
		if (sites.isEmpty()) {
			replyProduct(new PrintStubsDiagramProduct());
			return false;
		}

		// 向DATA主站点发出命令
		boolean success = completeTo(sites, cmd);
		// 反馈错误
		if (!success) {
			replyFault(Major.FAULTED, Minor.SYSTEM_FAILED);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		PrintStubsDiagramProduct product = new PrintStubsDiagramProduct();
		List<Integer> keys = getEchoKeys();
		// 保存全部
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					PrintStubsDiagramProduct e = super.getObject(PrintStubsDiagramProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = replyProduct(product);

		return useful(success);
	}

}
