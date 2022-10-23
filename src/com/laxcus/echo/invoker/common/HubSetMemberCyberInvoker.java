/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.cyber.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 管理节点的设置成员虚拟空间参数调用器。<br>
 * 是TOP/HOME/BANK节点的父类。
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public abstract class HubSetMemberCyberInvoker extends CommonWatchShareInvoker {

	/** 反馈结果集合 **/
	private VirtualCyberProduct product = new VirtualCyberProduct();

	/**
	 * 构造默认的管理节点的设置成员虚拟空间参数调用器，指定命令
	 * @param cmd 管理节点的设置成员虚拟空间参数
	 */
	protected HubSetMemberCyberInvoker(SetMemberCyber cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetMemberCyber getCommand() {
		return (SetMemberCyber) super.getCommand();
	}

	/**
	 * 发送处理结果
	 */
	protected void reply() {
		replyProduct(product);
	}

	/**
	 * 重置设置成员虚拟空间参数时间
	 */
	private boolean reset() {
		return false;
	}

	/**
	 * 生成副本命令
	 * @return
	 */
	private SetMemberCyber createSubCommand() {
		SetMemberCyber cmd = getCommand();
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
//					SetMemberCyber sub = createSubCommand();
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
		SetMemberCyber cmd = getCommand();
		List<Node> sites = fetchSubSites();

		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();

		if (cmd.isAll()) {
			SetMemberCyber sub = createSubCommand();
			// 逐一分配给下属
			for (Node slave : sites) {
				CommandItem item = new CommandItem(slave, sub);
				array.add(item);
			}
		} else {
			//			for(Node node : cmd.list()) {
			//				// 被检查的站点不属于当前站点的子站点，且命令不是来自WATCH站点，忽略它！
			//				if (!isSlaveSite(node) && !isFromWatch()) {
			//					continue;
			//				}
			//				
			//				if(node.compareTo(getLocal()) == 0){
			//					boolean success = reset();
			//					product.add(getLocal(), success);
			//				} else if (sites.contains(node)) {
			//					SetMemberCyber sub = createSubCommand();
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
					boolean success = reset();
					product.add(getLocal(), success);
				} else if (sites.contains(node)) { // 2. 直属当前集群的节点
					SetMemberCyber sub = createSubCommand();
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
				VirtualCyberProduct that = getObject(VirtualCyberProduct.class, index);
				for (VirtualCyberItem e : that.list()) {
					// 如果单元已经存在，并且是成功时，删除旧单元，保存新单元。注意，是成功！
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
	//				SetMemberCyber sub = createSubCommand();
	//				CommandItem e = new CommandItem(node, sub);
	//				temps.add(e);
	//			}
	//		}
	//		// 3. 把工作站点分配给HOME站点的命令单元
	//		for (CommandItem e : temps) {
	//			SetMemberCyber cmd = (SetMemberCyber) e.getCommand();
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
	 * 获得注册的子级站点，由子类实现。
	 * @return 全部子类站点地址。
	 */
	protected abstract List<Node> fetchSubSites(); 

}