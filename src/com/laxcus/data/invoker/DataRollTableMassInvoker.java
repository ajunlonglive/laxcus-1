/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.access.stub.sign.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;

/**
 * 修复表数据块调用器。<br>
 * 
 * 是DATA从站点的操作。为减少并行资源消耗，采用串行方式逐一向DATA主站点发送命令，逐一接受反馈结果。
 * 
 * @author scott.liang
 * @version 1.0 4/12/2017
 * @since laxcus 1.0
 */
public class DataRollTableMassInvoker extends DataInvoker {

	/**
	 * 构造修复表数据块，指定命令
	 * @param cmd 修复表数据块
	 */
	public DataRollTableMassInvoker(RollTableMass cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RollTableMass getCommand() {
		return (RollTableMass) super.getCommand();
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		RollTableMass cmd = getCommand();
	//
	//		RollTable table = cmd.getTable();
	//		Node hub = table.getMaster();
	//		Space space = table.getSpace();
	//
	//		RollTableMassProduct product = new RollTableMassProduct();
	//
	//		for (StubSign sign : table.list()) {
	//			// 生成下载信息
	//			long stub = sign.getStub();
	//			StubFlag flag = new StubFlag(space, stub);
	//			DownloadMass sub = new DownloadMass(flag);
	//
	//			// 首先把存储块文件目录
	//			String filename = AccessTrustor.findChunkPath(space, stub);
	//			// 没有且是从站点时，找缓存映像块文件目录
	//			if (filename == null && isSlave()) {
	//				filename = AccessTrustor.findCacheReflexPath(space, stub);
	//				if (filename != null) sub.setCacheReflex(true); // 是缓存映像块
	//			}
	//
	//			// 以上没找到，记录这个数据块。继续下一个。
	//			if (filename == null) {
	//				Logger.warning(this, "launch", "cannot be find '%s'", flag);
	//				product.add(new RollTableItem(stub, false));
	//				continue;
	//			}
	//
	//			Logger.debug(this, "launch", "%x path is %s", stub, filename);
	//
	//			DownloadMassHook hook = new DownloadMassHook();
	//			ShiftDownloadMass shift = new ShiftDownloadMass(hub, sub, hook, filename);
	//			shift.setDisk(isDisk()); // 硬盘/内存模式
	//
	//			boolean success = DataCommandPool.getInstance().press(shift);
	//			// 成功，等待返回结果
	//			if (!success) {
	//				Logger.error(this, "launch", "cannot be download '%s'", flag);
	//				product.add(new RollTableItem(stub, false));
	//				continue;
	//			}
	//
	//			// 等待反馈结果
	//			hook.await();
	//			// 判断成功或者不成功
	//			success = hook.isSuccessful();
	//			if (success) {
	//				StubSign sg = AccessTrustor.affix(space, stub);
	//				// 判断新的签名一致
	//				success = (sg != null && sg.getHash().equals(sign.getHash()));
	//			}
	//			// 保存参数
	//			RollTableItem item = new RollTableItem(stub, success);
	//			product.add(item);
	//		}
	//
	//		// 反馈下载结果
	//		boolean success = replyProduct(product);
	//
	//		Logger.debug(this, "launch", success, "RollTableItem size is:%d", product.size());
	//
	//		return useful(success);
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RollTableMass cmd = getCommand();

		RollTable table = cmd.getTable();
		Node hub = table.getMaster();
		Space space = table.getSpace();

		RollTableMassProduct product = new RollTableMassProduct();

		/**
		 * 恢复数据，三种情况：
		 * 1. 来源是固态块（CHUNK），当前是缓存映像块（CACHE REFLEX）。
		 * 2. 来源是固态块（CHUNK），当前是固态块（CHUNK）。
		 * 3. 来源是缓存块（CACHE），当前是缓存映像块（CACHE REFLEX）。
		 */

		for (StubSign source : table.list()) {
			// 生成下载信息
			long stub = source.getStub();

			boolean success = false;
			if (source.isChunk() && isCacheReflex(space, stub)) {
				success = doCacheReflexToChunk(hub, source.getHash(), space, stub);
			} else if (source.isChunk() && isChunk(space, stub)) {
				success = doChunkToChunk(hub, source.getHash(), space, stub);
			} else if (source.isCache() && isCacheReflex(space, stub)) {
				success = doCacheToCacheReflex(hub, source.getHash(), space, stub);
			}

			// 保存参数
			RollTableItem item = new RollTableItem(stub, success);
			product.add(item);

			//			DownloadMass sub = new DownloadMass(flag);
			//
			//			// 首先把存储块文件目录
			//			String filename = AccessTrustor.findChunkPath(space, stub);
			//			// 没有且是从站点时，找缓存映像块文件目录
			//			if (filename == null && isSlave()) {
			//				filename = AccessTrustor.findCacheReflexPath(space, stub);
			//				if (filename != null) sub.setCacheReflex(true); // 是缓存映像块
			//			}
			//
			//			// 以上没找到，记录这个数据块。继续下一个。
			//			if (filename == null) {
			//				Logger.warning(this, "launch", "cannot be find '%s'", flag);
			//				product.add(new RollTableItem(stub, false));
			//				continue;
			//			}
			//
			//			Logger.debug(this, "launch", "%x path is %s", stub, filename);
			//
			//			DownloadMassHook hook = new DownloadMassHook();
			//			ShiftDownloadMass shift = new ShiftDownloadMass(hub, sub, hook, filename);
			//			shift.setDisk(isDisk()); // 硬盘/内存模式
			//
			//			boolean success = DataCommandPool.getInstance().press(shift);
			//			// 成功，等待返回结果
			//			if (!success) {
			//				Logger.error(this, "launch", "cannot be download '%s'", flag);
			//				product.add(new RollTableItem(stub, false));
			//				continue;
			//			}
			//
			//			// 等待反馈结果
			//			hook.await();
			//			// 判断成功或者不成功
			//			success = hook.isSuccessful();
			//			if (success) {
			//				StubSign sg = AccessTrustor.affix(space, stub);
			//				// 判断新的签名一致
			//				success = (sg != null && sg.getHash().equals(sign.getHash()));
			//			}
			//			// 保存参数
			//			RollTableItem item = new RollTableItem(stub, success);
			//			product.add(item);
		}

		// 反馈下载结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "RollTableItem size is:%d", product.size());

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
	//	 * 生成缓存映像块文件路径
	//	 * @param space 数据表名
	//	 * @param stub 数据块编号
	//	 * @return 返回缓存映像发起人文件路径，或者空指针
	//	 */
	//	private String createCacheReflexPath(Space space, long stub) {
	//		if (isSlave()) {
	//			return AccessTrustor.findCacheReflexPath(space, stub);
	//		}
	//		return null;
	//	}

	/**
	 * 判断当前是从站点且有指定的缓存映像块
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回真或者假
	 */
	private boolean isCacheReflex(Space space, long stub) {
		boolean success = isSlave();
		if (success) {
			success = (AccessTrustor.findCacheReflexPath(space, stub) != null);
		}
		return success;
	}

	/**
	 * 判断当前是固定块
	 * @param space
	 * @param stub
	 * @return
	 */
	private boolean isChunk(Space space, long stub) {
		String filename = AccessTrustor.findChunkPath(space, stub);
		return filename != null;
	}

	/**
	 * 从缓存映像块状态到固态块状态的下载
	 * @param hub DATA MASTER节点地址
	 * @param hash 源固态块文件MD5签名
	 * @param space 表名
	 * @param stub 数据块编号
	 * @return 成功返回真，否则假
	 */
	private boolean doCacheReflexToChunk(Node hub, MD5Hash hash, Space space, long stub) {
		//		Space space = flag.getSpace();
		//		long stub = flag.getStub();
		//		StubFlag flag = new StubFlag(space, stub);

		// 生成固态数据块文件目录
		String filename = AccessTrustor.doChunkFile(space, stub);

		// 以上没找到，记录这个数据块。继续下一个。
		if (filename == null) {
			Logger.error(this, "doCacheReflexToChunk", "cannot be find '%s#%x'", space, stub);
			return false;
		}

		//		DownloadMass cmd = new DownloadMass(flag);
		//		DownloadMassHook hook = new DownloadMassHook();
		//		ShiftDownloadMass shift = new ShiftDownloadMass(hub, cmd, hook, filename);
		//		shift.setDisk(isDisk()); // 硬盘/内存模式
		//
		//		boolean success = DataCommandPool.getInstance().press(shift);
		//		// 成功，等待返回结果
		//		if (!success) {
		//			Logger.error(this, "doCacheReflexToChunk", "cannot be download '%s'", flag);
		//			//			product.add(new RollTableItem(stub, false));
		//			//			continue;
		//			return false;
		//		}
		//
		//		// 等待反馈结果
		//		hook.await();
		//		// 判断成功或者不成功
		//		success = hook.isSuccessful();
		//		if (success) {
		//			StubSign sg = AccessTrustor.affix(space, stub);
		//			// 判断新的签名一致
		//			success = (sg != null && sg.getHash().equals(hash));
		//		}

		Logger.debug(this, "doCacheReflexToChunk", "'%s#%x' path is %s", space, stub, filename);
		
		// 下载数据块
		boolean success = download(hub, hash, space, stub, false, filename);

		// 以上成功，删除磁盘上的缓存映像块
		if(success) {
			int ret = AccessTrustor.deleteCacheReflex(space, stub);
			Logger.note(this, "doCacheReflexToChunk", ret ==0, "delete %s#%x", space, stub);
		}

		return success;
	}

	/**
	 * 从固态块到固态块
	 * @param hub
	 * @param hash
	 * @param space
	 * @param stub
	 * @return
	 */
	private boolean doChunkToChunk(Node hub, MD5Hash hash, Space space, long stub) {
		//		StubFlag flag = new StubFlag(space, stub);
		//		DownloadMass cmd = new DownloadMass(flag);

		//		Space space = flag.getSpace();
		//		long stub = flag.getStub();

		// 首先把存储块文件目录
		String filename = AccessTrustor.findChunkPath(space, stub);
		//		// 没有且是从站点时，找缓存映像块文件目录
		//		if (filename == null && isSlave()) {
		//			filename = AccessTrustor.findCacheReflexPath(space, stub);
		//			if (filename != null) cmd.setCacheReflex(true); // 是缓存映像块
		//		}

		// 以上没找到，记录这个数据块。继续下一个。
		if (filename == null) {
			Logger.error(this, "doChunkToChunk", "cannot be find '%s#%x'", space, stub);
			// product.add(new RollTableItem(stub, false));
			// continue;
			return false;
		}

		Logger.debug(this, "doChunkToChunk", "'%s#%x' path is %s", space, stub, filename);

		return download(hub, hash, space, stub, false, filename);

		//		DownloadMassHook hook = new DownloadMassHook();
		//		ShiftDownloadMass shift = new ShiftDownloadMass(hub, cmd, hook, filename);
		//		shift.setDisk(isDisk()); // 硬盘/内存模式
		//
		//		boolean success = DataCommandPool.getInstance().press(shift);
		//		// 成功，等待返回结果
		//		if (!success) {
		//			Logger.error(this, "launch", "cannot be download '%s'", flag);
		//			//			product.add(new RollTableItem(stub, false));
		//			//			continue;
		//			return false;
		//		}
		//
		//		// 等待反馈结果
		//		hook.await();
		//		// 判断成功或者不成功
		//		success = hook.isSuccessful();
		//		if (success) {
		//			StubSign sg = AccessTrustor.affix(space, stub);
		//			// 判断新的签名一致
		//			success = (sg != null && sg.getHash().equals(hash));
		//		}
		//
		//		return success;
	}

	/**
	 * 从缓存块（CACHE）到缓存映像块（CACHE REFLEX）的下载
	 * @param hub 服务器地址
	 * @param hash 源头文件散列码
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return
	 */
	private boolean doCacheToCacheReflex(Node hub, MD5Hash hash, Space space, long stub) {
		//		StubFlag flag = new StubFlag(space, stub);
		//		DownloadMass cmd = new DownloadMass(flag);

		//		Space space = flag.getSpace();
		//		long stub = flag.getStub();

		//		// 首先把存储块文件目录
		//		String filename = AccessTrustor.findChunkPath(space, stub);
		//		// 没有且是从站点时，找缓存映像块文件目录
		//		if (filename == null && isSlave()) {
		//			filename = AccessTrustor.findCacheReflexPath(space, stub);
		//			if (filename != null) cmd.setCacheReflex(true); // 是缓存映像块
		//		}

		// 生成缓存映像块文件路径
		String filename = (isSlave() ? AccessTrustor.findCacheReflexPath(space, stub) : null);

		// 以上没找到，记录这个数据块。继续下一个。
		if (filename == null) {
			Logger.error(this, "doCacheToCacheReflex", "cannot be find '%s#%x'", space, stub);
			return false;
		}

		//		// 声明是缓存映像块
		//		cmd.setCacheReflex(true); 

		Logger.debug(this, "doCacheToCacheReflex", "'%s#%x' path is %s", space, stub, filename);

		// 下载数据块，是缓存映像块
		return download(hub, hash, space, stub, true, filename);

		//		DownloadMassHook hook = new DownloadMassHook();
		//		ShiftDownloadMass shift = new ShiftDownloadMass(hub, cmd, hook, filename);
		//		shift.setDisk(isDisk()); // 硬盘/内存模式
		//
		//		boolean success = DataCommandPool.getInstance().press(shift);
		//		// 成功，等待返回结果
		//		if (!success) {
		//			Logger.error(this, "launch", "cannot be download '%s'", flag);
		//			//			product.add(new RollTableItem(stub, false));
		//			//			continue;
		//			return false;
		//		}
		//
		//		// 等待反馈结果
		//		hook.await();
		//		// 判断成功或者不成功
		//		success = hook.isSuccessful();
		//		if (success) {
		//			StubSign sg = AccessTrustor.affix(space, stub);
		//			// 判断新的签名一致
		//			success = (sg != null && sg.getHash().equals(hash));
		//		}
		//
		//		return success;
	}

	//	private boolean doStateToState(Node hub, MD5Hash hash, Space space, long stub) {
	//		StubFlag flag = new StubFlag(space, stub);
	//		DownloadMass cmd = new DownloadMass(flag);
	//
	////		Space space = flag.getSpace();
	////		long stub = flag.getStub();
	//		
	//		// 首先把存储块文件目录
	//		String filename = AccessTrustor.findChunkPath(space, stub);
	//		// 没有且是从站点时，找缓存映像块文件目录
	//		if (filename == null && isSlave()) {
	//			filename = AccessTrustor.findCacheReflexPath(space, stub);
	//			if (filename != null) cmd.setCacheReflex(true); // 是缓存映像块
	//		}
	//
	//		// 以上没找到，记录这个数据块。继续下一个。
	//		if (filename == null) {
	//			Logger.warning(this, "launch", "cannot be find '%s'", flag);
	//			//			product.add(new RollTableItem(stub, false));
	//			//			continue;
	//			return false;
	//		}
	//
	//		Logger.debug(this, "launch", "%x path is %s", stub, filename);
	//
	//		DownloadMassHook hook = new DownloadMassHook();
	//		ShiftDownloadMass shift = new ShiftDownloadMass(hub, cmd, hook, filename);
	//		shift.setDisk(isDisk()); // 硬盘/内存模式
	//
	//		boolean success = DataCommandPool.getInstance().press(shift);
	//		// 成功，等待返回结果
	//		if (!success) {
	//			Logger.error(this, "launch", "cannot be download '%s'", flag);
	//			//			product.add(new RollTableItem(stub, false));
	//			//			continue;
	//			return false;
	//		}
	//
	//		// 等待反馈结果
	//		hook.await();
	//		// 判断成功或者不成功
	//		success = hook.isSuccessful();
	//		if (success) {
	//			StubSign sg = AccessTrustor.affix(space, stub);
	//			// 判断新的签名一致
	//			success = (sg != null && sg.getHash().equals(hash));
	//		}
	//
	//		return success;
	//	}

	/**
	 * 从源头站点，下载一个完整的数据块
	 * @param hub 服务器地址（当前节点是客户机身份）
	 * @param hash 源数据块MD5签名
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @param cacheReflex 缓存映像
	 * @param filename 本地文件名
	 * @return 下载成功返回真，否则假
	 */
	private boolean download(Node hub, MD5Hash hash, Space space, long stub,
			boolean cacheReflex, String filename) {

		StubFlag flag = new StubFlag(space, stub);
		// 生成命令
		DownloadMass cmd = new DownloadMass(flag);
		// 声明缓存映像块，是或者否
		cmd.setCacheReflex(cacheReflex); 

		DownloadMassHook hook = new DownloadMassHook();
		ShiftDownloadMass shift = new ShiftDownloadMass(hub, cmd, hook, filename);
		shift.setDisk(isDisk()); // 硬盘/内存模式

		Logger.debug(this, "download", "<%s> path is [%s]", flag, filename);

		boolean success = DataCommandPool.getInstance().press(shift);
		// 成功，等待返回结果
		if (!success) {
			Logger.error(this, "donwload", "cannot press! '%s'", flag);
			return false;
		}

		// 等待反馈结果
		hook.await();
		// 判断成功或者不成功
		success = hook.isSuccessful();
		if (success) {
			StubSign sign = AccessTrustor.affix(space, stub);
			// 判断新的签名一致
			success = (sign != null && Laxkit.compareTo(sign.getHash(), hash) == 0);
		}
		
		Logger.note(this, "download", success, "update <%s> to [%s]", flag, filename);

		return success;
	}
}
