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
import com.laxcus.util.*;

/**
 * 授权建表命令调用器。<br>
 * 
 * 区分主从节点，按照不同的要求建立数据表。
 * 
 * @author scott.liang
 * @version 1.0 09/03/2012
 * @since laxcus 1.0
 */
public class DataAwardCreateTableInvoker extends DataInvoker {

	/**
	 * 构造建表调用器，指定命令。
	 * @param cmd 建表命令
	 */
	public DataAwardCreateTableInvoker(AwardCreateTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardCreateTable getCommand() {
		return (AwardCreateTable) super.getCommand();
	}

	/**
	 * 向HOME站点反馈结果
	 * @param success
	 */
	private boolean reply(boolean success) {
		Space space = getCommand().getTable().getSpace();
		CreateTableProduct product = new CreateTableProduct(space, success);
		product.setSuccessful(success);
		return replyProduct(product);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardCreateTable cmd = getCommand();
		Refer refer = cmd.getRefer();
		Siger siger = refer.getUsername();

		// 建立账号前的确认操作
		boolean success = confirm(siger);
		// 建立数据表
		if (success) {
			Table table = cmd.getTable();
			if (isMaster()) {
				success = createPrimeTable(table);
			} else {
				success = createSlaveTable(table);
			}
		}

		Logger.debug(this, "launch", success, "create %s - %s", siger, cmd.getTable());

		// 向HOME站点发送处理结果
		success = reply(success);
		
		// 如果成功，要求系统延时重新注册
		if (success) {
			getLauncher().checkin(false);
		}

		return useful(success);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
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
	 * 建立数据表之前的确认操作，包括：<br>
	 * 1. 检查用户签名存在，不存在建立一个新的 <br>
	 * 2. 检查有关联的ACCOUNT站点（同步方式） <br>
	 * 3. 下载分布任务组件和码位计算器组件（异步方式） <br><br>
	 * 
	 * @param siger 用户签名
	 * @return 成功返回真，否则假。
	 */
	private boolean confirm(Siger siger) {
		// 判断账号存在
		boolean success = StaffOnDataPool.getInstance().hasRefer(siger);
		// 如果账号不存在，建立它
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