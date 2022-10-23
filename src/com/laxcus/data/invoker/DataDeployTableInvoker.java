/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.stub.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 部署数据表调用器
 * 在一个DATA节点（不分主从）建立资源引用和数据表
 * 
 * @author scott.liang
 * @version 1.0 6/15/2019
 * @since laxcus 1.0
 */
public class DataDeployTableInvoker extends DataInvoker {

	/**
	 * @param cmd
	 */
	public DataDeployTableInvoker(DeployTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployTable getCommand() {
		return (DeployTable) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DeployTable cmd = getCommand();
		Refer refer = cmd.getRefer();
		Table table = cmd.getTable();

//		// 判断已经存在资源引用
//		boolean success = StaffOnDataPool.getInstance().hasRefer(refer.getUsername());
//		if (!success) {
//			// 1.建立账号（虚拟）  2.设置资源引用（实际）
//			success = StaffOnDataPool.getInstance().create(refer.getUsername());
//			if (success) {
//				success = StaffOnDataPool.getInstance().buckle(refer);
//			}
//		}
		
		// 诊断资源引用
		boolean success = confirm(refer.getUsername());
		// 建表
		if (success) {
			success = StaffOnDataPool.getInstance().hasTable(table.getSpace());
			// 当表不存在时，建表
			if (!success) {
				if (isMaster()) {
					success = createPrimeTable(table);
				} else {
					success = createSlaveTable(table);
				}
			}
		}

		// 返回结果
		Seat seat = new Seat(refer.getUsername(), getLocal());
		DeployTableItem item = new DeployTableItem(seat, success);
		DeployTableProduct product = new DeployTableProduct(item);
		success = replyProduct(product);

		// 延迟重新注册
		if (success) {
			getLauncher().checkin(false);
		}

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
	 * 建立数据表之前的确认操作，包括：<br>
	 * 1. 检查用户签名存在，不存在建立一个新的 <br>
	 * 2. 检查有关联的ACCOUNT站点（同步方式） <br>
	 * 3. 下载分布任务组件和码位计算器组件（异步方式） <br><br>
	 * 
	 * @param siger 用户签名
	 * @return 成功返回真，否则假。
	 */
	private boolean confirm(Siger siger) {
		// 判断资源引用存在
		boolean success = StaffOnDataPool.getInstance().hasRefer(siger);
		// 如果资源引用不存在，建立它
		if (!success) {
			success = StaffOnDataPool.getInstance().create(siger);
			// 分配关联的用户资源
			if (success) {
				allocate(siger);
			}
		}

		Logger.debug(this, "confirm", success, "check %s", siger);

		return success;
	}

	/**
	 * 在建立数据表之前，分配与数据表关联的用户资源，包括：<br>
	 * 1. 获取与用户签名关联的ACCOUNT站点地址。<br>
	 * 2. 从ACCOUNT站点下载关联的分布任务组件和码位计算器组件。<br><br>
	 * 
	 * @param siger 用户签名
	 */
	private void allocate(Siger siger) {
		// 判断没有分配ACCOUNT站点
		boolean empty = AccountOnCommonPool.getInstance().isEmpty();

		// 以同步方式去HOME站点获取ACCOUNT站点，同步方式等待返回结果
		boolean success = AccountOnCommonPool.getInstance().load(siger);
		if (success) {
			// 如果是空集合，加载系统级分布任务组件
			if (empty) {
				StaffOnDataPool.getInstance().loadTasks(null);
			}
			// 以异步方式加载用户级分布任务组件，和码位计算器组件（码位计算器组件只有用户层组件，没有系统层组件）
			// 异步方式是发出命令后不再等待，返回结果自行处理
			StaffOnDataPool.getInstance().loadTasks(siger);
			
//			StaffOnDataPool.getInstance().loadScaler(siger);
		}
	}

	/**
	 * 建立主表
	 * @param table
	 * @return
	 */
	private boolean createPrimeTable(Table table) {
		// 统计没有使用的数据块编号的数量
		int size = AccessTrustor.getCountFreeStubs();
		// 如果数据块编号不足，向HOME站点申请新的数据块编号
		boolean success = (size > 0);
		if (!success) {
			success = takeStubs();
		}
		// 建表
		if (success) {
			success = StaffOnDataPool.getInstance().createTable(table);
		}

		Logger.debug(this, "createPrimeTable", success, "create %s", table);

		return success;
	}

	/**
	 * 建立从表
	 * @param table
	 * @return
	 */
	private boolean createSlaveTable(Table table) {
		boolean success = StaffOnDataPool.getInstance().createTable(table);

		Logger.debug(this, "createSlaveTable", success, "create %s", table);

		return success;
	}
	
	/**
	 * 申请数据块
	 * @return
	 */
	private boolean takeStubs() {
		TakeStub cmd = new TakeStub(5); // 申请一批数据块编号，数量是5个
		TakeStubHook hook = new TakeStubHook();
		ShiftTakeStub shift = new ShiftTakeStub(cmd, hook);

		// 进入快车道
		boolean success = DataCommandPool.getInstance().press(shift);
		// 等待结果
		if (!success) {
			return false;
		}
		// 逗留
		hook.await();
		// 取出结果
		StubProduct product = hook.getStubProduct();
		success = (product != null);
		// 取出数据块编号，保存到磁盘上
		if (success) {
			for (long stub : product.list()) {
				AccessTrustor.addStub(stub);
			}
		}
		// 返回结果
		return success;
	}
}
