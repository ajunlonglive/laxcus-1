/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.each;

/**
 * LAXCUS EACH数字签名。<br><br>
 * 
 * 这个签名算法对传入的字节数组进行签名，返回一个64位的长整型。
 * 值的范围在0 - java.lang.Long.MAX_VALUE之间，实际是一个63位的长整形正数值。<br><br>
 * 
 * EACH签名算法为LAXCUS大数据管理系统私有。
 * 
 * @author scott.liang
 * @version 1.0 1/5/2009
 * @since laxcus 1.0
 */
final class Each {

	/**
	 * 数据签名(输入二进制字节数组，产生一个64位整型值)
	 * @param b 字节数组
	 * @param off 字节数组开始下标
	 * @param len 长度
	 * @return 返回签名数值
	 */
	static native long sign(byte[] b, int off, int len);

}