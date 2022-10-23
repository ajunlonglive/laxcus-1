/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.command.site.gate.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.gate.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;

/**
 * FRONT站点注册调用器。<br>
 * 
 * 账号持有人登录到自己的所在GATE站点
 * 
 * @author scott.liang
 * @version 1.0 7/14/2018
 * @since laxcus 1.0
 */
public class GateFrontLoginInvoker extends GateSeekAccountSiteInvoker {

	/**
	 * 构造FRONT站点注册调用器，指定命令
	 * @param cmd FRONT站点注册
	 */
	public GateFrontLoginInvoker(FrontLogin cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FrontLogin getCommand() {
		return (FrontLogin) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FrontLogin cmd = getCommand();
		FrontSite front = cmd.getSite();
		User user = front.getUser();
		Node node = front.getNode();

//		Logger.debug(this, "launch", "%s ready ...", user.getUsername());

		// 账号持有人自己，分别：加载账号、加载关联的CALL站点，注册到管理池
		Siger authorizer = user.getUsername();
		boolean success = StaffOnGatePool.getInstance().loadAccount(user);
		// 加载账号不成功，账号很可能不存在非法，把账号保存到黑单名
		if (!success) {
			BlackOnGatePool.getInstance().add(user);
		}

		// 加载CALL站点
		if (success) {
			success = CallOnGatePool.getInstance().loadCallSites(authorizer);
			// 容错，如果没有找到CALL节点，GATE -> BANK -> WATCH 提示处理
			if (!success) {
				SiteMissing sub = new SiteMissing();
				sub.add(user.getUsername(), SiteTag.CALL_SITE);
				directToHub(sub);
			}
		}
		// 成功则保存FRONT账号到FRONT站点管理池
		if (success) {
			success = FrontOnGatePool.getInstance().add(front);
			// 冗余处理，之前可能因为登录失败，保存进入黑名单，这里要删除它们！
			BlackOnGatePool.getInstance().remove(user.getUsername());
			FaultOnGatePool.getInstance().remove(user);
		}
		// 以上不成功时，撤销
		if (!success) {
			// 删除注册地址
			FrontOnGatePool.getInstance().remove(node);

			boolean exists = (ConferrerFrontOnGatePool.getInstance().hasAuthorizer(authorizer) || 
					RuleHouse.getInstance().contains(authorizer));

			// 被授权人管理池和事务管理池没有签名时，删除账号记录
			if (!exists) {
				StaffOnGatePool.getInstance().drop(authorizer);
				CallOnGatePool.getInstance().remove(authorizer);
			}
		}
		
		// 如果不成功，并且黑名单里没有，加到登录故障名单里面去
		if (!success) {
			boolean exists = BlackOnGatePool.getInstance().contains(user);
			if (!exists) {
				 // 容错处理，登记失败但不属于黑名单的用户，先保存，用户再次查询时通知用户
				FaultOnGatePool.getInstance().add(user);
			}
		}

		// 无论是否成功，都删除驻留管理池的FRONT站点地址
		StayFrontOnGatePool.getInstance().remove(node);

//		Logger.debug(this, "launch", success, "%s", authorizer);

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

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		FrontLogin cmd = getCommand();
	//		FrontSite front = cmd.getSite();
	//
	//		// 判断是被授权人登录
	//		boolean success = front.isConferred();
	//		// 两种登录情况：1. 被授权人借助授权人账号登录 2. 授权人自己登录
	//		if (success) {
	//			success = conferrerLogin();
	//		} else {
	//			success = login();
	//		}
	//
	//		Logger.debug(this, "launch", success, "%s 被授权人身份：%s'", front.getUser(), front.isConferred());
	//
	//		return useful(success);
	//	}



	//	/**
	//	 * 以被授权人身份登录
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean conferrerLogin() {
	//		FrontLogin cmd = getCommand();
	//		FrontSite front = cmd.getSite();
	//
	//		// 授权人和被授权人
	//		Siger authorizer = front.getAuthorizer();
	//		Siger conferrer = front.getUsername();
	//
	//		// 授权人和被授权人
	//		boolean success = ConferrerStaffOnGatePool.getInstance().loadRefer(authorizer, conferrer); 
	//		if (success) {
	//			success = CallOnGatePool.getInstance().loadSites(authorizer);
	//		}
	//		if (success) {
	//			success = ConferrerFrontOnGatePool.getInstance().add(front);
	//		}
	//		// 以上成功，删除驻留管理池的FRONT地址
	//		if(success) {
	//			Node node = front.getNode();
	//			success = StayFrontOnGatePool.getInstance().remove(node);
	//		}
	//
	//		// 以上不成功，删除！
	//		if (!success) {
	//			ConferrerFrontOnGatePool.getInstance().remove(front.getNode(), authorizer);
	//			ConferrerStaffOnGatePool.getInstance().drop(conferrer);
	//			
	//			// 判断账号在这个三个位置存在！
	//			boolean exists = (RulePool.getInstance().contains(authorizer) || 
	//					FrontOnGatePool.getInstance().contains(authorizer));
	//
	//			// 授权人管理池和被授权人管理池都没有授权人记录，删除这个账号
	//			if(!exists) {
	//				StaffOnGatePool.getInstance().drop(authorizer);
	//				CallOnGatePool.getInstance().remove(authorizer);
	//			}
	//			
	////			// 授权人管理池中没有授权人记录，删除CALL站点
	////			if (!StaffOnGatePool.getInstance().contains(authorizer)) {
	////				CallOnGatePool.getInstance().remove(authorizer);
	////			}
	//		}
	//
	//		return success;
	//	}
	//
	//	/**
	//	 * 账号所有者登录
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean login() {
	//		FrontLogin cmd = getCommand();
	//		FrontSite front = cmd.getSite();
	//		User user = front.getUser();
	//
	//		// 账号持有人自己，分别：加载账号、加载关联的CALL站点，注册到管理池
	//		Siger authorizer = user.getUsername();
	//		boolean success = StaffOnGatePool.getInstance().loadAccount(user);
	//		if (success) {
	//			success = CallOnGatePool.getInstance().loadSites(authorizer);
	//		}
	//		if (success) {
	//			success = FrontOnGatePool.getInstance().add(front);
	//		}
	//		// 以上成功，删除驻留管理池的FRONT站点地址
	//		if(success) {
	//			Node node = front.getNode();
	//			success = StayFrontOnGatePool.getInstance().remove(node);
	//		}
	//
	//		// 以上不成功时，撤销
	//		if (!success) {
	//			FrontOnGatePool.getInstance().remove(front.getNode());
	//			
	//			boolean exists = (ConferrerFrontOnGatePool.getInstance().hasAuthorizer(authorizer) || 
	//					RulePool.getInstance().contains(authorizer));
	//			
	//			// 被授权人管理池和事务管理池没有签名时，删除账号记录
	//			if (!exists) {
	//				StaffOnGatePool.getInstance().drop(authorizer);
	//				CallOnGatePool.getInstance().remove(authorizer);
	//			}
	//			
	////			StaffOnGatePool.getInstance().drop(authorizer);
	////			// 被授权人管理池没有授权人记录，删除CALL站点
	////			if (!ConferrerFrontOnGatePool.getInstance().hasAuthorizer(authorizer)) {
	////				CallOnGatePool.getInstance().remove(authorizer);
	////			}
	//		}
	//		// 返回结果
	//		return success;
	//	}

	//	/**
	//	 * 测试故障!
	//	 * @param authorizer
	//	 * @param conferrer
	//	 * @return
	//	 */
	//	private boolean checkRefer(Siger authorizer, Siger conferrer) {
	//		Node remote = seekAccountSite(conferrer);
	//		boolean success = (remote != null);
	//		Logger.debug(this, "checkRefer", success, "被授权人：%s 的账号地址是：%s", conferrer, remote);
	//		
	//		remote = seekAccountSite(authorizer);
	//		success = (remote != null);
	//		Logger.debug(this, "checkRefer", success, "授权人：%s 的账号地址是：%s", authorizer, remote);
	//		
	//		return success;
	//	}
	//	
	//	/**
	//	 * 根据签名找到ACCOUNT站点
	//	 * @param siger 用户签名
	//	 * @return 返回HASH站点或者空指针
	//	 */
	//	protected Node seekAccountSite(Siger siger) {
	//		//  去HASH站点，找到账号关联的ACCOUNT站点地址
	//		TakeAccountSite cmd = new TakeAccountSite(siger);
	//		TakeAccountSiteHook hook = new TakeAccountSiteHook();
	//		TakeAccountSite shift = new TakeAccountSite(cmd, hook);
	//		boolean success = getCommandPool().admit(shift);
	//		if (!success) {
	//			Logger.error(this, "seekAccountSite", "cannot be press!");
	//			return null;
	//		}
	//		hook.await();
	//		
	//		// 返回ACCOUNT站点
	//		return hook.getRemote();
	//	}

	//	/**
	//	 * 测试故障!
	//	 * @param authorizer
	//	 * @param conferrer
	//	 * @return
	//	 */
	//	private void checkRefer(Siger authorizer, Siger conferrer) {
	//		seekAccountSite(conferrer);
	//		seekAccountSite(authorizer);
	//	}
	//
	//	/**
	//	 * 根据签名找到ACCOUNT站点
	//	 * @param siger 用户签名
	//	 * @return 返回HASH站点或者空指针
	//	 */
	//	protected void seekAccountSite(Siger siger) {
	//		//  去HASH站点，找到账号关联的ACCOUNT站点地址
	//		TakeAccountSite cmd = new TakeAccountSite(siger);
	//		TakeAccountSiteHook hook = new TakeAccountSiteHook();
	//		TakeAccountSite shift = new TakeAccountSite(cmd, hook);
	//		getCommandPool().admit(shift);
	//	}

}