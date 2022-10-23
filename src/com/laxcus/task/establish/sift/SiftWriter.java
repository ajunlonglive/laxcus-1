/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.sift;

import com.laxcus.distribute.mid.*;

/**
 * SIFT中间数据写入器 <br>
 * 
 * SIFT中间数据是以“模”为标记写入，读取亦是。
 * 
 * @author scott.liang
 * @version 1.2 7/12/2013
 * @since laxcus 1.0
 */
public interface SiftWriter extends MiddleWriter {

	/**
	 * 以模为单位，追加数据
	 * @param mod 模值
	 * @param b 写入数据字节数组
	 * @param off 数据下标
	 * @param len 数据长度
	 * @return 追加的数据长度
	 */
	int append(long mod, byte[] b, int off, int len);
	
	/**
	 * 更新数据。被更新的数据，已经在已有数据之上执行
	 * @param mod 模值
	 * @param seek 已有数据下标
	 * @param b 写入数据字节数组
	 * @param off 数据下标
	 * @param len 数据长度
	 * @return 返回更新的数据长度
	 */
	int update(long mod, long seek, byte[] b, int off, int len);
}
