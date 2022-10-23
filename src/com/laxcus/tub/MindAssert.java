/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub;

/**
 * FIXP"<b>请求命令</b>"判断工具。
 * 
 * @author scott.liang
 * @version 1.0 4/14/2009
 * @since laxcus 1.0
 */
public final class MindAssert {

	/**
	 * 将主从码合并为命令符号
	 * @param major 主码
	 * @param minor 从码
	 * @return 合并后的FIXP协议标头码
	 */
	public static short combin(byte major, byte minor) {
		int code = major & 0xFF;
		code <<= 8;
		code |= minor;
		return (short) code;
	}

	/**
	 * 检测FIXP协议标头与主码、从码匹配
	 * @param mark FIXP协议标头
	 * @param major 主码
	 * @param minor 从码
	 * @return 返回真或者假
	 */
	private static boolean match(Mind mark, byte major, byte minor) {
		return mark.isAsk() && mark.getCode() == MindAssert.combin(major, minor);
	}

	/**
	 * 判断是RPC调用命令。只判断主标记，不需要子标记
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isRPC(Mind mark) {
		return mark.isAsk() && mark.getMajor() == MindAsk.RPC;
	}
	
	/**
	 * 判断是远程操作方法。只判断主标记，不考虑从标记
	 * @param mark 命令头
	 * @return 返回真或者假
	 */
	public static boolean isMethod(Mind mark) {
		return mark.isAsk() && mark.getMajor() == MindAsk.METHOD;
	}

	/**
	 * 判断是退出命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isExit(Mind mark) {
		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.EXIT);
	}

	/**
	 * 判断是应答命令且是再见（GOODBYE命令是ASK.NOTITY,EXIT退出的应答）
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isGoodbye(Mind mark) {
		return mark.isAnswer() && mark.getCode() == MindAnswer.GOODBYE;
	}

//	/**
//	 * 服务器安全模式询问(返回包括不检查、地址、密文、双重)
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isSecureQuery(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.SECURE_QUERY);
//	}
//
//	/**
//	 * 服务器私密安全模式询问(返回包括不检查、地址、密文、双重)
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isPrivateSecureQuery(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.PRIVATE_SECURE_QUERY);
//	}
//
//	/**
//	 * 判断是“建立安全”命令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isSecureCreate(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.SECURE_CREATE);
//	}
//
//	/**
//	 * 判断是“撤销安全”命令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isSecureDrop(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.SECURE_DROP);
//	}
//
//	/**
//	 * 判断是“替换安全”命令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isSecureReplace(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.SECURE_REPLACE);
//	}
//
//
//	/**
//	 * 判断是私有“撤销安全”命令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isPrivateSecureDrop(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.PRIVATE_SECURE_DROP);
//	}
//
//	/**
//	 * 判断是私有“建立安全”命令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isPrivateSecureCreate(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.PRIVATE_SECURE_CREATE);
//	}

//	/**
//	 * 判断是“远程关闭节点”命令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isShutdown(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.SHUTDOWN);
//	}

//	/**
//	 * 判断是快速投递握手
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isCastHelo(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.CASTHELO);
//	}
//
//	/**
//	 * 判断是快速投递
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isCast(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.CAST);
//	}
//
//	/**
//	 * 判断是快速投递退出
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isCastExit(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.CASTEXIT);
//	}
//
//	/**
//	 * 判断是重新投递全部数据包
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isCastRefire(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.CASTREFIRE);
//	}

	/**
	 * 判断是“HELO”命令。<br>
	 * 客户端向服务器发送UDP模式的“HELO”命令，保持通信双方的激活状态，证明双方存在着。
	 * 
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isHelo(Mind mark) {
		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.HELO);
	}

	/**
	 * 判断是测试指令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isTest(Mind mark) {
		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.TEST);
	}

//	/**
//	 * 判断是地址检测指令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isReflect(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.REFLECT);
//	}
//
//	/**
//	 * 判断是FIXP UDP服务器的地址和端口检测命令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isShine(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.SHINE);
//	}

	/**
	 * 判断借用FIXP UDP服务器信道的RPC操作
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isVisit(Mind mark) {
		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.VISIT);
	}

	/**
	 * 判断借用FIXP UDP服务器信道的RPC操作
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isRunTubService(Mind mark) {
		return MindAssert.match(mark, MindAsk.METHOD, MindAsk.RUN_TUB_SERVICE);
	}

	public static boolean isStopTubService(Mind mark) {
		return MindAssert.match(mark, MindAsk.METHOD, MindAsk.STOP_TUB_SERVICE);
	}

	public static boolean isPrintTubService(Mind mark) {
		return MindAssert.match(mark, MindAsk.METHOD, MindAsk.PRINT_TUB_SERVICE);
	}

	public static boolean isShowTubService(Mind mark) {
		return MindAssert.match(mark, MindAsk.METHOD, MindAsk.SHOW_TUB_SERVICE);
	}

	public static boolean isCheckTubListen(Mind mark) {
		return MindAssert.match(mark, MindAsk.METHOD, MindAsk.CHECK_TUB_LISTEN);
	}	
	
//	/**
//	 * 判断是回调命令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isComeback(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.COMEBACK);
//	}

//	/**
//	 * 判断是尝试发送子包
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isRetrySubPacket(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.RETRY_SUBPACKET);
//	}
//
//	/**
//	 * 判断是尝试取消包
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isCancelPacket(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.CANCEL_PACKET);
//	}

//	/**
//	 * 判断是增加一组日志记录（在日志节点）
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isAddLog(Mind mark) {
//		return MindAssert.match(mark, MindAsk.NOTIFY, MindAsk.ADD_LOG);
//	}

}