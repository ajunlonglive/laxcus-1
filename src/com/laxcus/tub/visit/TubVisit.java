/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.visit;

import com.laxcus.tub.command.*;
import com.laxcus.tub.product.*;

/**
 * TUB远程命令访问接口
 * 
 * @author scott.liang
 * @version 1.0 10/11/2020
 * @since laxcus 1.0
 */
public interface TubVisit {

	/**
	 * 启动边缘计算远程命令操作
	 * @param cmd 边缘计算命令
	 * @return 边缘计算结果报告
	 * @throws TubVisitException
	 */
	TubProduct submit(TubCommand cmd) throws TubVisitException;

}