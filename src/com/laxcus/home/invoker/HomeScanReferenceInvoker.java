/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.scan.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 分析用户资源调用器
 * 
 * @author scott.liang
 * @version 1.0 12/2/2013
 * @since laxcus 1.0
 */
public abstract class HomeScanReferenceInvoker extends HomeInvoker{

	/**
	 * 构造分析用户资源调用器，指定命令
	 * @param cmd 分析用户资源命令
	 */
	protected HomeScanReferenceInvoker(ScanReference cmd) {
		super(cmd);
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
		// 退出
		return useful(success);
	}

	/**
	 * 分发给DATA站点
	 * @param cmds 站点/命令组
	 * @return 发送成功返回真，否则假
	 */
	protected boolean distribute(Map<Node, ScanTable> cmds) {
		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, ScanTable>> iterator = cmds.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, ScanTable> entry = iterator.next();
			CommandItem e = new CommandItem(entry.getKey(), entry.getValue());
			array.add(e);
		}

		// 判断有命令
		boolean success = (array.size() > 0);
		// 以容错模式发送到DATA站点
		if (success) {
			int count = incompleteTo(array);
			success = (count > 0);
		}

		// 如果不成功，发送一个空集合给WATCH/TOP站点
		if (!success) {
			replyProduct(new ScanTableProduct());
		}

		Logger.debug(this, "distribute", success, "count is %d", array.size());

		return success;
	}
}