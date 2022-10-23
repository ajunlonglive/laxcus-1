/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.trust.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.account.*;
import com.laxcus.command.attend.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.cross.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.field.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.login.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.scan.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.find.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.stub.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.command.stub.reflex.*;
import com.laxcus.command.stub.sign.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.command.task.*;
import com.laxcus.command.task.talk.*;
import com.laxcus.command.traffic.*;
import com.laxcus.command.zone.*;
import com.laxcus.data.invoker.*;
import com.laxcus.distribute.calculate.command.*;
import com.laxcus.distribute.conduct.command.*;
import com.laxcus.distribute.establish.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;

/**
 * DATA站点异步命令管理池。<br><br>
 * 
 * DATA站点接受来自任何站点的命令，接受的条件必须有回显地址，否则无法回传命令处理数据。
 * 
 * @author scott.liang
 * @version 1.1 7/23/2013
 * @since laxcus 1.0
 */
public class DataCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static DataCommandPool selfHandle = new DataCommandPool();

	/**
	 * 构造DATA异步命令管理池
	 */
	private DataCommandPool() {
		super();
	}

	/**
	 * 返回DATA管理池的静态句柄
	 * @return DATA异步命令管理池
	 */
	public static DataCommandPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("DataCommandPool.getInstance");
		// 返回实例
		return DataCommandPool.selfHandle;
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		// HOME授权“建立/删除”DATA站点的用户资源引用
		if (cmd.getClass() == AwardCreateRefer.class) {
			invoker = new DataAwardCreateReferInvoker((AwardCreateRefer) cmd);
		} else if (cmd.getClass() == AwardDropRefer.class) {
			invoker = new DataAwardDropReferInvoker((AwardDropRefer) cmd);
		}
		//
		else if (cmd.getClass() == SetRefer.class) {
			invoker = new DataSetReferInvoker((SetRefer) cmd);
		}
		// 授权删除数据库
		else if (cmd.getClass() == AwardDropSchema.class) {
			invoker = new DataAwardDropSchemaInvoker((AwardDropSchema) cmd);
		}
		// HOME授权建表/删表
		else if (cmd.getClass() == AwardCreateTable.class) {
			invoker = new DataAwardCreateTableInvoker((AwardCreateTable) cmd);
		} else if (cmd.getClass() == AwardDropTable.class) {
			invoker = new DataAwardDropTableInvoker((AwardDropTable) cmd);
		}
		// SQL操作
		else if (cmd.getClass() == InsertGuide.class) {
			invoker = new DataInsertInvoker((InsertGuide) cmd);
		} else if (cmd.getClass() == CastSelect.class) {
			invoker = new DataCastSelectInvoker((CastSelect) cmd);
		} else if (cmd.getClass() == CastDelete.class) {
			invoker = new DataCastDeleteInvoker((CastDelete) cmd);
		} else if (cmd.getClass() == CastUpdate.class) {
			invoker = new DataCastUpdateInvoker((CastUpdate) cmd);
		}
		// 加载/卸载索引、数据实体
		else if (cmd.getClass() == LoadIndex.class) {
			invoker = new DataLoadIndexInvoker((LoadIndex) cmd);
		} else if (cmd.getClass() == LoadEntity.class) {
			invoker = new DataLoadEntityInvoker((LoadEntity) cmd);
		} else if (cmd.getClass() == StopIndex.class) {
			invoker = new DataStopIndexInvoker((StopIndex) cmd);
		} else if (cmd.getClass() == StopEntity.class) {
			invoker = new DataStopEntityInvoker((StopEntity) cmd);
		}
		// 扫描数据块
		else if (cmd.getClass() == ScanEntity.class) {
			invoker = new DataScanEntityInvoker((ScanEntity) cmd);
		}
		// 查找数据块编号命令
		else if (cmd.getClass() == GitStubs.class) {
			invoker = new DataGitStubsInvoker((GitStubs) cmd);
		} else if (cmd.getClass() == PrintStubsDiagram.class) {
			invoker = new DataPrintStubsDiagramInvoker((PrintStubsDiagram) cmd);
		} else if (cmd.getClass() == SingleExportEntity.class) {
			invoker = new DataSingleExportEntityInvoker((SingleExportEntity) cmd);
		}
		// 复制数据块
		else if (cmd.getClass() == CopyEntity.class) {
			invoker = new DataCopyEntityInvoker((CopyEntity) cmd);
		}
		// 数据优化操作
		else if (cmd.getClass() == Regulate.class) {
			invoker = new DataRegulateInvoker((Regulate) cmd);
		}
		// 通知更新数据块
		else if(cmd.getClass () == UpdateMass.class)  {
			invoker = new DataUpdateMassInvoker((UpdateMass) cmd);
		}
		// 查询表的全部行数和有效行数
		else if (cmd.getClass() == ScanTable.class) {
			invoker = new DataScanTableInvoker((ScanTable) cmd);
		}
		// 查询数据块签名
		else if (cmd.getClass() == TakeSign.class) {
			invoker = new DataTakeSignInvoker((TakeSign) cmd);
		}

		// CACHE状态数据块强制转换为CHUNK状态
		else if (cmd.getClass() == Rush.class) {
			invoker = new DataRushInvoker((Rush) cmd);
		} else if (cmd.getClass() == Compact.class) {
			invoker = new DataCompactInvoker((Compact) cmd);
		} else if (cmd.getClass() == SetDSMReduce.class) {
			invoker = new DataSetDSMReduceInvoker((SetDSMReduce) cmd);
		}
		// CONDUCT.FROM数据计算
		else if (cmd.getClass() == FromStep.class) {
			invoker = new DataConductFromInvoker((FromStep) cmd);
		}
		// ESTABLISH.SCAN/RISE阶段命令
		else if (cmd.getClass() == ScanStep.class) {
			invoker = new DataEstablishScanInvoker((ScanStep) cmd);
		} else if (cmd.getClass() == RiseStep.class) {
			invoker = new DataEstablishRiseInvoker((RiseStep) cmd);
		}
		// 获得CONDUCT计算过程中，DATA站点的中间数据
		else if (cmd.getClass() == TakeFluxData.class) {
			invoker = new CommonTakeFluxDataInvoker((TakeFluxData) cmd);
		}
		// 释放被缓存的中间数据（FluxField/FluxArea）
		else if (cmd.getClass() == ReleaseFluxField.class) {
			invoker = new CommonReleaseFluxFieldInvoker((ReleaseFluxField) cmd);
		} else if(cmd.getClass() == ReleaseFluxArea.class) {
			invoker = new CommonReleaseFluxAreaInvoker((ReleaseFluxArea)cmd);
		}
		// 获取分布组件标识
		else if (cmd.getClass() == TakeTaskTag.class) {
			invoker = new DataTakeTaskTagInvoker((TakeTaskTag) cmd);
		}
		// 获取码位计算器组件标识
//		else if (cmd.getClass() == TakeScalerTag.class) {
//			invoker = new CommonTakeScalerTagInvoker((TakeScalerTag) cmd);
//		}	
		
		// 增加/删除ARCHIVE站点（HOME站点发送）
		else if (cmd.getClass() == PushAccountSite.class) {
			invoker = new DataPushArchiveSiteInvoker((PushAccountSite) cmd);
		} else if (cmd.getClass() == DropAccountSite.class) {
			invoker = new CommonDropAccountSiteInvoker((DropAccountSite) cmd);
		}

		// 切换注册站点命令
		else if (cmd.getClass() == SwitchHub.class) {
			invoker = new CommonSwitchHubInvoker((SwitchHub) cmd);
		}
		// 查询索引分布区域命令
		else if (cmd.getClass() == FindIndexZone.class) {
			invoker = new DataFindIndexZoneInvoker((FindIndexZone) cmd);
		}
		// 查找域数据命令
		else if (cmd.getClass() == FindDataField.class) {
			invoker = new DataFindDataFieldInvoker((FindDataField) cmd);
		}
		// 筛选元数据命令
		else if (cmd.getClass() == SelectFieldToCall.class) {
			invoker = new DataSelectFieldToCallInvoker((SelectFieldToCall) cmd);
		}
		// 根据SELECT命令查关联的数据块编号
		else if (cmd.getClass() == FilteSelectStub.class) {
			invoker = new DataFilteSelectStubInvoker((FilteSelectStub) cmd);
		}
		// 设置存储映像数据（主块删除时，在DATA从站点发生）
		else if (cmd.getClass() == SetChunkReflexData.class) {
			invoker = new DataSetChunkReflexDataInvoker((SetChunkReflexData) cmd);
		} 
		// 设置缓存映像数据（主块添加或者删除时，在DATA从站点发生）
		else if (cmd.getClass() == SetCacheReflexData.class) {
			invoker = new DataSetCacheReflexDataInvoker((SetCacheReflexData) cmd);
		}
		// 删除缓存映像数据块（主块添满后，在DATA从站点发生）
		else if (cmd.getClass() == DeleteCacheReflex.class) {
			invoker = new DataDeleteCacheReflexInvoker((DeleteCacheReflex) cmd);
		}
		// 上传数据块命令
		else if (cmd.getClass() == DownloadMass.class) {
			invoker = new CommonUploadMassInvoker((DownloadMass) cmd);
		}
		// 检查表分布数据容量
		else if (cmd.getClass() == ScanSketch.class) {
			invoker = new DataScanSketchInvoker((ScanSketch) cmd);
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
		// 调用器相互确认命令
		else if (cmd.getClass() == SeekAttender.class) {
			invoker = new CommonSeekAttenderInvoker((SeekAttender) cmd);
		}
		// 修复表数据块
		else if (cmd.getClass() == RollTableMass.class) {
			invoker = new DataRollTableMassInvoker((RollTableMass) cmd);
		}
		// 强制重新注册
		else if (cmd.getClass() == RefreshLogin.class) {
			invoker = new CommonRefreshLoginInvoker((RefreshLogin) cmd);
		}
		// 检索分布任务组件/码位计算器
		else if (cmd.getClass() == SeekTask.class) {
			invoker = new DataSeekTaskInvoker((SeekTask) cmd);
		} 
//		else if (cmd.getClass() == SeekScaler.class) {
//			invoker = new CommonSeekScalerInvoker((SeekScaler) cmd);
//		}
		
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
		// 注册到HOME站点
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// 刷新发布
		else if (cmd.getClass() == RefreshPublish.class) {
			invoker = new DataRefreshPublishInvoker((RefreshPublish) cmd);
		}
		// 当前站点状态
		else if (cmd.getClass() == SeekSiteRuntime.class) {
			invoker = new DataSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
		}
		// 检索在线命令/在线资源
		else if (cmd.getClass() == SeekOnlineCommand.class) {
			invoker = new CommonSeekOnlineCommandInvoker((SeekOnlineCommand) cmd);
		} else if (cmd.getClass() == SeekOnlineResource.class) {
			invoker = new DataSeekOnlineResourceInvoker((SeekOnlineResource) cmd);
		}
		// 检索用户在线注册元数据
		else if (cmd.getClass() == SeekRegisterMetadata.class) {
			invoker = new DataSeekRegisterMetadataInvoker((SeekRegisterMetadata) cmd);
		}
		// 分布任务组件之间的检查、交互操作
		else if (cmd.getClass() == ShiftTalkCheck.class) {
			invoker = new CommonShiftTalkCheckInvoker((ShiftTalkCheck) cmd);
		} else if (cmd.getClass() == ShiftTalkAsk.class) {
			invoker = new CommonShiftTalkAskInvoker((ShiftTalkAsk) cmd);
		}
		// 来自其它节点的检查、交互操作
		else if (cmd.getClass() == TalkCheck.class) {
			invoker = new CommonTalkCheckInvoker((TalkCheck) cmd);
		} else if (cmd.getClass() == TalkAsk.class) {
			invoker = new DataTalkAskInvoker((TalkAsk) cmd);
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
			invoker = new DataDeployTableInvoker((DeployTable) cmd);
		}
		// DATA主从节点复制数据块，接受这个命令的必须是DATA主节点
		else if (cmd.getClass() == CopyMasterMass.class) {
			invoker = new DataCopyMasterMassInvoker((CopyMasterMass) cmd);
		}
		// 从HOME节点获得资源引用
		else if (cmd.getClass() == ShiftTakeRefer.class) {
			invoker = new DataShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}
		// DATA向HOME提交故障表，这是一个单向操作，不返回HOME反馈
		else if (cmd.getClass() == SubmitFaultTable.class) {
			invoker = new DataSubmitFaultTableInvoker((SubmitFaultTable) cmd);
		}
		// 接受分布任务组件附件/动态链接库
		else if (cmd.getClass() == MailTaskAssistComponent.class) {
			invoker = new DataMailTaskAssistComponentInvoker((MailTaskAssistComponent) cmd);
		} else if(cmd.getClass() == MailTaskLibraryComponent.class) {
			invoker = new DataMailTaskLibraryComponentInvoker((MailTaskLibraryComponent)cmd);
		}
		// 删除云应用
		else if (cmd.getClass() == DropTaskApplication.class) {
			invoker = new DataDropTaskApplicationInvoker((DropTaskApplication) cmd);
		}
//		// 接受码位计算器/动态链接库
//		else if (cmd.getClass() == MailScalerAssistComponent.class) {
//			invoker = new DataMailScalerAssistComponentInvoker((MailScalerAssistComponent) cmd);
//		} else if (cmd.getClass() == MailScalerLibraryComponent.class) {
//			invoker = new DataMailScalerLibraryComponentInvoker((MailScalerLibraryComponent) cmd);
//		}
		// 设置虚拟空间参数
		else if (cmd.getClass() == SetMemberCyber.class) {
			invoker = new DataSetMemberCyberInvoker((SetMemberCyber) cmd);
		} else if (cmd.getClass() == CheckUserCyber.class) {
			invoker = new DataCheckUserCyberInvoker((CheckUserCyber) cmd);
		}
		// 查找注册节点
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new DataAskClusterMemberInvoker((AskClusterMember) cmd);
		} else if (cmd.getClass() == ShiftPushRegisterMember.class) {
			invoker = new DataShiftPushRegisterMemberInvoker((ShiftPushRegisterMember) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new SubOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new SubCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 检索云端应用
		else if (cmd.getClass() == SeekCloudWare.class) {
			invoker = new DataSeekCloudWareInvoker((SeekCloudWare) cmd);
		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 如果没有匹配，发送一个不支持的异步应答；否则转交给调用器管理池处理
		boolean success = (invoker != null);
		if (success) {
			success = DataInvokerPool.getInstance().launch(invoker);
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

		// 通过HOME站点，向TOP站点获取数据块编号
		if (cmd.getClass() == ShiftTakeStub.class) {
			invoker = new CommonShiftTakeStubInvoker((ShiftTakeStub) cmd);
		}
		// 从HOME节点获得资源引用
		else if (cmd.getClass() == ShiftTakeRefer.class) {
			invoker = new DataShiftTakeReferInvoker((ShiftTakeRefer) cmd);
		}
		// 查询表配置
		else if (cmd.getClass() == ShiftTakeTable.class) {
			invoker = new CommonShiftTakeTableInvoker((ShiftTakeTable) cmd);
		}
		// 映像数据
		else if (cmd.getClass() == ShiftFindReflexStubSite.class) {
			invoker = new DataShiftFindReflexStubSiteInvoker((ShiftFindReflexStubSite) cmd);
		} else if (cmd.getClass() == ShiftSetReflexData.class) {
			invoker = new DataShiftSetReflexDataInvoker((ShiftSetReflexData) cmd);
		} else if (cmd.getClass() == SetCacheReflexStub.class) {
			invoker = new DataSetCacheReflexStubInvoker((SetCacheReflexStub) cmd);
		}
		// 更新数据块转发操作
		else if (cmd.getClass() == ShiftUpdateMass.class) {
			invoker = new CommonShiftUpdateMassInvoker((ShiftUpdateMass) cmd);
		}
		// 向HOME站点发送缓存映像数据块
		else if (cmd.getClass() == SetCacheReflexStub.class) {
			invoker = new DataSetCacheReflexStubInvoker((SetCacheReflexStub) cmd);
		}
		// 删除映像数据块
		else if (cmd.getClass() == ShiftDeleteCacheReflex.class) {
			invoker = new CommonShiftDeleteCacheReflexInvoker((ShiftDeleteCacheReflex) cmd);
		}
		// 获取分布任务组件
		else if (cmd.getClass() == ShiftTakeTaskComponent.class) {
			invoker = new DataShiftTakeTaskComponentInvoker((ShiftTakeTaskComponent) cmd);
		}
//		// 获取码位计算器组件
//		else if (cmd.getClass() == ShiftTakeScalerComponent.class) {
//			invoker = new CommonShiftTakeScalerComponentInvoker((ShiftTakeScalerComponent) cmd);
//		}
		// 下载数据块命令
		else if (cmd.getClass() == ShiftDownloadMass.class) {
			invoker = new CommonShiftDownloadMassInvoker((ShiftDownloadMass) cmd);
		}
		// 查询数据块编号关联的从站点
		else if(cmd.getClass() == ShiftFindStubSlaveSite.class) {
			invoker = new DataShiftFindStubSlaveSiteInvoker((ShiftFindStubSlaveSite)cmd);
		}
		// 查询关联站点
		else if (cmd.getClass() == ShiftFindSite.class) {
			invoker = new CommonShiftFindSiteInvoker((ShiftFindSite) cmd);
		} else if (cmd.getClass() == ShiftFindPhaseSite.class) {
			invoker = new CommonShiftFindPhaseSiteInvoker((ShiftFindPhaseSite) cmd);
		} else if (cmd.getClass() == ShiftFindSpaceSite.class) {
			invoker = new CommonShiftFindSpaceSiteInvoker((ShiftFindSpaceSite) cmd);
		} else if (cmd.getClass() == ShiftFindUserSite.class) {
			invoker = new CommonShiftFindUserSiteInvoker((ShiftFindUserSite) cmd);
		}
		// 获得ARCHIVE站点地址
		else if (cmd.getClass() == ShiftTakeSigerSite.class) {
			invoker = new CommonShiftTakeSigerSiteInvoker((ShiftTakeSigerSite) cmd);
		}
		// 异步调用器相互确认（在主控节点不参与的情况下的、去中心化处理）
		else if (cmd.getClass() == ShiftSeekAttender.class) {
			invoker = new CommonShiftSeekAttenderInvoker((ShiftSeekAttender) cmd);
		}
		// 查询授权单元
		else if (cmd.getClass() == ShiftSeekActiveItem.class) {
			invoker = new DataShiftSeekActiveItemInvoker((ShiftSeekActiveItem) cmd);
		}
		// INSERT/DELETE/LEAVE代理执行
		else if (cmd.getClass() == ShiftTrustInsert.class) {
			invoker = new DataShiftTrustInsertInvoker((ShiftTrustInsert) cmd);
		} else if (cmd.getClass() == ShiftTrustDelete.class) {
			invoker = new DataShiftTrustDeleteInvoker((ShiftTrustDelete) cmd);
		}
		// 注册到HOME站点
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// 找系统组件ACCOUNT站点
		else if (cmd.getClass() == ShiftLoadSystemTask.class) {
			invoker = new SubCommonShiftLoadSystemTaskInvoker((ShiftLoadSystemTask) cmd);
		}
		// 下载数据块命令
		else if (cmd.getClass() == ShiftDownloadMass.class) {
			invoker = new CommonShiftDownloadMassInvoker((ShiftDownloadMass) cmd);
		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 判断调用器成立
		boolean success = (invoker != null);
		if (success) {
			success = DataInvokerPool.getInstance().launch(invoker);
		}
		return success;
	}

}