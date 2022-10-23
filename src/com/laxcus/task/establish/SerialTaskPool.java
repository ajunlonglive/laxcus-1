/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish;

import com.laxcus.distribute.meta.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.seeker.*;

/**
 * 串行任务资源管理池。
 * 
 * @author scott.liang
 * @version 1.1 12/27/2015
 * @since laxcus 1.0
 */
public class SerialTaskPool extends RemoteTaskPool {

	/** SCAN阶段资源向导接口  **/
	private ScanSeeker scanSeeker;

	/** SIFT阶段资源向导接口 **/
	private SiftSeeker siftSeeker;

	/** 元数据存取代理 **/
	private MetaTrustor metaTrustor;
	
	/**
	 * 构造串行任务资源管理池，指定阶段类型
	 * @param family ESTABLISH阶段类型
	 */
	protected SerialTaskPool(int family) {
		super(family);
	}

	/**
	 * 设置SCAN阶段资源向导接口 
	 * @param e ScanSeeker实例
	 */
	public void setScanSeeker(ScanSeeker e) {
		scanSeeker = e;
	}

	/**
	 * 返回SCAN阶段资源向导接口 
	 * @return ScanSeeker实例
	 */
	public ScanSeeker getScanSeeker() {
		return scanSeeker;
	}

	/**
	 * 设置SIFT阶段资源向导接口 
	 * @param e SiftSeeker实例
	 */
	public void setSiftSeeker(SiftSeeker e) {
		siftSeeker = e;
	}

	/**
	 * 返回SIFT阶段资源向导接口 
	 * @return SiftSeeker实例
	 */
	public SiftSeeker getSiftSeeker() {
		return siftSeeker;
	}

	/**
	 * 设置元数据存取代理。CallLauncher在启动时设置
	 * @param e MetaTrustor实例
	 */
	public void setMetaTrustor(MetaTrustor e) {
		metaTrustor = e;
	}

	/**
	 * 返回元数据存取代理
	 * @return MetaTrustor实例
	 */
	protected MetaTrustor getMetaTrustor() {
		return metaTrustor;
	}

}
