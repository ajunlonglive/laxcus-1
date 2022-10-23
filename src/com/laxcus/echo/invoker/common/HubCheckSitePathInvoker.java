/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 管理节点的打印站点检测目录调用器。<br>
 * 是TOP/HOME/BANK节点的父类。
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.0
 */
public abstract class HubCheckSitePathInvoker extends CommonCheckSitePathInvoker {

	/** 反馈结果集合 **/
	private CheckSitePathProduct product = new CheckSitePathProduct();

	/**
	 * 构造默认的管理节点的打印站点检测目录调用器，指定命令
	 * @param cmd 管理节点的打印站点检测目录
	 */
	protected HubCheckSitePathInvoker(CheckSitePath cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckSitePath getCommand() {
		return (CheckSitePath) super.getCommand();
	}

	/**
	 * 发送处理结果
	 */
	protected void reply() {
		replyProduct(product);
	}

	/**
	 * 生成副本命令
	 * @return
	 */
	private CheckSitePath createSubCommand() {
		CheckSitePath cmd = getCommand();
//		cmd.setQuick(true);
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
//					CheckSitePath sub = createSubCommand();
//					CommandItem item = new CommandItem(node, sub);
//					array.add(item);
//					count++;
//				}
//			}
//		}
//		return (count > 0);
//	}
	
	
//	/**
//	 * 当前HUB节点已经存在于单元中
//	 * @param hub
//	 * @param array
//	 * @return
//	 */
//	private boolean exists(Node hub, List<CommandItem> array) {
//		int count = 0;
//		for (CommandItem item : array) {
//			boolean success = (Laxkit.compareTo(item.getHub(), hub) == 0);
//			if (success) {
//				count++;
//			}
//		}
//		return (count > 0);
//	}
//	
//	/**
//	 * 把一个工作站点分配给HOME/BANK站点
//	 * @param hubs 当前HUB站点的子类站点
//	 * @param array 输入的命令单元列表
//	 * @param slave 工作站点
//	 * @return 成功返回真，否则假
//	 */
//	private boolean dispatchToSlaveHub(List<Node> hubs, List<CommandItem> array, Node slave) {
//		int count = 0;
//		for (Node hub : hubs) {
//			// 1. 非直属主机，忽略
//			if (!isDirectlySlaveSite(hub.getFamily(), slave.getFamily())) {
//				continue;
//			}
//			// 2. 判断节点存在于CommandItem集合
//			boolean exists = exists(hub, array);
//			// 3. 不成立，生成命令副本，保存它!
//			if (!exists) {
//				Command sub = getCommand().duplicate();
//				CommandItem item = new CommandItem(hub, sub);
//				array.add(item);
//				count++;
//			}
//		}
//		
//		// 如果是日志节点，特殊处理，分配给每一个Manager节点
//		if (SiteTag.isLog(slave.getFamily())) {
//			for (Node hub : hubs) {
//				boolean exists = exists(hub, array);
//				boolean manager = RankTag.isManager(hub.getRank());
//				// 不存在，并且是管理节点时
//				if (!exists && manager) {
//					Command sub = getCommand().duplicate();
//					CommandItem item = new CommandItem(hub, sub);
//					array.add(item);
//					count++;
//				}
//			}
//		}
//		
//		return (count > 0);
//	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckSitePath cmd = getCommand();
		// 获得当前管理站点的子类站点
		List<Node> sites = fetchSubSites();

		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		// 如果是全部，直接更新
		if (cmd.isAll()) {
			CheckSitePath sub = createSubCommand();
			// 逐一分配给下属
			for (Node node : sites) {
				CommandItem item = new CommandItem(node, sub);
				array.add(item);
			}
			// 更新自己
			product.add(pickup());
		} else {
			ArrayList<Node> other = new ArrayList<Node>();
			for (Node node : cmd.list()) {
				if (node.compareTo(getLocal()) == 0) { // 1. 地址匹配的节点
					product.add(pickup());// 保存检测结果
				} else if (sites.contains(node)) { // 2. 直属当前集群的节点
					CheckSitePath sub = createSubCommand();
					// 生成命令单元保存
					CommandItem item = new CommandItem(node, sub);
					array.add(item);
				} else if(!isSlaveSite(node)) { // 3. 非当前集群节点，不存在主从关系，忽略它!
					
				} else {
					other.add(node);
				}
			}
			// 找到HOME/BANK，把站点地址分配到它的下属。如果不成功，保存到结果集中。
			for (Node node : other) {
				boolean success = dispatchToSlaveHub(sites, array, node);
				if (!success) product.add(node);
				
				Logger.note(this, "launch", success, "dispatch slave site: %s", node);
			}
		}
		
		// 判断不是空集合，接收命令到子节点
		boolean success = (array.size() > 0);
		// 以容错模式接收到下属站点
		if (success) {
			int count = incompleteTo(array);
			success = (count > 0);
		}
		// 不成功，返回本次结果
		if (!success) {
			reply();
		}

//		// 以上成功，记录本次处理时间
//		if (success) {
//			product.addProcessTime(getProcessTime());
//		}

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
			// 不成功，记录和忽略它
			if (!isSuccessObjectable(index)) {
				Node node = getBufferHub(index);
				// 记录这个节点
				product.add(node);
				continue;
			}
			try {
				CheckSitePathProduct a = getObject(CheckSitePathProduct.class, index);
				for (CheckSitePathItem e : a.list()) {
					// 如果单元已经存在，并且是成功的，删除旧单元，保存新单元
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
		
//		print();
		
		// 发送处理结果
		reply();

		return useful();
	}
	
//	private void print() {
//		for (CheckSitePathItem e : product.list()) {
//			Logger.error(this, "print", "%s", e.toString());
//		}
//	}
	
	/**
	 * 获得注册的子级站点，由子类实现。
	 * @return 全部子类站点地址。
	 */
	protected abstract List<Node> fetchSubSites(); 

}