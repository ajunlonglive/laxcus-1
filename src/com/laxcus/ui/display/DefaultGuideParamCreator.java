/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ui.display;

import com.laxcus.gui.frame.*;
import com.laxcus.task.guide.parameter.*;

/**
 * 初始参数生成器
 * 
 * @author laxcus programer
 * @version 1.0 6/17/2022
 * @since laxcus 1.0
 */
public class DefaultGuideParamCreator implements GuideParamCreator {

	/** 窗口 **/
	private LightFrame parent;
	
	/**
	 * 构造初始参数生成器
	 * @param e
	 */
	public DefaultGuideParamCreator(LightFrame e) {
		super();
		if (e == null) {
			throw new NullPointerException("parent window is null!");
		}
		parent = e;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.GuideParamCreator#create(com.laxcus.task.guide.parameter.InputParameterList)
	 */
	@Override
	public boolean create(String caption, InputParameterList list) {
		DefaultGuideParamDialog dialog = new DefaultGuideParamDialog(caption, list);
		return dialog.showDialog(parent);
	}

}
