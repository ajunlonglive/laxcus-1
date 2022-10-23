/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home;

import java.io.*;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;

/**
 * HOME节点数据流适配器
 * 
 * @author scott.liang
 * @version 1.1 2/10/2010
 * @since laxcus 1.0
 */
public class HomeStreamAdapter extends StreamAdapter {

	/**
	 * 构造HOME节点数据流适配器
	 */
	public HomeStreamAdapter() {
		super();
	}

	/**
	 * 发起请求
	 * @see com.laxcus.invoke.StreamAdapter#apply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void apply(Stream request, OutputStream output) throws IOException {
		
	}

	/**
	 * 处理应答
	 * @see com.laxcus.invoke.StreamAdapter#reply(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	protected void reply(Stream stream, OutputStream output) throws IOException {
		Mark cmd = stream.getMark();
		short code = cmd.getAnswer();
		switch (code) {
		case Answer.ISEE:
			HomeLauncher.getInstance().refreshEndTime();
			break;
		case Answer.NOTLOGIN:
			HomeLauncher.getInstance().kiss();
			break;
		}
	}

}