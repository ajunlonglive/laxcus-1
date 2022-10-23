/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.visit.*;

/**
 * 检索用户阶段命名分布调用器 <br>
 * 
 * TOP站点只检索ARCHIVE站点，只保存用户签名，不返回阶段命名。
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class TopSeekUserTaskInvoker extends TopInvoker {

	/**
	 * 构造检索用户阶段命名分布，指定命令
	 * @param cmd 检索用户阶段命名分布命令
	 */
	public TopSeekUserTaskInvoker(SeekUserTask cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserTask getCommand() {
		return (SeekUserTask) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekUserTask cmd = getCommand();
		List<Node> slaves = HomeOnTopPool.getInstance().detail();
		// 以容错模式发送到HOME站点
		int count = incompleteTo(slaves, cmd);
		boolean success = (count > 0);
		if (!success) {
			replyProduct(new SeekUserTaskProduct());
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekUserTaskProduct product = new SeekUserTaskProduct();

		List<Integer> keys = super.getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					SeekUserTaskProduct e = getObject(SeekUserTaskProduct.class, index);
					if (e != null) {
						product.addAll(e.list());
					}
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 反馈结果
		boolean success = replyProduct(product);
		Logger.debug(this, "launch", success, "item size:%d", product.size());

		return useful(success);
	}


}
