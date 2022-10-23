/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.secure.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.util.*;

/**
 * 读取密钥令牌调用器
 * 
 * @author scott.liang
 * @version 1.0 2/14/2021
 * @since laxcus 1.0
 */
public abstract class CommonShowSecureTokenInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造读取密钥令牌调用器，指定命令
	 * @param cmd 读取密钥令牌
	 */
	protected CommonShowSecureTokenInvoker(ShowSecureToken cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowSecureToken getCommand() {
		return (ShowSecureToken) super.getCommand();
	}

	/**
	 * 检测结果
	 * @return 返回ShowSecureTokenItem实例
	 */
	protected ShowSecureTokenItem pickup() {
		List<SecureToken> tokens = SecureController.getInstance().list();

		// 取出记录
		ShowSecureTokenItem item = new ShowSecureTokenItem(getLocal(), true);
		for (SecureToken token : tokens) {
			SecureTokenSlat slat = new SecureTokenSlat(token.getName(), token.getFamily(), token.getMode());
			slat.addRanges(token.list());

			// 私钥
			PrivateStripe s1 = token.getServerKey().getStripe();
			slat.setPrivateModulus(Laxkit.doSHA256Hash(s1.getModulus()));
			slat.setPrivateExponent(Laxkit.doSHA256Hash(s1.getExponent()));
			// 公钥
			PublicStripe s2 = token.getClientKey().getStripe();
			slat.setPublicModulus(Laxkit.doSHA256Hash(s2.getModulus()));
			slat.setPublicExponent(Laxkit.doSHA256Hash(s2.getExponent()));
			// 保存！
			item.add(slat);
		}

		return item;
	}

	
}