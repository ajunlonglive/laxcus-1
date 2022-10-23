/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * DELETE诊断命令。<br><br>
 * 
 * 此命令CALL站点发出，在收到全部DATA站点的DeleteSumbit命令，进行汇总，反馈给DATA站点。DeleteReply由CALL站点发送一次。
 * 
 * @author scott.liang
 * @version 1.12 9/07/2016
 * @since laxcus 1.0
 */
public final class AssertDelete extends AssertConsult {

	private static final long serialVersionUID = 7522875670087318262L;

	/**
	 * 构造默认和私有的DELETE诊断命令
	 */
	private AssertDelete() {
		super();
	}

	/**
	 * 根据传入的DELETE诊断命令，生成它的数据副本
	 * @param that AssertDelete实例
	 */
	private AssertDelete(AssertDelete that) {
		super(that);
	}

	/**
	 * 构造DELETE诊断命令，指定表名和状态码
	 * @param space 表名
	 * @param status 状态码
	 */
	public AssertDelete(Space space, byte status) {
		this();
		setSpace(space);
		setStatus(status);
	}

	/**
	 * 从可类化数据读取器中解析DELETE诊断命令。
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AssertDelete(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AssertDelete duplicate() {
		return new AssertDelete(this);
	}
}
