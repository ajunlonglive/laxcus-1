/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.sift;

import java.util.*;

import com.laxcus.distribute.mid.*;

/**
 * SIFT中间数据读取器 <br>
 * 
 * 数据读取是以模为单位为进行。“模”是一个长整型值，由用户定义，是区分不同数据的标记。
 * 
 * @author scott.liang
 * @version 1.2 7/12/2013
 * @since laxcus 1.0
 */
public interface SiftReader extends MiddleReader {
	
	/**
	 * 返回全部模值
	 * @return 长整型列表
	 */
	List<Long> getMods();
	
	/**
	 * 当前的全部数据
	 * @return 数据的长整型值
	 */
	long length();
	
	/**
	 * 以模为单位，在其下的全部数据长度。
	 * @param mod 模值
	 * @return 返回这个模值下的全部数据，没有是-1L。
	 */
	long length(long mod);

	/**
	 * 指定模值，和数据位置，读取指定的数据
	 * @param mod 模值
	 * @param seek 指定数据下标位置
	 * @param len 指定数据长度
	 * @return 返回对应下标位置的字节数组
	 * @throws ArrayIndexOutOfException, MemoryOutError
	 */
	byte[] read(long mod, long seek, int len);
	
	/**
	 * 以模为单位，读取它下面的全部数据
	 * @param mod 模值
	 * @return 返回这个模值下的全部数据，如果没有是
	 * @throws MemoryOutError
	 */
	byte[] read(long mod);
}