/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.bank.pool.*;
import com.laxcus.command.cloud.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 删除云端应用调用器。 
 * 
 * @author scott.liang
 * @version 1.0 6/22/2020
 * @since laxcus 1.0
 */
public abstract class BankDropCloudPackageInvoker extends BankInvoker {

	/**
	 * 构造删除云端应用调用器，指定命令
	 * @param cmd 删除云端应用
	 */
	protected BankDropCloudPackageInvoker(DropCloudPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropCloudPackage getCommand() {
		return (DropCloudPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 找到所有ACCOUNT节点，发送这个命令!
		List<Node> subs = AccountOnBankPool.getInstance().detail();

		DropCloudPackage cmd = getCommand();
		// 因为来自WATCH节点，这里撤销签名，表示为系统应用！
		cmd.setIssuer(null);
		// 发送给所有ACCOUNT节点
		int count = incompleteTo(subs, cmd);

		// 判断有命令发出
		boolean success = (count > 0);
		if (!success) {
			replyProduct(new DropCloudPackageProduct());
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DropCloudPackageProduct product = new DropCloudPackageProduct();
		List<Integer> lists = getEchoKeys();
		for (int index : lists) {
			try {
				if (isSuccessObjectable(index)) {
					DropCloudPackageProduct sub = getObject(DropCloudPackageProduct.class, index);
					product.addRights(sub.getRights());
					product.addFaults(sub.getFaults());
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		replyProduct(product);
		// 执行上传操作
		return useful();
	}

}