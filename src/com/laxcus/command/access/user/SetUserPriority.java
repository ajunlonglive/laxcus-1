/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置用户命令的权级。<br>
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/06/2021
 * @since laxcus 1.0
 */
public final class SetUserPriority extends SetMultiUserParameter {

	private static final long serialVersionUID = -1820920976369793542L;

	/** 用户命令权级 **/
	private int userPriority;

	/**
	 * 根据传入的设置用户命令的权级，生成它的数据副本
	 * @param that 设置用户命令的权级
	 */
	private SetUserPriority(SetUserPriority that) {
		super(that);
		userPriority = that.userPriority;
	}

	/**
	 * 构造默认和私有的设置用户命令的权级
	 */
	private SetUserPriority() {
		super();

		userPriority = CommandPriority.NONE;
	}

	/**
	 * 构造设置用户命令权级
	 * @param priority 用户命令权级
	 */
	public SetUserPriority(int priority) {
		this();
		setUserPriority(priority);
	}

	/**
	 * 构造设置用户命令的权级，指定用户签名
	 * @param siger 用户签名
	 */
	public SetUserPriority(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置用户命令的权级
	 * @param reader 可类化数据读取器
	 */
	public SetUserPriority(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户命令的权级
	 * @param who 设置用户命令的权级
	 */
	public void setUserPriority(int who) {
		if (!(CommandPriority.NONE <= who && who <= CommandPriority.MAX)) {
			throw new IllegalValueException("Illegal %d", who);
		}

		userPriority = who;
	}

	/**
	 * 返回设置用户命令的权级
	 * @return 设置用户命令的权级
	 */
	public int getUserPriority() {
		return userPriority;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleUserPriority e = new SetSingleUserPriority(siger, userPriority);
			array.add(e);
		}
		return array;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetUserPriority duplicate() {
		return new SetUserPriority(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(userPriority);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		userPriority = reader.readInt();
	}
}