/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.limit;

import com.laxcus.util.classable.*;

/**
 * 建立限制操作命令。<br>
 * 
 * 这是一个限制操作预定义命令。由FRONT用户发出，目标是TOP站点。要求TOP保存，并转存到GATE站点。<br>
 * 当数据处理发生“写操作”错误时，将启动“CreateLimit”命令，相关的资源将被锁定。<br>
 * 
 * 本命令的对应命令是“DropLimit”。
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public class CreateLimit extends PostLimit {

	private static final long serialVersionUID = 9026267510495835594L;

	/**
	 * 构造默认的建立限制操作命令
	 */
	public CreateLimit() {
		super();
	}

	/**
	 * 生成建立限制操作命令的数据副本
	 * @param that CreateLimit实例
	 */
	private CreateLimit(CreateLimit that) {
		super(that);
	}
	
	/**
	 * 从可类化数据读取器中解析建立限制操作命令
	 * @param reader 可类化数据读取器
	 */
	public CreateLimit(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CreateLimit duplicate() {
		return new CreateLimit(this);
	}

}