/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.echo;

import com.laxcus.echo.*;

/**
 * 异步数据受理器。<br>
 * 当“异步数据接收器（EchoReceiver）”收到数据后，向“异步数据受理器（EchoAcceptor）”传递一个回显标识，通知它数据到达。
 * “异步数据受理器”将根据回显标识，找到等待中的异步任务，调用异步任务去完成它的数据处理工作。<br><br>
 * 
 * “异步应答数据受理器”由管理异步任务的“管理池”实现。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/29/2012
 * @since laxcus 1.0
 */
public interface EchoAcceptor {

	/**
	 * 异步数据接收器（EchoReceiver）通知异步数据受理器（EchoAcceptor），异步数据已经到达！<br><br>
	 * 
	 * look方法返回一个布尔值。接受是返回“真”，否则“假”。返回“假”的原因是内存已经没有这个异步调用器。
	 * 比如在通知到来之前，异步调用器已经被用户或者系统关闭和释放掉。<br><br>
	 * 
	 * 另外说明：<br>
	 * 为降低计算机运行负担和简化数据处理规则，设计规定，当一个异步调用器向多个节点发送命令后，
	 * 只有当全部节点通过look方法反馈结果（不区分成功或者失败），才启动异步调用器处理工作。
	 * 或者说，当一个异步调用器在处理“ending”阶段的工作时，它发出的请求已经得到全部反馈，无论成功或者失败。<br>
	 * 
	 * @param flag 回显标识
	 * @return 接受返回“真”，否则“假”。
	 */
	boolean look(EchoFlag flag);
}