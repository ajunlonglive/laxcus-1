/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.pool;

import com.laxcus.account.invoker.*;
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
import com.laxcus.command.licence.*;
import com.laxcus.command.limit.*;
import com.laxcus.command.login.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.task.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;

/**
 * ACCOUNT命令管理池。<br>
 * 
 * ACCOUNT节点位于内网，默认接受所有命令，忽略检查工作。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class AccountCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static AccountCommandPool selfHandle = new AccountCommandPool();

	/**
	 * 构造异步命令管理池
	 */
	private AccountCommandPool() {
		super();
	}

	/**
	 * 返回ACCOUNT命令管理池的静态句柄
	 * @return ACCOUNT命令管理池
	 */
	public static AccountCommandPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("AccountCommandPool.getInstance");
		return AccountCommandPool.selfHandle;
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
		
		// 保存命令
		boolean success = add(cmd);
		
		// 保存命令
		if (success) {
			Tigger.command(cmd);
		}

		Logger.note(this, "accept", success, "from %s - %s", cabin, cmd);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;
		
		// 密钥令牌管理命令
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
		// 重新加载链接库、安全策略、许可证、追踪命令
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
			invoker = new AccountSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
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
		
		/// 本地处理的账号命令 ///
		
		// GATE站点获取账号，下载加载到本地内存。
		else if (cmd.getClass() == TakeAccount.class) {
			invoker = new AccountTakeAccountInvoker((TakeAccount) cmd);
		}
		// 获取数据表命令
		else if(cmd.getClass() == TakeTable.class) {
			invoker = new AccountTakeTableInvoker((TakeTable)cmd);
		}
		// 建立、删除、修改、判断账号
		else if (cmd.getClass() == CreateUser.class) {
			invoker = new AccountCreateUserInvoker((CreateUser) cmd);
		} else if (cmd.getClass() == DropUser.class) {
			invoker = new AccountDropUserInvoker((DropUser) cmd);
		} else if (cmd.getClass() == AlterUser.class) {
			invoker = new AccountAlterUserInvoker((AlterUser) cmd);
		} else if (cmd.getClass() == AssertUser.class) {
			invoker = new AccountAssertUserInvoker((AssertUser) cmd);
		} else if (cmd.getClass() == OpenUser.class) {
			invoker = new AccountOpenUserInvoker((OpenUser) cmd);
		} else if (cmd.getClass() == CloseUser.class) {
			invoker = new AccountCloseUserInvoker((CloseUser) cmd);
		}
		// 单账号授权/解除授权
		else if (cmd.getClass() == SingleGrant.class) {
			invoker = new AccountSingleGrantInvoker((SingleGrant) cmd);
		} else if (cmd.getClass() == SingleRevoke.class) {
			invoker = new AccountSingleRevokeInvoker((SingleRevoke) cmd);
		}
		// 建立、删除、判断数据库名
		else if (cmd.getClass() == CreateSchema.class) {
			invoker = new AccountCreateSchemaInvoker((CreateSchema) cmd);
		} else if (cmd.getClass() == DropSchema.class) {
			invoker = new AccountDropSchemaInvoker((DropSchema) cmd);
		} else if (cmd.getClass() == AssertSchema.class) {
			invoker = new AccountAssertSchemaInvoker((AssertSchema) cmd);
		}
		// 建立、删除、判断数据表名
		else if (cmd.getClass() == CreateTable.class) {
			invoker = new AccountCreateTableInvoker((CreateTable) cmd);
		} else if (cmd.getClass() == DropTable.class) {
			invoker = new AccountDropTableInvoker((DropTable) cmd);
		} else if (cmd.getClass() == AssertTable.class) {
			invoker = new AccountAssertTableInvoker((AssertTable) cmd);
		}
		// 根据坐标获得账号
		else if (cmd.getClass() == TakeAccountSiger.class) {
			invoker = new AccountTakeAccountSigerInvoker((TakeAccountSiger) cmd);
		}
		// 获得被授权账号的资源引用
		else if (cmd.getClass() == TakeRefer.class) {
			invoker = new AccountTakeReferInvoker((TakeRefer) cmd);
		}
		
		// 设置/提取账号参数
		else if (cmd.getClass() == SetEntitySize.class) {
			invoker = new AccountSetEntitySizeInvoker((SetEntitySize) cmd);
		} else if (cmd.getClass() == ShowEntitySize.class) {
			invoker = new AccountShowEntitySizeInvoker((ShowEntitySize) cmd);
		}
//		// 删除快捷组件
//		else if (cmd.getClass() == DropSwift.class) {
//			invoker = new AccountDropSwiftInvoker((DropSwift) cmd);
//		}
		
		// 建立/撤销数据优化时间
		else if (cmd.getClass() == CreateRegulateTime.class) {
			invoker = new AccountCreateRegulateTimeInvoker((CreateRegulateTime) cmd);
		} else if (cmd.getClass() == DropRegulateTime.class) {
			invoker = new AccountDropRegulateTimeInvoker((DropRegulateTime) cmd);
		}

//		// GATE/WATCH站点上传分布任务组件、码位计算器、快捷组件
//		else if (cmd.getClass() == UploadTask.class) {
//			invoker = new AccountUploadTaskInvoker((UploadTask) cmd);
//		} 
//		else if (cmd.getClass() == UploadScaler.class) {
//			invoker = new AccountUploadScalerInvoker((UploadScaler) cmd);
//		} 
//		else if (cmd.getClass() == UploadSwift.class) {
//			invoker = new AccountUploadSwiftInvoker((UploadSwift) cmd);
//		} 
		
//		else if (cmd.getClass() == RegisterSwift.class) {
//			invoker = new AccountRegisterSwiftInvoker((RegisterSwift) cmd); // 注册快捷组件
//		}
		
//		// 在本地保存分布任务组件
//		else if (cmd.getClass() == MailTaskComponent.class) {
//			invoker = new AccountMailTaskComponentInvoker((MailTaskComponent) cmd);
//		}
		
//		// 在本地保存码位计算器
//		else if(cmd.getClass() == MailScalerComponent.class) {
//			invoker = new AccountMailScalerComponentInvoker((MailScalerComponent)cmd);
//		}
//		// 在本地保存快捷组件
//		else if(cmd.getClass() == MailSwiftComponent.class) {
//			invoker = new AccountMailSwiftComponentInvoker((MailSwiftComponent)cmd);
//		}
		
		// 根据阶段部件，获得分布任务组件标识
		if (cmd.getClass() == TakeTaskTag.class) {
			invoker = new AccountTakeTaskTagInvoker((TakeTaskTag) cmd);
		}
//		// 根据用户签名，获得码位计算器标识
//		else if (cmd.getClass() == TakeScalerTag.class) {
//			invoker = new AccountTakeScalerTagInvoker((TakeScalerTag) cmd);
//		}
//		// 根据用户签名，获得快捷组件标识
//		else if (cmd.getClass() == TakeSwiftTag.class) {
//			invoker = new AccountTakeSwiftTagInvoker((TakeSwiftTag) cmd);
//		}
		
		// 根据组件标识，获得分布任务组件数据包(.dtc后缀)
		else if (cmd.getClass() == TakeTaskComponent.class) {
			invoker = new AccountTakeTaskComponentInvoker((TakeTaskComponent) cmd);
		}
//		// 根据码位计算器标识，获对应的数据包（".csc"后缀的文件）
//		else if (cmd.getClass() == TakeScalerComponent.class) {
//			invoker = new AccountTakeScalerComponentInvoker((TakeScalerComponent) cmd);
//		}
//		// 根据快捷组件标识，获取对应的数据包（以“.spc”为后缀的文件）
//		else if (cmd.getClass() == TakeSwiftComponent.class) {
//			invoker = new AccountTakeSwiftComponentInvoker((TakeSwiftComponent) cmd);
//		}
		
		// 建立/删除拒绝操作规则
		else if (cmd.getClass() == CreateLimit.class) {
			invoker = new AccountCreateLimitInvoker((CreateLimit) cmd);
		} else if (cmd.getClass() == DropLimit.class) {
			invoker = new AccountDropLimitInvoker((DropLimit) cmd);
		}
		
		// 授权人开放/关闭共享资源
		else if (cmd.getClass() == OpenShareSchema.class) {
			invoker = new AccountOpenShareSchemaInvoker((OpenShareSchema) cmd);
		} else if (cmd.getClass() == CloseShareSchema.class) {
			invoker = new AccountCloseShareSchemaInvoker((CloseShareSchema) cmd);
		} else if (cmd.getClass() == OpenShareTable.class) {
			invoker = new AccountOpenShareTableInvoker((OpenShareTable) cmd);
		} else if (cmd.getClass() == CloseShareTable.class) {
			invoker = new AccountCloseShareTableInvoker((CloseShareTable) cmd);
		} 
		// 被授权人接受：开放/关闭授权人的共享资源（流程：ACCOUNT -> BANK -> OTHER ACCOUNT）
		else if (cmd.getClass() == AwardOpenActiveItem.class) {
			invoker = new AccountAwardOpenActiveItemInvoker((AwardOpenActiveItem) cmd);
		} else if (cmd.getClass() == AwardCloseActiveItem.class) {
			invoker = new AccountAwardCloseActiveItemInvoker((AwardCloseActiveItem) cmd);
		}
		
		// 设置单个账号的最大限制参数
		else if (cmd.getClass() == SetSingleMaxJobs.class) {
			invoker = new AccountSetSingleMaxJobsInvoker((SetSingleMaxJobs) cmd);
		} else if (cmd.getClass() == SetSingleMaxMembers.class) {
			invoker = new AccountSetSingleMaxMembersInvoker((SetSingleMaxMembers) cmd);
		} else if (cmd.getClass() == SetSingleMaxTasks.class) {
			invoker = new AccountSetSingleMaxTasksInvoker((SetSingleMaxTasks) cmd);
		} else if (cmd.getClass() == SetSingleMaxSize.class) {
			invoker = new AccountSetSingleMaxSizeInvoker((SetSingleMaxSize) cmd);
		} else if (cmd.getClass() == SetSingleMaxGroups.class) {
			invoker = new AccountSetSingleMaxGroupsInvoker((SetSingleMaxGroups) cmd);
		} else if (cmd.getClass() == SetSingleMaxGateways.class) {
			invoker = new AccountSetSingleMaxGatewaysInvoker((SetSingleMaxGateways) cmd);
		} else if (cmd.getClass() == SetSingleMaxWorkers.class) {
			invoker = new AccountSetSingleMaxWorkersInvoker((SetSingleMaxWorkers) cmd);
		} else if (cmd.getClass() == SetSingleMaxBuilders.class) {
			invoker = new AccountSetSingleMaxBuildersInvoker((SetSingleMaxBuilders) cmd);
		} else if (cmd.getClass() == SetSingleExpireTime.class) {
			invoker = new AccountSetSingleExpireTimeInvoker((SetSingleExpireTime) cmd);
		}else if (cmd.getClass() == SetSingleMaxTables.class) {
			invoker = new AccountSetSingleMaxTablesInvoker((SetSingleMaxTables) cmd);
		} else if (cmd.getClass() == SetSingleMaxIndexes.class) {
			invoker = new AccountSetSingleMaxIndexesInvoker((SetSingleMaxIndexes) cmd);
		} else if (cmd.getClass() == SetSingleMaxRegulates.class) {
			invoker = new AccountSetSingleMaxRegulatesInvoker((SetSingleMaxRegulates) cmd);
		} else if (cmd.getClass() == SetMaxDSMReduce.class) {
			invoker = new AccountSetMaxDSMReduceInvoker((SetMaxDSMReduce) cmd);
		} else if(cmd.getClass() == SetSingleMiddleBuffer.class) {
			invoker = new AccountSetSingleMiddleBufferInvoker((SetSingleMiddleBuffer)cmd);
		} else if (cmd.getClass() == SetSingleCloudSize.class) {
			invoker = new AccountSetSingleCloudSizeInvoker((SetSingleCloudSize) cmd);
		} else if (cmd.getClass() == SetSingleUserPriority.class) {
			invoker = new AccountSetSingleUserPriorityInvoker((SetSingleUserPriority) cmd);
		}
		// 转发给BANK处理
		else if(cmd.getClass() == BatchPressRegulate.class) {
			invoker = new AccountBatchPressRegulateInvoker((BatchPressRegulate)cmd);
		}
		// 网络流量测试
		else if (cmd.getClass() == Gust.class) {
			invoker = new CommonClientGustInvoker((Gust) cmd);
		} else if (cmd.getClass() == Swarm.class) {
			invoker = new CommonServerSwarmInvoker((Swarm) cmd);
		}
		// 检测用户存在
		else if (cmd.getClass() == SeekUserArea.class) {
			invoker = new AccountSeekUserAreaInvoker((SeekUserArea) cmd);
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
		} else if (cmd.getClass() == LicenceTimeout.class) {
			invoker = new SubLicenceTimeoutInvoker((LicenceTimeout) cmd);
		}
		// CPU/虚拟机内存/系统内存/磁盘空间限制
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
		// 虚拟空间
		else if (cmd.getClass() == SetMemberCyber.class) {
			invoker = new AccountSetMemberCyberInvoker((SetMemberCyber) cmd);
		} else if (cmd.getClass() == CheckUserCyber.class) {
			invoker = new AccountCheckUserCyberInvoker((CheckUserCyber) cmd);
		}
		// 查询注册用户
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new AccountAskClusterMemberInvoker((AskClusterMember) cmd);
		}
		// 推送注册用户给BANK节点
		else if(cmd.getClass() == ShiftPushRegisterMember.class) {
			invoker = new AccountShiftPushRegisterMemberInvoker((ShiftPushRegisterMember)cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new SubOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new SubCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 发布用户级分布式应用
		else if (cmd.getClass() == DeployConductPackage.class) {
			invoker = new AccountDeployConductPackageInvoker((DeployConductPackage) cmd);
		} else if (cmd.getClass() == DeployEstablishPackage.class) {
			invoker = new AccountDeployEstablishPackageInvoker((DeployEstablishPackage) cmd);
		} else if (cmd.getClass() == DeployContactPackage.class) {
			invoker = new AccountDeployContactPackageInvoker((DeployContactPackage) cmd);
		} 
		// 发布系统级分布式应用
		else if (cmd.getClass() == DeployConductSystemPackage.class) {
			invoker = new AccountDeployConductSystemPackageInvoker((DeployConductSystemPackage) cmd);
		} else if (cmd.getClass() == DeployEstablishSystemPackage.class) {
			invoker = new AccountDeployEstablishSystemPackageInvoker((DeployEstablishSystemPackage) cmd);
		} else if (cmd.getClass() == DeployContactSystemPackage.class) {
			invoker = new AccountDeployContactSystemPackageInvoker((DeployContactSystemPackage) cmd);
		}
		// 删除分布应用
		else if (cmd.getClass() == DropConductPackage.class) {
			invoker = new AccountDropConductPackageInvoker((DropConductPackage) cmd);
		} else if (cmd.getClass() == DropEstablishPackage.class) {
			invoker = new AccountDropEstablishPackageInvoker((DropEstablishPackage) cmd);
		} else if (cmd.getClass() == DropContactPackage.class) {
			invoker = new AccountDropContactPackageInvoker((DropContactPackage) cmd);
		} 
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}
		
		// 不支持操作
		boolean success = (invoker != null);
		if (success) {
			success = AccountInvokerPool.getInstance().launch(invoker);
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
		
		// 向BANK站点申请主机编号
		if (cmd.getClass() == ShiftTakeSiteSerial.class) {
			invoker = new CommonShiftTakeSiteSerialInvoker((ShiftTakeSiteSerial) cmd);
		}
		// 转发发布分布任务的应用附件和动态链接库
		else if (cmd.getClass() == ShiftPublishSingleTaskAssistComponent.class) {
			invoker = new AccountShiftPublishSingleTaskAssistComponentInvoker((ShiftPublishSingleTaskAssistComponent) cmd);
		} else if (cmd.getClass() == ShiftPublishSingleTaskLibraryComponent.class) {
			invoker = new AccountShiftPublishSingleTaskLibraryComponentInvoker((ShiftPublishSingleTaskLibraryComponent) cmd);
		}
		// 转发删除分布应用
		else if (cmd.getClass() == ShiftDropTaskApplication.class) {
			invoker = new AccountShiftDropTaskApplicationInvoker((ShiftDropTaskApplication) cmd);
		}
		
//		// 转发发布码位计算器的应用附件和动态链接库
//		else if(cmd.getClass() == ShiftPublishSingleScalerAssistComponent.class) {
//			invoker = new AccountShiftPublishSingleScalerAssistComponentInvoker((ShiftPublishSingleScalerAssistComponent)cmd);
//		} else if(cmd.getClass() == ShiftPublishSingleScalerLibraryComponent.class) {
//			invoker = new AccountShiftPublishSingleScalerLibraryComponentInvoker((ShiftPublishSingleScalerLibraryComponent)cmd);
//		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 判断调用器有效
		boolean success = (invoker != null);
		if (success) {
			success = AccountInvokerPool.getInstance().launch(invoker);
		}
		return success;
	}

}
