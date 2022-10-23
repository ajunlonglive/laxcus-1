/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import com.laxcus.site.*;

/**
 * 站点服务代理 <br>
 * 
 * 提供访问本地站点的安全检查服务。
 * 
 * @author scott.liang
 * @version 1.0 12/17/2013
 * @since laxcus 1.0
 */
public interface SiteTrustor extends SigerTrustor {

	/**
	 * 返回当前站点的监听地址。<br>
	 * 取得监听地址需要分布任务组件提供自己的调用器编号，这是安全检查的一部分，要求保证每个调用器都是安全可信的。
	 * 
	 * @param invokerId 当前异步调用器编号
	 * @return 当前站点监听地址
	 * @throws TaskException 如果调用器不存在，弹出异常
	 */
	Node getLocal(long invokerId) throws TaskException;
}