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
 * SELECT封装包。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 12/08/2015
 * @since laxcus 1.0
 */
public class SelectCasket extends StubCasket {

	/** SELECT实例 **/
	private Select cmd;

	/**
	 * 构造默认的SELECT封装包
	 */
	public SelectCasket() {
		super();
	}

	/**
	 * 构造SELECT封装包，指定参数
	 * @param cmd SQL SELECT命令
	 * @param stub 数据块编号
	 */
	public SelectCasket(Select cmd, long stub) {
		this();
		setSelect(cmd);
		setStub(stub);
	}

	/**
	 * 设置SQL SELECT命令
	 * @param e SELECT对象实例
	 */
	public void setSelect(Select e) {
		cmd = e;
	}

	/**
	 * 返回SQL SELECT命令
	 * @return Select对象实例
	 */
	public Select getSelect() {
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