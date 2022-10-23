/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch.hub;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

import org.w3c.dom.*;

import com.laxcus.fixp.client.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.launch.*;
import com.laxcus.launch.licence.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.hit.*;
import com.laxcus.site.*;
import com.laxcus.site.Node;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.xml.*;

/**
 * LAXCUS集群中心站点启动器。<br><br>
 * 
 * 设计说明：<br>
 * 1. 中心站点是一个集群的核心，包括TOP/HOME两种节点。<br>
 * 2. 按照工作性质，分为管理站点和监视站点。<br>
 * 3. 管理站点只能一个，监视站点数据不限。<br>
 * 4. 管理站点保存本集群所有站点注册时提供的元数据，并且依据这些元数据，执行对本集群内所有站点（包括监视站点）的组织、协调、控制工作。<br>
 * 5. 监视站点要注册到管理节点，并监视管理站点的运行，复制管理站点的配置数据（从管理站点下载），实际是对管理站点的“镜像”。<br>
 * 6. 集群启动时，首先启动的中心站点将成为管理站点，其它成为监视站点。<br>
 * 7. 管理站点发生故障后，监视站点通过“选举”，从中选出一个监视站点，成为新的管理站点，其它监视站点重新注册到这个新管理站点下面。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 4/12/2010
 * @since laxcus 1.0
 */
public abstract class HubLauncher extends SiteLauncher {

	/** 管理节点（主运行站点）。如此这个参数不是空值，它是一个“监视站点”  **/
	private Node manager;

	/** hit socket 连接时间 **/
	private int hitTimeout;

	/** hit socket 连接模式 **/
	private byte hitMode;

	/** 监视站点集合 **/
	private NodeSet monitors = new NodeSet(); 

	/** 注册到TOP/HOME/BANK管理站点下面的子站点，这些站点由“管理站点”推送过来 **/
	private NodeSet slaves = new NodeSet();

	/**
	 * 初始化管理站点启动器
	 */
	protected HubLauncher() {
		super();
		// 默认10秒
		hitTimeout = 10000; 
		// 默认TCP模式
		hitMode = SocketTag.TCP;
	}

	/**
	 * 返回连接时间
	 * @return 毫秒时间
	 */
	protected int getHitTimeout() {
		return hitTimeout;
	}

	/**
	 * 增加一个子级站点。
	 * @param e 子级站点。
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean push(Node e) {
		Laxkit.nullabled(e);

		return slaves.push(e);
	}

	/**
	 * 删除一个子级站点地址。
	 * @param e 子级站点地址
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean drop(Node e) {
		Laxkit.nullabled(e);

		return slaves.drop(e);
	}

	/**
	 * 输出全部下属站点地址
	 * @return 节点地址列表
	 */
	public List<Node> getSlaveSites() {
		return slaves.show();
	}

	/**
	 * 输出全部监视站点地址
	 * @return 节点地址列表
	 */
	public List<Node> getMonitorSites() {
		return monitors.show();
	}

	/**
	 * 设置“管理站点”地址。如果节点参数是空值，当前站点是”管理站点“。
	 * @param e Node实例
	 */
	protected void setManager(Node e) {
		manager = e;

		// 定义当前节点是管理节点或者监视器节点
		if (isManager()) {
			getListener().setRank(RankTag.MANAGER);
		} else if (isMonitor()) {
			getListener().setRank(RankTag.MONITOR);
		}

		Logger.info(this, "setManager", "manager site is %s", manager);
	}

	/**
	 * 返回“管理站点”地址。
	 * @return 如果当前站点是“管理站点”，返回空值；否则返回一个站点地址。
	 */
	public Node getManager() {
		return manager;
	}

	/**
	 * 判断当前站点是“管理站点”。
	 * 判断管理站点成立的条件是“manager”是空指针，即没有指向。
	 * @return 返回“是”或者“否”。
	 */
	public boolean isManager() {
		return manager == null;
	}

	/**
	 * 判断当前站点是“监视站点”。如果不是“管理站点”，即是“监视站点”。
	 * @return 返回“是”或者“否”。
	 */
	public boolean isMonitor() {
		return !isManager();
	}

	/**
	 * 判断传入站点在“监视站点”集合里。
	 * @param node 站点地址
	 * @return 返回“是”或者“否”。
	 */
	public boolean hasMonitor(Node node) {
		return monitors.exists(node);
	}

	/**
	 * 连接HitVisit服务器 
	 * @param hub 目标地址
	 * @return 返回连接句柄，或者空指针
	 */
	protected HitClient fetchHitClient(Node hub, int receiveTimeout) {
		SocketHost endpoint = hub.getStreamHost();
		HitClient client = ClientCreator.create(HitClient.class, endpoint);
		if (client == null) {
			Logger.error(this, "fetchHitClient", "cannot find %s", hub);
		}
		return client;
	}

	/**
	 * 连接HitVisit服务器 
	 * @param hub 目标地址
	 * @return 返回连接句柄，或者空指针
	 */
	protected HitClient fetchHitClient(Node hub) {
		// 判断连接模式
		boolean stream = SocketTag.isStream(hitMode);
		// 生成地址！
		SocketHost endpoint = hub.choice(stream);
		// 产生客户端句柄！
		HitClient client = ClientCreator.create(HitClient.class, endpoint, hitTimeout);
		if (client == null) {
			Logger.error(this, "fetchHitClient", "cannot find %s", endpoint);
		}
		return client;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.launch.SiteLauncher#cancel(com.laxcus.site.Node)
//	 */
//	@Override
//	protected boolean cancel(Node hub) {
//		return super.cancel(hub);
//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#dropSecure()
	 */
	@Override
	protected boolean dropSecure() {
		return super.dropSecure();
	}

	/**
	 * 启用RING操作，判断TCP通信
	 * @param remote 目标地址
	 * @return 成功返回真，否则假
	 */
	private boolean doStream(SocketHost remote) {
		// 判断是TCP模式
		if (!remote.isStream()) {
			throw new IllegalValueException("illegal socket type! %s", remote);
		}
		FixpStreamClient client = new FixpStreamClient();
		// 超时时间
		client.setConnectTimeout(hitTimeout);
		client.setReceiveTimeout(hitTimeout);
		// 尝试tcp socket连接，并且调用“test”方法
		boolean success = false;
		try {
			client.connect(remote);
			SocketHost host = client.test();
			success = (host != null); // 成功或者失败
			// 关闭连接
			client.close();
		} catch (IOException e) {
			Logger.error(e);
		}

//		// 关闭连接
//		client.close(success);
		
		// 关闭
		client.destroy();

		Logger.note(this, "doStream", success, "ring check %s", remote);
		return success;
	}

	/**
	 * 启动RING操作，采用UDP通信！
	 * 调用FIXP.TEST命令
	 * 
	 * @param node 目标地址
	 * @return 成功返回真，否则假
	 */
	private boolean doPacket(SocketHost remote) {
		// 判断是UDP模式
		if (!remote.isPacket()) {
			throw new IllegalValueException("illegal socket type! %s", remote);
		}
		// 采用UDP连接
		FixpPacketClient client = new FixpPacketClient();
		// 超时时间
		client.setConnectTimeout(hitTimeout);
		client.setReceiveTimeout(hitTimeout);
		// 尝试udp socket连接，并且调用“test”方法
		// 由服务器定位的本机socket方法
		SocketHost host = null;
		try {
			boolean success = client.bind();
			if (success) {
				// 询问服务器安全状态
				int type = client.askSecure(remote);
				// 如果要求加密时
				boolean secure = SecureType.isCipher(type);
				// 发送测试命令
				host = client.test(remote, secure);
			}
			// 正常关闭socket
			client.close();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 销毁，如果以上执行，这个方法不执行！
		client.destroy();
		
		// 判断成功!
		boolean success = (host != null);

		Logger.note(this, "doPacket", success, "ring check %s", remote);
		return success;
	}

	/**
	 * 执行RING操作，同时测试TCP/UDP两种模式。<br>
	 * 使用FIXP.TEST命令测试 <br>
	 * 
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean ring(Node node) {
		// 先测试UDP模式，再测试TCP模式
		boolean success = doPacket(node.choice(false));
		if (success) {
			success = doStream(node.choice(true));
		}
		return success;
	}

	/**
	 * 从监视站点中发现管理站点。<br>
	 * 处理方案： 逐一访问“local.xml”配置文件中指定的监视站点，其中任何一个站点的“HitVisit.isMaster”方法返回“真”，它即是管理站点。
	 * @return 返回“管理站点”地址，否则是空指针，当前站点即为管理站点。
	 */
	public Node consult() {
		for (Node node : monitors.list()) {
			// 判断节点有效！
			boolean success = ring(node);
			if (!success) {
				Logger.error(this, "consult", "cannot be find %s", node);
				continue;
			}

			// 返回关联的监视站点
			HitClient client = fetchHitClient(node);
			if (client == null) {
				continue;
			}

//			boolean exit = false;
			
			// 判断被检查的站点已经运行
			boolean running = false;
			try {
				running = client.isManager();
				// 柔性关闭套接字，或者否！
				client.close();
				
//				// 上面工作正常，关闭SOCKET之前发出“exit”指令，柔性结束服务器/客户机通信
//				// 柔性结束让服务器及时释放客户端的密钥，而不是等到超时后再释放！
//				exit = true;
			} catch (VisitException e) {
				Logger.error(e);
			}
//			// 柔性关闭套接字，或者否！
//			client.close();

			// 强制销毁资源
			client.destroy(); 

			// 这个站点已经运行，当前站点即为监视站点
			if (running) {
				Logger.info(this, "consult", "%s is run site", node);
				return node;
			}
		}
		// 检测完成，以上站点的“管理站点”状态不成立，当前站点成为管理站点
		return null;
	}

	/**
	 * 加载监视站点。<br>
	 * 监视站点通常有多个。当管理站点故障时，通过协商从中选择一个代替故障站点，成为新的管理站点。
	 * @param document XML文档
	 * @return 成功返回真，否则假
	 */
	protected boolean loadMonitor(Document document) {
		NodeList list = document.getElementsByTagName(SiteMark.MARK_MONITOR_SITES);

		Logger.debug(this, "loadMonitor", "%s size is %d", SiteMark.MARK_MONITOR_SITES, list.getLength());

		// 允许没有监视站点
		if (list.getLength() == 0) {
			return true;
		}
		// 监视站点配置只能有一个，否则是错误！
		if (list.getLength() != 1) {
			return false;
		}

		// 保存监视站点
		Element root = (Element) list.item(0);

		// socket 连接超时
		String value = root.getAttribute(SiteMark.MONITOR_SOCKET_TIMEOUT);
		hitTimeout = (int) ConfigParser.splitTime(value, hitTimeout);
		// socket连接模式
		value = root.getAttribute(SiteMark.MONITOR_SOCKET_MODE);
		hitMode = ConfigParser.splitSocketFamily(value, SocketTag.TCP);

		Logger.debug(this, "loadMonitor", "communicate timeout: %d ms, communicate mode: %s", hitTimeout, SocketTag.translate(hitMode));

		list = root.getElementsByTagName(SiteMark.MONITOR_NODE);
		for (int i = 0; i < list.getLength(); i++) {
			Element sub = (Element) list.item(i);
			String content = sub.getTextContent();

			try {
				com.laxcus.site.Node node = new com.laxcus.site.Node(content);
				Logger.debug(this, "loadMonitor", "monitor site is \"%s\"", node);

				// 必须同类型站点
				if (node.getFamily() != getFamily()) {
					Logger.error(this, "loadMonitor", "cannot be match: %d,%d", node.getFamily(), getFamily());
					return false;
				}
				monitors.add(node);
			} catch (UnknownHostException exp) {
				Logger.error(exp);
				return false;
			}
		}

		Logger.debug(this, "loadMonitor", "monitor site size:%d", monitors.size());

		return true;
	}

	/**
	 * 返回有标记为“daemon-time”的XML成员
	 * @param document XML文档
	 * @param tag 标签
	 * @return 文本数据
	 */
	private String getDaemonValue(Document document, String tag) {
		Element element = (Element) document.getElementsByTagName(HubSiteMark.DAEMON_TIME).item(0);
		return XMLocal.getValue(element, tag);
	}

	/**
	 * 设置管理池的延时时间（站点检查触发时间间隔）。
	 * @param pools 管理池数组
	 * @param document XML文档
	 */
	protected void setSleepTimeout(HubPool[] pools, Document document) {
		String input = getDaemonValue(document, HubSiteMark.SLEEP_TIME);
		long ms = ConfigParser.splitTime(input, 5000); // 默认5秒
		for (int i = 0; i < pools.length; i++) {
			pools[i].setSleepTimeMillis(ms);
		}
		Logger.debug(this, "setSleepTimeout", "sleep timeout %d", ms);
	}

	/**
	 * 设置管理池属下的注册站点超时时间(所有站点的超时时间是一致的)
	 * @param pools 管理池数组
	 * @param document XML文档
	 */
	protected void setActiveTimeout(HubPool[] pools, Document document) {
		String input = getDaemonValue(document, HubSiteMark.SITE_ACTIVE_TIMEOUT);
		long ms = ConfigParser.splitTime(input, 20000); // 默认20秒
		for (int i = 0; i < pools.length; i++) {
			pools[i].setActiveTimeMillis(ms);
		}

		Logger.debug(this, "setActiveTimeout", "site active timeout %d", ms);
	}

	/**
	 * 设置管理池属下的注册站点超时删除时间（一般是站点超时时间的3倍）
	 * @param pools 管理池数组
	 * @param document XML文档
	 */
	protected void setDeleteTimeout(HubPool[] pools, Document document) {
		String input = getDaemonValue(document, HubSiteMark.SITE_DELETE_TIMEOUT);
		long ms = ConfigParser.splitTime(input, 60000); // 默认60秒
		for (int i = 0; i < pools.length; i++) {
			pools[i].setDeleteTimeMillis(ms);
		}

		Logger.debug(this, "setDeleteTimeout", "site delete timeout %d", ms);
	}

	/**
	 * 返回许可证文件“conf/licence”文件中某个管理节点下的“sites-limit”的XML成员
	 * @param file 磁盘文件
	 * @param rootTag 根标签
	 * @return 返回Element实例，失败返回空指针！
	 */
	protected Element fatchLimitElement(File file, String rootTag) {
		// 解析XML根成员
		org.w3c.dom.Element element = LicenceRegister.readXMLElement(file, "subsites-limit-configure");
		if (element == null) {
			Logger.error(this, "fatchLicenceElement", "cannot resolve \"subsites-limit-configure\"!");
			return null;
		}

		org.w3c.dom.NodeList nodes = element.getChildNodes();
		// 顺序逐个按名字查找
		int len = nodes.getLength();
		for (int i = 0; i < len; i++) {
			org.w3c.dom.Node node = nodes.item(i);
			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				String name = node.getNodeName();
				if (Laxkit.compareTo(name, rootTag) == 0) {
					return (org.w3c.dom.Element) node;
				}
			}
		}

		return null;
	}

	/**
	 * 从配置档案中取出参数，设置管理池注册节点限制数
	 * @param limit
	 * @param tag
	 * @param pool
	 * @return 成功返回真，否则假
	 */
	protected boolean setMaxMember(Element limit, final String tag, HubPool pool) {
		String input = XMLocal.getValue(limit, tag);
		int value = ConfigParser.splitInteger(input, 0);
		if (value < 1) {
			return false;
		}
		pool.setMaxMembers(value);
		return true;
	}

}