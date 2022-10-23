/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * 异步调用器的序列化处理步骤。<br>
 * 
 * 使用此序列化处理步骤的调用器，包括业务、禁止操作。<br>
 * 
 * 分为五个阶段，其中“PROCESS”阶段由各子类根据自己的需求来实现
 * 
 * @author scott.liang
 * @version 1.0 8/27/2013
 * @since laxcus 1.0
 */
public enum SerialStage {

	NONE(0), INSURE(1), CHECK_INSURE(2), PROCESS(3), REVOKE(4), CHECK_REVOKE(5);

	/**
	 * 处理步骤默认0
	 */
	private int stage = 0;

	/**
	 * 设置处理步骤
	 * @param who 处理步骤
	 */
	private SerialStage(int who) {
		stage = who;
	}

	/**
	 * 返回处理步骤的字符串描述
	 * @return 字符串描述
	 */
	public String type() {
		switch(stage){
		case 1:
			return "INSURE";
		case 2:
			return "CHECK-INSURE";
		case 3:
			return "PROCESS";
		case 4:
			return "REVOKE";
		case 5:
			return "CHECK-REVOKE";
		default:
			return "NONE";
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return type();
	}

}