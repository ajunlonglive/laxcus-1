/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.from;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.command.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.task.*;
import com.laxcus.task.flux.*;

/**
 * <code>DIFFUSE/CONVERGE</code>分布计算在<code>DIFFUSE(FROM)</code>阶段的任务接口。<br><br>
 * 
 * 方法说明：<br>
 * <1> <code>divide</code>方法。数据以字节数组的形式输入，在方法内进行解析，按照用户自定义需求实现数据分割。数据分割的标识是：任务号(taskid)+模值(mod)。此方法可以任意调用多次。<br>
 * <2> <code>assemble</code>方法。当全部“divide”方法完成后，进行的数据聚合/汇总操作。如将被分割的数据写入磁盘/内存。每次数据处理只能调用一次“assemble”方法。<br>
 * <3> <code>effuse</code>方法。收集"divide"方法分割的数据记录，形成FluxArea实例输出。<br>
 * <4> <code>flushTo</code>方法。将FluxArea数据流输出到磁盘上。运行过程中，effuse/flushTo只能二选一，调用其中一个。<br>
 * 
 * @author scott.liang
 * @version 1.3 9/23/2012
 * @since laxcus 1.0
 */
public abstract class FromTask extends FluxTask {

	/** FROM阶段资源代理 **/
	private FromTrustor fromTrustor;

	/**
	 * 构造默认的FROM阶段任务实例
	 */
	protected FromTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.FluxTask#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		fromTrustor = null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#getCommand()
	 */
	@Override
	public FromStep getCommand() {
		return (FromStep) super.getCommand();
	}

	/**
	 * 返回FROM阶段会话，FROM对应DIFFUSE阶段。
	 * @return FromSession实例
	 */
	public FromSession getSession() {
		FromStep cmd = getCommand();
		return cmd.getSession();
	}

	/**
	 * 返回FROM阶段任务迭代编号 <br>
	 * 按照DIFFUSE/CONVERGE算法，DIFFUSE只执行一个，所以它的迭代编号总是-1
	 * 
	 * @return 迭代编号
	 */
	public int getIterateIndex() {
		FromSession session = getSession();
		return session.getIterateIndex();
	}

	/**
	 * 设置FROM阶段资源代理（FromTaskPool赋值，只限类内部使用）
	 * @param e FromTrustor实例
	 */
	protected void setFromTrustor(FromTrustor e) {
		fromTrustor = e;
	}

	/**
	 * 返回FROM阶段资源代理
	 * @return FromTrustor实例
	 */
	protected FromTrustor getFromTrustor() {
		return fromTrustor;
	}

	/**
	 * 判断会话中有指定类型的命令
	 * @param clazz 类类型
	 * @return 返回真或者假
	 */
	public boolean hasSessionCommand(Class<?> clazz) {
		FromSession session = getSession();
		return session != null && session.isCommand(clazz);
	}

	/**
	 * 建立一个新的FluxArea
	 * @return FluxArea实例
	 * @throws TaskException - 如果发生异常
	 */
	protected FluxArea createFluxArea() throws TaskException {
		FluxWriter writer = fetchWriter();
		FluxArea area = writer.collect();
		// FROM迭代编号是-1
		area.setIterateIndex(getIterateIndex());
		// 返回结果
		return area;
	}

	/**
	 * 查找账号所属的表配置
	 * @param space 数据表名
	 * @return 返回表实例或者空指针
	 * @throws FromTaskException
	 */
	protected Table findTable(Space space) throws TaskException {
		Table table = getFromTrustor().findFromTable(getInvokerId(), space);
		if (table == null) {
			throw new FromTaskException("cannot be find \"%s\"", space);
		}
		return table;
	}

	/**
	 * 数据分割。<br><br>
	 * 
	 * DATA节点执行的数据切割操作，这个方法由用户调用处理。外部方法中调用它一次。<br>
	 * 用户的数据分割按照自己的规则进行，可以调用本地接口，但是不允许产生网络通信。<br>
	 * 被分割数据，以任务号(taskid)+模值(mod)的方式，将数据结果（实体数据）写入磁盘，
	 * 而返回元数据的字节长度。<br>
	 * 
	 * @return 返回FluxArea的字节数组长度（元数据字节长度）
	 * @throws TaskException
	 */
	public abstract long divide() throws TaskException;

	/**
	 * 生成数据映像，返回它的元数据字节数组（通常是FluxArea字节数组） <br>
	 * “effuse”在“assemble”之后执行。
	 * 
	 * @return 元数据字节数组
	 * @throws TaskException
	 */
	public abstract byte[] effuse() throws TaskException;

	/**
	 * 将FluxArea数据流输出到指定的文件。
	 * @param file  磁盘文件
	 * @return 返回写入磁盘的数据长度
	 * @throws TaskException
	 */
	public abstract long flushTo(File file) throws TaskException;

}