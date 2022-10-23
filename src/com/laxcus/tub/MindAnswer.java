/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub;

/**
 * FIXP协议应答码。<br><br>
 * 
 * FIXP应答码是一个2字节的短整型，出现在服务端向客户端发送的报文头里。它说明任务处理结果，有以下情况：<br>
 * 1. 0表示通用型正确。<br>
 * 2. -1表示通用型错误。<br>
 * 3. 大于0的正整数表示某个具体的正确应答。<br>
 * 4. 小于0且不等于-1的负整数表示某个具体的错误应答。<br>
 * 5. 含“对象化”数据的应答码有错误和正确两种，它们除了符合上述特征外，在位序列的0位置都是1。
 * 通过 (code & 0x1) ==1 判断它们是“对象化”数据。<br>
 * 
 * @author scott.liang
 * @version 1.1 10/13/2009
 * @since laxcus 1.0
 */
public final class MindAnswer {

	// ************ 请求正确 ***************
	public final static short OKAY = 1010;

	/** 再见命令。在收到ASK.EXIT命令后的反馈。 **/
	public final static short GOODBYE = 1013;

	/** 请求正确并且接受 **/
	public final static short ACCEPTED = 1011;

	/** 因为FIXP协议版本不同而不能支持 **/
	public final static short UNSUPPORT = 2001;

	/**
	 * 判断是OKAY命令
	 * @param e FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isOkay(Mind e) {
		return e.isAnswer() && e.getAnswer() == MindAnswer.OKAY;
	}

	/**
	 * 判断是ACCEPT命令
	 * @param e FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isAccept(Mind e) {
		return e.isAnswer() && e.getAnswer() == MindAnswer.ACCEPTED;
	}


	/** 应答码范围 **/
	public static final short RANGE[] = new short[] { 1000, 2999 };

	/**
	 * 判断FIXP应答码在规定范围内
	 * @param reply 被检索的FIXP应答码
	 * @return 返回真或者假
	 */
	public static boolean isAnswer(short reply){
		return MindAnswer.RANGE[0] <= reply && reply <= MindAnswer.RANGE[1];
	}

	//	// ***** 一般性应答 **********
	//	
	//	/** 知道了 **/
	//	public static final short ISEE = 1000;
	//
	//	/** 服务器通知，要求执行安全通信(请求没有错误，因为服务端原因，要求客户端重试) **/
	//	public final static short SECURE_NOTIFY = 1001;
	//	public final static short PRIVATE_SECURE_NOTIFY = 1001;
	//
	//	/** 请求正确并且有效回应 **/
	//
	//	
	//	
	//	/** 安全通信请求并且接受 **/
	//	public final static short SECURE_ACCEPTED = 1012;
	//	
	//	
	//	/** 全部子包已经受理 **/
	//	public final static short SUBPACKETS_ACCEPTED = 1014;
	//	
	//	/** Reflect命令应答 **/
	//	public final static short REFLECT_ACCEPTED = 1015;
	//	
	//	/** Shine命令应答 **/
	//	public final static short SHINE_ACCEPTED = 1016;
	//	
	//	/** Visit命令应答 **/
	//	public final static short VISIT_ACCEPTED = 1017;
	//
	//	/** 接受删除私密安全请求 **/
	//	public final static short PRIVATE_DROP_SECURE_ACCEPTED = 1030;
	//
	//	/** 不接受删除私密安全请求 **/
	//	public final static short PRIVATE_DROP_SECURE_REFUSE = 1031;
	//
	//	/** 接受建立私密安全请求 **/
	//	public final static short PRIVATE_CREATE_SECURE_ACCEPTED = 1032;
	//
	//	/** 不接受建立私密安全请求 **/
	//	public final static short PRIVATE_CREATE_SECURE_REFUSE = 1033;
	//	
	//	// ********* 快速投递确认 **************
	//
	//	/** 确认HELO指令 **/
	//	public final static short CASTHELO_OKAY = 1020;
	//
	//	/** 投递成功 **/
	//	public final static short CAST_OKAY = 1021;
	//
	//	/** 结束投递成功 **/
	//	public final static short CASTEXIT_OKAY = 1022;
	//	
	//	/** 在收到CAST_OKAY后，返回CAST_OKAY_REPLY，形成安全三段 **/
	//	public final static short CAST_OKAY_REPLY = 1023;
	//
	//	// ******** 错误的应答码 *********
	//	
	//	/** 拒绝 **/
	//	public final static short REFUSE = 2000;
	//
	//	
	//	/** 没有注册 **/
	//	public final static short NOTLOGIN = 2002;
	//
	//	/** 客户端错误 **/
	//	public final static short CLIENT_ERROR = 2003;
	//
	//	/** 服务器错误 **/
	//	public final static short SERVER_ERROR = 2004;
	//
	//	/** 安全配置已经存在 **/
	//	public final static short SECURE_EXISTED = 2005;
	//
	//	/** 安全参数错误，拒绝接受 **/
	//	public final static short SECURE_REFUSE = 2006;
	//
	//	/** 没有找到 **/
	//	public final static short NOTFOUND = 2007;

	//	/**
	//	 * 判断应答码是一般性确认
	//	 * @param reply 应答码
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isIsee(short reply) {
	//		return reply == MindAnswer.ISEE;
	//	}
	//
	//
	//
	//	/**
	//	 * 判断应答码是没有注册
	//	 * @param reply 应答码
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isNotLogin(short reply) {
	//		return reply == MindAnswer.NOTLOGIN;
	//	}
	//	
	//	/**
	//	 * 判断是CAST HELO OKAY命令
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isCastHeloOkay(Mind mark) {
	//		return mark.isAnswer() && mark.getAnswer() == MindAnswer.CASTHELO_OKAY;
	//	}
	//	
	//	/**
	//	 * 判断是CAST OKAY命令
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isCastOkay(Mind mark) {
	//		return mark.isAnswer() && mark.getAnswer() == MindAnswer.CAST_OKAY;
	//	}
	//	
	//	/**
	//	 * 判断是CAST OKAY REPLY命令
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isCastOkayReply(Mind mark) {
	//		return mark.isAnswer() && mark.getAnswer() == MindAnswer.CAST_OKAY_REPLY;
	//	}
	//	
	//	/**
	//	 * 判断是CAST EXIT OKAY命令
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isCastExitOkay(Mind mark) {
	//		return mark.isAnswer() && mark.getAnswer() == MindAnswer.CASTEXIT_OKAY;
	//	}
	//	
	//	/**
	//	 * 判断是子包已经受理反馈
	//	 * @param mark FIXP协议标识
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isSubPacketsAccepted(Mind mark) {
	//		return mark.isAnswer() && mark.getAnswer() == MindAnswer.SUBPACKETS_ACCEPTED;
	//	}
	//	
	//	/**
	//	 * 判断是Reflect命令应答
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isReflectAccepted(Mind mark) {
	//		return mark.isAnswer() && mark.getAnswer() == MindAnswer.REFLECT_ACCEPTED;
	//	}
	//	
	//	/**
	//	 * 判断是Shine命令应答
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isShineAccepted(Mind mark) {
	//		return mark.isAnswer() && mark.getAnswer() == MindAnswer.SHINE_ACCEPTED;
	//	}
	//	
	//	/**
	//	 * 判断是Visit命令应答
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isVisitAccepted(Mind mark) {
	//		return mark.isAnswer() && mark.getAnswer() == MindAnswer.VISIT_ACCEPTED;
	//	}
	//	
	//	/**
	//	 * 判断是Visit命令应答
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isPrivateSecureNotify(Mind mark) {
	//		return mark.isAnswer() && mark.getAnswer() == MindAnswer.PRIVATE_SECURE_NOTIFY;
	//	}
	//
	//	/**
	//	 * 判断是接受建立私密密钥（在保存密钥的服务器端）
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isPrivateCreateSecureAccepted(Mind mark) {
	//		return mark.isAnswer()
	//				&& mark.getAnswer() == MindAnswer.PRIVATE_CREATE_SECURE_ACCEPTED;
	//	}
	//
	//	/**
	//	 * 判断是拒绝建立私密密（在保存密钥的服务器端）
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isPrivateCreateSecureRefuse(Mind mark) {
	//		return mark.isAnswer()
	//				&& mark.getAnswer() == MindAnswer.PRIVATE_CREATE_SECURE_REFUSE;
	//	}
	//
	//	/**
	//	 * 判断是接受删除私密密钥（在删除密钥的服务器端）
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isPrivateDropSecureAccepted(Mind mark) {
	//		return mark.isAnswer()
	//				&& mark.getAnswer() == MindAnswer.PRIVATE_DROP_SECURE_ACCEPTED;
	//	}
	//
	//	/**
	//	 * 判断是拒绝删除私密密钥（在删除密钥的服务器端）
	//	 * @param mark FIXP协议标头
	//	 * @return 返回真或者假
	//	 */
	//	public static boolean isPrivateDropSecureRefuse(Mind mark) {
	//		return mark.isAnswer()
	//				&& mark.getAnswer() == MindAnswer.PRIVATE_DROP_SECURE_REFUSE;
	//	}


}