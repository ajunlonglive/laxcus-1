/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.home.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 建立账号调用器。<br><br>
 * 
 * TOP站点在此是中转作用，工作内容：<br>
 * 1. 检查HOME站点，生成“AWARD CREATE REFER”命令提交给它们，等待回应。<br>
 * 2. 接受HOME反馈结果，判断结果（成功或者否），向BANK反馈处理结果。如果不成功，直接删除HOME站点的记录。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/17/2018
 * @since laxcus 1.0
 */
public class TopCreateUserInvoker extends TopInvoker {

	/** 记录发布的HOME站点 **/
	private ArrayList<Node> slaves = new ArrayList<Node>();

	/**
	 * 创建建立账号调用器，指定账号命令
	 * @param cmd 建立账号命令
	 */
	public TopCreateUserInvoker(CreateUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateUser getCommand() {
		return (CreateUser) super.getCommand();
	}

	/**
	 * 返回账号签名
	 * @return 账号签名
	 */
	private Siger getSiger() {
		return getCommand().getUsername();
	}
	
	/**
	 * 向BANK站点反馈结果
	 * @param successful
	 */
	private void reply(boolean successful) {
		CreateUser cmd = getCommand();
		if (cmd.isDirect()) {
			return;
		}
		CreateUserProduct product = new CreateUserProduct(cmd.getUsername());
		product.setSuccessful(successful);
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		if (!isFromBank()) {
			refuse();
			return false;
		}

		// 从管理池中选择HOME子站点，并且保存它
		slaves.addAll(choice());

		ArrayList<CommandItem> array = new ArrayList<CommandItem>();

		// 给HOME站点发送资源引用
		CreateUser cmd = getCommand();
		User user = cmd.getUser();
		Refer refer = new Refer(user);
		AwardCreateRefer award = new AwardCreateRefer(refer);
		// 命令分配给每个站点
		for (Node node : slaves) {
			array.add(new CommandItem(node, award));
		}
		
		// 以容错模式向HOME站点发送命令
		int count = incompleteTo(array);
		// 只要有一个即成功
		boolean success = (count > 0);
		if (!success) {
			reply(false);
		}
		
		Logger.debug(this, "launch", success, "send count:%d, home sites:%d", count, slaves.size());

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CreateUserProduct product = new CreateUserProduct(getSiger());
		ArrayList<Node> nodes = new ArrayList<Node>();

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					CreateUserProduct sub = getObject(CreateUserProduct.class, index);
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
		
		// 成功，投递给BANK站点
		if (success) {
			success = replyProduct(product);
		}

		Logger.debug(this, "ending", success, "right count:%d, failed count:%d", 
				product.getRights(), product.getFaults());

		// 不成功，通知已经建立的站点删除它们
		if (!success) {
			// 建立命令，不需要反馈
			AwardDropRefer drop = new AwardDropRefer(getSiger());
			directTo(nodes, drop);
			// 向BANK反馈处理结果
			reply(success);
		}

		// 退出
		return useful(success);
	}

	/**
	 * 根据命令枚举HOME站点
	 * @param cmd 根据命令要求，选择合适的HOME站点
	 * @return 返回选择后的HOME站点
	 */
	private List<Node> choice() {
		CreateUser cmd = getCommand();
		TreeSet<Node> array = new TreeSet<Node>();

		// 判断HOME集合
		int groups = cmd.getUser().getGroups();
		NodeSet set = HomeOnTopPool.getInstance().list();
		int size = set.size();
		for (int i = 0; i < size; i++) {
			if (array.size() >= groups) {
				break;
			}
			Node node = set.next();
			HomeSite site = (HomeSite) HomeOnTopPool.getInstance().find(node);
			// 不存在，或者过滤非管理站点
			if (site == null || !site.isManager()) {
				continue;
			}
			array.add(node);
		}

		Logger.debug(this, "choice", "home size:%d, return size:%d,%d",
				set.size(), array.size(), groups);

		// 小于指定数目，返回空集合
		if (array.size() < groups) {
			return new ArrayList<Node>();
		}

		return new ArrayList<Node>(array);
	}

}