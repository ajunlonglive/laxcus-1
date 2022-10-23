/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.task.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 获得分布组件标识命令调用器 <br>
 * 这个调用器将根据组件标识，向ACCOUNT站点检查关联的分布任务组件存在。<br><br>
 * 
 * 这是一个基础虚拟类，对各JOB类站点的分布任务组件有效且一致性的状态，由子类的“check”方法去实现。
 * 
 * @author scott.liang
 * @version 1.2 8/12/2015
 * @since laxcus 1.0
 */
public abstract class VirtualTakeTaskTagInvoker extends CommonInvoker {

	/**
	 * 构造获得分布组件标识命令调用器，指定命令
	 * @param cmd 获得分布组件标识命令
	 */
	protected VirtualTakeTaskTagInvoker(TakeTaskTag cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeTaskTag getCommand() {
		return (TakeTaskTag) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeTaskTag cmd = getCommand();
		Node remote = cmd.getRemote();
		
		// 站点地址
		List<Node> hubs = null;
		// 有指定目标地址，以它为准，否则取保存中的
		if (remote != null) {
			hubs = new ArrayList<Node>();
			hubs.add(remote);
		} else {
			// 是用户签名，查找关联的ACCOUNT站点集合；否则，获取全部站点
			if (cmd.isUserLevel()) {
				hubs = AccountOnCommonPool.getInstance().findSites(cmd.getTaskIssuer());
			} else {
				hubs = AccountOnCommonPool.getInstance().getHubs();
			}
		}

		// 判断有效
		boolean success = (hubs != null && hubs.size() > 0);
		// 以容错模式，发送到多个ACCOUNT地址
		if (success) {
			int count = incompleteTo(hubs, cmd);
			success = (count > 0);
		}

		Logger.debug(this, "launch", success, "check %s", cmd.getPart());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int count = 0;

		List<Integer> keys = getEchoKeys();
		try {
			for(int index : keys){	
				if (!isSuccessObjectable(index)) {
					continue;
				}
				TakeTaskTagProduct product = getObject(TakeTaskTagProduct.class, index);

				boolean success = check(index, product);
				if (success) count++;
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (count > 0);

		Logger.debug(this, "ending", success, "take %s", getCommand().getPart());

		return useful(success);
	}

	/**
	 * 检查组件存在，如果不存在，启动下载组件操作。
	 * @param index 缓存编号
	 * @param product 检查结果
	 * @return 成功返回真，否则假
	 */
	protected abstract boolean check(int index, TakeTaskTagProduct product);
}
