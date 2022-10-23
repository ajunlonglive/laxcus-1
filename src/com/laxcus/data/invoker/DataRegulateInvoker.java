/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.stub.site.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 数据优化调用器。<br>
 * 这个命令由FRONT启动，通过CALL站点发给关联的DATA主站点。
 * 
 * @author scott.liang
 * @version 1.2 9/09/2013
 * @since laxcus 1.0
 */
public class DataRegulateInvoker extends DataSerialInvoker {

	/** END阶段迭代编号，从1开始 **/
	private int endStep = 1;

	/** 数据优化后的数据块 **/
	private ArrayList<Long> results = new ArrayList<Long>();

	/** 发送命令到从站点 **/
	private final static int TO_SLAVE_SITE = 1;

	/** 从子站点接收反馈 **/
	private final static int FROM_SLAVE_SITE = 2;

	/**
	 * 构造数据优化调用器，指定优化命令
	 * @param cmd 数据优化
	 */
	public DataRegulateInvoker(Regulate cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Regulate getCommand() {
		return (Regulate) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须是主节点
		boolean success = isMaster();
		if(!success) {
			Logger.error(this, "launch", "must be prime site"); 
			super.replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		Regulate cmd = getCommand();
		Dock dock = cmd.getDock();

		// 锁定表
		lock(dock.getSpace());

		long time = System.currentTimeMillis();
		Logger.info(this, "launch", "regulate %s", dock); 

		// 调用JNI接口优化数据(删除过期数据,重新压缩数据并且存储)
		long[] stubs = AccessTrustor.regulate(dock); 

		Logger.info(this, "launch", "regulate %s, usedtime:%d, byte size:%d", 
				dock, System.currentTimeMillis() - time, (stubs == null ? -1 : stubs.length));

		// 判断不是空集合
		success = !Laxkit.isEmpty(stubs);
		// 不成功，向目标地址返回报告
		if (!success) {
			RegulateProduct product = new RegulateProduct(dock, 0);
			super.replyProduct(product);
			// 退出
			return useful(success);
		}

		// 查询数据块的DATA从节点地址
		SeekSlaveStubSite query = new SeekSlaveStubSite(dock.getSpace());
		// 保存数据块编号
		for (int i = 0; i < stubs.length; i++) {
			query.addStub(stubs[i]);
			results.add(stubs[i]);
		}

		// 向目标站点发送查询数据块子站点命令
		Node hub = cmd.getSource().getNode();
		success = super.completeTo(hub, query);

		Logger.debug(this, "launch", success, "send %s", hub);

		return success;
	}

	/**
	 * 执行更新后的处理，向请求端返回处理结果。
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		boolean success = false;
		switch (endStep) {
		case TO_SLAVE_SITE:
			success = doToSlaveSites();
			break;
		case FROM_SLAVE_SITE:
			success  = doFromSlaveSite();
			break;
		}

		endStep++;

		return success;
	}

	/**
	 * 接收CALL站点返回的报告，分发“数据块通知命令”到子站点
	 * @return 成功返回真，否则假
	 */
	private boolean doToSlaveSites() {
		// 获得返回结果
		SlaveStubSiteProduct product = null;
		if (super.isSuccessObjectable(0)) {
			try {
				product = getObject(SlaveStubSiteProduct.class, 0);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 不成功，退出
		if (!success) {
			super.replyFault(Major.FAULTED, Minor.ECHO_ERROR);
			return useful(false);
		}

		Regulate regulate = getCommand();
		// 是空集合，发送报告和退出
		if (product.isEmpty()) {
			RegulateProduct reply = new RegulateProduct(regulate.getDock(), results.size());
			super.replyProduct(reply);
			return useful(true);
		}

		// 通知DATA从站点，执行分发数据块操作
		Space space = regulate.getSpace();

		ArrayList<CommandItem> array = new ArrayList<CommandItem>();

		for (SlaveStubSite site : product.list()) {
			Node remote = site.getSource(); // 从站点地址
			if (space.compareTo(site.getSpace()) != 0) {
				continue;
			}
			// 保存每个地址
			for (long stub : site.getStubs()) {
				UpdateMass cmd = new UpdateMass(space, stub);
				CommandItem item = new CommandItem(remote, cmd);
				array.add(item);
			}
		}

		success = super.completeTo(array);

		Logger.debug(this, "doSlaveStubSiteProduct", success, "result is");

		return success;
	}

	/**
	 * 接收全部子站点的反馈报告
	 * @return
	 */
	private boolean doFromSlaveSite() {
		int count = 0;
		List<Integer> list = this.getEchoKeys();
		for (int index : list) {
			if (!super.isSuccessObjectable(index)) {
				continue;
			}
			try {
				UpdateMassProduct product = this.getObject(
						UpdateMassProduct.class, index);
				if (product.isSuccessful()) {
					count++;
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (count == list.size());

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		// 解除锁定
		if (isAlive()) {
			Regulate cmd = getCommand();
			if (cmd != null) {
				unlock(cmd.getSpace());
			}
		}
		// 调用上级方法
		super.destroy();
	}
}