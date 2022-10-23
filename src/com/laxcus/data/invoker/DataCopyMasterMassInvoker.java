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
import com.laxcus.command.stub.transfer.*;
import com.laxcus.data.pool.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * DATA数据块主从复制调用器
 * 
 * @author scott.liang
 * @version 1.0 6/15/2019
 * @since laxcus 1.0
 */
public class DataCopyMasterMassInvoker extends DataInvoker { // DataSerialWriteInvoker {

	/**
	 * 构造DATA数据块主从复制调用器，指定命令
	 * @param cmd 复制DATA主节点数据块
	 */
	public DataCopyMasterMassInvoker(CopyMasterMass cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CopyMasterMass getCommand() {
		return (CopyMasterMass) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须是MASTER，否则拒绝
		boolean success = isMaster();
		if (!success) {
			replyFault(Major.FAULTED, Minor.PERMISSION_DENIED);
			return useful(false);
		}

		// 命令
		CopyMasterMass cmd = getCommand();
		Space space = cmd.getSpace();

		// 判断有数据表
		success = StaffOnDataPool.getInstance().hasTable(space);
		if (!success) {
			replyFault(Major.FAULTED, Minor.PERMISSION_DENIED);
			return useful(false);
		}

		// 从JNI取出缓存块和固定块编号
		long cacheId = AccessTrustor.getCacheStub(space);
		long[] chunkIds = AccessTrustor.getChunkStubs(space);
		
		// DEBUG CODE, START
		Logger.debug(this, "launch", "cache id:%x", cacheId);
		for (int i = 0; chunkIds != null && i < chunkIds.length; i++) {
			Logger.debug(this, "launch", "chunk id:%x", chunkIds[i]);
		}
		// DEBUG CODE, END
		
		// 判断和分别保存
		long cacheStub = 0;
		TreeSet<Long> chunkStubs = new TreeSet<Long>();
		
		// 全部数据块
		if (cmd.isAllStubs()) {
			cacheStub = cacheId;
			for (int i = 0; chunkIds != null && i < chunkIds.length; i++) {
				chunkStubs.add(chunkIds[i]);
			}
		} else {
			for (long stub : cmd.getStubs()) {
				if (stub == cacheId) {
					cacheStub = cacheId; // 缓存块
				} else {
					for (int i = 0; chunkIds != null && i < chunkIds.length; i++) {
						if (stub == chunkIds[i]) {
							chunkStubs.add(chunkIds[i]); // 匹配的固定块
							break;
						}
					}
				}
			}
		}
		
		// 报告结果
		CopyMassProduct product = new CopyMassProduct();
		
		// DATA从站点地址，这个地址已经在HOME节点上存在通过，所以在DATA主节点上，直接把数据发送从节点。
		List<Node> slaves = cmd.getSlaves();
		// 缓存编号
		if (cacheStub != 0) {
			for (Node slave : slaves) {
				success = doOverrideCacheReflex(slave, space, cacheStub);
				product.add(cacheStub, success);
			}
		}
		// 固态块编码
		for (long chunkId : chunkStubs) {
			for (Node slave : slaves) {
				success = doOverrideChunk(slave, space, chunkId);
				product.add(chunkId, success);
			}
		}
		
		// 发送反馈结果
		success = replyProduct(product);
		
		Logger.debug(this, "launch", success, "product size %d", product.size());

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
	 * 完整替换一个缓存映像块
	 * @param slave 从站点
	 * @param space 表名
	 * @param stub 数据块编号
	 * @return 成功返回真，否则假
	 */
	private boolean doOverrideCacheReflex(Node slave, Space space, long stub) {
		UpdateMass sub = new UpdateMass(space, stub);
		sub.setCacheReflex(true); // 是缓存映像数据块

		UpdateMassHook hook = new UpdateMassHook();
		ShiftUpdateMass shift = new ShiftUpdateMass(slave, sub, hook);
		// 投递给从站点
		boolean success = DataCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "doOverrideCacheReflex", "cannot submit to slave:%s", slave);
			return false;
		}
		// 等待结果
		hook.await();
		// 判断结果
		UpdateMassProduct product = hook.getProduct();
		success = (product != null && product.isSuccessful());

		Logger.debug(this, "doOverrideCacheReflex", success, "update %s:%x to %s",
				space, stub, slave);

		return success;
	}
	
	/**
	 * 找到全部从站点，更新一个数据块
	 * @param space 表名
	 * @param stub 数据块编号
	 * @return 更新到全部从站点，返回真，否则假。
	 */
	private boolean doOverrideChunk(Node slave, Space space, long stub) {
		UpdateMass cmd = new UpdateMass(space, stub);
		UpdateMassHook hook = new UpdateMassHook();
		ShiftUpdateMass shift = new ShiftUpdateMass(slave, cmd, hook);

		// 投递给从站点
		boolean success = DataCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "doOverrideChunk", "cannot submit to slave: %s", slave);
			return false;
		}
		// 等待结果
		hook.await();
		// 判断结果
		UpdateMassProduct product = hook.getProduct();
		success = (product != null && product.isSuccessful());

		Logger.debug(this, "doOverrideChunk", success, "update %s:%x to %s", space, stub, slave);
		
		return success;
	}
	
//	/**
//	 * 找到全部从站点，更新一个数据块
//	 * @param space 表名
//	 * @param stub 数据块编号
//	 * @return 更新到全部从站点，返回真，否则假。
//	 */
//	private boolean doOverrideChunk(Node slave, Space space, long stub) {
//		Node hub = getCommandSite();
//
//		List<Node> subs = findStubSlaveSite(hub, space, 0);
//		if (subs == null) {
//			Logger.debug(this, "doOverrideChunk", "cannot be find slave site, from %s", hub);
//			return false;
//		}
//
//		Logger.debug(this, "doOverrideChunk", "%s:%x site size %d", space, stub,
//				subs.size());
//
//		// 通知DATA从站点更新数据块
//		int count = 0;
//		for (Node node : subs) {
//			// 节点不一致则忽略它
//			if (Laxkit.compareTo(slave, node) != 0) {
//				continue;
//			}
//			
//			UpdateMass cmd = new UpdateMass(space, stub);
//			UpdateMassHook hook = new UpdateMassHook();
//			ShiftUpdateMass shift = new ShiftUpdateMass(node, cmd, hook);
//
//			// 投递给从站点
//			boolean success = DataCommandPool.getInstance().press(shift);
//			if (!success) {
//				Logger.error(this, "doOverrideChunk", "cannot submit to hub:%s", hub);
//				continue;
//			}
//			// 等待结果
//			hook.await();
//			// 判断结果
//			UpdateMassProduct product = hook.getProduct();
//			success = (product != null && product.isSuccessful());
//			if (success) {
//				count++;
//			}
//
//			Logger.debug(this, "doOverrideChunk", success, "update %s:%x to %s",
//					space, stub, node);
//		}
//		// 返回结果
//		boolean success = (count == subs.size());
//
//		Logger.debug(this, "doOverrideChunk", success, "successful count:%d, slave site:%d", count, subs.size());
//
//		return success;
//	}
}
