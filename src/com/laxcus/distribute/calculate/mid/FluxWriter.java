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
 * CONDUCT命令中间数据写入器。
 * 
 * @author scott.liang
 * @version 1.1 3/15/2013
 * @since laxcus 1.0
 */
public interface FluxWriter extends MiddleWriter {

	/**
	 * 收集中间数据映像域，合并组成中间数据区域区返回
	 * @return 返回合并后的FluxArea实例
	 */
	FluxArea collect();
	
	/**
	 * 数据写入磁盘，返回这块数据的中间数据映像域。如果失败，返回空指针(null)
	 * @param mod  模值
	 * @param elements 成员数
	 * @param b 写入的字节数组
	 * @param off 指定的字节数组下标
	 * @param len 指定有效长度
	 * @return 返回中间映像数据域FluxField
	 */
	FluxField append(long mod, int elements, byte[] b, int off, int len);

	/**
	 * 更新数据到磁盘文件的指定位置。如果失败，返回空指针
	 * @param mod 模
	 * @param seek 磁盘文件/内存下标
	 * @param b 字节数组
	 * @param off 数组开始位置
	 * @param len 有效字节长度
	 * @return 返回中间数据映像域FluxField
	 */
	FluxField update(long mod, long seek, byte[] b, int off, int len);

}