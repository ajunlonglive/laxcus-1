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
 * 推送WORK站点元数据。<br>
 * 这个命令由HOME站点发出，目标是CALL站点。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public final class PushWorkField extends PushField {
	
	private static final long serialVersionUID = -5135374931019988299L;

	/**
	 * 根据传入的推送WORK站点元数据，生成它的数据副本
	 * @param that 推送WORK站点元数据实例
	 */
	private PushWorkField(PushWorkField that) {
		super(that);
	}

	/**
	 * 构造默认和私有的推送WORK站点元数据
	 */
	private PushWorkField() {
		super();
	}

	/**
	 * 构造推送WORK站点元数据，指定WORK站点地址
	 * @param from WORK站点地址
	 */
	public PushWorkField(Node from) {
		this();
		setNode(from);
	}

	/**
	 * 从可类化数据读取器中解析推送WORK站点元数据
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public PushWorkField(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PushWorkField duplicate() {
		return new PushWorkField(this);
	}

}