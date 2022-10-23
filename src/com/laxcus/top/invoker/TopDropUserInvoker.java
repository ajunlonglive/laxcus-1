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
import com.laxcus.command.refer.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 删除用户账号调用器。
 * 
 * 命令来自BANK站点，TOP找到签名相关的HOME站点，把命令转发给它们。
 * 返回删除的资源数目，并且说明是否绝对成功。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class TopDropUserInvoker extends TopInvoker {
	
	/** 记录HOME站点地址 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();

	/**
	 * 构造删除用户账号调用器，指定命令
	 * @param cmd 删除用户账号
	 */
	public TopDropUserInvoker(DropUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropUser getCommand() {
		return (DropUser) super.getCommand();
	}

	/**
	 * 反馈删除结果
	 * @param successful 删除成功标识
	 */
	private boolean reply(boolean successful) {
		DropUser cmd = getCommand();
		if (cmd.isDirect()) {
			return true;
		}
		// 被删除的账号
		Siger siger = cmd.getUsername();
		// 反馈到BANK站点
		DropUserProduct product = new DropUserProduct(siger, successful);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropUser cmd = getCommand();
		Siger siger = cmd.getUsername();

		// 找到与账号关联的HOME站点
		NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}
		// 空集合
		if (slaves.isEmpty()) {
			reply(false);
			return useful();
		}

		// 授权删除账号
		AwardDropRefer award = new AwardDropRefer(siger);

		// 以容错模式发送命令到HOME站点
		int count = incompleteTo(slaves, award);
		boolean success = (count > 0);
		// 不成功，反馈
		if (!success) {
			reply(false);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DropUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		DropUserProduct product = new DropUserProduct(siger);
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		List<Integer> keys = getEchoKeys();
		for(int index : keys) {
			try {
				if(isSuccessObjectable(index)) {
					DropUserProduct sub = getObject(DropUserProduct.class, index);
					if (sub.isSuccessful()) {
						nodes.add(getBufferHub(index));
					}
					product.add(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断成功（如果不成功，存在残余数据，这是个问题）
		boolean success = (slaves.size() == nodes.size() && product.isSuccessful());

		// 反馈给BANK
		if (success) {
			replyProduct(product);
		} else {
			reply(false);
		}

		// 退出
		return useful(success);
	}

}
