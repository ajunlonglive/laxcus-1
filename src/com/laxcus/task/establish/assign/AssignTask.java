/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.assign;

import java.io.*;
import java.util.*;

import com.laxcus.command.establish.*;
import com.laxcus.distribute.establish.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.distribute.meta.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 数据构建的“ASSIGN”阶段任务。<BR>
 * 
 * ASSIGN介于SCAN/SIFT/ASSIGN之间，处理SCAN/SIFT/SUBSIFT阶段的数据，为后续的SIFT/SUBSIFT/RISE阶段分配资源。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 1/12/2012
 * @since laxcus 1.0
 */
public abstract class AssignTask extends SerialTask {
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.SerialTask#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
	}

	/**
	 * 构造数据构建的“ASSIGN”阶段任务实例
	 */
	protected AssignTask() {
		super();
	}

	/**
	 * 返回ASSIGN阶段对象实例
	 * @return AssignObject对象
	 */
	public AssignObject getObject() {
		Establish establish = getCommand();
		return establish.getAssignObject();
	}

	/**
	 * 建立元数据标识
	 * @param phase 阶段命名
	 * @param iterateIndex 迭代编号
	 * @return 返回MetaTag实例
	 */
	protected MetaTag createMetaTag(Phase phase, int iterateIndex) {
		return new MetaTag(getInvokerId(), getIssuer(), phase, iterateIndex);
	}

	/**
	 * 建立SIFT DOCK标识
	 * @param iterateIndex 迭代编号
	 * @return 返回标注“SIFT_DOCK”子命名的的MetaTag实例
	 */
	private MetaTag createDockTag(int iterateIndex) {
		Phase phase = getPhase().duplicate();
		phase.setSub("SIFT_DOCK");
		return createMetaTag(phase, iterateIndex);
	}

	/**
	 * 输出当前保存的全部SIFT锚点
	 * @return SiftDock列表
	 */
	public List<SiftDock> getSiftDocks() throws TaskException {
		MetaTag tag = createDockTag(0);
		ArrayList<SiftDock> docks = new ArrayList<SiftDock>();

		byte[] b = read(tag);
		if (Laxkit.isEmpty(b)) {
			return docks;
		}

		// 解析参数
		ClassReader reader = new ClassReader(b);
		while (reader.hasLeft()) {
			SiftDock dock = new SiftDock(reader);
			docks.add(dock);
		}
		return docks;
	}

	/**
	 * 保存某个SIFT阶段锚点数据
	 * @param docks SiftDock数组
	 * @throws TaskException
	 */
	protected void addSiftDocks(SiftDock[] docks) throws TaskException {
		// 判断是空
		if (Laxkit.isEmpty(docks)) {
			throw new NullPointerException();
		}

		// 转为可类化数据
		ClassWriter writer = new ClassWriter();
		for (SiftDock dock : docks) {
			writer.writeObject(dock);
		}
		byte[] b = writer.effuse();
		// 元数据标识
		MetaTag tag = createDockTag(0);
		// 写入中间数据锚点
		write(tag, b, 0, b.length);
	}

	/**
	 * 保存中间数据锚点
	 * @param array SiftArea数组
	 */
	protected void addSiftDocks(SiftArea[] array) throws TaskException {
		// 空数组不处理
		if (Laxkit.isEmpty(array)) {
			return;
		}

		ArrayList<SiftDock> docks = new ArrayList<SiftDock>();
		for (int i = 0; i < array.length; i++) {
			SiftArea e = array[i];
			for (SiftField field : e.list()) {
				SiftDock dock = new SiftDock(field.getSource(), field.getSpace());
				docks.add(dock);
			}
		}
		// 保留中间数据锚点
		SiftDock[] a = new SiftDock[docks.size()];
		addSiftDocks(docks.toArray(a));
	}

	/**
	 * 保存中间数据锚点
	 * @param array SiftArea列表
	 */
	protected void addSiftDocks(List<SiftArea> array)
			throws TaskException {
		// 不处理空指针或者空列表
		if (array == null || array.isEmpty()) {
			return;
		}

		SiftArea[] areas = new SiftArea[array.size()];
		addSiftDocks(array.toArray(areas));
	}


//	public abstract SiftObject assort(SiftObject sift, File[] files)
//			throws TaskException;

//	public abstract SiftObject assort(SiftObject sift, byte[] b, int off,
//			int len) throws TaskException;

	/**
	 * 分析SCAN阶段产生的元数据，结合SIFT根对象要求，产生SIFT阶段会话（SiftSession），赋值到SIFT根对象。
	 * @param sift SIFT根对象
	 * @param files 在磁盘上的SCAN阶段元数据文件
	 * @return 赋值后的SIFT根对象
	 * @throws TaskException
	 */
	public abstract SiftObject scan(SiftObject sift, File[] files)
			throws TaskException;

	/**
	 * 分析SCAN阶段产生的元数据，结合SIFT根对象要求，产生SIFT阶段会话（SiftSession），赋值到SIFT根对象。
	 * @param sift SIFT根对象
	 * @param b SCAN阶段元数据字节数组
	 * @param off 开始下标
	 * @param len 有效字节长度
	 * @return 赋值后的SIFT根对象
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract SiftObject scan(SiftObject sift, byte[] b, int off, int len)
			throws TaskException;

	/**
	 * 分析上个SIFT阶段产生的元数据，结合当前SIFT子对象要求，产生SIFT子阶段会话（SiftSession），赋值到这个SIFT子对象中。<br>
	 * 这是SIFT阶段的迭代操作，在SIFT/SUBSIFT、SUBSIFT/SUBSIFT之间发生，非迭代的ESTABLISH跳过这一步。<br>
	 * 
	 * @param subsift SIFT子对象
	 * @param files 在磁盘上的SIFT阶段元数据文件
	 * @return 赋值后的SIFT子对象
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract SiftObject sift(SiftObject subsift, File[] files)
			throws TaskException;

	/**
	 * 分析上个SIFT阶段产生的元数据，结合当前SIFT子对象要求，产生SIFT子阶段会话（SiftSession），赋值到这个SIFT子对象中。<br>
	 * 这是SIFT阶段的迭代操作，在SIFT/SUBSIFT、SUBSIFT/SUBSIFT之间发生，非迭代的ESTABLISH跳过这一步。<br>
	 * 
	 * @param subsift SIFT子对象
	 * @param b SIFT阶段元数据字节数组
	 * @param off 开始下标
	 * @param len 有效字节长度
	 * @return 赋值后的SIFT子对象
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract SiftObject sift(SiftObject subsift, byte[] b, int off,
			int len) throws TaskException;

	/**
	 * 分析SIFT最后阶段产生的元数据，结合RISE对象要求，产生RISE阶段会话（RiseSession），赋值到RISE对象中。
	 * 
	 * @param rise RISE对象
	 * @param files SIFT阶段元数据
	 * @return 赋值后的RISE对象
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract RiseObject rise(RiseObject rise, File[] files)
			throws TaskException;

	/**
	 * 分析SIFT最后阶段产生的元数据，结合RISE对象要求，产生RISE阶段会话（RiseSession），赋值到RISE对象中。
	 * 
	 * @param rise RISE对象
	 * @param b SIFT阶段元数据字节数组
	 * @param off 开始下标
	 * @param len 有效字节长度
	 * @return 赋值后的RISE对象
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract RiseObject rise(RiseObject rise, byte[] b, int off,
			int len) throws TaskException;

}