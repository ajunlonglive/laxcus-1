/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import java.io.*;
import java.util.*;

import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.thread.*;
import com.laxcus.util.net.*;

/**
 * FIXP数据包任务分析和处理器。
 * 
 * @author scott.liang
 * @version 1.1 7/3/2010
 * @since laxcus 1.0
 */
final class PacketTask extends MutexThread {

	/** FIXP数据包辅助器 **/
	private FixpPacketHelper helper;

	/** KEEP UDP的子包集合 **/
	private ArrayList<PacketBucket> buckets = new ArrayList<PacketBucket>(50);

	/** 正常的UDP包集合 **/
	private ArrayList<Packet> packets = new ArrayList<Packet>(50);

	/**
	 * 构造FIXP包任务，指定FIXP数据包辅助器
	 * @param e FIXP数据包辅助器
	 */
	public PacketTask(FixpPacketHelper e) {
		super();
		helper = e;
	}

	/**
	 * 设置FIXP数据包辅助器
	 * @param e
	 */
	public void setHelper(FixpPacketHelper e) {
		helper = e;
	}

	/**
	 * 保存一个普通的FIXP UDP数据包
	 * @param packet FIXP数据包
	 */
	public boolean add(Packet packet) {
		boolean success = false;
		boolean empty = false;
		// 锁定
		super.lockSingle();
		try {
			empty = packets.isEmpty();
			success = packets.add(packet);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 唤醒线程
		if (empty && success) {
			wakeup();
		}
		return success;
	}

	/**
	 * 弹出一个普通的数据包
	 * @return Packet实例
	 */
	private Packet pollPacket() {
		super.lockSingle();
		try {
			if (packets.size() > 0) {
				return packets.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 保存一个子包集
	 * @param bucket FIXP子包集
	 */
	public boolean add(PacketBucket bucket) {
		boolean success = false;
		boolean empty = false;
		super.lockSingle();
		try {
			empty = buckets.isEmpty();
			success = buckets.add(bucket);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 唤醒线程
		if (empty && success) {
			wakeup();
		}
		return success;
	}

	/**
	 * 弹出一个子包集合
	 * @return PacketBucket实例
	 */
	private PacketBucket pollBucket() {
		super.lockSingle();
		try {
			if (buckets.size() > 0) {
				return buckets.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 处理普通的UDP数据包
	 * @throws IOException
	 */
	private int invokePacket() throws IOException {
		int size = packets.size();
		for (int i = 0; i < size; i++) {
			Packet packet = pollPacket();
			if (packet == null) {
				continue;			
			}

			Mark mark = packet.getMark();
			Packet resp = null;
			
			// 单包远程调用
			if (Assert.isVisit(mark)) {
				resp = helper.callVisit(packet);
			}
			// 首先处理"RPC/异步RPC"的调用，它是请求命令。
			else if (Assert.isRPCall(mark)) {
				resp = helper.callRPC(packet); // 调用RPC实例
			} else {
				// 交给数据包调用接口处理
				resp = helper.callMethod(packet);
			}
			// 可能出错或者不需要返回结果时...
			if (resp == null) {
				continue;
			}

			// 将处理结果反馈给调用端
			SocketHost remote = packet.getRemote();
			helper.send(remote, resp);
		}
		return size;
	}

	/**
	 * 处理KEEP UDP的子包集合
	 * @throws IOException
	 */
	private int invokeBucket() throws IOException {
		int size = buckets.size();
		for (int i = 0; i < size; i++) {
			PacketBucket bucket = pollBucket();
			if (bucket == null) {
				continue;
			}

			int packetId = bucket.getPacketId();

			// 将多个子包合并为一个数据包
			Packet packet = bucket.compose();
			Mark mark = packet.getMark();

			Packet resp = null;
			// 首先处理"RPC/异步RPC"的调用，如果不是交给UDP数据包接口处理。
			if (Assert.isRPCall(mark)) {
				//				Logger.debug(this, "invokeBacket", "处理来自 %s 的RPC数据！", packet.getRemote());

				resp = helper.callRPC(packet); // RPC

				//				Logger.debug(this, "invokeBacket", "完成来自 %s 的RPC数据处理！应答包：%s", packet.getRemote(), (resp != null ? "有效" : "无效"));

			} else {
				resp = helper.callMethod(packet); //调用方法实例
			}
			// 可能出错或者不需要返回结果时...
			if (resp == null) {
				continue;
			}

			// 处理结果反馈给调用端
			SocketHost remote = bucket.getRemote();
			helper.reply(remote, resp, packetId);
		}
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/**
	 * 处理普通的数据包和子包集合
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while (!isInterrupted()) {
			try {
				// 处理数据包
				int count = invokeBucket();
				count += invokePacket();
				// 没有处理，延时
				if (count == 0) {
					delay(1000);
				}
			} catch (IOException e) {
				Logger.error(e);
			} catch (Throwable t) {
				Logger.fatal(t);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 要求辅助器释放自己
		helper.release(this);
	}

}