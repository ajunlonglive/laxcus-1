/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.to;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.command.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.task.*;
import com.laxcus.task.flux.*;
import com.laxcus.util.*;

/**
 * <code>DIFFUSE/CONVERGE</code>分布计算在<code>TO(CONVERGE)</code>阶段的任务接口。<br>
 * TO阶段任务是迭代的，一次DIFFUSE/CONVERGE分布计算可以执行任意多次TO阶段处理。<br><br>
 * 
 * TO阶段计算三个步骤：<br>
 * 1. 根据传入数据进行计算（divide/evaluate）<br>
 * 2. 对数据结果进行汇总处理（assemble）<br>
 * 3. 输出数据（effuse/flushTo）<br><br>
 * 
 * ToTask根据DIFFUSE/CONVERGE分布计算的基础原理：产生/计算+分散/聚合设计。在它之下提供两个子类：ToGenerateTask、ToEvaluateTask。所有TO阶段任务实例必须从这两个类派生，而不是从ToTask派生。<br>
 * 
 * ToGenerateTask可以看作是FromTask在TO阶段的实现，方法包括：“divide/assemble/effuse/flushTo”，“divide”方法也是产生数据和对数据进行分片。与FromTask不同的是，ToGenerateTask部署在WORK站点，可以连接DATA站点检索数据。<br>
 * 
 * ToEvaluateTask是在已经分片的数据基础上进行数据计算工作，方法包括：“evaluate/assemble/effuse/flushTo”。数据有三种来源:FromTask、ToGenerateTask、上次的ToEvaluateTask。<br>
 * 
 * 处理过程中，外部接口可以多次调用“divide/evaluate”方法，但是“assemble/effuse/flushTo”方法只在最后调用一次，其中“effuse/flushTo”是数据输出，二选一。<br><br>
 * 
 * ToGenerateTask和ToEvaluateTask在处理完成后，都调用“effuse/flushTo”接口，ToGenerateTask返回FluxArea的字节数组，ToEvaluateTask在最后一个子类前，返回FluxArea的字节数组，最后一次返回实体数据（最终计算结果）。<br><br>
 * 
 * GENERATE和EVALUATE在TO迭代过程中，出现的位置和频率由用户根据需求定义。即一个ToGenerateTask实例之后，可以是另一个ToGenerateTask实例，或者ToEvaluateTask实例。ToEvaluateTask也是一样的情况。但是通常迭代的最后一个都是ToEvaluateTask实例。<br><br>
 * 
 * <b>特别注意：由于分布任务组件在沙箱中运行，ToTask的所有抽象方法都有使用FluxWriter/FluxReader执行磁盘读写操作的可能，这需要在DATA/WORK站点的“conf/site.policy”文件中设置规定的磁盘目录和读写权限，在运行时才能通过沙箱检查。</b><br>
 * 
 * 
 * @author scott.liang
 * @version 1.3 07/03/2014
 * @since laxcus 1.0
 */
public abstract class ToTask extends FluxTask {

	/** TO阶段的工作模式，见com.laxcus.distribute.conduct.ToMode中的定义 **/
	private int mode;

	/** TO阶段资源代理 */
	private ToTrustor toTrustor;

	/**
	 * 构造默认和私有的TO阶段计算实例
	 */
	private ToTask() {
		super();
	}

	/**
	 * 构造TO阶段计算实例，并且指定它的工作模式
	 * @param mode 工作模式
	 */
	protected ToTask(int mode) {
		this();
		setMode(mode);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.FluxTask#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		toTrustor = null;
	}

	/**
	 * 设置TO阶段工作模式。无效弹出异常
	 * @param who TO阶段工作模式
	 * @throws IllegalValueException
	 */
	private void setMode(int who) {
		if(!ToMode.isMode(who)) {
			throw new IllegalValueException("illegal mode:%d", who);
		}
		mode = who;
	}

	/**
	 * 返回TO阶段工作模式，见ToMode中定义。
	 * @return TO工作模式
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * 判断是“产生数据”状态
	 * @return 成功返回真，否则假
	 */
	public boolean isGenerate() {
		return ToMode.isGenerate(mode);
	}

	/**
	 * 判断是“计算数据”状态
	 * @return 成功返回真，否则假
	 */
	public boolean isEvaluate() {
		return ToMode.isEvaluate(mode);
	}

	/**
	 * 返回TO阶段命令
	 * @see com.laxcus.task.DistributedTask#getCommand()
	 */
	@Override
	public ToStep getCommand() {
		return (ToStep) super.getCommand();
	}

	/**
	 * 设置TO阶段资源代理(ToTaskPool赋值，只限内部使用)
	 * @param e ToTrustor实例
	 */
	protected void setToTrustor(ToTrustor e) {
		toTrustor = e;
	}

	/**
	 * 返回TO阶段资源代理
	 * @return ToTrustor实例
	 */
	protected ToTrustor getToTrustor() {
		return toTrustor;
	}

	/**
	 * 返回当前TO阶段会话
	 * @return ToSession实例
	 */
	public ToSession getSession() {
		ToStep cmd = getCommand();
		return cmd.getSession();
	}

	/**
	 * 判断当前任务有针对子级的数据分区（分区用来对下一级数据进行分割）
	 * @return 返回真或者假
	 */
	public boolean hasSessionSector() {
		ToSession session = getSession();
		return session != null && session.hasIndexSector();
	}

	/**
	 * 判断会话中有指定类型的命令
	 * @param clazz 类类型
	 * @return 返回真或者假
	 */
	public boolean hasSessionCommand(Class<?> clazz) {
		ToSession session = getSession();
		return session != null && session.isCommand(clazz);
	}

	/**
	 * 返回当前会话的迭代编号
	 * @return 迭代编号
	 */
	public int getIterateIndex() {
		ToSession session = getSession();
		return session.getIterateIndex();
	}

	/**
	 * 根据内存记录，建立分布计算区
	 * @return 返回FluxArea实例
	 * @throws TaskException
	 */
	protected FluxArea createFluxArea() throws TaskException {
		FluxWriter writer = fetchWriter();
		FluxArea area = writer.collect();
		// 设置迭代编号（必须！）
		area.setIterateIndex(getIterateIndex());
		// 返回结果
		return area;
	}

	/**
	 * 查找账号所属的表配置
	 * @param space 数据表名
	 * @return 返回表实例。如果签名校验错误，或者不存在时，弹出异常。
	 * @throws ToTaskException
	 */
	protected Table findTable(Space space) throws TaskException {
		Table table = getToTrustor().findToTable(getInvokerId(), space);
		if (table == null) {
			throw new ToTaskException("cannot be find '%s'", space);
		}
		return table;
	}

	/**
	 * 输出TO阶段计算过程中产生的数据 <br>
	 * 这是一个TO阶段任务计算的最后调用，此操作完成后，一个TO阶段计算组件将被释放。 <br><br>
	 * 
	 * 数据输出分为两种情况：<br>
	 * 1. 如果是TO迭代序列链条的最后一个，将输出它的最终计算结果。<br>
	 * 2. 如果不是，将返回FluxArea格式化后的字节数组。<br>
	 * 
	 * @return 输出字节数组
	 * @throws TaskException
	 */
	public abstract byte[] effuse() throws TaskException;

	/**
	 * 将计算结果数据输出到指定文件 <br>
	 * 这个方法是对“effuse”方法的补充，避免因为effuse产生的数据量过大，可能导致的内存溢出 <br><br>
	 * 
	 * 注意：<br>
	 * 1. 由于分布任务组件在沙箱中运行，受到安全管理的限制，这个文件路径必须是可写的，否则将引发安全异常。写权限在WORK站点的“site.policy”文件中设置。<br>
	 * 2. 外部程序成功调用这个方法后，文件的删除工作由外部程序来执行，分布任务组件不再负责。<br>
	 * 
	 * @param file 磁盘文件
	 * @return 返回写入磁盘的数据长度
	 * @throws TaskException - 分布任务组件异常
	 */
	public abstract long flushTo(File file) throws TaskException;
}