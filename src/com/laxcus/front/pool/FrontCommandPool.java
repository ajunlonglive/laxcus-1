/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.permit.*;
import com.laxcus.command.halt.*;
import com.laxcus.command.relate.*;
import com.laxcus.command.shutdown.Shutdown;
import com.laxcus.command.site.*;
import com.laxcus.command.site.entrance.*;
import com.laxcus.command.site.front.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.*;
import com.laxcus.front.invoker.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * FRONT命令管理池。
 * FRONT站点接受来自GATE/CALL站点的远程访问请求。
 * 
 * @author scott.liang
 * @version 1.1 12/19/2013
 * @since laxcus 1.0
 */
public abstract class FrontCommandPool extends CommandPool {

	/**
	 * 构造FRONT命令管理池
	 */
	protected FrontCommandPool() {
		super();
	}

	/**
	 * 返回异步调用器
	 * @return FrontInvokerPool实例
	 */
	protected FrontInvokerPool getInvokerPool() {
		FrontLauncher launcher = (FrontLauncher) super.getLauncher();
		return (FrontInvokerPool) launcher.getInvokerPool();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#accept(com.laxcus.command.Command)
	 */
	@Override
	public boolean accept(Command cmd) {
		// 这两种情况拒绝受理
		if (cmd == null ||  cmd.getSource() == null) {
			Logger.error(this, "accept", "this is null command, or listener is null");
			return false;
		}

		// 判断是来自上级注册站点的命令
		Cabin cabin = cmd.getSource();
		Node node = cabin.getNode();

		// 判断来自注册GATE站点地址
		boolean success = (Laxkit.compareTo(node, getHub()) == 0);
		// 是注册的CALL站点
		if (!success) {
			success = CallOnFrontPool.getInstance().contains(node);
		}
		// 判断是被授权站点
		if (!success) {
			success = AuthroizerGateOnFrontPool.getInstance().contains(node);
		}
		// 是切换站点
		if (!success) {
			success = isSwitchHub(cmd);
		}
		
		// 保存地址
		if (success) {
			success = add(cmd);
		}
		
		// 成功，保存它！
		if (success) {
			Tigger.command(cmd);
		}

		Logger.note(this, "accept", success, "from %s - \'%s\'", cabin, cmd);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		if (cmd.getClass() == SwitchHub.class) {
			invoker = new FrontSwitchHubInvoker((SwitchHub) cmd);
		} else if (cmd.getClass() == Halt.class) {
			invoker = new FrontHaltInvoker((Halt) cmd);
		} else if (cmd.getClass() == Shutdown.class) {
			invoker = new FrontShutdownInvoker((Shutdown) cmd);
		}
		// 登录后加载分布资源
		else if (cmd.getClass() == LoadSchedule.class) {
			invoker = new FrontLoadScheduleInvoker((LoadSchedule) cmd);
		}
		// 定时更新网络资源
		else if (cmd.getClass() == RefreshSchedule.class) {
			invoker = new FrontRefreshScheduleInvoker((RefreshSchedule) cmd);
		}

		boolean success = (invoker != null);
		if (success) {
			success = getInvokerPool().launch(invoker);
		}
		return success;
	}

	/**
	 * 子类“press”方法调用的登录命令
	 * @param cmd 命令
	 * @return 成功返回调用器，否则是空指针
	 */
	protected EchoInvoker createLoginInvoker(Command cmd) {
		EchoInvoker invoker = null;
		
		// 登录转发命令
		if (cmd.getClass() == ShiftTakeAccount.class) {
			invoker = new FrontShiftTakeAccountInvoker((ShiftTakeAccount) cmd);
		} else if (cmd.getClass() == ShiftTakeAuthorizerSite.class) {
			invoker = new FrontShiftTakeAuthorizerSiteInvoker((ShiftTakeAuthorizerSite) cmd);
		} else if (cmd.getClass() == ShiftTakeOwnerCall.class) {
			invoker = new FrontShiftTakeOwnerCallInvoker((ShiftTakeOwnerCall) cmd);
		} else if (cmd.getClass() == ShiftTakeGrade.class) {
			invoker = new FrontShiftTakeGradeInvoker((ShiftTakeGrade) cmd);
		} else if (cmd.getClass() == ShiftTakeAuthorizerCall.class) {
			invoker = new FrontShiftTakeAuthorizerCallInvoker((ShiftTakeAuthorizerCall) cmd);
		} else if (cmd.getClass() == ShiftTakeAuthorizerTable.class) {
			invoker = new FrontShiftTakeAuthorizerTableInvoker((ShiftTakeAuthorizerTable) cmd);
		}

		return invoker;
	}
}