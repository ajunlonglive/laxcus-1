/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.shutdown.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.home.*;
import com.laxcus.home.pool.*;

/**
 * HOME站点远程关闭调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 9/8/2019
 * @since laxcus 1.0
 */
public class HomeShutdownInvoker extends HubShutdownInvoker {

	/**
	 * 构造HOME站点远程关闭调用器，指定命令
	 * @param cmd 站点远程关闭命令
	 */
	public HomeShutdownInvoker(Shutdown cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubShutdownInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成子站点
		HomeLauncher launcher = (HomeLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 取出HOME节点下面的全部运行站点
		array.addAll(MonitorOnHomePool.getInstance().detail());
		array.addAll(CallOnHomePool.getInstance().detail());
		array.addAll(DataOnHomePool.getInstance().detail());
		array.addAll(BuildOnHomePool.getInstance().detail());
		array.addAll(WorkOnHomePool.getInstance().detail());
		array.addAll(LogOnHomePool.getInstance().detail());

		return array;
	}

}

///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license GNU Lesser General Public License (LGPL)
// */
//package com.laxcus.home.invoker;
//
//import java.util.*;
//
//import com.laxcus.command.shutdown.*;
//import com.laxcus.echo.*;
//import com.laxcus.echo.invoke.*;
//import com.laxcus.home.pool.*;
//import com.laxcus.log.client.*;
//import com.laxcus.site.*;
//import com.laxcus.util.*;
//import com.laxcus.visit.*;
//
///**
// * 远程关闭命令调用器 <br>
// * HOME站点的SHUTDOWN命令分别来自TOP/WATCH两个站点，其它情况将忽略
// * 
// * @author scott.liang
// * @version 1.0 09/12/2015
// * @since laxcus 1.0
// */
//public class HomeShutdownInvoker extends HomeInvoker {
//
//	/**
//	 * 构造远程关闭调用器，指定命令
//	 * @param cmd SHUTDOWN命令
//	 */
//	public HomeShutdownInvoker(Shutdown cmd) {
//		super(cmd);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
//	 */
//	@Override
//	public Shutdown getCommand() {
//		return (Shutdown) super.getCommand();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		Cabin cabin = getCommandSource();
//		Node node = cabin.getNode();
//		Node hub = getHub();
//
//		Logger.debug(this, "launch",  "from %s", cabin);
//
//		boolean success = (node.isHome() && isMonitor() && Laxkit.compareTo(node, hub) == 0);
//		// 只需要关闭自己，然后退出
//		if (success) {
//			success = shutdown();
//			setQuit(true);
//		} else {
//			// 判断来自TOP/WATCH站点，关闭全部
//			success = ((node.isTop() && Laxkit.compareTo(node, hub) == 0) || 
//					(node.isWatch() && WatchOnHomePool.getInstance().contains(node)));
//			// 找到下属站点，全部关闭
//			if (success) {
//				List<Node> sites = fetchSubSites();
//				if(sites.isEmpty()) {
//					success = shutdown();
//					setQuit(true);
//				} else {
//					success = sendAll(sites);
//				}
//			}
//		}
//
//		// 拒绝
//		if (!success) {
//			refuse();
//		}
//
//		return success;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
//	 */
//	@Override
//	public boolean ending() {
//		List<Integer> keys = getEchoKeys();
//		ShutdownProduct product = new ShutdownProduct();
//
//		for (int index : keys) {
//			try {
//				if (isSuccessObjectable(index)) {
//					ShutdownProduct e = getObject(ShutdownProduct.class, index);
//					product.addAll(e.list());
//				}
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//
//		// 本地监听地址
//		product.add(getLocal());
//
//		// 发送给来源站点
//		boolean success = replyProduct(product);
//		// 关闭本地站点
//		if (success) {
//			delay(5000); // 延时5秒
//			getLauncher().stop();
//		}
//
//		Logger.debug(this, "ending", success, "site is %d", product.size());
//
//		return useful(success);
//	}
//
//	/**
//	 * 获得全部子站点
//	 * @return 子站点地址列表
//	 */
//	private List<Node> fetchSubSites() {
//		ArrayList<Node> sites = new ArrayList<Node>();
//		sites.addAll(CallOnHomePool.getInstance().detail());
//		sites.addAll(DataOnHomePool.getInstance().detail());
//		sites.addAll(BuildOnHomePool.getInstance().detail());
//		sites.addAll(WorkOnHomePool.getInstance().detail());
//		sites.addAll(WatchOnHomePool.getInstance().detail());
//		sites.addAll(MonitorOnHomePool.getInstance().detail());
//		sites.addAll(LogOnHomePool.getInstance().detail());
//		// 删除来源WATCH站点
//		Cabin cabin = getCommandSource();
//		Node from = cabin.getNode();
//		sites.remove(from);
//		return sites;
//	}
//
//	/**
//	 * 发送到子站点
//	 * @param sites
//	 * @return 成功返回真，否则假
//	 */
//	private boolean sendAll(List<Node> sites) {
//		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
//		Shutdown cmd = new Shutdown();
//		cmd.setQuick(true); // 要求快速处理
//		for (Node sub : sites) {
//			CommandItem item = new CommandItem(sub, cmd);
//			array.add(item);
//		}
//		// 以容错模式，发送到指定站点
//		int count = incompleteTo(array);
//		return (count > 0);
//	}
//
//	/**
//	 * 关闭本地站点
//	 * @return
//	 */
//	private boolean shutdown() {
//		// 向来源发送一个处理结果
//		Node local = getLocal();
//		ShutdownProduct product = new ShutdownProduct(local);
//		boolean success = replyProduct(product);
//		// 关闭本地站点
//		if (success) {
//			delay(5000); // 延时5秒
//			getLauncher().stop();
//		}
//		return success;
//	}
//
//}
