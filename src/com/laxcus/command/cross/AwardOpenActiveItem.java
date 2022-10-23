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
 * 强制开放授权单元 <br><br>
 * 
 * 这个命令是把授权单元保存到授权人账号中
 * 
 * @author scott.liang
 * @version 1.0 7/29/2017
 * @since laxcus 1.0
 */
public class AwardOpenActiveItem extends AwardShareCross {

	private static final long serialVersionUID = -3385845247084021545L;

	/**
	 * 构造默认的强制开放授权单元
	 */
	public AwardOpenActiveItem() {
		super();
	}

	/**
	 * 构造强制开放授权单元，指定分享单元
	 * 
	 * @param field CrossField实例
	 */
	public AwardOpenActiveItem(CrossField field) {
		this();
		setField(field);
	}
	
	/**
	 * 从可类化数据读取器中解析强制开放授权单元命令
	 * @param reader 可类化数据读取器
	 */
	public AwardOpenActiveItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成强制开放授权单元的数据副本
	 * 
	 * @param that AwardOpenActiveItem实例
	 */
	private AwardOpenActiveItem(AwardOpenActiveItem that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardOpenActiveItem duplicate() {
		return new AwardOpenActiveItem(this);
	}

}