/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved. 
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.server;

import java.io.IOException;
import java.io.OutputStream;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;

/**
 * LOG节点数据流适配器（没有处理任务）。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2009
 * @since laxcus 1.0
 */
public class LogStreamAdapter extends StreamAdapter {

	/**
	 * 构造LOG节点数据流适配器
	 */
	public LogStreamAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdtapter#apply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void apply(Stream request, OutputStream output)
			throws IOException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdtapter#reply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void reply(Stream resp, OutputStream output) throws IOException {
		// TODO Auto-generated method stub
	}

}