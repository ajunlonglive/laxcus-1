/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet;

import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.*;
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
import com.laxcus.front.meet.invoker.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.front.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
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
public class MeetCommandDispatcher {

	/** 交互站点启动器 **/
	private static MeetLauncher launcher;

	/**
	 * 设置交互站点启动器
	 * @param e MeetLauncher实例
	 */
	public static void setMeetLauncher(MeetLauncher e) {
		MeetCommandDispatcher.launcher = e;
	}

	/**
	 * 返回交互站点启动器
	 * @return MeetLauncher实例
	 */
	public static MeetLauncher getMeetLauncher() {
		return MeetCommandDispatcher.launcher;
	}

	/** 命令核准接口 **/
	private MeetCommandAuditor auditor;

	/** 前端显示器 **/
	private MeetDisplay display;

	/** 语法检查器 **/
	private SyntaxChecker checker = new SyntaxChecker();
	
	/** 命令比较器 **/
	private SyntaxMatcher matcher = new SyntaxMatcher();

	/**
	 * 构造命令转发器
	 */
	public MeetCommandDispatcher() {
		super();
	}

	/**
	 * 发出提示声音
	 */
	private void playMessage() {
		MeetCommandDispatcher.launcher.playSound(SoundTag.MESSAGE);
	}

	/**
	 * 发出警告声音
	 */
	private void playWarning() {
		MeetCommandDispatcher.launcher.playSound(SoundTag.WARNING);
	}
	
	/**
	 * 发出错误声音
	 */
	private void playError() {
		MeetCommandDispatcher.launcher.playSound(SoundTag.ERROR);
	}

	/**
	 * 显示普通消息
	 * @param text 文本
	 */
	private void message(String text) {
		display.message(text);
		playMessage();
	}
	
	/**
	 * 显示警告信息
	 * @param text 文本
	 */
	private void warning(String text) {
		display.warning(text);
		playWarning();
	}

	/**
	 * 弹出错误信息
	 * @param text 文本
	 */
	private void fault(String text) {
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
	private boolean checkOnline() {
		// 检查，如果没有登录时，在窗口显示网络已经断开
		boolean online = launcher.getStaffPool().isOnline();
		if (!online) {
			String text = launcher.fault(FaultTip.OFFLINE_REFUSE);
			fault(text);
			return false;
		}
		return true;
	}

	/**
	 * 设置命令核准接口。
	 * @param e
	 */
	public void setCommandAuditor(MeetCommandAuditor e) {
		auditor = e;
	}

	/**
	 * 返回命令核准接口
	 * @return
	 */
	public MeetCommandAuditor getCommandAuditor() {
		return auditor;
	}

	/**
	 * 构造前端显示器
	 * @param e
	 */
	public void setDisplay(MeetDisplay e) {
		display = e;
	}

	/**
	 * 返回前端显示器
	 * @return
	 */
	public MeetDisplay getDisplay() {
		return display;
	}

	/**
	 * 判断是控制台
	 * @return 返回真或者假
	 */
	public boolean isConsole() {
		return launcher.isConsole();
	}

	/**
	 * 判断是图形终端
	 * @return 返回真或者假
	 */
	public boolean isTerminal() {
		return launcher.isTerminal();
	}

	/**
	 * 在发送命令前，提示用户核准本次操作
	 * @return 确定返回真，否则假
	 */
	private boolean confirm() {
		return auditor.confirm();
	}

	/**
	 * 在发送命令前，提示用户核准本次操作
	 * @param title 提示信息
	 * @return 确定返回真，否则假
	 */
	private boolean confirm(String title) {
		return auditor.confirm(title);
	}

	/**
	 * 在发送命令前，提示用户核准本次操作
	 * @param title 提示信息
	 * @param content 内容
	 * @return 确定返回真，否则假
	 */
	private boolean confirm(String title, String content) {
		return auditor.confirm(title, content);
	}

	/**
	 * 检查用户输入的命令
	 * 
	 * @param input 输入语句
	 * @param throwin 弹出错误
	 * @return 匹配返回真，否则假
	 */
	public boolean match(String input, boolean throwin) {
		try {
			return matching(input, false);
		} catch (SyntaxException e) {
			if (throwin) {
				fault(e.getMessage());
			}
		} catch (Throwable e) {
			if (throwin) {
				fault(e.getMessage());
			}
		}
		return false;
	}
	
	/**
	 * 测试命令
	 * @param input 输入语句
	 * @return 成功返回真，否则假
	 */
	private boolean matching(String input, boolean showResult) {
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

		// 打印语法检测报告
		if (showResult) {
			printSyntax(success);
		}
		// 返回判断结果
		return success;
	}

	/**
	 * 检查用户输入的语法
	 * @param input 输入语句
	 */
	public void check(String input) {
		try {
			testing(input, true);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
	}

	/**
	 * 检查用户输入的语法
	 * @param input 输入语句
	 */
	public boolean check(String input, boolean throwin) {
		try {
			return testing(input, false);
		} catch (SyntaxException e) {
			if (throwin) {
				fault(e.getMessage());
			}
		} catch (Throwable e) {
			if (throwin) {
				fault(e.getMessage());
			}
		}
		return false;
	}
	
	/**
	 * 测试输入语法
	 * @param input 输入语句
	 * @return 成功返回真，否则假
	 */
	private boolean testing(String input, boolean showResult) {
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
		if (!success) success = checker.isScanTable(input);
		if (!success) success = checker.isPrintTableDiagram(input);
		if (!success) success = checker.isShowTable(input);
		if (!success) success = checker.isCheckRemoteTable(input);
		if (!success) success = checker.isCheckRemoteTask(input);


		//		// 发布分布任务组件、码位计算器、快捷组件
		//		if (!success) success = checker.isPublishTaskComponent(input);
		//		if (!success) success = checker.isPublishTaskAssistComponent(input);
		//		if (!success) success = checker.isPublishTaskLibraryComponent(input);

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

		// 打印语法检测报告
		if (showResult) {
			printSyntax(success);
		}
		// 返回判断结果
		return success;
	}

	/**
	 * 打印语法检测结果
	 * @param success
	 */
	private void printSyntax(boolean success) {
		// 在窗口显示检查结果（多语言多语种显示）
		if (success) {
			String text = getMeetLauncher().message(MessageTip.CORRECT_SYNTAX);
			message(text);
		} else {
			String text = getMeetLauncher().fault(FaultTip.INCORRECT_SYNTAX);
			fault(text);
		}
	}

	/**
	 * 提交命令到目标站点
	 * @param input 输入语句
	 * @return 执行执行结果
	 */
	public int submit(String input) {
		boolean success = false;
		try {
			success = implement(input);
		} catch (Throwable e) {
			Logger.fatal(e);
			String msg = e.getMessage();
			fault(msg);
			return MeetSubmit.FAULTED;
		}
		return (success ? MeetSubmit.ACCEPTED : MeetSubmit.CANCELED);
	}


	/**
	 * 执行命令
	 * @param input 输入语句
	 * @return 命令正确且用户确认执行时返回真，否则假
	 */
	private boolean implement(String input) {
		// 检查连网状态
		if (!checkOnline()) {
			return false;
		}

		boolean pass = false;

		// 数据库
		boolean success = checker.isCreateSchema(input);
		pass = (success && confirm());
		if (pass) {
			doCreateSchema(input);
		}
		if (!success) {
			success = checker.isDropSchema(input);

			if (success) {
				String tag = (isConsole() ? "DROP-DATABASE/WARNING-CONTENT/CONSOLE" : "DROP-DATABASE/WARNING-CONTENT/TERMINAL");
				String content = MeetInvoker.getXMLContent(tag);
				pass = confirm(null, content);
				if (pass) {
					doDropSchema(input);
				}
			}
		}
		// 判断数据库存在
		if (!success) {
			success = checker.isAssertSchema(input);
			pass = (success && confirm());
			if (pass) {
				doAssertSchema(input);
			}
		}

		if (!success) {
			success = checker.isPrintSchemaDiagram(input);
			pass = (success && confirm());
			if (pass) {
				doPrintSchemaDiagram(input);
			}
		}
		if (!success) {
			success = checker.isShowSchema(input);
			pass = (success && confirm());
			if (pass) {
				doShowSchema(input);
			}
		}

		if (!success) {
			success = checker.isCreateUser(input);
			pass = (success && confirm());
			if (pass) {
				doCreateUser(input);
			}
		}
		if (!success) {
			success = checker.isDropUser(input);

			if (success) {
				String tag = (isConsole() ? "DROP-USER/WARNING-CONTENT/CONSOLE" : "DROP-USER/WARNING-CONTENT/TERMINAL");
				String content = MeetInvoker.getXMLContent(tag);
				pass = confirm(null, content);
				if (pass) {
					doDropUser(input);
				}
			}
		}

		if (!success) {
			success = checker.isAlterUser(input);
			pass = (success && confirm());
			if (pass) {
				doAlterUser(input);
			}
		}
		if (!success) {
			success = checker.isAssertUser(input);
			pass = (success && confirm());
			if (pass) {
				doAssertUser(input);
			}
		}
		if (!success) {
			success = checker.isPrintUserDiagram(input);
			pass = (success && confirm()); if(pass) {
				doPrintUserDiagram(input);
			}
		}
		if (!success) {
			success = checker.isOpenUser(input);
			pass = (success && confirm());
			if (pass) {
				doOpenUser(input);
			}
		}
		if (!success) {
			success = checker.isCloseUser(input);
			pass = (success && confirm());
			if (pass) {
				doCloseUser(input);
			}
		}

		if (!success) {
			success = checker.isGrant(input);
			pass = (success && confirm());
			if (pass) {
				doGrant(input);
			}
		}
		if (!success) {
			success = checker.isRevoke(input);
			pass = (success && confirm()); 
			if(pass) {
				doRevoke(input);
			}
		}
		if (!success) {
			success = checker.isPrintGrantDiagram(input);
			pass = (success && confirm()); 
			if(pass) {
				doPrintGrantDiagram(input);
			}
		}

		// 表命令
		if (!success) {
			success = checker.isCreateTable(input);
			pass = (success && confirm());
			if (pass) doCreateTable(input);
		}
		if (!success) {
			success = checker.isDropTable(input);
			if (success) {
				String tag = (isConsole() ? "DROP-TABLE/WARNING-CONTENT/CONSOLE"
						: "DROP-TABLE/WARNING-CONTENT/TERMINAL");
				String content = MeetInvoker.getXMLContent(tag);
				pass = confirm("", content);
				if (pass) {
					doDropTable(input);
				}
			}
		}
		if (!success) {
			success = checker.isPrintTableDiagram(input);
			pass = (success && confirm()); if(pass) doPrintTableDiagram(input);
		}
		if (!success) {
			success = checker.isShowTable(input);
			pass = (success && confirm()); if(pass) doShowTable(input);
		}
		if (!success) {
			pass = success = checker.isCheckRemoteTable(input);
			if (success) doCheckRemoteTable(input);
		}
		if (!success) {
			pass = success = checker.isCheckRemoteTask(input);
			if (success) doCheckRemoteTask(input);
		}

		// 扫描数据库
		if (!success) {
			success = checker.isScanSchema(input);
			pass = (success && confirm()); if(pass) {
				doScanSchema(input);
			}
		}
		// 扫描数据表
		if (!success) {
			success = checker.isScanTable(input);
			pass = (success && confirm()); if(pass) {
				doScanTable(input);
			}
		}

		//		// 发布分布任务组件
		//		if (!success) {
		//			success = checker.isPublishTaskComponent(input);
		//			pass = (success && confirm());
		//			if (pass) doPublishTaskComponent(input);
		//		}
		//		// 发布分布任务组件的应用附件
		//		if (!success) {
		//			success = checker.isPublishTaskAssistComponent(input);
		//			pass = (success && confirm());
		//			if (pass) doPublishTaskAssistComponent(input);
		//		}
		//		// 发布分布任务组件的动态链接库
		//		if (!success) {
		//			success = checker.isPublishTaskLibraryComponent(input);
		//			pass = (success && confirm());
		//			if (pass) doPublishTaskLibraryComponent(input);
		//		}


		// INJECT INTO ... SELECT操作
		if (!success) {
			success = checker.isInjectSelect(input, true);
			pass = (success && confirm()); if(pass) doInjectSelect(input);
		}
		// JOIN操作
		if (!success) {
			success = checker.isJoin(input);
			pass = (success && confirm()); if(pass) join(input);
		}

		if (!success) {
			success = checker.isSelect(input, true);
			pass = (success && confirm()); if(pass)
				doSelect(input);
		}
		if (!success) {
			success = checker.isDelete(input, true);
			pass = (success && confirm());
			if (pass) doDelete(input);
		}
		if (!success) {
			success = checker.isInsert(input, true);
			pass = (success && confirm());
			if (pass) doInsert(input);
		}
		if (!success) {
			success = checker.isInject(input, true);
			pass = (success && confirm()); if(pass) doInject(input);
		}
		if (!success) {
			success = checker.isUpdate(input, true);
			pass = (success && confirm()); if(pass) doUpdate(input);
		}
		// 分布计算
		if (!success) {
			success = checker.isConduct(input);
			pass = (success && confirm()); if(pass) doConduct(input);
		}
		// ESTABLISH分布数据构建
		if (!success) {
			success = checker.isEstablish(input);
			pass = (success && confirm()); if(pass) doEstablish(input);
		}
		// CONTACT分布迭代计算
		if (!success) {
			success = checker.isContact(input);
			pass = (success && confirm()); if(pass) doContact(input); 
		}

		if (!success) {
			success = checker.isLoadIndex(input);
			pass = (success && confirm()); if(pass)
				doLoadIndex(input);
		}
		if (!success) {
			success = checker.isStopIndex(input);
			pass = (success && confirm()); if(pass)
				doStopIndex(input);
		}
		if (!success) {
			success = checker.isLoadEntity(input);
			pass = (success && confirm()); if(pass) doLoadEntity(input);
		}
		if (!success) {
			success = checker.isStopEntity(input);
			pass = (success && confirm()); if(pass) doStopEntity(input);
		}

		// 数据调整
		if (!success) {
			success = checker.isModulate(input);
			pass = (success && confirm()); if(pass) doModulate(input);
		}
		// 数据优化
		if (!success) {
			success = checker.isRegulate(input);
			pass = (success && confirm()); if(pass) doRegulate(input);
		}
		// 建立数据优化时间(每周、每天、每时)
		if (!success) {
			success = checker.isCreateRegulateTime(input);
			pass = (success && confirm()); if(pass) doCreateRegulateTime(input);
		}
		// 撤销数据优化时间(每周、每天、每时)
		if (!success) {
			success = checker.isDropRegulateTime(input);
			pass = (success && confirm()); if(pass) doDropRegulateTime(input);
		}
		// 显示数据优化时间
		if (!success) {
			pass = success = checker.isPrintRegulateTime(input);
			if (success) doPrintRegulateTime(input);
		}
		// 检查表数据一致性
		if (!success) {
			success = checker.isCheckEntityConsistency(input);
			pass = (success && confirm()); if(pass) doCheckEntityConsistency(input);
		}
		// 修复表数据一致性
		if (!success) {
			success = checker.isRecoverEntityConsistency(input);
			pass = (success && confirm()); if(pass) doRecoverEntityConsistency(input);
		}

		// 分布数据容量分析
		if (!success) {
			success = checker.isScanSketch(input);
			pass = (success && confirm()); if(pass) doScanSketch(input);
		}

		// 数据块强制转换命令
		if (!success) {
			success = checker.isRush(input);
			pass = (success && confirm()); if(pass) doRush(input);
		}

		// 设置数据块尺寸
		if (!success) {
			success = checker.isSetEntitySize(input);
			pass = (success && confirm()); if(pass)
				doSetEntitySize(input);
		}
		// 显示数据块尺寸
		if (!success) {
			success = checker.isShowEntitySize(input);
			pass = (success && confirm()); if(pass)
				doShowEntitySize(input);
		}
		// 统计数据块尺寸
		if (!success) {
			success = checker.isScanEntity(input);
			pass = (success && confirm()); if(pass)
				doScanEntity(input);
		}
		// 获得数据块编号
		if (!success) {
			success = checker.isGitStubs(input);
			pass = (success && confirm()); if(pass) doGitStubs(input);
		}
		// 显示数据块分布图谱
		if (!success) {
			success = checker.isPrintStubsDiagram(input);
			pass = (success && confirm()); if(pass) doPrintStubsDiagram(input);
		}
		// 获得数据块数据
		if (!success) {
			success = checker.isExportEntity(input);
			pass = (success && confirm()); if(pass) doExportEntity(input);
		}
		// 获得数据块数据
		if (!success) {
			success = checker.isImportEntity(input);
			pass = (success && confirm()); if(pass) doImportEntity(input);
		}
		// 获得数据块数据
		if (!success) {
			success = checker.isCheckEntityContent(input);
			pass = (success && confirm()); if(pass) doCheckEntityContent(input);
			//			if (success ) doCheckEntityContent(input);
		}
		// 判断文件编码
		if (!success) {
			success = checker.isCheckEntityCharset(input);
			pass = (success && confirm()); if(pass) doCheckEntityCharset(input);
		}
		// 获得数据块数据
		if (!success) {
			success = checker.isCopyEntity(input);
			pass = (success && confirm()); if(pass) doCopyEntity(input);
		}

		// 计算散列码命令，HASH命令在本地执行，不需要确认
		if (!success) {
			pass = success = checker.isBuildHash(input);
			if (success) {
				doBuildHash(input);
			}
		}
		// 半截符编码
		if (!success) {
			pass = success = checker.isBuildHalf(input);
			if (success) {
				doBuildHalf(input);
			}
		}
		// EACH签名
		if (!success) {
			pass = success = checker.isBuildEach(input);
			if (success) {
				doBuildEach(input);
			}
		}
		// 命令处理模式。处理模式在本地执行，不需要确认
		if (!success) {
			success = checker.isCommandMode(input);
			pass = (success && confirm());
			if (pass) {
				doSetCommandMode(input);
			}
		}
		// 命令超时
		if (!success) {
			success = checker.isCommandTimeout(input);
			pass = (success && confirm());
			if (pass) {
				doSetCommandTimeout(input);
			}
		}

		// 服务器端密文超时
		if (!success) {
			success = checker.isCipherTimeout(input);
			pass = (success && confirm());
			if (pass) {
				doSetCipherTimeout(input);
			}
		}
		// 最大异步缓存尺寸
		if (!success) {
			success = checker.isMaxEchoBuffer(input);
			pass = (success && confirm());
			if (pass) {
				doSetMaxEchoBuffer(input);
			}
		}
		// 调用器最大参数
		if (!success) {
			success = checker.isMaxInvoker(input);
			pass = (success && confirm());
			if (pass) {
				doSetMaxInvoker(input);
			}
		}
		// FRONT网关
		if (!success) {
			success = checker.isPrintFrontGateway(input);
			pass = (success && confirm());
			if (pass) {
				doSetPrintFrontGateway(input);
			}
		}
		// 检测服务器系统信息
		if (!success) {
			success = checker.isCheckSystemInfo(input);
			pass = (success && confirm());
			if (pass) {
				doCheckSystemInfo(input);
			}
		}
		// 检测内网穿透信道
		if (!success) {
			success = checker.isCheckPockChannel(input);
			pass = (success && confirm());
			if (pass) {
				doCheckPockChannel(input);
			}
		}
		// 检测支持MASSIVE MIMO
		if (!success) {
			success = checker.isCheckMassiveMimo(input);
			pass = (success && confirm());
			if (pass) {
				doCheckMassiveMimo(input, display);
			}
		}
		
		// 建立限制操作规则
		if (!success) {
			success = checker.isCreateLimit(input);
			pass = (success && confirm()); if(pass) {
				doCreateLimit(input);
			}
		}
		// 删除限制操作规则
		if (!success) {
			success = checker.isDropLimit(input);
			pass = (success && confirm()); if(pass) {
				doDropLimit(input);
			}
		}
		// 显示限制操作单元
		if (!success) {
			success = checker.isShowLimit(input);
			pass = (success && confirm()); if(pass) {
				doShowLimit(input);
			}
		}
		// 提交锁定操作
		if (!success) {
			success = checker.isCreateFault(input);
			pass = (success && confirm()); if(pass) {
				doCreateFault(input);
			}
		}
		// 撤销锁定操作
		if (!success) {
			success = checker.isDropFault(input);
			pass = (success && confirm()); if(pass) {
				doDropFault(input);
			}
		}
		// 显示锁定单元
		if (!success) {
			success = checker.isShowFault(input);
			pass = (success && confirm()); if(pass) {
				doShowFault(input);
			}
		}
		// 显示禁止操作单元
		if (!success) {
			success = checker.isShowForbid(input);
			pass = (success && confirm()); if(pass) {
				doShowForbid(input);
			}
		}
		// 显示事务规则
		if (!success) {
			success = checker.isShowLockRule(input);
			pass = (success && confirm()); if(pass) {
				doShowLockRule(input);
			}
		}

		// 设置最大并行任务数
		if (!success) {
			success = checker.isSetMaxJobs(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxJobs(input);
			}
		}
		// 设置最大连接数
		if (!success) {
			success = checker.isSetMaxMembers(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxMembers(input);
			}
		}
		// 设置最大最多应用软件
		if (!success) {
			success = checker.isSetMaxTasks(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxTasks(input);
			}
		}
		// 设置最大优化表数目
		if (!success) {
			success = checker.isSetMaxRegulates(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxRegulates(input);
			}
		}
		// 设置最大磁盘空间
		if (!success) {
			success = checker.isSetMaxSize(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxSize(input);
			}
		}
		// 设置最大HOME子域集群数目
		if (!success) {
			success = checker.isSetMaxGroups(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxGroups(input);
			}
		}
		// 设置最大CALL网关节点数目
		if (!success) {
			success = checker.isSetMaxGateways(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxGateways(input);
			}
		}
		// BUILD节点数目
		if (!success) {
			success = checker.isSetMaxBuilders(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxBuilders(input);
			}
		}
		// WORK节点数目
		if (!success) {
			success = checker.isSetMaxWorkers(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxWorkers(input);
			}
		}
		// 用户账号到期
		if (!success) {
			success = checker.isSetExpireTime(input);
			pass = (success && confirm()); if(pass) {
				doSetExpireTime(input);
			}
		}
		if (!success) {
			success = checker.isSetMiddleBuffer(input);
			pass = (success && confirm()); if(pass) {
				doSetMiddleBuffer(input);
			}
		}
		if (!success) {
			success = checker.isSetCloudSize(input);
			pass = (success && confirm()); if(pass) {
				doSetCloudSize(input);
			}
		}
		if (!success) {
			success = checker.isSetUserPriority(input);
			pass = (success && confirm()); if(pass) {
				doSetUserPriority(input);
			}
		}
		
		// 设置最大表数目
		if (!success) {
			success = checker.isSetMaxTables(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxTables(input);
			}
		}
		// 设置一个表最大索引数目
		if (!success) {
			success = checker.isSetMaxIndexes(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxIndexes(input);
			}
		}
		// 设置DSM表最大压缩倍数
		if (!success) {
			success = checker.isSetMaxDSMReduce(input);
			pass = (success && confirm()); if(pass) {
				doSetMaxDSMReduce(input);
			}
		}

		// 开放/关闭数据库资源
		if (!success) {
			success = checker.isOpenShareSchema(input);
			pass = (success && confirm()); if(pass) {
				doOpenShareSchema(input);
			}
		}
		if (!success) {
			success = checker.isCloseShareSchema(input);
			pass = (success && confirm()); if(pass) {
				doCloseShareSchema(input);
			}
		}
		// 开放/关闭数据表资源
		if (!success) {
			success = checker.isOpenShareTable(input);
			pass = (success && confirm()); if(pass) {
				doOpenShareTable(input);
			}
		}
		if (!success) {
			success = checker.isCloseShareTable(input);
			pass = (success && confirm()); if(pass) {
				doCloseShareTable(input);
			}
		}
		// 授权人显示自己开放的数据资源
		if (!success) {
			success = checker.isShowOpenResource(input);
			pass = (success && confirm()); if(pass) {
				doShowOpenResource(input);
			}
		}
		// 被授权人显示授权人开放的数据资源
		if (!success) {
			success = checker.isShowPassiveResource(input);
			pass = (success && confirm()); if(pass) {
				doShowPassiveResource(input);
			}
		}

		// 检索在线命令
		if (!success) {
			success = checker.isSeekOnlineCommand(input);
			pass = (success && confirm()); if(pass) {
				doSeekOnlineCommand(input);
			}
		}

		// 并行流量测试
		if (!success) {
			success = checker.isParallelMultiSwarm(input);
			pass = (success && confirm()); 
			if(pass) {
				doParallelMultiSwarm(input);
			}
		}
		// 数据传输流量持续测试
		if (!success) {
			success = checker.isMultiSwarm(input);
			pass = (success && confirm()); 
			if(pass) {
				doMultiSwarm(input);
			}
		}
		// 数据传输流量测试
		if (!success) {
			success = checker.isSwarm(input);
			pass = (success && confirm());
			if (pass) {
				doSwarm(input);
			}
		}
		// 释放节点内存
		if(!success) {
			success = checker.isReleaseMemory(input);
			pass = (success && confirm());
			if (pass) {
				doReleaseMemory(input);
			}
		}
		// 释放节点内存
		if(!success) {
			success = checker.isReleaseMemoryInterval(input);
			pass = (success && confirm());
			if (pass) {
				doReleaseMemoryInterval(input);
			}
		}
		//		// 重置包尺寸
		//		if (!success) {
		//			success = checker.isReplyPacketSize(input);
		//			pass = (success && confirm());
		//			if (pass) {
		//				doReplyPacketSize(input);
		//			}
		//		}

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
		// 设置数据流队列
		if (!success) {
			success = checker.isReplyFlowControl(input);
			if (success && confirm()) {
				doReplyFlowControl(input);
			}
		}

		// 更新私有网络空间配置
		if (!success) {
			success = checker.isRefreshCyber(input);
			pass = (success && confirm());
			if (pass) {
				doRefreshCyber(input, 0);
			}
		}
		// 最大CPU占比
		if (!success) {
			success = checker.isMostCPU(input);
			pass = (success && confirm());
			if (pass) {
				doMostCPU(input);
			}
		}
		// 最大虚拟机内存占比
		if (!success) {
			success = checker.isMostVMMemory(input);
			pass = (success && confirm());
			if (pass) {
				doMostVMMemory(input);
			}
		}
		// 最少内存限制
		if (!success) {
			success = checker.isLeastMemory(input);
			pass = (success && confirm());
			if (pass) {
				doLeastMemory(input);
			}
		}
		// 最少磁盘空间限制
		if (!success) {
			success = checker.isLeastDisk(input);
			pass = (success && confirm());
			if (pass) {
				doLeastDisk(input);
			}
		}
		// 节点设备目录
		if (!success) {
			success = checker.isCheckSitePath(input);
			pass = (success && confirm());
			if (pass) {
				doCheckSitePath(input);
			}
		}

		// 启动连际容器服务
		if (!success) {
			success = checker.isRunTubService(input);
			pass = (success && confirm());
			if (pass) {
				doRunTubService(input);
			}
		}
		// 停止连际容器服务
		if (!success) {
			success = checker.isStopTubService(input);
			pass = (success && confirm());
			if (pass) {
				doStopTubService(input);
			}
		}
		// 显示运行中的容器服务
		if (!success) {
			success = checker.isPrintTubService(input);
			pass = (success && confirm());
			if (pass) {
				doPrintTubService(input);
			}
		}
		// 显示容器组件
		if (!success) {
			success = checker.isShowTubContainer(input);
			pass = (success && confirm());
			if (pass) {
				doShowTubContainer(input);
			}
		}
		// 检测边缘监听地址
		if (!success) {
			pass = success = checker.isCheckTubListener(input);
			if (pass) {
				doCheckTubListener(input);
			}
		}
		// 分布处理超时
		if (!success) {
			pass = success = checker.isDistributedTimeout(input);
			if (success) {
				doSetDistributedTimeout(input);
			}
		}

		// 打印本地组件
		if (!success) {
			pass = success = checker.isCheckLocalTask(input);
			if (success) {
				doCheckLocalTask(input);
			}
		}
		// 显示连接节点
		if (!success) {
			pass = success = checker.isCheckRemoteSite(input);
			if (success) {
				doCheckRemoteSite(input);
			}
		}
		// 显示连接节点
		if (!success) {
			pass = success = checker.isCheckJobSite(input);
			pass = (success && confirm());
			if (pass) {
				doCheckJobSite(input);
			}
		}
		
		// 最大异步缓存尺寸
		if (!success) {
			pass = success = checker.isMaxLogElements(input);
			if (success) {
				doSetMaxLogElements(input);
			}
		}
		// 开放TIGGER类型
		if (!success) {
			success = checker.isOpenTigger(input);
			pass = (success && confirm());
			if (pass) {
				doOpenTigger(input);
			}
		}
		// 关闭TIGGER类型
		if (!success) {
			success = checker.isCloseTigger(input);
			pass = (success && confirm());
			if (pass) {
				doCloseTigger(input);
			}
		}
		// 检索云应用
		if (!success) {
			success = checker.isSeekCloudWare(input);
			pass = (success && confirm());
			if (pass) {
				doSeekCloudWare(input);
			}
		}

		// 生成包
		if (!success) {
			success = checker.isBuildConductPackage(input);
			pass = (success && confirm());
			if (pass) {
				doBuildConductPackage(input);
			}
		}
		if (!success) {
			success = checker.isBuildEstablishPackage(input);
			pass = (success && confirm());
			if (pass) {
				doBuildEstablishPackage(input);
			}
		}

		if (!success) {
			success = checker.isBuildContactPackage(input);
			pass = (success && confirm());
			if (pass) {
				doBuildContactPackage(input);
			}
		}

		// 发布包
		if (!success) {
			success = checker.isDeployConductPackage(input);
			pass = (success && confirm());
			if (pass) {
				doDeployConductPackage(input);
			}
		}
		if (!success) {
			success = checker.isDeployEstablishPackage(input);
			pass = (success && confirm());
			if (pass) {
				doDeployEstablishPackage(input);
			}
		}
		if (!success) {
			success = checker.isDeployContactPackage(input);
			pass = (success && confirm());
			if (pass) {
				doDeployContactPackage(input);
			}
		}
		// 删除包
		if (!success) {
			success = checker.isDropConductPackage(input);
			pass = (success && confirm());
			if (pass) {
				doDropConductPackage(input);
			}
		}
		if (!success) {
			success = checker.isDropEstablishPackage(input);
			pass = (success && confirm());
			if (pass) {
				doDropEstablishPackage(input);
			}
		}
		if (!success) {
			success = checker.isDropContactPackage(input);
			pass = (success && confirm());
			if (pass) {
				doDropContactPackage(input);
			}
		}
		// 运行分布应用，转发处理
		if (!success) {
			pass = success = checker.isRunTask(input);
			if (success) {
				doRunTask(input);
			}
		}
		// 建立目录
		if (!success) {
			success = checker.isCreateCloudDirectory(input);
			pass = (success && confirm());
			if (pass) {
				doCreateCloudDirectory(input, display);
			}
		}
		// 删除目录
		if (!success) {
			success = checker.isDropCloudDirectory(input);
			pass = (success && confirm());
			if (pass) {
				doDropCloudDirectory(input, display);
			}
		}
		// 删除目录
		if (!success) {
			success = checker.isDropCloudFile(input);
			pass = (success && confirm());
			if (pass) {
				doDropCloudFile(input, display);
			}
		}
		// 修改目录名
		if (!success) {
			success = checker.isRenameCloudDirectory(input);
			pass = (success && confirm());
			if (pass) {
				doRenameCloudDirectory(input, display);
			}
		}
		// 修改文件名
		if (!success) {
			success = checker.isRenameCloudFile(input);
			pass = (success && confirm());
			if (pass) {
				doRenameCloudFile(input, display);
			}
		}
		
		// 上传文件
		if (!success) {
			success = checker.isUploadCloudFile(input);
			pass = (success && confirm());
			if (pass) {
				doUploadCloudFile(input, display);
			}
		}
		// 下载文件
		if (!success) {
			success = checker.isDownloadCloudFile(input);
			pass = (success && confirm());
			if (pass) {
				doDownloadCloudFile(input, display);
			}
		}
		// 扫描磁盘文件
		if (!success) {
			success = checker.isScanCloudDirectory(input);
			pass = (success && confirm());
			if (pass) {
				doScanCloudDirectory(input, display);
			}
		}
		
		// 判断是自定义命令
		if (!success) {
			success = CustomCreator.isCommand(input);
			if (success) {
				EchoInvoker invoker = CustomCreator.createInvoker(input);
				
				// 如果是FRONT自定义调用器，设置它的显示接口
				if (Laxkit.isClassFrom(invoker, CustomDisplayInvoker.class)) {
					((CustomDisplayInvoker) invoker).setDisplay(MeetInvoker.getDefaultDisplay());
				}
				
				// 判断是终端或者控制台，显示不同的提示
				if (launcher.isTerminal()) {
					// 参数见invoker.xml文件
					String title = MeetInvoker.getXMLAttribute("CUSTOM-COMMAND/MESSAGE-BOX/title");
					if (confirm(title)) {
						launch(invoker);
						pass = true;
					}
				} else {
					if (confirm()) {
						launch(invoker);
						pass = true;
					}
				}
			}
		}

		// 以上不成功，命令未知
		if (!success) {
			String text = getMeetLauncher().fault(FaultTip.ILLEGAL_COMMAND);
			fault(text);
			return false;
		}

		// 通过且命令追踪接口有效时，记录这个命令
		if (pass) {
			Tigger.command(input);
		}

		return pass;
	}

	/**
	 * 异步调用器转发给管理池处理
	 * @param invoker
	 */
	private void launch(EchoInvoker invoker) {
		getInvokerPool().launch(invoker);
	}

	/**
	 * 建立数据库
	 */
	private void doCreateSchema(String input) {
		CreateSchemaParser parser = new CreateSchemaParser();
		CreateSchema cmd = parser.split(input, true);
		MeetCreateSchemaInvoker invoker = new MeetCreateSchemaInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 删除数据库。<br>
	 * 根据名称解释，被删除的数据库，是当前账号下的一个或者全部数据库，以及数据库下的分布在集群中的全部数据记录。
	 * @param input 输入语句
	 */
	private void doDropSchema(String input) {
		// 解析数据库名称
		DropSchemaParser parser = new DropSchemaParser();
		DropSchema cmd = parser.split(input, true);
		// 生成异步调用器，发送异步命令
		MeetDropSchemaInvoker invoker = new MeetDropSchemaInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 诊断数据库存在。<br>
	 * @param input 输入语句
	 */
	private void doAssertSchema(String input) {
		// 解析数据库名称
		AssertSchemaParser parser = new AssertSchemaParser();
		AssertSchema cmd = parser.split(input);
		// 生成异步调用器，发送异步命令
		MeetAssertSchemaInvoker invoker = new MeetAssertSchemaInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示数据库配置
	 * @param input 输入语句
	 */
	private void doShowSchema(String input) {
		ShowSchemaParser parser = new ShowSchemaParser();
		ShowSchema cmd = parser.split(input, true);
		MeetShowSchemaInvoker invoker = new MeetShowSchemaInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示数据库配置
	 * @param input 输入语句
	 */
	private void doPrintSchemaDiagram(String input) {
		PrintSchemaDiagramParser parser = new PrintSchemaDiagramParser();
		PrintSchemaDiagram cmd = parser.split(input, true);
		MeetPrintSchemaDiagramInvoker invoker = new MeetPrintSchemaDiagramInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示数据库状态
	 * @param input 输入语句
	 */
	private void doPrintTableDiagram(String input) {
		PrintTableDiagramParser parser = new PrintTableDiagramParser();
		PrintTableDiagram cmd = parser.split(input, true);
		MeetPrintTableDiagramInvoker invoker = new MeetPrintTableDiagramInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 建立一个用户账号
	 * @param input 输入语句
	 */
	private void doCreateUser(String input) {
		CreateUserParser parser = new CreateUserParser();
		CreateUser user = parser.split(input, true);
		MeetCreateUserInvoker invoker = new MeetCreateUserInvoker(user);
		launch(invoker);
	}

	/**
	 * 删除账号以及账号下所有配置权限、数据库等
	 * @param input 输入语句
	 */
	private void doDropUser(String input) {
		DropUserParser parser = new DropUserParser();
		DropUser cmd = parser.split(input, true);
		MeetDropUserInvoker invoker = new MeetDropUserInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 修改账号密码
	 * @param input 输入语句
	 */
	private void doAlterUser(String input) {
		AlterUserParser parser = new AlterUserParser();
		AlterUser cmd = parser.split(input, true);
		MeetAlterUserInvoker invoker = new MeetAlterUserInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 判断用户名称
	 * @param input 输入语句
	 */
	private void doAssertUser(String input) {
		AssertUserParser parser = new AssertUserParser();
		AssertUser cmd = parser.split(input);
		MeetAssertUserInvoker invoker = new MeetAssertUserInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示注册用户状态
	 * @param input 输入语句
	 */
	private void doPrintUserDiagram(String input) {
		PrintUserDiagramParser parser = new PrintUserDiagramParser();
		PrintUserDiagram cmd = parser.split(input);
		MeetPrintUserDiagramInvoker invoker = new MeetPrintUserDiagramInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 禁用用户账号
	 * @param input 输入语句
	 */
	private void doCloseUser(String input) {
		CloseUserParser parser = new CloseUserParser();
		CloseUser cmd = parser.split(input, true);
		MeetCloseUserInvoker invoker = new MeetCloseUserInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 开放用户账号
	 * @param input 输入语句
	 */
	private void doOpenUser(String input) {
		OpenUserParser parser = new OpenUserParser();
		OpenUser cmd = parser.split(input, true);
		MeetOpenUserInvoker invoker = new MeetOpenUserInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 给注册用户授权
	 * @param input 输入语句
	 */
	private void doGrant(String input) {
		GrantParser parser = new GrantParser();
		Grant grant = parser.split(input, true);
		MeetGrantInvoker invoker = new MeetGrantInvoker(grant);
		launch(invoker);
	}

	/**
	 * 解除给注册用户的授权
	 * @param input 输入语句
	 */
	private void doRevoke(String input) {
		RevokeParser parser = new RevokeParser();
		Revoke revoke = parser.split(input, true);
		MeetRevokeInvoker invoker = new MeetRevokeInvoker(revoke);
		launch(invoker);
	}

	/**
	 * 显示注册用户授权
	 * @param input 输入语句
	 */
	private void doPrintGrantDiagram(String input) {
		PrintGrantDiagramParser parser = new PrintGrantDiagramParser();
		PrintGrantDiagram cmd = parser.split(input);
		MeetPrintGrantDiagramInvoker invoker = new MeetPrintGrantDiagramInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置数据表的数据块尺寸
	 * @param input 输入语句
	 */
	private void doSetEntitySize(String input) {
		SetEntitySizeParser parser = new SetEntitySizeParser();
		SetEntitySize cmd = parser.split(input, true);
		MeetSetEntitySizeInvoker invoker = new MeetSetEntitySizeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 建立数据库表
	 * @param input 输入语句
	 */
	private void doCreateTable(String input) {
		CreateTableParser parser = new CreateTableParser();
		CreateTable cmd = parser.split(input, true);
		// 设置当前持有人签名
		FrontSite local = StaffOnFrontPool.getFrontLauncher().getSite();
		cmd.setIssuer(local.getUser().getUsername());
		// 启动任务
		MeetCreateTableInvoker invoker = new MeetCreateTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示表配置参数
	 * @param input 输入语句
	 */
	private void doShowTable(String input) {
		ShowTableParser parser = new ShowTableParser();
		ShowTable cmd = parser.split(input, true);
		MeetShowTableInvoker invoker = new MeetShowTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检测远程表配置
	 * @param input 输入语句
	 */
	private void doCheckRemoteTable(String input) {
		CheckRemoteTableParser parser = new CheckRemoteTableParser();
		CheckRemoteTable cmd = parser.split(input, true);
		MeetCheckRemoteTableInvoker invoker = new MeetCheckRemoteTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检测远程组件配置
	 * @param input 输入语句
	 */
	private void doCheckRemoteTask(String input) {
		CheckRemoteTaskParser parser = new CheckRemoteTaskParser();
		CheckRemoteTask cmd = parser.split(input, true);
		MeetCheckRemoteTaskInvoker invoker = new MeetCheckRemoteTaskInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 删除数据表
	 * @param input 输入语句
	 */
	private void doDropTable(String input) {
		DropTableParser parser = new DropTableParser();
		DropTable cmd = parser.split(input, true);
		MeetDropTableInvoker invoker = new MeetDropTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 扫描数据表资源
	 * @param input 输入语句
	 */
	private void doScanTable(String input) {
		ScanTableParser parser = new ScanTableParser();
		ScanTable cmd = parser.split(input, true);
		MeetScanTableInvoker invoker = new MeetScanTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 扫描数据库资源（一个数据库下面包括任意多个表）
	 * @param input 输入语句
	 */
	private void doScanSchema(String input) {
		ScanSchemaParser parser = new ScanSchemaParser();
		ScanSchema cmd = parser.split(input, true);
		MeetScanSchemaInvoker invoker = new MeetScanSchemaInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 运行快捷组件
	 * @param input 输入语句
	 */
	private void doContact(String input) {
		ContactParser parser = new ContactParser();
		Contact cmd = parser.split(input, true);
		MeetContactInvoker invoker = new MeetContactInvoker(cmd);
		launch(invoker);
	}

	//	/**
	//	 * 发布分布任务组件
	//	 * @param input 输入语句
	//	 */
	//	private void doPublishTaskComponent(String input) {
	//		PublishMultiTaskComponentParser parser = new PublishMultiTaskComponentParser();
	//		PublishMultiTaskComponent cmd = parser.split(input);
	//		MeetPublishMultiTaskComponentInvoker invoker = new MeetPublishMultiTaskComponentInvoker(cmd);
	//		launch(invoker);
	//	}
	//
	//	/**
	//	 * 发布分布任务组件应用附件
	//	 * @param input 输入语句
	//	 */
	//	private void doPublishTaskAssistComponent(String input) {
	//		PublishTaskAssistComponentParser parser = new PublishTaskAssistComponentParser();
	//		PublishMultiTaskAssistComponent cmd = parser.split(input);
	//		MeetPublishMultiTaskAssistComponentInvoker invoker = new MeetPublishMultiTaskAssistComponentInvoker(cmd);
	//		launch(invoker);
	//	}
	//
	//	/**
	//	 * 发布分布任务组件链接库
	//	 * @param input 输入语句
	//	 */
	//	private void doPublishTaskLibraryComponent(String input) {
	//		PublishTaskLibraryComponentParser parser = new PublishTaskLibraryComponentParser();
	//		PublishMultiTaskLibraryComponent cmd = parser.split(input);
	//		MeetPublishMultiTaskLibraryComponentInvoker invoker = new MeetPublishMultiTaskLibraryComponentInvoker(cmd);
	//		launch(invoker);
	//	}

	/**
	 * 解析/执行JOIN语句
	 * @param input 输入语句
	 */
	private void join(String input) {
		JoinParser parser = new JoinParser();
		Join join = parser.split(input);
		MeetJoinInvoker invoker = new MeetJoinInvoker(join);
		launch(invoker);
	}

	/**
	 * 执行分布计算操作
	 * @param input 输入语句
	 */
	private void doConduct(String input) {
		ConductParser parser = new ConductParser();
		Conduct conduct = parser.split(input, true);
		MeetConductInvoker invoker = new MeetConductInvoker(conduct);
		launch(invoker);
	}

	/**
	 * 执行分布数据构建
	 * @param input ESTABLISH命令语句
	 */
	private void doEstablish(String input) {
		EstablishParser parser = new EstablishParser();
		Establish estab = parser.split(input, true);
		MeetEstablishInvoker invoker = new MeetEstablishInvoker(estab);
		launch(invoker);
	}

	/**
	 * SQL "INJECT INTO ... SELCT"异步操作
	 * @param input 输入语句
	 */
	private void doInjectSelect(String input) {
		InjectSelectParser parser = new InjectSelectParser();
		InjectSelect cmd = parser.split(input, true);
		MeetInjectSelectInvoker invoker = new MeetInjectSelectInvoker(cmd);
		launch(invoker);
	}

	/**
	 * SELECT异步操作。
	 * @param input 输入语句
	 */
	private void doSelect(String input) {
		SelectParser parser = new SelectParser();
		Select select = parser.split(input, true);
		MeetSelectInvoker invoker = new MeetSelectInvoker(select);
		launch(invoker);
	}

	/**
	 * SQL.DELETE异步操作
	 * @param input 输入语句
	 */
	private void doDelete(String input) {
		DeleteParser parser = new DeleteParser();
		Delete delete = parser.split(input, true);
		MeetDeleteInvoker invoker = new MeetDeleteInvoker(delete);
		launch(invoker);
	}

	/**
	 * 执行SQL.UPDATE命令的异步操作
	 * @param input 输入语句
	 */
	private void doUpdate(String input) {
		UpdateParser parser = new UpdateParser();
		Update update = parser.split(input, true);
		MeetUpdateInvoker invoker = new MeetUpdateInvoker(update);
		launch(invoker);
	}

	/**
	 * SQL "INSERT INTO"异步操作
	 * @param input 输入语句
	 */
	private void doInsert(String input) {
		InsertParser parser = new InsertParser();
		Insert insert = parser.splitInsert(input, true);
		MeetInsertInvoker invoker = new MeetInsertInvoker(insert);
		launch(invoker);
	}

	/**
	 * SQL "INJECT INTO"异步操作
	 * @param input 输入语句
	 */
	private void doInject(String input) {
		InsertParser parser = new InsertParser();
		Insert insert = parser.splitInject(input, true);
		MeetInsertInvoker invoker = new MeetInsertInvoker(insert);
		launch(invoker);
	}

	/**
	 * 数据块强制转化命令（只允许管理员操作）
	 * @param input 输入语句
	 */
	private void doRush(String input) {
		RushParser parser = new RushParser();
		Rush cmd = parser.split(input);
		MeetRushInvoker invoker = new MeetRushInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 数据优化命令（普通注册用户操作）
	 * @param input 输入语句
	 */
	private void doRegulate(String input) {		
		RegulateParser parser = new RegulateParser();
		Regulate cmd = parser.split(input);
		MeetRegulateInvoker invoker = new MeetRegulateInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 数据调整命令（普通注册用户操作）
	 * @param input 字符串命令
	 */
	private void doModulate(String input) {
		ModulateParser parser = new ModulateParser();
		Modulate cmd = parser.split(input);
		MeetModulateInvoker invoker = new MeetModulateInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检查表数据一致
	 * @param input 字符串命令
	 */
	private void doCheckEntityConsistency(String input) {
		CheckEntityConsistencyParser parser = new CheckEntityConsistencyParser();
		CheckEntityConsistency cmd = parser.split(input, true);
		MeetCheckEntityConsistencyInvoker invoker = new MeetCheckEntityConsistencyInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 修复表数据一致
	 * @param input 字符串命令
	 */
	private void doRecoverEntityConsistency(String input) {
		RecoverEntityConsistencyParser parser = new RecoverEntityConsistencyParser();
		RecoverEntityConsistency cmd = parser.split(input, true);
		MeetRecoverEntityConsistencyInvoker invoker = new MeetRecoverEntityConsistencyInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检查表分布数据容量
	 * @param input 输入语句
	 */
	private void doScanSketch(String input) {
		ScanSketchParser parser = new ScanSketchParser();
		ScanSketch cmd = parser.split(input, true);
		MeetScanSketchInvoker invoker = new MeetScanSketchInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 建立数据优化时间（普通注册用户操作）
	 * @param input 输入语句
	 */
	private void doCreateRegulateTime(String input) {
		CreateRegulateTimeParser parser = new CreateRegulateTimeParser();
		CreateRegulateTime cmd = parser.split(input);
		MeetCreateRegulateTimeInvoker invoker = new MeetCreateRegulateTimeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 撤销数据优化时间（普通注册用户操作）
	 * @param input 输入语句
	 */
	private void doDropRegulateTime(String input) {
		DropRegulateTimeParser parser = new DropRegulateTimeParser();
		DropRegulateTime cmd = parser.split(input, true);
		MeetDropRegulateTimeInvoker invoker = new MeetDropRegulateTimeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 打印数据优化时间（普通注册用户操作）
	 * @param input 输入语句
	 */
	private void doPrintRegulateTime(String input) {
		PrintRegulateTimeParser parser = new PrintRegulateTimeParser();
		PrintRegulateTime cmd = parser.split(input, true);
		MeetPrintRegulateTimeInvoker invoker = new MeetPrintRegulateTimeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 分派到DATA站点加载索引
	 * @param input 输入语句
	 */
	private void doLoadIndex(String input) {
		// 检查用户操作权限
		Account account = getMeetLauncher().getStaffPool().getAccount();
		if (!account.canLoadIndex()) {
			String text = getMeetLauncher().fault(FaultTip.PERMISSION_MISSING);
			fault(text);
			return;
		}

		LoadIndexParser parser = new LoadIndexParser();
		LoadIndex cmd = parser.split(input, true);
		MeetLoadIndexInvoker invoker = new MeetLoadIndexInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 从DATA站点卸载索引
	 * @param input 输入语句
	 */
	private void doStopIndex(String input) {
		// 检查用户操作权限
		Account account = getMeetLauncher().getStaffPool().getAccount();
		if (!account.canLoadIndex()) {
			String text = getMeetLauncher().fault(FaultTip.PERMISSION_MISSING);
			fault(text);
			return;
		}

		StopIndexParser parser = new StopIndexParser();
		StopIndex cmd = parser.split(input, true);
		MeetStopIndexInvoker invoker = new MeetStopIndexInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 分派到DATA站点加载数据块
	 * @param input 输入语句
	 */
	private void doLoadEntity(String input) {
		// 检查用户操作权限
		Account account = getMeetLauncher().getStaffPool().getAccount();
		if (!account.canLoadEntity()) {
			String text = getMeetLauncher().fault(FaultTip.PERMISSION_MISSING);
			fault(text);
			return;
		}

		LoadEntityParser parser = new LoadEntityParser();
		LoadEntity cmd = parser.split(input, true);
		MeetLoadEntityInvoker invoker = new MeetLoadEntityInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 从DATA站点卸载数据块
	 * @param input 输入语句
	 */
	private void doStopEntity(String input) {
		// 检查用户操作权限
		Account account = getMeetLauncher().getStaffPool().getAccount();
		if (!account.canLoadEntity()) {
			String text = getMeetLauncher().fault(FaultTip.PERMISSION_MISSING);
			fault(text);
			return;
		}

		StopEntityParser parser = new StopEntityParser();
		StopEntity cmd = parser.split(input, true);
		MeetStopEntityInvoker invoker = new MeetStopEntityInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示某个表下的数据块尺寸
	 * @param input 语句语句
	 */
	private void doShowEntitySize(String input) {
		ShowEntitySizeParser parser = new ShowEntitySizeParser();
		ShowEntitySize cmd = parser.split(input, true);
		MeetShowEntitySizeInvoker invoker = new MeetShowEntitySizeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 扫描数据块。流程：FRONT -> CALL -> DATA。
	 * @param input 输入语句
	 */
	private void doScanEntity(String input) {
		ScanEntityParser parser = new ScanEntityParser();
		ScanEntity cmd = parser.split(input, true);
		MeetScanEntityInvoker invoker = new MeetScanEntityInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 获取数据块编号。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doGitStubs(String input) {
		GitStubsParser parser = new GitStubsParser();
		GitStubs cmd = parser.split(input, true);
		MeetGitStubsInvoker invoker = new MeetGitStubsInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 获取数据块编号。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doPrintStubsDiagram(String input) {
		PrintStubsDiagramParser parser = new PrintStubsDiagramParser();
		PrintStubsDiagram cmd = parser.split(input, true);
		MeetPrintStubsDiagramInvoker invoker = new MeetPrintStubsDiagramInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 获取数据块数据。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doExportEntity(String input) {
		ExportEntityParser parser = new ExportEntityParser();
		ExportEntity cmd = parser.split(input, true);
		MeetExportEntityInvoker invoker = new MeetExportEntityInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 将文件中的数据导入集群。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doImportEntity(String input) {
		ImportEntityParser parser = new ImportEntityParser();
		ImportEntity cmd = parser.split(input, true);
		MeetImportEntityInvoker invoker = new MeetImportEntityInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 获取数据块数据。流程：FRONT -> CALL -> DATA（MASTER SITE）。
	 * @param input 输入语句
	 */
	private void doCopyEntity(String input) {
		CopyEntityParser parser = new CopyEntityParser();
		CopyEntity cmd = parser.split(input, true);
		MeetCopyEntityInvoker invoker = new MeetCopyEntityInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检测文件内容正确性
	 * @param input 输入语句
	 */
	private void doCheckEntityContent(String input) {
		CheckEntityContentParser parser = new CheckEntityContentParser();
		CheckEntityContent cmd = parser.split(input, true);
		MeetCheckEntityContentInvoker invoker = new MeetCheckEntityContentInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 判断文件编码类型
	 * @param input 输入语句
	 */
	private void doCheckEntityCharset(String input) {
		CheckEntityCharsetParser parser = new CheckEntityCharsetParser();
		CheckEntityCharset cmd = parser.split(input, true);
		MeetCheckEntityCharsetInvoker invoker = new MeetCheckEntityCharsetInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 计算散列码（在本地进行）
	 * @param input 输入语句
	 */
	private void doBuildHash(String input) {
		BuildHashParser parser = new BuildHashParser();
		BuildHash cmd = parser.split(input);
		MeetBuildHashInvoker invoker = new MeetBuildHashInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 半截符编码/解码（在本地进行）
	 * @param input 输入语句
	 */
	private void doBuildHalf(String input) {
		BuildHalfParser parser = new BuildHalfParser();
		BuildHalf cmd = parser.split(input);
		MeetBuildHalfInvoker invoker = new MeetBuildHalfInvoker(cmd);
		launch(invoker);
	}

	/**
	 * EACH签名（在本地进行）
	 * @param input 输入语句
	 */
	private void doBuildEach(String input) {
		BuildEachParser parser = new BuildEachParser();
		BuildEach cmd = parser.split(input);
		MeetBuildEachInvoker invoker = new MeetBuildEachInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置命令处理模式（在本地进行）
	 * @param input 输入语句
	 */
	private void doSetCommandMode(String input) {
		CommandModeParser parser = new CommandModeParser();
		CommandMode cmd = parser.split(input);
		MeetCommandModeInvoker invoker = new MeetCommandModeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置命令超时
	 * @param input 输入语句
	 */
	private void doSetCommandTimeout(String input) {
		CommandTimeoutParser parser = new CommandTimeoutParser();
		CommandTimeout cmd = parser.split(input);
		MeetCommandTimeoutInvoker invoker = new MeetCommandTimeoutInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置密文超时
	 * @param input 输入语句
	 */
	private void doSetCipherTimeout(String input) {
		CipherTimeoutParser parser = new CipherTimeoutParser();
		CipherTimeout cmd = parser.split(input);
		MeetCipherTimeoutInvoker invoker = new MeetCipherTimeoutInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 分布处理超时
	 * @param input 输入语句
	 */
	private void doSetDistributedTimeout(String input) {
		DistributedTimeoutParser parser = new DistributedTimeoutParser();
		DistributedTimeout cmd = parser.split(input);
		MeetDistributedTimeoutInvoker invoker = new MeetDistributedTimeoutInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 打印本地组件
	 * @param input 输入语句
	 */
	private void doCheckLocalTask(String input) {
		CheckLocalTaskParser parser = new CheckLocalTaskParser();
		CheckLocalTask cmd = parser.split(input);
		MeetCheckLocalTaskInvoker invoker = new MeetCheckLocalTaskInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示连接节点
	 * @param input 输入语句
	 */
	private void doCheckRemoteSite(String input) {
		CheckRemoteSiteParser parser = new CheckRemoteSiteParser();
		CheckRemoteSite cmd = parser.split(input);
		MeetCheckRemoteSiteInvoker invoker = new MeetCheckRemoteSiteInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示作业节点
	 * @param input 输入语句
	 */
	private void doCheckJobSite(String input) {
		CheckJobSiteParser parser = new CheckJobSiteParser();
		CheckJobSite cmd = parser.split(input);
		MeetCheckJobSiteInvoker invoker = new MeetCheckJobSiteInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 设置调用器数目
	 * @param input 输入语句
	 */
	private void doSetMaxInvoker(String input) {
		MaxInvokerParser parser = new MaxInvokerParser();
		MaxInvoker cmd = parser.split(input);
		MeetMaxInvokerInvoker invoker = new MeetMaxInvokerInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置异步缓存
	 * @param input 输入语句
	 */
	private void doSetMaxEchoBuffer(String input) {
		MaxEchoBufferParser parser = new MaxEchoBufferParser();
		MaxEchoBuffer cmd = parser.split(input);
		MeetMaxEchoBufferInvoker invoker = new MeetMaxEchoBufferInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检测服务器系统信息
	 * @param input 输入语句
	 */
	private void doCheckSystemInfo(String input) {
		CheckSystemInfoParser parser = new CheckSystemInfoParser();
		CheckSystemInfo cmd = parser.split(input);
		MeetCheckSystemInfoInvoker invoker = new MeetCheckSystemInfoInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检测内网穿透信道
	 * @param input 输入语句
	 */
	private void doCheckPockChannel(String input) {
		CheckPockChannelParser parser = new CheckPockChannelParser();
		CheckPockChannel cmd = parser.split(input);
		MeetCheckPockChannelInvoker invoker = new MeetCheckPockChannelInvoker(cmd);
		launch(invoker);
	}
	/**
	 * 检测支持MASSIVE MIMO
	 * @param input 输入语句
	 */
	private void doCheckMassiveMimo(String input, MeetDisplay display) {
		CheckMassiveMimoParser parser = new CheckMassiveMimoParser();
		CheckMassiveMimo cmd = parser.split(input);
		MeetCheckMassiveMimoInvoker invoker = new MeetCheckMassiveMimoInvoker(cmd);
		invoker.setDisplay(display);launch(invoker);
	}
	
	/**
	 * 设置密文超时
	 * @param input 输入语句
	 */
	private void doSetMaxLogElements(String input) {
		MaxLogElementsParser parser = new MaxLogElementsParser();
		MaxLogElements cmd = parser.split(input);
		MeetMaxLogElementsInvoker invoker = new MeetMaxLogElementsInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 打印FRONT网关
	 * @param input 输入语句
	 */
	private void doSetPrintFrontGateway(String input) {
		PrintFrontGatewayParser parser = new PrintFrontGatewayParser();
		PrintFrontGateway cmd = parser.split(input);
		MeetPrintFrontGatewayInvoker invoker = new MeetPrintFrontGatewayInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 建立限制操作
	 * @param input 输入语句
	 */
	private void doCreateLimit(String input) {		
		CreateLimitParser parser = new CreateLimitParser();
		CreateLimit cmd = parser.split(input, true);
		MeetCreateLimitInvoker invoker = new MeetCreateLimitInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 删除限制操作
	 * @param input 输入语句
	 */
	private void doDropLimit(String input) {		
		DropLimitParser parser = new DropLimitParser();
		DropLimit cmd = parser.split(input, true);
		MeetDropLimitInvoker invoker = new MeetDropLimitInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示限制操作单元
	 * @param input 输入语句
	 */
	private void doShowLimit(String input) {
		ShowLimitParser parser = new ShowLimitParser();
		ShowLimit cmd = parser.split(input);
		MeetShowLimitInvoker invoker = new MeetShowLimitInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 提交锁定操作
	 * @param input 输入语句
	 */
	private void doCreateFault(String input) {
		CreateFaultParser parser = new CreateFaultParser();
		CreateFault cmd = parser.split(input, true);
		MeetCreateFaultInvoker invoker = new MeetCreateFaultInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 撤销锁定操作
	 * @param input 输入语句
	 */
	private void doDropFault(String input) {
		DropFaultParser parser = new DropFaultParser();
		DropFault cmd = parser.split(input, true);
		MeetDropFaultInvoker invoker = new MeetDropFaultInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示锁定操作
	 * @param input 输入语句
	 */
	private void doShowFault(String input) {
		ShowFaultParser parser = new ShowFaultParser();
		ShowFault cmd = parser.split(input);
		MeetShowFaultInvoker invoker = new MeetShowFaultInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示禁止操作单元
	 * @param input 输入语句
	 */
	private void doShowForbid(String input) {
		ShowForbidParser parser = new ShowForbidParser();
		ShowForbid cmd = parser.split(input);
		MeetShowForbidInvoker invoker = new MeetShowForbidInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示事务规则
	 * @param input 输入语句
	 */
	private void doShowLockRule(String input) {
		ShowLockRuleParser parser = new ShowLockRuleParser();
		ShowLockRule cmd = parser.split(input);
		MeetShowLockRuleInvoker invoker = new MeetShowLockRuleInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置最大并行任务数
	 * @param input 输入语句
	 */
	private void doSetMaxJobs(String input) {
		SetMaxJobsParser parser = new SetMaxJobsParser();
		SetMaxJobs cmd = parser.split(input);
		MeetSetMaxJobsInvoker invoker = new MeetSetMaxJobsInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置最大连接数
	 * @param input 输入语句
	 */
	private void doSetMaxMembers(String input) {
		SetMaxMembersParser parser = new SetMaxMembersParser();
		SetMaxMembers cmd = parser.split(input);
		MeetSetMaxMembersInvoker invoker = new MeetSetMaxMembersInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置最多应用软件
	 * @param input 输入语句
	 */
	private void doSetMaxTasks(String input) {
		SetMaxTasksParser parser = new SetMaxTasksParser();
		SetMaxTasks cmd = parser.split(input);
		MeetSetMaxTasksInvoker invoker = new MeetSetMaxTasksInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置优化表数目
	 * @param input 输入语句
	 */
	private void doSetMaxRegulates(String input) {
		SetMaxRegulatesParser parser = new SetMaxRegulatesParser();
		SetMaxRegulates cmd = parser.split(input);
		MeetSetMaxRegulatesInvoker invoker = new MeetSetMaxRegulatesInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置磁盘空间尺寸
	 * @param input 输入语句
	 */
	private void doSetMaxSize(String input) {
		SetMaxSizeParser parser = new SetMaxSizeParser();
		SetMaxSize cmd = parser.split(input);
		MeetSetMaxSizeInvoker invoker = new MeetSetMaxSizeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置HOME子域集群数目
	 * @param input 输入语句
	 */
	private void doSetMaxGroups(String input) {
		SetMaxGroupsParser parser = new SetMaxGroupsParser();
		SetMaxGroups cmd = parser.split(input);
		MeetSetMaxGroupsInvoker invoker = new MeetSetMaxGroupsInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置CALL网关节点数目
	 * @param input 输入语句
	 */
	private void doSetMaxGateways(String input) {
		SetMaxGatewaysParser parser = new SetMaxGatewaysParser();
		SetMaxGateways cmd = parser.split(input);
		MeetSetMaxGatewaysInvoker invoker = new MeetSetMaxGatewaysInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置WORK节点数目
	 * @param input 输入语句
	 */
	private void doSetMaxWorkers(String input) {
		SetMaxWorkersParser parser = new SetMaxWorkersParser();
		SetMaxWorkers cmd = parser.split(input);
		MeetSetMaxWorkersInvoker invoker = new MeetSetMaxWorkersInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置BUILD节点数目
	 * @param input 输入语句
	 */
	private void doSetMaxBuilders(String input) {
		SetMaxBuildersParser parser = new SetMaxBuildersParser();
		SetMaxBuilders cmd = parser.split(input);
		MeetSetMaxBuildersInvoker invoker = new MeetSetMaxBuildersInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置BUILD节点数目
	 * @param input 输入语句
	 */
	private void doSetExpireTime(String input) {
		SetExpireTimeParser parser = new SetExpireTimeParser();
		SetExpireTime cmd = parser.split(input);
		MeetSetExpireTimeInvoker invoker = new MeetSetExpireTimeInvoker(cmd);
		launch(invoker);
	}
	/**
	 * 设置中间缓存尺寸
	 * @param input 输入语句
	 */
	private void doSetMiddleBuffer(String input) {
		SetMiddleBufferParser parser = new SetMiddleBufferParser();
		SetMiddleBuffer cmd = parser.split(input);
		MeetSetMiddleBufferInvoker invoker = new MeetSetMiddleBufferInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置云存储空间尺寸
	 * @param input 输入语句
	 */
	private void doSetCloudSize(String input) {
		SetCloudSizeParser parser = new SetCloudSizeParser();
		SetCloudSize cmd = parser.split(input);
		MeetSetCloudSizeInvoker invoker = new MeetSetCloudSizeInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 设置用户权级
	 * @param input 输入语句
	 */
	private void doSetUserPriority(String input) {
		SetUserPriorityParser parser = new SetUserPriorityParser();
		SetUserPriority cmd = parser.split(input);
		MeetSetUserPriorityInvoker invoker = new MeetSetUserPriorityInvoker(cmd);
		launch(invoker);
	}
	
	/**
	 * 设置最大表数目
	 * @param input 输入语句
	 */
	private void doSetMaxTables(String input) {
		SetMaxTablesParser parser = new SetMaxTablesParser();
		SetMaxTables cmd = parser.split(input);
		MeetSetMaxTablesInvoker invoker = new MeetSetMaxTablesInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置表最大索引数目
	 * @param input 输入语句
	 */
	private void doSetMaxIndexes(String input) {
		SetMaxIndexesParser parser = new SetMaxIndexesParser();
		SetMaxIndexes cmd = parser.split(input);
		MeetSetMaxIndexesInvoker invoker = new MeetSetMaxIndexesInvoker(cmd);
		launch(invoker);
	}

	/**
	 * DSM表最大压缩倍数
	 * @param input 输入语句
	 */
	private void doSetMaxDSMReduce(String input) {
		SetMaxDSMReduceParser parser = new SetMaxDSMReduceParser();
		SetMaxDSMReduce cmd = parser.split(input);
		MeetSetMaxDSMReduceInvoker invoker = new MeetSetMaxDSMReduceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 开放数据库共享给其他用户
	 * @param input 输入语句
	 */
	private void doOpenShareSchema(String input) {
		OpenShareSchemaParser parser = new OpenShareSchemaParser();
		OpenShareSchema cmd = parser.split(input, true);
		MeetOpenShareSchemaInvoker invoker = new MeetOpenShareSchemaInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 收回共享数据库
	 * @param input 输入语句
	 */
	private void doCloseShareSchema(String input) {
		CloseShareSchemaParser parser = new CloseShareSchemaParser();
		CloseShareSchema cmd = parser.split(input, true);
		MeetCloseShareSchemaInvoker invoker = new MeetCloseShareSchemaInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 开放数据表共享给其他用户
	 * @param input 输入语句
	 */
	private void doOpenShareTable(String input) {
		OpenShareTableParser parser = new OpenShareTableParser();
		OpenShareTable cmd = parser.split(input, true);
		MeetOpenShareTableInvoker invoker = new MeetOpenShareTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 收回共享数据表
	 * @param input 输入语句
	 */
	private void doCloseShareTable(String input) {
		CloseShareTableParser parser = new CloseShareTableParser();
		CloseShareTable cmd = parser.split(input, true);
		MeetCloseShareTableInvoker invoker = new MeetCloseShareTableInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 授权人显示自己开放的数据资源
	 * @param input 输入语句
	 */
	private void doShowOpenResource(String input) {
		ShowOpenResourceParser parser = new ShowOpenResourceParser();
		ShowOpenResource cmd = parser.split(input);
		MeetShowOpenResourceInvoker invoker = new MeetShowOpenResourceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 被授权人显示授权人开放给自己的数据资源
	 * @param input 输入语句
	 */
	private void doShowPassiveResource(String input) {
		ShowPassiveResourceParser parser = new ShowPassiveResourceParser();
		ShowPassiveResource cmd = parser.split(input);
		MeetShowPassiveResourceInvoker invoker = new MeetShowPassiveResourceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检索在线命令
	 * @param input 输入语句
	 */
	private void doSeekOnlineCommand(String input) {
		SeekOnlineCommandParser parser = new SeekOnlineCommandParser();
		SeekOnlineCommand cmd = parser.split(input);
		MeetSeekOnlineCommandInvoker invoker = new MeetSeekOnlineCommandInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 并行数据传输流量测试
	 * @param input 输入语句
	 */
	private void doParallelMultiSwarm(String input) {
		ParallelMultiSwarmParser parser = new ParallelMultiSwarmParser();
		ParallelMultiSwarm cmd = parser.split(input); // 解析命令
		MeetParallelMultiSwarmInvoker invoker = new MeetParallelMultiSwarmInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 数据传输流量持续测试
	 * @param input 输入语句
	 */
	private void doMultiSwarm(String input) {
		MultiSwarmParser parser = new MultiSwarmParser();
		MultiSwarm cmd = parser.split(input); // 解析命令
		MeetMultiSwarmInvoker invoker = new MeetMultiSwarmInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private void doSwarm(String input) {
		SwarmParser parser = new SwarmParser();
		Swarm cmd = parser.split(input, true); // 解析命令
		MeetSwarmInvoker invoker = new MeetSwarmInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 释放节点内存
	 * @param input 输入语句
	 */
	private void doReleaseMemory(String input) {
		ReleaseMemoryParser parser = new ReleaseMemoryParser();
		ReleaseMemory cmd = parser.split(input);
		MeetReleaseMemoryInvoker invoker = new MeetReleaseMemoryInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 节点内存释放间隔时间
	 * @param input 输入语句
	 */
	private void doReleaseMemoryInterval(String input) {
		ReleaseMemoryIntervalParser parser = new ReleaseMemoryIntervalParser();
		ReleaseMemoryInterval cmd = parser.split(input);
		MeetReleaseMemoryIntervalInvoker invoker = new MeetReleaseMemoryIntervalInvoker(cmd);
		launch(invoker);
	}

	//	/**
	//	 * 重置包尺寸
	//	 * @param input 输入语句
	//	 */
	//	private void doReplyPacketSize(String input) {
	//		ReplyPacketSizeParser parser = new ReplyPacketSizeParser();
	//		ReplyPacketSize cmd = parser.split(input);
	//		MeetReplyPacketSizeInvoker invoker = new MeetReplyPacketSizeInvoker(cmd);
	//		launch(invoker);
	//	}

	/**
	 * 设置反馈包模式
	 * @param input 输入语句
	 */
	private void doReplyPacketMode(String input) {
		ReplyPacketModeParser parser = new ReplyPacketModeParser();
		ReplyPacketMode cmd = parser.split(input);
		MeetReplyPacketModeInvoker invoker = new MeetReplyPacketModeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置反馈包尺寸
	 * @param input 输入语句
	 */
	private void doReplyPacketSize(String input) {
		ReplyPacketSizeParser parser = new ReplyPacketSizeParser();
		ReplyPacketSize cmd = parser.split(input);
		MeetReplyPacketSizeInvoker invoker = new MeetReplyPacketSizeInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置发送异步数据超时
	 * @param input 输入语句
	 */
	private void doReplySendTimeout(String input) {
		ReplySendTimeoutParser parser = new ReplySendTimeoutParser();
		ReplySendTimeout cmd = parser.split(input);
		MeetReplySendTimeoutInvoker invoker = new MeetReplySendTimeoutInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置接收异步数据超时
	 * @param input 输入语句
	 */
	private void doReplyReceiveTimeout(String input) {
		ReplyReceiveTimeoutParser parser = new ReplyReceiveTimeoutParser();
		ReplyReceiveTimeout cmd = parser.split(input);
		MeetReplyReceiveTimeoutInvoker invoker = new MeetReplyReceiveTimeoutInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 设置异步应答流量控制参数
	 * @param input 输入语句
	 */
	private void doReplyFlowControl(String input) {
		ReplyFlowControlParser parser = new ReplyFlowControlParser();
		ReplyFlowControl cmd = parser.split(input);
		MeetReplyFlowControlInvoker invoker = new MeetReplyFlowControlInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 更新私有网络空间
	 * @param input 输入语句
	 * @param delayTime 延时处理时间
	 * @return 启动成功返回真，否则假
	 */
	private boolean doRefreshCyber(String input , long delayTime) {
		RefreshCyberParser parser = new RefreshCyberParser();
		RefreshCyber cmd = parser.split(input);

		// 判断当前任务队列为空
		boolean empty = (getInvokerPool().size() == 0);
		if (!empty) {
			String text = getMeetLauncher().fault(FaultTip.REFUSE_REFRESH_CYBER);
			warning(text);
			return false;
		}

		MeetRefreshCyberInvoker invoker = new MeetRefreshCyberInvoker(cmd);
		invoker.setDelayTime(delayTime); // 延时处理时间
		launch(invoker);
		return true;
	}

	/**
	 * 最大CPU限制
	 * @param input 输入语句
	 */
	private void doMostCPU(String input) {
		MostCPUParser parser = new MostCPUParser();
		MostCPU cmd = parser.split(input);
		MeetMostCPUInvoker invoker = new MeetMostCPUInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 最大虚拟机限制
	 * @param input 输入语句
	 */
	private void doMostVMMemory(String input) {
		MostVMMemoryParser parser = new MostVMMemoryParser();
		MostVMMemory cmd = parser.split(input);
		MeetMostVMMemoryInvoker invoker = new MeetMostVMMemoryInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 最低内存限制
	 * @param input 输入语句
	 */
	private void doLeastMemory(String input) {
		LeastMemoryParser parser = new LeastMemoryParser();
		LeastMemory cmd = parser.split(input);
		MeetLeastMemoryInvoker invoker = new MeetLeastMemoryInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 最低磁盘空间限制
	 * @param input 输入语句
	 */
	private void doLeastDisk(String input) {
		LeastDiskParser parser = new LeastDiskParser();
		LeastDisk cmd = parser.split(input);
		MeetLeastDiskInvoker invoker = new MeetLeastDiskInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 最低磁盘空间限制
	 * @param input 输入语句
	 */
	private void doCheckSitePath(String input) {
		CheckSitePathParser parser = new CheckSitePathParser();
		CheckSitePath cmd = parser.split(input);
		MeetCheckSitePathInvoker invoker = new MeetCheckSitePathInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 启动边缘容器服务
	 * @param input 输入语句
	 */
	private void doRunTubService(String input) {
		RunTubServiceParser parser = new RunTubServiceParser();
		RunTubService cmd = parser.split(input, true);
		MeetRunTubServiceInvoker invoker = new MeetRunTubServiceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 停止边缘容器服务
	 * @param input 输入语句
	 */
	private void doStopTubService(String input) {
		StopTubServiceParser parser = new StopTubServiceParser();
		StopTubService cmd = parser.split(input, true);
		MeetStopTubServiceInvoker invoker = new MeetStopTubServiceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示边缘容器服务
	 * @param input 输入语句
	 */
	private void doPrintTubService(String input) {
		PrintTubServiceParser parser = new PrintTubServiceParser();
		PrintTubService cmd = parser.split(input, true);
		MeetPrintTubServiceInvoker invoker = new MeetPrintTubServiceInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 显示边缘容器服务
	 * @param input 输入语句
	 */
	private void doShowTubContainer(String input) {
		ShowTubContainerParser parser = new ShowTubContainerParser();
		ShowTubContainer cmd = parser.split(input, true);
		MeetShowTubContainerInvoker invoker = new MeetShowTubContainerInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 启动边缘容器服务
	 * @param input 输入语句
	 */
	private void doCheckTubListener(String input) {
		CheckTubListenerParser parser = new CheckTubListenerParser();
		CheckTubListener cmd = parser.split(input);
		MeetCheckTubListenerInvoker invoker = new MeetCheckTubListenerInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 开放TIGGER操作
	 * @param input 输入语句
	 */
	private void doOpenTigger(String input) {
		OpenTiggerParser parser = new OpenTiggerParser();
		OpenTigger cmd = parser.split(input);
		MeetOpenTiggerInvoker invoker = new MeetOpenTiggerInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 关闭TIGGER操作
	 * @param input 输入语句
	 */
	private void doCloseTigger(String input) {
		CloseTiggerParser parser = new CloseTiggerParser();
		CloseTigger cmd = parser.split(input);
		MeetCloseTiggerInvoker invoker = new MeetCloseTiggerInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 检索云应用
	 * @param input 输入语句
	 */
	private void doSeekCloudWare(String input) {
		SeekCloudWareParser parser = new SeekCloudWareParser();
		SeekCloudWare cmd = parser.split(input);
		MeetSeekCloudWareInvoker invoker = new MeetSeekCloudWareInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 生成分布计算包
	 * @param input 输入语句
	 */
	private void doBuildConductPackage(String input) {
		BuildConductPackageParser parser = new BuildConductPackageParser();
		BuildConductPackage cmd = parser.split(input, true);
		MeetBuildConductPackageInvoker invoker = new MeetBuildConductPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 生成分布数据构建包
	 * @param input 输入语句
	 */
	private void doBuildEstablishPackage(String input) {
		BuildEstablishPackageParser parser = new BuildEstablishPackageParser();
		BuildEstablishPackage cmd = parser.split(input, false);
		MeetBuildEstablishPackageInvoker invoker = new MeetBuildEstablishPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 生成CONTACT应用包
	 * @param input 输入语句
	 */
	private void doBuildContactPackage(String input) {
		BuildContactPackageParser parser = new BuildContactPackageParser();
		BuildContactPackage cmd = parser.split(input, true);
		MeetBuildContactPackageInvoker invoker = new MeetBuildContactPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 部署分布计算包
	 * @param input 输入语句
	 */
	private void doDeployConductPackage(String input) {
		DeployConductPackageParser parser = new DeployConductPackageParser();
		DeployConductPackage cmd = parser.split(input);
		EchoInvoker invoker = null;

		if (cmd.isSystemWare()) {
			invoker = new MeetSystemDeployConductPackageInvoker(cmd);
		} else if (cmd.isLocal()) {
			invoker = new MeetLocalDeployConductPackageInvoker(cmd);
		} else {
			invoker = new MeetDeployConductPackageInvoker(cmd);
		}

		// 发送
		if (invoker != null) {
			launch(invoker);
		}
	}

	/**
	 * 部署分布数据构建包
	 * @param input 输入语句
	 */
	private void doDeployEstablishPackage(String input) {
		DeployEstablishPackageParser parser = new DeployEstablishPackageParser();
		DeployEstablishPackage cmd = parser.split(input);
		EchoInvoker invoker = null;

		if (cmd.isSystemWare()) {
			invoker = new MeetSystemDeployEstablishPackageInvoker(cmd);
		} else if (cmd.isLocal()) {
			invoker = new MeetLocalDeployEstablishPackageInvoker(cmd);
		} else {
			invoker = new MeetDeployEstablishPackageInvoker(cmd);
		}

		// 发送
		if(invoker != null){
			launch(invoker);
		}
	}

	/**
	 * 部署CONTACT应用包
	 * @param input 输入语句
	 */
	private void doDeployContactPackage(String input) {
		DeployContactPackageParser parser = new DeployContactPackageParser();
		DeployContactPackage cmd = parser.split(input);
		EchoInvoker invoker = null;

		if (cmd.isSystemWare()) {
			invoker = new MeetSystemDeployContactPackageInvoker(cmd);
		} else if (cmd.isLocal()) {
			invoker = new MeetLocalDeployContactPackageInvoker(cmd);
		} else {
			invoker = new MeetDeployContactPackageInvoker(cmd);
		}

		// 发送
		if(invoker != null){
			launch(invoker);
		}
	}

	/**
	 * 删除分布计算包
	 * @param input 输入语句
	 */
	private void doDropConductPackage(String input) {
		DropConductPackageParser parser = new DropConductPackageParser();
		DropConductPackage cmd = parser.split(input);
		MeetDropConductPackageInvoker invoker = new MeetDropConductPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 删除分布数据构建包
	 * @param input 输入语句
	 */
	private void doDropEstablishPackage(String input) {
		DropEstablishPackageParser parser = new DropEstablishPackageParser();
		DropEstablishPackage cmd = parser.split(input);
		MeetDropEstablishPackageInvoker invoker = new MeetDropEstablishPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 删除CONTACT应用包
	 * @param input 输入语句
	 */
	private void doDropContactPackage(String input) {
		DropContactPackageParser parser = new DropContactPackageParser();
		DropContactPackage cmd = parser.split(input);
		MeetDropContactPackageInvoker invoker = new MeetDropContactPackageInvoker(cmd);
		launch(invoker);
	}

	/**
	 * 运行分布式应用
	 * @param input 输入语句
	 */
	private void doRunTask(String input) {
		RunTaskParser parser = new RunTaskParser();
		RunTask cmd = parser.split(input, true);
		// 转发处理
		auditor.shift(cmd);
	}

	/**
	 * 建立云存储目录
	 * @param input 输入语句
	 */
	private void doCreateCloudDirectory(String input, MeetDisplay display) {
		CreateDirectoryParser parser = new CreateDirectoryParser();
		CreateCloudDirectory cmd = parser.split(input, true);
		MeetCreateCloudDirectoryInvoker invoker = new MeetCreateCloudDirectoryInvoker(cmd);
		invoker.setDisplay(display);
		launch(invoker);
	}

	/**
	 * 删除云存储目录
	 * @param input 输入语句
	 */
	private void doDropCloudDirectory(String input, MeetDisplay display) {
		DropCloudDirectoryParser parser = new DropCloudDirectoryParser();
		DropCloudDirectory cmd = parser.split(input, true);
		MeetDropCloudDirectoryInvoker invoker = new MeetDropCloudDirectoryInvoker(cmd);
		invoker.setDisplay(display);
		launch(invoker);
	}

	/**
	 * 删除云存储目录
	 * @param input 输入语句
	 */
	private void doDropCloudFile(String input, MeetDisplay display) {
		DropCloudFileParser parser = new DropCloudFileParser();
		DropCloudFile cmd = parser.split(input, true);
		MeetDropCloudFileInvoker invoker = new MeetDropCloudFileInvoker(cmd);
		invoker.setDisplay(display);
		launch(invoker);
	}

	/**
	 * 修改云存储目录
	 * @param input 输入语句
	 */
	private void doRenameCloudDirectory(String input, MeetDisplay display) {
		RenameCloudDirectoryParser parser = new RenameCloudDirectoryParser();
		RenameCloudDirectory cmd = parser.split(input, true);
		MeetRenameCloudDirectoryInvoker invoker = new MeetRenameCloudDirectoryInvoker(cmd);
		invoker.setDisplay(display);
		launch(invoker);
	}

	/**
	 * 修改云存储目录
	 * @param input 输入语句
	 */
	private void doRenameCloudFile(String input, MeetDisplay display) {
		RenameCloudFileParser parser = new RenameCloudFileParser();
		RenameCloudFile cmd = parser.split(input, true);
		MeetRenameCloudFileInvoker invoker = new MeetRenameCloudFileInvoker(cmd);
		invoker.setDisplay(display);
		launch(invoker);
	}
	
	/**
	 * 上传文件到云端
	 * @param input 输入语句
	 */
	private void doUploadCloudFile(String input, MeetDisplay display) {
		UploadCloudFileParser parser = new UploadCloudFileParser();
		UploadCloudFile cmd = parser.split(input, true);
		MeetUploadCloudFileInvoker invoker = new MeetUploadCloudFileInvoker(cmd);
		invoker.setDisplay(display);
		launch(invoker);
	}

	/**
	 * 从云端下载文件
	 * @param input 输入语句
	 */
	private void doDownloadCloudFile(String input, MeetDisplay display) {
		DownloadCloudFileParser parser = new DownloadCloudFileParser();
		DownloadCloudFile cmd = parser.split(input, true);
		MeetDownloadCloudFileInvoker invoker = new MeetDownloadCloudFileInvoker(cmd);
		invoker.setDisplay(display);
		launch(invoker);
	}

	/**
	 * 扫描云端目录
	 * @param input 输入语句
	 */
	private void doScanCloudDirectory(String input, MeetDisplay display) {
		ScanCloudDirectoryParser parser = new ScanCloudDirectoryParser();
		ScanCloudDirectory cmd = parser.split(input, true);
		MeetScanCloudDirectoryInvoker invoker = new MeetScanCloudDirectoryInvoker(cmd);
		invoker.setDisplay(display);
		launch(invoker);
	}

	
	//	/**
	//	 * 发布分布任务组件。被外部调用
	//	 * @param input 输入语句
	//	 */
	//	public void runPublishTaskComponent(String input) {
	//		if (!checkOnline()) return;
	//
	//		// 检查
	//		boolean success = false;
	//		try {
	//			success = checker.isPublishTaskComponent(input);
	//			if (!success) printSyntax(false);
	//		} catch (SyntaxException e) {
	//			fault(e.getMessage());
	//		} catch (Throwable e) {
	//			fault(e.getMessage());
	//		}
	//		// 执行结果
	//		boolean pass = (success && confirm());
	//		if (pass) {
	//			doPublishTaskComponent(input);
	//		}
	//	}
	//
	//	/**
	//	 * 发布分布任务组件附件
	//	 * @param input 输入语句
	//	 */
	//	public void runPublishTaskAssistComponent(String input) {
	//		if (!checkOnline()) return;
	//
	//		// 检查
	//		boolean success = false;
	//		try {
	//			success = checker.isPublishTaskAssistComponent(input);
	//			if (!success) printSyntax(false);
	//		} catch (SyntaxException e) {
	//			fault(e.getMessage());
	//		} catch (Throwable e) {
	//			fault(e.getMessage());
	//		}
	//		// 执行结果
	//		boolean pass = (success && confirm());
	//		if (pass) {
	//			doPublishTaskAssistComponent(input);
	//		}
	//	}
	//
	//	/**
	//	 * 发布分布任务组件动态链接库
	//	 * @param input 输入语句
	//	 */
	//	public void runPublishTaskLibraryComponent(String input) {
	//		if (!checkOnline()) return;
	//
	//		// 检查
	//		boolean success = false;
	//		try {
	//			success = checker.isPublishTaskLibraryComponent(input);
	//			if (!success) printSyntax(false);
	//		} catch (SyntaxException e) {
	//			fault(e.getMessage());
	//		} catch (Throwable e) {
	//			fault(e.getMessage());
	//		}
	//		// 执行结果
	//		boolean pass = (success && confirm());
	//		if (pass) {
	//			doPublishTaskLibraryComponent(input);
	//		}
	//	}


	/**
	 * 生成分布计算应用。<br>
	 * 将分布计算的引导文件、附件包、动态链接库、许可证文件做成一个软件包的格式。<br><br>
	 * 这个软件包可以提供/出售给第三方使用。<br>
	 * 
	 * @param input 输入语句
	 */
	public void runBuildConductPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isBuildConductPackage(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doBuildConductPackage(input);
		}
	}

	/**
	 * 发布分布计算应用软件包
	 * @param input 输入语句
	 */
	public void runDeployConductPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isDeployConductPackage(input);
			if (!success) printSyntax(false);
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
	 * 删除分布计算应用软件包
	 * @param input 输入语句
	 */
	public void runDropConductPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isDropConductPackage(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doDropConductPackage(input);
		}
	}

	/**
	 * 生成分布数据构建应用。<br>
	 * 将分布数据构建的引导文件、附件包、动态链接库、许可证文件做成一个软件包的格式。<br><br>
	 * 这个软件包可以提供/出售给第三方使用。<br>
	 * 
	 * @param input 输入语句
	 */
	public void runBuildEstablishPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isBuildEstablishPackage(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doBuildEstablishPackage(input);
		}
	}

	/**
	 * 发布分布数据构建应用软件包
	 * @param input 输入语句
	 */
	public void runDeployEstablishPackage(String input) {
		if (!checkOnline()) {
			return;
		}

		// 检查
		boolean success = false;
		try {
			success = checker.isDeployEstablishPackage(input);
			if (!success) printSyntax(false);
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
	 * 删除数据构建应用软件包
	 * @param input 输入语句
	 */
	public void runDropEstablishPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isDropEstablishPackage(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doDropEstablishPackage(input);
		}
	}

	/**
	 * 生成快捷组件应用。<br>
	 * 将快捷组件的引导文件、附件包、动态链接库、许可证文件做成一个软件包的格式。<br><br>
	 * 这个软件包可以提供/出售给第三方使用。<br>
	 * 
	 * @param input 输入语句
	 */
	public void runBuildContactPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isBuildContactPackage(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doBuildContactPackage(input);
		}
	}

	/**
	 * 发布快捷组件应用软件包
	 * @param input 输入语句
	 */
	public void runDeployContactPackage(String input) {
		if (!checkOnline()) return;

		// 检查
		boolean success = false;
		try {
			success = checker.isDeployContactPackage(input);
			if (!success) printSyntax(false);
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
	 * 删除迭代应用软件包
	 * @param input 输入语句
	 */
	public void runDropContactPackage(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isDropContactPackage(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doDropContactPackage(input);
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
			if (!success) printSyntax(false);
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
			if (!success) printSyntax(false);
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

	/**
	 * 刷新用户网络空间。清除旧的记录，去网络上获取新的记录。
	 * @return 启动成功返回真，否则假
	 */
	public boolean runRefreshCyber() {
		if (!checkOnline()) {
			return false;
		}

		String syntax = "REFRESH CYBER";
		return doRefreshCyber(syntax, 2000);
	}

	/**
	 * 上传数据文件，被外部调用
	 * @param input 输入语句
	 */
	public void runImportEntity(String input) {
		if (!checkOnline()) return;

		// 检查
		boolean success = false;
		try {
			success = checker.isImportEntity(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doImportEntity(input);
		}
	}

	/**
	 * 下载数据块文件，被外部调用
	 * @param input 输入语句
	 */
	public void runExportEntity(String input) {
		if (!checkOnline()) return;

		// 检查
		boolean success = false;
		try {
			success = checker.isExportEntity(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doExportEntity(input);
		}
	}

	/**
	 * 检测文件内容，被外部调用
	 * @param input 输入语句
	 */
	public void runCheckEntityContent(String input) {
		if (!checkOnline()) return;

		// 检查
		boolean success = false;
		try {
			success = checker.isCheckEntityContent(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doCheckEntityContent(input);
		}
	}

	/**
	 * 检测文件字符集，被外部调用
	 * @param input 输入语句
	 */
	public void runCheckEntityCharset(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isCheckEntityCharset(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doCheckEntityCharset(input);
		}
	}

	/**
	 * 运行分布计算
	 * @param cmd 分布计算命令
	 * @return 成功运行返回真，否则假
	 */
	public boolean runConduct(Conduct cmd) {
		if (!checkOnline()) {
			return false;
		}
		MeetConductInvoker invoker = new MeetConductInvoker(cmd);
		launch(invoker);
		return true;
	}

	/**
	 * 运行迭代计算
	 * @param cmd 迭代计算命令
	 * @return 成功运行返回真，否则假
	 */
	public boolean runContact(Contact cmd) {
		if (!checkOnline()) {
			return false;
		}
		MeetContactInvoker invoker = new MeetContactInvoker(cmd);
		launch(invoker);
		return true;
	}

	/**
	 * 运行构建分布数据
	 * @param cmd 构建分布数据命令
	 * @return 成功运行返回真，否则假
	 */
	public boolean runEstablish(Establish cmd) {
		if (!checkOnline()) {
			return false;
		}
		MeetEstablishInvoker invoker = new MeetEstablishInvoker(cmd);
		launch(invoker);
		return true;
	}

	/**
	 * 启动本地边缘应用服务
	 * @param input 输入语句
	 */
	public void runRunTubService(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isRunTubService(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doRunTubService(input);
		}
	}

	/**
	 * 停止边缘应用
	 * @param input 输入语句
	 */
	public void runStopTubService(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isStopTubService(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doStopTubService(input);
		}
	}

	/**
	 * 显示运行中的边缘应用
	 * @param input 输入语句
	 */
	public void runPrintTubService(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isPrintTubService(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doPrintTubService(input);
		}
	}

	/**
	 * 显示全部应用
	 * @param input 输入语句
	 */
	public void runShowTubContainer(String input) {
		// 检查
		boolean success = false;
		try {
			success = checker.isShowTubContainer(input);
			if (!success) printSyntax(false);
		} catch (SyntaxException e) {
			fault(e.getMessage());
		} catch (Throwable e) {
			fault(e.getMessage());
		}
		// 执行结果
		boolean pass = (success && confirm());
		if (pass) {
			doShowTubContainer(input);
		}
	}
}