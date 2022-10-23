/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.tub.invoker;

import java.util.*;

import com.laxcus.command.traffic.*;
import com.laxcus.echo.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.front.tub.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 数据传输速率测试调用器。<br><br>
 * 
 * <B>FRONT节点位于“客户机”位置，向位于“服务器”位置的GATE节点发送数据。检测内网/外网两个网络之间的传输速率。</B>
 * 
 * @author scott.liang
 * @version 1.0 2/5/2019
 * @since laxcus 1.0
 */
public class TubSwarmInvoker extends TubInvoker {

	/**
	 * 构造数据传输速率测试调用器，指定命令
	 * @param cmd 数据传输速率测试命令
	 */
	public TubSwarmInvoker(TubMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Swarm getCommand() {
		return (Swarm) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Swarm cmd = getCommand();
		// 服务器地址不能定义！
		if (!cmd.isHub()) {
			faultX(FaultTip.CANNOT_SUBMIT);
			return false;
		}
		Node hub = getHub();
		boolean success = fireToHub(hub, cmd);
		Logger.debug(this, "launch", success, "to %s", hub);
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		SwarmReflex reflex = null;
		try {
			if (isSuccessObjectable(index)) {
				reflex = getObject(SwarmReflex.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 测试流量
		boolean success = (reflex != null);
		
		Logger.debug(this, "ending", success, "from %s", getHub());
		
		if (success) {
			success = send(reflex);
		}

		Logger.debug(this, "ending", success, "send from %s", getHub());
		
		// 以上错误，打印故障
		if (!success) {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		return useful(success);
	}

	/**
	 * 传输数据流
	 * @param reflex 反馈命令
	 * @return 传输成功返回真，否则假
	 */
	private boolean send(SwarmReflex reflex) {	
		Cabin remote = reflex.getSource();

		// 结果
		TrafficProduct product = null;

		// 启动异步通信
		CastToken token = start(remote);
		// 上传数据，测试传输速率
		if (token != null) {
			product = upload(token);
		}
		// 关闭连接
		exit(remote, product);

		// 判断成功
		boolean success = (product != null);
		if (success) {
			setProduct(product);
		}

		return success;
	}

	/**
	 * 启动操作
	 * @param remote 目标站点
	 * @return 返回快速投递标识
	 */
	private CastToken start(Cabin remote) {
		EchoCode code = new EchoCode(Major.SUCCESSFUL_DATA);
		CastFlag flag = createCastFlag(remote);
		EchoHead head = new EchoHead(code, 0, flag);

		// 建立连接
		EchoClient client = createEchoClient(remote);
		if (client == null) {
			Logger.error(this, "start", "cannot be create EchoClient");
			return null;
		}

		// 发送报头
		CastToken token = client.doCast(head);
		if(token == null){
			Logger.error(this, "start", "cannot be send EchoHead");
		}
		// 销毁
		client.destroy();

		return token;
	}

	/**
	 * 关闭异步连接
	 * @param remote 目标站点
	 * @return 成功返回真否则假
	 */
	private boolean exit(Cabin remote, TrafficProduct product) {
		int sendSize = -1;
		int sendPackets = -1;

		// 判断成功
		boolean success = (product != null && product.isSuccessful());
		if (success) {
			sendSize = product.getSendSize();
			sendPackets = product.getSendPacket();
		}

		// 发送报尾
		EchoTail tail = new EchoTail(success, sendSize, sendPackets);

		// 再次打开
		EchoClient client = createEchoClient(remote);
		if (client == null) {
			Logger.error(this, "exit", "cannot be create EchoClient");
			return false;
		}

		// 发送快速RPC关闭
		success = client.doExit(tail);
		// 销毁
		client.destroy();

		return success;
	}

	/**
	 * 向目标地址上传数据
	 * @param token
	 * @return 返回结果
	 */
	private TrafficProduct upload(CastToken token) {
		Swarm cmd = getCommand();
		final int length = cmd.getLength();
		final int packetSize = cmd.getPacketSize();
		final int subPacketSize = cmd.getSubPacketSize();

		// 分配内存，可能存在内存不足现象
		byte[] b = null;
		try {
			b = new byte[packetSize];
		} catch (OutOfMemoryError e) {
			faultX(FaultTip.SYSTEM_FAULT);
			return null;
		}
		Arrays.fill(b, (byte) 'A');

		// 建立实例，乱序发送数据包，绑定的地址由系统分配
		ReplyClient client = new ReplyClient(token);
		client.setSendInterval(cmd.getSendInterval());

		// 绑定本地地址
		boolean success = client.bind();
		if (!success) {
			Logger.error(this, "upload", "cannot bind socket!");
			return null;
		}

		// 发送HELO命令，确定握手成功。
		success = client.hello();

		// 不成功，关闭SOCKET退出
		if (!success) {
			Logger.error(this, "upload", "cannot send 'helo'");
			client.close();
			return null;
		}

		// 命令规定的子包长度
		client.setSubPacketSize(subPacketSize);
		// 以下三个参数：1. 发送间隔；2. 发送单元数；3. 发送模式（并行）。
		client.setSendInterval(0); // 0延时
		client.setSendUnit(0xFFFF - 1); // 最大限制值
		client.setMode(ReplyTransfer.PARALLEL_TRANSFER);
		// 拒绝服务端发来的流量控制参数
		client.setRefuseFlowControl(true);
		
		// 传输数据！
		int seek = 0;
		int end = seek + length;
		while (seek < end) {
			// 读取指定的长度
			int len = Laxkit.limit(seek, end, b.length);
			// 发送数据包
			success = client.send(b, 0, len, subPacketSize);
			if (!success) {
				Logger.error(this, "upload", "cannot send data");
				break;
			}
			// 移到下标
			seek += len;
		}
		// 通知服务器端退出和关闭套接字
		client.exit();
		client.close();

		// 记录参数
		success = (seek == end);

		Node hub = (cmd.isHub() ? getHub() : cmd.getSite());
		
		TrafficProduct product = new TrafficProduct(success, getLocal(), hub);
		product.setSendSize(seek);
		product.setSendPackets(client.getSubPackets());
		product.setTimeoutCount(client.getTimeoutCount());
		product.setRunTime(client.getRunTime());
		product.setRetries(client.getRetries());

		// 增加发送流量
		addSendFlowSize(seek);
		
		// 释放
		client.destroy();

		return product;
	}


}