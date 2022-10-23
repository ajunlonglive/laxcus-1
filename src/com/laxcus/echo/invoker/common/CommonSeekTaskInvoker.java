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
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 检索分布任务组件站点调用器。<br>
 * 子类实现抽象方法。
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public abstract class CommonSeekTaskInvoker extends CommonInvoker {

	/**
	 * 构造检索分布任务组件站点调用器，指定命令
	 * @param cmd 检索分布任务组件站点
	 */
	protected CommonSeekTaskInvoker(SeekTask cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekTask getCommand() {
		return (SeekTask) super.getCommand();
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		SeekTaskProduct product = new SeekTaskProduct();
//
//		SeekTask cmd = getCommand();
//		Siger username = cmd.getUsername();
//		Naming root = cmd.getRoot();
//		
//		// 本地节点地址
//		Node local = getLocal();
//		
//		// 获得当前节点的阶段命名
//		List<Phase> array = obtain();
//
//		if (username != null && root != null) {
//			for (Phase e : array) {
//				if (Laxkit.compareTo(e.getIssuer(), username) == 0
//						&& Laxkit.compareTo(e.getRoot(), root) == 0) {
//					product.add(local, e);
//				}
//			}
//		} else if (username != null) {
//			for (Phase e : array) {
//				if (Laxkit.compareTo(e.getIssuer(), username) == 0) {
//					product.add(local, e);
//				}
//			}
//		} else if (root != null) {
//			for (Phase e : array) {
//				if (Laxkit.compareTo(e.getRoot(), root) == 0) {
//					product.add(local, e);
//				}
//			}
//		}
//		
//		// 反馈结果
//		boolean success = replyProduct(product);
//		
//		Logger.debug(this, "launch", success, "origin phase size:%d, check phase size:%d",
//			array.size(), product.size());
//
//		return useful(success);
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekTaskProduct product = new SeekTaskProduct();

		SeekTask cmd = getCommand();
		Siger username = cmd.getUsername();
		List<Sock> roots = cmd.list(); // cmd.getRoot();
		
		// 本地节点地址
		Node local = getLocal();
		
		// 获得当前节点的阶段命名
		List<Phase> array = obtain();

		if (username != null && roots.size() > 0) {
			for (Phase e : array) {
				for (Sock root : roots) {
					boolean success = (Laxkit.compareTo(e.getIssuer(), username) == 0 && 
							Laxkit.compareTo(e.getSock(), root) == 0);
					// 一致，保存它！
					if (success) {
						product.add(local, e);
					}
				}
			}
		} else if (username != null) {
			for (Phase e : array) {
				if (Laxkit.compareTo(e.getIssuer(), username) == 0) {
					product.add(local, e);
				}
			}
		} else if (roots.size() > 0) {
			for (Phase e : array) {
				for (Sock root : roots) {
					if (Laxkit.compareTo(e.getSock(), root) == 0) {
						product.add(local, e);
					}
				}
			}
		}
		
		// 反馈结果
		boolean success = replyProduct(product);
		
		Logger.debug(this, "launch", success, "origin phase size:%d, check phase size:%d",
			array.size(), product.size());

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

	/**
	 * 获得本地全部分布任务组件的阶段命名
	 * 
	 * @return 全部本地阶段命名
	 */
	protected abstract List<Phase> obtain();

}
