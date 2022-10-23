/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.casket.*;
import com.laxcus.access.column.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.access.select.*;
import com.laxcus.data.pool.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.set.*;

/**
 * SQL.SELECT调用器。<br>
 * 执行DATA站点的数据检索。
 * 
 * @author scott.liang
 * @version 1.3 9/23/2013
 * @since laxcus 1.0
 */
public class DataCastSelectInvoker extends DataInvoker {

	/** 检索缓存 **/
	private SelectBuffer buffer;

	/**
	 * 构造SQL.SELECT调用器，指定异步命令
	 * @param cmd SELECT命令
	 */
	public DataCastSelectInvoker(CastSelect cmd) {
		super(cmd);
		// 建立缓存
		buffer = new SelectBuffer(isDisk());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CastSelect getCommand() {
		return (CastSelect) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 设置调用器编号
		buffer.setInvokerId(getInvokerId());

		CastSelect cmd = getCommand();
		Select select = cmd.getSelect();

		// 先进行内存检索，返回数据块编号，两组数据块编号进行“与”操作，再进行磁盘检索
		StubSet set = StaffOnDataPool.getInstance().query(select);
		boolean success = (set != null);

		// 数据有效
		if (success) {
			// “与”操作，保留相同编号，其它删除
			set.AND(cmd.getStubs());
			boolean allow = (set.size() > 0);

			Logger.debug(this, "launch", allow, "stub size is %d", set.size());

			// 有相同的数据块编号，去检索这些编号关联的数据
			if (allow) {
				// 如果有函数列和需要自动处理SQL函数，对数据流做解析，并且生成新的数据流
				if (select.hasFunctions() && select.isAutoAdjust()) {
					success = redirect(select, set.list());
				} else {
					success = direct(select, set.list());
				}
			}
		}

		// 判断成功或者失败
		if (success) {
			// 判断数据在磁盘或者内存，选择输出
			boolean ondisk = buffer.isDisk();
			if (ondisk) {
				File[] files = buffer.getFiles();
				replyFile(files);
			} else {
				byte[] b = buffer.getMemory();
				replyPrimitive(b);
			}
		} else {
			replyFault(Major.FAULTED, Minor.DEFAULT);
		}

		Logger.debug(this, "launch", success, "result is");
		return useful(success);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * SELECT语句中带有函数，要获取原始数据，重新整理后再发送
	 * @param select SELECT命令
	 * @param stubs 数据块编号集合
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean redirect(Select select, List<java.lang.Long> stubs) {
		// SELECT检索结果辅助信息
		Space space = select.getSpace();

		// 提取表配置
		Table table = StaffOnDataPool.getInstance().findTable(space);
		if (table == null) {
			Logger.error(this, "redirect", "cannot find %s", space);
			return false;
		}
		// SELECT中的全部显示成员集合，包括列成员，操作列的函数成员，不操作列的函数成员
		ListSheet listSheet = select.getListSheet();
		// 按照显示列，生成列属性顺序表
		Sheet sheet = listSheet.getColumnSheet(table);

		Logger.debug(this, "redirect", "column sheet size:%d", sheet.size());

		// 发送位置下标和发送次数统计
		int index = 0;
		for (; index < stubs.size(); index++) {
			// 生成命令
			long stub = stubs.get(index);
			SelectCasket casket = new SelectCasket(select, stub);
			// JNI检索数据
			byte[] b = null;
			try {
				b = AccessTrustor.select(casket);
			} catch (AccessException e) {
				Logger.error(e);
				continue;
			}

			Logger.debug(this, "redirect", "return size:%d", (b == null ? -1 : b.length));

			// 分析处理结果，取出数据内容
			AccessStack stack = new AccessStack(b);
			// 数据内容是空值不处理
			b = stack.getContent();
			if (Laxkit.isEmpty(b)) {
				continue;
			}

			// 分析报头，确定全部数据流长度(标记头和检索数据)
			MassFlag flag = new MassFlag();
			int off = flag.resolve(b, 0, b.length);
			
			Logger.debug(this, "redirect", "show sheet columns %d, sheet columns %d, flag columns %d", listSheet.size(), sheet.size(), flag.getColumns());

			// 解析输出的结果，在原记录结果上产生新的记录，并且以字节数组输出到内存
			RowCracker cracker = new RowCracker(sheet);
			cracker.split(b, off, b.length - off);
			List<Row> results = cracker.flush();
			Logger.debug(this, "redirect", "first row size %d", results.size());

			// 重组数据，返回新的数据流
			byte[] data = realign(listSheet, results);

			// 显示
			Logger.debug(this, "redirect", "choice stream size is %d, rows:%d, columns:%d", b.length,
					stack.getRows(), stack.getColumns());
			
			// 重新定义标头信息
			flag.setColumns((short) listSheet.size());
			flag.setModel(StorageModel.NSM);
			flag.setLength(data.length);
			// 定义新的头
			byte[] head = flag.build();
			
			// 写入数据
			buffer.append(head);
			buffer.append(data);
		}

		// 判断操作结果
		return (index == stubs.size());
	}


	/**
	 * 直接检索和发送数据
	 * @param select
	 * @param stubs
	 * @return
	 */
	private boolean direct(Select select, List<java.lang.Long> stubs) {
		int index = 0;
		for (; index < stubs.size(); index++) {
			// 生成命令
			long stub = stubs.get(index);
			SelectCasket casket = new SelectCasket(select, stub);
			// JNI检索数据
			byte[] b = null;
			try {
				b = AccessTrustor.select(casket);
			} catch (AccessException e) {
				Logger.error(e);
				continue;
			}

			Logger.debug(this, "direct", "stub:%x, choice stream size:%d", stub, (b == null ? -1 : b.length));

			// 分析处理结果，取出数据内容
			AccessStack stack = new AccessStack(b);
			if(stack.isEmptyContent()) {
				continue;
			}
			b = stack.getContent();

			Logger.debug(this, "direct", "choice stream size is %d, rows:%d, columns:%d", b.length,
					stack.getRows(), stack.getColumns());

			// 写入数据
			buffer.append(b);
		}

		boolean success = (index == stubs.size());

		Logger.debug(this, "direct", success, "stub size:%d", stubs.size());
		return success;
	}


	/**
	 * 取出表中的函数，根据实际列参数，产生新的行，重新排列输出
	 * @param sheet
	 * @param list
	 */
	private byte[] realign(ListSheet sheet, List<Row> list) {
		ClassWriter buff = new ClassWriter(10240);

		ArrayList<Row> array = new ArrayList<Row>(1);

		for(Row row : list) {
			if (array.size() > 0) {
				array.clear();
			}
			array.add(row);

			Row that = new Row();
			for(ListElement element : sheet.list()) {
				// 如果是列成员，返回列标识号；如果是函数成员，返回临时的函数编号(在列标识号之外的增加)
				short identity = element.getIdentity();

				if (element.isColumn()) {
					Column column = row.find(identity);
					if (column == null) {
						throw new ColumnException("cannot find column by %s-%d",
								element.getSpace(), identity);
					}
					that.add(column);
				} else if (element.isFunction()) {
					ColumnFunction function = ((FunctionElement) element).getFunction();
					// 根据函数，生成列成员，并且指定标识号
					Column column = function.makeup(array);
					if (column == null) {
						throw new ColumnException("cannot make column by %s-%d",
								element.getSpace(), identity);
					}
					column.setId(identity);
					that.add(column);
				}
			}

			// 转为数据流输出到写入器
			that.buildX(buff);
		}
		// 输出数据
		return buff.effuse();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		Logger.debug(this, "destroy", "%d usedtime:%d", getInvokerId(), getRunTime());
		
		// 销毁内存或者磁盘中的数据
		if (buffer != null) {
			buffer.destroy();
			buffer = null;
		}

		super.destroy();
	}

}