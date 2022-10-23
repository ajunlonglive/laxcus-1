/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import com.laxcus.platform.listener.*;
import com.laxcus.ray.runtime.*;

/**
 * WATCH资源适配器
 * 
 * @author scott.liang
 * @version 1.0 3/9/2022
 * @since laxcus 1.0
 */
class RayResourceAdapter implements WatchListener {

	/**
	 * 构造WATCH资源适配器
	 */
	public RayResourceAdapter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.platform.listener.WatchResourceListener#clear()
	 */
	@Override
	public void clear() {
//		// 注册地址
//		Node hub = RayLauncher.getInstance().getHub();

		// 清除用户
		RayRegisterMemberBasket.getInstance().clear();
		RayFrontMemberBasket.getInstance().clear();
		// 清除运行时
		RaySiteRuntimeBasket.getInstance().clear();
		// 清除节点
		SiteOnRayPool.getInstance().clear();

//		// 保存HUB地址
//		if (hub != null) {
//			SiteOnRayPool.getInstance().add(hub);
//		}
	}

}