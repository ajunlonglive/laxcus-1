/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;
import com.laxcus.fixp.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 子站点密文超时调用器
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class SubCipherTimeoutInvoker extends CommonInvoker {

	/**
	 * 构造子站点密文超时调用器，指定命令
	 * @param cmd 密文超时命令
	 */
	public SubCipherTimeoutInvoker(CipherTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CipherTimeout getCommand() {
		return (CipherTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CipherTimeout cmd = getCommand();
		Node from = cmd.getSourceSite();
		Node hub = getHub();

		// 命令必须来自管理站点
		boolean success = (Laxkit.compareTo(from, hub) == 0);
		// 根据命令，选择设置FIXP客户端/服务器密文超时
		if (success) {
			Cipher.setTimeout(cmd.getInterval());
		}

		// 要求反馈结果时...
		if (cmd.isReply()) {
			CipherTimeoutProduct product = new CipherTimeoutProduct();
			product.add(getLocal(), success);
			replyProduct(product);
		}

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
