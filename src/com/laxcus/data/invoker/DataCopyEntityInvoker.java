/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 复制数据块调用器
 * 
 * @author scott.liang
 * @version 1.0 11/11/2020
 * @since laxcus 1.0
 */
public class DataCopyEntityInvoker extends DataInvoker {

	/**
	 * 构造默认的复制数据块调用器，指定命令
	 * @param cmd 复制数据块
	 */
	public DataCopyEntityInvoker(CopyEntity cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CopyEntity getCommand() {
		return (CopyEntity) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CopyEntity cmd = getCommand();
		Space space = cmd.getSpace();
		Node hub = cmd.getFrom();
		
		ArrayList<ShiftDownloadMass> array = new ArrayList<ShiftDownloadMass>();
		// 取数据块编号
		for (long stub : cmd.list()) {
			String path = AccessTrustor.doChunkFile(space, stub);

			// 建立传输命令，要求采用流模式（保存到内存）
			StubFlag flag = new StubFlag(space, stub);
			DownloadMass sub = new DownloadMass(flag);
			sub.setMemory(true);
			// 转发命令
			DownloadMassHook hook = new DownloadMassHook();
			ShiftDownloadMass shift = new ShiftDownloadMass(hub, sub, hook, path);
			array.add(shift);
		}
		
		CopyEntityProduct product = new CopyEntityProduct();
		
		// 为减少轻计算机压力，每次只触发一个下载，直到完成
		for (int i = 0; i < array.size(); i++) {
			ShiftDownloadMass shift = array.get(i);
			StubFlag flag = shift.getCommand().getFlag();
			long stub = flag.getStub();

			// 转发处理
			boolean success = DataCommandPool.getInstance().press(shift);
			// 若不成功就退出
			if (!success) {
				Logger.error(this, "launch", "press error!");
				product.add(stub, false);
				continue;
			}
			// 等待结果
			DownloadMassHook hook = shift.getHook();
			hook.await();
			// 不成功退出
			success = hook.isSuccessful();

			// 保存结果
			product.add(stub, success);
		}

		boolean success = replyProduct(product);
		
		Logger.debug(this, "launch", success, "copy %s, stub count %d ", space, product.size());

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

//	/**
//	 * 建立SELECT命令，取出全部数据
//	 * @param space 数据表名
//	 * @return 返回SELECT命令
//	 */
//	private Select createSelect(Space space) {
//		Table table = StaffOnDataPool.getInstance().findTable(space);
//
//		// 选择键的时候，首选数字类型，因为它的处理速度经可变类型快。
//		ColumnAttribute key = null;
//		for (ColumnAttribute e : table.list()) {
//			// 找到一个非变长类型
//			if (e.isKey() && !e.isVariable()) {
//				key = e;
//				break;
//			}
//		}
//		// 以上不成立，选择主键
//		if (key == null) {
//			key = table.pid();
//		}
//
//		Logger.debug(this, "createSelect", "%s key is %s, type: %s", space,
//				key.getName(), ColumnType.translate(key.getType()));
//
//		WhereIndex index = createIndex(key);
//		// 生成命令，要求获得全部命令
//		Where where = new Where(CompareOperator.ALL, index);
//
//		// 排列表
//		ListSheet sheet = new ListSheet();
//		for (ColumnAttribute attribute : table.list()) {
//			ColumnElement cell = new ColumnElement(space, attribute.getTag());
//			if (sheet.contains(table.getSpace(), cell.getColumnId())) {
//				throw new IllegalValueException("overlap column '%s'", attribute.getNameText());
//			}
//			sheet.add(cell);
//		}
//
//		Select select = new Select(space);
//		select.setListSheet(sheet);
//		select.setWhere(where);
//
//		return select;
//	}
//
//	/**
//	 * 建立索引
//	 * @return 返回WHERE索引子类
//	 */
//	private WhereIndex createIndex(ColumnAttribute attribute) {
//		Column column = ColumnCreator.create(attribute.getType());
//		if(column == null){
//			throw new IllegalValueException("cannot be find attribute %d", attribute.getType());
//		}
//		// 设置列编号
//		short columnId = attribute.getColumnId();
//		column.setId(columnId);
//
//		// 短整型
//		if (column.isShort()) {
//			return new ShortIndex((short) 0, column);
//		}
//		// 整数
//		else if (column.isInteger() || column.isDate() || column.isTime()) {
//			return new IntegerIndex(0, column);
//		}
//		//		// 长整数
//		//		else if (column.isLong()) {
//		//			return new LongIndex(0, column);
//		//		}
//		// 单浮点
//		else if (column.isFloat()) {
//			return new FloatIndex(0, column);
//		}
//		// 双浮点
//		else if (column.isDouble()) {
//			return new DoubleIndex(0, column);
//		}
//		//		// 日期
//		//		else if (column.isDate()) {
//		//			return new IntegerIndex(0, column);
//		//		}
//		//		// 时间
//		//		else if (column.isTime()) {
//		//			return new IntegerIndex(0, column);
//		//		}
//		//		// 时间戳
//		//		else if (column.isTimestamp()) {
//		//			return new LongIndex(0, column);
//		//		}
//
//		// 以下是可变长数据类型
//		return new LongIndex(0, column);
//	}
//	
//	private String printX(byte[] b) {
//		StringBuilder bf = new StringBuilder();
//		for(int i =0; i < b.length; i++) {
//			String s = String.format("%X", b[i]);
//			if(bf.length() >0) {
//				bf.append(',');
//			}
//			if(s.length() == 1){
//				s = "0" + s;
//			}
//			bf.append(s);
//		}
//		return bf.toString();
//	}

}
