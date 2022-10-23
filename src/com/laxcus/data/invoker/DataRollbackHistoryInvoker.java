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

import com.laxcus.command.access.*;
import com.laxcus.command.access.table.*;
import com.laxcus.data.rollback.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 回滚历史数据调用器。<br>
 * 把保存在回滚目录中的数据，回滚到磁盘中
 * 
 * @author scott.liang
 * @version 1.0 5/21/2015
 * @since laxcus 1.0
 */
public class DataRollbackHistoryInvoker extends DataRollbackInvoker implements FileFilter, Comparator<File> {

	/**
	 * 构造回滚历史记录调用器，指定命令
	 * @param cmd
	 */
	public DataRollbackHistoryInvoker(RollbackHistory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RollbackHistory getCommand() {
		return (RollbackHistory) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.data.invoker.DataRollbackInvoker#getLastConsult()
	 */
	@Override
	public AssertConsult getLastConsult() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		File root = RollbackArchive.getDirectory();
		// 检查当前目录下的文件
		File[] files = root.listFiles(this);
		// 如果是空集合，返回真
		if (Laxkit.isEmpty(files)) {
			Logger.debug(this, "launch", "is empty!");
			return useful(true);
		}

		// 保存文件，对文件按照文件日期进行排序
		ArrayList<File> array = new ArrayList<File>(files.length);
		for (File file : files) {
			array.add(file);
		}
		// 排序
		Collections.sort(array, this);

		// 逐一回滚
		int count = 0;
		for (File file : array) {
			String name = file.getName();
			boolean success = false;
			// 回滚数据
			try {
				if (RollbackInsertItem.validate(name)) {
					success = rollbackInsert(file);
				} else if (RollbackDeleteItem.validate(name)) {
					success = rollbackDelete(file);
				} else if (RollbackUpdateDeleteItem.validate(name)) {
					success = rollbackDelete(file);
				} else if (RollbackUpdateInsertItem.validate(name)) {
					success = rollbackInsert(file);
				}
			} catch (IOException ex) {
				Logger.error(ex);
			}
			// 回滚成功，加1
			if (success) {
				count++;
			}
		}

		// 回滚信息
		boolean success = (count == array.size());
		Logger.debug(this, "launch", success, "rollback size:%d", array.size());
		// 返回结果
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

	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File file) {
		// 1. 判断是文件
		if (!file.isFile()) {
			return false;
		}
		// 2. 取文件名，判断是四种之一
		String name = file.getName();
		// 插入文件
		boolean success = RollbackInsertItem.validate(name);
		// 删除文件
		if (!success) {
			success = RollbackDeleteItem.validate(name);
		}
		// 更新删除后的文件
		if (!success) {
			success = RollbackUpdateDeleteItem.validate(name);
		}
		// 更新插入后的文件
		if (!success) {
			success = RollbackUpdateInsertItem.validate(name);
		}
		// 返回结果
		return success;
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(File file1, File file2) {
		long len1 = file1.lastModified();
		long len2 = file2.lastModified();
		return (len1 < len2 ? 1 : (len1 > len2 ? -1 : 0));
	}

	//	private boolean rollbackInsert(File file) throws IOException {
	//		ClassReader reader = new ClassReader(file);
	//		// 判断有剩余数据
	//		while(reader.hasLeft()) {
	//			// 从可类化读取器中解析堆栈中的数据
	//			ShiftStack stack = new ShiftStack(reader);
	//			// 写入不成功的，忽略它
	//			if (!stack.isSuccessful()) {
	//				continue;
	//			}
	//
	//			// 删除写入的数据
	//			long stub = stack.getStub();
	//
	//			// 取得数据块关联的表名
	//			Space space = AccessTrustor.findStubSpace(stub);
	//
	//			// 映像数据
	//			byte[] reflex = stack.getReflex();
	//			// 调用JNI.leave函数，撤销已经成功写入的数据（即删除数据），返回Leave堆栈数据
	//			byte[] primitive = AccessTrustor.leave(space, stub, reflex);
	//
	//			// 删除数据保存到堆栈和解析
	//			ShiftStack leaveStack = new ShiftStack(primitive);
	//			// 回滚不成功，是严重错误！！！（这一块的处理现在仍然没有确定！！！）
	//			if (!leaveStack.isSuccessful()) {
	//				Logger.fatal(this, "rollback", "cannot be leave! %s - %x", space, stub);
	//				continue;
	//			}
	//
	//			// 将整块数据备份到从站点
	//			boolean success = false;
	//			if (stack.isCacheStub()) {
	//				success = doUpdateCacheReflex(space, stub);
	//			} else if (stack.isChunkStub()) {
	//				success = doUpdateChunk(space, stub);
	//			}
	//
	//			//			// 找到对应的标识
	//			//			boolean success = false;
	//			//			// 如果之前备份成功，再次将映像数据备份到从站点
	//			//			if (flag.isBackup()) {
	//			//				if (stack.isChunkStub()) {
	//			//					success = doBackupChunk(space, leaveStack);
	//			//				} else if (stack.isCacheStub()) {
	//			//					success = doBackupCache(space, leaveStack);
	//			//				}
	//			//			} else {
	//			//				// 如果之前备份不成功，将整块数据复制到从站点
	//			//				if (flag.isChunkStub()) {
	//			//					success = doUpdateChunk(space, flag.getStub());
	//			//				} else if (flag.isCacheStub()) {
	//			//					success = doUpdateCacheReflex(space, flag.getStub());
	//			//				}
	//			//			}
	//
	//			// 如果成功，统计值增1
	//			if(success) {
	//				//				count++;
	//			}
	//
	//		}
	//		return false;
	//	}

	//	private boolean rollbackDelete(File file) throws IOException {
	//		ClassReader reader1 = new ClassReader(file);
	//		// 用数据堆栈解析在内存或者磁盘中的被删除数据
	//		ShiftStack stack1 = new ShiftStack(reader1);
	//		// 取出数据内容
	//		byte[] content = stack1.getContent();
	//
	//		// 解析位于数据开始位置的数据标识
	//		MassFlag head = new MassFlag();
	//		ClassReader reader2 = new ClassReader(content);
	//		head.resolve(reader2);
	//		Space space = head.getSpace();
	//
	//		// 查找表数据
	//		Table table = StaffOnDataPool.getInstance().findTable(space);
	//		// 表名不匹配是错误
	//		if (table == null) {
	//			Logger.error(this, "rollback", "cannot find %s", space);
	//			return false;
	//		}
	//
	//		// 按照列排序，生成排序表
	//		Sheet sheet = table.getSheet();		
	//		// 以行为单位，解析数据
	//		RowParser parser = new RowParser(sheet);
	//		parser.split(reader2);
	//
	//		// 输入全部行
	//		List<Row> rows = parser.flush();
	//		Logger.debug(this, "rollback", "row size %d", rows.size());
	//
	//		// 生成INSERT命令
	//		Insert insert = new Insert(head.getSpace());
	//		insert.addAll(rows);
	//
	//		// 查找表资源
	//		//		Space space = insert.getSpace();
	//		// 检查数据，不匹配即退出
	//		if (!check(insert)) {
	//			Logger.error(this, "doBackupInsert", "check '%s' failed", space);
	//			return false;
	//		}
	//
	//		// 默认要求将返回数据写入内存
	//		InsetCasket packet = new InsetCasket(insert);
	//		// 回滚数据写入硬盘，可能有多个实体返回
	//		byte[] primitive = AccessTrustor.insert(packet);
	//
	//		Logger.debug(this, "doBackupInsert", "insert primitive size is %d", primitive.length);
	//
	//		// 解析操作实体
	//		ClassReader reader3 = new ClassReader(primitive);
	//		ArrayList<ShiftStack> array = new ArrayList<ShiftStack>();
	//		// 逐一分析和保存
	//		while (reader3.hasLeft()) {
	//			ShiftStack stack = new ShiftStack(reader3);
	//			array.add(stack);
	//		}
	//
	//		// 备份到从站点
	//		int count = 0;
	//		for (int index = 0; index < array.size(); index++) {
	//			ShiftStack stack = array.get(index);
	//			// 不成功，忽略
	//			if (!stack.isSuccessful()) {
	//				continue;
	//			}
	//
	//			long stub = stack.getStub();
	//			boolean success = false;
	//
	//			//			if (item == null) { // 这是一个新数据块，取出映像数据，备份到从站点
	//			//				backuped = doBackupCache(space, stack);
	//			//			} else {
	//			//				if (stack.isInsertFull()) { // 已经转为CHUNK数据块，找到相关从站点，整块更新
	//			//					backuped = doReplaceCache(space, stack.getStub());
	//			//				} else {
	//			//					if (item.isBackup()) { // 之前有备份，再次备份
	//			//						backuped = doBackupCache(space, stack);
	//			//					} else { // 之前没有备份，整块复制到从站点
	//			//						backuped = doUpdateCacheReflex(space, stub); 
	//			//					}
	//			//				}
	//			//			}
	//
	//			if (stack.isInsertFull()) {
	//				success = doReplaceCache(space, stack.getStub());
	//			} else {
	//				success = doUpdateCacheReflex(space, stub);
	//			}
	//
	//			//			// 根据当前情况选择不同的备份手段
	//			//			if (stack.isInsertFull()) { // 在存储层，原来的CACHE已经转为CHUNK。这时要找到关联块从站点，整块更新
	//			//				success = doReplaceCache(space, stack.getStub());
	//			//			} else {
	//			//				if (item.isBackup()) { // 之前有备份，再次备份
	//			//					success = doBackupCache(space, stack);
	//			//				} else { // 之前没有备份，整块复制到从站点
	//			//					success = doUpdateCacheReflex(space, stub); 
	//			//				}
	//			//			}
	//
	//			// 备份成功，统计值增1
	//			if(success) {
	//				count++;
	//			}
	//		}
	//
	//		return false;
	//	}

	//	private boolean rollbackUpdateInsert(File file) {
	//		return false;
	//	}
	//
	//	private boolean rollbackUpdateDelete(File file) {
	//		return false;
	//	}
}
