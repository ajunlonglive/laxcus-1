/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

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
public final class Answer {

	// ***** 一般性应答 **********
	
	/** 知道了 **/
	public static final short ISEE = 1000;

	/** 服务器通知，要求执行安全通信(请求没有错误，因为服务端原因，要求客户端重试) **/
	public final static short SECURE_NOTIFY = 1001;
	
	/** 客户机上传明文数据给服务器，服务器通知，要求客户机加密数据内容 **/
	public final static short ENCRYPT_CONTENT_NOTIFY = 1002;
	
	/** 私密通信，要求执行加密 **/
	public final static short PRIVATE_SECURE_NOTIFY = 1003;

	/** 请求正确并且有效回应 **/

	// ************ 请求正确 ***************
	public final static short OKAY = 1010;

	/** 请求正确并且接受 **/
	public final static short ACCEPTED = 1011;

	/** 安全通信请求并且接受 **/
	public final static short SECURE_ACCEPTED = 1012;
	
	/** 再见命令。在收到ASK.EXIT命令后的反馈。 **/
	public final static short GOODBYE = 1013;

	/** 全部子包已经受理 **/
	public final static short SUBPACKETS_ACCEPTED = 1014;
	
	/** Reflect命令应答 **/
	public final static short REFLECT_ACCEPTED = 1015;
	
	/** Shine命令应答 **/
	public final static short SHINE_ACCEPTED = 1016;
	
	/** Visit命令应答 **/
	public final static short VISIT_ACCEPTED = 1017;

	/** 接受删除私密安全请求 **/
	public final static short PRIVATE_DROP_SECURE_ACCEPTED = 1030;

	/** 不接受删除私密安全请求 **/
	public final static short PRIVATE_DROP_SECURE_REFUSE = 1031;

	/** 接受建立私密安全请求 **/
	public final static short PRIVATE_CREATE_SECURE_ACCEPTED = 1032;

	/** 不接受建立私密安全请求 **/
	public final static short PRIVATE_CREATE_SECURE_REFUSE = 1033;
	
	// ********* 快速投递确认 **************

	/** 确认HELO指令 **/
	public final static short CASTHELO_OKAY = 1020;

	/** 投递成功 **/
	public final static short CAST_OKAY = 1021;

	/** 结束投递成功 **/
	public final static short CASTEXIT_OKAY = 1022;
	
	/** 在收到CAST_OKAY后，返回CAST_OKAY_REPLY，形成安全三段 **/
	public final static short CAST_OKAY_REPLY = 1023;
	
	/** ReplyReceiver接收完成进入锁定状态，向ReplySender投递锁定成功，等待返回CAST_OKAY_REPLY **/
	public final static short LOCK_CAST_OKAY = 1024;

	// ******** 错误的应答码 *********
	
	/** 拒绝 **/
	public final static short REFUSE = 2000;

	/** 因为FIXP协议版本不同而不能支持 **/
	public final static short UNSUPPORT = 2001;

	/** 没有注册 **/
	public final static short NOTLOGIN = 2002;

	/** 客户端错误 **/
	public final static short CLIENT_ERROR = 2003;

	/** 服务器错误 **/
	public final static short SERVER_ERROR = 2004;

	/** 安全配置已经存在 **/
	public final static short SECURE_EXISTED = 2005;

	/** 安全参数错误，拒绝接受 **/
	public final static short SECURE_REFUSE = 2006;

	/** 执行取消时，发生没有找到 **/
	public final static short CANCEL_NOTFOUND = 2007;
	
	/** 执行取消时，确认成功 **/
	public final static short CANCEL_OKAY = 2008;
	
	/** 应答码范围 **/
	public static final short RANGE[] = new short[] { 1000, 2999 };

	/**
	 * 判断FIXP应答码在规定范围内
	 * @param reply 被检索的FIXP应答码
	 * @return 返回真或者假
	 */
	public static boolean isAnswer(short reply){
		return Answer.RANGE[0] <= reply && reply <= Answer.RANGE[1];
	}

	/**
	 * 判断应答码是一般性确认
	 * @param reply 应答码
	 * @return 返回真或者假
	 */
	public static boolean isIsee(short reply) {
		return reply == Answer.ISEE;
	}

	/**
	 * 判断是OKAY命令
	 * @param e FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isOkay(Mark e) {
		return e.isAnswer() && e.getAnswer() == Answer.OKAY;
	}

	/**
	 * 判断是ACCEPT命令
	 * @param e FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isAccept(Mark e) {
		return e.isAnswer() && e.getAnswer() == Answer.ACCEPTED;
	}

	/**
	 * 判断应答码是没有注册
	 * @param reply 应答码
	 * @return 返回真或者假
	 */
	public static boolean isNotLogin(short reply) {
		return reply == Answer.NOTLOGIN;
	}
	
	/**
	 * 判断是CAST HELO OKAY命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isCastHeloOkay(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.CASTHELO_OKAY;
	}
	
	/**
	 * 判断是CAST OKAY命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isCastOkay(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.CAST_OKAY;
	}
	
	/**
	 * 判断是LOCK CAST OKAY命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isLockCastOkay(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.LOCK_CAST_OKAY;
	}
	
	/**
	 * 判断是CAST OKAY REPLY命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isCastOkayReply(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.CAST_OKAY_REPLY;
	}
	
	/**
	 * 判断是CAST EXIT OKAY命令
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isCastExitOkay(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.CASTEXIT_OKAY;
	}
	
	/**
	 * 判断是子包已经受理反馈
	 * @param mark FIXP协议标识
	 * @return 返回真或者假
	 */
	public static boolean isSubPacketsAccepted(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.SUBPACKETS_ACCEPTED;
	}
	
	/**
	 * 判断是Reflect命令应答
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isReflectAccepted(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.REFLECT_ACCEPTED;
	}
	
	/**
	 * 判断是Shine命令应答
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isShineAccepted(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.SHINE_ACCEPTED;
	}
	
	/**
	 * 判断是Visit命令应答
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isVisitAccepted(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.VISIT_ACCEPTED;
	}
	
	/**
	 * 判断是安全通知，服务器要求加密执行加密通信
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isSecureNotify(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.SECURE_NOTIFY;
	}

	/**
	 * 判断是安全通知，服务器要求加密数据内容
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isEncryptContentNotify(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.ENCRYPT_CONTENT_NOTIFY;
	}
	
	/**
	 * 判断是Visit命令应答
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isPrivateSecureNotify(Mark mark) {
		return mark.isAnswer() && mark.getAnswer() == Answer.PRIVATE_SECURE_NOTIFY;
	}

	/**
	 * 判断是接受建立私密密钥（在保存密钥的服务器端）
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isPrivateCreateSecureAccepted(Mark mark) {
		return mark.isAnswer()
				&& mark.getAnswer() == Answer.PRIVATE_CREATE_SECURE_ACCEPTED;
	}

	/**
	 * 判断是拒绝建立私密密（在保存密钥的服务器端）
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isPrivateCreateSecureRefuse(Mark mark) {
		return mark.isAnswer()
				&& mark.getAnswer() == Answer.PRIVATE_CREATE_SECURE_REFUSE;
	}

	/**
	 * 判断是接受删除私密密钥（在删除密钥的服务器端）
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isPrivateDropSecureAccepted(Mark mark) {
		return mark.isAnswer()
				&& mark.getAnswer() == Answer.PRIVATE_DROP_SECURE_ACCEPTED;
	}

	/**
	 * 判断是拒绝删除私密密钥（在删除密钥的服务器端）
	 * @param mark FIXP协议标头
	 * @return 返回真或者假
	 */
	public static boolean isPrivateDropSecureRefuse(Mark mark) {
		return mark.isAnswer()
				&& mark.getAnswer() == Answer.PRIVATE_DROP_SECURE_REFUSE;
	}
	

}