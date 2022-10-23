/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.each;

import com.laxcus.util.charset.*;

/**
 * EACH数字签名算法委托器。
 * 所有签名必须在获得授权的情况下才能调用和执行。
 * 
 * @author scott.liang
 * @version 1.0 1/5/2009
 * @since laxcus 1.0
 */
public class EachTrustor {

	/**
	 * 对签名操作进行安全检查
	 */
	private static void check() {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = "sign";
			sm.checkPermission(new EachPermission(name));
		}
	}

	/**
	 * 生成数据签名
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 返回签名（长整型）
	 */
	public static long sign(byte[] b, int off, int len) {
		EachTrustor.check();
		return Each.sign(b, off, len);
//		return Long.MAX_VALUE;
	}

	/**
	 * 生成数据签名
	 * @param b 字节数组
	 * @return 返回签名（长整型）
	 */
	public static long sign(byte[] b) {
		return EachTrustor.sign(b, 0, b.length);
	}

	/**
	 * 把字符串编码，生成数字签名
	 * @param text 文本字符串
	 * @return 返回签名（长整型）
	 */
	public static long sign(String text) {
		byte[] b = new UTF8().encode(text);
		return EachTrustor.sign(b);
	}

}