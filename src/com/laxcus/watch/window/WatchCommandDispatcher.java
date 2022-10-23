/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.window;

import com.laxcus.access.parse.*;
import com.laxcus.command.access.permit.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.scan.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.*;
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
import com.laxcus.util.tip.*;
import com.laxcus.watch.*;
import com.laxcus.watch.component.*;
import com.laxcus.watch.invoker.*;
import com.laxcus.watch.pool.*;

/**
 * WATCH站点命令分派器 <br>
 * 
 * 工作内容：<br>
 * 1. 检查窗口的命令语法正式性 <br>
 * 2. 将字符串命令转换为实际命令，发送到指定的位置 <br>
 * 
 * @author scott.liang
 * @version 1.0 4/23/2013
 * @since laxcus 1.0
 */
public class WatchCommandDispatcher {

	/** 命令核准接口 **/
	private WatchCommandAuditor auditor;

	/** 参数显示面板  **/
	private WatchMixedPanel display;

	/** 语法检查器 **/
	private SyntaxChecker checker = new SyntaxChecker();

	/**
	 * 构造WATCH站点命令分派器
	 */
	public WatchCommandDispatcher() {
		super();
	}

	/**
	 * 显示提示，同时播放声音
	 * @param text 显示文本
	 */
	private void message(String text) {
		display.message(text, true);
	}

	/**
	 * 显示错误信息，同时播放声音
	 * @param text 显示文本
	 */
	private void fault(String text) {
		display.fault(text, true);
	}

	/**
	 * 设置命令核准接口
	 * @param e
	 */
	public void setCommandAuditor(WatchCommandAuditor e) {
		auditor = e;
	}

	/**
	 * 设置显示操作面板
	 * @param e
	 */
	public void setDisplay(WatchMixedPanel e) {
		display = e;
	}

	/**
	 * 在发送WATCH站点的命令前，提示用户确认这个操作
	 * @return 确定返回真，否则假
	 */
	private boolean confirm() {
		return auditor.confirm();
	}

	/**
	 * 在发送WATCH站点的命令前，提示用户确认这个操作
	 * @param title 提示标题
	 * @param content 显示内容
	 * @return 确定返回真，否则假
	 */
	private boolean confirm(String title, String content) {
		return auditor.confirm(title, content);
	}

	/**
	 * 异步调用器转发给管理池处理
	 * @param invoker 异步调用器
	 */
	private void launch(EchoInvoker invoker) {
		WatchInvokerPool.getInstance().launch(invoker);
	}

	/**
	 * 检查用户输入的语法
	 * @param input 输入语句
	 */
	public void check(String input) {
		try {
			testing(input);
		} catch (SyntaxException e) {
			e.printStackTrace();
			fault(e.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
			fault(e.getMessage());
		}
	}

	/**
	 * 提交命令到目标站点
	 * @param input 输入语句
	 */
	public void submit(String input) {
		try {
			implement(input);
		} catch (Throwable e) {
			Logger.fatal(e);
			fault(e.getMessage());
		}
	}

	/**
	 * 检查命令
	 * @param input 输入语句
	 */
	private void testing(String input) {
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

//		// 发布系统分布任务组件、应用附件、动态链接库
//		if (!success) success = checker.isPublishTaskComponent(input);
//		if (!success) success = checker.isPublishTaskAssistComponent(input);
//		if (!success) success = checker.isPublishTaskLibraryComponent(input);
		
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
		
		// 以上不成立，由自定义命令生成器判断是否属于自定义命令
		if (!success) {
			success = CustomCreator.isCommand(input);
		}

		// 显示在窗口
		WatchLauncher launcher = WatchLauncher.getInstance();
		if (success) {
			String text = launcher.message(MessageTip.CORRECT_SYNTAX);
			message(text);
		} else {
			String text = launcher.fault(FaultTip.INCORRECT_SYNTAX);
			fault(text);
		}
	}

	/**
	 * 检查是在线模式
	 * @return 返回真或者假
	 */
	private boolean checkOnline() {
		// 判断连网
		WatchLauncher launcher = WatchLauncher.getInstance();
		boolean logined = launcher.isLogined();
		if (!logined) {
			String text = launcher.fault(FaultTip.OFFLINE_REFUSE);
			fault(text);
			return false;
		}
		return true;
	}

	/**
	 * 执行命令
	 * @param input 输入语句
	 */
	private void implement(String input) {
		// 检测是在线模式
		if (!checkOnline()) {
			return;
		}

		// 判断是散列码命令
		boolean success = checker.isBuildHash(input);
		if (success) {
			doBuildHash(input);
		}
		// 设置命令超时时间
		if (!success) {
			success = checker.isCommandTimeout(input);
			if (success) {
				doSetCommandTimeout(input);
			}
		}
		// 命令处理模式
		if (!success) {
			success = checker.isCommandMode(input);
			if (success) {
				doSetCommandMode(input);
			}
		}
		// 设置命令优先级
		if (!success) {
			success = checker.isCommandRank(input);
			if (success) {
				doSetCommandRank(input);
			}
		}

		// 设置FIXP本地客户端密文超时时间
		if (!success) {
			success = checker.isCipherTimeout(input);
			if (success && confirm()) {
				doSetCipherTimeout(input);
			}
		}
		// 设置调用器
		if (!success) {
			success = checker.isMaxInvoker(input);
			if (success && confirm()) {
				doSetMaxInvoker(input);
			}
		}
		// 设置调用器
		if (!success) {
			success = checker.isCheckSystemInfo(input);
			if (success && confirm()) {
				doCheckSystemInfo(input);
			}
		}
		// 设置调用器
		if (!success) {
			success = checker.isSetReflectPort(input);
			if (success && confirm()) {
				doSetReflectPort(input);
			}
		}
		// 设置异步缓存尺寸
		if (!success) {
			success = checker.isMaxEchoBuffer(input);
			if (success && confirm()) {
				doSetMaxEchoBuffer(input);
			}
		}
		// 判断RUSH命令
		if (!success) {
			success = checker.isRush(input);
			if (success && confirm()) {
				doRush(input);
			}
		}
		// 判断COMPACT命令
		if (!success) {
			success = checker.isCompact(input);
			if (success && confirm()) {
				doCompact(input);
			}
		}
		// 判断SET DSM REDUCE命令
		if (!success) {
			success = checker.isSetDSMReduce(input);
			if (success && confirm()) {
				doSetDSMReduce(input);
			}
		}
		
		// 建立站点密钥令牌
		if (!success) {
			success = checker.isCreateSecureToken(input);
			if (success && confirm()) {
				doCreateSecureToken(input);
			}
		}
		// 删除站点密钥令牌
		if (!success) {
			success = checker.isDropSecureToken(input);
			if (success && confirm()) {
				doDropSecureToken(input);
			}
		}
		// 输出站点密钥令牌到磁盘
		if (!success) {
			success = checker.isFlushSecureToken(input);
			if (success && confirm()) {
				doFlushSecureToken(input);
			}
		}
		// 显示站点密钥令牌
		if (!success) {
			success = checker.isShowSecureToken(input);
			if (success && confirm()) {
				doShowSecureToken(input);
			}
		}
		// 设置对称密钥长度
		if (!success) {
			success = checker.isSetSecureSize(input);
			if (success && confirm()) {
				doSetSecureSize(input);
			}
		}
		
		// 重装安装本地的动态链接库
		if(!success) {
			success = checker.isReloadLibrary(input);
			if(success && confirm()) {
				doReloadLibrary(input);
			}
		}
		// 释放节点内存
		if (!success) {
			success = checker.isReleaseMemory(input);
			if (success && confirm()) {
				doReleaseMemory(input);
			}
		}
		// 释放节点内存间隔时间
		if (!success) {
			success = checker.isReleaseMemoryInterval(input);
			if (success && confirm()) {
				doReleaseMemoryInterval(input);
			}
		}
		
		// 设置应答包模式
		if (!success) {
			success = checker.isReplyPacketMode(input);
			if (success && confirm()) {
				doReplyPacketMode(input);
			}
		}
		// 设置应答包尺寸
		if (!success) {
			success = checker.isReplyPacketSize(input);
			if (success && confirm()) {
				doReplyPacketSize(input);
			}
		}
		// 设置发送超时
		if (!success) {
			success = checker.isReplySendTimeout(input);
			if (success && confirm()) {
				doReplySendTimeout(input);
			}
		}
		// 设置接收超时
		if (!success) {
			success = checker.isReplyReceiveTimeout(input);
			if (success && confirm()) {
				doReplyReceiveTimeout(input);
			}
		}
		// 设置队列成员
		if (!success) {
			success = checker.isReplyFlowControl(input);
			if (success && confirm()) {
				doReplyFlowControl(input);
			}
		}
		
		// 重装安装本地的动态链接库
		if (!success) {
			success = checker.isReloadSecurityPolicy(input);
			if (success && confirm()) {
				doReloadSecurityPolicy(input);
			}
		}
		// 重装加载许可证
		if (!success) {
			success = checker.isReloadLicence(input);
			if (success && confirm()) {
				doReloadLicence(input);
			}
		}
		// 重装加载许可证
		if (!success) {
			success = checker.isPublishLicence(input);
			if (success && confirm()) {
				doPublishLicence(input);
			}
		}

		// 扫描堆栈命令
		if (!success) {
			success = checker.isScanCommandStack(input);
			if (success && confirm()) {
				doScanStackCommand(input);
			}
		}
		// 扫描数据库
		if (!success) {
			success = checker.isScanSchema(input);
			if (success && confirm()) {
				doScanSchema(input);
			}
		}
		// 扫描数据表
		if (!success) {
			success = checker.isScanTable(input);
			if (success && confirm()) {
				doScanTable(input);
			}
		}
		// 扫描数据块
		if (!success) {
			success = checker.isScanEntityWithWatch(input);
			if (success && confirm()) {
				doScanEntity(input);
			}
		}

		// FRONT站点分布命令
		if (!success) {
			success = checker.isSeekFrontUser(input);
			if (success && confirm()) {
				doSeekFrontUser(input);
			}
		}
		// 用户站点分布命令
		if (!success) {
			success = checker.isSeekUserSite(input);
			if (success && confirm()) {
				doSeekUserSite(input);
			}
		}
		// 用户数据表分布命令
		if (!success) {
			success = checker.isSeekUserTable(input);
			if (success && confirm()) {
				doSeekUserTable(input);
			}
		}
		// 用户阶段命名分布命令
		if (!success) {
			success = checker.isSeekUserTask(input);
			if (success && confirm()) {
				doSeekUserTask(input);
			}
		}

		// 显示FRONT站点
		if(!success) {
			success = checker.isSeekFrontSite(input);
			if (success && confirm()) {
				doSeekFrontSite(input);
			}
		}
		// 远程关闭命令
		if (!success) {
			success = checker.isShutdown(input);
			if (success && confirm()) {
				doShutdown(input);
			}
		}
		// 判断是刷新注册用户命令
		if (!success) {
			success = checker.isRefreshUser(input);
			if (success && confirm()) {
				doRefreshUser(input);
			}
		}
		// 判断是刷新元数据命令
		if (!success) {
			success = checker.isRefreshMetadata(input);
			if (success && confirm()) {
				doRefreshMetadata(input);
			}
		}
		// 判断是扫描用户节点关联时间间隔
		if (!success) {
			success = checker.isScanLinkTime(input);
			if (success && confirm()) {
				doScanLinkTime(input);
			}
		}
		// 强制要求站点重新注册
		if (!success) {
			success = checker.isRefreshLogin(input);
			if (success && confirm()) {
				doRefreshLogin(input);
			}
		}
		
//		// 判断是发布系统分布任务组件
//		if (!success) {
//			success = checker.isPublishTaskComponent(input);
//			if (success && confirm()) {
//				doPublishMultiTaskComponent(input);
//			}
//		}
//		// 判断是发布系统分布任务组件的应用附件
//		if (!success) {
//			success = checker.isPublishTaskAssistComponent(input);
//			if (success && confirm()) {
//				doPublishMultiTaskAssistComponent(input);
//			}
//		}
//		// 判断是发布系统分布任务组件的动态链接库
//		if (!success) {
//			success = checker.isPublishTaskLibraryComponent(input);
//			if (success && confirm()) {
//				doPublishMultiTaskLibraryComponent(input);
//			}
//		}

		// 判断是检索分布任务组件
		if (!success) {
			success = checker.isSeekTask(input);
			if (success && confirm()) {
				doSeekTask(input);
			}
		}
		
		// 判断是设置站点日志等级
		if (!success) {
			success = checker.isSetLogLevel(input);
			if (success && confirm()) {
				doSetLogLevel(input);
			}
		}
		// 重装加载/发布自定义JAR包
		if(!success) {
			success = checker.isReloadCustom(input);
			if(success && confirm()) {
				doReloadCustom(input);
			}
		}

		// 半截符编码
		if (!success) {
			success = checker.isBuildHalf(input);
			if (success) { // 在本地进行
				doBuildHalf(input);
			}
		}
		// EACH签名
		if (!success) {
			success = checker.isBuildEach(input);
			if (success) { // 在本地进行
				doBuildEach(input);
			}
		}

		// 显示用户/授权/数据库/数据表状态
		if (!success) {
			success = checker.isPrintUserDiagram(input);
			if (success && confirm()) {
				doPrintUserDiagram(input);
			}
		}
		if (!success) {
			success = checker.isPrintGrantDiagram(input);
			if (success && confirm()) {
				doPrintGrantDiagram(input);
			}
		}
		if (!success) {
			success = checker.isPrintSchemaDiagram(input);
			if (success && confirm()) {
				doPrintSchemaDiagram(input);
			}
		}
		if (!success) {
			success = checker.isPrintTableDiagram(input);
			if (success && confirm()) {
				doPrintTableDiagram(input);
			}
		}

		// 扫描用户日志
		if (!success) {
			success = checker.isScanUserLog(input);
			if (success && confirm()) {
				doScanUserLog(input);
			}
		}

		// 设置命令超时时间
		if (!success) {
			success = checker.isOutlookInterval(input);
			if (success) {
				doOutlookInterval(input);
			}
		}

		// 检索在线命令
		if (!success) {
			success = checker.isSeekOnlineCommand(input);
			if (success && confirm()) {
				doSeekOnlineCommand(input);
			}
		}
		// 检索在线命令
		if (!success) {
			success = checker.isSeekOnlineResource(input);
			if (success && confirm()) {
				doSeekOnlineResource(input);
			}
		}
		// 检索用户在线注册元数据
		if (!success) {
			success = checker.isSeekRegisterMetadata(input);
			if (success && confirm()) {
				doSeekRegisterMetadata(input);
			}
		}
		// 检索用户分布区域
		if (!success) {
			success = checker.isSeekUserArea(input);
			if (success && confirm()) {
				doSeekUserArea(input);
			}
		}

		// 并行数据传输流量测试
		if (!success) {
			success = checker.isParallelMultiGust(input);
			if (success && confirm()) {
				doParallelMultiGust(input);
			}
		}
		// 数据传输流量测试
		if (!success) {
			success = checker.isMultiGust(input);
			if (success && confirm()) {
				doMultiGust(input);
			}
		}
		// 数据传输流量测试
		if (!success) {
			success = checker.isGust(input);
			if (success && confirm()) {
				doGust(input);
			}
		}
		// 并行数据传输流量测试
		if (!success) {
			success = checker.isParallelMultiSwarm(input);
			if (success && confirm()) {
				doParallelMultiSwarm(input);
			}
		}
		// 持续数据传输流量测试
		if (!success) {
			success = checker.isMultiSwarm(input);
			if (success && confirm()) {
				doMultiSwarm(input);
			}
		}
		// 数据传输流量测试
		if (!success) {
			success = checker.isSwarm(input);
			if (success && confirm()) {
				doSwarm(input);
			}
		}

		// 设置FIXP远程客户端密文超时时间
		if (!success) {
			success = checker.isRing(input);
			if (success && confirm()) {
				doRing(input);
			}
		}
		// 发布用户到指定站点
		if (!success) {
			success = checker.isDeployUser(input);
			if (success && confirm()) {
				doDeployUser(input);
			}
		}
		// 发布用户到指定站点
		if (!success) {
			success = checker.isEraselUser(input);
			if (success) {
				doEraseUser(input);
			}
		}
		// 发布数据表到指定站点
		if (!success) {
			success = checker.isDeployTable(input);
			if (success && confirm()) {
				doDeployTable(input);
			}
		}
		// 从DATA节点复制数据块到从节点
		if (!success) {
			success = checker.isCopyMasterMass(input);
			if (success && confirm()) {
				doCopyMasterMass(input);
			}
		}
		// 定位GATE站点模式
		if (!success) {
			success = checker.isShadowMode(input);
			if (success && confirm()) {
				doShadowMode(input);
			}
		}
		// 检查GATE站点与编号一致性
		if (!success) {
			success = checker.isCheckShadowConsistency(input);
			if (success && confirm()) {
				doCheckShadowConsistency(input);
			}
		}

		// 最高CPU限制
		if (!success) {
			success = checker.isMostCPU(input);
			if (success && confirm()) {
				doMostCPU(input);
			}
		}
		// 最高虚拟机内存限制
		if (!success) {
			success = checker.isMostVMMemory(input);
			if (success && confirm()) {
				doMostVMMemory(input);
			}
		}
		// 最少内存限制
		if (!success) {
			success = checker.isLeastMemory(input);
			if (success && confirm()) {
				doLeastMemory(input);
			}
		}
		// 最少内存限制
		if (!success) {
			success = checker.isLeastDisk(input);
			if (success && confirm()) {
				doLeastDisk(input);
			}
		}
		// 打印节点设备目录
		if (!success) {
			success = checker.isCheckSitePath(input);
			if (success && confirm()) {
				doCheckSitePath(input);
			}
		}
		// 设置分布处理超时
		if (!success) {
			success = checker.isDistributedTimeout(input);
			if (success && confirm()) {
				doSetDistributedTimeout(input);
			}
		}

		// 打开/屏蔽其他节点的警告/故障消息
		if (!success) {
			success = checker.isEnableWarning(input);
			if (success && confirm()) {
				doEnableWarning(input);
			}
		}
		if (!success) {
			success = checker.isEnableFault(input);
			if (success && confirm()) {
				doEnableFault(input);
			}
		}
		if (!success) {
			success = checker.isDisableWarning(input);
			if (success && confirm()) {
				doDisableWarning(input);
			}
		}
		if (!success) {
			success = checker.isDisableFault(input);
			if (success && confirm()) {
				doDisableFault(input);
			}
		}
		// 设置虚拟空间
		if (!success) {
			success = checker.isSetMemberCyber(input);
			if (success && confirm()) {
				doSetMemberCyber(input);
			}
		}
		if (!success) {
			success = checker.isSetFrontCyber(input);
			if (success && confirm()) {
				doSetFrontCyber(input);
			}
		}
		if (!success) {
			success = checker.isCheckUserCyber(input);
			if (success && confirm()) {
				doCheckUserCyber(input);
			}
		}

		// 日志显示数目
		if (!success) {
			success = checker.isMaxLogElements(input);
			if (success) {
				doSetMaxLogElements(input);
			}
		}
		if (!success) {
			success = checker.isOpenTigger(input);
			if (success && confirm()) {
				doOpenTigger(input);
			}
		}
		if (!success) {
			success = checker.isCloseTigger(input);
			if (success && confirm()) {
				doCloseTigger(input);
			}
		}
		if (!success) {
			success = checker.isCheckDistributedMember(input);
			if (success && confirm()) {
				doCheckDistributedMember(input);
			}
		}

		// 部署包
		if (!success) {
			success = checker.isDeployConductPackage(input);
			if (success && confirm()) {
				doDeployConductPackage(input);
			}
		}
		if (!success) {
			success = checker.isDeployEstablishPackage(input);
			if (success && confirm()) {
				doDeployEstablishPackage(input);
			}
		}
		if (!success) {
			success = checker.isDeployContactPackage(input);
			if (success && confirm()) {
				doDeployContactPackage(input);
			}
		}

		// 删除包
		if (!success) {
			success = checker.isDropConductPackage(input);
			if (success && confirm()) {
				doDropConductPackage(input);
			}
		}
		if (!success) {
			success = checker.isDropEstablishPackage(input);
			if (success && confirm()) {
				doDropEstablishPackage(input);
			}
		}
		if (!success) {
			success = checker.isDropContactPackage(input);
			if (success && confirm()) {
				doDropContactPackage(input);
			}
		}

		// 自定义命令调用器来判断
		if (!success) {
			success = CustomCreator.isCommand(input);
			if (success) {
				// 参数见invoker.xml配置
				String content = WatchInvoker.getXMLAttribute("CUSTOM-COMMAND/MESSAGE-BOX/content");
				if (confirm(null, content)) {
					EchoInvoker invoker = CustomCreator.createInvoker(input);
					launch(invoker);
				}
			}
		}

		// 以上判断不成立时，拒绝执行；否则保存它！
		if (success) {
			Tigger.command(input);
		} else {
			WatchLauncher launcher = WatchLauncher.getInstance();
			String text = launcher.fault(FaultTip.ILLEGAL_COMMAND);
			fault(text);
		}
	}

	/**
	 * 计算散列码（在本地进行）
	 * @param input 输入语句
	 */
	private void doBuildHash(String input) {
		BuildHashParser parser = new BuildHashParser();
		BuildHash cmd = parser.split(input);
		WatchBuildHashInvoker invoker = new WatchBuildHashInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 远程关闭
	 * @param input 输入语句
	 */
	private void doShutdown(String input) {
		ShutdownParser parser = new ShutdownParser();
		Shutdown cmd = parser.split(input);
		WatchShutdownInvoker invoker = new WatchShutdownInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 刷新注册用户
	 * @param input 输入语句
	 */
	private void doRefreshUser(String input) {
		RefreshUserParser parser = new RefreshUserParser();
		RefreshUser cmd = parser.split(input);
		WatchRefreshUserInvoker invoker = new WatchRefreshUserInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 刷新元数据
	 * @param input 输入语句
	 */
	private void doRefreshMetadata(String input) {
		RefreshMetadataParser parser = new RefreshMetadataParser();
		RefreshMetadata cmd = parser.split(input);
		WatchRefreshMetadataInvoker invoker = new WatchRefreshMetadataInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 定时扫描用户关联的时间间隔
	 * @param input 输入语句
	 */
	private void doScanLinkTime(String input) {
		ScanLinkTimeParser parser = new ScanLinkTimeParser();
		ScanLinkTime cmd = parser.split(input);
		WatchScanLinkTimeInvoker invoker = new WatchScanLinkTimeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 强制站点重新注册
	 * @param input 输入语句
	 */
	private void doRefreshLogin(String input) {
		RefreshLoginParser parser = new RefreshLoginParser();
		RefreshLogin cmd = parser.split(input);
		WatchRefreshLoginInvoker invoker = new WatchRefreshLoginInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 强制转换命令
	 * @param input 输入语句
	 */
	private void doRush(String input) {
		RushParser parser = new RushParser();
		Rush cmd = parser.split(input);
		WatchRushInvoker invoker = new WatchRushInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 删除缓存数据块冗余数据
	 * @param input 输入语句
	 */
	private void doCompact(String input) {
		CompactParser parser = new CompactParser();
		Compact cmd = parser.split(input);
		WatchCompactInvoker invoker = new WatchCompactInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 强制转换命令
	 * @param input 输入语句
	 */
	private void doSetDSMReduce(String input) {
		SetDSMReduceParser parser = new SetDSMReduceParser();
		SetDSMReduce cmd = parser.split(input);
		WatchSetDSMReduceInvoker invoker = new WatchSetDSMReduceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示CALL/GATE节点上的登录用户
	 * @param input 输入语句
	 */
	private void doSeekFrontSite(String input) {
		SeekFrontSiteParser parser = new SeekFrontSiteParser();
		SeekFrontSite cmd = parser.split(input);
		WatchSeekFrontSiteInvoker invoker = new WatchSeekFrontSiteInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 删除站点密钥令牌
	 * @param input 输入语句
	 */
	private void doCreateSecureToken(String input) {
		CreateSecureTokenParser parser = new CreateSecureTokenParser();
		CreateSecureToken cmd = parser.split(input, true);
		WatchCreateSecureTokenInvoker invoker = new WatchCreateSecureTokenInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 删除站点密钥令牌
	 * @param input 输入语句
	 */
	private void doDropSecureToken(String input) {
		DropSecureTokenParser parser = new DropSecureTokenParser();
		DropSecureToken cmd = parser.split(input, true);
		WatchDropSecureTokenInvoker invoker = new WatchDropSecureTokenInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 保存站点密钥令牌
	 * @param input 输入语句
	 */
	private void doFlushSecureToken(String input) {
		FlushSecureTokenParser parser = new FlushSecureTokenParser();
		FlushSecureToken cmd = parser.split(input, true);
		WatchFlushSecureTokenInvoker invoker = new WatchFlushSecureTokenInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示站点密钥信息
	 * @param input 输入语句
	 */
	private void doShowSecureToken(String input) {
		ShowSecureTokenParser parser = new ShowSecureTokenParser();
		ShowSecureToken cmd = parser.split(input, true);
		WatchShowSecureTokenInvoker invoker = new WatchShowSecureTokenInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 设置对称密钥长度
	 * @param input 输入语句
	 */
	private void doSetSecureSize(String input) {
		SetSecureSizeParser parser = new SetSecureSizeParser();
		SetSecureSize cmd = parser.split(input, true);
		WatchSetSecureSizeInvoker invoker = new WatchSetSecureSizeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 重装加载节点的动态链接库
	 * @param input 输入语句
	 */
	private void doReloadLibrary(String input) {
		ReloadLibraryParser parser = new ReloadLibraryParser();
		ReloadLibrary cmd = parser.split(input);
		WatchReloadLibraryInvoker invoker = new WatchReloadLibraryInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 释放节点内存
	 * @param input 输入语句
	 */
	private void doReleaseMemory(String input) {
		ReleaseMemoryParser parser = new ReleaseMemoryParser();
		ReleaseMemory cmd = parser.split(input);
		WatchReleaseMemoryInvoker invoker = new WatchReleaseMemoryInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 释放节点内存
	 * @param input 输入语句
	 */
	private void doReleaseMemoryInterval(String input) {
		ReleaseMemoryIntervalParser parser = new ReleaseMemoryIntervalParser();
		ReleaseMemoryInterval cmd = parser.split(input);
		WatchReleaseMemoryIntervalInvoker invoker = new WatchReleaseMemoryIntervalInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置反馈包模式
	 * @param input 输入语句
	 */
	private void doReplyPacketMode(String input) {
		ReplyPacketModeParser parser = new ReplyPacketModeParser();
		ReplyPacketMode cmd = parser.split(input);
		WatchReplyPacketModeInvoker invoker = new WatchReplyPacketModeInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 设置反馈包尺寸
	 * @param input 输入语句
	 */
	private void doReplyPacketSize(String input) {
		ReplyPacketSizeParser parser = new ReplyPacketSizeParser();
		ReplyPacketSize cmd = parser.split(input);
		WatchReplyPacketSizeInvoker invoker = new WatchReplyPacketSizeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置发送异步数据超时
	 * @param input 输入语句
	 */
	private void doReplySendTimeout(String input) {
		ReplySendTimeoutParser parser = new ReplySendTimeoutParser();
		ReplySendTimeout cmd = parser.split(input);
		WatchReplySendTimeoutInvoker invoker = new WatchReplySendTimeoutInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 设置接收异步数据超时
	 * @param input 输入语句
	 */
	private void doReplyReceiveTimeout(String input) {
		ReplyReceiveTimeoutParser parser = new ReplyReceiveTimeoutParser();
		ReplyReceiveTimeout cmd = parser.split(input);
		WatchReplyReceiveTimeoutInvoker invoker = new WatchReplyReceiveTimeoutInvoker(cmd);
		launch(invoker);
	}
	/**
	 * 设置队列成员
	 * @param input 输入语句
	 */
	private void doReplyFlowControl(String input) {
		ReplyFlowControlParser parser = new ReplyFlowControlParser();
		ReplyFlowControl cmd = parser.split(input);
		WatchReplyFlowControlInvoker invoker = new WatchReplyFlowControlInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 重新设置节点的安全策略
	 * @param input 输入语句
	 */
	private void doReloadSecurityPolicy(String input) {
		ReloadSecurityPolicyParser parser = new ReloadSecurityPolicyParser();
		ReloadSecurityPolicy cmd = parser.split(input);
		WatchReloadSecurityPolicyInvoker invoker = new WatchReloadSecurityPolicyInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 重新加载许可证
	 * @param input 输入语句
	 */
	private void doReloadLicence(String input) {
		ReloadLicenceParser parser = new ReloadLicenceParser();
		ReloadLicence cmd = parser.split(input);
		WatchReloadLicenceInvoker invoker = new WatchReloadLicenceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 重新发布许可证
	 * @param input 输入语句
	 */
	private void doPublishLicence(String input) {
		PublishLicenceParser parser = new PublishLicenceParser();
		PublishLicence cmd = parser.split(input);
		WatchPublishLicenceInvoker invoker = new WatchPublishLicenceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 扫描堆栈命令
	 * @param input 输入语句
	 */
	private void doScanStackCommand(String input) {
		ScanCommandStackParser parser = new ScanCommandStackParser();
		ScanCommandStack cmd = parser.split(input);
		WatchScanCommandStackInvoker invoker = new WatchScanCommandStackInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 扫描数据库资源（一个数据库下面包括任意多个表）
	 * @param input 输入语句
	 */
	private void doScanSchema(String input) {
		ScanSchemaParser parser = new ScanSchemaParser();
		ScanSchema cmd = parser.split(input, false);
		WatchScanSchemaInvoker invoker = new WatchScanSchemaInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 扫描数据表资源
	 * @param input 输入语句
	 */
	private void doScanTable(String input) {
		ScanTableParser parser = new ScanTableParser();
		ScanTable cmd = parser.split(input, false);
		WatchScanTableInvoker invoker = new WatchScanTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 扫描数据块资源
	 * @param input 输入语句
	 */
	private void doScanEntity(String input) {
		ScanEntityWithWatchParser parser = new ScanEntityWithWatchParser();
		ScanEntity cmd = parser.split(input, false);
		WatchScanEntityInvoker invoker = new WatchScanEntityInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置命令超时
	 * @param input 输入语句
	 */
	private void doSetCommandTimeout(String input) {
		CommandTimeoutParser parser = new CommandTimeoutParser();
		CommandTimeout cmd = parser.split(input);
		WatchCommandTimeoutInvoker invoker = new WatchCommandTimeoutInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置命令处理模式（在本地进行）
	 * @param input 输入语句
	 */
	private void doSetCommandMode(String input) {
		CommandModeParser parser = new CommandModeParser();
		CommandMode cmd = parser.split(input);
		WatchCommandModeInvoker invoker = new WatchCommandModeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置命令优先级
	 * @param input 输入语句
	 */
	private void doSetCommandRank(String input) {
		CommandRankParser parser = new CommandRankParser();
		CommandRank cmd = parser.split(input);
		WatchCommandRankInvoker invoker = new WatchCommandRankInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置FIXP本地客户端密文超时
	 * @param input 输入语句
	 */
	private void doSetCipherTimeout(String input) {
		CipherTimeoutParser parser = new CipherTimeoutParser();
		CipherTimeout cmd = parser.split(input);
		WatchCipherTimeoutInvoker invoker = new WatchCipherTimeoutInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置分布处理超时
	 * @param input 输入语句
	 */
	private void doSetDistributedTimeout(String input) {
		DistributedTimeoutParser parser = new DistributedTimeoutParser();
		DistributedTimeout cmd = parser.split(input);
		WatchDistributedTimeoutInvoker invoker = new WatchDistributedTimeoutInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置调用器数目
	 * @param input 输入语句
	 */
	private void doSetMaxInvoker(String input) {
		MaxInvokerParser parser = new MaxInvokerParser();
		MaxInvoker cmd = parser.split(input);
		WatchMaxInvokerInvoker invoker = new WatchMaxInvokerInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 检测服务器系统信息
	 * @param input 输入语句
	 */
	private void doCheckSystemInfo(String input) {
		CheckSystemInfoParser parser = new CheckSystemInfoParser();
		CheckSystemInfo cmd = parser.split(input);
		WatchCheckSystemInfoInvoker invoker = new WatchCheckSystemInfoInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 设置映射端口
	 * @param input 输入语句
	 */
	private void doSetReflectPort(String input) {
		ReflectPortParser parser = new ReflectPortParser();
		ReflectPort cmd = parser.split(input);
		WatchReflectPortInvoker invoker = new WatchReflectPortInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 设置异步缓存尺寸
	 * @param input 输入语句
	 */
	private void doSetMaxEchoBuffer(String input) {
		MaxEchoBufferParser parser = new MaxEchoBufferParser();
		MaxEchoBuffer cmd = parser.split(input);
		WatchMaxEchoBufferInvoker invoker = new WatchMaxEchoBufferInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置图标界面显示日志数目
	 * @param input 输入语句
	 */
	private void doSetMaxLogElements(String input) {
		MaxLogElementsParser parser = new MaxLogElementsParser();
		MaxLogElements cmd = parser.split(input);
		WatchMaxLogElementsInvoker invoker = new WatchMaxLogElementsInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 查询FRONT站点分布
	 * @param input 输入语句
	 */
	private void doSeekFrontUser(String input) {
		SeekFrontUserParser parser = new SeekFrontUserParser();
		SeekFrontUser cmd = parser.split(input);
		WatchSeekFrontUserInvoker invoker = new WatchSeekFrontUserInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 查询用户站点分布
	 * @param input 输入语句
	 */
	private void doSeekUserSite(String input) {
		SeekUserSiteParser parser = new SeekUserSiteParser();
		SeekUserSite cmd = parser.split(input);
		WatchSeekUserSiteInvoker invoker = new WatchSeekUserSiteInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 查询用户数据表分布
	 * @param input 输入语句
	 */
	private void doSeekUserTable(String input) {
		SeekUserTableParser parser = new SeekUserTableParser();
		SeekUserTable cmd = parser.split(input);
		WatchSeekUserTableInvoker invoker = new WatchSeekUserTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 查询用户阶段命名分布
	 * @param input 输入语句
	 */
	private void doSeekUserTask(String input) {
		SeekUserTaskParser parser = new SeekUserTaskParser();
		SeekUserTask cmd = parser.split(input);
		WatchSeekUserTaskInvoker invoker = new WatchSeekUserTaskInvoker(cmd);
		launch(invoker);
	}

//	/**
//	 * 发布系统级的分布任务组件
//	 * @param input 输入语句
//	 */
//	private void doPublishMultiTaskComponent(String input) {
//		PublishMultiTaskComponentParser parser = new PublishMultiTaskComponentParser();
//		PublishMultiTaskComponent cmd = parser.split(input);
//		WatchPublishMultiTaskComponentInvoker invoker = new WatchPublishMultiTaskComponentInvoker(cmd);
//		launch(invoker);
//	}
//
//	/**
//	 * 发布系统级的分布任务组件应用附件
//	 * @param input 输入语句
//	 */
//	private void doPublishMultiTaskAssistComponent(String input) {
//		PublishTaskAssistComponentParser parser = new PublishTaskAssistComponentParser();
//		PublishMultiTaskAssistComponent cmd = parser.split(input);
//		WatchPublishMultiTaskAssistComponentInvoker invoker = new WatchPublishMultiTaskAssistComponentInvoker(cmd);
//		launch(invoker);
//	}
//
//	/**
//	 * 发布系统级的分布任务组件动态链接库
//	 * @param input 输入语句
//	 */
//	private void doPublishMultiTaskLibraryComponent(String input) {
//		PublishTaskLibraryComponentParser parser = new PublishTaskLibraryComponentParser();
//		PublishMultiTaskLibraryComponent cmd = parser.split(input);
//		WatchPublishMultiTaskLibraryComponentInvoker invoker = new WatchPublishMultiTaskLibraryComponentInvoker(cmd);
//		launch(invoker);
//	}

	/**
	 * 检索分布任务组件
	 * @param input
	 */
	private void doSeekTask(String input) {
		SeekTaskParser parser = new SeekTaskParser();
		SeekTask cmd = parser.split(input);
		WatchSeekTaskInvoker invoker = new WatchSeekTaskInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置日志级别
	 * @param input 输入语句
	 */
	private void doSetLogLevel(String input) {
		SetLogLevelParser parser = new SetLogLevelParser();
		SetLogLevel cmd = parser.split(input);
		WatchSetLogLevelInvoker invoker = new WatchSetLogLevelInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 重新加载/发布自定义JAR包
	 * @param input 输入语句
	 */
	private void doReloadCustom(String input) {
		ReloadCustomParser parser = new ReloadCustomParser();
		ReloadCustom cmd = parser.split(input);
		WatchReloadCustomInvoker invoker = new WatchReloadCustomInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 半截符编码/解码
	 * @param input 输入语句
	 */
	private void doBuildHalf(String input) {
		BuildHalfParser parser = new BuildHalfParser();
		BuildHalf cmd = parser.split(input);
		WatchBuildHalfInvoker invoker = new WatchBuildHalfInvoker(cmd);
		launch(invoker);
	}

	/**
	 * EACH签名
	 * @param input 输入语句
	 */
	private void doBuildEach(String input) {
		BuildEachParser parser = new BuildEachParser();
		BuildEach cmd = parser.split(input);
		WatchBuildEachInvoker invoker = new WatchBuildEachInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示注册用户状态
	 * @param input 输入语句
	 */
	private void doPrintUserDiagram(String input) {
		PrintUserDiagramParser parser = new PrintUserDiagramParser();
		PrintUserDiagram cmd = parser.split(input);
		WatchPrintUserDiagramInvoker invoker = new WatchPrintUserDiagramInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示注册用户授权
	 * @param input 输入语句
	 */
	private void doPrintGrantDiagram(String input) {
		PrintGrantDiagramParser parser = new PrintGrantDiagramParser();
		PrintGrantDiagram cmd = parser.split(input);
		WatchPrintGrantDiagramInvoker invoker = new WatchPrintGrantDiagramInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示数据库状态
	 * @param input 输入语句
	 */
	private void doPrintSchemaDiagram(String input) {
		PrintSchemaDiagramParser parser = new PrintSchemaDiagramParser();
		PrintSchemaDiagram cmd = parser.split(input, false); // WATCH站点不检查数据库存在
		WatchPrintSchemaDiagramInvoker invoker = new WatchPrintSchemaDiagramInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示数据表状态
	 * @param input 输入语句
	 */
	private void doPrintTableDiagram(String input) {
		PrintTableDiagramParser parser = new PrintTableDiagramParser();
		PrintTableDiagram cmd = parser.split(input, false); // WATCH站点不检查数据库存在
		WatchPrintTableDiagramInvoker invoker = new WatchPrintTableDiagramInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 扫描用户日志
	 * @param input 输入语句
	 */
	private void doScanUserLog(String input) {
		ScanUserLogParser parser = new ScanUserLogParser();
		ScanUserLog cmd = parser.split(input); // 不检查用户签名有效
		WatchScanUserLogInvoker invoker = new WatchScanUserLogInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 被WATCH监视的站点刷新间隔
	 * @param input 输入语句
	 */
	private void doOutlookInterval(String input) {
		OutlookIntervalParser parser = new OutlookIntervalParser();
		OutlookInterval cmd = parser.split(input); // 不检查用户签名有效
		WatchOutlookIntervalInvoker invoker = new WatchOutlookIntervalInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检索在线命令
	 * @param input 输入语句
	 */
	private void doSeekOnlineCommand(String input) {
		SeekOnlineCommandParser parser = new SeekOnlineCommandParser();
		SeekOnlineCommand cmd = parser.split(input); // 不检查用户签名有效
		WatchSeekOnlineCommandInvoker invoker = new WatchSeekOnlineCommandInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检索在线资源
	 * @param input 输入语句
	 */
	private void doSeekOnlineResource(String input) {
		SeekOnlineResourceParser parser = new SeekOnlineResourceParser();
		SeekOnlineResource cmd = parser.split(input); // 不检查用户签名有效
		WatchSeekOnlineResourceInvoker invoker = new WatchSeekOnlineResourceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检索用户在线注册元数据
	 * @param input 输入语句
	 */
	private void doSeekRegisterMetadata(String input) {
		SeekRegisterMetadataParser parser = new SeekRegisterMetadataParser();
		SeekRegisterMetadata cmd = parser.split(input); // 不检查用户签名有效
		WatchSeekRegisterMetadataInvoker invoker = new WatchSeekRegisterMetadataInvoker(cmd);
		launch(invoker);
	}
	/**
	 * 检索用户分布区域
	 * @param input 输入语句
	 */
	private void doSeekUserArea(String input) {
		SeekUserAreaParser parser = new SeekUserAreaParser();
		SeekUserArea cmd = parser.split(input); // 不检查用户签名有效
		WatchSeekUserAreaInvoker invoker = new WatchSeekUserAreaInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private void doMultiGust(String input) {
		MultiGustParser parser = new MultiGustParser();
		MultiGust cmd = parser.split(input); // 解析命令
		WatchMultiGustInvoker invoker = new WatchMultiGustInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private void doGust(String input) {
		GustParser parser = new GustParser();
		Gust cmd = parser.split(input); // 解析命令
		WatchGustInvoker invoker = new WatchGustInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 并行数据传输流量测试
	 * @param input 输入语句
	 */
	private void doParallelMultiSwarm(String input) {
		ParallelMultiSwarmParser parser = new ParallelMultiSwarmParser();
		ParallelMultiSwarm cmd = parser.split(input); // 解析命令
		WatchParallelMultiSwarmInvoker invoker = new WatchParallelMultiSwarmInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 并行数据传输流量测试
	 * @param input 输入语句
	 */
	private void doParallelMultiGust(String input) {
		ParallelMultiGustParser parser = new ParallelMultiGustParser();
		ParallelMultiGust cmd = parser.split(input); // 解析命令
		WatchParallelMultiGustInvoker invoker = new WatchParallelMultiGustInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 持续数据传输流量测试
	 * @param input 输入语句
	 */
	private void doMultiSwarm(String input) {
		MultiSwarmParser parser = new MultiSwarmParser();
		MultiSwarm cmd = parser.split(input); // 解析命令
		WatchMultiSwarmInvoker invoker = new WatchMultiSwarmInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private void doSwarm(String input) {
		SwarmParser parser = new SwarmParser();
		Swarm cmd = parser.split(input, true); // 解析命令
		WatchSwarmInvoker invoker = new WatchSwarmInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 站点连接测试
	 * @param input 输入语句
	 */
	private void doRing(String input) {
		RingParser parser = new RingParser();
		Ring cmd = parser.split(input, true); // 解析命令
		WatchRingInvoker invoker = new WatchRingInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 发布用户到指定站点
	 * @param input 输入语句
	 */
	private void doDeployUser(String input) {
		DeployUserParser parser = new DeployUserParser();
		DeployUser cmd = parser.split(input); // 解析命令
		WatchDeployUserInvoker invoker = new WatchDeployUserInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 发布数据表到指定站点
	 * @param input 输入语句
	 */
	private void doDeployTable(String input) {
		DeployTableParser parser = new DeployTableParser();
		DeployTable cmd = parser.split(input); // 解析命令
		WatchDeployTableInvoker invoker = new WatchDeployTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 从DATA主节点复制数据块到从节点
	 * @param input 输入语句
	 */
	private void doCopyMasterMass(String input) {
		CopyMasterMassParser parser = new CopyMasterMassParser();
		CopyMasterMass cmd = parser.split(input); // 解析命令
		WatchCopyMasterMassInvoker invoker = new WatchCopyMasterMassInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 定位GATE站点模式
	 * @param input 输入语句
	 */
	private void doShadowMode(String input) {
		ShadowModeParser parser = new ShadowModeParser();
		ShadowMode cmd = parser.split(input); // 解析命令
		WatchShadowModeInvoker invoker = new WatchShadowModeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检查GATE站点注册用户和站点编号一致性
	 * @param input 输入语句
	 */
	private void doCheckShadowConsistency(String input) {
		CheckShadowConsistencyParser parser = new CheckShadowConsistencyParser();
		CheckShadowConsistency cmd = parser.split(input); // 解析命令
		WatchCheckShadowConsistencyInvoker invoker = new WatchCheckShadowConsistencyInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 最大CPU限制
	 * @param input 输入语句
	 */
	private void doMostCPU(String input) {
		MostCPUParser parser = new MostCPUParser();
		MostCPU cmd = parser.split(input); // 解析命令
		WatchMostCPUInvoker invoker = new WatchMostCPUInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 最大虚拟机内存限制
	 * @param input 输入语句
	 */
	private void doMostVMMemory(String input) {
		MostVMMemoryParser parser = new MostVMMemoryParser();
		MostVMMemory cmd = parser.split(input); // 解析命令
		WatchMostVMMemoryInvoker invoker = new WatchMostVMMemoryInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 最少内存限制
	 * @param input 输入语句
	 */
	private void doLeastMemory(String input) {
		LeastMemoryParser parser = new LeastMemoryParser();
		LeastMemory cmd = parser.split(input); // 解析命令
		WatchLeastMemoryInvoker invoker = new WatchLeastMemoryInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 最少磁盘空间限制
	 * @param input 输入语句
	 */
	private void doLeastDisk(String input) {
		LeastDiskParser parser = new LeastDiskParser();
		LeastDisk cmd = parser.split(input); // 解析命令
		WatchLeastDiskInvoker invoker = new WatchLeastDiskInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示节点设备目录
	 * @param input 输入语句
	 */
	private void doCheckSitePath(String input) {
		CheckSitePathParser parser = new CheckSitePathParser();
		CheckSitePath cmd = parser.split(input); // 解析命令
		WatchCheckSitePathInvoker invoker = new WatchCheckSitePathInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 从指定节点清除用户记录
	 * @param input 输入语句
	 */
	private void doEraseUser(String input) {
		EraseUserParser parser = new EraseUserParser();
		EraseUser cmd = parser.split(input); // 解析命令

		boolean pass = false;
		// 判断有DATA节点，显示另一种提示
		if (cmd.hasDataSite()) {
			String content = WatchInvoker.getXMLContent("ERASE-USER/ERASE-DATA-BOX");
			pass = confirm(null, content);
		} else {
			String content = WatchInvoker.getXMLContent("ERASE-USER/ERASE-SITE-BOX");
			pass = confirm(null, content);
		}
		// 确定后，才执行
		if (pass) {
			WatchEraseUserInvoker invoker = new WatchEraseUserInvoker(cmd);
			launch(invoker);
		}
	}

	/**
	 * 开放警告消息
	 * @param input 输入语句
	 */
	private void doEnableWarning(String input) {
		EnableWarningParser parser = new EnableWarningParser();
		EnableWarning cmd = parser.split(input);
		WatchEnableWarningInvoker invoker = new WatchEnableWarningInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 开放故障消息
	 * @param input 输入语句
	 */
	private void doEnableFault(String input) {
		EnableFaultParser parser = new EnableFaultParser();
		EnableFault cmd = parser.split(input);
		WatchEnableFaultInvoker invoker = new WatchEnableFaultInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 屏蔽警告消息
	 * @param input 输入语句
	 */
	private void doDisableWarning(String input) {
		DisableWarningParser parser = new DisableWarningParser();
		DisableWarning cmd = parser.split(input);
		WatchDisableWarningInvoker invoker = new WatchDisableWarningInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 屏蔽故障消息
	 * @param input 输入语句
	 */
	private void doDisableFault(String input) {
		DisableFaultParser parser = new DisableFaultParser();
		DisableFault cmd = parser.split(input);
		WatchDisableFaultInvoker invoker = new WatchDisableFaultInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置成员虚拟空间
	 * @param input 输入语句
	 */
	private void doSetMemberCyber(String input) {
		SetMemberCyberParser parser = new SetMemberCyberParser();
		SetMemberCyber cmd = parser.split(input);
		WatchSetMemberCyberInvoker invoker = new WatchSetMemberCyberInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置FRONT在线用户虚拟空间
	 * @param input 输入语句
	 */
	private void doSetFrontCyber(String input) {
		SetFrontCyberParser parser = new SetFrontCyberParser();
		SetFrontCyber cmd = parser.split(input);
		WatchSetFrontCyberInvoker invoker = new WatchSetFrontCyberInvoker(cmd);
		launch(invoker);
	}

	/**
	 *  检测用户虚拟空间
	 * @param input 输入语句
	 */
	private void doCheckUserCyber(String input) {
		CheckUserCyberParser parser = new CheckUserCyberParser();
		CheckUserCyber cmd = parser.split(input);
		WatchCheckUserCyberInvoker invoker = new WatchCheckUserCyberInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 开放Tigger操作类型
	 * @param input 输入语句
	 */
	private void doOpenTigger(String input) {
		OpenTiggerParser parser = new OpenTiggerParser();
		OpenTigger cmd = parser.split(input); // 解析命令
		WatchOpenTiggerInvoker invoker = new WatchOpenTiggerInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 关闭Tigger操作类型
	 * @param input 输入语句
	 */
	private void doCloseTigger(String input) {
		CloseTiggerParser parser = new CloseTiggerParser();
		CloseTigger cmd = parser.split(input); // 解析命令
		WatchCloseTiggerInvoker invoker = new WatchCloseTiggerInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检索分布成员
	 * @param input 输入语句
	 */
	private void doCheckDistributedMember(String input) {
		CheckDistributedMemberParser parser = new CheckDistributedMemberParser();
		CheckDistributedMember cmd = parser.split(input); // 解析命令
		WatchCheckDistributedMemberInvoker invoker = new WatchCheckDistributedMemberInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 部署分布计算包
	 * @param input 输入语句
	 */
	private void doDeployConductPackage(String input) {
		DeployConductPackageParser parser = new DeployConductPackageParser();
		DeployConductPackage cmd = parser.split(input);
		WatchDeployConductPackageInvoker invoker = new WatchDeployConductPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 部署分布数据构建包
	 * @param input 输入语句
	 */
	private void doDeployEstablishPackage(String input) {
		DeployEstablishPackageParser parser = new DeployEstablishPackageParser();
		DeployEstablishPackage cmd = parser.split(input);
		WatchDeployEstablishPackageInvoker invoker = new WatchDeployEstablishPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 部署CONTACT应用包
	 * @param input 输入语句
	 */
	private void doDeployContactPackage(String input) {
		DeployContactPackageParser parser = new DeployContactPackageParser();
		DeployContactPackage cmd = parser.split(input);
		WatchDeployContactPackageInvoker invoker = new WatchDeployContactPackageInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 删除分布计算包
	 * @param input 输入语句
	 */
	private void doDropConductPackage(String input) {
		DropConductPackageParser parser = new DropConductPackageParser();
		DropConductPackage cmd = parser.split(input);
		WatchDropConductPackageInvoker invoker = new WatchDropConductPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 删除分布数据构建包
	 * @param input 输入语句
	 */
	private void doDropEstablishPackage(String input) {
		DropEstablishPackageParser parser = new DropEstablishPackageParser();
		DropEstablishPackage cmd = parser.split(input);
		WatchDropEstablishPackageInvoker invoker = new WatchDropEstablishPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 删除CONTACT应用包
	 * @param input 输入语句
	 */
	private void doDropContactPackage(String input) {
		DropContactPackageParser parser = new DropContactPackageParser();
		DropContactPackage cmd = parser.split(input);
		WatchDropContactPackageInvoker invoker = new WatchDropContactPackageInvoker(cmd);
		launch(invoker);
	}

//	/**
//	 * 发布系统级的任务分布组件
//	 * @param input 输入语句
//	 */
//	public void runPublishMultiTaskComponent(String input) {
//		if (!checkOnline()) return;
//
//		// 检查
//		boolean success = false;
//		try {
//			success = checker.isPublishTaskComponent(input);
//		} catch (SyntaxException e) {
//			fault(e.getMessage());
//		} catch (Throwable e) {
//			fault(e.getMessage());
//		}
//		// 执行结果
//		if (success && confirm()) {
//			doPublishMultiTaskComponent(input);
//		}
//	}
//
//	/**
//	 * 发布系统级的任务组件的应用附件
//	 * @param input 输入语句
//	 */
//	public void runPublishMultiTaskAssistComponent(String input) {
//		if (!checkOnline()) return;
//
//		// 检查
//		boolean success = false;
//		try {
//			success = checker.isPublishTaskAssistComponent(input);
//		} catch (SyntaxException e) {
//			fault(e.getMessage());
//		} catch (Throwable e) {
//			fault(e.getMessage());
//		}
//		// 执行结果
//		if (success && confirm()) {
//			doPublishMultiTaskAssistComponent(input);
//		}
//	}
//
//	/**
//	 * 发布系统级的任务组件的动态链接库
//	 * @param input 输入语句
//	 */
//	public void runPublishMultiTaskLibraryComponent(String input) {
//		if (!checkOnline()) return;
//
//		// 检查
//		boolean success = false;
//		try {
//			success = checker.isPublishTaskLibraryComponent(input);
//		} catch (SyntaxException e) {
//			fault(e.getMessage());
//		} catch (Throwable e) {
//			fault(e.getMessage());
//		}
//		// 执行结果
//		if (success && confirm()) {
//			doPublishMultiTaskLibraryComponent(input);
//		}
//	}

	/**
	 * 发布系统分布计算应用软件包
	 * @param input 输入语句
	 */
	public void runDeployConductPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isDeployConductPackage(input);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doDeployConductPackage(input);
		}
	}
	
	/**
	 * 发布系统数据构建应用软件包
	 * @param input 输入语句
	 */
	public void runDeployEstablishPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isDeployEstablishPackage(input);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doDeployEstablishPackage(input);
		}
	}
	
	/**
	 * 发布系统分布迭代应用软件包
	 * @param input 输入语句
	 */
	public void runDeployContactPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isDeployContactPackage(input);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doDeployContactPackage(input);
		}
	}
	
	/**
	 * 生成散列码
	 * @param input 输入语句
	 */
	public void runBuildHash(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isBuildHash(input);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}

		// 执行结果
		if (success) {
			doBuildHash(input);
		}
	}

	/**
	 * 生成散列码
	 * @param input 输入语句
	 */
	public void runBuildEach(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isBuildEach(input);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}

		// 执行结果
		if (success) {
			doBuildEach(input);
		}
	}
}