/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank;

import java.io.*;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;

/**
 * BANK节点数据流适配器
 * 
 * @author scott.liang
 * @version 1.0 6/25/2018
 * @since laxcus 1.0
 */
public class BankStreamAdapter extends StreamAdapter {

	/**
	 * 构造BANK节点数据流适配器
	 */
	public BankStreamAdapter() {
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
			BankLauncher.getInstance().refreshEndTime();
			break;
		case Answer.NOTLOGIN:
			BankLauncher.getInstance().kiss();
			break;
		}
	}

}
