/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.command.stub.reflex.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 数据更新调用器 <br>
 * 提供数据和数据块网络更新的方法，把数据从主站点传输到从站点。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2015
 * @since laxcus 1.0
 */
public abstract class DataSerialReplaceInvoker extends DataSerialBackupInvoker {

	/**
	 * 构造数据更新调用器，指定命令
	 * @param cmd 命令
	 */
	protected DataSerialReplaceInvoker(Command cmd) {
		super(cmd);
	}

	/**
	 * 更新数据块（存储块/缓存映像块） <br>
	 * 通知从站点连接到此处，下载数据块，并且保存到本地
	 * 
	 * @param slave DATA从节点地址
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @param cacheReflex 缓存映像块
	 * @return 成功返回真，否则假
	 */
	protected boolean doUpdateMass(Node slave, Space space, long stub, boolean cacheReflex) {
		UpdateMass cmd = new UpdateMass(space, stub);
		cmd.setCacheReflex(cacheReflex);
		// 要求快速处理
		cmd.setQuick(true);
		// 命令钩子和转发命令
		UpdateMassHook hook = new UpdateMassHook();
		ShiftUpdateMass shift = new ShiftUpdateMass(slave, cmd, hook);

		// 使用快速处理通道处理
		boolean success = DataCommandPool.getInstance().press(shift);
		// 成功，等待结果
		if (success) {
			// 等待
			hook.await();
			// 判断成功
			UpdateMassProduct product = hook.getProduct();
			success = (product != null && product.isSuccessful());
		}

		Logger.note(this, "doUploadChunk", success, "from %s", slave);
		return success;
	}

	/**
	 * 删除缓存映像块
	 * @param slave DATA从节点地址
	 * @param space 数据表名
	 * @param stub 缓存块编号
	 * @return 成功返回真，否则假
	 */
	protected boolean doDeleteCacheReflex(Node slave, Space space, long stub) {
		DeleteCacheReflex cmd = new DeleteCacheReflex(space, stub);
		// 快速处理
		cmd.setQuick(true);
		// 命令钩子和转发命令
		DeleteCacheReflexHook hook = new DeleteCacheReflexHook();
		ShiftDeleteCacheReflex shift = new ShiftDeleteCacheReflex(slave, cmd, hook);

		// 使用快速处理通道处理
		boolean success = DataCommandPool.getInstance().press(shift);
		// 成功，等待结果
		if (success) {
			// 等待
			hook.await();
			// 判断成功
			DeleteCacheReflexProduct product = hook.getProduct();
			success = (product != null && product.isSuccessful());
		}

		Logger.note(this, "doDeleteReflexCache", success, "from %s", slave);
		return success;
	}
	
}
