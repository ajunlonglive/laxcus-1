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
 * 黑账号 <br>
 * 
 * @author scott.liang
 * @version 1.0 8/2/2018
 * @since laxcus 1.0
 */
public final class BlackUser {

	/** 账号 **/
	private User user;
	
	/** 重试统计值 **/
	private int retries;

	/** 刻度时间 **/
	private long scaleTime;

	/**
	 * 构造默认的黑账号
	 */
	public BlackUser(User user) {
		super();
		retries = 0;
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

	/**
	 * 递增值
	 */
	public void increase() {
		retries++;
	}
	
	/**
	 * 判断达到最大化值
	 * @param max
	 * @return
	 */
	public boolean isMaxRetry(int max) {
		return retries >= max;
	}
}