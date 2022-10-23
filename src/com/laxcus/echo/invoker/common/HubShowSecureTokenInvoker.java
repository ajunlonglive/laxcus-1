/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.secure.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 读取密钥令牌调用器 <br>
 * 
 * TOP/HOME/BANK站点使用。
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public abstract class HubShowSecureTokenInvoker extends CommonShowSecureTokenInvoker {

	/** 集合 **/
	private ShowSecureTokenProduct product = new ShowSecureTokenProduct();

	/**
	 * 构造读取密钥令牌调用器，指定命令
	 * @param cmd 读取密钥令牌
	 */
	protected HubShowSecureTokenInvoker(ShowSecureToken cmd) {
		super(cmd);
	}

	/**
	 * 接收处理结果
	 */
	protected void reply() {
		replyProduct(product);
	}
	
	/**
	 * 生成一个用于接收的命令
	 * @return
	 */
	private ShowSecureToken createSubCommand() {
		ShowSecureToken cmd = getCommand();
		return cmd.duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubShowSecureTokenInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShowSecureToken cmd = getCommand();
		// 获得当前管理站点的子类站点
		List<Node> sites = fetchSubSites();

		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		// 如果是全部，直接更新
		if (cmd.isAll()) {
			ShowSecureToken sub = createSubCommand();
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
					ShowSecureToken sub = createSubCommand();
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
			if (!isSuccessObjectable(index)) {
				Node node = getBufferHub(index);
				product.add(node, false);
				continue;
			}
			// 逐一提取
			try {
				ShowSecureTokenProduct a = getObject(ShowSecureTokenProduct.class, index);
				for (ShowSecureTokenItem e : a.list()) {
					// 如果单元已经存在，并且成功，删除旧单元。必须是成功的！
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

		// 接收处理结果
		reply();

		return useful();
	}

	/**
	 * 获得注册的子级站点。由子类实现
	 * @return List<Node>
	 */
	protected abstract List<Node> fetchSubSites(); 

}