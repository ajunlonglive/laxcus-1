/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.flux;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.util.*;
import com.laxcus.task.talk.*;

/**
 * 数据计算的并行处理任务。<br>
 * 
 * 提供基于DIFFUSE/CONVERGE算法的数据计算规则设计和计算工作，子类包括FromTask、ToTask。<br><br>
 * 
 * 根据分布计算中的"移动计算"和"移动数据计算"的不同需要，FluxTask的工作包括"产生数据"、"分割数据"、"计算数据"三部分流程。<br><br>
 * 
 * FromTask属于"移动计算"，根据指令在DATA节点"产生数据"和"分割数据"，这些分割处理后的数据保存在本地的磁盘或者内存里。<br>
 * ToTask属于"移动数据计算"，主要完成"计算数据"的工作。相比FromTask，它有数据在网络传输中的时间消耗。<br><br>
 * 
 * FromTask的工作是：CALL节点向DATA节点发起命令，获取的数据保存在DATA节点，返回FluxArea元数据，分配给后续的TO阶段任务去计算。<br><br>
 * 
 * ToTask的工作有三种：<BR>
 * <1> 使用FromSession实例，在CALL节点向DATA节点发起命令，获取的数据保存在DATA节点。<br>
 * <2> 使用ToSession实例，在WORK节点向DATA节点发起命令，数据保存在WORK节点。<br>
 * <3> 从FROM阶段或者上级TO阶段获取需要的数据，在WORK节点进行计算。计算结果是FluxArea元数据或者结果数据。<br>
 * 
 * @author scott.liang
 * @version 1.3 11/9/2013
 * @since laxcus 1.0
 */
public abstract class FluxTask extends AccessTask {

	/** CONDUCT/CONTACT中间数据存取代理。只在类中可见 **/
	private FluxTrustor fluxTrustor;

	/** 动态交互代理 **/
	private TalkTrustor talkTrustor;

	/** 本次计算任务编号，初始值是-1。正常的任务编号是一个长整型正数 **/
	private long taskId;

	/**
	 * 构造默认的数据计算并行处理任务
	 */
	protected FluxTask() {
		super();
		taskId = -1L;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	public void destroy() {
		// 调用父类销毁
		super.destroy();
		// 清除
		fluxTrustor = null;
		talkTrustor = null;
		taskId = -1L;
	}
	
	/**
	 * 设置CONDUCT/CONTACT中间数据存取代理
	 * @param e FluxTrustor实例
	 */
	public void setFluxTrustor(FluxTrustor e) {
		fluxTrustor = e;
	}

	/**
	 * 返回CONDUCT/CONTACT中间数据存取代理
	 * @return FluxTrustor实例
	 */
	protected FluxTrustor getFluxTrustor() {
		return fluxTrustor;
	}

	/**
	 * 设置CONDUCT/CONTACT分布任务组件运行交互代理
	 * @param e TalkTrustor实例
	 */
	public void setTalkTrustor(TalkTrustor e) {
		talkTrustor = e;
	}

	/**
	 * 返回CONDUCT/CONTACT分布任务组件运行交互代理
	 * @return TalkTrustor实例
	 */
	protected TalkTrustor getTalkTrustor() {
		return talkTrustor;
	}

	/**
	 * 返回当前任务编号。小于0是无效。
	 * @return 长整数的任务编号
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * 统计实体数据尺寸。<br>
	 * 在最后输出数据时，外部接口根据这个方法，可以选择调用“effuse”或者“flushTo”方法。由此避免内存溢出的现象发生。
	 * 
	 * @return 返回实体数据长度（大于或者等于0）
	 * @throws TaskException - 在处理过程中发生的异常
	 */
	public long length() throws TaskException {
		// 任务编号无效，是0值
		if (taskId < 0L) {
			return 0L;
		}

		FluxWriter writer = fluxTrustor.findWriter(getInvokerId(), taskId);
		FluxArea area = writer.collect();
		long len = 0L;
		for (FluxField field : area.list()) {
			len += field.length();
		}
		return len;
	}

	/**
	 * 获得中间数据写入器。在第一次生成后，以后都将返回它。
	 * @param memory 内存模式
	 * @param capacity 内存容量（在memory为“真”时才生效）
	 * @return 返回FluxWriter实例
	 * @throws TaskException
	 */
	private FluxWriter fetchWriter(boolean memory, long capacity) throws TaskException {
		if (taskId == -1L) {
			taskId = fluxTrustor.createStack(getInvokerId(), memory, capacity);

//			Logger.debug(getIssuer(), this, "fetchWriter", "save to '%s'/%d, task id is %d",
//					(memory ? "MEMORY" : "DISK"), capacity, taskId);
		}
		if (taskId >= 0L) {
			return fluxTrustor.findWriter(getInvokerId(), taskId);
		}
		throw new TaskException("cannot be fetch FluxWriter");
	}

	/**
	 * 申请中间数据写入器。数据写入位置根据命令的“内存/磁盘”处理标识决定。
	 * @return 返回FluxWriter实例
	 * @throws TaskException
	 */
	protected FluxWriter fetchWriter() throws TaskException {
		boolean memory = super.isMemory();
		long size = (memory ? fluxTrustor.getMemberMemory(getInvokerId()) : -1L);
		return fetchWriter(memory, size);
	}

	/**
	 * 获得中间数据读取器。在第一次生成后，以后都将返回它
	 * @param memory 内存模式
	 * @param capacity 内存容量（在memory为“真”时才生效）
	 * @return 返回FluxReader实例
	 * @throws TaskException
	 */
	private FluxReader fetchReader(boolean memory, long capacity) throws TaskException {
		if (taskId == -1L) {
			taskId = fluxTrustor.createStack(getInvokerId(), memory, capacity);

//			Logger.debug(getIssuer(), this, "fetchReader", "save to '%s'/%d, task id is %d",
//					(memory ? "MEMORY" : "DISK"), capacity, taskId);
		}
		if (taskId >= 0L) {
			return fluxTrustor.findReader(getInvokerId(), taskId);
		}
		throw new TaskException("cannot be fetch FluxReader");
	}

	/**
	 * 获得默认的中间数据读取器。中间数据根据用户要求，参考计算机资源，从硬盘或者内存读出。
	 * @return 返回FluxReader实例
	 */
	protected FluxReader fetchReader() throws TaskException {
		boolean memory = super.isMemory();
		long size = (memory ? fluxTrustor.getMemberMemory(getInvokerId()) : -1L);
		return fetchReader(memory, size);
	}

	/**
	 * 将数据输出到指定的文件。在这之前，这个文件应该获得写权限。组件文件的沙箱权限配置在“site.policy”文件里面定义。 
	 * @param file 指定的磁盘文件
	 * @param append 添加模式（如果文件存在，数据追加到最后；否则建立一个新文件再写入）
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 返回写入数据的长度
	 * @throws TaskException - 可能产生的异常
	 */
	protected long writeTo(File file, boolean append, byte[] b, int off, int len) throws TaskException {
		long seek = 0L; // 断点
		// 如果是追加状态，并且文件存在时，记录文件长度
		if (append && (file.exists() && file.isFile())) {
			seek = file.length();
		}

		try {
			FileOutputStream out = new FileOutputStream(file, append);
			out.write(b, off, len);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new TaskException(e);
		}
		// 写入长度
		long size = file.length() - seek;

//		Logger.debug(getIssuer(), this, "writeTo", "write '%s' size:%d ", file, size);

		return size;
	}

	/**
	 * 按照索引分区要求，把记录分割入写入磁盘或者内存（按照用户命令要求写入媒介）
	 * 
	 * @param sector 列索引分区
	 * @param rows 记录集合
	 * @throws TaskException
	 */
	protected void splitWriteTo(ColumnSector sector, List<Row> rows) throws TaskException {
		Dock dock = sector.getDock();
		short columnId = dock.getColumnId();
		Space space = dock.getSpace();

		// 数据分组集合(分片下标.模 -> 记录集合)
		TreeMap<java.lang.Integer, RowBuffer> buffs = new TreeMap<java.lang.Integer, RowBuffer>();

		//1. 逐一提取记录，根据列值，按照分片下标分别保存
		for (Row row : rows) {
			Column column = row.find(columnId);
			if (column == null) {
				throw new TaskNotFoundException("cannot be find \"%s\"", dock);
			}
			// 根据列的值，找到它对应的分片下标位置。下标位置同时做为模值(mod)，这个非常关键!!!
			int index = sector.indexOf(column);

			// 以分片下标做为模(mod)，分组保存
			RowBuffer element = buffs.get(index);
			if (element == null) {
				element = new RowBuffer(index, space);
				buffs.put(index, element);
			}
			element.add(row);
		}

//		Logger.debug(getIssuer(), this, "splitWriteTo", "write to disk...");

		// 逐一提取数据，写入磁盘
		Iterator<Map.Entry<java.lang.Integer, RowBuffer>> iterator = buffs.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Integer, RowBuffer> entry = iterator.next();
			int mod = entry.getKey();
			
			RowBuffer buff = entry.getValue();
			// 生成数据流，写入磁盘
			byte[] b = buff.build();
			int elements = buff.size();

			// 拿到中间数据写入器句柄
			FluxWriter writer = fetchWriter();
			FluxField field = writer.append(mod, elements, b, 0, b.length);

			// 如果写入失败，弹出异常
			if (field == null) {
				throw new TaskException("cannot be write! taskId: %d", writer.getTaskId());
			}

//			Logger.debug(getIssuer(), this, "splitWriteTo", "write size is:%d, field '%s'", b.length, field);
		}
	}

	/**
	 * 检查目标站点上的关联组件状态。
	 * 
	 * @param remote 目标站点
	 * @param falg 实时会话标识
	 * @return 返回关联组件状态，包括：没有找到、等待处理、运行3种状态。
	 * @throws TaskException - 运行过程中发生分布计算异常
	 */
	protected TaskMoment check(Node remote, TalkFalg falg) throws TaskException {
		return talkTrustor.check(getInvokerId(), remote, falg);
	}

	/**
	 * 执行远程实时会话请求。<br>
	 * 方法通过RPC方式，投递到另一个节点，调用同级的“task”方法，返回会话协商结果。<br><br>
	 * 
	 * @param remote 目标站点
	 * @param quest 实时会话请求
	 * @return 返回实时会话应答结果
	 * @throws TaskException - 运行过程中发生分布计算异常
	 */
	protected TalkReply ask(Node remote, TalkQuest quest) throws TaskException {
		return talkTrustor.ask(getInvokerId(), remote, quest);
	}

	/**
	 * 同级关联组件之间的实时会话协商。<br>
	 * 本处是一个空方法，子类需要派生这个方法，实现具体的操作。<br>
	 * 
	 * @param quest 来自“ask”方法的实时会话请求
	 * @return 返回实时会话应答结果
	 */
	public TalkReply talk(TalkQuest quest) {
		return null;
	}

}