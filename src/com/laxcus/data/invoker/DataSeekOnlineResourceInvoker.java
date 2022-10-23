/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.data.pool.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.establish.scan.*;
import com.laxcus.util.naming.*;

/**
 * 检索站点在线资源调用器
 * 
 * @author scott.liang
 * @version 1.0 4/22/2018
 * @since laxcus 1.0
 */
public class DataSeekOnlineResourceInvoker extends DataInvoker {

	/**
	 * 构造检索站点在线资源调用器，指定命令
	 * @param cmd 检索站点在线资源
	 */
	public DataSeekOnlineResourceInvoker(SeekOnlineResource cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekOnlineResource getCommand() {
		return (SeekOnlineResource) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekOnlineResourceProduct product = new SeekOnlineResourceProduct();

		// 保存资源引用
		List<Refer> refers = StaffOnDataPool.getInstance().getRefers();
		for (Refer e : refers) {
			product.addRefer(e);
		}

		// 保存阶段命名
		ArrayList<Phase> phases = new ArrayList<Phase>();
		phases.addAll(FromTaskPool.getInstance().getPhases());
		// 主站点才保存SCAN阶段命名
		if (isMaster()) {
			phases.addAll(ScanTaskPool.getInstance().getPhases());
		}
		for (Phase e : phases) {
			product.addPhase(e);
		}
		
//		// 码位计算器
//		List<ScalerPart> parts = ScalerPool.getInstance().getScalerParts();
//		if (parts != null) {
//			for (ScalerPart part : parts) {
//				product.addScalerName(part.getIssuer(), part.getName());
//			}
//		}

		boolean success = replyProduct(product);
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

}