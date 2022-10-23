/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.to;

import java.io.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.util.classable.*;

/**
 * 汇总FROM节点产生的挖矿数据，全部保存后输出。
 * 
 * @author xiaoyang.yuan
 * @version 1.0 2015-2-12
 * @since laxcus 1.0
 */
public class MineToTask extends ToEvaluateTask {
	
	/** 缓存数据 **/
	private ClassWriter buff = new ClassWriter();

	/**
	 * 构造默认的挖矿TO阶段任务
	 */
	public MineToTask() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, byte[], int, int)
	 */
	@Override
	public boolean evaluate(FluxField field, byte[] b, int off, int len)
			throws TaskException {

		// 这里只做整合数据，原样保存，由PUT阶段处理
		if (len > 0) {
			buff.write(b, off, len);
		}
		
		Logger.debug(getIssuer(), this, "evaluate", "当前数据长度是：%d", buff.size());

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#evaluate(com.laxcus.distribute.conduct.mid.FluxField, java.io.File)
	 */
	@Override
	public boolean evaluate(FluxField field, File file) throws TaskException {
		return defaultEvaluate(field, file);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToEvaluateTask#assemble()
	 */
	@Override
	public long assemble() throws TaskException {
		return buff.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		return buff.effuse();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		byte[] b = effuse();
		return	writeTo(file, false, b, 0, b.length);
	}

}