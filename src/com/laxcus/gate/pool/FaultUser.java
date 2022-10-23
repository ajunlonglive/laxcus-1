/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import com.laxcus.access.diagram.*;
import com.laxcus.util.*;

/**
 * 登录失败 <br>
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public final class FaultUser {

	/** 账号 **/
	private User user;

	/** 刻度时间 **/
	private long scaleTime;

	/**
	 * 构造默认的登录失败
	 */
	public FaultUser(User user) {
		super();
		refreshTime();
		setUser(user);
	}

	/**
	 * 更新时间
	 */
	private void refreshTime() {
		scaleTime = System.currentTimeMillis();
	}

	/**
	 * 判断达到超时删除时间<br><br>
	 * 
	 * @param timeout 超时时限
	 * @return 返回真或者假。
	 */
	public boolean isTimeout(long timeout) {
		return (System.currentTimeMillis() - scaleTime >= timeout);
	}

	/**
	 * 设置账号
	 * @param e 账号实例
	 */
	public void setUser(User e) {
		Laxkit.nullabled(e);
		user = e;
	}

	/**
	 * 输出账号
	 * @return 账号实例
	 */
	public User getUser() {
		return user;
	}

}