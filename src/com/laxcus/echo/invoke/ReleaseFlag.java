/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import com.laxcus.echo.*;

/**
 * 被释放异步调用器标识 <br>
 * 
 * @author scott.liang
 * @version 1.0 07/09/2009
 * @since laxcus 1.0
 */
final class ReleaseFlag {

	/** 调用器编号 **/
	private long invokerId;

	/** 命令来源的回显地址 **/
	private Cabin cabin;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		cabin = null;
	}

	/**
	 * 构造被释放异步调用器标识，指定全部参数
	 * @param invokerId 调用器编号
	 * @param cabin 命令来源的回显地址
	 */
	public ReleaseFlag(long invokerId, Cabin cabin){
		super();
		setInvokerId(invokerId);
		setCabin(cabin);
	}

	/**
	 * 设置调用器编号 
	 * @param id 调用器编号
	 */
	public void setInvokerId(long id) {
		invokerId = id;
	}

	/**
	 * 返回调用器编号 
	 * @return 调用器编号
	 */
	public long getInvokerId() {
		return invokerId;
	}

	/**
	 * 设置命令来源的回显地址
	 * @param e 回显地址
	 */
	public void setCabin(Cabin e) {
		cabin = e;
	}

	/**
	 * 返回命令来源的回显地址
	 * @return Cabin实例
	 */
	public Cabin getCabin() {
		return cabin;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (cabin != null) {
			return String.format("%d $ %s", invokerId, cabin);
		} else {
			return String.format("%d", invokerId);
		}
	}
}