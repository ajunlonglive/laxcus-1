/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 切换管理节点命令。<br><br>
 * 
 * 这个命令由TOP/HOME监视站点发出，发出命令时已经升级为新的管理站点，它通知本集群的其它监视站点，重新注册到自己的地址上。<br>
 * 
 * 这个命令由监视站点（TOP/HOME）发出，目标是同级别的监视站点。要求监视站点无条件接受这项命令，重新注册到新的管理站点下面。<br>
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public final class SwitchPartner extends Command {

	private static final long serialVersionUID = 4411803835162864026L;

	/** 管理站点地址 **/
	private Node hub;

	/**
	 * 根据传入的“SWITCH PARTNER”命令，生成它的数据副本
	 * @param that SwitchPartner实例
	 */
	private SwitchPartner(SwitchPartner that) {
		super(that);
		hub = that.hub;
	}

	/**
	 * 构造默认和私有的“SWITCH PARTNER”命令
	 */
	private SwitchPartner() {
		super();
	}

	/**
	 * 构造“SWITCH PARTNER”命令，指定目标地址。
	 * @param hub 管理站点地址
	 */
	public SwitchPartner(Node hub) {
		this();
		setHub(hub);
	}

	/**
	 * 从可类化数据读取器中解析“SWITCH PARTNER”命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SwitchPartner(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置管理站点地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e.duplicate();
	}

	/**
	 * 返回管理站点地址
	 * @return Node实例
	 */
	public Node getHub() {
		return hub;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SwitchPartner duplicate() {
		return new SwitchPartner(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(hub);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		hub = new Node(reader);
	}

}