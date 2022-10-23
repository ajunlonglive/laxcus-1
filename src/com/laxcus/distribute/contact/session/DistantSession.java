/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.contact.session;

import com.laxcus.distribute.calculate.cyber.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;
import com.laxcus.site.Node;

/**
 * 快速计算的DISTANT阶段会话。
 * DISTANT阶段会话包含多个DISTANT阶段子级的SUBDISTANT阶段。
 * 包括DISTANT阶段的子级：SUBDISTANT阶段。<br>
 * 用户可以从这个类继续派生新的接口。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/9/2020
 * @since laxcus 1.0
 */
public final class DistantSession extends ContactSession {

	private static final long serialVersionUID = 7406149279448736789L;

	/** 当上次数据下载完成时，通知上级节点释放实体数据。默认是“真” **/
	private boolean autoRelease;

	/** 网络数据空间，包含任意多个模值、主机地址、任务编号、数据范围的信息 **/
	private CyberSphere sphere;

	/*
	 * 将DISTANT阶段会话写入可类化存储器
	 * @see com.laxcus.distribute.contact.session.ContactSession#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 1. 生成前缀
		super.buildSuffix(writer);
		// 2. 释放数据
		writer.writeBoolean(autoRelease);
		// 3.分布信息
		writer.writeInstance(sphere);
	}

	/*
	 * 从可类化读取器中解析DISTANT阶段会话
	 * @see com.laxcus.distribute.contact.session.ContactSession#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 1. 解析前缀
		super.resolveSuffix(reader);
		// 2. 释放数据
		autoRelease = reader.readBoolean();
		// 3.解析分布
		sphere = reader.readInstance(CyberSphere.class);
	}

	/**
	 * 根据传入的DISTANT阶段会话实例，生成它的副本
	 * @param that DistantSession实例
	 */
	private DistantSession(DistantSession that) {
		super(that);
		autoRelease = that.autoRelease;
		sphere = that.sphere;
	}

	/**
	 * 构造默认的DISTANT阶段会话
	 */
	public DistantSession() {
		super(PhaseTag.DISTANT);
		// 默认是“真”
		setAutoRelease(true);
	}

	/**
	 * 构造DISTANT阶段会话，指定阶段命名
	 * @param phase 阶段命名
	 */
	public DistantSession(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 构造DISTANT阶段会话，并且指定连接的目标地址
	 * @param remote 目标站点地址
	 */
	public DistantSession(Node remote) {
		this();
		setRemote(remote);
	}

	/**
	 * 构造DISTANT阶段会话，并且指定它的阶段命名和连接的目标地址
	 * 
	 * @param phase 阶段命名
	 * @param remote 目标站点地址
	 */
	public DistantSession(Phase phase, Node remote) {
		this(phase);
		setRemote(remote);
	}

	/**
	 * 从可类化数据读取器中解析DISTANT会话参数
	 * 
	 * @param reader 可类化数据读取器
	 */
	public DistantSession(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置自动释放数据。<br>
	 * 当下载完成时，要求上级节点去释放已经下载的数据域（FluxField指向的实体数据）。此操作在WorkDistantContactInvoker判断和执行。
	 * @param b 自动释放数据标记
	 */
	public void setAutoRelease(boolean b) {
		autoRelease = b;
	}

	/**
	 * 判断自动释放数据 <br>
	 * @return 返回“真”或者“假”。
	 */
	public boolean isAutoRelease() {
		return autoRelease;
	}

	/**
	 * 设置网络数据区
	 * @param e CyberSphere实例
	 */
	public void setSphere(CyberSphere e) {
		sphere = e;
	}

	/**
	 * 返回网络数据区
	 * @return CyberSphere实例
	 */
	public CyberSphere getSphere() {
		return sphere;
	}

	/**
	 * 生成DISTANT阶段会话任务的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public DistantSession duplicate() {
		return new DistantSession(this);
	}
}