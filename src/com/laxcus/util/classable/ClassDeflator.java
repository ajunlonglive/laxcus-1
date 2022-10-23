/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.classable;

/**
 * 可类化数据解码器。执行“解压/解密”操作。<br>
 * 与“可类化数据读取器（ClassReader）”配合使用，在可类化读取器解析数据之前，将密文还原为明文。
 * 
 * @author scott.liang
 * @version 1.0 3/29/2015
 * @since laxcus 1.0
 */
public interface ClassDeflator {

	/**
	 * 将传入的数据做“解压/解密”处理，还原成明文。
	 * @param encrypt 已经加密/压缩的字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 输出“解压/解密”后的明文数组
	 */
	byte[] deflate(byte[] encrypt, int off, int len) throws ClassableException;
}
