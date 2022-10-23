/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.security;

import java.security.*;
import java.security.interfaces.*;
import javax.crypto.*;

/**
 * 密文解码器
 * 
 * @author scott.liang
 * @version 1.0 10/29/2009
 * @since laxcus 1.0
 */
public class SecureDecryptor extends SecureGenerator {

	/**
	 * 使用RSA私钥解密数据流
	 * 
	 * @param key RSA私钥
	 * @param b 加密的数据流
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回解密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] rsa(RSAPrivateKey key, byte[] b, int off, int len) throws SecureException {
		if (key == null) {
			throw new SecureException("cannot be null");
		}
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(b, off, len);
		} catch (NoSuchAlgorithmException e) {
			throw new SecureException(e);
		} catch (NoSuchPaddingException e) {
			throw new SecureException(e);
		} catch (InvalidKeyException e) {
			throw new SecureException(e);
		} catch (IllegalBlockSizeException e) {
			throw new SecureException(e);
		} catch (BadPaddingException e) {
			throw new SecureException(e);
		}
	}
	
	/**
	 * 使用AES算法和密码解密数据流
	 * 
	 * @param pwd AES密码
	 * @param b 加密的数据流
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回解密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] aes(byte[] pwd, byte[] b, int off, int len) throws SecureException {
		SecretKey key = SecureGenerator.buildAESKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.AES_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(b, off, len);
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		} catch (NoSuchPaddingException exp) {
			throw new SecureException(exp);
		} catch (InvalidKeyException exp) {
			throw new SecureException(exp);
		} catch (BadPaddingException exp) {
			throw new SecureException(exp);
		} catch (IllegalBlockSizeException exp) {
			throw new SecureException(exp);
		}
	}
	
	/**
	 * 使用DES算法和密码解密数据流
	 * 
	 * @param pwd DES密码
	 * @param b 加密的数据流
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回解密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] des(byte[] pwd, byte[] b, int off, int len) throws SecureException {
		SecretKey key = SecureGenerator.buildDESKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.DES_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, key);	
			return cipher.doFinal(b, off, len);
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		} catch (NoSuchPaddingException exp) {
			throw new SecureException(exp);
		} catch (InvalidKeyException exp) {
			throw new SecureException(exp);
		} catch (BadPaddingException exp) {
			throw new SecureException(exp);
		} catch (IllegalBlockSizeException exp) {
			throw new SecureException(exp);
		}
	}
	
	/**
	 * 使用DES3算法和密码解密数据流
	 * 
	 * @param pwd DES3密码
	 * @param b 加密的数据流
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回解密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] des3(byte[] pwd, byte[] b, int off, int len) throws SecureException {
		SecretKey key = SecureGenerator.buildDES3Key(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.DES3_ALGO); // "DESede");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(b, off, len);
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		} catch (NoSuchPaddingException exp) {
			throw new SecureException(exp);
		} catch (InvalidKeyException exp) {
			throw new SecureException(exp);
		} catch (BadPaddingException exp) {
			throw new SecureException(exp);
		} catch (IllegalBlockSizeException exp) {
			throw new SecureException(exp);
		}
	}
	
	/**
	 * 使用BLOWFISH算法和密码解密数据流
	 * 
	 * @param pwd BLOWFISH密码
	 * @param b 加密的数据流
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回解密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] blowfish(byte[] pwd, byte[] b, int off, int len) throws SecureException {
		SecretKey key = SecureGenerator.buildBlowfishKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(b, off, len);
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		} catch (NoSuchPaddingException exp) {
			throw new SecureException(exp);
		} catch (InvalidKeyException exp) {
			throw new SecureException(exp);
		} catch (BadPaddingException exp) {
			throw new SecureException(exp);
		} catch (IllegalBlockSizeException exp) {
			throw new SecureException(exp);
		}
	}

	/**
	 * 去掉MD5的签名，返回MD5数据流
	 * @param b 加密的数据流
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回解码后的字节数组
	 * @throws SecureException
	 */
	public static byte[] md5(byte[] b, int off, int len) throws SecureException {
		if (len <= 16) {
			throw new SecureException("MD5 <= 16");
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(b, off + 16, len - 16);
			byte[] hash = md.digest();

			for (int j = 0, i = off; j < hash.length; j++) {
				if (hash[j] != b[i++]) return null;
			}

			byte[] raw = new byte[len - 16];
			System.arraycopy(b, off + 16, raw, 0, raw.length);
			return raw;
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		}
	}

	/**
	 * 去掉SHA1签名，返回数据流
	 * @param b 加密的数据流
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回解码后的字节数组
	 * @throws SecureException
	 */
	public static byte[] sha1(byte[] b, int off, int len) throws SecureException {
		if (len <= 20) {
			throw new SecureException("SHA1 <= 20");
		}
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(b, off + 20, len - 20);
			byte[] hash = md.digest();

			for (int j = 0, i = off; j < hash.length; j++) {
				if (hash[j] != b[i++]) return null;
			}

			byte[] raw = new byte[len - 20];
			System.arraycopy(b, off + 20, raw, 0, raw.length);
			return raw;
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		}
	}

}