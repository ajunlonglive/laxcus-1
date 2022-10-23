/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.distant;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.distribute.contact.command.*;
import com.laxcus.distribute.contact.session.*;
import com.laxcus.task.*;
import com.laxcus.task.flux.*;
import com.laxcus.util.*;

/**
 * <code>CONTACT</code>分布计算在<code>DISTANT</code>阶段任务。<br>
 * CONTACT是客户机/服务器模式的分布计算。<br>
 * 
 * 方法说明：只有一个"process"方法，产生原始数据
 * 
 * @author scott.liang
 * @version 1.0 5/3/2020
 * @since laxcus 1.0
 */
public abstract class DistantTask extends FluxTask {

	/** DISTANT阶段的工作模式，见com.laxcus.distribute.contact.DistantMode中的定义 **/
	private int mode;

	/** DISTANT阶段资源代理 */
	private DistantTrustor distantTrustor;

	/**
	 * 构造默认和私有的DISTANT阶段计算实例
	 */
	protected DistantTask() {
		super();
	}

	/**
	 * 构造DISTANT阶段计算实例，并且指定它的工作模式
	 * @param mode 工作模式
	 */
	protected DistantTask(int mode) {
		this();
		setMode(mode);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.FluxTask#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		distantTrustor = null;
	}

	/**
	 * 设置DISTANT阶段工作模式。无效弹出异常
	 * @param who DISTANT阶段工作模式
	 * @throws IllegalValueException
	 */
	private void setMode(int who) {
		if (!DistantMode.isMode(who)) {
			throw new IllegalValueException("illegal mode:%d", who);
		}
		mode = who;
	}

	/**
	 * 返回DISTANT阶段工作模式，见DistantMode中定义。
	 * @return DISTANT工作模式
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * 判断是“产生数据”状态
	 * @return 成功返回真，否则假
	 */
	public boolean isGenerate() {
		return DistantMode.isGenerate(mode);
	}

	/**
	 * 判断是“计算数据”状态
	 * @return 成功返回真，否则假
	 */
	public boolean isEvaluate() {
		return DistantMode.isEvaluate(mode);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#getCommand()
	 */
	@Override
	public DistantStep getCommand() {
		return (DistantStep) super.getCommand();
	}

	/**
	 * 设置DISTANT阶段资源代理（DistantTaskPool赋值，只限类内部使用）
	 * @param e DistantTrustor实例
	 */
	protected void setDistantTrustor(DistantTrustor e) {
		distantTrustor = e;
	}

	/**
	 * 返回DISTANT阶段资源代理
	 * @return DistantTrustor实例
	 */
	protected DistantTrustor getDistantTrustor() {
		return distantTrustor;
	}

	/**
	 * 返回当前DISTANT阶段会话
	 * @return DistantSession实例
	 */
	public DistantSession getSession() {
		DistantStep cmd = getCommand();
		return cmd.getSession();
	}

	/**
	 * 判断当前任务有针对子级的数据分区（分区用来对下一级数据进行分割）
	 * @return 返回真或者假
	 */
	public boolean hasSessionSector() {
		DistantSession session = getSession();
		return session != null && session.hasIndexSector();
	}

	/**
	 * 判断会话中有指定类型的命令
	 * @param clazz 类类型
	 * @return 返回真或者假
	 */
	public boolean hasSessionCommand(Class<?> clazz) {
		DistantSession session = getSession();
		return session != null && session.isCommand(clazz);
	}

	/**
	 * 返回当前会话的迭代编号
	 * @return 迭代编号
	 */
	public int getIterateIndex() {
		DistantSession session = getSession();
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
	 * @throws DistantTaskException
	 */
	protected Table findTable(Space space) throws TaskException {
		Table table = getDistantTrustor().findDistantTable(getInvokerId(), space);
		if (table == null) {
			throw new DistantTaskException("cannot be find '%s'", space);
		}
		return table;
	}

//	/**
//	 * 将数据输出到指定的文件。在这之前，这个文件应该获得写权限。组件文件的沙箱权限配置在“site.policy”文件里面定义。 
//	 * @param file 指定的磁盘文件
//	 * @param append 添加模式（如果文件存在，数据追加到最后；否则建立一个新文件再写入）
//	 * @param b 字节数组
//	 * @param off 开始下标
//	 * @param len 有效长度
//	 * @return 返回写入数据的长度
//	 * @throws TaskException 可能产生的异常
//	 */
//	public long writeTo(File file, boolean append, byte[] b, int off, int len) throws TaskException {
//		long seek = 0L; // 断点
//		// 如果是追加状态，并且文件存在时，记录文件长度
//		if (append && (file.exists() && file.isFile())) {
//			seek = file.length();
//		}
//
//		try {
//			FileOutputStream out = new FileOutputStream(file, append);
//			out.write(b, off, len);
//			out.flush();
//			out.close();
//		} catch (IOException e) {
//			throw new TaskException(e);
//		}
//		// 写入长度
//		long size = file.length() - seek;
//
////		Logger.debug(getIssuer(), this, "writeTo", "write '%s' size:%d ", file, size);
//
//		return size;
//	}
	
//	/**
//	 * 将计算结果数据输出到指定文件 <br>
//	 * 这个方法是对“effuse”方法的补充，避免因为effuse产生的数据量过大，可能导致的内存溢出 <br><br>
//	 * 
//	 * 注意：<br>
//	 * 1. 由于分布任务组件在沙箱中运行，受到安全管理的限制，这个文件路径必须是可写的，否则将引发安全异常。写权限在WORK站点的“site.policy”文件中设置。<br>
//	 * 2. 外部程序成功调用这个方法后，文件的删除工作由外部程序来执行，分布任务组件不再负责。<br>
//	 * 
//	 * @param file 磁盘文件
//	 * @return 返回写入磁盘的数据长度
//	 * @throws TaskException 分布任务组件异常
//	 */
//	public long defaultFlushTo(File file) throws TaskException {
//		// 转为字节数组
//		byte[] b = effuse();
//		// 输出到指定的文件
//		return	writeTo(file, false, b, 0, b.length);
//	}

	/**
	 * 输出DISTANT阶段计算过程中产生的数据 <br>
	 * 这是一个DISTANT阶段任务计算的最后调用，此操作完成后，一个DISTANT阶段计算组件将被释放。 <br><br>
	 * 
	 * 数据输出分为两种情况：<br>
	 * 1. 如果是DISTANT迭代序列链条的最后一个，将输出它的最终计算结果。<br>
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
	 * 1. 由于分布任务组件在沙箱中运行，受到安全管理的限制，这个文件路径必须是可写的，
	 * 否则将引发安全异常。写权限在WORK站点的“site.policy”文件中设置。<br>
	 * 2. 外部程序成功调用这个方法后，文件的删除工作由外部程序来执行，分布任务组件不再负责。<br>
	 * 
	 * @param file 磁盘文件
	 * @return 返回写入磁盘的数据长度
	 * @throws TaskException - 分布任务组件异常
	 */
	public abstract long flushTo(File file) throws TaskException;

//	/**
//	 * 查找账号所属的表配置
//	 * @param space 数据表名
//	 * @return 返回表实例或者空指针
//	 * @throws DistantTaskException
//	 */
//	protected Table findTable(Space space) throws TaskException {
//		Table table = getDistantTrustor().findTable(getInvokerId(), space);
//		if (table == null) {
//			throw new DistantTaskException("cannot be find \"%s\"", space);
//		}
//		return table;
//	}
//
//	/**
//	 * 将数据输出到指定的文件。在这之前，这个文件应该获得写权限。组件文件的沙箱权限配置在“site.policy”文件里面定义。 
//	 * @param file 指定的磁盘文件
//	 * @param append 添加模式（如果文件存在，数据追加到最后；否则建立一个新文件再写入）
//	 * @param b 字节数组
//	 * @param off 开始下标
//	 * @param len 有效长度
//	 * @return 返回写入数据的长度
//	 * @throws TaskException 可能产生的异常
//	 */
//	public long writeTo(File file, boolean append, byte[] b, int off, int len) throws TaskException {
//		long seek = 0L; // 断点
//		// 如果是追加状态，并且文件存在时，记录文件长度
//		if (append && (file.exists() && file.isFile())) {
//			seek = file.length();
//		}
//
//		try {
//			FileOutputStream out = new FileOutputStream(file, append);
//			out.write(b, off, len);
//			out.flush();
//			out.close();
//		} catch (IOException e) {
//			throw new TaskException(e);
//		}
//		// 写入长度
//		long size = file.length() - seek;
//
//		Logger.debug(getIssuer(), this, "writeTo", "write '%s' size:%d ", file, size);
//
//		return size;
//	}
//	
//	/**
//	 * 将计算结果数据输出到指定文件 <br>
//	 * 这个方法是对“effuse”方法的补充，避免因为effuse产生的数据量过大，可能导致的内存溢出 <br><br>
//	 * 
//	 * 注意：<br>
//	 * 1. 由于分布任务组件在沙箱中运行，受到安全管理的限制，这个文件路径必须是可写的，否则将引发安全异常。写权限在WORK站点的“site.policy”文件中设置。<br>
//	 * 2. 外部程序成功调用这个方法后，文件的删除工作由外部程序来执行，分布任务组件不再负责。<br>
//	 * 
//	 * @param file 磁盘文件
//	 * @return 返回写入磁盘的数据长度
//	 * @throws TaskException 分布任务组件异常
//	 */
//	public long defaultFlushTo(File file) throws TaskException {
//		// 转为字节数组
//		byte[] b = effuse();
//		// 输出到指定的文件
//		return	writeTo(file, false, b, 0, b.length);
//	}
//	
//	/**
//	 * 执行首次CONTACT计算。<br>
//	 * 
//	 * 注意，只在首次处理时调用！
//	 * 
//	 * @return 返回产生的字节数
//	 * @throws TaskException 快捷组件异常
//	 */
//	public abstract long process() throws TaskException;
//
//	/**
//	 * 输出DISTANT阶段处理后产生的结果数据 <br>
//	 * 
//	 * @return 输出字节数组
//	 * @throws TaskException
//	 */
//	public abstract byte[] effuse() throws TaskException;
//
//	/**
//	 * 将计算结果数据输出到指定文件 <br>
//	 * 这个方法是对“effuse”方法的补充，避免因为effuse产生的数据量过大，可能导致的内存溢出 <br><br>
//	 * 
//	 * 注意：<br>
//	 * 1. 由于分布任务组件在沙箱中运行，受到安全管理的限制，这个文件路径必须是可写的，否则将引发安全异常。写权限在WORK站点的“site.policy”文件中设置。<br>
//	 * 2. 外部程序成功调用这个方法后，文件的删除工作由外部程序来执行，分布任务组件不再负责。<br>
//	 * 
//	 * @param file 磁盘文件
//	 * @return 返回写入磁盘的数据长度
//	 * @throws TaskException - 分布任务组件异常
//	 */
//	public abstract long flushTo(File file) throws TaskException;
//
//	/**
//	 * 从磁盘读取数据，执行数据计算工作
//	 * @param field 数据片段元信息
//	 * @param file 磁盘文件，来自DATA/WORK节点。
//	 * @return 成功返回真，否则假
//	 * @throws TaskException
//	 */
//	protected boolean defaultEvaluate(FluxField field, File file) throws TaskException {
//		Logger.debug(getIssuer(), this, "defaultEvaluate", "file length: %d", file.length());
//
//		// 读磁盘文件
//		byte[] b = new byte[(int) file.length()];
//		// 读磁盘文件，存在安全异常的可能
//		try {
//			FileInputStream in = new FileInputStream(file);
//			in.read(b, 0, b.length);
//			in.close();
//		} catch (Throwable e) {
//			throw new DistantTaskException(e);
//		}
//		return evaluate(field, b, 0, b.length);
//	}
//	
//	/**
//	 * 向任务注入一段数据(本次分片信息和数据流组成，数据取自DATA节点或者上一次WORK节点)，
//	 * 处理结果可能通过磁盘委托器写入磁盘。允许用户任意多次调用这个方法。<br>
//	 * 
//	 * @param field 数据片段元信息，说明实际数据所在的DATA/WORK节点。
//	 * @param b 与元信息对应的数据实体，以字节数组输入
//	 * @param off 数据实体的字节数组开始位置
//	 * @param len 数据实体的有效数据长度
//	 * @return 成功返回真，否则假
//	 */
//	public abstract boolean evaluate(FluxField field, byte[] b, int off, int len) throws TaskException;
//
//	/**
//	 * 根据传入的FluxField和它对应的实体数据文件，进行计算。
//	 * 实体数据来源于DATA站点，或者上次的WORK站点，计算结果写入磁盘或者内存。<br>
//	 * 
//	 * 由于分布任务组件受到安全管理的限制，调用这个方法时，文件的读权限需要在policy文件中指定。
//	 * 
//	 * @param field 数据片段信息
//	 * @param file 关联的磁盘文件
//	 * @return 成功返回真，否则假
//	 * @throws TaskException
//	 */
//	public abstract boolean evaluate(FluxField field, File file) throws TaskException;
//
//	/**
//	 * 数据汇总操作 <br> 
//	 * “assemble”方法在“evaluate”方法之后执行，是对一次数据计算的数据汇总。它介于evaluate和effuse/flushTo之间，只能调用一次。<br>  
//	 * 
//	 * @return 返回待输出数据的字节数组长度（如果有子级迭代任务，返回的是FluxArea字节数组长度，否则是实体数据长度）。
//	 * @throws TaskException
//	 */
//	public abstract long assemble() throws TaskException;


}