/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.modulate.end;

import java.io.*;

import com.laxcus.task.*;
import com.laxcus.task.establish.end.*;

/**
 * 数据优化的“END”阶段任务。
 * 
 * MODULATE.END阶段任务部署在FRONT站点上，它是数据构建的最后阶段。负责显示、保存RISE阶段返回的数据处理结果。
 * 
 * @author scott.liang
 * @version 1.1 1/9/2013
 * @since laxcus 1.0
 */
public class ModulateEndTask extends EndTask {

	/**
	 * 构造MODULATE.END阶段任务实例
	 */
	public ModulateEndTask() {
		super();
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