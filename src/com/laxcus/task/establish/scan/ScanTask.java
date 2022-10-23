/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.scan;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.distribute.establish.command.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.*;

/**
 * ESTABLISH.SCAN阶段任务。<br><br>
 * 
 * ESTABLISH.SCAN阶段任务部署和运行在<b>DATA主站点</b>上，它承接ESTABLISH.ISSUE阶段任务，
 * 根据SCAN会话中提供的表名，扫描指定的数据块，并且返回这些数据块的元信息。<br><br>
 * 
 * 注意：SCAN阶段任务只分布在DATA主站点上，扫描工作只检查处于封闭状态的数据块(completed chunk)，缓存未封闭的数据块(cache)不处理。
 * 
 * @author scott.liang
 * @version 1.1 3/26/2012
 * @since laxcus 1.0
 */
public abstract class ScanTask extends ParallelTask {

	/** SCAN本地资源代理，由DATA节点实现和提供。**/
	private ScanTrustor scanTrustor;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		scanTrustor = null;
	}

	/**
	 * 构造数据构建的“SCAN”阶段任务
	 */
	protected ScanTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#getCommand()
	 */
	@Override
	public ScanStep getCommand() {
		return (ScanStep) super.getCommand();
	}

	/**
	 * 返回“SCAN”阶段会话句柄
	 * @return  ScanSession实例
	 */
	public ScanSession getSession() {
		ScanStep cmd = getCommand();
		return cmd.getSession();
	}

	/**
	 * 设置数据表空间扫描委托器
	 * @param e  ScanTrustor实例
	 */
	public void setScanTrustor(ScanTrustor e) {
		this.scanTrustor = e;
	}

	/**
	 * 返回表空间扫描委托器
	 * @return ScanTrustor实例
	 */
	public ScanTrustor getScanTrustor() {
		return this.scanTrustor;
	}

	/**
	 * 默认的数据块扫描。如果用户没有特别的定义，可以在"scan"实现方法中直接调用这个方法。仅对子类可见。
	 * 
	 * @return  返回一个或者多个表空间数据块扫描区域的集合（ScanArea）
	 * @throws TaskException
	 */
	protected ScanArea defaultScan() throws TaskException {
		long invokerId = super.getInvokerId();
		Node local = scanTrustor.getLocal(invokerId);

		ScanArea area = new ScanArea(local);

		ScanSession session = getSession();
		List<ScanMember> array = session.getMembers();

		// 进行参数检查
		for (ScanMember member : array) {
			Space space = member.getSpace();
			// 判断表存在
			if (!scanTrustor.hasScanTable(invokerId, space)) {
				throw new ScanTaskException("cannot be find '%s'!", space);
			}
		}

		// 逐次扫描表
		for (ScanMember member : array) {
			Space space = member.getSpace(); 
			// 扫描表，返回扫描单元
			ScanField field = scanTrustor.detect(invokerId, space);
			// 如果是空值，是一个错误，弹出错误！
			if (field == null) {
				throw new ScanTaskException("%s is null", space);
			}
			// 保存参数
			area.add(field);
		}

		// 返回扫描的表空间数据块集合
		return area;
	}

	/**
	 * 按照命令要求，分析DATA主节点、指定表空间下属的全部封闭状态数据块。
	 * @return 返回分析结果的字节数组
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract byte[] analyse() throws TaskException;

	/**
	 * 按照命令要求，分析DATA主节点、指定表空间下属的全部封闭状态数据块，并且把分析结果保存到指定的磁盘文件上。
	 * @param file 磁盘文件路径
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract void analyseTo(File file) throws TaskException;

}