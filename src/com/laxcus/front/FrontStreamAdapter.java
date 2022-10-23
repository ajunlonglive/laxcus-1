/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

import java.io.*;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;

/**
 * FRONT站点的数据流适配器。兼容终端、控制台、驱动程序三种模式
 * 
 * @author scott.liang
 * @version 1.1 8/19/2012
 * @since laxcus 1.0
 */
public class FrontStreamAdapter extends StreamAdapter {

	/**
	 * 构造前端数据流监听器
	 */
	public FrontStreamAdapter() {
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