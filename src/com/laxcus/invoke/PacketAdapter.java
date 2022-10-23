/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.invoke;

import com.laxcus.fixp.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 数据包适配器。实现“PacketInvoker”接口，提供UDP数据包处理服务。<br><br>
 * 
 * 所有站点的数据包适配器主要接受三个命令通知：<br>
 * 1. 来自上级站点的激活应答。<br>
 * 2. 远程关闭命令。<br>
 * 3. 上级站点的超时通知。<br>
 * 
 * @author scott.liang
 * @version 1.1 4/12/2013
 * @since laxcus 1.0
 */
public abstract class PacketAdapter implements PacketInvoker {

	/** FIXP数据包转发器，通过它直接发送应答包 **/
	private PacketTransmitter transmitter;

	/**
	 * 构造默认的数据包适配器
	 */
	protected PacketAdapter() {
		super();
	}

	/**
	 * 设置FIXP数据包转发器
	 * @param e PacketTransmitter实例
	 */
	@Override
	public void setPacketTransmitter(PacketTransmitter e) {
		this.transmitter = e;
	}

	/**
	 * 返回FIXP数据包转发器
	 * @return PacketTransmitter实例
	 */
	@Override
	public PacketTransmitter getPacketTransmitter() {
		return this.transmitter;
	}

	/**
	 * 分别处理FIXP的请求和应答数据
	 * @see com.laxcus.invoke.PacketInvoker#invoke(com.laxcus.fixp.Packet)
	 */
	@Override
	public Packet invoke(Packet packet) {
		Mark cmd = packet.getMark();
		if (cmd.isAsk()) {
			return apply(packet);
		} else if (cmd.isAnswer()) {
			return reply(packet);
		}
		return null;
	}

	/**
	 * 刷新注册站点
	 * @param request 请求数据包
	 * @return 返回应答数据包或者空指针 
	 */
	protected Packet refresh(Packet request) {	
		Message message = request.findMessage(MessageKey.NODE_ADDRESS);
		// 可疑数据包不理睬
		if (message == null || !message.isRaw()) {
			Logger.error(this, "refresh", "illegal message!");
			return null;
		}

		Node node = null;
		try {
			byte[] b = message.getValue();
			ClassReader reader = new ClassReader(b);
			node = new Node(reader);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 判断成功
		boolean success = (node != null);
		// 不成功，退出
		if (!success) {
			return new Packet(request.getRemote(), new Mark(Answer.CLIENT_ERROR));
		}

		// 激活注册站点
		success = active(node);
		
		// 激活失败！
		if (!success) {
			Logger.error(this, "refresh", "cannot be find %s # %s", request.getRemote(), node);
		}
		
		// 构造应答数据包
		Mark mark = new Mark(success ? Answer.ISEE : Answer.NOTLOGIN);
		Packet resp = new Packet(request.getRemote(), mark);
		resp.addMessage(MessageKey.SPEAK, "i say helo!");
		return resp;
	}

	/**
	 * 接受远程关闭命令，站点退出运行进程。
	 * @param launcher 站点启动器
	 * @param request 请求包
	 */
	protected void shutdown(SiteLauncher launcher, Packet request) {
		SocketHost remote = request.getRemote();
		// 判断数据包的来源地址在本地接受范围内
		boolean success = launcher.onShutdowns(remote.getAddress());
		// 如果远程地址在关闭表范围内就接受，否则拒绝
		short code = (success ? Answer.OKAY : Answer.REFUSE);
		// 为了防止丢包，连续发送5次
		for (int i = 0; i < 5; i++) {
			Mark cmd = new Mark(code);
			Packet resp = new Packet(remote, cmd);
			resp.addMessage(MessageKey.SPEAK, "goodbye!");
			// 发送应答包
			transmitter.reply(resp);
		}

		Logger.debug(this, "shutdown", success, "shutdown %s", launcher.getClass().getName());

		// 退出进程
		if (success) {
			launcher.stop();
		}		
	}

	/**
	 * 收到来自子级注册站点的HELLO通知，激活注册站点。
	 * @param node 注册站点地址
	 * @return 激活成功返回“真”，否则“假”。
	 */
	protected abstract boolean active(Node node);

	/**
	 * 普通的数据包请求操作（排除异步的数据包请求）
	 * @param request 请求数据包
	 * @return 受理返回应答包，否则是空值
	 */
	protected abstract Packet apply(Packet request);

	/**
	 * 应答包应答操作，如果没有返回空
	 * @param resp 应答数据包
	 * @return 受理返回再次应答包，否则是空值
	 */
	protected abstract Packet reply(Packet resp);

}