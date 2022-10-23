/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.net.*;
import java.util.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * LAXCUS类对象码生成器。<br><br>
 * 
 * LAXCUS类对象码实现每个类的唯一性，它包括三个元素：<br>
 * 1. 本地MAC地址 （可选） <br>
 * 2. 类所在目录路径 <br>
 * 3. 类实例名称 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 1/29/2021
 * @since laxcus 1.0
 */
public class ClassCodeCreator {

	/**
	 * 生成LAXCUS类对象码生成器
	 * @param clazz 类对象
	 * @param padding 填充字节数组
	 * @return ClassCode实例
	 */
	public static ClassCode create(Class<?> clazz, byte[] padding) {
		ClassWriter writer = new ClassWriter();

		// 1. 本地全部MAC地址
		try {
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
			while (enumeration.hasMoreElements()) {
				NetworkInterface networkInterface = enumeration.nextElement();
				if (networkInterface != null) {
					// MAC地址！
					byte[] bytes = networkInterface.getHardwareAddress();
					if (bytes != null && bytes.length > 0) {
						writer.write(bytes);
					}
				}
			}
		} catch (SocketException e) {

		}

		// 2. 类文件路径，不包括类名称
		String s = clazz.getResource("").toString();
		if (s != null) {
			writer.writeString(s);
		}
		// 3. 类名称
		writer.writeString(clazz.getName());
		// 4. 填充值不等于0时，追加它
		if (padding != null && padding.length > 0) {
			writer.write(padding);
		}
		// 输出
		byte[] b = writer.effuse();

		// 生成散列码
		MD5Hash hash = Laxkit.doMD5Hash(b, 0, b.length);
		return new ClassCode(hash);
	}
	
	/**
	 * 生成LAXCUS类对象码生成器
	 * @param clazz 类
	 * @param padding 填充值
	 * @return 返回对象码实例
	 */
	public static ClassCode create(Class<?> clazz, long padding) {
		if (padding != 0) {
			return ClassCodeCreator.create(clazz, Laxkit.toBytes(padding));
		} else {
			return ClassCodeCreator.create(clazz, null);
		}
	}

	/**
	 * 生成LAXCUS类对象码生成器
	 * @param clazz 类对象
	 * @return ClassCode实例
	 */
	public static ClassCode create(Class<?> clazz) {
		return ClassCodeCreator.create(clazz, null);
	}

	/**
	 * 依赖类对象生成类码
	 * @param object 对象实例
	 * @return ClassCode实例
	 */
	public static ClassCode create(Object object) {
		return ClassCodeCreator.create(object.getClass(), null);
	}

	/**
	 * 依赖类对象生成类码
	 * @param object 对象实例
	 * @param padding 填充值
	 * @return ClassCode实例
	 */
	public static ClassCode create(Object object, long padding) {
		return ClassCodeCreator.create(object.getClass(), padding);
	}

//	public static void main(String[] args) {
//		//		ClassCodeCreator m = new ClassCodeCreator();
//		//		System.out.println(m.getClass().getResource("").toString());
//		//		System.out.println(m.getClass().getResource("").getPath());
//		//		System.out.println(m.getClass().getResource("/"));
//		//		System.out.println(m.getClass().getResource("/").getPath());
//		//		System.out.printf("%X\n", System.currentTimeMillis());
//
//		ClassCode code = ClassCodeCreator.create(Halffer.class, System.currentTimeMillis());
//		System.out.println(code);
//
//		//		System.out.printf("cipher code %X, %X\n", Cipher.initSeed, Cipher.initSeed + System.currentTimeMillis());
//		//		long value = Long.MIN_VALUE + System.currentTimeMillis();
//		//		System.out.printf("%X / %d\n", value, value );
//	}

}