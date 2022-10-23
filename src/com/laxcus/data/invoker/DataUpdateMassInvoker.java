/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.data.pool.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 更新数据块调用器。<br><br>
 * 
 * 数据块可以存储块或者缓存映像块，只发生在DATA节点之间（主/从节点）。<br>
 * 当以下情况发生时，执行这个操作：<br>
 * 1. 分布状态的数据优化（MODULATE）完成。<br>
 * 2. DATA主节点的CACHE状态数据块写满之后（RUSH或者INSERT操作，从CACHE转到CHUNK状态）。<br>
 * 3. 执行了COMPACT操作，产生新的缓存映像块。<br><br>
 * 
 * 接收端判断数据表存在后，去命令源头下载指定的数据块，完成后，返回一个通知给命令来源。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/17/2015
 * @since laxcus 1.0
 */
public class DataUpdateMassInvoker extends DataInvoker {

	/**
	 * 构造下载数据块命令调用器，指定命令
	 * @param cmd 下载数据优化数据块命令
	 */
	public DataUpdateMassInvoker(UpdateMass cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public UpdateMass getCommand() {
		return (UpdateMass) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		UpdateMass cmd = getCommand();
		StubFlag flag = cmd.getFlag();
		Space space = flag.getSpace();
		long stub = flag.getStub();

		// 判断数据表存在
		boolean success = StaffOnDataPool.getInstance().hasTable(space);
		if (success) {
			success = AccessTrustor.hasSpace(space);
		}
		// 如果不存在，拒绝更新
		if (!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		UpdateMassProduct product = new UpdateMassProduct(flag, false);
		product.setCacheReflex(cmd.isCacheReflex());

		// 区分缓存块和存储块两种属性，生成一个数据块的文件路径，可以是存在或者不存在的数据块。
		String path = null;
		if (cmd.isCacheReflex()) {
			path = AccessTrustor.doCacheReflexFile(space, stub);
		} else {
			path = AccessTrustor.doChunkFile(space, stub);
		}

		// 判断成功
		success = (path != null);
		// 不成功退出
		if (!success) {
			super.replyProduct(product);
			return useful(false);
		}

		Logger.debug(this, "launch", "process mode is %s", (isDisk() ? "DISK" : "MEMORY"));

		// 异步下载一个数据，在此等待下载完成

		// 服务器地址
		Node hub = getCommandSite();
		// 生成下载数据块命令，指定参数
		DownloadMass sub = new DownloadMass(flag);
		sub.setCacheReflex(cmd.isCacheReflex()); // 缓存映像数据块
		sub.setMemory(isMemory()); // 选择内存/磁盘模式
		// 下载数据块命令钩子和转发命令
		DownloadMassHook hook = new DownloadMassHook();
		ShiftDownloadMass shift = new ShiftDownloadMass(hub, sub, hook, path); 

		// 转发命令给DATA命令管理池，然后等待转发调用器完成。
		success = DataCommandPool.getInstance().press(shift);
		// 在上述成功下进入等待，并接收结果
		if (success) {
			hook.await();
			// 判断成功
			success = hook.isSuccessful();
		}

		// 成功，设置数据块属性和更新索引
		if (success ) {
			// 如果是缓存映像数据，重新注册数据到HOME，HOME再转发到CALL站点
			if (cmd.isCacheReflex()) {
				StaffOnDataPool.getInstance().reloadCacheReflexStub();
			} else {
				// 如果是CHUNK状态数据块，根据当前DATA站点属性，将数据块设置“主块/从块”
				if (isSlave()) {
					boolean b = AccessTrustor.toSlave(space, stub);
					Logger.note(this, "launch", b, "%s#%x to slave is", space, stub);
				} else {
					boolean b = AccessTrustor.toPrime(space, stub);
					Logger.note(this, "launch", b, "%s#%x to prime is", space, stub);
				}
				// 重新加载索引
				StaffOnDataPool.getInstance().reloadIndex();
			}
		}

		// 修改成功标识，发送给请求端
		product.setSuccessful(success);
		replyProduct(product);

		Logger.debug(this, "launch", success, "update %s from %s", flag, hub);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}
