/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 删除用户资源引用调用器。
 * 
 * @author scott.liang
 * @version 1.1 9/01/2013
 * @since laxcus 1.0
 */
public class HomeAwardDropReferInvoker extends HomeInvoker {

	/** 被记录的下属站点地址 **/
	private TreeSet<Node> slaves = new TreeSet<Node>(); 

	/**
	 * 构造删除用户资源引用调用器，指定命令
	 * @param cmd 删除用户资源引用
	 */
	public HomeAwardDropReferInvoker(AwardDropRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardDropRefer getCommand() {
		return (AwardDropRefer) super.getCommand();
	}

	/**
	 * 反馈删除结果
	 * @param successful 删除成功标识
	 */
	private boolean reply(boolean successful) {
		AwardDropRefer cmd = getCommand();
		if (cmd.isDirect()) {
			return true;
		}
		// 被删除的账号
		Siger siger = cmd.getUsername();
		// 反馈到BANK站点
		DropUserProduct product = new DropUserProduct(siger, successful);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardDropRefer cmd = getCommand();
		Siger siger = cmd.getUsername();

		// 扫描下级工作站点
		scan(siger);
		// 没有，返回
		if (slaves.isEmpty()) {
			reply(true);
			return useful();
		}

		// 以容错模式发送
		int count = incompleteTo(slaves, cmd);
		boolean success = (count > 0);
		// 不成功，反馈
		if (!success) {
			reply(false);
		}

		Logger.debug(this, "launch", success, "sub site is:%d", count);

		//		// 判断账号存在
		//		boolean contains = StaffOnHomePool.getInstance().contains(siger);
		//
		//		Logger.debug(this, "launch", "job sites:%d, inside:%s", slaves.size() , contains);
		//
		//		boolean success = false;
		//		if (slaves.isEmpty()) {
		//			if (contains) {
		//				success = StaffOnHomePool.getInstance().drop(siger);
		//				if (cmd.isReply()) {
		//					super.replyStatus(success);
		//				}
		//			}
		//			setQuit(true); // 退出
		//		} else {
		//			// 发送删除命令给下级工作站点，等待反馈
		//			AwardDropRefer drop = new AwardDropRefer(siger);
		//			int count = incompleteTo(slaves, drop);
		//			success = (count > 0);
		//			Logger.debug(this, "launch", success, "sub site is:%d", count);
		//		}
		//
		//		if (!success && cmd.isReply()) {
		//			super.replyFault(Major.FAULTED, Minor.REFUSE);
		//		}
		//
		//		Logger.debug(this, "launch", success, "record site is:%d", slaves.size());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		AwardDropRefer cmd = getCommand();
		Siger siger = cmd.getUsername();
		DropUserProduct product = new DropUserProduct(siger);

		// 记录删除的站点
		ArrayList<Node> nodes = new ArrayList<Node>();

		List<Integer> keys = getEchoKeys();
		for(int index : keys) {
			try {
				if(isSuccessObjectable(index)) {
					DropUserProduct sub = getObject(DropUserProduct.class, index);
					if (sub.isSuccessful()) {
						nodes.add(getBufferHub(index));
					}
					product.add(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断成功（如果不成功，存在残余数据，这是个问题）
		boolean success = (slaves.size() == nodes.size() && product.isSuccessful());
		
		Logger.debug(this, "ending", success, "slave sites:%d, submit sites:%d, right count:%d, failed count:%d",
				slaves.size(), nodes.size(), product.getRights(), product.getFaults());

		// 删除本地记录，反馈给TOP
		if (success) {
			// 删除本地保存的账号
			StaffOnHomePool.getInstance().drop(siger);
			// 反馈报告！
			replyProduct(product);
		} else {
			reply(false);
		}

		// 退出
		return useful(success);

		//		ArrayList<Node> sites = new ArrayList<Node>();
		//
		//		List<Integer> keys = getEchoKeys();
		//		for(int index : keys) {
		//			if (!isSuccessObjectable(index)) {
		//				Logger.error(this, "ending", "echo buffer:%d, hub:%s", index, getBufferHub(index));
		//				continue;
		//			}
		//			try {
		//				StatusProduct b = getObject(StatusProduct.class, index);
		//				if (b.isSuccessful()) {
		//					sites.add(super.getBufferHub(index));
		//				}
		//			} catch (VisitException e) {
		//				Logger.error(e);
		//			}
		//		}
		//
		//		// 判断全部删除
		//		boolean success = (slaves.size() == sites.size());
		//
		//		Logger.debug(this, "ending", success, "records:%d, sites:%d", slaves.size(), sites.size());
		//
		//		// 删除用户资源引用
		//		AwardDropRefer cmd = getCommand();
		//		Siger username = cmd.getUsername();
		//		StaffOnHomePool.getInstance().drop(username);
		//
		//		// 反馈给TOP站点
		//		if (cmd.isReply()) {
		//			replyStatus(true);
		//		}
		//
		//		// 通知启动器重新注册
		//		if(success) {
		//			getLauncher().checkin(false);
		//		}
		//
		//		Logger.debug(this, "ending", success, "drop %s", username);
		//
		//		return useful(success);
	}

	/**
	 * 找到与签名关联的全部工作站点
	 * @param siger 用户签名
	 */
	private void scan(Siger siger) {
		NodeSet set = WorkOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}
		set = BuildOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}
		set = DataOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}
		set = CallOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}
	}

}