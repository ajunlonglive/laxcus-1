/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

/**
 * RSA密钥令牌XML文件标签
 * 
 * @author scott.liang
 * @version 1.0 12/07/2016
 * @since laxcus 1.0
 */
public final class SecureMark {

	/** 根标签 **/
	public static final String KEY_TOKENS = "key-tokens";

	/** 复制属性 **/
	public static final String COPY_ATTRIBUTE = "copy";
	
	/** 密钥令牌 **/
	public static final String TOKEN = "token";
	
	/** 名称 **/
	public static final String NAME_ATTRIBUTE = "name";

	/** 安全检查类型 **/
	public static final String CHECK_ATTRIBUTE = "check";

	/** 安全模式 **/
	public static final String MODE_ATTRIBUTE = "mode";

	/** 系数（模数） **/
	public static final String MODULUS = "modulus";

	/** 指数 **/
	public static final String EXPONENT = "exponent";

	/** RSA的模数和指数 */
	public static final String CODE = "code";

	/** RSA指向文件 **/
	public static final String FILE = "file";
	
	/** RSA明文 **/
	public static final String TEXT = "text";

	/** 自动生成RSA密钥 **/
	public static final String AUTO = "auto";
	
	/** RSA明文密码 **/
	public static final String PASSWORD = "password";

	/** 生成RSA密钥的数位尺寸 **/
	public static final String KEYSIZE = "keysize";

	/** 公钥 **/
	public static final String PUBLIC_KEY = "public-key";

	/** 私钥 **/
	public static final String PRIVATE_KEY = "private-key";
	
	/** 接受IP地址范围 **/
	public static final String RANGE = "range";
	
	/** RSA密钥标签 **/
	public static final String KEY = "key";

}