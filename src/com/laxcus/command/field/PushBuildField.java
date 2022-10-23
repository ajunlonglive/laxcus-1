/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 推送BUILD站点元数据。<br>
 * 这个命令由HOME站点发出，目标是CALL站点。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public final class PushBuildField extends PushField {

	private static final long serialVersionUID = 5847561133632005457L;

	/**
	 * 根据传入的推送BUILD站点元数据，生成它的数据副本
	 * @param that PushBuildField实例
	 */
	private PushBuildField(PushBuildField that) {
		super(that);
	}

	/**
	 * 构造默认和私有的推送BUILD站点元数据
	 */
	private PushBuildField() {
		super();
	}

	/**
	 * 构造推送BUILD站点元数据，指定BUILD站点地址
	 * @param node BUILD站点地址
	 */
	public PushBuildField(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析推送BUILD站点元数据
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public PushBuildField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PushBuildField duplicate() {
		return new PushBuildField(this);
	}

}