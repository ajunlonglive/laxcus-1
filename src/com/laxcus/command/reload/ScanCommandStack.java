/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.reload;

import com.laxcus.command.site.*;
import com.laxcus.util.classable.*;

/**
 * 扫描堆栈命令。<br><br>
 * 命令启动后，将把扫描结果打印在日志里。管理员通过调阅关联日志查询节点的命令运行情况。<br>
 * 
 * 这个命令由WATCH站点发起，分到不同的站点去执行。
 * 
 * @author scott.liang
 * @version 1.0 7/26/2018
 * @since laxcus 1.0
 */
public class ScanCommandStack extends MultiSite {

	private static final long serialVersionUID = 9211412510886713432L;

	/** 判断是停止扫描 **/
	private boolean stop;

	/** 触发间隔 **/
	private long interval;

	/**
	 * 构造默认的扫描堆栈命令命令
	 */
	public ScanCommandStack() {
		super();
		setStop(true);
	}

	/**
	 * 从可类化数据读取器中解析扫描堆栈命令命令
	 * @param reader 可类化数据读取器
	 */
	public ScanCommandStack(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成扫描堆栈命令命令的数据副本
	 * @param that ScanStackCommand实例
	 */
	private ScanCommandStack(ScanCommandStack that) {
		super(that);
		stop = that.stop;
		interval = that.interval;
	}

	/**
	 * 设置启动模式
	 * @param b
	 */
	public void setStart(boolean b) {
		setStop(!b);
	}

	/**
	 * 判断启动模式
	 * @return 返回真或者假
	 */
	public boolean isStart() {
		return !isStop();
	}

	/**
	 * 设置停止模式
	 * @param b
	 */
	public void setStop(boolean b) {
		stop = b;
	}

	/**
	 * 判断停止模式
	 * @return 返回真或者假
	 */
	public boolean isStop() {
		return stop;
	}

	/**
	 * 设置间隔时间
	 * @param n
	 */
	public void setInterval(long n) {
		interval = n;
	}

	/**
	 * 返回间隔时间
	 * @return
	 */
	public long getInterval() {
		return interval;
	}
	
	/**
	 * 判断是全部站点
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return getSiteSize() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ScanCommandStack duplicate() {
		return new ScanCommandStack(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeBoolean(stop);
		writer.writeLong(interval);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		stop = reader.readBoolean();
		interval = reader.readLong();
	}

}