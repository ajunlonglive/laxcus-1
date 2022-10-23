/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.put;

import com.laxcus.distribute.*;
import com.laxcus.task.*;
import com.laxcus.task.util.*;

/**
 * MINE PUT阶段存取盒
 * 
 * @author scott.liang
 * @version 1.0 5/27/2018
 * @since laxcus 1.0
 */
public class MinePutBox extends TaskParameterBox {

	/** 节点描述 **/
	private String node;

	/** 明文描述 **/
	private String text;

	/** 签名描述 **/
	private String sha256;

	/**
	 * 构造默认的存取盒
	 */
	public MinePutBox() {
		super();
	}

	/**
	 * “挖矿节点”的文本描述
	 * @return
	 */
	public String getNode() {
		return node;
	}

	/**
	 * “明文”的文本描述
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * “矿码”的文本描述
	 * @return
	 */
	public String getSHA256() {
		return this.sha256;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.util.ParameterFactory#readAll(com.laxcus.distribute.AccessObject)
	 */
	@Override
	public void readAll(AccessObject access) throws TaskException {
		// 字符串关键字在语句中定义
		node = findString(access, "NODE");
		sha256 = findString(access, "SHA256");
		text = findString(access, "TEXT");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.util.ParameterFactory#writeAll(com.laxcus.distribute.AccessObject)
	 */
	@Override
	public void writeAll(AccessObject access) {
		// TODO Auto-generated method stub

	}

}
