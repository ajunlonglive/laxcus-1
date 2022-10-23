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
 * 建立用户资源引用 <br>
 * 
 * 这个命令发生在HOME站点建立账户时，再经HOME分发给下属的CALL/DATA/WORK/BUILD站点。
 * 
 * @author scott.liang
 * @version 1.1 11/23/2015
 * @since laxcus 1.0
 */
public final class AwardCreateRefer extends ProcessRefer {

	private static final long serialVersionUID = -1280073524991999287L;

	/**
	 * 构造默认和私有的建立用户资源引用命令
	 */
	private AwardCreateRefer() {
		super();
	}

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that AwardCreateRefer实例
	 */
	private AwardCreateRefer(AwardCreateRefer that) {
		super(that);
	}

	/**
	 * 构造建立用户资源引用命令，指定参数
	 * @param refer Refer实例
	 */
	public AwardCreateRefer(Refer refer) {
		this();
		setRefer(refer);
	}

	/**
	 * 从可类化数据读取器中解析建立用户资源引用
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AwardCreateRefer(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardCreateRefer duplicate() {
		return new AwardCreateRefer(this);
	}

}