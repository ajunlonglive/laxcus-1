/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.top.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.set.*;

/**
 * TOP站点的异步命令调用器，只限TOP站点的命令使用。<br>
 * 
 * @author scott.liang
 * @version 1.2 3/23/2012
 * @since laxcus 1.0
 */
public abstract class TopInvoker extends EchoInvoker {
	
	/**
	 * 构造异步命令调用器，指定异步命令
	 * @param cmd 异步命令
	 */
	protected TopInvoker(Command cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public TopLauncher getLauncher() {
		return (TopLauncher) super.getLauncher();
	}

	/**
	 * 判断是管理站点
	 * @return - 返回真或者假
	 */
	public boolean isManager() {
		TopLauncher launcher = getLauncher();
		return launcher.isManager();
	}

	/**
	 * 判断是监视站点
	 * @return - 返回真或者假
	 */
	public boolean isMonitor() {
		TopLauncher launcher = getLauncher();
		return launcher.isMonitor();
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
	 * 没有找到
	 * @return 发送成功返回真，否则假
	 */
	protected boolean notfound() {
		return replyFault(Major.FAULTED, Minor.NOTFOUND);
	}

	/**
	 * 找到指定数目的HOME站点，向它们发送异步命令
	 * @param size 指定HOME站点数目
	 * @return HOME站点数目满足，且全部HOME站点接受时，返回“真”；否则“假”。
	 */
	protected boolean launchToSlaves(int size) {
		NodeSet set = HomeOnTopPool.getInstance().list();
		List<Node> array = set.show(); // 锁定获取
		if (array.size() < size) {
			return false;
		}

		Node[] slaves = new Node[size];
		for (int i = 0; i < size; i++) {
			slaves[i] = array.get(i);
		}
		return super.launchTo(slaves);
	}

	/**
	 * 找到全部HOME站点，向它们发送异步命令
	 * @return 找到全部HOME站点，且全部HOME站点接受时，返回“真”；否则“假”。
	 */
	protected boolean launchToSlaves() {
		NodeSet set = HomeOnTopPool.getInstance().list();
		List<Node> array = set.show();
		if (array.size() == 0) {
			return false;
		}
		return this.launchTo(array);
	}

	
	/**
	 * 检查是来自HOME站点的命令。只有在HOME管理池注册才有效。
	 * @return 通过返回“真”，否则“假”。
	 */
	protected boolean isLegalHome() {
		Command cmd = super.getCommand();
		Node node = cmd.getSource().getNode();
		// 1. 判断注册
		boolean success = HomeOnTopPool.getInstance().contains(node);
		// 2. 判断是HOME
		if (success) {
			success = node.isHome();
		}
		return success;
	}
	

}