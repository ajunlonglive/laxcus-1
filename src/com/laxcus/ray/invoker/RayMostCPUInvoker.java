/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 节点最大中央处理器使用率限制调用器。
 * 
 * @author scott.liang
 * @version 1.0 9/8/2019
 * @since laxcus 1.0
 */
public class RayMostCPUInvoker extends RayInvoker {

	/**
	 * 构造节点最大中央处理器使用率限制调用器，指定命令
	 * @param cmd 节点最大中央处理器使用率限制
	 */
	public RayMostCPUInvoker(MostCPU cmd) {
		super(cmd);
		setFast(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MostCPU getCommand() {
		return (MostCPU) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MostCPU cmd = getCommand();
		// 本地节点
		if (cmd.isLocal()) {
			boolean success = reset();
			return useful(success);
		} else {
			// 投递到HUB站点，分别处理
			return fireToHub();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		MostCPUProduct product = null; 
		int index = findEchoKey(0);
		try {
			if(isSuccessObjectable(index)) {
				product = getObject(MostCPUProduct.class, index);
			}
		} catch(VisitException e){
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
	 * 显示统计值
	 * @param count
	 */
	private void printCount(int count) {
		// 节点地址
		String key = getXMLContent("MOST-CPU/COUNT");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowIntegerCell(1, count));
		addShowItem(item);
	}

	/**
	 * 显示反馈结果
	 * @param a
	 */
	private void print(MostCPUProduct product) {
		ArrayList<Node> array = new ArrayList<Node>();
		MostCPU cmd = getCommand();
		if (cmd.isLocal() || cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}
		
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "MOST-CPU/STATUS", "MOST-CPU/SITE" });
		
//		String count = findXMLTitle("CHECK-SITE-PATH/ITEM/COUNT");
		
		// 显示全部
		
		printCount(product.size());

		// 处理单元
		for(Node node : array) {
			MostCPUItem e = product.find(node);
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowStringCell(1, e.getSite()));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}
	
//	/**
//	 * 显示反馈结果
//	 * @param a
//	 */
//	private void print(List<MostCPUItem> a) {
//		// 显示处理结果
//		printRuntime();
//		// 显示标题
//		createShowTitle(new String[] { "MOST-CPU/STATUS", "MOST-CPU/SITE" });
//		// 处理单元
//		for (MostCPUItem e : a) {
//			ShowItem item = new ShowItem();
//			item.add(createConfirmTableCell(0, e.isSuccessful()));
//			item.add(new ShowStringCell(1, e.getSite()));
//			addShowItem(item);
//		}
//		// 输出全部记录
//		flushTable();
//	}

	/**
	 * 重置节点最大中央处理器使用率限制
	 */
	private boolean reset() {
		MostCPU cmd = getCommand();

		EchoTransfer.setMaxCpuRate(cmd.getRate());

		// 打印结果
		print(true);

		return true;
	}

	/**
	 * 打印本地参数
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		// 设置标题
		createShowTitle(new String[] { "MOST-CPU/LOCAL" });

		ShowItem item = new ShowItem();
		String text = (success ? getXMLContent("MOST-CPU/LOCAL/SUCCESS")
				: getXMLContent("MOST-CPU/LOCAL/FAILED"));
		// 站点地址
		item.add(new ShowStringCell(0, text));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}