/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub;

/**
 * FIXP协议请求码。<br><br>
 * 请求码由主码和从码组成，主码和从码都是1个字节，表示一个具体的请求操作。<br>
 * 
 * @author scott.liang
 * @version 1.1 10/13/2009
 * @since laxcus 1.0
 */
public final class MindAsk { 

	/** 主码  */

	/** 通知 */
	public final static byte NOTIFY = 1;

	/** 远程过程调用 */
	public final static byte RPC = 2;

	/** 远程操作方法 **/
	public final static byte METHOD = 3;

	/**
	 * 检查请求码是否正确
	 * @param major 主码
	 * @param minor 从码
	 * @return 返回真或者假
	 */
	public static boolean isRequest(byte major, byte minor) {
		boolean success = false;
		switch (major) {
		case MindAsk.NOTIFY:
			success = MindAsk.isNotify(minor);
			break;
		case MindAsk.RPC:
			success = MindAsk.isRPC(minor);
			break;
		case MindAsk.METHOD:
			success = MindAsk.isMethod(minor);
			break;
		}
		return success;
	}

	/** 通知（NOTIFY）从码 */

	// 状态间歇测试交互
	public final static byte HELO = 1; 		// 握手操作，测试节点是否存在，起ping的作用
	public final static byte EXIT = 2; 		// 退出连接
	public final static byte TEST = 3;		// 向FIXP服务器发送一个测试包
	public final static byte VISIT = 4;		// 借用FIXP UDP信道的RPC操作。

	//	public final static byte COMEBACK = 3; 	// 管理节点通知注册子节点,注册已经超时,立即发送激活包
	//	public final static byte SHUTDOWN = 4; 	// 要求节点退出运行
	//	public final static byte REFLECT = 6;	// 集群内节点之间的地址检测，返回被检测节点地址。FixpPacketClient -> FixpPacketMonitor , FixpPacketMonitor -> FixpPacketMontor
	//	public final static byte SHINE = 7;		// 内网节点请求网关检测NAT出口地址，返回被检测节点地址。FixpPacketMontior(FRONT) ->  FixpPacketMonitor(GATE)


	//	// 子包
	//	public final static byte RETRY_SUBPACKET = 11;
	//	public final static byte CANCEL_PACKET = 12;

	//	// 安全
	//	public final static byte SECURE_QUERY = 21;	// 安全查询
	//	public final static byte SECURE_CREATE = 22;	// 建立密文通信，密文由客户机发出，被服务器保存，它们建立安全服务
	//	public final static byte SECURE_REPLACE = 23; // 更新密文
	//	public final static byte SECURE_DROP = 24; 	// 撤销密文，也是撤销安全服务
	//	public final static byte PRIVATE_SECURE_QUERY = 25; // 私密安全查询
	//	public final static byte PRIVATE_SECURE_DROP = 27; 	// 私密撤销密文，也是撤销安全服务
	//	public final static byte PRIVATE_SECURE_CREATE = 28; 	// 私密建立密文，也是撤销安全服务

	//	// 快速投递
	//	public final static byte CASTHELO = 31;	// 快速投递的握手
	//	public final static byte CAST = 32; 	// 快速投递
	//	public final static byte CASTEXIT = 33;	// 快速投递结束。
	//	public final static byte CASTREFIRE = 34; // 重新投递全部包

	//	// 其它
	//	public final static byte ADD_LOG = 51; 	// 其它节点通知日志节点，增加一段日志。

	/**
	 * 判断通知从码是否正确
	 * @param minor 从码
	 * @return 返回真或者假
	 */
	public static boolean isNotify(byte minor) {
		switch (minor) {
		// 状态间歇测试交互
		case MindAsk.HELO:
		case MindAsk.EXIT:
		case MindAsk.TEST:
		case MindAsk.VISIT:
			return true;
			//		case MindAsk.COMEBACK:
			//		case MindAsk.SHUTDOWN:
			//		case MindAsk.REFLECT:
			//		case MindAsk.SHINE:
			// 子包
			//		case MindAsk.RETRY_SUBPACKET:
			//		case MindAsk.CANCEL_PACKET:
			// 安全
			//		case MindAsk.SECURE_QUERY:
			//		case MindAsk.SECURE_CREATE:
			//		case MindAsk.SECURE_REPLACE:
			//		case MindAsk.SECURE_DROP:
			//		case MindAsk.PRIVATE_SECURE_QUERY:
			//		case MindAsk.PRIVATE_SECURE_DROP:
			//		case MindAsk.PRIVATE_SECURE_CREATE:
			// 快速投递
			//		case MindAsk.CASTHELO:
			//		case MindAsk.CAST:
			//		case MindAsk.CASTEXIT:
			//		case MindAsk.CASTREFIRE:
			// 其它
			//		case MindAsk.ADD_LOG:
			//			return true;
		default:
			return false;
		}
	}

	/** RPC从码 */
	public final static byte EXECUTE = 1;

	/**
	 * 判断是RPC从码
	 * @param minor 从码
	 * @return 返回真或者假
	 */
	public static boolean isRPC(byte minor) {
		return minor == MindAsk.EXECUTE;
	}

	/** METHOD 从码 **/
	public final static byte RUN_TUB_SERVICE = 1;
	public final static byte STOP_TUB_SERVICE = 2;
	public final static byte PRINT_TUB_SERVICE = 3;
	public final static byte SHOW_TUB_SERVICE = 4;
	public final static byte CHECK_TUB_LISTEN = 5;

	/**
	 * 远程执行方法
	 * @param minor 从码
	 * @return 返回真或者假
	 */
	public static boolean isMethod(byte minor) {
		switch (minor) {
		case MindAsk.RUN_TUB_SERVICE:
		case MindAsk.STOP_TUB_SERVICE:
		case MindAsk.PRINT_TUB_SERVICE:
		case MindAsk.SHOW_TUB_SERVICE:
		case MindAsk.CHECK_TUB_LISTEN:
			return true;
		default:
			return false;
		}
	}

}