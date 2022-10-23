/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 检索用户在线注册的元数据调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public class TopSeekRegisterMetadataInvoker extends TopInvoker {

	/** 记录结果 **/
	private SeekRegisterMetadataProduct product = new SeekRegisterMetadataProduct();

	/**
	 * 构造检索用户在线注册的元数据调用器，指定命令
	 * @param cmd 检索用户在线注册的元数据命令
	 */
	public TopSeekRegisterMetadataInvoker(SeekRegisterMetadata cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekRegisterMetadata getCommand() {
		return (SeekRegisterMetadata) super.getCommand();
	}
	
	/**
	 * 查找站点
	 * @param siger 用户签名
	 * @return 返回节点地址
	 */
	private List<Node> findSites(Siger siger) {
		TreeSet<Node> array = new TreeSet<Node>();

		NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
		if (set != null) {
			array.addAll(set.show());
		}

		return new ArrayList<Node>(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekRegisterMetadata cmd = getCommand();
		Node node = cmd.getSourceSite();
		// 判断命令来自WATCH，否则拒绝
		boolean success = WatchOnTopPool.getInstance().contains(node);
		if (!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		// 找匹配的HOME站点
		Map<Node, SeekRegisterMetadata> cmds = new TreeMap<Node, SeekRegisterMetadata>();
		for (Siger siger : cmd.getUsers()) {
			List<Node> slaves = findSites(siger);
			// 保存地址
			for (Node slave : slaves) {
				SeekRegisterMetadata sub = cmds.get(slave);
				if (sub == null) {
					sub = new SeekRegisterMetadata();
					cmds.put(slave, sub);
				}
				sub.addUser(siger);
			}
		}

		// 如果是空值
		if (cmds.isEmpty()) {
			replyProduct(product);
			return useful();
		}

		// 生成命令
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, SeekRegisterMetadata>> iterator = cmds.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, SeekRegisterMetadata> entry = iterator.next();
			CommandItem item = new CommandItem(entry.getKey(), entry.getValue());
			array.add(item);
		}
		
		// 发送到HOME站点
		int count = incompleteTo(array);
		// 判断成功
		success = (count > 0);
		if (!success) {
			replyProduct(product);
		}

		Logger.debug(this, "launch", success, "send size:%d, success count:%d",
				array.size(), count);

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
				if (isSuccessObjectable(index)) {
					SeekRegisterMetadataProduct e = getObject(SeekRegisterMetadataProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "Item Size:%d", product.size());

		return useful(success);
	}
	
}