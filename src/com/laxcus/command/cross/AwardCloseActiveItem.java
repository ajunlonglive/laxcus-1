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
 * 强制关闭授权单元 <br><br>
 * 
 * 这个命令是把授权单元从授权人账号中移除。
 * 
 * @author scott.liang
 * @version 1.0 7/29/2017
 * @since laxcus 1.0
 */
public class AwardCloseActiveItem extends AwardShareCross {

	private static final long serialVersionUID = 3564932483617997004L;

	/**
	 * 构造默认的强制关闭授权单元
	 */
	public AwardCloseActiveItem() {
		super();
	}

	/**
	 * 构造强制关闭授权单元，指定分享单元
	 * 
	 * @param field CrossField实例
	 */
	public AwardCloseActiveItem(CrossField field) {
		this();
		setField(field);
	}
	
	/**
	 * 从可类化数据读取器中解析强制关闭授权单元命令
	 * @param reader 可类化数据读取器
	 */
	public AwardCloseActiveItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成强制关闭授权单元的数据副本
	 * 
	 * @param that AwardCloseActiveItem实例
	 */
	private AwardCloseActiveItem(AwardCloseActiveItem that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardCloseActiveItem duplicate() {
		return new AwardCloseActiveItem(this);
	}

}