/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

/**
 * CallDeleteInvoker的“launch”阶段备注
 * 
 * @author scott.liang
 * @version 1.0 5/20/2015
 * @since laxcus 1.0
 */
final class DeleteLaunchRemark {

	/** 准备投递的站点数目 **/
	private int sites;

	/** 投递成功的站点数目 **/
	private int expresses;

	/**
	 * 构造默认的CALL站点删除标记
	 */
	public DeleteLaunchRemark() {
		sites = expresses = 0;
	}

	/**
	 * 设置准备投递的站点数目
	 * @param i
	 */
	public void setSites(int i) {
		sites = i;
	}

	/**
	 * 返回准备投递的站点数目
	 * @return
	 */
	public int getSites() {
		return sites;
	}

	/**
	 * 设置投递成功的站点数目
	 * @param i
	 */
	public void setExpresses(int i) {
		expresses = i;
	}

	/**
	 * 返回投递成功的站点数目
	 * @return
	 */
	public int getExpresses() {
		return expresses;
	}

	/**
	 * 判断绝对成功
	 * @return
	 */
	public boolean isAbsoluteSuccessful() {
		return sites > 0 && sites == expresses;
	}

	/**
	 * 判断绝对失败
	 * @return
	 */
	public boolean isAbsoluteFailed() {
		return sites > 0 && expresses == 0;
	}

	/**
	 * 判断部分成功（包括完全成功）
	 * @return
	 */
	public boolean isPossibleSuccessful() {
		return sites > 0 && expresses > 0 && expresses <= sites;
	}
}