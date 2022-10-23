/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.slide;

import com.laxcus.access.type.*;

/**
 * 音频列对象定位器。<br>
 * 系统默认使用。
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class AudioSlider extends VariableSlider {

	/**
	 * 构造默认的音频列对象定位器
	 */
	public AudioSlider() {
		super(ColumnType.AUDIO);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.slide.Slider#isSupport(java.lang.Object)
	 */
	@Override
	public boolean isSupport(Object e) {
		return (e.getClass() == com.laxcus.access.column.Audio.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.slide.Slider#seek(java.lang.Object)
	 */
	@Override
	public java.lang.Number seek(Object e) throws SliderException {
		// 不支持弹出异常
		if (!isSupport(e)) {
			throw new SliderException("cannot be support %s", e.getClass().getName());
		}

		return doDefaultSeek((com.laxcus.access.column.Audio) e);
	}

}
