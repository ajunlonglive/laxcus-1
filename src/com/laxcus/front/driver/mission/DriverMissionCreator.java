/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.mission;

import com.laxcus.access.parse.*;
import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.command.access.permit.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.cross.*;
import com.laxcus.command.establish.*;
import com.laxcus.command.forbid.*;
import com.laxcus.command.limit.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.rule.*;
import com.laxcus.command.scan.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.driver.*;
import com.laxcus.front.driver.invoker.*;
import com.laxcus.front.invoker.*;
import com.laxcus.mission.*;
import com.laxcus.util.*;

/**
 * 驱动任务生成器 <br>
 * 
 * 驱动任务生成器将命令文本语句转换成类实例驱动任务，或者将命令包装到驱动任务的服务。
 * 
 * @author scott.liang
 * @version 1.05 12/02/2015
 * @since laxcus 1.0
 */
public class DriverMissionCreator extends MissionCreator {

	/**
	 * 构造默认的任务生成器
	 */
	public DriverMissionCreator() {
		super();
	}

	/**
	 * 返回当前用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return DriverLauncher.getInstance().getUsername();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.mission.MissionCreator#create(java.lang.String)
	 */
	@Override
	public Command create(String input) throws MissionException {
		Laxkit.nullabled(input);

		Command cmd = null;
		try {
			cmd = split(input);
		} catch (Throwable e) {
			throw new MissionException(e);
		}
		return cmd;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.mission.MissionCreator#create(com.laxcus.command.Command)
	 */
	@Override
	public DriverMission create(Command cmd) throws MissionException {
		// 检测空指针
		Laxkit.nullabled(cmd);

		// 建立驱动任务
		return new DriverMission(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.mission.MissionCreator#createInvoker(com.laxcus.mission.Mission)
	 */
	@Override
	public FrontInvoker createInvoker(Mission m) throws MissionException {
		// 检测空指针
		Laxkit.nullabled(m);

		// 类实例检测
		if (!Laxkit.isClassFrom(m, DriverMission.class)) {
			throw new MissionException("cannot be cast! %s", m.getClass().getName());
		}

		DriverMission mission = (DriverMission) m;

		DriverInvoker invoker = null;
		// 取出命令
		Command cmd = mission.getCommand();
		Class<?> clazz = cmd.getClass();

		/** 以下将根据命令，建立对应的调用器 **/

		// 用户账号命令
		if (clazz == CreateUser.class) {
			invoker = new DriverCreateUserInvoker(mission);
		} else if (clazz == DropUser.class) {
			invoker = new DriverDropUserInvoker(mission);
		} else if (clazz == AlterUser.class) {
			invoker = new DriverAlterUserInvoker(mission);
		}
		// 授与/解除操作权限
		else if (clazz == Grant.class) {
			invoker = new DriverGrantInvoker(mission);
		} else if (clazz == Revoke.class) {
			invoker = new DriverRevokeInvoker(mission);
		}
		// 建立/删除数据库
		else if (clazz == CreateSchema.class) {
			invoker = new DriverCreateSchemaInvoker(mission);
		} else if (clazz == DropSchema.class) {
			invoker = new DriverDropSchemaInvoker(mission);
		} else if (clazz == ShowSchema.class) {
			invoker = new DriverShowSchemaInvoker(mission);
		} else if (clazz == ScanSchema.class) {
			invoker = new DriverScanSchemaInvoker(mission);
		}
		// 建立/删除数据表
		else if (clazz == CreateTable.class) {
			invoker = new DriverCreateTableInvoker(mission);
		} else if (clazz == DropTable.class) {
			invoker = new DriverDropTableInvoker(mission);
		} else if (clazz == ShowTable.class) {
			invoker = new DriverShowTableInvoker(mission);
		} else if (clazz == ScanTable.class) {
			invoker = new DriverScanTableInvoker(mission);
		}
		// 数据处理
		else if (clazz == Select.class) {
			invoker = ShiftCreator.createInvoker(mission);
		} else if (clazz == Insert.class) {
			invoker = new DriverInsertInvoker(mission);
		} else if (clazz == Delete.class) {
			invoker = ShiftCreator.createInvoker(mission);
		} else if (clazz == Update.class) {
			invoker = ShiftCreator.createInvoker(mission);
		}
		// 分布数据计算
		else if (clazz == Conduct.class) {
			invoker = new DriverConductInvoker(mission);
		}
		// 分布数据构造
		else if (clazz == Establish.class) {
			invoker = new DriverEstablishInvoker(mission);
		}
		// 数据本地优化
		else if (clazz == Regulate.class) {
			invoker = new DriverRegulateInvoker(mission);
		}
		// 数据分布网络优化
		else if (clazz == Modulate.class) {
			invoker = ShiftCreator.createInvoker(mission);
		}
		// 数据优化时间（建立、删除、打印）
		else if (clazz == CreateRegulateTime.class) {
			invoker = new DriverRegulateTimeInvoker(mission);
		} else if (clazz == DropRegulateTime.class) {
			invoker = new DriverDropRegulateTimeInvoker(mission);
		} else if (clazz == PrintRegulateTime.class) {
			invoker = new DriverPrintRegulateTimeInvoker(mission);
		}
		// 加载、卸载索引、数据块
		else if (clazz == LoadIndex.class) {
			invoker = new DriverLoadIndexInvoker(mission);
		} else if (clazz == StopIndex.class) {
			invoker = new DriverStopIndexInvoker(mission);
		} else if (clazz == LoadEntity.class) {
			invoker = new DriverLoadEntityInvoker(mission);
		} else if (clazz == StopEntity.class) {
			invoker = new DriverStopEntityInvoker(mission);
		}
		// 设置数据块尺寸/扫描数据块
		else if (clazz == SetEntitySize.class) {
			invoker = new DriverSetEntitySizeInvoker(mission);
		} else if (clazz == ScanEntity.class) {
			invoker = new DriverScanEntityInvoker(mission);
		}

		// 检查表数据一致
		else if (clazz == CheckEntityConsistency.class) {
			invoker = new DriverCheckEntityConsistencyInvoker(mission);
		}
		// 恢复表数据一致
		else if (clazz == RecoverEntityConsistency.class) {
			invoker = new DriverRecoverEntityConsistencyInvoker(mission);
		}
		// 表分布数据容量
		else if (clazz == ScanSketch.class) {
			invoker = new DriverScanSketchInvoker(mission);
		}

		// 资源限制
		else if (clazz == CreateLimit.class) {
			invoker = new DriverCreateLimitInvoker(mission);
		} else if (clazz == DropLimit.class) {
			invoker = new DriverDropLimitInvoker(mission);
		} else if (clazz == ShowLimit.class) {
			invoker = new DriverShowLimitInvoker(mission);
		} else if (clazz == CreateFault.class) {
			invoker = new DriverCreateFaultInvoker(mission);
		} else if (clazz == DropFault.class) {
			invoker = new DriverDropFaultInvoker(mission);
		} else if (clazz == ShowFault.class) {
			invoker = new DriverShowFaultInvoker(mission);
		} else if (clazz == ShowForbid.class) {
			invoker = new DriverShowForbidInvoker(mission);
		} else if (clazz == ShowLockRule.class) {
			invoker = new DriverShowLockRuleInvoker(mission);
		}

		// 开放/关联/打印共享资源
		else if (clazz == OpenShareSchema.class) {
			invoker = new DriverOpenShareSchemaInvoker(mission);
		} else if (clazz == OpenShareTable.class) {
			invoker = new DriverOpenShareTableInvoker(mission);
		} else if (clazz == CloseShareSchema.class) {
			invoker = new DriverCloseShareSchemaInvoker(mission);
		} else if (clazz == CloseShareTable.class) {
			invoker = new DriverCloseShareTableInvoker(mission);
		} else if (clazz == ShowOpenResource.class) {
			invoker = new DriverPrintShareTableInvoker(mission);
		}
//		// 快捷组件
//		else if (clazz == DropSwift.class) {
//			invoker = new DriverDropSwiftInvoker(mission);
//		} else if (clazz == PrintSwift.class) {
//			invoker = new DriverPrintSwiftInvoker(mission);
//		} else if (clazz == RunSwift.class) {
//			invoker = new DriverRunSwiftInvoker(mission);
//		}

		// 账号参数
		else if (clazz == SetMaxMembers.class) {
			invoker = new DriverSetMaxMembersInvoker(mission);
		} else if (clazz == SetMaxJobs.class) {
			invoker = new DriverSetMaxJobsInvoker(mission);
		} else if (clazz == SetMaxTasks.class) {
			invoker = new DriverSetMaxTasksInvoker(mission);
		} else if(clazz == SetMaxRegulates.class) {
			invoker = new DriverSetMaxRegulatesInvoker(mission);
		} else if (clazz == SetMaxSize.class) {
			invoker = new DriverSetMaxSizeInvoker(mission);
		} else if (clazz == SetMaxGroups.class) {
			invoker = new DriverSetMaxGroupsInvoker(mission);
		} else if (clazz == SetMaxGateways.class) {
			invoker = new DriverSetMaxGatewaysInvoker(mission);
		} else if(clazz == SetMaxWorkers.class) {
			invoker = new DriverSetMaxWorkersInvoker(mission);
		} else if (clazz == SetMaxBuilders.class) {
			invoker = new DriverSetMaxWorkersInvoker(mission);
		} else if (clazz == SetMaxTables.class) {
			invoker = new DriverSetMaxTablesInvoker(mission);
		} else if (clazz == SetMaxIndexes.class) {
			invoker = new DriverSetMaxIndexesInvoker(mission);
		}

		// 本地工具命令（散列命令、半截符命令、设置密文超时、命令模式、命令超时）
		else if (clazz == BuildHash.class) {
			invoker = new DriverBuildHashInvoker(mission);
		} else if (clazz == BuildHalf.class) {
			invoker = new DriverBuildHalfInvoker(mission);
		} 
		// 本地设置参数
		else if (clazz == CipherTimeout.class) {
			invoker = new DriverCipherTimeoutInvoker(mission);
		} else if (clazz == CommandTimeout.class) {
			invoker = new DriverCommandTimeoutInvoker(mission);
		} else if (clazz == CommandMode.class) {
			invoker = new DriverCommandModeInvoker(mission);
		}
		// 获取数据块编号/导出一个数据块/导入一组数据到集群
		else if (clazz == GitStubs.class) {
			invoker = new DriverGitStubsInvoker(mission);
		} else if (clazz == ExportEntity.class) {
			invoker = new DriverExportEntityInvoker(mission);
		} else if (clazz == ImportEntity.class) {
			invoker = new DriverImportEntityInvoker(mission);
		}
		// UDP数据流测试
		else if (clazz == Swarm.class) {
			invoker = new DriverSwarmInvoker(mission);
		}
		
		return invoker;
	}

	/**
	 * 解析语句，转成命令
	 * @param input LAXCUS分布描述语句
	 * @return 命令实例
	 */
	private Command split(String input) {
		// 建立/删除/修改用户账号
		if (checker.isCreateUser(input)) return doCreateUser(input);
		if (checker.isDropUser(input)) return doDropUser(input);
		if (checker.isAlterUser(input)) return doAlterUser(input);

		// 授权/解除授权
		if (checker.isGrant(input)) return doGrant(input);
		if (checker.isRevoke(input)) return doRevoke(input);

		// 建立/删除/扫描数据库
		if (checker.isCreateSchema(input)) return doCreateSchema(input);
		if (checker.isDropSchema(input)) return doDropSchema(input);
		if (checker.isShowSchema(input)) return doShowSchema(input);
		if (checker.isScanSchema(input)) return doScanSchema(input);

		// 建立/删除数据表
		if (checker.isCreateTable(input)) return doCreateTable(input);
		if (checker.isDropTable(input)) return doDropTable(input);
		if (checker.isShowTable(input)) return doShowTable(input);
		if (checker.isScanTable(input)) return doScanTable(input);

		// 建立/撤销/打印数据优化时间
		if (checker.isCreateRegulateTime(input)) return doCreateRegulateTime(input);
		if (checker.isDropRegulateTime(input)) return doDropRegulateTime(input);
		if (checker.isPrintRegulateTime(input)) return doPrintRegulateTime(input);

		// 加载/卸载索引和数据块
		if (checker.isLoadIndex(input)) return doLoadIndex(input);
		if (checker.isStopIndex(input)) return doStopIndex(input);
		if (checker.isLoadEntity(input)) return doLoadEntity(input);
		if (checker.isStopEntity(input)) return doStopEntity(input);
		// 设置数据块尺寸/扫描数据块
		if (checker.isSetEntitySize(input)) return doSetEntitySize(input);
		if (checker.isScanEntity(input)) return doScanEntity(input);

		// 判断是“建立/删除/显示”限制操作单元
		if (checker.isCreateLimit(input)) return doCreateLimit(input);
		if (checker.isDropLimit(input)) return doDropLimit(input);
		if (checker.isShowLimit(input)) return doShowLimit(input);

		// 判断是“提交/撤销/显示”锁定单元
		if (checker.isCreateFault(input)) return doCreateFault(input);
		if (checker.isDropFault(input)) return doDropFault(input);
		if (checker.isShowFault(input)) return doShowFault(input);

		// 判断是显示禁止操作单元
		if (checker.isShowForbid(input)) return doShowForbid(input);
		// 判断是显示事务规则
		if (checker.isShowLockRule(input)) return doShowLockRule(input);

		// 检查/修复表数据一致性
		if (checker.isCheckEntityConsistency(input)) return doCheckEntityConsistency(input);
		if (checker.isRecoverEntityConsistency(input)) return doRecoverEntityConsistency(input);
		// 检查分布数据容量
		if (checker.isScanSketch(input)) return doScanSketch(input);

		// 开放/关闭共享资源
		if (checker.isOpenShareSchema(input)) return doOpenShareSchema(input);
		if (checker.isOpenShareTable(input)) return doOpenShareTable(input);
		if (checker.isCloseShareSchema(input)) return doCloseShareSchema(input);
		if (checker.isCloseShareTable(input)) return doCloseShareTable(input);
		if (checker.isShowOpenResource(input)) return doPrintShareTable(input);

		// 用户参数
		if (checker.isSetMaxMembers(input)) return doSetMaxMembers(input);
		if (checker.isSetMaxJobs(input)) return doSetMaxJobs(input);
		if (checker.isSetMaxTasks(input)) return doSetMaxTasks(input);
		if (checker.isSetMaxSize(input)) return doSetMaxSize(input);
		if (checker.isSetMaxGroups(input)) return doSetMaxGroups(input);
		if (checker.isSetMaxGateways(input)) return doSetMaxGateways(input);
		if (checker.isSetMaxWorkers(input)) return doSetMaxWorkers(input);
		if (checker.isSetMaxBuilders(input)) return doSetMaxBuilders(input);
		if (checker.isSetMaxTables(input)) return doSetMaxTables(input);
		if (checker.isSetMaxIndexes(input)) return doSetMaxIndexes(input);

		// 本地命令（生成散列码、半截符、设置密文超时、命令模式、命令超时）
		if (checker.isBuildHash(input)) return doHashCommand(input); 
		if (checker.isBuildHalf(input)) return doHalfCommand(input);

		if (checker.isCipherTimeout(input)) return doSetCipherTimeout(input);
		if (checker.isCommandMode(input)) return doSetCommandMode(input);
		if (checker.isCommandTimeout(input)) return doSetCommandTimeout(input);

//		// 快捷组件
//		if (checker.isDropSwift(input)) return doDropSwift(input);
//		if (checker.isPrintSwift(input)) return doPrintSwift(input);
//		if (checker.isRunSwift(input)) return doRunSwift(input);

		// INSERT/INJECT插入数据。有“INSERT INTO”和“INJECT INTO”两种情况
		if (checker.isInsert(input, true)) return doInsert(input);
		if (checker.isInject(input, true)) return doInject(input);
		//  SELECT查询
		if (checker.isSelect(input, true)) return doSelect(input);
		// DELETE删除
		if (checker.isDelete(input, true)) return doDelete(input);
		// UPDATE更新
		if (checker.isUpdate(input, true)) return doUpdate(input);
		// 分布数据计算
		if (checker.isConduct(input)) return doConduct(input);
		// 分布数据构造
		if (checker.isEstablish(input)) return doEstablish(input);
		// 分布数据优化（生成ESTABLISH命令去处理）
		if (checker.isModulate(input)) return doModulate(input);
		// 本地数据优化
		if (checker.isRegulate(input)) return doRegulate(input);

		// 数据块
		if(checker.isGitStubs(input)) return doGitStubs(input);
		if(checker.isExportEntity(input)) return doExportEntity(input);
		if(checker.isImportEntity(input)) return doImportEntity(input);

		// UDP数据包测试
		if (checker.isSwarm(input)) return doSwarm(input);

		// 判断是自定义命令和解析命令（放在最后）
		if (CustomCreator.isCommand(input)) {
			return CustomCreator.split(input);
		}

		// 返回命令
		return null;
	}

	/**
	 * 建立用户账号
	 * @param input 输入语句
	 * @return 返回CreateUser命令
	 */
	private CreateUser doCreateUser(String input) {
		CreateUserParser parser = new CreateUserParser();
		return parser.split(input, true);
	}

	/**
	 * 删除用户账号
	 * @param input 输入语句
	 * @return 返回DropUser命令
	 */
	private DropUser doDropUser(String input) {
		DropUserParser parser = new DropUserParser();
		return parser.split(input, true);
	}

//	/**
//	 * 删除用户账号
//	 * @param input 输入语句
//	 * @return 返回DropUser命令
//	 */
//	private DropUser doDropSHA256User(String input) {
//		DropUserParser parser = new DropUserParser();
//		return parser.splitDropSHA256User(input, true);
//	}

	/**
	 * 修改用户账号密码
	 * @param input 输入语句
	 * @return 返回AlterUser命令
	 */
	private AlterUser doAlterUser(String input) {
		AlterUserParser parser = new AlterUserParser();
		AlterUser cmd = parser.split(input, true);
		return cmd;
	}

	/**
	 * 设置用户账号操作权限
	 * @param input 输入语句
	 * @return 返回Grant命令
	 */
	private Grant doGrant(String input) {
		GrantParser parser = new GrantParser();
		Grant grant = parser.split(input, true);
		return grant;
	}

	/**
	 * 回收用户账号操作权限
	 * @param input 输入语句
	 * @return 返回Revoke命令
	 */
	private Revoke doRevoke(String input) {
		RevokeParser parser = new RevokeParser();
		Revoke revoke = parser.split(input, true);
		return revoke;
	}

	/**
	 * 建立数据库
	 * @param input 输入语句
	 * @return 返回CreateSchema命令
	 */
	private CreateSchema doCreateSchema(String input) {
		CreateSchemaParser parser = new CreateSchemaParser();
		return parser.split(input, true);
	}

	/**
	 * 删除数据库
	 * @param input 输入语句
	 * @return 返回DropSchema命令
	 */
	private DropSchema doDropSchema(String input) {
		DropSchemaParser parser = new DropSchemaParser();
		return parser.split(input, true);
	}

	/**
	 * 显示数据库
	 * @param input 输入语句
	 * @return 返回ShowSchema命令
	 */
	private ShowSchema doShowSchema(String input) {
		ShowSchemaParser parser = new ShowSchemaParser();
		return parser.split(input, true);
	}

	/**
	 * 扫描数据库
	 * @param input 输入语句
	 * @return 返回ScanSchema命令
	 */
	private ScanSchema doScanSchema(String input) {
		ScanSchemaParser parser = new ScanSchemaParser();
		return parser.split(input, true);
	}

	/**
	 * 建立数据表
	 * @param input 输入语句
	 * @return 返回CreateTable命令
	 */
	private CreateTable doCreateTable(String input) {
		CreateTableParser parser = new CreateTableParser();
		CreateTable cmd = parser.split(input, true);
		// 设置当前持有人签名
		cmd.setIssuer(getUsername());
		return cmd;
	}

	/**
	 * 删除数据表
	 * @param input 输入语句
	 * @return 返回DropTable命令
	 */
	private DropTable doDropTable(String input) {
		DropTableParser parser = new DropTableParser();
		return parser.split(input, true);
	}

	/**
	 * 删除数据表
	 * @param input 输入语句
	 * @return 返回ShowTable命令
	 */
	private ShowTable doShowTable(String input) {
		ShowTableParser parser = new ShowTableParser();
		return parser.split(input, true);
	}

	/**
	 * 扫描数据表
	 * @param input 输入语句
	 * @return 返回ScanTable命令
	 */
	private ScanTable doScanTable(String input) {
		ScanTableParser parser = new ScanTableParser();
		return parser.split(input, true);
	}

	/**
	 * 计算散列码（在本地进行）
	 * @param input 输入语句
	 * @return 返回BuildHash命令
	 */
	private BuildHash doHashCommand(String input) {
		BuildHashParser parser = new BuildHashParser();
		return parser.split(input);
	}

	/**
	 * 计算散列码（在本地进行）
	 * @param input 输入语句
	 * @return 返回BuildHalf命令
	 */
	private BuildHalf doHalfCommand(String input) {
		BuildHalfParser parser = new BuildHalfParser();
		return parser.split(input);
	}

	/**
	 * 设置客户端密文
	 * @param input 输入语句
	 * @return 返回CipherTimeout命令
	 */
	private CipherTimeout doSetCipherTimeout(String input) {
		CipherTimeoutParser parser = new CipherTimeoutParser();
		return parser.split(input);
	}

	/**
	 * 设置命令模式
	 * @param input 输入语句
	 * @return 返回CommandMode命令
	 */
	private CommandMode doSetCommandMode(String input) {
		CommandModeParser parser = new CommandModeParser();
		return parser.split(input);
	}

	/**
	 * 设置命令超时
	 * @param input 输入语句
	 * @return 返回CommandTimeout命令
	 */
	private CommandTimeout doSetCommandTimeout(String input) {
		CommandTimeoutParser parser = new CommandTimeoutParser();
		return parser.split(input);
	}

	/**
	 * 检查表数据一致
	 * @param input 字符串命令
	 * @return 返回doCheckEntityConsistency命令
	 */
	private CheckEntityConsistency doCheckEntityConsistency(String input) {
		CheckEntityConsistencyParser parser = new CheckEntityConsistencyParser();
		return parser.split(input, true);
	}

	/**
	 * 修复表数据一致
	 * @param input 字符串命令
	 * @return 返回RecoverEntityConsistency命令
	 */
	private RecoverEntityConsistency doRecoverEntityConsistency(String input) {
		RecoverEntityConsistencyParser parser = new RecoverEntityConsistencyParser();
		return parser.split(input, true);
	}

	/**
	 * 检查表分布数据容量
	 * @param input 输入语句
	 * @return 返回ScanSketch命令
	 */
	private ScanSketch doScanSketch(String input) {
		ScanSketchParser parser = new ScanSketchParser();
		return parser.split(input, true);
	}

	/**
	 * 开放数据库共享给其他用户
	 * @param input 输入语句
	 * @return 返回OpenShareSchema命令
	 */
	private OpenShareSchema doOpenShareSchema(String input) {
		OpenShareSchemaParser parser = new OpenShareSchemaParser();
		return parser.split(input, true);
	}

	/**
	 * 收回共享数据库
	 * @param input 输入语句
	 * @return 返回CloseShareSchema命令
	 */
	private CloseShareSchema doCloseShareSchema(String input) {
		CloseShareSchemaParser parser = new CloseShareSchemaParser();
		return parser.split(input, true);
	}

	/**
	 * 开放数据表共享给其他用户
	 * @param input 输入语句
	 * @return 返回OpenShareTable命令
	 */
	private OpenShareTable doOpenShareTable(String input) {
		OpenShareTableParser parser = new OpenShareTableParser();
		return parser.split(input, true);
	}

	/**
	 * 收回共享数据表
	 * @param input 输入语句
	 * @return 返回CloseShareTable命令
	 */
	private CloseShareTable doCloseShareTable(String input) {
		CloseShareTableParser parser = new CloseShareTableParser();
		return parser.split(input, true);
	}

	/**
	 * 打印共享数据表
	 * @param input 输入语句
	 * @return 返回PrintShareTable命令
	 */
	private ShowOpenResource doPrintShareTable(String input) {
		ShowOpenResourceParser parser = new ShowOpenResourceParser();
		return parser.split(input);
	}

	/**
	 * 最大在线用户数目
	 * @param input 输入语句
	 * @return 返回SetMaxMembers命令
	 */
	private SetMaxMembers doSetMaxMembers(String input) {
		SetMaxMembersParser parser = new SetMaxMembersParser();
		return parser.split(input);
	}

	/**
	 * 用户的最大并行任务数目
	 * @param input 输入语句
	 * @return 返回SetMaxJobs命令
	 */
	private SetMaxJobs doSetMaxJobs(String input) {
		SetMaxJobsParser parser = new SetMaxJobsParser();
		return parser.split(input);
	}

	/**
	 * 用户的最大快捷组件数目
	 * @param input 输入语句
	 * @return 返回SetMaxTasks命令
	 */
	private SetMaxTasks doSetMaxTasks(String input) {
		SetMaxTasksParser parser = new SetMaxTasksParser();
		return parser.split(input);
	}

	/**
	 * 用户的最大磁盘空间数目
	 * @param input 输入语句
	 * @return 返回SetMaxSize命令
	 */
	private SetMaxSize doSetMaxSize(String input) {
		SetMaxSizeParser parser = new SetMaxSizeParser();
		return parser.split(input);
	}

	/**
	 * 用户HOME子域集群数目
	 * @param input 输入语句
	 * @return 返回SetMaxGroups命令
	 */
	private SetMaxGroups doSetMaxGroups(String input) {
		SetMaxGroupsParser parser = new SetMaxGroupsParser();
		return parser.split(input);
	}

	/**
	 * 用户的CALL网关节点数目
	 * @param input 输入语句
	 * @return 返回SetMaxGateways命令
	 */
	private SetMaxGateways doSetMaxGateways(String input) {
		SetMaxGatewaysParser parser = new SetMaxGatewaysParser();
		return parser.split(input);
	}

	/**
	 * 用户的WORK节点数目
	 * @param input 输入语句
	 * @return 返回SetMaxWorkers命令
	 */
	private SetMaxWorkers doSetMaxWorkers(String input) {
		SetMaxWorkersParser parser = new SetMaxWorkersParser();
		return parser.split(input);
	}
	
	/**
	 * 用户的BUILD节点数目
	 * @param input 输入语句
	 * @return 返回SetMaxBuilders命令
	 */
	private SetMaxBuilders doSetMaxBuilders(String input) {
		SetMaxBuildersParser parser = new SetMaxBuildersParser();
		return parser.split(input);
	}

	/**
	 * 用户可以建立的表数目
	 * @param input
	 * @return
	 */
	private SetMaxTables doSetMaxTables(String input) {
		SetMaxTablesParser parser = new SetMaxTablesParser();
		return parser.split(input);
	}

	/**
	 * 一个表的最大索引数目
	 * @param input
	 * @return
	 */
	private SetMaxIndexes doSetMaxIndexes(String input) {
		SetMaxIndexesParser parser = new SetMaxIndexesParser();
		return parser.split(input);
	}

	/**
	 * 解析“INSERT INTO”命令
	 * @param input 文本命令
	 * @return 返回INSERT命令
	 */
	private Insert doInsert(String input) {
		InsertParser parser = new InsertParser();
		return parser.splitInsert(input, true);
	}

	/**
	 * 解析“INJECT INTO”命令
	 * @param input 文本命令
	 * @return 返回INSERT命令
	 */
	private Insert doInject(String input) {
		InsertParser parser = new InsertParser();
		return parser.splitInject(input, true);
	}

	/**
	 * 解析SELECT命令
	 * @param input 输入语句
	 * @return 返回SELECT命令
	 */
	private Select doSelect(String input) {
		SelectParser parser = new SelectParser();
		return parser.split(input, true);
	}

	/**
	 * 解析DELETE命令
	 * @param input DELETE文本描述
	 * @return 返回DELETE命令
	 */
	private Command doDelete(String input) {
		DeleteParser parser = new DeleteParser();
		return parser.split(input, true);
	}

	/**
	 * 解析UPDATE命令
	 * @param input UPDATE文本描述
	 * @return 返回UPDATE命令
	 */
	private Command doUpdate(String input) {
		UpdateParser parser = new UpdateParser();
		return parser.split(input, true);
	}

	/**
	 * 解析REGULATE命令
	 * @param input 输入语句
	 * @return
	 */
	private Regulate doRegulate(String input) {
		RegulateParser parser = new RegulateParser();
		return parser.split(input);
	}

	/**
	 * 解析数据优化时间
	 * @param input 输入语句
	 * @return 返回RegulateTime命令
	 */
	private CreateRegulateTime doCreateRegulateTime(String input) {
		CreateRegulateTimeParser parser = new CreateRegulateTimeParser();
		return parser.split(input);
	}

	/**
	 * 撤销数据优化时间（普通注册用户操作）
	 * @param input 输入语句
	 * @return 返回DropRegulateTime命令
	 */
	private DropRegulateTime doDropRegulateTime(String input) {
		DropRegulateTimeParser parser = new DropRegulateTimeParser();
		return parser.split(input, true);
	}

	/**
	 * 打印数据优化时间（普通注册用户操作）
	 * @param input 输入语句
	 * @return PrintRegulateTime命令
	 */
	private PrintRegulateTime doPrintRegulateTime(String input) {
		PrintRegulateTimeParser parser = new PrintRegulateTimeParser();
		return parser.split(input, true);
	}

	/**
	 * 修改数据块尺寸
	 * @param input 输入语句
	 * @return 返回SetEntitySize命令
	 */
	private SetEntitySize doSetEntitySize(String input) {
		SetEntitySizeParser parser = new SetEntitySizeParser();
		return parser.split(input, true);
	}

	/**
	 * 扫描数据块。流程：FRONT -> CALL -> DATA。
	 * @param input 输入语句
	 * @return 返回ScanEntity命令
	 */
	private ScanEntity doScanEntity(String input) {
		ScanEntityParser parser = new ScanEntityParser();
		return parser.split(input, true);
	}

	/**
	 * 建立限制操作
	 * @param input 输入语句
	 * @return 返回CreateLimit命令
	 */
	private CreateLimit doCreateLimit(String input) {
		CreateLimitParser parser = new CreateLimitParser();
		return parser.split(input, true);
	}

	/**
	 * 删除限制操作
	 * @param input 输入语句
	 * @return 返回DropLimit命令
	 */
	private DropLimit doDropLimit(String input) {
		DropLimitParser parser = new DropLimitParser();
		return parser.split(input, true);
	}

	/**
	 * 显示限制操作单元
	 * @param input 输入语句
	 * @return 返回ShowLimit命令
	 */
	private ShowLimit doShowLimit(String input) {
		ShowLimitParser parser = new ShowLimitParser();
		return parser.split(input);
	}

	/**
	 * 提交锁定操作
	 * @param input 输入语句
	 * @return 返回CreateFault命令
	 */
	private CreateFault doCreateFault(String input) {
		CreateFaultParser parser = new CreateFaultParser();
		return parser.split(input, true);
	}

	/**
	 * 撤销锁定操作
	 * @param input 输入语句
	 * @return 返回DropFault命令
	 */
	private DropFault doDropFault(String input) {
		DropFaultParser parser = new DropFaultParser();
		return parser.split(input, true);
	}

	/**
	 * 显示锁定操作
	 * @param input 输入语句
	 * @return 返回ShowFault命令
	 */
	private ShowFault doShowFault(String input) {
		ShowFaultParser parser = new ShowFaultParser();
		return parser.split(input);
	}

	/**
	 * 显示禁止操作单元
	 * @param input 输入语句
	 * @return 返回ShowForbid命令
	 */
	private ShowForbid doShowForbid(String input) {
		ShowForbidParser parser = new ShowForbidParser();
		return parser.split(input);
	}

	/**
	 * 显示事务规则
	 * @param input 输入语句
	 * @return 返回ShowLockRule命令
	 */
	private ShowLockRule doShowLockRule(String input) {
		ShowLockRuleParser parser = new ShowLockRuleParser();
		return parser.split(input);
	}

//	/**
//	 * 删除快捷组件
//	 * @param input 输入语句
//	 * @return 返回DropSwift命令
//	 */
//	private DropSwift doDropSwift(String input) {
//		DropSwiftParser parser = new DropSwiftParser();
//		return parser.split(input, true);
//	}
//
//	/**
//	 * 打印快捷组件
//	 * @param input 输入语句
//	 * @return 返回PrintSwift命令
//	 */
//	private PrintSwift doPrintSwift(String input) {
//		PrintSwiftParser parser = new PrintSwiftParser();
//		return parser.split(input, true);
//	}
//
//	/**
//	 * 运行快捷组件
//	 * @param input 输入语句
//	 * @return 返回RunSwift命令
//	 */
//	private RunSwift doRunSwift(String input) {
//		RunSwiftParser parser = new RunSwiftParser();
//		return parser.split(input, true);
//	}

	/**
	 * 解析MODULATE命令
	 * @param input 输入语句
	 * @return 返回Modulate命令
	 */
	private Modulate doModulate(String input) {
		ModulateParser parser = new ModulateParser();
		return parser.split(input);
	}

	/**
	 * 解析CONDUCT命令（数据计算命令）
	 * @param input CONDUCT命令的文本描述
	 * @return CONDUCT命令实例
	 */
	private Conduct doConduct(String input) {
		ConductParser parser = new ConductParser();
		return parser.split(input, true);
	}

	/**
	 * 解析ESTABLISH命令（数据构建命令）
	 * @param input ESTABLISH命令的文本描述
	 * @return ESTABLISH命令实例
	 */
	private Establish doEstablish(String input) {
		EstablishParser parser = new EstablishParser();
		return parser.split(input, true);
	}

	/**
	 * 加载索引
	 * @param input 输入语句
	 * @return 返回LoadIndex命令
	 */
	private LoadIndex doLoadIndex(String input) {
		LoadIndexParser parser = new LoadIndexParser();
		return parser.split(input, true);
	}

	/**
	 * 卸载索引
	 * @param input 输入语句
	 * @return 返回StopIndex命令
	 */
	private StopIndex doStopIndex(String input) {
		StopIndexParser parser = new StopIndexParser();
		return parser.split(input, true);
	}

	/**
	 * 加载数据块
	 * @param input 输入语句
	 * @return 返回LoadEntity命令
	 */
	private LoadEntity doLoadEntity(String input) {
		LoadEntityParser parser = new LoadEntityParser();
		return parser.split(input, true);
	}

	/**
	 * 卸载数据块
	 * @param input 输入语句
	 * @return 返回StopEntity命令
	 */
	private StopEntity doStopEntity(String input) {
		StopEntityParser parser = new StopEntityParser();
		return parser.split(input, true);
	}

	/**
	 * 获得全部数据块编号
	 * @param input 输入语句
	 * @return 返回GitStubs命令
	 */
	private GitStubs doGitStubs(String input) {
		GitStubsParser parser = new GitStubsParser();
		return parser.split(input, true);
	}

	/**
	 * 导出数据块命令
	 * @param input 输入语句
	 * @return 返回ExportEntity命令
	 */
	private ExportEntity doExportEntity(String input) {
		ExportEntityParser parser = new ExportEntityParser();
		return parser.split(input, true);
	}

	/**
	 * 导入数据文件命令
	 * @param input 输入语句
	 * @return 返回ImportEntity命令
	 */
	private ImportEntity doImportEntity(String input) {
		ImportEntityParser parser = new ImportEntityParser();
		return parser.split(input, true);
	}

	/**
	 * 数据传输流量测试
	 * @param input 输入语句
	 */
	private Swarm doSwarm(String input) {
		SwarmParser parser = new SwarmParser();
		return parser.split(input, true); // 解析命令
	}

}