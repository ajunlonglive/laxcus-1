/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 删除用户资源引用调用器。<br>
 * 这个命令由HOME发来，DATA站点清除本地全部资源，包括分布组件
 * 
 * @author scott.liang
 * @version 1.0 07/18/2012
 * @since laxcus 1.0
 */
public class DataAwardDropReferInvoker extends DataInvoker {

	/**
	 * 构造删除用户资源引用调用器，指定命令
	 * @param cmd 删除用户资源引用命令
	 */
	public DataAwardDropReferInvoker(AwardDropRefer cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardDropRefer getCommand() {
		return (AwardDropRefer) super.getCommand();
	}

	/**
	 * 反馈删除结果
	 * @param successful 删除成功标识
	 */
	private boolean reply(boolean successful) {
		AwardDropRefer cmd = getCommand();
		if (cmd.isDirect()) {
			return true;
		}
		// 被删除的账号
		Siger siger = cmd.getUsername();
		// 反馈到HOME站点
		DropUserProduct product = new DropUserProduct(siger, successful);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardDropRefer cmd = getCommand();
		Siger siger = cmd.getUsername();

		// 删除账号以及账号下的全部表空间记录
		boolean success = StaffOnDataPool.getInstance().drop(siger);
		// 成功
		if (success) {
			// 删除分布组件
			StaffOnDataPool.getInstance().dropTask(siger);
			
//			// 删除码位计算器
//			StaffOnDataPool.getInstance().dropScaler(siger);
			
			// 删除ARCHIVE站点池中的用户签名
			AccountOnCommonPool.getInstance().remove(siger);
			// 重新注册
			getLauncher().checkin(false);
			// 投递通知给WATCH节点
			castToWatch(siger);
		}
		
		// 发送反馈给HOME站点
		boolean sended = reply(success);

		Logger.debug(this, "launch", sended, "drop '%s'", siger);

		return useful(sended);
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
	 * 建立删除注册成员命令，投递给HOME站点
	 * @param siger 用户签名
	 */
	private void castToWatch(Siger siger) {
		Seat seat = new Seat(siger, getLocal());
		DropRegisterMember sub = new DropRegisterMember(seat);
		directToHub(sub);
	}

}