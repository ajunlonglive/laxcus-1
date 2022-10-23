/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.security;

import java.math.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;

import javax.crypto.*;

/**
 * 安全参数生成器
 * 
 * @author scott.liang
 * @version 1.0 10/30/2009
 * @since laxcus 1.0
 */
public class SecureGenerator {
	
	static final String DES_ALGO = "DES";
	static final String DES3_ALGO = "DESede";
	static final String AES_ALGO = "AES";

	/**
	 * 使用模和公用指数，生成RSA公钥
	 * @param modulus 模
	 * @param exponent 公用指数
	 * @return RSAPublicKey实例
	 * @throws SecureException
	 */
	public static RSAPublicKey buildRSAPublicKey(String modulus, String exponent) throws SecureException {
		try {
			KeyFactory keyFac = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(
					modulus, 16), new BigInteger(exponent, 16));
			return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new SecureException(e);
		} catch (InvalidKeySpecException e) {
			throw new SecureException(e);
		}
	}

	/**
	 * 使用模和私用指数，生成RSA私钥 
	 * @param modulus 模
	 * @param exponent 私用指数
	 * @return RSAPrivateKey实例
	 * @throws SecureException
	 */
	public static RSAPrivateKey buildRSAPrivateKey(String modulus, String exponent) throws SecureException {
		try {
			KeyFactory factory = KeyFactory.getInstance("RSA");
			RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(
					new BigInteger(modulus, 16), new BigInteger(exponent, 16));
			return (RSAPrivateKey) factory.generatePrivate(priKeySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new SecureException(e);
		} catch (InvalidKeySpecException e) {
			throw new SecureException(e);
		}
	}
	
	/**
	 * 使用密码生成AES密钥
	 * @param pwd AES密码
	 * @return SecretKey实例
	 * @throws SecureException
	 */
	public static SecretKey buildAESKey(byte[] pwd) throws SecureException {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(pwd);
			KeyGenerator generator = KeyGenerator.getInstance(SecureGenerator.AES_ALGO);
			generator.init(128, random);
			return generator.generateKey();
		} catch (NoSuchAlgorithmException e) {
			throw new SecureException(e);
		}
	}
	
	/**
	 * 使用密码生成DES密钥
	 * @param pwd DES密码
	 * @return SecretKey实例
	 * @throws SecureException
	 */
	public static SecretKey buildDESKey(byte[] pwd) throws SecureException {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(pwd);
			KeyGenerator generator = KeyGenerator.getInstance(SecureGenerator.DES_ALGO);
			generator.init(56, random); //must be 56
			SecretKey key = generator.generateKey();
			return key;
		} catch (NoSuchAlgorithmException e) {
			throw new SecureException(e);
		}
	}
	
	/**
	 * 使用密码生成DES3密钥
	 * @param pwd DES3密码
	 * @return SecretKey实例
	 * @throws SecureException
	 */
	public static SecretKey buildDES3Key(byte[] pwd) throws SecureException {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(pwd);
			KeyGenerator generator = KeyGenerator.getInstance(SecureGenerator.DES3_ALGO);
			generator.init(168, random); // must be 56*3
			SecretKey key = generator.generateKey();
			return key;
		} catch (NoSuchAlgorithmException e) {
			throw new SecureException(e);
		}
	}
	
	/**
	 * 使用密码生成Blowfish密钥
	 * @param pwd BLOWFISH密码
	 * @return SecretKey实例
	 * @throws SecureException
	 */
	public static SecretKey buildBlowfishKey(byte[] pwd) throws SecureException {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(pwd);
			KeyGenerator generator = KeyGenerator.getInstance("Blowfish");
			generator.init(64, random); //size must be multiple of 8
			return generator.generateKey();
		} catch (NoSuchAlgorithmException e) {
			throw new SecureException(e);
		}
	}

}