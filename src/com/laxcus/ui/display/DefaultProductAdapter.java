/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ui.display;

/**
 * 默认异步监听器 <br><br>
 * 
 * 采取等待方式(wait方法)，接收来自服务器端的应答
 * 
 * @author laxcus programer
 * @version 1.0 4/7/2022
 * @since laxcus 1.0
 */
public class DefaultProductAdapter implements ProductListener {

	/** 等待状态，默认是“真” **/
	private boolean awaiting;
	
	/** 异步应答结果 **/
	private Object product;

	/**
	 * 构造默认异步监听器
	 */
	public DefaultProductAdapter() {
		super();
		reset();
	}

	/**
	 * 返回结果
	 * @return
	 */
	public Object getProduct() {
		return product;
	}

	/**
	 * 重新设置准备进入等待状态
	 */
	public void reset() {
		awaiting = true;
	}

	/**
	 * 执行延时
	 * @param ms 超时时间，单位：毫秒
	 */
	private synchronized void delay(long ms) {
		try {
			super.wait(ms);
		} catch (InterruptedException e) {

		}
	}

	/**
	 * 唤醒延时
	 */
	private synchronized void wakeup() {
		try {
			notify();
		} catch (IllegalMonitorStateException e) {

		}
	}

	/**
	 * 触发唤醒对象
	 */
	public void done() {
		awaiting = false;
		wakeup();
	}

	/**
	 * 进入等待，调用端调用这个方法
	 */
	public void await() {
		// 进行等待状态
		while (awaiting) {
			delay(1000L);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.ProductListener#push(com.laxcus.echo.product.EchoProduct)
	 */
	@Override
	public void push(Object e) {
		// 保存结果
		product = e;
		// 唤醒事件
		done();
	}

}