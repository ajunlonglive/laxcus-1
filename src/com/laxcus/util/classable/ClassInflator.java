/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.classable;

/**
 * 可类化数据编码器。执行“加密/压缩”操作。<br>
 * 与可类化数据存储器（ClassWriter）配合使用，在ClassWriter输出时。
 * 
 * @author scott.liang
 * @version 1.0 3/29/2015
 * @since laxcus 1.0
 */
public interface ClassInflator {
	
	/**
	 * 将传入的数据进行“压缩/加密”处理，输出为密文。
	 * @param b 原始字节数组（没有“加密/压缩”状态）
	 * @param off 开始下标
	 * @param len 有效字节长度
	 * 
	 * @return 压缩加密后的字节数组
	 * @throws ClassableException
	 */
	byte[] inflate(byte[] b, int off, int len) throws ClassableException;
}