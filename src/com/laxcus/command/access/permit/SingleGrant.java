/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.util.classable.*;

/**
 * 单账号授权命令。<br><br>
 *  
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class SingleGrant extends SingleCertificate {

	private static final long serialVersionUID = 3641202560160460591L;

	/**
	 * 根据传入的单账号授权命令，生成它的数据副本
	 * @param that 单账号授权命令实例
	 */
	private SingleGrant(SingleGrant that) {
		super(that);
	}
	
	/**
	 * 构造默认的单账号授权命令
	 */
	public SingleGrant() {
		super();
	}

	/**
	 * 从可类化读取器中解析单账号授权命令
	 * @param reader 可类化读取器
	 */
	public SingleGrant(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据当前单账号授权命令，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SingleGrant duplicate() {
		return new SingleGrant(this);
	}

}
