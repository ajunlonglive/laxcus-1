/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

/**
 * 记录用户执行搜索发生的时间 <br>
 * 
 * @author scott.liang
 * @version 1.0 6/4/2019
 * @since laxcus 1.0
 */
final class TrackTime {

	/** 刻度时间 **/
	private long time;

	/**
	 * 构造记录用户执行搜索发生的时间
	 */
	public TrackTime() {
		super();
		refreshTime();
	}

	/**
	 * 更新时间
	 */
	public void refreshTime() {
		time = System.currentTimeMillis();
	}
	
	/**
	 * 时间清除为0
	 */
	public void resetTime() {
		time = 0;
	}

	/**
	 * 判断达到超时删除时间<br><br>
	 * 
	 * @param timeout 超时时限
	 * @return 返回真或者假。
	 */
	public boolean isTimeout(long timeout) {
		return (System.currentTimeMillis() - time >= timeout);
	}

}