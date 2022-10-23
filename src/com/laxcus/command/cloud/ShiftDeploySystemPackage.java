/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发部署系统应用命令
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class ShiftDeploySystemPackage extends ShiftCommand {

	private static final long serialVersionUID = -7429875528050748082L;

	/** 目标ACCOUNT节点 **/
	private Node remote;
	
	/** 云应用包 **/
	private CloudPackageComponent component;
	
	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftDeploySystemPackage(ShiftDeploySystemPackage that){
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftDeploySystemPackage duplicate() {
		return new ShiftDeploySystemPackage(this);
	}
	
	/**
	 * 构造转发部署系统应用命令，指定参数
	 * @param cmd 部署系统应用命令
	 * @param hook 命令钩子
	 */
	public ShiftDeploySystemPackage(DeploySystemPackage cmd, DeploySystemPackageHook hook) {
		super(cmd, hook);
	}

	/**
	 * 构造转发部署系统应用命令，指定参数
	 * @param remote ACCOUNT站点地址
	 * @param component 应用包
	 * @param cmd 部署系统应用命令
	 * @param hook 命令钩子
	 */
	public ShiftDeploySystemPackage(Node remote, CloudPackageComponent component, DeploySystemPackage cmd, DeploySystemPackageHook hook) {
		super(cmd, hook);
		setRemote(remote);
		setComponent(component);
	}

	/**
	 * 设置目标ACCOUNT节点
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		Laxkit.nullabled(e);
		remote = e;
	}

	/**
	 * 返回目标ACCOUNT节点
	 * @return Node实例
	 */
	public Node getRemote() {
		return remote;
	}

	/**
	 * 设置云应用包
	 * @param e CloudPackageComponent实例
	 */
	public void setComponent(CloudPackageComponent e) {
		Laxkit.nullabled(e);
		component = e;
	}

	/**
	 * 返回云应用包
	 * @return CloudPackageComponent实例
	 */
	public CloudPackageComponent getComponent() {
		return component;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public DeploySystemPackage getCommand() {
		return (DeploySystemPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public DeploySystemPackageHook getHook() {
		return (DeploySystemPackageHook) super.getHook();
	}

}