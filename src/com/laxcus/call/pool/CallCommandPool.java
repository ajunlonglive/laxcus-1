/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import com.laxcus.access.diagram.*;
import com.laxcus.call.*;
import com.laxcus.call.invoker.*;
import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.account.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.cloud.store.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.contact.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.establish.*;
import com.laxcus.command.field.*;
import com.laxcus.command.halt.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.login.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.relate.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.scan.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.Shutdown;
import com.laxcus.command.site.*;
import com.laxcus.command.site.front.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.stub.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.command.stub.reflex.*;
import com.laxcus.command.stub.site.*;
import com.laxcus.command.task.*;
import com.laxcus.command.traffic.*;
import com.laxcus.command.zone.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * CALL站点异步命令管理池。<br><br>
 * 
 * 如果是FRONT站点发出的命令，必须防止它操作集群内部命令。
 * 
 * @author scott.liang
 * @version 1.3 9/23/2014
 * @since laxcus 1.0
 */
public final class CallCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static CallCommandPool selfHandle = new CallCommandPool();

	/**
	 * 初始化CALL异步命令管理池
	 */
	private CallCommandPool() {
		super();
	}

	/**
	 * 返回CALL异步命令管理池的静态句柄
	 * @return CALL管理池句柄
	 */
	public static CallCommandPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("CallCommandPool.getInstance");
		// 返回句柄
		return CallCommandPool.selfHandle;
	}

	/** FRONT命令 **/
	private Class<?>[] FRONT_COMMANDS = new Class<?>[] { Conduct.class,
			Establish.class, Contact.class, Regulate.class, InsertGuide.class, Select.class,
			Delete.class, Update.class, LoadIndex.class, StopIndex.class,
			LoadEntity.class, StopEntity.class, ScanTable.class,
			CheckEntityConsistency.class, RecoverEntityConsistency.class,
			ScanSketch.class, ScanEntity.class, GitStubs.class, PrintStubsDiagram.class,
			SingleExportEntity.class, CopyEntity.class, SeekCloudWare.class,
			CreateCloudDirectory.class, DropCloudDirectory.class, DropCloudFile.class, 
			RenameCloudDirectory.class, RenameCloudFile.class, UploadCloudFileGuide.class,
			DownloadCloudFileGuide.class, ScanCloudDirectory.class, CheckMassiveMimo.class, CheckJobSite.class };

	/**
	 * 判断是允许的FRONT节点命令
	 * @param cmd 命令类型
	 * @return 返回真或者假
	 */
	private boolean isFrontCommand(Command cmd) {
		// 判断是内部命令
		for (int i = 0; i < FRONT_COMMANDS.length; i++) {
			if (cmd.getClass() == FRONT_COMMANDS[i]) {
				return true;
			}
		}
		return false;
	}

//			/** 内部默认使用命令 **/
//			private Class<?>[] INTRA_COMMANDS = new Class<?>[] { CheckSwarm.class, Swarm.class, SwitchHub.class,
//					SeekSiteRunetime.class, SeekOnlineCommand.class, SeekOnlineResource.class, ProcessField.class,
//					FindStubSlaveSite.class, Shutdown.class, PressRegulate.class, PushDataField.class, PushBuildField.class,
//					PushWorkField.class};
	
	/** 内部默认使用命令 **/
	private Class<?>[] INTRA_COMMANDS = new Class<?>[] {
			MailTaskAssistComponent.class, MailTaskLibraryComponent.class,
			DropTaskApplication.class,
//			MailScalerAssistComponent.class, MailScalerLibraryComponent.class, 
			
			Swarm.class, Gust.class, SwitchHub.class, SeekSiteRuntime.class,
			SeekOnlineCommand.class, SeekOnlineResource.class, ProcessField.class,
			FindStubSlaveSite.class, Shutdown.class, PressRegulate.class, 
			// DATA/BUILD/WORK推送记录
			PushDataField.class, PushBuildField.class, PushWorkField.class};

	/**
	 * 判断是规定的内部命令
	 * @param cmd 命令实例
	 * @return 返回真或者假
	 */
	private boolean isIntraCommand(Command cmd) {
		// 判断是内部命令
		for (int i = 0; i < INTRA_COMMANDS.length; i++) {
			if (cmd.getClass() == INTRA_COMMANDS[i]) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#accept(com.laxcus.command.Command)
	 */
	@Override
	public boolean accept(Command cmd) {
		// 命令无效或者没有回显地址不受理
		Cabin cabin = (cmd != null ? cmd.getSource() : null);
		if (cabin == null) {
			return false;
		}
		Node node = cabin.getNode();

		// 判断命令是来自HOME站点
		boolean success = CallLauncher.getInstance().isHub(node);
		// 来自关联的HOME站点
		if (!success) {
			success = HomeOnCallPool.getInstance().contains(node);
		}
		// 判断来自FRONT站点
		if (!success) {
			success = FrontOnCallPool.getInstance().contains(node);
			// 如果命令来自FRONT站点，QUICK标识必须是假
			if (success) {
				// 如果不是许可的FRONT命令，拒绝它！
				if(!isFrontCommand(cmd)) {
					Logger.error(this, "accept", "refuse! <%s>", cmd.getClass().getSimpleName());
					return false;
				}
				
				// 调整权级，如果没有引用使用最小权级
				Siger issuer = cmd.getIssuer();
				Refer refer = StaffOnCallPool.getInstance().findRefer(issuer);
				if (refer != null) {
					byte priority = (byte) refer.getUser().getPriority();
					if (CommandPriority.isUserPriority(priority)) {
						cmd.setPriority(priority);
					} else {
						cmd.setPriority(CommandPriority.NONE);
					}
				} else {
					cmd.setPriority(CommandPriority.NONE);
				}
				
				cmd.setTigger(true); // 必须允许！
			}
		}
		// 来自关联的DATA站点
		if (!success) {
			success = DataOnCallPool.getInstance().contains(node);
		}
		// 来自关联的WORK站点
		if (!success) {
			success = WorkOnCallPool.getInstance().contains(node);
		}
		// 来自关联的BUILD站点
		if (!success) {
			success = BuildOnCallPool.getInstance().contains(node);
		}

		// 判断是规定的内部命令，非注册站点跨集群的操作
		if (!success) {
			success = isIntraCommand(cmd);
		}

		// 保存命令
		if (success) {
			success = add(cmd);
		}

		// 保存命令
		if (success) {
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

		// 分布计算/数据构建/SQL操作
		if (cmd.getClass() == Conduct.class) {
			invoker = new CallConductInvoker((Conduct) cmd);
		} else if (cmd.getClass() == Establish.class) {
			invoker = new CallEstablishInvoker((Establish) cmd);
		} else if (cmd.getClass() == InsertGuide.class) {
			invoker = new CallInsertInvoker((InsertGuide) cmd);
		} else if (cmd.getClass() == Select.class) {
			invoker = new CallDirectSelectInvoker((Select) cmd);
		} else if (cmd.getClass() == Delete.class) {
			invoker = new CallDirectDeleteInvoker((Delete) cmd);
		} else if (cmd.getClass() == Update.class) {
			invoker = new CallDirectUpdateInvoker((Update) cmd);
		}
		// 数据优化命令，通过CALL站点作用到DATA主站点
		else if (cmd.getClass() == Regulate.class) {
			invoker = new CallRegulateInvoker((Regulate) cmd);
		}
		// 启动数据优化命令，来自Gate站点
		else if (cmd.getClass() == PressRegulate.class) {
			invoker = new CallPressRegulateInvoker((PressRegulate) cmd);
		}
		// 查询数据块编号从站点，这个命令操作与Regulate关联，发生在DATA主站点，并由它发出
		else if (cmd.getClass() == SeekSlaveStubSite.class) {
			invoker = new CallSeekSlaveStubSiteInvoker((SeekSlaveStubSite) cmd);
		}
		// 加载/卸载命令
		else if (cmd.getClass() == LoadIndex.class) {
			invoker = new CallLoadIndexInvoker((LoadIndex) cmd);
		} else if (cmd.getClass() == StopIndex.class) {
			invoker = new CallStopIndexInvoker((StopIndex) cmd);
		} else if (cmd.getClass() == LoadEntity.class) {
			invoker = new CallLoadEntityInvoker((LoadEntity) cmd);
		} else if (cmd.getClass() == StopEntity.class) {
			invoker = new CallStopEntityInvoker((StopEntity) cmd);
		}
		// 分析数据表资源命令
		else if (cmd.getClass() == ScanTable.class) {
			invoker = new CallScanTableInvoker((ScanTable) cmd);
		}
		// 检查/恢复表数据一致性
		else if (cmd.getClass() == CheckEntityConsistency.class) {
			invoker = new CallCheckEntityConsistencyInvoker((CheckEntityConsistency) cmd);
		} else if (cmd.getClass() == RecoverEntityConsistency.class) {
			invoker = new CallRecoverEntityConsistencyInvoker((RecoverEntityConsistency) cmd);
		}
		// 检查表分布数据容量
		else if (cmd.getClass() == ScanSketch.class) {
			invoker = new CallScanSketchInvoker((ScanSketch) cmd);
		}
		// 扫描数据块
		else if (cmd.getClass() == ScanEntity.class) {
			invoker = new CallScanEntityInvoker((ScanEntity) cmd);
		} 
		// 查找数据块编号命令
		else if (cmd.getClass() == GitStubs.class) {
			invoker = new CallGitStubsInvoker((GitStubs) cmd);
		} else if (cmd.getClass() == PrintStubsDiagram.class) {
			invoker = new CallPrintStubsDiagramInvoker((PrintStubsDiagram) cmd);
		} else if (cmd.getClass() == SingleExportEntity.class) {
			invoker = new CallSingleExportEntityInvoker((SingleExportEntity) cmd);
		}
		// 复制数据块
		else if (cmd.getClass() == CopyEntity.class) {
			invoker = new CallCopyEntityInvoker((CopyEntity) cmd);
		}
		// 获取分布任务组件
		else if (cmd.getClass() == TakeTaskTag.class) {
			invoker = new CallTakeTaskTagInvoker((TakeTaskTag) cmd);
		}
//		// 获取码位计算器(使用一个公共调用器)
//		else if (cmd.getClass() == TakeScalerTag.class) {
//			invoker = new CommonTakeScalerTagInvoker((TakeScalerTag) cmd);
//		}
		// 增加/删除ACCOUNT站点（HOME站点发送）
		else if (cmd.getClass() == PushAccountSite.class) {
			invoker = new CallPushArchiveSiteInvoker((PushAccountSite) cmd);
		} else if (cmd.getClass() == DropAccountSite.class) {
			invoker = new CommonDropAccountSiteInvoker((DropAccountSite) cmd);
		}

		// HOME站点授权“建立/删除”CALL站点的用户资源引用
		else if (cmd.getClass() == AwardCreateRefer.class) {
			invoker = new CallAwardCreateReferInvoker((AwardCreateRefer) cmd);
		} else if (cmd.getClass() == AwardDropRefer.class) {
			invoker = new CallAwardDropReferInvoker((AwardDropRefer) cmd);
		}
		// 授权删除数据库
		else if (cmd.getClass() == AwardDropSchema.class) {
			invoker = new CallAwardDropSchemaInvoker((AwardDropSchema) cmd);
		}
		// HOME站点授权“建表/删表”
		else if (cmd.getClass() == AwardCreateTable.class) {
			invoker = new CallAwardCreateTableInvoker((AwardCreateTable) cmd);
		} else if (cmd.getClass() == AwardDropTable.class) {
			invoker = new CallAwardDropTableInvoker((AwardDropTable) cmd);
		}
		// HOME授权修改注册用户账号密码
		else if (cmd.getClass() == AwardAlterUser.class) {
			invoker = new CallAwardAlterUserInvoker((AwardAlterUser) cmd);
		}
		// 切换注册站点
		else if (cmd.getClass() == SwitchHub.class) {
			invoker = new CommonSwitchHubInvoker((SwitchHub) cmd);
		}
		// 设置用户资源引用命令
		else if (cmd.getClass() == SetRefer.class) {
			invoker = new CallSetReferInvoker((SetRefer) cmd);
		}
		// 元数据授权命令
		else if (cmd.getClass() == PushDataField.class) {
			invoker = new CallPushDataFieldInvoker((PushDataField) cmd);
		} else if (cmd.getClass() == PushWorkField.class) {
			invoker = new CallPushWorkFieldInvoker((PushWorkField) cmd);
		} else if (cmd.getClass() == PushBuildField.class) {
			invoker = new CallPushBuildFieldInvoker((PushBuildField) cmd);
		}
		// 撤销授权命令
		else if (cmd.getClass() == DropDataField.class) {
			invoker = new CallDropDataFieldInvoker((DropDataField) cmd);
		} else if (cmd.getClass() == DropBuildField.class) {
			invoker = new CallDropBuildFieldInvoker((DropBuildField) cmd);
		} else if (cmd.getClass() == DropWorkField.class) {
			invoker = new CallDropWorkFieldInvoker((DropWorkField) cmd);
		}
		// 查找本站点参数命令
		else if (cmd.getClass() == FindDataField.class) {
			invoker = new CallFindDataFieldInvoker((FindDataField) cmd);
		} else if (cmd.getClass() == FindWorkField.class) {
			invoker = new CallFindWorkFieldInvoker((FindWorkField) cmd);
		} else if (cmd.getClass() == FindBuildField.class) {
			invoker = new CallFindBuildFieldInvoker((FindBuildField) cmd);
		}
		// 保存缓存映像块编号
		else if (cmd.getClass() == SetCacheReflexStub.class) {
			invoker = new CallSetCacheReflexStubInvoker((SetCacheReflexStub) cmd);
		}
		// 查询缓存映像块/存储映像块（CACHE/CHUNK）站点
		else if (cmd.getClass() == FindCacheReflexStubSite.class) {
			invoker = new CallFindCacheReflexStubSiteInvoker((FindCacheReflexStubSite) cmd);
		} else if (cmd.getClass() == FindChunkReflexStubSite.class) {
			invoker = new CallFindChunkReflexStubSiteInvoker((FindChunkReflexStubSite) cmd);
		}

		// 查询关联的HOME站点
		else if (cmd.getClass() == FindRelateHome.class) {
			invoker = new CallFindRelateHomeInvoker((FindRelateHome) cmd);
		}
		// 查询数据块编号关联从站点
		else if (cmd.getClass() == FindStubSlaveSite.class) {
			invoker = new CallFindStubSlaveSiteInvoker((FindStubSlaveSite) cmd);
		}
		// 根据表名，查询关联的DATA主站点
		else if (cmd.getClass() == FindSpacePrimeSite.class) {
			invoker = new CallFindSpacePrimeSiteInvoker((FindSpacePrimeSite) cmd);
		}
		// 对每一个数据块编号选择一个关联站点（不区分主从，只顺序选择一个）
		else if (cmd.getClass() == ChoiceStubSite.class) {
			invoker = new CallChoiceStubSiteInvoker((ChoiceStubSite) cmd);
		} 
		// 只查询数据块的主站点
		else if (cmd.getClass() == FindStubPrimeSite.class) {
			invoker = new CallFindStubPrimeSiteInvoker((FindStubPrimeSite) cmd);
		}
		// 查询索引分区命令
		else if (cmd.getClass() == ShiftFindIndexZone.class) {
			invoker = new CallShiftFindIndexZoneInvoker((ShiftFindIndexZone) cmd);
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
			invoker = new CallSeekTaskInvoker((SeekTask) cmd);
		}
		// 检索登录在CALL节点上的FRONT注册用户
		else if (cmd.getClass() == SeekFrontUser.class) {
			invoker = new CallSeekFrontUserInvoker((SeekFrontUser) cmd);
		} else if (cmd.getClass() == SeekFrontSite.class) {
			invoker = new CallSeekFrontSiteInvoker((SeekFrontSite) cmd);
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
		// 设置站点日志等级
		else if (cmd.getClass() == SetLogLevel.class) {
			invoker = new SubSetLogLevelInvoker((SetLogLevel) cmd);
		}
		// 强制重新加载和发布自动JAR包
		else if (cmd.getClass() == ReloadCustom.class) {
			invoker = new CommonReloadCustomInvoker((ReloadCustom) cmd);
		}
		// 注册到HOME站点
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// 刷新组件
		else if (cmd.getClass() == RefreshPublish.class) {
			invoker = new CallRefreshPublishInvoker((RefreshPublish) cmd);
		}
		// 运行快捷组件
//		else if (cmd.getClass() == RunSwift.class) {
//			invoker = new CallRunSwiftInvoker((RunSwift) cmd);
//		} 
		else if (cmd.getClass() == Contact.class) {
			invoker = new CallContactInvoker((Contact) cmd);
		}
		// 检索用户日志
		else if (cmd.getClass() == ScanUserLog.class) {
			invoker = new CallScanUserLogInvoker((ScanUserLog) cmd);
		}
		// 当前站点状态
		else if (cmd.getClass() == SeekSiteRuntime.class) {
			invoker = new CallSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
		}
		// 检索在线命令/在线资源
		else if (cmd.getClass() == SeekOnlineCommand.class) {
			invoker = new CommonSeekOnlineCommandInvoker((SeekOnlineCommand) cmd);
		} else if (cmd.getClass() == SeekOnlineResource.class) {
			invoker = new CallSeekOnlineResourceInvoker((SeekOnlineResource) cmd);
		}
		// 检索用户在线注册元数据
		else if (cmd.getClass() == SeekRegisterMetadata.class) {
			invoker = new CallSeekRegisterMetadataInvoker((SeekRegisterMetadata) cmd);
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
		// 部署数据表
		else if (cmd.getClass() == DeployTable.class) {
			invoker = new CallDeployTableInvoker((DeployTable) cmd);
		}
		// 加载资源引用
		else if (cmd.getClass() == ShiftTakeRefer.class) {
			invoker = new CallShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}
		// 发布组件应用附件包 / 动态链接库
		else if (cmd.getClass() == MailTaskAssistComponent.class) {
			invoker = new CallMailTaskAssistComponentInvoker((MailTaskAssistComponent) cmd);
		} 
		else if (cmd.getClass() == MailTaskLibraryComponent.class) {
			invoker = new CallMailTaskLibraryComponentInvoker((MailTaskLibraryComponent) cmd);
		} 
		// 删除云应用
		else if (cmd.getClass() == DropTaskApplication.class) {
			invoker = new CallDropTaskApplicationInvoker((DropTaskApplication) cmd);
		}
		

//		else if (cmd.getClass() == MailRemoteTaskAssistComponent.class) {
//			invoker = new CallMailRemoteTaskAssistComponentInvoker((MailRemoteTaskAssistComponent) cmd);
//		} 
//		else if (cmd.getClass() == MailRemoteTaskLibraryComponent.class) {
//			invoker = new CallMailRemoteTaskLibraryComponentInvoker((MailRemoteTaskLibraryComponent) cmd);
//		}
//		// 发布码位计算器附件 / 动态链接库
//		else if(cmd.getClass() == MailScalerAssistComponent.class) {
//			invoker = new CallMailScalerAssistComponentInvoker((MailScalerAssistComponent)cmd);
//		} else if(cmd.getClass() == MailScalerLibraryComponent.class) {
//			invoker = new CallMailScalerLibraryComponentInvoker((MailScalerLibraryComponent)cmd);
//		}
//		// 发布快捷组件附件 / 动态链接库
//		else if (cmd.getClass() == MailSwiftAssistComponent.class) {
//			invoker = new CallMailSwiftAssistComponentInvoker((MailSwiftAssistComponent) cmd);
//		} else if (cmd.getClass() == MailSwiftLibraryComponent.class) {
//			invoker = new CallMailSwiftLibraryComponentInvoker((MailSwiftLibraryComponent) cmd);
//		}
		// 设置虚拟空间参数
		else if (cmd.getClass() == SetMemberCyber.class) {
			invoker = new CallSetMemberCyberInvoker((SetMemberCyber) cmd);
		} else if (cmd.getClass() == SetFrontCyber.class) {
			invoker = new CallSetFrontCyberInvoker((SetFrontCyber) cmd);
		} else if (cmd.getClass() == CheckUserCyber.class) {
			invoker = new CallCheckUserCyberInvoker((CheckUserCyber) cmd);
		}
		// CALL节点检索集群成员
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new CallAskClusterMemberInvoker((AskClusterMember) cmd);
		} 
		// 推送/删除在线用户，推送注册用户给WATCH节点（通过HOME节点转发）
		else if (cmd.getClass() == ShiftPushOnlineMember.class) {
			invoker = new CallShiftPushOnlineMemberInvoker((ShiftPushOnlineMember) cmd);
		} else if (cmd.getClass() == ShiftDropOnlineMember.class) {
			invoker = new CallShiftDropOnlineMemberInvoker((ShiftDropOnlineMember) cmd);
		} else if (cmd.getClass() == ShiftPushRegisterMember.class) {
			invoker = new CallShiftPushRegisterMemberInvoker((ShiftPushRegisterMember) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new SubOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new SubCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 检索云端应用
		else if (cmd.getClass() == SeekCloudWare.class) {
			invoker = new CallSeekCloudWareInvoker((SeekCloudWare) cmd);
		}
		// 云命令
		else if (cmd.getClass() == CreateCloudDirectory.class) {
			invoker = new CallCreateCloudDirectoryInvoker((CreateCloudDirectory) cmd);
		} else if (cmd.getClass() == DropCloudDirectory.class) {
			invoker = new CallDropCloudDirectoryInvoker((DropCloudDirectory) cmd);
		} else if(cmd.getClass() == DropCloudFile.class) {
			invoker = new CallDropCloudFileInvoker((DropCloudFile) cmd);
		} else if (cmd.getClass() == UploadCloudFileGuide.class) {
			invoker = new CallUploadCloudFileInvoker((UploadCloudFileGuide) cmd);
		} else if(cmd.getClass() == DownloadCloudFileGuide.class) {
			invoker = new CallDownloadCloudFileInvoker((DownloadCloudFileGuide)cmd);
		} else if (cmd.getClass() == ScanCloudDirectory.class) {
			invoker = new CallScanCloudDirectoryInvoker((ScanCloudDirectory) cmd);
		} else if (cmd.getClass() == RenameCloudFile.class) {
			invoker = new CallRenameCloudFileInvoker((RenameCloudFile) cmd);
		} else if (cmd.getClass() == RenameCloudDirectory.class) {
			invoker = new CallRenameCloudDirectoryInvoker((RenameCloudDirectory) cmd);
		}
		// 检测MASSIVE MIMO
		else if (cmd.getClass() == CheckMassiveMimo.class) {
			invoker = new CallCheckMassiveMimoInvoker((CheckMassiveMimo) cmd);
		} else if (cmd.getClass() == CheckJobSite.class) {
			invoker = new CallCheckJobSiteInvoker((CheckJobSite) cmd);
		}

		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 判断支持
		boolean success = (invoker != null);
		if (success) {
			success = CallInvokerPool.getInstance().launch(invoker);
		} else {
			unsupport(cmd);
		}

		Logger.debug(this, "dispatch", success, "%s", cmd);

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
			invoker = new CallShiftRequestReferInvoker((ShiftRequestRefer) cmd);
		} else if (cmd.getClass() == ShiftTakeTable.class) {
			invoker = new CommonShiftTakeTableInvoker((ShiftTakeTable) cmd);
		} else if (cmd.getClass() == ShiftFindIndexZone.class) {
			invoker = new CallShiftFindIndexZoneInvoker((ShiftFindIndexZone) cmd);
		} else if (cmd.getClass() == ShiftHalt.class) {
			invoker = new CommonShiftHaltInvoker((ShiftHalt) cmd);
		}
		// 获取分布任务组件
		else if (cmd.getClass() == ShiftTakeTaskComponent.class) {
			invoker = new CallShiftTakeTaskComponentInvoker((ShiftTakeTaskComponent) cmd);
		} 
//		// 获取码位计算器组件
//		else if (cmd.getClass() == ShiftTakeScalerComponent.class) {
//			invoker = new CommonShiftTakeScalerComponentInvoker((ShiftTakeScalerComponent) cmd);
//		}
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
			invoker = new CallShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}
		
//		// 转发分布任务组件的应用附件/动态链接库
//		else if (cmd.getClass() == ShiftPublishSingleTaskAssistComponent.class) {
//			invoker = new CallShiftPublishSingleTaskAssistComponentInvoker((ShiftPublishSingleTaskAssistComponent) cmd);
//		} else if (cmd.getClass() == ShiftPublishSingleTaskLibraryComponent.class) {
//			invoker = new CallShiftPublishSingleTaskLibraryComponentInvoker((ShiftPublishSingleTaskLibraryComponent) cmd);
//		}
//		// 转发码位计算器的应用附件
//		else if(cmd.getClass() == ShiftPublishSingleScalerAssistComponent.class) {
//			invoker = new CallShiftPublishSingleScalerAssistComponentInvoker((ShiftPublishSingleScalerAssistComponent) cmd);
//		} 
//		// 转发码位计算器的动态链接库
//		else if(cmd.getClass() == ShiftPublishSingleScalerLibraryComponent.class) {
//			invoker = new CallShiftPublishSingleScalerLibraryComponentInvoker((ShiftPublishSingleScalerLibraryComponent) cmd);
//		} 
//		// 转发快捷组件应用附件
//		else if(cmd.getClass() == ShiftPublishSingleSwiftAssistComponent.class) {
//			invoker = new CallShiftPublishSingleSwiftAssistComponentInvoker((ShiftPublishSingleSwiftAssistComponent)cmd);
//		}
//		// 转发快捷组件动态链接库
//		else if(cmd.getClass() == ShiftPublishSingleSwiftLibraryComponent.class) {
//			invoker = new CallShiftPublishSingleSwiftLibraryComponentInvoker((ShiftPublishSingleSwiftLibraryComponent)cmd);
//		}
		
		// 找系统组件ACCOUNT站点
		else if (cmd.getClass() == ShiftLoadSystemTask.class) {
			invoker = new SubCommonShiftLoadSystemTaskInvoker((ShiftLoadSystemTask) cmd);
		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 判断调用器成立
		boolean success = (invoker != null);
		if (success) {
			success = CallInvokerPool.getInstance().launch(invoker);
		}

		return success;
	}

}