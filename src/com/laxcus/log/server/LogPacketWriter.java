/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.server;

import java.io.*;
import java.text.*;
import java.util.*;

import com.laxcus.access.util.*;
import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.util.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.net.*;

/**
 * 日志磁盘写入器
 * 
 * @author scott.liang
 * @version 1.0 5/15/2009
 * @since laxcus 1.0
 */
final class LogPacketWriter implements PacketInvoker {

	/** 磁盘日志文件的最大尺寸, 10M **/
	private final static long MAX_LOGSIZE = 10 * 1024 * 1024;

	/** 日志基础目录 */
	private File root;

	/** 当天日期，发生变化就更新日志 */
	private int scale;

	/**
	 * 构造日志磁盘写入器
	 */
	private LogPacketWriter() {
		super();
		scale = SimpleDate.format();
	}

	/**
	 * 构造日志磁盘写入器，指定日志写入目录
	 * @param root
	 */
	public LogPacketWriter(File root) {
		this();
		setRoot(root);
	}

	/**
	 * 设置基础目录
	 * @param s
	 */
	public void setRoot(File s) {
		root = s;
	}

	/**
	 * 返回基础目录
	 * @return
	 */
	public File getRoot() {
		return root;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.PacketInvoker#invoke(com.laxcus.fixp.Packet)
	 */
	@Override
	public Packet invoke(Packet packet) {
		SocketHost remote = packet.getRemote();
		Mark mark = packet.getMark();
		
		// 只接受增加日志的日志，其它一概不支持
		short reply = Answer.UNSUPPORT;
		if (Assert.isFlushLog(mark)) {
			byte[] logs = packet.getData();
			// 判断日志非空
			boolean success = (!Laxkit.isEmpty(logs));
			// 解压日志和判断日志非空
			if (success) {
				logs = ungzip(logs);
				success = (!Laxkit.isEmpty(logs));
			}
			// 写日志到磁盘
			if (success) {
				success = writeLog(remote, logs);
			}
			// 返回应答包
			reply = (success ? Answer.OKAY : Answer.SERVER_ERROR);
		} else if(Assert.isFlushElmentLog(mark)) {
			// 记录
			byte[] b = packet.findRaw(MessageKey.SINGER);
			byte[] logs = packet.getData();
			
			// 判断日志非空
			boolean success = (b != null && !Laxkit.isEmpty(logs));
			// 解压日志和判断日志非空
			if (success) {
				logs = ungzip(logs);
				success = (!Laxkit.isEmpty(logs));
			}
			// 写日志到磁盘
			if (success) {
				Siger singer = new Siger(b);
				success = writeLog(remote, singer, logs);
			}
			// 返回应答包
			reply = (success ? Answer.OKAY : Answer.SERVER_ERROR);
		}
		
		Packet resp = new Packet(remote, reply);
		resp.addMessage(MessageKey.SPEAK, "reply log");
		return resp;
	}
	
	/**
	 * 对数据进行解压缩
	 * @param logs 压缩数据
	 * @return 返回解压的数据
	 */
	private byte[] ungzip(byte[] logs) {
		try {
			return Deflator.gzip(logs, 0, logs.length);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据日志来源的IP地址建立对应的目录
	 * @param source
	 * @return
	 */
	private File mkdirs(SocketHost source) {
		String name = String.format("%s_%d", source.getAddress(), source.getPort());
		File dir = new File(root, name);
		// 目录存在即返回
		if (dir.exists() && dir.isDirectory()) {
			return dir;
		}
		// 目录不存在，建立新目录
		boolean success = dir.mkdirs();
		return (success ? dir : null);
	}
	
	/**
	 * 根据日志来源的IP地址建立对应的目录
	 * @param source
	 * @param singer
	 * @return 返回实例
	 */
	private File mkdirs(SocketHost source, Siger singer) {
		String name = String.format("%s_%d", source.getAddress(), source.getPort());
		File dir = new File(root, name);
		dir = new File(dir, singer.toString().toLowerCase());
		// 目录存在即返回
		if (dir.exists() && dir.isDirectory()) {
			return dir;
		}
		// 目录不存在，建立新目录
		boolean success = dir.mkdirs();
		return (success ? dir : null);
	}

	/**
	 * 根据日志来源的网络地址生成文件
	 * @param source
	 * @return
	 */
	private File mkfile(SocketHost source) {
		// 建立目录
		File logPath = mkdirs(source);
		if (logPath == null) {
			return null;
		}

		// 根据当前日期建立日志文件
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date date = SimpleDate.format(scale);
		String today = df.format(date);

		for (int index = 1; index < Integer.MAX_VALUE; index++) {
			String suffix = String.format("%s(%d).log", today, index);
			File file = new File(logPath, suffix);
			//返回条件：1，文件不存在. 2，文件长度在范围内
			if (!file.exists()) {
				return file;
			} else if (file.length() < LogPacketWriter.MAX_LOGSIZE) {
				return file;
			}
		}
		// 返回空值，可能性几乎没有
		return null;
	}

	/**
	 * 根据日志来源的网络地址生成文件
	 * @param source
	 * @return
	 */
	private File mkfile(SocketHost source, Siger siger) {
		// 建立目录
		File logPath = mkdirs(source, siger);
		if (logPath == null) {
			return null;
		}

		// 根据当前日期建立日志文件
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date date = SimpleDate.format(scale);
		String today = df.format(date);

		for (int index = 1; index < Integer.MAX_VALUE; index++) {
			//	String suffix = String.format("%s_%s(%d).log", siger, today, index);
			String suffix = String.format("%s(%d).log", today, index);
			File file = new File(logPath, suffix);
			//返回条件：1，文件不存在. 2，文件长度在范围内
			if (!file.exists()) {
				return file;
			} else if (file.length() < LogPacketWriter.MAX_LOGSIZE) {
				return file;
			}
		}
		// 返回空值，可能性几乎没有
		return null;
	}
	
	/**
	 * 写日志到磁盘文件
	 * @param source 网络来源地址
	 * @param logs 日志文本
	 * @return 成功返回真，否则假
	 */
	private boolean writeLog(SocketHost source, byte[] logs) {
		boolean success = false;
		File filename = mkfile(source);
		if (filename == null) {
			return false;
		}
		try {
			FileOutputStream writer = new FileOutputStream(filename, true);
			writer.write(logs, 0, logs.length);
			writer.flush();
			writer.close();
			success = true;
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// 检查日期，如果不一致时以新的为准，后面将建立新日志文件
		int day = SimpleDate.format();
		if (scale != day) {
			scale = day;
		}

		return success;
	}
	
	/**
	 * 写日志到磁盘文件
	 * @param source 网络来源地址
	 * @param singer 用户签名
	 * @param logs 日志文本
	 * @return 成功返回真，否则假
	 */
	private boolean writeLog(SocketHost source, Siger siger, byte[] logs) {
		boolean success = false;
		File filename = mkfile(source, siger);
		if (filename == null) {
			return false;
		}
		try {
			FileOutputStream writer = new FileOutputStream(filename, true);
			writer.write(logs, 0, logs.length);
			writer.flush();
			writer.close();
			success = true;
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// 检查日期，如果不一致时以新的为准，后面将建立新日志文件
		int day = SimpleDate.format();
		if (scale != day) {
			scale = day;
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.PacketInvoker#setPacketTransmitter(com.laxcus.invoke.PacketTransmitter)
	 */
	@Override
	public void setPacketTransmitter(PacketTransmitter e) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.PacketInvoker#getPacketTransmitter()
	 */
	@Override
	public PacketTransmitter getPacketTransmitter() {
		// TODO Auto-generated method stub
		return null;
	}

}