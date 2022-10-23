/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client;

import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 回显传输参数
 * 
 * @author scott.liang
 * @version 1.0 01/17/2018
 * @since laxcus 1.0
 */
public class EchoTransfer {

	/** 调用器线程堆栈尺寸，默认是0，由系统分配 **/
	private static long stackSize = 0; 

	/** 默认的套接字连接模式是TCP **/
	private static byte transferMode = SocketTag.TCP;

	/** 最大错误重新传输次数，默认3次。 **/
	private static int maxRetry = 3;

	/** 故障重试延时时间，默认2秒。 **/
	private static int retryInterval = 2000;

	/**
	 * 设置线程堆栈尺寸，必须大于等于1M。<br>
	 * 堆栈设置是否生效，由当前操作系统决定。<br><br>
	 * 
	 * @param size 堆栈尺寸
	 * @return 返回设置后的堆栈尺寸，设置失败返回0。
	 */
	public static long setStackSize(long size) {
		if (size >= 0x100000) {
			stackSize = size;
		}
		return stackSize;
	}

	/**
	 * 返回线程堆栈尺寸，没有定义是0
	 * @return 堆栈尺寸
	 */
	public static long getStackSize() {
		return stackSize;
	}

	/**
	 * 设置套接字连接/发送模式，见SocketTag定义
	 * @param who 套接字连接模式
	 */
	public static void setTransferMode(byte who) {
		if (!SocketTag.isFamily(who)) {
			throw new IllegalValueException("illegal socket type:%d", who);
		}
		EchoTransfer.transferMode = who;
	}

	/**
	 * 返回套接字连接/发送模式
	 * @return 套接字传输模式，见SocketTag中定义
	 */
	public static byte getTransferMode() {
		return EchoTransfer.transferMode;
	}

	/**
	 * 判断传输模式是TCP
	 * @return 返回真或者假
	 */
	public static boolean isStreamTransfer() {
		return SocketTag.isStream(EchoTransfer.transferMode);
	}

	/**
	 * 判断传输模式是UDP
	 * @return 返回真或者假
	 */
	public static boolean isPacketTransfer() {
		return SocketTag.isPacket(EchoTransfer.transferMode);
	}

	/**
	 * 设置最大错误重传次数
	 * @param how 重试次数
	 * @return 返回实际的错误重传次数
	 */
	public static int setMaxRetry(int how) {
		if (how >= 1) {
			EchoTransfer.maxRetry = how;
		}
		return EchoTransfer.maxRetry;
	}

	/**
	 * 返回最大重传次数
	 * @return 重传次数
	 */
	public static int getMaxRetry() {
		return EchoTransfer.maxRetry;
	}

	/**
	 * 设置重试间隔时间
	 * @param timeout 间隔时间
	 * @return 返回重试间隔时间
	 */
	public static int setRetryInterval(int timeout) {
		if (timeout >= 1000) {
			EchoTransfer.retryInterval = timeout;
		}
		return EchoTransfer.retryInterval;
	}

	/**
	 * 输出重试间隔时间
	 * @return 重试间隔时间
	 */
	public static int getRetryInterval() {
		return EchoTransfer.retryInterval;
	}

	/** 命令超时时间，默认是0，不限制。
	 * 当达到这个时间后，将检查超时的命令，向请求端发送错误，然后把它们删除 **/
	private volatile static long commandTimeout = 0L;

	/**
	 * 设置命令超时时间
	 * @param millisecond 毫秒
	 */
	public static void setCommandTimeout(long millisecond) {
		EchoTransfer.commandTimeout = millisecond;
	}

	/**
	 * 返回命令超时时间，单位：毫秒
	 * @return 返回毫秒为单位的超时时间
	 */
	public static long getCommandTimeout() {
		return EchoTransfer.commandTimeout;
	}

	/** 调用器超时时间，默认是0，不限制。
	 * 当达到这个时间后，将检查超时的调用器，向请求端发送错误，然后把它们删除 **/
	private volatile static long invokerTimeout = 0L;

	/**
	 * 设置异步调用器超时时间
	 * @param millisecond 毫秒
	 */
	public static void setInvokerTimeout(long millisecond) {
		EchoTransfer.invokerTimeout = millisecond;
	}

	/**
	 * 返回异步调用器超时，单位：毫秒
	 * @return 返回毫秒为单位的超时时间
	 */
	public static long getInvokerTimeout() {
		return EchoTransfer.invokerTimeout;
	}

	/** 命令管理池可存储的最多命令数目。默认是-1，无限制 **/
	private static int maxCommands = -1;

	/**
	 * 设置命令管理池可存储的最多命令数目。
	 * @param size 命令数目
	 */
	public static void setMaxCommands(int size) {
		EchoTransfer.maxCommands = size;
	}

	/**
	 * 返回命令管理池可存储的最多命令数目
	 * @return 返回命令数目
	 */
	public static int getMaxCommands() {
		return EchoTransfer.maxCommands;
	}

	/** 运行状态的异步调用器数目。默认是100 **/
	private static volatile int maxInvokers = 100;
	
	/** 调用器最大限制时间，默认1分钟 **/
	private static volatile long maxConfineTime = 60000;

	/**
	 * 设置异步调用器数目（线程数目）。<br>
	 * 这个参数在系统的"local.xml"文件中定义，在站点启动时设置。
	 * @param num 调用器数目
	 * @return 返回生效后的调用器数目
	 */
	public static void setMaxInvokers(int num) {
		if (num > 0) {
			EchoTransfer.maxInvokers = num;
		}
	}

	/**
	 * 返回异步调用器数目（线程数目）
	 * @return 调用器数目
	 */
	public static int getMaxInvokers() {
		return EchoTransfer.maxInvokers;
	}

	/**
	 * 设置调用器最大限制时间，小于等于0是表示无限期锁定。<br>
	 *
	 * @param ms 限制时间，毫秒计
	 * @return 返回生效后的限制时间
	 */
	public static void setMaxConfineTime(long ms) {
		EchoTransfer.maxConfineTime = (ms < 1 ? 0 : ms);
	}

	/**
	 * 返回调用器最大限制时间
	 * @return 限制时间
	 */
	public static long getMaxConfineTime() {
		return EchoTransfer.maxConfineTime;
	}

	/** LaunchTrustor/EndingTrustor、EndingTrustor/next EndingTrustor交叠间隔时间，以毫秒为单位，默认1000毫秒。**/
	private static long crossInterval = 1000L;

	/**
	 * 设置launch/ending, ending/next ending交叠间隔时间 <br>
	 * 当launch/ending方法未退出，下个阶段的ending启动请求已经到达，即发生交叠现象。
	 * 此时给下个阶段的ending方法设置一个延时，待当前launch/ending方法完成后再启动。<br>
	 * 
	 * @param millisecond 毫秒
	 */
	public static void setCrossInterval(long millisecond) {
		EchoTransfer.crossInterval = millisecond;
	}

	/**
	 * 返回LAUNCH/ENDING, ENDING/NEXT ENDING交叠间隔时间 
	 * @return 毫秒
	 */
	public static long getCrossInterval() {
		return EchoTransfer.crossInterval;
	}

	/** CPU最大使用率。默认是60% **/
	private static volatile double maxCpuRate = 60.0f;

	/**
	 * 设置CPU最大使用比率，双浮点数
	 * @param rate CPU占比
	 */
	public static void setMaxCpuRate(double rate) {
		if (rate > 0.1f) EchoTransfer.maxCpuRate = rate;
	}

	/**
	 * 返回CPU最大使用比率
	 * @return 双浮点的CPU占比
	 */
	public static double getMaxCpuRate() {
		return EchoTransfer.maxCpuRate;
	}

	/** JVM内存最大使用率。默认是60% **/
	private static volatile double maxVMMemoryRate = 60.0f;

	/**
	 * 设置JVM内存最大使用比率，双浮点数
	 * @param rate 内存占比
	 */
	public static void setMaxVMMemoryRate(double rate) {
		if (rate > 0.0f) {
			EchoTransfer.maxVMMemoryRate = rate;
		}
	}

	/**
	 * 返回JVM内存最大使用比率
	 * @return 双浮点内存占比
	 */
	public static double getMaxVMMemoryRate() {
		return EchoTransfer.maxVMMemoryRate;
	}

	/**
	 * 判断JVM内存不足
	 * @param usedMemory JVM使用的内存
	 * @param maxMemory JVM最大内存
	 * @return 返回真或者假
	 */
	public static boolean isVMMemoryMissing(long usedMemory, long maxMemory) {
		double rate = ((double) usedMemory / (double) maxMemory) * 100.0f; // 实例使用比率
		// 判断超过限制
		return (EchoTransfer.maxVMMemoryRate > 0.0f && rate >= EchoTransfer.maxVMMemoryRate);
	}
	
	/** 命令信道TCP缓存 **/
	private static SocketBuffer commandStreamBuffer;

	/** 命令信道UDP缓存 **/
	private static SocketBuffer commandPacketBuffer;

	/**
	 * 设置命令信道TCP缓存
	 * @param len TCP缓存
	 */
	public static void setCommandStreamBuffer(int receive, int send) {
		EchoTransfer.commandStreamBuffer = new SocketBuffer(receive, send);
	}

	/**
	 * 返回命令信道TCP缓存
	 * @return TCP缓存
	 */
	public static SocketBuffer getCommandStreamBuffer() {
		return EchoTransfer.commandStreamBuffer;
	}
	
	/**
	 * 设置命令信道UDP缓存
	 * @param len UDP缓存
	 */
	public static void setCommandPacketBuffer(int receive, int send) {
		EchoTransfer.commandPacketBuffer = new SocketBuffer(receive, send);
	}

	/**
	 * 返回命令信道UDP缓存
	 * @return UDP缓存
	 */
	public static SocketBuffer getCommandPacketBuffer() {
		return EchoTransfer.commandPacketBuffer;
	}
	
	/** 数据接收信道SOCKET缓存 **/
	private volatile static SocketBuffer replySuckerBuffer;
	
	/** 数据分派信道SOCKET缓存 **/
	private volatile static SocketBuffer replyDispatcherBuffer;
	
	/**
	 * 设置数据接收信道SOCKET缓存
	 * @param len SOCKET缓存尺寸
	 */
	public static void setReplySuckerBuffer(int receive, int send) {
		EchoTransfer.replySuckerBuffer = new SocketBuffer(receive, send);
	}

	/**
	 * 返回数据接收信道SOCKET缓存
	 * @return SOCKET缓存尺寸
	 */
	public static SocketBuffer getReplySuckerBuffer() {
		return EchoTransfer.replySuckerBuffer;
	}
	
	/**
	 * 设置数据分派信道SOCKET缓存
	 * @param len SOCKET缓存尺寸
	 */
	public static void setReplyDispatcherBuffer(int receive, int send) {
		EchoTransfer.replyDispatcherBuffer = new SocketBuffer(receive, send);
	}

	/**
	 * 返回数据分派信道SOCKET缓存
	 * @return SOCKET缓存尺寸
	 */
	public static SocketBuffer getReplyDispatcherBuffer() {
		return EchoTransfer.replyDispatcherBuffer;
	}
}