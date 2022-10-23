/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.relate.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 获取CALL站点成员调用器。<br>
 * 
 * TOP站点查询关联的HOME站点，并且将命令转发给这些HOME站点，然后等待HOME站点的反馈。
 * 
 * @author scott.liang
 * @version 1.0 6/23/2013
 * @since laxcus 1.0
 */
public class TopTakeCallItemInvoker extends TopInvoker {

	/**
	 * 构造获取CALL站点成员调用器，指定命令
	 * @param cmd 获取CALL站点成员命令
	 */
	public TopTakeCallItemInvoker(TakeCallItem cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeCallItem getCommand() {
		return (TakeCallItem) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeCallItem cmd = getCommand();
		Siger siger = cmd.getUsername();

		ArrayList<Node> slaves = new ArrayList<Node>();

		// 查询关联的CALL站点
		NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}

		// 以容错模式，将命令发送到HOME站点
		int count = incompleteTo(slaves, cmd);
		// 判断操作结果
		boolean success = (count > 0);
		if (!success) {
			refuse();
		}

		Logger.debug(this, "launch", success, "send size:%d", count);
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TakeCallItem cmd = getCommand();
		Siger siger = cmd.getUsername();
		
		// 查询结果
		TakeCallItemProduct product = new TakeCallItemProduct(siger);

		// 读取全部结果
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					TakeCallItemProduct e = getObject(TakeCallItemProduct.class, index);
					product.accede(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 反馈回AID站点
		boolean success = replyProduct(product);
		Logger.debug(this, "ending", success, "product size: %d", product.size());

		return useful(success);
	}

}
