/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.net;

/**
 * 套接字连接类型
 * 
 * @author scott.liang
 * @version 1.0 10/10/2009
 * @since laxcus 1.0
 */
public final class SocketTag {

	/** TCP连接类型 **/
	public final static byte TCP = 1;

	/** UDP连接类型 **/
	public final static byte UDP = 2;

	/**
	 * 判断是有效的套接字类型
	 * @param who 套接字类型
	 * @return 返回真或者假
	 */
	public static boolean isFamily(byte who) {
		// 判断类型
		switch (who) {
		case SocketTag.TCP:
		case SocketTag.UDP:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是“数据流”模式
	 * @return 返回真或者假
	 */
	public static boolean isStream(byte family) {
		return family == SocketTag.TCP;
	}

	/**
	 * 判断是“数据包”模式
	 * @return 返回真或者假
	 */
	public static boolean isPacket(byte family) {
		return family == SocketTag.UDP;
	}	

	/**
	 * 将连接类型翻译为字符串描述
	 * @param family SOCKET连接类型
	 * @return 字符串
	 */
	public static String translate(byte family) {
		switch (family) {
		case SocketTag.TCP:
			return "tcp";
		case SocketTag.UDP:
			return "udp";
		default:
			return "none";
		}
	}

	/**
	 * 将字符串翻译为数字描述
	 * @param text 连接类型文本描述
	 * @return 字节
	 */
	public static byte translate(String text) {
		if (text.matches("^\\s*(?i)(?:TCP)\\s*$")) {
			return SocketTag.TCP;
		} else if (text.matches("^\\s*(?i)(?:UDP)\\s*$")) {
			return SocketTag.UDP;
		} else {
			return -1;
		}
	}

	/**
	 * 判断是有效的端口号
	 * @param port 端口号
	 * @return 返回真或者假
	 */
	public static boolean isPort(int port) {
		return (port >= 0 && port <= 0xFFFF);
	}

}