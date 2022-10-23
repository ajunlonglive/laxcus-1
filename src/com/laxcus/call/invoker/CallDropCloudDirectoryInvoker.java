/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.call.pool.*;
import com.laxcus.command.cloud.store.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 删除云端目录命令
 * 
 * @author scott.liang
 * @version 1.0 10/27/2021
 * @since laxcus 1.0
 */
public class CallDropCloudDirectoryInvoker extends CallInvoker {

	/**
	 * 构造删除云端目录命令
	 * @param cmd 删除云端目录
	 */
	public CallDropCloudDirectoryInvoker(DropCloudDirectory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropCloudDirectory getCommand() {
		return (DropCloudDirectory) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropCloudDirectory cmd = getCommand();
		SRL srl = cmd.getSRL();
		String path = srl.getPath();

		// 删除云端目录
		VPath dir = StoreOnCallPool.getInstance().dropDirectory(getIssuer(), path);
		boolean success = (dir != null);
		
		// 生成应答报道
		DropCloudDirectoryProduct product = new DropCloudDirectoryProduct();
		product.setState(success ? StoreState.SUCCESSFUL : StoreState.FAILED);
		if (success) {
			product.setPath(dir);
		}
		
		if (success) {
			Logger.info(this, "launch", "delete %s", path);
		} else {
			Logger.error(this, "launch", "cannot delete %s", path);
		}
		
//		Logger.debug(this, "launch", success, "drop %s", path);

		// 反馈结果
		success = replyProduct(product);
		// 退出
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

}
