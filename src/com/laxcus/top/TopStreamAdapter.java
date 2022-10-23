/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top;

import java.io.*;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;

/**
 * TOP节点数据流适配器
 * 
 * @author scott.liang
 * @version 1.0 11/12/2009
 * @since laxcus 1.0
 */
public class TopStreamAdapter extends StreamAdapter { 

	/**
	 * 构造TOP节点数据流适配器
	 */
	public TopStreamAdapter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdtapter#apply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void apply(Stream request, OutputStream output) throws IOException {
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.StreamAdapter#reply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void reply(Stream stream, OutputStream output) throws IOException {
		Stream resp = null;
		Mark cmd = stream.getMark();
		if (cmd.getAnswer() == Answer.ISEE) {
			Logger.debug("TopStreamInvokder.reply, ISEE Command!");
		} else {
			throw new FixpProtocolException("undefine response");
		}
		flush(resp, output);
	}

}