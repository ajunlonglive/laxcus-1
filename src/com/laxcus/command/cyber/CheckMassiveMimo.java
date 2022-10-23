/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检测内网/公网、网段之间是不是能够运行MASSIVE MIMO。<br>
 * 受限于NAT网络的点对点通信设计，NAT网络不能支持“发送和接收”端多个SOCKET之间的轮循收收发通信。
 * 目前MASSIVE MIMO只能支持SOCKET直接连接的网络环境。
 * 
 * 这个命令只在FRONT节点执行，检查FRONT与ENTRANCE、GATE、CALL节点之间的通信信道，包括命令控制信道和数据传输信道。
 * 
 * @author scott.liang
 * @version 1.0 2/21/2022
 * @since laxcus 1.0
 */
public class CheckMassiveMimo extends Command {

	private static final long serialVersionUID = -7690762608712793679L;
	
	/** 服务器地址 **/
	private Node site;

	/**
	 * 根据传入的检测穿透信道命令，生成它的数据副本
	 * @param that 检测穿透信道命令
	 */
	private CheckMassiveMimo(CheckMassiveMimo that) {
		super(that);
		site = that.site;
	}

	/**
	 * 构造检测穿透信道命令
	 */
	public CheckMassiveMimo() {
		super();
	}
	
	/**
	 * 从可类化数据读取器中解析检测穿透信道命令
	 * @param reader 可类化数据读取器
	 */
	public CheckMassiveMimo(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置服务器节点，不允许空指针
	 * @param e
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);
		site = e;
	}
	
	/**
	 * 返回服务器节点
	 * @return
	 */
	public Node getSite() {
		return site;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckMassiveMimo duplicate() {
		return new CheckMassiveMimo(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new Node(reader);
	}

}