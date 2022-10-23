/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.method;

import com.laxcus.tub.command.*;
import com.laxcus.tub.product.*;
import com.laxcus.util.net.*;

/**
 * 检查边缘监听运行器
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubCheckListenRunner extends TubCommandRunner {

	/**
	 * 构造检查边缘监听运行器，指定命令
	 * @param cmd 检查边缘监听
	 */
	public TubCheckListenRunner(TubCheckListen cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#getCommand()
	 */
	@Override
	public TubCheckListen getCommand() {
		return (TubCheckListen) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#launch()
	 */
	@Override
	public CheckTubListenerProduct launch() {
		// 监听地址
		SocketHost local = getLauncher().getTubHost();
		// 结果
		CheckTubListenerProduct product = new CheckTubListenerProduct();
		// 有效！
		if (local != null) {
			product.add(local);

			// 如果是通配符...
			if (local.getAddress().isAnyLocalAddress()) {
				Address[] addresses = Address.locales();
				for (Address sub : addresses) {
					boolean success = (sub.isSiteLocalAddress()
							|| sub.isWideAddress() || sub.isLoopbackAddress());
					if (success) {
						SocketHost host = new SocketHost(local.getFamily(), sub, local.getPort());
						product.add(host);
					}
				}
			}
		}
		
		return product;
	}

}