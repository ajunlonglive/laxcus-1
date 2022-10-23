/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.meta;

import com.laxcus.task.*;

/**
 * 元数据缓存代理。 <br>
 * 提供元数据的读、写、查询的服务。<br>
 * 这个代理被CONDUCT/INIT, CONDUCT/BALANCE，ESTABLISH/ISSUE, ESTABLISH/ASSIGN共同使用。<br>
 * 
 * @author scott.liang
 * @version 1.12 12/23/2015
 * @since laxcus 1.0
 */
public interface MetaTrustor {

	/**
	 * 写入元数据。用户只能写自己的元数据。
	 * @param tag 元数据标识
	 * @param b 元数据字节数组
	 * @param off 有效数据的开始下标
	 * @param len 有效数据长度
	 * @return 写入成功返回真，否则假
	 * @throws TaskException - 如果用户写其他用户的数据，或者内存溢出时，将弹出异常。
	 */
	boolean write(MetaTag tag, byte[] b, int off, int len)
			throws TaskException;

	/**
	 * 读取元数据。用户只能读出自己的元数据。
	 * @param tag 元数据标识
	 * @return 返回字节数组
	 * @throws TaskException - 如果用户读其他用户的数据，或者数据不存在时，将弹出异常。
	 */
	byte[] read(MetaTag tag) throws TaskException;

	/**
	 * 删除元数据
	 * @param tag 元数据标识
	 * @return 删除成功返回真，否则假
	 */
	boolean remove(MetaTag tag);

	/**
	 * 判断元数据存在
	 * @param tag 元数据标识
	 * @return 返回真或者假
	 */
	boolean contains(MetaTag tag);

}