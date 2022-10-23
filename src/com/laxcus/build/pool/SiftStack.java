/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.pool;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.mid.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.establish.sift.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * SIFT数据堆栈<br>
 * 
 * 提供存取SIFT中间数据业务。
 * 
 * @author scott.liang
 * @version 1.4 11/09/2013
 * @since laxcus 1.0
 */
public class SiftStack extends MutexHandler implements SiftWriter, SiftReader, Comparable<SiftStack> {
	
	/** 操作句柄 **/
	private SiftManager siftPool;
	
	/** 用户签名 **/
	private Siger issuer;
	
	/** 调用器编号，用于关联 **/
	private long invokerId;

	/** 任务编号，这是唯一的 */
	private long taskId;

	/** 建立时间 **/
	private long createTime;
	
	/** 内存记录器 **/
	private MemoryCounter counter;

	/** 模值 -> SIFT中间数据 **/
	private TreeMap<Long, SiftBuffer> buffers = new TreeMap<Long, SiftBuffer>();

	/**
	 * 构造默认的SIFT数据堆栈
	 */
	private SiftStack() {
		super();
		// 未定义
		invokerId = InvokerIdentity.INVALID;
		taskId = -1L;
		// 初始化时间
		resetTime();
	}

	/**
	 * 构造SIFT数据堆栈，指定用户签名和任务编号
	 * @param issuer 用户签名
	 * @param invokerId 调用器编号
	 * @param taskId SiftStack任务编号
	 */
	public SiftStack(Siger issuer, long invokerId, long taskId, SiftManager pool) {
		this();
		setIssuer(issuer);
		setInvokerId(invokerId);
		setTaskId(taskId);
		siftPool = pool;
	}

	/**
	 * 设置内存空间计数器
	 * @param e
	 */
	public void setCacheCounter(MemoryCounter e) {
		counter = e;
	}

	/**
	 * 返回内存空间计数器
	 * @return
	 */
	public MemoryCounter getCacheCounter() {
		return counter;
	}

	/**
	 * 根据传入的模值，和当前的任务号，生成一个指定目录下的磁盘文件名
	 * @param mod 模值
	 * @return 返回文件句柄
	 */
	private File createFile(long mod) {
		String name = String.format("%X_%X.sift", taskId, mod);
		// 放在指定目录下
		File root = siftPool.getRoot();
		return new File(root, name);
	}

	/**
	 * 重置时间，从这个时间开始计算超时，发生超时将执行分布数据的删除
	 */
	public void resetTime() {
		createTime = System.currentTimeMillis();
	}

	/**
	 * 检查是否超时
	 * @param timeout
	 * @return
	 */
	public boolean isTimeouted(long timeout) {
		return System.currentTimeMillis() - createTime >= timeout;
	}

	/**
	 * 设置用户签名
	 * @param e
	 */
	public void setIssuer(Siger e) {
		issuer = e;
	}

	/**
	 * 返回用户签名
	 * @return
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 设置关联的调用器编号
	 * @param who
	 */
	public void setInvokerId(long who) {
		if (InvokerIdentity.isInvalid(who)) {
			throw new IllegalValueException("illegal invoker identity %d", who);
		}
		invokerId = who;
	}

	/**
	 * 返回关联的调用器编号
	 * @return
	 */
	public long getInvokerId() {
		return invokerId;
	}

	/**
	 * 设置任务编号
	 * @param i
	 */
	public void setTaskId(long i) {
		taskId = i;
	}
	
	/**
	 * 建立当前实例的SIFT存取堆栈标识
	 * @return
	 */
	public SiftStackFlag createFlag() {
		return new SiftStackFlag(invokerId, taskId);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MidWriter#getTimeout()
	 */
	@Override
	public long getTimeout() {
		return siftPool.getTimeout();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MidStack#isMemory()
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
			Iterator<Map.Entry<Long, SiftBuffer>> iterator = buffers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, SiftBuffer> entry = iterator.next();
				if (entry.getValue().isMemory()) {
					count++;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return size == count;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MidStack#isDisk()
	 */
	@Override
	public boolean isDisk() {
		return !isMemory();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MidStack#getTaskId()
	 */
	@Override
	public long getTaskId() {
		return taskId;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MidStack#getLocal()
	 */
	@Override
	public Node getLocal() {
		return SiftManager.getLauncher().getListener();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SiftStack that) {
		if (that == null) {
			return 1;
		}
		// 比较一致
		int ret = Laxkit.compareTo(issuer, that.issuer);
		if (ret == 0) {
			ret = Laxkit.compareTo(invokerId, that.invokerId);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(taskId, that.taskId);
		}
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftReader#getMods()
	 */
	@Override
	public List<Long> getMods() {
		super.lockMulti();
		try {
			return new ArrayList<Long>(buffers.keySet());
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 释放内存或者硬盘中的数据
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean delete() {
		boolean success = false;
		super.lockSingle();
		try {
			Iterator<Map.Entry<Long, SiftBuffer>> iterator = buffers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, SiftBuffer> entry = iterator.next();
				entry.getValue().delete();
			}
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftReader#length()
	 */
	@Override
	public long length() {
		long length = 0;
		super.lockMulti();
		try {
			Iterator<Map.Entry<Long, SiftBuffer>> iterator = buffers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, SiftBuffer> entry = iterator.next();
				length += entry.getValue().length();
			}
		} catch(Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return length;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftReader#length(long)
	 */
	@Override
	public long length(long mod) {
		super.lockMulti();
		try {
			SiftBuffer buf = buffers.get(mod);
			if (buf != null) {
				return buf.length();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return -1L;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftReader#read(long, long, int)
	 */
	@Override
	public byte[] read(long mod, long seek, int size) {
		super.lockMulti();
		try {
			SiftBuffer e = buffers.get(mod);
			if (e != null) {
				return e.read(seek, size);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftReader#read(long)
	 */
	@Override
	public byte[] read(long mod) {
		super.lockMulti();
		try {
			SiftBuffer e = buffers.get(mod);
			if (e != null) {
				long length = e.length();
				return e.read(0, length);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftWriter#append(long, byte[], int, int)
	 */
	@Override
	public int append(long mod, byte[] b, int off, int len) {
		if (b == null || b.length == 0) {
			throw new NullPointerException();
		} else if (off < 0 || len < 1 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		// 建立一个本地文件
		File file = createFile(mod);
		Logger.debug(this, "append", "file is %s", file);

		super.lockSingle();
		try {
			SiftBuffer buf = buffers.get(mod);
			if (buf == null) {
				buf = new SiftBuffer(mod, file);
				buffers.put(mod, buf);
			}

			// 如果定义分配内存时...
			boolean memory = (counter != null);
			if (memory) {
				memory = counter.alloc(len);
			}
			// 追加数据
			boolean success = buf.append(memory, b, off, len);
			if (success) {
				return len;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 出错
		return -1;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftWriter#update(long, long, byte[], int, int)
	 */
	@Override
	public int update(long mod, long seek, byte[] b, int off, int len) {
		if (b == null || b.length == 0) {
			throw new NullPointerException();
		} else if (off < 0 || len < 1 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		// 建立一个本地文件
		File file = createFile(mod);
		Logger.debug(this, "append", "file is %s", file);

		super.lockSingle();
		try {
			SiftBuffer buf = buffers.get(mod);
			if (buf == null) {
				buf = new SiftBuffer(mod, file);
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
				return len;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 出错
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		delete();
		if (siftPool != null) {
			siftPool = null;
		}
	}
}