/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.factory;

import com.laxcus.application.manage.*;

/**
 * 删除应用
 * 
 * @author scott.liang
 * @version 1.0 8/5/2021
 * @since laxcus 1.0
 */
public interface UninstallFactory {

	/**
	 * 删除应用软件
	 * @param root 
	 * @return 成功返回真，否则假
	 */
	boolean deleteApplication(WRoot root);
}
