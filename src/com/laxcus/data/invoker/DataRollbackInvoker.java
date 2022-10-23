/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;
import java.io.*;

import com.laxcus.access.*;
import com.laxcus.access.casket.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.command.attend.*;
import com.laxcus.data.pool.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.classable.*;

/**
 * 数据回滚操作调用器 <br>
 * 提供将数据写回硬盘，和备份到从站点的方法。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2015
 * @since laxcus 1.0
 */
public abstract class DataRollbackInvoker extends DataSerialWriteInvoker implements Attender {

	/** 记录确认集合 **/
	private TreeSet<AttendItem> records = new TreeSet<AttendItem>();

	/**
	 * 构造数据回滚操作调用器，指定命令
	 * @param cmd 数据回滚命令
	 */
	protected DataRollbackInvoker(Command cmd) {
		super(cmd);
	}

	/**
	 * 回滚INSERT数据
	 * 为保证可靠性，回滚后的数据块，整块复制到从站点。
	 * 
	 * @param file 文件实例
	 * @return 成功返回真，否则假
	 */
	protected boolean rollbackInsert(File file) throws IOException {
		if (file == null) {
			Logger.debug(this, "rollbackInsert", "null file pointer");
			return false;
		}
		if (!file.exists()) {
			Logger.debug(this, "rollbackInsert", "cannot be find \"%s\"", file);
			return false;
		}

		// 统计数据堆栈，和回滚成功数目
		int sum = 0, count = 0;
		// 逐一读取数据段
		long fileoff = 0, filen = file.length();
		// 判断读完
		while (fileoff < filen) {
			AccessStack stack = new AccessStack();
			int len = stack.doDisk(file, fileoff);
			fileoff += len; // 统计读取长度

			// 写入不成功的，忽略它
			if (stack.isFault()) {
				Logger.warning(this, "rollbackInsert", "insert failed, ignore it! code:%d", stack.getFault());
				continue;
			}
			// 数据堆栈统计
			sum++;

			// 删除写入的数据
			long stub = stack.getStub();
			Space space = stack.getSpace();

			// 映像数据
			byte[] reflex = stack.getReflex();
			// 调用JNI.leave函数，撤销已经成功写入的数据（即删除数据），返回Leave堆栈数据
			byte[] primitive = AccessTrustor.leave(space, stub, reflex);

			// 删除数据保存到堆栈和解析
			AccessStack leaveStack = new AccessStack(primitive);
			// 回滚不成功，是严重错误！！！（这一块的处理现在仍然没有确定！！！）
			if (!leaveStack.isSuccessful()) {
				Logger.error(this, "rollbackInsert", "cannot be leave! %s - %x, fault code:%d",
						space, stub, leaveStack.getFault());
				continue;
			}

			/**
			 * 撤销数据成功后，为稳妥起见，将整块数据复制到从站点，无论被撤销的数据块是CACHE还是CHUNK状态。
			 */
			boolean success = false;
			if (leaveStack.isCacheStub()) {
				success = doUpdateCacheReflex(space, leaveStack.getStub());
			} else if (leaveStack.isChunkStub()) {
				success = doUpdateChunk(space, leaveStack.getStub());
			}

			// 如果成功，统计值增1
			if (success) count++;

			Logger.debug(this, "rollbackInsert", success, "leave %s - %x", space, stub);
		}

		// 判断回滚成功
		boolean success = (count == sum);

		Logger.debug(this, "rollbackInsert", success, "count:%d, total:%d", count, sum);

		return success;
	}

	/**
	 * 回滚删除数据
	 * @param file 磁盘文件
	 * @return 成功返回真，否则假
	 */
	protected boolean rollbackDelete(File file) throws IOException {
		if (file == null) {
			Logger.debug(this, "rollbackDelete", "null file pointer");
			return false;
		}
		if (!file.exists()) {
			Logger.debug(this, "rollbackDelete", "cannot be find \"%s\"", file);
			return false;
		}

		// 解析磁盘回滚文件，数据导入内存
		AccessStack deleteStack = new AccessStack();
		deleteStack.doDisk(file, 0);

		// 取出数据内容
		byte[] content = deleteStack.getContent();

		// 解析位于数据开始位置的数据标识
		MassFlag flag = new MassFlag();
		ClassReader reader = new ClassReader(content);
		flag.resolve(reader);

		// 找到表
		Space space = flag.getSpace();
		Table table = findTable(space);
		if (table == null) {
			Logger.error(this, "rollbackDelete", "cannot be find %s", space);
			return false;
		}

		// 按照列排序，生成排序表
		Sheet sheet = table.getSheet();		
		// 以行为单位，解析数据
		RowCracker cracker = new RowCracker(sheet);
		cracker.split(reader);

		// 输入全部行
		List<Row> rows = cracker.flush();
		Logger.debug(this, "rollbackDelete", "row size %d", rows.size());

		// 生成INSERT命令
		Insert insert = new Insert(space);
		insert.addAll(rows);

		// 检查数据，不匹配即退出
		if (!check(insert)) {
			Logger.error(this, "rollbackDelete", "check '%s' failed", space);
			return false;
		}

		// 默认要求将返回数据写入内存
		InsertCasket packet = new InsertCasket(insert);
		// 回滚数据写入硬盘，可能有多个实体返回
		byte[] primitive = AccessTrustor.insert(packet);

		Logger.debug(this, "rollbackDelete", "insert primitive size is %d", primitive.length);

		// 统计删除单元和回滚成功数目
		int sum = 0, count = 0;
		reader = new ClassReader(primitive);
		// 逐一分析和保存
		while (reader.hasLeft()) {
			AccessStack stack = new AccessStack(reader);
			// 删除不成功的，忽略它
			if (stack.isFault()) {
				Logger.warning(this, "rollbackDelete", "delete failed, ignore it! code:%d",
						stack.getFault());
				continue;
			}
			// 成员数增1
			sum++;

			/** 两种可能：
			 * 1. 如果CACHE数据块写满，存储层执行CACHE到CHUNK状态转换。这时要整块替换数据块（上传CHUNK数据块，然后删除CACHE映像数据块）
			 * 2. 如果CACHE块没有写满，为稳妥起见，整个缓存块复制到从站点
			 */
			boolean success = false;
			if (stack.isInsertFull()) {
				success = doReplaceCache(space, stack.getStub());
			} else {
				success = doUpdateCacheReflex(space, stack.getStub());
			}
			// 备份成功，统计值增1
			if(success) count++;
		}

		// 判断成功
		boolean success = (count == sum);

		Logger.debug(this, "rollbackDelete", success, "count:%d, total:%d", count, sum);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.invoker.InvokerAuditor#attend(com.laxcus.echo.Cabin)
	 */
	@Override
	public int attend(Cabin cabin) {
		super.lockSingle();
		try {
			AssertConsult consult = getLastConsult();
			// 还没有收到处理结果时，保存参数和等待
			if (consult == null) {
				AttendItem item = new AttendItem(cabin);
				records.add(item);
				return AttendTag.DELAY;
			}
			// 不在检测站点范围内，拒绝它
			if (!consult.isSeekSite(cabin)) {
				return AttendTag.REFUSE;
			}
			// 无论是确认或者取消，都记录这个站点，返回确认
			AttendItem item = new AttendItem(cabin, true);
			records.add(item);
			return AttendTag.CONFORM;
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 同一次操作的不同DATA调用器之间，进行相互确认。<br>
	 * 这是保证数据处理一致性的非常重要一步！<br>
	 * 它采用去中心化思想，在没有CALL节点参与情况下，由各调用器之间进行的相互确认。
	 * 
	 * @return 确认成功返回真，否则假
	 */
	protected boolean exlink() {
		AssertConsult consult = getLastConsult();
		List<Cabin> hubs = consult.getSeekSites();
		SeekAttenderTable table = null;
		// 只在有关联调用器的情况，才进行相互确认
		if (hubs.size() > 0) {
			SeekAttenderHook hook = new SeekAttenderHook();
			ShiftSeekAttender shift = new ShiftSeekAttender(hubs, hook);
			shift.setTimeout(getLeftTime()); // 命令剩余可用时间

			// 交给命令管理器立即处理
			DataCommandPool.getInstance().press(shift);
			hook.await();
			// 等待返回结果
			table = hook.getProduct();
		}

		// 两组参数比较，判断全部结果成功
		int count = 0; // 统计成功数目
		if(table != null) {
			for(SeekAttenderProduct product : table.list()) {
				Cabin cabin = product.getCabin();
				// 判断存在并且成功，统计增1
				boolean passed = hubs.contains(cabin) && product.isSuccessful();
				if (passed) count++;
			}
		}
		// 全部成功
		boolean success = (hubs.size() == count);

		Logger.debug(this, "exlink", success, "ASSERT SITES:%d, REPLY SITES:%d", hubs.size(), count);

		return success;
	}

	/**
	 * 判断来源调用器在指定集合中，发送反馈结果
	 * @param from 其它调用器来源地址
	 */
	private void post(Cabin from) {
		AssertConsult resultAssert = getLastConsult();
		// 确认调用器来源地址在指定集合中
		boolean success = (resultAssert != null && resultAssert.isSeekSite(from));
		SeekAttenderProduct product = new SeekAttenderProduct(from, success);
		// 反馈到目标调用器
		success = replyProduct(from, product);
		Logger.debug(this, "post", success, "send:%s", product);
	}

	/**
	 * 只处理延时未发送的的调用器
	 */
	private void processDelay() {
		for (AttendItem item : records) {
			// 没有执行投递的单元，在这里投递
			if (!item.isPost()) {
				post(item.getCabin());
			}
		}
		// 清除上述全部记录
		records.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		// 如果在存活期，释放本地资源
		if (isAlive()) {
			// 处理相互确认操作中，延时处理的调用器通信
			processDelay();
		}
		// 调用上级参数，销毁数据（放在后面处理）
		super.destroy();
	}

	/**
	 * 返回最后一段数据处理协商结果
	 * @return 返回AssertConsult实例
	 */
	protected abstract AssertConsult getLastConsult();

}
