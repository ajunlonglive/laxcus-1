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
 * 修改云端文件命令
 * 
 * @author scott.liang
 * @version 1.0 1/26/2022
 * @since laxcus 1.0
 */
public class CallRenameCloudFileInvoker extends CallInvoker {

	/**
	 * 构造修改云端文件命令
	 * @param cmd 修改云端文件
	 */
	public CallRenameCloudFileInvoker(RenameCloudFile cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RenameCloudFile getCommand() {
		return (RenameCloudFile) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RenameCloudFile cmd = getCommand();
		SRL srl = cmd.getSRL();
		String name = cmd.getName();
		String path = srl.getPath();

		// 修改云端文件
		SFile dir = StoreOnCallPool.getInstance().renameFile(getIssuer(), path, name);
		boolean success = (dir != null);
		
		// 生成应答报道
		RenameCloudFileProduct product = new RenameCloudFileProduct();
		product.setState(success ? StoreState.SUCCESSFUL : StoreState.FAILED);
		if (success) {
			product.setFile(dir);
		}
		
		Logger.debug(this, "launch", success, "rename %s", path);

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
