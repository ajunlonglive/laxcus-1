/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.pool;

import java.util.*;

import com.laxcus.command.cloud.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;

/**
 * 预读取软件包元组 <br>
 * 
 * @author scott.liang
 * @version 1.0 8/22/2020
 * @since laxcus 1.0
 */
public final class ReadPackageTuple implements Comparable<ReadPackageTuple>{

	/** 等待状态，默认是“真” **/
	private boolean awaiting;
	
	/** 任务部件 **/
	private TaskPart part;

	/** 成功或者失败 **/
	private boolean successful;

	/** 超时时间，默认1小时 **/
	private long timeout;
	
	/** 附件 **/
	private ArrayList<CloudPackageItem> assists = new ArrayList<CloudPackageItem>();

	/** 动态链接库附件属性 **/
	private ArrayList<CloudPackageItem> links = new ArrayList<CloudPackageItem>();
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.File#finalize()
	 */
	@Override
	public void finalize() {
		part = null;
		assists.clear();
		links.clear();
	}
	
	/**
	 * 预读取软件包元组
	 * @param issuer 任务部件
	 * @param timeout 超时时间
	 */
	public ReadPackageTuple(TaskPart issuer, long timeout) {
		super();
		setPart(issuer);
		setTimeout(timeout);
		awaiting = true; // 准备进入等待
		successful = false; // 默认不成功
	}

	/**
	 * 构造预读取软件包元组，指定文件
	 * @param issuer 任务部件
	 * @param family 阶段类型
	 */
	public ReadPackageTuple(TaskPart issuer) {
		this(issuer,  3600 * 1000);
	}

	/**
	 * 最大超时时间
	 * @param ms 毫秒
	 */
	public void setTimeout(long ms) {
		if(ms > 0)timeout = ms;
	}

	/**
	 * 设置基于秒时间的超时
	 * @param seconds
	 */
	public void setTimeoutWithSecond(int seconds) {
		setTimeout(seconds * 1000);
	}

	/**
	 * 返回超时时间
	 * @return 时间（长整型）
	 */
	public long getTimeout() {
		return timeout;
	}
	
	/**
	 * 设置任务部件，不允许空指针
	 * @param e 签名实例
	 */
	public void setPart(TaskPart e) {
		Laxkit.nullabled(e);
		part = e;
	}
	
	/**
	 * 返回任务部件
	 * @return 签名实例
	 */
	public TaskPart getPart() {
		return part;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return part.getIssuer();
	}

	/**
	 * 执行延时
	 * @param timeout 超时时间，单位：毫秒
	 */
	private synchronized void delay(long timeout) {
		try {
			super.wait(timeout);
		} catch (InterruptedException e) {
			
		}
	}

	/**
	 * 唤醒延时
	 */
	private synchronized void wakeup() {
		try {
			notify();
		} catch (IllegalMonitorStateException e) {
			
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
		// 最后触发时间
		long last = System.currentTimeMillis() + timeout;

		// 没有达到触发时间，延时等待
		while (System.currentTimeMillis() <= last) {
			// 没有触发就延时，否则退出！
			if (awaiting) {
				delay(1000L);
			} else {
				break;
			}
		}
	}

	/**
	 * 设置执行结果状态（成功或者失败），同时唤醒等待
	 * @param b 成功或者否
	 */
	public void setSuccessful(boolean b) {
		successful = b;
		done();
	}

	/**
	 * 判断执行成功或者失败
	 * @return 故障对象
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 保存JAR附件
	 * @param key JAR附件
	 * @return 保存成功返回真，否则假
	 */
	public boolean addJar(CloudPackageItem key) {
		if (key == null) {
			return false;
		}
		return assists.add(key);
	}
	
	/**
	 * 保存一批
	 * @param a
	 * @return
	 */
	public int addJars(Collection<CloudPackageItem> a) {
		if (a == null || a.isEmpty()) {
			return 0;
		}
		int size = assists.size();
		assists.addAll(a);
		return assists.size() - size;
	}
	
	/**
	 * 删除JAR附件
	 * @param key JAR附件
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeJar(CloudPackageItem key) {
		if (key == null) {
			return false;
		}
		return assists.remove(key);
	}

	/**
	 * 输出JAR包
	 * @return
	 */
	public List<CloudPackageItem> getJars() {
		return new ArrayList<CloudPackageItem>(assists);
	}

	/**
	 * 保存动态链接库附件
	 * @param key 动态链接库附件
	 * @return 保存成功返回真，否则假
	 */
	public boolean addLibrary(CloudPackageItem key) {
		if (key == null) {
			return false;
		}
		return links.add(key);
	}
	
	/**
	 * 保存一批
	 * @param a
	 * @return
	 */
	public int addLibraries(Collection<CloudPackageItem> a) {
		if (a == null || a.isEmpty()) {
			return 0;
		}
		int size = links.size();
		links.addAll(a);
		return links.size() - size;
	}
	
	/**
	 * 删除动态链接库附件
	 * @param key 动态链接库附件
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeLibrary(CloudPackageItem key) {
		if (key == null) {
			return false;
		}
		return links.remove(key);
	}

	/**
	 * 输出动态链接库附件
	 * @return CloudPackageItem集合
	 */
	public List<CloudPackageItem> getLibraries() {
		return new ArrayList<CloudPackageItem>(links);
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
		return compareTo((ReadPackageTuple) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return part.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ReadPackageTuple that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		// 判断一致
		return Laxkit.compareTo(part, that.part);
	}

}