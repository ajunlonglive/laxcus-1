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
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.data.rollback.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.visit.*;

/**
 * UPDATA命令调用器
 * 
 * @author scott.liang
 * @version 1.33 8/12/2016
 * @since laxcus 1.0
 */
public class DataCastUpdateInvoker extends DataRollbackInvoker {

	/** 更新备注 **/
	private UpdateRemark remark = new UpdateRemark();

	/** 被更新的数据单元 **/
	private TreeSet<RollbackUpdateItem> records = new TreeSet<RollbackUpdateItem>();

	/** UPDATE最后处理结果 **/
	private AssertUpdate assertUpdate;

	/**
	 * 构造UPDATA命令调用器，指定命令
	 * @param cmd UPDATA命令
	 */
	public DataCastUpdateInvoker(CastUpdate cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.data.invoker.DataRollbackInvoker#getLastConsult()
	 */
	@Override
	public AssertConsult getLastConsult() {
		return assertUpdate;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CastUpdate getCommand() {
		return (CastUpdate) super.getCommand();
	}

	/**
	 * 返回数据表名
	 * @return
	 */
	private Space getSpace() {
		CastUpdate cmd = getCommand();
		return (cmd != null ? cmd.getSpace() : null);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CastUpdate cmd = getCommand();
		Cabin hub = cmd.getSource();
		Space space = cmd.getSpace();

		// 判断是主站点且表有效（只有主站点才能执行更新操作）
		boolean success = isMaster();
		// 判断用户签名和表名匹配和有效
		if (success) {
			success = allow(cmd.getIssuer(), space);
		}
		// 判断有足够的剩余空间，最少1G
		if (success) {

		}

		// 以上判断错误，直接拒绝和退出
		if (!success) {
			Logger.error(this, "launch", "refuse %s", space);
			replyFault(Major.FAULTED, Minor.REFUSE);
			return useful(false);
		}

		// 申请锁定一个表
		lock(space);
		// 更新数据
		success = update(cmd);
		remark.setSuccessful(success);

		// 发送数据
		byte status = (success ? ConsultStatus.SUCCESS : ConsultStatus.FAILED);

		// 设置投递命令
		AssumeUpdate assume = new AssumeUpdate(space, status);
		if (success) {
			// 统计更新的行数
			assume.setRows(remark.getRows());
			// 获取删除的数据
			if (cmd.isSnatch()) {

			}
		}

		// 以回馈模式发送命令给请求端，等待请求端的回应
		success = replyTo(hub, assume);

		// 如果命令发送失败，回滚数据
		if (!success) {
			rollback();
		}

		Logger.debug(this, "launch", success, "reply to %s, %s rows is %d",
				hub, assume.getSpace(), assume.getRows());

		return success;
	}


	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		// 接收确认应答。以锁定方式执行，避免其它调用器同步操作“assertUpdate”时的异常
		super.lockSingle();
		try {
			if (isSuccessObjectable(index)) {
				assertUpdate = getObject(AssertUpdate.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 判断数据表名一致
		Space space = getSpace();
		boolean success = (assertUpdate != null && Laxkit.compareTo(space, assertUpdate.getSpace()) == 0);
		// 更新确认判断
		if (success) {
			success = (assertUpdate.isConfirm() && remark.isSuccessful());
		}
		// 各调用器之间，进行相互确认。这是保证数据一致性的重要一步。
		if (success) {
			success = exlink();
		}

		Logger.debug(this, "ending", success, "callback command is \"%s\"", assertUpdate);

		// 请求端监听地址
		Cabin hub = assertUpdate.getSource();
		// 设置返回状态
		if (success) {
			AssumeUpdate assume = new AssumeUpdate(space, ConsultStatus.CONFIRM_SUCCESS);
			assume.setRows(remark.getRows());
			// 发送给调用端
			success = replyCommand(hub, assume);
			// 如果发送不成功，回滚数据
			if(!success) {
				rollback();
			}
		} else {
			success = rollback(); // 回滚数据
			byte status = (success ? ConsultStatus.CANCEL_SUCCESS : ConsultStatus.CANCEL_FAILED);
			AssumeUpdate assume = new AssumeUpdate(space, status);
			// 发送给请求端
			success = replyCommand(hub, assume);
		}

		Logger.debug(this, "ending", success, "update %s is", space);

		// 删除磁盘文件
		for (RollbackUpdateItem item : records) {
			item.getDeleteItem().delete();
			item.getInsertItem().delete();
		}
		// 清除记录
		records.clear();

		return useful(success);
	}

	/**
	 * 更新全部数据
	 * @param cast 更新命令
	 * @return 成功返回真，否则假
	 */
	private boolean update(CastUpdate cast) {
		// 更新命令
		Update cmd = cast.getUpdate();
		Space space = cmd.getSpace();
		Table table = findTable(space);
		if (table == null) {
			Logger.error(this, "update", "cannot be find %s", space);
			return false;
		}
		// 被更新的数据块编号
		List<java.lang.Long> stubs = cast.getStubs();

		// 统计成功数目
		int count = 0;
		// 全部数据块数目
		final int size = stubs.size();
		// 逐一更新数据块中的数据
		for (int index = 0; index < size; index++) {
			long stub = stubs.get(index);
			// 删除数据
			int ret = delete(cmd, stub, table);
			// 大于等于0是正常，否则是错误
			boolean success = (ret >= 0);
			// 不成功退出，否则增1
			if (!success) break;
			count++;
		}
		// 判断成功
		boolean success = (count == size);
		// 把删除的数据，从磁盘中读出，修改后再写入磁盘
		if (success) {
			count = 0;
			for (RollbackUpdateItem item : records) {
				success = insert(item ,cmd , table);
				// 不成功退出，否则统计增1 
				if(!success) break;
				count++;
			}
			success = (count == records.size());
		}

		// 更新成功
		Logger.debug(this, "update", success, "count:%d, stubs size:%d", count, size);

		return success;
	}

	/**
	 * 删除数据，被删除的记录写入磁盘
	 * @param cmd 更新命令
	 * @param stub 数据块编号
	 * @param table 表名
	 * @return 返回被删除的行数，错误返回小于0
	 */
	private int delete(Update cmd, long stub, Table table) {
		Space space = cmd.getSpace();
		// 生成删除命令
		Delete delete = new Delete(space);
		delete.setWhere(cmd.getWhere());

		// 建立更新回滚单元
		RollbackUpdateItem item = createUpdateRollbackFile(stub);
		String filename = item.getDeleteItem().getPath();
		Logger.debug(this, "delete", "delete file '%s'", filename);

		// 通过JNI，删除磁盘数据
		DeleteCasket packet = new DeleteCasket(delete, stub, filename);
		byte[] primitive = null;
		try {
			primitive = AccessTrustor.delete(packet);
		} catch (AccessException e) {
			Logger.error(e);
			return -1; // 出错，返回-1
		}

		Logger.debug(this, "delete", "ant stack size %d", primitive.length);

		// 生成堆栈，分析处理结果
		AccessStack stack = new AccessStack(primitive);

		Logger.debug(this, "delete", "delete %d rows,%d columns, state:%d", 
				stack.getRows(), stack.getColumns(), stack.getState());

		// 没有找到，返回0
		if (stack.isNotFound()) {
			Logger.debug(this, "delete", "cannot be find by %x", stub);
			return 0;
		}
		// 删除发生错误，返回错误码
		if(stack.isFault()) {
			item.getDeleteItem().delete(); // 如果出错，删除磁盘文件
			Logger.error(this, "delete", "jni error! code is %d", stack.getFault());
			return stack.getFault();
		}

		// 保存单元
		records.add(item);

		// 区分CACHE/CHUNK块，将删除记录备份到从站点
		boolean success = false;
		if (stack.isCacheStub()) {
			success = doBackupCache(stack);
		} else if (stack.isChunkStub()) {
			success = doBackupChunk(stack);
		}

		Logger.debug(this, "delete", success, "delete %s#%X, rows:%d",
				stack.getSpace(), stack.getStub(), stack.getRows());

		// 正确或者出错
		return (success ? stack.getRows() : -1);
	}

	/**
	 * 插入数据
	 * @param item
	 * @param cmd
	 * @param table
	 * @return 成功返回真，否则假
	 */
	private boolean insert(RollbackUpdateItem item, Update cmd, Table table) {
		// 读取磁盘数据
		String filename = item.getDeleteItem().getPath();
		AccessStack deleteStack = new AccessStack();
		try {
			deleteStack.doDisk(filename, 0);
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}

		// 替换（修改某些DELETE的内容数据）
		Insert insert = replace(cmd, deleteStack, table);
		// 调用JNI接口，插入修改后的数据
		filename = item.getInsertItem().getPath();

		Logger.debug(this, "insert", "insert file '%s', rows size:%d",
				filename, insert.size());

		InsertCasket packet = new InsertCasket(insert, filename);
		byte[] primitive = AccessTrustor.insert(packet);

		// 更新备份内容
		int total = 0, count = 0;
		ClassReader reader = new ClassReader(primitive);
		while (reader.hasLeft()) {
			AccessStack stack = new AccessStack(reader);
			total++; // 统计增1

			// 忽略错误，但是后面操作仍然继续
			if (stack.isFault()) {
				Logger.error(this, "insert", "insert failed, code:%d", stack.getFault());
				continue;
			}

			// CACHE块写满，整块备份替换（上传CHUNK块，删除CACHE REFLEX块）
			// 否则取出CACHE映像数据，备份到CACHE REFLEX从站
			boolean success = false;
			if (stack.isInsertFull()) {
				remark.addCompletes(1);
				success = doReplaceCache(stack.getSpace(), stack.getStub());
			} else {
				success = doBackupCache(stack);
			}
			// 成功，增加统计值
			if (success) count++;
		}
		// 判断成功
		boolean success = (count == total);

		// 增加更新行数
		if (success) {
			remark.addRows(insert.size());
		}

		Logger.debug(this, "insert", success, "insert %s#%X, count rows:%d", deleteStack.getSpace(), deleteStack.getStub(), remark.getRows());

		return success;
	}

	/**
	 * 取出被删除的行记录，替换列数据
	 * @param cmd
	 * @param stack
	 * @param table
	 * @return 输出Insert命令
	 */
	private Insert replace(Update cmd, AccessStack stack, Table table) {
		// 取出数据内容，用可类化解析器解析它
		ClassReader reader = new ClassReader(stack.getContent());
		// 解析和跳过数据块标头，每个数据块必有
		MassFlag flag = new MassFlag();
		flag.resolve(reader);

		Logger.debug(this, "replace", "delete rows:%d", flag.getRows());

		// 生成序列表
		Sheet sheet = table.getSheet();		
		// 解析行记录
		RowCracker cracker = new RowCracker(sheet);
		cracker.split(reader);

		// 输入全部行数据
		List<Row> results = cracker.flush();

		Insert insert = new Insert(table.getSpace());

		// 修改列参数，保存到命令中
		for (Row row : results) {
			for (Column column : cmd.values()) {
				// 查找有匹配的列
				boolean success = row.hasColumn(column.getId(), column.getType());
				if (!success) {
					throw new ColumnNotFoundException("cannot be find %s#%d:%d",
							table.getSpace(), column.getId(), ColumnType.translate(column.getType()));
				}
				// 替换一列参数
				row.replace(column);
			}
			// 因为替换数据后，需要重新排序
			row.aligment();
			// 保存一行数据
			insert.add(row);
		}
		// 返回新INSERT命令
		return insert;
	}

	/**
	 * 回滚数据 <br>
	 * 先撤销INSERT数据，再把DELETE数据写入磁盘
	 * @return 成功返回真，否则假
	 */
	private boolean rollback() {
		if (records.isEmpty()) {
			return true;
		}

		ArrayList<RollbackUpdateInsertItem> insertArray = new ArrayList<RollbackUpdateInsertItem>();
		ArrayList<RollbackUpdateDeleteItem> deleteArray = new ArrayList<RollbackUpdateDeleteItem>();

		// 1. 把删除/插入单元保存到数组
		for (RollbackUpdateItem item : records) {
			if (item.getInsertItem() != null) {
				insertArray.add(item.getInsertItem());
			}
			if (item.getDeleteItem() != null) {
				deleteArray.add(item.getDeleteItem());
			}
		}
		// 2. 按照文件时间排序
		Collections.sort(insertArray);
		Collections.sort(deleteArray);
		// 3. 首先回滚INSERT文件
		int insertCount = 0;
		for (RollbackUpdateInsertItem item : insertArray) {
			File file = item.getFile();
			// 回滚INSERT数据，把数据从数据块中撤销
			try {
				boolean success = rollbackInsert(file);
				if (success) insertCount++;
			} catch (IOException e) {
				Logger.error(e);
			}
		}
		// 4. 再回滚DELETE文件
		int deleteCount = 0;
		for (RollbackUpdateDeleteItem item : deleteArray) {
			File file = item.getFile();
			try {
				boolean success = rollbackDelete(file);
				if (success) deleteCount++;
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		//5. 删除磁盘文件
		for (RollbackUpdateItem item : records) {
			item.getInsertItem().delete();
			item.getDeleteItem().delete();
		}

		// 判断回滚成功
		boolean success = (insertCount == insertArray.size() && deleteCount == deleteArray.size());

		Logger.debug(this, "rollback", success,
				"insert count:%d,%d, delete count:%d,%d", insertCount,
				insertArray.size(), deleteCount, deleteArray.size());

		// 清空全部
		records.clear();

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		Space space = getSpace();
		// 如果在存活期，回滚数据
		if (isAlive()) {
			rollback();
		}
		// 释放上级数据资源
		super.destroy();
		// 最后解决表锁定（destroy方法可能被JVM多次调用，表名可能是空指针，如果是将由unlock方法忽略）
		unlock(space);
	}

}