/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置映射端口调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 10/22/2020
 * @since laxcus 1.0
 */
public class RayReflectPortInvoker extends RayInvoker {

	/**
	 * 构造设置映射端口调用器，指定命令
	 * @param cmd 设置映射端口
	 */
	public RayReflectPortInvoker(ReflectPort cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReflectPort getCommand() {
		return (ReflectPort) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 投递到HUB站点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ReflectPortProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ReflectPortProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}
		return useful(success);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(ReflectPortProduct product) {
		// 显示运行时间
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "SET-REFLECT-PORT/STATUS",
				"SET-REFLECT-PORT/TYPE", "SET-REFLECT-PORT/PORT" });

		String stream = getXMLContent("SET-REFLECT-PORT/TYPE/STREAM");
		String packet = getXMLContent("SET-REFLECT-PORT/TYPE/PACKET");
		String sucker = getXMLContent("SET-REFLECT-PORT/TYPE/SUCKER");
		String dispatcher = getXMLContent("SET-REFLECT-PORT/TYPE/DISPATCHER");

		// 显示单元
		for (ReflectPortItem e : product.list()) {
			boolean success = e.isSuccessful();
			ShowItem item = new ShowItem();
			// 状态
			item.add(createConfirmTableCell(0, success));
			// 类型
			if (e.isStreamServer()) {
				item.add(new ShowStringCell(1, stream));
			} else if (e.isPacketServer()) {
				item.add(new ShowStringCell(1, packet));
			} else if (e.isSuckerServer()) {
				item.add(new ShowStringCell(1, sucker));
			} else if (e.isDispatcherServer()) {
				item.add(new ShowStringCell(1, dispatcher));
			} else {
				item.add(new ShowStringCell(1, "--"));
			}
			// 端口
			item.add(new ShowIntegerCell(2, e.getPort()));
			// 保存单元
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
	}

//	/**
//	 * 调用器数目
//	 */
//	private void reset() {
//		ReflectPort cmd = getCommand();
//
//		EchoTransfer.setReflectPorts(cmd.getInvokers());
//		EchoTransfer.setMaxConfineTime(cmd.getConfineTime());
//		long ms = EchoTransfer.getMaxConfineTime();
//
//		// 设置标题
//		createShowTitle(new String[] { "MAX-INVOKER/LOCAL/INVOKERS", "MAX-INVOKER/LOCAL/CONFINE-TIME"});
//		String alway = getXMLContent("MAX-INVOKER/ALWAY");
//		
//		// 显示单元
//		ShowItem item = new ShowItem();
//
//		item.add(new ShowIntegerCell(0, EchoTransfer.getReflectPorts()));
//		if (ms < 1) {
//			item.add(new ShowStringCell(1, alway));
//		} else {
//			item.add(new ShowLongCell(1, ms / 1000));
//		}
//		// 保存单元
//		addShowItem(item);
//
//		// 输出全部记录
//		flushTable();
//	}

}