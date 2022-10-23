/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 操作客户端发送接口。<br><br>
 * 
 * Biller记录客户LAXCUS系统的操作信息。这些信息在随后将通过网络，被发送到集群的操作服务器上。<br>
 * Biller的操作有五种状态，“COMMAND、MESSAGE、WARNING、ERROR、FATAL”，无级别之分。这一点与Logger不同。<br>
 * 带"Siger"用户签名的方法是给注册用户的分布任务组件、SWIFT中间件使用。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 01/06/2020
 * @since laxcus 1.0
 */
public final class Biller {

	/** 操作客户端，发送数据到操作服务器 **/
	private static BillClient client = new BillClient();
	
	/** 默认是支持全部操作 **/
	private static volatile int defaultType = BillType.ALL;

	/**
	 * 进行安全许可检查
	 * @param method 被调用的命令方法名
	 */
	private static void check(String method) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			BillerPermission e = new BillerPermission(name);
			sm.checkPermission(e);
		}
	}

	/**
	 * 执行安全许可检查
	 * @param method 被调用的命令方法名
	 * @param signer 用户签名
	 */
	private static void check(String method, Siger signer) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			BillerPermission e = new BillerPermission(name, signer.toString());
			sm.checkPermission(e);
		}
	}
	
	/**
	 * 设置许可类型
	 * @param who
	 */
	public static void setDefaultType(int who) {
		Biller.defaultType = who;
	}

	/**
	 * 返回许可类型
	 * @return
	 */
	public static int getDefaultType() {
		return Biller.defaultType;
	}
	
	/**
	 * 新增类型与原类型合并，“加操作”。<br>
	 * 标准“或”操作，任何一位“为1”，结果都是“为1”。
	 * 
	 * @param value 新值
	 * @return 返回“或”操作的结果
	 */
	public static int add(int value) {
		int type = Biller.getDefaultType();
		type = (type | value) & BillType.ALL;
		Biller.setDefaultType(type);
		return type;
	}

	/**
	 * “非与”，“减操作”，两者值不一致，以原类型值为准。
	 * @param type 原类型值
	 * @param value 传入类型值
	 * @param bit 操作位
	 * @return
	 */
	private static int __nxor(int type, int value, int bit) {
		type &= bit;
		value &= bit;
		return (type != value ? type : 0);
	}

//	/**
//	 * 减操作，
//	 * @param value 传入类型值
//	 * @return 返回“减操作”的结果值
//	 */
//	public static int subtract(int value) {
//		int type = Biller.getDefaultType();
//
//		int command = Biller.__nxor(type, value, BillType.COMMAND);
//		int invoker = Biller.__nxor(type, value, BillType.INVOKER);
//		int message = Biller.__nxor(type, value, BillType.MESSAGE);
//		int warning = Biller.__nxor(type, value, BillType.WARNING);
//		int error = Biller.__nxor(type, value, BillType.ERROR);
//		int fatal = Biller.__nxor(type, value, BillType.FATAL);
//
//		// 与操作，合并成新的结果
//		type = (command | invoker | message | warning | error | fatal);
//
//		Biller.setDefaultType(type);
//		return type;
//	}

	/**
	 * 减操作，
	 * @param value 传入类型值
	 * @return 返回“减操作”的结果值
	 */
	public static int subtract(int value) {
		int type = Biller.getDefaultType();

		int cost = Biller.__nxor(type, value, BillType.COST);
		int error = Biller.__nxor(type, value, BillType.ERROR);
		int fatal = Biller.__nxor(type, value, BillType.FATAL);

		// 与操作，合并成新的结果
		type = (cost | error | fatal);

		Biller.setDefaultType(type);
		return type;
	}
	
	/**
	 * 判断能够执行命令操作
	 * @return 返回真或者假
	 */
	public static boolean canCost() {
		return BillType.isCost(Biller.defaultType);
	}

//	/**
//	 * 判断能够执行调用器操作
//	 * @return 返回真或者假
//	 */
//	public static boolean canInvoker() {
//		return BillType.isInvoker(Biller.defaultType);
//	}
//	
//	/**
//	 * 判断能够执行消息操作
//	 * @return 返回真或者假
//	 */
//	public static boolean canMessage() {
//		return BillType.isMessage(Biller.defaultType);
//	}
//
//	/**
//	 * 判断能够执行警告操作
//	 * @return 返回真或者假
//	 */
//	public static boolean canWarning() {
//		return BillType.isWarning(Biller.defaultType);
//	}

	/**
	 * 判断能够执行错误操作
	 * @return 返回真或者假
	 */
	public static boolean canError() {
		return BillType.isError(Biller.defaultType);
	}

	/**
	 * 判断能够执行故障操作
	 * @return 返回真或者假
	 */
	public static boolean canFatal() {
		return BillType.isFatal(Biller.defaultType);
	}
	
	/**
	 * 格式化并且输出异常信息
	 * @param e
	 * @return
	 */
	private static String throwText(Throwable e) {
		if (e == null) return "";
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		PrintStream s = new PrintStream(buff, true);
		e.printStackTrace(s);
		byte[] b = buff.toByteArray();
		return new String(b, 0, b.length);
	}

	/**
	 * 构造默认和私有操作客户端
	 */
	private Biller() {
		super();
	}

	/**
	 * 设置控制台打印
	 * @param b
	 */
	public static void setConsolePrint(boolean b) {
		client.getBillConfigure().setConsolePrint(b);
	}

	/**
	 * 判断是控制台打印
	 * @return
	 */
	public static boolean isConsolePrint() {
		return client.getBillConfigure().isConsolePrint();
	}

	/**
	 * 判断发送到操作站点
	 * @return 返回真或者假
	 */
	public static boolean isSendToServer() {
		return client.getBillConfigure().isSendToServer();
	}

	/**
	 * 判断保存到磁盘文件
	 * @return 返回真或者假
	 */
	public static boolean isSendToDisk() {
		return client.getBillConfigure().isSendToDisk();
	}

	/**
	 * 返回操作存取目录，只有当操作写入本地时，这个目录才有效。
	 * 
	 * @return File实例
	 */
	public static File getDirectory() {
		return client.getBillConfigure().getDirectory();
	}

	/**
	 * 设置操作传输模式
	 * @param who
	 */
	public static void setTransferMode(int who) {
		client.getBillConfigure().setTransferMode(who);
	}

	/**
	 * 返回操作传输模式
	 * @return
	 */
	public static int getTransferMode() {
		return client.getBillConfigure().getTransferMode();
	}

	/**
	 * 返回操作配置静态句柄
	 * @return
	 */
	public static BillConfigure getBillConfigure() {
		return client.getBillConfigure();
	}

	/**
	 * 检测是否处于启动运行状态
	 * @return
	 */
	public static boolean isRunning() {
		return Biller.client.isRunning();
	}

	/**
	 * 加载本地操作配置资源
	 * @param filename 文件名
	 * @return 成功返回真，否则假
	 */
	public static boolean loadXML(String filename) {
		return client.getBillConfigure().loadXML(filename);
	}

	/**
	 * 加载操作配置
	 * @param data 操作配置数据
	 * @return 成功返回真，否则假
	 */
	public static boolean loadXML(byte[] data) {
		return client.getBillConfigure().loadXML(data);
	}

	/**
	 * 启动操作服务
	 * @param endpoint 操作服务器地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public static boolean loadService(SiteHost endpoint) {
		if (Biller.client.isRunning()) {
			Logger.warning("Biller.loadService, log client is running!");
			return false;
		}

		//		String level = BillLevel.getText(client.getBillConfigure().getLevel());
		//		Biller.info("Biller.loadService, set log level '%s'", level);
		return Biller.client.load(client.getBillConfigure(), endpoint);
	}

	/**
	 * 启动本地操作服务（操作在本地处理，不发送到操作服务器）
	 * @return 成功返回“真”，否则“假”。
	 */
	public static boolean loadService() {
		return Biller.loadService(null);
	}

	/**
	 * 停止操作服务
	 */
	public static void stopService() {
		Biller.client.stopService();
		while (Biller.client.isRunning()) {
			Biller.client.delay(500);
		}
	}
	
	/**
	 * 推送一行日志 <br><br>
	 * 
	 * @param who 日志类型
	 * @param log 日志内容。
	 */
	private static void push(int who, String log) {
		if (BillType.isCost(who)) {
			// 如果不允许命令操作，忽略它
			if (!Biller.canCost()) {
				return;
			}
			// 检查权限许可
			Biller.check("cost");
//		} else if (BillType.isInvoker(who)) {
//			// 如果不允许调用器操作，忽略它
//			if (!Biller.canInvoker()) {
//				return;
//			}
//			Biller.check("invoker");
//		} else if (BillType.isMessage(who)) {
//			// 如果不允许消息操作，忽略它
//			if (!Biller.canMessage()) {
//				return;
//			}
//			Biller.check("message");
//		} else if (BillType.isWarning(who)) {
//			// 如果不允许警告操作，忽略它
//			if (!Biller.canWarning()) {
//				return;
//			}
//			Biller.check("warning");
		} else if (BillType.isError(who)) {
			// 如果不允许错误操作，忽略它
			if (!Biller.canError()) {
				return;
			}
			Biller.check("error");
		} else if (BillType.isFatal(who)) {
			// 如果不允许故障操作，忽略它
			if (!Biller.canFatal()) {
				return;
			}
			Biller.check("fatal");
		}

		Biller.client.push(who, log);
	}
	
	/**
	 * 推送一行日志 <br><br>
	 * 
	 * @param who 日志类型
	 * @param log 日志内容。
	 */
	private static void push(Siger siger, int who, String log) {
		if (BillType.isCost(who)) {
			// 如果不允许命令操作，忽略它
			if (!Biller.canCost()) {
				return;
			}
			Biller.check("cost", siger);
//		} else if (BillType.isInvoker(who)) {
//			// 如果不允许调用器操作，忽略它
//			if (!Biller.canInvoker()) {
//				return;
//			}
//			Biller.check("invoker", siger);
//		} else if (BillType.isMessage(who)) {
//			// 如果不允许消息操作，忽略它
//			if (!Biller.canMessage()) {
//				return;
//			}
//			Biller.check("message", siger);
//		} else if (BillType.isWarning(who)) {
//			// 如果不允许警告操作，忽略它
//			if (!Biller.canWarning()) {
//				return;
//			}
//			Biller.check("warning", siger);
		} else if (BillType.isError(who)) {
			// 如果不允许错误操作，忽略它
			if (!Biller.canError()) {
				return;
			}
			Biller.check("error", siger);
		} else if (BillType.isFatal(who)) {
			// 如果不允许故障操作，忽略它
			if (!Biller.canFatal()) {
				return;
			}
			Biller.check("fatal", siger);
		}

		Biller.client.push(who, log);
	}
	
	/**
	 * 合并命令参数
	 * @param cmd
	 * @param suffix
	 * @return 返回合并的结果
	 */
	private static String doCommandLog(Command cmd, String suffix) {
		String log = cmd.getPrimitive();
		if (log == null || log.trim().isEmpty()) {
			log = String.format("<%s>", cmd.getClass().getName());
		}

		// 用户签名
		if (cmd.getIssuer() != null) {
			log = String.format("<%s> %s", cmd.getIssuer(), log);
		} else {
			log = String.format("<SYSTEM> %s", log);
		}
		// 来源地址
		if (cmd.getSource() != null) {
			log = String.format("<%s> %s", cmd.getSource(), log);
		}

		if (suffix != null && suffix.length() > 0) {
			log = String.format("%s\r\n%s", log, suffix);
		}
		return log;
	}
	
//	/**
//	 * 产生INVOKER日志
//	 * @param invoker 异步调用器
//	 * @param suffix后缀信息
//	 * @return 日志信息
//	 */
//	private static String doInvokerLog(EchoInvoker invoker, String suffix) {
//		String log = String.format("%d", invoker.getInvokerId());
//		Siger issuer = invoker.getIssuer();
//
//		// 调用器用户签名
//		log = String.format("%s <%s> ", log,
//				(issuer != null ? issuer.toString() : "<SYSTEM>"));
//		
//		// 调用器名称
//		log = String.format("%s %s", log, invoker.getClass().getName());
//
//		// 其它信息
//		if (suffix != null && suffix.length() > 0) {
//			log = String.format("%s\r\n%s", log, suffix);
//		}
//		return log;
//	}
	
	/**
	 * 格式化后缀
	 * @param format
	 * @param args
	 * @return 字符串
	 */
	private static String doSuffix(String format, Object ... args) {
		try {
			return String.format(format, args);
		} catch (Throwable e) {
			return "Build Suffix Error! - "  + Biller.throwText(e); 
		}
	}
	
	/**
	 * 输出命令原语
	 * @param primitive 命令原语
	 */
	public static void cost(String primitive) {
		if (!Biller.canCost()) {
			return;
		}
		Biller.push(BillType.COST, primitive);
	}

	/**
	 * 输出命令原语。
	 * 带用户签名的方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
	 * 
	 * @param siger 用户签名
	 * @param primitive 命令原语
	 */
	public static void cost(Siger siger, String primitive) {
		primitive = String.format("{%s} %s", siger, primitive);
		Biller.push(siger, BillType.COST, primitive);
	}

//	/**
//	 * 输出命令
//	 * @param cmd 命令实例
//	 */
//	public static void cost(Command cmd) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		String text = Biller.doCommandLog(cmd, null);
//		// 推送日志
//		Biller.cost(text);
//	}
//
//	/**
//	 * 输出命令和辅助信息
//	 * @param cmd 命令
//	 * @param suffix 后缀辅助信息
//	 */
//	public static void cost(Command cmd, String suffix) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		
//		String text = Biller.doCommandLog(cmd, suffix);
//		Biller.cost(text);
//	}
//
//	/**
//	 * 输出命令和辅助信息
//	 * @param cmd 命令
//	 * @param format 格式化
//	 * @param args 参数
//	 */
//	public static void cost(Command cmd, String format, Object... args) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		
//		String suffix = Biller.doSuffix(format, args);
//		Biller.cost(cmd, suffix);
//	}
	

//	/**
//	 * 输出命令和参数。
//	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
//	 * 
//	 * @param siger 用户签名
//	 * @param cmd 命令
//	 * @param suffix 辅助信息
//	 */
//	public static void cost(Siger siger, Command cmd, String suffix) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		
//		String text = Biller.doCommandLog(cmd, suffix);
//		Biller.cost(siger, text);
//	}

//	/**
//	 * 输出命令和参数。
//	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
//	 * 
//	 * @param siger 用户签名
//	 * @param cmd 命令
//	 * @param format 格式化
//	 * @param args 辅助信息
//	 */
//	public static void cost(Siger siger, Command cmd, String format, Object... args) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		
//		String suffix = Biller.doSuffix(format, args);
//		Biller.cost(siger, cmd, suffix);
//	}

//	/**
//	 * 执行调用器操作
//	 * @param primitive
//	 */
//	public static void invoker(String primitive) {
//		if (!Biller.canInvoker()) {
//			return;
//		}
//		Biller.push(BillType.INVOKER, primitive);
//	}
	
//	/**
//	 * 调用器“launch”操作
//	 * @param start
//	 * @param invoker
//	 */
//	public static void invoker(boolean start, EchoInvoker invoker) {
////		// 条件不成功，魅力
////		if (!Biller.canInvoker() || !invoker.isBiller()) {
////			return;
////		}
//
//		// 日志
//		String log = String.format("%s %s", invoker.getClass().getSimpleName(),
//				(start ? "start" : "stop"));
//
//		Siger issuer = invoker.getIssuer();
//		// 调用器用户签名
//		log = String.format("%s %d %d <%s> ", log, invoker.getInvokerId(),
//				invoker.getIterateIndex(), (issuer != null ? issuer.toString() : "SYSTEM"));
//
//		// 其它信息
//		if (!start) {
//			log = String.format("%s %d", log, invoker.getProcessTime());
//
//			// 完成处理或者否
//			if (invoker.isPerfectly()) {
//				log = String.format("%s %s", log,
//						(invoker.isQuit() ? "finished" : "continue"));
//			} else {
//				log += " failed!";
//			}
//		}
//
//		// 记录INVOKER日志
//		Biller.invoker(log);
//	}

//	/**
//	 * 输出消息原语
//	 * @param primitive 消息原语
//	 */
//	public static void message(String primitive) {
//		if (!Biller.canMessage()) {
//			return;
//		}
//		Biller.push(BillType.MESSAGE, primitive);
//	}
//
//	/**
//	 * 输出消息
//	 * @param cmd 消息实例
//	 */
//	public static void message(Command cmd) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		
//		String text = Biller.doCommandLog(cmd, null);
//		// 推送日志
//		Biller.message(text);
//	}
//
//	/**
//	 * 输出消息和辅助信息
//	 * @param cmd 消息
//	 * @param suffix 后缀辅助信息
//	 */
//	public static void message(Command cmd, String suffix) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		
//		String text = Biller.doCommandLog(cmd, suffix);
//		Biller.message(text);
//	}

//	/**
//	 * 输出消息和辅助信息
//	 * @param cmd 消息
//	 * @param format 格式化
//	 * @param args 参数
//	 */
//	public static void message(Command cmd, String format, Object... args) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		
//		String suffix = Biller.doSuffix(format, args);
//		Biller.message(cmd, suffix);
//	}
//	
//	/**
//	 * 输出消息原语。
//	 * 带用户签名的方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
//	 * 
//	 * @param siger 用户签名
//	 * @param primitive 消息原语
//	 */
//	public static void message(Siger siger, String primitive) {
//		primitive = String.format("{%s} %s", siger, primitive);
//		Biller.push(siger, BillType.MESSAGE, primitive);
//	}

//	/**
//	 * 输出消息和参数。
//	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
//	 * 
//	 * @param siger 用户签名
//	 * @param cmd 消息
//	 * @param suffix 辅助信息
//	 */
//	public static void message(Siger siger, Command cmd, String suffix) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		
//		String text = Biller.doCommandLog(cmd, suffix);
//		Biller.message(siger, text);
//	}
//
//	/**
//	 * 输出消息和参数。
//	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
//	 * 
//	 * @param siger 用户签名
//	 * @param cmd 消息
//	 * @param format 格式化
//	 * @param args 辅助信息
//	 */
//	public static void message(Siger siger, Command cmd, String format, Object... args) {
////		// 不要记录，忽略它
////		if (!cmd.isBiller()) {
////			return;
////		}
//		
//		String suffix = Biller.doSuffix(format, args);
//		Biller.message(siger, cmd, suffix);
//	}
	
//	/**
//	 * 输出警告原语
//	 * @param primitive 警告原语
//	 */
//	public static void warning(String primitive) {
//		if (!Biller.canWarning()) {
//			return;
//		}
//		Biller.push(BillType.WARNING, primitive);
//	}
//
//	/**
//	 * 输出警告
//	 * @param cmd 警告实例
//	 */
//	public static void warning(Command cmd) {
//		String text = Biller.doCommandLog(cmd, null);
//		// 推送日志
//		Biller.warning(text);
//	}
//
//	/**
//	 * 输出警告和辅助信息
//	 * @param cmd 警告
//	 * @param suffix 后缀辅助信息
//	 */
//	public static void warning(Command cmd, String suffix) {
//		String text = Biller.doCommandLog(cmd, suffix);
//		Biller.warning(text);
//	}
//
//	/**
//	 * 输出警告和辅助信息
//	 * @param cmd 警告
//	 * @param format 格式化
//	 * @param args 参数
//	 */
//	public static void warning(Command cmd, String format, Object... args) {
//		String suffix = Biller.doSuffix(format, args);
//		Biller.warning(cmd, suffix);
//	}
//	
//	/**
//	 * 输出警告原语。
//	 * 带用户签名的方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
//	 * 
//	 * @param siger 用户签名
//	 * @param primitive 警告原语
//	 */
//	public static void warning(Siger siger, String primitive) {
//		primitive = String.format("{%s} %s", siger, primitive);
//		Biller.push(siger, BillType.WARNING, primitive);
//	}
//
//	/**
//	 * 输出警告和参数。
//	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
//	 * 
//	 * @param siger 用户签名
//	 * @param cmd 警告
//	 * @param suffix 辅助信息
//	 */
//	public static void warning(Siger siger, Command cmd, String suffix) {
//		String text = Biller.doCommandLog(cmd, suffix);
//		Biller.warning(siger, text);
//	}
//
//	/**
//	 * 输出警告和参数。
//	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
//	 * 
//	 * @param siger 用户签名
//	 * @param cmd 警告
//	 * @param format 格式化
//	 * @param args 辅助信息
//	 */
//	public static void warning(Siger siger, Command cmd, String format, Object... args) {
//		String suffix = Biller.doSuffix(format, args);
//		Biller.warning(siger, cmd, suffix);
//	}
	
	/**
	 * 输出错误原语
	 * @param primitive 错误原语
	 */
	public static void error(String primitive) {
		if (!Biller.canError()) {
			return;
		}
		Biller.push(BillType.ERROR, primitive);
	}

	/**
	 * 输出错误
	 * @param cmd 错误实例
	 */
	public static void error(Command cmd) {
		String text = Biller.doCommandLog(cmd, null);
		// 推送日志
		Biller.error(text);
	}

	/**
	 * 输出错误和辅助信息
	 * @param cmd 错误
	 * @param suffix 后缀辅助信息
	 */
	public static void error(Command cmd, String suffix) {
		String text = Biller.doCommandLog(cmd, suffix);
		Biller.error(text);
	}

	/**
	 * 输出错误和辅助信息
	 * @param cmd 错误
	 * @param format 格式化
	 * @param args 参数
	 */
	public static void error(Command cmd, String format, Object... args) {
		String suffix = Biller.doSuffix(format, args);
		Biller.error(cmd, suffix);
	}
	
	/**
	 * 输出错误原语。
	 * 带用户签名的方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
	 * 
	 * @param siger 用户签名
	 * @param primitive 错误原语
	 */
	public static void error(Siger siger, String primitive) {
		primitive = String.format("{%s} %s", siger, primitive);
		Biller.push(siger, BillType.ERROR, primitive);
	}

	/**
	 * 输出错误和参数。
	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
	 * 
	 * @param siger 用户签名
	 * @param cmd 错误
	 * @param suffix 辅助信息
	 */
	public static void error(Siger siger, Command cmd, String suffix) {
		String text = Biller.doCommandLog(cmd, suffix);
		Biller.error(siger, text);
	}

	/**
	 * 输出错误和参数。
	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
	 * 
	 * @param siger 用户签名
	 * @param cmd 错误
	 * @param format 格式化
	 * @param args 辅助信息
	 */
	public static void error(Siger siger, Command cmd, String format, Object... args) {
		String suffix = Biller.doSuffix(format, args);
		Biller.error(siger, cmd, suffix);
	}
	
	/**
	 * 输出故障原语
	 * @param primitive 故障原语
	 */
	public static void fatal(String primitive) {
		if (!Biller.canFatal()) {
			return;
		}
		Biller.push(BillType.FATAL, primitive);
	}

	/**
	 * 输出故障
	 * @param cmd 故障实例
	 */
	public static void fatal(Command cmd) {
		String text = Biller.doCommandLog(cmd, null);
		// 推送日志
		Biller.fatal(text);
	}

	/**
	 * 输出故障和辅助信息
	 * @param cmd 故障
	 * @param suffix 后缀辅助信息
	 */
	public static void fatal(Command cmd, String suffix) {
		String text = Biller.doCommandLog(cmd, suffix);
		Biller.fatal(text);
	}

	/**
	 * 输出故障和辅助信息
	 * @param cmd 故障
	 * @param format 格式化
	 * @param args 参数
	 */
	public static void fatal(Command cmd, String format, Object... args) {
		String suffix = Biller.doSuffix(format, args);
		Biller.fatal(cmd, suffix);
	}
	
	/**
	 * 输出故障原语。
	 * 带用户签名的方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
	 * 
	 * @param siger 用户签名
	 * @param primitive 故障原语
	 */
	public static void fatal(Siger siger, String primitive) {
		primitive = String.format("{%s} %s", siger, primitive);
		Biller.push(siger, BillType.FATAL, primitive);
	}

	/**
	 * 输出故障和参数。
	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
	 * 
	 * @param siger 用户签名
	 * @param cmd 故障
	 * @param suffix 辅助信息
	 */
	public static void fatal(Siger siger, Command cmd, String suffix) {
		String text = Biller.doCommandLog(cmd, suffix);
		Biller.fatal(siger, text);
	}

	/**
	 * 输出故障和参数。
	 * 这个方法给注册用户在分布任务组件和SWIFT中间件等应用中使用的。
	 * 
	 * @param siger 用户签名
	 * @param cmd 故障
	 * @param format 格式化
	 * @param args 辅助信息
	 */
	public static void fatal(Siger siger, Command cmd, String format, Object... args) {
		String suffix = Biller.doSuffix(format, args);
		Biller.fatal(siger, cmd, suffix);
	}

//	/**
//	 * 记录一条“COMMAND”状态记录，并且随后发送到操作服务器上。
//	 * 这个方法被容器中的任务调用，包括分布任务组件、码位计算器、快捷组件。
//	 * @param siger 签名者
//	 * @param cmd 命令
//	 * @param suffix 附属信息
//	 */
//	public static void command(Siger siger, Command cmd, String suffix) {
//		String text = combine(cmd, suffix);
//		text = String.format("{%s} %s", siger, text);
//		push(siger, BillLevel.COMMAND, text);
//	}
//
//	/**
//	 * 
//	 * @param siger
//	 * @param cmd
//	 * @param format
//	 * @param args
//	 */
//	public static void command(Siger siger, Command cmd, String format, Object... args) {
//		String suffix = Biller.doSuffix(format, args);
//		Biller.command(siger, cmd, suffix);
//	}
//
//	/**
//	 * 
//	 * @param siger
//	 * @param cmd
//	 */
//	public static void command(Siger siger, Command cmd) {
//		Biller.command(siger, cmd, null);
//	}
	
//	/**
//	 * 记录消息
//	 * @param log 日志
//	 */
//	public static void message(String log) {
//		push(BillLevel.MESSAGE, log);
//	}
//
//	/**
//	 * 记录消息命令
//	 * @param cmd 命令
//	 * @param suffix 附属参数
//	 */
//	public static void message(Command cmd, String suffix) {
//		String log = combine(cmd, suffix);
//		message(log);
//	}
//	
//	/**
//	 * 记录警告
//	 * @param log 日志
//	 */
//	public static void warning(String log) {
//		push(BillLevel.WARNING, log);
//	}
//
//	/**
//	 * 记录错误
//	 * @param log 日志
//	 */
//	public static void error(String log) {
//		push(BillLevel.ERROR, log);
//	}
//
//	/**
//	 * 故障信息
//	 * @param log 日志
//	 */
//	public static void fatal(String log) {
//		push(BillLevel.FATAL, log);
//	}
//	
//	/**
//	 * 记录警告命令
//	 * @param cmd 命令
//	 * @param suffix 附属参数
//	 */
//	public static void warning(Command cmd, String suffix) {
//		String log = combine(cmd, suffix);
//		warning(log);
//	}
//	
//	/**
//	 * 记录错误命令
//	 * @param cmd 命令
//	 * @param suffix 附属参数
//	 */
//	public static void error(Command cmd, String suffix) {
//		String log = combine(cmd, suffix);
//		error(log);
//	}
//	
//	/**
//	 * 记录错误命令
//	 * @param cmd 命令
//	 * @param suffix 附属参数
//	 */
//	public static void fatal(Command cmd, String suffix) {
//		String log = combine(cmd, suffix);
//		fatal(log);
//	}
	
	
//	/**
//	 * 根据类和方法名，生成一条操作的前缀
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @return 字符串
//	 */
//	private static String doPrefix(Object sourceObject, String sourceMethod) {
//		try {
//			return String.format("%s.%s, ", sourceObject.getClass().getSimpleName(), sourceMethod);
//		} catch (Throwable e) {
//			return "Build Prefix Error! - " + Biller.throwText(e); 
//		}
//	}



//	/**
//	 * 生成一条操作记录
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param format
//	 * @param args
//	 * @return
//	 */
//	public static String doBill(Object sourceObject, String sourceMethod, String format, Object ... args) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		String suffix = Biller.doSuffix(format, args);
//		return (prefix + suffix);
//	}

//	/**
//	 * 记录一条“DEBUG”状态记录，并且随后发送到操作服务器上。
//	 * @param log 操作
//	 */
//	public static void debug(String log) {
//		Biller.check("debug");
//		// 当前操作级别必须小于或者等于DEBUG级别
//		if (Biller.getLevel() <= BillLevel.DEBUG) {
//			Biller.client.push(BillLevel.DEBUG, log);
//		}
//	}

//	/**
//	 * 记录一条带有格式化参数的“DEBUG”状态记录，并且随后发送到操作服务器上。 
//	 * @param format 格式
//	 * @param args 参数
//	 */
//	public static void debug(String format, Object ... args) {
//		Biller.debug(Biller.doSuffix(format, args));
//	}

//	/**
//	 * 记录一条“DEBUG”状态记录，指定它的源对象和源方法，在随后发送到操作服务器上。
//	 * @param sourceObject 源对象
//	 * @param sourceMethod 源方法
//	 * @param log 操作
//	 */
//	public static void debug(Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.debug(prefix + log);
//	}

//	/**
//	 * 格式化和发送DEBUG操作记录
//	 * @param sourceObject 源对象
//	 * @param sourceMethod 源方法
//	 * @param format 规则化字符
//	 * @param args 字符参数
//	 */
//	public static void debug(Object sourceObject, String sourceMethod , String format, Object ... args) {
//		Biller.debug( Biller.doBill(sourceObject, sourceMethod, format, args));
//	}
//
//	/**
//	 * 发送DEBUG操作
//	 * @param sourceObject 源接口实例句柄
//	 * @param sourceMethod 源方法
//	 * @param success 成功/失败
//	 * @param format 格式化字符串
//	 * @param args 参数
//	 */
//	public static void debug(Object sourceObject, String sourceMethod, boolean success, String format, Object... args) {
//		String suffix = Biller.doSuffix(format, args);
//		suffix += (success ? " yes!" : " no!");
//		Biller.debug(Biller.doBill(sourceObject, sourceMethod, suffix));
//	}

//	/**
//	 * 记录一条“DEBUG”状态记录，并且随后发送到操作服务器上。
//	 * 这个方法被容器中的任务调用，包括分布任务组件、码位计算器、快捷组件。
//	 * @param siger 签名者
//	 * @param log 操作
//	 */
//	public static void debug(Siger siger, String log) {
//		Biller.check("debug", siger);
//		// 当前操作级别必须小于或者等于DEBUG级别
//		if (Biller.getLevel() <= BillLevel.DEBUG) {
//			Biller.client.push(BillLevel.DEBUG, siger, log);
//		}
//	}
//
//	/**
//	 * 记录一条带有格式化参数的“DEBUG”状态记录，并且随后发送到操作服务器上。 
//	 * @param siger 签名者
//	 * @param format 格式
//	 * @param args 参数
//	 */
//	public static void debug(Siger siger, String format, Object... args) {
//		String log = Biller.doSuffix(format, args);
//		Biller.debug(siger, log);
//	}
//
//	/**
//	 * 记录一条“DEBUG”状态记录，指定它的源对象和源方法，在随后发送到操作服务器上。
//	 * @param siger 签名者
//	 * @param sourceObject 源对象
//	 * @param sourceMethod 源方法
//	 * @param log 操作
//	 */
//	public static void debug(Siger siger, Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.debug(siger, prefix + log);
//	}
//
//	/**
//	 * 格式化和发送DEBUG操作记录
//	 * @param siger 签名者
//	 * @param sourceObject 源对象
//	 * @param sourceMethod 源方法
//	 * @param format 规则化字符
//	 * @param args 字符参数
//	 */
//	public static void debug(Siger siger, Object sourceObject, String sourceMethod, String format, Object... args) {
//		String log = Biller.doBill(sourceObject, sourceMethod, format, args);
//		Biller.debug(siger, log);
//	}
//
//	/**
//	 * 发送DEBUG操作
//	 * @param siger 签名者
//	 * @param sourceObject 源接口实例句柄
//	 * @param sourceMethod 源方法
//	 * @param success 成功/失败
//	 * @param format 格式化字符串
//	 * @param args 参数
//	 */
//	public static void debug(Siger siger, Object sourceObject, String sourceMethod, boolean success, String format, Object... args) {
//		String suffix = Biller.doSuffix(format, args);
//		suffix += (success ? " yes!" : " no!");
//		String log = Biller.doBill(sourceObject, sourceMethod, suffix);
//		Biller.debug(siger, log);
//	}
//
//	/**
//	 * 发送"INFO"状态操作 
//	 * @param log 操作
//	 */
//	public static void info(String log) {
//		Biller.check("info");
//		if (Biller.getLevel() <= BillLevel.INFO) {
//			Biller.client.push(BillLevel.INFO, log);
//		}
//	}
//
//	/**
//	 * 发送"INFO"状态操作
//	 * @param format
//	 * @param args
//	 */
//	public static void info(String format, Object ... args) {
//		Biller.info(Biller.doSuffix(format, args));
//	}
//
//	/**
//	 * 生成"INFO"状态操作
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 */
//	public static void info(Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.info(prefix + log);
//	}
//
//	/**
//	 * 生成"INFO"状态操作
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param format
//	 * @param args
//	 */
//	public static void info(Object sourceObject, String sourceMethod, String format, Object... args) {
//		Biller.info(Biller.doBill(sourceObject, sourceMethod, format, args));
//	}
//
//	/**
//	 * 发送"INFO"状态操作 
//	 * @param siger 用户签名
//	 * @param log 操作
//	 */
//	public static void info(Siger siger, String log) {
//		Biller.check("info", siger);
//		if (Biller.getLevel() <= BillLevel.INFO) {
//			Biller.client.push(BillLevel.INFO, siger, log);
//		}
//	}
//
//	/**
//	 * 发送"INFO"状态操作
//	 * @param siger 用户签名
//	 * @param format
//	 * @param args
//	 */
//	public static void info(Siger siger, String format, Object... args) {
//		Biller.info(siger, Biller.doSuffix(format, args));
//	}
//
//	/**
//	 * 生成"INFO"状态操作
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 */
//	public static void info(Siger siger, Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.info(siger, prefix + log);
//	}
//
//	/**
//	 * 生成"INFO"状态操作
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param format
//	 * @param args
//	 */
//	public static void info(Siger siger, Object sourceObject, String sourceMethod, String format, Object... args) {
//		String log = Biller.doBill(sourceObject, sourceMethod, format, args);
//		Biller.info(siger, log);
//	}
//
//	/**
//	 * 发送"警告"状态操作
//	 * @param log 警告操作
//	 */
//	public static void warning(String log) {
//		Biller.check("warning");
//		if (Biller.getLevel() <= BillLevel.WARNING) {
//			Biller.client.push(BillLevel.WARNING, log);
//		}
//	}
//
//	/**
//	 * 发送"警告"状态操作
//	 * @param format
//	 * @param args
//	 */
//	public static void warning(String format, Object ... args) {
//		Biller.warning(Biller.doSuffix(format, args));
//	}
//
//	/**
//	 * 生成"警告"操作和保存它
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 */
//	public static void warning(Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.warning(prefix + log);
//	}
//
//	/**
//	 * 发送"警告"操作
//	 * @param log 前缀操作
//	 * @param e 故障
//	 */
//	public static void warning(String log, Throwable e) {
//		StringBuilder buff = new StringBuilder(1024 * 3);
//		// buff.append(String.format("%s - ", log));
//		buff.append(log + " - ");
//		buff.append(Biller.throwText(e));
//		Biller.warning(buff.toString());
//	}
//
//	/**
//	 * 发送"警告"操作
//	 * @param handle
//	 * @param format
//	 * @param args
//	 */
//	public static void warning(Throwable handle, String format, Object ... args) {
//		String suffix = Biller.doSuffix(format, args);
//		Biller.warning(suffix, handle);
//	}
//
//	/**
//	 * 生成"警告"操作和保存它
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param format
//	 * @param args
//	 */
//	public static void warning(Object sourceObject, String sourceMethod, String format, Object... args) {
//		Biller.warning(Biller.doBill(sourceObject, sourceMethod, format, args));
//	}
//
//	/**
//	 * 发送"警告"状态操作
//	 * @param siger 用户签名
//	 * @param log 警告操作
//	 */
//	public static void warning(Siger siger, String log) {
//		Biller.check("warning", siger);
//		if (Biller.getLevel() <= BillLevel.WARNING) {
//			Biller.client.push(BillLevel.WARNING, siger, log);
//		}
//	}
//
//	/**
//	 * 发送"警告"状态操作
//	 * @param siger 用户签名
//	 * @param format
//	 * @param args
//	 */
//	public static void warning(Siger siger, String format, Object... args) {
//		Biller.warning(siger, Biller.doSuffix(format, args));
//	}
//
//	/**
//	 * 生成"警告"操作和保存它
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 */
//	public static void warning(Siger siger, Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.warning(siger, prefix + log);
//	}
//
//	/**
//	 * 生成"警告"操作和保存它
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param format
//	 * @param args
//	 */
//	public static void warning(Siger siger, Object sourceObject, String sourceMethod, String format, Object... args) {
//		String log = Biller.doBill(sourceObject, sourceMethod, format, args);
//		Biller.warning(siger, log);
//	}
//
//	/**
//	 * 发送"错误"状态操作
//	 * @param log 错误操作
//	 */
//	public static void error(String log) {
//		Biller.check("error");
//		if (Biller.getLevel() <= BillLevel.ERROR) {
//			Biller.client.push(BillLevel.ERROR, log);
//		}
//	}
//
//	/**
//	 * 发送"错误"操作
//	 * @param log 前缀操作
//	 * @param e 故障
//	 */
//	public static void error(String log, Throwable e) {
//		StringBuilder buff = new StringBuilder(1024 * 3);
//		// buff.append(String.format("%s - ", log));
//		buff.append(log + " - ");
//		buff.append(Biller.throwText(e));
//		Biller.error(buff.toString());
//	}
//
//	/**
//	 * 发送"错误"异常操作
//	 * @param e
//	 */
//	public static void error(Throwable e) {
//		Biller.error(Biller.throwText(e));
//	}
//
//	/**
//	 * 发送"错误"操作
//	 * @param format
//	 * @param args
//	 */
//	public static void error(String format, Object ... args) {
//		Biller.error(Biller.doSuffix(format, args));
//	}
//
//	/**
//	 * 生成"错误"操作
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 */
//	public static void error(Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.error(prefix + log);
//	}
//
//	/**
//	 * 生成"错误"操作
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param format
//	 * @param args
//	 */
//	public static void error(Object sourceObject, String sourceMethod, String format, Object... args) {
//		Biller.error(Biller.doBill(sourceObject, sourceMethod, format, args));
//	}
//
//	/**
//	 * 发送"错误"操作
//	 * @param handle
//	 * @param format
//	 * @param args
//	 */
//	public static void error(Throwable handle, String format, Object ... args) {
//		String suffix = Biller.doSuffix(format, args);
//		Biller.error(suffix, handle);
//	}
//
//	/**
//	 * 生成"错误"操作和保存它
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param handle
//	 * @param format
//	 * @param args
//	 */
//	public static void error(Object sourceObject, String sourceMethod, Throwable handle, String format, Object ... args) {
//		Biller.error(Biller.doBill(sourceObject, sourceMethod, format, args));
//	}
//
//	/**
//	 * 发送"错误"状态操作
//	 * @param siger 用户签名
//	 * @param log 错误操作
//	 */
//	public static void error(Siger siger, String log) {
//		Biller.check("error", siger);
//		if (Biller.getLevel() <= BillLevel.ERROR) {
//			Biller.client.push(BillLevel.ERROR, siger, log);
//		}
//	}
//
//	/**
//	 * 发送"错误"操作
//	 * @param siger 用户签名
//	 * @param log
//	 * @param e
//	 */
//	public static void error(Siger siger, String log, Throwable e) {
//		StringBuilder buff = new StringBuilder(1024 * 3);
//		// buff.append(String.format("%s - ", log));
//		buff.append(log + " - ");
//		buff.append(Biller.throwText(e));
//		Biller.error(siger, buff.toString());
//	}
//
//	/**
//	 * 发送"错误"异常操作
//	 * @param siger 用户签名
//	 * @param t
//	 */
//	public static void error(Siger siger, Throwable t) {
//		Biller.error(siger, Biller.throwText(t));
//	}
//
//	/**
//	 * 发送"错误"操作
//	 * @param siger 用户签名
//	 * @param format
//	 * @param args
//	 */
//	public static void error(Siger siger, String format, Object... args) {
//		Biller.error(siger, Biller.doSuffix(format, args));
//	}
//
//	/**
//	 * 生成"错误"操作
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 */
//	public static void error(Siger siger, Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.error( siger, prefix + log);
//	}
//
//	/**
//	 * 生成"错误"操作
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param format
//	 * @param args
//	 */
//	public static void error(Siger siger, Object sourceObject, String sourceMethod, String format, Object... args) {
//		Biller.error( siger, Biller.doBill(sourceObject, sourceMethod, format, args));
//	}
//
//	/**
//	 * 发送"错误"操作
//	 * @param siger 用户签名
//	 * @param handle
//	 * @param format
//	 * @param args
//	 */
//	public static void error(Siger siger, Throwable handle, String format, Object ... args) {
//		String s = Biller.doSuffix(format, args);
//		Biller.error( siger, s, handle);
//	}
//
//	/**
//	 * 生成"错误"操作和保存它
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param handle
//	 * @param format
//	 * @param args
//	 */
//	public static void error(Siger siger, Object sourceObject, String sourceMethod, Throwable handle, String format, Object ... args) {
//		Biller.error(siger, Biller.doBill(sourceObject, sourceMethod, format, args));
//	}
//
//	/**
//	 * 发送"故障失败"操作
//	 * @param log 故障操作
//	 */
//	public static void fatal(String log) {
//		Biller.check("fatal");
//		if (Biller.getLevel() <= BillLevel.FATAL) {
//			Biller.client.push(BillLevel.FATAL, log);
//		}
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param log
//	 * @param e
//	 */
//	public static void fatal(String log, Throwable e) {
//		StringBuilder buff = new StringBuilder(1024 * 3);
//		// buff.append(String.format("%s - ", log));
//		buff.append(log + " - ");
//		buff.append(Biller.throwText(e));
//		Biller.fatal(buff.toString());
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param e
//	 */
//	public static void fatal(Throwable e) {
//		Biller.fatal(Biller.throwText(e));
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param format
//	 * @param args
//	 */
//	public static void fatal(String format, Object ... args) {
//		Biller.fatal(Biller.doSuffix(format, args));
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param handle
//	 * @param format
//	 * @param args
//	 */
//	public static void fatal(Throwable handle, String format, Object ... args) {
//		String s = Biller.doSuffix(format, args);
//		Biller.fatal(s, handle);
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 */
//	public static void fatal(Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.fatal(prefix + log);
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param format
//	 * @param args
//	 */
//	public static void fatal(Object sourceObject, String sourceMethod, String format, Object ... args) {
//		Biller.fatal(Biller.doBill(sourceObject, sourceMethod, format, args));
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param handle
//	 * @param format
//	 * @param args
//	 */
//	public static void fatal(Object sourceObject, String sourceMethod, Throwable handle, String format, Object ... args) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		String suffix = Biller.throwText(handle) + Biller.doSuffix(format, args);
//		Biller.fatal(prefix + suffix);
//	}
//
//	/**
//	 * 发送"故障失败"操作
//	 * @param siger 用户签名
//	 * @param log 故障操作
//	 */
//	public static void fatal(Siger siger, String log) {
//		Biller.check("fatal", siger);
//		if (Biller.getLevel() <= BillLevel.FATAL) {
//			Biller.client.push(BillLevel.FATAL, siger, log);
//		}
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param siger 用户签名
//	 * @param log
//	 * @param e
//	 */
//	public static void fatal(Siger siger, String log, Throwable e) {
//		StringBuilder buff = new StringBuilder(1024 * 3);
//		// buff.append(String.format("%s - ", log));
//		buff.append(log + " - ");
//		buff.append(Biller.throwText(e));
//		Biller.fatal(siger, buff.toString());
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param siger 用户签名
//	 * @param e
//	 */
//	public static void fatal(Siger siger, Throwable e) {
//		Biller.fatal( siger, Biller.throwText(e));
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param siger 用户签名
//	 * @param format
//	 * @param args
//	 */
//	public static void fatal(Siger siger, String format, Object... args) {
//		Biller.fatal(siger, Biller.doSuffix(format, args));
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param siger 用户签名
//	 * @param handle
//	 * @param format
//	 * @param args
//	 */
//	public static void fatal(Siger siger, Throwable handle, String format, Object ... args) {
//		String s = Biller.doSuffix(format, args);
//		Biller.fatal( siger, s, handle);
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 */
//	public static void fatal(Siger siger, Object sourceObject, String sourceMethod, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		Biller.fatal(siger, prefix + log);
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param format
//	 * @param args
//	 */
//	public static void fatal(Siger siger, Object sourceObject, String sourceMethod, String format, Object ... args) {
//		Biller.fatal(siger, Biller.doBill(sourceObject, sourceMethod, format, args));
//	}
//
//	/**
//	 * 生成"故障"操作
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param handle
//	 * @param format
//	 * @param args
//	 */
//	public static void fatal(Siger siger, Object sourceObject, String sourceMethod, Throwable handle, String format, Object ... args) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod);
//		String suffix = Biller.throwText(handle) + Biller.doSuffix(format, args);
//		Biller.fatal(siger, prefix + suffix);
//	}
//
//	/**
//	 * 根据是否成功，选择发送"信息"或者"错误"操作
//	 * @param prefix
//	 * @param success
//	 */
//	public static void note(String prefix, boolean success) {
//		if (success) {
//			Biller.info(prefix + " successful");
//		} else {
//			Biller.error(prefix + " failed");
//		}
//	}
//
//	/**
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 * @param success
//	 */
//	public static void note(Object sourceObject, String sourceMethod, String log, boolean success) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod) + log;
//		Biller.note(prefix, success);
//	}
//
//	/**
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param success
//	 * @param log
//	 */
//	public static void note(Object sourceObject, String sourceMethod, boolean success, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod) + log;
//		Biller.note(prefix, success);
//	}
//
//	/**
//	 * 根据是否成功，选择发送"信息"或者"错误"操作
//	 * 
//	 * @param success
//	 * @param format
//	 * @param args
//	 */
//	public static void note(boolean success, String format, Object... args) {
//		String s = Biller.doSuffix(format, args);
//		Biller.note(s, success);
//	}
//
//	/**
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param success
//	 * @param format
//	 * @param args
//	 */
//	public static void note(Object sourceObject, String sourceMethod, boolean success, String format, Object... args) {
//		String log = Biller.doBill(sourceObject, sourceMethod, format, args);
//		Biller.note(log, success);
//	}
//
//	/**
//	 * 根据是否成功，选择发送"信息"或者"错误"操作
//	 * @param siger 用户签名
//	 * @param prefix
//	 * @param success
//	 */
//	public static void note(Siger siger, String prefix, boolean success) {
//		if (success) {
//			Biller.info(siger, prefix + " successful");
//		} else {
//			Biller.error(siger, prefix + " failed");
//		}
//	}
//
//	/**
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param log
//	 * @param success
//	 */
//	public static void note(Siger siger, Object sourceObject, String sourceMethod, String log, boolean success) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod) + log;
//		Biller.note(siger, prefix, success);
//	}
//
//	/**
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param success
//	 * @param log
//	 */
//	public static void note(Siger siger, Object sourceObject, String sourceMethod, boolean success, String log) {
//		String prefix = Biller.doPrefix(sourceObject, sourceMethod) + log;
//		Biller.note(siger, prefix, success);
//	}
//
//	/**
//	 * 根据是否成功，选择发送"信息"或者"错误"操作
//	 * 
//	 * @param siger 用户签名
//	 * @param success
//	 * @param format
//	 * @param args
//	 */
//	public static void note(Siger siger, boolean success, String format, Object... args) {
//		String s = Biller.doSuffix(format, args);
//		Biller.note( siger, s, success);
//	}
//
//	/**
//	 * @param siger 用户签名
//	 * @param sourceObject
//	 * @param sourceMethod
//	 * @param success
//	 * @param format
//	 * @param args
//	 */
//	public static void note(Siger siger, Object sourceObject, String sourceMethod, boolean success, String format, Object... args) {
//		String log = Biller.doBill(sourceObject, sourceMethod, format, args);
//		Biller.note( siger, log, success);
//	}

	/**
	 * 输出操作信息到控制台。通常在运行出错的时候。
	 */
	public static void gushing() {
		Biller.client.gushing();
	}
}