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
import com.laxcus.command.rebuild.*;
import com.laxcus.data.pool.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;

/**
 * 设置DSM表压缩倍数调用器。<br>
 * 此命令只作用在DATA主站点上。
 * 
 * @author scott.liang
 * @version 1.0 5/20/2019
 * @since laxcus 1.0
 */
public class DataSetDSMReduceInvoker extends DataInvoker {

	/**
	 * 构造设置DSM表压缩倍数调用器，指定命令
	 * @param cmd 设置DSM表压缩倍数
	 */
	public DataSetDSMReduceInvoker(SetDSMReduce cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetDSMReduce getCommand() {
		return (SetDSMReduce) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetDSMReduce cmd = getCommand();
		Space space = cmd.getSpace();
		
		// 逐级判断正确

		// 判断是主站点。只有DATA主站点才能执行SET DSM REDUCE操作
		boolean success = isMaster();
		// 判断数据表存在
		if (success) {
			Table table = StaffOnDataPool.getInstance().findTable(space);
			success = (table != null && table.isDSM());
		}
		// 判断磁盘存在表空间
		if (success) {
			success = AccessTrustor.hasSpace(space);
		}
		// 拿到CACHE数据块编号
		if (success) {
			long stub = AccessTrustor.getCacheStub(space);
			success = (stub != 0);
		}
		// 以上一项不成功退出
		if (!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		// 强制转换
		int state = AccessTrustor.setDSMReduce(space, cmd.getMultiple());
		// 判断成功
		success = (state >= 0);

		Logger.debug(this, "launch", success, "set [%s - %d] dsm reduce is %d",
				space, cmd.getMultiple(), state);

		// 反馈处理结果
		SetDSMReduceProduct product = new SetDSMReduceProduct(getLocal(), state);
		replyProduct(product);

		Logger.debug(this, "launch", success, "reload %s", space);

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