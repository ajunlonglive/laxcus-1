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
import com.laxcus.util.*;

/**
 * 扫描云端磁盘记录
 * 
 * @author scott.liang
 * @version 1.0 10/30/2021
 * @since laxcus 1.0
 */
public class CallScanCloudDirectoryInvoker extends CallInvoker {

	/**
	 * 扫描磁盘记录
	 * @param cmd
	 */
	public CallScanCloudDirectoryInvoker(ScanCloudDirectory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanCloudDirectory getCommand(){
		return (ScanCloudDirectory)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanCloudDirectory cmd = getCommand();
		Siger issuer = cmd.getIssuer();
		SRL srl = cmd.getSRL();
		// 路径
		VPath path = StoreOnCallPool.getInstance().scanDisk(issuer, srl.getPath());
		// 结果
		ScanCloudDirectoryProduct product = new ScanCloudDirectoryProduct();
		// 定义内网和公网地址
		product.setSite(getLocal());
		product.setGateway(getPublicListener());

		// 虚拟路径
		product.setVPath(path);
		// 返回应答
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

}