/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发传输数据块命令。
 * 
 * 产生和执行这个命令的是请求端，接受实际命令的是服务端。
 * 
 * @author scott.liang
 * @version 1.0 11/23/2012
 * @since laxcus 1.0
 */
public class ShiftDownloadMass extends ShiftCommand {

	private static final long serialVersionUID = -1015586784153981814L;

	/** 目标地址 **/
	private Node hub;

	/** 目标文件名 **/
	private String filename;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftDownloadMass(ShiftDownloadMass that){
		super(that);
		hub = that.hub;
		filename = that.filename;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftDownloadMass duplicate() {
		return new ShiftDownloadMass(this);
	}
	
	/**
	 * 构造转发传输数据块命令，指定全部参数
	 * @param hub 服务器地址
	 * @param cmd 传输数据块命令
	 * @param hook 命令钩子
	 * @param filename 目标文件
	 */
	public ShiftDownloadMass(Node hub, DownloadMass cmd, DownloadMassHook hook, String filename) {
		super(cmd, hook);
		setHub(hub);
		setFilename(filename);
	}

	/**
	 * 设置目标地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回目标地址
	 * @return Node实例
	 */
	public Node getHub() {
		return hub;
	}

	/**
	 * 设置文件名
	 * @param e 文件名
	 */
	public void setFilename(String e) {
		filename = e;
	}

	/**
	 * 返回文件名
	 * @return 文件名
	 */
	public String getFilename() {
		return filename;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public DownloadMass getCommand() {
		return (DownloadMass) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public DownloadMassHook getHook() {
		return (DownloadMassHook) super.getHook();
	}

}