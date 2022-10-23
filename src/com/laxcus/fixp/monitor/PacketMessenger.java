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
 * FIXP UDP数据包信者。(客户端向服务端发送UDP报文)
 * 
 * @author scott.liang
 * @version 1.0 11/29/2010
 * @since laxcus 1.0
 */
public interface PacketMessenger {

	/**
	 * 站点以客户端的身份，通过自己的FIXP数据包服务器，向另一个站点FIXP数据包服务器发送数据，包括对安全通信的处理。
	 * @param packet FIXP数据包
	 * @return 发送成功返回真，否则假
	 */
	boolean notice(Packet packet);

}