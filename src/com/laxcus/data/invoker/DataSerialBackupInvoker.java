/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.command.stub.reflex.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 数据备份调用器 <br>
 * 提供数据备份的方法
 * 
 * @author scott.liang
 * @version 1.0 7/23/2015
 * @since laxcus 1.0
 */
public abstract class DataSerialBackupInvoker extends DataSerialInvoker {

	/**
	 * 构造数据备份调用器，指定命令
	 * @param cmd 数据处理命令
	 */
	protected DataSerialBackupInvoker(Command cmd) {
		super(cmd);
	}

	/**
	 * 查找一个数据块的全部从站点
	 * @param hub CALL点地址
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回DATA从站点地址集合
	 */
	protected List<Node> findStubSlaveSite(Node hub, Space space, long stub) {
		FindStubSlaveSite cmd = new FindStubSlaveSite(space, stub);
		// 快速处理
		cmd.setQuick(true);
		FindStubSiteHook hook = new FindStubSiteHook();
		ShiftFindStubSlaveSite shift = new ShiftFindStubSlaveSite(hub, cmd, hook);
		
		// 发送给CALL站点
		boolean success = DataCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "findStubSlaveSite", "cannot submit to hub:%s", hub);
			return null;
		}
		// 等待返回结果
		hook.await();
		// 处理结果
		FindStubSiteProduct product = hook.getStubSiteProduct();
		success = (product != null);
		if (!success) {
			Logger.debug(this, "findStubSlaveSite", "cannot be take product");
			return null;
		}

		Logger.debug(this, "findStubSlaveSite", "site size %d", product.size());

		return product.getSites();
	}

	/**
	 * 去目标站点，查找与数据块关联的从站点。包含缓存块和存储块两种状态。
	 * 
	 * @param hub 目标站点地址
	 * @param cmd 查询数据块命令
	 * @return 成功返回从站点地址，没有返回空集合，否则返回空指针
	 */
	protected List<Node> findReflexStubSite(Node hub, FindReflexStubSite cmd) {
		// 快速处理
		cmd.setQuick(true);
		// 命令钩子和转发命令
		FindReflexStubSiteHook hook = new FindReflexStubSiteHook();
		ShiftFindReflexStubSite shift = new ShiftFindReflexStubSite(hub, cmd, hook);
		// 发送命令到CALL站点
		boolean success = DataCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "findReflexStubSite", "cannot submit to hub");
			return null;
		}
		// 等待
		hook.await();
		// 取结果
		ReflexStubSiteProduct product = hook.getProduct();
		success = (product != null);
		if (!success) {
			Logger.debug(this, "findReflexStubSite", "cannot be take product");
			return null;
		}

		Logger.debug(this, "findReflexStubSite", "site size %d", product.size());

		return product.list();
	}

	/**
	 * 设置映像数据。<br>
	 * 此操作由DATA主站点发出，目标是DATA从站点，映像数据分为缓存块映像数据（CACHE）和存储块映像数据（CHUNK）两种。
	 * 
	 * @param slave 从站点地址
	 * @param cmd 设置映像数据命令
	 * @return 成功返回真，否则假
	 */
	protected boolean doSetReflexData(Node slave, SetReflexData cmd) {
		SetReflexDataHook hook = new SetReflexDataHook();
		ShiftSetReflexData shift = new ShiftSetReflexData(slave, cmd, hook);

		// 进入快车道
		boolean success = DataCommandPool.getInstance().press(shift);
		// 成功，等待处理结果
		if (success) {
			// 等待
			hook.await();
			// 判断结果
			SetReflexDataProduct product = hook.getProduct();
			success = (product != null && product.isSuccessful());
		}

		Logger.note(this, "doSetReflexData", success, "set %s, size:%d to %s",
				cmd.getClass().getSimpleName(), cmd.getData().length, slave);
		return success;
	}

}