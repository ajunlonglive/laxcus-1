/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 投递分布任务组件应用附件包。<br>
 * 
 * 流程：<br>
 * 1. FRONT -> GATE <br>
 * 2. GATE (TakeJobSite) -> DATA/WORK/BUILD/CALL <br>
 * 3. GATE -> FRONT <br><br>
 * 
 * @author scott.liang
 * @version 1.0 10/8/2019
 * @version 1.1 3/21/2020
 * @since laxcus 1.0
 */
public class MailTaskAssistComponent extends Command {

	private static final long serialVersionUID = 8162617725214447834L;

	/** 附件包投递位置，见PhaseTag中的定义 **/
	private int family;
	
	/**
	 * 构造默认和私有的投递分布任务组件应用附件包
	 */
	private MailTaskAssistComponent() {
		super();
	}

	/**
	 * 构造投递分布任务组件应用附件包的数据副本
	 * @param that 投递分布任务组件应用附件包
	 */
	protected MailTaskAssistComponent(MailTaskAssistComponent that) {
		super(that);
		family = that.family;
	}

	/**
	 * 构造投递分布任务组件应用附件包，指定阶段类型
	 * @param family 阶段类型
	 */
	public MailTaskAssistComponent(int family) {
		this();
		setFamily(family);
	}

	/**
	 * 从可类化读取器中解析投递分布任务组件应用附件包
	 * @param reader 可类化数据读取器
	 */
	public MailTaskAssistComponent(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置投递阶段类型
	 * @param who 投递阶段类型
	 */
	public void setFamily(int who) {
		if (!PublishTaskAxes.isPhase(who)) {
			throw new IllegalValueException("illegal family %d", who);
		}
		family = who;
	}

	/**
	 * 返回投递阶段类型
	 * @return 投递阶段类型
	 */
	public int getFamily() {
		return family;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public MailTaskAssistComponent duplicate() {
		return new MailTaskAssistComponent(this);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(family);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		family = reader.readInt();
	}

}