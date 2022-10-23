/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.account.*;
import com.laxcus.command.cross.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.field.*;
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
import com.laxcus.command.shutdown.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.find.*;
import com.laxcus.command.site.front.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.stub.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.command.task.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.home.*;
import com.laxcus.home.invoker.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * HOME站点的异步命令管理池。<br>
 * 
 * 受理和处理来自TOP站点和下属站点的异步命令。
 * 
 * @author scott.liang
 * @version 1.3 07/08/2013
 * @since laxcus 1.0
 */
public class HomeCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static HomeCommandPool selfHandle = new HomeCommandPool();

	/**
	 * 初始化异步命令管理池
	 */
	private HomeCommandPool() {
		super();
	}

	/**
	 * 返回异步命令管理池的静态句柄
	 * @return HomeCommandPool实例
	 */
	public static HomeCommandPool getInstance() {
		return HomeCommandPool.selfHandle;
	}

	/** 内部默认使用命令 **/
	private Class<?>[] SKIP_COMMANDS = new Class<?>[] { SwitchHub.class,
			SeekSiteRuntime.class, SeekOnlineCommand.class,
			SeekOnlineResource.class, Gust.class, Swarm.class, AskSite.class };

	/**
	 * 判断是规定的内部命令，跨过检查
	 * @param cmd 命令实例
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

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.CommandPool#accept(com.laxcus.command.Command)
	 */
	@Override
	public boolean accept(Command cmd) {
		// 没有定义回显地址不受理
		Cabin cabin = cmd.getSource();
		if (cabin == null) {
			return false;
		}

		Node node = cabin.getNode();
		// 是TOP站点发来的命令
		boolean success = HomeLauncher.getInstance().isHub(node);
		// HOME站点的下属站点（CALL/DATA/WORK/BUILD/WATCH）发来的命令
		if (!success) {
			success = CallOnHomePool.getInstance().contains(node);
		}
		if (!success) {
			success = DataOnHomePool.getInstance().contains(node);
		}
		if (!success) {
			success = WorkOnHomePool.getInstance().contains(node);
		}
		if (!success) {
			success = BuildOnHomePool.getInstance().contains(node);
		}
		if (!success) {
			success = WatchOnHomePool.getInstance().contains(node);
		}
		if (!success) {
			success = MonitorOnHomePool.getInstance().contains(node);
		}
		if (!success) {
			success = LogOnHomePool.getInstance().contains(node);
		}

		// 判断是HOME管理站点发来的命令
		if (!success) {
			Node hub = HomeLauncher.getInstance().getManager();
			success = (Laxkit.compareTo(node, hub) == 0);
			// 判断来自监视站点（通常是发送“SWITCH PARTENT”命令）
			if (!success) {
				success = HomeLauncher.getInstance().hasMonitor(node);
			}
		}

		// 判断是跨集群的内部命令
		if (!success) {
			success = isSkip(cmd);
		}

		// 保存命令
		if (success) {
			success = add(cmd);
			// 记录命令
			Tigger.command(cmd);
		}

		Logger.note(this, "accept", success, "from %s - %s", cabin, cmd);
		
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		// LAXCUS 3.X新命令
		if (cmd.getClass() == RefreshRefer.class) {
			invoker = new HomeRefreshReferInvoker((RefreshRefer) cmd);
		}
		// 设置用户资源引用命令
		else if (cmd.getClass() == SetRefer.class) {
			invoker = new HomeSetReferInvoker((SetRefer) cmd);
		}
		// 取HOME节点上的资源引用
		else if (cmd.getClass() == TakeRefer.class) {
			invoker = new HomeTakeReferInvoker((TakeRefer) cmd);
		}
		// 找包含系统组件的ACCOUNT站点
		else if (cmd.getClass() == TakeSystemTaskSite.class) {
			invoker = new HomeTakeSystemTaskSiteInvoker((TakeSystemTaskSite) cmd);
		}

		// TOP站点授权“建立/删除”HOME站点的用户资源引用
		else if (cmd.getClass() == AwardCreateRefer.class) {
			invoker = new HomeAwardCreateReferInvoker((AwardCreateRefer) cmd);
		} else if (cmd.getClass() == AwardDropRefer.class) {
			invoker = new HomeAwardDropReferInvoker((AwardDropRefer) cmd);
		}
		// 授权修改账号密码
		else if (cmd.getClass() == AwardAlterUser.class) {
			invoker = new HomeAwardAlterUserInvoker((AwardAlterUser) cmd);
		}
		// 授权删除数据库
		else if (cmd.getClass() == AwardDropSchema.class) {
			invoker = new HomeAwardDropSchemaInvoker((AwardDropSchema) cmd);
		}
		// 授权建立/删除数据表
		else if (cmd.getClass() == AwardCreateTable.class) {
			invoker = new HomeAwardCreateTableInvoker((AwardCreateTable) cmd);
		} else if (cmd.getClass() == AwardDropTable.class) {
			invoker = new HomeAwardDropTableInvoker((AwardDropTable) cmd);
		}

		// 刷新元数据（HOME通知DATA/BUILD/WORK站点，重新提交元数据到CALL站点）
		else if (cmd.getClass() == RefreshMetadata.class) {
			invoker = new HomeRefreshMetadataInvoker((RefreshMetadata) cmd);
		}
		// 强制要求指定站点重新注册
		else if (cmd.getClass() == RefreshLogin.class) {
			invoker = new HomeRefreshLoginInvoker((RefreshLogin) cmd);
		}
		// 获得表配置
		else if (cmd.getClass() == TakeTable.class) {
			invoker = new HomeTakeTableInvoker((TakeTable) cmd);
		}
		// CACHE状态数据块，强制转换为CHUNK状态数据块
		else if (cmd.getClass() == Rush.class) {
			invoker = new HomeRushInvoker((Rush) cmd);
		} else if (cmd.getClass() == Compact.class) {
			invoker = new HomeCompactInvoker((Compact) cmd);
		} else if (cmd.getClass() == SetDSMReduce.class) {
			invoker = new HomeSetDSMReduceInvoker((SetDSMReduce) cmd);
		}
		// 加载/卸载命令
		else if (cmd.getClass() == LoadIndex.class) {
			invoker = new HomeLoadIndexInvoker((LoadIndex) cmd);
		} else if (cmd.getClass() == StopIndex.class) {
			invoker = new HomeStopIndexInvoker((StopIndex) cmd);
		} else if (cmd.getClass() == LoadEntity.class) {
			invoker = new HomeLoadEntityInvoker((LoadEntity) cmd);
		} else if (cmd.getClass() == StopEntity.class) {
			invoker = new HomeStopEntityInvoker((StopEntity) cmd);
		}
		// 查询数据块尺寸命令
		else if (cmd.getClass() == ScanEntity.class) {
			invoker = new HomeScanEntityInvoker((ScanEntity) cmd);
		}
		// 查找关联站点命令
		else if (cmd.getClass() == FindSite.class) {
			invoker = new HomeFindSiteInvoker((FindSite) cmd);
		} else if (cmd.getClass() == FindTableSite.class) {
			invoker = new HomeFindTableSiteInvoker((FindTableSite) cmd);
		} else if (cmd.getClass() == FindPhaseSite.class) {
			invoker = new HomeFindPhaseSiteInvoker((FindPhaseSite) cmd);
		} else if (cmd.getClass() == FindUserSite.class) {
			invoker = new HomeFindUserSiteInvoker((FindUserSite) cmd);
		}
		// 检索用户分布资源
		else if (cmd.getClass() == SeekUserSite.class) {
			invoker = new HomeSeekUserSiteInvoker((SeekUserSite) cmd);
		} else if (cmd.getClass() == SeekUserTable.class) {
			invoker = new HomeSeekUserTableInvoker((SeekUserTable) cmd);
		} else if (cmd.getClass() == SeekUserTask.class) {
			invoker = new HomeSeekUserTaskInvoker((SeekUserTask) cmd);
		}
		// 检索用户关联站点
		else if (cmd.getClass() == SeekFrontSite.class) {
			invoker = new HomeSeekFrontSiteInvoker((SeekFrontSite) cmd);
		} else if (cmd.getClass() == SeekFrontUser.class) {
			invoker = new HomeSeekFrontUserInvoker((SeekFrontUser) cmd);
		}
		// CALL/WORK/BUILD站点请求分配资源引用
		else if (cmd.getClass() == RequestCallRefer.class) {
			invoker = new HomeRequestCallReferInvoker((RequestCallRefer) cmd);
		} else if (cmd.getClass() == RequestWorkRefer.class) {
			invoker = new HomeRequestWorkReferInvoker((RequestWorkRefer) cmd);
		} else if (cmd.getClass() == RequestBuildRefer.class) {
			invoker = new HomeRequestBuildReferInvoker((RequestBuildRefer) cmd);
		}
		// HOME管理站点/监视站点之间的备份操作
		else if (cmd.getClass() == DispatchReserveResource.class) {
			invoker = new HomeDispatchReserveResourceInvoker((DispatchReserveResource) cmd); // 在管理站点
		} else if (cmd.getClass() == CommitReserveResource.class) {
			invoker = new HomeCommitReserveResourceInvoker((CommitReserveResource) cmd); // 在监视站点（下载）
		} else if (cmd.getClass() == TakeReserveResource.class) {
			invoker = new HomeTakeReserveResourceInvoker((TakeReserveResource) cmd); // 在管理站点（上传）
		}
		// 获得数据块标识号命令
		else if (cmd.getClass() == TakeStub.class) {
			invoker = new HomeTakeStubInvoker((TakeStub) cmd);
		}
		// 通知CALL撤销DATA/WORK/BUILD站点数据
		else if (cmd.getClass() == ShiftDropField.class) {
			invoker = new HomeShiftDropFieldInvoker((ShiftDropField) cmd);
		}
		// 请求DATA/WORK/BUILD站点，向CALL站点投递元数据
		else if (cmd.getClass() == ShiftSelectFieldToCall.class) {
			invoker = new HomeShiftSelectFieldToCallInvoker((ShiftSelectFieldToCall) cmd);
		}
		// 查找与某个账号关联的CALL/HOME站点
		else if (cmd.getClass() == TakeOwnerCall.class) {
			invoker = new HomeFindRelateCallInvoker((TakeOwnerCall) cmd);
		} else if (cmd.getClass() == FindRelateHome.class) {
			invoker = new HomeFindRelateHomeInvoker((FindRelateHome) cmd);
		}
		// HOME管理站点通知HOME监视站点/WATCH站点，推送子级站点（发出CAST SITE命令）
		else if (cmd.getClass() == ShiftCastSite.class) {
			invoker = new CommonShiftCastSiteInvoker((ShiftCastSite) cmd);
		}
		// HOME管理站点发送，HOME监视站点接收的子级站点注册、注销、故障销毁命令（接收CAST SITE命令子类）
		else if(cmd.getClass() == PushSite.class) {
			invoker = new HomePushSiteInvoker((PushSite)cmd);
		} else if(cmd.getClass() == DropSite.class) {
			invoker = new HomeDropSiteInvoker((DropSite)cmd);
		} else if(cmd.getClass() == DestroySite.class) {
			invoker = new HomeDestroySiteInvoker((DestroySite)cmd);
		}

		// 新的HOME管理站点向下属的站点发出切换站点命令
		else if(cmd.getClass() == ShiftSwitchHub.class) {
			invoker = new HomeShiftSwitchHubInvoker((ShiftSwitchHub)cmd);
		}
		// 来自TOP站点，要求切换注册站点命令
		else if (cmd.getClass() == SwitchHub.class) {
			invoker = new CommonSwitchHubInvoker((SwitchHub) cmd);
		}
		// 切换HOME同级站点
		else if (cmd.getClass() == ShiftSwitchPartner.class) {
			invoker = new HomeShiftSwitchPartnerInvoker((ShiftSwitchPartner) cmd);
		} else if (cmd.getClass() == SwitchPartner.class) {
			invoker = new HomeSwitchPartnerInvoker((SwitchPartner) cmd);
		}
		// 询问分布站点，只能由WATCH站点发出
		else if (cmd.getClass() == AskSite.class) {
			invoker = new HomeAskSiteInvoker((AskSite) cmd);
		}
		// 判断站点存在
		else if (cmd.getClass() == AssertSite.class) {
			invoker = new HomeAssertSiteInvoker((AssertSite) cmd);
		}
		// 设置缓存映像数据
		else if (cmd.getClass() == SetCacheReflexStub.class) {
			invoker = new HomeSetCacheReflexStubInvoker((SetCacheReflexStub) cmd);
		}

		// 查找DATA/WORK/BUILD站点的元数据
		else if (cmd.getClass() == FindDataField.class) {
			invoker = new HomeFindDataFieldInvoker((FindDataField) cmd);
		} else if (cmd.getClass() == FindWorkField.class) {
			invoker = new HomeFindWorkFieldInvoker((FindWorkField) cmd);
		} else if (cmd.getClass() == FindBuildField.class) {
			invoker = new HomeFindBuildFieldInvoker((FindBuildField) cmd);
		}
		// 获取ACCOUNT站点地址
		else if (cmd.getClass() == TakeSigerSite.class) {
			invoker = new HomeTakeSigerSiteInvoker((TakeSigerSite) cmd);
		}
		// 增加/删除ARCHIVE站点（由TOP发送过来）
		else if (cmd.getClass() == PushAccountSite.class) {
			invoker = new HomePushArchiveSiteInvoker((PushAccountSite) cmd);
		} else if (cmd.getClass() == DropAccountSite.class) {
			invoker = new HomeDropAccountSiteInvoker((DropAccountSite) cmd);
		}
		// 获得关联的CALL站点记录
		else if(cmd.getClass() == TakeCallItem.class){
			invoker = new HomeTakeCallItemInvoker((TakeCallItem)cmd);
		}
		// 处理密钥令牌
		else if (cmd.getClass() == CreateSecureToken.class) {
			invoker = new HomeCreateSecureTokenInvoker((CreateSecureToken) cmd);
		} else if (cmd.getClass() == DropSecureToken.class) {
			invoker = new HomeDropSecureTokenInvoker((DropSecureToken) cmd);
		} else if (cmd.getClass() == FlushSecureToken.class) {
			invoker = new HomeFlushSecureTokenInvoker((FlushSecureToken) cmd);
		} else if (cmd.getClass() == ShowSecureToken.class) {
			invoker = new HomeShowSecureTokenInvoker((ShowSecureToken) cmd);
		} else if (cmd.getClass() == SetSecureSize.class) {
			invoker = new HomeSetSecureSizeInvoker((SetSecureSize) cmd);
		}
		// 重装加载链接库、安全策略、许可证、追踪命令
		else if (cmd.getClass() == ReloadLibrary.class) {
			invoker = new HomeReloadLibraryInvoker((ReloadLibrary) cmd);
		} else if (cmd.getClass() == ReloadSecurityPolicy.class) {
			invoker = new HomeReloadSecurityPolicyInvoker((ReloadSecurityPolicy) cmd);
		} else if (cmd.getClass() == ReloadLicence.class) {
			invoker = new HomeReloadLicenceInvoker((ReloadLicence) cmd);
		} else if (cmd.getClass() == MailLicence.class) {
			invoker = new HomeMailLicenceInvoker((MailLicence) cmd);
		} else if (cmd.getClass() == ScanCommandStack.class) {
			invoker = new HomeScanCommandStackInvoker((ScanCommandStack) cmd);
		} else if (cmd.getClass() == ReleaseMemory.class) {
			invoker = new HomeReleaseMemoryInvoker((ReleaseMemory) cmd);
		} else if (cmd.getClass() == ReleaseMemoryInterval.class) {
			invoker = new HomeReleaseMemoryIntervalInvoker((ReleaseMemoryInterval) cmd);
		}
		// 应答数据命令
		else if (cmd.getClass() == ReplyPacketMode.class) {
			invoker = new HomeReplyPacketModeInvoker((ReplyPacketMode) cmd);
		} else if (cmd.getClass() == ReplyPacketSize.class) {
			invoker = new HomeReplyPacketSizeInvoker((ReplyPacketSize) cmd);
		} else if (cmd.getClass() == ReplySendTimeout.class) {
			invoker = new HomeReplySendTimeoutInvoker((ReplySendTimeout) cmd);
		} else if (cmd.getClass() == ReplyReceiveTimeout.class) {
			invoker = new HomeReplyReceiveTimeoutInvoker((ReplyReceiveTimeout) cmd);
		} else if(cmd.getClass() == ReplyFlowControl.class) {
			invoker = new HomeReplyFlowControlInvoker((ReplyFlowControl)cmd);
		}
		// 远程关闭命令
		else if (cmd.getClass() == Shutdown.class) {
			invoker = new HomeShutdownInvoker((Shutdown) cmd);
		}
		
		// 搜索分布任务组件和码位计算器的分布站点地址
		else if (cmd.getClass() == SeekTask.class) {
			invoker = new HomeSeekTaskInvoker((SeekTask) cmd);
		} 
//		else if (cmd.getClass() == SeekScaler.class) {
//			invoker = new HomeSeekScalerInvoker((SeekScaler) cmd);
//		}

		// 查询授权单元
		else if (cmd.getClass() == SeekActiveItem.class) {
			invoker = new HomeSeekActiveItemInvoker((SeekActiveItem) cmd);
		}
		// 修改日志模式
		else if (cmd.getClass() == SetLogLevel.class) {
			invoker = new HomeSetLogLevelInvoker((SetLogLevel) cmd);
		}
		
		// 密文超时
		else if (cmd.getClass() == CipherTimeout.class) {
			invoker = new HomeCipherTimeoutInvoker((CipherTimeout) cmd);
		}
		// 分布处理超时
		else if (cmd.getClass() == DistributedTimeout.class) {
			invoker = new HomeDistributedTimeoutInvoker((DistributedTimeout) cmd);
		}
		// 最大缓存空间
		else if (cmd.getClass() == MaxEchoBuffer.class) {
			invoker = new HomeMaxEchoBufferInvoker((MaxEchoBuffer) cmd);
		} else if (cmd.getClass() == MaxInvoker.class) {
			invoker = new HomeMaxInvokerInvoker((MaxInvoker) cmd);
		}
		// 检测系统信息
		else if (cmd.getClass() == CheckSystemInfo.class) {
			invoker = new HomeCheckSystemInfoInvoker((CheckSystemInfo) cmd);
		}
		// 分析用户资源
		else if (cmd.getClass() == ScanSchema.class) {
			invoker = new HomeScanSchemaInvoker((ScanSchema) cmd);
		} else if (cmd.getClass() == ScanTable.class) {
			invoker = new HomeScanTableInvoker((ScanTable) cmd);
		}
		// 强制重新加载和发布自动JAR包
		else if (cmd.getClass() == ReloadCustom.class) {
			invoker = new HomeReloadCustomInvoker((ReloadCustom) cmd);
		}
		// 注册站点
		else if (cmd.getClass() == LoginSite.class) {
			invoker = new HomeLoginSiteInvoker((LoginSite) cmd);
		}
		// 注册到TOP站点
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// 刷新发布的组件（来自AID/WATCH站点）
		else if (cmd.getClass() == RefreshPublish.class) {
			invoker = new HomeRefreshPublishInvoker((RefreshPublish) cmd);
		}
		// 检索用户日志
		else if (cmd.getClass() == ScanUserLog.class) {
			invoker = new HomeScanUserLogInvoker((ScanUserLog) cmd);
		}
		// 当前站点状态
		else if (cmd.getClass() == SeekSiteRuntime.class) {
			invoker = new HomeSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
		}
		// 检索在线命令
		else if (cmd.getClass() == SeekOnlineCommand.class) {
			invoker = new CommonSeekOnlineCommandInvoker((SeekOnlineCommand) cmd);
		} else if (cmd.getClass() == SeekOnlineResource.class) {
			invoker = new HomeSeekOnlineResourceInvoker((SeekOnlineResource) cmd);
		}
		// DATA站点查找与数据表关联的CALL站点
		else if(cmd.getClass() == FindTableCallSite.class) {
			invoker = new HomeFindTableCallSiteInvoker((FindTableCallSite)cmd);
		}
		// 检索用户注册元数据
		else if (cmd.getClass() == SeekRegisterMetadata.class) {
			invoker = new HomeSeekRegisterMetadataInvoker((SeekRegisterMetadata) cmd);
		}
		// 检索用户分布区域
		else if (cmd.getClass() == SeekUserArea.class) {
			invoker = new HomeSeekUserAreaInvoker((SeekUserArea) cmd);
		}
		// 扫描用户关联的时间间隔
		else if (cmd.getClass() == ScanLinkTime.class) {
			invoker = new HomeScanLinkTimeInvoker((ScanLinkTime) cmd);
		}
		// 转发站点不足命令
		else if (cmd.getClass() == ShiftSiteMissing.class) {
			invoker = new HomeShiftSiteMissingInvoker((ShiftSiteMissing) cmd);
		}
		// 网络流量测试
		else if (cmd.getClass() == Gust.class) {
			invoker = new CommonClientGustInvoker((Gust) cmd);
		} else if (cmd.getClass() == Swarm.class) {
			invoker = new CommonServerSwarmInvoker((Swarm) cmd);
		}
		// 发布用户到指定站点 / 从指定站点清除用户
		else if (cmd.getClass() == DeployUser.class) {
			invoker = new HomeDeployUserInvoker((DeployUser) cmd);
		} else if (cmd.getClass() == EraseUser.class) {
			invoker = new HomeEraseUserInvoker((EraseUser) cmd);
		}
		// 发布数据表
		else if (cmd.getClass() == DeployTable.class) {
			invoker = new HomeDeployTableInvoker((DeployTable) cmd);
		}
		// 内存/磁盘空间不足报警，HOME转发给WATCH节点
		else if (cmd.getClass() == VMMemoryMissing.class) {
			invoker = new HomeVMMemoryMissingInvoker((VMMemoryMissing) cmd);
		} else if (cmd.getClass() == MemoryMissing.class) {
			invoker = new HomeMemoryMissingInvoker((MemoryMissing) cmd);
		} else if (cmd.getClass() == DiskMissing.class) {
			invoker = new HomeDiskMissingInvoker((DiskMissing) cmd);
		} else if (cmd.getClass() == MemberMissing.class) {
			invoker = new HomeMemberMissingInvoker((MemberMissing) cmd);
		} else if (cmd.getClass() == MemberFull.class) {
			invoker = new HomeMemberFullInvoker((MemberFull) cmd);
		} else if (cmd.getClass() == FrontMissing.class) {
			invoker = new HomeFrontMissingInvoker((FrontMissing) cmd);
		} else if (cmd.getClass() == FrontFull.class) {
			invoker = new HomeFrontFullInvoker((FrontFull) cmd);
		} else if(cmd.getClass() == LicenceTimeout.class) {
			invoker = new HomeLicenceTimeoutInvoker((LicenceTimeout)cmd);
		}
		// DATA主节点复制数据给从节点
		else if (cmd.getClass() == CopyMasterMass.class) {
			invoker = new HomeCopyMasterMassInvoker((CopyMasterMass) cmd);
		}
		// 接收来自DATA站点的故障表
		else if (cmd.getClass() == SubmitFaultTable.class) {
			invoker = new HomeSubmitFaultTableInvoker((SubmitFaultTable) cmd);
		}
		// CPU/内存/磁盘空间限制
		else if (cmd.getClass() == MostCPU.class) {
			invoker = new HomeMostCPUInvoker((MostCPU) cmd);
		} else if(cmd.getClass() == MostVMMemory.class) {
			invoker = new HomeMostVMMemoryInvoker((MostVMMemory)cmd);
		} else if (cmd.getClass() == LeastMemory.class) {
			invoker = new HomeLeastMemoryInvoker((LeastMemory) cmd);
		} else if (cmd.getClass() == LeastDisk.class) {
			invoker = new HomeLeastDiskInvoker((LeastDisk) cmd);
		} else if (cmd.getClass() == CheckSitePath.class) {
			invoker = new HomeCheckSitePathInvoker((CheckSitePath) cmd);
		} else if (cmd.getClass() == ReflectPort.class) {
			invoker = new HomeReflectPortInvoker((ReflectPort) cmd);
		}
		// 集群用户的虚拟空间
		else if (cmd.getClass() == SetMemberCyber.class) {
			invoker = new HomeSetMemberCyberInvoker((SetMemberCyber) cmd);
		} else if (cmd.getClass() == SetFrontCyber.class) {
			invoker = new HomeSetFrontCyberInvoker((SetFrontCyber) cmd);
		} else if (cmd.getClass() == CheckUserCyber.class) {
			invoker = new HomeCheckUserCyberInvoker((CheckUserCyber) cmd);
		}
		// WATCH节点检索HOME集群注册成员
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new HomeAskClusterMemberInvoker((AskClusterMember) cmd);
		} else if (cmd.getClass() == PushRegisterMember.class) {
			invoker = new HomePushRegisterMemberInvoker((PushRegisterMember) cmd);
		} else if(cmd.getClass() == DropRegisterMember.class) {
			invoker = new HomeDropRegisterMemberInvoker((DropRegisterMember)cmd);
		} else if (cmd.getClass() == PushOnlineMember.class) {
			invoker = new HomePushOnlineMemberInvoker((PushOnlineMember) cmd);
		} else if (cmd.getClass() == DropOnlineMember.class) {
			invoker = new HomeDropOnlineMemberInvoker((DropOnlineMember) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new HomeOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new HomeCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 获得工作节点地址
		else if (cmd.getClass() == TakeJobSite.class) {
			invoker = new HomeTakeJobSiteInvoker((TakeJobSite) cmd);
		}
		// 显示作业节点
		else if (cmd.getClass() == CheckJobSite.class) {
			invoker = new HomeCheckJobSiteInvoker((CheckJobSite) cmd);
		}
		else if(cmd.getClass() == CheckUserCost.class) {
			invoker = new HomeCheckUserCostInvoker((CheckUserCost)cmd);
		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 没有匹配，转发一个不支持应答；否则转发给调用器管理池处理
		boolean success = (invoker != null);
		if (success) {
			success = HomeInvokerPool.getInstance().launch(invoker);
		} else {
			super.unsupport(cmd);
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

		if (cmd.getClass() == ShiftTakeTable.class) {
			invoker = new CommonShiftTakeTableInvoker((ShiftTakeTable) cmd);
		}
		// 获得ACCOUNT站点地址
		else if (cmd.getClass() == ShiftTakeSigerSite.class) {
			invoker = new CommonShiftTakeSigerSiteInvoker((ShiftTakeSigerSite) cmd);
		}
		// 注册到TOP站点
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// 去ACCOUNT站点获取账号/资源引用
		else if (cmd.getClass() == ShiftTakeAccount.class) {
			invoker = new HomeShiftTakeAccountInvoker((ShiftTakeAccount) cmd);
		} else if (cmd.getClass() == ShiftTakeRefer.class) {
			invoker = new HomeShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}

		// 判断是自定义命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		boolean success = (invoker != null);
		if (success) {
			success = HomeInvokerPool.getInstance().launch(invoker);
		}

		return success;
	}

}