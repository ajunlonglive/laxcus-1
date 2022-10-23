/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 日志客户端发送接口。<br><br>
 * 
 * Logger记录客户LAXCUS系统或者中间件组件发送的日志信息。这些信息在随后将通过网络，被发送到集群的日志服务器上。
 * Logger的日志分为五种状态，由低到高依次是：“DEBUG、INFO、WARNING、ERROR、FAILED”。
 * 用户可以在每个节点的配置参数文档里设置日志的发送级别。如果日志处于高阶级，而发送的日志属于低于它的阶级，日志将不能发送。
 * 例如，如果用户的日志被设置成“INFO”状态，用户发送一条“DEBUG”状态的日志，这条日志会被设置是无效的，在检查时自动过滤，不会发送到日志服务器上。
 * 
 * @author scott.liang
 * @version 1.1 03/03/2012
 * @since laxcus 1.0
 */
public final class Logger {

	/** 日志客户端，发送数据到日志服务器 **/
	private static LogClient client = new LogClient();

	/**
	 * 进行安全许可检查
	 * @param method 被调用的命令方法名
	 */
	private static void check(String method) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			LoggerPermission e = new LoggerPermission(name);
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
			LoggerPermission e = new LoggerPermission(name, signer.toString());
			sm.checkPermission(e);
		}
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
	 * 构造默认和私有日志客户端
	 */
	private Logger() {
		super();
	}

	/**
	 * 设置控制台打印
	 * @param b
	 */
	public static void setConsolePrint(boolean b) {
		client.getLogConfigure().setConsolePrint(b);
	}

	/**
	 * 判断是控制台打印
	 * @return
	 */
	public static boolean isConsolePrint() {
		return client.getLogConfigure().isConsolePrint();
	}

	/**
	 * 设置日志等级，依次是:debug, info, warning, error, fatal。<br>
	 * 低于指定级别的日志不发送。<br>
	 * @param who
	 */
	public static void setLevel(int who) {
		client.getLogConfigure().setLevel(who);
	}

	/**
	 * 返回日志等级
	 * @return
	 */
	public static int getLevel() {
		return client.getLogConfigure().getLevel();
	}

	/**
	 * 判断发送到日志站点
	 * @return 返回真或者假
	 */
	public static boolean isSendToServer() {
		return client.getLogConfigure().isSendToServer();
	}

	/**
	 * 判断保存到磁盘文件
	 * @return 返回真或者假
	 */
	public static boolean isSendToDisk() {
		return client.getLogConfigure().isSendToDisk();
	}

	/**
	 * 返回日志存取目录，只有当日志写入本地时，这个目录才有效。
	 * 
	 * @return File实例
	 */
	public static File getDirectory() {
		return client.getLogConfigure().getDirectory();
	}

	/**
	 * 设置日志传输模式
	 * @param who
	 */
	public static void setTransferMode(int who) {
		client.getLogConfigure().setTransferMode(who);
	}

	/**
	 * 返回日志传输模式
	 * @return
	 */
	public static int getTransferMode() {
		return client.getLogConfigure().getTransferMode();
	}

	/**
	 * 返回日志配置静态句柄
	 * @return
	 */
	public static LogConfigure getLogConfigure() {
		return client.getLogConfigure();
	}

	/**
	 * 设置日志打印接口(打印接口由用户定义，可以是终端、图形界面、或其它界面)
	 * @param e
	 */
	public static void setLogPrinter(LogPrinter e) {
		Logger.client.setLogPrinter(e);
	}

	/**
	 * 返回日志打印接口
	 * @return
	 */
	public static LogPrinter getLogPrinter() {
		return Logger.client.getLogPrinter();
	}

	/**
	 * 检测是否处于启动运行状态
	 * @return
	 */
	public static boolean isRunning() {
		return Logger.client.isRunning();
	}

	/**
	 * 加载本地日志配置资源
	 * @param filename 文件名
	 * @return 成功返回真，否则假
	 */
	public static boolean loadXML(String filename) {
		return client.getLogConfigure().loadXML(filename);
	}

	/**
	 * 加载日志配置
	 * @param data 日志配置数据
	 * @return 成功返回真，否则假
	 */
	public static boolean loadXML(byte[] data) {
		return client.getLogConfigure().loadXML(data);
	}

	/**
	 * 启动日志服务
	 * @param endpoint 日志服务器地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public static boolean loadService(SiteHost endpoint) {
		if (Logger.client.isRunning()) {
			Logger.warning("Logger.loadService, log client is running!");
			return false;
		}

		String level = LogLevel.getText(client.getLogConfigure().getLevel());
		Logger.info("Logger.loadService, set log level '%s'", level);
		return Logger.client.load(client.getLogConfigure(), endpoint);
	}

	/**
	 * 启动本地日志服务（日志在本地处理，不发送到日志服务器）
	 * @return 成功返回“真”，否则“假”。
	 */
	public static boolean loadService() {
		return Logger.loadService(null);
	}

	/**
	 * 停止日志服务
	 */
	public static void stopService() {
		Logger.client.stopService();
		while (Logger.client.isRunning()) {
			Logger.client.delay(500);
		}
	}

	/**
	 * 根据类和方法名，生成一条日志的前缀
	 * @param sourceObject
	 * @param sourceMethod
	 * @return 字符串
	 */
	private static String doPrefix(Object sourceObject, String sourceMethod) {
		try {
			return String.format("%s.%s, ", sourceObject.getClass().getSimpleName(), sourceMethod);
		} catch (Throwable e) {
			return "Build Prefix Error! - " + Logger.throwText(e); 
		}
	}

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
			return "Build Suffix Error! - "  + Logger.throwText(e); 
		}
	}

	/**
	 * 生成一条日志记录
	 * @param sourceObject
	 * @param sourceMethod
	 * @param format
	 * @param args
	 * @return
	 */
	public static String doLog(Object sourceObject, String sourceMethod, String format, Object ... args) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		String suffix = Logger.doSuffix(format, args);
		return (prefix + suffix);
	}

	/**
	 * 记录一条“DEBUG”状态记录，并且随后发送到日志服务器上。
	 * @param log 日志
	 */
	public static void debug(String log) {
		Logger.check("debug");
		// 当前日志级别必须小于或者等于DEBUG级别
		if (Logger.getLevel() <= LogLevel.DEBUG) {
			Logger.client.push(LogLevel.DEBUG, log);
		}
	}

	/**
	 * 记录一条带有格式化参数的“DEBUG”状态记录，并且随后发送到日志服务器上。 
	 * @param format 格式
	 * @param args 参数
	 */
	public static void debug(String format, Object ... args) {
		Logger.debug(Logger.doSuffix(format, args));
	}

	/**
	 * 记录一条“DEBUG”状态记录，指定它的源对象和源方法，在随后发送到日志服务器上。
	 * @param sourceObject 源对象
	 * @param sourceMethod 源方法
	 * @param log 日志
	 */
	public static void debug(Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.debug(prefix + log);
	}

	/**
	 * 格式化和发送DEBUG日志记录
	 * @param sourceObject 源对象
	 * @param sourceMethod 源方法
	 * @param format 规则化字符
	 * @param args 字符参数
	 */
	public static void debug(Object sourceObject, String sourceMethod , String format, Object ... args) {
		Logger.debug( Logger.doLog(sourceObject, sourceMethod, format, args));
	}

	/**
	 * 发送DEBUG日志
	 * @param sourceObject 源接口实例句柄
	 * @param sourceMethod 源方法
	 * @param success 成功/失败
	 * @param format 格式化字符串
	 * @param args 参数
	 */
	public static void debug(Object sourceObject, String sourceMethod, boolean success, String format, Object... args) {
		String suffix = Logger.doSuffix(format, args);
		suffix += (success ? " yes!" : " no!");
		Logger.debug(Logger.doLog(sourceObject, sourceMethod, suffix));
	}

	/**
	 * 记录一条“DEBUG”状态记录，并且随后发送到日志服务器上。
	 * 这个方法被容器中的任务调用，包括分布任务组件、码位计算器、快捷组件。
	 * @param siger 签名者
	 * @param log 日志
	 */
	public static void debug(Siger siger, String log) {
		Logger.check("debug", siger);
		// 当前日志级别必须小于或者等于DEBUG级别
		if (Logger.getLevel() <= LogLevel.DEBUG) {
			Logger.client.push(siger, LogLevel.DEBUG, log);
		}
	}

	/**
	 * 记录一条带有格式化参数的“DEBUG”状态记录，并且随后发送到日志服务器上。 
	 * @param siger 签名者
	 * @param format 格式
	 * @param args 参数
	 */
	public static void debug(Siger siger, String format, Object... args) {
		String log = Logger.doSuffix(format, args);
		Logger.debug(siger, log);
	}

	/**
	 * 记录一条“DEBUG”状态记录，指定它的源对象和源方法，在随后发送到日志服务器上。
	 * @param siger 签名者
	 * @param sourceObject 源对象
	 * @param sourceMethod 源方法
	 * @param log 日志
	 */
	public static void debug(Siger siger, Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.debug(siger, prefix + log);
	}

	/**
	 * 格式化和发送DEBUG日志记录
	 * @param siger 签名者
	 * @param sourceObject 源对象
	 * @param sourceMethod 源方法
	 * @param format 规则化字符
	 * @param args 字符参数
	 */
	public static void debug(Siger siger, Object sourceObject, String sourceMethod, String format, Object... args) {
		String log = Logger.doLog(sourceObject, sourceMethod, format, args);
		Logger.debug(siger, log);
	}

	/**
	 * 发送DEBUG日志
	 * @param siger 签名者
	 * @param sourceObject 源接口实例句柄
	 * @param sourceMethod 源方法
	 * @param success 成功/失败
	 * @param format 格式化字符串
	 * @param args 参数
	 */
	public static void debug(Siger siger, Object sourceObject, String sourceMethod, boolean success, String format, Object... args) {
		String suffix = Logger.doSuffix(format, args);
		suffix += (success ? " yes!" : " no!");
		String log = Logger.doLog(sourceObject, sourceMethod, suffix);
		Logger.debug(siger, log);
	}

	/**
	 * 发送"INFO"状态日志 
	 * @param log 日志
	 */
	public static void info(String log) {
		Logger.check("info");
		if (Logger.getLevel() <= LogLevel.INFO) {
			Logger.client.push(LogLevel.INFO, log);
		}
	}

	/**
	 * 发送"INFO"状态日志
	 * @param format
	 * @param args
	 */
	public static void info(String format, Object ... args) {
		Logger.info(Logger.doSuffix(format, args));
	}

	/**
	 * 生成"INFO"状态日志
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 */
	public static void info(Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.info(prefix + log);
	}

	/**
	 * 生成"INFO"状态日志
	 * @param sourceObject
	 * @param sourceMethod
	 * @param format
	 * @param args
	 */
	public static void info(Object sourceObject, String sourceMethod, String format, Object... args) {
		Logger.info(Logger.doLog(sourceObject, sourceMethod, format, args));
	}

	/**
	 * 发送"INFO"状态日志 
	 * @param siger 用户签名
	 * @param log 日志
	 */
	public static void info(Siger siger, String log) {
		Logger.check("info", siger);
		if (Logger.getLevel() <= LogLevel.INFO) {
			Logger.client.push(siger, LogLevel.INFO, log);
		}
	}

	/**
	 * 发送"INFO"状态日志
	 * @param siger 用户签名
	 * @param format
	 * @param args
	 */
	public static void info(Siger siger, String format, Object... args) {
		Logger.info(siger, Logger.doSuffix(format, args));
	}

	/**
	 * 生成"INFO"状态日志
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 */
	public static void info(Siger siger, Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.info(siger, prefix + log);
	}

	/**
	 * 生成"INFO"状态日志
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param format
	 * @param args
	 */
	public static void info(Siger siger, Object sourceObject, String sourceMethod, String format, Object... args) {
		String log = Logger.doLog(sourceObject, sourceMethod, format, args);
		Logger.info(siger, log);
	}

	/**
	 * 发送"警告"状态日志
	 * @param log 警告日志
	 */
	public static void warning(String log) {
		Logger.check("warning");
		if (Logger.getLevel() <= LogLevel.WARNING) {
			Logger.client.push(LogLevel.WARNING, log);
		}
	}

	/**
	 * 发送"警告"状态日志
	 * @param format
	 * @param args
	 */
	public static void warning(String format, Object ... args) {
		Logger.warning(Logger.doSuffix(format, args));
	}

	/**
	 * 生成"警告"日志和保存它
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 */
	public static void warning(Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.warning(prefix + log);
	}

	/**
	 * 发送"警告"日志
	 * @param log 前缀日志
	 * @param e 故障
	 */
	public static void warning(String log, Throwable e) {
		StringBuilder buff = new StringBuilder(1024 * 3);
		// buff.append(String.format("%s - ", log));
		buff.append(log + " - ");
		buff.append(Logger.throwText(e));
		Logger.warning(buff.toString());
	}

	/**
	 * 发送"警告"日志
	 * @param handle
	 * @param format
	 * @param args
	 */
	public static void warning(Throwable handle, String format, Object ... args) {
		String suffix = Logger.doSuffix(format, args);
		Logger.warning(suffix, handle);
	}

	/**
	 * 生成"警告"日志和保存它
	 * @param sourceObject
	 * @param sourceMethod
	 * @param format
	 * @param args
	 */
	public static void warning(Object sourceObject, String sourceMethod, String format, Object... args) {
		Logger.warning(Logger.doLog(sourceObject, sourceMethod, format, args));
	}

	/**
	 * 发送"警告"状态日志
	 * @param siger 用户签名
	 * @param log 警告日志
	 */
	public static void warning(Siger siger, String log) {
		Logger.check("warning", siger);
		if (Logger.getLevel() <= LogLevel.WARNING) {
			Logger.client.push(siger, LogLevel.WARNING, log);
		}
	}

	/**
	 * 发送"警告"状态日志
	 * @param siger 用户签名
	 * @param format
	 * @param args
	 */
	public static void warning(Siger siger, String format, Object... args) {
		Logger.warning(siger, Logger.doSuffix(format, args));
	}

	/**
	 * 生成"警告"日志和保存它
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 */
	public static void warning(Siger siger, Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.warning(siger, prefix + log);
	}

	/**
	 * 生成"警告"日志和保存它
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param format
	 * @param args
	 */
	public static void warning(Siger siger, Object sourceObject, String sourceMethod, String format, Object... args) {
		String log = Logger.doLog(sourceObject, sourceMethod, format, args);
		Logger.warning(siger, log);
	}

	/**
	 * 发送"错误"状态日志
	 * @param log 错误日志
	 */
	public static void error(String log) {
		Logger.check("error");
		if (Logger.getLevel() <= LogLevel.ERROR) {
			Logger.client.push(LogLevel.ERROR, log);
		}
	}

	/**
	 * 发送"错误"日志
	 * @param log 前缀日志
	 * @param e 故障
	 */
	public static void error(String log, Throwable e) {
		StringBuilder buff = new StringBuilder(1024 * 3);
		buff.append(log + " - ");
		buff.append(Logger.throwText(e));
		Logger.error(buff.toString());
	}

	/**
	 * 发送"错误"异常日志
	 * @param e
	 */
	public static void error(Throwable e) {
		Logger.error(Logger.throwText(e));
	}

	/**
	 * 发送"错误"日志
	 * @param format
	 * @param args
	 */
	public static void error(String format, Object ... args) {
		Logger.error(Logger.doSuffix(format, args));
	}

	/**
	 * 生成"错误"日志
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 */
	public static void error(Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.error(prefix + log);
	}

	/**
	 * 生成"错误"日志
	 * @param sourceObject
	 * @param sourceMethod
	 * @param format
	 * @param args
	 */
	public static void error(Object sourceObject, String sourceMethod, String format, Object... args) {
		Logger.error(Logger.doLog(sourceObject, sourceMethod, format, args));
	}

	/**
	 * 发送"错误"日志
	 * @param handle
	 * @param format
	 * @param args
	 */
	public static void error(Throwable handle, String format, Object ... args) {
		String suffix = Logger.doSuffix(format, args);
		Logger.error(suffix, handle);
	}

	/**
	 * 生成"错误"日志和保存它
	 * @param sourceObject
	 * @param sourceMethod
	 * @param handle
	 * @param format
	 * @param args
	 */
	public static void error(Object sourceObject, String sourceMethod, Throwable handle, String format, Object ... args) {
		Logger.error(Logger.doLog(sourceObject, sourceMethod, format, args));
	}

	/**
	 * 发送"错误"状态日志
	 * @param siger 用户签名
	 * @param log 错误日志
	 */
	public static void error(Siger siger, String log) {
		Logger.check("error", siger);
		if (Logger.getLevel() <= LogLevel.ERROR) {
			Logger.client.push(siger, LogLevel.ERROR, log);
		}
	}

	/**
	 * 发送"错误"日志
	 * @param siger 用户签名
	 * @param log
	 * @param e
	 */
	public static void error(Siger siger, String log, Throwable e) {
		StringBuilder buff = new StringBuilder(1024 * 3);
		// buff.append(String.format("%s - ", log));
		buff.append(log + " - ");
		buff.append(Logger.throwText(e));
		Logger.error(siger, buff.toString());
	}

	/**
	 * 发送"错误"异常日志
	 * @param siger 用户签名
	 * @param t
	 */
	public static void error(Siger siger, Throwable t) {
		Logger.error(siger, Logger.throwText(t));
	}

	/**
	 * 发送"错误"日志
	 * @param siger 用户签名
	 * @param format
	 * @param args
	 */
	public static void error(Siger siger, String format, Object... args) {
		Logger.error(siger, Logger.doSuffix(format, args));
	}

	/**
	 * 生成"错误"日志
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 */
	public static void error(Siger siger, Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.error( siger, prefix + log);
	}

	/**
	 * 生成"错误"日志
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param format
	 * @param args
	 */
	public static void error(Siger siger, Object sourceObject, String sourceMethod, String format, Object... args) {
		Logger.error( siger, Logger.doLog(sourceObject, sourceMethod, format, args));
	}

	/**
	 * 发送"错误"日志
	 * @param siger 用户签名
	 * @param handle
	 * @param format
	 * @param args
	 */
	public static void error(Siger siger, Throwable handle, String format, Object ... args) {
		String s = Logger.doSuffix(format, args);
		Logger.error( siger, s, handle);
	}

	/**
	 * 生成"错误"日志和保存它
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param handle
	 * @param format
	 * @param args
	 */
	public static void error(Siger siger, Object sourceObject, String sourceMethod, Throwable handle, String format, Object ... args) {
		Logger.error(siger, Logger.doLog(sourceObject, sourceMethod, format, args));
	}

	/**
	 * 发送"故障失败"日志
	 * @param log 故障日志
	 */
	public static void fatal(String log) {
		Logger.check("fatal");
		if (Logger.getLevel() <= LogLevel.FATAL) {
			Logger.client.push(LogLevel.FATAL, log);
		}
	}

	/**
	 * 生成"故障"日志
	 * @param log
	 * @param e
	 */
	public static void fatal(String log, Throwable e) {
		StringBuilder buff = new StringBuilder(1024 * 3);
		// buff.append(String.format("%s - ", log));
		buff.append(log + " - ");
		buff.append(Logger.throwText(e));
		Logger.fatal(buff.toString());
	}

	/**
	 * 生成"故障"日志
	 * @param e
	 */
	public static void fatal(Throwable e) {
		Logger.fatal(Logger.throwText(e));
	}

	/**
	 * 生成"故障"日志
	 * @param format
	 * @param args
	 */
	public static void fatal(String format, Object ... args) {
		Logger.fatal(Logger.doSuffix(format, args));
	}

	/**
	 * 生成"故障"日志
	 * @param handle
	 * @param format
	 * @param args
	 */
	public static void fatal(Throwable handle, String format, Object ... args) {
		String s = Logger.doSuffix(format, args);
		Logger.fatal(s, handle);
	}

	/**
	 * 生成"故障"日志
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 */
	public static void fatal(Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.fatal(prefix + log);
	}

	/**
	 * 生成"故障"日志
	 * @param sourceObject
	 * @param sourceMethod
	 * @param format
	 * @param args
	 */
	public static void fatal(Object sourceObject, String sourceMethod, String format, Object ... args) {
		Logger.fatal(Logger.doLog(sourceObject, sourceMethod, format, args));
	}

	/**
	 * 生成"故障"日志
	 * @param sourceObject
	 * @param sourceMethod
	 * @param handle
	 * @param format
	 * @param args
	 */
	public static void fatal(Object sourceObject, String sourceMethod, Throwable handle, String format, Object ... args) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		String suffix = Logger.throwText(handle) + Logger.doSuffix(format, args);
		Logger.fatal(prefix + suffix);
	}

	/**
	 * 发送"故障失败"日志
	 * @param siger 用户签名
	 * @param log 故障日志
	 */
	public static void fatal(Siger siger, String log) {
		Logger.check("fatal", siger);
		if (Logger.getLevel() <= LogLevel.FATAL) {
			Logger.client.push(siger, LogLevel.FATAL, log);
		}
	}

	/**
	 * 生成"故障"日志
	 * @param siger 用户签名
	 * @param log
	 * @param e
	 */
	public static void fatal(Siger siger, String log, Throwable e) {
		StringBuilder buff = new StringBuilder(1024 * 3);
		// buff.append(String.format("%s - ", log));
		buff.append(log + " - ");
		buff.append(Logger.throwText(e));
		Logger.fatal(siger, buff.toString());
	}

	/**
	 * 生成"故障"日志
	 * @param siger 用户签名
	 * @param e
	 */
	public static void fatal(Siger siger, Throwable e) {
		Logger.fatal( siger, Logger.throwText(e));
	}

	/**
	 * 生成"故障"日志
	 * @param siger 用户签名
	 * @param format
	 * @param args
	 */
	public static void fatal(Siger siger, String format, Object... args) {
		Logger.fatal(siger, Logger.doSuffix(format, args));
	}

	/**
	 * 生成"故障"日志
	 * @param siger 用户签名
	 * @param handle
	 * @param format
	 * @param args
	 */
	public static void fatal(Siger siger, Throwable handle, String format, Object ... args) {
		String s = Logger.doSuffix(format, args);
		Logger.fatal( siger, s, handle);
	}

	/**
	 * 生成"故障"日志
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 */
	public static void fatal(Siger siger, Object sourceObject, String sourceMethod, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		Logger.fatal(siger, prefix + log);
	}

	/**
	 * 生成"故障"日志
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param format
	 * @param args
	 */
	public static void fatal(Siger siger, Object sourceObject, String sourceMethod, String format, Object ... args) {
		Logger.fatal(siger, Logger.doLog(sourceObject, sourceMethod, format, args));
	}

	/**
	 * 生成"故障"日志
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param handle
	 * @param format
	 * @param args
	 */
	public static void fatal(Siger siger, Object sourceObject, String sourceMethod, Throwable handle, String format, Object ... args) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod);
		String suffix = Logger.throwText(handle) + Logger.doSuffix(format, args);
		Logger.fatal(siger, prefix + suffix);
	}

	/**
	 * 根据是否成功，选择发送"信息"或者"错误"日志
	 * @param prefix
	 * @param success
	 */
	public static void note(String prefix, boolean success) {
		if (success) {
			Logger.info(prefix + " successful");
		} else {
			Logger.error(prefix + " failed");
		}
	}

	/**
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 * @param success
	 */
	public static void note(Object sourceObject, String sourceMethod, String log, boolean success) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod) + log;
		Logger.note(prefix, success);
	}

	/**
	 * @param sourceObject
	 * @param sourceMethod
	 * @param success
	 * @param log
	 */
	public static void note(Object sourceObject, String sourceMethod, boolean success, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod) + log;
		Logger.note(prefix, success);
	}

	/**
	 * 根据是否成功，选择发送"信息"或者"错误"日志
	 * 
	 * @param success
	 * @param format
	 * @param args
	 */
	public static void note(boolean success, String format, Object... args) {
		String s = Logger.doSuffix(format, args);
		Logger.note(s, success);
	}

	/**
	 * @param sourceObject
	 * @param sourceMethod
	 * @param success
	 * @param format
	 * @param args
	 */
	public static void note(Object sourceObject, String sourceMethod, boolean success, String format, Object... args) {
		String log = Logger.doLog(sourceObject, sourceMethod, format, args);
		Logger.note(log, success);
	}

	/**
	 * 根据是否成功，选择发送"信息"或者"错误"日志
	 * @param siger 用户签名
	 * @param prefix
	 * @param success
	 */
	public static void note(Siger siger, String prefix, boolean success) {
		if (success) {
			Logger.info(siger, prefix + " successful");
		} else {
			Logger.error(siger, prefix + " failed");
		}
	}

	/**
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param log
	 * @param success
	 */
	public static void note(Siger siger, Object sourceObject, String sourceMethod, String log, boolean success) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod) + log;
		Logger.note(siger, prefix, success);
	}

	/**
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param success
	 * @param log
	 */
	public static void note(Siger siger, Object sourceObject, String sourceMethod, boolean success, String log) {
		String prefix = Logger.doPrefix(sourceObject, sourceMethod) + log;
		Logger.note(siger, prefix, success);
	}

	/**
	 * 根据是否成功，选择发送"信息"或者"错误"日志
	 * 
	 * @param siger 用户签名
	 * @param success
	 * @param format
	 * @param args
	 */
	public static void note(Siger siger, boolean success, String format, Object... args) {
		String s = Logger.doSuffix(format, args);
		Logger.note( siger, s, success);
	}

	/**
	 * @param siger 用户签名
	 * @param sourceObject
	 * @param sourceMethod
	 * @param success
	 * @param format
	 * @param args
	 */
	public static void note(Siger siger, Object sourceObject, String sourceMethod, boolean success, String format, Object... args) {
		String log = Logger.doLog(sourceObject, sourceMethod, format, args);
		Logger.note( siger, log, success);
	}

	/**
	 * 输出日志信息到控制台。通常在运行出错的时候。
	 */
	public static void gushing() {
		Logger.client.gushing();
	}
}