/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.tub;

import com.laxcus.tub.servlet.*;
import com.laxcus.ui.display.*;

/**
 * 边缘容器显示器。
 * 
 * @author scott.liang
 * @version 1.0 6/24/2019
 * @since laxcus 1.0
 */
public class TubDisplayImpl implements TubDisplay {
	
	/** 边缘容器显示接口。显示命令操作过程中产生信息 **/
	private MeetDisplay display;

	/**
	 * 构造边缘容器显示接口，指定显示器
	 * @param display 显示器
	 */
	public TubDisplayImpl(MeetDisplay display) {
		super();
		setDisplay(display);
	}

	/**
	 * 设置边缘容器显示接口。在进程启动时设置，信息通过这个接口显示在界面
	 * @param e MeetDisplay实例
	 */
	private void setDisplay(MeetDisplay e) {
		display = e;
	}
	

	@Override
	public void message(String text, boolean focus) {
		display.message(text, focus);
	}

	/**
	 * 向窗口投递警告
	 * @param text 警告文本
	 * @param focus 获得焦点
	 */
	public void warning(String text, boolean focus) {
		display.warning(text, focus);
	}

	/**
	 * 向窗口投递错误
	 * @param text 错误文本
	 * @param focus 获得焦点
	 */
	public void fault(String text, boolean focus) {
		display.fault(text, focus);
	}
	
	/**
	 * 向窗口投递消息
	 * @param text 消息文本
	 */
	public void message(String text) {
		display.message(text, true);
	}

	/**
	 * 向窗口投递警告
	 * @param text 警告文本
	 */
	public void warning(String text) {
		display.warning(text, true);
	}

	/**
	 * 向窗口投递错误
	 * @param text 错误文本
	 */
	public void fault(String text) {
		display.fault(text, true);
	}

}