/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.refer.*;
import com.laxcus.top.pool.*;
import com.laxcus.site.*;

/**
 * 获得账号资源引用调用器。<br>
 * 命令来自HOME站点，TOP将命令转发给BANK站点。
 * 
 * @author scott.liang
 * @version 1.0 7/27/2017
 * @since laxcus 1.0
 */
public class TopTakeReferInvoker extends TopInvoker {

	/**
	 * 构造获得账号资源引用调用器，指定命令
	 * @param cmd 获得资源引用
	 */
	public TopTakeReferInvoker(TakeRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeRefer getCommand() {
		return (TakeRefer) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Node slave = BankOnTopPool.getInstance().getManagerSite();
		boolean success = (slave != null);
		if (success) {
			TakeRefer cmd = getCommand();
			success = launchTo(slave, cmd);
		}
		if (!success) {
			failed();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return reflect(); // 反馈结果给HOME站点
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		TakeRefer cmd = getCommand();
	//		Siger conferrer = cmd.getSiger();
	//
	//		// 找到资源数据
	//		Refer refer = DictPool.getInstance().createRefer(conferrer);
	//		boolean success = (refer != null);
	//		if (success) {
	//			TakeReferProduct product = new TakeReferProduct(refer);
	//			success = replyProduct(product);
	//		} else {
	//			replyFault();
	//		}
	//
	//		// 退出
	//		return useful(success);
	//	}



}