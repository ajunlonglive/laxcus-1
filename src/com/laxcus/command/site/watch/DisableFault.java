/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.util.classable.*;

/**
 * 屏蔽收到的错误通知 <br>
 * 
 * WATCH节点收到错误通知后，不显示信息，信息包括文字和声音提示。
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public class DisableFault extends ProcessSiteNotice {

	private static final long serialVersionUID = -3796883506278149796L;

	/**
	 * 构造屏蔽收到的错误通知
	 */
	public DisableFault() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析屏蔽收到的错误通知，
	 * @param reader 可类化数据读取器
	 */
	public DisableFault(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成屏蔽收到的错误通知副本
	 * @param that 屏蔽收到的错误通知
	 */
	private DisableFault(ProcessSiteNotice that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DisableFault duplicate() {
		return new DisableFault(this);
	}

}
