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
 * 强制开放被授权单元 <br><br>
 * 
 * 这个命令是把被授权单元保存到被授权人账号里。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/29/2017
 * @since laxcus 1.0
 */
public class AwardOpenPassiveItem extends AwardSharePassiveItem {

	private static final long serialVersionUID = -3549954994777331312L;

	/**
	 * 构造默认的强制开放被授权单元
	 */
	public AwardOpenPassiveItem() {
		super();
	}

	/**
	 * 构造强制开放被授权单元，指定分享单元
	 * @param field CrossField实例
	 */
	public AwardOpenPassiveItem(CrossField field) {
		this();
		setField(field);
	}

	/**
	 * 从可类化数据读取器中解析强制开放被授权单元命令
	 * @param reader 可类化数据读取器
	 */
	public AwardOpenPassiveItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成强制开放被授权单元的数据副本
	 * 
	 * @param that AwardOpenPassiveItem实例
	 */
	private AwardOpenPassiveItem(AwardOpenPassiveItem that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardOpenPassiveItem duplicate() {
		return new AwardOpenPassiveItem(this);
	}

}