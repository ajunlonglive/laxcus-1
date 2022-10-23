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
 * 屏蔽收到的警告通知 <br>
 * 
 * WATCH节点收到警告通知后，不显示信息，信息包括文字和声音提示。
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public class DisableWarning extends ProcessSiteNotice {

	private static final long serialVersionUID = 4597225086344210345L;

	/**
	 * 构造屏蔽收到的警告通知
	 */
	public DisableWarning() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析屏蔽收到的警告通知，
	 * @param reader 可类化数据读取器
	 */
	public DisableWarning(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成屏蔽收到的警告通知副本
	 * @param that 屏蔽收到的警告通知
	 */
	private DisableWarning(ProcessSiteNotice that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DisableWarning duplicate() {
		return new DisableWarning(this);
	}

}
