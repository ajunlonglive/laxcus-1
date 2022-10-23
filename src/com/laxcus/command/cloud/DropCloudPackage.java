/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 删除部署在云端的应用软件包。<br>
 * 删除基于用户的软件包名称，将把DTC软件包、附件包、动态链接库全部删除！。
 * 
 * @author scott.liang
 * @version 1.0 6/20/2020
 * @since laxcus 1.0
 */
public abstract class DropCloudPackage extends Command {

	private static final long serialVersionUID = -3491787874102114375L;

	/** 软件名称 **/
	private Naming ware;

	/** 只删除本地 **/
	private boolean local;
	
	/**
	 * 构造删除部署在云端的应用软件包
	 */
	protected DropCloudPackage() {
		super();
		local = false;
	}

	/**
	 * 删除部署在云端的应用软件包副本
	 * @param that 删除部署在云端的应用软件包
	 */
	protected DropCloudPackage(DropCloudPackage that) {
		super(that);
		ware = that.ware;
		local = that.local;
	}

	/**
	 * 设置软件名称，不允许空指针
	 * @param e Naming实例
	 */
	public void setWare(Naming e) {
		Laxkit.nullabled(e);
		ware = e;
	}

	/**
	 * 返回软件名称
	 * @return Naming实例
	 */
	public Naming getWare() {
		return ware;
	}
	
	/**
	 * 判断是系统软件
	 * @return 返回真或者假
	 */
	public boolean isSystemWare() {
		String str = ware.toString();
		return str.matches(Sock.SYSTEM_REGEX);
	}

	/**
	 * 设置在FRONT节点本地部署
	 * @param b 真或者假
	 */
	public void setLocal(boolean b) {
		local = b;
	}
	
	/**
	 * 判断在FRONT节点本地部署
	 * @return 真或者假
	 */
	public boolean isLocal() {
		return local;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(ware);
		writer.writeBoolean(local);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		ware = new Naming(reader);
		local = reader.readBoolean();
	}

}