/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.permit.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 权限操作调用器<br>
 * 管理员向注册用户进行授权或者解除授权
 * 
 * @author scott.liang
 * @version 1.1 05/09/2015
 * @since laxcus 1.0
 */
public abstract class MeetCertificateInvoker extends MeetInvoker {

	/**
	 * 构造权限操作调用器，指定授权命令
	 * @param cmd 权限操作
	 */
	protected MeetCertificateInvoker(Certificate cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Certificate getCommand() {
		return (Certificate) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CertificateProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CertificateProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			fault(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product);
		} else {
			Certificate cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd.getPrimitive());
		}

		// 结束
		return useful(success);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(CertificateProduct product) {
		// 显示运行时间
		printRuntime();
		// 取命令
		Certificate cmd = getCommand();
		// 建立标题
		createShowTitle(new String[] { "CERTIFICATE/STATUS", "CERTIFICATE/USERNAME" });

		// 显示成功的签名
		for (Siger hash : product.getIssuers()) {
			String name = cmd.findPlainText(hash);
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, true));
			item.add(new ShowStringCell(1, name));
			// 保存行单元
			addShowItem(item);
		}
		// 显示失败的签名
		for (Siger hash : product.getIneffects()) {
			String name = cmd.findPlainText(hash);
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, false));
			item.add(new ShowStringCell(1, name));
			// 保存行单元
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
	}

}