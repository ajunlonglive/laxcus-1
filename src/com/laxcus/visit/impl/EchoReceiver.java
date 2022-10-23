/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl;

import com.laxcus.echo.*;

/**
 * 异步数据接收器。<br><br>
 * 
 * 这个接口负责接收来自服务端的异步应答数据。接口由异步通信的请求端去实现，
 * 已经实现的包括：DoubleClient、EchoBuffer。<br><br>
 * 
 * 异步数据接收器在接收数据前，使用“回显标识”注册到“异步数据代理器（EchoAgent）”。
 * 当“异步数据代理”收到应答数据，再通过“回显标识”找到它，将数据转发给它处理。
 * 
 * @author scott.liang
 * @version 1.0 03/09/2009
 * @since laxcus 1.0
 */
public interface EchoReceiver {

	/**
	 * 系统中断。接收器无条件退出，包括关闭网络连接和退出注册。 
	 */
	void halt();

	/**
	 * 返回回显标识
	 * @return 回显标识
	 */
	EchoFlag getFlag();

	/**
	 * 开始接收异步应答数据
	 * @param head 异步应答报头
	 * @return 接受返回真，否则假。
	 */
	boolean start(EchoHead head);

	/**
	 * 接收一条异步应答数据
	 * @param field 异步应答数据域（异步通信将一个大的数据块分成小块和多次传输）
	 * @return 返回下次传输的数据下标位置（等于或者大于0）
	 * @throws EchoException, IndexOutOfBoundsException 如果发生错误时
	 */
	long push(EchoField field);

	/**
	 * 结束接收异步应答数据
	 * @param tail 异步应答尾端
	 * @return 接受返回真，否则假。
	 */
	boolean stop(EchoTail tail);
	
	/**
	 * 启动快速异步通信。<br>
	 * 
	 * 这是第一步，拿到接收端的监听地址。<br>
	 * 即客户端发起操作，请求拿到服务器端ReplySucker地址，这个操作是发生在服务器端！<br>
	 * 实现客户端的ReplyDispatcher向服务器端的ReplySucker发送数据。<br>
	 * 
	 * @param head 异步应答报头
	 * @return 成功返回快速通信标识，失败返回空指针
	 */
	CastToken cast(EchoHead head);
	
	/**
	 * 关闭快速异步通信。<br>
	 * 最后一步，结束异步通信。
	 * 
	 * @param tail 异步应答尾端
	 * @return 接受返回真，否则假。
	 */
	boolean exit(EchoTail tail);

}