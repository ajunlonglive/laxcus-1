/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.mid;

import com.laxcus.distribute.mid.*;

/**
 * CONDUCT命令中间数据读取器
 * 
 * @author scott.liang
 * @version 1.1 3/15/2013
 * @since laxcus 1.0
 */
public interface FluxReader extends MiddleReader {

	/**
	 * 从磁盘或者内存中读出指定范围的数据流。如果成功，返回指定长度的字节数组；如果失败，返回空指针(null)
	 * @param mod 模值
	 * @param seek 数据下标位置（使用长整型，可以处理超过2G的超大数据）
	 * @param len 数据读取长度（单次读取的数据长度）
	 * @return 指定长度的字节数组，或者空指针
	 */
	byte[] read(long mod, long seek, int len);
}