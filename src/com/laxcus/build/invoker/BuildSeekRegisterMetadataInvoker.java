/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.build.pool.*;
import com.laxcus.command.access.user.*;
import com.laxcus.site.*;
import com.laxcus.task.establish.sift.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 检索用户注册元数据调用器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public class BuildSeekRegisterMetadataInvoker extends BuildInvoker {

	/**
	 * 检索检索用户注册元数据调用器
	 * @param cmd 检索用户在线注册的元数据
	 */
	public BuildSeekRegisterMetadataInvoker(SeekRegisterMetadata cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekRegisterMetadata getCommand() {
		return (SeekRegisterMetadata) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekRegisterMetadataProduct product = new SeekRegisterMetadataProduct();

		SeekRegisterMetadata cmd = getCommand();
		for(Siger siger : cmd.getUsers()) {
			Seat seat = new Seat(siger, getLocal());
			SeekRegisterMetadataItem item = new SeekRegisterMetadataItem(seat);

			// 用户资源
			Refer refer = StaffOnBuildPool.getInstance().findRefer(siger);
			if (refer != null) {
				for (Space space : refer.getTables()) {
					// 本地的表名
					item.addLocalTable(space);
				}
			}

			// 找阶段命名
			List<Phase> phases = findLocalPhases(siger);
			for(Phase e : phases) {
				item.addLocalPhase(e);
			}
//			// 码位计算器
//			List<ScalerPart> parts = ScalerPool.getInstance(). findScalerParts(siger);
//			for(ScalerPart e : parts) {
//				item.addLocalScaler(e.getName());
//			}
			
			// 保存
			product.add(item);
		}

		// 发送给TOP/HOME
		boolean success = replyProduct(product);
		return useful(success);
	}

	/**
	 * 找本地的分布任务组件
	 * @param siger 用户签名
	 * @return 阶段命名列表
	 */
	private List<Phase> findLocalPhases(Siger siger) {
		// 保存阶段命名
		ArrayList<Phase> phases = new ArrayList<Phase>();
		phases.addAll(SiftTaskPool.getInstance().findPhases(siger));
		return phases;
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