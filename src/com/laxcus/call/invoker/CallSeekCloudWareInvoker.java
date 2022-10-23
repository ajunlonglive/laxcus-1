/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.call.pool.*;
import com.laxcus.command.cloud.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 检索云端应用。<br>
 * FRONT节点发出，CALL节点接受，分散给DATA/WORK/BUILD节点，本地检索INIT/BALANCE/ISSUE/ASSIGN组件、码位计算器。
 * 
 * @author scott.liang
 * @version 1.0 2/10/2020
 * @since laxcus 1.0
 */
public class CallSeekCloudWareInvoker extends CommonSeekCloudWareInvoker {

	/**
	 * 检索云端应用调用器，指定命令
	 * @param cmd 云端应用
	 */
	public CallSeekCloudWareInvoker(SeekCloudWare cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekCloudWare getCommand() {
		return (SeekCloudWare) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekCloudWare cmd = getCommand();

		// 取全部关联地址
		TreeSet<Node> sites = new TreeSet<Node>();
		Siger issuer = cmd.getIssuer();

		sites.addAll(findDataNodes(issuer));
		sites.addAll(findWorkNodes(issuer));
		sites.addAll(findBuildNodes(issuer));

		// 没有找到节点地址
		if (sites.isEmpty()) {
			SeekCloudWareProduct product = new SeekCloudWareProduct();
			CloudWareItem item = createItem();
			if (item.size() > 0) {
				product.add(item);
			}
			// 返回结果
			replyProduct(product);
			return useful(false);
		}

		// 以容错模式发送给关联节点
		int count = incompleteTo(sites, cmd);
		boolean success = (count > 0);
		if (!success) {
			replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekCloudWareProduct product = new SeekCloudWareProduct();
		List<Integer> keys = getEchoKeys();
		// 收集结果
		for (int index : keys) {
			if (isSuccessCompleted(index)) {
				try {
					SeekCloudWareProduct sub = getObject(
							SeekCloudWareProduct.class, index);
					product.addAll(sub);
				} catch (VisitException e) {
					Logger.error(e);
				}
			}
		}

		// 本地单元
		CloudWareItem item = createItem();
		// 保存单元
		if (item.size() > 0) {
			product.add(item);
		}

		// 返回结果
		replyProduct(product);

		// 退出
		return useful();
	}

	/**
	 * 找WORK节点
	 * @param issuer 用户签名
	 * @return
	 */
	private List<Node> findWorkNodes(Siger issuer) {
		return WorkOnCallPool.getInstance().findNodes(issuer);
	}

	/**
	 * 找BUILD节点
	 * @param issuer 用户签名
	 * @return
	 */
	private List<Node> findBuildNodes(Siger issuer) {
		return BuildOnCallPool.getInstance().findNodes(issuer);
	}

	/**
	 * 找DATA节点
	 * @param issuer 用户签名
	 * @return
	 */
	private List<Node> findDataNodes(Siger issuer) {
		return DataOnCallPool.getInstance().findNodes(issuer);
	}
	
	/**
	 * 生成单元
	 * @return
	 */
	private CloudWareItem createItem() {
		// 收集CALL节点上的应用，使用公网地址
		Node local = getPublicListener();
		if (local == null) {
			local = getLocal();
		}
		
		CloudWareItem item = new CloudWareItem(local);
		loadTasks(item);

		return item;
	}

	/**
	 * 导入组件命名到云应用包单元
	 * @param item 云应用包单元
	 */
	private void loadTasks(CloudWareItem item) {
		loadTasks(InitTaskPool.getInstance(), item);
		loadTasks(BalanceTaskPool.getInstance(), item);
		loadTasks(IssueTaskPool.getInstance(), item);
		loadTasks(AssignTaskPool.getInstance(), item);
		loadTasks(ForkTaskPool.getInstance(), item);
		loadTasks(MergeTaskPool.getInstance(), item);
	}

}