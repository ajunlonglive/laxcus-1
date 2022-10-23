/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.pool;

import com.laxcus.command.cloud.*;
import com.laxcus.util.*;

/**
 * 预写入软件包元组 <br>
 * 
 * @author scott.liang
 * @version 1.0 8/22/2020
 * @since laxcus 1.0
 */
final class WritePackageTuple implements Comparable<WritePackageTuple>{

	/** 等待状态，默认是“真” **/
	private boolean awaiting;
	
	/** 用户签名 **/
	private Siger issuer;

	/** 分布式应用软件包 **/
	private CloudPackageComponent component;

	/** 成功或者失败 **/
	private boolean successful;

	/** 超时时间，默认1小时 **/
	private long timeout;

	/*
	 * (non-Javadoc)
	 * @see java.lang.File#finalize()
	 */
	@Override
	public void finalize() {
		issuer = null;
		component = null;
	}
	
	/**
	 * 预写入软件包元组
	 * @param component 分布式应用软件包
	 * @param timeout 超时时间
	 */
	public WritePackageTuple(Siger issuer, CloudPackageComponent component, long timeout) {
		super();
		setIssuer(issuer);
		setComponent(component);
		setTimeout(timeout);
		awaiting = true; // 准备进入等待
		successful = false; // 默认不成功
	}

	/**
	 * 构造预写入软件包元组，指定文件
	 * @param issuer 用户签名
	 * @param component 分布式应用软件包
	 */
	public WritePackageTuple(Siger issuer, CloudPackageComponent component) {
		this(issuer, component, 3600 * 1000);
	}
	
	/**
	 * 最大超时时间
	 * @param ms 毫秒
	 */
	public void setTimeout(long ms) {
		if(ms > 0)timeout = ms;
	}

	/**
	 * 设置基于秒时间的超时
	 * @param seconds
	 */
	public void setTimeoutWithSecond(int seconds) {
		setTimeout(seconds * 1000);
	}

	/**
	 * 返回超时时间
	 * @return 时间（长整型）
	 */
	public long getTimeout() {
		return timeout;
	}
	
	/**
	 * 设置用户签名
	 * @param e 签名实例
	 */
	public void setIssuer(Siger e) {
		issuer = e;
	}
	
	/**
	 * 返回用户签名
	 * @return 签名实例
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 设置分布式应用软件包
	 * @param e 分布式应用软件包
	 */
	private final void setComponent(CloudPackageComponent e) {
		Laxkit.nullabled(e);
		component = e;
	}

	/**
	 * 返回分布式应用软件包
	 * @return 分布式应用软件包
	 */
	public CloudPackageComponent getComponent() {
		return component;
	}

	/**
	 * 执行延时
	 * @param timeout 超时时间，单位：毫秒
	 */
	private synchronized void delay(long timeout) {
		try {
			super.wait(timeout);
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
	 * 判断处于等待状态
	 * @return 返回真或者假
	 */
	public boolean isAwaiting() {
		return awaiting;
	}

	/**
	 * 触发唤醒对象
	 */
	public void done() {
		awaiting = false;
		wakeup();
	}

	/**
	 * 进入等待状态，直到返回处理结果
	 */
	public void await() {
		// 最后触发时间
		long last = System.currentTimeMillis() + timeout;

		// 没有达到触发时间，延时等待
		while (System.currentTimeMillis() <= last) {
			// 没有触发就延时，否则退出！
			if (awaiting) {
				delay(1000L);
			} else {
				break;
			}
		}
	}

	/**
	 * 设置执行结果状态（成功或者失败），同时唤醒等待
	 * @param b 成功或者否
	 */
	public void setSuccessful(boolean b) {
		successful = b;
		done();
	}

	/**
	 * 判断执行成功或者失败
	 * @return 故障对象
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((WritePackageTuple) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if(component == null) {
			return 0;
		}
		return component.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WritePackageTuple that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		// 判断文件一致
		return component.compareTo(that.component);
	}

}