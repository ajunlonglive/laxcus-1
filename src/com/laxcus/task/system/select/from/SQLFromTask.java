/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.from;

import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.util.*;

/**
 * SQL分布计算的FROM阶段任务
 * 
 * @author scott.liang
 * @version 1.1 1/23/2013
 * @since laxcus 1.0
 */
public abstract class SQLFromTask extends FromTask {

	/**
	 * 构造SQL分布计算的FROM阶段任务
	 */
	protected SQLFromTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTask#divide()
	 */
	@Override
	public long divide() throws TaskException {
		FromSession session = getSession();
		CastSelect cmd = (CastSelect) session.getCommand();
		Select select = cmd.getSelect();
		
//		// 打印结果
//		System.out.printf("SELECT IS: %s\n", select.getPrimitive());

		List<java.lang.Long> list = cmd.getStubs();

		int index = 0;
		for (; index < list.size(); index++) {
			long stub = list.get(index);
			// JNI检索数据
			byte[] content = getFromTrustor().select(getInvokerId(), select, stub);

			Logger.debug(getIssuer(), this, "divide", "%s#%x content size:%d", 
				select.getSpace(), stub, (content == null ? -1 : content.length));
			
			// 如果是空记录，忽略
			if (Laxkit.isEmpty(content)) {
				continue;
			}

			// 把数据传递给子类方法，去分割数据
			divide(content, 0, content.length);
		}

		boolean success = (index == list.size());
		// 不成功，弹出异常
		if (!success) {
			throw new FromTaskException("divide failed!");
		}
		// 返回字节数组长度
		return assemble();
	}

	/**
	 * 数据汇总操作 <br>
	 * “assemble”方法在“divide”方法之后执行，是对“divide”方法产生的数据进行汇总计算，它返回FluxArea的字节数组长度。每次计算只能调用一次。
	 * 
	 * @return 返回FluxArea的字节数组长度
	 * @throws TaskException - 分布任务异常
	 */
	private long assemble() throws TaskException {
		// 统计映像数据长度
		byte[] b = effuse();
		return b.length;
	}

	/**
	 * 解析数据和分割保存
	 * @param sheet 列序列表
	 * @param sector 索引分区
	 * @param b 字节数组
	 * @param off 数据开始下标
	 * @param len 有效长度
	 * @return 返回解析的字节数组长度
	 * @throws TaskException - 分布任务异常
	 */
	protected int splitTo(Sheet sheet, ColumnSector sector, byte[] b, int off, int len) throws TaskException {
		// 解析数据
		RowCracker cracker = new RowCracker(sheet);
		int size = cracker.split(b, off, len);

		// 解析长度必须一致
		if (size != len) {
			throw new FromTaskException("split error! %d != %d", size, len);
		}

		// 输出全部
		List<Row> rows = cracker.flush();

		// 数据分片和写入磁盘
		splitWriteTo(sector, rows);

		return size;
	}

	/**
	 * 数据分割，允许在一次处理过程中任意多次调用。<br>
	 * 用户实现这个方法，按照需要的规则对数据进行处理，将字节数组流解释成需要的数据，
	 * 在分割后，以任务号(taskid)+模值(mod)的方式，将结果写入磁盘。<br>
	 * 
	 * @param b 待处理的检索内容
	 * @param off 数组开始位置
	 * @param len 有效数据长度
	 * @throws TaskException - 分布任务异常
	 */
	protected abstract void divide(byte[] b, int off, int len) throws TaskException;

}