/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit;

import com.laxcus.echo.*;

/**
 * RPC异步应答接口。 <br><br>
 * 
 * 这个接口的客户端实现是：EchoClient；服务端实现是：EchoAgent。<br><br>
 * 
 * 客户端的EchoClient负责发送异步应答数据，服务端的EchoAgent，在FIXP服务器启动，
 * 被注册和绑定到“远程过程调用适配器（VisitAdapter）”。
 * 当“VisitAdapter”收到FIXP服务器的异步RPC请求时，“VisitAdapter”调用“EchoAgent”方法，
 * 由EchoAgent将数据转发给EchoReceiver。实现RPC异步应答操作。<br><br>
 * 
 * 实现EchoAgent到EchoReceiver数据转发的关键是“回显标识（EchoFlag）”，这是识别EchoReceiver的唯一途径。
 * 每个EchoReceiver必须在启动时，通过回显标识注册到EchoAgent。<br><br>
 * 
 * RPC异步应答接口有两种模式：1.普通模式。2.快速模式。<br><br>
 * 
 * 普通模式的异步应答数据处理分为三个阶段：<br>
 * 1. 启动异步数据传输，对应方法是"start"。只有一次<br>
 * 2. 执行异步数据传输，对应方法是"push"。任意多次<br>
 * 3. 结束异步数据传输，对应方法是"stop"。 只有一次<br>
 * 
 * "push"是可选操作，在没有数据发送的情况下忽略。"start"、"stop"是必选操作。<br>
 * 普通模式适用于少量数据（通常在几K之内）的数据传输。<br><br>
 * 
 * 快速模式的异步应答数据处理分为三个阶段：<br>
 * 1. 启动快速异步数据传输，对应方法“cast”。只有一次。<br>
 * 2. 启动一个异步快速发送（ReplyClient/ReplySender），对应方法在EchoVisit实现类实现“ReplyReceiver”方法。通过UDP套接字，以无序发送/接收纠正模式，实现数据发送/接收。任意多次。<br>
 * 3. 结束快速异步数据对应，对应方法“exit”。只有一次。<br>
 *
 * 快速模式针对网络间的大规模/大批量的数据传输，如数据块分发这样的业务。<br>
 * 
 * @author scott.liang
 * @version 1.1 1/09/2018
 * @since laxcus 1.0
 */
public interface EchoVisit extends Visit {

	/**
	 * 启动异步通信发送，这是异步应答的第1次。
	 * @param flag 回显标识。由请求方(xxxClient)定义，EchoAgent通过这个参数找到等待中的请求方。
	 * @param head 后续数据的基本信息。
	 * @return 接受返回真，否则返回假。
	 * @throws VisitException - 远程访问异常
	 */
	boolean start(EchoFlag flag, EchoHead head) throws VisitException;

	/**
	 * 发送数据。这是从异步应答的第2次及以后的发送
	 * @param flag 回显标识。
	 * @param field 回显数据域
	 * @return 返回下次传输的数据数组下标位置(正常的返回等于大于0；出错是-1，可以停止传输)
	 * @throws VisitException - 远程访问异常
	 */
	long push(EchoFlag flag, EchoField field) throws VisitException;

	/**
	 * 结束发送。这是任务异步应答的最后一次发送。
	 * @param flag 回显标识
	 * @param tail 结束记录。
	 * @return 接受返回true，否则返回false。
	 * @throws VisitException - 远程访问异常
	 */
	boolean stop(EchoFlag flag, EchoTail tail) throws VisitException;

	/**
	 * 启动UDP快速异步通信。这个异步应答的第1次
	 * @param flag 回显标识。
	 * @param head 后续数据的基本信息
	 * @return 返回套接字和加密的基本信息，如果失败返回空指针。
	 * @throws VisitException - 远程访问异常
	 */
	CastToken cast(EchoFlag flag, EchoHead head) throws VisitException;

	/**
	 * 结束UDP快速异步通信。是异步应答的最后一次发送。
	 * @param flag 回显标识
	 * @param tail 结束记录
	 * @return 接受返回真，否则假
	 * @throws VisitException - 远程访问异常
	 */
	boolean exit(EchoFlag flag, EchoTail tail) throws VisitException;
}