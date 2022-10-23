/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom.front;

import com.laxcus.command.custom.*;
import com.laxcus.front.invoker.*;

/**
 * FRONT节点的自定义调用器。<br><br>
 * 
 * 特别说明：每个自定义调用器都有一个对应的自定义命令。自定义命令和自定义调用器是一对多的关系，即一个命令针对多个节点的调用器。<br><br>
 * 
 * 与分布任务组件不同的是，自定义调用器属于系统层面，默认拥有系统级权限，基于INVOKE/PRODUCE机制，开发、部署、运行归属集群所有者。安全管理比分布任务组件宽松。<br>
 * 自定义调用器派生自EchoInvoker，可以操作EchoInvoker所有功能。<br>
 * 用户在运行前，需要将每个站点上自定义调用器与命令的关系写在配置文件中，并打包发送到指定的各节点目录下，后面工作由系统调用。<br><br>
 * 
 * 自定义配置文件指向在每个节点的“local.xml”文件里。
 * 
 * @author scott.liang
 * @version 1.0 11/2/2017
 * @since laxcus 1.0
 */
public abstract class CustomFrontInvoker extends FrontInvoker {

	/**
	 * 构造默认的FRONT节点自定义调用器
	 */
	protected CustomFrontInvoker() {
		super();
	}

	/**
	 * 构造默认的FRONT节点的自定义命令调用器，指定命令
	 * @param cmd 自定义命令
	 */
	protected CustomFrontInvoker(CustomCommand cmd) {
		super(cmd);
	}

}
