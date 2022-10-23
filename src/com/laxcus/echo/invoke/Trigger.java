/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.io.*;
import com.laxcus.echo.*;

/**
 * 触发标识。<br>
 * 触发标识由回显标识和触发时间组成，关联一个异步调用器，被InvokerPool判断和使用。
 * 当回显标识达到规定的触发时间后，InvokerPool才允许操作异步调用器（EchoInvoker）。
 * 
 * @author scott.liang
 * @version 1.1 8/24/2015
 * @since laxcus 1.0
 */
final class Trigger implements Comparable<Trigger>, Serializable, Cloneable {

	private static final long serialVersionUID = 7193990678183457628L;

	/** 回显标识 **/
	private EchoFlag flag;

	/** 触发时间 **/
	private long touchTime;
	
	/**
	 * 构造默认和私有的触发标识
	 */
	private Trigger() {
		super();
	}

	/**
	 * 根据传入实例，生成它的数据副本
	 * @param that 触发标识实例
	 */
	private Trigger(Trigger that) {
		this();
		setEchoFlag(that.flag);
		setTouchTime(that.touchTime);
	}

	/**
	 * 构造触发标识，指定它的全部参数
	 * @param flag 回显标识
	 * @param time 触发时间
	 */
	public Trigger(EchoFlag flag, long time) {
		this();
		setEchoFlag(flag);
		setTouchTime(time);
	}
	
	/**
	 * 设置回显标识
	 * @param e 回显标识
	 */
	public void setEchoFlag(EchoFlag e) {
		flag = e;
	}
	
	/**
	 * 返回回显标识
	 * @return EchoFlag实例
	 */
	public EchoFlag getEchoFlag() {
		return flag;
	}
	
	/**
	 * 返回调用器编号
	 * @return 调用器编号
	 */
	public long getInvokerId() {
		return flag.getInvokerId();
	}
	
	/**
	 * 设置触发时间，单位：毫秒
	 * @param time long类型
	 */
	public void setTouchTime(long time) {
		touchTime = time;
	}
	
	/**
	 * 返回触发时间，单位：毫秒
	 * @return long类型
	 */
	public long getTouchTime() {
		return touchTime;
	}
	
	/**
	 * 判断可以触发操作
	 * @return 返回真或者假
	 */
	public boolean isTouchable() {
		return System.currentTimeMillis() >= touchTime;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Trigger.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Trigger) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return flag.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new Trigger(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s,%d", flag, touchTime);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Trigger that) {
		if(that == null) {
			return 1;
		}
		return flag.compareTo(that.flag);
	}

}