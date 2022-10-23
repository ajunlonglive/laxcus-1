/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.pool;

import com.laxcus.build.invoker.*;
import com.laxcus.command.*;
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
import com.laxcus.command.stub.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.command.task.*;
import com.laxcus.command.task.talk.*;
import com.laxcus.command.traffic.*;
import com.laxcus.distribute.establish.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;

/**
 * BUILD站点异步命令管理池。
 * 
 * @author scott.liang
 * @version 1.1 09/21/2011
 * @since laxcus 1.0
 */
public class BuildCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static BuildCommandPool selfHandle = new BuildCommandPool();

	/**
	 * 构造BUILD站点异步命令管理池
	 */
	private BuildCommandPool() {
		super();
	}

	/**
	 * 返回BUILD管理池的静态句柄
	 * @return BUILD站点异步命令管理池
	 */
	public static BuildCommandPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("BuildCommandPool.getInstance");
		// 返回句柄
		return BuildCommandPool.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#accept(com.laxcus.command.Command)
	 */
	@Override
	public boolean accept(Command cmd) {
		// 必须有回显地址
		if (cmd == null || cmd.getSource() == null) {
			return false;
		}

		// 保存命令
		boolean success = add(cmd);
		// 保存命令
		if (success) {
			Tigger.command(cmd);
		}
		
		Logger.note(this, "accept", success, "from %s - '%s'", cmd.getSource(), cmd);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		// HOME站点授权“建立/删除”BUILD站点的用户资源引用
		if (cmd.getClass() == AwardCreateRefer.class) {
			invoker = new BuildAwardCreateReferInvoker((AwardCreateRefer) cmd);
		} else if (cmd.getClass() == AwardDropRefer.class) {
			invoker = new BuildAwardDropReferInvoker((AwardDropRefer) cmd);
		}
		// 设置用户资源引用命令
		else if (cmd.getClass() == SetRefer.class) {
			invoker = new BuildSetReferInvoker((SetRefer) cmd);
		}
		// 授权删除数据库
		else if (cmd.getClass() == AwardDropSchema.class) {
			invoker = new BuildAwardDropSchemaInvoker((AwardDropSchema) cmd);
		}
		// HOME授权建表/删表
		else if (cmd.getClass() == AwardCreateTable.class) {
			invoker = new BuildAwardCreateTableInvoker((AwardCreateTable) cmd);
		} else if (cmd.getClass() == AwardDropTable.class) {
			invoker = new BuildAwardDropTableInvoker((AwardDropTable) cmd);
		}
		// 获得分布组件
		else if (cmd.getClass() == TakeTaskTag.class) {
			invoker = new BuildTakeTaskTagInvoker((TakeTaskTag) cmd);
		}
//		// 向ACCOUNT站点获取码位计算器标识
//		else if (cmd.getClass() == TakeScalerTag.class) {
//			invoker = new CommonTakeScalerTagInvoker((TakeScalerTag) cmd);
//		}
		// 增加/删除ACCOUNT站点（HOME站点发送）
		else if (cmd.getClass() == PushAccountSite.class) {
			invoker = new BuildPushArchiveSiteInvoker((PushAccountSite) cmd);
		} else if (cmd.getClass() == DropAccountSite.class) {
			invoker = new CommonDropAccountSiteInvoker((DropAccountSite) cmd);
		}

		// SIFT命令，释放SIFT资源命令
		else if (cmd.getClass() == SiftStep.class) {
			invoker = new BuildEstablishSiftInvoker((SiftStep) cmd);
		} else if (cmd.getClass() == ReleaseSiftSource.class) {
			invoker = new BuildReleaseSiftSourceInvoker((ReleaseSiftSource) cmd);
		}
		// 其它
		else if (cmd.getClass() == SelectFieldToCall.class) {
			invoker = new BuildSelectFieldToCallInvoker((SelectFieldToCall) cmd);
		}
		// 切换注册站点
		else if (cmd.getClass() == SwitchHub.class) {
			invoker = new CommonSwitchHubInvoker((SwitchHub) cmd);
		}
		// 上传数据块命令
		else if (cmd.getClass() == DownloadMass.class) {
			invoker = new CommonUploadMassInvoker((DownloadMass) cmd);
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
		// 检索分布任务组件
		else if (cmd.getClass() == SeekTask.class) {
			invoker = new BuildSeekTaskInvoker((SeekTask) cmd);
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
		// 刷新注册
		else if (cmd.getClass() == RefreshPublish.class) {
			invoker = new BuildRefreshPublishInvoker((RefreshPublish) cmd);
		}
		// 当前站点状态
		else if (cmd.getClass() == SeekSiteRuntime.class) {
			invoker = new BuildSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
		}
		// 检索在线命令/在线资源
		else if (cmd.getClass() == SeekOnlineCommand.class) {
			invoker = new CommonSeekOnlineCommandInvoker((SeekOnlineCommand) cmd);
		} else if (cmd.getClass() == SeekOnlineResource.class) {
			invoker = new BuildSeekOnlineResourceInvoker((SeekOnlineResource) cmd);
		}
		// 检索用户在线注册元数据
		else if (cmd.getClass() == SeekRegisterMetadata.class) {
			invoker = new BuildSeekRegisterMetadataInvoker((SeekRegisterMetadata) cmd);
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
			invoker = new BuildTalkAskInvoker((TalkAsk) cmd);
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
			invoker = new BuildDeployTableInvoker((DeployTable) cmd);
		}
		// 加载资源引用
		else if (cmd.getClass() == ShiftTakeRefer.class) {
			invoker = new BuildShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}
		// 接受分布任务组件附件/动态链接库
		else if (cmd.getClass() == MailTaskAssistComponent.class) {
			invoker = new BuildMailTaskAssistComponentInvoker((MailTaskAssistComponent) cmd);
		} else if(cmd.getClass() == MailTaskLibraryComponent.class) {
			invoker = new BuildMailTaskLibraryComponentInvoker((MailTaskLibraryComponent)cmd);
		}
		// 删除云应用
		else if (cmd.getClass() == DropTaskApplication.class) {
			invoker = new BuildDropTaskApplicationInvoker((DropTaskApplication) cmd);
		}
		
//		// 接受码位计算器附件/动态链接库
//		else if(cmd.getClass() == MailScalerAssistComponent.class) {
//			invoker = new BuildMailScalerAssistComponentInvoker((MailScalerAssistComponent)cmd);
//		} else if(cmd.getClass() == MailScalerLibraryComponent.class) {
//			invoker = new BuildMailScalerLibraryComponentInvoker((MailScalerLibraryComponent)cmd);
//		}
		
		// 设置虚拟空间参数
		else if (cmd.getClass() == SetMemberCyber.class) {
			invoker = new BuildSetMemberCyberInvoker((SetMemberCyber) cmd);
		} else if (cmd.getClass() == CheckUserCyber.class) {
			invoker = new BuildCheckUserCyberInvoker((CheckUserCyber) cmd);
		}
		// 查找注册节点
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new BuildAskClusterMemberInvoker((AskClusterMember) cmd);
		} else if (cmd.getClass() == ShiftPushRegisterMember.class) {
			invoker = new BuildShiftPushRegisterMemberInvoker((ShiftPushRegisterMember) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new SubOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new SubCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 检索云端应用
		else if (cmd.getClass() == SeekCloudWare.class) {
			invoker = new BuildSeekCloudWareInvoker((SeekCloudWare) cmd);
		}

		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		boolean success = (invoker != null);
		if (success) {
			success = BuildInvokerPool.getInstance().launch(invoker);
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
			invoker = new BuildShiftRequestReferInvoker((ShiftRequestRefer) cmd);
		}
		// 查询表配置
		else if (cmd.getClass() == ShiftTakeTable.class) {
			invoker = new CommonShiftTakeTableInvoker((ShiftTakeTable) cmd);
		}
		// 向HOME站点获取数据块编号
		else if (cmd.getClass() == ShiftTakeStub.class) {
			invoker = new CommonShiftTakeStubInvoker((ShiftTakeStub) cmd);
		}
		// 向ACCOUNT站点获取新的分布任务组件包
		else if (cmd.getClass() == ShiftTakeTaskComponent.class) {
			invoker = new BuildShiftTakeTaskComponentInvoker((ShiftTakeTaskComponent) cmd);
		}
//		// 向ACCOUNT站点获取新的码位计算器组件包
//		else if (cmd.getClass() == ShiftTakeScalerComponent.class) {
//			invoker = new CommonShiftTakeScalerComponentInvoker((ShiftTakeScalerComponent) cmd);
//		}
		// 下载数据块命令
		else if (cmd.getClass() == ShiftDownloadMass.class) {
			invoker = new CommonShiftDownloadMassInvoker((ShiftDownloadMass) cmd);
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
			invoker = new BuildShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}
		// 找系统组件ACCOUNT站点
		else if (cmd.getClass() == ShiftLoadSystemTask.class) {
			invoker = new SubCommonShiftLoadSystemTaskInvoker((ShiftLoadSystemTask) cmd);
		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 判断调用器有效
		boolean success = (invoker != null);
		if (success) {
			success = BuildInvokerPool.getInstance().launch(invoker);
		}
		return success;
	}

}