/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.security;

import java.io.*;
import java.security.*;
import java.security.interfaces.*;
import javax.crypto.*;

/**
 * 密文加密器。
 * 
 * @author scott.liang
 * @version 1.0 10/28/2009
 * @since laxcus 1.0
 */
public class SecureEncryptor extends SecureGenerator {
	
	/**
	 * 使用RSA公钥加密数据流
	 * @param key RSA公钥
	 * @param b 数据明文的字节数组
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回加密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] rsa(RSAPublicKey key, byte[] b, int off, int len) throws SecureException {
		if (key == null) {
			throw new SecureException("cannot be null");
		}
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
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
	 * 使用AES算法和密码加密数据流
	 * @param pwd AES密码
	 * @param b 数据明文的字节数组
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回加密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] aes(byte[] pwd, byte[] b, int off, int len) throws SecureException {
		SecretKey key = SecureGenerator.buildAESKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.AES_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, key);
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
	 * 使用DES算法和密码加密数据流
	 * 
	 * @param pwd DES密码
	 * @param b 数据明文的字节数组
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回加密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] des(byte[] pwd, byte[] b, int off, int len) throws SecureException {
		SecretKey key = SecureGenerator.buildDESKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.DES_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, key);

			ByteArrayOutputStream buff = new ByteArrayOutputStream(len - len % 8 + 8);
			CipherOutputStream cos = new CipherOutputStream(buff, cipher);
			cos.write(b, off, len);
			cos.flush();
			cos.close();
			return buff.toByteArray();
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		} catch (NoSuchPaddingException exp) {
			throw new SecureException(exp);
		} catch (InvalidKeyException exp) {
			throw new SecureException(exp);
		} catch (IOException exp) {
			throw new SecureException(exp);
		}
	}

	/**
	 * 使用DES3算法和密码加密数据流
	 * @param pwd DES3密码
	 * @param b 数据明文的字节数组
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回加密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] des3(byte[] pwd, byte[] b, int off, int len) throws SecureException {
		SecretKey key = SecureGenerator.buildDES3Key(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.DES3_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, key);

			ByteArrayOutputStream buff = new ByteArrayOutputStream(len - len % 8 + 8);
			CipherOutputStream cos = new CipherOutputStream(buff, cipher);
			cos.write(b, off, len);
			cos.flush();
			cos.close();
			return buff.toByteArray();
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		} catch (NoSuchPaddingException exp) {
			throw new SecureException(exp);
		} catch (InvalidKeyException exp) {
			throw new SecureException(exp);
		} catch (IOException exp) {
			throw new SecureException(exp);
		}
	}

	/**
	 * 使用BLOWFISH算法和密码加密数据流
	 * @param pwd BLOWFISH密码
	 * @param b 数据明文的字节数组
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回加密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] blowfish(byte[] pwd, byte[] b, int off, int len) throws SecureException {
		SecretKey key = SecureGenerator.buildBlowfishKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.ENCRYPT_MODE, key);
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
	 * 对MD5数据流进行编码，格式:MD5签名(16字节)+数据
	 * 
	 * @param b 数据明文的字节数组
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回编码后的字节数组
	 * @throws SecureException
	 */
	public static byte[] md5(byte[] b, int off, int len) throws SecureException {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(b, off, len);
			byte[] hash = md.digest();

			ByteArrayOutputStream buff = new ByteArrayOutputStream(hash.length + len);
			buff.write(hash, 0, hash.length);
			buff.write(b, off, len);
			return buff.toByteArray();
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		}
	}

	/**
	 * 对SHA1数据流进行编码，格式:SHA1签名(20字节)+数据
	 * 
	 * @param b 数据明文的字节数组
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 返回编码后的字节数组
	 * @throws SecureException
	 */
	public static byte[] sha1(byte[] b, int off, int len) throws SecureException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(b, off, len);
			byte[] hash = md.digest();

			ByteArrayOutputStream buff = new ByteArrayOutputStream(hash.length + len);
			buff.write(hash, 0, hash.length);
			buff.write(b, off, len);
			return buff.toByteArray();
		} catch (NoSuchAlgorithmException exp) {
			throw new SecureException(exp);
		}
	}

	//	public static void main(String[] args) {
	//		Provider[] ps = Security.getProviders();
	//		for(int i = 0; i < ps.length; i++) {
	//			System.out.printf("provider name:%s\n", ps[i].getName());
	//		}
	//		
	//		byte[] pwd = "www.laxcus.com".getBytes();
	//		
	////		StringBuilder sb = new StringBuilder();
	////		for(int i = 0; i < 4600; i++) {
	////			sb.append('a');
	////		}
	////		byte[] data = sb.toString().getBytes();
	////		for(int i = 0; i <data.length; i++) data[i] = (byte)65;
	//		
	//		byte[] data = "UnixSystem+Pentium@laxcus/SERVER".getBytes();
	//		
	//		byte[] raw = SecureEncryptor.desEncrypt(pwd, data);
	//		System.out.printf("origin data size: %d\n", data.length);
	//		System.out.printf("encrypt des3 raw size:%d, [%s]\n", raw.length, new String(raw));
	//		
	//		byte[] b = SecureDecryptor.desDecrypt(pwd, raw);
	//		System.out.printf("decrypt string:%s\n", new String(b));
	//		
	////		raw = SecureEncryptor.md5Encrypt(data);
	////		System.out.printf("data size:%d, md5 hash size:%d\n", data.length, raw.length);
	////		
	////		raw = SecureEncryptor.sha1Encrypt(data);
	////		System.out.printf("data size:%d, sha1 hash size:%d", data.length, raw.length);
	//	}


	//	public static void main(String[] args) {
	//		byte[] pwd = "www.laxcus.com".getBytes();
	//		byte[] data = "UNIX-SERVER".getBytes();
	//
	//		byte[] raw = SecureEncryptor.blowfishEncrypt(pwd, data);
	//		System.out.printf("origin data size: %d\n", data.length);
	//		System.out.printf("encrypt blowfish raw size:%d, String:%s\n", raw.length, new String(raw));
	//		
	//		byte[] b = SecureDecryptor.blowfishDecrypt(pwd, raw);
	//		System.out.printf("decrypt string:%s\n", new String(b));
	//	}

	//	public static void main(String[] args) {
	//		byte[] pwd = "www.laxcus.com".getBytes();
	//		byte[] data = "laxcus".getBytes();
	//
	//		byte[] raw = SecureEncryptor.aes(pwd, data);
	//		System.out.printf("origin data size: %d\n", data.length);
	//		System.out.printf("encrypt aes raw size:%d, String:%s\n", raw.length, new String(raw));
	//		
	//		byte[] b = SecureDecryptor.aes(pwd, raw);
	//		System.out.printf("decrypt string:%s\n", new String(b));
	//	}

	//	public static void main(String[] args) {
	//		String s = "remark1";
	//		byte[] pwd = "pentium".getBytes();
	//		try {
	//			// AES算法
	//			byte[] b = SecureEncryptor.aes(pwd, s.getBytes(), 0, s.getBytes().length);
	//			for(int i = 0; i < b.length; i++) {
	//				System.out.printf("%X ", b[i] & 0xff);
	//			}
	//
	//			b = SecureDecryptor.aes(pwd, b, 0, b.length);
	//			System.out.printf("\n%s\n\n", new String(b));
	//			
	//			// DES3 算法
	//			b = SecureEncryptor.des3(pwd, s.getBytes(), 0, s.getBytes().length);
	//			for(int i = 0; i < b.length; i++) {
	//				System.out.printf("%X ", b[i] & 0xff);
	//			}
	//			b = SecureDecryptor.des3(pwd, b, 0, b.length);
	//			System.out.printf("\n%s\n\n", new String(b));
	//
	//			// DES 算法
	//			b = SecureEncryptor.des(pwd, s.getBytes(), 0, s.getBytes().length);
	//			for(int i = 0; i < b.length; i++) {
	//				System.out.printf("%X ", b[i] & 0xff);
	//			}
	//			b = SecureDecryptor.des(pwd, b, 0, b.length);
	//			System.out.printf("\n%s\n\n", new String(b));
	//			
	//			// BLOWFISH算法
	//			b = SecureEncryptor.blowfish(pwd, s.getBytes(), 0, s.getBytes().length);
	//			for(int i = 0; i < b.length; i++) {
	//				System.out.printf("%X ", b[i] & 0xff);
	//			}
	//			b = SecureDecryptor.blowfish(pwd, b, 0, b.length);
	//			System.out.printf("\n%s\n\n", new String(b));
	//
	//			byte[] data = "UnixSystem".getBytes();
	//			byte[] result = com.laxcus.security.SecureEncryptor.sha1(data, 0, data.length);
	//			System.out.printf("md5 encrypt is:%d\n", result.length);
	//
	//			data = com.laxcus.security.SecureDecryptor.sha1(result, 0, result.length);
	//			System.out.printf("md5 decrypt is:%d\n", data.length);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//	}

}