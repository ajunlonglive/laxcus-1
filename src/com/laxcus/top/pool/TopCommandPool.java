/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.account.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.login.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.relate.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.reserve.*;
import com.laxcus.command.scan.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.Shutdown;
import com.laxcus.command.site.*;
import com.laxcus.command.site.find.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.stub.*;
import com.laxcus.command.task.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.*;
import com.laxcus.top.invoker.*;
import com.laxcus.util.*;

/**
 * TOP站点的异步命令管理池。<br>
 * 提供TOP站点上的异步命令处理服务。
 * 
 * @author scott.liang
 * @version 1.2 6/30/2012
 * @since laxcus 1.0
 */
public final class TopCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static TopCommandPool selfHandle = new TopCommandPool();

	/**
	 * 初始化异步命令管理池
	 */
	private TopCommandPool() {
		super();
	}

	/**
	 * 返回异步命令管理池的静态句柄
	 * @return
	 */
	public static TopCommandPool getInstance() {
		return TopCommandPool.selfHandle;
	}
	
	/** 内部默认使用命令。TOP节点是备份状态（Monitor）时，来自WATCH的检查。 **/
	private Class<?>[] SKIP_COMMANDS = new Class<?>[] { SwitchHub.class,
			SeekSiteRuntime.class, SeekOnlineCommand.class,
			SeekOnlineResource.class, Gust.class, Swarm.class, AskSite.class };

	/**
	 * 判断是需要跨过的命令
	 * @param cmd 命令
	 * @return 返回真或者假
	 */
	private boolean isSkip(Command cmd) {
		// 判断是内部命令
		for (int i = 0; i < SKIP_COMMANDS.length; i++) {
			if (cmd.getClass() == SKIP_COMMANDS[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 受理一个异步操作命令。只接受来自终端和HOME站点的命令。
	 * @param cmd 异步操作命令
	 * @return 接受返回“真”，否则“假”。
	 */
	@Override
	public boolean accept(Command cmd) {
		// 没有定义回显地址不受理
		Cabin cabin = cmd.getSource();
		if (cabin == null) {
			return false;
		}

		Node node = cabin.getNode();
		// 是TOP下属站点（HOME/BANK/LOG/WATCH/TOP MONITOR）发来的命令
		boolean success = HomeOnTopPool.getInstance().contains(node);
		if (!success) {
			success = BankOnTopPool.getInstance().contains(node);
		}
		if (!success) {
			success = WatchOnTopPool.getInstance().contains(node);
		}
		if (!success) {
			success = LogOnTopPool.getInstance().contains(node);
		}
		if (!success) {
			success = MonitorOnTopPool.getInstance().contains(node);
		}

		// TOP主/从站点交互
		if (!success) {
			// 判断是TOP主站点（管理站点）发来的命令
			Node hub = TopLauncher.getInstance().getManager();
			success = (Laxkit.compareTo(hub, node) == 0);
			// 判断是TOP从站点（监视器站点）发来的命令
			if (!success) {
				success = TopLauncher.getInstance().hasMonitor(node);
			}
		}

		// 判断是跨集群的内部命令
		if (!success) {
			success = isSkip(cmd);
		}

		// 保存命令
		if (success) {
			success = add(cmd);
			Tigger.command(cmd);
		}
		
		Logger.note(this, "accept", success, "from %s - %s", cabin, cmd);

		return success;
	}

	/**
	 * 检查和执行异步处理命令
	 * @param cmd 命令句柄
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		// LAXCUS 3.X命令处理
		if (cmd.getClass() == CreateUser.class) {
			invoker = new TopCreateUserInvoker((CreateUser) cmd);
		} else if (cmd.getClass() == DropUser.class) {
			invoker = new TopDropUserInvoker((DropUser) cmd);
		} else if (cmd.getClass() == AlterUser.class) {
			invoker = new TopAlterUserInvoker((AlterUser) cmd);
		} else if (cmd.getClass() == DropSchema.class) {
			invoker = new TopDropSchemaInvoker((DropSchema) cmd);
		} else if (cmd.getClass() == CreateTable.class) {
			invoker = new TopCreateTableInvoker((CreateTable) cmd);
		} else if (cmd.getClass() == DropTable.class) {
			invoker = new TopDropTableInvoker((DropTable) cmd);
		}

		// 获得账号实例 / 账号的资源引用
		else if (cmd.getClass() == TakeAccount.class) {
			invoker = new TopTakeAccountInvoker((TakeAccount) cmd);
		} else if (cmd.getClass() == TakeRefer.class) {
			invoker = new TopTakeReferInvoker((TakeRefer) cmd);
		}
		// 刷新资源引用，来自BANK站点
		else if (cmd.getClass() == RefreshRefer.class) {
			invoker = new TopRefreshReferInvoker((RefreshRefer) cmd);
		}
		// HOME站点来获取数据表资源
		else if (cmd.getClass() == TakeTable.class) {
			invoker = new TopTakeTableInvoker((TakeTable) cmd);
		}
		// 找包含系统组件的ACCOUNT站点
		else if (cmd.getClass() == TakeSystemTaskSite.class) {
			invoker = new TopTakeSystemTaskSiteInvoker((TakeSystemTaskSite) cmd);
		}

		// TOP要求HOME集群，刷新元数据
		else if (cmd.getClass() == RefreshMetadata.class) {
			invoker = new TopRefreshMetadataInvoker((RefreshMetadata) cmd);
		}
		// BANK通知TOP刷新发布的组件
		else if (cmd.getClass() == RefreshPublish.class) {
			invoker = new TopRefreshPublishInvoker((RefreshPublish) cmd);
		}
		// 获取分布的CALL站点记录。GATE站点发出，BANK/TOP中转
		else if (cmd.getClass() == TakeCallItem.class) {
			invoker = new TopTakeCallItemInvoker((TakeCallItem) cmd);
		}
		// 检索TOP集群上的注册用户签名
		else if (cmd.getClass() == SeekUserSite.class) {
			invoker = new TopSeekUserSiteInvoker((SeekUserSite) cmd);
		} 

		// 查找站点命令
		else if (cmd.getClass() == FindSite.class) {
			invoker = new TopFindSiteInvoker((FindSite) cmd);
		} else if (cmd.getClass() == FindTableSite.class) {
			invoker = new TopFindTableSiteInvoker((FindTableSite) cmd);
		} else if (cmd.getClass() == FindUserSite.class) {
			invoker = new TopFindUserSiteInvoker((FindUserSite) cmd);
		}

		else if (cmd.getClass() == SeekUserTable.class) {
			invoker = new TopSeekUserTableInvoker((SeekUserTable) cmd);
		} else if (cmd.getClass() == SeekUserTask.class) {
			invoker = new TopSeekUserTaskInvoker((SeekUserTask) cmd);
		}
		// 扫描与账号匹配的CALL/HOME站点
		else if (cmd.getClass() == TakeOwnerCall.class) {
			invoker = new TopFindRelateCallInvoker((TakeOwnerCall) cmd);
		} else if (cmd.getClass() == FindRelateHome.class) {
			invoker = new TopFindRelateHomeInvoker((FindRelateHome) cmd);
		}

		// 查找ARCHIVE站点
		else if (cmd.getClass() == TakeSigerSite.class) {
			invoker = new TopTakeSigerSiteInvoker((TakeSigerSite) cmd);
		}
		
//		// 转发ARCHIVE站点记录到HOME站点
//		else if (cmd.getClass() == ShiftPushArchiveSite.class) {
//			invoker = new TopShiftPushArchiveSiteInvoker((ShiftPushArchiveSite) cmd);
//		} else if (cmd.getClass() == ShiftDropArchiveSite.class) {
//			invoker = new TopShiftDropArchiveSiteInvoker((ShiftDropArchiveSite) cmd);
//		}

		// 搜索分布任务组件和码位计算器的分布站点地址
		else if (cmd.getClass() == SeekTask.class) {
			invoker = new TopSeekTaskInvoker((SeekTask) cmd);
		} 
//		else if (cmd.getClass() == SeekScaler.class) {
//			invoker = new TopSeekScalerInvoker((SeekScaler) cmd);
//		}

		// 分析用户资源
		else if (cmd.getClass() == ScanSchema.class) {
			invoker = new TopScanSchemaInvoker((ScanSchema) cmd);
		} else if (cmd.getClass() == ScanTable.class) {
			invoker = new TopScanTableInvoker((ScanTable) cmd);
		} else if (cmd.getClass() == ScanEntity.class) {
			invoker = new TopScanEntityInvoker((ScanEntity) cmd);
		}

		/** 系统业务命令 **/
		// 转换数据块状态
		else if (cmd.getClass() == Rush.class) {
			invoker = new TopRushInvoker((Rush) cmd);
		} else if (cmd.getClass() == Compact.class) {
			invoker = new TopCompactInvoker((Compact) cmd);
		} else if (cmd.getClass() == SetDSMReduce.class) {
			invoker = new TopSetDSMReduceInvoker((SetDSMReduce) cmd);
		}
		// 强制要求指定站点重新注册
		else if (cmd.getClass() == RefreshLogin.class) {
			invoker = new TopRefreshLoginInvoker((RefreshLogin) cmd);
		}
		// TOP管理站点/监视站点之间的备份操作
		else if (cmd.getClass() == DispatchReserveResource.class) {
			invoker = new TopDispatchReserveResourceInvoker((DispatchReserveResource) cmd);  // 在管理站点
		} else if (cmd.getClass() == CommitReserveResource.class) {
			invoker = new TopCommitReserveResourceInvoker((CommitReserveResource) cmd); // 在监视站点（下载）
		} else if (cmd.getClass() == TakeReserveResource.class) {
			invoker = new TopTakeReserveResourceInvoker((TakeReserveResource) cmd); // 在管理站点（上传）
		}
		// 申请数据块标识号命令
		else if (cmd.getClass() == TakeStub.class) {
			invoker = new TopTakeStubInvoker((TakeStub) cmd);
		}
		// TOP管理站点通知下属监视站点、WATCH站点，推送一个子级站点（发出CAST SITE命令）。
		else if (cmd.getClass() == ShiftCastSite.class) {
			invoker = new CommonShiftCastSiteInvoker((ShiftCastSite) cmd);
		}
		// TOP管理站点发送，TOP监视站点接收的子级站点注册、注销、故障销毁命令（CAST SITE命令子类）
		else if(cmd.getClass() == PushSite.class) {
			invoker = new TopPushSiteInvoker((PushSite)cmd);
		} else if(cmd.getClass() == DropSite.class) {
			invoker = new TopDropSiteInvoker((DropSite)cmd);
		} else if(cmd.getClass() == DestroySite.class) {
			invoker = new TopDestroySiteInvoker((DestroySite)cmd);
		}
		// TOP监视站点通知下属站点，切换注册站点
		else if (cmd.getClass() == ShiftSwitchHub.class) {
			invoker = new TopShiftSwitchHubInvoker((ShiftSwitchHub) cmd);
		}
		// 新的TOP管理站点通知监视站点，切换注册地址
		else if (cmd.getClass() == ShiftSwitchPartner.class) {
			invoker = new TopShiftSwitchPartnerInvoker((ShiftSwitchPartner) cmd);
		}
		// TOP监视站点接受新的管理站点命令，重新注册到新管理站点下面。
		else if (cmd.getClass() == SwitchPartner.class) {
			invoker = new TopSwitchPartnerInvoker((SwitchPartner) cmd);
		}
		// 询问分布站点地址命令。这个命令由WATCH站点发出
		else if (cmd.getClass() == AskSite.class) {
			invoker = new TopAskSiteInvoker((AskSite) cmd);
		} 
		// 查询一个注册站点存在
		else if (cmd.getClass() == AssertSite.class) {
			invoker = new TopAssertSiteInvoker((AssertSite) cmd);
		}
		// 处理密钥令牌
		else if (cmd.getClass() == CreateSecureToken.class) {
			invoker = new TopCreateSecureTokenInvoker((CreateSecureToken) cmd);
		} else if (cmd.getClass() == DropSecureToken.class) {
			invoker = new TopDropSecureTokenInvoker((DropSecureToken) cmd);
		} else if (cmd.getClass() == FlushSecureToken.class) {
			invoker = new TopFlushSecureTokenInvoker((FlushSecureToken) cmd);
		} else if (cmd.getClass() == ShowSecureToken.class) {
			invoker = new TopShowSecureTokenInvoker((ShowSecureToken) cmd);
		} else if (cmd.getClass() == SetSecureSize.class) {
			invoker = new TopSetSecureSizeInvoker((SetSecureSize) cmd);
		}
		// 重装加载链接库、安全策略、许可证、追踪命令
		else if (cmd.getClass() == ReloadLibrary.class) {
			invoker = new TopReloadLibraryInvoker((ReloadLibrary) cmd);
		} else if (cmd.getClass() == ReloadSecurityPolicy.class) {
			invoker = new TopReloadSecurityPolicyInvoker((ReloadSecurityPolicy) cmd);
		} else if (cmd.getClass() == ReloadLicence.class) {
			invoker = new TopReloadLicenceInvoker((ReloadLicence) cmd);
		} else if (cmd.getClass() == MailLicence.class) {
			invoker = new TopMailLicenceInvoker((MailLicence) cmd);
		} else if (cmd.getClass() == ScanCommandStack.class) {
			invoker = new TopScanCommandStackInvoker((ScanCommandStack) cmd);
		} else if (cmd.getClass() == ReleaseMemory.class) {
			invoker = new TopReleaseMemoryInvoker((ReleaseMemory) cmd);
		} else if (cmd.getClass() == ReleaseMemoryInterval.class) {
			invoker = new TopReleaseMemoryIntervalInvoker((ReleaseMemoryInterval) cmd);
		}
		// 应答数据传输
		else if (cmd.getClass() == ReplyPacketMode.class) {
			invoker = new TopReplyPacketModeInvoker((ReplyPacketMode) cmd);
		} else if (cmd.getClass() == ReplyPacketSize.class) {
			invoker = new TopReplyPacketSizeInvoker((ReplyPacketSize) cmd);
		} else if (cmd.getClass() == ReplySendTimeout.class) {
			invoker = new TopReplySendTimeoutInvoker((ReplySendTimeout) cmd);
		} else if (cmd.getClass() == ReplyReceiveTimeout.class) {
			invoker = new TopReplyReceiveTimeoutInvoker((ReplyReceiveTimeout) cmd);
		} else if(cmd.getClass() == ReplyFlowControl.class) {
			invoker = new TopReplyFlowControlInvoker((ReplyFlowControl)cmd);
		}
		// 远程关闭命令
		else if (cmd.getClass() == Shutdown.class) {
			invoker = new TopShutdownInvoker((Shutdown) cmd);
		}
		// 设置日志模式
		else if (cmd.getClass() == SetLogLevel.class) {
			invoker = new TopSetLogLevelInvoker((SetLogLevel) cmd);
		}
		
		// 客户机/服务器密文超时
		else if (cmd.getClass() == CipherTimeout.class) {
			invoker = new TopCipherTimeoutInvoker((CipherTimeout) cmd);
		}
		// 分布处理超时
		else if (cmd.getClass() == DistributedTimeout.class) {
			invoker = new TopDistributedTimeoutInvoker((DistributedTimeout) cmd);
		}
		// 最大缓存空间 / 最大调用器数目
		else if (cmd.getClass() == MaxEchoBuffer.class) {
			invoker = new TopMaxEchoBufferInvoker((MaxEchoBuffer) cmd);
		} else if(cmd.getClass() == MaxInvoker.class) {
			invoker = new TopMaxInvokerInvoker((MaxInvoker)cmd);
		}
		// 检测系统信息
		else if (cmd.getClass() == CheckSystemInfo.class) {
			invoker = new TopCheckSystemInfoInvoker((CheckSystemInfo) cmd);
		}
		// 强制重新加载和发布自动JAR包
		else if (cmd.getClass() == ReloadCustom.class) {
			invoker = new TopReloadCustomInvoker((ReloadCustom) cmd);
		}
		// 注册站点
		else if (cmd.getClass() == LoginSite.class) {
			invoker = new TopLoginSiteInvoker((LoginSite) cmd);
		}
		// 检索用户日志
		else if (cmd.getClass() == ScanUserLog.class) {
			invoker = new TopScanUserLogInvoker((ScanUserLog) cmd);
		}
		// 当前站点状态
		else if (cmd.getClass() == SeekSiteRuntime.class) {
			invoker = new TopSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
		}
		// 检索在线命令
		else if (cmd.getClass() == SeekOnlineCommand.class) {
			invoker = new CommonSeekOnlineCommandInvoker((SeekOnlineCommand) cmd);
		}
		// 检索用户元数据
		else if(cmd.getClass() == SeekRegisterMetadata.class) {
			invoker = new TopSeekRegisterMetadataInvoker((SeekRegisterMetadata)cmd);
		} 
		// 检索分布资源
		else if (cmd.getClass() == SeekUserArea.class) {
			invoker = new TopSeekUserAreaInvoker((SeekUserArea) cmd);
		}
		// 扫描用户关联的时间间隔
		else if (cmd.getClass() == ScanLinkTime.class) {
			invoker = new TopScanLinkTimeInvoker((ScanLinkTime) cmd);
		}
		// 网络流量测试
		else if (cmd.getClass() == Gust.class) {
			invoker = new CommonClientGustInvoker((Gust) cmd);
		} else if (cmd.getClass() == Swarm.class) {
			invoker = new CommonServerSwarmInvoker((Swarm) cmd);
		}
		// 发布用户到指定站点
		else if (cmd.getClass() == DeployUser.class) {
			invoker = new TopDeployUserInvoker((DeployUser) cmd);
		} else if (cmd.getClass() == EraseUser.class) {
			invoker = new TopEraseUserInvoker((EraseUser) cmd);
		}
		// 发布数据表
		else if (cmd.getClass() == DeployTable.class) {
			invoker = new TopDeployTableInvoker((DeployTable) cmd);
		}
		// 内存/磁盘空间不足报警，通过WATCH节点
		else if (cmd.getClass() == VMMemoryMissing.class) {
			invoker = new TopVMMemoryMissingInvoker((VMMemoryMissing) cmd);
		} else if (cmd.getClass() == MemoryMissing.class) {
			invoker = new TopMemoryMissingInvoker((MemoryMissing) cmd);
		} else if (cmd.getClass() == DiskMissing.class) {
			invoker = new TopDiskMissingInvoker((DiskMissing) cmd);
		} else if (cmd.getClass() == LicenceTimeout.class) {
			invoker = new TopLicenceTimeoutInvoker((LicenceTimeout) cmd);
		}
		// CPU/内存/磁盘空间限制
		else if (cmd.getClass() == MostCPU.class) {
			invoker = new TopMostCPUInvoker((MostCPU) cmd);
		} else if(cmd.getClass() == MostVMMemory.class) {
			invoker = new TopMostVMMemoryInvoker((MostVMMemory)cmd);
		} else if (cmd.getClass() == LeastMemory.class) {
			invoker = new TopLeastMemoryInvoker((LeastMemory) cmd);
		} else if (cmd.getClass() == LeastDisk.class) {
			invoker = new TopLeastDiskInvoker((LeastDisk) cmd);
		} else if (cmd.getClass() == CheckSitePath.class) {
			invoker = new TopCheckSitePathInvoker((CheckSitePath) cmd);
		} else if (cmd.getClass() == ReflectPort.class) {
			invoker = new TopReflectPortInvoker((ReflectPort) cmd);
		}
		// 集群用户的虚拟空间
		else if (cmd.getClass() == SetMemberCyber.class) {
			invoker = new TopSetMemberCyberInvoker((SetMemberCyber) cmd);
		} else if (cmd.getClass() == SetFrontCyber.class) {
			invoker = new TopSetFrontCyberInvoker((SetFrontCyber) cmd);
		} else if (cmd.getClass() == CheckUserCyber.class) {
			invoker = new TopCheckUserCyberInvoker((CheckUserCyber) cmd);
		}
		// WATCH节点检索TOP集群注册成员
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new TopAskClusterMemberInvoker((AskClusterMember) cmd);
		} else if (cmd.getClass() == PushRegisterMember.class) {
			invoker = new TopPushRegisterMemberInvoker((PushRegisterMember) cmd);
		} else if (cmd.getClass() == DropRegisterMember.class) {
			invoker = new TopDropRegisterMemberInvoker((DropRegisterMember) cmd);
		} else if (cmd.getClass() == PushOnlineMember.class) {
			invoker = new TopPushOnlineMemberInvoker((PushOnlineMember) cmd);
		} else if (cmd.getClass() == DropOnlineMember.class) {
			invoker = new TopDropOnlineMemberInvoker((DropOnlineMember) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new TopOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new TopCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 获得工作节点地址
		else if (cmd.getClass() == TakeJobSite.class) {
			invoker = new TopTakeJobSiteInvoker((TakeJobSite) cmd);
		}
		//
		else if(cmd.getClass() == ShiftLoginSite.class) {
			invoker = new TopShiftLoginSiteInvoker((ShiftLoginSite)cmd);
		}
		//
		else if(cmd.getClass() == CheckUserCost.class) {
			invoker = new TopCheckUserCostInvoker((CheckUserCost)cmd);
		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 判断成立
		boolean success = (invoker != null);
		if (success) {
			success = TopInvokerPool.getInstance().launch(invoker);
		} else {
			unsupport(cmd); // 发送不支持通知给客户机
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#post(com.laxcus.command.Command)
	 */
	@Override
	public boolean press(Command cmd) {
		EchoInvoker invoker = null;

		// 判断是自定义命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 启动调用器
		boolean success = (invoker != null);
		if (success) {
			success = TopInvokerPool.getInstance().launch(invoker);
		}
		return success;
	}

}