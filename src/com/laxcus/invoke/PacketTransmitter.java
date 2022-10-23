/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.invoke;

import com.laxcus.fixp.*;

/**
 * 数据包转发器。<br>
 * 
 * 调用者使用这个接口，通过reply方法直接调用FixpPacketMonitor.send方法，向其它站点发送应答数据报文。<br>
 * 
 * 这个接口可以避免因为建立SOCKET，造成请求端接收数据包时，判断发送/应答的IP地址和端口不一致的现象。
 * 
 * @author scott.liang
 * @version 1.2 5/16/2012
 * @since laxcus 1.0
 */
public interface PacketTransmitter {

	/**
	 * 发送应答包。目标地址必须已经在包中指定，否则发送会失败。
	 * @param resp FIXP UDP应答包
	 * @return 发送成功返回“真”，否则“假”。
	 */
	boolean reply(Packet resp);
}