/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.account.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.field.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.login.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.command.task.*;
import com.laxcus.command.task.talk.*;
import com.laxcus.command.traffic.*;
import com.laxcus.distribute.calculate.command.*;
import com.laxcus.distribute.conduct.command.*;
import com.laxcus.distribute.contact.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.work.invoker.*;

/**
 * WORK站点的异步命令管理池。<br>
 * 
 * WORK站点位于集群内部，属于“安全环境”，默认接收所有命令
 * 
 * @author scott.liang
 * @version 1.1 09/03/2013
 * @since laxcus 1.0
 */
public class WorkCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static WorkCommandPool selfHandle = new WorkCommandPool();

	/**
	 * 构造WORK站点异步命令管理池
	 */
	private WorkCommandPool() {
		super();
	}

	/**
	 * 返回WORK管理池的静态句柄
	 * @return WORK站点异步命令管理池
	 */
	public static WorkCommandPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("WorkCommandPool.getInstance");
		// 返回句柄
		return WorkCommandPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#accept(com.laxcus.command.Command)
	 */
	@Override
	public boolean accept(Command cmd) {
		// 来访命令必须有回显地址
		if (cmd == null || cmd.getSource() == null) {
			return false;
		}

		// 保存命令
		boolean success = add(cmd);
		// 保存命令
		if (success) {
			Tigger.command(cmd);
		}

		Logger.note(this, "accept", success, "from %s - %s", cmd.getSource(), cmd);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		// CONDUCT/TO分布计算
		if (cmd.getClass() == ToStep.class) {
			invoker = new WorkConductToInvoker((ToStep) cmd);
		}
		// CONTACT/DISTANT分布计算
		else if (cmd.getClass() == DistantStep.class) {
			invoker = new WorkContactDistantInvoker((DistantStep) cmd);
		}
		// 获得CONDUCT/CONTACT计算过程中，上一级的中间数据（接受TakeFluxData的调用器即WORK站点的上一级）
		else if (cmd.getClass() == TakeFluxData.class) {
			invoker = new CommonTakeFluxDataInvoker((TakeFluxData) cmd);
		}
		// 释放被缓存的中间数据（FluxField/FluxArea）
		else if (cmd.getClass() == ReleaseFluxField.class) {
			invoker = new CommonReleaseFluxFieldInvoker((ReleaseFluxField) cmd);
		} else if (cmd.getClass() == ReleaseFluxArea.class) {
			invoker = new CommonReleaseFluxAreaInvoker((ReleaseFluxArea) cmd);
		}
		
		// 向ACCOUNT站点获取分布任务组件
		else if (cmd.getClass() == TakeTaskTag.class) {
			invoker = new WorkTakeTaskTagInvoker((TakeTaskTag) cmd);
		}
		
//		// 去ACCOUNT获取新的码位计算器组件
//		else if(cmd.getClass() == TakeScalerTag.class) {
//			invoker = new CommonTakeScalerTagInvoker((TakeScalerTag)cmd);
//		}
//		// 去ACCOUNT站点获取新的快捷组件文件
//		else if (cmd.getClass() == TakeSwiftTag.class) {
//			invoker = new WorkTakeSwiftTagInvoker((TakeSwiftTag) cmd);
//		}
		// 增加/删除ACCOUNT站点（HOME站点发送）
		else if (cmd.getClass() == PushAccountSite.class) {
			invoker = new WorkPushArchiveSiteInvoker((PushAccountSite) cmd);
		} else if (cmd.getClass() == DropAccountSite.class) {
			invoker = new CommonDropAccountSiteInvoker((DropAccountSite) cmd);
		}

		// 筛选元数据，投递到CALL站点
		else if (cmd.getClass() == SelectFieldToCall.class) {
			invoker = new WorkSelectFieldToCallInvoker((SelectFieldToCall) cmd);
		}
		// 根据表名，去CALL站点查询关联的DATA主站点
		else if (cmd.getClass() == ShiftFindSpacePrimeSite.class) {
			invoker = new WorkShiftFindSpacePrimeSiteInvoker((ShiftFindSpacePrimeSite) cmd);
		}
		// 根据SELECT命令中的WHERE语句，去DATA站点查询关联的数据块，返回数据块编号
		else if (cmd.getClass() == ShiftFilteSelectStub.class) {
			invoker = new CommonShiftFilteSelectStubInvoker((ShiftFilteSelectStub) cmd);
		}
		// 查询数据块编号
		else if (cmd.getClass() == ShiftFindStubSite.class) {
			invoker = new CommonShiftFindStubSiteInvoker((ShiftFindStubSite) cmd);
		}
		// SELECT查询命令
		else if (cmd.getClass() == ShiftCastSelect.class) {
			invoker = new WorkShiftCastSelectInvoker((ShiftCastSelect) cmd);
		}

		// HOME授权“建立/删除”WORK站点的用户资源引用
		else if (cmd.getClass() == AwardCreateRefer.class) {
			invoker = new WorkAwardCreateReferInvoker((AwardCreateRefer) cmd);
		} else if (cmd.getClass() == AwardDropRefer.class) {
			invoker = new WorkAwardDropReferInvoker((AwardDropRefer) cmd);
		}
		// 设置用户资源引用命令
		else if (cmd.getClass() == SetRefer.class) {
			invoker = new WorkSetReferInvoker((SetRefer) cmd);
		}
		// 授权删除数据库
		else if (cmd.getClass() == AwardDropSchema.class) {
			invoker = new WorkAwardDropSchemaInvoker((AwardDropSchema) cmd);
		}
		// HOME授权建表/删表
		else if (cmd.getClass() == AwardCreateTable.class) {
			invoker = new WorkAwardCreateTableInvoker((AwardCreateTable) cmd);
		} else if (cmd.getClass() == AwardDropTable.class) {
			invoker = new WorkAwardDropTableInvoker((AwardDropTable) cmd);
		}

		// 切换注册站点
		else if (cmd.getClass() == SwitchHub.class) {
			invoker = new CommonSwitchHubInvoker((SwitchHub) cmd);
		}
		// 数据添加操作（组合后，等于CALL站点的INSERT操作）
		else if (cmd.getClass() == ShiftInsert.class) {
			invoker = new CommonShiftInsertInvoker((ShiftInsert) cmd);
		} else if (cmd.getClass() == ShiftAssertInsert.class) {
			invoker = new CommonShiftAssertInsertInvoker((ShiftAssertInsert) cmd);
		}
		// 数据删除操作（组合后，等于CALL站点的DELETE操作）
		else if (cmd.getClass() == ShiftCastDelete.class) {
			invoker = new CommonShiftCastDeleteInvoker((ShiftCastDelete) cmd);
		} else if (cmd.getClass() == ShiftAssertDelete.class) {
			invoker = new CommonShiftAssertDeleteInvoker((ShiftAssertDelete) cmd);
		}
		// 数据更新操作（组合后，等于CALL站点的UPDATE操作）
		else if (cmd.getClass() == ShiftCastUpdate.class) {
			invoker = new CommonShiftCastUpdateInvoker((ShiftCastUpdate) cmd);
		} else if (cmd.getClass() == ShiftAssertUpdate.class) {
			invoker = new CommonShiftAssertUpdateInvoker((ShiftAssertUpdate) cmd);
		}
		// 重装加载本地安全配置 / 链接库 / 安全策略
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
		else if(cmd.getClass() == ReloadLibrary.class) {
			invoker = new SubReloadLibraryInvoker((ReloadLibrary)cmd);
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
		// 远程关闭命令
		else if (cmd.getClass() == Shutdown.class) {
			invoker = new SubShutdownInvoker((Shutdown) cmd);
		}
		// 强制重新注册
		else if (cmd.getClass() == RefreshLogin.class) {
			invoker = new CommonRefreshLoginInvoker((RefreshLogin) cmd);
		}
		// 检索分布任务组件/码位计算器
		else if (cmd.getClass() == SeekTask.class) {
			invoker = new WorkSeekTaskInvoker((SeekTask) cmd);
		} 
		
		// 设置站点日志等级
		else if (cmd.getClass() == SetLogLevel.class) {
			invoker = new SubSetLogLevelInvoker((SetLogLevel) cmd);
		}

		// 服务器密文超时
		else if (cmd.getClass() == CipherTimeout.class) {
			invoker = new SubCipherTimeoutInvoker((CipherTimeout) cmd);
		}
		// 分布处理超时
		else if (cmd.getClass() == DistributedTimeout.class) {
			invoker = new SubDistributedTimeoutInvoker((DistributedTimeout) cmd);
		}
		// 最大缓存空间
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
		// 注册到HOME站点
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// 刷新发布
		else if (cmd.getClass() == RefreshPublish.class) {
			invoker = new WorkRefreshPublishInvoker((RefreshPublish) cmd);
		}
//		// 运行快捷组件
//		else if (cmd.getClass() == RunSwift.class) {
//			invoker = new WorkRunSwiftInvoker((RunSwift) cmd);
//		} 
		
		// 当前站点状态
		else if (cmd.getClass() == SeekSiteRuntime.class) {
			invoker = new WorkSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
		}
		// 检索在线命令/站点在线资源
		else if (cmd.getClass() == SeekOnlineCommand.class) {
			invoker = new CommonSeekOnlineCommandInvoker((SeekOnlineCommand) cmd);
		} else if (cmd.getClass() == SeekOnlineResource.class) {
			invoker = new WorkSeekOnlineResourceInvoker((SeekOnlineResource) cmd);
		}
		// 检索用户在线注册元数据
		else if (cmd.getClass() == SeekRegisterMetadata.class) {
			invoker = new WorkSeekRegisterMetadataInvoker((SeekRegisterMetadata) cmd);
		}
		// 分布任务组件之间的检查、协商操作
		else if (cmd.getClass() == ShiftTalkCheck.class) {
			invoker = new CommonShiftTalkCheckInvoker((ShiftTalkCheck) cmd);
		} else if (cmd.getClass() == ShiftTalkAsk.class) {
			invoker = new CommonShiftTalkAskInvoker((ShiftTalkAsk) cmd);
		}
		// 来自其它节点的检查、协商操作
		else if (cmd.getClass() == TalkCheck.class) {
			invoker = new CommonTalkCheckInvoker((TalkCheck) cmd);
		} else if (cmd.getClass() == TalkAsk.class) {
			invoker = new WorkTalkAskInvoker((TalkAsk) cmd);
		}
		// 网络流量测试
		else if (cmd.getClass() == Gust.class) {
			invoker = new CommonClientGustInvoker((Gust) cmd);
		} else if (cmd.getClass() == Swarm.class) {
			invoker = new CommonServerSwarmInvoker((Swarm) cmd);
		}
		// 内存/磁盘空间不足报警，通过HOME站点，转发给WATCH节点，在图形界面上通知系统管理员
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
		// 部署数据表
		else if (cmd.getClass() == DeployTable.class) {
			invoker = new WorkDeployTableInvoker((DeployTable) cmd);
		}
		// 加载资源引用
		else if (cmd.getClass() == ShiftTakeRefer.class) {
			invoker = new WorkShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}
		// 接受分布任务组件附件/动态链接库
		else if (cmd.getClass() == MailTaskAssistComponent.class) {
			invoker = new WorkMailTaskAssistComponentInvoker((MailTaskAssistComponent) cmd);
		} else if (cmd.getClass() == MailTaskLibraryComponent.class) {
			invoker = new WorkMailTaskLibraryComponentInvoker((MailTaskLibraryComponent) cmd);
		}
		// 删除云应用
		else if (cmd.getClass() == DropTaskApplication.class) {
			invoker = new WorkDropTaskApplicationInvoker((DropTaskApplication) cmd);
		}
		
//		// 接受码位计算器附件/动态链接库
//		else if (cmd.getClass() == MailScalerAssistComponent.class) {
//			invoker = new WorkMailScalerAssistComponentInvoker((MailScalerAssistComponent) cmd);
//		} else if(cmd.getClass() == MailScalerLibraryComponent.class) {
//			invoker = new WorkMailScalerLibraryComponentInvoker((MailScalerLibraryComponent)cmd);
//		}
//		// 接受快捷组件附件/动态链接库
//		else if (cmd.getClass() == MailSwiftAssistComponent.class) {
//			invoker = new WorkMailSwiftAssistComponentInvoker((MailSwiftAssistComponent) cmd);
//		} else if (cmd.getClass() == MailSwiftLibraryComponent.class) {
//			invoker = new WorkMailSwiftLibraryComponentInvoker((MailSwiftLibraryComponent) cmd);
//		}
		// 设置虚拟空间参数
		else if (cmd.getClass() == SetMemberCyber.class) {
			invoker = new WorkSetMemberCyberInvoker((SetMemberCyber) cmd);
		} else if (cmd.getClass() == CheckUserCyber.class) {
			invoker = new WorkCheckUserCyberInvoker((CheckUserCyber) cmd);
		}
		// 查找注册节点 / 推送注册用户
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new WorkAskClusterMemberInvoker((AskClusterMember) cmd);
		} else if (cmd.getClass() == ShiftPushRegisterMember.class) {
			invoker = new WorkShiftPushRegisterMemberInvoker((ShiftPushRegisterMember) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new SubOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new SubCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 检索云端应用
		else if (cmd.getClass() == SeekCloudWare.class) {
			invoker = new WorkSeekCloudWareInvoker((SeekCloudWare) cmd);
		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		boolean success = (invoker != null);
		if (success) {
			success = WorkInvokerPool.getInstance().launch(invoker);
		} else {
			unsupport(cmd);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#press(com.laxcus.command.Command)
	 */
	@Override
	public boolean press(Command cmd) {
		EchoInvoker invoker = null;

		if (cmd.getClass() == ShiftRequestRefer.class) {
			invoker = new WorkShiftRequestReferInvoker((ShiftRequestRefer) cmd);
		} else if (cmd.getClass() == ShiftTakeTable.class) {
			invoker = new CommonShiftTakeTableInvoker((ShiftTakeTable) cmd);
		}
		// 获取分布任务组件
		else if (cmd.getClass() == ShiftTakeTaskComponent.class) {
			invoker = new WorkShiftTakeTaskComponentInvoker((ShiftTakeTaskComponent) cmd);
		}
		
//		// 获取码位计算器组件
//		else if (cmd.getClass() == ShiftTakeScalerComponent.class) {
//			invoker = new CommonShiftTakeScalerComponentInvoker((ShiftTakeScalerComponent) cmd);
//		}
//		// 获得快捷组件
//		else if (cmd.getClass() == ShiftTakeSwiftComponent.class) {
//			invoker = new WorkShiftTakeSwiftComponentInvoker((ShiftTakeSwiftComponent) cmd);
//		}
		// 查数据块编号
		else if(cmd.getClass() == ShiftFindStubSite.class) {
			invoker = new CommonShiftFindStubSiteInvoker((ShiftFindStubSite)cmd);
		}
		// 执行SELECT检索
		else if (cmd.getClass() == ShiftCastSelect.class) {
			invoker = new WorkShiftCastSelectInvoker((ShiftCastSelect) cmd);
		}
		// 获得ACCOUNT站点地址
		else if (cmd.getClass() == ShiftTakeSigerSite.class) {
			invoker = new CommonShiftTakeSigerSiteInvoker((ShiftTakeSigerSite) cmd);
		}
		// 注册到HOME站点
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// 加载资源引用
		else if (cmd.getClass() == ShiftTakeRefer.class) {
			invoker = new WorkShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}
		// 找系统组件ACCOUNT站点
		else if (cmd.getClass() == ShiftLoadSystemTask.class) {
			invoker = new SubCommonShiftLoadSystemTaskInvoker((ShiftLoadSystemTask) cmd);
		}

		// 以上不成立，去自定义调用器检查
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 判断调用器有效
		boolean success = (invoker != null);
		if (success) {
			success = WorkInvokerPool.getInstance().launch(invoker);
		}
		return success;
	}

}