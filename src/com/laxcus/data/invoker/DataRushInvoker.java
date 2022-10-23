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
import com.laxcus.command.rebuild.*;
import com.laxcus.command.site.find.*;
import com.laxcus.command.stub.reflex.*;
import com.laxcus.data.pool.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 数据块强制转换命令调用器。<br>
 * 此命令只作用在DATA主站点上。
 * 
 * @author scott.liang
 * @version 1.15 8/12/2016
 * @since laxcus 1.0
 */
public class DataRushInvoker extends DataSerialReplaceInvoker {

	/**
	 * 构造数据块强制转换命令调用器，指定命令
	 * @param cmd 数据块强制转换命令
	 */
	public DataRushInvoker(Rush cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Rush getCommand() {
		return (Rush) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Rush cmd = getCommand();
		Space space = cmd.getSpace();
		long stub = 0; // 无效编号
		
		Logger.debug(this, "launch", "process mode is %s", (isDisk() ? "DISK" : "MEMORY"));

		// 逐级判断正确

		// 判断是主站点。只有DATA主站点才能执行RUSH操作
		boolean success = isMaster();
		// 判断数据表存在
		if (success) {
			success = StaffOnDataPool.getInstance().hasTable(space);
		}
		// 判断磁盘存在表空间
		if (success) {
			success = AccessTrustor.hasSpace(space);
		}
		// 拿到CACHE数据块编号
		if (success) {
			stub = AccessTrustor.getCacheStub(space);
			success = (stub != 0);
		}
		// 以上一项不成功退出
		if (!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		// 强制转换
		int state = AccessTrustor.rush(space);
		// 判断成功
		success = (state >= 0);

		Logger.debug(this, "launch", success, "rush %s is %d", space, state);

		// 成功，通知关联节点替换数据块（删除旧的，下载新的）
		if (success) {
			this.doReplace(space, stub);
		}

		// 反馈处理结果
		RushProduct product = new RushProduct(getLocal(), state);
		super.replyProduct(product);

		// 通知DATA站点重新注册
		if (success) {
			StaffOnDataPool.getInstance().reloadIndex();
			StaffOnDataPool.getInstance().reloadCacheReflexStub();
		}

		Logger.debug(this, "launch", success, "reload %s", space);

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
	 * 向HOME站点查询一个与指定表空间关联的CALL站点，然后连接CALL站点，查找与CACHE数据块关联的DATA从站点。
	 * @param space 数据表名
	 * @return 返回CALL站点，或者空指针。
	 */
	private Node findCallSpaceSite(Space space) {
		Node hub = getHub();
		FindTableSite cmd = new FindTableSite(SiteTag.CALL_SITE, space);
		FindTableSiteHook hook = new FindTableSiteHook();
		ShiftFindSpaceSite shift = new ShiftFindSpaceSite(hub, cmd, hook);

		ArrayList<Node> sites = new ArrayList<Node>();

		boolean success = DataCommandPool.getInstance().press(shift);
		// 以上成功，等待
		if (success) {
			// 等待
			hook.await();
			// 收集站点
			FindTableSiteProduct product = hook.getProduct();
			// 判断结果
			success = (product != null);
			if (success) {
				sites.addAll(product.getSites());
			}
		}
		// 检查
		int size = sites.size();

		Logger.note(this, "findCallSpaceSite", success, "from %s, call site size:%d", hub, size);

		return (size > 0 ? sites.get(0) : null);
	}
	
	/**
	 * 去CALL站点，查找与数据块关联的从站点
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功返回从站点地址，没有返回空集合，否则返回空指针
	 */
	private List<Node> findReflexCacheSite(Space space, long stub) {
		// 去HOME站点查询关联的CALL站点
		Node hub = findCallSpaceSite(space);
		if (hub == null) {
			return null; // 可能找不到
		}
		// 根据数据块编号和数据表名，去CALL站点查询关联的DATA从站点
		FindCacheReflexStubSite cmd = new FindCacheReflexStubSite(space, stub);
		// 去CALL站点查询关联缓存块的DATA从站点
		return super.findReflexStubSite(hub, cmd);
	}


	/**
	 * 通知DATA从站点更新数据块
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功返回真，否则假
	 */
	private boolean doReplace(Space space, long stub) {
		// 查找关联的从站点
		List<Node> slaves = findReflexCacheSite(space, stub);
		// 失败
		if (slaves == null) {
			return false;
		}

		// 上传数据块
		for (Node slave : slaves) {
			// 通知从站点，下载存储块
			doUpdateMass(slave, space, stub, false);
			// 通知从站点，删除缓存映像块
			doDeleteCacheReflex(slave, space, stub);
		}

		return true;
	}
}