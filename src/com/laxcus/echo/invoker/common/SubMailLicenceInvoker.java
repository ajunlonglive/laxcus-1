/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.licence.*;

/**
 * 工作类站点的发布许可证调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public class SubMailLicenceInvoker extends CommonMailLicenceInvoker {

	/**
	 * 构造发布许可证调用器，指定命令
	 * @param cmd 发布许可证
	 */
	public SubMailLicenceInvoker(MailLicence cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 重装加载本地动态链接库
		MailLicenceItem item = reload();
		// 生成报告
		MailLicenceProduct product = new MailLicenceProduct();
		product.add(item);
		boolean success = replyProduct(product);
		// 退出
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
