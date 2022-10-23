/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 刷新元数据命令调用器 <br>
 * 
 * WATCH站点发起命令，作用到HOME站点，HOME站点通知DATA/WORK/BUILD站点，重新注册到CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public class RayRefreshMetadataInvoker extends RayRefreshResourceInvoker {

	/**
	 * 构造刷新元数据命令调用器，指定命令
	 * @param cmd 刷新元数据命令
	 */
	public RayRefreshMetadataInvoker(RefreshMetadata cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshMetadata getCommand() {
		return (RefreshMetadata) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 此操作必须注册到TOP/HOME，如果在BANK站点，拒绝操作！
		if (isBankHub()) {
			faultX(FaultTip.TOP_HOME_RETRY);
			return false;
		}
		
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 处理返回结果
		int index = findEchoKey(0);
		RefreshMetadataProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(RefreshMetadataProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 打印结果
		print(product, true);

		return useful();
	}

}