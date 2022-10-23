/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

/**
 * 进制数单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public abstract class ShowIntegralCell extends ShowItemCell {
	
	private static final long serialVersionUID = -382316737477864236L;

	/** 进制数 **/
	private int radix;

	/**
	 * 构造默认的进制数单元
	 */
	protected ShowIntegralCell() {
		super();
		setRadix(10);
	}

	/**
	 * 生成传入的进制数单元的数据副本
	 * @param that 进制数单元
	 */
	protected ShowIntegralCell(ShowIntegralCell that) {
		super(that);
		radix = that.radix;
	}

	/**
	 * 设置进制数
	 * @param i
	 */
	public void setRadix(int i){
		radix = i;
	}
	
	/**
	 * 返回进制数
	 * @return
	 */
	public int getRadix(){
		return radix;
	}
	
	/**
	 * 判断是16进制
	 * @return
	 */
	public boolean isHex(){
		return radix == 16;
	}
}
