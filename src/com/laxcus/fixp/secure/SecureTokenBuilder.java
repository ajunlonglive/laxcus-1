/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import com.laxcus.util.*;
import com.laxcus.util.charset.*;

/**
 * 生成RSA密钥配置的XML格式数据
 * 
 * @author scott.liang
 * @version 1.0 2/10/2021
 * @since laxcus 1.0
 */
public class SecureTokenBuilder {

	/**
	 * 构造生成器
	 */
	public SecureTokenBuilder(){
		super();
	}
	
	/**
	 * 生成属性
	 * @param key
	 * @param value
	 * @return
	 */
	private String buildAttribute(String key, String value) {
		return String.format("%s=\"%s\"", key, value);
	}
	
	/**
	 * 生成标签
	 * @param key
	 * @param value
	 * @return
	 */
	private String buildLabel(String key, String value) {
		return String.format("<%s>%s</%s>", key, value, key);
	}
	
	/**
	 * 生成RSA密钥
	 * @param secureStripe
	 * @return
	 */
	private String buildClasp(SecureStripe secureStripe) {
		String mo = buildLabel(SecureMark.MODULUS, Laxkit.itoh(secureStripe.getModulus()));
		String ex = buildLabel(SecureMark.EXPONENT, Laxkit.itoh(secureStripe.getExponent()));
		return mo + ex;
	}
	
	/**
	 * 生成RSA私钥
	 * @param secureStripe
	 * @return
	 */
	private String buildPrivateKey(SecureStripe secureStripe) {
		String clasp = buildClasp(secureStripe);
		return buildLabel(SecureMark.PRIVATE_KEY, clasp);
	}
	
	/**
	 * 生成RSA公钥
	 * @param secureStripe
	 * @return
	 */
	private String buildPublicKey(SecureStripe secureStripe) {
		String clasp = buildClasp(secureStripe);
		return buildLabel(SecureMark.PUBLIC_KEY, clasp);
	}
	
	/**
	 * 生成一个加密令牌的XML数据
	 * @param token 加密令牌
	 * @return 返回字符串 
	 */
	public String buildToken(SecureToken token) {
		StringBuilder ranges = new StringBuilder();
		// 地址
		for (SecureRange range : token.list()) {
			String s = buildLabel(SecureMark.RANGE, range.toString());
			ranges.append(s);
		}
		// 服务器私钥
		String privateKey = buildPrivateKey(token.getServerKey().getStripe());
		// 客户机公钥
		String publicKey = buildPublicKey(token.getClientKey().getStripe());
		
		String code = buildLabel(SecureMark.CODE, privateKey + publicKey);
		String key = buildLabel(SecureMark.KEY, code);
		
		// 属性参数
		String family = SecureType.translate(token.getFamily());
		String mode = SecureMode.translate(token.getMode());

		String name = buildAttribute(SecureMark.NAME_ATTRIBUTE, token.getName().toString());
		family = buildAttribute(SecureMark.CHECK_ATTRIBUTE, family);
		mode = buildAttribute(SecureMark.MODE_ATTRIBUTE, mode);
		
		String attribute = String.format("%s %s %s", name, family, mode);

		// 生成实例
		return String.format("<%s %s>%s</%s>", SecureMark.TOKEN, attribute, ranges.toString() + key, SecureMark.TOKEN);
	}
	
	/**
	 * 生成全部文档
	 * @return 字节数组
	 */
	public byte[] buildTokens() {
		StringBuilder buff = new StringBuilder();		
		// 指定值
		for(SecureToken token : SecureController.getInstance().getSpecialTokens()) {
			String str = buildToken(token);
			buff.append(str);
		}
		// 默认值
		SecureToken defaultToken = SecureController.getInstance().getDefaultToken();
		if(defaultToken != null) {
			String str = buildToken(defaultToken);
			buff.append(str);
		}
		
		// 生成对象
		String str = buildLabel(SecureMark.KEY_TOKENS, buff.toString());
		str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+str;
		
		// 采用UTF8编码生成文档
		return new UTF8().encode(str);
	}
	
}


///**
// * 生成一个加密令牌的XML数据
// * @param token 加密令牌
// * @return 返回字符串 
// */
//public String buildToken(SecureToken token) {
//	StringBuilder ranges = new StringBuilder();
//	// 地址
////	System.out.printf("token range is %d\n", token.getServerKey().size());
//	for (SecureRange range : token.getServerKey().list()) {
//		String s = buildLabel(SecureMark.RANGE, range.toString());
//		ranges.append(s);
//	}
//	// 服务器私钥
//	String privateKey = buildPrivateKey(token.getServerKey().getStripe());
//	// 服务器参数
//	String server = buildLabel(SecureMark.SERVER, ranges.toString() + privateKey);
//
//	// 客户机公钥
//	String publicKey = buildPublicKey(token.getClientKey().getStripe());
//	// 客户机参数
//	String client = buildLabel(SecureMark.CLIENT, publicKey);
//
//	String family = SecureType.translate(token.getFamily());
//	String mode = SecureMode.translate(token.getMode());
//
//	family = buildAttribute(SecureMark.CHECK_ATTRIBUTE, family);
//	mode = buildAttribute(SecureMark.MODE_ATTRIBUTE, mode);
//
//	// 生成实例
//	return String.format("<%s %s>%s</%s>", SecureMark.TOKEN, family + " " + mode, 
//			server + client, SecureMark.TOKEN);
//}