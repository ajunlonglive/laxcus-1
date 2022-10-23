/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.build.pool.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.task.establish.sift.*;
import com.laxcus.util.*;

/**
 * 建立用户资源引用调用器。<br>
 * 这个命令由HOME发来，WORK在管理池中保存，向ACCOUNT站点申请加载分布组件。
 * 
 * @author scott.liang
 * @version 1.0 07/18/2012
 * @since laxcus 1.0
 */
public class BuildAwardCreateReferInvoker extends BuildInvoker {

	/**
	 * 构造建立用户资源引用调用器，指定命令
	 * @param cmd 建立用户资源引用命令
	 */
	public BuildAwardCreateReferInvoker(AwardCreateRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardCreateRefer getCommand() {
		return (AwardCreateRefer) super.getCommand();
	}

	/**
	 * 向HOME反馈结果
	 * @param success 成功或者否
	 * @return 发送成功返回真，否则假
	 */
	private boolean reply(boolean success) {
		AwardCreateRefer cmd = getCommand();
		if (cmd.isDirect()) {
			return true;
		}
		CreateUserProduct product = new CreateUserProduct(cmd.getUsername(), success);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardCreateRefer cmd = getCommand();
		Refer refer = cmd.getRefer();
		Siger siger = refer.getUsername();

		// 建立账号
		boolean created = StaffOnBuildPool.getInstance().create(refer);
		// 成功
		if (created) {
			allocate(siger);
		}

		// 发送反馈给HOME站点
		boolean sended = reply(created);

		// 判断成功
		boolean success = (created && sended);
		// 延时触发注册，或者取消！
		if (success) {
			getLauncher().checkin(false);
			// 投递给WATCH节点
			castToWatch(siger);
		} else {
			cancel(siger);
		}


		Logger.debug(this, "launch", success, "create %s", siger);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 分配与用户签名关联的ACCOUNT站点地址、分布任务组件和码位计算器组件。
	 * @param issuer 用户签名
	 */
	private void allocate(Siger issuer) {
		//		// 判断没有分配ACCOUNT站点
		//		boolean empty = AccountOnCommonPool.getInstance().isEmpty();

		// 去HOME站点获取ACCOUNT站点（以同步方式获取）
		boolean success = AccountOnCommonPool.getInstance().load(issuer);
		if(!success) {
			Logger.error(this, "allocate", "cannot find account site");
			return ;
		}
		// 如果SIFT系统组件不存在，加载系统级分布任务组件
		if(!hasSystemTasks()) {
			StaffOnBuildPool.getInstance().loadTask(null);
		}
		// 加载用户级分布任务组件，和码位计算器组件（码位计算器组件只有用户层组件，没有系统层组件）
		StaffOnBuildPool.getInstance().loadTask(issuer);
//		StaffOnBuildPool.getInstance().loadScaler(issuer);
	}

	/**
	 * 取消与用户签名相关的分布组件和ACCOUNT站点关联
	 * @param issuer 用户签名
	 */
	private void cancel(Siger issuer) {
		// 删除分布任务组件
		StaffOnBuildPool.getInstance().dropTask(issuer);
		// 删除ACCOUNT管理池中的签名
		AccountOnCommonPool.getInstance().remove(issuer);
		// 删除账号资源
		StaffOnBuildPool.getInstance().drop(issuer);
	}

	/**
	 * 判断有系统级分布任务组件
	 * @return 返回真或者假
	 */
	private boolean hasSystemTasks() {
		return SiftTaskPool.getInstance().hasSystemTask();
	}

	/**
	 * 投递给HOME站点
	 * @param siger 用户签名
	 */
	private void castToWatch(Siger siger) {
		Seat seat = new Seat(siger, getLocal());
		// 查找引用
		Refer refer = StaffOnBuildPool.getInstance().findRefer(siger);
		if (refer != null) {
			seat.setPlainText(refer.getUser().getPlainText());
		}
		// 投递命令
		PushRegisterMember sub = new PushRegisterMember(seat);
		directToHub(sub);
	}

}