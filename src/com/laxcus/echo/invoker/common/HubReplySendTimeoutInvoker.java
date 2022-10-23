/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.reload.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 设置发送异步数据超时调用器 <br>
 * 
 * TOP/HOME/BANK站点使用。
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public abstract class HubReplySendTimeoutInvoker extends CommonReplySendTimeoutInvoker {

	/** 集合 **/
	private ReplySendTimeoutProduct product = new ReplySendTimeoutProduct();

	/**
	 * 构造设置发送异步数据超时调用器，指定命令
	 * @param cmd 应答包尺寸
	 */
	protected HubReplySendTimeoutInvoker(ReplySendTimeout cmd) {
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
	//	private ReplySendTimeout createSubCommand() {
	//		ReplySendTimeout cmd = getCommand();
	//		ReplySendTimeout sub = new ReplySendTimeout();
	//		sub.setQuick(cmd.isQuick());
	//		return cmd;
	//	}

	/**
	 * 生成一个用于发送的命令
	 * @return
	 */
	private ReplySendTimeout createSubCommand() {
		ReplySendTimeout cmd = getCommand();
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
//					ReplySendTimeout sub = createSubCommand();
//					CommandItem item = new CommandItem(node, sub);
//					array.add(item);
//					count++;
//				}
//			}
//		}
//		return (count > 0);
//	}

	/**
	 * 生成结果单元
	 * @param node
	 * @param success
	 * @return
	 */
	private ReplySendTimeoutItem createItem(Node node, boolean success) {
		// 处理结果
		ReplySendTimeoutItem item = new ReplySendTimeoutItem(node, success);
		if (success) {
			item.setDisableTimeout(ReplyWorker.getDisableTimeout());
			item.setSubPacketTimeout(ReplyWorker.getSubPacketTimeout());
			item.setInterval(ReplyWorker.getSendInterval());
		}
		return item;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubReplySendTimeoutInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReplySendTimeout cmd = getCommand();
		// 获得当前管理站点的子类站点
		List<Node> sites = fetchSubSites();

		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		// 如果是全部，直接更新
		if (cmd.isAll()) {
			ReplySendTimeout sub = createSubCommand();
			// 逐一分配给下属
			for (Node node : sites) {
				CommandItem item = new CommandItem(node, sub);
				array.add(item);
			}
			// 更新自己
			boolean success = reset();
			product.add(createItem(getLocal(), success));
		} else {
			//			// 逐一检查，匹配才处理
			//			for (Node node : cmd.list()) {
			//				// 被检查的站点不属于当前站点的子站点，且命令不是来自WATCH站点，忽略它！
			//				if (!isSlaveSite(node) && !isFromWatch()) {
			//					continue;
			//				}
			//				
			//				if (node.compareTo(getLocal()) == 0) { // 是当前节点，更新自己配置
			//					boolean success = reset();
			//					product.add(createItem(getLocal(), success));
			//				} else if (sites.contains(node)) { // 地址匹配，生成一个新命令保存
			//					ReplySendTimeout sub = createSubCommand();
			//					sub.add(node); // 保存本节点
			//					// 生成命令单元保存
			//					CommandItem item = new CommandItem(node, sub);
			//					array.add(item);
			//				} else {
			//					// 找到HOME站点，把站点地址分配到它的下属
			//					boolean success = dispatchToSlaveHub(sites, array, node);
			//					if (!success) product.add(node, false);
			//				}
			//				
			////				else if (isBottomSite(node.getFamily())) { // 是当前站点的子级节点的子级节点，保存
			////					// 找到HOME站点，把站点地址分配到它的下属
			////					boolean success = dispatchToSlaveHub(sites, array, node);
			////					if (!success) product.add(node, false);
			////				} else {
			////					product.add(node, false); // 不存在
			////				}
			//			}

			ArrayList<Node> other = new ArrayList<Node>();
			for (Node node : cmd.list()) {
				if (node.compareTo(getLocal()) == 0) { // 1. 地址匹配的节点
					boolean success = reset();
					product.add(createItem(getLocal(), success));
				} else if (sites.contains(node)) { // 2. 直属当前集群的节点
					ReplySendTimeout sub = createSubCommand();
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

//		// 以上成功，记录本次处理时间
//		if(success){
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
			if (!isSuccessObjectable(index)) {
				Node node = getBufferHub(index);
				product.add(node, false);
				continue;
			}
			// 逐一提取
			try {
				ReplySendTimeoutProduct a = getObject(ReplySendTimeoutProduct.class, index);
				for (ReplySendTimeoutItem e : a.list()) {
					// 如果单元已经存在，并且成功，删除旧单元。注意，是成功！
					if (product.contains(e) && e.isSuccessful()) {
						product.remove(e);
					}
					// 保存新单元
					product.add(e);
				}
				// 逐一累加处理时间
				product.addProcessTime(a.getProcessTime());
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 发送处理结果
		reply();

		return useful();
	}

	//	/**
	//	 * 判断是TOP的子管理节点（HOME/BANK）
	//	 * @param family 节点类型
	//	 * @return 返回真或者假
	//	 */
	//	private boolean isSlaveHub(Node node) {
	//		return SiteTag.isBank(node.getFamily()) || SiteTag.isHome(node.getFamily());
	//	}

	//	/**
	//	 * 把一个工作站点分配给HOME/BANK站点
	//	 * @param sites 当前HUB站点的子类站点
	//	 * @param inputs 输入的命令单元列表
	//	 * @param job 工作站点
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean dispatchToSlaveHub(List<Node> sites, List<CommandItem> inputs, Node job) {
	//		ArrayList<CommandItem> temps = new ArrayList<CommandItem>();
	//		// 1. 找到HOME站点
	//		for (CommandItem e : inputs) {
	//			// 判断是HOME/BANK节点，保存它！
	//			if (isSlaveHub(e.getHub())) {
	//				temps.add(e);
	//			}
	//		}
	//		// 2. 如果没有，从输入的命令单元中找到HOME/BANK站点，生成命令单元
	//		boolean empty = (temps.isEmpty());
	//		if (empty) {
	//			for (Node node : sites) {
	//				// 不是HOME/BANK站点，忽略
	//				if (!isSlaveHub(node)) {
	//					continue;
	//				}
	//				// 生成命令单元，保存到临时数组中
	//				ReplySendTimeout sub = createSubCommand();
	//				CommandItem e = new CommandItem(node, sub);
	//				temps.add(e);
	//			}
	//		}
	//		// 3. 把工作站点分配给HOME站点的命令单元
	//		for (CommandItem e : temps) {
	//			ReplySendTimeout cmd = (ReplySendTimeout) e.getCommand();
	//			cmd.add(job);
	//		}
	//		// 4. 以上空时，把生成单元保存到输出数据
	//		if (empty) {
	//			inputs.addAll(temps);
	//		}
	//		// 有数组结果
	//		return temps.size() > 0;
	//	}


	//	/**
	//	 * 把一个工作站点分配给HOME站点
	//	 * @param sites 当前HUB站点的子类站点
	//	 * @param inputs 输入的命令单元列表
	//	 * @param job 工作站点
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean dispatchToHome(List<Node> sites, List<CommandItem> inputs, Node job) {
	//		ArrayList<CommandItem> temps = new ArrayList<CommandItem>();
	//		// 1. 找到HOME站点
	//		for (CommandItem e : inputs) {
	//			// 不是HOME站点，忽略它
	//			if (!e.getHub().isHome()) {
	//				continue;
	//			}
	//			temps.add(e);
	//		}
	//		// 2. 如果没有，从输入的命令单元中找到HOME站点，生成命令单元
	//		boolean empty = (temps.isEmpty());
	//		if (empty) {
	//			for (Node node : sites) {
	//				// 不是HOME站点，忽略
	//				if (!node.isHome()) {
	//					continue;
	//				}
	//				// 生成命令单元，保存到临时数组中
	//				ReplySendTimeout sub = createSubCommand();
	//				CommandItem e = new CommandItem(node, sub);
	//				temps.add(e);
	//			}
	//		}
	//		// 3. 把工作站点分配给HOME站点的命令单元
	//		for (CommandItem e : temps) {
	//			ReplySendTimeout cmd = (ReplySendTimeout) e.getCommand();
	//			cmd.add(job);
	//		}
	//		// 4. 以上空时，把生成单元保存到输出数据
	//		if (empty) {
	//			inputs.addAll(temps);
	//		}
	//		// 有数组结果
	//		return temps.size() > 0;
	//	}

	/**
	 * 获得注册的子级站点。由子类实现
	 * @return List<Node>
	 */
	protected abstract List<Node> fetchSubSites(); 

	//	/**
	//	 * 相对于当前管理节点，判断是当前管理节点的子节点的子节点。如果TOP则是，HOME则不是。<br>
	//	 * LOG/DATA/WORK/BUILD/CALL五类最底层站点。<br>
	//	 * 注：LAXCUS是两级管理结构，一个TOP节点下面有N个HOME站点。<br>
	//	 * 
	//	 * @param family 节点数据
	//	 * @return 返回真或者假
	//	 */
	//	protected abstract boolean isBottomSite(byte family);
}