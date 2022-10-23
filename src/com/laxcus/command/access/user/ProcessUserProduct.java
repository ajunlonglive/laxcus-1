/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 用户处理报告
 * 
 * @author scott.liang
 * @version 1.1 6/26/2015
 * @since laxcus 1.0
 */
public abstract class ProcessUserProduct extends MultiProcessProduct {

	private static final long serialVersionUID = -7171807215852768688L;
	
	/** 用户签名 **/
	private Siger username;
	
	/**
	 * 构造默认的账号处理报告
	 */
	protected ProcessUserProduct() {
		super();
	}

	/**
	 * 生成一个账号处理报告数据副本
	 * @param that ProcessUserProduct实例
	 */
	public ProcessUserProduct(ProcessUserProduct that) {
		super(that);
		username = that.username;
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		username = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInstance(username);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		username = reader.readInstance(Siger.class);
	}
}