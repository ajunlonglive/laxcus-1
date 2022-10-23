/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.refer.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;

/**
 * 构造刷新资源引用调用器。<br>
 * HOME站点将命令投递给关联的HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class HomeRefreshReferInvoker extends HomeInvoker {

	/**
	 * 构造刷新资源引用调用器，指定命令
	 * @param cmd 刷新资源引用
	 */
	public HomeRefreshReferInvoker(RefreshRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshRefer getCommand() {
		return (RefreshRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshRefer cmd = getCommand();
		Siger siger = cmd.getSiger();

		// 如果没能这个签名则忽略
		boolean success = StaffOnHomePool.getInstance().contains(siger);
		if (!success) {
			return useful(false);
		}

		// ACCOUNT站点
		Node account = cmd.getLocal();

		// 保存签名和ACCOUNT站点关联
		if (siger != null) {
			AccountOnCommonPool.getInstance().add(siger, account);
		}

		// 去ACCOUNT站点获取账号
		TakeRefer sub = new TakeRefer(siger);
		// 命令发达到ACCOUNT站点
		success = launchTo(account, sub);
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		Refer refer = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				TakeReferProduct product = getObject(TakeReferProduct.class, index);
				refer = product.getRefer();
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		// 判断成功 
		boolean success = (refer != null);
		// 判断账号有效
		if (success) {
			// 保存到HOME资源管理池
			StaffOnHomePool.getInstance().create(refer);
			// 广播给CALL站点
			multicast(refer);
		}
		
		Logger.debug(this, "ending", success, "set refer %s", getCommand());

		return useful(success);
	}

	/**
	 * 把资源引用投递给CALL/WORK/BUID站点
	 * @param refer 资源引用
	 */
	private void multicast(Refer refer) {
		// 查找CALL站点，修改配置
		Siger siger = refer.getUsername();
		
		// 收集关联的CALL、WORK、BUILD站点，DATA节点保存相关的授权人/被授权人信息
		ArrayList<Node> slaves = new ArrayList<Node>();
		// call site
		NodeSet set = CallOnHomePool.getInstance().findSites(siger);
		if (set != null) slaves.addAll(set.show());
		// work site
		set = WorkOnHomePool.getInstance().findSites(siger);
		if (set != null) slaves.addAll(set.show());
		// build site
		set = BuildOnHomePool.getInstance().findSites(siger);
		if (set != null) slaves.addAll(set.show());
		// data site
		set = DataOnHomePool.getInstance().findSites(siger);
		if (set != null) slaves.addAll(set.show());

		// 以容错模式投递给全部站点
		if (slaves.size() > 0) {
			SetRefer cmd = new SetRefer(refer);
			directTo(slaves, cmd, false);
		}
	}

}
