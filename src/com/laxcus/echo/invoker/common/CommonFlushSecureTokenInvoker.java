/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.secure.*;
import com.laxcus.log.client.*;

/**
 * 输出密钥令牌调用器
 * 把内存中的密钥令牌输出到磁盘保存！
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public abstract class CommonFlushSecureTokenInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造输出密钥令牌调用器，指定命令
	 * @param cmd 输出密钥令牌
	 */
	protected CommonFlushSecureTokenInvoker(FlushSecureToken cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FlushSecureToken getCommand() {
		return (FlushSecureToken) super.getCommand();
	}
	
	/**
	 * 加载新的网络通信安全配置
	 * @return 返回真或者假
	 */
	protected boolean reload() {
		boolean success = getLauncher().writeSecure();
		Logger.debug(this, "reload", success, "write secure token!");
		return success;
	}

//	/**
//	 * 加载新的网络通信安全配置
//	 * @return 返回真或者假
//	 */
//	protected boolean reload() {
//		final String key = "laxcus.site.default";
//		String root = System.getProperty(key);
//		if(root == null) {
//			Logger.debug(this, "reload", "cannot be find %s", key);
//			return false;
//		}
//
//		// 找到本地配置文件
//		File file = new File(root, "/conf/local.xml");
//		boolean success = (file.exists() && file.isFile());
//		// 读取FIXP安全配置文件
//		if (success) {
//			Document document = XMLocal.loadXMLSource(file);
//			success = (document != null);
//			// 成功，读取通信安全配置
//			if (success) {
//				String filename = XMLocal.getXMLValue(document
//						.getElementsByTagName("security-network"));
//				filename = ConfigParser.splitPath(filename);
//				file = new File(filename);
//				success = (file.exists() && file.isFile());
//			}
//		}
//		// 以上成功，清除旧的RSA密钥令牌，加载新的RSA密钥令牌
//		if (success) {
//			SecureTokenParser parser = new SecureTokenParser();
//			// 清除旧数据
//			ClientTokenManager.getInstance().clear();
//			ServerTokenManager.getInstance().clear();
//			// 加载新的RSA密钥令牌到内存
//			success = parser.split(file);
//		}
//		return success;
//	}
	
//	/**
//	 * 加载新的网络通信安全配置
//	 * @return 返回真或者假
//	 */
//	protected boolean reload() {
//		final String key = "laxcus.site.default";
//		final String root = System.getProperty(key);
//		if (root == null) {
//			Logger.debug(this, "reload", "cannot be find %s", key);
//			return false;
//		}
//
//		File file = new File(root, "/conf/security.xml");
//		// 判断配置文件有效
//		boolean success = (file.exists() && file.isFile());
//		// 不成立，找到本地local.xml配置文件，从里面读取参数
//		if (!success) {
//			file = new File(root, "/conf/local.xml");
//			// 读取FIXP安全配置文件
//			Document document = XMLocal.loadXMLSource(file);
//			success = (document != null);
//			// 成功，读取通信安全配置
//			if (success) {
//				String filename = XMLocal.getXMLValue(document.getElementsByTagName("security-network"));
//				filename = ConfigParser.splitPath(filename);
//				success = (filename != null && filename.length() > 0);
//				if (success) {
//					file = new File(filename);
//				}
//			}
//		}
//		
//		// 判断有效
//		success = (file.exists() && file.isFile());
//		// 清除记录，重新加载
//		if (success) {
//			SecureController.getInstance().clear();
//			// 解析密钥令牌
//			SecureTokenParser parser = new SecureTokenParser();
//			success = parser.split(file);
//		}
//		
//		return success;
//	}

}