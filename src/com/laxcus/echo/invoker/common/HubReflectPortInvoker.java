/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 管理站点的释放节点内存命令调用器 <br>
 * 
 * TOP/HOME/BANK站点使用。
 * 
 * @author scott.liang
 * @version 1.0 11/23/2015
 * @since laxcus 1.0
 */
public abstract class HubReflectPortInvoker extends CommonReflectPortInvoker {

	/** 集合 **/
	private ReflectPortProduct product = new ReflectPortProduct();

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 释放节点内存命令
	 */
	protected HubReflectPortInvoker(ReflectPort cmd) {
		super(cmd);
	}

	/**
	 * 发送处理结果
	 */
	protected void reply() {
		replyProduct(product);
	}
	
//	/**
//	 * 生成一个用于发送的命令
//	 * @return
//	 */
//	private ReflectPort createSubCommand() {
//		ReflectPort cmd = getCommand();
//		ReflectPort sub = cmd.duplicate();
//		sub.setQuick(cmd.isQuick());
//		return sub;
//	}

	/**
	 * 生成一个用于发送的命令
	 * @return
	 */
	private ReflectPort createSubCommand() {
		ReflectPort cmd = getCommand();
		return cmd.duplicate();
	}
	
//	/**
//	 * 把一个工作站点分配给HOME/BANK站点
//	 * @param sites 当前HUB站点的子类站点
//	 * @param array 输入的命令单元列表
//	 * @param slave 工作站点
//	 * @return 成功返回真，否则假
//	 */
//	private boolean dispatchToSlaveHub(List<Node> sites, List<CommandItem> array, Node slave) {
//		int count = 0;
//		for (CommandItem item : array) {
//			if (isSlaveSite(item.getHubFamily(), slave.getFamily())) {
//				count++;
//			}
//		}
//		if (count == 0) {
//			for (Node node : sites) {
//				// 是匹配的类型
//				if (isSlaveSite(node.getFamily(), slave.getFamily())) {
//					ReflectPort sub = createSubCommand();
//					CommandItem item = new CommandItem(node, sub);
//					array.add(item);
//					count++;
//				}
//			}
//		}
//		return (count > 0);
//	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubReflectPortInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReflectPort cmd = getCommand();
		// 获得当前管理站点的子类站点
		List<Node> sites = fetchSubSites();

		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();

		Node node = cmd.getSite();
//		if (node.compareTo(getLocal()) == 0) { // 是当前节点，更新自己配置
//			ReflectPortProduct ret = reload();
//			product.add(ret);
//		} else if (sites.contains(node)) { // 地址匹配，生成一个新命令保存
//			ReflectPort sub = createSubCommand();
//			// 生成命令单元保存
//			CommandItem item = new CommandItem(node, sub);
//			array.add(item);
//		}
//		// 下属节点
//		else {
//			// 找到HOME站点，把站点地址分配到它的下属
//			boolean success = dispatchToSlaveHub(sites, array);
//			if (!success) {
//				for(ReflectPortItem e : cmd.list()) {
//					ReflectPortItem sub = e.duplicate();
//					sub.setSuccessful(false);
//					product.add(sub);
//				}
//			}
//		}

		ArrayList<Node> other = new ArrayList<Node>();

		if (node.compareTo(getLocal()) == 0) { // 1. 地址匹配的节点
			ReflectPortProduct ret = reload();
			product.add(ret);
		} else if (sites.contains(node)) { // 2. 直属当前集群的节点
			ReflectPort sub = createSubCommand();
			// 生成命令单元保存
			CommandItem item = new CommandItem(node, sub);
			array.add(item);
		} else if(!isSlaveSite(node)) { // 3. 非当前集群节点，不存在主从关系，忽略

		} else {
			other.add(node); // 4. 未知，保存！
		}

		// 找到HOME/BANK，把站点地址分配到它的下属
		for (Node slave : other) {
			boolean success = dispatchToSlaveHub(sites, array, slave);
			if (!success) {
				for(ReflectPortItem e : cmd.list()) {
					ReflectPortItem sub = e.duplicate();
					sub.setSuccessful(false);
					product.add(sub);
				}
			}
		}
		
		// 判断不是空集合，发送命令到子节点
		boolean success = (array.size() > 0);
		// 以容错模式发送到下属站点
		if (success) {
			int count = incompleteTo(array);
			success = (count > 0);
		}
		// 不成功，返回本次结果
		if (!success) {
			reply();
		}

		Logger.debug(this, "launch", success, "site size:%d, product size:%d", array.size(), product.size());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				ReflectPortProduct a = getObject(ReflectPortProduct.class, index);
				for (ReflectPortItem e : a.list()) {
					// 如果单元已经存在，并且成功时，而且当前是成功状态，删除旧单元
					if (product.contains(e) && e.isSuccessful()) {
						product.remove(e);
					}
					// 保存新单元
					product.add(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 发送处理结果
		reply();

		return useful();
	}

	/**
	 * 获得注册的子级站点。由子类实现
	 * @return List<Node>
	 */
	protected abstract List<Node> fetchSubSites(); 

}