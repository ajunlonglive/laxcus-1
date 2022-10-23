/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.echo;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.visit.*;

/**
 * EchoCustomer/EchoExplorer类实例
 * 
 * @author scott.liang
 * @version 1.0 3/2/2019
 * @since laxcus 1.0
 */
public interface EchoTracker extends EchoVisit {
	
	/**
	 * 销毁！
	 */
	void destroy();
	
	/**
	 * 设置回显标识
	 * @param e
	 */
	void setEchoFlag(EchoFlag e);

	/**
	 * 返回接收的数据流量
	 * @return 字节长度（长整型）
	 */
	long getReceiveFlowSize();

	/**
	 * 返回已经发送的数据流量
	 * @return 字节长度（长整型）
	 */
	long getSendFlowSize();
	
	/**
	 * 启动RPC快速异步通信
	 * @param head 回显报头
	 * @return 返回标记头，失败返回空指针。
	 */
	CastToken doCast(EchoHead head);
	
	/**
	 * 结束快速RPC异步通信
	 * @param tail 回显报尾
	 * @return 成功返回真，否则假
	 */
	boolean doExit(EchoTail tail);
	
	/**
	 * 简化投递。<br>
	 * 只调用EchoVisit.start, EchoVisit.stop方法，忽略EchoVisit.push方法，发送处理结果。
	 * 
	 * @param head 异步应答报头
	 * @param b 应答数据
	 * @return 发送成功返回真，否则假
	 */
	boolean shoot(EchoHead head);
	
	/**
	 * 快速投递文件到目标地址
	 * @param head 回显报头
	 * @param tailHelp 报尾辅助信息
	 * @param sender 投递代理
	 * @return 成功返回真，否则假
	 */
	boolean post(EchoHead head, EchoHelp tailHelp, ReplySender sender);
	
	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速通信标识
	 * @param sender 快速投递代理
	 * @return 成功返回真，否则假
	 */
	boolean post(EchoCode code, CastFlag flag, ReplySender sender);
	
	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速异步通信标识，这个参数必须指定
	 * @param files 文件
	 * @return 投递成功返回真，否则假。
	 */
	boolean post(EchoCode code, CastFlag flag, File[] files);
	
	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速异步通信标识
	 * @param files 文件数组
	 * @return 成功返回真，否则假
	 */
	boolean post(boolean successful, CastFlag flag, File[] files);
	
	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速异步通信标识
	 * @param data 数据内容
	 * @return 投递成功返回真，否则假。
	 */
	boolean post(EchoCode code, CastFlag flag, byte[] data);
	
	/**
	 * 快速投递数据内容到目标地址
	 * @param successful 成功或者失败
	 * @param flag 快速异步通信标识
	 * @param data 数据内容
	 * @return 成功返回真，否则假
	 */
	boolean post(boolean successful, CastFlag flag, byte[] data);
	
	/**
	 * 指定成功或者失败，发送一个对象
	 * @param successful 成功
	 * @param flag 辅助信息
	 * @param param 对象
	 * @return 发送成功返回真，否则假
	 */
	boolean post(boolean successful, CastFlag flag, Object param);
	
	/**
	 * 默认是成功，向目标地址发送对象
	 * @param flag 辅助信息
	 * @param param 对象
	 * @return 发送成功返回真，否则假
	 */
	boolean post(CastFlag flag, Object param) ;
	
}
