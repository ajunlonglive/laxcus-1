/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.product;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 停止边缘计算服务
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubStopServiceProduct extends TubConfirmProduct {

	/** 状态码 **/
	private int status;

	/** 进程号 **/
	private long processId;

	/** 命名 **/
	private Naming naming;

	/**
	 * 构造默认的检测边缘服务监听处理结果
	 */
	public TubStopServiceProduct() {
		super();
		setSuccessful(false);
	}
	
	public TubStopServiceProduct(boolean success) {
		this();
		setSuccessful(success);
	}

	/**
	 * 生成检测边缘服务监听处理结果的副本
	 * @param that
	 */
	private TubStopServiceProduct(TubStopServiceProduct that) {
		this();
		status = that.status;
		processId = that.processId;
		if (that.naming != null) {
			naming = that.naming.duplicate();
		}
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public TubStopServiceProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置状态码
	 * @param who
	 */
	public void setStatus(int who) {
		status = who;
	}
	
	/**
	 * 返回状态码
	 * @return
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * 设置命名
	 * @param e
	 */
	public void setNaming(Naming e) {
		naming = e;
	}
	
	/**
	 * 返回命名
	 * @return
	 */
	public Naming getNaming() {
		return naming;
	}
	
	/**
	 * 设置进程编号
	 * @param who
	 */
	public void setProcessId(long who) {
		processId = who;
	}
	
	/**
	 * 返回进程编号
	 * @return
	 */
	public long getProcessId() {
		return processId;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.product.TubProduct#duplicate()
	 */
	@Override
	public TubStopServiceProduct duplicate() {
		return new TubStopServiceProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.product.TubProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(status);
		writer.writeLong(processId);
		boolean success = (naming != null);
		writer.writeBoolean(success);
		if (success) {
			writer.writeObject(naming);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.product.TubProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		status = reader.readInt();
		processId = reader.readLong();
		boolean success = reader.readBoolean();
		if (success) {
			naming = new Naming(reader);
		} else {
			naming = null;
		}
	}

}
