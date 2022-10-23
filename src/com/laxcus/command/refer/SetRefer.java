/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.access.diagram.*;
import com.laxcus.util.classable.*;

/**
 * 设置用户资源引用 <br>
 * 
 * 这个命令发生在用户修改密码、操作权限后。由TOP发出，分发给下属的HOME，再经HOME分发给下属的CALL站点。
 * 
 * @author scott.liang
 * @version 1.1 11/23/2015
 * @since laxcus 1.0
 */
public final class SetRefer extends ProcessRefer {

	private static final long serialVersionUID = 8288831835775657031L;

	/**
	 * 构造默认和私有的设置用户资源引用命令
	 */
	private SetRefer() {
		super();
	}

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that 设置用户资源引用实例
	 */
	private SetRefer(SetRefer that) {
		super(that);
	}

	/**
	 * 构造设置用户资源引用，指定参数
	 * @param refer Refer实例
	 */
	public SetRefer(Refer refer) {
		this();
		setRefer(refer);
	}

	/**
	 * 从可类化数据读取器中解析设置用户资源引用
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SetRefer(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetRefer duplicate() {
		return new SetRefer(this);
	}

}
