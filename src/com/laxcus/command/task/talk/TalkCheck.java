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
 * 分布任务组件状态查询
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class TalkCheck extends Command {

	private static final long serialVersionUID = 3972137752446587379L;

	/** 对话标识 **/
	private TalkFalg flag;
	
	/**
	 * 构造默认和私有的分布任务组件状态查询命令
	 */
	private TalkCheck() {
		super();
	}

	/**
	 * 构造分布任务组件状态查询命令，指定标记
	 * @param flag 分布会话标识
	 */
	public TalkCheck(TalkFalg flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 从可类化读取器中解析分布任务组件状态查询
	 * @param reader 可类化读取器
	 */
	public TalkCheck(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成分布任务组件状态查询命令的数据副本
	 * @param that
	 */
	private TalkCheck(TalkCheck that) {
		super(that);
		flag = that.flag;
	}

	/**
	 * 设置交互标识<br>
	 * @param e TalkTag实例
	 */
	public void setFlag(TalkFalg e) {
		Laxkit.nullabled(e);
		flag = e;
	}

	/**
	 * 返回交互标识
	 * @return TalkTag实例
	 */
	public TalkFalg getFlag() {
		return flag;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TalkCheck duplicate() {
		return new TalkCheck(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(flag);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		flag = new TalkFalg(reader);
	}

}
