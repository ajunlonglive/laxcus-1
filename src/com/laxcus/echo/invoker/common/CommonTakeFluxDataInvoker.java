/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.distribute.calculate.command.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.echo.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.task.*;
import com.laxcus.task.flux.*;
import com.laxcus.util.*;

/**
 * TakeFluxData命令由WORK站点发出，CommonTakeFluxDataInvoker调用器部署在DATA/WORK站点，获取本地的CONDUCT命令中间数据后，发送给WORK站点。<br>
 * 
 * 数据传输采用超级传输模式。以小块分组方式，单次传输一个大数据块（一般超过64K），传输模式是UDP。
 * 
 * @author scott.liang
 * @version 1.0 7/12/2012
 * @since laxcus 1.0
 */
public class CommonTakeFluxDataInvoker extends CommonInvoker {

	/**
	 * 构造FLUX数据调用器，指定命令
	 * @param cmd TakeFluxData实例
	 */
	public CommonTakeFluxDataInvoker(TakeFluxData cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeFluxData getCommand() {
		return (TakeFluxData) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeFluxData cmd = getCommand();
		long taskId = cmd.getTaskId();
		FluxField field = cmd.getField();

		Logger.debug(this, "launch", "taskid:%d, field:%s", taskId, field);

		// 取得FLUX读取器
		FluxReader reader = null;
		try {
			reader = FluxTrustorPool.getInstance().findReader(getInvokerId(), taskId);
		} catch (TaskException e) {
			Logger.error(e);
		}

		// 判断FLUX读取器有效
		boolean success = (reader != null);
		if (success) {
			success = upload(reader, field);
		}

		// 不成功，反馈结果
		if (!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
		}

		Logger.debug(this, "launch", success, "taskid:%d, field:%s", taskId, field);

		// 结束
		return useful(success);
	}

	/**
	 * 根据请求单元，发送CONDUCT中间数据
	 * @param reader FLUX数据读取器
	 * @param field FLUX数据域
	 * @return 成功返回真，否则假
	 */
	private boolean upload(FluxReader reader, FluxField field) {
		// 单次最大传输尺寸
		int maxsize = ReplyTransfer.getDefaultPacketContentSize(); // SocketTransfer.getTransferSize(); 

		Logger.debug(this, "upload", "%s max size is:%d", field, maxsize);

		long mod = field.getMod();
		long length = field.getSize();
		// 下标开始位置
		long seek = field.getSeek();
		// 最后结束位置
		long end = seek + length;

		// 生成辅助信息，投递给目标站点
		CastFlag flag = createCastFlag();
		EchoHead head = new EchoHead(Major.SUCCESSFUL_FILE, length, flag);
		
		// 记录开始时间
		long launchTime = System.currentTimeMillis();

		// 建立句柄
		Cabin endpoint = getCommandSource();
		EchoClient client = ClientCreator.createEchoClient(endpoint);
		if (client == null) {
			Logger.error(this, "upload", "cannot be create EchoClient");
			return false;
		}
		// 发送报头
		CastToken token = client.doCast(head);
		if (token == null) {
			Logger.error(this, "upload", "cannot be send EchoHead");
			return false;
		}
		// 销毁
		client.destroy();

		// 建立实例，乱序发送数据包，绑定的地址由系统分配
		ReplyClient caster = new ReplyClient(token); 
		//		caster.setPacketSize(SocketTransfer.getTransferSize()); // 定义单包尺寸

		// 绑定地址
		boolean success = caster.bind();
		if (!success) {
			Logger.error(this, "upload", "cannot be bind");
			return false;
		}

		// 发送HELO命令，确定握手成功。
		success = caster.hello();
		Logger.note(this, "upload", success, "cast hello command");

		// 循环从磁盘读出数据，发送它
		while(success && seek < end) {
			// 本次读取尺寸
			int len = (int) Laxkit.limit(seek, end, maxsize);
			// 根据长度读取数据
			byte[] b = reader.read(mod, seek, len);
			// 判断读成功
			if (b == null || b.length != len) {
				Logger.error(this, "upload", "b is null, or b.length != %d", len);
				break;
			}

			// 发送FIXP UDP数据包
			success = caster.send(b, 0, len);
			if (!success) {
				Logger.error(this, "upload", "cannot be send data");
				break;
			}
			// 指向下一段
			seek += len;
		}
		// 通知服务端关闭，和关闭本地套接字
		caster.exit();
		caster.close();
		caster.destroy();

		// 保存数据传输时间
		long castTime = caster.getRunTime();
//		Logger.debug(this, "upload", "transfer timed:%d ms", caster.getRunTime());

		// 统计接收/发送数据
		addReceiveFlowSize(caster.getReceiveFlowSize());
		addSendFlowSize(caster.getSendFlowSize());

		// 判断成功
		success = (seek == end);
		// 生成报尾
		EchoTail tail = new EchoTail(success, seek, caster.getSubPackets());

		// 再次打开
		client = createEchoClient(endpoint, false);
		if (client == null) {
			Logger.error(this, "upload", "cannot be create EchoClient");
			return false;
		}
		// 发送快速RPC关闭
		success = client.doExit(tail);
		// 关闭
		client.destroy();

		Logger.debug(this, "upload", success,
				"transfer timed:%d ms, client timed:%d ms, send %s, seek:%d, length:%d, blocks:%d",
				castTime, (System.currentTimeMillis()-launchTime), field, seek, length, caster.getSubPackets());

		return success;
	}


	//	/**
	//	 * 根据请求单元，发送CONDUCT中间数据
	//	 * @param reader FLUX数据读取器
	//	 * @param field FLUX数据域
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean upload(FluxReader reader, FluxField field) {
	//		// 单次最大传输尺寸
	//		int maxsize = SocketTransfer.getTransferSize(); 
	//
	//		Logger.debug(this, "upload", "%s max size is:%d", field, maxsize);
	//
	//		long mod = field.getMod();
	//		long length = field.getSize();
	//		// 下标开始位置
	//		long seek = field.getSeek();
	//		// 最后结束位置
	//		long end = seek + length;
	//
	//		// 生成辅助信息，投递给目标站点
	//		CastFlag flag = createCastFlag();
	//		EchoHead head = new EchoHead(Major.SUCCESSFUL_FILE, length, flag);
	//
	//		// 建立句柄
	//		Cabin endpoint = getCommandSource();
	//		EchoClient client = ClientCreator.createEchoClient(endpoint);
	//		if (client == null) {
	//			Logger.error(this, "upload", "cannot be create EchoClient");
	//			return false;
	//		}
	//		// 发送报头
	//		CastToken token = client.doCast(head);
	//		if (token == null) {
	//			Logger.error(this, "upload", "cannot be send EchoHead");
	//			return false;
	//		}
	//		// 销毁
	//		client.destroy();
	//
	//		// 建立实例，乱序发送数据包，绑定的地址由系统分配
	//		CastClient caster = new CastClient(token); 
	//		
	//		boolean success = caster.bind();
	//		if (!success) {
	//			Logger.error(this, "upload", "cannot be bind");
	//			return false;
	//		}
	//
	//		// 发送HELO命令，确定握手成功。
	//		success = caster.hello();
	//		Logger.note(this, "upload", success, "cast hello command");
	//
	//		// 循环从磁盘读出数据，发送它
	//		while(success && seek < end) {
	//			// 本次读取尺寸
	//			int len = (int) Laxkit.limit(seek, end, maxsize);
	//			// 根据长度读取数据
	//			byte[] b = reader.read(mod, seek, len);
	//			// 判断读成功
	//			if (b == null || b.length != len) {
	//				Logger.error(this, "upload", "b is null, or b.length != %d", len);
	//				break;
	//			}
	//
	//			// 发送FIXP UDP数据包
	//			success = caster.send(b, 0, len);
	//			if (!success) {
	//				Logger.error(this, "upload", "cannot be send data");
	//				break;
	//			}
	//			// 指向下一段
	//			seek += len;
	//		}
	//		// 通知服务端关闭，和关闭本地套接字
	//		caster.exit();
	//		caster.close();
	//
	//		// 统计接收/发送数据
	//		addReceiveFlowSize(caster.getReceiveFlowSize());
	//		addSendFlowSize(caster.getSendFlowSize());
	//
	//		// 判断成功
	//		success = (seek == end);
	//		// 生成报尾
	//		EchoTail tail = new EchoTail(success, seek, caster.getSubPackets());
	//
	//		// 再次打开
	//		client = createEchoClient(endpoint, false);
	//		if (client == null) {
	//			Logger.error(this, "upload", "cannot be create EchoClient");
	//			return false;
	//		}
	//		// 发送快速RPC关闭
	//		success = client.doExit(tail);
	//		// 关闭
	//		client.destroy();
	//
	//		Logger.debug(this, "upload", success,
	//				"send %s, seek:%d, length:%d, blocks:%d", field, seek, length,
	//				caster.getSubPackets());
	//
	//		return success;
	//	}

	//	/**
	//	 * 根据请求单元，发送CONDUCT中间数据
	//	 * @param reader FLUX数据读取器
	//	 * @param field FLUX数据域
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean send(FluxReader reader, FluxField field) {
	//		// 传输模式，TCP/UDP中的任意一个
	//		boolean stream = EchoClient.isLargeStreamTrasfer(); 
	//		// 单次最大传输尺寸
	//		int maxsize = EchoClient.getLargeMTU(); 
	//
	//		long mod = field.getMod();
	//		long length = field.getSize();
	//		// 下标开始位置
	//		long seek = field.getSeek();
	//		// 最后结束位置
	//		long end = seek + length;
	//
	//		// 建立句柄
	//		Cabin hub = super.getCommandSource();
	//		EchoClient client = ClientCreator.createEchoClient(hub, stream);
	//		if (client == null) {
	//			Logger.error(this, "send", "cannot be create EchoClient");
	//			return false;
	//		}
	//		// 设置传输尺寸
	//		client.setDefaultSize(maxsize);
	//		// 发送报头
	//		EchoHead head = new EchoHead(Major.SUCCESSFUL, length);
	//		boolean success = client.sendStart(head);
	//		if (!success) {
	//			Logger.error(this, "send", "cannot be send EchoHead");
	//			return false;
	//		}
	//
	//		final long scale = seek; // 刻度开始下标
	//		while(seek < end) {
	//			// 本次读取尺寸
	//			int readlen = (int) Laxkit.limit(seek, end, maxsize);
	//			// 根据长度读取数据
	//			byte[] b = reader.read(mod, seek, readlen);
	//
	//			// 发送数据
	//			long sendlen = client.sendField(seek - scale, b, 0, b.length);
	//
	//			// 不匹配是错误
	//			if (scale + readlen != scale + sendlen) {
	//				Logger.error(this, "send", "%d + %d != %d + %d", seek, readlen, seek, sendlen);
	//				break;
	//			}
	//			// 下一段
	//			seek += readlen;
	//		}
	//		// 判断成功
	//		success = (seek == end);
	//
	//		//		short resp = (success ? Major.SUCCESSFUL : Major.FAULTED);
	//		int count = client.blocks(length);
	//		EchoTail tail = new EchoTail(success, length, count);
	//		success = client.sendStop(tail);
	//
	//		Logger.debug(this, "send", success, "blocks:%d",count);
	//
	//		// 关闭
	//		client.close();
	//
	//		return success;
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
