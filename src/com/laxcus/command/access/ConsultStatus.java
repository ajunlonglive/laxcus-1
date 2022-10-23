/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

/**
 * 分布数据处理协商操作状态 <br><br>
 * 
 * 
 * 针对集群的数据处理特点，采用三段式协商形式，数据处理的目标包括：“写入、删除、更新”。<br>
 * 首先由数据处理节点（如DATA节点，但是不限于）发起，发出命令的节点（如CALL/WORK节点）收到和汇总全部数据处理节点的处理结果时，做出一个统一的处理响应（确认全部节点的操作或者要求全部节点回滚数据），逐一通知到每个数据处理节点。数据处理节点据此，做出最后的确认执行和回滚数据的操作。<br><br>
 * 
 * 三段协商机制保证分布数据处理的数据一致性和完整性。当发生数据处理不一致情况下，向请求端发出检查一次性的通知。<br>
 * 
 * @author scott.liang
 * @version 1.0 9/9/2013
 * @since laxcus 1.0
 */
public final class ConsultStatus {

	/** 第一阶段状态：成功/失败。数据处理节点发送，命令请求节点接收 **/
	public final static byte SUCCESS = 1;
	public final static byte FAILED = 2;

	/** 第二阶段状态：确认/取消。命令请求节点发送，数据处理节点接收 **/
	public final static byte CONFIRM = 3;
	public final static byte CANCEL = 4;

	/**
	 * 第三阶段：确认成功、取消成功、取消失败。<br>
	 * 如果第一阶段，数据处理节点发出“成功”，接收判断节点发出“确认”，将产生“确认成功（CONFIRM_SUCCESS）”<br>
	 * 如果不满足这个条件，将执行“取消（回滚）”操作。成功，返回“取消成功（CANCEL_SUCCESS）”，否则是“取消失败（CANCEL_FAILED）”。
	 **/
	public final static byte CONFIRM_SUCCESS = 5;
	public final static byte CANCEL_SUCCESS = 6;
	public final static byte CANCEL_FAILED = 7;

	/**
	 * 判断是由数据处理节点发出的提交状态码
	 * @param status 协商状态码
	 * @return 返回真或者假
	 */
	public static boolean isAssume(byte status) {
		switch (status) {
		// 第一阶段
		case ConsultStatus.SUCCESS:
		case ConsultStatus.FAILED:
		// 第三阶段
		case ConsultStatus.CONFIRM_SUCCESS:
		case ConsultStatus.CANCEL_SUCCESS:
		case ConsultStatus.CANCEL_FAILED:
			return true;
		default:
			return false;
		}
	}


	/**
	 * 判断是命令请求节点发出的回复状态码
	 * @param status 状态码
	 * @return 返回真或者假
	 */
	public static boolean isAssert(byte status) {
		switch (status) {
		case ConsultStatus.CONFIRM:
		case ConsultStatus.CANCEL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 判断是协商状态码
	 * @param status 协商状态码
	 * @return 返回真或者假
	 */
	public static boolean isConsult(byte status) {
		return ConsultStatus.isAssume(status) || ConsultStatus.isAssert(status);
	}

	/**
	 * 判断是数据处理节点处理成功
	 * @param status 协商状态码
	 * @return 返回真或者假
	 */
	public static boolean isSuccess(byte status) {
		return status == ConsultStatus.SUCCESS;
	}

	/**
	 * 判断是数据处理节点处理失败
	 * @param status 协商状态码
	 * @return 返回真或者假
	 */
	public static boolean isFailed(byte status) {
		return status == ConsultStatus.FAILED;
	}

	/**
	 * 判断是命令请求节点确认成功（在接收到数据处理节点成功基础上的判断）
	 * @param status 协商状态码
	 * @return 返回真或者假 
	 */
	public static boolean isConfirm(byte status) {
		return status == ConsultStatus.CONFIRM;
	}

	/**
	 * 判断是命令请求节点确认取消（当任何一个节点执行失败，即产生取消动作）
	 * @param status 协商状态码
	 * @return 返回真或者假
	 */
	public static boolean isCancel(byte status) {
		return status == ConsultStatus.CANCEL;
	}

	/**
	 * 判断是数据处理节点再次确认成功
	 * @param status 协商状态码
	 * @return 返回真或者假
	 */
	public static boolean isConfirmSuccess(byte status) {
		return status == ConsultStatus.CONFIRM_SUCCESS;
	}

	/**
	 * 判断是数据处理节点取消成功
	 * @param status 协商状态码
	 * @return 返回真或者假
	 */
	public static boolean isCancelSuccess(byte status) {
		return status == ConsultStatus.CANCEL_SUCCESS;
	}

	/**
	 * 判断是数据处理节点取消失败
	 * @param status 协商状态码
	 * @return 返回真或者假
	 */
	public static boolean isCancelFailed(byte status) {
		return status == ConsultStatus.CANCEL_FAILED;
	}

	/**
	 * 将协商状态码的数字描述转为字符串描述
	 * @param status 协商状态码
	 * @return 字符串文本
	 */
	public static String translate(byte status) {
		switch (status) {
		// 第一阶段
		case ConsultStatus.SUCCESS:
			return "SUCCESS";
		case ConsultStatus.FAILED:
			return "FAILED";
		// 第二阶段
		case ConsultStatus.CONFIRM:
			return "CONFIRM";
		case ConsultStatus.CANCEL:
			return "CANCEL";
		// 第三阶段
		case ConsultStatus.CONFIRM_SUCCESS:
			return "CONFIRM SUCCESS";
		case ConsultStatus.CANCEL_SUCCESS:
			return "CANCEL SUCCESS";
		case ConsultStatus.CANCEL_FAILED:
			return "CANCEL FAILED";
		}
		return "ILLEGAL STATUS";
	}
}