/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.put;

import java.io.*;

import com.laxcus.task.*;

/**
 * SELECT标准查询PUT阶段任务
 * 
 * @author scott.liang
 * @version 1.0 7/23/2012
 * @since laxcus 1.0
 */
public class SelectPutTask extends DefaultSelectPutTask {

	/**
	 * 构造默认的SELECT标准查询PUT阶段任务
	 */
	public SelectPutTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(byte[], int, int)
	 */
	@Override
	public long display(byte[] b, int off, int len) throws TaskException {
		long readlen = print(b, off, len);
		printRows();
		return readlen;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(java.io.File[])
	 */
	@Override
	public long display(File[] files) throws TaskException {
		long readlen = print(files);
		printRows();
		return readlen;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(java.io.File)
	 */
	@Override
	public long display(File file) throws TaskException {
		long readlen = print(new File[] { file });
		printRows();
		return readlen;
	}

}