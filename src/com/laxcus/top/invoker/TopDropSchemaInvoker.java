/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 删除数据库命令调用器。<br><br>
 * 
 * 根据签名，找到关联的HOME站点，发送给它们。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2018
 * @since laxcus 1.0
 */
public class TopDropSchemaInvoker extends TopInvoker {
	
	/** 记录HOME站点地址 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();

	/**
	 * 构造删除数据库命令处理器
	 * @param cmd 删除数据库命令
	 */
	public TopDropSchemaInvoker(DropSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropSchema getCommand() {
		return (DropSchema) super.getCommand();
	}

	/**
	 * 回复删表结果
	 * @param successful
	 * @return 发送成功返回真，否则假
	 */
	private boolean reply(boolean successful) {
		DropSchema cmd = getCommand();
		// 单向处理，不需要反馈，退出！
		if (cmd.isDirect()) {
			return true;
		}
		DropSchemaProduct product = new DropSchemaProduct(cmd.getFame(), successful);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.top.runner.EchoRunner#launch()
	 */
	@Override
	public boolean launch() {
		DropSchema cmd = getCommand();
		Siger siger = cmd.getIssuer();

		// 拿到全部HOME站点，向它们发送删除命令。
		// 这样的删除存在冗余，有些HOME集群可能没有。为了保证绝对清除，需要付出这样的成本。
		NodeSet set = HomeOnTopPool.getInstance().list();
		if (set != null) {
			slaves.addAll(set.show());
		}

		// 转成授权删除数据库命令，发送给HOME站点
		AwardDropSchema award = new AwardDropSchema(cmd.getFame());
		int count = incompleteTo(slaves, award);
		boolean success = (count > 0);

		// 失败，向请求端发送应答
		if (!success) {
			reply(false);
		}

		Logger.debug(this, "launch", success, "send '%s'", siger);
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DropSchema cmd = getCommand();
		DropSchemaProduct product = new DropSchemaProduct(cmd.getFame());
		ArrayList<Node> nodes = new ArrayList<Node>();

		// 统计结果
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					DropSchemaProduct sub = getObject(DropSchemaProduct.class, index);
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

		Logger.note(this, "ending", success, "drop '%s'", cmd.getFame());
		
		// 退出
		return useful(success);
	}

}