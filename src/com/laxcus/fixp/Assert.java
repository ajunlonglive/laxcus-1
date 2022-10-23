/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

/**
 * FIXP"<b>请求命令</b>"判断工具。
 * 
 * @author scott.liang
 * @version 1.0 4/14/2009
 * @since laxcus 1.0
 */
public final class Assert {

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
	private static boolean match(Mark mark, byte major, byte minor) {
		return mark.isAsk() && mark.getCode() == Assert.combin(major, minor);
	}

	/**
	 * 判断是RPC调用命令。只判断主标记，不需要子标记
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isRPCall(Mark mark) {
		return mark.isAsk() && mark.getMajor() == Ask.RPC;
	}

	/**
	 * 判断是退出命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isExit(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.EXIT);
	}

	/**
	 * 判断是应答命令且是再见（GOODBYE命令是ASK.NOTITY,EXIT退出的应答）
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isGoodbye(Mark mark) {
		return mark.isAnswer() && mark.getCode() == Answer.GOODBYE;
	}

	/**
	 * 服务器安全模式询问(返回包括不检查、地址、密文、双重)
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isSecureQuery(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.SECURE_QUERY);
	}

	/**
	 * 服务器私密安全模式询问(返回包括不检查、地址、密文、双重)
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isPrivateSecureQuery(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.PRIVATE_SECURE_QUERY);
	}

	/**
	 * 判断是“建立安全”命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isSecureCreate(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.SECURE_CREATE);
	}

	/**
	 * 判断是“撤销安全”命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isSecureDrop(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.SECURE_DROP);
	}

	/**
	 * 判断是私有“撤销安全”命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isPrivateSecureDrop(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.PRIVATE_SECURE_DROP);
	}

	/**
	 * 判断是私有“建立安全”命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isPrivateSecureCreate(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.PRIVATE_SECURE_CREATE);
	}

	/**
	 * 判断是“远程关闭节点”命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isShutdown(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.SHUTDOWN);
	}

	/**
	 * 判断是快速投递握手
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isCastHelo(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.CASTHELO);
	}

	/**
	 * 判断是快速投递
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isCast(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.CAST);
	}

	/**
	 * 判断是快速投递退出
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isCastExit(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.CASTEXIT);
	}

	/**
	 * 判断是重新投递全部数据包
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isCastRefire(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.CASTREFIRE);
	}

	/**
	 * 判断是“HELO”命令。<br>
	 * 客户端向服务器发送UDP模式的“HELO”命令，保持通信双方的激活状态，证明双方存在着。
	 * 
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isHelo(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.HELO);
	}

	/**
	 * 判断是测试指令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isTest(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.TEST);
	}

	/**
	 * 判断是地址检测指令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isReflect(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.REFLECT);
	}

	/**
	 * 判断是FIXP UDP服务器的地址和端口检测命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isShine(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.SHINE);
	}

	/**
	 * 判断借用FIXP UDP服务器信道的RPC操作
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isVisit(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.VISIT);
	}

	/**
	 * 判断是回调命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isComeback(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.COMEBACK);
	}

	/**
	 * 判断是尝试发送子包
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isRetrySubPacket(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.RETRY_SUBPACKET);
	}

	/**
	 * 判断是尝试取消包
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isCancelPacket(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.CANCEL_PACKET);
	}

	/**
	 * 空操作
	 * @param mark
	 * @return
	 */
	public static boolean isEmptyOperate(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.EMPTY_OPERATE);
	}

//	/**
//	 * 判断是增加一组日志记录（在日志节点）
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isAddLog(Mark mark) {
//		return Assert.match(mark, Ask.NOTIFY, Ask.ADD_LOG);
//	}

	/**
	 * 判断是增加一组日志记录（在日志节点）
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isFlushLog(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.FLUSH_LOG);
	}
	
	/**
	 * 判断是输出成员日志
	 * @param mark FIXP协议头
	 * @return 返回真或者假
	 */
	public static boolean isFlushElmentLog(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.FLUSH_MEMBER_LOG);
	}

	/**
	 * 输出
	 * @param mark
	 * @return
	 */
	public static boolean isFlushTig(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.FLUSH_TIG);
	}

	/**
	 * 输出
	 * @param mark
	 * @return
	 */
	public static boolean isFlushBill(Mark mark) {
		return Assert.match(mark, Ask.NOTIFY, Ask.FLUSH_BILL);
	}

}