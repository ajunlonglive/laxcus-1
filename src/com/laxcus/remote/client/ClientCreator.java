/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client;

import java.io.*;
import java.lang.reflect.*;

import com.laxcus.echo.*;
import com.laxcus.fixp.client.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.util.net.*;

/**
 * RPC客户端生成器 <br>
 * 
 * 生成多种类型网络通信客户端。
 * 
 * @author scott.liang
 * @version 1.0 07/06/2009
 * @since laxcus 1.0
 */
public class ClientCreator {

	/**
	 * 构造RPC客户端生成器
	 */
	public ClientCreator() {
		super();
	}

	/**
	 * 检查RPC客户端是基于RemoteClient的派生。
	 * @param clazz 类定义
	 * @return 如果是返回“真”，否则“假"。
	 */
	private static boolean isRemoteClient(Class<?> clazz) {
		if (clazz == RemoteClient.class) {
			return true;
		}
		Class<?> parent = clazz.getSuperclass();
		if (parent == null) {
			return false;
		}
		return ClientCreator.isRemoteClient(parent);
	}

	/**
	 * 根据类定义，建立一个基于RemoteClient的实例，然后连接到FIXP服务器。
	 * @param <T> RemoteClient子类
	 * @param clazz 类定义
	 * @param endpoint 服务器套接字监听地址(FIXP TCP SERVER / FIXP UDP SERVER)
	 * @param receiveTimeout 套接字接收超时，单位：毫秒
	 * @return 如果成功返回类实例，否则是null。
	 */
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> clazz, SocketHost endpoint, int receiveTimeout) {
		// 类必须继承自RemoteClient
		if (!ClientCreator.isRemoteClient(clazz)) {
			return null;
		}

		// 生成实例
		T client = null;
		try {
			Constructor<?> cons = clazz
					.getConstructor(new Class<?>[] { SocketHost.class });
			client = (T) cons.newInstance(new Object[] { endpoint });
			// 检查SOCKET接收超时，这项必须有，否则会形成持续等待的结果
			if (receiveTimeout < 1000) {
				receiveTimeout = 5000;
			}
			// 设置接收超时
			((RemoteClient) client).setReceiveTimeout(receiveTimeout);
		} catch (SecurityException e) {
			Logger.error(e);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
		} catch (InstantiationException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		}
		// 失败返回空值
		if (client == null) {
			return null;
		}

		// 连接目标主机，最大3次
		for (int index = 0; index < 3; index++) {
			// 大于0是重连
			if (index > 0) {
				((RemoteClient) client).delay(1000);
				// 再次设置超时
				((RemoteClient) client).setReceiveTimeout(receiveTimeout);
				// 重新连接时，因为之前的“destroy”方法会释放站点地址，所以需要再次设置目标站点地址，
				((RemoteClient) client).setRemote(endpoint); 
			}
			// 连接目标站点
			try {
				((RemoteClient) client).reconnect();
				return client;
			} catch (IOException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
			// 不成功，销毁它
			((RemoteClient) client).destroy();
		}

		return null;
	}

	/**
	 * 根据参数，建立一个基于RemoteClient的RPC客户端。提供默认的接收超时：60秒。
	 * @param <T> RemoteClient子类
	 * @param clazz RemoteClient子类定义
	 * @param endpoint 服务器套接字监听地址(FIXP TCP SERVER / FIXP UDP SERVER)
	 * @return 如果成功返回类实例，否则是null。
	 */
	public static <T> T create(Class<T> clazz, SocketHost endpoint) {
		int ms = SocketTransfer.getDefaultReceiveTimeout();
		
		return ClientCreator.create(clazz, endpoint, ms);
	}

	/**
	 * 根据类定义，建立一个基于RemoteClient的实例，和默认采用KEEP UDP模式连接到目标地址。
	 * @param <T> RemoteClient子类。
	 * @param clazz RemoteClient子类定义。
	 * @param hub 服务器站点地址。
	 * @return 成功返回类实例，否则是空指针。
	 */
	public static <T> T create(Class<T> clazz, SiteHost hub) {
		// 根据配置中对通信模式的定义，选择TCP/UDP中的任意一种
		boolean stream = EchoTransfer.isStreamTransfer();
		SocketHost remote = hub.choice(stream);
		// 分配和连接服务器
		return ClientCreator.create(clazz, remote);
	}

	/**
	 * 根据类定义，建立一个基于RemoteClient的实例，和默认采用KEEP UDP模式连接到目标地址。
	 * @param <T> RemoteClient子类
	 * @param clazz RemoteClient子类定义
	 * @param hub 服务器站点地址
	 * @return 成功返回类实例，否则是空指针。
	 */
	public static <T> T create(Class<T> clazz, Node hub) {
		// 根据配置中对通信模式的定义，选择TCP/UDP中的任意一种
		boolean stream = EchoTransfer.isStreamTransfer();
		SocketHost endpoint = hub.choice(stream);
		// 构造对象，建立连接，返回实例
		return ClientCreator.create(clazz, endpoint);
	}

	/**
	 * 根据请求端的回显地址和数据传输模式，建立一个异步应答客户端
	 * @param hub 目标站点
	 * @param stream 数据流(TCP)模式
	 * @return 成功返回类实例，失败是空指针
	 */
	public static EchoClient createEchoClient(Cabin hub, boolean stream) {
		EchoClient client = null;
		
		// 如果是内网，采用遂道穿透
		if (hub.isPock()) {
			int ms = SocketTransfer.getDefaultReceiveTimeout();
			SocketHost endpoint = hub.choice(false);
			client = new EchoCustomer(endpoint, ms);
		} else {
			SocketHost endpoint = hub.choice(stream);
			// 分配和连接服务器
			client = ClientCreator.create(EchoTraveller.class, endpoint);
		}
		
		// 设置回显标识
		if (client != null) {
			client.setEchoFlag(hub.getFlag());
		}
		return client;
	}

	/**
	 * 根据请求端的回显地址，建立一个基于数据包的异步应答客户端
	 * @param hub 目标站点
	 * @return 成功返回类实例，否则返回空指针
	 */
	public static EchoClient createEchoClient(Cabin hub) {
		// 根据配置中对通信模式的定义，选择TCP/UDP中的任意一种
		boolean stream = EchoTransfer.isStreamTransfer();
		return ClientCreator.createEchoClient(hub, stream);
	}
	
	/**
	 * 指定管理站点地址，建立与它的连接
	 * @param endpoint 管理站点地址
	 * @return 成功返回HubClient句柄，否则是空指针
	 */
	public static HubClient createHubClient(SocketHost endpoint) {
		return ClientCreator.create(HubClient.class, endpoint);
	}

	/**
	 * 指定管理站点地址，选择TCP/UDP模式，建立与它的连接。
	 * @param hub 管理站点地址
	 * @param stream 流模式（TCP模式）
	 * @return 成功返回HubClient句柄，否则是空指针
	 */
	public static HubClient createHubClient(SiteHost hub, boolean stream) {
		SocketHost endpoint = hub.choice(stream);
		return createHubClient(endpoint);
	}
	
	/**
	 * 指定管理站点地址，建立与它的连接。默认采用TCP连接模式
	 * @param hub 管理站点地址
	 * @return 成功返回HubClient句柄，否则是空指针
	 */
	public static HubClient createHubClient(SiteHost hub) {
		return createHubClient(hub, true);
	}

	/**
	 * 指定管理站点地址，建立与它的连接。默认采用TCP连接模式
	 * @param hub 管理站点地址
	 * @return 成功返回HubClient句柄，否则是空指针
	 */
	public static HubClient createHubClient(Node hub) {
		SocketHost endpoint = hub.choice(true);
		return createHubClient(endpoint);
	}

	/**
	 * 指定FRONT管理站点地址（ENTRANCE/GATE），建立与它的连接
	 * @param endpoint FRONT管理站点地址，包括ENTRANCE/GATE两种
	 * @return 成功返回FrontClient句柄，失败返回空指针
	 */
	public static FrontClient createFrontClient(SocketHost endpoint) {
		return ClientCreator.create(FrontClient.class, endpoint);
	}

	/**
	 * 指定FRONT管理站点地址，建立与它的连接。连接模式用户指定
	 * @param hub FRONT管理站点地址
	 * @param stream TCP流模式或者否
	 * @return 成功返回FrontClient句柄，否则是空指针
	 */
	public static FrontClient createFrontClient(SiteHost hub, boolean stream) {
		SocketHost endpoint = hub.choice(stream);
		return createFrontClient(endpoint);
	}

	/**
	 * 指定FRONT管理站点地址，建立与它的连接。连接模式用户指定
	 * @param hub FRONT管理站点地址
	 * @param stream TCP流模式或者否
	 * @return 成功返回FrontClient句柄，否则是空指针
	 */
	public static FrontClient createFrontClient(Node hub, boolean stream) {
		SocketHost endpoint = hub.choice(stream);
		return createFrontClient(endpoint);
	}
}