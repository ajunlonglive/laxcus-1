/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import java.text.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.command.rule.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.fixp.client.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.front.*;
import com.laxcus.front.pool.*;
import com.laxcus.front.util.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;

/**
 * FRONT异步调用器。<br><br>
 * 
 * FRONT异步调用器被终端、控制台、驱动程序共用。<br><br>
 * 
 * FRONT站点的所有命令都以异步操作的形式进行。
 * 
 * @author scott.liang
 * @version 1.5 7/19/2015
 * @since laxcus 1.0
 */
public abstract class FrontInvoker extends EchoInvoker {

	/** 拒绝管理员操作 **/
	private boolean refuseAdministrator;

	/**
	 * 构造默认的FRONT调用器
	 */
	protected FrontInvoker() {
		super();
		setRefuseAdministrator(false);
	}

	/**
	 * 构造FRONT站点调用器，指定命令
	 * @param cmd 异步命令
	 */
	protected FrontInvoker(Command cmd) {
		this();
		// 设置命令发布者
		cmd.setIssuer(getUsername());
		// 流式处理或者否
		cmd.setMemory(isStream());
		// 命令超时
		cmd.setTimeout(getCommandTimeout());
		// 设置命令
		setCommand(cmd);
	}

	/**
	 * 设置拒绝管理员操作调用器
	 * @param b 真或者假
	 */
	public void setRefuseAdministrator(boolean b) {
		refuseAdministrator = b;
	}

	/**
	 * 判断是拒绝管理员操作
	 * @return 返回真或者假
	 */
	public boolean isRefuseAdministrator() {
		return refuseAdministrator;
	}
	
	/**
	 * 返回FRONT站点启动器
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public FrontLauncher getLauncher() {
		return (FrontLauncher) super.getLauncher();
	}
	
	/**
	 * 返回两个网关（entrance/gate）先后定位确认的front出口地址
	 * @return 返回SocketHost实例，没有定义是空指针
	 */
	public SocketHost getDoorHost() {
		return getLauncher().getDoorHost();
	}

	/**
	 * 判断当前front节点出口是位于公网
	 * @return 返回真或者假
	 */
	public boolean isWideAddress() {
		return getLauncher().isWideAddress();
	}

	/**
	 * 判断是驱动程序站点
	 * @return 返回真或者假
	 */
	public boolean isDriver() {
		FrontLauncher launcher = getLauncher();
		return launcher.isDriver();
	}

	/**
	 * 判断是字符控制台站点
	 * @return 返回真或者假
	 */
	public boolean isConsole() {
		FrontLauncher launcher = getLauncher();
		return launcher.isConsole();
	}

	/**
	 * 判断是图形终端站点
	 * @return 返回真或者假
	 */
	public boolean isTerminal() {
		FrontLauncher launcher = getLauncher();
		return launcher.isTerminal();
	}

	/**
	 * 判断是专门用于边缘计算的服务端节点（采用后台运行模式）
	 * @return 返回真或者假
	 */
	public boolean isEdge() {
		FrontLauncher launcher = getLauncher();
		return launcher.isEdge();
	}

	/**
	 * 判断是桌面
	 * @return
	 */
	public boolean isDesktop() {
		FrontLauncher launcher = getLauncher();
		return launcher.isDesktop();
	}

	/**
	 * 判断是独立的应用程序
	 * @return
	 */
	public boolean isApplication() {
		FrontLauncher launcher = getLauncher();
		return launcher.isApplication();
	}
	
	/**
	 * 返回资源管理池
	 * @return StaffOnFrontPool实例
	 */
	public StaffOnFrontPool getStaffPool() {
		return getLauncher().getStaffPool();
	}

	/**
	 * 判断是被授权数据表
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean isPassiveTable(Space space) {
		StaffOnFrontPool instance = getStaffPool();
		return instance.isPassiveTable(space);
	}

	/**
	 * 根据数据表名，查找被授权表的授权人
	 * @param space 数据表名
	 * @return Siger签名
	 */
	public Siger findAuthorizer(Space space) {
		StaffOnFrontPool instance = getStaffPool();
		return instance.findAuthorizer(space);
	}

	/**
	 * 返回FRONT调用器管理池
	 * @return FrontInvokerPool实例
	 */
	public FrontInvokerPool getInvokerPool() {
		return (FrontInvokerPool) super.getInvokerPool();
	}

	/**
	 * 返回FRONT命令管理池
	 * @return FrontCommandPool实例
	 */
	public FrontCommandPool getCommandPool() {
		return (FrontCommandPool) super.getCommandPool();
	}
	
//	/**
//	 * 判断是离线状态
//	 * @return 返回真或者假
//	 */
//	public boolean isOffline() {
//		FrontLauncher launcher = getLauncher();
//		return launcher.isOffline();
//	}

	/**
	 * 判断是管理员
	 * @return 返回真或者假
	 */
	public boolean isAdministrator() {
		FrontLauncher launcher = getLauncher();
		return launcher.isAdministrator();
	}

	/**
	 * 判断不是系统管理员，但是用户拥有管理员操作权限
	 * @return 返回真或者假
	 */
	public boolean canDBA() {
		return (!isAdministrator() && getStaffPool().canDBA());
	}

	/**
	 * 判断是普通注册用户
	 * @return 返回真或者假
	 */
	public boolean isUser() {
		FrontLauncher launcher = getLauncher();
		return launcher.isUser();
	}

	/**
	 * 返回账号等级
	 * @return 账号等级
	 */
	public int getGrade() {
		FrontLauncher launcher = getLauncher();
		return launcher.getGrade();
	}

	/**
	 * 返回当前账号的注册用户名
	 * @return SHA256散列码的用户签名
	 */
	public Siger getUsername() {
		FrontLauncher launcher = getLauncher();
		return launcher.getUsername();
	}
	
	/**
	 * 判断签名是自己
	 * @param other
	 * @return
	 */
	protected boolean isSelfly(Siger other) {
		Siger siger = this.getUsername();
		return (siger != null && Laxkit.compareTo(siger, other) == 0);
	}

	/**
	 * 判断系统默认命令是流处理模式（内存计算模式）
	 * @return 返回真或者假
	 */
	public boolean isStream() {
		FrontLauncher launcher = getLauncher();
		return launcher.isMemory();
	}

	/**
	 * 返回用户定义的命令超时
	 * @return 超时时间，-1是无限制。
	 */
	public long getCommandTimeout() {
		FrontLauncher launcher = getLauncher();
		return launcher.getCommandTimeout();
	}

	/**
	 * 解析LAXCUS大数据操作系统时间参数
	 * @param time 采用LAXCUS系统定义的“年/月/日  时.分.秒  毫秒”格式
	 * @return 返回本地时间系统解析的字符串格式
	 */
	protected String splitLaxcusTime(long time) {
		// 小于1是无定义
		if (time < 1) {
			return "";
		}
		Date date = com.laxcus.util.datetime.SimpleTimestamp.format(time);
		DateFormat dt = DateFormat.getDateTimeInstance(); // 系统默认的日期/时间格式
		return dt.format(date);
	}

	/**
	 * 把事务规则进行分类。<br>
	 * 对每个事务规则，找到关联的GATE站点，生成命令单元。<br>
	 * 这个操作主要是发现共享表，找到对应的授权人签名，进而找到GATE站点。<br><br>
	 * 
	 * @param items 事务规则列表
	 * @return 返回RuleToken列表
	 */
	protected RuleTokenSheet classify(Collection<RuleItem> items) {
		int index = 0;
		// 拿到账号资源
		Map<Siger, AttachRule> rules = new TreeMap<Siger, AttachRule>();
		for (RuleItem item : items) {
			// 如果是表级别，判断是共享表
			if (item.getClass() == TableRuleItem.class) {
				Space space = ((TableRuleItem) item).getSpace();
				// 判断是一个分享表
				if (isPassiveTable(space)) {
					Siger authorizer = findAuthorizer(space); // 找到分享表的授权人签名
					// 如果是空值，是错误，返回空指针
					if (authorizer == null) {
						Logger.error(this, "classify", "cannot be find authorizer by '%s'", space);
						return null;
					}
					AttachRule cmd = rules.get(authorizer);
					if (cmd == null) {
						ProcessRuleTag tag = new ProcessRuleTag(getLocal(), getInvokerId(), index++); // 事务处理标识
						cmd = new AttachRule(tag);
						cmd.setIssuer(authorizer); // 以授权人身份发送命令
						rules.put(authorizer, cmd);
					}
					cmd.add(item);
					continue;
				}
			}
			// 是自己的资源，
			Siger issuer = getUsername();
			AttachRule cmd = rules.get(issuer);
			if (cmd == null) {
				ProcessRuleTag tag = new ProcessRuleTag(getLocal(), getInvokerId(), index++); // 事务处理标识
				cmd = new AttachRule(tag);
				cmd.setIssuer(issuer); // 以执行人身份发送命令
				rules.put(issuer, cmd);
			}
			cmd.add(item);
		}

		// 根据签名，找到对应的GATE站点，生成命令单元
		//		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		//		ArrayList<RuleToken> array = new ArrayList<RuleToken>();

		RuleTokenSheet sheet = new RuleTokenSheet();

		Iterator<Map.Entry<Siger, AttachRule>> iterator = rules.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Siger, AttachRule> entry = iterator.next();
			Siger signer = entry.getKey();
			// 如果是用户自己，取它本身的注册GATE站点地址；否则取关联站点地址
			if (signer.equals(getUsername())) {
				Node hub = getHub();
				sheet.add(new RuleToken(hub, entry.getValue()));
			} else {
				// 去管理池查询关联GATE站点
				Node hub = AuthroizerGateOnFrontPool.getInstance().findSite(signer);
				// 如果没有找到，返回空指针
				if (hub == null) {
					Logger.error(this, "classify", "cannot be find hub-site by '%s'", signer);
					return null;
				}
				// 记录这个节点地址，把关联资源保存
				sheet.add(new RuleToken(hub, entry.getValue()));
			}
		}

		// 返回结果
		return sheet;
	}
	
//	/**
//	 * 检测授权的GATE节点
//	 * @param node 节点
//	 */
//	private void checkAuthroizerGatePock(Node node) {
//		final SocketHost hub = node.getPacketHost();
//		
//		// 与GATE节点的REPLY DISPATCHER 服务器建立映像
//		FrontClient client = ClientCreator.createFrontClient(hub);
//		if (client == null) {
//			Logger.error(this, "checkAuthroizerGatePock", "cannot be git HubClient!");
//			return;
//		}
//		// 找到ReplyDispatcher主机地址
//		SocketHost dispatcher = null;
//		try {
//			dispatcher = client.getHubDispatcher(isWideAddress());
//			client.close();
//		} catch (VisitException e) {
//			Logger.error(e);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//		client.destroy();
//		
//		// 没找到
//		if (dispatcher == null) {
//			Logger.error(this, "checkAuthroizerGatePock", "cannot be find ReplyDispatcher host!");
//			return;
//		}
//		
//		ReplyHelper replyHelper = getLauncher().getReplyHelper();
//		FixpPacketHelper packetHelper = getLauncher().getPacketHelper();
//		int timeout = SocketTransfer.getDefaultChannelTimeout();
//		
//		// Front FixpPacketMonitor 向 Gate Authorizer FixpPacketMonitor查询自己的NAT地址
//		SocketHost natLocal = packetHelper.checkPock(hub, timeout, 3);
//		// Front Reply Sucker 向 Gate Reply Dispatcher 查询自己的NAT地址，形成地址映像
//		SocketHost suckerLocal = replyHelper.checkPock(dispatcher, timeout, 3);
//
//		Logger.info(this, "checkAuthroizerGatePock", "Front Fixp Packet Monitor nat %s -> Call Fixp Packet Monitor %s ", natLocal, hub);
//		Logger.info(this, "checkAuthroizerGatePock", "Front Reply Sucker nat %s -> Call Reply Dispatcher %s ", suckerLocal, dispatcher);
//		
//		// 判断出错
//		if (natLocal == null || suckerLocal == null) {
//			Logger.error(this, "checkAuthroizerGatePock", "cannot be git nat address!");
//			return;
//		}
//
//		// 保存Front FixpPacketMonitor 与 Call FixpPacketMonitor之间的映射
//		packetHelper.addPock(hub, natLocal);
//		// 保存Front ReplySucker -> Call ReplyDispatcher 的映射
//		replyHelper.addPock(dispatcher, suckerLocal);
//	}
	
//	/**
//	 * 检测关联的CALL节点
//	 * @param node 节点
//	 */
//	private void checkCallPock(Node node) {
//		// 与CALL节点的FIXP UDP 服务器建立映像
//		final SocketHost hub = node.getPacketHost();
//		
//		// 与CALL节点的REPLY DISPATCHER 服务器建立映像
//		FrontClient client = ClientCreator.createFrontClient(hub);
//		if (client == null) {
//			Logger.error(this, "checkCallPock", "cannot be git FrontClient!");
//			return;
//		}
//		// 找到ReplyDispatcher主机地址
//		SocketHost dispatcher = null;
//		try {
//			dispatcher = client.getHubDispatcher(isWideAddress());
//			client.close();
//		} catch (VisitException e) {
//			Logger.error(e);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//		client.destroy();
//
//		// 没找到
//		if (dispatcher == null) {
//			Logger.error(this, "checkCallPock", "cannot be find ReplyDispatcher host!");
//			return;
//		}
//		
//		FixpPacketHelper packetHelper = getLauncher().getPacketHelper();
//		ReplyHelper replyHelper = getLauncher().getReplyHelper();
//		int timeout = SocketTransfer.getDefaultChannelTimeout();
//
//		// Front FixpPacketMonitor 向 Call FixpPacketMonitor查询自己的NAT地址
//		SocketHost natLocal = packetHelper.checkPock(hub, timeout, 3);
//		Logger.info(this, "checkCallPock", "Front FixpPacketMonitor nat %s -> Call FixpPacketMonitor %s", natLocal, hub);
//		
//		// Front Reply Sucker 向 Call Reply Dispatcher 查询自己的NAT地址，形成地址映像
//		SocketHost suckerLocal = replyHelper.checkPock(dispatcher, timeout, 3);
//		Logger.info(this, "checkCallPock", "Front ReplySucker nat %s -> Call ReplyDispatcher %s", suckerLocal, dispatcher);
//		
////		if (natLocal == null) {
////			Logger.error(this, "checkCallPock", "cannot be git Local FixpPacketMonitor NAT!");
////			return;
////		}
////		if (suckerLocal == null) {
////			Logger.error(this, "checkCallPock", "cannot be git Locak ReplySucker NAT!");
////			return;
////		}
//		
////		// 判断出错
////		if (natLocal == null || suckerLocal == null) {
////			Logger.error(this, "checkCallPock", "cannot be git nat address!");
////			return;
////		}
//		
//		
//
//		// 保存Front FixpPacketMonitor 与 Call FixpPacketMonitor之间的映射
//		packetHelper.addPock(hub, natLocal);
//		// 保存Front ReplySucker -> Call ReplyDispatcher 的映射
//		replyHelper.addPock(dispatcher, suckerLocal);
//	}
	
	private SocketHost findReplyDispatcher(Node node) {
		// 与CALL节点的REPLY DISPATCHER"服务器建立映像
		FrontClient client = ClientCreator.createFrontClient(node, false);
		if (client == null) {
			Logger.error(this, "findReplyDispatcher", "cannot be git FrontClient!");
			return null;
		}
		// 找到ReplyDispatcher主机地址
		SocketHost dispatcher = null;
		try {
			dispatcher = client.getHubDispatcher(isWideAddress());
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		client.destroy();
		// 返回ReplyDispatcher主机地址
		Logger.debug(this, "findReplyDispatcher", "%s reply dispatcher is %s", node, dispatcher);
		return dispatcher;
	}

	/**
	 * 检测关联的CALL节点
	 * @param node 节点
	 */
	private void checkCallPock(Node node) {
		final SocketHost remote = node.getPacketHost();
		// 与CALL节点的FIXP UDP 服务器建立映像
		FixpPacketHelper packetHelper = getLauncher().getPacketHelper();
		ReplyHelper replyHelper = getLauncher().getReplyHelper();
		// 检测通信端口，如果都存在了，忽略它！
		boolean exists = packetHelper.hasPock(remote);
		// 根据服务端节点地址，找到对应的ReplyDispatcher主机
		SocketHost dispatcher = replyHelper.findReplyDispatcherByHub(node);
		// 判断都存在，忽略！
		if (exists && dispatcher != null) {
			return;
		}

//		// 与CALL节点的REPLY DISPATCHER"服务器建立映像
//		FrontClient client = ClientCreator.createFrontClient(hub);
//		if (client == null) {
//			Logger.error(this, "checkCallPock", "cannot be git FrontClient!");
//			return;
//		}
//		// 找到ReplyDispatcher主机地址
//		SocketHost dispatcher = null;
//		try {
//			dispatcher = client.getHubDispatcher(isWideAddress());
//			client.close();
//		} catch (VisitException e) {
//			Logger.error(e);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//		client.destroy();
		
		// 连接服务器找到ReplyDispatcher主机
		dispatcher = findReplyDispatcher(node);
		if (dispatcher == null) {
			Logger.error(this, "checkCallPock", "cannot be find ReplyDispatcher host!");
			return;
		}
		
		int timeout = SocketTransfer.getDefaultChannelTimeout();

		// Front FixpPacketMonitor 向 Call FixpPacketMonitor查询自己的NAT地址
		SocketHost localNAT = packetHelper.checkPock(remote, timeout, 3);
		Logger.note(this, "checkCallPock", localNAT != null, "Front FixpPacketMonitor NAT %s -> Call FixpPacketMonitor %s",
				localNAT, remote);

		// Front Reply Sucker 向 Call Reply Dispatcher 查询自己的NAT地址，形成地址映像
		SocketHost localSuckerNAT = replyHelper.checkPock(dispatcher, timeout, 3);
		Logger.note(this, "checkCallPock", localSuckerNAT != null, "Front ReplySucker NAT %s -> Call ReplyDispatcher %s",
				localSuckerNAT, dispatcher);

		// 都有效，保存它们
		if (localNAT != null && localSuckerNAT != null) {
			// 保存Front FixpPacketMonitor 与 Call FixpPacketMonitor之间的映射
			packetHelper.addPock(node, remote, localNAT);
			// 保存Front ReplySucker -> Call ReplyDispatcher 的映射
			replyHelper.addPock(node, dispatcher, localSuckerNAT);
		}
	}

	/**
	 * 检测授权的GATE节点
	 * @param node 节点
	 */
	private void checkAuthroizerGatePock(Node node) {
		final SocketHost remote = node.getPacketHost();
		
		// 与授权人的GATE节点的FixpPacketMonitor/ReplyDispatcher服务器建立映像
		ReplyHelper replyHelper = getLauncher().getReplyHelper();
		FixpPacketHelper packetHelper = getLauncher().getPacketHelper();
		// 检测通信端口
		boolean exists = packetHelper.hasPock(remote);
		// 根据服务端节点地址，找到对应的ReplyDispatcher主机
		SocketHost dispatcher = replyHelper.findReplyDispatcherByHub(node);
		// 判断都存在，忽略！
		if (exists && dispatcher != null) {
			return;
		}
		
		// 连接服务器找到ReplyDispatcher主机
		dispatcher = findReplyDispatcher(node);
		if (dispatcher == null) {
			Logger.error(this, "checkAuthroizerGatePock", "cannot be find ReplyDispatcher host!");
			return;
		}
		
		int timeout = SocketTransfer.getDefaultChannelTimeout();

		// Front FixpPacketMonitor 向授权人Gate FixpPacketMonitor查询自己的NAT地址
		SocketHost localNAT = packetHelper.checkPock(remote, timeout, 3);
		Logger.note(this, "checkAuthroizerGatePock", localNAT != null, "Front FixpPacketMonitor NAT %s -> Call FixpPacketMonitor %s",
				localNAT, remote);

		// Front ReplySucker 向授权人Gate ReplyDispatcher查询自己的NAT地址，形成地址映像
		SocketHost localSuckerNAT = replyHelper.checkPock(dispatcher, timeout, 3);
		Logger.note(this, "checkAuthroizerGatePock", localSuckerNAT != null, "Front ReplySucker NAT %s -> Call ReplyDispatcher %s",
				localSuckerNAT, dispatcher);

		// 都有效，保存它们
		if (localNAT != null && localSuckerNAT != null) {
			// 保存Front FixpPacketMonitor 与 Call FixpPacketMonitor之间的映射
			packetHelper.addPock(node, remote, localNAT);
			// 保存Front ReplySucker -> Call ReplyDispatcher 的映射
			replyHelper.addPock(node, dispatcher, localSuckerNAT);
		}
		
//		// 与GATE节点的REPLY DISPATCHER 服务器建立映像
//		FrontClient client = ClientCreator.createFrontClient(hub);
//		if (client == null) {
//			Logger.error(this, "checkAuthroizerGatePock", "cannot be git HubClient!");
//			return;
//		}
//		// 找到ReplyDispatcher主机地址
//		SocketHost dispatcher = null;
//		try {
//			dispatcher = client.getHubDispatcher(isWideAddress());
//			client.close();
//		} catch (VisitException e) {
//			Logger.error(e);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//		client.destroy();
//		
//		// 没找到
//		if (dispatcher == null) {
//			Logger.error(this, "checkAuthroizerGatePock", "cannot be find ReplyDispatcher host!");
//			return;
//		}
//		
//		int timeout = SocketTransfer.getDefaultChannelTimeout();
//		
//		// Front FixpPacketMonitor 向 Gate Authorizer FixpPacketMonitor查询自己的NAT地址
//		SocketHost natLocal = packetHelper.checkPock(hub, timeout, 3);
//		// Front Reply Sucker 向 Gate Reply Dispatcher 查询自己的NAT地址，形成地址映像
//		SocketHost suckerLocal = replyHelper.checkPock(dispatcher, timeout, 3);
//
//		Logger.info(this, "checkAuthroizerGatePock", "Front Fixp Packet Monitor nat %s -> Call Fixp Packet Monitor %s ", natLocal, hub);
//		Logger.info(this, "checkAuthroizerGatePock", "Front Reply Sucker nat %s -> Call Reply Dispatcher %s ", suckerLocal, dispatcher);
//		
//		// 判断出错
//		if (natLocal == null || suckerLocal == null) {
//			Logger.error(this, "checkAuthroizerGatePock", "cannot be git nat address!");
//			return;
//		}
//
//		// 保存Front FixpPacketMonitor 与 Call FixpPacketMonitor之间的映射
//		packetHelper.addPock(hub, natLocal);
//		// 保存Front ReplySucker -> Call ReplyDispatcher 的映射
//		replyHelper.addPock(dispatcher, suckerLocal);
	}

	/**
	 * 通过CAL/GATEL节点，判断出与它连接的NAT地址，并且保存在本地
	 * @param node CALL/GATE节点地址
	 */
	protected void checkPock(Node node) {
		// 如果不是在NAT的内网里，忽略后面的操作
		if (!isPock()) {
			return;
		}

		// 如果是CALL节点地址
		if (node.isCall()) {
			checkCallPock(node);
		} 
		// 如果是GATE节点，检测与授权人节点的信道
		else if(node.isGate()) {
			checkAuthroizerGatePock(node);
		} 
		// 以上不成立，忽略
		else {
			Logger.warning(this, "checkPock", "illegal node %s", node);
			return;
		}
	}

}