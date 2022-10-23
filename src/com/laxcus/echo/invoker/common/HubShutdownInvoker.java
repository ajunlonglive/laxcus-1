/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.shutdown.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 管理站点远程关闭调用器。<br>
 * 是TOP/HOME/BANK节点的父类。
 * 
 * @author scott.liang
 * @version 1.0 8/8/2019
 * @since laxcus 1.0
 */
public abstract class HubShutdownInvoker extends CommonWatchShareInvoker {

	/** 反馈结果集合 **/
	private ShutdownProduct product = new ShutdownProduct();

	/** 关闭自己 **/
	private boolean selflly;

	/**
	 * 构造默认的管理站点远程关闭调用器，指定命令
	 * @param cmd 管理站点远程关闭
	 */
	protected HubShutdownInvoker(Shutdown cmd) {
		super(cmd);
		selflly = false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Shutdown getCommand() {
		return (Shutdown) super.getCommand();
	}

	/**
	 * 发送处理结果
	 */
	protected void reply() {
		replyProduct(product);
	}

	/**
	 * 生成副本命令
	 * @return Shutdown命令副本
	 */
	private Shutdown createSubCommand() {
		Shutdown cmd = getCommand();
		Shutdown sub = cmd.duplicate();
		// 快速处理
		sub.setQuick(true);
		return sub;
	}

	/**
	 * 关闭本地站点
	 */
	private void shutdown() {
		long delay = getCommand().getDelay();
		Logger.debug(this, "shutdown", "delay %d", delay);
		// 关闭前延时
		delay(delay);

		// 关闭本地
		getLauncher().stop();
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
//					Shutdown sub = createSubCommand();
//					CommandItem item = new CommandItem(node, sub);
//					array.add(item);
//					count++;
//				}
//			}
//		}
//		return (count > 0);
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Shutdown cmd = getCommand();
		List<Node> sites = fetchSubSites();

		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();

		if (cmd.isAll()) {
			Shutdown sub = createSubCommand();
			// 逐一分配给下属
			for (Node slave : sites) {
				CommandItem item = new CommandItem(slave, sub);
				array.add(item);
			}
			// 关闭自己
			selflly = true;
		} else {
			//			for(Node node : cmd.list()) {
			//				// 被检查的站点不属于当前站点的子站点，且命令不是来自WATCH站点，忽略它！
			//				if (!isSlaveSite(node) && !isFromWatch()) {
			//					continue;
			//				}
			//
			//				if(node.compareTo(getLocal()) == 0){
			//					selflly = true; // 是自己
			//				} else if (sites.contains(node)) {
			//					Shutdown sub = createSubCommand();
			//					// 生成命令单元保存
			//					CommandItem item = new CommandItem(node, sub);
			//					array.add(item);
			//				} else {
			//					// 找到HOME站点，把站点地址分配到它的下属
			//					boolean success = dispatchToSlaveHub(sites, array, node);
			//					if (!success) product.add(node, false);
			//				}
			//			}

			ArrayList<Node> other = new ArrayList<Node>();
			for (Node node : cmd.list()) {
				if (node.compareTo(getLocal()) == 0) { // 1. 地址匹配的节点
					selflly = true; // 是自己
				} else if (sites.contains(node)) { // 2. 直属当前集群的节点
					Shutdown sub = createSubCommand();
					// 生成命令单元保存
					CommandItem item = new CommandItem(node, sub);
					array.add(item);
				} else if(!isSlaveSite(node)) { // 3. 非当前集群节点，不存在主从关系

				} else {
					other.add(node);
				}
			}
			// 找到HOME/BANK，把站点地址分配到它的下属
			for (Node node : other) {
				boolean success = dispatchToSlaveHub(sites, array, node);
				if (!success) product.add(node, false);
			}
		}

		// 判断有子集
		boolean success = (array.size() > 0);
		// 有子节点
		if (success) {
			int count = incompleteTo(array);
			success = (count > 0);
			// 不成功，向WATCH返回结果。不考虑关闭自己！
			if (!success) {
				reply();
			}
		} else {
			// 关闭自己
			if (selflly) {
				// 保存自己
				product.add(getLocal(), true);
				// 发送处理结果
				reply();
				// 关闭
				shutdown();
				// 退出
				setQuit(true);
				success = true;
			} else {
				// 这是空集合
				reply();
			}
		}

		Logger.debug(this, "launch", success, "command item size:%d, product size:%d", array.size(), product.size());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			// 不成功，记录和忽略它
			if (!isSuccessObjectable(index)) {
				Node node = getBufferHub(index);
				product.add(node, false);
				continue;
			}
			try {
				ShutdownProduct that = getObject(ShutdownProduct.class, index);
				for (ShutdownItem e : that.list()) {
					// 如果单元已经存在，并且成功，删除旧单元，保存新单元。注意，是成功！
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

		// 判断是关闭自己
		if (selflly) {
			product.add(getLocal(), true);
		}

		// 发送处理结果
		reply();

		// 如果关闭自己，延时等待结果发送出去，然后关闭自己
		if (selflly) {
			shutdown();
		}

		return useful();
	}

	/**
	 * 获得注册的子级站点，由子类实现。
	 * @return 全部子类站点地址。
	 */
	protected abstract List<Node> fetchSubSites(); 

}