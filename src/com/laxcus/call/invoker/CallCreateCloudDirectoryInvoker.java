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
 * 建立云端目录命令
 * 
 * @author scott.liang
 * @version 1.0 10/24/2021
 * @since laxcus 1.0
 */
public class CallCreateCloudDirectoryInvoker extends CallInvoker {

	/**
	 * 构造建立云端目录命令
	 * @param shift 建立云端目录
	 */
	public CallCreateCloudDirectoryInvoker(CreateCloudDirectory shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateCloudDirectory getCommand() {
		return (CreateCloudDirectory) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateCloudDirectory cmd = getCommand();
		SRL srl = cmd.getSRL();
		String path = srl.getPath();

//		// 生成目录
//		SDirectory dir = StoreOnCallPool.getInstance().createDirectory(getIssuer(), path);
//		boolean success = (dir != null);
		
		// 生成目录
		VPath dir = StoreOnCallPool.getInstance().createDirectory(getIssuer(), path);
		boolean success = (dir != null);
		
		// 生成应答报道
		CreateCloudDirectoryProduct product = new CreateCloudDirectoryProduct();
		product.setState(success ? StoreState.SUCCESSFUL : StoreState.FAILED);
		if (success) {
			product.setPath(dir);
		}
		
		Logger.debug(this, "launch", success, "create %s", path);

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
