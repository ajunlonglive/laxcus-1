/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.init;

import com.laxcus.access.index.section.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.task.conduct.init.*;

/**
 * SQL分布计算INIT阶段基础接口。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/30/2014
 * @since laxcus 1.0
 */
public abstract class SQLInitTask extends InitTask {

	/**
	 * 构造SQLInitTask实例
	 */
	protected SQLInitTask() {
		super();
	}

	/**
	 * 当前阶段给它的前一阶段设置索引分区。在分布计算过程中，每个阶段将根据索引分区的有无，决定对数据是否分片和数据分片。<br>
	 * 在给上个阶段设置索引分区时，必须确定对象存在，否则将弹出异常。<br><br>
	 * 
	 * 当前阶段给前一阶段设置索引分区时，存在两种情况：<br>
	 * 1. 如果TO对象分派器存在，找到最后一个，设置它的索引分区。<br>
	 * 2. TO对象不存在，找到FROM对象分派器（FROM对象不迭代，只有一个），设置FROM对象和会话的索引分区。<br>
	 * 
	 * @param sector - 根据当前对象需求生成的索引分区
	 * @param conduct - CONDUCT命令
	 * @throws InitTaskException
	 */
	protected void setPreviousSector(ColumnSector sector, Conduct conduct) throws InitTaskException {
		ConductDispatcher dispatcher = null;
		// 先检查TO阶段对象，再检查FROM对象
		ToObject last = conduct.getLastToObject();
		if (last != null) {
			dispatcher = last.getDispatcher();
		} else {
			dispatcher = conduct.getFromObject().getDispatcher();
		}
		// 没有是错误
		if (dispatcher == null) {
			throw new InitTaskException("cannot be find last dispatcher");
		}

		// 设置索引分区
		dispatcher.setIndexSector(sector);
	}

	//	/**
	//	 * 在INIT阶段设置SELECT，通常在BALANCE阶段取出来
	//	 * @param object
	//	 * @param select
	//	 */
	//	protected void setSelect(AccessObject object, Select select) {
	//		object.addCommand(SQLTaskKit.SELECT_OBJECT, select);
	//	}

	//	/**
	//	 * @param object
	//	 * @return
	//	 * @throws TaskException
	//	 */
	//	protected Select getSelect(AccessObject object) throws TaskException {
	//		TaskParameter param = object.findValue(SQLTaskKit.SELECT_OBJECT);
	//		if (param == null || !param.isCommand()) {
	//			throw new TaskException("cannot find select");
	//		}
	//		TaskCommand instance = (TaskCommand) param;
	//		return (Select) instance.getValue();
	//	}

}