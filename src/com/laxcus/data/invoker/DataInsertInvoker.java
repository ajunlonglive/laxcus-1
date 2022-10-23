/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.io.*;

import com.laxcus.access.*;
import com.laxcus.access.casket.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.missing.*;
import com.laxcus.data.pool.*;
import com.laxcus.data.rollback.*;
import com.laxcus.data.slider.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.visit.*;

/**
 * INSERT命令调用器 <br>
 * 
 * INSERT操作由是一个共享写操作，在DATA站点采用“串行写”执行（一个时间内，只允许一个相同表名的事务执行）。
 * 
 * @author scott.liang
 * @version 1.16 8/12/2016
 * @since laxcus 1.0
 */
public class DataInsertInvoker extends DataRollbackInvoker {

	/** ENDING阶段操作步骤，分为：数据处理/复核 **/
	private static final int PROCESS = 1;

	private static final int CHECKUP = 2;

	/** ENDING阶段处理步骤 **/
	private int step = DataInsertInvoker.PROCESS;

	/** 插入备注 **/
	private InsertRemark remark = new InsertRemark();

	/** 数据回滚单元 **/
	private RollbackInsertItem rollItem;

	/** 最后处理结果 **/
	private AssertInsert resultAssert;

	/**
	 * 构造INSERT命令调用器，指定命令
	 * @param cmd INSERT协商命令
	 */
	public DataInsertInvoker(InsertGuide cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.data.invoker.DataRollbackInvoker#getLastConsult()
	 */
	@Override
	public AssertConsult getLastConsult() {
		return resultAssert;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public InsertGuide getCommand() {
		return (InsertGuide) super.getCommand();
	}

	/**
	 * 返回表名
	 * @return
	 */
	private Space getSpace() {
		InsertGuide cmd = getCommand();
		return (cmd != null ? cmd.getSpace() : null);
	}

	/**
	 * 受理CALL.INSERT插入请求，返回DATA.INSERT的插入确认标识或者空值。
	 * @return 处理成功返回“真”，否则“假”。
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 收到请求，检查本地参数，发送反馈信息
		InsertGuide cmd = getCommand();
		Cabin hub = cmd.getSource();
		// 客户端要求的参数
		Space space = cmd.getSpace();
		long capacity = cmd.getCapacity();

		// 1. 是主站点，并且签名和表有效
		boolean success = isMaster();
		if (success) {
			success = allow(cmd.getIssuer(), space);
		}
		// 2. 判断有足够的空间，要求磁盘保留最少1G的空间（未完成）
		if (success) {
			success = StaffOnDataPool.getInstance().conform(space, capacity);
			// 条件不满足，通过HOME站点转发到WATCH站点，在图形界面上显示磁盘空间不足（未完成）
			if (!success) {
				// Node local = DataLauncher.getInstance().getListener();
				// DiskMissing miss = new DiskMissing(local, space);
				// DataCommandPool.getInstance().admit(miss); // 交由命令管理池处理

				// 生成磁盘空间不足命令，交由命令管理池处理
				DiskMissing missing = new DiskMissing(space);
				DataCommandPool.getInstance().admit(missing);

				Logger.warning(this, "launch", "DISK MISSING! %s", cmd);
			}
		}

		// 以上判断错误，直接拒绝和退出
		if (!success) {
			Logger.error(this, "launch", "refuse %s", space);
			replyFault(Major.FAULTED, Minor.REFUSE);
			return useful(false);
		}

		// 调整为磁盘模式，之后CALL发送的INSERT数据写入磁盘。
		boolean ondisk = isDisk();
		setDisk(true);

		// 2.以回馈模式（先在本地建立回显缓存，然后发送命令），发送确认命令给请求端，并且等等请求端发送INSERT实体数据，实体数据被写入本地硬盘
		InsertGuide guide = new InsertGuide(space);
		success = replyTo(hub, guide); // 回显数据写入硬盘/内存，由用户初始决定。

		// 恢复为指定的存取模式
		setDisk(ondisk);

		Logger.debug(this, "launch", success, "reply to %s", hub);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 最多只执行两步
		boolean success = false;
		switch (step) {
		case DataInsertInvoker.PROCESS:
			// 执行处理
			success = process();
			step = DataInsertInvoker.CHECKUP; // 进入下一步
			break;
		case DataInsertInvoker.CHECKUP:
			success = checkup();
			break;
		}
		
		// 执行两步后返回
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#isQuick()
	 */
	@Override
	public boolean isQuick() {
		// 如果进入ENDING状态的第2步，要求InvokerPool优先处理。否则调用上级同名方法
		if (step == DataInsertInvoker.CHECKUP) {
			return true;
		} else {
			return super.isQuick();
		}
	}

	/**
	 * 执行ENDING第一阶段操作 <br>
	 * 1. 申请锁定表资源 <br>
	 * 2. 执行写入操作 <br>
	 * 3. 判断写入成功或者失败，向CALL站点发出提交命令 <br>
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean process() {
		InsertGuide cmd = getCommand();
		Space space = cmd.getSpace();
		Cabin hub = cmd.getSource();

		// 1. 判断接收数据成功
		boolean success = isSuccessCompleted();
		// 不成功即退出
		if (!success) {
			Logger.error(this, "process", "upload failed");
			replyFault(); // 通知错误和退出
			return false;
		}

		// 2. 锁定数据表名，执行写入和备份
		
		// 提交申请锁定数据表名
		lock(space);

		// 从磁盘读取对象
		Insert insert = readInsert();
		success = (insert != null);
		if (!success) {
			Logger.error(this, "process", "read and check Insert command, failed!");
			replyFault();
			return false;
		}

		// 把数据写入磁盘
		success = doInsert(insert);
		// 设置写入成功或者否
		remark.setSuccessful(success);

		// 判断成功/失败，选择不同的状态码
		byte status = (success ? ConsultStatus.SUCCESS : ConsultStatus.FAILED);
		AssumeInsert assume = new AssumeInsert(space, status);
		// 如果成功，设置写入的行数
		if (success) {
			assume.setRows(remark.getRows());
		}

		// 选择以回馈/发送模式，向请求端发送命令，然后等待请求端的反馈
		success = replyTo(hub, assume);

		// 发送不成功，回滚数据和退出
		if (!success) {
			rollback();
		}

		Logger.debug(this, "process", success, "insert %s is", space);

		return success;
	}

	/**
	 * 执行第二阶段操作
	 * 1. 接受来自CALL站点的判断
	 * 2. 根据CALL站点的指令，做写入或者回滚数据的操作
	 * 3. 向CALL站点返回处理结果命令
	 * @return 成功返回真，否则假
	 */
	private boolean checkup() {
		int index = findEchoKey(0);
		// 以锁定方式接收确认应答
		super.lockSingle();
		try {
			if (isSuccessObjectable(index)) {
				resultAssert = getObject(AssertInsert.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 表名
		Space space = getSpace();
		// 判断表名匹配
		boolean success = (resultAssert != null && Laxkit.compareTo(resultAssert.getSpace(), space) == 0);
		// 判断操作匹配（请求确认插入，和本地数据插入成功）
		if (success) {
			success = (resultAssert.isConfirm() && remark.isSuccessful());
		}

		// 在以上操作成功的前提下，进行节点间的相互确认。这是一个去中心化的操作，在主控节点不参与的情况下，实现INSERT任务确认前处理。
		if (success) {
			success = exlink();
		}

		// 请求端监听地址
		Cabin hub = resultAssert.getSource(); 

		// 两组参数比较，完全成功
		if (success) {
			// 建立投递命令
			AssumeInsert assume = new AssumeInsert(space, ConsultStatus.CONFIRM_SUCCESS);
			assume.setRows(remark.getRows());
			// 发送命令到请求端站点
			success = replyCommand(hub, assume);
			// 发送不成功，回滚数据
			if(success) {
				// 数据写满，更新索引和缓存映像数据块
				if (remark.isCompleted()) { // insertItem.getFullStacks() > 0) {
					StaffOnDataPool.getInstance().reloadIndex();
					StaffOnDataPool.getInstance().reloadCacheReflexStub();
				}
			} else {
				rollback();
			}
		} else {
			// 回滚数据
			success = rollback();
			// 状态码
			byte status = (success ? ConsultStatus.CANCEL_SUCCESS : ConsultStatus.CANCEL_FAILED);
			// 建立投递命令
			AssumeInsert assume = new AssumeInsert(space, status);
			// 发送到CALL站点
			success = replyCommand(hub, assume);
		}

		Logger.debug(this, "checkup", success, "insert %s is", space);

		// 删除本地文件
		deleteFile();

		//		// 释放资源
		//		insertItem.clear();

		// 完成和退出
		return useful(success);
	}

	/**
	 * 从磁盘读取INSERT命令
	 * @return 成功返回Insert对象，否则是空指针!
	 */
	private Insert readInsert() {
		int echoIndex = findEchoKey(0);
		File file = findFile(echoIndex);
		Logger.debug(this, "readInsert", "file is '%s', length: %d", file, file.length());

		// 数据解码
		Insert insert = null;
		try {
			insert = new Insert(file);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
		// 判断INSERT命令有效
		Space space = getSpace();
		// 有效
		boolean success = (insert != null);
		// 判断表名一致
		if (success) {
			success = (Laxkit.compareTo(insert.getSpace(), space) == 0);
		}
		// 检查行数据和表排列格式一致
		if (success) {
			success = check(insert);
		}
		return (success ? insert : null);
	}

	/**
	 * 将数据写入磁盘，并且分发到备份站点
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean doInsert(Insert insert) {
		// 表名
		Space space = getSpace();

		// 将INSERT写入的应答数据备份到另一个磁盘文件
		rollItem = createInsertRollbackFile();
		String filename = rollItem.getPath();
		InsertCasket packet = new InsertCasket(insert, filename);

		// 调用JNI.insert函数，将数据写入存储层，返回INSERT映像数据。
		// 因为是以添加方式写入数据块，存在写入尺寸超过数据块限制并截断和分割的可能，所以数据可能被写到多个数据块中。
		byte[] primitive = AccessTrustor.insert(packet);

		// 解析操作实体
		ClassReader reader = new ClassReader(primitive);
		// 统计成功次数
		int count = 0;
		// 数据单元数目
		int items = 0;

		// 逐一分析和保存
		while (reader.hasLeft()) {
			AccessStack stack = new AccessStack(reader);
			items++; // 统计单元数目

			Logger.debug(this, "doInsert", "state:%d, fault:%d, rows:%d",
					stack.getState(), stack.getFault(), stack.getRows());

			// 不成功，忽略它
			if (stack.isFault()) {
				Logger.error(this, "doInsert", "insert failed, code:%d", stack.getFault());
				continue;
			}

			// 统计写入行数
			remark.addRows(stack.getRows());

			boolean success = false;
			// 如果CACHE数据块写满，找到关联站点，替换这个数据块（上传CHUNK数据块，然后删除CACHE数据块）
			if (stack.isInsertFull()) {
				remark.addCompletes(1);
				success = doReplaceCache(space, stack.getStub());
			} else {
				// 如果没有写满，取出CACHE映像数据，备份到从站点。
				success = doBackupCache(stack);
			}
			// 成功，统计值加1
			if (success) count++;
		}

		// 判断完全成功
		boolean success = (count == items);

		// 注入码位
		if (success) {
			boolean f = DataSliderPool.getInstance().insert(insert);
			Logger.debug(this, "doInsert", f, "insert to DataSliderPool");
		}

		return success;
	}

	//	/**
	//	 * 将数据写入磁盘，并且分发到备份站点
	//	 * @return 成功返回“真”，否则“假”。
	//	 */
	//	private boolean insert() {
	//		int echoIndex = findEchoKey(0);
	//		File file = findFile(echoIndex);
	//		Logger.debug(this, "insert", "file is '%s', length: %d", file, file.length());
	//
	//		// 数据解码
	//		Insert insert = null;
	//		try {
	//			insert = new Insert(file);
	//		} catch (IOException e) {
	//			Logger.error(e);
	//			return false;
	//		}
	//		// 判断INSERT命令有效
	//		Space space = getSpace();
	//		// 有效
	//		boolean success = (insert != null);
	//		// 判断表名一致
	//		if (success) {
	//			success = (Laxkit.compareTo(insert.getSpace(), space) == 0);
	//		}
	//		// 检查行数据和表排列格式一致
	//		if (success) {
	//			success = check(insert);
	//		}
	//		if (!success) {
	//			Logger.error(this, "insert", "check '%s' failed", space);
	//			return false;
	//		}
	//		
	//		// 将INSERT写入的应答数据备份到另一个磁盘文件
	//		rollItem = createInsertRollbackFile();
	//		String filename = rollItem.getPath();
	//		InsertCasket packet = new InsertCasket(insert, filename);
	//
	//		// 调用JNI.insert函数，将数据写入存储层，返回INSERT映像数据。
	//		// 因为是以添加方式写入数据块，存在写入尺寸超过数据块限制并截断和分割的可能，所以数据可能被写到多个数据块中。
	//		byte[] primitive = AccessTrustor.insert(packet);
	//
	//		// 解析操作实体
	//		ClassReader reader = new ClassReader(primitive);
	//		// 统计成功次数
	//		int count = 0;
	//		// 数据单元数目
	//		int items = 0;
	//		
	//		// 逐一分析和保存
	//		while (reader.hasLeft()) {
	//			AccessStack stack = new AccessStack(reader);
	//			items++; // 统计单元数目
	//
	//			Logger.debug(this, "insert", "state:%d, fault:%d, rows:%d",
	//					stack.getState(), stack.getFault(), stack.getRows());
	//
	//			// 不成功，忽略它
	//			if (stack.isFault()) {
	//				Logger.error(this, "insert", "insert failed, code:%d", stack.getFault());
	//				continue;
	//			}
	//			
	//			// 统计写入行数
	//			remark.addRows(stack.getRows());
	//			
	//			success = false;
	//			// 如果CACHE数据块写满，找到关联站点，替换这个数据块（上传CHUNK数据块，然后删除CACHE数据块）
	//			if (stack.isInsertFull()) {
	//				remark.addCompletes(1);
	//				success = doReplaceCache(space, stack.getStub());
	//			} else {
	//				// 如果没有写满，取出CACHE映像数据，备份到从站点。
	//				success = doBackupCache(stack);
	//			}
	//			// 成功，统计值加1
	//			if (success) count++;
	//		}
	//
	//		// 判断完全成功
	//		success = (count == items);
	//
	//		// 注入码位
	//		if (success) {
	//			boolean f = DataSliderPool.getInstance().insert(insert);
	//			Logger.debug(this, "insert", f, "insert to scaler");
	//		}
	//
	//		return success;
	//	}

	/**
	 * 回滚INSERT数据（在回滚INSERT写入成功的数据，不成功的数据忽略）<br><br>
	 * 
	 * 回滚处理过程：<br>
	 * 1. 从磁盘中读出INSERT堆栈数据，解析每一组INSERT写入单元<br>
	 * 2. 找到已经成功写入的INSERT单元 <br>
	 * 3. 从INSERT单元中取出映像数据，调用JNI.leave函数进行数据回滚 <br>
	 * 4. 判断成功或者失败，成功继续，不成功记录和忽略它 <br>
	 * 5. 根据数据块编号，取出InsertProcessFlag，判断是否备份 <br>
	 * 6. 如果之前INSERT备份成功，再次将LEAVE映像数据备份到从站点 <br>
	 * 7. 如果之前INSERT备份不成功，复制整块数据到从站点 <br><br>
	 * 
	 * @return 回滚成功返回真，否则假
	 */
	private boolean rollback() {
		if (rollItem == null) {
			return true;
		}
		// 回滚数据，判断成功
		boolean success = false;
		try {
			success = rollbackInsert(rollItem.getFile());
		} catch (IOException e) {
			Logger.error(e);
		}
		// 以上成功，删除磁盘文件和释放资源
		if (success) {
			success = deleteFile();
		}

		Logger.debug(this, "rollback", success, "rollback %s", getSpace());

		return success;
	}

	/**
	 * 删除磁盘文件
	 * @return
	 */
	private boolean deleteFile() {
		boolean success = (rollItem != null);
		if (success) {
			success = rollItem.delete();
		}
		if (success) {
			rollItem = null;
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		// 如果在存活期，释放本地资源，回滚数据
		Space space = getSpace();
		if (isAlive()) {
			rollback();
		}
		// 释放上级资源
		super.destroy();
		// 最后解决表锁定（destroy方法可能被JVM多次调用，表名可能是空指针，如果是将由unlock方法忽略）
		unlock(space);
	}

}