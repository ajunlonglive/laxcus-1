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
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 刷新元数据命令调用器。
 * 
 * TOP站点根据用户签名，发送命令到关联的HOME站点
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public class TopRefreshMetadataInvoker extends TopInvoker {

	/** 处理结果 **/
	private RefreshMetadataProduct product = new RefreshMetadataProduct();
	
	/**
	 * 刷新元数据命令调用器，指定命令
	 * @param cmd 刷新元数据命令
	 */
	public TopRefreshMetadataInvoker(RefreshMetadata cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshMetadata getCommand() {
		return (RefreshMetadata) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshMetadata cmd = getCommand();

		// HOME站点地址 -> 新的刷新命令
		Map<Node, RefreshMetadata> array = new TreeMap<Node, RefreshMetadata>();

		// 找到匹配的HOME站点
		for (Siger siger : cmd.getUsers()) {
			NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
			if (set == null) {
				Logger.warning(this, "launch", "cannot be find '%s'", siger);
				product.add(null, siger, false);
				continue;
			}

			for (Node site : set.show()) {
				RefreshMetadata meta = array.get(site);
				if (meta == null) {
					meta = new RefreshMetadata();
					array.put(site, meta);
				}
				meta.addUser(siger);
			}
		}

		// 空值，退出
		if (array.isEmpty()) {
			replyProduct(product);
			return useful(false);
		}

		// 命令单元
		ArrayList<CommandItem> items = new ArrayList<CommandItem>();
		// 提交参数，逐一投递
		Iterator<Map.Entry<Node, RefreshMetadata>> iterator = array.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, RefreshMetadata> entry = iterator.next();
			Node site = entry.getKey();
			RefreshMetadata meta = entry.getValue();
			// 保存命令单元
			CommandItem item = new CommandItem(site, meta);
			items.add(item);
		}

		// 投递命令
		int count = incompleteTo(items);
		// 反馈结果
		boolean success = (count > 0);
		// 不成功，通知来源
		if (!success) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
		}

		Logger.debug(this, "launch", success, "command size is %d", items.size());
		
		// 退出
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			// 不成功，记录它
			if (!isSuccessObjectable(index)) {
				Node hub = findHub(index);
				RefreshMetadata cmd = (RefreshMetadata) findCommand(index);
				for (Siger siger : cmd.getUsers()) {
					product.add(hub, siger, false);
				}
				continue;
			}
			try {
				RefreshMetadataProduct e = getObject(RefreshMetadataProduct.class, index);
				product.addAll(e);
			} catch (VisitException ex) {
				Logger.error(ex);
			}
		}

		// 反馈结果
		boolean success = replyProduct(product);

		Logger.debug(this, "ending", "size is %d", product.size());

		return useful(success);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		product.clear();
	}
}
