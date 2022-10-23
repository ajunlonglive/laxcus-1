/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.scan.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 扫描数据表命令调用器。<br>
 * 
 * 对于扫描数据表命令，CALL站点处于中继位置，它接受来自FRONT站点的请求，然后找到关联的DATA主站点，分发给它们。
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class CallScanTableInvoker extends CallInvoker {

	/**
	 * 构造扫描数据表命令调用器，指定命令
	 * @param cmd 扫描数据表命令
	 */
	public CallScanTableInvoker(ScanTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanTable getCommand() {
		return (ScanTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanTable shift = getCommand();

		TreeMap<Node, ScanTable> sites = new TreeMap<Node, ScanTable>();
		// 找到主站点
		for (Space space : shift.list()) {
			// 查找数据表所在的DATA站点，包括主/从站点
			NodeSet set = DataOnCallPool.getInstance().findTableSites(space);
			// 没有，忽略它
			if(set == null){
				continue;
			}

			// 提取站点，生成命令
			List<Node> nodes = set.show();
			for(Node node : nodes) {
				ScanTable sub = sites.get(node);
				if (sub == null) {
					sub = new ScanTable();
					sites.put(node, sub);
				}
				// 保存一个表名
				sub.add(space);
			}
		}

		// 没有找到提示
		if (sites.isEmpty()) {
			replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
			return useful(false);
		}

		// 生成命令队列
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, ScanTable>> iterator = sites.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, ScanTable> entry = iterator.next();
			CommandItem item = new CommandItem(entry.getKey(), entry.getValue());
			array.add(item);
		}

		// 容错模式发送到DATA站点
		int count = incompleteTo(array);
		boolean success = (count > 0);

		Logger.debug(this, "launch", success, "send size:%d", array.size());

		// 通知请求端出错
		if (!success) {
			replyFault();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ScanVolumeProduct product = new ScanVolumeProduct();

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			// 不成功，继续下一个
			if (!isSuccessObjectable(index)) {
				continue;
			}
			try {
				ScanTableProduct e = getObject(ScanTableProduct.class, index);
				product.cumulate(e);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 发送给请求端
		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "scan table item is %d", product.size());

		return useful(success);
	}

}
