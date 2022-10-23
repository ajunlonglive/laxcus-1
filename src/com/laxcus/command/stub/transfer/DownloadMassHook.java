/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import com.laxcus.command.*;

/**
 * 传输数据块命令钩子
 * 
 * @author scott.liang
 * @version 1.0 11/23/2012
 * @since laxcus 1.0
 */
public class DownloadMassHook extends CommandHook {

	/** 写入JNI.DB的文件 **/
	private String filename;

	/**
	 * 构造默认的传输数据块命令钩子
	 */
	public DownloadMassHook() {
		super();
	}

	/**
	 * 设置文件名
	 * @param e 文件名
	 */
	public void setFilename(String e) {
		filename = e;
	}

	/**
	 * 返回文件名
	 * @return 文件名
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * 判断成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return filename != null;
	}

}
