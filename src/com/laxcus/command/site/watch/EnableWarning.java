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
 * 生效收到的警告通知 <br>
 * 
 * WATCH节点收到警告通知后，显示它们，信息包括文字和声音提示。
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public class EnableWarning extends ProcessSiteNotice {

	private static final long serialVersionUID = 1741512027449064517L;

	/**
	 * 构造生效收到的警告通知
	 */
	public EnableWarning() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析生效收到的警告通知，
	 * @param reader 可类化数据读取器
	 */
	public EnableWarning(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成生效收到的警告通知副本
	 * @param that 生效收到的警告通知
	 */
	private EnableWarning(ProcessSiteNotice that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public EnableWarning duplicate() {
		return new EnableWarning(this);
	}

}
