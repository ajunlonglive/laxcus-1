/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.flux;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.mid.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * CONDUCT中间数据堆栈。<br>
 * 
 * 提供存取中间数据的服务。
 * 
 * @author scott.liang
 * @version 1.2 8/12/2014
 * @since laxcus 1.0
 */
public class FluxStack extends MutexHandler implements FluxReader, FluxWriter, Comparable<FluxStack> {

	/** 代理池。由调用池设置，避免FluxTrustorPool.getInstance()发生安全异常 **/
	private FluxTrustorPool fluxPool;

	/** 用户签名 **/
	private Siger issuer;
	
	/** 任务编号，这是唯一的 */
	private long taskId;
	
	/** 建立时间 **/
	private long createTime;

	/** 内存记录器 **/
	private MemoryCounter counter;

	/** 模值 -> 磁盘数据位置记录 */
	private Map<Long, FluxBuffer> buffers = new TreeMap<Long, FluxBuffer>();

	/**
	 * 构造默认和私有的CONDUCT中间数据管理器
	 */
	private FluxStack() {
		super();
		// 未定义
		taskId = -1L;
		// 初始化时间
		resetTime();
	}

	/**
	 * 构造中间数据管理器，指定用户签名和任务编号
	 * @param siger 用户签名
	 * @param taskId 任务编号
	 * @param pool 代理池
	 */
	public FluxStack(Siger siger, long taskId, FluxTrustorPool pool) {
		this();
		setIssuer(siger);
		setTaskId(taskId);
		fluxPool = pool;
	}

	/**
	 * 根据传入的模值，和当前的任务号，生成一个指定目录下的磁盘文件名
	 * @param mod 模值
	 * @return 返回文件句柄
	 */
	private File createFile(long mod) {
		String name = String.format("%X_%X.flux", taskId, mod);
		// 放在指定目录下
		File root = fluxPool.getRoot();
		return new File(root, name);
	}

	/**
	 * 设置内存空间计数器
	 * @param e MemoryCounter实例
	 */
	public void setCacheCounter(MemoryCounter e) {
		counter = e;
	}

	/**
	 * 返回内存空间计数器
	 * @return MemoryCounter实例
	 */
	public MemoryCounter getCacheCounter() {
		return counter;
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		Laxkit.nullabled(e);
		issuer = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 重置时间，从这个时间开始计算超时，发生超时将执行分布数据的删除
	 */
	public void resetTime() {
		createTime = System.currentTimeMillis();
	}

	/**
	 * 检查是否超时
	 * @param timeout 超时间隔
	 * @return 返回真或者假
	 */
	public boolean isTimeouted(long timeout) {
		return System.currentTimeMillis() - createTime >= timeout;
	}

	/**
	 * 设置任务编号
	 * @param i 任务编号
	 */
	public void setTaskId(long i) {
		taskId = i;
	}

	/**
	 * 返回任务编号
	 * @return 任务编号
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * 设置单元完成标记
	 * @param mod 模值
	 * @param b 完成标记
	 */
	public boolean setCompleted(long mod, boolean b) {
		boolean success = false;
		super.lockSingle();
		try {
			FluxBuffer buf = buffers.get(mod);
			success = (buf != null);
			if (success) {
				buf.setCompleted(b);
			}
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 判断全部完成
	 * @return 返回真或者假
	 */
	public boolean isComplted() {
		int size = -1;
		int count = 0;

		super.lockMulti();
		try {
			size = buffers.size();
			Iterator<Map.Entry<Long, FluxBuffer>> iterator = buffers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, FluxBuffer> entry = iterator.next();
				if (entry.getValue().isCompleted()) {
					count++;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		} finally {
			super.unlockMulti();
		}

		return count == size;
	}

	/**
	 * 释放内存或者硬盘中的数据
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean delete() {
		boolean success = false;
		super.lockSingle();
		try {
			Iterator<Map.Entry<Long, FluxBuffer>> iterator = buffers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, FluxBuffer> entry = iterator.next();
				entry.getValue().delete();
			}
			buffers.clear();
			success = true;
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (taskId >>> 32 ^ taskId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FluxStack.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((FluxStack) that) == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FluxStack that) {
		// 空对象排在前面
		if(that == null) {
			return 1;
		}
		return Laxkit.compareTo(taskId, that.taskId);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MidReader#isMemory()
	 */
	@Override
	public boolean isMemory() {
		// 如果不是内存状态时...
		if (counter == null) {
			return false;
		}

		int size = -1;
		int count = 0;

		super.lockMulti();
		try {
			size = buffers.size();
			Iterator<Map.Entry<Long, FluxBuffer>> iterator = buffers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, FluxBuffer> entry = iterator.next();
				if (entry.getValue().isMemory()) {
					count++;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		} finally {
			super.unlockMulti();
		}

		return size == count;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MidReader#isDisk()
	 */
	@Override
	public boolean isDisk() {
		return !isMemory();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MidReader#getLocal()
	 */
	@Override
	public Node getLocal() {
		return fluxPool.getLocal(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.AccessWriter#getTimeout()
	 */
	@Override
	public long getTimeout() {
		return fluxPool.getTimeout();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxWriter#collect()
	 */
	@Override
	public FluxArea collect() {
		Node local = getLocal();
		int timeout = (int) (getTimeout() / 1000);
		FluxArea area = new FluxArea(local, taskId, timeout);

		// 锁定数据
		super.lockSingle();
		try {
			for (FluxBuffer buf : buffers.values()) {
				area.add(buf.getField());
			}
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		} finally {
			super.unlockSingle();
		}
		// 返回结果
		return area;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxWriter#append(long, int, byte[], int, int)
	 */
	@Override
	public FluxField append(long mod, int elements, byte[] b, int off, int len) {
		if (b == null || b.length == 0) {
			throw new NullPointerException();
		} else if (off < 0 || len < 1 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		// 建立一个本地文件
		File file = createFile(mod);
//		Logger.debug(issuer, this, "append", "file is %s", file);

		// 锁定
		super.lockSingle();
		try {
			FluxBuffer buf = buffers.get(mod);
			if (buf == null) {
				buf = new FluxBuffer(issuer, mod, file);
				buffers.put(mod, buf);
			}

			// 如果定义分配内存时...
			boolean memory = (counter != null);
			if (memory) {
				memory = counter.alloc(len);
			}
			// 追加数据
			boolean success = buf.append(memory, elements, b, off, len);
			if (success) {
				return buf.getField();
			}
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		} finally {
			super.unlockSingle();
		}
		// 出错，返回空值
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxWriter#update(long, long, byte[], int, int)
	 */
	@Override
	public FluxField update(long mod, long seek, byte[] b, int off, int len) {
		if (b == null || b.length == 0) {
			throw new NullPointerException();
		} else if (off < 0 || len < 1 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		// 建立一个本地文件
		File file = createFile(mod);
//		Logger.debug(issuer, this, "append", "file is %s", file);

		// 锁定
		super.lockSingle();
		try {
			FluxBuffer buf = buffers.get(mod);
			if (buf == null) {
				buf = new FluxBuffer(issuer, mod, file);
				buffers.put(mod, buf);
			}

			// 如果定义分配内存时...
			boolean memory = (counter != null);
			if (memory) {
				memory = counter.alloc(len);
			}
			// 追加数据
			boolean success = buf.update(memory, seek, b, off, len);
			if (success) {
				return buf.getField();
			}
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		} finally {
			super.unlockSingle();
		}
		// 出错，返回空值
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxReader#read(long, long, int)
	 */
	@Override
	public byte[] read(long mod, long seek, int size) {
		super.lockSingle();
		try {
			FluxBuffer buf = buffers.get(mod);
			// 读数据
			if (buf != null) {
				return buf.read(seek, size);
			}
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		} finally {
			super.unlockSingle();
		}
		// 返回空值
		return null;
	}

	/**
	 * 在发生意外情况下，由JVM删除磁盘和内存数据
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		delete();
		fluxPool = null;
	}
}