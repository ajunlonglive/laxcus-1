/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 数据块快速处理调用器。<br><br>
 * 
 * 根据数据表名，转发到指定的DATA站点去执行。<br>
 * 
 * FastMass命令是LoadIndex, StopIndex, LoadEntity, StopEntity的超类。<br>
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class CallFastMassInvoker extends CallInvoker {

	/**
	 * 构造数据块操作调用器，指定命令
	 * @param cmd 数据块操作命令
	 */
	protected CallFastMassInvoker(FastMass cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FastMass getCommand() {
		return (FastMass) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FastMass cmd = getCommand();
		// 检查权限
		boolean success = checkPermission(cmd);
		if (!success) {
			replyFault(Major.FAULTED, Minor.PERMISSION_DENIED);
			return false;
		}
		Space space = cmd.getSpace();

		// 查找关联的站点
		NodeSet set = DataOnCallPool.getInstance().findTableSites(space);

		// 判断站点地址
		List<Node> sites = (set != null ? set.show() : null);
		success = (sites != null && sites.size() > 0);
		// 以容错发送到目标站点
		if (success) {
			int count = incompleteTo(sites, cmd);
			success = (count > 0);
		}
		
		// 不成功，向请求端反馈结果
		if(!success){
			replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
		}

		// 返回
		return success; 
	}
	
	/**
	 * 检查权限！
	 * @param cmd
	 * @return 允许返回真，否则假
	 */
	private boolean checkPermission(FastMass cmd) {
		Siger siger = cmd.getIssuer();
		Refer refer = StaffOnCallPool.getInstance().findRefer(siger);
		if (refer == null) {
			return false;
		}

		if (Laxkit.isClassFrom(cmd, LoadIndex.class)
				|| Laxkit.isClassFrom(cmd, StopIndex.class)) {
			if (!refer.canLoadIndex()) {
				return false;
			}
		} else if (Laxkit.isClassFrom(cmd, LoadEntity.class)
				|| Laxkit.isClassFrom(cmd, StopEntity.class)) {
			if (!refer.canLoadEntity()) {
				return false;
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		FastMassProduct product = new FastMassProduct();
		
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			FastMassProduct sub = null;
			if (isSuccessObjectable(index)) {
				try {
					sub = getObject(FastMassProduct.class, index);
				} catch (VisitException e) {
					Logger.error(e);
				}
			}
			// 保存参数
			if (sub == null) {
				Node hub = super.findHub(index);
				FastMassItem item = new FastMassItem(hub, false);
				product.add(item);
			} else {
				product.addAll(sub);
			}
		}

		// 向终端发送记录
		boolean success = replyProduct(product);

		// 完成操作
		return useful(success);
	}

}
