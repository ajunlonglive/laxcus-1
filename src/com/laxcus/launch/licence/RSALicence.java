/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch.licence;

import java.security.*;

/**
 * 许可证签名
 * 
 * @author scott.liang
 * @version 1.0 7/9/2020
 * @since laxcus 1.0
 */
public class RSALicence {

	/** 分隔符，在正文和填充数组之间 **/
	static byte[] SPLITTER = new byte[] { (byte) 0xDF, (byte) 0xB8, (byte) 0x7D,
		(byte) 0x25, (byte) 0xC0, (byte) 0xA7, (byte) 0xDA, (byte) 0x0D,
		(byte) 0xEA, (byte) 0xD3, (byte) 0xF8, (byte) 0x70, (byte) 0x42,
		(byte) 0x0D, (byte) 0xD8, (byte) 0x42, (byte) 0x00, (byte) 0x0E,
		(byte) 0xC5, (byte) 0xAF, (byte) 0x86, (byte) 0x11, (byte) 0xB5,
		(byte) 0x5B, (byte) 0x7B, (byte) 0x5D, (byte) 0xB5, (byte) 0xE0,
		(byte) 0x46, (byte) 0x34, (byte) 0x32, (byte) 0x8B, (byte) 0xF2,
		(byte) 0x95, (byte) 0x18, (byte) 0x45, (byte) 0x07, (byte) 0xAA,
		(byte) 0x15, (byte) 0xAA, (byte) 0xFB, (byte) 0x6A, (byte) 0x48,
		(byte) 0xDC, (byte) 0x9C, (byte) 0xEC, (byte) 0x58, (byte) 0xAA,
		(byte) 0xD6, (byte) 0x72, (byte) 0x18, (byte) 0x13, (byte) 0x89,
		(byte) 0x6A, (byte) 0xD7, (byte) 0x5A, (byte) 0x51, (byte) 0xAD,
		(byte) 0xDF, (byte) 0x02, (byte) 0xAA, (byte) 0x12, (byte) 0x3D,
		(byte) 0x40, (byte) 0xF2, (byte) 0x4C, (byte) 0xB4, (byte) 0xA6,
		(byte) 0x39, (byte) 0xD7, (byte) 0xB4, (byte) 0xC3, (byte) 0xF6,
		(byte) 0x02, (byte) 0x89, (byte) 0xD3, (byte) 0x40, (byte) 0xA7,
		(byte) 0xF1, (byte) 0x56, (byte) 0x1F, (byte) 0xCF, (byte) 0x69,
		(byte) 0xB4, (byte) 0x95, (byte) 0xA7, (byte) 0xEE, (byte) 0x22,
		(byte) 0x74, (byte) 0xBF, (byte) 0x79, (byte) 0x8C, (byte) 0x95,
		(byte) 0x67, (byte) 0x96, (byte) 0x3C };

	/**
	 * 根据数据内容产生数字签名
	 * @param content 数据内容
	 * @return SHA512签名
	 */
	protected static byte[] doSHA512(byte[] b, int off, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(b, off, len);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {

		}
		return null;
	}

	/**
	 * 字符串转换数位
	 * @param c 原始位
	 * @return 修改位
	 */
	private static byte ctoc(byte c) {
		byte low = (byte) ((c >>> 4) & 0xF);
		byte height = (byte) ((c & 0xF) << 4);
		return (byte) (height | low);
	}

	/**
	 * 置换位置
	 * @param b
	 */
	private static void convert(byte[] b) {
		int len = (b.length - b.length % 2);
		for (int i = 0; i < len; i += 2) {
			byte w = b[i];
			b[i] = RSALicence.ctoc(b[i + 1]);
			b[i + 1] = RSALicence.ctoc(w);
		}
	}

	/**
	 * 根据长度产生数字签名
	 * 
	 * @param value 长度值
	 * @return SHA512签名
	 */
	private static byte[] doSign(long value) {
		byte[] b = new byte[8];

		// 从低到高，转成字节
		int off = 0;
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) ((value >>> off) & 0xFF);
			off += 8;
		}

		RSALicence.convert(b);

		// 生成SHA512签名
		byte[] sign = RSALicence.doSHA512(b, 0, b.length);
		// 置换数位
		if (sign != null) {
			RSALicence.convert(sign);
			return sign;
		}
		return null;
	}

	/**
	 * 异或数据
	 * @param content 内容
	 * @param pwd 密码
	 */
	protected static void xor(byte[] content, byte[] pwd) {
		for (int off = 0; off < content.length;) {
			int left = content.length - off; // 剩余字节
			int len = (left > pwd.length ? pwd.length : left);
			for (int i = 0; i < len; i++) {
				int seek = off + i;
				content[seek] ^= pwd[i];
			}
			off += len;
		}
	}

	/**
	 * 生成混淆数据 <br>
	 * 流程：<br>
	 * 1. 根据内容长度产生SHA512签名，做为密码 <br>
	 * 2. 密码和内容进行混淆，加密数据。<br>
	 * 
	 * @param content 数据内容
	 * @return 返回混淆结果
	 */
	protected static void admix(byte[] content) {
		byte[] pwd = RSALicence.doSign(content.length);
		RSALicence.xor(content, pwd);
	}

	/**
	 * 生成混淆数据 <br>
	 * 流程：<br>
	 * 1. 根据内容长度产生SHA512签名，做为密码 <br>
	 * 2. 密码和内容进行混淆，加密数据。<br>
	 * 
	 * @param content 数据内容
	 * @return 返回混淆结果
	 */
	protected static void confuse(byte[] content) {
		byte[] pwd = RSALicence.doSign(content.length);
		RSALicence.xor(content, pwd);
	}

	//	private static void print(byte[] b) {
	//		StringBuilder bf = new StringBuilder();
	//		for(int i =0; i < b.length; i++){
	//			if(i >0) bf.append(",");
	//			String s = String.format("%X", b[i]);
	//			if(s.length() ==1) s = "0" + s;
	//			bf.append(s);
	//
	//		}
	//		System.out.println(bf.toString());
	//	}

	//	public static void main(String[] args) {
	//		System.out.println(RSALicence.SPLITTER.length);
	//		java.io.File src = new java.io.File("i:/licence.xml");
	//		java.io.File dest = new java.io.File("f:/encrypt/work");
	//
	//		boolean success = LicenceWriter.write(src, dest);
	//		System.out.printf("%s -> %s is %s\n", src, dest, success);
	//
	//		byte[] b = LicenceReader.read(dest);
	//		System.out.printf("read %s, len %d\n\n", dest,
	//				(b == null ? -1 : b.length));
	//		if(b != null) {
	//			System.out.println(new String(b));
	//		}
	//	}

}