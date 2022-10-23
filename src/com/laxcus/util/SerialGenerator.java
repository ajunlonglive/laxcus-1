/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

/**
 * 序列号生成器。<br><br>
 * 
 * 序列号是一个长整型数字，默认在“0 - Long.MAX_VALUE”之间循环，同时可以在初始化时定义，每次调用返回一个。
 * 用于需要短时间内唯一且保证有效的环境中，比如异步通信时的会话任务、工作任务编号。
 * 由于长整型的范围足够大，可以保证短时间内不会重复。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/2/2009
 * @since laxcus 1.0
 */
public final class SerialGenerator {

	/** 序列号循环 **/
	private long serialIndex;

	/** 最小序列号 **/
	private long minIndex;

	/** 最大序列号 **/
	private long maxIndex;
	
	/** 循环次数 **/
	private long loop;

	/**
	 * 构造序列号生成器，指定范围
	 * @param min 最小值
	 * @param max 最大值
	 */
	public SerialGenerator(long min, long max) {
		super();
		loop = 0;
		setRange(min, max);
		serialIndex = minIndex;
	}

	/**
	 * 构造一个序列号生成器
	 */
	public SerialGenerator() {
		this(0, Long.MAX_VALUE);
	}

	/**
	 * 设置范围
	 * @param min
	 * @param max
	 */
	private void setRange(long min, long max) {
		// 不能小于0
		if (min < 0 || max < 0) {
			throw new IllegalValueException("illegal min: %d max: %d", min, max);
		}
		if (min >= max) {
			throw new IllegalValueException("min %d >= max %d", min, max);
		}
		minIndex = min;
		maxIndex = max;
	}

	/**
	 * 返回最小序列号
	 * @return 序列号
	 */
	public long getMinSerial() {
		return minIndex;
	}

	/**
	 * 返回最大序列号
	 * @return 长整数
	 */
	public long getMaxSerial() {
		return maxIndex;
	}

	/**
	 * 以同步方式生成一个序列号
	 * @return 长整型，在0-规定最大值之间，默认最大值是Long.MAX_VALUE。
	 */
	public synchronized long nextSerial() {
		if (serialIndex >= maxIndex) {
			serialIndex = minIndex; // 回收最小
			// 循环，直到最大值时
			if (loop >= Long.MAX_VALUE) {
				loop = 1;
			} else {
				loop++;
			}
		}
		return serialIndex++;
	}
	
	/**
	 * 返回当前序列号
	 * @return 长整数
	 */
	public long getCurrentSerial() {
		return serialIndex;
	}
	
	/**
	 * 循环值
	 * @return 长整数
	 */
	public long getLoop() {
		return loop;
	}

}

//	/** 序列号循环 **/
//	private long serialIndex;
//
//	/**
//	 * 构造一个序列号生成器
//	 */
//	public SerialGenerator() {
//		super();
//		serialIndex = 0L;
//	}
//
//	/**
//	 * 以同步方式生成一个序列号
//	 * @return 长整型，在0-Long.MAX_VALUE之间。
//	 */
//	public synchronized long nextSerial() {
//		if (serialIndex == Long.MAX_VALUE) {
//			serialIndex = 0L;
//		}
//		return serialIndex++;
//	}


//	/**
//	 * 设置最大序列号
//	 * @param who
//	 * @return 成功是真，否则假
//	 */
//	public boolean setMaxSerial(long who) {
//		if (who > 0 && who <= Long.MAX_VALUE) {
//			maxIndex = who;
//			return true;
//		}
//		return false;
//	}