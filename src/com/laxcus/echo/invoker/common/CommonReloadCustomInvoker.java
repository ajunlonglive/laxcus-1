/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.reload.*;
import com.laxcus.echo.invoker.custom.*;

/**
 * 重新发布自定义包命令调用器。<br><br>
 * 
 * 这个调用器部署在除TOP/HOME/WATCH/FRONT之外的所有节点。<br>
 * WATCH/FRONT节点的"auto-update=yes"，自动感应新JAR包后更新。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/13/2017
 * @since laxcus 1.0
 */
public class CommonReloadCustomInvoker extends CommonInvoker {

	/**
	 * 构造重新发布自定义包命令调用器，指定命令
	 * @param cmd 重新发布自定义包命令
	 */
	public CommonReloadCustomInvoker(ReloadCustom cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReloadCustom getCommand() {
		return (ReloadCustom) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 强制启动重新加载和发布
		CustomClassPool.getInstance().setForceUpdate(true);

		// 反馈处理结果
		ReloadCustomProduct product = new ReloadCustomProduct();
		product.add(getLocal(), true);
		replyProduct(product);

		return useful();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

//	/**
//	 * 加载新的网络通信安全配置
//	 * @return 返回真或者假
//	 */
//	protected boolean reload1() {
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

}