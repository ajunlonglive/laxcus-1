/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import com.laxcus.law.cross.*;
import com.laxcus.util.classable.*;

/**
 * 强制关闭被授权单元 <br><br>
 * 
 * 这个命令是把被授权单元从被授权人账号中移除
 * 
 * @author scott.liang
 * @version 1.0 7/29/2017
 * @since laxcus 1.0
 */
public class AwardClosePassiveItem extends AwardSharePassiveItem {

	private static final long serialVersionUID = -4422380254563429701L;

	/**
	 * 构造默认的强制关闭被授权单元
	 */
	public AwardClosePassiveItem() {
		super();
	}

	/**
	 * 构造强制关闭被授权单元，指定分享单元
	 * @param field CrossField实例
	 */
	public AwardClosePassiveItem(CrossField field) {
		this();
		setField(field);
	}
	
	/**
	 * 从可类化数据读取器中解析强制关闭被授权单元命令
	 * @param reader 可类化数据读取器
	 */
	public AwardClosePassiveItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成强制关闭被授权单元的数据副本
	 * 
	 * @param that AwardClosePassiveItem实例
	 */
	private AwardClosePassiveItem(AwardClosePassiveItem that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardClosePassiveItem duplicate() {
		return new AwardClosePassiveItem(this);
	}

}