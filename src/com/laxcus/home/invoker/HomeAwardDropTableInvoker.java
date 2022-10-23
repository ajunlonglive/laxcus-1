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
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 授权删表调用器 <br>
 * 
 * TOP站点要求HOME集群，删除一个表，以及这个表下面的全部数据。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2012
 * @since laxcus 1.0
 */
public class HomeAwardDropTableInvoker extends HomeInvoker {

	/** 分布站点记录 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();

	/**
	 * 构造授权删表调用器，指定命令
	 * @param cmd 授权删表命令
	 */
	public HomeAwardDropTableInvoker(AwardDropTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardDropTable getCommand() {
		return (AwardDropTable) super.getCommand();
	}
	
	/**
	 * 反馈删表处理结果
	 * @param successful 成功标识
	 * @return 报告发送成功返回真，否则假
	 */
	private boolean reply(boolean successful) {
		AwardDropTable cmd = getCommand();
		// 如果是单向操作
		if (cmd.isDirect()) {
			return true;
		}
		Space space = cmd.getSpace();
		DropTableProduct product = new DropTableProduct(space, successful);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardDropTable cmd = getCommand();
		Space space = cmd.getSpace();

		// 找到表持有人的资源引用
		Refer refer = StaffOnHomePool.getInstance().find(space);
		// 没有引用或者没有数据表，是错误！
		if (refer == null || !refer.hasTable(space)) {
			reply(false);
			return false;
		}

		// 用户名称
		Siger siger = refer.getUsername();
	
		// 收集与签名和数据表关联的工作站点
		askSites(siger, space);
		
		// 是空集合，删除表和返回
		if (slaves.isEmpty()) {
			boolean success = StaffOnHomePool.getInstance().dropTable(space);
			reply(success);
			return useful(success);
		}

		// 容错模式向目标站点发送命令
		int count = incompleteTo(slaves, cmd);
		boolean success = (count > 0);

		Logger.debug(this, "launch", success, "send sites is '%d - %d'", slaves.size(), count);

		// 不成功通知TOP站点
		if (!success) {
			reply(false);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		AwardDropTable cmd = getCommand();
		Space space = cmd.getSpace();

		DropTableProduct product = new DropTableProduct(space);
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		List<Integer> keys = getEchoKeys();
		for(int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					DropTableProduct sub = getObject(DropTableProduct.class, index);
					// 统计删除成功的站点
					if(sub.isSuccessful()) {
						nodes.add(getBufferHub(index));
					}
					product.add(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断完全成功
		boolean success = (slaves.size() == nodes.size() && product.isSuccessful());
		// 删除HOME磁盘记录
		if (success) {
			// 删除表
			StaffOnHomePool.getInstance().dropTable(space);
			// 重新注册
			getLauncher().checkin(false);
		}
		
		// 通知TOP站点		
		reply(success);

		Logger.note(this, "ending", success, "drop '%s'", space);

		// 退出操作
		return useful(success);
	}

	/**
	 * 根据签名和表名，找到关联的工作站点
	 * @param siger 用户签名
	 * @param space 数据表名
	 */
	private void askSites(Siger siger, Space space) {
		// CALL站点记录
		NodeSet set = CallOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}
		// 根据表名查找DATA站点记录
		set = DataOnHomePool.getInstance().findSites(space);
		if (set != null) {
			slaves.addAll(set.show());
		}
		// WORK站点记录
		set = WorkOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}
		// BUILD站点记录
		set = BuildOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}
	}

}