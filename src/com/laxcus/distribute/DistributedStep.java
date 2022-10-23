/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分布式处理步骤。<br><br>
 * 
 * 分布式处理步骤是对分布式会话的简单包装，用在客户机向服务器投递命令的操作过程中。发生在CALL/DATA/WORK/BUILD站点之间。<br>
 * 它的子类包括属于CONDUCT命令的FROM/TO阶段会话，和属于ESTABLISH命令的SCAN/SIFT/RISE阶段会话。
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public abstract class DistributedStep extends Command {

	private static final long serialVersionUID = 612486331752458522L;

	/** 操作步骤会话 **/
	private StepSession session;
	
	/** 最后一次迭代，默认是假 **/
	private boolean last;

	/**
	 * 构造默认的分布式执行过程中的分布式处理步骤
	 */
	protected DistributedStep() {
		super();
		last = false;
	}

	/**
	 * 根据传入实例，构造一个浅层分布式执行过程中的分布式处理步骤的副本
	 * @param that 分布式执行过程中的分布式处理步骤实例
	 */
	protected DistributedStep(DistributedStep that) {
		super(that);
		session = that.session;
		last = that.last;
	}
	
	/**
	 * 设置操作步骤会话。不允许空指针。
	 * 
	 * @param e StepSession实例
	 */
	public void setSession(StepSession e) {
		Laxkit.nullabled(e);
		session = e;
	}
	
	/**
	 * 返回操作步骤会话
	 * @return StepSession实例
	 */
	public StepSession getSession() {
		return session;
	}

	/**
	 * 设置最后一次迭代会话
	 * @param b 真或者假
	 */
	public void setLast(boolean b) {
		last = b;
	}

	/**
	 * 判断是最后一次迭代会话
	 * @return 真或者假
	 */
	public boolean isLast() {
		return last;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeDefault(session);
		writer.writeBoolean(last);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		session = (StepSession) reader.readDefault();
		last = reader.readBoolean();
	}

}