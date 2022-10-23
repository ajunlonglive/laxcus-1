/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.sift;

import java.io.*;

import com.laxcus.distribute.establish.command.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.*;

/**
 * 数据构建的“SIFT”阶段任务。<br><br>
 * 
 * SIFT阶段任务位于BUILD站点上，由SiftTaskPool根据阶段命名产生。
 * BUILD站点是专门为数据构建定义的站点，与数据计算（CONDUCT）的WORK站点性质类似。<br><br>
 * 
 * “SIFT”阶段是ESTABLISH命令的核心。在BUILD站点上，首先根据SIFT阶段会话的数据块元数据，从DATA站点下载数据块，
 * 然后将数据构建工作转交到这里。SIFT任务将完成真正的数据构建。
 * 数据构建完成后，新数据块的元数据要传回到CALL站点，由CALL.ASSIGN分配给DATA站点。在DATA站点全部下载后，CALL站点发出“ERASE SIFT”通知，删除本地数据空间，和本地生成的数据块。
 * 
 * SIFT阶段的数据构建工作包括两种：<br>
 * 
 * 1. 数据优化(modulate)。是对原数据块的删除冗余过期数据和压缩，不改变数据内容本身。
 * 这个工作是DATA.REGULATE命令在BUILD站点实现。原因是regulate工作负载过重，影响DATA站点运行效率，所以转移到BUILD站点进行。目前这个工作已经由系统实现。<br>
 * 
 * 2. 数据重组(reshuffle)。是从任意多个表的数据中，抓取需要的数据，生成新的数据块。不修改原数据块中的内容。<br>
 * 
 * @author scott.liang
 * @version 1.3 6/25/2014
 * @since laxcus 1.0
 */
public abstract class SiftTask extends ParallelTask {

	/** SIFT本地资源代理，在WORK站点实现和提供 **/
	private SiftTrustor siftTrustor;

	/** 当前SIFT任务实例编号。默认是-1，没有定义 **/
	private long taskId;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		// 清除
		siftTrustor = null;
		taskId = -1L;
	}

	/**
	 * 生成SIFT阶段任务
	 */
	protected SiftTask() {
		super();
		taskId = -1L; // 无定义
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#getCommand()
	 */
	@Override
	public SiftStep getCommand() {
		return (SiftStep) super.getCommand();
	}

	/**
	 * 返回“SIFT”阶段会话句柄
	 * @return SiftSession实例
	 */
	public SiftSession getSession() {
		SiftStep cmd = getCommand();
		return cmd.getSession();
	}

	/**
	 * 设置SIFT工作代理
	 * @param e SiftTrustor实例
	 */
	public void setSiftTrustor(SiftTrustor e) {
		siftTrustor = e;
	}

	/**
	 * 返回SIFT工作代理
	 * @return SiftTrustor实例
	 */
	public SiftTrustor getSiftTrustor() {
		return siftTrustor;
	}

	/**
	 * 返回当前任务编号。小于0是无效。
	 * @return 长整数
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * 获得SIFT数据写入器。在第一次生成后，以后都将返回它。
	 * 
	 * @param memory 内存模式
	 * @param capacity 内存容量（在memory is true时才生效）
	 * @return 返回SiftWriter实例
	 * @throws TaskException
	 */
	protected SiftWriter fetchWriter(boolean memory, long capacity) throws TaskException {
		long invokerId = super.getInvokerId();
		if (taskId == -1L) {
			taskId = siftTrustor.createStack(invokerId, memory, capacity);
			Logger.debug(this, "fetchWriter", "create stack, task id is %d", taskId);
		}
		if (taskId >= 0L) {
			return siftTrustor.findWriter(invokerId, taskId);
		}
		throw new TaskException("cannot be fetch SiftWriter");
	}

	/**
	 * 获取SIFT数据写入器，指定内存/磁盘模式
	 * 
	 * @param memory 内存模式
	 * @return 返回SiftWriter实例
	 * @throws TaskException
	 */
	protected SiftWriter fetchWriter(boolean memory) throws TaskException {
		long capacity = siftTrustor.getMemberMemory();
		return fetchWriter(memory, capacity);
	}

	/**
	 * 获得默认的SIFT数据写入器。SIFT数据写入硬盘
	 * 
	 * @return 返回SiftWriter实例
	 */
	protected SiftWriter fetchWriter() throws TaskException {
		return fetchWriter(false, -1L);
	}

	/**
	 * 获得SIFT数据读取器。在第一次生成后，以后都将返回它
	 * @param memory 内存模式
	 * @param capacity 内存容量（在memory is true时才生效）
	 * @return 返回SiftReader实例
	 * @throws TaskException
	 */
	protected SiftReader fetchReader(boolean memory, long capacity) throws TaskException {
		long invokerId = super.getInvokerId();
		if (taskId == -1L) {
			taskId = siftTrustor.createStack(invokerId,  memory, capacity);
			Logger.debug(this, "fetchReader", "create stack, task id is %d", taskId);
		}
		if (taskId >= 0L) {
			return siftTrustor.findReader(invokerId, taskId);
		}
		throw new TaskException("cannot be fetch SiftReader");
	}

	/**
	 * 获取SIFT数据读取器，指定内存/磁盘模式
	 * @param memory 内存模式
	 * @return 返回SiftReader实例
	 * @throws TaskException
	 */
	protected SiftReader fetchReader(boolean memory) throws TaskException {
		long capacity = siftTrustor.getMemberMemory();
		return fetchReader(memory, capacity);
	}

	/**
	 * 获得默认的SIFT数据读取器。SIFT数据从硬盘读出
	 * @return 返回SiftReader实例
	 */
	protected SiftReader fetchReader() throws TaskException {
		return fetchReader(false, -1L);
	}

	/**
	 * 执行数据构建的SIFT阶段任务
	 * @return 返回SIFT阶段的数据构建元数据
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract byte[] implement() throws TaskException;

	/**
	 * 执行数据构建的SIFT阶段任务，把元数据输出到磁盘文件
	 * @param file 元数据的磁盘文件
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract void implementTo(File file) throws TaskException;

}