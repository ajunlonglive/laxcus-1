/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import com.laxcus.fixp.*;

/**
 * RPC ECHO 通信信者
 * 
 * @author scott.liang
 * @version 1.0 3/1/2019
 * @since laxcus 1.0
 */
public interface EchoMessenger {

	/**
	 * 站点以客户端的身份，通过自己的FIXP数据包服务器，向另一个站点FIXP数据包服务器发送数据，包括对安全通信的处理。
	 * @param packet FIXP数据包
	 * @param timeout 超时时间
	 * @return 发送成功返回真，否则假
	 */
	Packet mailing(Packet packet, long timeout);
	
}
