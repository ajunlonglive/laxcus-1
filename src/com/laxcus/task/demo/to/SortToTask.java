/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.to;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.util.classable.*;

/**
 * 整数排序实例的TO(CONVERGE)阶段任务。这里执行两个工作：<br>
 * (1) 从多个FROM节点上拿到被分配的整数值数组。<br>
 * (2) 对这些整数数组进行排序，排序分为升序或者降序，这个参数由用户在输入命令时指定。<br>
 * 
 * @author scott.liang
 * @version 1.0 05/09/2015
 * @since laxcus 1.0
 */
public class SortToTask extends ToEvaluateTask {

	/** 整型值数组 **/
	private List<Integer> array;

	/**
	 * 构造SortToTask实例
	 */
	public SortToTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, byte[], int, int)
	 */
	@Override
	public boolean evaluate(FluxField field, byte[] b, int off, int len) throws TaskException {
		// 分配内存空间
		if (array == null) {
			int capacity = (int) (field.length() >> 2);
			array = new ArrayList<Integer>(capacity);
		}

		// 保存参数，先不排序
		ClassReader reader = new ClassReader(b, off, len);
		while (reader.getLeft() > 0) {
			int digit = reader.readInt();
			array.add(digit);
		}

		Logger.debug(getIssuer(), this, "evaluate", "array size is %d", array.size());

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, java.io.File)
	 */
	@Override
	public boolean evaluate(FluxField field, File file) throws TaskException {
		Logger.debug(getIssuer(), this, "evaluate", "file length: %d", file.length());

		// 读磁盘文件
		byte[] b = new byte[(int) file.length()];
		// 读磁盘文件，存在安全异常的可能
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b, 0, b.length);
			in.close();
		} catch (Throwable e) {
			throw new ToTaskException(e);
		}
		return evaluate(field, b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {

//		// debug code, start
//		Logger.debug(getIssuer(), this, "assemble", "memory is %s", getCommand().isMemory());
//		super.getCommand().setMemory(false);
//		// debug code, end
		
		boolean desc = false;
		TaskParameter value = getSession().findParameter("orderby");
		if (value != null && value.getClass() == TaskString.class) {
			String text = ((TaskString) value).getValue();
			desc = text.matches("^\\s*(?i)DESC\\s*$");
		}

		// 选择升序或者降序排列
		IntegerComparator comparator = new IntegerComparator(desc);
		Collections.sort(array, comparator);

		// 统计输出的字节数组长度
		long size = array.size() * 4;

		Logger.debug(getIssuer(), this, "assemble", "size is %d", size);
		return size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		// 写入内存
		int size = array.size();
		ClassWriter writer = new ClassWriter(size * 4);
		for (int i = 0; i < size; i++) {
			writer.writeInt(array.get(i));
		}
		// 转为字节数组输出
		return writer.effuse();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		byte[] b = effuse();
		return writeTo(file, false, b, 0, b.length);
	}
}