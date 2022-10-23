/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.balance;

import java.io.*;
import java.util.*;

import com.laxcus.command.conduct.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.meta.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 数据平衡计算和资源分配接口。<br><br>
 * 
 * 任务实例工作在FROM阶段之后，每个TO阶段之前，包括两项主要工作：<br>
 * <1> 根据传入的字节流数组，解析资源在各节点上的分布记录，以此为基础进行平衡计算分配。<br>
 * <2> 按照要求分配下阶段(一定是TO阶段)的网络计算资源，如分布数据节点地址，再下一阶段的分片信息等。<br>
 *
 * @author scott.liang 
 * @version 1.2 9/23/2012
 * @since laxcus 1.0
 */
public abstract class BalanceTask extends DesignTask {

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.DesignTask#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
	}

	/**
	 * 构造一个BALANCE阶段任务实例
	 */
	protected BalanceTask() {
		super();
	}

	/**
	 * 返回资源平衡对象实例
	 * @return BalanceObject实例
	 */
	public BalanceObject getObject() {
		Conduct conduct = super.getCommand();
		return conduct.getBalanceObject();
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
	 * 根据迭代编号，建立中间数据锚点标识
	 * @param iterateIndex 迭代编号
	 * @return 返回MetaTag实例
	 */
	private MetaTag createDockTag(int iterateIndex) {
		Phase phase = getPhase().duplicate(); // 生成一个副本
		phase.setSub("BALANCE_DOCK"); // 固定的子命名，这样可以区分与其它元数据标识的不同
		return createMetaTag(phase, iterateIndex);
	}

	/**
	 * 保存元数据
	 * @param phase 阶段命名
	 * @param iterateIndex 迭代编号
	 * @param b 元数据字节数组
	 * @param off 数组开始下标
	 * @param len 有效数据长度
	 * @return 写入成功返回真，否则假
	 * @throws TaskException
	 */
	protected boolean write(Phase phase, int iterateIndex, byte[] b, int off,
			int len) throws TaskException {
		MetaTag tag = createMetaTag(phase, iterateIndex);
		return write(tag, b, off, len);
	}

	/**
	 * 判断元数据存在
	 * @param phase 阶段命名
	 * @param iterateIndex 迭代编号
	 * @return 写入成功返回真，否则假
	 * @throws TaskException
	 */
	protected boolean contains(Phase phase, int iterateIndex)
	throws TaskException {
		MetaTag tag = createMetaTag(phase, iterateIndex);
		return contains(tag);
	}

	/**
	 * 读取元数据
	 * @param phase 阶段命名
	 * @param iterateIndex 迭代编号
	 * @return 写入成功返回真，否则假
	 * @throws TaskException
	 */
	protected byte[] read(Phase phase, int iterateIndex) throws TaskException {
		MetaTag tag = createMetaTag(phase, iterateIndex);
		return read(tag);
	}

	/**
	 * 输出当前保存的全部中间数据锚点
	 * @return FluxDock列表
	 */
	public List<FluxDock> getFluxDocks() throws TaskException {
		MetaTag tag = createDockTag(0);
		ArrayList<FluxDock> docks = new ArrayList<FluxDock>();

		byte[] b = read(tag);
		if (Laxkit.isEmpty(b)) {
			return docks;
		}

		// 解析参数
		ClassReader reader = new ClassReader(b);
		while (reader.hasLeft()) {
			FluxDock dock = new FluxDock(reader);
			docks.add(dock);
		}
		return docks;
	}

	/**
	 * 保存一批中间数据锚点
	 * @param docks 中间数据锚点数组
	 * @throws TaskException
	 */
	protected void addFluxDocks(FluxDock[] docks) throws TaskException {
		// 判断是空
		if (Laxkit.isEmpty(docks)) {
			throw new NullPointerException();
		}

		// 转为可类化数据
		ClassWriter writer = new ClassWriter();
		for (FluxDock dock : docks) {
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
	 * @param e FluxDock实例
	 */
	protected void addFluxDock(FluxDock e) throws TaskException {
		Laxkit.nullabled(e);

		addFluxDocks(new FluxDock[] { e });
	}

	/**
	 * 保存中间数据锚点
	 * @param node 站点地址
	 * @param taskId 任务编号
	 */
	protected void addFluxDock(Node node, long taskId) throws TaskException {
		addFluxDock(new FluxDock(node, taskId));
	}

	/**
	 * 从FluxArea中取出节点地址和任务编号，生成锚点保存它们
	 * @param areas 分布计算区
	 * @throws TaskException
	 */
	protected void addFluxDocks(FluxArea[] areas) throws TaskException {
		// 空数组不处理
		if (Laxkit.isEmpty(areas)) {
			return;
		}

		FluxDock[] docks = new FluxDock[areas.length];
		for (int i = 0; i < areas.length; i++) {
			FluxArea e = areas[i];
			docks[i] = new FluxDock(e.getSource(), e.getTaskId());
		}
		// 保留中间数据锚点
		addFluxDocks(docks);
	}

	/**
	 * 从中间数据区中提取参数，生成中间数据锚点，然后保存起来。
	 * @param array FluxArea列表
	 */
	protected void addFluxDocks(List<FluxArea> array) throws TaskException {
		// 不处理空指针或者空列表
		if (array == null || array.isEmpty()) {
			return;
		}

		FluxArea[] areas = new FluxArea[array.size()];
		addFluxDocks(areas);
	}

	/**
	 * 保存TO阶段处理过程中产生的元数据
	 * @param phase 阶段命名
	 * @param iterateIndex TO阶段迭代编号
	 * @param areas 分布数据计算区数组，生成为中间数据锚点
	 * @throws TaskException
	 */
	protected void addFluxAreas(Phase phase, int iterateIndex, FluxArea[] areas) throws TaskException {
		// 空数组不处理
		if (Laxkit.isEmpty(areas)) {
			return;
		}

		// 保存元数据
		ClassWriter writer = new ClassWriter();
		for (int i = 0; i < areas.length; i++) {
			writer.writeObject(areas[i]);
		}
		byte[] b = writer.effuse();
		// 保存元数据
		write(phase, iterateIndex, b, 0, b.length);
	}

	/**
	 * 判断FLUX映像数据存在
	 * @param phase 阶段命名
	 * @param iterateIndex 迭代编号
	 * @return 返回真或者假
	 * @throws TaskException
	 */
	protected boolean hasFluxArea(Phase phase, int iterateIndex)
	throws TaskException {
		return contains(phase, iterateIndex);
	}

	/**
	 * 解析分布资源的数据图谱，返回FluxArea对象数组
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 返回FluxArea数组
	 */
	public FluxArea[] splitFluxArea(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		// 解析 FluxArea
		ArrayList<FluxArea> array = new ArrayList<FluxArea>();
		while (reader.hasLeft()) {
			FluxArea area = new FluxArea(reader);
			array.add(area);
		}

		FluxArea[] areas = new FluxArea[array.size()];
		return array.toArray(areas);
	}

	/**
	 * 查询元数据记录
	 * @param phase 阶段命名
	 * @param iterateIndex 迭代编号
	 * @return FluxArea数组
	 */
	protected FluxArea[] findFluxArea(Phase phase, int iterateIndex) throws TaskException {
		// 读数据
		byte[] b = read(phase, iterateIndex);
		if (Laxkit.isEmpty(b)) {
			return null;
		}
		// 解析数据
		return splitFluxArea(b, 0, b.length);
	}


	/**
	 * 从磁盘读文件，调用默认的接口，去分派会话
	 * @param object 本次TO（SUBTO）阶段对象
	 * @param files FluxArea的文件格式
	 * @return 被分配后的对象
	 * @throws TaskException
	 */
	protected ToObject defaultAdmix(ToObject object, File[] files) throws TaskException {
		// 统计文件长度
		long len = 0;
		for(File file : files) {
			len += file.length();
		}
		// 读磁盘文件
		ClassWriter writer = new ClassWriter((int)len);
		for(File file : files) {
			byte[] b = readFile(file);
			writer.write(b);
		}

		byte[] b = writer.effuse();
		
		return admix(object, b, 0, b.length);
	}

	/**
	 * 根据上个阶段（FROM/TO/SUBTO）产生的元数据，结合本TO（SUBTO）阶段需求，产生关联的阶段会话（ToSession），赋值到本阶段TO对象中。
	 * 
	 * @param object 本次TO(SUBTO)阶段对象
	 * @param b 上个阶段（FROM/TO/SUBTO）产生的元数据集合(FluxArea)
	 * @param off 数组开始下标(一般从0开始)
	 * @param len 数组有效区域长度
	 * @return 返回分配参数后的本次TO(SUBTO)阶段对象
	 * @throws TaskException - 分布任务异常
	 */
	public abstract ToObject admix(ToObject object, byte[] b, int off, int len) throws TaskException;

	/**
	 * 根据上个阶段（FROM/TO/SUBTO）产生的元数据，结合本TO（SUBTO）阶段需求，产生关联的阶段会话（ToSession），赋值到本阶段TO对象中。
	 * 
	 * @param object 本次TO/SUBTO阶段对象
	 * @param files -上个阶段（FROM/TO/SUBTO）产生的元数据集合文件(FluxArea磁盘数据)  
	 * @return 返回分配参数后的本次TO(SUBTO)阶段对象
	 * @throws TaskException - 分布任务异常
	 */
	public abstract ToObject admix(ToObject object, File[] files) throws TaskException;
}