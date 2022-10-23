/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import com.laxcus.command.cloud.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.contact.distant.*;

/**
 * 检索云端应用调用器。<br>
 * 
 * WORK节点检索TO组件、SCALER、SWIFT
 * 
 * @author scott.liang
 * @version 1.0 2/11/2020
 * @since laxcus 1.0
 */
public class WorkSeekCloudWareInvoker extends CommonSeekCloudWareInvoker {

	/**
	 * 构造检索云端应用调用器，指定命令
	 * @param cmd 检索云端应用
	 */
	public WorkSeekCloudWareInvoker(SeekCloudWare cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekCloudWareProduct product = new SeekCloudWareProduct();
		
		CloudWareItem item = new CloudWareItem(getLocal());
		loadTasks(item);

		// 保存单元
		if (item.size() > 0) {
			product.add(item);
		}

		// 返回结果
		boolean success = replyProduct(product);
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
	 * 导入组件命名到云应用包单元
	 * @param item 云应用包单元
	 */
	private void loadTasks(CloudWareItem item) {
		loadTasks(ToTaskPool.getInstance(), item);
		loadTasks(DistantTaskPool.getInstance(), item);
	}

}