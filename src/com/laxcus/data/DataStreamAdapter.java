/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data;

import java.io.*;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;

/**
 * DATA节点数据流适配器
 * 
 * @author scott.liang
 * @version 1.1 10/13/2009
 * @since laxcus 1.0
 */
public class DataStreamAdapter extends StreamAdapter {

	/**
	 * 构造DATA节点数据流适配器
	 */
	public DataStreamAdapter() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdapter#apply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void apply(Stream request, OutputStream output)
			throws IOException {

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdapter#reply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void reply(Stream resp, OutputStream output) throws IOException {

	}
	
}