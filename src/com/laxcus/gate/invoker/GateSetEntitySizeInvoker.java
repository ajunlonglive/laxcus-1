/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.fast.*;

/**
 * 修改数据块尺寸命令调用器。
 * 更新数据块尺寸
 * 
 * @author scott.liang
 * @version 1.0 7/7/2018
 * @since laxcus 1.0
 */
public class GateSetEntitySizeInvoker extends GateSelfOperateInvoker {

	/**
	 * 建立修改数据块尺寸命令调用器
	 * @param cmd 修改数据块尺寸命令
	 */
	public GateSetEntitySizeInvoker(SetEntitySize cmd) {
		super(cmd);
		setRefresh(true);
		setMulticast(true);
	}

	//	/** 操作步骤，从1开始 **/
	//	private int step;
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		return todo();
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	//	 */
	//	@Override
	//	public boolean ending() {
	//		return todo();
	//	}
	//	
	//	/**
	//	 * 按照顺序，执行分布调用操作
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean todo() {
	//		boolean success = false;
	//		switch (step) {
	//		case 1:
	//			success = doFirst();
	//			break;
	//		case 2:
	//			success = doSecond();
	//			break;
	//		case 3:
	//			success = doThird();
	//			break;
	//		}
	//		step++;
	//		// 不成功，或者步骤完成，退出！
	//		if (!success || step > 3) {
	//			if (!success) {
	//				failed();
	//			}
	//			setQuit(true);
	//		}
	//		return success;
	//	}
	//	
	//	/**
	//	 * 第一步：去HASH站点查找ACCOUNT站点
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean doFirst() {
	//		Siger siger = getIssuer();
	//		return super.seekSite(siger);
	//	}
	//	
	//	/**
	//	 * 第二步：把命令投递给ACCOUNT站点
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean doSecond() {
	//		Node account = super.replySite();
	//		boolean success = (account!=null);
	//		if(success) {
	//			Command cmd = getCommand();
	//			success = super.launchTo(account, cmd);
	//		}
	//		return success;
	//	}
	//	
	//	/**
	//	 * 返回是终结果
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean doThird() {
	//		return reflect();
	//	}

}