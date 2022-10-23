/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 节点最小内存限制调用器。
 * 
 * @author scott.liang
 * @version 1.0 8/8/2019
 * @since laxcus 1.0
 */
public class RayLeastMemoryInvoker extends RayInvoker {

	/**
	 * 构造节点最小内存限制调用器，指定命令
	 * @param cmd 节点最小内存限制
	 */
	public RayLeastMemoryInvoker(LeastMemory cmd) {
		super(cmd);
		setFast(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public LeastMemory getCommand() {
		return (LeastMemory) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		LeastMemory cmd = getCommand();
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
		LeastMemoryProduct product = null; 
		int index = findEchoKey(0);
		try {
			if(isSuccessObjectable(index)) {
				product = getObject(LeastMemoryProduct.class, index);
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
		String key = getXMLContent("LEAST-MEMORY/COUNT");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowIntegerCell(1, count));
		addShowItem(item);
	}
	
	/**
	 * 显示反馈结果
	 * @param a
	 */
	private void print(LeastMemoryProduct product) {
		ArrayList<Node> array = new ArrayList<Node>();
		LeastMemory cmd = getCommand();
		if (cmd.isLocal() || cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}
		
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "LEAST-MEMORY/STATUS", "LEAST-MEMORY/SITE" });
		
		printCount(product.size());
		
		// 处理单元
		for(Node node : array) {
			LeastMemoryItem e = product.find(node);
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
//	private void print(List<LeastMemoryItem> a) {
//		// 显示处理结果
//		printRuntime();
//		// 显示标题
//		createShowTitle(new String[] { "LEAST-MEMORY/STATUS", "LEAST-MEMORY/SITE" });
//		// 处理单元
//		for (LeastMemoryItem e : a) {
//			ShowItem item = new ShowItem();
//			item.add(createConfirmTableCell(0, e.isSuccessful()));
//			item.add(new ShowStringCell(1, e.getSite()));
//			addShowItem(item);
//		}
//		// 输出全部记录
//		flushTable();
//	}

	/**
	 * 重置节点最小内存限制
	 */
	private boolean reset() {
		LeastMemory cmd = getCommand();

		boolean success = true;
		if (isLinux()) {
			if (cmd.isUnlimit()) {
				LinuxDevice.getInstance().setMemoryUnlimit();
			} else {
				LinuxDevice.getInstance().setMemoryLeast(cmd.getCapacity());
				LinuxDevice.getInstance().setMemoryLeastRate(cmd.getRate());
			}
			// 重新检测
			try {
				LinuxDevice.getInstance().checkMemory();
			} catch (IOException e) {
				Logger.error(e);
			}
		} else if (isWindows()) {
			if (cmd.isUnlimit()) {
				WindowsDevice.getInstance().setMemoryUnlimit();
			} else {
				WindowsDevice.getInstance().setMemoryLeast(cmd.getCapacity());
				WindowsDevice.getInstance().setMemoryLeastRate(cmd.getRate());
			}
			// 重新检测
			WindowsDevice.getInstance().checkMemory();
		} else {
			success = false; // 无确定！
		}
		
		// 打印结果
		print(success);
		
		return success;
	}
	
	/**
	 * 打印本地参数
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		// 设置标题
		createShowTitle(new String[] { "LEAST-MEMORY/LOCAL" });

		ShowItem item = new ShowItem();
		String text = (success ? getXMLContent("LEAST-MEMORY/LOCAL/SUCCESS")
				: getXMLContent("LEAST-MEMORY/LOCAL/FAILED"));
		// 站点地址
		item.add(new ShowStringCell(0, text));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}