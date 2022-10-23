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
 * 日志发送客户端。<br>
 * 负责日志本地保存，网络发送(写入本地或者发送到日志服务器)，和接收应答数据。<br>
 * 
 * 日志发送采用FIXP KEEP UDP方式<br>
 * 
 * @author scott.liang
 * @version 1.2 5/6/2013
 * @since laxcus 1.0
 */
public final class LogClient extends MutexThread { // VirtualThread {

	/** 日志文件后缀 **/
	private final static String suffix = ".log";

	/** 日志服务器地址 **/
	private SocketHost remote = new SocketHost(SocketTag.UDP);

	/** 日志发送客户端 **/
	private FixpPacketClient client = new FixpPacketClient();

	/** 日志文件 **/
	private File diskFile;

	/** 当前日期，默认是-1未定义 **/
	private int today;

	/** 日志保存缓冲区 **/
	private LogBuffer buff = new LogBuffer();

	/** 日志的时间格式 **/
	private SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

	/** 日志配置 **/
	protected LogConfigure configure = new LogConfigure();

	/** 日志打印接口 **/
	private LogPrinter printer;

	/** 静默统计时间 **/
	private long silentTime;

	/** 成员日志 **/
	private Map<Siger, LogBuffer> elements = new TreeMap<Siger, LogBuffer>();

	/**
	 * 构造日志客户端。
	 */
	public LogClient() {
		super();
		today = -1;
		silentTime = System.currentTimeMillis();
		// FIXP子包持续时间:10秒(keep udp timeout)
		client.setSubPacketTimeout(10000);
		// 接收数据包超时，30秒
		client.setReceiveTimeout(30000);
	}

	/**
	 * FIXP客户端套接字子包超时时间，即"KEEP UDP TIMEOUT"的时间
	 * 
	 * @param ms 毫秒
	 */
	public void setSubPacketTimeout(int ms) {
		client.setSubPacketTimeout(ms);
	}

	/**
	 * FIXP客户端套接字接收数据包超时
	 * 
	 * @param ms
	 */
	public void setReceiveTimeout(int ms) {
		client.setReceiveTimeout(ms);
	}

	/**
	 * 设置日志配置
	 * @param e
	 */
	public void setLogConfigure(LogConfigure e) {
		configure = e;
		buff.ensure(configure.getBufferSize());
	}

	/**
	 * 返回日志配置
	 * @return
	 */
	public LogConfigure getLogConfigure() {
		return configure;
	}

	/**
	 * 设置日志打印接口(如果没有指定打印接口，默认是终端打印)
	 * 
	 * @param e
	 */
	public void setLogPrinter(LogPrinter e) {
		printer = e;
	}

	/**
	 * 返回日志打印接口
	 * 
	 * @return
	 */
	public LogPrinter getLogPrinter() {
		return printer;
	}

	//	/**
	//	 * 关闭SOCKET连接
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
	 * 建立本地日志目录
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
	 * 定义日志文件名
	 * @return boolean
	 */
	private boolean choose() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String today = df.format(new java.util.Date());
		for (int index = 1; index < Integer.MAX_VALUE; index++) {
			String name = String.format("%s(%d)%s", today, index, LogClient.suffix);
			File file = new File(configure.getDirectory(), name);
			if (!file.exists()) {
				diskFile = file;
				return true;
			}
		}
		return false;
	}

	//	/**
	//	 * 执行UDP绑定本地，然后连接到指定端口
	//	 * @param ip
	//	 * @param port
	//	 * @throws SocketException
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
	//		}
	//		return false;
	//	}

	/**
	 * 以通配符方式，绑定本地IP，端口由系统分配
	 * @return 成功返回真，否则假
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
	
//	/**
//	 * 判断静默超时，两种可能：
//	 * 1. 当前时间大于上次的记录时间，并且超时。这是正常行为
//	 * 2. 当前时间小于上次记录时间，并且超时。这不是正常行为，发生了用户修改时间
//	 * @param ms
//	 * @return
//	 */
//	private boolean isSilentTimeout(long ms) {
//		long nowTime = System.currentTimeMillis();
//		if ((silentTime > nowTime && silentTime - nowTime >= ms)
//				|| (nowTime > silentTime && nowTime - silentTime >= ms)) {
//			return true;
//		}
//		return false;
//	}

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

		// 不成功，关闭SOCKET，不要发送“exit”指令
		if (!success) {
			close();
		}

		silentTime = System.currentTimeMillis();
	}

	/**
	 * 加载日志服务
	 * @param config 日志配置
	 * @param endpoint 日志服务器地址。如果空值，日志在本地处理。
	 * @return
	 */
	public boolean load(LogConfigure config, SiteHost endpoint) {		
		boolean success = false;
		if (config.isNotSend() || config.isSendToBuffer()) {
			setLogConfigure(config);
			success = true;
		} else if (config.isSendToDisk()) {
			setLogConfigure(config);
			createDirectory(config.getDirectory());
			success = choose();
		} else if(config.isSendToServer()) {
			// 如果是服务器模式，必须有目标服务器地址
			if (endpoint == null) {
				Logger.error("LogClient.load, log service site is null!");
				return false;
			}
			remote = endpoint.getPacketHost();
			success = bind();
			setLogConfigure(config);
		} else {
			throw new IllegalArgumentException("illegal log mode type!");
		}

		// 超时
		client.setReceiveTimeout(config.getReceiveTimeout());
		client.setSubPacketTimeout(config.getSubPacketTimeout());

		// 设置日志输出间隔时间
		setSleepTime(config.getSendInterval());

		// 启动日志线程
		if (success) {
			success = start();
		}
		return success;
	}

	/**
	 * 关闭线程，停止日志服务
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
					Logger.warning(this, "receiveRubbish", "rubbish packet %s, from %s", 
							resp.getMark(), resp.getRemote());
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
	 * 发送日志
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
			Logger.error(e);
			return;
		}

		Mark mark = new Mark(Ask.NOTIFY, Ask.FLUSH_LOG);
		Packet request = new Packet(remote, mark);
		request.setData(b, 0, b.length);

		long startTime = System.currentTimeMillis();

		boolean fatal = false;
		Packet resp = null;
		try {
			resp = client.batch(request);
		} catch (SocketTimeoutException e) {
			pushText(log); 
			Logger.error(e); // 记录！
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
			Logger.error(this, "sendToServer", "cannot be send to %s, timeout:%d ms, collect garbage!", remote, usedTime);
			// 不关闭SOCKET，只是接收剩余的数据，然后延时再试！
			receiveRubbish();
		}
	}

	//	/**
	//	 * 发送日志
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
	//		Mark mark = new Mark(Ask.NOTIFY, Ask.ADD_LOG);
	//		Packet request = new Packet(remote, mark);
	//		request.setData(b, 0, b.length);
	//
	//		long startTime = System.currentTimeMillis();
	//		
	//		Packet resp = null;
	//		try {
	//			resp = client.batch(request);
	//			// 通知服务器，解决加密密钥。下次再用时重新启动
	//			client.detach();
	////		} catch (FixpProtocolException e) {
	////			// 协议错误，接收剩余包，重新连接
	//			
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
	//			// 发生错误，关闭SOCKET
	//			Logger.error(this, "sendToServer", "cannot send to %s, timeout:%d ms, retry!", remote, usedTime);
	//			// 关闭SOCKET，不要发送“exit”命令
	//			close(false);
	//			// 如果缓存超过1M，证明持续发送失败，为避免内存溢出，删除内存中的日志
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
	 * 日志写入磁盘
	 * @param log
	 */
	private void writeToDisk(String log) {
		if (diskFile == null) {
			boolean success = choose();
			if (!success) return;
		}
		// 日志转换成UTF8编码
		byte[] b = toUTF8(log);
		if (b == null || b.length == 0) return;
		// 日志以追加方式写入磁盘
		try {
			FileOutputStream out = new FileOutputStream(diskFile, true);
			out.write(b, 0, b.length);
			out.close();
		} catch (IOException exp) {
			exp.printStackTrace();
		} catch(Throwable exp) {
			exp.printStackTrace();
		}

		// 如果日志文件尺寸溢出，建立新文件
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
	 * 日志写入磁盘，或者发到日志服务器。
	 */
	private void flushLog() {
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
	 * 发送某个用户的日志
	 * @param siger 用户签名
	 * @param log 日志内容
	 */
	private void sendToServer(Siger siger, String log) {
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
			Logger.error(e);
			return;
		}

		Mark mark = new Mark(Ask.NOTIFY, Ask.FLUSH_MEMBER_LOG);
		Packet request = new Packet(remote, mark);
		request.addMessage(MessageKey.SINGER , siger.binary());
		request.setData(b, 0, b.length);

		long startTime = System.currentTimeMillis();

		boolean fatal = false;
		Packet resp = null;
		try {
			resp = client.batch(request);
		} catch (SocketTimeoutException e) {
			pushText(log); 
			Logger.error(e); // 记录！
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
			Logger.error(this, "sendToServer", "cannot be send to %s, timeout:%d ms, collect garbage!", remote, usedTime);
			// 不关闭SOCKET，只是接收剩余的数据，然后延时再试！
			receiveRubbish();
		}
	}

	/**
	 * 输出内容
	 */
	private void flushElementLogs() {
		// 锁定
		super.lockSingle();
		try {
			Iterator<Map.Entry<Siger, LogBuffer>> iterator = elements.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, LogBuffer> entry = iterator.next();
				Siger siger = entry.getKey();
				LogBuffer buff = entry.getValue();
				String log = buff.remove();

				// 只处理发送给服务器的
				if (configure.isSendToServer()) {
					sendToServer(siger, log);
				}
			}
			// 清除全部
			elements.clear();
		} catch (Throwable e) {

		} finally {
			super.unlockSingle();
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
	 * 推送一行日志 <br><br>
	 * 
	 * 推送日志的处理过程：<br>
	 * 1. 如果要求打印在终端，或者打印到接口时，输入到指定位置。<br>
	 * 2. 检查传输模式，如果不传输而忽略退出。<br>
	 * 3. 如果传输到缓存，当缓存“满”时，自动删除。<br>
	 * 4. 如果传输到本地文件/服务器，当缓存“满”后，写入本地磁盘文件，或者发送到服务器。<br><br>
	 * 
	 * @param level 日志级别
	 * @param log 日志内容。
	 */
	void push(int level, String log) {
		String text = String.format("%s: %s %s\r\n", LogLevel.getText(level), now(), log);
		push(text);
	}

	//	/**
	//	 * 推送一行日志
	//	 * 
	//	 * @param level 日志级别
	//	 * @param signer 用户签名
	//	 * @param log 日志
	//	 */
	//	void push(int level, Siger signer, String log) {
	//		String text = String.format("%s: %s {%s} %s\r\n",
	//				LogLevel.getText(level), now(), signer, log);
	//		push(text);
	//	}

	/**
	 * 推送一行日志
	 * 
	 * @param signer 签名
	 * @param level 级别
	 * @param log 日志
	 */
	void push(Siger signer, int level, String log) {
		String text = String.format("%s: %s %s\r\n", LogLevel.getText(level), now(), log);

		// 锁定和写入
		super.lockSingle();
		try {
			LogBuffer buf = elements.get(signer);
			if (buf == null) {
				buf = new LogBuffer();
				elements.put(signer, buf);
			}
			buf.append(text);
		} catch (Throwable e) {

		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 推送一行日志 <br><br>
	 * 
	 * 推送日志的处理过程：<br>
	 * 1. 如果要求打印在终端，或者打印到接口时，输入到指定位置。<br>
	 * 2. 检查传输模式，如果不传输而忽略退出。<br>
	 * 3. 如果传输到缓存，当缓存“满”时，自动删除。<br>
	 * 4. 如果传输到本地文件/服务器，当缓存“满”后，写入本地磁盘文件，或者发送到服务器。<br><br>
	 * 
	 * @param log 日志文本
	 */
	private void push(String log) {
		// 指定打印接口时，输出到打印接口
		if (printer != null) {
			printer.print(log);
		}
		// 要求控制台打印，输出到控制台
		if (configure.isConsolePrint()) {
			System.out.print(log);
		}
		// 如果需要发送日志
		if (!configure.isNotSend()) {
			pushText(log);
		}
	}

	/**
	 * 保存数据
	 * @param text
	 */
	private void pushText(String text) {
		// 保存日志
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
	 * 输出日志到终端
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
		// 否则是返回成功
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
			// 输出日志
			flushLog();
			// 输出成员日志
			flushElementLogs();
			// 延时
			sleep();
		}
		Logger.info(this, "process", "exit");
		flushLog();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 关闭SOCKET连接
		close();
	}
}