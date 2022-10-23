/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dispatcher;

import com.laxcus.access.parse.*;
import com.laxcus.command.*;
import com.laxcus.command.access.permit.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.shutdown.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.scan.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.entrance.*;
import com.laxcus.command.site.front.*;
import com.laxcus.command.site.gate.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.command.task.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.listener.*;
import com.laxcus.ray.*;
import com.laxcus.ray.invoker.*;
import com.laxcus.ray.pool.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.sound.*;
import com.laxcus.util.tip.*;

/**
 * FRONT交互命令分派器 <br><br>
 * 
 * 用于图形终端或者字符控制台的两种界面，接受来自键盘输入命令。<br>
 * 命令发送前，需要得到用户的确认，然后分派给异步调用器去执行命令处理。<br>
 * 
 * @author scott.liang
 * @version 1.0 04/09/2013
 * @since laxcus 1.0
 */
public class RayCommandDispatcher implements CommandDispatcher {

	/** 交互站点启动器 **/
	private static RayLauncher launcher;

	/**
	 * 设置交互站点启动器
	 * @param e RayLauncher实例
	 */
	public static void setRayLauncher(RayLauncher e) {
		RayCommandDispatcher.launcher = e;
	}

	/**
	 * 返回交互站点启动器
	 * @return RayLauncher实例
	 */
	public static RayLauncher getRayLauncher() {
		return RayCommandDispatcher.launcher;
	}

	/** 语法检查器 **/
	private SyntaxChecker checker = new SyntaxChecker();

	/** 语法检查器 **/
	private SyntaxMatcher matcher = new SyntaxMatcher();

	/**
	 * 构造命令转发器
	 */
	public RayCommandDispatcher() {
		super();
	}

	/**
	 * 输出语法字节流
	 * @return 返回XML的字节流
	 */
	public byte[] getSyntaxStream() {
		String xmlPath = "conf/ray/syntax/tokens.xml";
		ResourceLoader loader = new ResourceLoader();
		return loader.findStream(xmlPath);
	}

//	/**
//	 * 发出警告声音
//	 */
//	private void playWarning() {
//		RayCommandDispatcher.launcher.playSound(SoundTag.WARNING);
//	}

//	/**
//	 * 显示警告信息
//	 * @param text 文本
//	 */
//	private void warning(String text, MeetDisplay display) {
//		display.warning(text);
//		playWarning();
//	}

	/**
	 * 发出错误声音
	 */
	private void playError() {
		RayCommandDispatcher.launcher.playSound(SoundTag.ERROR);
	}

	/**
	 * 弹出错误信息
	 * @param text 文本
	 */
	private void fault(String text, MeetDisplay display) {
		display.fault(text);
		// 警告声音
		playError();
	}

	/**
	 * 返回异步调用器
	 * @return
	 */
	private RayInvokerPool getInvokerPool() {
		return launcher.getInvokerPool();
	}

	/**
	 * 检查连网状态
	 * @return
	 */
	private boolean checkOnline(MeetDisplay display) {
		// 检查，如果没有登录时，在窗口显示网络已经断开
		boolean online = launcher.isLogined(); // launcher.getStaffPool().isOnline();
		if (!online) {
			String text = launcher.fault(FaultTip.OFFLINE_REFUSE);
			fault(text, display);
			return false;
		}
		return true;
	}

	/**
	 * 判断是桌面
	 * @return
	 */
	public boolean isWatch() {
		return launcher.isWatch();
	}

	/**
	 * 在发送命令前，提示用户核准本次操作
	 * @return 确定返回真，否则假
	 */
	private boolean confirm(CommandAuditor auditor) {
		return auditor.confirm();
	}

	/**
	 * 在发送命令前，提示用户核准本次操作
	 * @param title 提示信息
	 * @return 确定返回真，否则假
	 */
	protected boolean confirm(String title, CommandAuditor auditor) {
		return auditor.confirm(title);
	}

	/**
	 * 在发送命令前，提示用户核准本次操作
	 * @param title 提示信息
	 * @param content 内容
	 * @return 确定返回真，否则假
	 */
	private boolean confirm(String title, String content, CommandAuditor auditor) {
		return auditor.confirm(title, content);
	}
	
	/**
	 * 检查用户输入的语法
	 * @param input 输入语句
	 * @param throwin 弹出异常
	 * @param display 交互显示接口
	 * @return 成功返回真，否则假
	 */
	@Override
	public boolean match(String input, boolean throwin, MeetDisplay display) {
		boolean success = false;
		try {
			success = match(input);
			if (!success && display != null && throwin) {
				String text = launcher.fault(FaultTip.ILLEGAL_COMMAND);
				fault(text, display);
			}
		} catch (SyntaxException e) {
			Logger.error(input, e);
			if (display != null && throwin) {
				fault(e.getMessage(), display);
			}
		} catch (Throwable e) {
			Logger.fatal(input, e);
			if (display != null && throwin) {
				fault(e.getMessage(), display);
			}
		}
		return success;
	}
	
	/**
	 * 检查命令
	 * @param input 输入语句
	 */
	private boolean match(String input) {
		// 判断是BUILD HASH命令
		boolean success = matcher.isBuildHash(input);
		// 命令超时时间
		if (!success) success = matcher.isCommandTimeout(input);
		// 处理模式
		if (!success) success = matcher.isCommandMode(input);	
		// 命令优先级
		if (!success) success = matcher.isCommandRank(input);	

		// FIXP密文超时时间
		if (!success) success = matcher.isCipherTimeout(input);
		// 设置异步缓存尺寸
		if (!success) success = matcher.isMaxEchoBuffer(input);
		if (!success) success = matcher.isMaxInvoker(input);
		if (!success) success = matcher.isSetReflectPort(input);
		
		// 判断是远程关闭命令
		if (!success) success = matcher.isShutdown(input);
		// 缓存换数据块命令
		if (!success) success = matcher.isRush(input);
		if (!success) success = matcher.isCompact(input);
		if (!success) success = matcher.isSetDSMReduce(input);

		// 释放节点内存
		if (!success) success = matcher.isReleaseMemory(input);
		if (!success) success = matcher.isReleaseMemoryInterval(input);
		
		// 应答数据
		if (!success) success = matcher.isReplyPacketMode(input);
		if (!success) success = matcher.isReplyPacketSize(input);
		if (!success) success = matcher.isReplySendTimeout(input);
		if (!success) success = matcher.isReplyReceiveTimeout(input);
		if (!success) success = matcher.isReplyFlowControl(input);
		// 判断是密钥令牌命令
		if (!success) success = matcher.isCreateSecureToken(input);
		if (!success) success = matcher.isDropSecureToken(input);
		if (!success) success = matcher.isFlushSecureToken(input);
		if (!success) success = matcher.isShowSecureToken(input);
		if (!success) success = matcher.isSetSecureSize(input);
		
		// 判断是重新加载本地链接库
		if (!success) success = matcher.isReloadLibrary(input);
		// 判断是重新设置节点的安全策略
		if (!success) success = matcher.isReloadSecurityPolicy(input);
		// 判断是重新加载许可证
		if (!success) success = matcher.isReloadLicence(input);
		// 判断是发布新的许可证
		if (!success) success = matcher.isPublishLicence(input);

		// 判断是扫描堆栈命令
		if (!success) success = matcher.isScanCommandStack(input);
		// 判断是刷新注册用户命令
		if (!success) success = matcher.isRefreshUser(input);
		// 判断是刷新元数据命令
		if (!success) success = matcher.isRefreshMetadata(input);
		// 判断是扫描用户关联时间间隔
		if (!success) success = matcher.isScanLinkTime(input);
		// 强制要求站点注册
		if (!success) success = matcher.isRefreshLogin(input);
		// 扫描数据库
		if (!success) success = matcher.isScanSchema(input);
		// 扫描数据表
		if (!success) success = matcher.isScanTable(input);
		// 扫描数据块
		if (!success) success = matcher.isScanEntityWithWatch(input);

		// 用户资源分布
		if (!success) success = matcher.isSeekFrontSite(input);
		if (!success) success = matcher.isSeekFrontUser(input);
		if (!success) success = matcher.isSeekUserSite(input);
		if (!success) success = matcher.isSeekUserTable(input);
		if (!success) success = matcher.isSeekUserTask(input);

		// 部署/删除系统应用
		if (!success) success = matcher.isDeployConductPackage(input);
		if (!success) success = matcher.isDeployEstablishPackage(input);
		if (!success) success = matcher.isDeployContactPackage(input);
		if (!success) success = matcher.isDropConductPackage(input);
		if (!success) success = matcher.isDropEstablishPackage(input);
		if (!success) success = matcher.isDropContactPackage(input); 

		// 检索分布任务组件或者码位计算器
		if (!success) success = matcher.isSeekTask(input);

		// 设置站点日志
		if (!success) success = matcher.isSetLogLevel(input);
		// 重新加载/发布自定义JAR包
		if (!success) success = matcher.isReloadCustom(input);
		// 半截符编码/解码
		if (!success) success = matcher.isBuildHalf(input);
		// EACH签名
		if (!success) success = matcher.isBuildEach(input);

		// 显示注册用户/授权/数据库/数据表状态
		if (!success) success = matcher.isPrintUserDiagram(input);
		if (!success) success = matcher.isPrintGrantDiagram(input);
		if (!success) success = matcher.isPrintSchemaDiagram(input);
		if (!success) success = matcher.isPrintTableDiagram(input);

		// 获取用户日志
		if (!success) success = matcher.isScanUserLog(input);

		// 命令超时时间
		if (!success) success = matcher.isOutlookInterval(input);
		// 检索在线命令
		if (!success) success = matcher.isSeekOnlineCommand(input);
		// 检索在线资源
		if (!success) success = matcher.isSeekOnlineResource(input);
		// 检索用户在线注册元数据
		if (!success) success = matcher.isSeekRegisterMetadata(input);
		// 检索用户分布区域
		if (!success) success = matcher.isSeekUserArea(input);

		// 数据流量测试
		if (!success) success = matcher.isParallelMultiGust(input);
		if (!success) success = matcher.isMultiGust(input);
		if (!success) success = matcher.isGust(input);
		if (!success) success = matcher.isParallelMultiSwarm(input);
		if (!success) success = matcher.isMultiSwarm(input);
		if (!success) success = matcher.isSwarm(input);

		// SOCKET连接检测
		if (!success) success = matcher.isRing(input);

		// 发布用户到指定站点
		if (!success) success = matcher.isDeployUser(input);
		// 从指定节点清除用户记录
		if (!success) success = matcher.isEraselUser(input);
		// 发布数据表到指定站点
		if (!success) success = matcher.isDeployTable(input);
		// 从DATA节点复制数据块到从节点
		if (!success) success = matcher.isCopyMasterMass(input);

		// 定位GATE站点模式
		if (!success) success = matcher.isShadowMode(input);
		// 检查GATE站点注册用户与站点编号一致性
		if (!success) success = matcher.isCheckShadowConsistency(input);
		// 最大CPU/最大虚拟机内存/最小内存/最低磁盘空间限制
		if (!success) success = matcher.isMostCPU(input);
		if (!success) success = matcher.isMostVMMemory(input);
		if (!success) success = matcher.isLeastMemory(input);
		if (!success) success = matcher.isLeastDisk(input);
		if (!success) success = matcher.isCheckSitePath(input);

		// 分布处理超时
		if (!success) success = matcher.isDistributedTimeout(input);

		// 打开或者关闭警告/故障消息
		if (!success) success = matcher.isEnableWarning(input);
		if (!success) success = matcher.isEnableFault(input);
		if (!success) success = matcher.isDisableWarning(input);
		if (!success) success = matcher.isDisableFault(input);

		// 设置虚拟空间参数
		if (!success) success = matcher.isSetMemberCyber(input);
		if (!success) success = matcher.isSetFrontCyber(input);
		if (!success) success = matcher.isCheckUserCyber(input);

		// 日志显示数目
		if (!success) success = matcher.isMaxLogElements(input);
		// 开放/关闭日志
		if (!success) success = matcher.isOpenTigger(input);
		if (!success) success = matcher.isCloseTigger(input);

		// 检查集群成员
		if (!success) success = matcher.isCheckDistributedMember(input);

		// 删除系统包
		if (!success) success = matcher.isDropConductPackage(input);
		if (!success) success = matcher.isDropEstablishPackage(input);
		if (!success) success = matcher.isDropContactPackage(input);

		// 检测服务器系统信息
		if (!success) success = matcher.isCheckSystemInfo(input);
		
		// 检测用户消耗的资源
		if (!success) success = matcher.isCheckUserCost(input);
		
		// 以上不成立，由自定义命令生成器判断是否属于自定义命令
		if (!success) {
			success = CustomCreator.isCommand(input);
		}

		return success;
	}

	/**
	 * 检查用户输入的语法
	 * @param input 输入语句
	 * @param throwin 弹出异常
	 * @param display 交互显示接口
	 * @return 成功返回真，否则假
	 */
	@Override
	public boolean check(String input, boolean throwin, MeetDisplay display) {
		boolean success = false;
		try {
			success = check(input);
			if (!success && display != null && throwin) {
				String text = launcher.fault(FaultTip.ILLEGAL_COMMAND);
				fault(text, display);
			}
		} catch (SyntaxException e) {
			Logger.error(input, e);
			if (display != null && throwin) {
				fault(e.getMessage(), display);
			}
		} catch (Throwable e) {
			Logger.fatal(input, e);
			if (display != null && throwin) {
				fault(e.getMessage(), display);
			}
		}
		return success;
	}
	
	/**
	 * 检查命令
	 * @param input 输入语句
	 */
	private boolean check(String input) {
		// 判断是BUILD HASH命令
		boolean success = checker.isBuildHash(input);
		// 命令超时时间
		if (!success) success = checker.isCommandTimeout(input);
		// 处理模式
		if (!success) success = checker.isCommandMode(input);	
		// 命令优先级
		if (!success) success = checker.isCommandRank(input);	

		// FIXP密文超时时间
		if (!success) success = checker.isCipherTimeout(input);
		// 设置异步缓存尺寸
		if (!success) success = checker.isMaxEchoBuffer(input);
		if (!success) success = checker.isMaxInvoker(input);
		if (!success) success = checker.isSetReflectPort(input);
		
		// 检测服务器系统信息
		if (!success) success = checker.isCheckSystemInfo(input);
		
		// 判断是远程关闭命令
		if (!success) success = checker.isShutdown(input);
		// 缓存换数据块命令
		if (!success) success = checker.isRush(input);
		if (!success) success = checker.isCompact(input);
		if (!success) success = checker.isSetDSMReduce(input);

		// 释放节点内存
		if (!success) success = checker.isReleaseMemory(input);
		if (!success) success = checker.isReleaseMemoryInterval(input);
		
		// 应答数据
		if (!success) success = checker.isReplyPacketMode(input);
		if (!success) success = checker.isReplyPacketSize(input);
		if (!success) success = checker.isReplySendTimeout(input);
		if (!success) success = checker.isReplyReceiveTimeout(input);
		if (!success) success = checker.isReplyFlowControl(input);
		// 判断是密钥令牌命令
		if (!success) success = checker.isCreateSecureToken(input);
		if (!success) success = checker.isDropSecureToken(input);
		if (!success) success = checker.isFlushSecureToken(input);
		if (!success) success = checker.isShowSecureToken(input);
		if (!success) success = checker.isSetSecureSize(input);
		
		// 判断是重新加载本地链接库
		if (!success) success = checker.isReloadLibrary(input);
		// 判断是重新设置节点的安全策略
		if (!success) success = checker.isReloadSecurityPolicy(input);
		// 判断是重新加载许可证
		if (!success) success = checker.isReloadLicence(input);
		// 判断是发布新的许可证
		if (!success) success = checker.isPublishLicence(input);

		// 判断是扫描堆栈命令
		if (!success) success = checker.isScanCommandStack(input);
		// 判断是刷新注册用户命令
		if (!success) success = checker.isRefreshUser(input);
		// 判断是刷新元数据命令
		if (!success) success = checker.isRefreshMetadata(input);
		// 判断是扫描用户关联时间间隔
		if (!success) success = checker.isScanLinkTime(input);
		// 强制要求站点注册
		if (!success) success = checker.isRefreshLogin(input);
		// 扫描数据库
		if (!success) success = checker.isScanSchema(input);
		// 扫描数据表
		if (!success) success = checker.isScanTable(input);
		// 扫描数据块
		if (!success) success = checker.isScanEntityWithWatch(input);

		// 用户资源分布
		if (!success) success = checker.isSeekFrontSite(input);
		if (!success) success = checker.isSeekFrontUser(input);
		if (!success) success = checker.isSeekUserSite(input);
		if (!success) success = checker.isSeekUserTable(input);
		if (!success) success = checker.isSeekUserTask(input);

		// 部署/删除系统应用
		if (!success) success = checker.isDeployConductPackage(input);
		if (!success) success = checker.isDeployEstablishPackage(input);
		if (!success) success = checker.isDeployContactPackage(input);
		if (!success) success = checker.isDropConductPackage(input);
		if (!success) success = checker.isDropEstablishPackage(input);
		if (!success) success = checker.isDropContactPackage(input); 

		// 检索分布任务组件或者码位计算器
		if (!success) success = checker.isSeekTask(input);

		// 设置站点日志
		if (!success) success = checker.isSetLogLevel(input);
		// 重新加载/发布自定义JAR包
		if (!success) success = checker.isReloadCustom(input);
		// 半截符编码/解码
		if (!success) success = checker.isBuildHalf(input);
		// EACH签名
		if (!success) success = checker.isBuildEach(input);

		// 显示注册用户/授权/数据库/数据表状态
		if (!success) success = checker.isPrintUserDiagram(input);
		if (!success) success = checker.isPrintGrantDiagram(input);
		if (!success) success = checker.isPrintSchemaDiagram(input);
		if (!success) success = checker.isPrintTableDiagram(input);

		// 获取用户日志
		if (!success) success = checker.isScanUserLog(input);

		// 命令超时时间
		if (!success) success = checker.isOutlookInterval(input);
		// 检索在线命令
		if (!success) success = checker.isSeekOnlineCommand(input);
		// 检索在线资源
		if (!success) success = checker.isSeekOnlineResource(input);
		// 检索用户在线注册元数据
		if (!success) success = checker.isSeekRegisterMetadata(input);
		// 检索用户分布区域
		if (!success) success = checker.isSeekUserArea(input);

		// 数据流量测试
		if (!success) success = checker.isParallelMultiGust(input);
		if (!success) success = checker.isMultiGust(input);
		if (!success) success = checker.isGust(input);
		if (!success) success = checker.isParallelMultiSwarm(input);
		if (!success) success = checker.isMultiSwarm(input);
		if (!success) success = checker.isSwarm(input);

		// SOCKET连接检测
		if (!success) success = checker.isRing(input);

		// 发布用户到指定站点
		if (!success) success = checker.isDeployUser(input);
		// 从指定节点清除用户记录
		if (!success) success = checker.isEraselUser(input);
		// 发布数据表到指定站点
		if (!success) success = checker.isDeployTable(input);
		// 从DATA节点复制数据块到从节点
		if (!success) success = checker.isCopyMasterMass(input);

		// 定位GATE站点模式
		if (!success) success = checker.isShadowMode(input);
		// 检查GATE站点注册用户与站点编号一致性
		if (!success) success = checker.isCheckShadowConsistency(input);
		// 最大CPU/最大虚拟机内存/最小内存/最低磁盘空间限制
		if (!success) success = checker.isMostCPU(input);
		if (!success) success = checker.isMostVMMemory(input);
		if (!success) success = checker.isLeastMemory(input);
		if (!success) success = checker.isLeastDisk(input);
		if (!success) success = checker.isCheckSitePath(input);

		// 分布处理超时
		if (!success) success = checker.isDistributedTimeout(input);

		// 打开或者关闭警告/故障消息
		if (!success) success = checker.isEnableWarning(input);
		if (!success) success = checker.isEnableFault(input);
		if (!success) success = checker.isDisableWarning(input);
		if (!success) success = checker.isDisableFault(input);

		// 设置虚拟空间参数
		if (!success) success = checker.isSetMemberCyber(input);
		if (!success) success = checker.isSetFrontCyber(input);
		if (!success) success = checker.isCheckUserCyber(input);

		// 日志显示数目
		if (!success) success = checker.isMaxLogElements(input);
		// 开放/关闭日志
		if (!success) success = checker.isOpenTigger(input);
		if (!success) success = checker.isCloseTigger(input);

		// 检查集群成员
		if (!success) success = checker.isCheckDistributedMember(input);

		// 删除系统包
		if (!success) success = checker.isDropConductPackage(input);
		if (!success) success = checker.isDropEstablishPackage(input);
		if (!success) success = checker.isDropContactPackage(input);

		// 检测服务器系统信息
		if (!success) success = checker.isCheckSystemInfo(input);
		
		// 检测服务器系统信息
		if (!success) success = checker.isCheckUserCost(input);
		
		
		// 以上不成立，由自定义命令生成器判断是否属于自定义命令
		if (!success) {
			success = CustomCreator.isCommand(input);
		}

		return success;
	}

//
//	/**
//	 * 测试输入语法
//	 * @param input 输入语句
//	 */
//	private boolean testing(String input) {
//		// 账号
//		boolean success = checker.isCreateUser(input);
//		if (!success) success = checker.isDropUser(input);
//		if (!success) success = checker.isAlterUser(input);
//		if (!success) success = checker.isAssertUser(input);
//		if (!success) success = checker.isPrintUserDiagram(input);
//		if (!success) success = checker.isOpenUser(input);
//		if (!success) success = checker.isCloseUser(input);
//
//		// 权限
//		if (!success) success = checker.isGrant(input);
//		if (!success) success = checker.isRevoke(input);
//		if (!success) success = checker.isPrintGrantDiagram(input);
//
//		// 数据库
//		if (!success) success = checker.isCreateSchema(input);
//		if (!success) success = checker.isDropSchema(input);
//		if (!success) success = checker.isAssertSchema(input);
//		if (!success) success = checker.isScanSchema(input);
//		if (!success) success = checker.isPrintSchemaDiagram(input);
//		if (!success) success = checker.isShowSchema(input);
//
//		// 数据表
//		if (!success) success = checker.isCreateTable(input);
//		if (!success) success = checker.isDropTable(input);
//		if (!success) success = checker.isScanTable(input);
//		if (!success) success = checker.isPrintTableDiagram(input);
//		if (!success) success = checker.isShowTable(input);
//		if (!success) success = checker.isCheckRemoteTable(input);
//		if (!success) success = checker.isCheckRemoteTask(input);
//
//		// 建立/撤销数据优化时间
//		if (!success) success = checker.isCreateRegulateTime(input);
//		if (!success) success = checker.isDropRegulateTime(input);
//		if (!success) success = checker.isPrintRegulateTime(input);
//
//		// 判断是“建立/删除/显示”限制操作单元
//		if (!success) success = checker.isCreateLimit(input);
//		if (!success) success = checker.isDropLimit(input);
//		if (!success) success = checker.isShowLimit(input);
//
//		// 判断是“提交/撤销/显示”锁定单元
//		if (!success) success = checker.isCreateFault(input);
//		if (!success) success = checker.isDropFault(input);
//		if (!success) success = checker.isShowFault(input);
//
//		// 判断是显示禁止操作单元
//		if (!success) success = checker.isShowForbid(input);
//		// 判断是显示事务规则
//		if (!success) success = checker.isShowLockRule(input);
//
//		// 检查/修复表数据一致性
//		if (!success) success = checker.isCheckEntityConsistency(input);
//		if (!success) success = checker.isRecoverEntityConsistency(input);
//		// 检查分布数据容量
//		if (!success) success = checker.isScanSketch(input);
//
//		// 数据块强制转换
//		if (!success) success = checker.isRush(input);
//
//		// 数据块
//		if (!success) success = checker.isSetEntitySize(input);
//		if (!success) success = checker.isShowEntitySize(input);
//		if (!success) success = checker.isScanEntity(input);
//		if (!success) success = checker.isGitStubs(input);
//		if (!success) success = checker.isPrintStubsDiagram(input);
//		if (!success) success = checker.isExportEntity(input);
//		if (!success) success = checker.isImportEntity(input);
//		if (!success) success = checker.isCheckEntityContent(input);
//		if (!success) success = checker.isCheckEntityCharset(input);
//		if (!success) success = checker.isCopyEntity(input);
//
//		// 加载/卸载
//		if (!success) success = checker.isLoadIndex(input);
//		if (!success) success = checker.isStopIndex(input);
//		if (!success) success = checker.isLoadEntity(input);
//		if (!success) success = checker.isStopEntity(input);
//
//		// 判断命令
//		if (!success) success = checker.isBuildHash(input);
//		// 半截符编码/解码
//		if (!success) success = checker.isBuildHalf(input);
//		// EACH签名
//		if (!success) success = checker.isBuildEach(input);
//		// 判断是处理模式（内存/磁盘）
//		if(!success) success = checker.isCommandMode(input);
//		// 判断是命令超时
//		if(!success) success = checker.isCommandTimeout(input);
//
//		// 密文超时...
//		if(!success) success = checker.isCipherTimeout(input);
//		// 最大异步缓存尺寸
//		if(!success) success = checker.isMaxEchoBuffer(input);
//		if(!success) success = checker.isMaxInvoker(input);
//		// 打印FRONT网关
//		if(!success) success = checker.isPrintFrontGateway(input);
//		// 检测本地系统信息
//		if(!success) success = checker.isCheckSystemInfo(input);
//		// 检测内网穿透信道
//		if(!success) success = checker.isCheckPockChannel(input);
//		// 检测支持MASSIVE MIMO
//		if(!success) success = checker.isCheckMassiveMimo(input);
//
//		// 设置最大任务数目/最大在线用户 .... 数目
//		if (!success) success = checker.isSetMaxJobs(input);
//		if (!success) success = checker.isSetMaxMembers(input);
//		if (!success) success = checker.isSetMaxTasks(input);
//		if (!success) success = checker.isSetMaxRegulates(input);
//		if (!success) success = checker.isSetMaxSize(input);
//		if (!success) success = checker.isSetMaxGroups(input);
//		if (!success) success = checker.isSetMaxGateways(input);
//		if (!success) success = checker.isSetMaxWorkers(input);
//		if (!success) success = checker.isSetMaxBuilders(input);
//		if (!success) success = checker.isSetMaxTables(input);
//		if (!success) success = checker.isSetMaxIndexes(input);
//		if (!success) success = checker.isSetMaxDSMReduce(input);
//		if (!success) success = checker.isSetExpireTime(input);
//		if (!success) success = checker.isSetMiddleBuffer(input);
//		if (!success) success = checker.isSetCloudSize(input);
//
//		// 开放/关闭共享资源
//		if (!success) success = checker.isOpenShareSchema(input);
//		if (!success) success = checker.isOpenShareTable(input);
//		if (!success) success = checker.isCloseShareSchema(input);
//		if (!success) success = checker.isCloseShareTable(input);
//		if (!success) success = checker.isShowOpenResource(input);
//		if (!success) success = checker.isShowPassiveResource(input);
//
//		// 检索在线命令
//		if (!success) success = checker.isSeekOnlineCommand(input);
//
//		// 重置包尺寸
//		if (!success) success = checker.isReplyPacketMode(input);
//		if (!success) success = checker.isReplyPacketSize(input);
//		if (!success) success = checker.isReplySendTimeout(input);
//		if (!success) success = checker.isReplyReceiveTimeout(input);
//		if (!success) success = checker.isReplyFlowControl(input);
//		// 流量测试
//		if (!success) success = checker.isParallelMultiSwarm(input);
//		if (!success) success = checker.isMultiSwarm(input);
//		if (!success) success = checker.isSwarm(input);
//		// 释放节点内存
//		if (!success) success = checker.isReleaseMemory(input);
//		if (!success) success = checker.isReleaseMemoryInterval(input);
//
//		// 判断INJECT INTO ... SELECT操作
//		if(!success) success = checker.isInjectSelect(input, false);
//		// JOIN操作
//		if(!success) success = checker.isJoin(input);
//		// 判断SQL SELECT语句
//		if (!success) success = checker.isSelect(input, false);
//		// 判断SQL "DELETE FROM"语句
//		if (!success) success = checker.isDelete(input, false);
//		// 判断SQL "INSERT INTO"
//		if(!success) success = checker.isInsert(input, false);
//		// 判断SQL "INJECT INTO"
//		if(!success) success = checker.isInject(input, false);
//		// 判断SQL "UPDATE ... SET ..."
//		if (!success) success = checker.isUpdate(input, false);
//		// 判断分布计算 "CONDUCT ..."
//		if (!success) success = checker.isConduct(input);
//		// 判断分布数据构建
//		if (!success) success = checker.isEstablish(input);
//		// 分布迭代计算
//		if (!success) success = checker.isContact(input);
//
//		// 执行数据优化
//		if (!success) success = checker.isRegulate(input);
//		// 数据调整（regulate在BUILD站点上的操作，使用ESTABLISH命令）
//		if (!success) success = checker.isModulate(input);
//
//		// 更新私有网络空间
//		if (!success) success = checker.isRefreshCyber(input);
//
//		// 最大CPU占比/最大虚拟机内存占比/最小系统内存/磁盘空间限制
//		if (!success) success = checker.isMostCPU(input);
//		if (!success) success = checker.isMostVMMemory(input);
//		if (!success) success = checker.isLeastMemory(input);
//		if (!success) success = checker.isLeastDisk(input);
//		if (!success) success = checker.isCheckSitePath(input);
//
//		// 边缘计算
//		if (!success) success = checker.isRunTubService(input);
//		if (!success) success = checker.isStopTubService(input);
//		if (!success) success = checker.isPrintTubService(input);
//		if (!success) success = checker.isShowTubContainer(input);
//		if (!success) success = checker.isCheckTubListener(input);
//
//		// 分布处理超时
//		if(!success) success = checker.isDistributedTimeout(input);
//
//		// 显示本地应用
//		if(!success) success = checker.isCheckLocalTask(input);
//		// 显示连接节点
//		if(!success) success = checker.isCheckRemoteSite(input);
//		// 显示日志
//		if(!success) success = checker.isMaxLogElements(input);
//		// TIGGER
//		if (!success) success = checker.isOpenTigger(input);
//		if (!success) success = checker.isCloseTigger(input);
//
//		// 检索云应用
//		if (!success) success = checker.isSeekCloudWare(input);
//
//		// 生成包
//		if (!success) success = checker.isBuildConductPackage(input);
//		if (!success) success = checker.isBuildEstablishPackage(input);
//		if (!success) success = checker.isBuildContactPackage(input);
//
//		// 发布包
//		if (!success) success = checker.isDeployConductPackage(input);
//		if (!success) success = checker.isDeployEstablishPackage(input);
//		if (!success) success = checker.isDeployContactPackage(input);
//
//		// 删除包
//		if (!success) success = checker.isDropConductPackage(input);
//		if (!success) success = checker.isDropEstablishPackage(input);
//		if (!success) success = checker.isDropContactPackage(input);
//		// 运行分布应用
//		if (!success) success = checker.isRunTask(input);
//
//		// 建立目录/删除目录
//		if (!success) success = checker.isCreateCloudDirectory(input);
//		if (!success) success = checker.isDropCloudDirectory(input);
//		if (!success) success = checker.isDropCloudFile(input);
//		if (!success) success = checker.isRenameCloudDirectory(input);
//		if (!success) success = checker.isRenameCloudFile(input);
//		if (!success) success = checker.isUploadCloudFile(input);
//		if (!success) success = checker.isDownloadCloudFile(input);
//		if (!success) success = checker.isScanCloudDirectory(input);
//
//		// 以上不成立，由自定义命令调用器去判断是属于自定义命令
//		if (!success) {
//			success = CustomCreator.isCommand(input);
//		}
//
//		return success;
//	}

	/**
	 * 提交命令到目标站点
	 * @param input 输入语句
	 * @return 执行执行结果
	 */
	public int submit(String input, CommandAuditor auditor, MeetDisplay display) {
		boolean success = false;
		try {
			success = implement(input, auditor, display);
		} catch (Throwable e) {
			Logger.fatal(input, e);
			String msg = e.getMessage();
			fault(msg, display);
			return CommandSubmit.FAULTED;
		}
		return (success ? CommandSubmit.ACCEPTED : CommandSubmit.CANCELED);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.platform.CommandDispatcher#submit(com.laxcus.command.Command, boolean, com.laxcus.platform.CommandAuditor, com.laxcus.task.display.MeetDisplay)
	 */
	@Override
	public int submit(Command cmd, boolean auto, CommandAuditor auditor, MeetDisplay display) {
		boolean success = false;
		try {
			success = implement(cmd, auto, auditor, display);
		} catch (Throwable e) {
			Logger.fatal(e);
			String msg = e.getMessage();
			fault(msg, display);
			return CommandSubmit.FAULTED;
		}
		return (success ? CommandSubmit.ACCEPTED : CommandSubmit.CANCELED);
	}
	
	private boolean doAskSite(AskSite cmd, MeetDisplay display) {
		RayAskSiteInvoker invoker = new RayAskSiteInvoker(cmd);
		return launch(invoker, display);
	}

	private boolean doAskClusterMember(AskClusterMember cmd, MeetDisplay display) {
		RayAskClusterMemberInvoker invoker = new RayAskClusterMemberInvoker(cmd);
		return launch(invoker, display);
	}
	
	private boolean doShiftSeekSiteRuntime(ShiftSeekSiteRuntime cmd, MeetDisplay display) {
		RayShiftSeekSiteRuntimeInvoker invoker = new RayShiftSeekSiteRuntimeInvoker(cmd);
		return launch(invoker, display);
	}

	/**
	 * 执行命令
	 * @param cmd
	 * @param auto
	 * @param auditor
	 * @param display
	 * @return
	 */
	private boolean implement(Command cmd, boolean auto, CommandAuditor auditor, MeetDisplay display) {
		// 检查连网状态
		if (!checkOnline(display)) {
			return false;
		}
		
		boolean success = true;
		if (cmd.getClass() == AskSite.class) {
			if (auto) {
				success = doAskSite((AskSite) cmd, display);
			} else {
				success = confirm(auditor);
				if (success) {
					success = doAskSite((AskSite) cmd, display);
				}
			}
		} else if(cmd.getClass() == AskClusterMember.class) {
			if (auto) {
				doAskClusterMember((AskClusterMember) cmd, display);
			} else {
				success = confirm(auditor);
				if (success) {
					success = doAskClusterMember((AskClusterMember) cmd, display);
				}
			}
		} else if(cmd.getClass() == ShiftSeekSiteRuntime.class) {
			if (auto) {
				success = doShiftSeekSiteRuntime((ShiftSeekSiteRuntime) cmd, display);
			} else {
				success = confirm(auditor);
				if (success) {
					success = doShiftSeekSiteRuntime((ShiftSeekSiteRuntime) cmd, display);
				}
			}
		} else {
			success = false;
		}
		
		return success;
		

		//		// 扫描磁盘目录，两种情况：1. 如果是自动，立即执行；2. 不是自动，确认后执行
		//		if (cmd.getClass() == ScanCloudDirectory.class) {
		//			if (auto) {
		//				doScanDirectory((ScanCloudDirectory) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doScanDirectory((ScanCloudDirectory) cmd, display);
		//				}
		//			}
		//		}
		//		// 生成目录
		//		else if (cmd.getClass() == CreateCloudDirectory.class) {
		//			if (auto) {
		//				doCreateDirectory((CreateCloudDirectory) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doCreateDirectory((CreateCloudDirectory) cmd, display);
		//				}
		//			}
		//		}
		//		// 下载文件
		//		else if (cmd.getClass() == DownloadCloudFile.class) {
		//			if (auto) {
		//				doDownloadFile((DownloadCloudFile) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doDownloadFile((DownloadCloudFile) cmd, display);
		//				}
		//			}
		//		}
		//		// 上传文件
		//		else if (cmd.getClass() == UploadCloudFile.class) {
		//			if (auto) {
		//				doUploadFile((UploadCloudFile) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doUploadFile((UploadCloudFile) cmd, display);
		//				}
		//			}
		//		}
		//		// 删除云端文件
		//		else if (cmd.getClass() == DropCloudFile.class) {
		//			if (auto) {
		//				doDropCloudFile((DropCloudFile) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doDropCloudFile((DropCloudFile) cmd, display);
		//				}
		//			}
		//		}
		//		// 删除云端目录
		//		else if (cmd.getClass() == DropCloudDirectory.class) {
		//			if (auto) {
		//				doDropCloudDirectory((DropCloudDirectory) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doDropCloudDirectory((DropCloudDirectory) cmd, display);
		//				}
		//			}
		//		}
		//		// 修改云端文件
		//		else if (cmd.getClass() == RenameCloudFile.class) {
		//			if (auto) {
		//				doRenameCloudFile((RenameCloudFile) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doRenameCloudFile((RenameCloudFile) cmd, display);
		//				}
		//			}
		//		}
		//		// 修改云端目录
		//		else if (cmd.getClass() == RenameCloudDirectory.class) {
		//			if (auto) {
		//				doRenameCloudDirectory((RenameCloudDirectory) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doRenameCloudDirectory((RenameCloudDirectory) cmd, display);
		//				}
		//			}
		//		}
		//		
		//		// CONDUCT命令
		//		else if (cmd.getClass() == Conduct.class) {
		//			if (auto) {
		//				doConduct((Conduct) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doConduct((Conduct) cmd, display);
		//				}
		//			}
		//		}
		//		// CONTACT命令
		//		else if (cmd.getClass() == Contact.class) {
		//			if (auto) {
		//				doContact((Contact) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doContact((Contact) cmd, display);
		//				}
		//			}
		//		}
		//		// ESTABLISH命令
		//		else if (cmd.getClass() == Establish.class) {
		//			if (auto) {
		//				doEstablish((Establish) cmd, display);
		//			} else {
		//				boolean b = confirm(auditor);
		//				if (b) {
		//					doEstablish((Establish) cmd, display);
		//				}
		//			}
		//		}
		//		//
		//		else {
		//			return false;
		//		}
	}

	/**
	 * 执行命令
	 * @param input 输入语句
	 */
	private boolean implement(String input, CommandAuditor auditor, MeetDisplay display) {
		// 检测是在线模式
		if (!checkOnline(display)) {
			return false;
		}

		// 判断是散列码命令
		boolean success = checker.isBuildHash(input);
		if (success) {
			doBuildHash(input, display);
		}
		// 设置命令超时时间
		if (!success) {
			success = checker.isCommandTimeout(input);
			if (success) {
				doSetCommandTimeout(input, display);
			}
		}
		// 命令处理模式
		if (!success) {
			success = checker.isCommandMode(input);
			if (success) {
				doSetCommandMode(input, display);
			}
		}
		// 设置命令优先级
		if (!success) {
			success = checker.isCommandRank(input);
			if (success) {
				doSetCommandRank(input, display);
			}
		}

		// 设置FIXP本地客户端密文超时时间
		if (!success) {
			success = checker.isCipherTimeout(input);
			if (success && confirm(auditor)) {
				doSetCipherTimeout(input, display);
			}
		}
		// 设置调用器
		if (!success) {
			success = checker.isMaxInvoker(input);
			if (success && confirm(auditor)) {
				doSetMaxInvoker(input, display);
			}
		}
		// 设置调用器
		if (!success) {
			success = checker.isCheckSystemInfo(input);
			if (success && confirm(auditor)) {
				doCheckSystemInfo(input, display);
			}
		}
		// 检查资源
		if (!success) {
			success = checker.isCheckUserCost(input);
			if (success && confirm(auditor)) {
				doCheckUserCost(input, display);
			}
		}

		// 设置调用器
		if (!success) {
			success = checker.isSetReflectPort(input);
			if (success && confirm(auditor)) {
				doSetReflectPort(input, display);
			}
		}
		// 设置异步缓存尺寸
		if (!success) {
			success = checker.isMaxEchoBuffer(input);
			if (success && confirm(auditor)) {
				doSetMaxEchoBuffer(input, display);
			}
		}
		// 判断RUSH命令
		if (!success) {
			success = checker.isRush(input);
			if (success && confirm(auditor)) {
				doRush(input, display);
			}
		}
		// 判断COMPACT命令
		if (!success) {
			success = checker.isCompact(input);
			if (success && confirm(auditor)) {
				doCompact(input, display);
			}
		}
		// 判断SET DSM REDUCE命令
		if (!success) {
			success = checker.isSetDSMReduce(input);
			if (success && confirm(auditor)) {
				doSetDSMReduce(input, display);
			}
		}
		
		// 建立站点密钥令牌
		if (!success) {
			success = checker.isCreateSecureToken(input);
			if (success && confirm(auditor)) {
				doCreateSecureToken(input, display);
			}
		}
		// 删除站点密钥令牌
		if (!success) {
			success = checker.isDropSecureToken(input);
			if (success && confirm(auditor)) {
				doDropSecureToken(input, display);
			}
		}
		// 输出站点密钥令牌到磁盘
		if (!success) {
			success = checker.isFlushSecureToken(input);
			if (success && confirm(auditor)) {
				doFlushSecureToken(input, display);
			}
		}
		// 显示站点密钥令牌
		if (!success) {
			success = checker.isShowSecureToken(input);
			if (success && confirm(auditor)) {
				doShowSecureToken(input, display);
			}
		}
		// 设置对称密钥长度
		if (!success) {
			success = checker.isSetSecureSize(input);
			if (success && confirm(auditor)) {
				doSetSecureSize(input, display);
			}
		}
		
		// 重装安装本地的动态链接库
		if(!success) {
			success = checker.isReloadLibrary(input);
			if(success && confirm(auditor)) {
				doReloadLibrary(input, display);
			}
		}
		// 释放节点内存
		if (!success) {
			success = checker.isReleaseMemory(input);
			if (success && confirm(auditor)) {
				doReleaseMemory(input, display);
			}
		}
		// 释放节点内存间隔时间
		if (!success) {
			success = checker.isReleaseMemoryInterval(input);
			if (success && confirm(auditor)) {
				doReleaseMemoryInterval(input, display);
			}
		}
		
		// 设置应答包模式
		if (!success) {
			success = checker.isReplyPacketMode(input);
			if (success && confirm(auditor)) {
				doReplyPacketMode(input, display);
			}
		}
		// 设置应答包尺寸
		if (!success) {
			success = checker.isReplyPacketSize(input);
			if (success && confirm(auditor)) {
				doReplyPacketSize(input, display);
			}
		}
		// 设置发送超时
		if (!success) {
			success = checker.isReplySendTimeout(input);
			if (success && confirm(auditor)) {
				doReplySendTimeout(input, display);
			}
		}
		// 设置接收超时
		if (!success) {
			success = checker.isReplyReceiveTimeout(input);
			if (success && confirm(auditor)) {
				doReplyReceiveTimeout(input, display);
			}
		}
		// 设置队列成员
		if (!success) {
			success = checker.isReplyFlowControl(input);
			if (success && confirm(auditor)) {
				doReplyFlowControl(input, display);
			}
		}
		
		// 重装安装本地的动态链接库
		if (!success) {
			success = checker.isReloadSecurityPolicy(input);
			if (success && confirm(auditor)) {
				doReloadSecurityPolicy(input, display);
			}
		}
		// 重装加载许可证
		if (!success) {
			success = checker.isReloadLicence(input);
			if (success && confirm(auditor)) {
				doReloadLicence(input, display);
			}
		}
		// 重装加载许可证
		if (!success) {
			success = checker.isPublishLicence(input);
			if (success && confirm(auditor)) {
				doPublishLicence(input, display);
			}
		}

		// 扫描堆栈命令
		if (!success) {
			success = checker.isScanCommandStack(input);
			if (success && confirm(auditor)) {
				doScanStackCommand(input, display);
			}
		}
		// 扫描数据库
		if (!success) {
			success = checker.isScanSchema(input);
			if (success && confirm(auditor)) {
				doScanSchema(input, display);
			}
		}
		// 扫描数据表
		if (!success) {
			success = checker.isScanTable(input);
			if (success && confirm(auditor)) {
				doScanTable(input, display);
			}
		}
		// 扫描数据块
		if (!success) {
			success = checker.isScanEntityWithWatch(input);
			if (success && confirm(auditor)) {
				doScanEntity(input, display);
			}
		}

		// FRONT站点分布命令
		if (!success) {
			success = checker.isSeekFrontUser(input);
			if (success && confirm(auditor)) {
				doSeekFrontUser(input, display);
			}
		}
		// 用户站点分布命令
		if (!success) {
			success = checker.isSeekUserSite(input);
			if (success && confirm(auditor)) {
				doSeekUserSite(input, display);
			}
		}
		// 用户数据表分布命令
		if (!success) {
			success = checker.isSeekUserTable(input);
			if (success && confirm(auditor)) {
				doSeekUserTable(input, display);
			}
		}
		// 用户阶段命名分布命令
		if (!success) {
			success = checker.isSeekUserTask(input);
			if (success && confirm(auditor)) {
				doSeekUserTask(input, display);
			}
		}

		// 显示FRONT站点
		if(!success) {
			success = checker.isSeekFrontSite(input);
			if (success && confirm(auditor)) {
				doSeekFrontSite(input, display);
			}
		}
		// 远程关闭命令
		if (!success) {
			success = checker.isShutdown(input);
			if (success && confirm(auditor)) {
				doShutdown(input, display);
			}
		}
		// 判断是刷新注册用户命令
		if (!success) {
			success = checker.isRefreshUser(input);
			if (success && confirm(auditor)) {
				doRefreshUser(input, display);
			}
		}
		// 判断是刷新元数据命令
		if (!success) {
			success = checker.isRefreshMetadata(input);
			if (success && confirm(auditor)) {
				doRefreshMetadata(input, display);
			}
		}
		// 判断是扫描用户节点关联时间间隔
		if (!success) {
			success = checker.isScanLinkTime(input);
			if (success && confirm(auditor)) {
				doScanLinkTime(input, display);
			}
		}
		// 强制要求站点重新注册
		if (!success) {
			success = checker.isRefreshLogin(input);
			if (success && confirm(auditor)) {
				doRefreshLogin(input, display);
			}
		}
		
//		// 判断是发布系统分布任务组件
//		if (!success) {
//			success = checker.isPublishTaskComponent(input);
//			if (success && confirm(auditor)) {
//				doPublishMultiTaskComponent(input);
//			}
//		}
//		// 判断是发布系统分布任务组件的应用附件
//		if (!success) {
//			success = checker.isPublishTaskAssistComponent(input);
//			if (success && confirm(auditor)) {
//				doPublishMultiTaskAssistComponent(input);
//			}
//		}
//		// 判断是发布系统分布任务组件的动态链接库
//		if (!success) {
//			success = checker.isPublishTaskLibraryComponent(input);
//			if (success && confirm(auditor)) {
//				doPublishMultiTaskLibraryComponent(input);
//			}
//		}

		// 判断是检索分布任务组件
		if (!success) {
			success = checker.isSeekTask(input);
			if (success && confirm(auditor)) {
				doSeekTask(input, display);
			}
		}
		
		// 判断是设置站点日志等级
		if (!success) {
			success = checker.isSetLogLevel(input);
			if (success && confirm(auditor)) {
				doSetLogLevel(input, display);
			}
		}
		// 重装加载/发布自定义JAR包
		if(!success) {
			success = checker.isReloadCustom(input);
			if(success && confirm(auditor)) {
				doReloadCustom(input, display);
			}
		}

		// 半截符编码
		if (!success) {
			success = checker.isBuildHalf(input);
			if (success) { // 在本地进行
				doBuildHalf(input, display);
			}
		}
		// EACH签名
		if (!success) {
			success = checker.isBuildEach(input);
			if (success) { // 在本地进行
				doBuildEach(input, display);
			}
		}

		// 显示用户/授权/数据库/数据表状态
		if (!success) {
			success = checker.isPrintUserDiagram(input);
			if (success && confirm(auditor)) {
				doPrintUserDiagram(input, display);
			}
		}
		if (!success) {
			success = checker.isPrintGrantDiagram(input);
			if (success && confirm(auditor)) {
				doPrintGrantDiagram(input, display);
			}
		}
		if (!success) {
			success = checker.isPrintSchemaDiagram(input);
			if (success && confirm(auditor)) {
				doPrintSchemaDiagram(input, display);
			}
		}
		if (!success) {
			success = checker.isPrintTableDiagram(input);
			if (success && confirm(auditor)) {
				doPrintTableDiagram(input, display);
			}
		}

		// 扫描用户日志
		if (!success) {
			success = checker.isScanUserLog(input);
			if (success && confirm(auditor)) {
				doScanUserLog(input, display);
			}
		}

		// 设置命令超时时间
		if (!success) {
			success = checker.isOutlookInterval(input);
			if (success) {
				doOutlookInterval(input, display);
			}
		}

		// 检索在线命令
		if (!success) {
			success = checker.isSeekOnlineCommand(input);
			if (success && confirm(auditor)) {
				doSeekOnlineCommand(input, display);
			}
		}
		// 检索在线命令
		if (!success) {
			success = checker.isSeekOnlineResource(input);
			if (success && confirm(auditor)) {
				doSeekOnlineResource(input, display);
			}
		}
		// 检索用户在线注册元数据
		if (!success) {
			success = checker.isSeekRegisterMetadata(input);
			if (success && confirm(auditor)) {
				doSeekRegisterMetadata(input, display);
			}
		}
		// 检索用户分布区域
		if (!success) {
			success = checker.isSeekUserArea(input);
			if (success && confirm(auditor)) {
				doSeekUserArea(input, display);
			}
		}

		// 并行数据传输流量测试
		if (!success) {
			success = checker.isParallelMultiGust(input);
			if (success && confirm(auditor)) {
				doParallelMultiGust(input, display);
			}
		}
		// 数据传输流量测试
		if (!success) {
			success = checker.isMultiGust(input);
			if (success && confirm(auditor)) {
				doMultiGust(input, display);
			}
		}
		// 数据传输流量测试
		if (!success) {
			success = checker.isGust(input);
			if (success && confirm(auditor)) {
				doGust(input, display);
			}
		}
		// 并行数据传输流量测试
		if (!success) {
			success = checker.isParallelMultiSwarm(input);
			if (success && confirm(auditor)) {
				doParallelMultiSwarm(input, display);
			}
		}
		// 持续数据传输流量测试
		if (!success) {
			success = checker.isMultiSwarm(input);
			if (success && confirm(auditor)) {
				doMultiSwarm(input, display);
			}
		}
		// 数据传输流量测试
		if (!success) {
			success = checker.isSwarm(input);
			if (success && confirm(auditor)) {
				doSwarm(input, display);
			}
		}

		// 设置FIXP远程客户端密文超时时间
		if (!success) {
			success = checker.isRing(input);
			if (success && confirm(auditor)) {
				doRing(input, display);
			}
		}
		// 发布用户到指定站点
		if (!success) {
			success = checker.isDeployUser(input);
			if (success && confirm(auditor)) {
				doDeployUser(input, display);
			}
		}
		// 发布用户到指定站点
		if (!success) {
			success = checker.isEraselUser(input);
			if (success) {
				doEraseUser(input, auditor, display);
			}
		}
		// 发布数据表到指定站点
		if (!success) {
			success = checker.isDeployTable(input);
			if (success && confirm(auditor)) {
				doDeployTable(input, display);
			}
		}
		// 从DATA节点复制数据块到从节点
		if (!success) {
			success = checker.isCopyMasterMass(input);
			if (success && confirm(auditor)) {
				doCopyMasterMass(input, display);
			}
		}
		// 定位GATE站点模式
		if (!success) {
			success = checker.isShadowMode(input);
			if (success && confirm(auditor)) {
				doShadowMode(input, display);
			}
		}
		// 检查GATE站点与编号一致性
		if (!success) {
			success = checker.isCheckShadowConsistency(input);
			if (success && confirm(auditor)) {
				doCheckShadowConsistency(input, display);
			}
		}

		// 最高CPU限制
		if (!success) {
			success = checker.isMostCPU(input);
			if (success && confirm(auditor)) {
				doMostCPU(input, display);
			}
		}
		// 最高虚拟机内存限制
		if (!success) {
			success = checker.isMostVMMemory(input);
			if (success && confirm(auditor)) {
				doMostVMMemory(input, display);
			}
		}
		// 最少内存限制
		if (!success) {
			success = checker.isLeastMemory(input);
			if (success && confirm(auditor)) {
				doLeastMemory(input, display);
			}
		}
		// 最少内存限制
		if (!success) {
			success = checker.isLeastDisk(input);
			if (success && confirm(auditor)) {
				doLeastDisk(input, display);
			}
		}
		// 打印节点设备目录
		if (!success) {
			success = checker.isCheckSitePath(input);
			if (success && confirm(auditor)) {
				doCheckSitePath(input, display);
			}
		}
		// 设置分布处理超时
		if (!success) {
			success = checker.isDistributedTimeout(input);
			if (success && confirm(auditor)) {
				doSetDistributedTimeout(input, display);
			}
		}

		// 打开/屏蔽其他节点的警告/故障消息
		if (!success) {
			success = checker.isEnableWarning(input);
			if (success && confirm(auditor)) {
				doEnableWarning(input, display);
			}
		}
		if (!success) {
			success = checker.isEnableFault(input);
			if (success && confirm(auditor)) {
				doEnableFault(input, display);
			}
		}
		if (!success) {
			success = checker.isDisableWarning(input);
			if (success && confirm(auditor)) {
				doDisableWarning(input, display);
			}
		}
		if (!success) {
			success = checker.isDisableFault(input);
			if (success && confirm(auditor)) {
				doDisableFault(input, display);
			}
		}
		// 设置虚拟空间
		if (!success) {
			success = checker.isSetMemberCyber(input);
			if (success && confirm(auditor)) {
				doSetMemberCyber(input, display);
			}
		}
		if (!success) {
			success = checker.isSetFrontCyber(input);
			if (success && confirm(auditor)) {
				doSetFrontCyber(input, display);
			}
		}
		if (!success) {
			success = checker.isCheckUserCyber(input);
			if (success && confirm(auditor)) {
				doCheckUserCyber(input, display);
			}
		}

		// 日志显示数目
		if (!success) {
			success = checker.isMaxLogElements(input);
			if (success) {
				doSetMaxLogElements(input, display);
			}
		}
		if (!success) {
			success = checker.isOpenTigger(input);
			if (success && confirm(auditor)) {
				doOpenTigger(input, display);
			}
		}
		if (!success) {
			success = checker.isCloseTigger(input);
			if (success && confirm(auditor)) {
				doCloseTigger(input, display);
			}
		}
		if (!success) {
			success = checker.isCheckDistributedMember(input);
			if (success && confirm(auditor)) {
				doCheckDistributedMember(input, display);
			}
		}

		// 部署包
		if (!success) {
			success = checker.isDeployConductPackage(input);
			if (success && confirm(auditor)) {
				doDeployConductPackage(input, display);
			}
		}
		if (!success) {
			success = checker.isDeployEstablishPackage(input);
			if (success && confirm(auditor)) {
				doDeployEstablishPackage(input, display);
			}
		}
		if (!success) {
			success = checker.isDeployContactPackage(input);
			if (success && confirm(auditor)) {
				doDeployContactPackage(input, display);
			}
		}

		// 删除包
		if (!success) {
			success = checker.isDropConductPackage(input);
			if (success && confirm(auditor)) {
				doDropConductPackage(input, display);
			}
		}
		if (!success) {
			success = checker.isDropEstablishPackage(input);
			if (success && confirm(auditor)) {
				doDropEstablishPackage(input, display);
			}
		}
		if (!success) {
			success = checker.isDropContactPackage(input);
			if (success && confirm(auditor)) {
				doDropContactPackage(input, display);
			}
		}
		
		
		// 自定义命令调用器来判断
		// 判断是自定义命令
		if (!success) {
			success = CustomCreator.isCommand(input);
			if (success) {
				EchoInvoker invoker = CustomCreator.createInvoker(input);
				// 如果是基于RayCustomInvoker实例，设置它的调用器
				if (Laxkit.isClassFrom(invoker, CustomDisplayInvoker.class)) {
					((CustomDisplayInvoker) invoker).setDisplay(display);
				}
				// 确认后执行
				if (confirm(auditor)) {
					launch(invoker, display);
					success = true;
				}
			}
		}

		// 以上判断不成立时，拒绝执行；否则保存它！
		if (success) {
			Tigger.command(input);
		} else {
			String text = launcher.fault(FaultTip.ILLEGAL_COMMAND);
			fault(text, display);
		}
		// 返回结果
		return success;
	}


//	/**
//	 * 执行命令
//	 * @param input 输入语句
//	 * @return 命令正确且用户确认执行时返回真，否则假
//	 */
//	private boolean implementX(String input, CommandAuditor auditor, MeetDisplay display) {
//		// 检查连网状态
//		if (!checkOnline(display)) {
//			return false;
//		}
//
//		boolean pass = false;
//
//		// 数据库
//		boolean success = checker.isCreateSchema(input);
//		pass = (success && confirm(auditor));
//		if (pass) {
//			doCreateSchema(input, display);
//		}
//		if (!success) {
//			success = checker.isDropSchema(input);
//
//			if (success) {
//				String tag = (isWatch() ? "DROP-DATABASE/WARNING-CONTENT/CONSOLE" : "DROP-DATABASE/WARNING-CONTENT/TERMINAL");
//				String content = RayInvoker.getXMLContent(tag);
//				pass = confirm(null, content, auditor);
//				if (pass) {
//					doDropSchema(input, display);
//				}
//			}
//		}
//		// 判断数据库存在
//		if (!success) {
//			success = checker.isAssertSchema(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doAssertSchema(input, display);
//			}
//		}
//
//		if (!success) {
//			success = checker.isPrintSchemaDiagram(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doPrintSchemaDiagram(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isShowSchema(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doShowSchema(input, display);
//			}
//		}
//
//		if (!success) {
//			success = checker.isCreateUser(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCreateUser(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isDropUser(input);
//
//			if (success) {
//				String tag = (isWatch() ? "DROP-USER/WARNING-CONTENT/CONSOLE" : "DROP-USER/WARNING-CONTENT/TERMINAL");
//				String content = RayInvoker.getXMLContent(tag);
//				pass = confirm(null, content, auditor);
//				if (pass) {
//					doDropUser(input, display);
//				}
//			}
//		}
//
//		if (!success) {
//			success = checker.isAlterUser(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doAlterUser(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isAssertUser(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doAssertUser(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isPrintUserDiagram(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doPrintUserDiagram(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isOpenUser(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doOpenUser(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isCloseUser(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCloseUser(input, display);
//			}
//		}
//
//		if (!success) {
//			success = checker.isGrant(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doGrant(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isRevoke(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doRevoke(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isPrintGrantDiagram(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doPrintGrantDiagram(input, display);
//			}
//		}
//
//		// 表命令
//		if (!success) {
//			success = checker.isCreateTable(input);
//			pass = (success && confirm(auditor));
//			if (pass) doCreateTable(input, display);
//		}
//		if (!success) {
//			success = checker.isDropTable(input);
//			if (success) {
//				String tag = (isWatch() ? "DROP-TABLE/WARNING-CONTENT/CONSOLE"
//						: "DROP-TABLE/WARNING-CONTENT/TERMINAL");
//				String content = RayInvoker.getXMLContent(tag);
//				pass = confirm("", content, auditor);
//				if (pass) {
//					doDropTable(input, display);
//				}
//			}
//		}
//		if (!success) {
//			success = checker.isPrintTableDiagram(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doPrintTableDiagram(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isShowTable(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doShowTable(input, display);
//			}
//		}
//		if (!success) {
//			pass = success = checker.isCheckRemoteTable(input);
//			if (success) {
//				doCheckRemoteTable(input, display);
//			}
//		}
//		if (!success) {
//			pass = success = checker.isCheckRemoteTask(input);
//			if (success) {
//				doCheckRemoteTask(input, display);
//			}
//		}
//
//		// 扫描数据库
//		if (!success) {
//			success = checker.isScanSchema(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doScanSchema(input, display);
//			}
//		}
//		// 扫描数据表
//		if (!success) {
//			success = checker.isScanTable(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doScanTable(input, display);
//			}
//		}
//
//		//		// 发布分布任务组件
//		//		if (!success) {
//		//			success = checker.isPublishTaskComponent(input);
//		//			pass = (success && confirm(auditor));
//		//			if (pass) doPublishTaskComponent(input, display);
//		//		}
//		//		// 发布分布任务组件的应用附件
//		//		if (!success) {
//		//			success = checker.isPublishTaskAssistComponent(input);
//		//			pass = (success && confirm(auditor));
//		//			if (pass) doPublishTaskAssistComponent(input, display);
//		//		}
//		//		// 发布分布任务组件的动态链接库
//		//		if (!success) {
//		//			success = checker.isPublishTaskLibraryComponent(input);
//		//			pass = (success && confirm(auditor));
//		//			if (pass) doPublishTaskLibraryComponent(input, display);
//		//		}
//
//
//		// INJECT INTO ... SELECT操作
//		if (!success) {
//			success = checker.isInjectSelect(input, true);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doInjectSelect(input, display);
//			}
//		}
//		// JOIN操作
//		if (!success) {
//			success = checker.isJoin(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				join(input, display);
//			}
//		}
//
//		if (!success) {
//			success = checker.isSelect(input, true);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSelect(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isDelete(input, true);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDelete(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isInsert(input, true);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doInsert(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isInject(input, true);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doInject(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isUpdate(input, true);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doUpdate(input, display);
//			}
//		}
//		// 分布计算
//		if (!success) {
//			success = checker.isConduct(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doConduct(input, display);
//			}
//		}
//		// ESTABLISH分布数据构建
//		if (!success) {
//			success = checker.isEstablish(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doEstablish(input, display);
//			}
//		}
//		// CONTACT分布迭代计算
//		if (!success) {
//			success = checker.isContact(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doContact(input, display);
//			}
//		}
//
//		if (!success) {
//			success = checker.isLoadIndex(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doLoadIndex(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isStopIndex(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doStopIndex(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isLoadEntity(input);
//			pass = (success && confirm(auditor)); if(pass) doLoadEntity(input, display);
//		}
//		if (!success) {
//			success = checker.isStopEntity(input);
//			pass = (success && confirm(auditor)); if(pass) doStopEntity(input, display);
//		}
//
//		// 数据调整
//		if (!success) {
//			success = checker.isModulate(input);
//			pass = (success && confirm(auditor)); if(pass) doModulate(input, display);
//		}
//		// 数据优化
//		if (!success) {
//			success = checker.isRegulate(input);
//			pass = (success && confirm(auditor)); if(pass) doRegulate(input, display);
//		}
//		// 建立数据优化时间(每周、每天、每时)
//		if (!success) {
//			success = checker.isCreateRegulateTime(input);
//			pass = (success && confirm(auditor)); if(pass) doCreateRegulateTime(input, display);
//		}
//		// 撤销数据优化时间(每周、每天、每时)
//		if (!success) {
//			success = checker.isDropRegulateTime(input);
//			pass = (success && confirm(auditor)); if(pass) doDropRegulateTime(input, display);
//		}
//		// 显示数据优化时间
//		if (!success) {
//			pass = success = checker.isPrintRegulateTime(input);
//			if (success) doPrintRegulateTime(input, display);
//		}
//		// 检查表数据一致性
//		if (!success) {
//			success = checker.isCheckEntityConsistency(input);
//			pass = (success && confirm(auditor)); if(pass) doCheckEntityConsistency(input, display);
//		}
//		// 修复表数据一致性
//		if (!success) {
//			success = checker.isRecoverEntityConsistency(input);
//			pass = (success && confirm(auditor)); if(pass) doRecoverEntityConsistency(input, display);
//		}
//
//		// 分布数据容量分析
//		if (!success) {
//			success = checker.isScanSketch(input);
//			pass = (success && confirm(auditor)); if(pass) doScanSketch(input, display);
//		}
//
//		// 数据块强制转换命令
//		if (!success) {
//			success = checker.isRush(input);
//			pass = (success && confirm(auditor)); if(pass) doRush(input, display);
//		}
//
//		// 设置数据块尺寸
//		if (!success) {
//			success = checker.isSetEntitySize(input);
//			pass = (success && confirm(auditor)); if(pass)
//				doSetEntitySize(input, display);
//		}
//		// 显示数据块尺寸
//		if (!success) {
//			success = checker.isShowEntitySize(input);
//			pass = (success && confirm(auditor)); if(pass)
//				doShowEntitySize(input, display);
//		}
//		// 统计数据块尺寸
//		if (!success) {
//			success = checker.isScanEntity(input);
//			pass = (success && confirm(auditor)); if(pass)
//				doScanEntity(input, display);
//		}
//		// 获得数据块编号
//		if (!success) {
//			success = checker.isGitStubs(input);
//			pass = (success && confirm(auditor)); if(pass) doGitStubs(input, display);
//		}
//		// 显示数据块分布图谱
//		if (!success) {
//			success = checker.isPrintStubsDiagram(input);
//			pass = (success && confirm(auditor)); if(pass) doPrintStubsDiagram(input, display);
//		}
//		// 获得数据块数据
//		if (!success) {
//			success = checker.isExportEntity(input);
//			pass = (success && confirm(auditor)); if(pass) doExportEntity(input, display);
//		}
//		// 获得数据块数据
//		if (!success) {
//			success = checker.isImportEntity(input);
//			pass = (success && confirm(auditor)); if(pass) doImportEntity(input, display);
//		}
//		// 获得数据块数据
//		if (!success) {
//			success = checker.isCheckEntityContent(input);
//			pass = (success && confirm(auditor)); if(pass) doCheckEntityContent(input, display);
//		}
//		// 判断文件编码
//		if (!success) {
//			success = checker.isCheckEntityCharset(input);
//			pass = (success && confirm(auditor)); if(pass) doCheckEntityCharset(input, display);
//		}
//		// 获得数据块数据
//		if (!success) {
//			success = checker.isCopyEntity(input);
//			pass = (success && confirm(auditor)); if(pass) doCopyEntity(input, display);
//		}
//
//		// 计算散列码命令，HASH命令在本地执行，不需要确认
//		if (!success) {
//			success = checker.isBuildHash(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doBuildHash(input, display);
//			}
//		}
//		// 半截符编码
//		if (!success) {
//			success = checker.isBuildHalf(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doBuildHalf(input, display);
//			}
//		}
//		// EACH签名
//		if (!success) {
//			success = checker.isBuildEach(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doBuildEach(input, display);
//			}
//		}
//		// 命令处理模式。处理模式在本地执行，不需要确认
//		if (!success) {
//			success = checker.isCommandMode(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetCommandMode(input, display);
//			}
//		}
//		// 命令超时
//		if (!success) {
//			success = checker.isCommandTimeout(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetCommandTimeout(input, display);
//			}
//		}
//
//		// 服务器端密文超时
//		if (!success) {
//			success = checker.isCipherTimeout(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetCipherTimeout(input, display);
//			}
//		}
//		// 最大异步缓存尺寸
//		if (!success) {
//			success = checker.isMaxEchoBuffer(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxEchoBuffer(input, display);
//			}
//		}
//		// 调用器最大参数
//		if (!success) {
//			success = checker.isMaxInvoker(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxInvoker(input, display);
//			}
//		}
//		// FRONT网关
//		if (!success) {
//			success = checker.isPrintFrontGateway(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetPrintFrontGateway(input, display);
//			}
//		}
//		// 检测服务器系统信息
//		if (!success) {
//			success = checker.isCheckSystemInfo(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCheckSystemInfo(input, display);
//			}
//		}
//		// 检测内网穿透信道
//		if (!success) {
//			success = checker.isCheckPockChannel(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCheckPockChannel(input, display);
//			}
//		}
//		// 检测支持MASSIVE MIMO
//		if (!success) {
//			success = checker.isCheckMassiveMimo(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCheckMassiveMimo(input, display);
//			}
//		}
//		
//		// 建立限制操作规则
//		if (!success) {
//			success = checker.isCreateLimit(input);
//			pass = (success && confirm(auditor)); if(pass) {
//				doCreateLimit(input, display);
//			}
//		}
//		// 删除限制操作规则
//		if (!success) {
//			success = checker.isDropLimit(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDropLimit(input, display);
//			}
//		}
//		// 显示限制操作单元
//		if (!success) {
//			success = checker.isShowLimit(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doShowLimit(input, display);
//			}
//		}
//		// 提交锁定操作
//		if (!success) {
//			success = checker.isCreateFault(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCreateFault(input, display);
//			}
//		}
//		// 撤销锁定操作
//		if (!success) {
//			success = checker.isDropFault(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDropFault(input, display);
//			}
//		}
//		// 显示锁定单元
//		if (!success) {
//			success = checker.isShowFault(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doShowFault(input, display);
//			}
//		}
//		// 显示禁止操作单元
//		if (!success) {
//			success = checker.isShowForbid(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doShowForbid(input, display);
//			}
//		}
//		// 显示事务规则
//		if (!success) {
//			success = checker.isShowLockRule(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doShowLockRule(input, display);
//			}
//		}
//
//		// 设置最大并行任务数
//		if (!success) {
//			success = checker.isSetMaxJobs(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxJobs(input, display);
//			}
//		}
//		// 设置最大连接数
//		if (!success) {
//			success = checker.isSetMaxMembers(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxMembers(input, display);
//			}
//		}
//		// 设置最大最多应用软件
//		if (!success) {
//			success = checker.isSetMaxTasks(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxTasks(input, display);
//			}
//		}
//		// 设置最大优化表数目
//		if (!success) {
//			success = checker.isSetMaxRegulates(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxRegulates(input, display);
//			}
//		}
//		// 设置最大磁盘空间
//		if (!success) {
//			success = checker.isSetMaxSize(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxSize(input, display);
//			}
//		}
//		// 设置最大HOME子域集群数目
//		if (!success) {
//			success = checker.isSetMaxGroups(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxGroups(input, display);
//			}
//		}
//		// 设置最大CALL网关节点数目
//		if (!success) {
//			success = checker.isSetMaxGateways(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxGateways(input, display);
//			}
//		}
//		// BUILD节点数目
//		if (!success) {
//			success = checker.isSetMaxBuilders(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxBuilders(input, display);
//			}
//		}
//		// WORK节点数目
//		if (!success) {
//			success = checker.isSetMaxWorkers(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxWorkers(input, display);
//			}
//		}
//		// 用户账号到期
//		if (!success) {
//			success = checker.isSetExpireTime(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetExpireTime(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isSetMiddleBuffer(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMiddleBuffer(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isSetCloudSize(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetCloudSize(input, display);
//			}
//		}
//
//		// 设置最大表数目
//		if (!success) {
//			success = checker.isSetMaxTables(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxTables(input, display);
//			}
//		}
//		// 设置一个表最大索引数目
//		if (!success) {
//			success = checker.isSetMaxIndexes(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxIndexes(input, display);
//			}
//		}
//		// 设置DSM表最大压缩倍数
//		if (!success) {
//			success = checker.isSetMaxDSMReduce(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSetMaxDSMReduce(input, display);
//			}
//		}
//
//		// 开放/关闭数据库资源
//		if (!success) {
//			success = checker.isOpenShareSchema(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doOpenShareSchema(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isCloseShareSchema(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCloseShareSchema(input, display);
//			}
//		}
//		// 开放/关闭数据表资源
//		if (!success) {
//			success = checker.isOpenShareTable(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doOpenShareTable(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isCloseShareTable(input);
//			pass = (success && confirm(auditor)); if(pass) {
//				doCloseShareTable(input, display);
//			}
//		}
//		// 授权人显示自己开放的数据资源
//		if (!success) {
//			success = checker.isShowOpenResource(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doShowOpenResource(input, display);
//			}
//		}
//		// 被授权人显示授权人开放的数据资源
//		if (!success) {
//			success = checker.isShowPassiveResource(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doShowPassiveResource(input, display);
//			}
//		}
//
//		// 检索在线命令
//		if (!success) {
//			success = checker.isSeekOnlineCommand(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSeekOnlineCommand(input, display);
//			}
//		}
//
//		// 并行流量测试
//		if (!success) {
//			success = checker.isParallelMultiSwarm(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doParallelMultiSwarm(input, display);
//			}
//		}
//		// 数据传输流量持续测试
//		if (!success) {
//			success = checker.isMultiSwarm(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doMultiSwarm(input, display);
//			}
//		}
//		// 数据传输流量测试
//		if (!success) {
//			success = checker.isSwarm(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSwarm(input, display);
//			}
//		}
//		// 释放节点内存
//		if (!success) {
//			success = checker.isReleaseMemory(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doReleaseMemory(input, display);
//			}
//		}
//		// 释放节点内存
//		if (!success) {
//			success = checker.isReleaseMemoryInterval(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doReleaseMemoryInterval(input, display);
//			}
//		}
//		//		// 重置包尺寸
//		//		if (!success) {
//		//			success = checker.isReplyPacketSize(input);
//		//			pass = (success && confirm(auditor));
//		//			if (pass) {
//		//				doReplyPacketSize(input, display);
//		//			}
//		//		}
//
//		// 设置应答包模式
//		if (!success) {
//			success = checker.isReplyPacketMode(input);
//			if (success && confirm(auditor)) {
//				doReplyPacketMode(input, display);
//			}
//		}
//		// 设置应答包尺寸
//		if (!success) {
//			success = checker.isReplyPacketSize(input);
//			if (success && confirm(auditor)) {
//				doReplyPacketSize(input, display);
//			}
//		}
//		// 设置发送超时
//		if (!success) {
//			success = checker.isReplySendTimeout(input);
//			if (success && confirm(auditor)) {
//				doReplySendTimeout(input, display);
//			}
//		}
//		// 设置接收超时
//		if (!success) {
//			success = checker.isReplyReceiveTimeout(input);
//			if (success && confirm(auditor)) {
//				doReplyReceiveTimeout(input, display);
//			}
//		}
//		// 设置数据流队列
//		if (!success) {
//			success = checker.isReplyFlowControl(input);
//			if (success && confirm(auditor)) {
//				doReplyFlowControl(input, display);
//			}
//		}
//
//		// 更新私有网络空间配置
//		if (!success) {
//			success = checker.isRefreshCyber(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doRefreshCyber(input, 0, display);
//			}
//		}
//		// 最大CPU占比
//		if (!success) {
//			success = checker.isMostCPU(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doMostCPU(input, display);
//			}
//		}
//		// 最大虚拟机内存占比
//		if (!success) {
//			success = checker.isMostVMMemory(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doMostVMMemory(input, display);
//			}
//		}
//		// 最少内存限制
//		if (!success) {
//			success = checker.isLeastMemory(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doLeastMemory(input, display);
//			}
//		}
//		// 最少磁盘空间限制
//		if (!success) {
//			success = checker.isLeastDisk(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doLeastDisk(input, display);
//			}
//		}
//		// 节点设备目录
//		if (!success) {
//			success = checker.isCheckSitePath(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCheckSitePath(input, display);
//			}
//		}
//
//		// 启动连际容器服务
//		if (!success) {
//			success = checker.isRunTubService(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doRunTubService(input, display);
//			}
//		}
//		// 停止连际容器服务
//		if (!success) {
//			success = checker.isStopTubService(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doStopTubService(input, display);
//			}
//		}
//		// 显示运行中的容器服务
//		if (!success) {
//			success = checker.isPrintTubService(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doPrintTubService(input, display);
//			}
//		}
//		// 显示容器组件
//		if (!success) {
//			success = checker.isShowTubContainer(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doShowTubContainer(input, display);
//			}
//		}
//		// 检测边缘监听地址
//		if (!success) {
//			pass = success = checker.isCheckTubListener(input);
//			if (pass) {
//				doCheckTubListener(input, display);
//			}
//		}
//		// 分布处理超时
//		if (!success) {
//			pass = success = checker.isDistributedTimeout(input);
//			if (success) {
//				doSetDistributedTimeout(input, display);
//			}
//		}
//
//		// 打印本地组件
//		if (!success) {
//			pass = success = checker.isCheckLocalTask(input);
//			if (success) {
//				doCheckLocalTask(input, display);
//			}
//		}
//		// 显示连接节点
//		if (!success) {
//			pass = success = checker.isCheckRemoteSite(input);
//			if (success) {
//				doCheckRemoteSite(input, display);
//			}
//		}
//
//		// 最大异步缓存尺寸
//		if (!success) {
//			pass = success = checker.isMaxLogElements(input);
//			if (success) {
//				doSetMaxLogElements(input, display);
//			}
//		}
//		// 开放TIGGER类型
//		if (!success) {
//			success = checker.isOpenTigger(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doOpenTigger(input, display);
//			}
//		}
//		// 关闭TIGGER类型
//		if (!success) {
//			success = checker.isCloseTigger(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCloseTigger(input, display);
//			}
//		}
//		// 检索云应用
//		if (!success) {
//			success = checker.isSeekCloudWare(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doSeekCloudWare(input, display);
//			}
//		}
//
//		// 生成包
//		if (!success) {
//			success = checker.isBuildConductPackage(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doBuildConductPackage(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isBuildEstablishPackage(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doBuildEstablishPackage(input, display);
//			}
//		}
//
//		if (!success) {
//			success = checker.isBuildContactPackage(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doBuildContactPackage(input, display);
//			}
//		}
//
//		// 发布包
//		if (!success) {
//			success = checker.isDeployConductPackage(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDeployConductPackage(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isDeployEstablishPackage(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDeployEstablishPackage(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isDeployContactPackage(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDeployContactPackage(input, display);
//			}
//		}
//		// 删除包
//		if (!success) {
//			success = checker.isDropConductPackage(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDropConductPackage(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isDropEstablishPackage(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDropEstablishPackage(input, display);
//			}
//		}
//		if (!success) {
//			success = checker.isDropContactPackage(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDropContactPackage(input, display);
//			}
//		}
//		// 运行分布应用，转发处理
//		if (!success) {
//			pass = success = checker.isRunTask(input);
//			if (success) {
//				doRunTask(input, auditor, display);
//			}
//		}
//
//		// 建立目录
//		if (!success) {
//			success = checker.isCreateCloudDirectory(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doCreateCloudDirectory(input, display);
//			}
//		}
//		// 删除目录
//		if (!success) {
//			success = checker.isDropCloudDirectory(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDropCloudDirectory(input, display);
//			}
//		}
//		// 删除目录
//		if (!success) {
//			success = checker.isDropCloudFile(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDropCloudFile(input, display);
//			}
//		}
//		// 修改目录名
//		if (!success) {
//			success = checker.isRenameCloudDirectory(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doRenameCloudDirectory(input, display);
//			}
//		}
//		// 修改文件名
//		if (!success) {
//			success = checker.isRenameCloudFile(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doRenameCloudFile(input, display);
//			}
//		}
//		
//		// 上传文件
//		if (!success) {
//			success = checker.isUploadCloudFile(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doUploadCloudFile(input, display);
//			}
//		}
//		// 下载文件
//		if (!success) {
//			success = checker.isDownloadCloudFile(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doDownloadCloudFile(input, display);
//			}
//		}
//		// 扫描磁盘文件
//		if (!success) {
//			success = checker.isScanCloudDirectory(input);
//			pass = (success && confirm(auditor));
//			if (pass) {
//				doScanCloudDirectory(input, display);
//			}
//		}
//
//		// 判断是自定义命令
//		if (!success) {
//			success = CustomCreator.isCommand(input);
//			if (success) {
//				EchoInvoker invoker = CustomCreator.createInvoker(input);
//				// 如果是基于FrontCustomInvoker实例，设置它的调用器
//				if (Laxkit.isClassFrom(invoker, CustomDisplayInvoker.class)) {
//					((CustomDisplayInvoker) invoker).setDisplay(display);
//				}
//
//				// 确认后执行
//				if (confirm(auditor)) {
//					launch(invoker);
//					pass = true;
//				}
//
//				//				// 判断是终端或者控制台，显示不同的提示
//				//				if (launcher.isTerminal()) {
//				//					// 参数见invoker.xml文件
//				//					String title = RayInvoker.getXMLAttribute("CUSTOM-COMMAND/MESSAGE-BOX/title");
//				//					if (confirm(title, auditor)) {
//				//						launch(invoker);
//				//						pass = true;
//				//					}
//				//				} else {
//				//					if (confirm(auditor)) {
//				//						launch(invoker);
//				//						pass = true;
//				//					}
//				//				}
//			}
//		}
//
//		// 以上不成功，命令未知
//		if (!success) {
//			String text = getRayLauncher().fault(FaultTip.ILLEGAL_COMMAND);
//			fault(text, display);
//			return false;
//		}
//
//		// 通过且命令追踪接口有效时，记录这个命令
//		if (pass) {
//			Tigger.command(input);
//		}
//
//		return pass;
//	}

//	/**
//	 * 异步调用器转发给管理池处理
//	 * @param invoker
//	 */
//	private void launch(EchoInvoker invoker) {
//		getInvokerPool().launch(invoker);
//	}

	/**
	 * 异步调用器转发给管理池处理
	 * @param invoker
	 */
	private boolean launch(EchoInvoker invoker, MeetDisplay display) {
		if (display != null) {
			if (Laxkit.isClassFrom(invoker, RayInvoker.class)) {
				((RayInvoker) invoker).setDisplay(display);
			}
		}
		// 启动
		return getInvokerPool().launch(invoker);
	}
	
	
//	/**
//	 * 建立数据库
//	 */
//	private void doCreateSchema(String input, MeetDisplay display) {
//		CreateSchemaParser parser = new CreateSchemaParser();
//		CreateSchema cmd = parser.split(input, true);
//		RayCreateSchemaInvoker invoker = new RayCreateSchemaInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 删除数据库。<br>
//	 * 根据名称解释，被删除的数据库，是当前账号下的一个或者全部数据库，以及数据库下的分布在集群中的全部数据记录。
//	 * @param input 输入语句
//	 */
//	private void doDropSchema(String input, MeetDisplay display) {
//		// 解析数据库名称
//		DropSchemaParser parser = new DropSchemaParser();
//		DropSchema cmd = parser.split(input, true);
//		// 生成异步调用器，发送异步命令
//		RayDropSchemaInvoker invoker = new RayDropSchemaInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 诊断数据库存在。<br>
//	 * @param input 输入语句
//	 */
//	private void doAssertSchema(String input, MeetDisplay display) {
//		// 解析数据库名称
//		AssertSchemaParser parser = new AssertSchemaParser();
//		AssertSchema cmd = parser.split(input);
//		// 生成异步调用器，发送异步命令
//		RayAssertSchemaInvoker invoker = new RayAssertSchemaInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示数据库配置
//	 * @param input 输入语句
//	 */
//	private void doShowSchema(String input, MeetDisplay display) {
//		ShowSchemaParser parser = new ShowSchemaParser();
//		ShowSchema cmd = parser.split(input, true);
//		RayShowSchemaInvoker invoker = new RayShowSchemaInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示数据库配置
//	 * @param input 输入语句
//	 */
//	private void doPrintSchemaDiagram(String input, MeetDisplay display) {
//		PrintSchemaDiagramParser parser = new PrintSchemaDiagramParser();
//		PrintSchemaDiagram cmd = parser.split(input, true);
//		RayPrintSchemaDiagramInvoker invoker = new RayPrintSchemaDiagramInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示数据库状态
//	 * @param input 输入语句
//	 */
//	private void doPrintTableDiagram(String input, MeetDisplay display) {
//		PrintTableDiagramParser parser = new PrintTableDiagramParser();
//		PrintTableDiagram cmd = parser.split(input, true);
//		RayPrintTableDiagramInvoker invoker = new RayPrintTableDiagramInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 建立一个用户账号
//	 * @param input 输入语句
//	 */
//	private void doCreateUser(String input, MeetDisplay display) {
//		CreateUserParser parser = new CreateUserParser();
//		CreateUser user = parser.split(input, true);
//		RayCreateUserInvoker invoker = new RayCreateUserInvoker(user);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 删除账号以及账号下所有配置权限、数据库等
//	 * @param input 输入语句
//	 */
//	private void doDropUser(String input, MeetDisplay display) {
//		DropUserParser parser = new DropUserParser();
//		DropUser cmd = parser.split(input, true);
//		RayDropUserInvoker invoker = new RayDropUserInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 修改账号密码
//	 * @param input 输入语句
//	 */
//	private void doAlterUser(String input, MeetDisplay display) {
//		AlterUserParser parser = new AlterUserParser();
//		AlterUser cmd = parser.split(input, true);
//		RayAlterUserInvoker invoker = new RayAlterUserInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 判断用户名称
//	 * @param input 输入语句
//	 */
//	private void doAssertUser(String input, MeetDisplay display) {
//		AssertUserParser parser = new AssertUserParser();
//		AssertUser cmd = parser.split(input);
//		RayAssertUserInvoker invoker = new RayAssertUserInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示注册用户状态
//	 * @param input 输入语句
//	 */
//	private void doPrintUserDiagram(String input, MeetDisplay display) {
//		PrintUserDiagramParser parser = new PrintUserDiagramParser();
//		PrintUserDiagram cmd = parser.split(input);
//		RayPrintUserDiagramInvoker invoker = new RayPrintUserDiagramInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 禁用用户账号
//	 * @param input 输入语句
//	 */
//	private void doCloseUser(String input, MeetDisplay display) {
//		CloseUserParser parser = new CloseUserParser();
//		CloseUser cmd = parser.split(input, true);
//		RayCloseUserInvoker invoker = new RayCloseUserInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 开放用户账号
//	 * @param input 输入语句
//	 */
//	private void doOpenUser(String input, MeetDisplay display) {
//		OpenUserParser parser = new OpenUserParser();
//		OpenUser cmd = parser.split(input, true);
//		RayOpenUserInvoker invoker = new RayOpenUserInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 给注册用户授权
//	 * @param input 输入语句
//	 */
//	private void doGrant(String input, MeetDisplay display) {
//		GrantParser parser = new GrantParser();
//		Grant grant = parser.split(input, true);
//		RayGrantInvoker invoker = new RayGrantInvoker(grant);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 解除给注册用户的授权
//	 * @param input 输入语句
//	 */
//	private void doRevoke(String input, MeetDisplay display) {
//		RevokeParser parser = new RevokeParser();
//		Revoke revoke = parser.split(input, true);
//		RayRevokeInvoker invoker = new RayRevokeInvoker(revoke);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示注册用户授权
//	 * @param input 输入语句
//	 */
//	private void doPrintGrantDiagram(String input, MeetDisplay display) {
//		PrintGrantDiagramParser parser = new PrintGrantDiagramParser();
//		PrintGrantDiagram cmd = parser.split(input);
//		RayPrintGrantDiagramInvoker invoker = new RayPrintGrantDiagramInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置数据表的数据块尺寸
//	 * @param input 输入语句
//	 */
//	private void doSetEntitySize(String input, MeetDisplay display) {
//		SetEntitySizeParser parser = new SetEntitySizeParser();
//		SetEntitySize cmd = parser.split(input, true);
//		RaySetEntitySizeInvoker invoker = new RaySetEntitySizeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 建立数据库表
//	 * @param input 输入语句
//	 */
//	private void doCreateTable(String input, MeetDisplay display) {
//		CreateTableParser parser = new CreateTableParser();
//		CreateTable cmd = parser.split(input, true);
//		// 设置当前持有人签名
//		FrontSite local = StaffOnFrontPool.getFrontLauncher().getSite();
//		cmd.setIssuer(local.getUser().getUsername());
//		// 启动任务
//		RayCreateTableInvoker invoker = new RayCreateTableInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示表配置参数
//	 * @param input 输入语句
//	 */
//	private void doShowTable(String input, MeetDisplay display) {
//		ShowTableParser parser = new ShowTableParser();
//		ShowTable cmd = parser.split(input, true);
//		RayShowTableInvoker invoker = new RayShowTableInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 检测远程表配置
//	 * @param input 输入语句
//	 */
//	private void doCheckRemoteTable(String input, MeetDisplay display) {
//		CheckRemoteTableParser parser = new CheckRemoteTableParser();
//		CheckRemoteTable cmd = parser.split(input, true);
//		RayCheckRemoteTableInvoker invoker = new RayCheckRemoteTableInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 检测远程组件配置
//	 * @param input 输入语句
//	 */
//	private void doCheckRemoteTask(String input, MeetDisplay display) {
//		CheckRemoteTaskParser parser = new CheckRemoteTaskParser();
//		CheckRemoteTask cmd = parser.split(input, true);
//		RayCheckRemoteTaskInvoker invoker = new RayCheckRemoteTaskInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 删除数据表
//	 * @param input 输入语句
//	 */
//	private void doDropTable(String input, MeetDisplay display) {
//		DropTableParser parser = new DropTableParser();
//		DropTable cmd = parser.split(input, true);
//		RayDropTableInvoker invoker = new RayDropTableInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 扫描数据表资源
//	 * @param input 输入语句
//	 */
//	private void doScanTable(String input, MeetDisplay display) {
//		ScanTableParser parser = new ScanTableParser();
//		ScanTable cmd = parser.split(input, true);
//		RayScanTableInvoker invoker = new RayScanTableInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 扫描数据库资源（一个数据库下面包括任意多个表）
//	 * @param input 输入语句
//	 */
//	private void doScanSchema(String input, MeetDisplay display) {
//		ScanSchemaParser parser = new ScanSchemaParser();
//		ScanSchema cmd = parser.split(input, true);
//		RayScanSchemaInvoker invoker = new RayScanSchemaInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 解析/执行JOIN语句
//	 * @param input 输入语句
//	 */
//	private void join(String input, MeetDisplay display) {
//		JoinParser parser = new JoinParser();
//		Join join = parser.split(input);
//		RayJoinInvoker invoker = new RayJoinInvoker(join);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 执行分布计算操作
//	 * @param input 输入语句
//	 */
//	private void doConduct(String input, MeetDisplay display) {
//		ConductParser parser = new ConductParser();
//		Conduct conduct = parser.split(input, true);
//		RayConductInvoker invoker = new RayConductInvoker(conduct);
//		invoker.setDisplay(display);launch(invoker);
//	}
//	
//	/**
//	 * 执行分布计算操作
//	 * @param conduct 分布计算
//	 */
//	private void doConduct(Conduct conduct, MeetDisplay display) {
//		RayConductInvoker invoker = new RayConductInvoker(conduct);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 运行快速计算
//	 * @param input 输入语句
//	 */
//	private void doContact(String input, MeetDisplay display) {
//		ContactParser parser = new ContactParser();
//		Contact cmd = parser.split(input, true);
//		RayContactInvoker invoker = new RayContactInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 运行快速计算
//	 * @param cmd 快速计算
//	 */
//	private void doContact(Contact cmd, MeetDisplay display) {
//		RayContactInvoker invoker = new RayContactInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//	
//	/**
//	 * 执行分布数据构建
//	 * @param input ESTABLISH命令语句
//	 */
//	private void doEstablish(String input, MeetDisplay display) {
//		EstablishParser parser = new EstablishParser();
//		Establish estab = parser.split(input, true);
//		RayEstablishInvoker invoker = new RayEstablishInvoker(estab);
//		invoker.setDisplay(display);launch(invoker);
//	}
//	
//	/**
//	 * 执行分布数据构建
//	 * @param input ESTABLISH命令语句
//	 */
//	private void doEstablish(Establish establish, MeetDisplay display) {
//		RayEstablishInvoker invoker = new RayEstablishInvoker(establish);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * SQL "INJECT INTO ... SELCT"异步操作
//	 * @param input 输入语句
//	 */
//	private void doInjectSelect(String input, MeetDisplay display) {
//		InjectSelectParser parser = new InjectSelectParser();
//		InjectSelect cmd = parser.split(input, true);
//		RayInjectSelectInvoker invoker = new RayInjectSelectInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * SELECT异步操作。
//	 * @param input 输入语句
//	 */
//	private void doSelect(String input, MeetDisplay display) {
//		SelectParser parser = new SelectParser();
//		Select select = parser.split(input, true);
//		RaySelectInvoker invoker = new RaySelectInvoker(select);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * SQL.DELETE异步操作
//	 * @param input 输入语句
//	 */
//	private void doDelete(String input, MeetDisplay display) {
//		DeleteParser parser = new DeleteParser();
//		Delete delete = parser.split(input, true);
//		RayDeleteInvoker invoker = new RayDeleteInvoker(delete);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 执行SQL.UPDATE命令的异步操作
//	 * @param input 输入语句
//	 */
//	private void doUpdate(String input, MeetDisplay display) {
//		UpdateParser parser = new UpdateParser();
//		Update update = parser.split(input, true);
//		RayUpdateInvoker invoker = new RayUpdateInvoker(update);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * SQL "INSERT INTO"异步操作
//	 * @param input 输入语句
//	 */
//	private void doInsert(String input, MeetDisplay display) {
//		InsertParser parser = new InsertParser();
//		Insert insert = parser.splitInsert(input, true);
//		RayInsertInvoker invoker = new RayInsertInvoker(insert);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * SQL "INJECT INTO"异步操作
//	 * @param input 输入语句
//	 */
//	private void doInject(String input, MeetDisplay display) {
//		InsertParser parser = new InsertParser();
//		Insert insert = parser.splitInject(input, true);
//		RayInsertInvoker invoker = new RayInsertInvoker(insert);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 数据块强制转化命令（只允许管理员操作）
//	 * @param input 输入语句
//	 */
//	private void doRush(String input, MeetDisplay display) {
//		RushParser parser = new RushParser();
//		Rush cmd = parser.split(input);
//		RayRushInvoker invoker = new RayRushInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 数据优化命令（普通注册用户操作）
//	 * @param input 输入语句
//	 */
//	private void doRegulate(String input, MeetDisplay display) {		
//		RegulateParser parser = new RegulateParser();
//		Regulate cmd = parser.split(input);
//		RayRegulateInvoker invoker = new RayRegulateInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 数据调整命令（普通注册用户操作）
//	 * @param input 字符串命令
//	 */
//	private void doModulate(String input, MeetDisplay display) {
//		ModulateParser parser = new ModulateParser();
//		Modulate cmd = parser.split(input);
//		RayModulateInvoker invoker = new RayModulateInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 检查表数据一致
//	 * @param input 字符串命令
//	 */
//	private void doCheckEntityConsistency(String input, MeetDisplay display) {
//		CheckEntityConsistencyParser parser = new CheckEntityConsistencyParser();
//		CheckEntityConsistency cmd = parser.split(input, true);
//		RayCheckEntityConsistencyInvoker invoker = new RayCheckEntityConsistencyInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 修复表数据一致
//	 * @param input 字符串命令
//	 */
//	private void doRecoverEntityConsistency(String input, MeetDisplay display) {
//		RecoverEntityConsistencyParser parser = new RecoverEntityConsistencyParser();
//		RecoverEntityConsistency cmd = parser.split(input, true);
//		RayRecoverEntityConsistencyInvoker invoker = new RayRecoverEntityConsistencyInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 检查表分布数据容量
//	 * @param input 输入语句
//	 */
//	private void doScanSketch(String input, MeetDisplay display) {
//		ScanSketchParser parser = new ScanSketchParser();
//		ScanSketch cmd = parser.split(input, true);
//		RayScanSketchInvoker invoker = new RayScanSketchInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 建立数据优化时间（普通注册用户操作）
//	 * @param input 输入语句
//	 */
//	private void doCreateRegulateTime(String input, MeetDisplay display) {
//		CreateRegulateTimeParser parser = new CreateRegulateTimeParser();
//		CreateRegulateTime cmd = parser.split(input);
//		RayCreateRegulateTimeInvoker invoker = new RayCreateRegulateTimeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 撤销数据优化时间（普通注册用户操作）
//	 * @param input 输入语句
//	 */
//	private void doDropRegulateTime(String input, MeetDisplay display) {
//		DropRegulateTimeParser parser = new DropRegulateTimeParser();
//		DropRegulateTime cmd = parser.split(input, true);
//		RayDropRegulateTimeInvoker invoker = new RayDropRegulateTimeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 打印数据优化时间（普通注册用户操作）
//	 * @param input 输入语句
//	 */
//	private void doPrintRegulateTime(String input, MeetDisplay display) {
//		PrintRegulateTimeParser parser = new PrintRegulateTimeParser();
//		PrintRegulateTime cmd = parser.split(input, true);
//		RayPrintRegulateTimeInvoker invoker = new RayPrintRegulateTimeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 分派到DATA站点加载索引
//	 * @param input 输入语句
//	 */
//	private void doLoadIndex(String input, MeetDisplay display) {
//		// 检查用户操作权限
//		Account account = getRayLauncher().getStaffPool().getAccount();
//		if (!account.canLoadIndex()) {
//			String text = getRayLauncher().fault(FaultTip.PERMISSION_MISSING);
//			fault(text, display);
//			return;
//		}
//
//		LoadIndexParser parser = new LoadIndexParser();
//		LoadIndex cmd = parser.split(input, true);
//		RayLoadIndexInvoker invoker = new RayLoadIndexInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 从DATA站点卸载索引
//	 * @param input 输入语句
//	 */
//	private void doStopIndex(String input, MeetDisplay display) {
//		// 检查用户操作权限
//		Account account = getRayLauncher().getStaffPool().getAccount();
//		if (!account.canLoadIndex()) {
//			String text = getRayLauncher().fault(FaultTip.PERMISSION_MISSING);
//			fault(text, display);
//			return;
//		}
//
//		StopIndexParser parser = new StopIndexParser();
//		StopIndex cmd = parser.split(input, true);
//		RayStopIndexInvoker invoker = new RayStopIndexInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 分派到DATA站点加载数据块
//	 * @param input 输入语句
//	 */
//	private void doLoadEntity(String input, MeetDisplay display) {
//		// 检查用户操作权限
//		Account account = getRayLauncher().getStaffPool().getAccount();
//		if (!account.canLoadEntity()) {
//			String text = getRayLauncher().fault(FaultTip.PERMISSION_MISSING);
//			fault(text, display);
//			return;
//		}
//
//		LoadEntityParser parser = new LoadEntityParser();
//		LoadEntity cmd = parser.split(input, true);
//		RayLoadEntityInvoker invoker = new RayLoadEntityInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 从DATA站点卸载数据块
//	 * @param input 输入语句
//	 */
//	private void doStopEntity(String input, MeetDisplay display) {
//		// 检查用户操作权限
//		Account account = getRayLauncher().getStaffPool().getAccount();
//		if (!account.canLoadEntity()) {
//			String text = getRayLauncher().fault(FaultTip.PERMISSION_MISSING);
//			fault(text, display);
//			return;
//		}
//
//		StopEntityParser parser = new StopEntityParser();
//		StopEntity cmd = parser.split(input, true);
//		RayStopEntityInvoker invoker = new RayStopEntityInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示某个表下的数据块尺寸
//	 * @param input 语句语句
//	 */
//	private void doShowEntitySize(String input, MeetDisplay display) {
//		ShowEntitySizeParser parser = new ShowEntitySizeParser();
//		ShowEntitySize cmd = parser.split(input, true);
//		RayShowEntitySizeInvoker invoker = new RayShowEntitySizeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 扫描数据块。流程：FRONT -> CALL -> DATA。
//	 * @param input 输入语句
//	 */
//	private void doScanEntity(String input, MeetDisplay display) {
//		ScanEntityParser parser = new ScanEntityParser();
//		ScanEntity cmd = parser.split(input, true);
//		RayScanEntityInvoker invoker = new RayScanEntityInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 获取数据块编号。流程：FRONT -> CALL -> DATA（MASTER SITE）。
//	 * @param input 输入语句
//	 */
//	private void doGitStubs(String input, MeetDisplay display) {
//		GitStubsParser parser = new GitStubsParser();
//		GitStubs cmd = parser.split(input, true);
//		RayGitStubsInvoker invoker = new RayGitStubsInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 获取数据块编号。流程：FRONT -> CALL -> DATA（MASTER SITE）。
//	 * @param input 输入语句
//	 */
//	private void doPrintStubsDiagram(String input, MeetDisplay display) {
//		PrintStubsDiagramParser parser = new PrintStubsDiagramParser();
//		PrintStubsDiagram cmd = parser.split(input, true);
//		RayPrintStubsDiagramInvoker invoker = new RayPrintStubsDiagramInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 获取数据块数据。流程：FRONT -> CALL -> DATA（MASTER SITE）。
//	 * @param input 输入语句
//	 */
//	private void doExportEntity(String input, MeetDisplay display) {
//		ExportEntityParser parser = new ExportEntityParser();
//		ExportEntity cmd = parser.split(input, true);
//		RayExportEntityInvoker invoker = new RayExportEntityInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 将文件中的数据导入集群。流程：FRONT -> CALL -> DATA（MASTER SITE）。
//	 * @param input 输入语句
//	 */
//	private void doImportEntity(String input, MeetDisplay display) {
//		ImportEntityParser parser = new ImportEntityParser();
//		ImportEntity cmd = parser.split(input, true);
//		RayImportEntityInvoker invoker = new RayImportEntityInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 获取数据块数据。流程：FRONT -> CALL -> DATA（MASTER SITE）。
//	 * @param input 输入语句
//	 */
//	private void doCopyEntity(String input, MeetDisplay display) {
//		CopyEntityParser parser = new CopyEntityParser();
//		CopyEntity cmd = parser.split(input, true);
//		RayCopyEntityInvoker invoker = new RayCopyEntityInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 检测文件内容正确性
//	 * @param input 输入语句
//	 */
//	private void doCheckEntityContent(String input, MeetDisplay display) {
//		CheckEntityContentParser parser = new CheckEntityContentParser();
//		CheckEntityContent cmd = parser.split(input, true);
//		RayCheckEntityContentInvoker invoker = new RayCheckEntityContentInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 判断文件编码类型
//	 * @param input 输入语句
//	 */
//	private void doCheckEntityCharset(String input, MeetDisplay display) {
//		CheckEntityCharsetParser parser = new CheckEntityCharsetParser();
//		CheckEntityCharset cmd = parser.split(input, true);
//		RayCheckEntityCharsetInvoker invoker = new RayCheckEntityCharsetInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 计算散列码（在本地进行）
//	 * @param input 输入语句
//	 */
//	private void doBuildHash(String input, MeetDisplay display) {
//		BuildHashParser parser = new BuildHashParser();
//		BuildHash cmd = parser.split(input);
//		RayBuildHashInvoker invoker = new RayBuildHashInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 半截符编码/解码（在本地进行）
//	 * @param input 输入语句
//	 */
//	private void doBuildHalf(String input, MeetDisplay display) {
//		BuildHalfParser parser = new BuildHalfParser();
//		BuildHalf cmd = parser.split(input);
//		RayBuildHalfInvoker invoker = new RayBuildHalfInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * EACH签名（在本地进行）
//	 * @param input 输入语句
//	 */
//	private void doBuildEach(String input, MeetDisplay display) {
//		BuildEachParser parser = new BuildEachParser();
//		BuildEach cmd = parser.split(input);
//		RayBuildEachInvoker invoker = new RayBuildEachInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置命令处理模式（在本地进行）
//	 * @param input 输入语句
//	 */
//	private void doSetCommandMode(String input, MeetDisplay display) {
//		CommandModeParser parser = new CommandModeParser();
//		CommandMode cmd = parser.split(input);
//		RayCommandModeInvoker invoker = new RayCommandModeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置命令超时
//	 * @param input 输入语句
//	 */
//	private void doSetCommandTimeout(String input, MeetDisplay display) {
//		CommandTimeoutParser parser = new CommandTimeoutParser();
//		CommandTimeout cmd = parser.split(input);
//		RayCommandTimeoutInvoker invoker = new RayCommandTimeoutInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置密文超时
//	 * @param input 输入语句
//	 */
//	private void doSetCipherTimeout(String input, MeetDisplay display) {
//		CipherTimeoutParser parser = new CipherTimeoutParser();
//		CipherTimeout cmd = parser.split(input);
//		RayCipherTimeoutInvoker invoker = new RayCipherTimeoutInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 分布处理超时
//	 * @param input 输入语句
//	 */
//	private void doSetDistributedTimeout(String input, MeetDisplay display) {
//		DistributedTimeoutParser parser = new DistributedTimeoutParser();
//		DistributedTimeout cmd = parser.split(input);
//		RayDistributedTimeoutInvoker invoker = new RayDistributedTimeoutInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 打印本地组件
//	 * @param input 输入语句
//	 */
//	private void doCheckLocalTask(String input, MeetDisplay display) {
//		CheckLocalTaskParser parser = new CheckLocalTaskParser();
//		CheckLocalTask cmd = parser.split(input);
//		RayCheckLocalTaskInvoker invoker = new RayCheckLocalTaskInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示连接节点
//	 * @param input 输入语句
//	 */
//	private void doCheckRemoteSite(String input, MeetDisplay display) {
//		CheckRemoteSiteParser parser = new CheckRemoteSiteParser();
//		CheckRemoteSite cmd = parser.split(input);
//		RayCheckRemoteSiteInvoker invoker = new RayCheckRemoteSiteInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置调用器数目
//	 * @param input 输入语句
//	 */
//	private void doSetMaxInvoker(String input, MeetDisplay display) {
//		MaxInvokerParser parser = new MaxInvokerParser();
//		MaxInvoker cmd = parser.split(input);
//		RayMaxInvokerInvoker invoker = new RayMaxInvokerInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 设置异步缓存
//	 * @param input 输入语句
//	 */
//	private void doSetMaxEchoBuffer(String input, MeetDisplay display) {
//		MaxEchoBufferParser parser = new MaxEchoBufferParser();
//		MaxEchoBuffer cmd = parser.split(input);
//		RayMaxEchoBufferInvoker invoker = new RayMaxEchoBufferInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 检测服务器系统信息
//	 * @param input 输入语句
//	 */
//	private void doCheckSystemInfo(String input, MeetDisplay display) {
//		CheckSystemInfoParser parser = new CheckSystemInfoParser();
//		CheckSystemInfo cmd = parser.split(input);
//		RayCheckSystemInfoInvoker invoker = new RayCheckSystemInfoInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 检测内网穿透信道
//	 * @param input 输入语句
//	 */
//	private void doCheckPockChannel(String input, MeetDisplay display) {
//		CheckPockChannelParser parser = new CheckPockChannelParser();
//		CheckPockChannel cmd = parser.split(input);
//		RayCheckPockChannelInvoker invoker = new RayCheckPockChannelInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//	
//	/**
//	 * 检测支持MASSIVE MIMO
//	 * @param input 输入语句
//	 */
//	private void doCheckMassiveMimo(String input, MeetDisplay display) {
//		CheckMassiveMimoParser parser = new CheckMassiveMimoParser();
//		CheckMassiveMimo cmd = parser.split(input);
//		RayCheckMassiveMimoInvoker invoker = new RayCheckMassiveMimoInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置密文超时
//	 * @param input 输入语句
//	 */
//	private void doSetMaxLogElements(String input, MeetDisplay display) {
//		MaxLogElementsParser parser = new MaxLogElementsParser();
//		MaxLogElements cmd = parser.split(input);
//		RayMaxLogElementsInvoker invoker = new RayMaxLogElementsInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 打印FRONT网关
//	 * @param input 输入语句
//	 */
//	private void doSetPrintFrontGateway(String input, MeetDisplay display) {
//		PrintFrontGatewayParser parser = new PrintFrontGatewayParser();
//		PrintFrontGateway cmd = parser.split(input);
//		RayPrintFrontGatewayInvoker invoker = new RayPrintFrontGatewayInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 建立限制操作
//	 * @param input 输入语句
//	 */
//	private void doCreateLimit(String input, MeetDisplay display) {		
//		CreateLimitParser parser = new CreateLimitParser();
//		CreateLimit cmd = parser.split(input, true);
//		RayCreateLimitInvoker invoker = new RayCreateLimitInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 删除限制操作
//	 * @param input 输入语句
//	 */
//	private void doDropLimit(String input, MeetDisplay display) {		
//		DropLimitParser parser = new DropLimitParser();
//		DropLimit cmd = parser.split(input, true);
//		RayDropLimitInvoker invoker = new RayDropLimitInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示限制操作单元
//	 * @param input 输入语句
//	 */
//	private void doShowLimit(String input, MeetDisplay display) {
//		ShowLimitParser parser = new ShowLimitParser();
//		ShowLimit cmd = parser.split(input);
//		RayShowLimitInvoker invoker = new RayShowLimitInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 提交锁定操作
//	 * @param input 输入语句
//	 */
//	private void doCreateFault(String input, MeetDisplay display) {
//		CreateFaultParser parser = new CreateFaultParser();
//		CreateFault cmd = parser.split(input, true);
//		RayCreateFaultInvoker invoker = new RayCreateFaultInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 撤销锁定操作
//	 * @param input 输入语句
//	 */
//	private void doDropFault(String input, MeetDisplay display) {
//		DropFaultParser parser = new DropFaultParser();
//		DropFault cmd = parser.split(input, true);
//		RayDropFaultInvoker invoker = new RayDropFaultInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示锁定操作
//	 * @param input 输入语句
//	 */
//	private void doShowFault(String input, MeetDisplay display) {
//		ShowFaultParser parser = new ShowFaultParser();
//		ShowFault cmd = parser.split(input);
//		RayShowFaultInvoker invoker = new RayShowFaultInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示禁止操作单元
//	 * @param input 输入语句
//	 */
//	private void doShowForbid(String input, MeetDisplay display) {
//		ShowForbidParser parser = new ShowForbidParser();
//		ShowForbid cmd = parser.split(input);
//		RayShowForbidInvoker invoker = new RayShowForbidInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示事务规则
//	 * @param input 输入语句
//	 */
//	private void doShowLockRule(String input, MeetDisplay display) {
//		ShowLockRuleParser parser = new ShowLockRuleParser();
//		ShowLockRule cmd = parser.split(input);
//		RayShowLockRuleInvoker invoker = new RayShowLockRuleInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置最大并行任务数
//	 * @param input 输入语句
//	 */
//	private void doSetMaxJobs(String input, MeetDisplay display) {
//		SetMaxJobsParser parser = new SetMaxJobsParser();
//		SetMaxJobs cmd = parser.split(input);
//		RaySetMaxJobsInvoker invoker = new RaySetMaxJobsInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置最大连接数
//	 * @param input 输入语句
//	 */
//	private void doSetMaxMembers(String input, MeetDisplay display) {
//		SetMaxMembersParser parser = new SetMaxMembersParser();
//		SetMaxMembers cmd = parser.split(input);
//		RaySetMaxMembersInvoker invoker = new RaySetMaxMembersInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置最多应用软件
//	 * @param input 输入语句
//	 */
//	private void doSetMaxTasks(String input, MeetDisplay display) {
//		SetMaxTasksParser parser = new SetMaxTasksParser();
//		SetMaxTasks cmd = parser.split(input);
//		RaySetMaxTasksInvoker invoker = new RaySetMaxTasksInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置优化表数目
//	 * @param input 输入语句
//	 */
//	private void doSetMaxRegulates(String input, MeetDisplay display) {
//		SetMaxRegulatesParser parser = new SetMaxRegulatesParser();
//		SetMaxRegulates cmd = parser.split(input);
//		RaySetMaxRegulatesInvoker invoker = new RaySetMaxRegulatesInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置磁盘空间尺寸
//	 * @param input 输入语句
//	 */
//	private void doSetMaxSize(String input, MeetDisplay display) {
//		SetMaxSizeParser parser = new SetMaxSizeParser();
//		SetMaxSize cmd = parser.split(input);
//		RaySetMaxSizeInvoker invoker = new RaySetMaxSizeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置HOME子域集群数目
//	 * @param input 输入语句
//	 */
//	private void doSetMaxGroups(String input, MeetDisplay display) {
//		SetMaxGroupsParser parser = new SetMaxGroupsParser();
//		SetMaxGroups cmd = parser.split(input);
//		RaySetMaxGroupsInvoker invoker = new RaySetMaxGroupsInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置CALL网关节点数目
//	 * @param input 输入语句
//	 */
//	private void doSetMaxGateways(String input, MeetDisplay display) {
//		SetMaxGatewaysParser parser = new SetMaxGatewaysParser();
//		SetMaxGateways cmd = parser.split(input);
//		RaySetMaxGatewaysInvoker invoker = new RaySetMaxGatewaysInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置WORK节点数目
//	 * @param input 输入语句
//	 */
//	private void doSetMaxWorkers(String input, MeetDisplay display) {
//		SetMaxWorkersParser parser = new SetMaxWorkersParser();
//		SetMaxWorkers cmd = parser.split(input);
//		RaySetMaxWorkersInvoker invoker = new RaySetMaxWorkersInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置BUILD节点数目
//	 * @param input 输入语句
//	 */
//	private void doSetMaxBuilders(String input, MeetDisplay display) {
//		SetMaxBuildersParser parser = new SetMaxBuildersParser();
//		SetMaxBuilders cmd = parser.split(input);
//		RaySetMaxBuildersInvoker invoker = new RaySetMaxBuildersInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置BUILD节点数目
//	 * @param input 输入语句
//	 */
//	private void doSetExpireTime(String input, MeetDisplay display) {
//		SetExpireTimeParser parser = new SetExpireTimeParser();
//		SetExpireTime cmd = parser.split(input);
//		RaySetExpireTimeInvoker invoker = new RaySetExpireTimeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置中间缓存尺寸
//	 * @param input 输入语句
//	 */
//	private void doSetMiddleBuffer(String input, MeetDisplay display) {
//		SetMiddleBufferParser parser = new SetMiddleBufferParser();
//		SetMiddleBuffer cmd = parser.split(input);
//		RaySetMiddleBufferInvoker invoker = new RaySetMiddleBufferInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//	/**
//	 * 设置云存储空间
//	 * @param input 输入语句
//	 */
//	private void doSetCloudSize(String input, MeetDisplay display) {
//		SetCloudSizeParser parser = new SetCloudSizeParser();
//		SetCloudSize cmd = parser.split(input);
//		RaySetCloudSizeInvoker invoker = new RaySetCloudSizeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置最大表数目
//	 * @param input 输入语句
//	 */
//	private void doSetMaxTables(String input, MeetDisplay display) {
//		SetMaxTablesParser parser = new SetMaxTablesParser();
//		SetMaxTables cmd = parser.split(input);
//		RaySetMaxTablesInvoker invoker = new RaySetMaxTablesInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置表最大索引数目
//	 * @param input 输入语句
//	 */
//	private void doSetMaxIndexes(String input, MeetDisplay display) {
//		SetMaxIndexesParser parser = new SetMaxIndexesParser();
//		SetMaxIndexes cmd = parser.split(input);
//		RaySetMaxIndexesInvoker invoker = new RaySetMaxIndexesInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * DSM表最大压缩倍数
//	 * @param input 输入语句
//	 */
//	private void doSetMaxDSMReduce(String input, MeetDisplay display) {
//		SetMaxDSMReduceParser parser = new SetMaxDSMReduceParser();
//		SetMaxDSMReduce cmd = parser.split(input);
//		RaySetMaxDSMReduceInvoker invoker = new RaySetMaxDSMReduceInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 开放数据库共享给其他用户
//	 * @param input 输入语句
//	 */
//	private void doOpenShareSchema(String input, MeetDisplay display) {
//		OpenShareSchemaParser parser = new OpenShareSchemaParser();
//		OpenShareSchema cmd = parser.split(input, true);
//		RayOpenShareSchemaInvoker invoker = new RayOpenShareSchemaInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 收回共享数据库
//	 * @param input 输入语句
//	 */
//	private void doCloseShareSchema(String input, MeetDisplay display) {
//		CloseShareSchemaParser parser = new CloseShareSchemaParser();
//		CloseShareSchema cmd = parser.split(input, true);
//		RayCloseShareSchemaInvoker invoker = new RayCloseShareSchemaInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 开放数据表共享给其他用户
//	 * @param input 输入语句
//	 */
//	private void doOpenShareTable(String input, MeetDisplay display) {
//		OpenShareTableParser parser = new OpenShareTableParser();
//		OpenShareTable cmd = parser.split(input, true);
//		RayOpenShareTableInvoker invoker = new RayOpenShareTableInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 收回共享数据表
//	 * @param input 输入语句
//	 */
//	private void doCloseShareTable(String input, MeetDisplay display) {
//		CloseShareTableParser parser = new CloseShareTableParser();
//		CloseShareTable cmd = parser.split(input, true);
//		RayCloseShareTableInvoker invoker = new RayCloseShareTableInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 授权人显示自己开放的数据资源
//	 * @param input 输入语句
//	 */
//	private void doShowOpenResource(String input, MeetDisplay display) {
//		ShowOpenResourceParser parser = new ShowOpenResourceParser();
//		ShowOpenResource cmd = parser.split(input);
//		RayShowOpenResourceInvoker invoker = new RayShowOpenResourceInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 被授权人显示授权人开放给自己的数据资源
//	 * @param input 输入语句
//	 */
//	private void doShowPassiveResource(String input, MeetDisplay display) {
//		ShowPassiveResourceParser parser = new ShowPassiveResourceParser();
//		ShowPassiveResource cmd = parser.split(input);
//		RayShowPassiveResourceInvoker invoker = new RayShowPassiveResourceInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 检索在线命令
//	 * @param input 输入语句
//	 */
//	private void doSeekOnlineCommand(String input, MeetDisplay display) {
//		SeekOnlineCommandParser parser = new SeekOnlineCommandParser();
//		SeekOnlineCommand cmd = parser.split(input);
//		RaySeekOnlineCommandInvoker invoker = new RaySeekOnlineCommandInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 并行数据传输流量测试
//	 * @param input 输入语句
//	 */
//	private void doParallelMultiSwarm(String input, MeetDisplay display) {
//		ParallelMultiSwarmParser parser = new ParallelMultiSwarmParser();
//		ParallelMultiSwarm cmd = parser.split(input); // 解析命令
//		RayParallelMultiSwarmInvoker invoker = new RayParallelMultiSwarmInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 数据传输流量持续测试
//	 * @param input 输入语句
//	 */
//	private void doMultiSwarm(String input, MeetDisplay display) {
//		MultiSwarmParser parser = new MultiSwarmParser();
//		MultiSwarm cmd = parser.split(input); // 解析命令
//		RayMultiSwarmInvoker invoker = new RayMultiSwarmInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 数据传输流量测试
//	 * @param input 输入语句
//	 */
//	private void doSwarm(String input, MeetDisplay display) {
//		SwarmParser parser = new SwarmParser();
//		Swarm cmd = parser.split(input, true); // 解析命令
//		RaySwarmInvoker invoker = new RaySwarmInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 释放节点内存
//	 * @param input 输入语句
//	 */
//	private void doReleaseMemory(String input, MeetDisplay display) {
//		ReleaseMemoryParser parser = new ReleaseMemoryParser();
//		ReleaseMemory cmd = parser.split(input);
//		RayReleaseMemoryInvoker invoker = new RayReleaseMemoryInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 节点内存释放间隔时间
//	 * @param input 输入语句
//	 */
//	private void doReleaseMemoryInterval(String input, MeetDisplay display) {
//		ReleaseMemoryIntervalParser parser = new ReleaseMemoryIntervalParser();
//		ReleaseMemoryInterval cmd = parser.split(input);
//		RayReleaseMemoryIntervalInvoker invoker = new RayReleaseMemoryIntervalInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	//	/**
//	//	 * 重置包尺寸
//	//	 * @param input 输入语句
//	//	 */
//	//	private void doReplyPacketSize(String input, MeetDisplay display) {
//	//		ReplyPacketSizeParser parser = new ReplyPacketSizeParser();
//	//		ReplyPacketSize cmd = parser.split(input);
//	//		RayReplyPacketSizeInvoker invoker = new RayReplyPacketSizeInvoker(cmd);
//	//		invoker.setDisplay(display);launch(invoker);
//	//	}
//
//	/**
//	 * 设置反馈包模式
//	 * @param input 输入语句
//	 */
//	private void doReplyPacketMode(String input, MeetDisplay display) {
//		ReplyPacketModeParser parser = new ReplyPacketModeParser();
//		ReplyPacketMode cmd = parser.split(input);
//		RayReplyPacketModeInvoker invoker = new RayReplyPacketModeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置反馈包尺寸
//	 * @param input 输入语句
//	 */
//	private void doReplyPacketSize(String input, MeetDisplay display) {
//		ReplyPacketSizeParser parser = new ReplyPacketSizeParser();
//		ReplyPacketSize cmd = parser.split(input);
//		RayReplyPacketSizeInvoker invoker = new RayReplyPacketSizeInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置发送异步数据超时
//	 * @param input 输入语句
//	 */
//	private void doReplySendTimeout(String input, MeetDisplay display) {
//		ReplySendTimeoutParser parser = new ReplySendTimeoutParser();
//		ReplySendTimeout cmd = parser.split(input);
//		RayReplySendTimeoutInvoker invoker = new RayReplySendTimeoutInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置接收异步数据超时
//	 * @param input 输入语句
//	 */
//	private void doReplyReceiveTimeout(String input, MeetDisplay display) {
//		ReplyReceiveTimeoutParser parser = new ReplyReceiveTimeoutParser();
//		ReplyReceiveTimeout cmd = parser.split(input);
//		RayReplyReceiveTimeoutInvoker invoker = new RayReplyReceiveTimeoutInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 设置异步应答流量控制参数
//	 * @param input 输入语句
//	 */
//	private void doReplyFlowControl(String input, MeetDisplay display) {
//		ReplyFlowControlParser parser = new ReplyFlowControlParser();
//		ReplyFlowControl cmd = parser.split(input);
//		RayReplyFlowControlInvoker invoker = new RayReplyFlowControlInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 更新私有网络空间
//	 * @param input 输入语句
//	 * @param delayTime 延时处理时间
//	 * @return 启动成功返回真，否则假
//	 */
//	private boolean doRefreshCyber(String input , long delayTime, MeetDisplay display) {
//		RefreshCyberParser parser = new RefreshCyberParser();
//		RefreshCyber cmd = parser.split(input);
//
//		// 判断当前任务队列为空
//		boolean empty = (getInvokerPool().size() == 0);
//		if (!empty) {
//			String text = getRayLauncher().fault(FaultTip.REFUSE_REFRESH_CYBER);
//			warning(text, display);
//			return false;
//		}
//
//		RayRefreshCyberInvoker invoker = new RayRefreshCyberInvoker(cmd);
//		invoker.setDelayTime(delayTime); // 延时处理时间
//		invoker.setDisplay(display);launch(invoker);
//		return true;
//	}
//
//	/**
//	 * 最大CPU限制
//	 * @param input 输入语句
//	 */
//	private void doMostCPU(String input, MeetDisplay display) {
//		MostCPUParser parser = new MostCPUParser();
//		MostCPU cmd = parser.split(input);
//		RayMostCPUInvoker invoker = new RayMostCPUInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 最大虚拟机限制
//	 * @param input 输入语句
//	 */
//	private void doMostVMMemory(String input, MeetDisplay display) {
//		MostVMMemoryParser parser = new MostVMMemoryParser();
//		MostVMMemory cmd = parser.split(input);
//		RayMostVMMemoryInvoker invoker = new RayMostVMMemoryInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 最低内存限制
//	 * @param input 输入语句
//	 */
//	private void doLeastMemory(String input, MeetDisplay display) {
//		LeastMemoryParser parser = new LeastMemoryParser();
//		LeastMemory cmd = parser.split(input);
//		RayLeastMemoryInvoker invoker = new RayLeastMemoryInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 最低磁盘空间限制
//	 * @param input 输入语句
//	 */
//	private void doLeastDisk(String input, MeetDisplay display) {
//		LeastDiskParser parser = new LeastDiskParser();
//		LeastDisk cmd = parser.split(input);
//		RayLeastDiskInvoker invoker = new RayLeastDiskInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 最低磁盘空间限制
//	 * @param input 输入语句
//	 */
//	private void doCheckSitePath(String input, MeetDisplay display) {
//		CheckSitePathParser parser = new CheckSitePathParser();
//		CheckSitePath cmd = parser.split(input);
//		RayCheckSitePathInvoker invoker = new RayCheckSitePathInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 启动边缘容器服务
//	 * @param input 输入语句
//	 */
//	private void doRunTubService(String input, MeetDisplay display) {
//		RunTubServiceParser parser = new RunTubServiceParser();
//		RunTubService cmd = parser.split(input, true);
//		RayRunTubServiceInvoker invoker = new RayRunTubServiceInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 停止边缘容器服务
//	 * @param input 输入语句
//	 */
//	private void doStopTubService(String input, MeetDisplay display) {
//		StopTubServiceParser parser = new StopTubServiceParser();
//		StopTubService cmd = parser.split(input, true);
//		RayStopTubServiceInvoker invoker = new RayStopTubServiceInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示边缘容器服务
//	 * @param input 输入语句
//	 */
//	private void doPrintTubService(String input, MeetDisplay display) {
//		PrintTubServiceParser parser = new PrintTubServiceParser();
//		PrintTubService cmd = parser.split(input, true);
//		RayPrintTubServiceInvoker invoker = new RayPrintTubServiceInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 显示边缘容器服务
//	 * @param input 输入语句
//	 */
//	private void doShowTubContainer(String input, MeetDisplay display) {
//		ShowTubContainerParser parser = new ShowTubContainerParser();
//		ShowTubContainer cmd = parser.split(input, true);
//		RayShowTubContainerInvoker invoker = new RayShowTubContainerInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 启动边缘容器服务
//	 * @param input 输入语句
//	 */
//	private void doCheckTubListener(String input, MeetDisplay display) {
//		CheckTubListenerParser parser = new CheckTubListenerParser();
//		CheckTubListener cmd = parser.split(input);
//		RayCheckTubListenerInvoker invoker = new RayCheckTubListenerInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 开放TIGGER操作
//	 * @param input 输入语句
//	 */
//	private void doOpenTigger(String input, MeetDisplay display) {
//		OpenTiggerParser parser = new OpenTiggerParser();
//		OpenTigger cmd = parser.split(input);
//		RayOpenTiggerInvoker invoker = new RayOpenTiggerInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 关闭TIGGER操作
//	 * @param input 输入语句
//	 */
//	private void doCloseTigger(String input, MeetDisplay display) {
//		CloseTiggerParser parser = new CloseTiggerParser();
//		CloseTigger cmd = parser.split(input);
//		RayCloseTiggerInvoker invoker = new RayCloseTiggerInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 检索云应用
//	 * @param input 输入语句
//	 */
//	private void doSeekCloudWare(String input, MeetDisplay display) {
//		SeekCloudWareParser parser = new SeekCloudWareParser();
//		SeekCloudWare cmd = parser.split(input);
//		RaySeekCloudWareInvoker invoker = new RaySeekCloudWareInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 生成分布计算包
//	 * @param input 输入语句
//	 */
//	private void doBuildConductPackage(String input, MeetDisplay display) {
//		BuildConductPackageParser parser = new BuildConductPackageParser();
//		BuildConductPackage cmd = parser.split(input, true);
//		RayBuildConductPackageInvoker invoker = new RayBuildConductPackageInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 生成分布数据构建包
//	 * @param input 输入语句
//	 */
//	private void doBuildEstablishPackage(String input, MeetDisplay display) {
//		BuildEstablishPackageParser parser = new BuildEstablishPackageParser();
//		BuildEstablishPackage cmd = parser.split(input, false);
//		RayBuildEstablishPackageInvoker invoker = new RayBuildEstablishPackageInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 生成CONTACT应用包
//	 * @param input 输入语句
//	 */
//	private void doBuildContactPackage(String input, MeetDisplay display) {
//		BuildContactPackageParser parser = new BuildContactPackageParser();
//		BuildContactPackage cmd = parser.split(input, true);
//		RayBuildContactPackageInvoker invoker = new RayBuildContactPackageInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 部署分布计算包
//	 * @param input 输入语句
//	 */
//	private void doDeployConductPackage(String input, MeetDisplay display) {
//		DeployConductPackageParser parser = new DeployConductPackageParser();
//		DeployConductPackage cmd = parser.split(input);
//		RayInvoker invoker = null;
//
//		if (cmd.isSystemWare()) {
//			invoker = new RaySystemDeployConductPackageInvoker(cmd);
//		} else if (cmd.isLocal()) {
//			invoker = new RayLocalDeployConductPackageInvoker(cmd);
//		} else {
//			invoker = new RayDeployConductPackageInvoker(cmd);
//		}
//
//		// 发送
//		if (invoker != null) {
//			invoker.setDisplay(display);
//			launch(invoker);
//		}
//	}
//
//	/**
//	 * 部署分布数据构建包
//	 * @param input 输入语句
//	 */
//	private void doDeployEstablishPackage(String input, MeetDisplay display) {
//		DeployEstablishPackageParser parser = new DeployEstablishPackageParser();
//		DeployEstablishPackage cmd = parser.split(input);
//		RayInvoker invoker = null;
//
//		if (cmd.isSystemWare()) {
//			invoker = new RaySystemDeployEstablishPackageInvoker(cmd);
//		} else if (cmd.isLocal()) {
//			invoker = new RayLocalDeployEstablishPackageInvoker(cmd);
//		} else {
//			invoker = new RayDeployEstablishPackageInvoker(cmd);
//		}
//
//		// 发送
//		if (invoker != null) {
//			invoker.setDisplay(display);launch(invoker);
//		}
//	}
//
//	/**
//	 * 部署CONTACT应用包
//	 * @param input 输入语句
//	 */
//	private void doDeployContactPackage(String input, MeetDisplay display) {
//		DeployContactPackageParser parser = new DeployContactPackageParser();
//		DeployContactPackage cmd = parser.split(input);
//		RayInvoker invoker = null;
//
//		if (cmd.isSystemWare()) {
//			invoker = new RaySystemDeployContactPackageInvoker(cmd);
//		} else if (cmd.isLocal()) {
//			invoker = new RayLocalDeployContactPackageInvoker(cmd);
//		} else {
//			invoker = new RayDeployContactPackageInvoker(cmd);
//		}
//
//		// 发送
//		if (invoker != null) {
//			invoker.setDisplay(display);launch(invoker);
//		}
//	}
//
//	/**
//	 * 删除分布计算包
//	 * @param input 输入语句
//	 */
//	private void doDropConductPackage(String input, MeetDisplay display) {
//		DropConductPackageParser parser = new DropConductPackageParser();
//		DropConductPackage cmd = parser.split(input);
//		RayDropConductPackageInvoker invoker = new RayDropConductPackageInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 删除分布数据构建包
//	 * @param input 输入语句
//	 */
//	private void doDropEstablishPackage(String input, MeetDisplay display) {
//		DropEstablishPackageParser parser = new DropEstablishPackageParser();
//		DropEstablishPackage cmd = parser.split(input);
//		RayDropEstablishPackageInvoker invoker = new RayDropEstablishPackageInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 删除CONTACT应用包
//	 * @param input 输入语句
//	 */
//	private void doDropContactPackage(String input, MeetDisplay display) {
//		DropContactPackageParser parser = new DropContactPackageParser();
//		DropContactPackage cmd = parser.split(input);
//		RayDropContactPackageInvoker invoker = new RayDropContactPackageInvoker(cmd);
//		invoker.setDisplay(display);launch(invoker);
//	}
//
//	/**
//	 * 运行分布式应用
//	 * @param input 输入语句
//	 */
//	private void doRunTask(String input, CommandAuditor auditor, MeetDisplay display) {
//		RunTaskParser parser = new RunTaskParser();
//		RunTask cmd = parser.split(input, true);
//		// 转发处理
//		RunTaskProcesser processer = new RunTaskProcesser(auditor, display, cmd);
//		processer.process();
//	}
//
//	/**
//	 * 建立云存储目录
//	 * @param input 输入语句
//	 */
//	private void doCreateCloudDirectory(String input, MeetDisplay display) {
//		CreateDirectoryParser parser = new CreateDirectoryParser();
//		CreateCloudDirectory cmd = parser.split(input, true);
//		RayCreateCloudDirectoryInvoker invoker = new RayCreateCloudDirectoryInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 删除云存储目录
//	 * @param input 输入语句
//	 */
//	private void doDropCloudDirectory(String input, MeetDisplay display) {
//		DropCloudDirectoryParser parser = new DropCloudDirectoryParser();
//		DropCloudDirectory cmd = parser.split(input, true);
//		RayDropCloudDirectoryInvoker invoker = new RayDropCloudDirectoryInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 删除云存储目录
//	 * @param input 输入语句
//	 */
//	private void doDropCloudFile(String input, MeetDisplay display) {
//		DropCloudFileParser parser = new DropCloudFileParser();
//		DropCloudFile cmd = parser.split(input, true);
//		RayDropCloudFileInvoker invoker = new RayDropCloudFileInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 修改云存储目录
//	 * @param input 输入语句
//	 */
//	private void doRenameCloudDirectory(String input, MeetDisplay display) {
//		RenameCloudDirectoryParser parser = new RenameCloudDirectoryParser();
//		RenameCloudDirectory cmd = parser.split(input, true);
//		RayRenameCloudDirectoryInvoker invoker = new RayRenameCloudDirectoryInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 修改云存储目录
//	 * @param input 输入语句
//	 */
//	private void doRenameCloudFile(String input, MeetDisplay display) {
//		RenameCloudFileParser parser = new RenameCloudFileParser();
//		RenameCloudFile cmd = parser.split(input, true);
//		RayRenameCloudFileInvoker invoker = new RayRenameCloudFileInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//	
//	/**
//	 * 上传文件到云端
//	 * @param input 输入语句
//	 */
//	private void doUploadCloudFile(String input, MeetDisplay display) {
//		UploadCloudFileParser parser = new UploadCloudFileParser();
//		UploadCloudFile cmd = parser.split(input, true);
//		RayUploadCloudFileInvoker invoker = new RayUploadCloudFileInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 从云端下载文件
//	 * @param input 输入语句
//	 */
//	private void doDownloadCloudFile(String input, MeetDisplay display) {
//		DownloadCloudFileParser parser = new DownloadCloudFileParser();
//		DownloadCloudFile cmd = parser.split(input, true);
//		RayDownloadCloudFileInvoker invoker = new RayDownloadCloudFileInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 扫描云端目录
//	 * @param input 输入语句
//	 */
//	private void doScanCloudDirectory(String input, MeetDisplay display) {
//		ScanCloudDirectoryParser parser = new ScanCloudDirectoryParser();
//		ScanCloudDirectory cmd = parser.split(input, true);
//		RayScanCloudDirectoryInvoker invoker = new RayScanCloudDirectoryInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 扫描云端目录
//	 * @param cmd
//	 * @param display
//	 */
//	private void doScanDirectory(ScanCloudDirectory cmd, MeetDisplay display) {
//		RayScanCloudDirectoryInvoker invoker = new RayScanCloudDirectoryInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 建立云存储目录
//	 * @param cmd 命令
//	 * @param display
//	 */
//	private void doCreateDirectory(CreateCloudDirectory cmd, MeetDisplay display) {
//		RayCreateCloudDirectoryInvoker invoker = new RayCreateCloudDirectoryInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 下载文件
//	 * @param cmd 命令
//	 * @param display
//	 */
//	private void doDownloadFile(DownloadCloudFile cmd, MeetDisplay display) {
//		RayDownloadCloudFileInvoker invoker = new RayDownloadCloudFileInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 上传文件
//	 * @param cmd 命令
//	 * @param display
//	 */
//	private void doUploadFile(UploadCloudFile cmd, MeetDisplay display) {
//		RayUploadCloudFileInvoker invoker = new RayUploadCloudFileInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//
//	/**
//	 * 删除云端文件
//	 * @param cmd 命令
//	 * @param display
//	 */
//	private void doDropCloudFile(DropCloudFile cmd, MeetDisplay display) {
//		RayDropCloudFileInvoker invoker = new RayDropCloudFileInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//	
//	/**
//	 * 删除云端目录
//	 * @param cmd 命令
//	 * @param display
//	 */
//	private void doDropCloudDirectory(DropCloudDirectory cmd, MeetDisplay display) {
//		RayDropCloudDirectoryInvoker invoker = new RayDropCloudDirectoryInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//	/**
//	 * 修改云端文件
//	 * @param cmd 命令
//	 * @param display
//	 */
//	private void doRenameCloudFile(RenameCloudFile cmd, MeetDisplay display) {
//		RayRenameCloudFileInvoker invoker = new RayRenameCloudFileInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
//	
//	/**
//	 * 修改云端目录
//	 * @param cmd 命令
//	 * @param display
//	 */
//	private void doRenameCloudDirectory(RenameCloudDirectory cmd, MeetDisplay display) {
//		RayRenameCloudDirectoryInvoker invoker = new RayRenameCloudDirectoryInvoker(cmd);
//		invoker.setDisplay(display);
//		launch(invoker);
//	}
	
	/**
	 * 计算散列码（在本地进行）
	 * @param input 输入语句
	 */
	private void doBuildHash(String input, MeetDisplay display) {
		BuildHashParser parser = new BuildHashParser();
		BuildHash cmd = parser.split(input);
		RayBuildHashInvoker invoker = new RayBuildHashInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 远程关闭
	 * @param input 输入语句
	 */
	private void doShutdown(String input, MeetDisplay display) {
		ShutdownParser parser = new ShutdownParser();
		Shutdown cmd = parser.split(input);
		RayShutdownInvoker invoker = new RayShutdownInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 刷新注册用户
	 * @param input 输入语句
	 */
	private void doRefreshUser(String input, MeetDisplay display) {
		RefreshUserParser parser = new RefreshUserParser();
		RefreshUser cmd = parser.split(input);
		RayRefreshUserInvoker invoker = new RayRefreshUserInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 刷新元数据
	 * @param input 输入语句
	 */
	private void doRefreshMetadata(String input, MeetDisplay display) {
		RefreshMetadataParser parser = new RefreshMetadataParser();
		RefreshMetadata cmd = parser.split(input);
		RayRefreshMetadataInvoker invoker = new RayRefreshMetadataInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 定时扫描用户关联的时间间隔
	 * @param input 输入语句
	 */
	private void doScanLinkTime(String input, MeetDisplay display) {
		ScanLinkTimeParser parser = new ScanLinkTimeParser();
		ScanLinkTime cmd = parser.split(input);
		RayScanLinkTimeInvoker invoker = new RayScanLinkTimeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 强制站点重新注册
	 * @param input 输入语句
	 */
	private void doRefreshLogin(String input, MeetDisplay display) {
		RefreshLoginParser parser = new RefreshLoginParser();
		RefreshLogin cmd = parser.split(input);
		RayRefreshLoginInvoker invoker = new RayRefreshLoginInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 强制转换命令
	 * @param input 输入语句
	 */
	private void doRush(String input, MeetDisplay display) {
		RushParser parser = new RushParser();
		Rush cmd = parser.split(input);
		RayRushInvoker invoker = new RayRushInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除缓存数据块冗余数据
	 * @param input 输入语句
	 */
	private void doCompact(String input, MeetDisplay display) {
		CompactParser parser = new CompactParser();
		Compact cmd = parser.split(input);
		RayCompactInvoker invoker = new RayCompactInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 强制转换命令
	 * @param input 输入语句
	 */
	private void doSetDSMReduce(String input, MeetDisplay display) {
		SetDSMReduceParser parser = new SetDSMReduceParser();
		SetDSMReduce cmd = parser.split(input);
		RaySetDSMReduceInvoker invoker = new RaySetDSMReduceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示CALL/GATE节点上的登录用户
	 * @param input 输入语句
	 */
	private void doSeekFrontSite(String input, MeetDisplay display) {
		SeekFrontSiteParser parser = new SeekFrontSiteParser();
		SeekFrontSite cmd = parser.split(input);
		RaySeekFrontSiteInvoker invoker = new RaySeekFrontSiteInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除站点密钥令牌
	 * @param input 输入语句
	 */
	private void doCreateSecureToken(String input, MeetDisplay display) {
		CreateSecureTokenParser parser = new CreateSecureTokenParser();
		CreateSecureToken cmd = parser.split(input, true);
		RayCreateSecureTokenInvoker invoker = new RayCreateSecureTokenInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 删除站点密钥令牌
	 * @param input 输入语句
	 */
	private void doDropSecureToken(String input, MeetDisplay display) {
		DropSecureTokenParser parser = new DropSecureTokenParser();
		DropSecureToken cmd = parser.split(input, true);
		RayDropSecureTokenInvoker invoker = new RayDropSecureTokenInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 保存站点密钥令牌
	 * @param input 输入语句
	 */
	private void doFlushSecureToken(String input, MeetDisplay display) {
		FlushSecureTokenParser parser = new FlushSecureTokenParser();
		FlushSecureToken cmd = parser.split(input, true);
		RayFlushSecureTokenInvoker invoker = new RayFlushSecureTokenInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示站点密钥信息
	 * @param input 输入语句
	 */
	private void doShowSecureToken(String input, MeetDisplay display) {
		ShowSecureTokenParser parser = new ShowSecureTokenParser();
		ShowSecureToken cmd = parser.split(input, true);
		RayShowSecureTokenInvoker invoker = new RayShowSecureTokenInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 设置对称密钥长度
	 * @param input 输入语句
	 */
	private void doSetSecureSize(String input, MeetDisplay display) {
		SetSecureSizeParser parser = new SetSecureSizeParser();
		SetSecureSize cmd = parser.split(input, true);
		RaySetSecureSizeInvoker invoker = new RaySetSecureSizeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 重装加载节点的动态链接库
	 * @param input 输入语句
	 */
	private void doReloadLibrary(String input, MeetDisplay display) {
		ReloadLibraryParser parser = new ReloadLibraryParser();
		ReloadLibrary cmd = parser.split(input);
		RayReloadLibraryInvoker invoker = new RayReloadLibraryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 释放节点内存
	 * @param input 输入语句
	 */
	private void doReleaseMemory(String input, MeetDisplay display) {
		ReleaseMemoryParser parser = new ReleaseMemoryParser();
		ReleaseMemory cmd = parser.split(input);
		RayReleaseMemoryInvoker invoker = new RayReleaseMemoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 释放节点内存
	 * @param input 输入语句
	 */
	private void doReleaseMemoryInterval(String input, MeetDisplay display) {
		ReleaseMemoryIntervalParser parser = new ReleaseMemoryIntervalParser();
		ReleaseMemoryInterval cmd = parser.split(input);
		RayReleaseMemoryIntervalInvoker invoker = new RayReleaseMemoryIntervalInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置反馈包模式
	 * @param input 输入语句
	 */
	private void doReplyPacketMode(String input, MeetDisplay display) {
		ReplyPacketModeParser parser = new ReplyPacketModeParser();
		ReplyPacketMode cmd = parser.split(input);
		RayReplyPacketModeInvoker invoker = new RayReplyPacketModeInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 设置反馈包尺寸
	 * @param input 输入语句
	 */
	private void doReplyPacketSize(String input, MeetDisplay display) {
		ReplyPacketSizeParser parser = new ReplyPacketSizeParser();
		ReplyPacketSize cmd = parser.split(input);
		RayReplyPacketSizeInvoker invoker = new RayReplyPacketSizeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置发送异步数据超时
	 * @param input 输入语句
	 */
	private void doReplySendTimeout(String input, MeetDisplay display) {
		ReplySendTimeoutParser parser = new ReplySendTimeoutParser();
		ReplySendTimeout cmd = parser.split(input);
		RayReplySendTimeoutInvoker invoker = new RayReplySendTimeoutInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 设置接收异步数据超时
	 * @param input 输入语句
	 */
	private void doReplyReceiveTimeout(String input, MeetDisplay display) {
		ReplyReceiveTimeoutParser parser = new ReplyReceiveTimeoutParser();
		ReplyReceiveTimeout cmd = parser.split(input);
		RayReplyReceiveTimeoutInvoker invoker = new RayReplyReceiveTimeoutInvoker(cmd);
		launch(invoker, display);
	}
	/**
	 * 设置队列成员
	 * @param input 输入语句
	 */
	private void doReplyFlowControl(String input, MeetDisplay display) {
		ReplyFlowControlParser parser = new ReplyFlowControlParser();
		ReplyFlowControl cmd = parser.split(input);
		RayReplyFlowControlInvoker invoker = new RayReplyFlowControlInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 重新设置节点的安全策略
	 * @param input 输入语句
	 */
	private void doReloadSecurityPolicy(String input, MeetDisplay display) {
		ReloadSecurityPolicyParser parser = new ReloadSecurityPolicyParser();
		ReloadSecurityPolicy cmd = parser.split(input);
		RayReloadSecurityPolicyInvoker invoker = new RayReloadSecurityPolicyInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 重新加载许可证
	 * @param input 输入语句
	 */
	private void doReloadLicence(String input, MeetDisplay display) {
		ReloadLicenceParser parser = new ReloadLicenceParser();
		ReloadLicence cmd = parser.split(input);
		RayReloadLicenceInvoker invoker = new RayReloadLicenceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 重新发布许可证
	 * @param input 输入语句
	 */
	private void doPublishLicence(String input, MeetDisplay display) {
		PublishLicenceParser parser = new PublishLicenceParser();
		PublishLicence cmd = parser.split(input);
		RayPublishLicenceInvoker invoker = new RayPublishLicenceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描堆栈命令
	 * @param input 输入语句
	 */
	private void doScanStackCommand(String input, MeetDisplay display) {
		ScanCommandStackParser parser = new ScanCommandStackParser();
		ScanCommandStack cmd = parser.split(input);
		RayScanCommandStackInvoker invoker = new RayScanCommandStackInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描数据库资源（一个数据库下面包括任意多个表）
	 * @param input 输入语句
	 */
	private void doScanSchema(String input, MeetDisplay display) {
		ScanSchemaParser parser = new ScanSchemaParser();
		ScanSchema cmd = parser.split(input, false);
		RayScanSchemaInvoker invoker = new RayScanSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描数据表资源
	 * @param input 输入语句
	 */
	private void doScanTable(String input, MeetDisplay display) {
		ScanTableParser parser = new ScanTableParser();
		ScanTable cmd = parser.split(input, false);
		RayScanTableInvoker invoker = new RayScanTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描数据块资源
	 * @param input 输入语句
	 */
	private void doScanEntity(String input, MeetDisplay display) {
		ScanEntityWithWatchParser parser = new ScanEntityWithWatchParser();
		ScanEntity cmd = parser.split(input, false);
		RayScanEntityInvoker invoker = new RayScanEntityInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置命令超时
	 * @param input 输入语句
	 */
	private void doSetCommandTimeout(String input, MeetDisplay display) {
		CommandTimeoutParser parser = new CommandTimeoutParser();
		CommandTimeout cmd = parser.split(input);
		RayCommandTimeoutInvoker invoker = new RayCommandTimeoutInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置命令处理模式（在本地进行）
	 * @param input 输入语句
	 */
	private void doSetCommandMode(String input, MeetDisplay display) {
		CommandModeParser parser = new CommandModeParser();
		CommandMode cmd = parser.split(input);
		RayCommandModeInvoker invoker = new RayCommandModeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置命令优先级
	 * @param input 输入语句
	 */
	private void doSetCommandRank(String input, MeetDisplay display) {
		CommandRankParser parser = new CommandRankParser();
		CommandRank cmd = parser.split(input);
		RayCommandRankInvoker invoker = new RayCommandRankInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置FIXP本地客户端密文超时
	 * @param input 输入语句
	 */
	private void doSetCipherTimeout(String input, MeetDisplay display) {
		CipherTimeoutParser parser = new CipherTimeoutParser();
		CipherTimeout cmd = parser.split(input);
		RayCipherTimeoutInvoker invoker = new RayCipherTimeoutInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置分布处理超时
	 * @param input 输入语句
	 */
	private void doSetDistributedTimeout(String input, MeetDisplay display) {
		DistributedTimeoutParser parser = new DistributedTimeoutParser();
		DistributedTimeout cmd = parser.split(input);
		RayDistributedTimeoutInvoker invoker = new RayDistributedTimeoutInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置调用器数目
	 * @param input 输入语句
	 */
	private void doSetMaxInvoker(String input, MeetDisplay display) {
		MaxInvokerParser parser = new MaxInvokerParser();
		MaxInvoker cmd = parser.split(input);
		RayMaxInvokerInvoker invoker = new RayMaxInvokerInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 检测服务器系统信息
	 * @param input 输入语句
	 */
	private void doCheckSystemInfo(String input, MeetDisplay display) {
		CheckSystemInfoParser parser = new CheckSystemInfoParser();
		CheckSystemInfo cmd = parser.split(input);
		RayCheckSystemInfoInvoker invoker = new RayCheckSystemInfoInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 检测用户消耗的资源
	 * @param input 输入语句
	 */
	private void doCheckUserCost(String input, MeetDisplay display) {
		CheckUserCostParser parser = new CheckUserCostParser();
		CheckUserCost cmd = parser.split(input, true);
		RayCheckUserCostInvoker invoker = new RayCheckUserCostInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 设置映射端口
	 * @param input 输入语句
	 */
	private void doSetReflectPort(String input, MeetDisplay display) {
		ReflectPortParser parser = new ReflectPortParser();
		ReflectPort cmd = parser.split(input);
		RayReflectPortInvoker invoker = new RayReflectPortInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 设置异步缓存尺寸
	 * @param input 输入语句
	 */
	private void doSetMaxEchoBuffer(String input, MeetDisplay display) {
		MaxEchoBufferParser parser = new MaxEchoBufferParser();
		MaxEchoBuffer cmd = parser.split(input);
		RayMaxEchoBufferInvoker invoker = new RayMaxEchoBufferInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置图标界面显示日志数目
	 * @param input 输入语句
	 */
	private void doSetMaxLogElements(String input, MeetDisplay display) {
		MaxLogElementsParser parser = new MaxLogElementsParser();
		MaxLogElements cmd = parser.split(input);
		RayMaxLogElementsInvoker invoker = new RayMaxLogElementsInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 查询FRONT站点分布
	 * @param input 输入语句
	 */
	private void doSeekFrontUser(String input, MeetDisplay display) {
		SeekFrontUserParser parser = new SeekFrontUserParser();
		SeekFrontUser cmd = parser.split(input);
		RaySeekFrontUserInvoker invoker = new RaySeekFrontUserInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 查询用户站点分布
	 * @param input 输入语句
	 */
	private void doSeekUserSite(String input, MeetDisplay display) {
		SeekUserSiteParser parser = new SeekUserSiteParser();
		SeekUserSite cmd = parser.split(input);
		RaySeekUserSiteInvoker invoker = new RaySeekUserSiteInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 查询用户数据表分布
	 * @param input 输入语句
	 */
	private void doSeekUserTable(String input, MeetDisplay display) {
		SeekUserTableParser parser = new SeekUserTableParser();
		SeekUserTable cmd = parser.split(input);
		RaySeekUserTableInvoker invoker = new RaySeekUserTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 查询用户阶段命名分布
	 * @param input 输入语句
	 */
	private void doSeekUserTask(String input, MeetDisplay display) {
		SeekUserTaskParser parser = new SeekUserTaskParser();
		SeekUserTask cmd = parser.split(input);
		RaySeekUserTaskInvoker invoker = new RaySeekUserTaskInvoker(cmd);
		launch(invoker, display);
	}

//	/**
//	 * 发布系统级的分布任务组件
//	 * @param input 输入语句
//	 */
//	private void doPublishMultiTaskComponent(String input, MeetDisplay display) {
//		PublishMultiTaskComponentParser parser = new PublishMultiTaskComponentParser();
//		PublishMultiTaskComponent cmd = parser.split(input);
//		RayPublishMultiTaskComponentInvoker invoker = new RayPublishMultiTaskComponentInvoker(cmd);
//		launch(invoker, display);
//	}
//
//	/**
//	 * 发布系统级的分布任务组件应用附件
//	 * @param input 输入语句
//	 */
//	private void doPublishMultiTaskAssistComponent(String input, MeetDisplay display) {
//		PublishTaskAssistComponentParser parser = new PublishTaskAssistComponentParser();
//		PublishMultiTaskAssistComponent cmd = parser.split(input);
//		RayPublishMultiTaskAssistComponentInvoker invoker = new RayPublishMultiTaskAssistComponentInvoker(cmd);
//		launch(invoker, display);
//	}
//
//	/**
//	 * 发布系统级的分布任务组件动态链接库
//	 * @param input 输入语句
//	 */
//	private void doPublishMultiTaskLibraryComponent(String input, MeetDisplay display) {
//		PublishTaskLibraryComponentParser parser = new PublishTaskLibraryComponentParser();
//		PublishMultiTaskLibraryComponent cmd = parser.split(input);
//		RayPublishMultiTaskLibraryComponentInvoker invoker = new RayPublishMultiTaskLibraryComponentInvoker(cmd);
//		launch(invoker, display);
//	}

	/**
	 * 检索分布任务组件
	 * @param input
	 */
	private void doSeekTask(String input, MeetDisplay display) {
		SeekTaskParser parser = new SeekTaskParser();
		SeekTask cmd = parser.split(input);
		RaySeekTaskInvoker invoker = new RaySeekTaskInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置日志级别
	 * @param input 输入语句
	 */
	private void doSetLogLevel(String input, MeetDisplay display) {
		SetLogLevelParser parser = new SetLogLevelParser();
		SetLogLevel cmd = parser.split(input);
		RaySetLogLevelInvoker invoker = new RaySetLogLevelInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 重新加载/发布自定义JAR包
	 * @param input 输入语句
	 */
	private void doReloadCustom(String input, MeetDisplay display) {
		ReloadCustomParser parser = new ReloadCustomParser();
		ReloadCustom cmd = parser.split(input);
		RayReloadCustomInvoker invoker = new RayReloadCustomInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 半截符编码/解码
	 * @param input 输入语句
	 */
	private void doBuildHalf(String input, MeetDisplay display) {
		BuildHalfParser parser = new BuildHalfParser();
		BuildHalf cmd = parser.split(input);
		RayBuildHalfInvoker invoker = new RayBuildHalfInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * EACH签名
	 * @param input 输入语句
	 */
	private void doBuildEach(String input, MeetDisplay display) {
		BuildEachParser parser = new BuildEachParser();
		BuildEach cmd = parser.split(input);
		RayBuildEachInvoker invoker = new RayBuildEachInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示注册用户状态
	 * @param input 输入语句
	 */
	private void doPrintUserDiagram(String input, MeetDisplay display) {
		PrintUserDiagramParser parser = new PrintUserDiagramParser();
		PrintUserDiagram cmd = parser.split(input);
		RayPrintUserDiagramInvoker invoker = new RayPrintUserDiagramInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示注册用户授权
	 * @param input 输入语句
	 */
	private void doPrintGrantDiagram(String input, MeetDisplay display) {
		PrintGrantDiagramParser parser = new PrintGrantDiagramParser();
		PrintGrantDiagram cmd = parser.split(input);
		RayPrintGrantDiagramInvoker invoker = new RayPrintGrantDiagramInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示数据库状态
	 * @param input 输入语句
	 */
	private void doPrintSchemaDiagram(String input, MeetDisplay display) {
		PrintSchemaDiagramParser parser = new PrintSchemaDiagramParser();
		PrintSchemaDiagram cmd = parser.split(input, false); // WATCH站点不检查数据库存在
		RayPrintSchemaDiagramInvoker invoker = new RayPrintSchemaDiagramInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示数据表状态
	 * @param input 输入语句
	 */
	private void doPrintTableDiagram(String input, MeetDisplay display) {
		PrintTableDiagramParser parser = new PrintTableDiagramParser();
		PrintTableDiagram cmd = parser.split(input, false); // WATCH站点不检查数据库存在
		RayPrintTableDiagramInvoker invoker = new RayPrintTableDiagramInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描用户日志
	 * @param input 输入语句
	 */
	private void doScanUserLog(String input, MeetDisplay display) {
		ScanUserLogParser parser = new ScanUserLogParser();
		ScanUserLog cmd = parser.split(input); // 不检查用户签名有效
		RayScanUserLogInvoker invoker = new RayScanUserLogInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 被WATCH监视的站点刷新间隔
	 * @param input 输入语句
	 */
	private void doOutlookInterval(String input, MeetDisplay display) {
		OutlookIntervalParser parser = new OutlookIntervalParser();
		OutlookInterval cmd = parser.split(input); // 不检查用户签名有效
		RayOutlookIntervalInvoker invoker = new RayOutlookIntervalInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检索在线命令
	 * @param input 输入语句
	 */
	private void doSeekOnlineCommand(String input, MeetDisplay display) {
		SeekOnlineCommandParser parser = new SeekOnlineCommandParser();
		SeekOnlineCommand cmd = parser.split(input); // 不检查用户签名有效
		RaySeekOnlineCommandInvoker invoker = new RaySeekOnlineCommandInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检索在线资源
	 * @param input 输入语句
	 */
	private void doSeekOnlineResource(String input, MeetDisplay display) {
		SeekOnlineResourceParser parser = new SeekOnlineResourceParser();
		SeekOnlineResource cmd = parser.split(input); // 不检查用户签名有效
		RaySeekOnlineResourceInvoker invoker = new RaySeekOnlineResourceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检索用户在线注册元数据
	 * @param input 输入语句
	 */
	private void doSeekRegisterMetadata(String input, MeetDisplay display) {
		SeekRegisterMetadataParser parser = new SeekRegisterMetadataParser();
		SeekRegisterMetadata cmd = parser.split(input); // 不检查用户签名有效
		RaySeekRegisterMetadataInvoker invoker = new RaySeekRegisterMetadataInvoker(cmd);
		launch(invoker, display);
	}
	/**
	 * 检索用户分布区域
	 * @param input 输入语句
	 */
	private void doSeekUserArea(String input, MeetDisplay display) {
		SeekUserAreaParser parser = new SeekUserAreaParser();
		SeekUserArea cmd = parser.split(input); // 不检查用户签名有效
		RaySeekUserAreaInvoker invoker = new RaySeekUserAreaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private void doMultiGust(String input, MeetDisplay display) {
		MultiGustParser parser = new MultiGustParser();
		MultiGust cmd = parser.split(input); // 解析命令
		RayMultiGustInvoker invoker = new RayMultiGustInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private void doGust(String input, MeetDisplay display) {
		GustParser parser = new GustParser();
		Gust cmd = parser.split(input); // 解析命令
		RayGustInvoker invoker = new RayGustInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 并行数据传输流量测试
	 * @param input 输入语句
	 */
	private void doParallelMultiSwarm(String input, MeetDisplay display) {
		ParallelMultiSwarmParser parser = new ParallelMultiSwarmParser();
		ParallelMultiSwarm cmd = parser.split(input); // 解析命令
		RayParallelMultiSwarmInvoker invoker = new RayParallelMultiSwarmInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 并行数据传输流量测试
	 * @param input 输入语句
	 */
	private void doParallelMultiGust(String input, MeetDisplay display) {
		ParallelMultiGustParser parser = new ParallelMultiGustParser();
		ParallelMultiGust cmd = parser.split(input); // 解析命令
		RayParallelMultiGustInvoker invoker = new RayParallelMultiGustInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 持续数据传输流量测试
	 * @param input 输入语句
	 */
	private void doMultiSwarm(String input, MeetDisplay display) {
		MultiSwarmParser parser = new MultiSwarmParser();
		MultiSwarm cmd = parser.split(input); // 解析命令
		RayMultiSwarmInvoker invoker = new RayMultiSwarmInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private void doSwarm(String input, MeetDisplay display) {
		SwarmParser parser = new SwarmParser();
		Swarm cmd = parser.split(input, true); // 解析命令
		RaySwarmInvoker invoker = new RaySwarmInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 站点连接测试
	 * @param input 输入语句
	 */
	private void doRing(String input, MeetDisplay display) {
		RingParser parser = new RingParser();
		Ring cmd = parser.split(input, true); // 解析命令
		RayRingInvoker invoker = new RayRingInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 发布用户到指定站点
	 * @param input 输入语句
	 */
	private void doDeployUser(String input, MeetDisplay display) {
		DeployUserParser parser = new DeployUserParser();
		DeployUser cmd = parser.split(input); // 解析命令
		RayDeployUserInvoker invoker = new RayDeployUserInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 发布数据表到指定站点
	 * @param input 输入语句
	 */
	private void doDeployTable(String input, MeetDisplay display) {
		DeployTableParser parser = new DeployTableParser();
		DeployTable cmd = parser.split(input); // 解析命令
		RayDeployTableInvoker invoker = new RayDeployTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 从DATA主节点复制数据块到从节点
	 * @param input 输入语句
	 */
	private void doCopyMasterMass(String input, MeetDisplay display) {
		CopyMasterMassParser parser = new CopyMasterMassParser();
		CopyMasterMass cmd = parser.split(input); // 解析命令
		RayCopyMasterMassInvoker invoker = new RayCopyMasterMassInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 定位GATE站点模式
	 * @param input 输入语句
	 */
	private void doShadowMode(String input, MeetDisplay display) {
		ShadowModeParser parser = new ShadowModeParser();
		ShadowMode cmd = parser.split(input); // 解析命令
		RayShadowModeInvoker invoker = new RayShadowModeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检查GATE站点注册用户和站点编号一致性
	 * @param input 输入语句
	 */
	private void doCheckShadowConsistency(String input, MeetDisplay display) {
		CheckShadowConsistencyParser parser = new CheckShadowConsistencyParser();
		CheckShadowConsistency cmd = parser.split(input); // 解析命令
		RayCheckShadowConsistencyInvoker invoker = new RayCheckShadowConsistencyInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 最大CPU限制
	 * @param input 输入语句
	 */
	private void doMostCPU(String input, MeetDisplay display) {
		MostCPUParser parser = new MostCPUParser();
		MostCPU cmd = parser.split(input); // 解析命令
		RayMostCPUInvoker invoker = new RayMostCPUInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 最大虚拟机内存限制
	 * @param input 输入语句
	 */
	private void doMostVMMemory(String input, MeetDisplay display) {
		MostVMMemoryParser parser = new MostVMMemoryParser();
		MostVMMemory cmd = parser.split(input); // 解析命令
		RayMostVMMemoryInvoker invoker = new RayMostVMMemoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 最少内存限制
	 * @param input 输入语句
	 */
	private void doLeastMemory(String input, MeetDisplay display) {
		LeastMemoryParser parser = new LeastMemoryParser();
		LeastMemory cmd = parser.split(input); // 解析命令
		RayLeastMemoryInvoker invoker = new RayLeastMemoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 最少磁盘空间限制
	 * @param input 输入语句
	 */
	private void doLeastDisk(String input, MeetDisplay display) {
		LeastDiskParser parser = new LeastDiskParser();
		LeastDisk cmd = parser.split(input); // 解析命令
		RayLeastDiskInvoker invoker = new RayLeastDiskInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示节点设备目录
	 * @param input 输入语句
	 */
	private void doCheckSitePath(String input, MeetDisplay display) {
		CheckSitePathParser parser = new CheckSitePathParser();
		CheckSitePath cmd = parser.split(input); // 解析命令
		RayCheckSitePathInvoker invoker = new RayCheckSitePathInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 从指定节点清除用户记录
	 * @param input 输入语句
	 */
	private void doEraseUser(String input, CommandAuditor auditor, MeetDisplay display) {
		EraseUserParser parser = new EraseUserParser();
		EraseUser cmd = parser.split(input); // 解析命令

		boolean pass = false;
		// 判断有DATA节点，显示另一种提示
		if (cmd.hasDataSite()) {
			String content = RayInvoker.getXMLContent("ERASE-USER/ERASE-DATA-BOX");
			pass = confirm(null, content, auditor);
		} else {
			String content = RayInvoker.getXMLContent("ERASE-USER/ERASE-SITE-BOX");
			pass = confirm(null, content, auditor);
		}
		// 确定后，才执行
		if (pass) {
			RayEraseUserInvoker invoker = new RayEraseUserInvoker(cmd);
			launch(invoker, display);
		}
	}

	/**
	 * 开放警告消息
	 * @param input 输入语句
	 */
	private void doEnableWarning(String input, MeetDisplay display) {
		EnableWarningParser parser = new EnableWarningParser();
		EnableWarning cmd = parser.split(input);
		RayEnableWarningInvoker invoker = new RayEnableWarningInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 开放故障消息
	 * @param input 输入语句
	 */
	private void doEnableFault(String input, MeetDisplay display) {
		EnableFaultParser parser = new EnableFaultParser();
		EnableFault cmd = parser.split(input);
		RayEnableFaultInvoker invoker = new RayEnableFaultInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 屏蔽警告消息
	 * @param input 输入语句
	 */
	private void doDisableWarning(String input, MeetDisplay display) {
		DisableWarningParser parser = new DisableWarningParser();
		DisableWarning cmd = parser.split(input);
		RayDisableWarningInvoker invoker = new RayDisableWarningInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 屏蔽故障消息
	 * @param input 输入语句
	 */
	private void doDisableFault(String input, MeetDisplay display) {
		DisableFaultParser parser = new DisableFaultParser();
		DisableFault cmd = parser.split(input);
		RayDisableFaultInvoker invoker = new RayDisableFaultInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置成员虚拟空间
	 * @param input 输入语句
	 */
	private void doSetMemberCyber(String input, MeetDisplay display) {
		SetMemberCyberParser parser = new SetMemberCyberParser();
		SetMemberCyber cmd = parser.split(input);
		RaySetMemberCyberInvoker invoker = new RaySetMemberCyberInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置FRONT在线用户虚拟空间
	 * @param input 输入语句
	 */
	private void doSetFrontCyber(String input, MeetDisplay display) {
		SetFrontCyberParser parser = new SetFrontCyberParser();
		SetFrontCyber cmd = parser.split(input);
		RaySetFrontCyberInvoker invoker = new RaySetFrontCyberInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 *  检测用户虚拟空间
	 * @param input 输入语句
	 */
	private void doCheckUserCyber(String input, MeetDisplay display) {
		CheckUserCyberParser parser = new CheckUserCyberParser();
		CheckUserCyber cmd = parser.split(input);
		RayCheckUserCyberInvoker invoker = new RayCheckUserCyberInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 开放Tigger操作类型
	 * @param input 输入语句
	 */
	private void doOpenTigger(String input, MeetDisplay display) {
		OpenTiggerParser parser = new OpenTiggerParser();
		OpenTigger cmd = parser.split(input); // 解析命令
		RayOpenTiggerInvoker invoker = new RayOpenTiggerInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 关闭Tigger操作类型
	 * @param input 输入语句
	 */
	private void doCloseTigger(String input, MeetDisplay display) {
		CloseTiggerParser parser = new CloseTiggerParser();
		CloseTigger cmd = parser.split(input); // 解析命令
		RayCloseTiggerInvoker invoker = new RayCloseTiggerInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检索分布成员
	 * @param input 输入语句
	 */
	private void doCheckDistributedMember(String input, MeetDisplay display) {
		CheckDistributedMemberParser parser = new CheckDistributedMemberParser();
		CheckDistributedMember cmd = parser.split(input); // 解析命令
		RayCheckDistributedMemberInvoker invoker = new RayCheckDistributedMemberInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 部署分布计算包
	 * @param input 输入语句
	 */
	private void doDeployConductPackage(String input, MeetDisplay display) {
		DeployConductPackageParser parser = new DeployConductPackageParser();
		DeployConductPackage cmd = parser.split(input);
		RayDeployConductPackageInvoker invoker = new RayDeployConductPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 部署分布数据构建包
	 * @param input 输入语句
	 */
	private void doDeployEstablishPackage(String input, MeetDisplay display) {
		DeployEstablishPackageParser parser = new DeployEstablishPackageParser();
		DeployEstablishPackage cmd = parser.split(input);
		RayDeployEstablishPackageInvoker invoker = new RayDeployEstablishPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 部署CONTACT应用包
	 * @param input 输入语句
	 */
	private void doDeployContactPackage(String input, MeetDisplay display) {
		DeployContactPackageParser parser = new DeployContactPackageParser();
		DeployContactPackage cmd = parser.split(input);
		RayDeployContactPackageInvoker invoker = new RayDeployContactPackageInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 删除分布计算包
	 * @param input 输入语句
	 */
	private void doDropConductPackage(String input, MeetDisplay display) {
		DropConductPackageParser parser = new DropConductPackageParser();
		DropConductPackage cmd = parser.split(input);
		RayDropConductPackageInvoker invoker = new RayDropConductPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除分布数据构建包
	 * @param input 输入语句
	 */
	private void doDropEstablishPackage(String input, MeetDisplay display) {
		DropEstablishPackageParser parser = new DropEstablishPackageParser();
		DropEstablishPackage cmd = parser.split(input);
		RayDropEstablishPackageInvoker invoker = new RayDropEstablishPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除CONTACT应用包
	 * @param input 输入语句
	 */
	private void doDropContactPackage(String input, MeetDisplay display) {
		DropContactPackageParser parser = new DropContactPackageParser();
		DropContactPackage cmd = parser.split(input);
		RayDropContactPackageInvoker invoker = new RayDropContactPackageInvoker(cmd);
		launch(invoker, display);
	}	
}