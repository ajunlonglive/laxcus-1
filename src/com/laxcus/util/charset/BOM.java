/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.charset;

/**
 * BOM(Byte Order Mark)。字节顺序标记。<br>
 * 出现在文件的开始位置。
 * 
 * @author scott.liang
 * @version 1.0 5/14/2019
 * @since laxcus 1.0
 */
public class BOM {

	/** UTF8编码  **/
	public static final byte[] UTF8 = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

	public static final byte[] UTF16_BE = { (byte) 0xFE, (byte) 0xFF };

	public static final byte[] UTF16_LE = { (byte) 0xFF, (byte) 0xFE };

	public static final byte[] UTF32_BE = { (byte) 0x0, (byte) 0x0, (byte) 0xFE, (byte) 0xFF };

	public static final byte[] UTF32_LE = { (byte) 0xFF, (byte) 0xFE, (byte) 0x0, (byte) 0x0 };

	public static final byte[] GB18030 = { (byte) 0x84, (byte) 0x31, (byte) 0x95, (byte) 0x33 };
	
	/**
	 * 检查匹配的BOM符号
	 * @param charset
	 * @return
	 */
	public static byte[] find(String charset) {
		int who = CharsetType.translate(charset);
		return BOM.find(who);
	}
	
	/**
	 * 查找匹配的BOM的字节序列
	 * @param charset 字符集
	 * @return 返回BOM字节序列，没有是空指针。
	 */
	public static byte[] find(int charset) {
		switch(charset) {
		case CharsetType.UTF8:
			return BOM.UTF8;
		case CharsetType.UTF16_BE:
			return BOM.UTF16_BE;
		case CharsetType.UTF16_LE:
			return BOM.UTF16_LE;
		case CharsetType.UTF32_BE:
			return BOM.UTF32_BE;
		case CharsetType.UTF32_LE:
			return BOM.UTF32_LE;
		case CharsetType.GB18030:
			return BOM.GB18030;
		}
		return null;
	}
}