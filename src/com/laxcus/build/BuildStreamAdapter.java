/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build;

import java.io.*;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;

/**
 * BUILD节点数据流适配器
 * 
 * @author scott.liang
 * @version 1.0 8/19/2009
 * @since laxcus 1.0
 */
public class BuildStreamAdapter extends StreamAdapter { 

	/**
	 * 构造BUILD节点数据流适配器
	 */
	public BuildStreamAdapter() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdapter#apply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void apply(Stream request, OutputStream resp) throws IOException {

	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdapter#reply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void reply(Stream request, OutputStream resp) throws IOException {
		// 空
	}

}