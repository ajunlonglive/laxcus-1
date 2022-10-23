/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.command.access.permit.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.cross.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.forbid.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.limit.*;
import com.laxcus.command.login.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.relate.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.rule.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.Shutdown;
import com.laxcus.command.site.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.command.site.front.*;
import com.laxcus.command.site.gate.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.gate.*;
import com.laxcus.gate.invoker.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;

/**
 * GATE命令管理池
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class GateCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static GateCommandPool selfHandle = new GateCommandPool();

	/**
	 * 构造异步命令管理池
	 */
	private GateCommandPool() {
		super();
	}

	/**
	 * 返回GATE命令管理池的静态句柄
	 * @return GATE命令管理池
	 */
	public static GateCommandPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("GateCommandPool.getInstance");
		return GateCommandPool.selfHandle;
	}

	/** 内部默认使用命令，跨越节点 **/
	private Class<?>[] skips = new Class<?>[] { SwitchHub.class,
			SeekSiteRuntime.class, SeekOnlineCommand.class, SeekOnlineResource.class , Gust.class, Swarm.class, AssertGateUser.class};

	/**
	 * 判断是规定的集群内部命令
	 * @param cmd 命令实例
	 * @return 匹配返回真，否则假
	 */
	private boolean isSkipCommand(Command cmd) {
		// 判断是内部命令
		for (int i = 0; i < skips.length; i++) {
			if (cmd.getClass() == skips[i]) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#accept(com.laxcus.command.Command)
	 */
	@Override
	public boolean accept(Command cmd) {
		// 必须有回显地址
		Cabin cabin = (cmd != null ? cmd.getSource() : null);
		// 不成功，返回假
		if (cabin == null) {
			return false;
		}

		// 来源站点
		Node node = cabin.getNode();

		// 如果是FRONT站点，必须已经注册
		boolean success = FrontOnGatePool.getInstance().contains(node);
		// 判断是被授权账号
		if (!success) {
			success = ConferrerFrontOnGatePool.getInstance().contains(node);
		}
		// 所有来自FRONT站点的命令都是普通命令，不定义优先级
		if (success) {
			cmd.setPriority(CommandPriority.NONE);
			cmd.setTigger(true); // 必须支持
		}
		// 来自BANK站点
		if (!success) {
			success = GateLauncher.getInstance().isHub(node);
		}
		// 判断是内部命令
		if (!success) {
			success = isSkipCommand(cmd);
		}

		// 保存命令
		if (success) {
			success = add(cmd);
		}
		
		// 保存命令
		if (success) {
			Tigger.command(cmd);
		}

		Logger.note(this, "accept", success, "from %s '%s'", cabin, cmd);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		// FRONT站点/被授权FRONT站点注册
		if (cmd.getClass() == FrontLogin.class) {
			invoker = new GateFrontLoginInvoker((FrontLogin) cmd);
		} else if (cmd.getClass() == ConferrerLogin.class) {
			invoker = new GateConferrerLoginInvoker((ConferrerLogin) cmd);
		}
		// 重装加载本地安全配置 / 动态链接库 / 安全策略 / 命令追踪
		if (cmd.getClass() == CreateSecureToken.class) {
			invoker = new SubCreateSecureTokenInvoker((CreateSecureToken) cmd);
		} else if (cmd.getClass() == DropSecureToken.class) {
			invoker = new SubDropSecureTokenInvoker((DropSecureToken) cmd);
		} else if (cmd.getClass() == FlushSecureToken.class) {
			invoker = new SubFlushSecureTokenInvoker((FlushSecureToken) cmd);
		} else if (cmd.getClass() == ShowSecureToken.class) {
			invoker = new SubShowSecureTokenInvoker((ShowSecureToken) cmd);
		} else if (cmd.getClass() == SetSecureSize.class) {
			invoker = new SubSetSecureSizeInvoker((SetSecureSize) cmd);
		} 
		else if (cmd.getClass() == ReloadLibrary.class) {
			invoker = new SubReloadLibraryInvoker((ReloadLibrary) cmd);
		} else if (cmd.getClass() == ReloadSecurityPolicy.class) {
			invoker = new SubReloadSecurityPolicyInvoker((ReloadSecurityPolicy) cmd);
		} else if (cmd.getClass() == ReloadLicence.class) {
			invoker = new SubReloadLicenceInvoker((ReloadLicence) cmd);
		} else if (cmd.getClass() == MailLicence.class) {
			invoker = new SubMailLicenceInvoker((MailLicence) cmd);
		} else if (cmd.getClass() == ScanCommandStack.class) {
			invoker = new SubScanCommandStackInvoker((ScanCommandStack) cmd);
		} else if (cmd.getClass() == ReleaseMemory.class) {
			invoker = new SubReleaseMemoryInvoker((ReleaseMemory) cmd);
		} else if (cmd.getClass() == ReleaseMemoryInterval.class) {
			invoker = new SubReleaseMemoryIntervalInvoker((ReleaseMemoryInterval) cmd);
		} else if (cmd.getClass() == ReplyPacketMode.class) {
			invoker = new SubReplyPacketModeInvoker((ReplyPacketMode) cmd);
		} else if (cmd.getClass() == ReplyPacketSize.class) {
			invoker = new SubReplyPacketSizeInvoker((ReplyPacketSize) cmd);
		} else if (cmd.getClass() == ReplySendTimeout.class) {
			invoker = new SubReplySendTimeoutInvoker((ReplySendTimeout) cmd);
		} else if (cmd.getClass() == ReplyReceiveTimeout.class) {
			invoker = new SubReplyReceiveTimeoutInvoker((ReplyReceiveTimeout) cmd);
		} else if(cmd.getClass() == ReplyFlowControl.class) {
			invoker = new SubReplyFlowControlInvoker((ReplyFlowControl)cmd);
		}
		// 强制重新注册
		else if (cmd.getClass() == RefreshLogin.class) {
			invoker = new CommonRefreshLoginInvoker((RefreshLogin) cmd);
		}
		// BANK集群管理员发送的刷新注册用户（账号在线才要刷新，包括CALL站点）
		else if (cmd.getClass() == RefreshUser.class) {
			invoker = new GateRefreshUserInvoker((RefreshUser) cmd);
		}
		// 设置站点日志等级
		else if (cmd.getClass() == SetLogLevel.class) {
			invoker = new SubSetLogLevelInvoker((SetLogLevel) cmd);
		}
		// 切换管理站点命令，来自BANK站点
		else if (cmd.getClass() == SwitchHub.class) {
			invoker = new CommonSwitchHubInvoker((SwitchHub) cmd);
		}
		
		// 服务器密文超时
		else if (cmd.getClass() == CipherTimeout.class) {
			invoker = new SubCipherTimeoutInvoker((CipherTimeout) cmd);
		}
		// 分布处理超时
		else if (cmd.getClass() == DistributedTimeout.class) {
			invoker = new SubDistributedTimeoutInvoker((DistributedTimeout) cmd);
		}
		// 最大缓存空间 / 最大调用器
		else if (cmd.getClass() == MaxEchoBuffer.class) {
			invoker = new SubMaxEchoBufferInvoker((MaxEchoBuffer) cmd);
		} else if (cmd.getClass() == MaxInvoker.class) {
			invoker = new SubMaxInvokerInvoker((MaxInvoker) cmd);
		}
		// 检测系统信息
		else if (cmd.getClass() == CheckSystemInfo.class) {
			invoker = new SubCheckSystemInfoInvoker((CheckSystemInfo) cmd);
		}
		// 强制重新加载和发布自动JAR包
		else if (cmd.getClass() == ReloadCustom.class) {
			invoker = new CommonReloadCustomInvoker((ReloadCustom) cmd);
		}
		// 当前站点状态
		else if (cmd.getClass() == SeekSiteRuntime.class) {
			invoker = new GateSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
		}
		// 检索在线命令
		else if (cmd.getClass() == SeekOnlineCommand.class) {
			invoker = new CommonSeekOnlineCommandInvoker((SeekOnlineCommand) cmd);
		}
		// 注册到BANK节点
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// 远程关闭命令
		else if (cmd.getClass() == Shutdown.class) {
			invoker = new SubShutdownInvoker((Shutdown) cmd);
		}
		// 向BANK站点申请主机编号
		else if (cmd.getClass() == ShiftTakeSiteSerial.class) {
			invoker = new CommonShiftTakeSiteSerialInvoker((ShiftTakeSiteSerial) cmd);
		}
		// GATE申请全部HASH站点
		else if (cmd.getClass() == ShiftTakeBankSubSites.class) {
			invoker = new CommonShiftTakeBankSubSitesInvoker((ShiftTakeBankSubSites) cmd);
		} 
		// GATE向HASH站点查询一个签名的ACCOUNT站点地址
		else if (cmd.getClass() == ShiftTakeAccountSite.class) {
			invoker = new GateShiftTakeAccountSiteInvoker((ShiftTakeAccountSite) cmd);
		}
		// GATE向ACCOUNT站点申请ACCOUNT实例
		else if (cmd.getClass() == ShiftTakeAccount.class) {
			invoker = new GateShiftTakeAccountInvoker((ShiftTakeAccount) cmd);
		}
		// 查找数据表
		else if (cmd.getClass() == ShiftTakeTable.class) {
			invoker = new GateShiftTakeTableInvoker((ShiftTakeTable) cmd);
		}
		// GATE向BANK查询与某个签名关联的CALL站点
		else if (cmd.getClass() == ShiftTakeCallItem.class) {
			invoker = new GateShiftTakeCallItemInvoker((ShiftTakeCallItem) cmd);
		}
		else if(cmd.getClass() == ShiftTakeRefer.class) {
			invoker = new GateShiftTakeReferInvoker((ShiftTakeRefer)cmd);
		}

		// 更新账号，来自BANK站点，或者GATE站点自己（当操作者增加/修改自己的参数时，会发出更新）
		else if (cmd.getClass() == RefreshAccount.class) {
			invoker = new GateRefreshAccountInvoker((RefreshAccount) cmd);
		}
		// 来自BANK站点，强制删除GATE站点上的账号，没有则忽略。
		else if (cmd.getClass() == AwardDropRefer.class) {
			invoker = new GateAwardDropReferInvoker((AwardDropRefer) cmd);
		}
		// 来自BANK站点，修改GATE站点上的账号密码，没有则忽略
		else if (cmd.getClass() == AwardAlterUser.class) {
			invoker = new GateAwardAlterUserInvoker((AwardAlterUser) cmd);
		}
		// BANK要求启动多个优化操作（本地转成单个PressRegulate处理）
		else if (cmd.getClass() == BatchPressRegulate.class) {
			invoker = new GateBatchPressRegulateInvoker((BatchPressRegulate) cmd);
		}

		/** 以下是来自FRONT站点的命令 **/
		// 申请/撤销事务操作请求
		else if (cmd.getClass() == AttachRule.class) {
			invoker = new GateAttachRuleInvoker((AttachRule) cmd);
		} else if (cmd.getClass() == DetachRule.class) {
			invoker = new GateDetachRuleInvoker((DetachRule) cmd);
		}
		// FRONT登录命令：注册站点获取自己的操作权级
		else if (cmd.getClass() == TakeGrade.class) {
			invoker = new GateTakeGradeInvoker((TakeGrade) cmd);
		}
		// FRONT登录命令：获取用户账号
		else if (cmd.getClass() == TakeAccount.class) {
			invoker = new GateTakeAccountInvoker((TakeAccount) cmd);
		}
		// FRONT登录命令：获得所有人的CALL站点 | 获得授权人的CALL站点 | 获得授权人分享的数据表实例
		else if (cmd.getClass() == TakeOwnerCall.class) {
			invoker = new GateTakeOwnerCallInvoker((TakeOwnerCall) cmd);
		} else if (cmd.getClass() == TakeAuthorizerCall.class) {
			invoker = new GateTakeAuthorizerCallInvoker((TakeAuthorizerCall) cmd);
		} else if (cmd.getClass() == TakeAuthorizerTable.class) {
			invoker = new GateTakeAuthorizerTableInvoker((TakeAuthorizerTable) cmd);
		}
		// 建立用户账号是串行操作。删除、修改密码、查询是并行操作
		else if (cmd.getClass() == CreateUser.class) {
			invoker = new GateCreateUserInvoker((CreateUser) cmd);
		} else if (cmd.getClass() == DropUser.class) {
			invoker = new GateDropUserInvoker((DropUser) cmd);
		} else if (cmd.getClass() == AlterUser.class) {
			invoker = new GateAlterUserInvoker((AlterUser) cmd);
		} else if (cmd.getClass() == AssertUser.class) {
			invoker = new GateAssertUserInvoker((AssertUser) cmd);
		} else if (cmd.getClass() == OpenUser.class) {
			invoker = new GateOpenUserInvoker((OpenUser) cmd);
		} else if (cmd.getClass() == CloseUser.class) {
			invoker = new GateCloseUserInvoker((CloseUser) cmd);
		}
		// 授权和解除授权操作，并行处理
		else if (cmd.getClass() == Grant.class) {
			invoker = new GateGrantInvoker((Grant) cmd);
		} else if (cmd.getClass() == Revoke.class) {
			invoker = new GateRevokeInvoker((Revoke) cmd);
		}
		// 建立数据库串行操作。删除、查询是并行操作
		else if (cmd.getClass() == CreateSchema.class) {
			invoker = new GateCreateSchemaInvoker((CreateSchema) cmd);
		} else if (cmd.getClass() == DropSchema.class) {
			invoker = new GateDropSchemaInvoker((DropSchema) cmd);
		} else if (cmd.getClass() == AssertSchema.class) {
			invoker = new GateAssertSchemaInvoker((AssertSchema) cmd);
		} else if(cmd.getClass() == ShowSchema.class) {
			invoker = new GateShowSchemaInvoker((ShowSchema)cmd);
		}
		// 建立、删除、查询数据表，全部是并行操作。
		else if (cmd.getClass() == CreateTable.class) {
			invoker = new GateCreateTableInvoker((CreateTable) cmd);
		} else if (cmd.getClass() == DropTable.class) {
			invoker = new GateDropTableInvoker((DropTable) cmd);
		} else if (cmd.getClass() == AssertTable.class) {
			invoker = new GateAssertTableInvoker((AssertTable) cmd);
		} else if (cmd.getClass() == ShowTable.class) {
			invoker = new GateShowTableInvoker((ShowTable) cmd);
		}
//		// 快捷组件命令
//		else if (cmd.getClass() == DropSwift.class) {
//			invoker = new GateDropSwiftInvoker((DropSwift) cmd);
//		} else if (cmd.getClass() == AssertSwift.class) {
//			invoker = new GateAssertSwiftInvoker((AssertSwift) cmd);
//		}
		// “建立/删除/显示”拒绝操作规则。GATE转发给ACCOUNT站点。
		else if (cmd.getClass() == CreateLimit.class) {
			invoker = new GateCreateLimitInvoker((CreateLimit) cmd);
		} else if (cmd.getClass() == DropLimit.class) {
			invoker = new GateDropLimitInvoker((DropLimit) cmd);
		} else if (cmd.getClass() == ShowLimit.class) {
			invoker = new GateShowLimitInvoker((ShowLimit) cmd);
		}
		// 发生写操作故障后，提交拒绝操作规则。在确定写操作故障消除后，撤销拒绝操作规则
		else if (cmd.getClass() == CreateFault.class) {
			invoker = new GateCreateFaultInvoker((CreateFault) cmd);
		} else if (cmd.getClass() == DropFault.class) {
			invoker = new GateDropFaultInvoker((DropFault) cmd);
		} else if (cmd.getClass() == ShowFault.class) {
			invoker = new GateShowFaultInvoker((ShowFault) cmd); // 显示已经被锁定的拒绝操作单元
		}
		// 禁止操作。此命令被受理后，所有操作全部读写禁止，直到被撤销
		else if (cmd.getClass() == CreateForbid.class) {
			invoker = new GateCreateForbidInvoker((CreateForbid) cmd);
		} else if (cmd.getClass() == DropForbid.class) {
			invoker = new GateDropForbidInvoker((DropForbid) cmd);
		} else if (cmd.getClass() == ShowForbid.class) {
			invoker = new GateShowForbidInvoker((ShowForbid) cmd);
		}
		// 显示锁事务规则
		else if (cmd.getClass() == ShowLockRule.class) {
			invoker = new GateShowLockRuleInvoker((ShowLockRule) cmd);
		}
		// 建立/撤销数据优化时间
		else if (cmd.getClass() == CreateRegulateTime.class) {
			invoker = new GateCreateRegulateTimeInvoker((CreateRegulateTime) cmd);
		} else if (cmd.getClass() == DropRegulateTime.class) {
			invoker = new GateDropRegulateTimeInvoker((DropRegulateTime) cmd);
		}
		// 启动数据优化操作
		else if (cmd.getClass() == PressRegulate.class) {
			invoker = new GatePressRegulateInvoker((PressRegulate) cmd);
		}
		// 数据块尺寸命令
		else if (cmd.getClass() == SetEntitySize.class) {
			invoker = new GateSetEntitySizeInvoker((SetEntitySize) cmd);
		} else if (cmd.getClass() == ShowEntitySize.class) {
			invoker = new GateShowEntitySizeInvoker((ShowEntitySize) cmd);
		}
		// 单次设置多个账号的最大限制参数
		else if (cmd.getClass() == SetMaxJobs.class) {
			invoker = new GateSetMaxJobsInvoker((SetMaxJobs) cmd);
		} else if (cmd.getClass() == SetMaxMembers.class) {
			invoker = new GateSetMaxMembersInvoker((SetMaxMembers) cmd);
		} else if (cmd.getClass() == SetMaxTasks.class) {
			invoker = new GateSetMaxTasksInvoker((SetMaxTasks) cmd);
		} else if (cmd.getClass() == SetMaxSize.class) {
			invoker = new GateSetMaxSizeInvoker((SetMaxSize) cmd);
		} else if (cmd.getClass() == SetMaxGroups.class) {
			invoker = new GateSetMaxGroupsInvoker((SetMaxGroups) cmd);
		} else if (cmd.getClass() == SetMaxGateways.class) {
			invoker = new GateSetMaxGatewaysInvoker((SetMaxGateways) cmd);
		} else if (cmd.getClass() == SetMaxWorkers.class) {
			invoker = new GateSetMaxWorkersInvoker((SetMaxWorkers) cmd);
		} else if (cmd.getClass() == SetMaxBuilders.class) {
			invoker = new GateSetMaxBuildersInvoker((SetMaxBuilders) cmd);
		} else if (cmd.getClass() == SetExpireTime.class) {
			invoker = new GateSetExpireTimeInvoker((SetExpireTime) cmd);
		} else if (cmd.getClass() == SetMaxTables.class) {
			invoker = new GateSetMaxTablesInvoker((SetMaxTables) cmd);
		} else if (cmd.getClass() == SetMaxIndexes.class) {
			invoker = new GateSetMaxIndexesInvoker((SetMaxIndexes) cmd);
		} else if (cmd.getClass() == SetMaxRegulates.class) {
			invoker = new GateSetMaxRegulatesInvoker((SetMaxRegulates) cmd);
		} else if (cmd.getClass() == SetMaxDSMReduce.class) {
			invoker = new GateSetMaxDSMReduceInvoker((SetMaxDSMReduce) cmd);
		} else if(cmd.getClass() == SetMiddleBuffer.class) {
			invoker = new GateSetMiddleBufferInvoker((SetMiddleBuffer)cmd);
		} else if (cmd.getClass() == SetCloudSize.class) {
			invoker = new GateSetCloudSizeInvoker((SetCloudSize) cmd);
		} else if (cmd.getClass() == SetUserPriority.class) {
			invoker = new GateSetUserPriorityInvoker((SetUserPriority) cmd);
		}
		
		//		// 发布分布任务组件的引导包、附件、动态链接库
		//		else if (cmd.getClass() == MailTaskComponent.class) {
		//			invoker = new GateMailTaskComponentInvoker((MailTaskComponent) cmd);
		//		} else if(cmd.getClass() == MailTaskAssistComponent.class) {
		//			invoker = new GateMailTaskAssistComponentInvoker((MailTaskAssistComponent)cmd);
		//		} else if(cmd.getClass() == MailTaskLibraryComponent.class) {
		//			invoker = new GateMailTaskLibraryComponentInvoker((MailTaskLibraryComponent)cmd);
		//		}

		//		// 发布码位计算器的引导包、附件、动态链接库
		//		else if(cmd.getClass() == MailScalerComponent.class) {
		//			invoker = new GateMailScalerComponentInvoker((MailScalerComponent)cmd);
		//		} else if(cmd.getClass() == MailScalerAssistComponent.class) {
		//			invoker = new GateMailScalerAssistComponentInvoker((MailScalerAssistComponent)cmd);
		//		} else if(cmd.getClass() == MailScalerLibraryComponent.class) {
		//			invoker = new GateMailScalerLibraryComponentInvoker((MailScalerLibraryComponent)cmd);
		//		}
		//		// 发布快捷组件的引导包、附件、动态链接库
		//		else if (cmd.getClass() == MailSwiftComponent.class) {
		//			invoker = new GateMailSwiftComponentInvoker((MailSwiftComponent) cmd);
		//		} else if (cmd.getClass() == MailSwiftAssistComponent.class) {
		//			invoker = new GateMailSwiftAssistComponentInvoker((MailSwiftAssistComponent) cmd);
		//		} else if (cmd.getClass() == MailSwiftLibraryComponent.class) {
		//			invoker = new GateMailSwiftLibraryComponentInvoker((MailSwiftLibraryComponent) cmd);
		//		}
		
		// 开放/关闭/打印共享资源
		else if (cmd.getClass() == OpenShareSchema.class) {
			invoker = new GateOpenShareSchemaInvoker((OpenShareSchema) cmd);
		} else if (cmd.getClass() == CloseShareSchema.class) {
			invoker = new GateCloseShareSchemaInvoker((CloseShareSchema) cmd);
		} else if (cmd.getClass() == OpenShareTable.class) {
			invoker = new GateOpenShareTableInvoker((OpenShareTable) cmd);
		} else if (cmd.getClass() == CloseShareTable.class) {
			invoker = new GateCloseShareTableInvoker((CloseShareTable) cmd);
		}
		// 显示授权资源，授权人操作
		else if (cmd.getClass() == ShowOpenResource.class) {
			invoker = new GateShowOpenResourceInvoker((ShowOpenResource) cmd);
		}
		// 显示被授权资源，被授权人操作
		else if(cmd.getClass() == ShowPassiveResource.class) {
			invoker = new GateShowPassiveResourceInvoker((ShowPassiveResource)cmd);
		}

		// 显示在线用户，BANK站点转发
		else if (cmd.getClass() == SeekFrontSite.class) {
			invoker = new GateSeekFrontSiteInvoker((SeekFrontSite) cmd);
		} else if (cmd.getClass() == SeekFrontUser.class) {
			invoker = new GateSeekFrontUserInvoker((SeekFrontUser) cmd);
		} else if (cmd.getClass() == SeekUserArea.class) {
			invoker = new GateSeekUserAreaInvoker((SeekUserArea) cmd);
		}

		// BANK节点通知GATE站点，HASH注册或者注销
		else if (cmd.getClass() == PushHashSite.class) {
			invoker = new GatePushHashSiteInvoker((PushHashSite) cmd);
		} else if (cmd.getClass() == DropHashSite.class) {
			invoker = new GateDropHashSiteInvoker((DropHashSite) cmd);
		}

		// 网络流量测试
		else if (cmd.getClass() == Gust.class) {
			invoker = new CommonClientGustInvoker((Gust) cmd);
		} else if (cmd.getClass() == Swarm.class) {
			invoker = new CommonServerSwarmInvoker((Swarm) cmd);
		}
		// 内存/磁盘空间不足报警，通过BANK站点，转发给WATCH节点，在图形界面上通知系统管理员
		else if (cmd.getClass() == VMMemoryMissing.class) {
			invoker = new SubVMMemoryMissingInvoker((VMMemoryMissing) cmd);
		} else if (cmd.getClass() == MemoryMissing.class) {
			invoker = new SubMemoryMissingInvoker((MemoryMissing) cmd);
		} else if (cmd.getClass() == DiskMissing.class) {
			invoker = new SubDiskMissingInvoker((DiskMissing) cmd);
		} else if (cmd.getClass() == MemberMissing.class) {
			invoker = new SubMemberMissingInvoker((MemberMissing) cmd);
		} else if (cmd.getClass() == MemberFull.class) {
			invoker = new SubMemberFullInvoker((MemberFull) cmd);
		} else if (cmd.getClass() == FrontMissing.class) {
			invoker = new SubFrontMissingInvoker((FrontMissing) cmd);
		} else if (cmd.getClass() == FrontFull.class) {
			invoker = new SubFrontFullInvoker((FrontFull) cmd);
		} else if (cmd.getClass() == LicenceTimeout.class) {
			invoker = new SubLicenceTimeoutInvoker((LicenceTimeout) cmd);
		}
		// CPU/内存/磁盘空间限制
		else if (cmd.getClass() == MostCPU.class) {
			invoker = new SubMostCPUInvoker((MostCPU) cmd);
		} else if(cmd.getClass() == MostVMMemory.class) {
			invoker = new SubMostVMMemoryInvoker((MostVMMemory)cmd);
		} else if (cmd.getClass() == LeastMemory.class) {
			invoker = new SubLeastMemoryInvoker((LeastMemory) cmd);
		} else if (cmd.getClass() == LeastDisk.class) {
			invoker = new SubLeastDiskInvoker((LeastDisk) cmd);
		} else if (cmd.getClass() == CheckSitePath.class) {
			invoker = new SubCheckSitePathInvoker((CheckSitePath) cmd);
		} else if (cmd.getClass() == ReflectPort.class) {
			invoker = new SubReflectPortInvoker((ReflectPort) cmd);
		}
		// 判断账号在GATE站点存在，ENTRANCE发出。
		else if (cmd.getClass() == AssertGateUser.class) {
			invoker = new GateAssertGateUserInvoker((AssertGateUser) cmd);
		}
		// 检查GATE站点注册用户与所在站点编号一致性
		else if (cmd.getClass() == CheckShadowConsistency.class) {
			invoker = new GateCheckShadowConsistencyInvoker((CheckShadowConsistency) cmd);
		}
		// 设置虚拟空间参数
		else if (cmd.getClass() == SetMemberCyber.class) {
			invoker = new GateSetMemberCyberInvoker((SetMemberCyber) cmd);
		} else if (cmd.getClass() == SetFrontCyber.class) {
			invoker = new GateSetFrontCyberInvoker((SetFrontCyber) cmd);
		} else if (cmd.getClass() == CheckUserCyber.class) {
			invoker = new GateCheckUserCyberInvoker((CheckUserCyber) cmd);
		}
		// 查询在线用户，投递给WATCH节点
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new GateAskClusterMemberInvoker((AskClusterMember) cmd);
		}
		// 推送/删除在线用户给WATCH节点（通过BANK节点转发）
		else if (cmd.getClass() == ShiftPushOnlineMember.class) {
			invoker = new GateShiftPushOnlineMemberInvoker((ShiftPushOnlineMember) cmd);
		} else if (cmd.getClass() == ShiftDropOnlineMember.class) {
			invoker = new GateShiftDropOnlineMemberInvoker((ShiftDropOnlineMember) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new SubOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new SubCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 发布分布式应用
		else if (cmd.getClass() == DeployConductPackage.class) {
			invoker = new GateDeployConductPackageInvoker((DeployConductPackage) cmd);
		} else if (cmd.getClass() == DeployEstablishPackage.class) {
			invoker = new GateDeployEstablishPackageInvoker((DeployEstablishPackage) cmd);
		} else if (cmd.getClass() == DeployContactPackage.class) {
			invoker = new GateDeployContactPackageInvoker((DeployContactPackage) cmd);
		}
		// 删除分布式应用
		else if (cmd.getClass() == DropConductPackage.class) {
			invoker = new GateDropConductPackageInvoker((DropConductPackage) cmd);
		} else if (cmd.getClass() == DropEstablishPackage.class) {
			invoker = new GateDropEstablishPackageInvoker((DropEstablishPackage) cmd);
		} else if (cmd.getClass() == DropContactPackage.class) {
			invoker = new GateDropContactPackageInvoker((DropContactPackage) cmd);
		}
		// 检测MASSIVE MIMO
		else if (cmd.getClass() == CheckMassiveMimo.class) {
			invoker = new GateCheckMassiveMimoInvoker((CheckMassiveMimo) cmd);
		}

		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 不支持操作
		boolean success = (invoker != null);
		if (success) {
			success = GateInvokerPool.getInstance().launch(invoker);
		} else {
			unsupport(cmd);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#press(com.laxcus.command.Command)
	 */
	@Override
	public boolean press(Command cmd) {
		EchoInvoker invoker = null;

//		// 上传分布任务组件给ACCOUNT
//		if (cmd.getClass() == ShiftPublishSingleTaskComponent.class) {
//			invoker = new GateShiftPublishSingleTaskComponentInvoker((ShiftPublishSingleTaskComponent) cmd);
//		}
//		// 上传码位计算器给ACCOUNT
//		else if (cmd.getClass() == ShiftPublishSingleScalerComponent.class) {
//			invoker = new GateShiftPublishSingleScalerComponentInvoker((ShiftPublishSingleScalerComponent) cmd);
//		} 
//		// 上传快捷组件给ACCOUN站点
//		else if (cmd.getClass() == ShiftPublishSingleSwiftComponent.class) {
//			invoker = new GateShiftPublishSingleSwiftComponentInvoker((ShiftPublishSingleSwiftComponent) cmd);
//		}

		// GATE向HASH站点查询一个签名的ACCOUNT站点地址
		if (cmd.getClass() == ShiftTakeAccountSite.class) {
			invoker = new GateShiftTakeAccountSiteInvoker((ShiftTakeAccountSite) cmd);
		}
		// GATE向ACCOUNT站点申请ACCOUNT实例
		else if (cmd.getClass() == ShiftTakeAccount.class) {
			invoker = new GateShiftTakeAccountInvoker((ShiftTakeAccount) cmd);
		}
		// GATE向BANK查询与某个签名关联的CALL站点
		else if (cmd.getClass() == ShiftTakeCallItem.class) {
			invoker = new GateShiftTakeCallItemInvoker((ShiftTakeCallItem) cmd);
		}
		// 转发获得被授权用户账号
		else if (cmd.getClass() == ShiftTakeRefer.class) {
			invoker = new GateShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}
		// 向BANK站点获取管理员账号
		else if (cmd.getClass() == ShiftTakeAdministrator.class) {
			invoker = new GateShiftTakeAdministratorInvoker((ShiftTakeAdministrator) cmd);
		}
		// 索取数据表实例
		else if (cmd.getClass() == ShiftTakeTable.class) {
			invoker = new GateShiftTakeTableInvoker((ShiftTakeTable) cmd);
		}
		// 转发事务规则
		else if (cmd.getClass() == ShiftAttachRule.class) {
			invoker = new GateShiftAttachRuleInvoker((ShiftAttachRule) cmd);
		}
		
//		// 转发分布任务组件附件、码位计算器的应用附件和动态链接库
//		else if(cmd.getClass() == ShiftPublishSingleTaskAssistComponent.class) {
//			invoker = new GateShiftPublishSingleTaskAssistComponentInvoker((ShiftPublishSingleTaskAssistComponent)cmd);
//		} else if(cmd.getClass() == ShiftPublishSingleTaskLibraryComponent.class) {
//			invoker = new GateShiftPublishSingleTaskLibraryComponentInvoker((ShiftPublishSingleTaskLibraryComponent)cmd);
//		}

//		else if(cmd.getClass() == ShiftPublishSingleSwiftAssistComponent.class) {
//			invoker = new GateShiftPublishSingleSwiftAssistComponentInvoker((ShiftPublishSingleSwiftAssistComponent)cmd);
//		} else if(cmd.getClass() == ShiftPublishSingleSwiftLibraryComponent.class) {
//			invoker = new GateShiftPublishSingleSwiftLibraryComponentInvoker((ShiftPublishSingleSwiftLibraryComponent)cmd);
//		} 
//		else if(cmd.getClass() == ShiftPublishSingleScalerAssistComponent.class) {
//			invoker = new GateShiftPublishSingleScalerAssistComponentInvoker((ShiftPublishSingleScalerAssistComponent)cmd);
//		} else if(cmd.getClass() == ShiftPublishSingleScalerLibraryComponent.class) {
//			invoker = new GateShiftPublishSingleScalerLibraryComponentInvoker((ShiftPublishSingleScalerLibraryComponent)cmd);
//		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 判断调用器有效
		boolean success = (invoker != null);
		if (success) {
			success = GateInvokerPool.getInstance().launch(invoker);
		}
		return success;
	}

}