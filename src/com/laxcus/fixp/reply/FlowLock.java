/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.net.*;

/**
 * 数据流锁 <br>
 * 
 * ReplyClient发送数据前，先调用数据流锁定资源，顺序发送，降低接收端的压力，防止UDP发送造成的SOCKET接收缓存溢出。
 * 
 * 注意！只在包内可见！
 * 
 * @author scott.liang
 * @version 1.0 9/3/2020
 * @since laxcus 1.0
 */
final class FlowLock extends MutexHandler {

	/**
	 * 序列锁成员
	 * 
	 * @author scott.liang
	 * @version 1.0 9/3/2020
	 * @since laxcus 1.0
	 */
	class LockMember implements Comparable<LockMember> {

		/** 客户机编号 **/
		private long clientId;

		/** 等待状态，默认是“真” **/
		private boolean awaiting;

		/**
		 * 构造序列锁成员
		 * @param id 编号
		 */
		public LockMember(long id) {
			super();
			// 进入等待状态
			awaiting = true;
			setClientId(id);
		}

		/**
		 * 设置编号
		 * @param id 编号
		 */
		public void setClientId(long id) {
			clientId = id;
		}

		/**
		 * 唤醒
		 */
		private synchronized void wakeup() {
			try {
				super.notify();
			} catch (IllegalMonitorStateException e) {
				com.laxcus.log.client.Logger.error(e);
			}
		}

		/**
		 * 任务延时
		 * @param ms 等待时间，单位：毫秒
		 */
		private synchronized void delay(long ms) {
			try {
				if (ms > 0L) {
					super.wait(ms);
				}
			} catch (InterruptedException e) {
				com.laxcus.log.client.Logger.error(e);
			}
		}

		/**
		 * 判断处于等待状态
		 * @return 返回真或者假
		 */
		public boolean isAwaiting() {
			return awaiting;
		}

		/**
		 * 触发唤醒对象
		 */
		public void done() {
			awaiting = false;
			wakeup();
		}

		/**
		 * 进入等待状态，直到返回处理结果
		 */
		public void await() {
			// 进行等待状态
			while (awaiting) {
				delay(200L);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object that) {
			if (that == null || that.getClass() != getClass()) {
				return false;
			} else if (this == that) {
				return true;
			}
			return compareTo((LockMember) that) == 0;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return (int) ((clientId >>> 32) ^ clientId);
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(LockMember that) {
			if (that == null) {
				return -1;
			}
			return Laxkit.compareTo(clientId, that.clientId);
		}
	}

	/**
	 * 客户成员组
	 *
	 * @author scott.liang
	 * @version 1.0 9/3/2020
	 * @since laxcus 1.0
	 */
	class LockDocument {

		private ArrayList<LockMember> array = new ArrayList<LockMember>();

		/**
		 * 构造客户组
		 */
		public LockDocument() {
			super();
		}

		/**
		 * 保存一个
		 * @param e
		 */
		public boolean add(LockMember e) {
			return array.add(e);
		}

		/**
		 * 删除
		 * @param e
		 * @return
		 */
		public boolean remove(LockMember e) {
			return array.remove(e);
		}

		/**
		 * 取出队列中的第一个。注意：不出栈!
		 * @return 返回实例，没有是空指针
		 */
		public LockMember next() {
			if (array.isEmpty()) {
				return null;
			}
			return array.get(0);
		}

		/**
		 * 判断是空集合
		 * @return 真或者假
		 */
		public boolean isEmpty() {
			return array.isEmpty();
		}

		/**
		 * 返回成员数目
		 * @return
		 */
		public int size() {
			return array.size();
		}
	}

	/** 设置实例 **/
	private static FlowLock selfHandle = new FlowLock();

	/** 成员集合 */
	private TreeMap<SocketHost, LockDocument> documents = new TreeMap<SocketHost, LockDocument>();

	/**
	 * 构造默认的数据流锁
	 */
	private FlowLock() {
		super();
	}

	/**
	 * 返回实例
	 * @return
	 */
	public static FlowLock getInstance() {
		return FlowLock.selfHandle;
	}

	/**
	 * 锁定顺序锁资源，保证每次都是唯一的
	 * @param host 服务器主机地址
	 * @param clientId ReplyClient客户机编号
	 */
	public void lock(SocketHost host, long clientId) {
		LockMember member = new LockMember(clientId);

		boolean success = false;
		// 锁定！
		super.lockSingle();
		try {
			LockDocument document = documents.get(host);
			if (document == null) {
				document = new LockDocument();
				documents.put(host, document);
			}
			// 如果是空集合，客户机就能够获得发送数据包的权限！
			success = document.isEmpty();
			// 保存这个成员
			document.add(member);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 不成功，等待直到获得
		if (!success) {
			member.await();
		}
	}

	/**
	 * 解除锁定
	 * @param host 服务器主机地址
	 * @param clientId 客户机编号
	 */
	public void unlock(SocketHost host, long clientId) {
		LockMember member = new LockMember(clientId);
		LockMember next = null;

		// 锁定
		super.lockSingle();
		try {
			LockDocument document = documents.get(host);
			if (document != null) {
				// 1. 删除
				boolean success = document.remove(member);
				// 2. 成功，取下一个
				if (success) {
					next = document.next();
					// 没有取出并且是空集合时，删除它
					if (next == null && document.isEmpty()) {
						documents.remove(host);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 存在下一个，唤醒它!
		if (next != null) {
			next.done();
		}
	}
	
//	/**
//	 * 锁定顺序锁资源，保证每次都是唯一的
//	 * @param host 服务器主机地址
//	 * @param clientId ReplyClient客户机编号
//	 */
//	public void lock(SocketHost host, long clientId) {
//		
//	}
//
//	/**
//	 * 解除锁定
//	 * @param host 服务器主机地址
//	 * @param clientId 客户机编号
//	 */
//	public void unlock(SocketHost host, long clientId) {
//		
//	}
}