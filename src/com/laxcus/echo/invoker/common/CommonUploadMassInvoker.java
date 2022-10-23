/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.access.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.echo.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.util.*;

/**
 * 上传数据块调用器。<br><br>
 * 
 * 这个调用器位于BUILD/DATA站点，是一个“服务器”状态的调用。它从本地Access/JNI接口中读取数据块，传输给请求端。
 * 
 * @author scott.liang
 * @version 1.1 3/23/2012
 * @since laxcus 1.0
 */
public class CommonUploadMassInvoker extends CommonInvoker {

	/**
	 * 构造上传数据块调用器，指定命令
	 * @param cmd 下载数据块命令
	 */
	public CommonUploadMassInvoker(DownloadMass cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DownloadMass getCommand() {
		return (DownloadMass) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DownloadMass cmd = getCommand();
		StubFlag flag = cmd.getFlag();

		// 返回磁盘文件路径
		String path = null;
		if (cmd.isCacheReflex()) {
			path = AccessTrustor.findCachePath(flag.getSpace(), flag.getStub());
		} else {
			path = AccessTrustor.findChunkPath(flag.getSpace(), flag.getStub());
		}
		boolean success = (path != null);

		Logger.debug(this, "launch", success, "%s at %s", flag, path);

		// 快速上传数据
		if (success) {
			success = upload(path);
		}

		// 不成功，通知调用方
		if (!success) {
			Logger.error(this, "launch", "cannot find '%s'", flag);
			replyFault(Major.FAULTED, Minor.CHUNK_NOTFOUND);
		}

		return useful(success);
	}

	/**
	 * 快速上传数据块文件
	 * @param path 磁盘文件路径
	 * @return 上传成功返回真，否则假
	 */
	private boolean upload(String path) {
		// 确认文件长度
		long length = AccessTrustor.length(path);
		// 大规模数据传输尺寸，默认采用UDP传输，单次传输不超过512K为宜
//		int maxsize = SocketTransfer.getTransferSize();
		int maxsize = ReplyTransfer.getDefaultPacketContentSize();

		Logger.debug(this, "upload", "%s filesize is %d, maxsize:%d",
				path, length, maxsize);

		// 建立基础参数
		CastFlag flag = createCastFlag();
		EchoHead head = new EchoHead(Major.SUCCESSFUL_FILE, length, flag);

		// 采用TCP/UDP模式，由配置决定
		Cabin endpoint = getCommandSource();
		EchoClient client = createEchoClient(endpoint);
		if (client == null) {
			Logger.error(this, "upload", "cannot be create EchoClient");
			return false;
		}

		// 发送报头
		CastToken token = client.doCast(head);
		if(token == null){
			Logger.error(this, "upload", "cannot be send EchoHead");
			return false; 
		}
		// 销毁
		client.destroy();

		// 建立实例，乱序发送数据包，绑定的地址由系统分配
		ReplyClient caster = new ReplyClient(token);
//		caster.setPacketSize(SocketTransfer.getTransferSize());

		// 绑定本地地址
		boolean success = caster.bind();
		if (!success) {
			Logger.error(this, "upload", "cannot be bind");
			return false;
		}

		// 发送HELO命令，确定握手成功。
		success = caster.hello();
		Logger.note(this, "upload", success, "cast hello command");

		// 读数据块内容，发送数据块
		long seek = 0L;
		// 以HELO命令成功前提下，读出数据，直到完成 
		while(success && seek < length) {
			// 按照用户要求单元尺寸，读取磁盘上的数据
			int len = (int) Laxkit.limit(seek, length, maxsize);
			// 从JNI接口读一段数据
			byte[] b = AccessTrustor.read(path, seek, len);
			if (b == null || b.length != len) {
				Logger.error(this, "upload", "b is null, or b.length != %d", len);
				break;
			}

			// 发送数据包
			success = caster.send(b, 0, len);
			if(!success){
				Logger.error(this, "upload", "cannot be send data");
				break;
			}
			// 下一段
			seek += len;
		}
		// 通知服务器端退出，和关闭套接字
		caster.exit();
		caster.close();
		caster.destroy();
		
		Logger.debug(this, "upload", "transfer timed: %d ms", caster.getRunTime());

		// 统计接收/发送数据
		addReceiveFlowSize(caster.getReceiveFlowSize());
		addSendFlowSize(caster.getSendFlowSize());

		// 判断成功
		success = (seek == length);
		// 发送报尾
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

		Logger.debug(this, "upload", success, "send %s, length:%d, blocks:%d",
				path, length, caster.getSubPackets());

		return success;
	}

	//	/**
	//	 * 上传数据块文件
	//	 * @param path - 磁盘文件路径
	//	 * @return - 上传成功返回真，否则假
	//	 */
	//	private boolean upload1(String path) {
	//		// 确认文件长度
	//		long length = AccessTrustor.length(path);
	//
	//		//		super.getLocal().getAddress();
	//
	//		Logger.debug(this, "upload", "%s filesize is %d", path, length);
	//
	//		final int maxsize = EchoClient.getLargeMTU(); // 超级传输数据尺寸
	//		boolean stream = EchoClient.isDefaultStreamTransfer(); // .isLargeStreamTrasfer();
	//
	//		// 建立数据发送器，指定数据流模式和传输尺寸
	//		Cabin endpoint = getCommandSource();
	//		EchoClient client = createEchoClient(endpoint, stream);
	//		if (client == null) {
	//			Logger.error(this, "upload", "cannot be create EchoClient");
	//			return false; // useful(false);
	//		}
	//		client.setDefaultSize(maxsize);
	//		// 发送报头
	//		EchoHead head = new EchoHead(Major.SUCCESSFUL, length);
	//		boolean success = client.sendStart(head);
	//		if (!success) {
	//			Logger.error(this, "upload", "cannot be send EchoHead");
	//			return false; 
	//		}
	//
	//		// 读数据块内容，发送数据块
	//		long seek = 0L;
	//		int index = 0;
	//		// 读出数据，直到最后
	//		while(seek < length) {
	//			// 判断单次读取最大长度
	//			int len = (int) Laxkit.limit(seek, length, maxsize);
	//			// 从JNI接口读一段数据
	//			byte[] b = AccessTrustor.read(path, seek, len);
	//			if (b == null || b.length != len) {
	//				Logger.error(this, "upload", "b is null, or b.length != %d", len);
	//				break;
	//			}
	//
	//			// 发送一块数据
	//			EchoField field = new EchoField(index, seek, b, 0, b.length);
	//			long next = client.sendPush(field);
	//			if (seek + len != next) {
	//				Logger.error(this, "upload", "%d + %d != %d", seek, len, next);
	//				break;
	//			}
	//
	//			// 下一段
	//			seek += len;
	//			index++;
	//		}
	//
	//		// 判断成功
	//		success = (seek == length);
	//		// 发送报尾
	////		short reply = (success ? Major.SUCCESSFUL : Major.FAULTED);
	//		EchoTail tail = new EchoTail(success, seek, index);
	//		success = client.sendStop(tail);
	//		// 关闭
	//		client.close();
	//
	//		Logger.debug(this, "upload", success, "send %s, length:%d, blocks:%d",
	//				path, length, index);
	//
	//		return success;
	//	}

	//	public boolean launch() {
	//		DownloadChunk cmd = getCommand();
	//
	//		StubFlag flag = cmd.getFlag();
	//
	//		// 返回磁盘文件路径
	//		String path = Access.findChunkPath(flag.getSpace(), flag.getStub());
	//		boolean success = (path != null);
	//
	//		Logger.debug(this, "launch", success, "%s at %s", flag, path);
	//
	//		// 不成功，退出
	//		if (!success) {
	//			Logger.error(this, "launch", "cannot find '%s'", flag);
	//			super.sendFault(Major.FAULT, Minor.CHUNK_NOTFOUND);
	//			return useful(false);
	//		}
	//		// 确认文件长度
	//		long length = Access.length(path);
	//
	//		Logger.debug(this, "launch", "%s filesize is %d", path, length);
	//
	//		final int maxsize = EchoClient.getSuperTransferSize(); // 超级传输数据尺寸
	//		boolean stream = EchoClient.isStreamTransfer();
	//
	//		// 建立数据发送器，指定数据流模式和传输尺寸
	//		Cabin endpoint = getCommandListener();
	//		EchoClient client = createEchoClient(endpoint, stream);
	//		if (client == null) {
	//			Logger.error(this, "launch", "cannot be create EchoClient");
	//			return useful(false);
	//		}
	//		client.setDefaultSize(maxsize);
	//		// 发送报头
	//		EchoHead head = new EchoHead(Major.SUCCESS, length);
	//		success = client.sendHead(head);
	//		if (!success) {
	//			Logger.error(this, "launch", "cannot be send EchoHead");
	//			return useful(false);
	//		}
	//
	//		// 读数据块内容，发送数据块
	//		long seek = 0L;
	//		int index = 0;
	//		// 读出数据，直到最后
	//		while(seek < length) {
	//			// 判断单次读取最大长度
	//			int len = (int) Laxkit.limit(seek, length, maxsize);
	//			// 从JNI接口读一段数据
	//			byte[] b = Access.read(path, seek, len);
	//			if (b == null || b.length != len) {
	//				Logger.error(this, "launch", "b is null, or b.length != %d", len);
	//				break;
	//			}
	//
	//			// 发送一块数据
	//			EchoField field = new EchoField(index, seek, b, 0, b.length);
	//			long next = client.sendPush(field);
	//			if (seek + len != next) {
	//				Logger.error(this, "launch", "%d + %d != %d", seek, len, next);
	//				break;
	//			}
	//
	//			// 下一段
	//			seek += len;
	//			index++;
	//		}
	//
	//		// 判断成功
	//		success = (seek == length);
	//		// 发送报尾
	//		short reply = (success ? Major.SUCCESS : Major.FAULT);
	//		EchoTail tail = new EchoTail(reply, seek, index);
	//		success = client.sendTail(tail);
	//		// 关闭
	//		client.close();
	//
	//		Logger.debug(this, "launch", success, "send %s, length:%d, blocks:%d",
	//				path, length, index);
	//
	//		return useful(success);
	//	}


	//	/**
	//	 * 快速上传数据块文件
	//	 * @param path 磁盘文件路径
	//	 * @return 上传成功返回真，否则假
	//	 */
	//	private boolean castUpload(String path) {
	//		// 确认文件长度
	//		long length = AccessTrustor.length(path);
	//		// 超级传输数据尺寸，单次传输不超过512K为宜
	//		int maxsize = EchoClient.getLargeMTU();
	//
	//		Logger.debug(this, "castUpload", "%s filesize is %d, maxsize:%d",
	//				path, length, maxsize);
	//
	//		//		// 建立数据发送器，指定数据流模式和传输尺寸
	//		//		Cabin endpoint = getCommandSource();
	//		//		Address local = getLocal().getAddress();
	//		//		SiteLauncher launcher = super.getLauncher();
	//		//		// 如果当前节点是公网，且命令来源是公网地址，那么本地也是公网地址
	//		//		if (launcher.isGateway() && server.getAddress().isWideAddress()) {
	//		//			GatewaySite site = (GatewaySite)launcher.getSite();
	//		//			local = site.getPublic().getAddress();
	//		//		}
	//		//
	//		//		// 生成报头
	//		//		CastHelp help = new CastHelp(server.getAddress(), local);
	//
	//		// 建立基础参数
	//		CastHelp help = createCastHelp();
	//		EchoHead head = new EchoHead(Major.SUCCESSFUL_FILE, length, help);
	//
	//		// 采用UDP模式
	//		Cabin endpoint = getCommandSource();
	//		EchoClient client = createEchoClient(endpoint, false);
	//		if (client == null) {
	//			Logger.error(this, "castUpload", "cannot be create EchoClient");
	//			return false;
	//		}
	//
	//		// 发送报头
	//		CastFlag flag = client.sendCast(head);
	//		if(flag == null){
	//			Logger.error(this, "castUpload", "cannot be send EchoHead");
	//			return false; 
	//		}
	//		// 销毁
	//		client.destroy();
	//
	//		// 建立实例，乱序发送数据包，绑定的地址由系统分配
	//		CastClient caster = new CastClient(flag);
	//		boolean success = caster.bind();
	//		if (!success) {
	//			Logger.error(this, "castUpload", "cannot be bind");
	//			return false;
	//		}
	//
	//		// 发送HELO命令，确定握手成功。
	//		success = caster.hello();
	//		Logger.note(this, "castUpload", success, "cast hello command");
	//
	//		// 读数据块内容，发送数据块
	//		long seek = 0L;
	//		// 以HELO命令成功前提下，读出数据，直到完成 
	//		while(success && seek < length) {
	//			// 以2M为一个单元，读取磁盘上的数据
	//			int len = (int) Laxkit.limit(seek, length, maxsize);
	//			// 从JNI接口读一段数据
	//			byte[] b = AccessTrustor.read(path, seek, len);
	//			if (b == null || b.length != len) {
	//				Logger.error(this, "castUpload", "b is null, or b.length != %d", len);
	//				break;
	//			}
	//
	//			// 发送数据包
	//			success = caster.send(b, 0, len);
	//			if(!success){
	//				Logger.error(this, "castUpload", "cannot be send data");
	//				break;
	//			}
	//			// 下一段
	//			seek += len;
	//		}
	//		// 通知服务器端退出，和关闭套接字
	//		caster.exit();
	//		caster.close();
	//
	//		// 判断成功
	//		success = (seek == length);
	//		// 发送报尾
	////		short reply = (success ? Major.SUCCESSFUL : Major.FAULTED);
	//		EchoTail tail = new EchoTail(success, seek, caster.getSubPackets());
	//
	//		// 再次打开
	//		client = createEchoClient(endpoint, false);
	//		if (client == null) {
	//			Logger.error(this, "castUpload", "cannot be create EchoClient");
	//			return false;
	//		}
	//
	//		// 发送快速RPC关闭
	//		success = client.sendExit(tail);
	//		// 关闭
	//		client.destroy();
	//
	//		Logger.debug(this, "castUpload", success, "send %s, length:%d, blocks:%d",
	//				path, length, caster.getSubPackets());
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		Logger.debug(this, "destroy", "%d usedtime:%d", getInvokerId(), getRunTime());
		super.destroy();
	}
}
