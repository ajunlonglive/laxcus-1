/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.hit;

import com.laxcus.visit.*;
import com.laxcus.site.Node;

/**
 * 管理站点之间的远程访问接口。适用于TOP/HOME/BANK站点。
 * 
 * @author scott.liang
 * @version 1.0 1/28/2009
 * @since laxcus 1.0
 */
public interface HitVisit extends Visit {

	/**
	 * 判断被访问的站点是“管理站点”
	 * 
	 * @return 返回“是”或者“否”。
	 * @throws VisitException
	 */
	boolean isManager() throws VisitException;

	/**
	 * 询问被访问的监视器站点，判断它们共同的管理站点已经失效。
	 * 
	 * @param hub 管理站点地址
	 * @return 失败返回“真”，否则“假”。
	 * @throws VisitException
	 */
	boolean isManagerDisabled(Node hub) throws VisitException;

	/**
	 * 询问目标地址，推举站点成为新的“管理站点”。
	 * 
	 * @param sponsor 请求发起方地址
	 * @return 接受返回“真”，否则“假”。
	 * @throws VisitException
	 */
	boolean discuss(Node sponsor) throws VisitException;
}