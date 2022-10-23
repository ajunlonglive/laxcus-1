/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.cloud.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 删除数据构建应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopDropEstablishPackageInvoker extends DesktopDropCloudPackageInvoker {

	/**
	 * 构造删除数据构建应用软件包，指定命令
	 * @param cmd 删除数据构建应用软件包
	 */
	public DesktopDropEstablishPackageInvoker(DropEstablishPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.desktop.invoker.DesktopDropCloudPackageInvoker#getCommand()
	 */
	@Override
	public DropEstablishPackage getCommand() {
		return (DropEstablishPackage) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.desktop.invoker.DesktopDropCloudPackageInvoker#checkPermission()
	 */
	@Override
	public boolean checkPermission() {
		// 检测有发布应用软件的权限
		boolean success = getStaffPool().canPublishTask();
		if (success) {
			// 如果是系统软件，只能删除本地的，否则拒绝
			DropEstablishPackage cmd = getCommand();
			if (cmd.isSystemWare()) {
				success = cmd.isLocal();
			}
		}
		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.desktop.invoker.DesktopDropCloudPackageInvoker#hasLocal()
	 */
	@Override
	protected boolean hasLocal() {
		DropEstablishPackage cmd = getCommand();
		Naming software = cmd.getWare();
		Siger issuer = cmd.getIssuer();
		return EndTaskPool.getInstance().hasTaskElement(issuer, software);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.desktop.invoker.DesktopDropCloudPackageInvoker#dropLocal()
	 */
	@Override
	protected boolean dropLocal() {
		DropEstablishPackage cmd = getCommand();
		Naming software = cmd.getWare();
		Siger issuer = cmd.getIssuer();
		// 删除，返回统计值
		return EndTaskPool.getInstance().drop(issuer, software);
	}

}