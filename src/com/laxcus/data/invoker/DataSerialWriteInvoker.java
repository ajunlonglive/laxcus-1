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

import com.laxcus.access.casket.*;
import com.laxcus.access.column.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.command.stub.reflex.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.data.pool.*;
import com.laxcus.data.rollback.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 数据写操作调用器 <br>
 * 提供数据写入磁盘的方法。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2015
 * @since laxcus 1.0
 */
public abstract class DataSerialWriteInvoker extends DataSerialReplaceInvoker {

	/**
	 * 构造数据写操作调用器，指定命令
	 * @param cmd 数据处理命令
	 */
	protected DataSerialWriteInvoker(Command cmd) {
		super(cmd);
	}

	/**
	 * 生成插入操作的回滚文件
	 * @return 返回插入操作单元
	 */
	protected RollbackInsertItem createInsertRollbackFile() {
		long invokerId = getInvokerId();
		for (int index = 0; index < java.lang.Integer.MAX_VALUE; index++) {
			RollbackInsertItem item = new RollbackInsertItem(invokerId, index);
			File file = new File(RollbackArchive.getDirectory(), item.toString());
			// 不存在，输出
			if (!file.exists()) {
				item.setPath(file.getAbsolutePath());
				return item;
			}
		}
		return null;
	}

	/**
	 * 建立删除操作的回滚文件
	 * @param stub 数据块编号
	 * @return 返回删除操作单元
	 */
	protected RollbackDeleteItem createDeleteRollbackFile(long stub) {
		long invokerId = getInvokerId();
		for (int index = 0; index < java.lang.Integer.MAX_VALUE; index++) {
			RollbackDeleteItem item = new RollbackDeleteItem(invokerId, stub, index);
			File file = new File(RollbackArchive.getDirectory(), item.toString());
			// 不存在，输出
			if (!file.exists()) {
				item.setPath(file.getAbsolutePath());
				return item;
			}
		}
		return null;
	}

	/**
	 * 建立更新操作的回滚文件
	 * @param stub 数据块编号
	 * @return 返回更新操作单元
	 */
	protected RollbackUpdateItem createUpdateRollbackFile(long stub) {
		long invokerId = getInvokerId();
		for (int index = 0; index < java.lang.Integer.MAX_VALUE; index++) {
			// 删除和插入单元
			RollbackUpdateDeleteItem deleteItem = new RollbackUpdateDeleteItem(invokerId, stub, index);
			RollbackUpdateInsertItem insertItem = new RollbackUpdateInsertItem(invokerId, stub, index);
			// 删除和插入文件
			File deleteFile = new File(RollbackArchive.getDirectory(), deleteItem.toString());
			File insertFile = new File(RollbackArchive.getDirectory(), insertItem.toString());
			
			// 不存在，输出它
			if(!deleteFile.exists() && !insertFile.exists()) {
				deleteItem.setPath(deleteFile.getAbsolutePath());
				insertItem.setPath(insertFile.getAbsolutePath());
				return new RollbackUpdateItem(deleteItem, insertItem);
			}
		}
		return null;
	}

	/**
	 * 检查INSERT参数和TABLE的排列顺序一致
	 * @param insert 插入命令
	 * @return 一致返回真，否则假
	 */
	protected boolean check(Insert insert) {
		Space space = insert.getSpace();
		Table table = StaffOnDataPool.getInstance().findTable(space);
		if (table == null) {
			Logger.error(this, "check", "cannot be find '%s'", space);
			return false;
		}

		// 按照列的排列顺序，返回定义的全部列数据类型
		byte[] types = table.getColumnTypes();

		int size = insert.size();
		for (int i = 0; i < size; i++) {
			Row row = insert.get(i);
			// 检查成员一致
			if (row.size() != types.length) {
				Logger.error(this, "check", "%s, %d != %d", space, row.size(), types.length);
				return false;
			}
			// 检查排列一致
			for (int n = 0; n < types.length; n++) {
				Column column = row.get(n);
				if (column == null || column.getType() != types[n]) {
					
					Logger.error(this, "check", "%s, %s != %s", space,
							ColumnType.translate(column.getType()), ColumnType.translate(types[n]));

					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 替换数据块 <br><br>
	 * 
	 * 此操作发生在JNI.INSERT写数据之后，在JNI存储层，当数据块写满，从CACHE状态转到CHUNK状态返回后，ShiftStack获得此通知（根据AntHelp.INSERT_FULL标识判断）。然后去网络上找到关联的从站点，要求从站点下载新的数据块，再把本地同编号的缓存映像块（CacheReflex stub）删除。<br><br>
	 * 
	 * 
	 * 处理过程：<br>
	 * 1. 去CALL站点找到关联的DATA从站点。<br>
	 * 2. 上传CHUNK数据块给每个DATA从站点（考虑到集群中的资源限制，采取时间换空间方案，串行逐一传输）<br>
	 * 3. 删除DATA从站点的缓存映像块<br>
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功返回真，否则假
	 */
	protected boolean doReplaceCache(Space space, long stub) {
		Node hub = getCommandSite();

		FindCacheReflexStubSite cmd = new FindCacheReflexStubSite(space, stub); 
		// 查找关联的从站点
		List<Node> slaves = findReflexStubSite(hub, cmd); 
		// 如果是空指针，是错误；如果DATA子站点集合是空，可能是没有子站点。
		if (slaves == null) {
			Logger.error(this, "doReplaceCache","cannot be find slave site, from %s", hub);
			return false;
		} else if (slaves.isEmpty()) {
			Logger.warning(this, "doReplaceCache","slave sites is empty! from %s", hub);
			return true;
		}

		// 统计成功数目
		int count = 0;
		// 先更新，后删除
		for (Node slave : slaves) {
			// 通知从节点，下载存储块
			boolean f1 = doUpdateMass(slave, space, stub, false);
			// 通知从节点，删除缓存块
			boolean f2 = doDeleteCacheReflex(slave, space, stub);
			// 全部成功，加1
			if (f1 && f2) {
				count++;
			}
		}

		return (count == slaves.size());
	}

	/**
	 * 找到全部从站点，更新一个数据块
	 * @param space 表名
	 * @param stub 数据块编号
	 * @return 更新到全部从站点，返回真，否则假。
	 */
	protected boolean doUpdateChunk(Space space, long stub) {
		Node hub = getCommandSite();

		List<Node> slaves = findStubSlaveSite(hub, space, stub);
		if (slaves == null) {
			Logger.debug(this, "doUpdateChunk", "cannot be find slave site, from %s", hub);
			return false;
		}

		Logger.debug(this, "doUpdateChunk", "%s:%x site size %d", space, stub,
				slaves.size());

		// 通知DATA从站点更新数据块
		int count = 0;
		for (Node slave : slaves) {
			UpdateMass cmd = new UpdateMass(space, stub);
			UpdateMassHook hook = new UpdateMassHook();
			ShiftUpdateMass shift = new ShiftUpdateMass(slave, cmd, hook);

			// 投递给从站点
			boolean success = DataCommandPool.getInstance().press(shift);
			if (!success) {
				Logger.error(this, "doUpdateChunk", "cannot submit to hub:%s", slave);
				continue;
			}
			// 等待结果
			hook.await();
			// 判断结果
			UpdateMassProduct product = hook.getProduct();
			success = (product != null && product.isSuccessful());
			if (success) {
				count++;
			}

			Logger.debug(this, "doUpdateChunk", success, "update %s:%x to %s",
					space, stub, slave);
		}
		// 返回结果
		boolean success = (count == slaves.size());

		Logger.debug(this, "doUpdateChunk", success, "successful count:%d, slave site:%d", count, slaves.size());

		return success;
	}

	/**
	 * 完整替换一个缓存映像块
	 * @param space 表名
	 * @param stub 数据块编号
	 * @return 成功返回真，否则假
	 */
	protected boolean doUpdateCacheReflex(Space space, long stub) {
		Node hub = getCommandSite();

		FindCacheReflexStubSite cmd = new FindCacheReflexStubSite(space, stub);
		// 查找关联的从站点
		List<Node> slaves = findReflexStubSite(hub, cmd);
		// 如果是空指针，是一个错误；如果是空集合，可能是没有相关DATA子站点，不属于错误
		if (slaves == null) {
			Logger.error(this, "doUpdateCacheReflex", "cannot be find slave site, from %s", hub);
			return false;
		} else if (slaves.isEmpty()) {
			Logger.warning(this, "doUpdateCacheReflex", "slave sites is empty! from %s", hub);
			return true;
		}

		// 更新缓存映像块
		int count = 0; // 统计成功数目
		for (Node slave : slaves) {
			UpdateMass sub = new UpdateMass(space, stub);
			sub.setCacheReflex(true); // 是缓存映像数据块

			UpdateMassHook hook = new UpdateMassHook();
			ShiftUpdateMass shift = new ShiftUpdateMass(slave, sub, hook);

			// 投递给从站点
			boolean success = DataCommandPool.getInstance().press(shift);
			if (!success) {
				Logger.error(this, "doUpdateCacheReflex", "cannot submit to hub:%s", slave);
				continue;
			}
			// 等待结果
			hook.await();
			// 判断结果
			UpdateMassProduct product = hook.getProduct();
			success = (product != null && product.isSuccessful());
			if (success) {
				count++;
			}

			Logger.debug(this, "doUpdateCacheReflex", success, "update %s:%x to %s",
					space, stub, slave);
		}

		boolean success = (count == slaves.size());

		Logger.debug(this, "doUpdateCacheReflex", success,
				"slave sites:%d, successful count:%d", slaves.size(), count);

		return success;
	}

	/**
	 * 将缓存映像数据通过网络备份到从站点
	 * @param stack 数据堆栈
	 * @return 备份成功返回真，否则假
	 */
	protected boolean doBackupCache(AccessStack stack) {
		Node hub = getCommandSite();
		long stub = stack.getStub();
		// 查询CALL站点的缓存映像块
		Space space = stack.getSpace();
		FindCacheReflexStubSite cmd = new FindCacheReflexStubSite(space, stub);
		List<Node> slaves = findReflexStubSite(hub, cmd);
		// 判断，如果是空指针，是错误；如果是返回的DATA从站点地址集是空，可能是没有子站点。
		if (slaves == null) {
			Logger.error(this, "doBackupCache", "cannot be find slave site, from %s", hub);
			return false;
		} else if(slaves.isEmpty()) {
			Logger.warning(this, "doBackupCache", "slave sites is empty! from %s", hub);
			return true;
		}

		// 映像数据
		byte[] reflex = stack.getReflex();

		Logger.debug(this, "doBackupCache",
				"stub:%x, rows:%d, cache reflex size:%d - %d", stack.getStub(),
				stack.getRows(), stack.getReflexSize(), reflex.length);

		// 传输映像数据到DATA从站点
		int count = 0;
		for (Node slave : slaves) {
			SetCacheReflexData sub = new SetCacheReflexData(space, stub, reflex);

			Logger.debug(this, "doBackupCache", "To %s", slave);

			boolean success = doSetReflexData(slave, sub);
			if (success) count++;
		}
		// 返回结果
		return count == slaves.size();
	}

	/**
	 * 将存储块映像数据通过网络备份到从站点
	 * @param stack 数据堆栈
	 * @return 备份成功返回真，否则假
	 */
	protected boolean doBackupChunk(AccessStack stack) {
		Node hub = getCommandSite();
		long stub = stack.getStub();
		// 查询CALL站点的映像存储块
		Space space = stack.getSpace();
		FindChunkReflexStubSite cmd = new FindChunkReflexStubSite(space, stub);
		List<Node> slaves = findReflexStubSite(hub, cmd);
		// 如果是空指针，是一个错误；如果是空集合，可能是没有相关DATA子站点
		if (slaves == null) {
			Logger.error(this, "doBackupChunk", "cannot be find slave site, from %s", hub);
			return false;
		} else if(slaves.isEmpty()) {
			Logger.error(this, "doBackupChunk", "slave sites is empty! from %s", hub);
			return true;
		}
		
		byte[] reflex = stack.getReflex();

		Logger.debug(this, "doBackupChunk", "stub:%x, rows:%d, chunk reflex size:%d-%d", 
				stack.getStub(), stack.getRows(), stack.getReflexSize(), reflex.length);

		// 备份映像数据到DATA从站点
		int count = 0;
		for (Node slave : slaves) {
			SetChunkReflexData sub = new SetChunkReflexData(space, stub, reflex);

			Logger.debug(this, "doBackupChunk", "To %s", slave);

			// 发送数据
			boolean success = doSetReflexData(slave, sub);
			if (success) count++;
		}
		// 返回结果
		return count == slaves.size();
	}

}