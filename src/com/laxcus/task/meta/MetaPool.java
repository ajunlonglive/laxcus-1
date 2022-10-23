/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.meta;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.meta.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.task.*;
import com.laxcus.task.mid.*;

/**
 * 元数据缓存管理池
 * 
 * @author scott.liang
 * @version 1.12 12/23/2015
 * @since laxcus 1.0
 */
public final class MetaPool extends MidPool implements MetaTrustor {

	/** 元数据缓存管理池，全局唯一。**/
	private static MetaPool selfHandle = new MetaPool();

	/**
	 * 返回元数据缓存管理池句柄
	 * @return MetaPool实例
	 */
	public static MetaPool getInstance() {
		VirtualPool.check("MetaPool.getInstance");
		return MetaPool.selfHandle;
	}

	/** 调用器管理池 **/
	private InvokerPool invokerPool;

	/** 元数据缓存标识 -> 全部元数据缓存 **/
	private TreeMap<MetaTag, MetaBuffer> stacks = new TreeMap<MetaTag, MetaBuffer>();

	/**
	 * 构造默认和私有的元数据缓存管理池
	 */
	private MetaPool() {
		super();
		// 一分钟检查一次
		setSleepTime(60);
	}

	/**
	 * 设置调用器管理池
	 * @param e 调用器管理池句柄
	 */
	public void setInvokerPool(InvokerPool e) {
		VirtualPool.check("setInvokerPool");
		invokerPool = e;
	}

	/**
	 * 删除缓存元数据
	 * @see com.laxcus.distribute.meta.MetaTrustor#remove(com.laxcus.distribute.meta.MetaTag)
	 */
	@Override
	public boolean remove(MetaTag tag) {
		boolean success = false;
		super.lockSingle();
		try {
			MetaBuffer buf = stacks.remove(tag);
			success = (buf != null);
			// 删除内存/磁盘数据
			if (success) {
				buf.delete();
			}
		} catch (Throwable e) {
			Logger.fatal(tag.getIssuer(), e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(tag.getIssuer(), this, "remove", success, "%s", tag);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.meta.MetaTrustor#contains(com.laxcus.distribute.meta.MetaTag)
	 */
	@Override
	public boolean contains(MetaTag tag) {
		super.lockMulti();
		try {
			return stacks.get(tag) != null;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 建立一个元数据缓存文件
	 * @param invokerId 调用器编号
	 * @return 返回文件名
	 */
	private File createFile(long invokerId) {
		do {
			long taskId = nextTaskId();
			String name = String.format("%x_%x.meta", invokerId, taskId);
			File file = new File(getRoot(), name);
			if (!file.exists()) {
				return file;
			}
		} while (true);
	}

	/**
	 * 检查异步调用器存在
	 * @param invokerId 调用器编号
	 * @throws TaskSecurityException
	 */
	private void available(long invokerId) throws TaskSecurityException {
		boolean success = invokerPool.hasInvoker(invokerId);
		// 如果不存在，弹出异常
		if (!success) {
			throw new TaskSecurityException("cannot be find invoker:%d", invokerId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.meta.MetaTrustor#write(com.laxcus.distribute.meta.MetaTag, byte[], int, int)
	 */
	@Override
	public boolean write(MetaTag tag, byte[] b, int off, int len) throws TaskException {
		EchoInvoker invoker = invokerPool.findInvoker(tag.getInvokerId());
		boolean success = (invoker != null);
		// 如果不存在，弹出异常
		if (!success) {
			throw new TaskSecurityException("illegal invoker:%s", tag);
		}

		// 如果磁盘模式，中间数据将写入硬盘
		File file = null;
		// 判断采用内存模式，并且有足够的内容余量
		boolean memory = invoker.isMemory();
		if (memory) {

		}
		// 内存不足时，将数据改写入硬盘
		if(!memory) {
			file = createFile(tag.getInvokerId());
		}

		super.lockSingle();
		try {
			MetaBuffer buff = stacks.get(tag);
			if (buff == null) {
				buff = new MetaBuffer(tag, file);
				stacks.put(buff.getTag(), buff);
			}
			// 写入数据
			success = buff.append(buff.isMemory(), b, off, len);
		} catch (Throwable e) {
			Logger.fatal(tag.getIssuer(), e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(tag.getIssuer(), this, "write", success, "%s", tag);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.meta.MetaTrustor#read(com.laxcus.distribute.meta.MetaTag)
	 */
	@Override
	public byte[] read(MetaTag tag) throws TaskException {
		// 调用器有效性检查
		available(tag.getInvokerId());

		// 共享锁定，读出数据
		super.lockMulti();
		try {
			MetaBuffer buff = stacks.get(tag);
			if (buff != null) {
				return buff.read();
			}
		} catch (Throwable e) {
			Logger.fatal(tag.getIssuer(), e);
		} finally {
			super.unlockMulti();
		}

		return null;
	}

	/**
	 * 删除一个调用器下面的全部元数据
	 * @param invokerId 调用器编号
	 * @return 返回被删除的元数据数目，没有是0
	 */
	public int remove(long invokerId) {
		ArrayList<MetaTag> array = new ArrayList<MetaTag>();

		super.lockSingle();
		try {
			Iterator<Map.Entry<MetaTag, MetaBuffer>> iterator = stacks.entrySet().iterator();
			// 检查调用器编号匹配
			while (iterator.hasNext()) {
				Map.Entry<MetaTag, MetaBuffer> entry = iterator.next();
				MetaTag tag = entry.getKey();
				if (tag.getInvokerId() == invokerId) {
					array.add(tag);
				}
			}
			// 删除缓存元数据
			for (MetaTag tag : array) {
				MetaBuffer buff = stacks.remove(tag);
				if (buff != null) {
					buff.delete();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "remove", "remove invoker: %d, tag size is %d", invokerId, array.size());

		return array.size();
	}

	/**
	 * 清除内存记录
	 */
	public void clear() {
		super.lockSingle();
		try {
			stacks.clear();
		} catch(Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 检查残留的垃圾数据 <br>
	 * 这种情况通常是防止调用器故障中断，但是内存数据没有没有释放的情况存在。
	 */
	private void check() {
		if (stacks.isEmpty()) {
			return;
		}

		ArrayList<MetaTag> array = new ArrayList<MetaTag>();
		//		InvokerPool pool = getLauncher().getInvokerPool();
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<MetaTag, MetaBuffer>> iterator = stacks.entrySet().iterator();
			// 检查调用器编号匹配
			while (iterator.hasNext()) {
				Map.Entry<MetaTag, MetaBuffer> entry = iterator.next();
				MetaTag tag = entry.getKey();
				// 如果调用器不存在
				boolean success = invokerPool.hasInvoker(tag.getInvokerId());
				if (!success) {
					array.add(tag);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		// 删除过期数据
		for (MetaTag tag : array) {
			remove(tag);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 必须由用户指定目录
		if (super.getRoot() == null) {
			Logger.error(this, "init", "cannot be set root directory");
			return false;
		}

		Logger.info(this, "init", "middle cache size is %d M", counter.getMaxSize()/0x100000L);

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		while (!isInterrupted()) {
			// 定时检查
			check();
			// 延时...
			sleep();
		}
		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		clear();
	}

}