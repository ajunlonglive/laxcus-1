/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.invoke;

import com.laxcus.tub.command.*;
import com.laxcus.tub.product.*;

/**
 * 边缘容器方法调用器。<br>
 * 
 * 以显示命令的方式，执行具体的边缘命令操作
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public interface TubMethodInvoker {

	/**
	 * 启动边缘计算服务
	 * @param cmd 命令
	 * @return 返回实例
	 */
	TubProduct doTubRunService(TubRunService cmd);

	/**
	 * 停止边缘计算服务
	 * @param cmd
	 * @return
	 */
	TubProduct doTubStopService(TubStopService cmd);

	/**
	 * 显示运行中的容器
	 * @param cmd
	 * @return
	 */
	TubProduct doTubPrintService(TubPrintService cmd);

	/**
	 * 运行存在的边缘容器
	 * @param cmd
	 * @return
	 */
	TubProduct doTubShowContainer(TubShowContainer cmd);


	/**
	 * 检查本地节点的监听地址
	 * @param cmd 命令
	 * @return 返回调用器结果
	 */
	TubProduct doTubCheckListen(TubCheckListen cmd);

}