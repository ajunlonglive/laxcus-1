/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.front.pool.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 建表命令调用器。FRONT站点建立数据表。<br><br>
 * 
 * 建表命令只限普通注册用户使用，且要得到管理员的“建表”授权。管理员没有建表能力（是系统的设计规定）
 * 
 * 操作流程：<br>
 * 1. FRONT通过AID站点发向TOP。
 * 2. TOP.launch 检查参数，按照要求找到一到多个HOME站点，保证存在且有效。命令分配到HOME站点。
 * 3. HOME.launch 检查当前的DATA/CALL站点，有效发向DATA站点。
 * 4. Home.ending(1) 判断DATA反馈结果，失败退出，否则发命令给CALL站点。
 * 5. HOME.ending(2) 判断CALL反馈结果，失败删除DATA上的记录和返回，否则向TOP返回成功标记，包含CALL站点信息。
 * 6. TOP.ending接收HOME反馈，通过AID反馈给FRONT。
 * 
 * 反馈结果是“”对象，其中包含被部署的CALL站点地址。FRONT应该立即去连接它。
 * 
 * @author scott.liang
 * @version 1.1 3/23/2013
 * @since laxcus 1.0
 */
public class MeetCreateTableInvoker extends MeetRuleInvoker {

	/** 步骤 **/
	private int step = 1;

	/**
	 * 构造建表命令调用器，指定建表命令
	 * @param cmd 建表命令
	 */
	public MeetCreateTableInvoker(CreateTable cmd) {
		super(cmd, true); // 要求锁定资源的处理，防止FrontScheduleRefreshInvoker同步
		initRule();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateTable getCommand() {
		return (CreateTable) super.getCommand();
	}

	/**
	 * 初始化事务规则
	 */
	private void initRule() {
		CreateTable cmd = getCommand();		
		// 定义规则
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(cmd.getSpace());
		addRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		boolean success = false;
		switch (step) {
		case 1:
			success = send();
			break;
		case 2:
			success = receive();
			break;
		}
		// 自增1
		step++;
		// 大于2是完成，否则是没有完成
		return (!success || step > 2);
	}

	/**
	 * 投递命令到注册站点
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		return fireToHub();
	}

	/**
	 * 从注册站点接收处理结果
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		CreateTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		CreateTable cmd = getCommand();
		Table table = cmd.getTable();
		Space space = table.getSpace();

		Logger.debug(this, "receive", success, "creata table %s", space);
		
		ArrayList<Node> servers = new ArrayList<Node>();

		if (success) {
			// 注册到CALL站点
			List<GatewayNode> list = product.list();
			for (GatewayNode gateway : list) {
				Logger.info(this, "receive", "\'%s\' table site is %s", space, gateway);

				// 此时的内网/公网地址是一致的，随便取一个即可！
				Node hub = gateway.getPublic();
				// 形成FRONT -> CALL站点映射
				checkPock(hub);

				// 判断地址是否存在，存在，保存地址；地址不存在，注册再保存址！
				boolean exists = CallOnFrontPool.getInstance().contains(hub);
				if (exists) {
					boolean b = getStaffPool().addTableSite(hub, space);
					if (b) servers.add(hub);
				} else {
					// 注册到CALL站点
					boolean logined = CallOnFrontPool.getInstance().login(hub);
					// 注册成功，保存数据表名
					if (logined) {
						boolean b = getStaffPool().addTableSite(hub, space);
						if (b) servers.add(hub);
					}
				}
			}
			// 保存表
			getStaffPool().createTable(table);

			// 通知FRONT重新注册
			getLauncher().checkin(false);
		} 

		// 打印结果
		if (success && servers.size() > 0) {
			successful(space, servers);
		} else {
			print(success, space);
		}

		return success;
	}
	
	/**
	 * 显示成功!
	 * @param space
	 * @param hubs
	 */
	private void successful(Space space, List<Node> hubs) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "CREATE-TABLE/STATUS", "CREATE-TABLE/TABLE", "CREATE-TABLE/SITE" });

		for (Node hub : hubs) {
			ShowItem showItem = new ShowItem();
			showItem.add(createConfirmTableCell(0, true));
			showItem.add(new ShowStringCell(1, space));
			showItem.add(new ShowStringCell(2, hub.toString()));
			addShowItem(showItem);
		}

		// 输出全部记录
		flushTable();
	}

	/**
	 * 显示处理结果
	 * @param success 成功或者否
	 * @param space 数据表名
	 */
	private void print(boolean success, Space space) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "CREATE-TABLE/STATUS", "CREATE-TABLE/TABLE"});

		ShowItem showItem = new ShowItem();
		showItem.add(createConfirmTableCell(0, success));
		showItem.add(new ShowStringCell(1, space));
		addShowItem(showItem);
		
		// 输出全部记录
		flushTable();
		
		// 出错
		if (!success) {
			String content = getXMLContent("CREATE-TABLE/FAILED");
			fault(content, true);
			setSound(false); // 不播放声音
		}
	}

}