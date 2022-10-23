/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch;

import java.io.*;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;

/**
 * WATCH站点数据流适配器<br>
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public class WatchStreamAdapter extends StreamAdapter {

	/**
	 * 构造WATCH站点数据流适配器
	 */
	public WatchStreamAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdapter#apply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void apply(Stream request, OutputStream output)
			throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdapter#reply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void reply(Stream resp, OutputStream output) throws IOException {
		// TODO Auto-generated method stub

	}

}
