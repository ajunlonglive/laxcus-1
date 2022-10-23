/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.security.interfaces.*;

import com.laxcus.command.secure.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.log.client.*;
import com.laxcus.security.*;

/**
 * 建立密钥令牌调用器
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public abstract class CommonCreateSecureTokenInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造建立密钥令牌调用器，指定命令
	 * @param cmd 建立密钥令牌
	 */
	protected CommonCreateSecureTokenInvoker(CreateSecureToken cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateSecureToken getCommand() {
		return (CreateSecureToken) super.getCommand();
	}
	
	/**
	 * 使用模值和私用指数，生成RSA私钥
	 * @param modulus 模
	 * @param exponent 私用指数
	 * @return RSAPrivateKey实例
	 * @throws SecureException
	 */
	private RSAPrivateKey createPrivateKey(String modulus, String exponent)
			throws SecureException {
		return SecureGenerator.buildRSAPrivateKey(modulus, exponent);
	}

	/**
	 * 使用模值和公用指数，生成RSA公钥
	 * @param modulus 模
	 * @param exponent 公用指数
	 * @return RSAPublicKey实例
	 * @throws SecureException
	 */
	private RSAPublicKey createPublicKey(String modulus, String exponent)
			throws SecureException {
		return SecureGenerator.buildRSAPublicKey(modulus, exponent);
	}
	
	/**
	 * 设置密钥令牌
	 * @return 返回真或者假
	 */
	protected boolean reload() {
		CreateSecureToken cmd = getCommand();
		
		SecureTokenSlice slice = cmd.getSlice();
		
		SecureToken token = new SecureToken(slice.getName(), slice.getFamily(), slice.getMode());
		// 保存地址范围
		token.addAll(slice.getRanges());

		boolean success = false;
		try {
			// 服务器密钥
			ServerKey serverKey = new ServerKey();
			PrivateStripe privateStripe = slice.getPrivateStripe();
			RSAPrivateKey privateKey = createPrivateKey( privateStripe.getHexModulus(), privateStripe.getHexExponent() );
			serverKey.setKey(privateKey);
			serverKey.setStripe(privateStripe);

			// 客户机密钥
			ClientKey clientKey = new ClientKey();
			PublicStripe publicStripe = slice.getPublicStripe();
			RSAPublicKey publicKey = createPublicKey( publicStripe.getHexModulus(), publicStripe.getHexExponent() );
			clientKey.setKey(publicKey);
			clientKey.setStripe(publicStripe);

			// 设置RSA密钥
			token.setServerKey(serverKey);
			token.setClientKey(clientKey);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 保存！
		if (success) {
			success = SecureController.getInstance().add(token);
		}
		
		Logger.debug(this, "reload", success, "set secure token %s!", slice.getName());
		return success;
	}

}