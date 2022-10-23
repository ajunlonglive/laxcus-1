/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.rise;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.distribute.establish.command.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * ESTABLISH.RISE阶段任务。<br><br>
 * 
 * ESTABLISH.RISE阶段任务部署在DATA站点上（全部DATA站点，不分主/从）。它负责从BUILD站点下载新的数据块，然后覆盖或者更新本地的数据块，并且生成用户自定义的处理信息，返回给ESTABLISH.END阶段任务。<br>
 * 
 * 在ESTABLISH阶段序列中，RISE在SIFT之后，END之前。<br><br>
 * 
 * 工作内容：<br>
 * 1. 删除本地旧的数据块 <br>
 * 2. 从BUILD站点下载新的数据块，取代本地旧的数据块。<br>
 * 
 * @author scott.liang
 * @version 1.1 3/26/2012
 * @since laxcus 1.0
 */
public abstract class RiseTask extends ParallelTask {

	/** RISE工作委托器，由DATA节点实现和提供。**/
	private RiseTrustor riseTrustor;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		riseTrustor = null;
	}

	/**
	 * 构造数据构建的“RISE”阶段会话
	 */
	public RiseTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#getCommand()
	 */
	@Override
	public RiseStep getCommand() {
		return (RiseStep) super.getCommand();
	}

	/**
	 * 返回“RISE”阶段会话句柄
	 * @return RiseSession实例
	 */
	public RiseSession getSession() {
		RiseStep cmd = getCommand();
		return cmd.getSession();
	}

	/**
	 * 设置RISE阶段工作委托器
	 * @param e RiseTrustor实例
	 */
	public void setRiseTrustor(RiseTrustor e) {
		riseTrustor = e;
	}

	/**
	 * 返回RISE阶段工作委托器
	 * @return RiseTrustor实例
	 */
	public RiseTrustor getRiseTrustor() {
		return riseTrustor;
	}

	/**
	 * 默认的替换操作。<br>
	 * 除非特殊情况，一般的RISE阶段任务实例都可以使用这个方法，执行从BUILD站点下载数据，然后在本地更新的工作。
	 * RiseTask子类的convert方法中，可以调用这个方法。
	 * 
	 * @return 返回RiseArea实例
	 * @throws TaskException
	 */
	protected RiseArea defaultConvert() throws TaskException {
		// 本机站点地址
		Node local = riseTrustor.getLocal(getInvokerId());
		// 建立RISE区，做为输出
		RiseArea area = new RiseArea(local);

		RiseSession session = getSession();
		// 删除过期数据块，保存它的映像参数
		for(RiseOldHead head : session.getDeleteHeads()) {
			Space space = head.getSpace();
			EstablishFlag flag = new EstablishFlag(space, local);
			RiseOldField field = new RiseOldField(flag);

			for (long stub : head.getStubs()) {
				// 判断旧的数据块存在
				boolean success = riseTrustor.hasChunk(getInvokerId(), space, stub);
				if(!success) {
					continue;
				}

				// 删除数据块
				StubItem item = riseTrustor.deleteChunk(getInvokerId(), space, stub);
				success = (item != null);
				// 如果成功，保存被删除的数据块
				if (success) {
					field.addStubItem(item);
				}

				Logger.note(this, "defaultConvert", success, "delete %s",
						new StubFlag(space, stub));
			}
			// 保存删除域
			area.addOldField(field);
		}

		// 从BUILD站点下载数据块，更新本地数据块
		for (RiseNewHead head : session.getUpdateHeads()) {
			Space space = head.getSpace();
			EstablishFlag flag = new EstablishFlag(space, local);
			RiseNewField field = new RiseNewField(flag);

			// 去BUILD站点下载和在本地更新
			Node hub = head.getSource();
			for (long stub : head.getStubs()) {
				// 更新数据块
				StubItem item = riseTrustor.updateChunk(getInvokerId(), hub, space, stub);
				boolean success = (item != null);
				Logger.note(this, "defaultConvert", success, "replace '%s'", new StubFlag(space, stub));

				// 不成功，忽略它
				if (!success) {
					continue;
				}
				field.addStubItem(item);
			}
			// 保存被更新的数据域
			area.addNewField(field);
		}

		Logger.debug(this, "defaultConvert", "update fields:%d, delete fields:%d", 
				area.getNewFields().size(), area.getOldFields().size());

		return area;
	}

	//	/**
	//	 * 删除本地的数据块
	//	 * @param space 数据表名
	//	 * @param stub 数据块编号
	//	 * @return 返回数据块参数信息
	//	 */
	//	private StubItem doDeleteChunk(Space space, long stub) throws TaskException {
	//		long invokerId = getInvokerId();
	//		// 判断数据块存在
	//		boolean success = trustor.hasChunk(invokerId, space, stub);
	//		if (!success) {
	//			Logger.error(this, "doDeleteChunk", "cannot find %s$%x", space, stub);
	//			return null;
	//		}
	//		// 删除数据块
	//		return trustor.deleteChunk(invokerId, space, stub);
	//	}

	//	/**
	//	 * 更新数据块<br><br>
	//	 * 
	//	 * 更新存在两种可能：<br>
	//	 * 1. 硬盘上有旧的数据块，先删除，再执行下载和本地更新。<br>
	//	 * 2. 硬盘上没有这个数据，直接下载，然后本地更新。<br>
	//	 * 
	//	 * @param hub 服务器地址
	//	 * @param space 数据表名
	//	 * @param stub 数据块编号
	//	 * @return 返回更新后的数据块元数据
	//	 */
	//	private StubItem doUpdateChunk(Node hub, Space space, long stub) throws TaskException {
	//		//		long invokerId = getInvokerId();
	//		//		// 判断数据块存在
	//		//		boolean success = trustor.hasChunk(invokerId, space, stub);
	//		//		// 如果存在数据，删除它
	//		//		if (success) {
	//		//			StubItem item = trustor.deleteChunk(invokerId, space, stub);
	//		//			success = (item != null);
	//		//			Logger.note(this, "doUpdateChunk", success, "delete chunk '%s'",
	//		//					new StubFlag(space, stub));
	//		//		}
	//
	//		// 通过代理更新数据块
	//		return trustor.updateChunk(getInvokerId(), hub, space, stub);
	//	}

	/**
	 * 按照RISE会话中提供的参数，从BUILD站点下载数据块，替换本地旧的数据块，删除过期数据块
	 * @return 返回元数据信息
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract byte[] convert() throws TaskException;

	/**
	 * 按照RISE会话中提供的参数，从BUILD站点下载数据块，替换本地旧的数据块，删除过期数据块。本操作的元数据信息写入磁盘。
	 * @param file 元数据的磁盘文件
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract void convertTo(File file) throws TaskException;

}