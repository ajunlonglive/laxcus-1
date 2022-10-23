/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.account;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 刷新已经发布的工作组件。组件类型包括：分布任务组件、码位计算器、快捷组件。<br><br>
 * 
 * 当GATE站点向ACCOUNT站点成功上传一个组件（分布任务、码位计算器、快捷组件）后，通过BANK -> TOP -> HOME，
 * 通知到相关的任务站点（CALL/DATA/WORK/BUILD），这些任务站点根据自身情况，判断和下载新的组件。<br><br>
 * 
 * 这是一个单向处理命令，不需要反馈结果。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/12/2018
 * @since laxcus 1.0
 */
public final class RefreshPublish extends Command {

	private static final long serialVersionUID = -513659473571114662L;
	
	/** ACCOUNT站点 **/
	private Node remote;

	/** 用户签名 **/
	private Siger siger;

	/** 分布任务组件的阶段类型 **/
	private int taskFamily;

	/**
	 * 根据传入的刷新已经发布的任务组件，生成它的数据副本
	 * @param that RefreshPublish实例
	 */
	private RefreshPublish(RefreshPublish that) {
		super(that);
		remote = that.remote;
		siger = that.siger;
		taskFamily = that.taskFamily;
	}

	/**
	 * 构造默认和私有的刷新已经发布的任务组件。
	 */
	private RefreshPublish() {
		super();
	}


	/**
	 * 构造刷新已经发布的任务组件，指定用户签名。
	 * @param account ACCOUNT站点
	 */
	public RefreshPublish(Node account) {
		this();
		setRemote(account);
	}
	
	/**
	 * 构造刷新已经发布的任务组件，指定用户签名。
	 * @param siger 用户签名
	 */
	private RefreshPublish(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 构造刷新已经发布的任务组件，指定用户签名。
	 * @param account ACCOUNT站点
	 * @param siger 用户签名
	 */
	public RefreshPublish(Node account, Siger siger) {
		this(siger);
		setRemote(account);
	}

	/**
	 * 从可类化数据读取器中解析刷新已经发布的任务组件
	 * @param reader 可类化数据读取器
	 */
	public RefreshPublish(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置ACCOUNT站点，不允许空指针
	 * @param e ACCOUNT站点
	 */
	public void setRemote(Node e) {
		Laxkit.nullabled(e);
		remote = e;
	}

	/**
	 * 返回ACCOUNT站点
	 * @return ACCOUNT站点
	 */
	public Node getRemote() {
		return remote;
	}

	/**
	 * 设置用户签名，允许空指针。
	 * @param e Siger实例
	 * @throws NullPointerException
	 */
	public void setSiger(Siger e) {
		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}
	
	/**
	 * 设置发布的分布任务组件类型
	 * @param who 分布任务组件类型
	 */
	public void setTaskFamily(int who) {
		if (!PhaseTag.isPhase(who)) {
			throw new IllegalValueException("illegal task type:%d", who);
		}
		taskFamily = who;
	}

	/**
	 * 返回发布的分布任务组件类型
	 * @return 分布任务组件类型
	 */
	public int getTaskFamily() {
		return taskFamily;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RefreshPublish duplicate() {
		return new RefreshPublish(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(remote);
		writer.writeInstance(siger);
		writer.writeInt(taskFamily);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		remote = new Node(reader);
		siger = reader.readInstance(Siger.class);
		taskFamily = reader.readInt();
	}

}