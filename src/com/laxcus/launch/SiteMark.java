/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

/**
 * 通用站点标签 <br><br>
 * 
 * 这些标签在“local.xml”中。包括“hub-site”、“local-site”、“monitor-sites”。
 * 带“MK”前缀是标签，数据参数在子集中。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/30/2016
 * @since laxcus 1.0
 */
public final class SiteMark {

	/** 线程堆栈尺寸 **/
	public static final String STACK_SIZE = "stack-size";

	/** 注册的上级站点地址 **/
	public static final String HUB = "hub-site";

	/** 本地站点标签 **/
	public static final String MARK_LOCAL_SITE = "local-site";

	/** 服务器延规定的一般迟注册间隔时间。只需要在TOP/HOME/BANK节点注册，它们规定下属的延时注册时间。 **/
	public static final String HUB_REGISTER_INTERVAL = "hub-register-interval";

	/** 服务器规定的最大延时注册间隔时间。只需要在TOP/HOME/BANK节点注册，达到这个时间，无条件重新注册。**/
	public static final String HUB_MAX_REGISTER_INTERVAL = "hub-max-register-interval";

	/** 站点线程循环单次延时时间。 **/
	public static final String SILENT_TIME = "silent-time";

	/** FIXP服务器RSA密钥令牌默认尺寸 **/
	public static final String SECURE_KEYSIZE = "secure-keysize";
	
	/** 垃圾回收间隔时间 **/
	public static final String GC_INTERVAL = "gc-interval";
	
	/** 可以支持的最大用户数 **/
	public static final String MAX_PERSONS = "max-members";
	
	/** 最大用户数阀值，一个比例值，超过报警 **/
	public static final String MAX_PERSONS_THRESHOLD = "threshold";
	
	/** 检测时间，默认是1分钟  **/
	public static final String MAX_PERSONS_CHECKTIMEOUT = "check-timeout";
	
	/** 一个节点可以承载最多FRONT连接，应用在GATE/CALL节点 **/
	public static final String MAX_FRONTS = "max-fronts";
	
	/** 最大用户数阀值，一个比例值，超过报警 **/
	public static final String MAX_FRONTS_THRESHOLD = "threshold";
	
	/** 检测时间，默认是1分钟  **/
	public static final String MAX_FRONTS_CHECKTIMEOUT = "check-timeout";
	
	/** 边缘容器监听服务器，接收来自终端的通信 **/
	public static final String MARK_TUB_SERVER = "tub-server";
	
	/** 边缘容器服务器的TCP服务器 **/
	public static final String MARK_TUB_STREAM_MONITOR = "stream-monitor";
	
	/** 边缘容器服务器的TCP服务器是监听地址 **/
	public static final String MARK_TUB_STREAM_MONITOR_LISTEN = "listen";

	
	/** 主域服务器，接受TCP/UDP的通信 **/
	public static final String MARK_DOMAIN_SERVER = "domain-server";
	
	/** 主域服务器下TCP服务器 **/
	public static final String MARK_DOMAIN_STREAM_MONITOR = "stream-monitor";
	
	/** 主域服务器下的UDP服务器 **/
	public static final String MARK_DOMAIN_PACKET_MONITOR = "packet-monitor";
	
	/** 主域服务器下的SOCKET客户机 **/
	public static final String MARK_DOMAIN_SOCKET_CLIENT = "client";

	/** 主域服务器下的密文配置 **/
	public static final String MARK_DOMAIN_SERVER_CIPHER = "cipher";

	/** 反馈服务器，向请求站点反馈处理结果数据 **/
	public static final String MARK_REPLY_SERVER = "reply-server";

	/** 本地节点地址 */
	public static final String LOCAL_NODE = "local-node";

	/** 网关站点内部地址 */
	public static final String INNER_NODE = "inner-node";

	/** 网关站点外部地址 */
	public static final String OUTER_NODE = "outer-node";

	/** 分布命令处理模式，只在FRONT/WATCH站点使用。 **/
	public static final String COMMAND_MODE = "command-mode";

	/** 分布命令超时，只在FRONT/WATCH站点使用 **/
	public static final String COMMAND_TIMEOUT = "command-timeout";

	/** FRONT登录GATE/ENTRANCE节点时的循环查询等待间隔时间 **/
	public static final String LINGER_TIMEOUT = "linger-timeout";

	/** 内网节点检测自己在NAT设备地址的时间间隔，目前只在FRONT站点使用。 **/
	public static final String POCK_INTERVAL = "pock-interval";

	/** 被WATCH监视的站点定时刷新时间间隔 **/
	public static final String OUTLOOK_INTERVAL = "outlook-interval";

	/** 监视站点标签 **/
	public static final String MARK_MONITOR_SITES = "monitor-sites";

	/** 监视节点地址 **/
	public static final String MONITOR_NODE = "monitor-node";

	/** Monitor 客户端连接超时，属性参数 **/
	public static final String MONITOR_SOCKET_TIMEOUT = "com-timeout";

	/** Monitor 客户端连接模式，属性参数 **/
	public static final String MONITOR_SOCKET_MODE = "com-mode";

	/** FIXP服务器密文超时。超时后，FIXP服务器把密文删除。 格式是： 
	 * <cipher> <timeout> 120s </timeout> <client-bits> 128 </client-bits> <server-bits>256</server-bits> </cipher> 
	 **/
	public static final String CIPHER_TIMEOUT = "timeout";

	/** FIXP服务器，处于客户端状态时的密文规定长度，以数位计，即8位，16位，32位，以此类推 */
	public static final String CIPHER_CLIENT_BITS = "client-bits";

	/** FIXP服务器，处于服务器状态的密文规定长度，以数位计，即8位、16位、32位，以此类推 */
	public static final String CIPHER_SERVER_BITS = "server-bits";

	/** TCP SOCKET堆栈等待数目，即socket api, listen(int sockfe, int backlog)函数的第二个参数 */
	public static final String TCP_BLOCKS = "tcp-blocks";

	/** FIXP TCP服务器的SOCKET接收缓存尺寸 **/
	public static final String SERVER_TCP_RECEIVE_BUFFERSIZE = "receive-buffer-size";

	/** FIXP TCP服务器线程堆栈尺寸 **/
	public static final String SERVER_TCP_STACK_SIZE = "stack-size";

	/** FIXP UDP服务器的SOCKET接收缓存尺寸 **/
	public static final String SERVER_UDP_RECEIVER_BUFFERSIZE = "receive-buffer-size";

	/** FIXP UDP服务器的SOCKET发送缓存尺寸 **/
	public static final String SERVER_UDP_SEND_BUFFERSIZE = "send-buffer-size";

	/** FIXP UDP服务器线程堆栈尺寸 **/
	public static final String SERVER_UDP_STACK_SIZE = "stack-size";

	/** SOCKET发送缓存，适用TCP/UDP两种模式 **/
	public static final String CLIENT_SEND_BUFFERSIZE = "send-buffer-size";

	/** SOCKET接收缓存，适用TCP/UDP两种模式 **/
	public static final String CLIENT_RECEIVE_BUFFERSIZE = "receive-buffer-size";

	/** SOCKET连接超时，适用于TCP/UDP两种模式 **/
	public static final String CLIENT_CONNECT_TIMEOUT = "connect-timeout";

	/** SOCKET接收数据超时，适用于TCP/UDP两种模式 **/
	public static final String CLIENT_RECEIVE_TIMEOUT = "receive-timeout";

	/** SOCKET子包接收超时，用于UDP模式，FIXP协议特有 **/
	public static final String CLIENT_SUBPACKET_RECEIVE_TIMEOUT = "subpacket-receive-timeout";

	/** SOCKET子包尺寸，不得大于512字节 **/
	public static final String CLIENT_SUBPACKET_SIZE = "subpacket-size";

	/** 处理UDP数据包业务的并行线程数目。运行过程中，UDP业务频繁，UDP监视器将把这些包分发下去，让它们并行处理，提高处理效率  **/
	public static final String PACKET_TASK_THREADS = "task-threads";

	/** socket通信信道超时时间  **/
	public static final String CHANNEL_TIMEOUT = "channel-timeout";
	
	/** 映射端口 **/
	public static final String REFLECT_PORT = "reflect-port";

}