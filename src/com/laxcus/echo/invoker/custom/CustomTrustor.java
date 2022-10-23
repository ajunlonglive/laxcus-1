/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

import com.laxcus.site.*;

/**
 * 自定义资源代理。<br><br>
 * 
 * 提供自定义调用器运行时所需的本地基础资源。
 * 
 * @author scott.liang
 * @version 1.0 10/30/2017
 * @since laxcus 1.0
 */
public interface CustomTrustor {

	/**
	 * 返回注册站点地址。<br>
	 * 
	 * @return 注册站点地址
	 */
	Node getHub();

	/**
	 * 返回本地站点地址。<br>
	 * 
	 * @return 本地站点地址
	 */
	Node getLocal();

	/**
	 * 返回次级注册站点地址。<br>
	 * 次级注册站点地址限于FRONT/CALL站点。FRONT注册到AID/CALL站点，CALL站点注册到多个HOME站点。
	 * 
	 * @return 次级站点地址，或者空指针。
	 */
	Node[] getSubHubs();

	/**
	 * 返回自定义显示器
	 * @return CustomDisplay实例
	 */
	CustomDisplay getCustomDisplay();

}