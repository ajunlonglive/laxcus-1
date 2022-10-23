/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.to;

import java.io.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.task.*;

/**
 * TO(CONVERGE)阶段任务的数据生成和分割操作实例。<br><br>
 * 
 * ToGenerateTask的任务要求与FromTask相同，或者根据SELECT语句从DATA节点获取数据，或者使用携带的自定义参数生成数据。数据在本地分片后，实体数据写入磁盘，输出FluxAare映像数据（元信息）给BalanceTask。<br>
 * 在每个ToGenerateTask实例后面，必有一个ToEvaluateTask，或者另一个ToGenerateTask实例。也就是说，ToGenerateTask一定不会是TO阶段的结束，而ToEvaluateTask是。<br><br>
 * 
 * 流程：<br>
 * <1> 如果ToSession有SELECT语句，表示从DATA节点获取数据。divide方法接收数据，在本地分片和可能通过磁盘委托器保存。<br>
 * <2> 如果ToSession没有SELECT语句，divide将根据用户自定义参数生产数据和分片(由用户解释和实现)。可能通过磁盘委托器接口写入硬盘。<br>
 * <3> effuse方法收集FluxAare元信息，以字节数组方式输出。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/12/2009
 * @since laxcus 1.0
 */
public abstract class ToGenerateTask extends ToTask {

	/**
	 * 构造ToGenerateTask实例
	 */
	public ToGenerateTask() {
		super(ToMode.GENERATE);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		// 生成元数据
		FluxArea area =	createFluxArea();
		// 转为字节数组输出
		return area.build();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		// 转为字节数组
		byte[] b = effuse();
		// 输出到指定的文件
		return	writeTo(file, false, b, 0, b.length);
	}

	/**
	 * 数据分割 <br>
	 * 这个方法可视为DATA站点的数据分割在WORK站点的执行过程。<br>
	 * 两者的区别是,DATA站点的数据分割不发生网络通信，而WORK站点上的数据分割可能存在通信情况。具有处理方式由用户决定。<br>
	 * 
	 * @return 返回FluxArea元数据的字节数组长度（注意，是元数据，不是实体数据）
	 * @throws TaskException
	 */
	public abstract long divide() throws TaskException;

}