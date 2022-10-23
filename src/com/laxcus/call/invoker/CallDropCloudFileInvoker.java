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
 * 删除云端文件命令
 * 
 * @author scott.liang
 * @version 1.0 1/22/2022
 * @since laxcus 1.0
 */
public class CallDropCloudFileInvoker extends CallInvoker {

	/**
	 * 构造删除云端文件命令
	 * @param cmd 删除云端文件
	 */
	public CallDropCloudFileInvoker(DropCloudFile cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropCloudFile getCommand() {
		return (DropCloudFile) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropCloudFile cmd = getCommand();
		SRL srl = cmd.getSRL();
		String path = srl.getPath();

		// 删除文件
		VPath file = StoreOnCallPool.getInstance().dropFile(getIssuer(), path);
		boolean success = (file != null);
		
		// 生成应答报道
		DropCloudFileProduct product = new DropCloudFileProduct();
		product.setState(success ? StoreState.SUCCESSFUL : StoreState.FAILED);
		if (success) {
			product.setPath(file);
		}
		
		if (success) {
			Logger.info(this, "launch", "delete %s", path);
		} else {
			Logger.error(this, "launch", "cannot delete %s", path);
		}
		
//		Logger.debug(this, "launch", success, "delete %s", path);

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
