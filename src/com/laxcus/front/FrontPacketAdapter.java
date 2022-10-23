/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

import com.laxcus.fixp.*;
import com.laxcus.front.pool.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * FRONT站点的数据包适配器。兼容终端、控制台、驱动程序三种模式
 * 
 * @author scott.liang
 * @version 1.1 8/19/2012
 * @since laxcus 1.0
 */
public class FrontPacketAdapter extends PacketAdapter { 

	/** 前端启动器 **/
	private FrontLauncher launcher;

	/**
	 * 构造前端数据包监听器，指定前端启动器
	 * @param e 前端启动器
	 */
	public FrontPacketAdapter(FrontLauncher e) {
		super();
		launcher = e;
	}

	/**
	 * 处理请求包
	 * @see com.laxcus.invoke.PacketAdapter#apply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet apply(Packet request) {
		Packet resp = null;

		Mark cmd = request.getMark();
		if (Assert.isShutdown(cmd)) {
			shutdown(request);
		} else if(Assert.isComeback(cmd)) {
			Logger.debug(this,"apply", "re-ping! from %s", request.getRemote());
			launcher.hurry();
		}

		return resp;
	}

	/**
	 * 处理应答包
	 * @see com.laxcus.invoke.PacketAdapter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet reply(Packet packet) {
		Mark cmd = packet.getMark();
		// 收到激活回应
		if (Answer.isIsee(cmd.getAnswer())) {
			Logger.debug(this, "reply", "from %s", packet.getRemote());
			saw(packet);
		} else if (Answer.isNotLogin(cmd.getAnswer())) {			
			// 1. TERMINAL/CONSOLE，弹出窗口重新注册；DRIVER/EDGE，没有窗口
			// 2. 如果来自CALL站点，检测有注册，重新注册
			forsake(packet);
		}
		return null;
	}

	/**
	 * 重新注册，用线程！不影响调用器的使用
	 * @param packet 来源数据包
	 */
	private void forsake(Packet packet) {
		SocketHost remote = packet.getRemote();
		Node hub = launcher.getHub();
		
		Logger.warning(this, "forsake", "from %s # hub is %s", remote, hub);

		// 判断是来自GATE站点
		boolean success = (Laxkit.compareTo(hub.getPacketHost(), remote) == 0);

		// 如果来自GATE站点，重新注册
		if (success) {
			// 重新注册
			GateForsakeThread e = new GateForsakeThread(launcher);
			e.start();
		} else {
			// 重新注册
			CallForsakeThread e = new CallForsakeThread(launcher, remote);
			e.start();
		}
	}

	/**
	 * 处理服务器端的激活反馈。来自GATE/CALL站点。
	 * @param packet 来源数据包
	 */
	private void saw(Packet packet) {
		SocketHost remote = packet.getRemote();

		// 判断来自注册的GATE站点，刷新它
		Node hub = launcher.getHub();
		if (Laxkit.compareTo(hub.getPacketHost(), remote) == 0) {
			// 刷新时间
			launcher.refreshEndTime();
			// 显示激活图标
			launcher.ticking();
		}
		
		// 来自注册的CALL节点，刷新它
		if (CallOnFrontPool.getInstance().contains(remote)) {
			CallOnFrontPool.getInstance().refresh(remote);
		}
		// 来自授权人GATE站点，刷新它
		if (AuthroizerGateOnFrontPool.getInstance().contains(remote)) {
			AuthroizerGateOnFrontPool.getInstance().refresh(remote);
		}
	}

	/**
	 * 前端关闭和退出
	 * @param request
	 */
	private void shutdown(Packet request) {
		SocketHost remote = request.getRemote();
		for (int i = 0; i < 3; i++) {
			Mark cmd = new Mark(Answer.OKAY);
			Packet resp = new Packet(remote, cmd);
			resp.addMessage(MessageKey.SPEAK, "goodbye!");
			PacketTransmitter transmitter = super.getPacketTransmitter();
			transmitter.reply(resp);
		}
		// 前端关闭和退出
		launcher.shutdown();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
	 */
	@Override
	protected boolean active(Node node) {
		return false;
	}
}