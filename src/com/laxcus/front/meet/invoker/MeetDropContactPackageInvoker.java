/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.cloud.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 删除快速计算应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 6/20/2020
 * @since laxcus 1.0
 */
public class MeetDropContactPackageInvoker extends MeetDropCloudPackageInvoker {

	/**
	 * 构造删除快速计算应用软件包，指定命令
	 * @param cmd 删除快速计算应用软件包
	 */
	public MeetDropContactPackageInvoker(DropContactPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetDropCloudPackageInvoker#getCommand()
	 */
	@Override
	public DropContactPackage getCommand() {
		return (DropContactPackage) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetDropCloudPackageInvoker#checkPermission()
	 */
	@Override
	public boolean checkPermission() {
		// 检测有发布应用软件的权限
		boolean success = getStaffPool().canPublishTask();
		if (success) {
			DropContactPackage cmd = getCommand();
			// 如果是系统软件，只能删除本地的，否则拒绝
			if (cmd.isSystemWare()) {
				success = cmd.isLocal();
			}
		}
		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetDropCloudPackageInvoker#hasLocal()
	 */
	@Override
	protected boolean hasLocal() {
		DropContactPackage cmd = getCommand();
		Naming software = cmd.getWare();
		Siger issuer = cmd.getIssuer();
		return NearTaskPool.getInstance().hasTaskElement(issuer, software);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetDropCloudPackageInvoker#dropLocal()
	 */
	@Override
	protected boolean dropLocal() {
		DropContactPackage cmd = getCommand();
		Naming software = cmd.getWare();
		Siger issuer = cmd.getIssuer();
		// 删除，返回统计值
		return NearTaskPool.getInstance().drop(issuer, software);
	}

}