/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

/**
 * 释放元数据命令。<br><br>
 * 
 * 这个命令由HOME站点发出，目标是CALL站点。HOME站点通知CALL站点，清除某个节点下属的全部元数据。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/12/2014
 * @since laxcus 1.0
 */
public abstract class DropField extends ProcessField {

	private static final long serialVersionUID = -6340854821134740067L;

	/**
	 * 构造默认的释放元数据命令
	 */
	protected DropField() {
		super();
	}

	/**
	 * 根据传入的释放元数据命令，生成它的数据副本
	 * @param that
	 */
	protected DropField(DropField that) {
		super(that);
	}

}