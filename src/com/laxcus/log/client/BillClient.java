/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved. 
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import com.laxcus.access.util.*;
import com.laxcus.fixp.*;
import com.laxcus.fixp.client.*;
import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 操作发送客户端。<br>
 * 负责操作本地保存，网络发送(写入本地或者发送到操作服务器)，和接收应答数据。<br>
 * 
 * 操作发送采用FIXP KEEP UDP方式<br>
 * 
 * @author scott.liang
 * @version 1.0 10/13/2022
 * @since laxcus 1.0
 */
public final class BillClient extends VirtualThread {

	/** 操作文件后缀 **/
	private final static String suffix = ".bil";

	/** 操作服务器地址 **/
	private SocketHost remote = new SocketHost(SocketTag.UDP);

	/** 操作发送客户端 **/
	private FixpPacketClient client = new FixpPacketClient();

	/** 操作文件 **/
	private File diskFile;

	/** 当前日期，默认是-1未定义 **/
	private int today;

	/** 操作保存缓冲区 **/
	private BillBuffer buff = new BillBuffer();

	/** 操作的时间格式 **/
	private SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

	/** 操作配置 **/
	protected BillConfigure configure = new BillConfigure();

	/** 静默记录时间  **/
	private long silentTime;

	/**
	 * 构造操作客户端。
	 */
	public BillClient() {
		super();
		today = -1;
		silentTime = System.currentTimeMillis();
		// FIXP子包持续时间:10秒(keep udp timeout)
		client.setSubPacketTimeout(10000);
		// 接收数据包超时，30秒
		client.setReceiveTimeout(30000);
	}

	/**
	 * 设置操作配置
	 * @param e
	 */
	public void setBillConfigure(BillConfigure e) {
		configure = e;
		buff.ensure(configure.getBufferSize());
	}

	/**
	 * 返回操作配置
	 * @return
	 */
	public BillConfigure getBillConfigure() {
		return configure;
	}

//	/**
//	 * 关闭SOCKET连接，之前发出“exit”命令
//	 */
//	private void close(boolean exit) {
//		client.close(exit);
//	}

	/**
	 * 关闭SOCKET连接
	 */
	private void close() {
		client.close();
	}
	
	/**
	 * 判断SOCKET已经关闭
	 * @return 返回真或者假
	 */
	private boolean isClosed() {
		return client.isClosed();
	}

	/**
	 * 建立本地操作目录
	 * @param path
	 */
	private boolean createDirectory(File path) {
		Logger.info(this, "createDirectory", "directory is '%s'", path);
		if (!path.exists() || !path.isDirectory()) {
			return path.mkdirs();
		}
		return true;
	}

	/**
	 * 定义操作文件名
	 * @return boolean
	 */
	private boolean choose() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String today = df.format(new java.util.Date());
		for (int index = 1; index < Integer.MAX_VALUE; index++) {
			String name = String.format("%s(%d)%s", today, index, BillClient.suffix);
			File file = new File(configure.getDirectory(), name);
			if (!file.exists()) {
				diskFile = file;
				return true;
			}
		}
		return false;
	}

	//	/**
	//	 * 执行UDP绑定本地，然后连接到指定服务器
	//	 * 成功返回真，否则假
	//	 */
	//	private boolean bind() {
	//		try {
	//			boolean success = client.bind();
	//			if (success) {
	//				// client.connect(remote);
	//				return true;
	//			}
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		}
	//		return false;
	//	}

	/**
	 * 以SOCKET UDP方式，绑定本地，端口由系统分配
	 * 成功返回真，否则假
	 */
	private boolean bind() {
		try {
			boolean success = client.bind();
			if (success) {
				return true;
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return false;
	}
	
	/**
	 * 判断超时 <br>
	 * 1. 当前时间大于上次的记录时间，并且超时。这是正常行为。<br>
	 * 2. 当前时间小于上次记录时间，不考虑超时间隔，也算是超时。这不是正常行为，发生了用户修改时间。<br><br>
	 * 
	 * @param silentTime 上次记录时间
	 * @param gap 超时间隔
	 * @return 返回真或者假
	 */
	private boolean isTimeout(long gap) {
		long nowTime = System.currentTimeMillis();
		// 1. 当前时间大于上次记录时间，并且超时
		if (nowTime > silentTime && nowTime - silentTime >= gap) {
			return true;
		}
		// 2. 当前时间小于上次记录时间，不考虑超时间隔，也是超时
		else if (silentTime > nowTime && silentTime - nowTime >= gap) {
			return true;
		}
		return false;
	}

	/**
	 * 空操作，保持SOCKET有效。超过20秒，发送一个空包
	 */
	private void sendEmptyToServer() {
		// 没有超时则忽略
		if (!isTimeout(20000)) {
			return;
		}

		// 如果已经关闭，重新绑定本地地址
		if (isClosed()) {
			boolean success = bind();
			if (!success) {
				close();
				return;
			}
		}

		// 判断处于连接状态，发送空包
		boolean success = false;
		try {
			client.empty(remote);
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 不成功，忽略!
		if (!success) {
			close();
		}

		silentTime = System.currentTimeMillis();
	}

	/**
	 * 加载操作服务
	 * @param config 操作配置
	 * @param endpoint 操作服务器地址。如果空值，操作在本地处理。
	 * @return
	 */
	public boolean load(BillConfigure config, SiteHost endpoint) {		
		boolean success = false;
		if (config.isNotSend() || config.isSendToBuffer()) {
			setBillConfigure(config);
			success = true;
		} else if (config.isSendToDisk()) {
			setBillConfigure(config);
			createDirectory(config.getDirectory());
			success = choose();
		} else if(config.isSendToServer()) {
			// 如果是服务器模式，必须有目标服务器地址
			if (endpoint == null) {
				Logger.error("BillClient.load, bill service site is null!");
				return false;
			}
			remote = endpoint.getPacketHost();
			success = bind();
			setBillConfigure(config);
		} else {
			throw new IllegalArgumentException("illegal bill mode type!");
		}

		// 超时
		client.setReceiveTimeout(config.getReceiveTimeout());
		client.setSubPacketTimeout(config.getSubPacketTimeout());

		// 设置操作输出间隔时间
		setSleepTime(config.getSendInterval());

		// 启动操作线程
		if (success) {
			success = start();
		}
		return success;
	}

	/**
	 * 关闭线程，停止操作服务
	 */
	public void stopService() {
		stop();
	}

	/**
	 * 在发生错误后，接收剩余的包，这些包被丢弃
	 */
	private void receiveRubbish() {
		// 最少10秒延时
		int ms = configure.getSendInterval() * 1000;
		ms = (ms >= 10000 ? ms : 10000);
		// 接收可能存在的剩余包
		try {
			client.setSoTimeout(ms);
			// 在10秒钟内，收集剩余的包
			while (true) {
				Packet resp = client.receive();
				if (resp != null) {
					Logger.warning(this, "receiveRubbish", "rubbish packet %s, from %s", resp.getMark(), resp.getRemote());
				}
			}
		} catch (IOException e) {

		}
		// 重置为指定超时时间
		try {
			client.resetDefaultSoTimeout();
		} catch (IOException e) {

		}
	}

	/**
	 * 发送操作
	 * @param log
	 */
	private void sendToServer(String log) {
		// 如果已经关闭，重新绑定本地地址
		if (isClosed()) {
			boolean success = bind();
			if (!success) return;
		}

		// 指定使用UTF8编码
		byte[] b = toUTF8(log);
		if (Laxkit.isEmpty(b)) {
			return;
		}

		// 对数据进行GZIP压缩
		try {
			b = Inflator.gzip(b, 0, b.length);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Mark cmd = new Mark(Ask.NOTIFY, Ask.FLUSH_BILL);
		Packet request = new Packet(remote, cmd);
		request.setData(b, 0, b.length);

		// 开始时间
		long startTime = System.currentTimeMillis();

		boolean fatal = false;
		Packet resp = null;
		try {
			resp = client.batch(request);
		} catch (SocketTimeoutException e) {
			pushText(log); // 重新保存
			Logger.error(e);
		} catch (IOException e) {
			fatal = true;
			pushText(log); // 重新保存
			Logger.error(e);
		} catch (Throwable e) {
			fatal = true;
			pushText(log); // 重新保存
			Logger.fatal(e);
		}

		long usedTime = System.currentTimeMillis() - startTime;
		// 出错
		if (fatal) {
			// 发生错误，关闭SOCKET
			Logger.fatal(this, "sendToServer", "send to %s, failed! timeout:%d ms, close socket! retry!", remote, usedTime);
			// 关闭SOCKET，不要发送“exit”命令。
			close();
			// 如果缓存超过1M，证明持续发送失败，为避免内存溢出，删除内存中的操作
			if (buff.length() >= 0x100000) buff.clear();
			return ;
		}

		// 判断收到应答包
		boolean success = (resp != null);
		if (success) {
			success = (resp.getMark().getAnswer() == Answer.OKAY);
		}
		// 以上不成功时，收集垃圾数据，准备下次再发送数据包
		if (!success) {
			// 发生错误，关闭SOCKET
			Logger.error(this, "sendToServer", "cannot be send to %s, timeout:%d ms, collect garbage!", remote, usedTime);
			// 不关闭SOCKET，只是接收剩余的数据，然后延时再试！
			receiveRubbish();
		}
	}

	//	/**
	//	 * 发送操作
	//	 * @param log
	//	 */
	//	private void sendToServer(String log) {
	//		// 如果已经关闭，重新绑定本地地址
	//		if (isClosed()) {
	//			boolean success = bind();
	//			if (!success) return;
	//		}
	//		
	//		// 指定使用UTF8编码
	//		byte[] b = toUTF8(log);
	//		if (Laxkit.isEmpty(b)) {
	//			return;
	//		}
	//
	//		// 对数据进行GZIP压缩
	//		try {
	//			b = Inflator.gzip(b, 0, b.length);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//			return;
	//		}
	//
	//		Mark cmd = new Mark(Ask.NOTIFY, Ask.ADD_LOG);
	//		Packet request = new Packet(remote, cmd);
	//		request.setData(b, 0, b.length);
	//
	//		long startTime = System.currentTimeMillis();
	//		
	//		Packet resp = null;
	//		try {
	//			resp = client.batch(request);
	//			// 通知服务器，解除加密密钥
	//			client.detach();
	//		} catch (IOException e) {
	//			pushText(log); // 重新保存
	//			Logger.error(e);
	//		} catch (Throwable e) {
	//			pushText(log); // 重新保存
	//			Logger.fatal(e);
	//		}
	//
	//		// 判断收到应答包
	//		boolean success = (resp != null);
	//		if (success) {
	//			success = (resp.getMark().getAnswer() == Answer.OKAY);
	//		} else {
	//			long usedTime = System.currentTimeMillis() - startTime;
	//			
	//			// 发生错误，关闭SOCKET
	//			Logger.error(this, "sendToServer", "cannot send to %s, timeout:%d ms, retry!", remote, usedTime);
	//			// 关闭SOCKET，不要发送“exit”命令
	//			close(false);
	//			// 如果缓存超过1M，证明持续发送失败，为避免内存溢出，删除内存中的操作
	//			if (buff.length() >= 0x100000) buff.clear();
	//		}
	//	}

	/**
	 * 转化为UTF8编码
	 * @param log
	 * @return
	 */
	private byte[] toUTF8(String log) {
		try {
			return log.getBytes("UTF-8");
		} catch (UnsupportedEncodingException exp) {

		}
		return null;
	}

	/**
	 * 操作写入磁盘
	 * @param log
	 */
	private void writeToDisk(String log) {
		if (diskFile == null) {
			boolean success = choose();
			if (!success) return;
		}
		// 操作转换成UTF8编码
		byte[] b = toUTF8(log);
		if (b == null || b.length == 0) return;
		// 操作以追加方式写入磁盘
		try {
			FileOutputStream out = new FileOutputStream(diskFile, true);
			out.write(b, 0, b.length);
			out.close();
		} catch (IOException exp) {
			exp.printStackTrace();
		} catch(Throwable exp) {
			exp.printStackTrace();
		}

		// 如果操作文件尺寸溢出，建立新文件
		if (diskFile.length() >= configure.getFileSize()) {
			boolean success = choose();
			if (!success) {
				diskFile = null;
				return;
			}
		} else {
			// 如果日期变化也更新文件
			Calendar dar = Calendar.getInstance();
			dar.setTime(new java.util.Date(System.currentTimeMillis()));
			int day = dar.get(Calendar.DAY_OF_MONTH);
			if (today == -1) {
				today = day;
			} else if (today != day) {
				today = day;
				choose();
			}
		}
	}

	/**
	 * 操作写入磁盘，或者发到操作服务器。
	 */
	private void flush() {
		if (buff.isEmpty()) {
			// 如果是传给服务器时，发送一个空包，保证服务端的密钥激活和SOCKET有效
			if (configure.isSendToServer()) {
				sendEmptyToServer();
			}
			return;
		}

		String log = buff.remove();
		if (configure.isSendToDisk()) {
			writeToDisk(log); // 写入磁盘
		} else if (configure.isSendToServer()) {
			sendToServer(log); // 发送到服务器
		}
	}

	/**
	 * 当前时间
	 * @return
	 */
	private String now() {
		return style.format(new Date());
	}

	/**
	 * 推送一行操作 <br><br>
	 * 
	 * 推送操作的处理过程：<br>
	 * 1. 如果要求打印在终端，或者打印到接口时，输入到指定位置。<br>
	 * 2. 检查传输模式，如果不传输而忽略退出。<br>
	 * 3. 如果传输到缓存，当缓存“满”时，自动删除。<br>
	 * 4. 如果传输到本地文件/服务器，当缓存“满”后，写入本地磁盘文件，或者发送到服务器。<br><br>
	 * 
	 * @param level 操作级别
	 * @param log 操作内容。
	 */
	void push(int level, String log) {
		// 没有回车换行符，记录它
		if (!log.endsWith("\r\n")) {
			log = log + "\r\n";
		}

		String text = String.format("%s: %s %s", BillType.getText(level), now(), log);
		push(text);
	}

	//	/**
	//	 * 推送一行操作
	//	 * 
	//	 * @param level 操作级别
	//	 * @param signer 用户签名
	//	 * @param log 操作
	//	 */
	//	void push(int level, Siger signer, String log) {
	//		String text = String.format("%s: %s {%s} %s\r\n",
	//				BillLevel.getText(level), now(), signer, log);
	//		push(text);
	//	}

	/**
	 * 推送一行操作 <br><br>
	 * 
	 * 推送操作的处理过程：<br>
	 * 1. 如果要求打印在终端，或者打印到接口时，输入到指定位置。<br>
	 * 2. 检查传输模式，如果不传输而忽略退出。<br>
	 * 3. 如果传输到缓存，当缓存“满”时，自动删除。<br>
	 * 4. 如果传输到本地文件/服务器，当缓存“满”后，写入本地磁盘文件，或者发送到服务器。<br><br>
	 * 
	 * @param log 操作文本
	 */
	private void push(String log) {
		// 要求控制台打印，输出到控制台
		if (configure.isConsolePrint()) {
			System.out.print(log);
		}
		// 如果需要发送操作
		if (!configure.isNotSend()) {
			pushText(log);
		}
	}

	/**
	 * 保存数据
	 * @param text
	 */
	private void pushText(String text) {
		// 保存操作
		buff.append(text);
		// 如果是发送缓存模式，并且超出512K指定长度时，缓存将清空
		if (configure.isSendToBuffer()) {
			if (buff.length() >= 0x80000) buff.clear();
		} else if (buff.isFull()) {
			// 唤醒线程，输出到磁盘或者服务器
			wakeup();
		}
	}

	/**
	 * 输出操作到终端
	 */
	public void gushing() {
		String log = buff.remove();
		if (configure.isSendToBuffer()) {
			if (log != null && log.length() > 0) {
				System.out.println(log);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 如果是要求连接到服务器，绑定本地SOCKET端口
		if (configure.isSendToServer()) {
			return bind();
		}
		// 否则返回成功
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "send interval: %d s, into...", configure.getSendInterval());
		setSleepTime(configure.getSendInterval());
		// 循环
		while (!isInterrupted()) {
			flush();
			sleep();
		}
		Logger.info(this, "process", "exit");
		flush();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 关闭SOCKET连接，发送“exit”命令
		close();
	}
}