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

import com.laxcus.fixp.*;
import com.laxcus.fixp.client.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.log.client.*;
import com.laxcus.util.net.*;

/**
 * 远程关闭调用器 <br>
 * 
 * 向一个指定的运行站点发起远程关闭通知，然后等待返回结果。
 * 
 * @author scott.liang
 * @version 1.0 7/11/2011
 * @since laxcus 1.0
 */
public class ShutdownSpider {

	/** FIXP UDP客户端 **/
	private FixpPacketClient client;

	/**
	 * 构造默认的远程关闭调用器
	 */
	public ShutdownSpider() {
		super();
		client = new FixpPacketClient();
	}
	
	/**
	 * 设置以毫秒为单位的接收超时
	 * @param ms 接收超时时间
	 * @return 返回为毫秒为单位的接收超时时间
	 */
	public int setReceiveTimeout(int ms) {
		return client.setReceiveTimeout(ms);
	}
	
	/**
	 * 判断是绑定
	 * @return 返回真或者假
	 */
	public boolean isBound() {
		return client.isBound();
	}
	
	/**
	 * 绑定本地址
	 * @param local 本地地址
	 * @return 绑定成功返回真，否则假
	 * @throws IOException
	 */
	public boolean bind(SocketAddress local) throws IOException {
		return client.bind(local);
	}

	/**
	 * 关闭SOCKET，如果是加密通信并且已经绑定时，柔性关闭，否则直接关闭SOCKET
	 */
	public void close(boolean direct) {
		if (direct) {
			client.destroy();
		} else if (client.isBound() && client.isSecured()) {
			client.close();
		} else {
			client.destroy();
		}
	}
	
	/**
	 * 连续发送请求，不等待回答
	 * @param packets 数据包数组
	 * @param sends 发送次数
	 * @return 返回发送成功次数
	 */
	public int send(Packet[] packets, int sends) throws IOException {
		// 检查
		for (int index = 0; index < packets.length; index++) {
			// 必须是请求包
			if (!packets[index].getMark().isAsk()) {
				throw new IllegalArgumentException("illegal command");
			}
			// 检查目标节点地址
			if (!packets[index].getRemote().isValid()) {
				throw new IllegalArgumentException("illegal target site");
			}
		}

		int count = 0;

		// 如果没有绑定时，随机绑定一个本地地址
		if (!client.isBound()) {
			client.bind();
		}
		// 询问服务器是否要求加密
		for (int index = 0; index < packets.length; index++) {
			SocketHost remote = packets[index].getRemote();
			// 如果没有加密先检查
			if (!client.isSecured(remote)) {
				int secured = client.askSecure(remote);
				// 如果要求加密通信，初始化密文
				if (SecureType.isCipher(secured)) {
					boolean success = client.createSecure(remote);
					if (!success) continue;
				}
			}
			// 连续发送数据包，不等待回答
			for (int n = 0; n < sends; n++) {
				client.send(packets[index]);
			}
			count++;
		}

		return count;
	}
	
	/**
	 * 连续发送请求，不等待回答
	 * @param packet 数据包
	 * @param sends 发送次数
	 * @return 发送次数
	 */
	public boolean send(Packet packet, int sends) throws IOException {
		return send(new Packet[] { packet }, sends) > 0;
	}

	/**
	 * 一次网络通信，返回应答包
	 * @param request 请求包
	 * @return 返回应答包
	 */
	public Packet swap(Packet request) {
		// 必须是请求包
		if (!request.getMark().isAsk()) {
			throw new IllegalArgumentException("illegal command");
		}
		// 检查目标地址
		SocketHost remote = request.getRemote();
		if (!remote.isValid()) {
			throw new IllegalArgumentException("illegal target site");
		}

		try {
			// 1. 如果没有绑定，从本地随机选择一个地址进行绑定
			if (!client.isBound()) {
				client.bind();
			}
			// 2. 启动包通信，安全检查在内部执行
			Packet resp = client.swap(request);
			return resp;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		return null;
	}

}