/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import java.security.interfaces.*;

import com.laxcus.util.*;

/**
 * RSA公钥。
 * 
 * 保存在服务端，分发给客户端使用。
 * 
 * @author scott.liang
 * @version 1.0 2/10/2021
 * @since laxcus 1.0
 */
public class ClientKey {

	/** RSA公钥 **/
	private PublicStripe stripe;

	/** RSA公钥，是客户端的密钥 */
	private RSAPublicKey key;

	/**
	 * 构造默认的客户机RSA密钥令牌
	 */
	public ClientKey() {
		super();
	}

	/**
	 * 设置RSA公钥
	 * @param e
	 */
	public void setStripe(PublicStripe e) {
		Laxkit.nullabled(e);
		stripe = e;
	}

	/**
	 * 返回RSA公钥
	 * @return
	 */
	public PublicStripe getStripe() {
		return stripe;
	}

	/**
	 * 设置RSA公钥
	 * 
	 * @param e RSA公钥
	 */
	public void setKey(RSAPublicKey e) {
		Laxkit.nullabled(e);
		key = e;
	}

	/**
	 * 返回RSA公钥
	 * 
	 * @return RSA公钥
	 */
	public RSAPublicKey getKey() {
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
