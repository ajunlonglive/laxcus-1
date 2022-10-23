/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.put;

import java.io.*;

import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.task.*;

/**
 * 分布计算PUT阶段默认任务
 * 
 * @author scott.liang
 * @version 1.3 7/16/2013
 * @since laxcus 1.0
 */
public class DefaultPutTask extends PutTask {

	/**
	 * 构造默认的分布计算PUT阶段默认任务
	 */
	public DefaultPutTask() {
		super();
	}

	/**
	 * 数据写入磁盘
	 * @param object
	 * @param b
	 * @param off
	 * @param len
	 */
	private void writeTo(PutObject object, byte[] b, int off, int len) {
		String filename = object.getWriteTo();
		if (filename == null) {
			return;
		}
		boolean success = super.writeTo(filename, b, off, len);
		if (success) {
			message("data write to %s", filename);
		} else {
			fault("write %s error!", filename);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.local.LocalTask#display(byte[], int, int)
	 */
	@Override
	public long display(byte[] b, int off, int len) throws TaskException {
		Conduct conduct = super.getCommand();
		PutObject object = conduct.getPutObject();
		if (object != null) {
			this.writeTo(object, b, off, len);
		}

		message("'%s' result data size:%d", conduct, len);

		return len;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(java.io.File[])
	 */
	@Override
	public long display(File[] files) throws TaskException {
		return defaultDisplay(files);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(java.io.File)
	 */
	@Override
	public long display(File file) throws TaskException {
		return defaultDisplay(new File[] { file });
	}

}