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
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.data.pool.*;
import com.laxcus.data.rollback.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.visit.*;

/**
 * DELETE命令调用器
 * 
 * @author scott.liang
 * @version 1.33 8/12/2016
 * @since laxcus 1.0
 */
public class DataCastDeleteInvoker extends DataRollbackInvoker {

	/** 被删除的数据单元 **/
	private TreeSet<RollbackDeleteItem> records = new TreeSet<RollbackDeleteItem>();

	/** 删除备注 **/
	private DeleteRemark remark = new DeleteRemark();

	/** 最后处理结果 **/
	private AssertDelete assertDelete;

	/**
	 * 构造DELETE命令调用器，指定命令
	 * @param cmd 删除命令
	 */
	public DataCastDeleteInvoker(CastDelete cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.data.invoker.DataRollbackInvoker#getLastConsult()
	 */
	@Override
	public AssertConsult getLastConsult() {
		return assertDelete;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CastDelete getCommand() {
		return (CastDelete) super.getCommand();
	}

	/**
	 * 返回数据表名
	 * @return
	 */
	private Space getSpace() {
		CastDelete cmd = getCommand();
		return (cmd != null ? cmd.getSpace() : null);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CastDelete cmd = getCommand();
		Cabin hub = cmd.getSource();
		Space space = cmd.getSpace();

		// 判断是主站点且表有效（只有主站点才能执行删除操作）
		boolean success = isMaster();
		if (success) {
			success = StaffOnDataPool.getInstance().allow(cmd.getIssuer(), space);
		}
		// 以上判断错误，直接拒绝和退出
		if (!success) {
			Logger.error(this, "launch", "refuse %s", space);
			super.replyFault();
			return false;
		}

		// 申请锁定一个表
		lock(space);
		// 执行“删除操作和删除备份”
		success = delete(cmd);
		// 记录完全成功
		remark.setSuccessful(success);

		// 发送数据
		byte status = (success ? ConsultStatus.SUCCESS : ConsultStatus.FAILED);

		AssumeDelete assume = new AssumeDelete(space, status);
		// 如果成功，统计删除行数，和判断是否返回删除数据
		if(success) {
			// 统计删除的行数
			assume.setRows(remark.getRows());
			// 发送被删除的原始行数据（不是映像数据）
			if (cmd.isSnatch()) {
				success = false; //假设是假
				ContentBuffer buf = new ContentBuffer();
				try {
					for (RollbackDeleteItem item : records) {
						ClassReader reader = new ClassReader(item.getFile());
						AccessStack stack = new AccessStack(reader);
						byte[] b = stack.getContent();
						buf.append(b, 0, b.length);
					}
					assume.setData(buf.toByteArray());
					success = true;
				} catch (Throwable e) {
					Logger.fatal(e); // 存在内存溢出的可能
				}
			}
		}

		// 以回馈模式，发送命令给请求端，然后异步等待请求端的应答
		success = replyTo(hub, assume);

		// 如果发送失败，回滚数据和退出
		if (!success) {
			rollback();
		}

		Logger.debug(this, "launch", success, "reply to %s, %s rows is %d",
				hub, assume.getSpace(), assume.getRows());

		// 如果发送失败，调用器退出；否则将再次接收CALL站点的反馈。
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		// 接收确认应答
		super.lockSingle();
		try {
			if (isSuccessObjectable(index)) {
				assertDelete = getObject(AssertDelete.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		} catch(Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Space space = getSpace();
		// 判断表名匹配
		boolean success = (assertDelete != null && Laxkit.compareTo(space, assertDelete.getSpace()) == 0);
		// 判断操作匹配（调用端回应确认标识，本地也是删除成功）
		if (success) {
			success = (assertDelete.isConfirm() && remark.isSuccessful());
		}
		
		Logger.debug(this, "ending", success, "assert delete %s", space);
		
		// 各调用器之间，进行相互确认。这是保证数据一致性的重要一步。
		if (success) {
			success = exlink();
		}

		Cabin hub = assertDelete.getSource();		
		if (success) {
			// 判断完全成功
			AssumeDelete assume = new AssumeDelete(space, ConsultStatus.CONFIRM_SUCCESS);
			assume.setRows(remark.getRows());
			// 发送给请求端
			success = replyCommand(hub, assume);
			// 如果发送不成功，回滚数据
			if(!success) {
				rollback();
			}
		} else {
			success = rollback();
			byte status = (success ? ConsultStatus.CANCEL_SUCCESS : ConsultStatus.CANCEL_FAILED);
			AssumeDelete assume = new AssumeDelete(space, status);
			// 发送给请求端
			success = replyCommand(hub, assume);
		}

		Logger.debug(this, "ending", success, "删除：%s ，最后结果", space);

		// 删除磁盘文件
		for (RollbackDeleteItem item : records) {
			item.delete();
		}
		// 清除记录
		records.clear();

		// 返回结果
		return useful(success);
	}

	/**
	 * 删除磁盘记录
	 * @param cast
	 * @return 全部成功返回真，否则假
	 */
	private boolean delete(CastDelete cast) {
		// 删除命令
		Delete cmd = cast.getDelete();
		// 被删除的数据块编号
		List<Long> stubs = cast.getStubs();

		// 统计成功数目
		int count = 0;
		// 全部数据块数目
		final int size = stubs.size();
		// 逐一删除数据块中的数据
		for (int index = 0; index < size; index++) {
			long stub = stubs.get(index);
			boolean success = delete(cmd, stub);
			// 不成功退出
			if (!success) break;
			// 成功，统计加1
			count++;
		}
		boolean success = (count == size);
		// 删除成功
		Logger.debug(this, "delete", success, "count:%d, stubs size:%d", count, size);

		return success;
	}

	/**
	 * 执行删除数据操作
	 * @param cmd 删除命令
	 * @param stub 数据块编号
	 * @return 删除和备份成功，返回真，否则假。
	 */
	private boolean delete(Delete cmd, long stub) {
		// 建立回滚文件
		RollbackDeleteItem item = createDeleteRollbackFile(stub);
		String filename = item.getPath();
		DeleteCasket casket = new DeleteCasket(cmd, stub, filename);

		Logger.debug(this, "delete", "回滚数据保存文件 \"%s\"", filename);

		// 调用JNI接口，删除数据
		byte[] primitive = null;
		try {
			primitive = AccessTrustor.delete(casket);
		} catch (AccessException e) {
			Logger.error(e);
			return false;
		}

		Logger.debug(this, "delete", "数据块编号：%x|映像堆栈长度：%d", stub, primitive.length);

		// 分析处理结果，取出数据内容
		AccessStack stack = new AccessStack(primitive);

		// 没有找到，返回成功，继续下一个数据块
		if (stack.isNotFound()) {
			item.delete(); // 没有找到，删除磁盘文件
			return true;
		}
		// 发生错误，返回错误码
		if (stack.isFault()) {
			item.delete(); // 如果出错，删除磁盘文件
			Logger.error(this, "delete", "jni error! code is %d", stack.getFault());
			return false;
		}
		
		Space space = stack.getSpace();
		String path = AccessTrustor.getCachePath(space);
		path = String.format("%s/%x.lxcd", path, stub);
		Logger.debug(this, "delete", "备份前磁盘文件：%s, 长度：%d", path, AccessTrustor.length(path));

		// 记录删除成功的单元
		records.add(item);
		// 统计被删除的行数
		remark.addRows(stack.getRows());

		boolean success = false;
		// 分为缓存块和固定块，备份到从站点
		if (stack.isCacheStub()) {
			success = doBackupCache(stack);
		} else if (stack.isChunkStub()) {
			success = doBackupChunk(stack);
		}

		Logger.debug(this, "delete", success, "备份 %s#%X", cmd.getSpace(), stub);
		Logger.debug(this, "delete", "备份后磁盘文件：%s, 长度：%d", path, AccessTrustor.length(path));
		
		return success;
	}

	/**
	 * 回滚数据。将文件中的数据重新写入磁盘，和备份到从站点
	 * @return
	 */
	private boolean rollback() {
		// 如果是空值，不处理
		if (records.isEmpty()) {
			return true;
		}

		// 按照文件时间，从新到旧排序
		ArrayList<RollbackDeleteItem> array = new ArrayList<RollbackDeleteItem>();
		array.addAll(records);
		Collections.sort(array);

		// 逐一回滚
		int count = 0;
		for (RollbackDeleteItem item : array) {
			File file = item.getFile();
			boolean success = false;
			try {
				success = rollbackDelete(file);
			} catch (IOException e) {
				Logger.error(e);
			}
			// 回滚成功，删除磁盘文件
			if (success) {
				success = item.delete();
			}
			// 成功，统计值加1
			if (success) {
				count++;
			}

			Logger.debug(this, "rollback", success, "revoke %s", file);
		}
		// 判断成功
		boolean success = (count == array.size());

		// 无论成功失败，释放内存。但是故障文件仍然保存在磁盘上
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
		// 如果在存活期，释放本地资源
		if (isAlive()) {
			rollback(); // 回滚数据
		}
		// 释放上级数据资源
		super.destroy();
		// 最后解决表锁定（destroy方法可能被JVM多次调用，表名可能是空指针，如果是将由unlock方法忽略）
		unlock(space);
	}

}