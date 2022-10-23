/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.shutdown;

import java.io.*;
import java.net.*;

import org.w3c.dom.*;

import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.util.net.*;
import com.laxcus.xml.*;

/**
 * 关闭节点启动器。<br>
 * 
 * 
 * 从local.xml文件中读取配置信息，向指定的站点发起远程终止运行命令
 * 
 * @author scott.liang
 * @version 1.1 12/5/2013
 * @since laxcus 1.0
 */
public class ShutdownLauncher {

	/**
	 * 构造默认的关闭节点启动器
	 */
	public ShutdownLauncher() {
		super();
	}
	
	/**
	 * 从配置文件中找到本地站点地址，通知节点结束服务
	 * @param filename 配置文件名
	 * @return 成功返回真，否则假
	 */
	public boolean launch(String filename) {
		Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return false;
		}

		Element elem = (Element) document.getElementsByTagName("local-site").item(0);
		
		// 单模式站点地址
		String address = XMLocal.getXMLValue(elem.getElementsByTagName("local-node"));
		// 双模式站点地址
		if (address.isEmpty()) {
			address = XMLocal.getXMLValue(elem.getElementsByTagName("inner-node"));
		}
		if (address.isEmpty()) {
			System.out.println("empty site address!");
			return false;
		}

//		String secureFile = XMLocal.getXMLValue(document.getElementsByTagName("security-network"));
//		secureFile = ConfigParser.splitPath(secureFile);
//		
//		// 解析密钥令牌
//		SecureTokenParser parser = new SecureTokenParser();
//		if (!parser.split(secureFile)) {
//			System.out.printf("cannot resolve %s\n", secureFile);
//			return false;
//		}

		// 服务器地址指定，本地地址系统选择端口
		InetSocketAddress local = null;
		SocketHost remote = null;
		try {
			com.laxcus.site.Node node = new com.laxcus.site.Node(address);
			remote = node.getPacketHost();
			local = new InetSocketAddress(remote.getInetAddress(), 0);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}

		// 关闭数据包
		Mark cmd = new Mark(Ask.NOTIFY, Ask.SHUTDOWN);
		Packet request = new Packet(remote, cmd);
		
		boolean success = false;
		ShutdownSpider spider = new ShutdownSpider();
		// 设置数据包接收超时时间
		spider.setReceiveTimeout(10000);

		try {
			//1. 绑定本地IP地址
			spider.bind(local);
			//2. 发送并且接收应答
			Packet resp = spider.swap(request);
			if (resp != null) {
				success = (resp.getMark().getAnswer() == Answer.OKAY);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Throwable e) {
			e.printStackTrace();
		}
		// 直接关闭
		spider.close(true);

		System.out.printf("%s send to %s %s\n", local.getAddress(), remote, (success ? "success" : "failed"));

		// 打印出错信息
		if (!success) {
			Logger.gushing();
		}
		return success;
	}

	/**
	 * 启动
	 * @param args
	 */
	public static void main(String[] args) {
		ShutdownLauncher shutdown = new ShutdownLauncher();
		if (args.length == 1) {
			String filename = args[0];
			shutdown.launch(filename);
		} else {
			System.out.println("invalid!");
		}
	}

}