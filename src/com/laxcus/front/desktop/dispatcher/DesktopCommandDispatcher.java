/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dispatcher;

import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.*;
import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.command.access.permit.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.cloud.store.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.contact.*;
import com.laxcus.command.cross.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.establish.*;
import com.laxcus.command.forbid.*;
import com.laxcus.command.limit.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.rule.*;
import com.laxcus.command.scan.*;
import com.laxcus.command.site.front.*;
import com.laxcus.command.traffic.*;
import com.laxcus.command.tub.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.desktop.*;
import com.laxcus.front.desktop.invoker.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.listener.*;
import com.laxcus.site.front.*;
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
public class DesktopCommandDispatcher implements CommandDispatcher {

	/** 交互站点启动器 **/
	private static DesktopLauncher launcher;

	/**
	 * 设置交互站点启动器
	 * @param e DesktopLauncher实例
	 */
	public static void setDesktopLauncher(DesktopLauncher e) {
		DesktopCommandDispatcher.launcher = e;
	}

	/**
	 * 返回交互站点启动器
	 * @return DesktopLauncher实例
	 */
	public static DesktopLauncher getDesktopLauncher() {
		return DesktopCommandDispatcher.launcher;
	}

	/** 语法检查器 **/
	private SyntaxChecker checker = new SyntaxChecker();

	/** 语法检查器 **/
	private SyntaxMatcher matcher = new SyntaxMatcher();

	/**
	 * 构造命令转发器
	 */
	public DesktopCommandDispatcher() {
		super();
	}

	/**
	 * 输出语法字节流
	 * @return 返回XML的字节流
	 */
	public byte[] getSyntaxStream() {
		String xmlPath = "conf/desktop/syntax/tokens.xml";
		ResourceLoader loader = new ResourceLoader();
		return loader.findStream(xmlPath);
	}

	/**
	 * 发出警告声音
	 */
	private void playWarning() {
		DesktopCommandDispatcher.launcher.playSound(SoundTag.WARNING);
	}

	/**
	 * 发出错误声音
	 */
	private void playError() {
		DesktopCommandDispatcher.launcher.playSound(SoundTag.ERROR);
	}

	/**
	 * 显示警告信息
	 * @param text 文本
	 */
	private void warning(String text, MeetDisplay display) {
		display.warning(text);
		playWarning();
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
	private FrontInvokerPool getInvokerPool() {
		return (FrontInvokerPool) launcher.getInvokerPool();
	}

	/**
	 * 检查连网状态
	 * @return
	 */
	private boolean checkOnline(MeetDisplay display) {
		// 检查，如果没有登录时，在窗口显示网络已经断开
		boolean online = launcher.getStaffPool().isOnline();
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
	public boolean isDesktop() {
		return launcher.isDesktop();
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.platform.listener.CommandDispatcher#match(java.lang.String, boolean, com.laxcus.ui.display.MeetDisplay)
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
	 * 测试输入语法
	 * @param input 输入语句
	 */
	private boolean match(String input) {
		// 账号
		boolean success = matcher.isCreateUser(input);
		if (!success) success = matcher.isDropUser(input);
		if (!success) success = matcher.isAlterUser(input);
		if (!success) success = matcher.isAssertUser(input);
		if (!success) success = matcher.isPrintUserDiagram(input);
		if (!success) success = matcher.isOpenUser(input);
		if (!success) success = matcher.isCloseUser(input);

		// 权限
		if (!success) success = matcher.isGrant(input);
		if (!success) success = matcher.isRevoke(input);
		if (!success) success = matcher.isPrintGrantDiagram(input);

		// 数据库
		if (!success) success = matcher.isCreateSchema(input);
		if (!success) success = matcher.isDropSchema(input);
		if (!success) success = matcher.isAssertSchema(input);
		if (!success) success = matcher.isScanSchema(input);
		if (!success) success = matcher.isPrintSchemaDiagram(input);
		if (!success) success = matcher.isShowSchema(input);

		// 数据表
		if (!success) success = matcher.isCreateTable(input);
		if (!success) success = matcher.isDropTable(input);
		if (!success) success = matcher.isAssertTable(input);
		if (!success) success = matcher.isScanTable(input);
		if (!success) success = matcher.isPrintTableDiagram(input);
		if (!success) success = matcher.isShowTable(input);
		if (!success) success = matcher.isCheckRemoteTable(input);
		if (!success) success = matcher.isCheckRemoteTask(input);

		// 建立/撤销数据优化时间
		if (!success) success = matcher.isCreateRegulateTime(input);
		if (!success) success = matcher.isDropRegulateTime(input);
		if (!success) success = matcher.isPrintRegulateTime(input);

		// 判断是“建立/删除/显示”限制操作单元
		if (!success) success = matcher.isCreateLimit(input);
		if (!success) success = matcher.isDropLimit(input);
		if (!success) success = matcher.isShowLimit(input);

		// 判断是“提交/撤销/显示”锁定单元
		if (!success) success = matcher.isCreateFault(input);
		if (!success) success = matcher.isDropFault(input);
		if (!success) success = matcher.isShowFault(input);

		// 判断是显示禁止操作单元
		if (!success) success = matcher.isShowForbid(input);
		// 判断是显示事务规则
		if (!success) success = matcher.isShowLockRule(input);

		// 检查/修复表数据一致性
		if (!success) success = matcher.isCheckEntityConsistency(input);
		if (!success) success = matcher.isRecoverEntityConsistency(input);
		// 检查分布数据容量
		if (!success) success = matcher.isScanSketch(input);

		// 数据块强制转换
		if (!success) success = matcher.isRush(input);

		// 数据块
		if (!success) success = matcher.isSetEntitySize(input);
		if (!success) success = matcher.isShowEntitySize(input);
		if (!success) success = matcher.isScanEntity(input);
		if (!success) success = matcher.isGitStubs(input);
		if (!success) success = matcher.isPrintStubsDiagram(input);
		if (!success) success = matcher.isExportEntity(input);
		if (!success) success = matcher.isImportEntity(input);
		if (!success) success = matcher.isCheckEntityContent(input);
		if (!success) success = matcher.isCheckEntityCharset(input);
		if (!success) success = matcher.isCopyEntity(input);

		// 加载/卸载
		if (!success) success = matcher.isLoadIndex(input);
		if (!success) success = matcher.isStopIndex(input);
		if (!success) success = matcher.isLoadEntity(input);
		if (!success) success = matcher.isStopEntity(input);

		// 判断命令
		if (!success) success = matcher.isBuildHash(input);
		// 半截符编码/解码
		if (!success) success = matcher.isBuildHalf(input);
		// EACH签名
		if (!success) success = matcher.isBuildEach(input);
		// 判断是处理模式（内存/磁盘）
		if(!success) success = matcher.isCommandMode(input);
		// 判断是命令超时
		if(!success) success = matcher.isCommandTimeout(input);

		// 密文超时...
		if(!success) success = matcher.isCipherTimeout(input);
		// 最大异步缓存尺寸
		if(!success) success = matcher.isMaxEchoBuffer(input);
		if(!success) success = matcher.isMaxInvoker(input);
		// 打印FRONT网关
		if(!success) success = matcher.isPrintFrontGateway(input);
		// 检测本地系统信息
		if(!success) success = matcher.isCheckSystemInfo(input);
		// 检测内网穿透信道
		if(!success) success = matcher.isCheckPockChannel(input);
		// 检测支持MASSIVE MIMO
		if(!success) success = matcher.isCheckMassiveMimo(input);

		// 设置最大任务数目/最大在线用户 .... 数目
		if (!success) success = matcher.isSetMaxJobs(input);
		if (!success) success = matcher.isSetMaxMembers(input);
		if (!success) success = matcher.isSetMaxTasks(input);
		if (!success) success = matcher.isSetMaxRegulates(input);
		if (!success) success = matcher.isSetMaxSize(input);
		if (!success) success = matcher.isSetMaxGroups(input);
		if (!success) success = matcher.isSetMaxGateways(input);
		if (!success) success = matcher.isSetMaxWorkers(input);
		if (!success) success = matcher.isSetMaxBuilders(input);
		if (!success) success = matcher.isSetMaxTables(input);
		if (!success) success = matcher.isSetMaxIndexes(input);
		if (!success) success = matcher.isSetMaxDSMReduce(input);
		if (!success) success = matcher.isSetExpireTime(input);
		if (!success) success = matcher.isSetMiddleBuffer(input);
		if (!success) success = matcher.isSetCloudSize(input);
		if (!success) success = matcher.isSetUserPriority(input);

		// 开放/关闭共享资源
		if (!success) success = matcher.isOpenShareSchema(input);
		if (!success) success = matcher.isOpenShareTable(input);
		if (!success) success = matcher.isCloseShareSchema(input);
		if (!success) success = matcher.isCloseShareTable(input);
		if (!success) success = matcher.isShowOpenResource(input);
		if (!success) success = matcher.isShowPassiveResource(input);

		// 检索在线命令
		if (!success) success = matcher.isSeekOnlineCommand(input);

		// 重置包尺寸
		if (!success) success = matcher.isReplyPacketMode(input);
		if (!success) success = matcher.isReplyPacketSize(input);
		if (!success) success = matcher.isReplySendTimeout(input);
		if (!success) success = matcher.isReplyReceiveTimeout(input);
		if (!success) success = matcher.isReplyFlowControl(input);
		// 流量测试
		if (!success) success = matcher.isParallelMultiSwarm(input);
		if (!success) success = matcher.isMultiSwarm(input);
		if (!success) success = matcher.isSwarm(input);
		// 释放节点内存
		if (!success) success = matcher.isReleaseMemory(input);
		if (!success) success = matcher.isReleaseMemoryInterval(input);

		// 判断INJECT INTO ... SELECT操作
		if(!success) success = matcher.isInjectSelect(input, false);
		// JOIN操作
		if(!success) success = matcher.isJoin(input);
		// 判断SQL SELECT语句
		if (!success) success = matcher.isSelect(input, false);
		// 判断SQL "DELETE FROM"语句
		if (!success) success = matcher.isDelete(input, false);
		// 判断SQL "INSERT INTO"
		if(!success) success = matcher.isInsert(input, false);
		// 判断SQL "INJECT INTO"
		if(!success) success = matcher.isInject(input, false);
		// 判断SQL "UPDATE ... SET ..."
		if (!success) success = matcher.isUpdate(input, false);
		// 判断分布计算 "CONDUCT ..."
		if (!success) success = matcher.isConduct(input);
		// 判断分布数据构建
		if (!success) success = matcher.isEstablish(input);
		// 分布迭代计算
		if (!success) success = matcher.isContact(input);

		// 执行数据优化
		if (!success) success = matcher.isRegulate(input);
		// 数据调整（regulate在BUILD站点上的操作，使用ESTABLISH命令）
		if (!success) success = matcher.isModulate(input);

		// 更新私有网络空间
		if (!success) success = matcher.isRefreshCyber(input);

		// 最大CPU占比/最大虚拟机内存占比/最小系统内存/磁盘空间限制
		if (!success) success = matcher.isMostCPU(input);
		if (!success) success = matcher.isMostVMMemory(input);
		if (!success) success = matcher.isLeastMemory(input);
		if (!success) success = matcher.isLeastDisk(input);
		if (!success) success = matcher.isCheckSitePath(input);

		// 边缘计算
		if (!success) success = matcher.isRunTubService(input);
		if (!success) success = matcher.isStopTubService(input);
		if (!success) success = matcher.isPrintTubService(input);
		if (!success) success = matcher.isShowTubContainer(input);
		if (!success) success = matcher.isCheckTubListener(input);

		// 分布处理超时
		if(!success) success = matcher.isDistributedTimeout(input);

		// 显示本地应用
		if(!success) success = matcher.isCheckLocalTask(input);
		// 显示连接节点
		if(!success) success = matcher.isCheckRemoteSite(input);
		if(!success) success = matcher.isCheckJobSite(input);
		// 显示日志
		if(!success) success = matcher.isMaxLogElements(input);
		// TIGGER
		if (!success) success = matcher.isOpenTigger(input);
		if (!success) success = matcher.isCloseTigger(input);

		// 检索云应用
		if (!success) success = matcher.isSeekCloudWare(input);

		// 生成包
		if (!success) success = matcher.isBuildConductPackage(input);
		if (!success) success = matcher.isBuildEstablishPackage(input);
		if (!success) success = matcher.isBuildContactPackage(input);

		// 发布包
		if (!success) success = matcher.isDeployConductPackage(input);
		if (!success) success = matcher.isDeployEstablishPackage(input);
		if (!success) success = matcher.isDeployContactPackage(input);

		// 删除包
		if (!success) success = matcher.isDropConductPackage(input);
		if (!success) success = matcher.isDropEstablishPackage(input);
		if (!success) success = matcher.isDropContactPackage(input);
		// 运行分布应用
		if (!success) success = matcher.isRunTask(input);

		// 建立目录/删除目录
		if (!success) success = matcher.isCreateCloudDirectory(input);
		if (!success) success = matcher.isDropCloudDirectory(input);
		if (!success) success = matcher.isDropCloudFile(input);
		if (!success) success = matcher.isRenameCloudDirectory(input);
		if (!success) success = matcher.isRenameCloudFile(input);
		if (!success) success = matcher.isUploadCloudFile(input);
		if (!success) success = matcher.isDownloadCloudFile(input);
		if (!success) success = matcher.isScanCloudDirectory(input);

		// 以上不成立，由自定义命令调用器去判断是属于自定义命令
		if (!success) {
			success = CustomCreator.isCommand(input);
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.platform.listener.CommandDispatcher#check(java.lang.String, boolean, com.laxcus.ui.display.MeetDisplay)
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
	 * 测试输入语法
	 * @param input 输入语句
	 */
	private boolean check(String input) {
		// 账号
		boolean success = checker.isCreateUser(input);
		if (!success) success = checker.isDropUser(input);
		if (!success) success = checker.isAlterUser(input);
		if (!success) success = checker.isAssertUser(input);
		if (!success) success = checker.isPrintUserDiagram(input);
		if (!success) success = checker.isOpenUser(input);
		if (!success) success = checker.isCloseUser(input);

		// 权限
		if (!success) success = checker.isGrant(input);
		if (!success) success = checker.isRevoke(input);
		if (!success) success = checker.isPrintGrantDiagram(input);

		// 数据库
		if (!success) success = checker.isCreateSchema(input);
		if (!success) success = checker.isDropSchema(input);
		if (!success) success = checker.isAssertSchema(input);
		if (!success) success = checker.isScanSchema(input);
		if (!success) success = checker.isPrintSchemaDiagram(input);
		if (!success) success = checker.isShowSchema(input);

		// 数据表
		if (!success) success = checker.isCreateTable(input);
		if (!success) success = checker.isDropTable(input);
		if (!success) success = checker.isAssertTable(input);
		if (!success) success = checker.isScanTable(input);
		if (!success) success = checker.isPrintTableDiagram(input);
		if (!success) success = checker.isShowTable(input);
		if (!success) success = checker.isCheckRemoteTable(input);
		if (!success) success = checker.isCheckRemoteTask(input);

		// 建立/撤销数据优化时间
		if (!success) success = checker.isCreateRegulateTime(input);
		if (!success) success = checker.isDropRegulateTime(input);
		if (!success) success = checker.isPrintRegulateTime(input);

		// 判断是“建立/删除/显示”限制操作单元
		if (!success) success = checker.isCreateLimit(input);
		if (!success) success = checker.isDropLimit(input);
		if (!success) success = checker.isShowLimit(input);

		// 判断是“提交/撤销/显示”锁定单元
		if (!success) success = checker.isCreateFault(input);
		if (!success) success = checker.isDropFault(input);
		if (!success) success = checker.isShowFault(input);

		// 判断是显示禁止操作单元
		if (!success) success = checker.isShowForbid(input);
		// 判断是显示事务规则
		if (!success) success = checker.isShowLockRule(input);

		// 检查/修复表数据一致性
		if (!success) success = checker.isCheckEntityConsistency(input);
		if (!success) success = checker.isRecoverEntityConsistency(input);
		// 检查分布数据容量
		if (!success) success = checker.isScanSketch(input);

		// 数据块强制转换
		if (!success) success = checker.isRush(input);

		// 数据块
		if (!success) success = checker.isSetEntitySize(input);
		if (!success) success = checker.isShowEntitySize(input);
		if (!success) success = checker.isScanEntity(input);
		if (!success) success = checker.isGitStubs(input);
		if (!success) success = checker.isPrintStubsDiagram(input);
		if (!success) success = checker.isExportEntity(input);
		if (!success) success = checker.isImportEntity(input);
		if (!success) success = checker.isCheckEntityContent(input);
		if (!success) success = checker.isCheckEntityCharset(input);
		if (!success) success = checker.isCopyEntity(input);

		// 加载/卸载
		if (!success) success = checker.isLoadIndex(input);
		if (!success) success = checker.isStopIndex(input);
		if (!success) success = checker.isLoadEntity(input);
		if (!success) success = checker.isStopEntity(input);

		// 判断命令
		if (!success) success = checker.isBuildHash(input);
		// 半截符编码/解码
		if (!success) success = checker.isBuildHalf(input);
		// EACH签名
		if (!success) success = checker.isBuildEach(input);
		// 判断是处理模式（内存/磁盘）
		if(!success) success = checker.isCommandMode(input);
		// 判断是命令超时
		if(!success) success = checker.isCommandTimeout(input);

		// 密文超时...
		if(!success) success = checker.isCipherTimeout(input);
		// 最大异步缓存尺寸
		if(!success) success = checker.isMaxEchoBuffer(input);
		if(!success) success = checker.isMaxInvoker(input);
		// 打印FRONT网关
		if(!success) success = checker.isPrintFrontGateway(input);
		// 检测本地系统信息
		if(!success) success = checker.isCheckSystemInfo(input);
		// 检测内网穿透信道
		if(!success) success = checker.isCheckPockChannel(input);
		// 检测支持MASSIVE MIMO
		if(!success) success = checker.isCheckMassiveMimo(input);

		// 设置最大任务数目/最大在线用户 .... 数目
		if (!success) success = checker.isSetMaxJobs(input);
		if (!success) success = checker.isSetMaxMembers(input);
		if (!success) success = checker.isSetMaxTasks(input);
		if (!success) success = checker.isSetMaxRegulates(input);
		if (!success) success = checker.isSetMaxSize(input);
		if (!success) success = checker.isSetMaxGroups(input);
		if (!success) success = checker.isSetMaxGateways(input);
		if (!success) success = checker.isSetMaxWorkers(input);
		if (!success) success = checker.isSetMaxBuilders(input);
		if (!success) success = checker.isSetMaxTables(input);
		if (!success) success = checker.isSetMaxIndexes(input);
		if (!success) success = checker.isSetMaxDSMReduce(input);
		if (!success) success = checker.isSetExpireTime(input);
		if (!success) success = checker.isSetMiddleBuffer(input);
		if (!success) success = checker.isSetCloudSize(input);
		if (!success) success = checker.isSetUserPriority(input);

		// 开放/关闭共享资源
		if (!success) success = checker.isOpenShareSchema(input);
		if (!success) success = checker.isOpenShareTable(input);
		if (!success) success = checker.isCloseShareSchema(input);
		if (!success) success = checker.isCloseShareTable(input);
		if (!success) success = checker.isShowOpenResource(input);
		if (!success) success = checker.isShowPassiveResource(input);

		// 检索在线命令
		if (!success) success = checker.isSeekOnlineCommand(input);

		// 重置包尺寸
		if (!success) success = checker.isReplyPacketMode(input);
		if (!success) success = checker.isReplyPacketSize(input);
		if (!success) success = checker.isReplySendTimeout(input);
		if (!success) success = checker.isReplyReceiveTimeout(input);
		if (!success) success = checker.isReplyFlowControl(input);
		// 流量测试
		if (!success) success = checker.isParallelMultiSwarm(input);
		if (!success) success = checker.isMultiSwarm(input);
		if (!success) success = checker.isSwarm(input);
		// 释放节点内存
		if (!success) success = checker.isReleaseMemory(input);
		if (!success) success = checker.isReleaseMemoryInterval(input);

		// 判断INJECT INTO ... SELECT操作
		if(!success) success = checker.isInjectSelect(input, false);
		// JOIN操作
		if(!success) success = checker.isJoin(input);
		// 判断SQL SELECT语句
		if (!success) success = checker.isSelect(input, false);
		// 判断SQL "DELETE FROM"语句
		if (!success) success = checker.isDelete(input, false);
		// 判断SQL "INSERT INTO"
		if(!success) success = checker.isInsert(input, false);
		// 判断SQL "INJECT INTO"
		if(!success) success = checker.isInject(input, false);
		// 判断SQL "UPDATE ... SET ..."
		if (!success) success = checker.isUpdate(input, false);
		// 判断分布计算 "CONDUCT ..."
		if (!success) success = checker.isConduct(input);
		// 判断分布数据构建
		if (!success) success = checker.isEstablish(input);
		// 分布迭代计算
		if (!success) success = checker.isContact(input);

		// 执行数据优化
		if (!success) success = checker.isRegulate(input);
		// 数据调整（regulate在BUILD站点上的操作，使用ESTABLISH命令）
		if (!success) success = checker.isModulate(input);

		// 更新私有网络空间
		if (!success) success = checker.isRefreshCyber(input);

		// 最大CPU占比/最大虚拟机内存占比/最小系统内存/磁盘空间限制
		if (!success) success = checker.isMostCPU(input);
		if (!success) success = checker.isMostVMMemory(input);
		if (!success) success = checker.isLeastMemory(input);
		if (!success) success = checker.isLeastDisk(input);
		if (!success) success = checker.isCheckSitePath(input);

		// 边缘计算
		if (!success) success = checker.isRunTubService(input);
		if (!success) success = checker.isStopTubService(input);
		if (!success) success = checker.isPrintTubService(input);
		if (!success) success = checker.isShowTubContainer(input);
		if (!success) success = checker.isCheckTubListener(input);

		// 分布处理超时
		if(!success) success = checker.isDistributedTimeout(input);

		// 显示本地应用
		if(!success) success = checker.isCheckLocalTask(input);
		// 显示连接节点
		if(!success) success = checker.isCheckRemoteSite(input);
		if(!success) success = checker.isCheckJobSite(input);
		// 显示日志
		if(!success) success = checker.isMaxLogElements(input);
		// TIGGER
		if (!success) success = checker.isOpenTigger(input);
		if (!success) success = checker.isCloseTigger(input);

		// 检索云应用
		if (!success) success = checker.isSeekCloudWare(input);

		// 生成包
		if (!success) success = checker.isBuildConductPackage(input);
		if (!success) success = checker.isBuildEstablishPackage(input);
		if (!success) success = checker.isBuildContactPackage(input);

		// 发布包
		if (!success) success = checker.isDeployConductPackage(input);
		if (!success) success = checker.isDeployEstablishPackage(input);
		if (!success) success = checker.isDeployContactPackage(input);

		// 删除包
		if (!success) success = checker.isDropConductPackage(input);
		if (!success) success = checker.isDropEstablishPackage(input);
		if (!success) success = checker.isDropContactPackage(input);
		// 运行分布应用
		if (!success) success = checker.isRunTask(input);

		// 建立目录/删除目录
		if (!success) success = checker.isCreateCloudDirectory(input);
		if (!success) success = checker.isDropCloudDirectory(input);
		if (!success) success = checker.isDropCloudFile(input);
		if (!success) success = checker.isRenameCloudDirectory(input);
		if (!success) success = checker.isRenameCloudFile(input);
		if (!success) success = checker.isUploadCloudFile(input);
		if (!success) success = checker.isDownloadCloudFile(input);
		if (!success) success = checker.isScanCloudDirectory(input);

		// 以上不成立，由自定义命令调用器去判断是属于自定义命令
		if (!success) {
			success = CustomCreator.isCommand(input);
		}

		return success;
	}

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

		// 扫描磁盘目录，两种情况：1. 如果是自动，立即执行；2. 不是自动，确认后执行
		if (cmd.getClass() == ScanCloudDirectory.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doScanDirectory((ScanCloudDirectory) cmd, display);
			}
		}
		// 生成目录
		else if (cmd.getClass() == CreateCloudDirectory.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doCreateDirectory((CreateCloudDirectory) cmd, display);
			}
		}
		// 下载文件
		else if (cmd.getClass() == DownloadCloudFile.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doDownloadFile((DownloadCloudFile) cmd, display);
			}
		}
		// 上传文件
		else if (cmd.getClass() == UploadCloudFile.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doUploadFile((UploadCloudFile) cmd, display);
			}
		}
		// 删除云端文件
		else if (cmd.getClass() == DropCloudFile.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doDropCloudFile((DropCloudFile) cmd, display);
			} 
		}
		// 删除云端目录
		else if (cmd.getClass() == DropCloudDirectory.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doDropCloudDirectory((DropCloudDirectory) cmd, display);
			}
		}
		// 修改云端文件
		else if (cmd.getClass() == RenameCloudFile.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doRenameCloudFile((RenameCloudFile) cmd, display);
			} 
		}
		// 修改云端目录
		else if (cmd.getClass() == RenameCloudDirectory.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doRenameCloudDirectory((RenameCloudDirectory) cmd, display);
			}
		}
		// CONDUCT命令
		else if (cmd.getClass() == Conduct.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doConduct((Conduct) cmd, display);
			}
		}
		// CONTACT命令
		else if (cmd.getClass() == Contact.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doContact((Contact) cmd, display);
			}
		}
		// ESTABLISH命令
		else if (cmd.getClass() == Establish.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doEstablish((Establish) cmd, display);
			}
		}
		// RefreshCyber命令
		else if (cmd.getClass() == RefreshCyber.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doRefreshCyber((RefreshCyber) cmd, display);
			}
		}
		// INSERT COMMAND
		else if (cmd.getClass() == Insert.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doInsert((Insert) cmd, display);
			}
		}
		// SELECT COMMAND
		else if (cmd.getClass() == Select.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doSelect((Select) cmd, display);
			}
		}
		// RUN DAPP，执行分布处理命令
		else if (cmd.getClass() == RunTask.class) {
			doRunTask((RunTask) cmd, auto, auditor, display);
		}
		// 检测组件
		else if (cmd.getClass() == CheckRemoteTask.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doCheckRemoteTask((CheckRemoteTask) cmd, display);
			}
		} else if (cmd.getClass() == CheckLocalTask.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doCheckLocalTask((CheckLocalTask) cmd, display);
			}
		}
		// 部署分布式组件：CONDUCT/CONTACT/ESTABLISH
		else if (cmd.getClass() == DeployConductPackage.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doDeployConductPackage((DeployConductPackage) cmd, display);
			}
		} else if (cmd.getClass() == DeployContactPackage.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doDeployContactPackage((DeployContactPackage) cmd, display);
			}
		} else if (cmd.getClass() == DeployEstablishPackage.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doDeployEstablishPackage((DeployEstablishPackage) cmd, display);
			}
		}
		// 建立数据库和建表
		else if (cmd.getClass() == CreateSchema.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doCreateSchema((CreateSchema) cmd, display);
			}
		} else if (cmd.getClass() == CreateTable.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doCreateTable((CreateTable) cmd, display);
			}
		}
		// 诊断数据库和数据表
		else if(cmd.getClass() == AssertSchema.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doAssertSchema((AssertSchema) cmd, display);
			}
		} else if(cmd.getClass() == AssertTable.class) {
			boolean success = (auto ? true : confirm(auditor));
			if (success) {
				doAssertTable((AssertTable) cmd, display);
			}
		}
		//
		else {
			return false;
		}

		return true;
	}

	/**
	 * 执行命令
	 * @param input 输入语句
	 * @return 命令正确且用户确认执行时返回真，否则假
	 */
	private boolean implement(String input, CommandAuditor auditor, MeetDisplay display) {
		// 检查连网状态
		if (!checkOnline(display)) {
			return false;
		}

		boolean pass = false;

		// 数据库
		boolean success = checker.isCreateSchema(input);
		pass = (success && confirm(auditor));
		if (pass) {
			doCreateSchema(input, display);
		}
		if (!success) {
			success = checker.isDropSchema(input);

			if (success) {
				String tag = (isDesktop() ? "DROP-DATABASE/WARNING-CONTENT/CONSOLE" : "DROP-DATABASE/WARNING-CONTENT/TERMINAL");
				String content = DesktopInvoker.getXMLContent(tag);
				pass = confirm(null, content, auditor);
				if (pass) {
					doDropSchema(input, display);
				}
			}
		}
		// 判断数据库存在
		if (!success) {
			success = checker.isAssertSchema(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doAssertSchema(input, display);
			}
		}

		if (!success) {
			success = checker.isPrintSchemaDiagram(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doPrintSchemaDiagram(input, display);
			}
		}
		if (!success) {
			success = checker.isShowSchema(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doShowSchema(input, display);
			}
		}

		if (!success) {
			success = checker.isCreateUser(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCreateUser(input, display);
			}
		}
		if (!success) {
			success = checker.isDropUser(input);

			if (success) {
				String tag = (isDesktop() ? "DROP-USER/WARNING-CONTENT/CONSOLE" : "DROP-USER/WARNING-CONTENT/TERMINAL");
				String content = DesktopInvoker.getXMLContent(tag);
				pass = confirm(null, content, auditor);
				if (pass) {
					doDropUser(input, display);
				}
			}
		}

		if (!success) {
			success = checker.isAlterUser(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doAlterUser(input, display);
			}
		}
		if (!success) {
			success = checker.isAssertUser(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doAssertUser(input, display);
			}
		}
		if (!success) {
			success = checker.isPrintUserDiagram(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doPrintUserDiagram(input, display);
			}
		}
		if (!success) {
			success = checker.isOpenUser(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doOpenUser(input, display);
			}
		}
		if (!success) {
			success = checker.isCloseUser(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCloseUser(input, display);
			}
		}

		if (!success) {
			success = checker.isGrant(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doGrant(input, display);
			}
		}
		if (!success) {
			success = checker.isRevoke(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doRevoke(input, display);
			}
		}
		if (!success) {
			success = checker.isPrintGrantDiagram(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doPrintGrantDiagram(input, display);
			}
		}

		// 表命令
		if (!success) {
			success = checker.isCreateTable(input);
			pass = (success && confirm(auditor));
			if (pass) doCreateTable(input, display);
		}
		if (!success) {
			success = checker.isDropTable(input);
			if (success) {
				String tag = (isDesktop() ? "DROP-TABLE/WARNING-CONTENT/CONSOLE"
						: "DROP-TABLE/WARNING-CONTENT/TERMINAL");
				String content = DesktopInvoker.getXMLContent(tag);
				pass = confirm("", content, auditor);
				if (pass) {
					doDropTable(input, display);
				}
			}
		}
		// 判断数据库存在
		if (!success) {
			success = checker.isAssertTable(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doAssertTable(input, display);
			}
		}

		if (!success) {
			success = checker.isPrintTableDiagram(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doPrintTableDiagram(input, display);
			}
		}
		if (!success) {
			success = checker.isShowTable(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doShowTable(input, display);
			}
		}
		if (!success) {
			pass = success = checker.isCheckRemoteTable(input);
			if (success) {
				doCheckRemoteTable(input, display);
			}
		}
		if (!success) {
			pass = success = checker.isCheckRemoteTask(input);
			if (success) {
				doCheckRemoteTask(input, display);
			}
		}

		// 扫描数据库
		if (!success) {
			success = checker.isScanSchema(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doScanSchema(input, display);
			}
		}
		// 扫描数据表
		if (!success) {
			success = checker.isScanTable(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doScanTable(input, display);
			}
		}

		//		// 发布分布任务组件
		//		if (!success) {
		//			success = checker.isPublishTaskComponent(input);
		//			pass = (success && confirm(auditor));
		//			if (pass) doPublishTaskComponent(input, display);
		//		}
		//		// 发布分布任务组件的应用附件
		//		if (!success) {
		//			success = checker.isPublishTaskAssistComponent(input);
		//			pass = (success && confirm(auditor));
		//			if (pass) doPublishTaskAssistComponent(input, display);
		//		}
		//		// 发布分布任务组件的动态链接库
		//		if (!success) {
		//			success = checker.isPublishTaskLibraryComponent(input);
		//			pass = (success && confirm(auditor));
		//			if (pass) doPublishTaskLibraryComponent(input, display);
		//		}


		// INJECT INTO ... SELECT操作
		if (!success) {
			success = checker.isInjectSelect(input, true);
			pass = (success && confirm(auditor));
			if (pass) {
				doInjectSelect(input, display);
			}
		}
		// JOIN操作
		if (!success) {
			success = checker.isJoin(input);
			pass = (success && confirm(auditor));
			if (pass) {
				join(input, display);
			}
		}

		if (!success) {
			success = checker.isSelect(input, true);
			pass = (success && confirm(auditor));
			if (pass) {
				doSelect(input, display);
			}
		}
		if (!success) {
			success = checker.isDelete(input, true);
			pass = (success && confirm(auditor));
			if (pass) {
				doDelete(input, display);
			}
		}
		if (!success) {
			success = checker.isInsert(input, true);
			pass = (success && confirm(auditor));
			if (pass) {
				doInsert(input, display);
			}
		}
		if (!success) {
			success = checker.isInject(input, true);
			pass = (success && confirm(auditor));
			if (pass) {
				doInject(input, display);
			}
		}
		if (!success) {
			success = checker.isUpdate(input, true);
			pass = (success && confirm(auditor));
			if (pass) {
				doUpdate(input, display);
			}
		}
		// 分布计算
		if (!success) {
			success = checker.isConduct(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doConduct(input, display);
			}
		}
		// ESTABLISH分布数据构建
		if (!success) {
			success = checker.isEstablish(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doEstablish(input, display);
			}
		}
		// CONTACT分布迭代计算
		if (!success) {
			success = checker.isContact(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doContact(input, display);
			}
		}

		if (!success) {
			success = checker.isLoadIndex(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doLoadIndex(input, display);
			}
		}
		if (!success) {
			success = checker.isStopIndex(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doStopIndex(input, display);
			}
		}
		if (!success) {
			success = checker.isLoadEntity(input);
			pass = (success && confirm(auditor)); if(pass) doLoadEntity(input, display);
		}
		if (!success) {
			success = checker.isStopEntity(input);
			pass = (success && confirm(auditor)); if(pass) doStopEntity(input, display);
		}

		// 数据调整
		if (!success) {
			success = checker.isModulate(input);
			pass = (success && confirm(auditor)); if(pass) doModulate(input, display);
		}
		// 数据优化
		if (!success) {
			success = checker.isRegulate(input);
			pass = (success && confirm(auditor)); if(pass) doRegulate(input, display);
		}
		// 建立数据优化时间(每周、每天、每时)
		if (!success) {
			success = checker.isCreateRegulateTime(input);
			pass = (success && confirm(auditor)); if(pass) doCreateRegulateTime(input, display);
		}
		// 撤销数据优化时间(每周、每天、每时)
		if (!success) {
			success = checker.isDropRegulateTime(input);
			pass = (success && confirm(auditor)); if(pass) doDropRegulateTime(input, display);
		}
		// 显示数据优化时间
		if (!success) {
			pass = success = checker.isPrintRegulateTime(input);
			if (success) doPrintRegulateTime(input, display);
		}
		// 检查表数据一致性
		if (!success) {
			success = checker.isCheckEntityConsistency(input);
			pass = (success && confirm(auditor)); if(pass) doCheckEntityConsistency(input, display);
		}
		// 修复表数据一致性
		if (!success) {
			success = checker.isRecoverEntityConsistency(input);
			pass = (success && confirm(auditor)); if(pass) doRecoverEntityConsistency(input, display);
		}

		// 分布数据容量分析
		if (!success) {
			success = checker.isScanSketch(input);
			pass = (success && confirm(auditor)); if(pass) doScanSketch(input, display);
		}

		// 数据块强制转换命令
		if (!success) {
			success = checker.isRush(input);
			pass = (success && confirm(auditor)); if(pass) doRush(input, display);
		}

		// 设置数据块尺寸
		if (!success) {
			success = checker.isSetEntitySize(input);
			pass = (success && confirm(auditor)); if(pass)
				doSetEntitySize(input, display);
		}
		// 显示数据块尺寸
		if (!success) {
			success = checker.isShowEntitySize(input);
			pass = (success && confirm(auditor)); if(pass)
				doShowEntitySize(input, display);
		}
		// 统计数据块尺寸
		if (!success) {
			success = checker.isScanEntity(input);
			pass = (success && confirm(auditor)); if(pass)
				doScanEntity(input, display);
		}
		// 获得数据块编号
		if (!success) {
			success = checker.isGitStubs(input);
			pass = (success && confirm(auditor)); if(pass) doGitStubs(input, display);
		}
		// 显示数据块分布图谱
		if (!success) {
			success = checker.isPrintStubsDiagram(input);
			pass = (success && confirm(auditor)); if(pass) doPrintStubsDiagram(input, display);
		}
		// 获得数据块数据
		if (!success) {
			success = checker.isExportEntity(input);
			pass = (success && confirm(auditor)); if(pass) doExportEntity(input, display);
		}
		// 获得数据块数据
		if (!success) {
			success = checker.isImportEntity(input);
			pass = (success && confirm(auditor)); if(pass) doImportEntity(input, display);
		}
		// 获得数据块数据
		if (!success) {
			success = checker.isCheckEntityContent(input);
			pass = (success && confirm(auditor)); if(pass) doCheckEntityContent(input, display);
		}
		// 判断文件编码
		if (!success) {
			success = checker.isCheckEntityCharset(input);
			pass = (success && confirm(auditor)); if(pass) doCheckEntityCharset(input, display);
		}
		// 获得数据块数据
		if (!success) {
			success = checker.isCopyEntity(input);
			pass = (success && confirm(auditor)); if(pass) doCopyEntity(input, display);
		}

		// 计算散列码命令，HASH命令在本地执行，不需要确认
		if (!success) {
			success = checker.isBuildHash(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doBuildHash(input, display);
			}
		}
		// 半截符编码
		if (!success) {
			success = checker.isBuildHalf(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doBuildHalf(input, display);
			}
		}
		// EACH签名
		if (!success) {
			success = checker.isBuildEach(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doBuildEach(input, display);
			}
		}
		// 命令处理模式。处理模式在本地执行，不需要确认
		if (!success) {
			success = checker.isCommandMode(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetCommandMode(input, display);
			}
		}
		// 命令超时
		if (!success) {
			success = checker.isCommandTimeout(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetCommandTimeout(input, display);
			}
		}

		// 服务器端密文超时
		if (!success) {
			success = checker.isCipherTimeout(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetCipherTimeout(input, display);
			}
		}
		// 最大异步缓存尺寸
		if (!success) {
			success = checker.isMaxEchoBuffer(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxEchoBuffer(input, display);
			}
		}
		// 调用器最大参数
		if (!success) {
			success = checker.isMaxInvoker(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxInvoker(input, display);
			}
		}
		// FRONT网关
		if (!success) {
			success = checker.isPrintFrontGateway(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetPrintFrontGateway(input, display);
			}
		}
		// 检测服务器系统信息
		if (!success) {
			success = checker.isCheckSystemInfo(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCheckSystemInfo(input, display);
			}
		}
		// 检测内网穿透信道
		if (!success) {
			success = checker.isCheckPockChannel(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCheckPockChannel(input, display);
			}
		}
		// 检测支持MASSIVE MIMO
		if (!success) {
			success = checker.isCheckMassiveMimo(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCheckMassiveMimo(input, display);
			}
		}

		// 建立限制操作规则
		if (!success) {
			success = checker.isCreateLimit(input);
			pass = (success && confirm(auditor)); if(pass) {
				doCreateLimit(input, display);
			}
		}
		// 删除限制操作规则
		if (!success) {
			success = checker.isDropLimit(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDropLimit(input, display);
			}
		}
		// 显示限制操作单元
		if (!success) {
			success = checker.isShowLimit(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doShowLimit(input, display);
			}
		}
		// 提交锁定操作
		if (!success) {
			success = checker.isCreateFault(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCreateFault(input, display);
			}
		}
		// 撤销锁定操作
		if (!success) {
			success = checker.isDropFault(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDropFault(input, display);
			}
		}
		// 显示锁定单元
		if (!success) {
			success = checker.isShowFault(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doShowFault(input, display);
			}
		}
		// 显示禁止操作单元
		if (!success) {
			success = checker.isShowForbid(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doShowForbid(input, display);
			}
		}
		// 显示事务规则
		if (!success) {
			success = checker.isShowLockRule(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doShowLockRule(input, display);
			}
		}

		// 设置最大并行任务数
		if (!success) {
			success = checker.isSetMaxJobs(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxJobs(input, display);
			}
		}
		// 设置最大连接数
		if (!success) {
			success = checker.isSetMaxMembers(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxMembers(input, display);
			}
		}
		// 设置最大最多应用软件
		if (!success) {
			success = checker.isSetMaxTasks(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxTasks(input, display);
			}
		}
		// 设置最大优化表数目
		if (!success) {
			success = checker.isSetMaxRegulates(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxRegulates(input, display);
			}
		}
		// 设置最大磁盘空间
		if (!success) {
			success = checker.isSetMaxSize(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxSize(input, display);
			}
		}
		// 设置最大HOME子域集群数目
		if (!success) {
			success = checker.isSetMaxGroups(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxGroups(input, display);
			}
		}
		// 设置最大CALL网关节点数目
		if (!success) {
			success = checker.isSetMaxGateways(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxGateways(input, display);
			}
		}
		// BUILD节点数目
		if (!success) {
			success = checker.isSetMaxBuilders(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxBuilders(input, display);
			}
		}
		// WORK节点数目
		if (!success) {
			success = checker.isSetMaxWorkers(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxWorkers(input, display);
			}
		}
		// 用户账号到期
		if (!success) {
			success = checker.isSetExpireTime(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetExpireTime(input, display);
			}
		}
		if (!success) {
			success = checker.isSetMiddleBuffer(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMiddleBuffer(input, display);
			}
		}
		if (!success) {
			success = checker.isSetCloudSize(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetCloudSize(input, display);
			}
		}
		if (!success) {
			success = checker.isSetUserPriority(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetUserPriority(input, display);
			}
		}

		// 设置最大表数目
		if (!success) {
			success = checker.isSetMaxTables(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxTables(input, display);
			}
		}
		// 设置一个表最大索引数目
		if (!success) {
			success = checker.isSetMaxIndexes(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxIndexes(input, display);
			}
		}
		// 设置DSM表最大压缩倍数
		if (!success) {
			success = checker.isSetMaxDSMReduce(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSetMaxDSMReduce(input, display);
			}
		}

		// 开放/关闭数据库资源
		if (!success) {
			success = checker.isOpenShareSchema(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doOpenShareSchema(input, display);
			}
		}
		if (!success) {
			success = checker.isCloseShareSchema(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCloseShareSchema(input, display);
			}
		}
		// 开放/关闭数据表资源
		if (!success) {
			success = checker.isOpenShareTable(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doOpenShareTable(input, display);
			}
		}
		if (!success) {
			success = checker.isCloseShareTable(input);
			pass = (success && confirm(auditor)); if(pass) {
				doCloseShareTable(input, display);
			}
		}
		// 授权人显示自己开放的数据资源
		if (!success) {
			success = checker.isShowOpenResource(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doShowOpenResource(input, display);
			}
		}
		// 被授权人显示授权人开放的数据资源
		if (!success) {
			success = checker.isShowPassiveResource(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doShowPassiveResource(input, display);
			}
		}

		// 检索在线命令
		if (!success) {
			success = checker.isSeekOnlineCommand(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSeekOnlineCommand(input, display);
			}
		}

		// 并行流量测试
		if (!success) {
			success = checker.isParallelMultiSwarm(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doParallelMultiSwarm(input, display);
			}
		}
		// 数据传输流量持续测试
		if (!success) {
			success = checker.isMultiSwarm(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doMultiSwarm(input, display);
			}
		}
		// 数据传输流量测试
		if (!success) {
			success = checker.isSwarm(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSwarm(input, display);
			}
		}
		// 释放节点内存
		if (!success) {
			success = checker.isReleaseMemory(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doReleaseMemory(input, display);
			}
		}
		// 释放节点内存
		if (!success) {
			success = checker.isReleaseMemoryInterval(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doReleaseMemoryInterval(input, display);
			}
		}
		//		// 重置包尺寸
		//		if (!success) {
		//			success = checker.isReplyPacketSize(input);
		//			pass = (success && confirm(auditor));
		//			if (pass) {
		//				doReplyPacketSize(input, display);
		//			}
		//		}

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
		// 设置数据流队列
		if (!success) {
			success = checker.isReplyFlowControl(input);
			if (success && confirm(auditor)) {
				doReplyFlowControl(input, display);
			}
		}

		// 更新私有网络空间配置
		if (!success) {
			success = checker.isRefreshCyber(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doRefreshCyber(input, 0, display);
			}
		}
		// 最大CPU占比
		if (!success) {
			success = checker.isMostCPU(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doMostCPU(input, display);
			}
		}
		// 最大虚拟机内存占比
		if (!success) {
			success = checker.isMostVMMemory(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doMostVMMemory(input, display);
			}
		}
		// 最少内存限制
		if (!success) {
			success = checker.isLeastMemory(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doLeastMemory(input, display);
			}
		}
		// 最少磁盘空间限制
		if (!success) {
			success = checker.isLeastDisk(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doLeastDisk(input, display);
			}
		}
		// 节点设备目录
		if (!success) {
			success = checker.isCheckSitePath(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCheckSitePath(input, display);
			}
		}

		// 启动连际容器服务
		if (!success) {
			success = checker.isRunTubService(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doRunTubService(input, display);
			}
		}
		// 停止连际容器服务
		if (!success) {
			success = checker.isStopTubService(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doStopTubService(input, display);
			}
		}
		// 显示运行中的容器服务
		if (!success) {
			success = checker.isPrintTubService(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doPrintTubService(input, display);
			}
		}
		// 显示容器组件
		if (!success) {
			success = checker.isShowTubContainer(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doShowTubContainer(input, display);
			}
		}
		// 检测边缘监听地址
		if (!success) {
			pass = success = checker.isCheckTubListener(input);
			if (pass) {
				doCheckTubListener(input, display);
			}
		}
		// 分布处理超时
		if (!success) {
			pass = success = checker.isDistributedTimeout(input);
			if (success) {
				doSetDistributedTimeout(input, display);
			}
		}

		// 打印本地组件
		if (!success) {
			pass = success = checker.isCheckLocalTask(input);
			if (success) {
				doCheckLocalTask(input, display);
			}
		}
		// 显示连接节点
		if (!success) {
			pass = success = checker.isCheckRemoteSite(input);
			if (success) {
				doCheckRemoteSite(input, display);
			}
		}
		// 显示连接节点
		if (!success) {
			pass = success = checker.isCheckJobSite(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCheckJobSite(input, display);
			}
		}
		
		// 最大异步缓存尺寸
		if (!success) {
			pass = success = checker.isMaxLogElements(input);
			if (success) {
				doSetMaxLogElements(input, display);
			}
		}
		// 开放TIGGER类型
		if (!success) {
			success = checker.isOpenTigger(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doOpenTigger(input, display);
			}
		}
		// 关闭TIGGER类型
		if (!success) {
			success = checker.isCloseTigger(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCloseTigger(input, display);
			}
		}
		// 检索云应用
		if (!success) {
			success = checker.isSeekCloudWare(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doSeekCloudWare(input, display);
			}
		}

		// 生成包
		if (!success) {
			success = checker.isBuildConductPackage(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doBuildConductPackage(input, display);
			}
		}
		if (!success) {
			success = checker.isBuildEstablishPackage(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doBuildEstablishPackage(input, display);
			}
		}

		if (!success) {
			success = checker.isBuildContactPackage(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doBuildContactPackage(input, display);
			}
		}

		// 发布包
		if (!success) {
			success = checker.isDeployConductPackage(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDeployConductPackage(input, display);
			}
		}
		if (!success) {
			success = checker.isDeployEstablishPackage(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDeployEstablishPackage(input, display);
			}
		}
		if (!success) {
			success = checker.isDeployContactPackage(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDeployContactPackage(input, display);
			}
		}
		// 删除包
		if (!success) {
			success = checker.isDropConductPackage(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDropConductPackage(input, display);
			}
		}
		if (!success) {
			success = checker.isDropEstablishPackage(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDropEstablishPackage(input, display);
			}
		}
		if (!success) {
			success = checker.isDropContactPackage(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDropContactPackage(input, display);
			}
		}
		// 运行分布应用，转发处理
		if (!success) {
			pass = success = checker.isRunTask(input);
			if (success) {
				doRunTask(input, auditor, display);
			}
		}

		// 建立目录
		if (!success) {
			success = checker.isCreateCloudDirectory(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doCreateCloudDirectory(input, display);
			}
		}
		// 删除目录
		if (!success) {
			success = checker.isDropCloudDirectory(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDropCloudDirectory(input, display);
			}
		}
		// 删除目录
		if (!success) {
			success = checker.isDropCloudFile(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDropCloudFile(input, display);
			}
		}
		// 修改目录名
		if (!success) {
			success = checker.isRenameCloudDirectory(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doRenameCloudDirectory(input, display);
			}
		}
		// 修改文件名
		if (!success) {
			success = checker.isRenameCloudFile(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doRenameCloudFile(input, display);
			}
		}

		// 上传文件
		if (!success) {
			success = checker.isUploadCloudFile(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doUploadCloudFile(input, display);
			}
		}
		// 下载文件
		if (!success) {
			success = checker.isDownloadCloudFile(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doDownloadCloudFile(input, display);
			}
		}
		// 扫描磁盘文件
		if (!success) {
			success = checker.isScanCloudDirectory(input);
			pass = (success && confirm(auditor));
			if (pass) {
				doScanCloudDirectory(input, display);
			}
		}

		// 判断是自定义命令
		if (!success) {
			success = CustomCreator.isCommand(input);
			if (success) {
				EchoInvoker invoker = CustomCreator.createInvoker(input);
				// 如果是基于FrontCustomInvoker实例，设置它的调用器
				if (Laxkit.isClassFrom(invoker, CustomDisplayInvoker.class)) {
					((CustomDisplayInvoker) invoker).setDisplay(display);
				}

				// 确认后执行
				if (confirm(auditor)) {
					launch(invoker, display);
					pass = true;
				}
			}
		}

		// 以上不成功，命令未知
		if (!success) {
			String text = getDesktopLauncher().fault(FaultTip.ILLEGAL_COMMAND);
			fault(text, display);
			return false;
		}

		// 通过且命令追踪接口有效时，记录这个命令
		if (pass) {
			Tigger.command(input);
		}

		return pass;
	}

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
	private void launch(EchoInvoker invoker, MeetDisplay display) {
		if (display != null) {
			if (Laxkit.isClassFrom(invoker, DesktopInvoker.class)) {
				((DesktopInvoker) invoker).setDisplay(display);
			}
		}
		getInvokerPool().launch(invoker);
	}

	/**
	 * 建立数据库
	 */
	private void doCreateSchema(String input, MeetDisplay display) {
		CreateSchemaParser parser = new CreateSchemaParser();
		CreateSchema cmd = parser.split(input, true);
		DesktopCreateSchemaInvoker invoker = new DesktopCreateSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除数据库。<br>
	 * 根据名称解释，被删除的数据库，是当前账号下的一个或者全部数据库，以及数据库下的分布在集群中的全部数据记录。
	 * @param input 输入语句
	 */
	private void doDropSchema(String input, MeetDisplay display) {
		// 解析数据库名称
		DropSchemaParser parser = new DropSchemaParser();
		DropSchema cmd = parser.split(input, true);
		// 生成异步调用器，发送异步命令
		DesktopDropSchemaInvoker invoker = new DesktopDropSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 诊断数据库存在。<br>
	 * @param input 输入语句
	 */
	private void doAssertSchema(String input, MeetDisplay display) {
		// 解析数据库名称
		AssertSchemaParser parser = new AssertSchemaParser();
		AssertSchema cmd = parser.split(input);
		// 生成异步调用器，发送异步命令
		DesktopAssertSchemaInvoker invoker = new DesktopAssertSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示数据库配置
	 * @param input 输入语句
	 */
	private void doShowSchema(String input, MeetDisplay display) {
		ShowSchemaParser parser = new ShowSchemaParser();
		ShowSchema cmd = parser.split(input, true);
		DesktopShowSchemaInvoker invoker = new DesktopShowSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示数据库配置
	 * @param input 输入语句
	 */
	private void doPrintSchemaDiagram(String input, MeetDisplay display) {
		PrintSchemaDiagramParser parser = new PrintSchemaDiagramParser();
		PrintSchemaDiagram cmd = parser.split(input, true);
		DesktopPrintSchemaDiagramInvoker invoker = new DesktopPrintSchemaDiagramInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示数据库状态
	 * @param input 输入语句
	 */
	private void doPrintTableDiagram(String input, MeetDisplay display) {
		PrintTableDiagramParser parser = new PrintTableDiagramParser();
		PrintTableDiagram cmd = parser.split(input, true);
		DesktopPrintTableDiagramInvoker invoker = new DesktopPrintTableDiagramInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 建立一个用户账号
	 * @param input 输入语句
	 */
	private void doCreateUser(String input, MeetDisplay display) {
		CreateUserParser parser = new CreateUserParser();
		CreateUser user = parser.split(input, true);
		DesktopCreateUserInvoker invoker = new DesktopCreateUserInvoker(user);
		launch(invoker, display);
	}

	/**
	 * 删除账号以及账号下所有配置权限、数据库等
	 * @param input 输入语句
	 */
	private void doDropUser(String input, MeetDisplay display) {
		DropUserParser parser = new DropUserParser();
		DropUser cmd = parser.split(input, true);
		DesktopDropUserInvoker invoker = new DesktopDropUserInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 修改账号密码
	 * @param input 输入语句
	 */
	private void doAlterUser(String input, MeetDisplay display) {
		AlterUserParser parser = new AlterUserParser();
		AlterUser cmd = parser.split(input, true);
		DesktopAlterUserInvoker invoker = new DesktopAlterUserInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 判断用户名称
	 * @param input 输入语句
	 */
	private void doAssertUser(String input, MeetDisplay display) {
		AssertUserParser parser = new AssertUserParser();
		AssertUser cmd = parser.split(input);
		DesktopAssertUserInvoker invoker = new DesktopAssertUserInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示注册用户状态
	 * @param input 输入语句
	 */
	private void doPrintUserDiagram(String input, MeetDisplay display) {
		PrintUserDiagramParser parser = new PrintUserDiagramParser();
		PrintUserDiagram cmd = parser.split(input);
		DesktopPrintUserDiagramInvoker invoker = new DesktopPrintUserDiagramInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 禁用用户账号
	 * @param input 输入语句
	 */
	private void doCloseUser(String input, MeetDisplay display) {
		CloseUserParser parser = new CloseUserParser();
		CloseUser cmd = parser.split(input, true);
		DesktopCloseUserInvoker invoker = new DesktopCloseUserInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 开放用户账号
	 * @param input 输入语句
	 */
	private void doOpenUser(String input, MeetDisplay display) {
		OpenUserParser parser = new OpenUserParser();
		OpenUser cmd = parser.split(input, true);
		DesktopOpenUserInvoker invoker = new DesktopOpenUserInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 给注册用户授权
	 * @param input 输入语句
	 */
	private void doGrant(String input, MeetDisplay display) {
		GrantParser parser = new GrantParser();
		Grant grant = parser.split(input, true);
		DesktopGrantInvoker invoker = new DesktopGrantInvoker(grant);
		launch(invoker, display);
	}

	/**
	 * 解除给注册用户的授权
	 * @param input 输入语句
	 */
	private void doRevoke(String input, MeetDisplay display) {
		RevokeParser parser = new RevokeParser();
		Revoke revoke = parser.split(input, true);
		DesktopRevokeInvoker invoker = new DesktopRevokeInvoker(revoke);
		launch(invoker, display);
	}

	/**
	 * 显示注册用户授权
	 * @param input 输入语句
	 */
	private void doPrintGrantDiagram(String input, MeetDisplay display) {
		PrintGrantDiagramParser parser = new PrintGrantDiagramParser();
		PrintGrantDiagram cmd = parser.split(input);
		DesktopPrintGrantDiagramInvoker invoker = new DesktopPrintGrantDiagramInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置数据表的数据块尺寸
	 * @param input 输入语句
	 */
	private void doSetEntitySize(String input, MeetDisplay display) {
		SetEntitySizeParser parser = new SetEntitySizeParser();
		SetEntitySize cmd = parser.split(input, true);
		DesktopSetEntitySizeInvoker invoker = new DesktopSetEntitySizeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 建立数据库表
	 * @param input 输入语句
	 */
	private void doCreateTable(String input, MeetDisplay display) {
		CreateTableParser parser = new CreateTableParser();
		CreateTable cmd = parser.split(input, true);
		// 设置当前持有人签名
		FrontSite local = StaffOnFrontPool.getFrontLauncher().getSite();
		cmd.setIssuer(local.getUser().getUsername());
		// 启动任务
		DesktopCreateTableInvoker invoker = new DesktopCreateTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示表配置参数
	 * @param input 输入语句
	 */
	private void doShowTable(String input, MeetDisplay display) {
		ShowTableParser parser = new ShowTableParser();
		ShowTable cmd = parser.split(input, true);
		DesktopShowTableInvoker invoker = new DesktopShowTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检测远程表配置
	 * @param input 输入语句
	 */
	private void doCheckRemoteTable(String input, MeetDisplay display) {
		CheckRemoteTableParser parser = new CheckRemoteTableParser();
		CheckRemoteTable cmd = parser.split(input, true);
		DesktopCheckRemoteTableInvoker invoker = new DesktopCheckRemoteTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检测远程组件配置
	 * @param input 输入语句
	 */
	private void doCheckRemoteTask(String input, MeetDisplay display) {
		CheckRemoteTaskParser parser = new CheckRemoteTaskParser();
		CheckRemoteTask cmd = parser.split(input, true);
		DesktopCheckRemoteTaskInvoker invoker = new DesktopCheckRemoteTaskInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除数据表
	 * @param input 输入语句
	 */
	private void doDropTable(String input, MeetDisplay display) {
		DropTableParser parser = new DropTableParser();
		DropTable cmd = parser.split(input, true);
		DesktopDropTableInvoker invoker = new DesktopDropTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 诊断数据库存在。<br>
	 * @param input 输入语句
	 */
	private void doAssertTable(String input, MeetDisplay display) {
		// 解析数据库名称
		AssertTableParser parser = new AssertTableParser();
		AssertTable cmd = parser.split(input);
		// 生成异步调用器，发送异步命令
		DesktopAssertTableInvoker invoker = new DesktopAssertTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描数据表资源
	 * @param input 输入语句
	 */
	private void doScanTable(String input, MeetDisplay display) {
		ScanTableParser parser = new ScanTableParser();
		ScanTable cmd = parser.split(input, true);
		DesktopScanTableInvoker invoker = new DesktopScanTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描数据库资源（一个数据库下面包括任意多个表）
	 * @param input 输入语句
	 */
	private void doScanSchema(String input, MeetDisplay display) {
		ScanSchemaParser parser = new ScanSchemaParser();
		ScanSchema cmd = parser.split(input, true);
		DesktopScanSchemaInvoker invoker = new DesktopScanSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 解析/执行JOIN语句
	 * @param input 输入语句
	 */
	private void join(String input, MeetDisplay display) {
		JoinParser parser = new JoinParser();
		Join join = parser.split(input);
		DesktopJoinInvoker invoker = new DesktopJoinInvoker(join);
		launch(invoker, display);
	}

	/**
	 * 执行分布计算操作
	 * @param input 输入语句
	 */
	private void doConduct(String input, MeetDisplay display) {
		ConductParser parser = new ConductParser();
		Conduct conduct = parser.split(input, true);
		DesktopConductInvoker invoker = new DesktopConductInvoker(conduct);
		launch(invoker, display);
	}

	/**
	 * 执行分布计算操作
	 * @param conduct 分布计算
	 */
	private void doConduct(Conduct conduct, MeetDisplay display) {
		DesktopConductInvoker invoker = new DesktopConductInvoker(conduct);
		launch(invoker, display);
	}

	/**
	 * 运行快速计算
	 * @param input 输入语句
	 */
	private void doContact(String input, MeetDisplay display) {
		ContactParser parser = new ContactParser();
		Contact cmd = parser.split(input, true);
		DesktopContactInvoker invoker = new DesktopContactInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 运行快速计算
	 * @param cmd 快速计算
	 */
	private void doContact(Contact cmd, MeetDisplay display) {
		DesktopContactInvoker invoker = new DesktopContactInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 执行分布数据构建
	 * @param input ESTABLISH命令语句
	 */
	private void doEstablish(String input, MeetDisplay display) {
		EstablishParser parser = new EstablishParser();
		Establish estab = parser.split(input, true);
		DesktopEstablishInvoker invoker = new DesktopEstablishInvoker(estab);
		launch(invoker, display);
	}

	/**
	 * 执行分布数据构建
	 * @param input ESTABLISH命令语句
	 */
	private void doEstablish(Establish establish, MeetDisplay display) {
		DesktopEstablishInvoker invoker = new DesktopEstablishInvoker(establish);
		launch(invoker, display);
	}

	/**
	 * 更新私有网络空间
	 * @param cmd 命令 
	 * @param display 显示接口
	 */
	private void doRefreshCyber(RefreshCyber cmd, MeetDisplay display) {
		DesktopRefreshCyberInvoker invoker = new DesktopRefreshCyberInvoker(cmd);
		invoker.setDelayTime(0); // 延时处理时间
		launch(invoker, display);
	}

	/**
	 * 插入一行
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doInsert(Insert cmd, MeetDisplay display) {
		DesktopInsertInvoker invoker = new DesktopInsertInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 查询命令
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doSelect(Select cmd, MeetDisplay display) {
		DesktopSelectInvoker invoker = new DesktopSelectInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 诊断服务器组件
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doCheckRemoteTask(CheckRemoteTask cmd, MeetDisplay display) {
		DesktopCheckRemoteTaskInvoker invoker = new DesktopCheckRemoteTaskInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 诊断本地组件
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doCheckLocalTask(CheckLocalTask cmd, MeetDisplay display) {
		DesktopCheckLocalTaskInvoker invoker = new DesktopCheckLocalTaskInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 发布CONDUCT组件
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doDeployConductPackage(DeployConductPackage cmd, MeetDisplay display) {
		DesktopDeployConductPackageInvoker invoker = new DesktopDeployConductPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 发布CONTACT组件
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doDeployContactPackage(DeployContactPackage cmd, MeetDisplay display) {
		DesktopDeployContactPackageInvoker invoker = new DesktopDeployContactPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 发布ESTABLISH组件
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doDeployEstablishPackage(DeployEstablishPackage cmd, MeetDisplay display) {
		DesktopDeployEstablishPackageInvoker invoker = new DesktopDeployEstablishPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 建立数据库
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doCreateSchema(CreateSchema cmd, MeetDisplay display) {
		DesktopCreateSchemaInvoker invoker = new DesktopCreateSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 建立数据表
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doCreateTable(CreateTable cmd, MeetDisplay display) {
		DesktopCreateTableInvoker invoker = new DesktopCreateTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 判断数据库存在
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doAssertSchema(AssertSchema cmd, MeetDisplay display) {
		DesktopAssertSchemaInvoker invoker = new DesktopAssertSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 判断数据表存在
	 * @param cmd 命令
	 * @param display 显示接口
	 */
	private void doAssertTable(AssertTable cmd, MeetDisplay display) {
		DesktopAssertTableInvoker invoker = new DesktopAssertTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 处理分布式计算任务
	 * @param cmd 运行分布式计算工作
	 * @param auto 自动处理或者否
	 * @param auditor 命令核准器
	 * @param display 交互显示接口
	 */
	private void doRunTask(RunTask cmd, boolean auto, CommandAuditor auditor, MeetDisplay display) {
		RunTaskProcesser processer = new RunTaskProcesser(cmd, auto, auditor, display);
		processer.process();
	}

	/**
	 * SQL "INJECT INTO ... SELCT"异步操作
	 * @param input 输入语句
	 */
	private void doInjectSelect(String input, MeetDisplay display) {
		InjectSelectParser parser = new InjectSelectParser();
		InjectSelect cmd = parser.split(input, true);
		DesktopInjectSelectInvoker invoker = new DesktopInjectSelectInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * SELECT异步操作。
	 * @param input 输入语句
	 */
	private void doSelect(String input, MeetDisplay display) {
		SelectParser parser = new SelectParser();
		Select select = parser.split(input, true);
		DesktopSelectInvoker invoker = new DesktopSelectInvoker(select);
		launch(invoker, display);
	}

	/**
	 * SQL.DELETE异步操作
	 * @param input 输入语句
	 */
	private void doDelete(String input, MeetDisplay display) {
		DeleteParser parser = new DeleteParser();
		Delete delete = parser.split(input, true);
		DesktopDeleteInvoker invoker = new DesktopDeleteInvoker(delete);
		launch(invoker, display);
	}

	/**
	 * 执行SQL.UPDATE命令的异步操作
	 * @param input 输入语句
	 */
	private void doUpdate(String input, MeetDisplay display) {
		UpdateParser parser = new UpdateParser();
		Update update = parser.split(input, true);
		DesktopUpdateInvoker invoker = new DesktopUpdateInvoker(update);
		launch(invoker, display);
	}

	/**
	 * SQL "INSERT INTO"异步操作
	 * @param input 输入语句
	 */
	private void doInsert(String input, MeetDisplay display) {
		InsertParser parser = new InsertParser();
		Insert insert = parser.splitInsert(input, true);
		DesktopInsertInvoker invoker = new DesktopInsertInvoker(insert);
		launch(invoker, display);
	}

	/**
	 * SQL "INJECT INTO"异步操作
	 * @param input 输入语句
	 */
	private void doInject(String input, MeetDisplay display) {
		InsertParser parser = new InsertParser();
		Insert insert = parser.splitInject(input, true);
		DesktopInsertInvoker invoker = new DesktopInsertInvoker(insert);
		launch(invoker, display);
	}

	/**
	 * 数据块强制转化命令（只允许管理员操作）
	 * @param input 输入语句
	 */
	private void doRush(String input, MeetDisplay display) {
		RushParser parser = new RushParser();
		Rush cmd = parser.split(input);
		DesktopRushInvoker invoker = new DesktopRushInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 数据优化命令（普通注册用户操作）
	 * @param input 输入语句
	 */
	private void doRegulate(String input, MeetDisplay display) {		
		RegulateParser parser = new RegulateParser();
		Regulate cmd = parser.split(input);
		DesktopRegulateInvoker invoker = new DesktopRegulateInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 数据调整命令（普通注册用户操作）
	 * @param input 字符串命令
	 */
	private void doModulate(String input, MeetDisplay display) {
		ModulateParser parser = new ModulateParser();
		Modulate cmd = parser.split(input);
		DesktopModulateInvoker invoker = new DesktopModulateInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检查表数据一致
	 * @param input 字符串命令
	 */
	private void doCheckEntityConsistency(String input, MeetDisplay display) {
		CheckEntityConsistencyParser parser = new CheckEntityConsistencyParser();
		CheckEntityConsistency cmd = parser.split(input, true);
		DesktopCheckEntityConsistencyInvoker invoker = new DesktopCheckEntityConsistencyInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 修复表数据一致
	 * @param input 字符串命令
	 */
	private void doRecoverEntityConsistency(String input, MeetDisplay display) {
		RecoverEntityConsistencyParser parser = new RecoverEntityConsistencyParser();
		RecoverEntityConsistency cmd = parser.split(input, true);
		DesktopRecoverEntityConsistencyInvoker invoker = new DesktopRecoverEntityConsistencyInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检查表分布数据容量
	 * @param input 输入语句
	 */
	private void doScanSketch(String input, MeetDisplay display) {
		ScanSketchParser parser = new ScanSketchParser();
		ScanSketch cmd = parser.split(input, true);
		DesktopScanSketchInvoker invoker = new DesktopScanSketchInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 建立数据优化时间（普通注册用户操作）
	 * @param input 输入语句
	 */
	private void doCreateRegulateTime(String input, MeetDisplay display) {
		CreateRegulateTimeParser parser = new CreateRegulateTimeParser();
		CreateRegulateTime cmd = parser.split(input);
		DesktopCreateRegulateTimeInvoker invoker = new DesktopCreateRegulateTimeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 撤销数据优化时间（普通注册用户操作）
	 * @param input 输入语句
	 */
	private void doDropRegulateTime(String input, MeetDisplay display) {
		DropRegulateTimeParser parser = new DropRegulateTimeParser();
		DropRegulateTime cmd = parser.split(input, true);
		DesktopDropRegulateTimeInvoker invoker = new DesktopDropRegulateTimeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 打印数据优化时间（普通注册用户操作）
	 * @param input 输入语句
	 */
	private void doPrintRegulateTime(String input, MeetDisplay display) {
		PrintRegulateTimeParser parser = new PrintRegulateTimeParser();
		PrintRegulateTime cmd = parser.split(input, true);
		DesktopPrintRegulateTimeInvoker invoker = new DesktopPrintRegulateTimeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 分派到DATA站点加载索引
	 * @param input 输入语句
	 */
	private void doLoadIndex(String input, MeetDisplay display) {
		// 检查用户操作权限
		Account account = getDesktopLauncher().getStaffPool().getAccount();
		if (!account.canLoadIndex()) {
			String text = getDesktopLauncher().fault(FaultTip.PERMISSION_MISSING);
			fault(text, display);
			return;
		}

		LoadIndexParser parser = new LoadIndexParser();
		LoadIndex cmd = parser.split(input, true);
		DesktopLoadIndexInvoker invoker = new DesktopLoadIndexInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 从DATA站点卸载索引
	 * @param input 输入语句
	 */
	private void doStopIndex(String input, MeetDisplay display) {
		// 检查用户操作权限
		Account account = getDesktopLauncher().getStaffPool().getAccount();
		if (!account.canLoadIndex()) {
			String text = getDesktopLauncher().fault(FaultTip.PERMISSION_MISSING);
			fault(text, display);
			return;
		}

		StopIndexParser parser = new StopIndexParser();
		StopIndex cmd = parser.split(input, true);
		DesktopStopIndexInvoker invoker = new DesktopStopIndexInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 分派到DATA站点加载数据块
	 * @param input 输入语句
	 */
	private void doLoadEntity(String input, MeetDisplay display) {
		// 检查用户操作权限
		Account account = getDesktopLauncher().getStaffPool().getAccount();
		if (!account.canLoadEntity()) {
			String text = getDesktopLauncher().fault(FaultTip.PERMISSION_MISSING);
			fault(text, display);
			return;
		}

		LoadEntityParser parser = new LoadEntityParser();
		LoadEntity cmd = parser.split(input, true);
		DesktopLoadEntityInvoker invoker = new DesktopLoadEntityInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 从DATA站点卸载数据块
	 * @param input 输入语句
	 */
	private void doStopEntity(String input, MeetDisplay display) {
		// 检查用户操作权限
		Account account = getDesktopLauncher().getStaffPool().getAccount();
		if (!account.canLoadEntity()) {
			String text = getDesktopLauncher().fault(FaultTip.PERMISSION_MISSING);
			fault(text, display);
			return;
		}

		StopEntityParser parser = new StopEntityParser();
		StopEntity cmd = parser.split(input, true);
		DesktopStopEntityInvoker invoker = new DesktopStopEntityInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示某个表下的数据块尺寸
	 * @param input 语句语句
	 */
	private void doShowEntitySize(String input, MeetDisplay display) {
		ShowEntitySizeParser parser = new ShowEntitySizeParser();
		ShowEntitySize cmd = parser.split(input, true);
		DesktopShowEntitySizeInvoker invoker = new DesktopShowEntitySizeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描数据块。流程：FRONT -> CALL -> DATA。
	 * @param input 输入语句
	 */
	private void doScanEntity(String input, MeetDisplay display) {
		ScanEntityParser parser = new ScanEntityParser();
		ScanEntity cmd = parser.split(input, true);
		DesktopScanEntityInvoker invoker = new DesktopScanEntityInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 获取数据块编号。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doGitStubs(String input, MeetDisplay display) {
		GitStubsParser parser = new GitStubsParser();
		GitStubs cmd = parser.split(input, true);
		DesktopGitStubsInvoker invoker = new DesktopGitStubsInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 获取数据块编号。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doPrintStubsDiagram(String input, MeetDisplay display) {
		PrintStubsDiagramParser parser = new PrintStubsDiagramParser();
		PrintStubsDiagram cmd = parser.split(input, true);
		DesktopPrintStubsDiagramInvoker invoker = new DesktopPrintStubsDiagramInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 获取数据块数据。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doExportEntity(String input, MeetDisplay display) {
		ExportEntityParser parser = new ExportEntityParser();
		ExportEntity cmd = parser.split(input, true);
		DesktopExportEntityInvoker invoker = new DesktopExportEntityInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 将文件中的数据导入集群。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doImportEntity(String input, MeetDisplay display) {
		ImportEntityParser parser = new ImportEntityParser();
		ImportEntity cmd = parser.split(input, true);
		DesktopImportEntityInvoker invoker = new DesktopImportEntityInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 获取数据块数据。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doCopyEntity(String input, MeetDisplay display) {
		CopyEntityParser parser = new CopyEntityParser();
		CopyEntity cmd = parser.split(input, true);
		DesktopCopyEntityInvoker invoker = new DesktopCopyEntityInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检测文件内容正确性
	 * @param input 输入语句
	 */
	private void doCheckEntityContent(String input, MeetDisplay display) {
		CheckEntityContentParser parser = new CheckEntityContentParser();
		CheckEntityContent cmd = parser.split(input, true);
		DesktopCheckEntityContentInvoker invoker = new DesktopCheckEntityContentInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 判断文件编码类型
	 * @param input 输入语句
	 */
	private void doCheckEntityCharset(String input, MeetDisplay display) {
		CheckEntityCharsetParser parser = new CheckEntityCharsetParser();
		CheckEntityCharset cmd = parser.split(input, true);
		DesktopCheckEntityCharsetInvoker invoker = new DesktopCheckEntityCharsetInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 计算散列码（在本地进行）
	 * @param input 输入语句
	 */
	private void doBuildHash(String input, MeetDisplay display) {
		BuildHashParser parser = new BuildHashParser();
		BuildHash cmd = parser.split(input);
		DesktopBuildHashInvoker invoker = new DesktopBuildHashInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 半截符编码/解码（在本地进行）
	 * @param input 输入语句
	 */
	private void doBuildHalf(String input, MeetDisplay display) {
		BuildHalfParser parser = new BuildHalfParser();
		BuildHalf cmd = parser.split(input);
		DesktopBuildHalfInvoker invoker = new DesktopBuildHalfInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * EACH签名（在本地进行）
	 * @param input 输入语句
	 */
	private void doBuildEach(String input, MeetDisplay display) {
		BuildEachParser parser = new BuildEachParser();
		BuildEach cmd = parser.split(input);
		DesktopBuildEachInvoker invoker = new DesktopBuildEachInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置命令处理模式（在本地进行）
	 * @param input 输入语句
	 */
	private void doSetCommandMode(String input, MeetDisplay display) {
		CommandModeParser parser = new CommandModeParser();
		CommandMode cmd = parser.split(input);
		DesktopCommandModeInvoker invoker = new DesktopCommandModeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置命令超时
	 * @param input 输入语句
	 */
	private void doSetCommandTimeout(String input, MeetDisplay display) {
		CommandTimeoutParser parser = new CommandTimeoutParser();
		CommandTimeout cmd = parser.split(input);
		DesktopCommandTimeoutInvoker invoker = new DesktopCommandTimeoutInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置密文超时
	 * @param input 输入语句
	 */
	private void doSetCipherTimeout(String input, MeetDisplay display) {
		CipherTimeoutParser parser = new CipherTimeoutParser();
		CipherTimeout cmd = parser.split(input);
		DesktopCipherTimeoutInvoker invoker = new DesktopCipherTimeoutInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 分布处理超时
	 * @param input 输入语句
	 */
	private void doSetDistributedTimeout(String input, MeetDisplay display) {
		DistributedTimeoutParser parser = new DistributedTimeoutParser();
		DistributedTimeout cmd = parser.split(input);
		DesktopDistributedTimeoutInvoker invoker = new DesktopDistributedTimeoutInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 打印本地组件
	 * @param input 输入语句
	 */
	private void doCheckLocalTask(String input, MeetDisplay display) {
		CheckLocalTaskParser parser = new CheckLocalTaskParser();
		CheckLocalTask cmd = parser.split(input);
		DesktopCheckLocalTaskInvoker invoker = new DesktopCheckLocalTaskInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示连接节点
	 * @param input 输入语句
	 */
	private void doCheckRemoteSite(String input, MeetDisplay display) {
		CheckRemoteSiteParser parser = new CheckRemoteSiteParser();
		CheckRemoteSite cmd = parser.split(input);
		DesktopCheckRemoteSiteInvoker invoker = new DesktopCheckRemoteSiteInvoker(cmd);
		launch(invoker, display);
	}
	
	/**
	 * 显示作业节点
	 * @param input 输入语句
	 */
	private void doCheckJobSite(String input, MeetDisplay display) {
		CheckJobSiteParser parser = new CheckJobSiteParser();
		CheckJobSite cmd = parser.split(input);
		DesktopCheckJobSiteInvoker invoker = new DesktopCheckJobSiteInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置调用器数目
	 * @param input 输入语句
	 */
	private void doSetMaxInvoker(String input, MeetDisplay display) {
		MaxInvokerParser parser = new MaxInvokerParser();
		MaxInvoker cmd = parser.split(input);
		DesktopMaxInvokerInvoker invoker = new DesktopMaxInvokerInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置异步缓存
	 * @param input 输入语句
	 */
	private void doSetMaxEchoBuffer(String input, MeetDisplay display) {
		MaxEchoBufferParser parser = new MaxEchoBufferParser();
		MaxEchoBuffer cmd = parser.split(input);
		DesktopMaxEchoBufferInvoker invoker = new DesktopMaxEchoBufferInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检测服务器系统信息
	 * @param input 输入语句
	 */
	private void doCheckSystemInfo(String input, MeetDisplay display) {
		CheckSystemInfoParser parser = new CheckSystemInfoParser();
		CheckSystemInfo cmd = parser.split(input);
		DesktopCheckSystemInfoInvoker invoker = new DesktopCheckSystemInfoInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检测内网穿透信道
	 * @param input 输入语句
	 */
	private void doCheckPockChannel(String input, MeetDisplay display) {
		CheckPockChannelParser parser = new CheckPockChannelParser();
		CheckPockChannel cmd = parser.split(input);
		DesktopCheckPockChannelInvoker invoker = new DesktopCheckPockChannelInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检测支持MASSIVE MIMO
	 * @param input 输入语句
	 */
	private void doCheckMassiveMimo(String input, MeetDisplay display) {
		CheckMassiveMimoParser parser = new CheckMassiveMimoParser();
		CheckMassiveMimo cmd = parser.split(input);
		DesktopCheckMassiveMimoInvoker invoker = new DesktopCheckMassiveMimoInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置密文超时
	 * @param input 输入语句
	 */
	private void doSetMaxLogElements(String input, MeetDisplay display) {
		MaxLogElementsParser parser = new MaxLogElementsParser();
		MaxLogElements cmd = parser.split(input);
		DesktopMaxLogElementsInvoker invoker = new DesktopMaxLogElementsInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 打印FRONT网关
	 * @param input 输入语句
	 */
	private void doSetPrintFrontGateway(String input, MeetDisplay display) {
		PrintFrontGatewayParser parser = new PrintFrontGatewayParser();
		PrintFrontGateway cmd = parser.split(input);
		DesktopPrintFrontGatewayInvoker invoker = new DesktopPrintFrontGatewayInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 建立限制操作
	 * @param input 输入语句
	 */
	private void doCreateLimit(String input, MeetDisplay display) {		
		CreateLimitParser parser = new CreateLimitParser();
		CreateLimit cmd = parser.split(input, true);
		DesktopCreateLimitInvoker invoker = new DesktopCreateLimitInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除限制操作
	 * @param input 输入语句
	 */
	private void doDropLimit(String input, MeetDisplay display) {		
		DropLimitParser parser = new DropLimitParser();
		DropLimit cmd = parser.split(input, true);
		DesktopDropLimitInvoker invoker = new DesktopDropLimitInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示限制操作单元
	 * @param input 输入语句
	 */
	private void doShowLimit(String input, MeetDisplay display) {
		ShowLimitParser parser = new ShowLimitParser();
		ShowLimit cmd = parser.split(input);
		DesktopShowLimitInvoker invoker = new DesktopShowLimitInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 提交锁定操作
	 * @param input 输入语句
	 */
	private void doCreateFault(String input, MeetDisplay display) {
		CreateFaultParser parser = new CreateFaultParser();
		CreateFault cmd = parser.split(input, true);
		DesktopCreateFaultInvoker invoker = new DesktopCreateFaultInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 撤销锁定操作
	 * @param input 输入语句
	 */
	private void doDropFault(String input, MeetDisplay display) {
		DropFaultParser parser = new DropFaultParser();
		DropFault cmd = parser.split(input, true);
		DesktopDropFaultInvoker invoker = new DesktopDropFaultInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示锁定操作
	 * @param input 输入语句
	 */
	private void doShowFault(String input, MeetDisplay display) {
		ShowFaultParser parser = new ShowFaultParser();
		ShowFault cmd = parser.split(input);
		DesktopShowFaultInvoker invoker = new DesktopShowFaultInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示禁止操作单元
	 * @param input 输入语句
	 */
	private void doShowForbid(String input, MeetDisplay display) {
		ShowForbidParser parser = new ShowForbidParser();
		ShowForbid cmd = parser.split(input);
		DesktopShowForbidInvoker invoker = new DesktopShowForbidInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示事务规则
	 * @param input 输入语句
	 */
	private void doShowLockRule(String input, MeetDisplay display) {
		ShowLockRuleParser parser = new ShowLockRuleParser();
		ShowLockRule cmd = parser.split(input);
		DesktopShowLockRuleInvoker invoker = new DesktopShowLockRuleInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置最大并行任务数
	 * @param input 输入语句
	 */
	private void doSetMaxJobs(String input, MeetDisplay display) {
		SetMaxJobsParser parser = new SetMaxJobsParser();
		SetMaxJobs cmd = parser.split(input);
		DesktopSetMaxJobsInvoker invoker = new DesktopSetMaxJobsInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置最大连接数
	 * @param input 输入语句
	 */
	private void doSetMaxMembers(String input, MeetDisplay display) {
		SetMaxMembersParser parser = new SetMaxMembersParser();
		SetMaxMembers cmd = parser.split(input);
		DesktopSetMaxMembersInvoker invoker = new DesktopSetMaxMembersInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置最多应用软件
	 * @param input 输入语句
	 */
	private void doSetMaxTasks(String input, MeetDisplay display) {
		SetMaxTasksParser parser = new SetMaxTasksParser();
		SetMaxTasks cmd = parser.split(input);
		DesktopSetMaxTasksInvoker invoker = new DesktopSetMaxTasksInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置优化表数目
	 * @param input 输入语句
	 */
	private void doSetMaxRegulates(String input, MeetDisplay display) {
		SetMaxRegulatesParser parser = new SetMaxRegulatesParser();
		SetMaxRegulates cmd = parser.split(input);
		DesktopSetMaxRegulatesInvoker invoker = new DesktopSetMaxRegulatesInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置磁盘空间尺寸
	 * @param input 输入语句
	 */
	private void doSetMaxSize(String input, MeetDisplay display) {
		SetMaxSizeParser parser = new SetMaxSizeParser();
		SetMaxSize cmd = parser.split(input);
		DesktopSetMaxSizeInvoker invoker = new DesktopSetMaxSizeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置HOME子域集群数目
	 * @param input 输入语句
	 */
	private void doSetMaxGroups(String input, MeetDisplay display) {
		SetMaxGroupsParser parser = new SetMaxGroupsParser();
		SetMaxGroups cmd = parser.split(input);
		DesktopSetMaxGroupsInvoker invoker = new DesktopSetMaxGroupsInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置CALL网关节点数目
	 * @param input 输入语句
	 */
	private void doSetMaxGateways(String input, MeetDisplay display) {
		SetMaxGatewaysParser parser = new SetMaxGatewaysParser();
		SetMaxGateways cmd = parser.split(input);
		DesktopSetMaxGatewaysInvoker invoker = new DesktopSetMaxGatewaysInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置WORK节点数目
	 * @param input 输入语句
	 */
	private void doSetMaxWorkers(String input, MeetDisplay display) {
		SetMaxWorkersParser parser = new SetMaxWorkersParser();
		SetMaxWorkers cmd = parser.split(input);
		DesktopSetMaxWorkersInvoker invoker = new DesktopSetMaxWorkersInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置BUILD节点数目
	 * @param input 输入语句
	 */
	private void doSetMaxBuilders(String input, MeetDisplay display) {
		SetMaxBuildersParser parser = new SetMaxBuildersParser();
		SetMaxBuilders cmd = parser.split(input);
		DesktopSetMaxBuildersInvoker invoker = new DesktopSetMaxBuildersInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置BUILD节点数目
	 * @param input 输入语句
	 */
	private void doSetExpireTime(String input, MeetDisplay display) {
		SetExpireTimeParser parser = new SetExpireTimeParser();
		SetExpireTime cmd = parser.split(input);
		DesktopSetExpireTimeInvoker invoker = new DesktopSetExpireTimeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置中间缓存尺寸
	 * @param input 输入语句
	 */
	private void doSetMiddleBuffer(String input, MeetDisplay display) {
		SetMiddleBufferParser parser = new SetMiddleBufferParser();
		SetMiddleBuffer cmd = parser.split(input);
		DesktopSetMiddleBufferInvoker invoker = new DesktopSetMiddleBufferInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置云存储空间
	 * @param input 输入语句
	 */
	private void doSetCloudSize(String input, MeetDisplay display) {
		SetCloudSizeParser parser = new SetCloudSizeParser();
		SetCloudSize cmd = parser.split(input);
		DesktopSetCloudSizeInvoker invoker = new DesktopSetCloudSizeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置云存储空间
	 * @param input 输入语句
	 */
	private void doSetUserPriority(String input, MeetDisplay display) {
		SetUserPriorityParser parser = new SetUserPriorityParser();
		SetUserPriority cmd = parser.split(input);
		DesktopSetUserPriorityInvoker invoker = new DesktopSetUserPriorityInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置最大表数目
	 * @param input 输入语句
	 */
	private void doSetMaxTables(String input, MeetDisplay display) {
		SetMaxTablesParser parser = new SetMaxTablesParser();
		SetMaxTables cmd = parser.split(input);
		DesktopSetMaxTablesInvoker invoker = new DesktopSetMaxTablesInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置表最大索引数目
	 * @param input 输入语句
	 */
	private void doSetMaxIndexes(String input, MeetDisplay display) {
		SetMaxIndexesParser parser = new SetMaxIndexesParser();
		SetMaxIndexes cmd = parser.split(input);
		DesktopSetMaxIndexesInvoker invoker = new DesktopSetMaxIndexesInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * DSM表最大压缩倍数
	 * @param input 输入语句
	 */
	private void doSetMaxDSMReduce(String input, MeetDisplay display) {
		SetMaxDSMReduceParser parser = new SetMaxDSMReduceParser();
		SetMaxDSMReduce cmd = parser.split(input);
		DesktopSetMaxDSMReduceInvoker invoker = new DesktopSetMaxDSMReduceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 开放数据库共享给其他用户
	 * @param input 输入语句
	 */
	private void doOpenShareSchema(String input, MeetDisplay display) {
		OpenShareSchemaParser parser = new OpenShareSchemaParser();
		OpenShareSchema cmd = parser.split(input, true);
		DesktopOpenShareSchemaInvoker invoker = new DesktopOpenShareSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 收回共享数据库
	 * @param input 输入语句
	 */
	private void doCloseShareSchema(String input, MeetDisplay display) {
		CloseShareSchemaParser parser = new CloseShareSchemaParser();
		CloseShareSchema cmd = parser.split(input, true);
		DesktopCloseShareSchemaInvoker invoker = new DesktopCloseShareSchemaInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 开放数据表共享给其他用户
	 * @param input 输入语句
	 */
	private void doOpenShareTable(String input, MeetDisplay display) {
		OpenShareTableParser parser = new OpenShareTableParser();
		OpenShareTable cmd = parser.split(input, true);
		DesktopOpenShareTableInvoker invoker = new DesktopOpenShareTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 收回共享数据表
	 * @param input 输入语句
	 */
	private void doCloseShareTable(String input, MeetDisplay display) {
		CloseShareTableParser parser = new CloseShareTableParser();
		CloseShareTable cmd = parser.split(input, true);
		DesktopCloseShareTableInvoker invoker = new DesktopCloseShareTableInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 授权人显示自己开放的数据资源
	 * @param input 输入语句
	 */
	private void doShowOpenResource(String input, MeetDisplay display) {
		ShowOpenResourceParser parser = new ShowOpenResourceParser();
		ShowOpenResource cmd = parser.split(input);
		DesktopShowOpenResourceInvoker invoker = new DesktopShowOpenResourceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 被授权人显示授权人开放给自己的数据资源
	 * @param input 输入语句
	 */
	private void doShowPassiveResource(String input, MeetDisplay display) {
		ShowPassiveResourceParser parser = new ShowPassiveResourceParser();
		ShowPassiveResource cmd = parser.split(input);
		DesktopShowPassiveResourceInvoker invoker = new DesktopShowPassiveResourceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检索在线命令
	 * @param input 输入语句
	 */
	private void doSeekOnlineCommand(String input, MeetDisplay display) {
		SeekOnlineCommandParser parser = new SeekOnlineCommandParser();
		SeekOnlineCommand cmd = parser.split(input);
		DesktopSeekOnlineCommandInvoker invoker = new DesktopSeekOnlineCommandInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 并行数据传输流量测试
	 * @param input 输入语句
	 */
	private void doParallelMultiSwarm(String input, MeetDisplay display) {
		ParallelMultiSwarmParser parser = new ParallelMultiSwarmParser();
		ParallelMultiSwarm cmd = parser.split(input); // 解析命令
		DesktopParallelMultiSwarmInvoker invoker = new DesktopParallelMultiSwarmInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 数据传输流量持续测试
	 * @param input 输入语句
	 */
	private void doMultiSwarm(String input, MeetDisplay display) {
		MultiSwarmParser parser = new MultiSwarmParser();
		MultiSwarm cmd = parser.split(input); // 解析命令
		DesktopMultiSwarmInvoker invoker = new DesktopMultiSwarmInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private void doSwarm(String input, MeetDisplay display) {
		SwarmParser parser = new SwarmParser();
		Swarm cmd = parser.split(input, true); // 解析命令
		DesktopSwarmInvoker invoker = new DesktopSwarmInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 释放节点内存
	 * @param input 输入语句
	 */
	private void doReleaseMemory(String input, MeetDisplay display) {
		ReleaseMemoryParser parser = new ReleaseMemoryParser();
		ReleaseMemory cmd = parser.split(input);
		DesktopReleaseMemoryInvoker invoker = new DesktopReleaseMemoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 节点内存释放间隔时间
	 * @param input 输入语句
	 */
	private void doReleaseMemoryInterval(String input, MeetDisplay display) {
		ReleaseMemoryIntervalParser parser = new ReleaseMemoryIntervalParser();
		ReleaseMemoryInterval cmd = parser.split(input);
		DesktopReleaseMemoryIntervalInvoker invoker = new DesktopReleaseMemoryIntervalInvoker(cmd);
		launch(invoker, display);
	}

	//	/**
	//	 * 重置包尺寸
	//	 * @param input 输入语句
	//	 */
	//	private void doReplyPacketSize(String input, MeetDisplay display) {
	//		ReplyPacketSizeParser parser = new ReplyPacketSizeParser();
	//		ReplyPacketSize cmd = parser.split(input);
	//		DesktopReplyPacketSizeInvoker invoker = new DesktopReplyPacketSizeInvoker(cmd);
	//		launch(invoker, display);
	//	}

	/**
	 * 设置反馈包模式
	 * @param input 输入语句
	 */
	private void doReplyPacketMode(String input, MeetDisplay display) {
		ReplyPacketModeParser parser = new ReplyPacketModeParser();
		ReplyPacketMode cmd = parser.split(input);
		DesktopReplyPacketModeInvoker invoker = new DesktopReplyPacketModeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置反馈包尺寸
	 * @param input 输入语句
	 */
	private void doReplyPacketSize(String input, MeetDisplay display) {
		ReplyPacketSizeParser parser = new ReplyPacketSizeParser();
		ReplyPacketSize cmd = parser.split(input);
		DesktopReplyPacketSizeInvoker invoker = new DesktopReplyPacketSizeInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置发送异步数据超时
	 * @param input 输入语句
	 */
	private void doReplySendTimeout(String input, MeetDisplay display) {
		ReplySendTimeoutParser parser = new ReplySendTimeoutParser();
		ReplySendTimeout cmd = parser.split(input);
		DesktopReplySendTimeoutInvoker invoker = new DesktopReplySendTimeoutInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置接收异步数据超时
	 * @param input 输入语句
	 */
	private void doReplyReceiveTimeout(String input, MeetDisplay display) {
		ReplyReceiveTimeoutParser parser = new ReplyReceiveTimeoutParser();
		ReplyReceiveTimeout cmd = parser.split(input);
		DesktopReplyReceiveTimeoutInvoker invoker = new DesktopReplyReceiveTimeoutInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 设置异步应答流量控制参数
	 * @param input 输入语句
	 */
	private void doReplyFlowControl(String input, MeetDisplay display) {
		ReplyFlowControlParser parser = new ReplyFlowControlParser();
		ReplyFlowControl cmd = parser.split(input);
		DesktopReplyFlowControlInvoker invoker = new DesktopReplyFlowControlInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 更新私有网络空间
	 * @param input 输入语句
	 * @param delayTime 延时处理时间
	 * @return 启动成功返回真，否则假
	 */
	private boolean doRefreshCyber(String input, long delayTime, MeetDisplay display) {
		RefreshCyberParser parser = new RefreshCyberParser();
		RefreshCyber cmd = parser.split(input);

		// 判断当前任务队列为空
		boolean empty = (getInvokerPool().size() == 0);
		if (!empty) {
			String text = getDesktopLauncher().fault(FaultTip.REFUSE_REFRESH_CYBER);
			warning(text, display);
			return false;
		}

		DesktopRefreshCyberInvoker invoker = new DesktopRefreshCyberInvoker(cmd);
		invoker.setDelayTime(delayTime); // 延时处理时间
		launch(invoker, display);
		return true;
	}

	/**
	 * 最大CPU限制
	 * @param input 输入语句
	 */
	private void doMostCPU(String input, MeetDisplay display) {
		MostCPUParser parser = new MostCPUParser();
		MostCPU cmd = parser.split(input);
		DesktopMostCPUInvoker invoker = new DesktopMostCPUInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 最大虚拟机限制
	 * @param input 输入语句
	 */
	private void doMostVMMemory(String input, MeetDisplay display) {
		MostVMMemoryParser parser = new MostVMMemoryParser();
		MostVMMemory cmd = parser.split(input);
		DesktopMostVMMemoryInvoker invoker = new DesktopMostVMMemoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 最低内存限制
	 * @param input 输入语句
	 */
	private void doLeastMemory(String input, MeetDisplay display) {
		LeastMemoryParser parser = new LeastMemoryParser();
		LeastMemory cmd = parser.split(input);
		DesktopLeastMemoryInvoker invoker = new DesktopLeastMemoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 最低磁盘空间限制
	 * @param input 输入语句
	 */
	private void doLeastDisk(String input, MeetDisplay display) {
		LeastDiskParser parser = new LeastDiskParser();
		LeastDisk cmd = parser.split(input);
		DesktopLeastDiskInvoker invoker = new DesktopLeastDiskInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 最低磁盘空间限制
	 * @param input 输入语句
	 */
	private void doCheckSitePath(String input, MeetDisplay display) {
		CheckSitePathParser parser = new CheckSitePathParser();
		CheckSitePath cmd = parser.split(input);
		DesktopCheckSitePathInvoker invoker = new DesktopCheckSitePathInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 启动边缘容器服务
	 * @param input 输入语句
	 */
	private void doRunTubService(String input, MeetDisplay display) {
		RunTubServiceParser parser = new RunTubServiceParser();
		RunTubService cmd = parser.split(input, true);
		DesktopRunTubServiceInvoker invoker = new DesktopRunTubServiceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 停止边缘容器服务
	 * @param input 输入语句
	 */
	private void doStopTubService(String input, MeetDisplay display) {
		StopTubServiceParser parser = new StopTubServiceParser();
		StopTubService cmd = parser.split(input, true);
		DesktopStopTubServiceInvoker invoker = new DesktopStopTubServiceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示边缘容器服务
	 * @param input 输入语句
	 */
	private void doPrintTubService(String input, MeetDisplay display) {
		PrintTubServiceParser parser = new PrintTubServiceParser();
		PrintTubService cmd = parser.split(input, true);
		DesktopPrintTubServiceInvoker invoker = new DesktopPrintTubServiceInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 显示边缘容器服务
	 * @param input 输入语句
	 */
	private void doShowTubContainer(String input, MeetDisplay display) {
		ShowTubContainerParser parser = new ShowTubContainerParser();
		ShowTubContainer cmd = parser.split(input, true);
		DesktopShowTubContainerInvoker invoker = new DesktopShowTubContainerInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 启动边缘容器服务
	 * @param input 输入语句
	 */
	private void doCheckTubListener(String input, MeetDisplay display) {
		CheckTubListenerParser parser = new CheckTubListenerParser();
		CheckTubListener cmd = parser.split(input);
		DesktopCheckTubListenerInvoker invoker = new DesktopCheckTubListenerInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 开放TIGGER操作
	 * @param input 输入语句
	 */
	private void doOpenTigger(String input, MeetDisplay display) {
		OpenTiggerParser parser = new OpenTiggerParser();
		OpenTigger cmd = parser.split(input);
		DesktopOpenTiggerInvoker invoker = new DesktopOpenTiggerInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 关闭TIGGER操作
	 * @param input 输入语句
	 */
	private void doCloseTigger(String input, MeetDisplay display) {
		CloseTiggerParser parser = new CloseTiggerParser();
		CloseTigger cmd = parser.split(input);
		DesktopCloseTiggerInvoker invoker = new DesktopCloseTiggerInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 检索云应用
	 * @param input 输入语句
	 */
	private void doSeekCloudWare(String input, MeetDisplay display) {
		SeekCloudWareParser parser = new SeekCloudWareParser();
		SeekCloudWare cmd = parser.split(input);
		DesktopSeekCloudWareInvoker invoker = new DesktopSeekCloudWareInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 生成分布计算包
	 * @param input 输入语句
	 */
	private void doBuildConductPackage(String input, MeetDisplay display) {
		BuildConductPackageParser parser = new BuildConductPackageParser();
		BuildConductPackage cmd = parser.split(input, true);
		DesktopBuildConductPackageInvoker invoker = new DesktopBuildConductPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 生成分布数据构建包
	 * @param input 输入语句
	 */
	private void doBuildEstablishPackage(String input, MeetDisplay display) {
		BuildEstablishPackageParser parser = new BuildEstablishPackageParser();
		BuildEstablishPackage cmd = parser.split(input, false);
		DesktopBuildEstablishPackageInvoker invoker = new DesktopBuildEstablishPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 生成CONTACT应用包
	 * @param input 输入语句
	 */
	private void doBuildContactPackage(String input, MeetDisplay display) {
		BuildContactPackageParser parser = new BuildContactPackageParser();
		BuildContactPackage cmd = parser.split(input, true);
		DesktopBuildContactPackageInvoker invoker = new DesktopBuildContactPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 部署分布计算包
	 * @param input 输入语句
	 */
	private void doDeployConductPackage(String input, MeetDisplay display) {
		DeployConductPackageParser parser = new DeployConductPackageParser();
		DeployConductPackage cmd = parser.split(input);
		DesktopInvoker invoker = null;

		if (cmd.isSystemWare()) {
			invoker = new DesktopSystemDeployConductPackageInvoker(cmd);
		} else if (cmd.isLocal()) {
			invoker = new DesktopLocalDeployConductPackageInvoker(cmd);
		} else {
			invoker = new DesktopDeployConductPackageInvoker(cmd);
		}

		// 发送
		if (invoker != null) {
			launch(invoker, display);
		}
	}

	/**
	 * 部署分布数据构建包
	 * @param input 输入语句
	 */
	private void doDeployEstablishPackage(String input, MeetDisplay display) {
		DeployEstablishPackageParser parser = new DeployEstablishPackageParser();
		DeployEstablishPackage cmd = parser.split(input);
		DesktopInvoker invoker = null;

		if (cmd.isSystemWare()) {
			invoker = new DesktopSystemDeployEstablishPackageInvoker(cmd);
		} else if (cmd.isLocal()) {
			invoker = new DesktopLocalDeployEstablishPackageInvoker(cmd);
		} else {
			invoker = new DesktopDeployEstablishPackageInvoker(cmd);
		}

		// 发送
		if (invoker != null) {
			launch(invoker, display);
		}
	}

	/**
	 * 部署CONTACT应用包
	 * @param input 输入语句
	 */
	private void doDeployContactPackage(String input, MeetDisplay display) {
		DeployContactPackageParser parser = new DeployContactPackageParser();
		DeployContactPackage cmd = parser.split(input);
		DesktopInvoker invoker = null;

		if (cmd.isSystemWare()) {
			invoker = new DesktopSystemDeployContactPackageInvoker(cmd);
		} else if (cmd.isLocal()) {
			invoker = new DesktopLocalDeployContactPackageInvoker(cmd);
		} else {
			invoker = new DesktopDeployContactPackageInvoker(cmd);
		}

		// 发送
		if (invoker != null) {
			launch(invoker, display);
		}
	}

	/**
	 * 删除分布计算包
	 * @param input 输入语句
	 */
	private void doDropConductPackage(String input, MeetDisplay display) {
		DropConductPackageParser parser = new DropConductPackageParser();
		DropConductPackage cmd = parser.split(input);
		DesktopDropConductPackageInvoker invoker = new DesktopDropConductPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除分布数据构建包
	 * @param input 输入语句
	 */
	private void doDropEstablishPackage(String input, MeetDisplay display) {
		DropEstablishPackageParser parser = new DropEstablishPackageParser();
		DropEstablishPackage cmd = parser.split(input);
		DesktopDropEstablishPackageInvoker invoker = new DesktopDropEstablishPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除CONTACT应用包
	 * @param input 输入语句
	 */
	private void doDropContactPackage(String input, MeetDisplay display) {
		DropContactPackageParser parser = new DropContactPackageParser();
		DropContactPackage cmd = parser.split(input);
		DesktopDropContactPackageInvoker invoker = new DesktopDropContactPackageInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 运行分布式应用
	 * @param input 输入语句
	 */
	private void doRunTask(String input, CommandAuditor auditor, MeetDisplay display) {
		RunTaskParser parser = new RunTaskParser();
		RunTask cmd = parser.split(input, true);
		// 转发处理
		RunTaskProcesser processer = new RunTaskProcesser(cmd, auditor, display);
		processer.process();
	}

	/**
	 * 建立云存储目录
	 * @param input 输入语句
	 */
	private void doCreateCloudDirectory(String input, MeetDisplay display) {
		CreateDirectoryParser parser = new CreateDirectoryParser();
		CreateCloudDirectory cmd = parser.split(input, true);
		DesktopCreateCloudDirectoryInvoker invoker = new DesktopCreateCloudDirectoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除云存储目录
	 * @param input 输入语句
	 */
	private void doDropCloudDirectory(String input, MeetDisplay display) {
		DropCloudDirectoryParser parser = new DropCloudDirectoryParser();
		DropCloudDirectory cmd = parser.split(input, true);
		DesktopDropCloudDirectoryInvoker invoker = new DesktopDropCloudDirectoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除云存储目录
	 * @param input 输入语句
	 */
	private void doDropCloudFile(String input, MeetDisplay display) {
		DropCloudFileParser parser = new DropCloudFileParser();
		DropCloudFile cmd = parser.split(input, true);
		DesktopDropCloudFileInvoker invoker = new DesktopDropCloudFileInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 修改云存储目录
	 * @param input 输入语句
	 */
	private void doRenameCloudDirectory(String input, MeetDisplay display) {
		RenameCloudDirectoryParser parser = new RenameCloudDirectoryParser();
		RenameCloudDirectory cmd = parser.split(input, true);
		DesktopRenameCloudDirectoryInvoker invoker = new DesktopRenameCloudDirectoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 修改云存储目录
	 * @param input 输入语句
	 */
	private void doRenameCloudFile(String input, MeetDisplay display) {
		RenameCloudFileParser parser = new RenameCloudFileParser();
		RenameCloudFile cmd = parser.split(input, true);
		DesktopRenameCloudFileInvoker invoker = new DesktopRenameCloudFileInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 上传文件到云端
	 * @param input 输入语句
	 */
	private void doUploadCloudFile(String input, MeetDisplay display) {
		UploadCloudFileParser parser = new UploadCloudFileParser();
		UploadCloudFile cmd = parser.split(input, true);
		DesktopUploadCloudFileInvoker invoker = new DesktopUploadCloudFileInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 从云端下载文件
	 * @param input 输入语句
	 */
	private void doDownloadCloudFile(String input, MeetDisplay display) {
		DownloadCloudFileParser parser = new DownloadCloudFileParser();
		DownloadCloudFile cmd = parser.split(input, true);
		DesktopDownloadCloudFileInvoker invoker = new DesktopDownloadCloudFileInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描云端目录
	 * @param input 输入语句
	 */
	private void doScanCloudDirectory(String input, MeetDisplay display) {
		ScanCloudDirectoryParser parser = new ScanCloudDirectoryParser();
		ScanCloudDirectory cmd = parser.split(input, true);
		DesktopScanCloudDirectoryInvoker invoker = new DesktopScanCloudDirectoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 扫描云端目录
	 * @param cmd
	 * @param display
	 */
	private void doScanDirectory(ScanCloudDirectory cmd, MeetDisplay display) {
		DesktopScanCloudDirectoryInvoker invoker = new DesktopScanCloudDirectoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 建立云存储目录
	 * @param cmd 命令
	 * @param display
	 */
	private void doCreateDirectory(CreateCloudDirectory cmd, MeetDisplay display) {
		DesktopCreateCloudDirectoryInvoker invoker = new DesktopCreateCloudDirectoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 下载文件
	 * @param cmd 命令
	 * @param display
	 */
	private void doDownloadFile(DownloadCloudFile cmd, MeetDisplay display) {
		DesktopDownloadCloudFileInvoker invoker = new DesktopDownloadCloudFileInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 上传文件
	 * @param cmd 命令
	 * @param display
	 */
	private void doUploadFile(UploadCloudFile cmd, MeetDisplay display) {
		DesktopUploadCloudFileInvoker invoker = new DesktopUploadCloudFileInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除云端文件
	 * @param cmd 命令
	 * @param display
	 */
	private void doDropCloudFile(DropCloudFile cmd, MeetDisplay display) {
		DesktopDropCloudFileInvoker invoker = new DesktopDropCloudFileInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 删除云端目录
	 * @param cmd 命令
	 * @param display
	 */
	private void doDropCloudDirectory(DropCloudDirectory cmd, MeetDisplay display) {
		DesktopDropCloudDirectoryInvoker invoker = new DesktopDropCloudDirectoryInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 修改云端文件
	 * @param cmd 命令
	 * @param display
	 */
	private void doRenameCloudFile(RenameCloudFile cmd, MeetDisplay display) {
		DesktopRenameCloudFileInvoker invoker = new DesktopRenameCloudFileInvoker(cmd);
		launch(invoker, display);
	}

	/**
	 * 修改云端目录
	 * @param cmd 命令
	 * @param display
	 */
	private void doRenameCloudDirectory(RenameCloudDirectory cmd, MeetDisplay display) {
		DesktopRenameCloudDirectoryInvoker invoker = new DesktopRenameCloudDirectoryInvoker(cmd);
		launch(invoker, display);
	}

}