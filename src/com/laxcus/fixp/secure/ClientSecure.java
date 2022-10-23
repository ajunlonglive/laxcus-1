/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import java.security.interfaces.*;

import com.laxcus.security.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 客户机密钥
 * 
 * @author scott.liang
 * @version 1.0 2/10/2021
 * @since laxcus 1.0
 */
public class ClientSecure implements Comparable<ClientSecure>{

	/** 目标地址 **/
	private SocketHost remote;

	/** 服务器提供的加密 **/
	private PublicSecure secure;

	/** RSA公钥，是客户端的密钥 */
	private RSAPublicKey key;

	/** 启动时间，超时后删除 */
	public long lastTime;

	/**
	 * 构造客户机密钥
	 */
	public ClientSecure() {
		super();
		lastTime = System.currentTimeMillis();
	}

	/**
	 * 构造客户机密钥
	 * @param remote
	 * @param secure
	 * @throws SecureException
	 */
	public ClientSecure(SocketHost remote, PublicSecure secure) throws SecureException {
		this();
		setRemote(remote);
		setPublicSecure(secure);
	}
	
	/**
	 * 判断超时
	 * @param ms
	 * @return
	 */
	public boolean isTimeout(long ms) {
		return (System.currentTimeMillis() - lastTime >= ms);
	}

	/**
	 * 设置目标地址
	 * @param e SocketHost 目标地址
	 */
	public void setRemote(SocketHost e) {
		Laxkit.nullabled(e);
		remote = e.duplicate();
	}

	/**
	 * 返回目标地址
	 * @return SocketHost
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/**
	 * 设置客户机密钥
	 * @param e
	 * @throws SecureException 
	 */
	public void setPublicSecure(PublicSecure e) throws SecureException {
		Laxkit.nullabled(e);
		secure = e.duplicate();

		// 生成RSA公钥
		PublicStripe clasp = secure.getStripe();
		key = SecureGenerator.buildRSAPublicKey(clasp.getHexModulus(), clasp.getHexExponent());
	}

	/**
	 * 返回客户机密钥
	 * @return
	 */
	public PublicSecure getPublicSecure() {
		return secure;
	}
	
	/**
	 * 返回加密类型
	 * @return 整数
	 */
	public int getFamily() {
		return this.secure.getFamily();
	}

	//	/**
	//	 * 设置客户机密钥
	//	 * 
	//	 * @param e 客户机密钥
	//	 */
	//	public void setKey(RSAPublicKey e) {
	//		Laxkit.nullabled(e);
	//		key = e;
	//	}

	/**
	 * 返回客户机密钥
	 * 
	 * @return 客户机密钥
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
		return secure.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ClientSecure that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(remote, that.remote);
	}

}