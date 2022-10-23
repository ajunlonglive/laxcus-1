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
 * 单账号授权解除命令。<br><br>
 *  
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class SingleRevoke extends SingleCertificate {

	private static final long serialVersionUID = 7179606293029887056L;

	/**
	 * 根据传入的单账号授权解除命令，生成它的数据副本
	 * @param that 单账号授权解除命令实例
	 */
	private SingleRevoke(SingleRevoke that) {
		super(that);
	}
	
	/**
	 * 构造默认的单账号授权解除命令
	 */
	public SingleRevoke() {
		super();
	}

	/**
	 * 从可类化读取器中解析单账号授权解除命令
	 * @param reader 可类化读取器
	 */
	public SingleRevoke(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据当前单账号授权解除命令，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SingleRevoke duplicate() {
		return new SingleRevoke(this);
	}

}
