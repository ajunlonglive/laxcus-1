/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 扫描用户资源调用器
 * 
 * @author scott.liang
 * @version 1.0 12/2/2013
 * @since laxcus 1.0
 */
public abstract class TopScanReferenceInvoker extends TopInvoker{

	/**
	 * 构造扫描用户资源调用器，指定命令
	 * @param cmd 扫描用户资源命令
	 */
	protected TopScanReferenceInvoker(ScanReference cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanReference getCommand() {
		return (ScanReference) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ScanTableProduct product = new ScanTableProduct();

		// 取出参数
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					ScanTableProduct e = getObject(ScanTableProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 发送给WATCH站点
		boolean success = replyProduct(product);

		return useful(success);
	}

	/**
	 * 分发给HOME站点
	 * @param cmd ScanReference命令
	 * @param spaces 筛选后的数据表名
	 * @return 发送成功返回真，否则假
	 */
	protected boolean distribute(ScanReference cmd, List<Space> spaces) {
		// 查站点站点地址
		Map<Node, ScanTable> cmds = new TreeMap<Node, ScanTable>();
		for (Space space : spaces) {
			NodeSet set = HomeOnTopPool.getInstance().find(space);
			List<Node> nodes = (set == null ? null : set.show());
			// 忽略空指针
			if (nodes == null) {
				continue;
			}
			// 生成子命令
			for (Node node : nodes) {
				ScanTable sub = cmds.get(node);
				if (sub == null) {
					sub = new ScanTable();
					sub.addSites(cmd.getSites());
					cmds.put(node, sub);
				}
				sub.add(space);
			}
		}

		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, ScanTable>> iterator = cmds.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, ScanTable> entry = iterator.next();
			CommandItem e = new CommandItem(entry.getKey(), entry.getValue());
			array.add(e);
		}

		// 判断有成员
		boolean success = (array.size() > 0);
		// 以容错模式发送到HOME站点
		if (success) {
			int count = incompleteTo(array);
			success = (count > 0);
		}

		// 如果不成功，发送一个空集合给WATCH站点
		if (!success) {
			replyProduct(new ScanTableProduct());
		}

		Logger.debug(this, "distribute", success, "count is %d", array.size());

		return success;
	}
}