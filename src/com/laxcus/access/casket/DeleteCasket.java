/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.casket;

import com.laxcus.command.access.*;
import com.laxcus.util.classable.*;

/**
 * DELETE封装包。<br><br>
 * 
 * DELETE操作只能发生在DATA主站点上，DATA从站点以“备份”的形式复制主站点的DELETE数据。
 * 
 * @author scott.liang
 * @version 1.0 12/08/2013
 * @since laxcus 1.0
 */
public class DeleteCasket extends StubCasket {

	/** DELETE实例 **/
	private Delete cmd;

	/**
	 * 构造默认的DELETE封装包。
	 */
	public DeleteCasket() {
		super();
	}

	/**
	 * 构造DELETE封装包，指定参数
	 * @param cmd DELETE命令
	 * @param stub 数据块编号
	 */
	public DeleteCasket(Delete cmd, long stub) {
		this();
		setDelete(cmd);
		setStub(stub);
	}

	/**
	 * 构造DELETE封装包，指定参数
	 * @param cmd DELETE命令
	 * @param stub 数据块编号
	 * @param filename ShiftStack文件（被删除的数据写入这个文件）
	 */
	public DeleteCasket(Delete cmd, long stub, String filename) {
		this(cmd, stub);
		setFile(filename);
	}

	/**
	 * 设置DELETE命令
	 * @param e
	 */
	public void setDelete(Delete e) {
		cmd = e;
	}

	/**
	 * 返回DELETE命令
	 * @return
	 */
	public Delete getDelete() {
		return cmd;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.act.PutStub#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);

		byte[] b = cmd.buildX();
		writer.writeInt(b.length);
		writer.write(b);
	}

}