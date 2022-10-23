/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.end;

import java.io.*;

import com.laxcus.task.*;
import com.laxcus.ui.display.*;

/**
 * ESTABLISH.END阶段默认实例。<br>
 * 当用户没有定义自己的END阶段实例时，可以使用它
 * 
 * @author scott.liang
 * @version 1.2 1/23/2013
 * @since laxcus 1.0
 */
public class DefaultEndTask extends EndTask {

	/**
	 * 构造默认的END阶段实例。
	 */
	public DefaultEndTask() {
		super();
	}

	/**
	 * 构造默认的END阶段任务实例，指定显示器
	 * @param display 终端显示器
	 */
	public DefaultEndTask(MeetDisplay display) {
		this();
		this.setDisplay(display);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.end.EndTask#display(byte[], int, int)
	 */
	@Override
	public long display(byte[] b, int off, int len) throws TaskException {
		return super.defaultDisplay(b, off, len);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.end.EndTask#display(java.io.File[])
	 */
	@Override
	public long display(File[] files) throws TaskException {
		return super.defaultDisplay(files);
	}

}