/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit;

import com.laxcus.command.*;

/**
 * 异步命令的远程调用接口。<br><br>
 * 
 * 这个接口的客户机实现是“CommandClient”，服务器的实现由各个站点去分别定义，规则是在接口后面加站点名称，如：
 * CommandVisitOnTop、CommandVisitOnHome、CommandVisitOnCall ...... <br><br>
 * 
 * CommandVisit客户机负责向服务器提交异步命令，服务器判断客户机的异步命令，选择“受理”或者“不受理”两种可能（布尔值），返回给客户机。客户机在收到服务器“受理”通知后要继续等待。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/12/2009
 * @since laxcus 1.0
 */
public interface CommandVisit extends Visit {

	/**
	 * 客户机向服务器提交一个异步命令。<br>
	 * 这个方法是异步操作的第一个阶段，客户机向服务器发起命令后，得到服务器的“受理/不受理”的通知，然后立即返回，不等待处理结果。
	 * 命令处理结果是在服务器处理完成后，根据客户端提供的回显地址，由EchoClient发送给客户机的异步代理，再由异步代理转发给客户机，完成一个完整的异步操作。
	 * 客户机在收到服务器的受理通知后，将进入等待，直到收到处理结果，或者超时关闭。<br>
	 * 
	 * @param cmd 异步命令。必须含有回显地址，否则服务端不受理。
	 * @return 服务端受理客户机异步命令，返回“真”，否则返回“假”。
	 * @throws VisitException - 远程访问异常
	 */
	boolean submit(Command cmd) throws VisitException;
}