/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 删除数据库命令调用器。<br><br>
 * 
 * HOME站点查找与签名关联的CALL/DATA/WORK/BUILD站点，把命令投递给它们。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2018
 * @since laxcus 1.0
 */
public class HomeAwardDropSchemaInvoker extends HomeInvoker {
	
	/** 记录HOME站点地址 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();

	/**
	 * 构造删除数据库命令处理器
	 * @param cmd 删除数据库命令
	 */
	public HomeAwardDropSchemaInvoker(AwardDropSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardDropSchema getCommand() {
		return (AwardDropSchema) super.getCommand();
	}

	/**
	 * 回复删表结果
	 * @param successful
	 * @return 发送成功返回真，否则假
	 */
	private boolean reply(boolean successful) {
		AwardDropSchema cmd = getCommand();
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
		AwardDropSchema cmd = getCommand();
		Fame fame = cmd.getFame();

		// 找到与数据库关联的全部工作站点
		askSites(fame);

		// 如果是空集合，反馈成功和退出
		if (slaves.isEmpty()) {
			reply(true);
			return useful();
		}

		// 转成授权删除数据库命令，发送给HOME站点
		int count = incompleteTo(slaves, cmd);
		boolean success = (count > 0);

		// 失败，向请求端发送应答
		if (!success) {
			reply(false);
		}

		Logger.debug(this, "launch", success, "send '%s'", fame);
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		AwardDropSchema cmd = getCommand();
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
		
		// 如果删除成功，同时清除内存中的记录
		if (success) {
			// 删除数据库及下属表
			StaffOnHomePool.getInstance().dropSchema(cmd.getFame());
			// 重新注册
			getLauncher().checkin(false);
		}

		// 向BANK反馈结果
		reply(success);

		Logger.note(this, "ending", success, "drop '%s'", cmd.getFame());
		
		// 退出
		return useful(success);
	}

	/**
	 * 根据关联数据库的全部工作站点
	 * @param fame 数据库名
	 */
	private void askSites(Fame fame) {
		// CALL站点记录
		List<Node> set = CallOnHomePool.getInstance().findSites(fame);
		if (set != null) {
			slaves.addAll(set);
		}
		// DATA站点记录
		set = DataOnHomePool.getInstance().findSites(fame);
		if (set != null) {
			slaves.addAll(set);
		}
		// WORK站点记录
		set = WorkOnHomePool.getInstance().findSites(fame);
		if (set != null) {
			slaves.addAll(set);
		}
		// BUILD站点记录
		set = BuildOnHomePool.getInstance().findSites(fame);
		if (set != null) {
			slaves.addAll(set);
		}
	}
}