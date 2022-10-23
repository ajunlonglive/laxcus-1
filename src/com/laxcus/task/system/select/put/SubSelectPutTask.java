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
 * SELECT嵌套查询PUT阶段任务
 * 
 * @author scott.liang
 * @version 1.0 12/17/2014
 * @since laxcus 1.0
 */
public class SubSelectPutTask extends DefaultSelectPutTask {

	/**
	 * 构造默认的SELECT嵌套查询PUT阶段任务
	 */
	public SubSelectPutTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(byte[], int, int)
	 */
	@Override
	public long display(byte[] b, int off, int len) throws TaskException {
//		super.writeTo("/home/subselect.txt", b, off, len);
//		this.message("数据写入磁盘！");
		
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