/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work;

import java.io.*;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;

/**
 * WORK节点数据流适配器<br>
 * 目前数据流服务接收器只受理conduct命令。<br>
 * 
 * @author scott.liang
 * @version 1.1 10/19/2009
 * @since laxcus 1.0
 */
public class WorkStreamAdapter extends StreamAdapter {

	/**
	 * 构造WORK节点数据流适配器
	 */
	public WorkStreamAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdtapter#reply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void reply(Stream resp, OutputStream output) throws IOException {
		// TODO Auto-generated method stub		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdtapter#apply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void apply(Stream request, OutputStream output) throws IOException {

	}

}