/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Private License (LPL)
 */
package com.laxcus.fixp.secure;

import java.security.interfaces.*;

import com.laxcus.util.*;

/**
 * RSA私钥 <br>
 * 
 * 存在服务端，解密客户端数据。
 * 
 * @author scott.liang
 * @version 1.0 2/10/2021
 * @since laxcus 1.0
 */
public class ServerKey {

	/** RSA私钥 **/
	private PrivateStripe stripe;

	/** RSA 公钥，是客户端的密钥 */
	private RSAPrivateKey key;

	/**
	 * 构造默认的客户机RSA密钥令牌
	 */
	public ServerKey() {
		super();
	}

	/**
	 * 设置RSA私钥，不允许空指针
	 * @param e RSA私钥
	 */
	public void setStripe(PrivateStripe e) {
		Laxkit.nullabled(e);
		stripe = e;
	}

	/**
	 * 返回RSA私钥
	 * @return PrivateClasp实例
	 */
	public PrivateStripe getStripe() {
		return stripe;
	}

	/**
	 * 设置RSA私钥，不允许空指针
	 * 
	 * @param e RSA私钥
	 */
	public void setKey(RSAPrivateKey e) {
		Laxkit.nullabled(e);
		key = e;
	}

	/**
	 * 返回RSA私钥
	 * 
	 * @return RSA私钥
	 */
	public RSAPrivateKey getKey() {
		return key;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return stripe.toString();
	}

}