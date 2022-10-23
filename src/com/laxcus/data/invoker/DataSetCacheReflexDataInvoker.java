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
import com.laxcus.command.stub.reflex.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;

/**
 * 更新缓存映像数据。<br>
 * 更新缓存映像数据的命令从DATA主站点发出，目标是DATA子站点。子站点更新本地保存的缓存映像数据
 * 
 * @author scott.liang
 * @version 1.0 1/21/2013
 * @since laxcus 1.0
 */
public class DataSetCacheReflexDataInvoker extends DataInvoker {

	/**
	 * 构造更新缓存映像数据调用器，指定命令
	 * @param cmd SetCacheReflexData命令
	 */
	public DataSetCacheReflexDataInvoker(SetCacheReflexData cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetCacheReflexData getCommand() {
		return (SetCacheReflexData) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetCacheReflexData cmd = getCommand();

		Space space = cmd.getSpace();
		long stub = cmd.getStub();
		byte[] reflex = cmd.getData();

		// 写入JNI
		int ret = AccessTrustor.setCacheReflex(space, stub, reflex);
		// 判断结果
		boolean success = (ret >= 0);

		// 第一次写入且成功，返回0。这时要更新映像数据到CALL站点
		if (ret == 0) {
			StaffOnDataPool.getInstance().reloadCacheReflexStub();
		}

		SetReflexDataProduct product = new SetReflexDataProduct(ret);
		replyProduct(product);

		Logger.debug(this, "launch", success, "set %s#%x, code is %d", space, stub, ret);

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

	//	/**
	//	 * 更新映像数据块到CALL站点（通过HOME转发）
	//	 * @param space 数据表名
	//	 * @param stub 数据块编号
	//	 * @return
	//	 */
	//	private boolean update(Space space, long stub) {
	//		Node local = getLocal();
	//
	//		// 提取全部映像数据块编号
	//		List<CacheReflexStub> list = Access.doCacheReflexStubs(local);
	//
	//		// 设置命令
	//		SetCacheReflexStub cmd = new SetCacheReflexStub();
	//		// 单向和快速投递（不需要返回应答，走快速数据处理通道）
	//		cmd.setDirect(true);
	//		cmd.setQuick(true);
	//		cmd.addAll(list);
	//
	//		// 投递到HOME站点，再经过HOME站点转发到CALL站点
	//		Node hub = getHub();
	//		boolean success = directTo(hub, cmd);
	//
	//		Logger.debug(this, "update", success, "submit %d to %s ", cmd.size(), hub);
	//
	//		return success;
	//	}

}
