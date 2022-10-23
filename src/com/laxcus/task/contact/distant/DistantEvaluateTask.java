/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.distant;

import java.io.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;

/**
 * 快速计算的DISTANT(CONVERGE)阶段任务的数据计算实例。<br><br>
 * 
 * 流程：<br>
 * <1> "evaluate"方法接受来自上一次WORK节点的数据。<br>
 * <2> 按照自定义规则对数据进行计算和分片，结果可能通过磁盘委托器写入硬盘。<br>
 * <3> "effuse/flushTo"方法输出计算结果(最后计算结果，或者FluxArea元数据)。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 5/10/2020
 * @since laxcus 1.0
 */
public abstract class DistantEvaluateTask extends DistantTask {

	/**
	 * 构造DISTANT阶段的数据计算实例
	 */
	protected DistantEvaluateTask() {
		super(DistantMode.EVALUATE);
	}
	
	/**
	 * 从磁盘读取数据，执行数据计算工作
	 * @param field 数据片段元信息
	 * @param file 磁盘文件，来自WORK节点。
	 * @return 成功返回真，否则假
	 * @throws TaskException
	 */
	protected boolean defaultEvaluate(FluxField field, File file) throws TaskException {
		Logger.debug(getIssuer(), this, "defaultEvaluate", "file length: %d", file.length());

		// 读磁盘文件
		byte[] b = new byte[(int) file.length()];
		// 读磁盘文件，存在安全异常的可能
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b, 0, b.length);
			in.close();
		} catch (Throwable e) {
			throw new DistantTaskException(e);
		}
		return evaluate(field, b, 0, b.length);
	}

	/**
	 * 向任务注入一段数据(本次分片信息和数据流组成，数据取自DATA节点或者上一次WORK节点)，
	 * 处理结果可能通过磁盘委托器写入磁盘。允许用户任意多次调用这个方法。<br>
	 * 
	 * @param field 数据片段元信息，说明实际数据所在的WORK节点。
	 * @param b 与元信息对应的数据实体，以字节数组输入
	 * @param off 数据实体的字节数组开始位置
	 * @param len 数据实体的有效数据长度
	 * @return 成功返回真，否则假
	 */
	public abstract boolean evaluate(FluxField field, byte[] b, int off, int len) throws TaskException;

	/**
	 * 根据传入的FluxField和它对应的实体数据文件，进行计算。
	 * 实体数据来源于DATA站点，或者上次的WORK站点，计算结果写入磁盘或者内存。<br>
	 * 
	 * 由于分布任务组件受到安全管理的限制，调用这个方法时，文件的读权限需要在policy文件中指定。
	 * 
	 * @param field 数据片段信息
	 * @param file 关联的磁盘文件
	 * @return 成功返回真，否则假
	 * @throws TaskException
	 */
	public abstract boolean evaluate(FluxField field, File file) throws TaskException;

	/**
	 * 数据汇总操作 <br> 
	 * “assemble”方法在“evaluate”方法之后执行，是对一次数据计算的数据汇总。
	 * 它介于evaluate和effuse/flushDistant之间，只能调用一次。<br>  
	 * 
	 * @return 返回待输出数据的字节数组长度（如果有子级迭代任务，返回的是FluxArea字节数组长度，否则是实体数据长度）。
	 * @throws TaskException
	 */
	public abstract long assemble() throws TaskException;

}