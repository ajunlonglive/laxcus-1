/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.io.*;

import com.laxcus.command.licence.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 发布许可证调用器。<br>
 * 当管理员修改节点conf/site.policy文件后，调用方法重置。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public abstract class CommonMailLicenceInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造发布许可证调用器，指定命令
	 * @param cmd 发布许可证
	 */
	protected CommonMailLicenceInvoker(MailLicence cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MailLicence getCommand() {
		return (MailLicence) super.getCommand();
	}

	/**
	 * 加载本地许可证文件
	 * @return 返回加载结果
	 */
	protected MailLicenceItem reload() {
		// 来自远程WATCH节点的操作，加载本地许可证
		MailLicence cmd = getCommand();
		boolean success = writeLicence(cmd.getContent());
		
//		Logger.debug(this, "reload", success, "write licence");

		// 当写入成功时...
		if (success) {
			// 判断要求立即执行时...
			if (cmd.isImmediate()) {
				success = getLauncher().loadLicence(true);
//				Logger.debug(this, "reload", success, "load licence");
			}
		}

		// 反馈给请求端
		Node local = getLocal();
		return new MailLicenceItem(local, success);
	}
	
	/**
	 * 将许可证内容写入磁盘 
	 * @param content
	 * @return 成功返回真，否则假
	 */
	private boolean writeLicence(byte[] content) {
		String bin = System.getProperty("user.dir");
		bin += "/../conf/licence";
		File file = new File(bin);

		// 写入磁盘文件
		try {
			FileOutputStream out = new FileOutputStream(file);
			out.write(content);
			out.close();
			return true;
		} catch (IOException e) {
			Logger.error(e);
		}
		return false;
	}

}