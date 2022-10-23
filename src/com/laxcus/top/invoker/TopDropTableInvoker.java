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
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 删表命令调用器。<br><br>
 * 
 * 根据签名，找到关联的HOME站点，发送给它们。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2018
 * @since laxcus 1.0
 */
public class TopDropTableInvoker extends TopInvoker {
	
	/** 记录HOME站点地址 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();

	/**
	 * 构造删表命令处理器
	 * @param cmd 删表命令
	 */
	public TopDropTableInvoker(DropTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropTable getCommand() {
		return (DropTable) super.getCommand();
	}

	/**
	 * 回复删表结果
	 * @param successful
	 * @return 发送成功返回真，否则假
	 */
	private boolean reply(boolean successful) {
		DropTable cmd = getCommand();
		// 单向处理，不需要反馈，退出！
		if (cmd.isDirect()) {
			return true;
		}
		DropTableProduct product = new DropTableProduct(cmd.getSpace(), successful);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.top.runner.EchoRunner#launch()
	 */
	@Override
	public boolean launch() {
		DropTable cmd = getCommand();
		Space space = cmd.getSpace();

		// 找到HOME站点
		NodeSet set = HomeOnTopPool.getInstance().find(space);
		if (set != null) {
			slaves.addAll(set.show());
		}

		// 转成授权删表命令，发送给HOME站点
		AwardDropTable award = new AwardDropTable(space);
		int count = incompleteTo(slaves, award);
		boolean success = (count > 0);

		// 失败，向请求端发送应答
		if (!success) {
			reply(false);
		}

		Logger.debug(this, "launch", success, "send '%s'", space);
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DropTable cmd = getCommand();
		Space space = cmd.getSpace();
		DropTableProduct product = new DropTableProduct(space);
		ArrayList<Node> nodes = new ArrayList<Node>();

		// 统计结果
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					DropTableProduct sub = getObject(DropTableProduct.class, index);
					// 统计删除成功的站点
					if(sub.isSuccessful()) {
						nodes.add(getBufferHub(index));
					}
					product.add(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 判断处理结果
		boolean success = (slaves.size() == nodes.size() && product.isSuccessful());

		// 向BANK反馈结果
		reply(success);

		Logger.note(this, "ending", success, "drop '%s'", space);
		
		// 退出
		return useful(success);
	}

}