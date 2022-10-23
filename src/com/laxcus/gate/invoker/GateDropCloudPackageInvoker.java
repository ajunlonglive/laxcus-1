/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.cloud.*;
import com.laxcus.echo.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 删除云端应用调用器。
 * GATE节点在此做为中继节点，接收FRONT/ACCOUNT的通信，向双方发送！
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public abstract class GateDropCloudPackageInvoker extends GateSeekAccountSiteInvoker {

	/** 阶段步骤，从1开始 **/
	private int step;

	/**
	 * 构造删除云端应用调用器，指定命令
	 * @param cmd 删除云端应用
	 */
	protected GateDropCloudPackageInvoker(DropCloudPackage cmd) {
		super(cmd);
		// 初始化1！
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropCloudPackage getCommand() {
		return (DropCloudPackage) super.getCommand();
	}

	/**
	 * 检查用户权限
	 * @return 允许返回真，否则假
	 */
	private boolean checkPermission() {
		DropCloudPackage cmd = getCommand();
		Node hub = cmd.getSourceSite();
		// 来自FRONT节点，必须有发布权限并且不是系统组件
		if (hub.isFront()) {
			// 判断有发布应用的权限， 同时也有删除权限
			boolean success = canPublishTask();
			if (success) {
				success = !cmd.isSystemWare();
			}
			return success;
		}
		// 其它条件不成立!
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断允许发布，如果不允许直接拒绝
		boolean success = checkPermission();
		Logger.debug(this, "launch", success, "check permission");
		
		if (!success) {
			replyFault(Major.FAULTED, Minor.CANNOT_PUBLISH);
			return false;
		}
		// 执行上传操作
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 执行上传操作
		return todo();
	}
	
	/**
	 * 分阶段执行删除操作！
	 * @return 成功返回真，失败返回假
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		}
		step++;

		// 判断结束
		if (!success || step > 3) {
			if (!success) {
				refuse();
			}
			setQuit(true);
		}
		return success;
	}
	
	/**
	 * 第一步：通过HASH站点，找ACCOUNT站点
	 * @return 命令发送成功返回真，否则假
	 */
	private boolean doFirst() {
		Siger issuer = getIssuer();
		return seekSite(issuer);
	}
	
	/**
	 * 第二阶段处理：接收ACCOUNT节点地址，向ACCOUNT节点发送命令
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		// 从HASH站点收到ACCOUNT站点地址
		Node node = replySite();
		// 判断地址有效
		boolean success = (node != null);
		// 发送命令给ACCOUNT节点
		if (success) {
			success = launchTo(node, getCommand());
		}
		return success;
	}
	
	/**
	 * 第三步，接收ACCOUNT的处理结果，反馈给FRONT节点。原样转发，不做处理！
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		// 强制更新资源
		refresh();
		
		// 返回结果
		return reflect();
	}

	/**
	 * 强制更新CALL节点记录
	 */
	private void refresh() {
		Siger issuer = getIssuer();
		// 判断有记录
		boolean success = CallOnGatePool.getInstance().contains(issuer);
		// 清除旧的，当FRONT再次请求时，内存时将重新加载!
		if (success) {
			CallOnGatePool.getInstance().remove(issuer);
		}
	}
}