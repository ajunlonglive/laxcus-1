/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.*;
import com.laxcus.access.casket.*;
import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.command.access.table.*;
import com.laxcus.data.pool.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 导出数据块调用器
 * 
 * @author scott.liang
 * @version 1.0 2/11/2018
 * @since laxcus 1.0
 */
public class DataSingleExportEntityInvoker extends DataInvoker {

	/**
	 * 构造默认的导出数据块调用器，指定命令
	 * @param cmd 导出数据块
	 */
	public DataSingleExportEntityInvoker(SingleExportEntity cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SingleExportEntity getCommand() {
		return (SingleExportEntity) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SingleExportEntity cmd = getCommand();
		Siger siger = cmd.getIssuer();
		Space space = cmd.getSpace();
		long stub = cmd.getStub();

		// 判断操作允许且数据块存在
		boolean success = StaffOnDataPool.getInstance().allow(siger, space);
		if (success) {
			success = AccessTrustor.hasChunk(space, stub);
			// 判断是缓存块编号
			if (!success) {
				long identity = AccessTrustor.getCacheStub(space);
				success = (identity == stub);
			}
		}
		// 以上不成功，返回错误
		if (!success) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}

		// 生成SELECT命令
		Select select = createSelect(space);
		// 检查数据
		SelectCasket casket = new SelectCasket(select, stub);
		// 检查当前内存，如果不足，生成一个磁盘文件，写入磁盘
		//		casket.setFile(null);

		// 检索数据
		byte[] b = null;
		try {
			b = AccessTrustor.select(casket);
		} catch (AccessException e) {
			Logger.error(e);
			return false;
		}

		Logger.debug(this, "launch", "select result size:%d", (b == null ? -1 : b.length));

		// 分析处理结果，取出数据内容
		AccessStack stack = new AccessStack(b);
		// 数据内容是空值不处理
		byte[] content = stack.getContent();
		if (Laxkit.isEmpty(content)) {
			// 记录错误
			String log = printX(b);
			Logger.error(this, "launch", log);
			// 反馈结果
			replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}

		// 反馈数据
		success = replyPrimitive(content);

		Logger.debug(this, "launch", success, "send %s - %x data", space, stub);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 建立SELECT命令，取出全部数据
	 * @param space 数据表名
	 * @return 返回SELECT命令
	 */
	private Select createSelect(Space space) {
		Table table = StaffOnDataPool.getInstance().findTable(space);

		// 选择键的时候，首选数字类型，因为它的处理速度经可变类型快。
		ColumnAttribute key = null;
		for (ColumnAttribute e : table.list()) {
			// 找到一个非变长类型
			if (e.isKey() && !e.isVariable()) {
				key = e;
				break;
			}
		}
		// 以上不成立，选择主键
		if (key == null) {
			key = table.pid();
		}

		Logger.debug(this, "createSelect", "%s key is %s, type: %s", space,
				key.getName(), ColumnType.translate(key.getType()));

		WhereIndex index = createIndex(key);
		// 生成命令，要求获得全部命令
		Where where = new Where(CompareOperator.ALL, index);

		// 排列表
		ListSheet sheet = new ListSheet();
		for (ColumnAttribute attribute : table.list()) {
			ColumnElement cell = new ColumnElement(space, attribute.getTag());
			if (sheet.contains(table.getSpace(), cell.getColumnId())) {
				throw new IllegalValueException("overlap column '%s'", attribute.getNameText());
			}
			sheet.add(cell);
		}

		Select select = new Select(space);
		select.setListSheet(sheet);
		select.setWhere(where);

		return select;
	}

	/**
	 * 建立索引
	 * @return 返回WHERE索引子类
	 */
	private WhereIndex createIndex(ColumnAttribute attribute) {
		Column column = ColumnCreator.create(attribute.getType());
		if (column == null) {
			throw new IllegalValueException("cannot be find attribute %d", attribute.getType());
		}
		// 设置列编号
		short columnId = attribute.getColumnId();
		column.setId(columnId);

		// 短整型
		if (column.isShort()) {
			return new ShortIndex((short) 0, column);
		}
		// 整数
		else if (column.isInteger() || column.isDate() || column.isTime()) {
			return new IntegerIndex(0, column);
		}
		//		// 长整数
		//		else if (column.isLong()) {
		//			return new LongIndex(0, column);
		//		}
		// 单浮点
		else if (column.isFloat()) {
			return new FloatIndex(0, column);
		}
		// 双浮点
		else if (column.isDouble()) {
			return new DoubleIndex(0, column);
		}
		//		// 日期
		//		else if (column.isDate()) {
		//			return new IntegerIndex(0, column);
		//		}
		//		// 时间
		//		else if (column.isTime()) {
		//			return new IntegerIndex(0, column);
		//		}
		//		// 时间戳
		//		else if (column.isTimestamp()) {
		//			return new LongIndex(0, column);
		//		}

		// 以下是可变长数据类型
		return new LongIndex(0, column);
	}
	
	private String printX(byte[] b) {
		StringBuilder bf = new StringBuilder();
		for(int i =0; i < b.length; i++) {
			String s = String.format("%X", b[i]);
			if(bf.length() >0) {
				bf.append(',');
			}
			if(s.length() == 1){
				s = "0" + s;
			}
			bf.append(s);
		}
		return bf.toString();
	}
}
