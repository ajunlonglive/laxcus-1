/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.util.*;

/**
 * 数据处理设定命令 <br><br>
 * 
 * 当数据的处理节点完成数据处理工作后，生成这个命令的子类，发送到命令请求节点。如DATA节点和CALL节点之间的关系。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/25/2015
 * @since laxcus 1.0
 */
public abstract class AssumeConsult extends Consult {

	private static final long serialVersionUID = -271833618741281173L;

	/**
	 * 构造默认的数据处理设定命令
	 */
	protected AssumeConsult() {
		super();
	}

	/**
	 * 生成数据处理设定命令的数据副本
	 * @param that AssumeConsult实例
	 */
	protected AssumeConsult(AssumeConsult that) {
		super(that);
	}

	/**
	 * 设置提交状态，见ConsultStatus定义
	 * 
	 * @param who 提交状态
	 */
	public final void setStatus(byte who) {
		if (!ConsultStatus.isAssume(who)) {
			throw new IllegalValueException("illegal status %d", who);
		}
		super.setStatus(who);
	}
	
	/**
	 * 判断是DATA站点处理成功
	 * @return 返回真或者假
	 */
	public boolean isSuccess() {
		return ConsultStatus.isSuccess(getStatus());
	}

	/**
	 * 判断是DATA站点处理失败
	 * @return 返回真或者假
	 */
	public boolean isFailed() {
		return ConsultStatus.isFailed(getStatus());
	}

	/**
	 * 判断是DATA站点再次确认成功
	 * @return 返回真或者假
	 */
	public boolean isConfirmSuccess() {
		return ConsultStatus.isConfirmSuccess(getStatus());
	}

	/**
	 * 判断是DATA站点取消成功
	 * @return 返回真或者假
	 */
	public boolean isCancelSuccess() {
		return ConsultStatus.isCancelSuccess(getStatus());
	}

	/**
	 * 判断是DATA站点取消失败
	 * @return 返回真或者假
	 */
	public boolean isCancelFailed() {
		return ConsultStatus.isCancelFailed(getStatus());
	}
}