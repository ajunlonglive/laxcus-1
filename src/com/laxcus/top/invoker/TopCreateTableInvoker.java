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
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 建表命令调用器。<br><br>
 * 
 * 接受来自BANK站点的命令，根据签名，生成“AWARD CREATE TABLE”命令，发送给HOME站点。
 * 
 * @author scott.liang
 * @version 1.1 8/2/2012
 * @since laxcus 1.0
 */
public class TopCreateTableInvoker extends TopInvoker {

	/** 被发布到的HOME站点集合 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();

	/**
	 * 构造建表命令调用器，指定建表命令。
	 * @param cmd 建表命令
	 */
	public TopCreateTableInvoker(CreateTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateTable getCommand() {
		return (CreateTable) super.getCommand();
	}
	
	/**
	 * 返回表名
	 * @return
	 */
	private Space getSpace() {
		return getCommand().getSpace();
	}
	
	/**
	 * 向BANK站点反馈结果
	 * @param success
	 */
	private void reply(boolean success) {
		CreateTableProduct product = new CreateTableProduct(getSpace(), success);
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateTable cmd = getCommand();
		
		// 找到资源引用
		Siger siger = cmd.getIssuer();
		
		// 找到全部关联的HOME站点
		NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}

		// 发送给目标站点
		Table table = cmd.getTable();
		AwardCreateTable award = new AwardCreateTable(null, table);
		// 以容错模式发送给HOME站点
		int count = incompleteTo(slaves, award);
		boolean success = (count > 0);
		if (!success) {
			refuse();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		CreateTableProduct product = new CreateTableProduct(getSpace());

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					CreateTableProduct sub = getObject(CreateTableProduct.class, index);
					// 统计发送成功的HOME站点
					if (sub.isSuccessful()) {
						nodes.add(getBufferHub(index));
					}
					product.add(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 判断完成成功。条件：1. 发送和接收一致。2. 没有发生错误
		boolean success = (slaves.size() == nodes.size() && product.isSuccessful());
		
		Logger.debug(this, "ending", success, "right count:%d, failed count:%d", 
				product.getRights(), product.getFaults());

		// 以上成功，向BANK反馈处理结果
		if (success) {
			success = replyProduct(product);
		}
		
		// 以上不成功，通知已经建立的HOME站点删除表
		if (!success) {
			// 通知HOME站点删除数据表，不需要反馈
			CreateTable cmd = getCommand();
			AwardDropTable award = new AwardDropTable(cmd.getSpace());
			directTo(nodes, award);
			// 发送拒绝操作
			reply(false);
		}

		// 退出
		return useful(success);
	}

}