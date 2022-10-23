/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.gate.*;
import com.laxcus.gate.pool.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * GATE站点的异步命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public abstract class GateInvoker extends EchoInvoker {

	/**
	 * 构造GATE站点调用器，指定命令
	 * @param cmd 异步命令
	 */
	protected GateInvoker(Command cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public GateLauncher getLauncher() {
		return (GateLauncher) super.getLauncher();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommandPool()
	 */
	@Override
	public GateCommandPool getCommandPool() {
		return (GateCommandPool) super.getCommandPool();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getInvokerPool()
	 */
	@Override
	public GateInvokerPool getInvokerPool() {
		return (GateInvokerPool) super.getInvokerPool();
	}

	/**
	 * 向请求端发送一个拒绝通知
	 */
	protected void refuse() {
		replyFault(Major.FAULTED, Minor.REFUSE); 
	}

	/**
	 * 向请求端发送一个操作失败通知
	 */
	protected void failed() {
		replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
	}

	/**
	 * 定位HASH站点
	 * @param siger 用户签名
	 * @return 返回HASH站点
	 */
	protected Node locate(Siger siger) {
		return StaffOnGatePool.getInstance().locate(siger);
	}

	/**
	 * 返回BANK主机地址
	 * @return SiteHost实例
	 */
	public SiteHost getHubHost() {
		return getHub().getHost();
	}

	/**
	 * 从管理池中找到注册用户
	 * @return 返回用户，或者空指针
	 */
	protected User findUser() {
		Node from = getCommandSite();
		FrontSite site = (FrontSite) FrontOnGatePool.getInstance().find(from);
		return (site != null ? site.getUser() : null);
	}

	/**
	 * 找到在线用户的账号
	 * @return 返回账号实例，或者空指针
	 */
	protected Account findAccount(boolean duplicate) {
		User user = findUser();
		if (user != null) {
			return StaffOnGatePool.getInstance().findAccount(user, duplicate);
		}
		return null;
	}

	/**
	 * 找到在线用户的账号
	 * @return 返回账号实例，或者空指针
	 */
	protected Account findAccount() {
		return findAccount(false);
	}

	/**
	 * 判断调用器操作者是系统管理员
	 * @return 返回真或者假
	 */
	public boolean isAdministrator() {
		User user = findUser();
		boolean success = (user != null);
		if (success) {
			success = StaffOnGatePool.getInstance().isAdminstrator(user);
		}
		return success;
	}
	
	/**
	 * 拥有管理员身份
	 * @return
	 */
	public boolean isSameAdministrator() {
		User user = findUser();
		boolean success = (user != null);
		if (success) {
			// 不是系统管理员，即是注册用户了！
			success = (!StaffOnGatePool.getInstance().isAdminstrator(user));
			if (success) {
				success = StaffOnGatePool.getInstance().contains(user);
			}
			if (success) {
				Account account = StaffOnGatePool.getInstance().findAccount(user, false);
				success = (account != null && account.canDBA()); // 拥有管理员身份
			}
		}
		return success;
	}

	/**
	 * 判断调用器操作是普通的注册用户
	 * @return 返回真或者假
	 */
	public boolean isUser() {
		User user = findUser();
		boolean success = (user != null);
		if (success) {
			// 不是系统管理员，即是注册用户了！
			success = (!StaffOnGatePool.getInstance().isAdminstrator(user) && 
					StaffOnGatePool.getInstance().contains(user));
		}
		return success;
	}

	/**
	 * 判断操作者能不能执行系统管理员级别的操作
	 * @return 返回真或者假
	 */
	public boolean canDBA() {
		// 判断是系统管理员
		if (isAdministrator()) {
			return true;
		}
		// 找到用户账号，判断有管理员权限！
		Account account = findAccount();
		return (account != null && account.canDBA());
	}

	/**
	 * 判断有建立账号权限
	 * @return 返回真或者假
	 */
	public boolean canCreateUser() {
		// 判断是系统管理员
		if (isAdministrator()) {
			return true;
		}
		// 找到用户账号
		Account account = findAccount();
		// 判断拥有建立账号权力
		return (account != null && (account.canDBA() || account.canCreateUser()));
	}

	/**
	 * 判断有删除账号权限
	 * @return 返回真或者假
	 */
	public boolean canDropUser() {
		// 判断是系统管理员
		if (isAdministrator()) {
			return true;
		}
		// 找到用户账号
		Account account = findAccount();
		// 判断拥有DBA、删除账号权限
		return (account != null && (account.canDBA() || account.canDropUser()));
	}

	/**
	 * 判断有修改账号权限
	 * @return 返回真或者假
	 */
	public boolean canAlterUser() {
		// 判断是系统管理员
		if (isAdministrator()) {
			return true;
		}
		// 找到用户账号
		Account account = findAccount();
		// 判断拥有建立账号权力
		return (account != null && (account.canDBA() || account.canAlterUser()));
	}

	/**
	 * 判断操作者拥有建立数据库的权限。<br><br>
	 * 
	 * 两个限制：<br>
	 * 1.管理员不拥有建库权限。<br>
	 * 2. 用户必须得到授权后才有建库能力。<br>
	 * 
	 * @param fame 数据库名
	 * @return 返回真或者假
	 */
	public boolean canCreateSchema(Fame fame) {
		// 管理员不允许建立数据库
		if(isAdministrator()) {
			return false;
		}
		// 取用户账号
		Account account = findAccount();
		// 判断账号有效
		boolean success = (account != null);
		if (success) {
			success = account.canCreateSchema();
			if (!success) {
				success = account.canCreateSchema(fame);
			}
		}
		return success;
	}

	/**
	 * 判断操作者能够删除数据库。<br><br>
	 * 
	 * 限制条件：<br>
	 * 1. 管理员不能删除数据库。<br>
	 * 2. 用户拥有授权且是自己的数据库时，才能删除。<br><br>
	 * 
	 * @param fame 数据库名
	 * @return 返回真或者假
	 */
	public boolean canDropSchema(Fame fame) {
		// 管理员不允许建立数据库
		if(isAdministrator()) {
			return false;
		}
		// 取用户账号
		Account account = findAccount();
		// 判断账号有效
		boolean success = (account != null);
		if(success) {
			success = account.canDropSchema();
			if(!success) {
				success = account.canDropSchema(fame);
			}
		}
		return success;
	}

	/**
	 * 判断操作者拥有建立数据表的权限。<br><br>
	 * 
	 * 两个限制：<br>
	 * 1.管理员不拥有建表权限。<br>
	 * 2. 用户必须得到授权后才有建表能力。<br>
	 * 
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean canCreateTable(Space space) {
		// 管理员不允许建立数据表
		if(isAdministrator()) {
			return false;
		}
		// 取用户账号
		Account account = findAccount();
		// 判断账号有效
		boolean success = (account != null);
		if(success) {
			success = account.canCreateTable();
			if(!success) {
				success = account.canCreateTable(space);
			}
		}
		return success;
	}

	/**
	 * 判断可以独享计算机资源
	 * @return 返回真或者假
	 */
	public boolean canExclusive() {
		// 取用户账号
		Account account = findAccount();
		// 判断账号有效
		boolean success = (account != null);
		if (success) {
			success = account.canExclusive();
		}
		return success;
	}

	/**
	 * 判断操作者能够删除数据表。<br><br>
	 * 
	 * 限制条件：<br>
	 * 1. 管理员不能删除数据表。<br>
	 * 2. 用户拥有授权且是自己的数据表时，才能删除。<br><br>
	 * 
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean canDropTable(Space space) {
		// 管理员不允许建立数据表
		if(isAdministrator()) {
			return false;
		}
		// 取用户账号
		Account account = findAccount();
		// 判断账号有效
		boolean success = (account != null);
		if(success) {
			success = account.canDropTable();
			if(!success) {
				success = account.canDropTable(space);
			}
		}
		return success;
	}

	/**
	 * 判断有授权权限
	 * @return 返回真或者假
	 */
	public boolean canGrant() {
		// 管理员不允许建立数据库
		if (isAdministrator()) {
			return true;
		}
		// 取用户账号
		Account account = findAccount();
		return (account != null && (account.canDBA() || account.canGrant()));
	}

	/**
	 * 判断有撤销权限
	 * @return 返回真或者假
	 */
	public boolean canRevoke() {
		// 管理员不允许建立数据库
		if (isAdministrator()) {
			return true;
		}
		// 取用户账号
		Account account = findAccount();
		return (account != null && (account.canDBA() || account.canRevoke()));
	}

	/**
	 * 判断注册用户有发布分布任务组件，包括应用附件的权限。<br><br>
	 * 
	 * 说明：必须是注册用户自己发布！<br>
	 * 
	 * @return 返回真或者假
	 */
	public boolean canPublishTask() {
		// 取用户账号
		Account account = findAccount();
		return (account != null && account.canPublishTask());
	}

	/**
	 * 判断注册用户有发布分布任务组件动态链接库的权限。<br><br>
	 * 
	 * 说明：必须是注册用户自己发布<br>
	 * 
	 * @return 返回真或者假
	 */
	public boolean canPublishTaskLibrary() {
		// 取用户账号
		Account account = findAccount();
		// 判断许可
		return (account != null && account.canPublishTaskLibrary());
	}

//	/**
//	 * 判断当前用户有发布码位计算器权限。<br><br>
//	 * 
//	 * 注意，只判断注册用户自己！<br>
//	 * 
//	 * @return 返回真或者假
//	 */
//	public boolean canPublishScaler() {
//		// 取用户账号
//		Account account = findAccount();
//		return (account != null && account.canPublishScaler());
//	}
//
//	/**
//	 * 判断当前用户有发布码位计算器动态链接库权限。<br><br>
//	 * 
//	 * 注意，只判断注册用户自己！<br>
//	 * 
//	 * @return 返回真或者假
//	 */
//	public boolean canPublishScalerLibrary() {
//		// 取用户账号
//		Account account = findAccount();
//		return (account != null && account.canPublishScalerLibrary());
//	}

//	/**
//	 * 判断用户有发布快捷组件权限。<br><br>
//	 * 
//	 * 注意！只能是注册用户自己发布<br>
//	 * 
//	 * @param siger 用户签名
//	 * @return 返回真或者假
//	 */
//	public boolean canPublishSwift() {
//		// 取用户账号
//		Account account = findAccount();
//		return (account != null && account.canPublishSwift());
//	}
//
//	/**
//	 * 判断用户有发布快捷组件动态链接库权限。<br><br>
//	 * 
//	 * 注意！只能是注册用户自己发布<br>
//	 * 
//	 * @param siger 用户签名
//	 * @return 返回真或者假
//	 */
//	public boolean canPublishSwiftLibrary() {
//		// 取用户账号
//		Account account = findAccount();
//		return (account != null && account.canPublishSwiftLibrary());
//	}

	/**
	 * 判断操作者有开放数据资源的权限。<br><br>
	 * 
	 * 说明：只有注册用户自己且得到授权后，才能开放自己的数据资源。<br>
	 * 
	 * @param object 用户签名
	 * @return 返回真或者假
	 */
	public boolean canOpenResource() {
		// 管理员不允许操作
		if (isAdministrator()) {
			return false;
		}
		// 取用户账号
		Account account = findAccount();
		return (account != null && account.canOpenResource());
	}

	/**
	 * 判断操作者有开放数据资源的权限。<br><br>
	 * 
	 * 说明：只有注册用户自己且得到授权后，才能开放自己的数据资源。<br>
	 * 
	 * @param object 用户签名
	 * @return 返回真或者假
	 */
	public boolean canCloseResource() {
		// 管理员不允许操作
		if (isAdministrator()) {
			return false;
		}
		// 取用户账号
		Account account = findAccount();
		return (account != null && account.canCloseResource());
	}

}