/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task.talk;

import com.laxcus.command.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分布任务组件交互操作
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class TalkAsk extends Command {

	private static final long serialVersionUID = 3972137752446587379L;

	/** 对话请求指令 **/
	private TalkQuest quest;
	
	/**
	 * 构造默认和私有的分布任务组件交互操作命令
	 */
	private TalkAsk() {
		super();
	}

	/**
	 * 构造分布任务组件交互操作命令，指定对话请求
	 * @param quest 对话请求
	 */
	public TalkAsk(TalkQuest quest) {
		this();
		setQuest(quest);
	}

	/**
	 * 从可类化读取器中解析分布任务组件交互操作
	 * @param reader 可类化读取器
	 */
	public TalkAsk(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成分布任务组件交互操作命令的数据副本
	 * @param that 分布任务组件交互操作
	 */
	private TalkAsk(TalkAsk that) {
		super(that);
		quest = that.quest;
	}

	/**
	 * 设置交互标识<br>
	 * @param e TalkQuest实例
	 */
	public void setQuest(TalkQuest e) {
		Laxkit.nullabled(e);
		quest = e;
	}

	/**
	 * 返回交互标识
	 * @return TalkQuest实例
	 */
	public TalkQuest getQuest() {
		return quest;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TalkAsk duplicate() {
		return new TalkAsk(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeDefault(quest);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		quest = (TalkQuest) reader.readDefault();
	}

}