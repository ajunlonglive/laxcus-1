/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置用户的命令权级。
 * 
 * @author scott.liang
 * @version 1.0 10/06/2021
 * @since laxcus 1.0
 */
public class SetSingleUserPriority extends SetSingleUserParameter {

	private static final long serialVersionUID = 2777692526095900873L;

	/** 命令权级 **/
	private int userPriority;

	/**
	 * 根据传入的设置用户的命令权级命令，生成它的数据副本
	 * @param that 设置用户的命令权级命令
	 */
	private SetSingleUserPriority(SetSingleUserPriority that) {
		super(that);
		userPriority = that.userPriority;
	}

	/**
	 * 构造默认和私有的设置用户的命令权级命令
	 */
	private SetSingleUserPriority() {
		super();
	}

	/**
	 * 构造设置用户的命令权级，指定数目
	 * @param siger 用户签名
	 * @param priority 命令权级
	 */
	public SetSingleUserPriority(Siger siger, int priority) {
		this();
		setSiger(siger);
		setUserPriority(priority);
	}

	/**
	 * 从可类化数据读取器中解析设置用户的命令权级命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleUserPriority(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户的命令权级数
	 * @param who 命令权级
	 */
	public void setUserPriority(int who) {
		if (!(CommandPriority.NONE <= who && who <= CommandPriority.MAX)) {
			throw new IllegalValueException("Illegal %d", who);
		}
		userPriority = who;
	}

	/**
	 * 返回命令权级
	 * @return 命令权级
	 */
	public int getUserPriority() {
		return userPriority;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleUserPriority duplicate() {
		return new SetSingleUserPriority(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(userPriority);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		userPriority = reader.readInt();
	}

}