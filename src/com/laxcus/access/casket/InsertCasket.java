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
 * INSERT封装包 <br><br>
 * 
 * INSERT只发生在DATA主站点上，DATA从站点以“备份”的形式复制主站点的INSERT数据。
 * 
 * @author scott.liang
 * @version 1.0 12/08/2013
 * @since laxcus 1.0
 */
public class InsertCasket extends AccessCasket {

	/** INSERT命令 **/
	private Insert cmd;

	/**
	 * 构造默认的INSERT封装包
	 */
	public InsertCasket() {
		super();
	}

	/**
	 * 构造INSERT封装包，设置命令句柄
	 * @param cmd INSERT实例
	 */
	public InsertCasket(Insert cmd) {
		this();
		setInsert(cmd);
	}

	/**
	 * 构造INSERT封闭命令，设置命令和写入的文件名
	 * @param cmd INSERT命令
	 * @param filename ShiftStack文件
	 */
	public InsertCasket(Insert cmd, String filename) {
		this(cmd);
		setFile(filename);
	}

	/**
	 * 设置INSERT命令
	 * @param e
	 */
	public void setInsert(Insert e) {
		cmd = e;
	}

	/**
	 * 返回INSERT命令
	 * @return
	 */
	public Insert getInsert() {
		return cmd;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.act.PutAct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		byte[] b = cmd.buildX();
		writer.writeInt(b.length);
		writer.write(b);
	}

}