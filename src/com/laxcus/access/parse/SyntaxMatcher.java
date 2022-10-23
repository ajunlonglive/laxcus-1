/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

/**
 * LAXCUS分布式命令语法检查器 <br><br>
 * 
 * 根据输入的字符串，判断分布描述语言（DDL）命令合法。SQL是分布描述语言命令的一个子集。<br>
 * 
 * @author scott.liang
 * @version 1.0 9/6//2022
 * @since laxcus 1.0
 */
public final class SyntaxMatcher {

	/**
	 * 构造LAXCUS分布式命令语法检查器
	 */
	public SyntaxMatcher() {
		super();
	}

	/**
	 * 检查是否匹配"CREATE DATABASE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isCreateSchema(String input) {
		CreateSchemaParser parser = new CreateSchemaParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"DROP DATABASE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isDropSchema(String input) {
		DropSchemaParser parser = new DropSchemaParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"ASSERT DATABASE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isAssertSchema(String input) {
		AssertSchemaParser parser = new AssertSchemaParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"SHOW DATABASE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isShowSchema(String input) {
		ShowSchemaParser parser = new ShowSchemaParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配"CREATE USER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isCreateUser(String input) {
		// 检查语法是否匹配
		CreateUserParser parser = new CreateUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查匹配"DROP USER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isDropUser(String input) {
		DropUserParser parser = new DropUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"ASSERT TABLE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isAssertTable(String input) {
		AssertTableParser parser = new AssertTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查匹配"CLOSE USER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isCloseUser(String input) {
		CloseUserParser parser = new CloseUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查匹配"OPEN USER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isOpenUser(String input) {
		OpenUserParser parser = new OpenUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"ALTER USER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isAlterUser(String input) {
		AlterUserParser parser = new AlterUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"ASSERT USER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isAssertUser(String input) {
		AssertUserParser parser = new AssertUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查权限设置 "GRANT ..."语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isGrant(String input) {
		GrantParser parser = new GrantParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查回收权限"REVOKE ..."语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isRevoke(String input) {
		RevokeParser parser = new RevokeParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"CREATE TABLE ..."语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isCreateTable(String input) {
		CreateTableParser parser = new CreateTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"DROP TABLE..."语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isDropTable(String input) {
		DropTableParser parser = new DropTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"SHOW TABLE ..."语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isShowTable(String input) {
		ShowTableParser parser = new ShowTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"CHECK REMOTE TABLE ..."语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isCheckRemoteTable(String input) {
		CheckRemoteTableParser parser = new CheckRemoteTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"CHECK REMOTE TASK ..."语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isCheckRemoteTask(String input) {
		CheckRemoteTaskParser parser = new CheckRemoteTaskParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配“SCAN TABLE”语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isScanTable(String input) {
		ScanTableParser parser = new ScanTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配“SCAN DATABASE”语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isScanSchema(String input) {
		ScanSchemaParser parser = new ScanSchemaParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"SELECT ... FROM ... WHERE "语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isSelect(String input, boolean online) {
		SelectParser parser = new SelectParser();
		return parser.matches(true, input);
	}

	/**
	 * 测试JOIN语句，成功返回TRUE，失败返回FALSE
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isJoin(String input) {
		JoinParser parser = new JoinParser();
		return parser.matches(input);
	}

	/**
	 * 测试"DELETE FROM"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isDelete(String input, boolean online) {
		DeleteParser parser = new DeleteParser();
		return parser.matches(true, input);
	}

	/**
	 * 测试"INSERT INTO"语句
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isInsert(String input, boolean online) {
		InsertParser parser = new InsertParser();
		return parser.isInsert(input);
	}

	/**
	 * 测试"INJECT INTO"语句
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isInject(String input, boolean online) {
		InsertParser parser = new InsertParser();
		return parser.isInject(input);
	}

	/**
	 * 测试"INJECT INTO ... SELECT "语句。<br>
	 * 在SELECT查询基础之上实现插入数据。
	 * 
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isInjectSelect(String input, boolean online) {
		InjectSelectParser parser = new InjectSelectParser();
		return parser.matches(input);
	}

	/**
	 * 测试"UPDATE ... SET"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isUpdate(String input, boolean online) {
		UpdateParser parser = new UpdateParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是"CONTACT..."语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isContact(String input) {
		ContactParser parser = new ContactParser();
		return parser.matches(true, input);
	}

	/**
	 * 测试数据计算的CONDUCT语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isConduct(String input) {
		ConductParser parser = new ConductParser();
		return parser.matches(true, input);
	}

	/**
	 * 测试数据构建ESTABLISH语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isEstablish(String input) {
		EstablishParser parser = new EstablishParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断并且解析数据块尺寸
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isSetEntitySize(String input) {
		SetEntitySizeParser parser = new SetEntitySizeParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"SHOW ENTITY SIZE ..."语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isShowEntitySize(String input) {
		ShowEntitySizeParser parser = new ShowEntitySizeParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"SCAN ENTITY  ..."语句。用于FRONT站点。
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isScanEntity(String input) {
		ScanEntityParser parser = new ScanEntityParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"SCAN ENTITY  ..."语句。用于WATCH站点。
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isScanEntityWithWatch(String input) {
		ScanEntityWithWatchParser parser = new ScanEntityWithWatchParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是建立数据优化时间
	 * @param input 语法格式: CREATE REGULATE TIME 数据库名.表名 (HOURLY|DAILY|WEEKLY|MONTHLY) '时间格式' [ORDER BY 列名]
	 * @return 返回真或者假
	 */
	public boolean isCreateRegulateTime(String input) {
		CreateRegulateTimeParser parser = new CreateRegulateTimeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是撤销数据优化时间
	 * @param input 语法格式: DROP REGULATE TIME 数据库名.表名
	 * @return 返回真或者假
	 */
	public boolean isDropRegulateTime(String input) {
		DropRegulateTimeParser parser = new DropRegulateTimeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是显示数据优化时间
	 * @param input 语法格式: PRINT REGULATE TIME 数据库名.表名, ...
	 * @return 返回真或者假
	 */
	public boolean isPrintRegulateTime(String input) {
		PrintRegulateTimeParser parser = new PrintRegulateTimeParser();
		return parser.matches(true, input);
	}

	/**
	 * 检测数据优化语句
	 * @param input 语法格式: regulate schema.table [order by [column-name]] [to [ip address...]]
	 * @return 返回真或者假
	 */
	public boolean isRegulate(String input) {
		RegulateParser parser = new RegulateParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是数据调整语句
	 * @param input 语法格式：modulate schema.table | modulate schema.table/column
	 * @return 返回真或者假
	 */
	public boolean isModulate(String input) {
		ModulateParser parser = new ModulateParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是数据块强制转换语句（只限WATCH站点使用）
	 * @param input 语句格式：RUSH 数据库.表 [TO [DATA主机地址, ...]]
	 * @return 返回真或者假
	 */
	public boolean isRush(String input) {
		RushParser parser = new RushParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是删除缓存数据块过期数据命令（只限WATCH站点使用）
	 * @param input 语句格式：COMPACT 数据库.表 [TO [DATA主机地址, ...]]
	 * @return 返回真或者假
	 */
	public boolean isCompact(String input) {
		CompactParser parser = new CompactParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置DSN表压缩倍数（只限WATCH站点使用）
	 * @param input 语句格式：SET DSM REDUCE 数据库.表  倍数 [TO [DATA主机地址, ...]]
	 * @return 返回真或者假
	 */
	public boolean isSetDSMReduce(String input) {
		SetDSMReduceParser parser = new SetDSMReduceParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是远程关闭语句
	 * @param input 语句格式：SHUTDOWN ALL | CALL://128.2.34.9:900_900, WORK://192.168.1.25:900_900
	 * @return 返回真或者假
	 */
	public boolean isShutdown(String input) {
		ShutdownParser parser = new ShutdownParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是重装加载远程节点的动态链接库（只限WATCH站点使用）
	 * @param input 语句格式：RELOAD DYNAMIC LIBRARY TO [ALL|节点地址]
	 * @return 返回真或者假
	 */
	public boolean isReloadLibrary(String input) {
		ReloadLibraryParser parser = new ReloadLibraryParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是重新设置节点的安全策略（只限WATCH站点使用）
	 * @param input 语句格式：RELOAD SECURITY POLICY TO [ALL|节点地址]
	 * @return 返回真或者假
	 */
	public boolean isReloadSecurityPolicy(String input) {
		ReloadSecurityPolicyParser parser = new ReloadSecurityPolicyParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是重新加载许可证（只限WATCH站点使用）
	 * @param input 语句格式：RELOAD LICENCE TO [ALL|节点地址]
	 * @return 返回真或者假
	 */
	public boolean isReloadLicence(String input) {
		ReloadLicenceParser parser = new ReloadLicenceParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是发布许可证（只限WATCH站点使用）
	 * @param input 语句格式：PUBLIC LICENCE xxx TO [ALL|节点地址]
	 * @return 返回真或者假
	 */
	public boolean isPublishLicence(String input) {
		PublishLicenceParser parser = new PublishLicenceParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是释放节点内存（只限WATCH站点使用）
	 * @param input 语句格式：RELEASE MEMORY TO [ALL|节点地址]
	 * @return 返回真或者假
	 */
	public boolean isReleaseMemory(String input) {
		ReleaseMemoryParser parser = new ReleaseMemoryParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是释放节点内存间隔时间（只限WATCH站点使用）
	 * @param input 语句格式：SET RELEASE MEMORY INTERVAL XXX[H|M|S) TO [ALL|节点地址]
	 * @return 返回真或者假
	 */
	public boolean isReleaseMemoryInterval(String input) {
		ReleaseMemoryIntervalParser parser = new ReleaseMemoryIntervalParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置异步数据传输模式
	 * @param input 语句格式：SET REPLY PACKET MODEL SERIAL|PARALLEL TO [节点地址|ALL|LOCAL]
	 * @return 返回真或者假
	 */
	public boolean isReplyPacketMode(String input) {
		ReplyPacketModeParser parser = new ReplyPacketModeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置应用包尺寸
	 * @param input 语句格式：SET REPLY PACKET SIZE 包集尺寸 子包尺寸 TO [节点地址|ALL|LOCAL]
	 * @return 返回真或者假
	 */
	public boolean isReplyPacketSize(String input) {
		ReplyPacketSizeParser parser = new ReplyPacketSizeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置发送异步数据超时
	 * @param input 语句格式：SET REPLY SEND TIMEOUT FIXP包失效超时 FIXP子包超时 FIXP子包发送间隔 TO [节点地址|ALL|LOCAL]
	 * @return 返回真或者假
	 */
	public boolean isReplySendTimeout(String input) {
		ReplySendTimeoutParser parser = new ReplySendTimeoutParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置接收异步数据超时时间
	 * @param input 语句格式：SET REPLY RECEIVE TIMEOUT  FIXP包失效超时 FIXP子包超时 TO [节点地址|ALL|LOCAL]
	 * @return 返回真或者假
	 */
	public boolean isReplyReceiveTimeout(String input) {
		ReplyReceiveTimeoutParser parser = new ReplyReceiveTimeoutParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置异步应答流量控制参数
	 * @param input 语句格式：SET REPLY FLOW CONTROL -BLOCK 队列成员数 -TIMESLICE UDP操作时间片 TO [节点地址|ALL|LOCAL]
	 * @return 返回真或者假
	 */
	public boolean isReplyFlowControl(String input) {
		ReplyFlowControlParser parser = new ReplyFlowControlParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是扫描堆栈命令（只限WATCH站点使用）
	 * @param input 语句格式：SCAN COMMAND STACK 间隔时间 TO [ALL|节点地址] | EXIT COMMAND STACK FROM [ALL|节点地址]
	 * @return 返回真或者假
	 */
	public boolean isScanCommandStack(String input) {
		ScanCommandStackParser parser = new ScanCommandStackParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是刷新注册用户命令
	 * @param input 语句格式：REFRESH USER [SIGN [xdigit]], [text username]
	 * @return 返回真或者假
	 */
	public boolean isRefreshUser(String input) {
		RefreshUserParser parser = new RefreshUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是刷新元数据命令
	 * @param input 语句格式：REFRESH METADATA [SIGN [xdigit]], [text username]
	 * @return 返回真或者假
	 */
	public boolean isRefreshMetadata(String input) {
		RefreshMetadataParser parser = new RefreshMetadataParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是定时扫描用户关联的间隔时间解析器
	 * @param input 语句格式：SET SCAN LINK TIME [数字] S|M|H|秒|分|小时|时 [REFRESH]
	 * @return 返回真或者假
	 */
	public boolean isScanLinkTime(String input) {
		ScanLinkTimeParser parser = new ScanLinkTimeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是强制刷新站点注册命令
	 * @param input 语句格式： REFRESH LOGIN TO [ALL | other site address]
	 * @return 返回真或者假
	 */
	public boolean isRefreshLogin(String input) {
		RefreshLoginParser parser = new RefreshLoginParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"LOAD INDEX"语句
	 * @param input 语法格式: LOAD INDEX schema.table [TO address, address, ...]
	 * @return 返回真或者假
	 */
	public boolean isLoadIndex(String input) {
		LoadIndexParser parser = new LoadIndexParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"STOP INDEX"语句
	 * @param input 语法格式: STOP INDEX|UNLOAD INDEX schema.table [FROM address, address, ...]
	 * @return 返回真或者假
	 */
	public boolean isStopIndex(String input) {
		StopIndexParser parser = new StopIndexParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"LOAD ENTITY"语句
	 * @param input 语法格式: LOAD ENTITY schema.table [TO address, address, ...]
	 * @return 返回真或者假
	 */
	public boolean isLoadEntity(String input) {
		LoadEntityParser parser = new LoadEntityParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"STOP ENTITY|UNLOAD ENTITY"语句
	 * @param input 语法格式: STOP ENTITY|UNLOAD ENTITY schema.table [FROM address, address, ...]
	 * @return 返回真或者假
	 */
	public boolean isStopEntity(String input) {
		StopEntityParser parser = new StopEntityParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是检查表数据一致性命令
	 * @param input 语句格式：CHECK ENTITY CONSISTENCY 数据库.表
	 * @return 返回真或者假
	 */
	public boolean isCheckEntityConsistency(String input) {
		CheckEntityConsistencyParser parser = new CheckEntityConsistencyParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是恢复表数据一致性命令
	 * @param input 语句格式：RECOVER ENTITY CONSISTENCY 数据库.表
	 * @return 返回真或者假
	 */
	public boolean isRecoverEntityConsistency(String input) {
		RecoverEntityConsistencyParser parser = new RecoverEntityConsistencyParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是检测表分布数据容量命令
	 * @param input 语句格式：SCAN SKETCH 数据库名.表名
	 * @return 返回真或者假
	 */
	public boolean isScanSketch(String input) {
		ScanSketchParser parser = new ScanSketchParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配检索FRONT站点分布命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isSeekFrontUser(String input) {
		SeekFrontUserParser parser = new SeekFrontUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配检索用户站点分布命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isSeekUserSite(String input) {
		SeekUserSiteParser parser = new SeekUserSiteParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配检索用户数据表分布命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isSeekUserTable(String input) {
		SeekUserTableParser parser = new SeekUserTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配检索用户阶段命名分布命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isSeekUserTask(String input) {
		SeekUserTaskParser parser = new SeekUserTaskParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“build SHA1|SHA256|SHA512|MD5 [CASE|NOT CASE] 文本”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isBuildHash(String input) {
		BuildHashParser parser = new BuildHashParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“ENCODE HALF [CASE|NOT CASE] 文本 | DECODE HALF 文本”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isBuildHalf(String input) {
		BuildHalfParser parser = new BuildHalfParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“BUILD EACH SIGN [CASE|NOT CASE] 文本”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isBuildEach(String input) {
		BuildEachParser parser = new BuildEachParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET COMMAND MODE [DISK|MEMORY]”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCommandMode(String input) {
		CommandModeParser parser = new CommandModeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET COMMAND TIMEOUT [DIGIT][H|M|S|HOUR|MINUTE|SECOND]”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCommandTimeout(String input) {
		CommandTimeoutParser parser = new CommandTimeoutParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET COMMAND PRIORITY [NONE|MIN|NORMAL|MAX|FAST]”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCommandRank(String input) {
		CommandRankParser parser = new CommandRankParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET CIPHER TIMEOUT [DIGIT][H|M|S|HOUR|MINUTE|SECOND]”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCipherTimeout(String input) {
		CipherTimeoutParser parser = new CipherTimeoutParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET ECHO BUFFER [DIGIT][K|M|G] TO [LOCAL|ALL|SITE,...]
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isMaxEchoBuffer(String input) {
		MaxEchoBufferParser parser = new MaxEchoBufferParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET MAX INVOKER [DIGIT] TO [LOCAL|ALL|SITE,...]
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isMaxInvoker(String input) {
		MaxInvokerParser parser = new MaxInvokerParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET REFLECT PORT ...
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetReflectPort(String input) {
		ReflectPortParser parser = new ReflectPortParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET LOG ELEMENTS "
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isMaxLogElements(String input) {
		MaxLogElementsParser parser = new MaxLogElementsParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SEEK FRONT SITE TO ALL | CALL SITE or GATE SITE ...
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSeekFrontSite(String input) {
		SeekFrontSiteParser parser = new SeekFrontSiteParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是检索分布任务组件命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSeekTask(String input) {
		SeekTaskParser parser = new SeekTaskParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是建立限制操作规则命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCreateLimit(String input) {
		CreateLimitParser parser = new CreateLimitParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是删除限制操作规则命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDropLimit(String input) {
		DropLimitParser parser = new DropLimitParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是显示限制操作语句
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isShowLimit(String input) {
		ShowLimitParser parser = new ShowLimitParser();
		return parser.matches(input);
	}

	/**
	 * 判断是发送锁定操作语句
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCreateFault(String input) {
		CreateFaultParser parser = new CreateFaultParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是撤销锁定操作语句
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDropFault(String input) {
		DropFaultParser parser = new DropFaultParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是显示锁定操作语句
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isShowFault(String input) {
		ShowFaultParser parser = new ShowFaultParser();
		return parser.matches(input);
	}

	/**
	 * 判断是显示禁止操作单元语句
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isShowForbid(String input) {
		ShowForbidParser parser = new ShowForbidParser();
		return parser.matches(input);
	}

	/**
	 * 判断是显示事务规则语句
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isShowLockRule(String input) {
		ShowLockRuleParser parser = new ShowLockRuleParser();
		return parser.matches(input);
	}

	/**
	 * 判断是设置最大并行任务数
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxJobs(String input) {
		SetMaxJobsParser parser = new SetMaxJobsParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置最大连接数
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxMembers(String input) {
		SetMaxMembersParser parser = new SetMaxMembersParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是开放数据库共享命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isOpenShareSchema(String input) {
		OpenShareSchemaParser parser = new OpenShareSchemaParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是开放数据表共享命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isOpenShareTable(String input) {
		OpenShareTableParser parser = new OpenShareTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是关闭数据库共享命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCloseShareSchema(String input) {
		CloseShareSchemaParser parser = new CloseShareSchemaParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是关闭数据表共享命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCloseShareTable(String input) {
		CloseShareTableParser parser = new CloseShareTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是“显示授权人开放出来的数据资源”命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isShowOpenResource(String input) {
		ShowOpenResourceParser parser = new ShowOpenResourceParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是“被授权人显示授权开放给自己的数据资源”命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isShowPassiveResource(String input) {
		ShowPassiveResourceParser parser = new ShowPassiveResourceParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置日志级别命令
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetLogLevel(String input) {
		SetLogLevelParser parser = new SetLogLevelParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是重新加载和发布自定义包（只限WATCH站点使用）
	 * @param input 语句格式：RELOAD CUSTOM PACKAGE TO [ALL| site address]
	 * @return 返回真或者假
	 */
	public boolean isReloadCustom(String input) {
		ReloadCustomParser parser = new ReloadCustomParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是获取数据块编号命令
	 * @param input 输入语句：GIT ALL STUBS 数据库.表
	 * @return 返回真或者假
	 */
	public boolean isGitStubs(String input) {
		GitStubsParser parser = new GitStubsParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是获取数据块编号命令
	 * @param input 输入语句：PRINT ENTITY STUBS DIAGRAM 数据库.表
	 * @return 返回真或者假
	 */
	public boolean isPrintStubsDiagram(String input) {
		PrintStubsDiagramParser parser = new PrintStubsDiagramParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是获取数据块数据命令
	 * @param input 输入语句：EXPORT ENTITY 数据库.表  数据块编号 TO 磁盘文件  TYPE [CSV|TXT] CHARSET [GBK|UTF8|UTF16|UTF32]
	 * @return 返回真或者假
	 */
	public boolean isExportEntity(String input) {
		ExportEntityParser parser = new ExportEntityParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是从文件导入数据到集群的命令
	 * @param input 输入语句：IMPORT ENTITY 数据库.表  FROM 磁盘文件  TYPE [CSV|TXT] CHARSET [GBK|UTF8|UTF16|UTF32] SECTION 单次读取行数
	 * @return 返回真或者假
	 */
	public boolean isImportEntity(String input) {
		ImportEntityParser parser = new ImportEntityParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是获取数据块数据命令
	 * @param input 输入语句：COPY ENTITY 数据库.表  数据块编号 FROM xxx TO xxx
	 * @return 返回真或者假
	 */
	public boolean isCopyEntity(String input) {
		CopyEntityParser parser = new CopyEntityParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是检测导入内容命令
	 * @param input 输入语句：CHECK ENTITY CONTENT 数据库.表  FROM 磁盘文件  TYPE [CSV|TXT] CHARSET [GBK|UTF8|UTF16|UTF32] 
	 * @return 返回真或者假
	 */
	public boolean isCheckEntityContent(String input) {
		CheckEntityContentParser parser = new CheckEntityContentParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是检测文件编码命令
	 * @param input 输入语句：CHECK ENTITY CHARSET 文件路径
	 * @return 返回真或者假
	 */
	public boolean isCheckEntityCharset(String input) {
		CheckEntityCharsetParser parser = new CheckEntityCharsetParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是显示数据库状态
	 * @param input 输入语句：PRINT DATABASE DIAGRAM 数据库, ...
	 * @return 返回真或者假
	 */
	public boolean isPrintSchemaDiagram(String input) {
		PrintSchemaDiagramParser parser = new PrintSchemaDiagramParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是显示数据表状态
	 * @param input 输入语句：PRINT TABLE DIAGRAM 数据库.表 , ...
	 * @return 返回真或者假
	 */
	public boolean isPrintTableDiagram(String input) {
		PrintTableDiagramParser parser = new PrintTableDiagramParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是注册用户状态
	 * @param input 输入语句：PRINT USER DIAGRAM 用户名, 用户签名 , ...
	 * @return 返回真或者假
	 */
	public boolean isPrintUserDiagram(String input) {
		PrintUserDiagramParser parser = new PrintUserDiagramParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是注册用户授权状态
	 * @param input 输入语句：PRINT GRANT DIAGRAM 用户名, 用户签名 , ...
	 * @return 返回真或者假
	 */
	public boolean isPrintGrantDiagram(String input) {
		PrintGrantDiagramParser parser = new PrintGrantDiagramParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是打印FRONT网关（CALL节点地址）
	 * @param input 输入语句：PRINT FRONT GATEWAY
	 * @return 返回真或者假
	 */
	public boolean isPrintFrontGateway(String input) {
		PrintFrontGatewayParser parser = new PrintFrontGatewayParser();
		return parser.matches(input);
	}

	/**
	 * 判断是设置最多应用软件数
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxTasks(String input) {
		SetMaxTasksParser parser = new SetMaxTasksParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置最大优化表数目
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxRegulates(String input) {
		SetMaxRegulatesParser parser = new SetMaxRegulatesParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置最大磁盘空间
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxSize(String input) {
		SetMaxSizeParser parser = new SetMaxSizeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置最大HOME子域集群
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxGroups(String input) {
		SetMaxGroupsParser parser = new SetMaxGroupsParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置最大网关数目
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxGateways(String input) {
		SetMaxGatewaysParser parser = new SetMaxGatewaysParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置WORK节点数目
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxWorkers(String input) {
		SetMaxWorkersParser parser = new SetMaxWorkersParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置最大BUILD节点数目
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxBuilders(String input) {
		SetMaxBuildersParser parser = new SetMaxBuildersParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断用户账号到期
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetExpireTime(String input) {
		SetExpireTimeParser parser = new SetExpireTimeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置最大表数目
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxTables(String input) {
		SetMaxTablesParser parser = new SetMaxTablesParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置最大索引数目
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMaxIndexes(String input) {
		SetMaxIndexesParser parser = new SetMaxIndexesParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置中间缓存数目
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetMiddleBuffer(String input) {
		SetMiddleBufferParser parser = new SetMiddleBufferParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置云存储数目
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetCloudSize(String input) {
		SetCloudSizeParser parser = new SetCloudSizeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置用户权级
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSetUserPriority(String input) {
		SetUserPriorityParser parser = new SetUserPriorityParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是“SCAN USER LOG”命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isScanUserLog(String input) {
		ScanUserLogParser parser = new ScanUserLogParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET OUTLOOK INTERVAL [DIGIT][H|M|S|HOUR|MINUTE|SECOND]”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isOutlookInterval(String input) {
		OutlookIntervalParser parser = new OutlookIntervalParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SEEK ONLINE COMMAND TO 节点...”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isSeekOnlineCommand(String input) {
		SeekOnlineCommandParser parser = new SeekOnlineCommandParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SEEK ONLINE RESOURCE TO 节点...”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isSeekOnlineResource(String input) {
		SeekOnlineResourceParser parser = new SeekOnlineResourceParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SEEK REGISTER METADATA 用户名|SIGN 用户签名”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isSeekRegisterMetadata(String input) {
		SeekRegisterMetadataParser parser = new SeekRegisterMetadataParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SEEK USER AREA 用户名|SIGN 用户签名”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isSeekUserArea(String input) {
		SeekUserAreaParser parser = new SeekUserAreaParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SWARM 数据尺寸 数据单元 TO 站点地址”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isSwarm(String input) {
		SwarmParser parser = new SwarmParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“MULTI SWARM 数据尺寸 数据包尺寸 子包尺寸 发送间隔 TO 站点地址 发送次数”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isMultiSwarm(String input) {
		MultiSwarmParser parser = new MultiSwarmParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“PARALLEL MULTI SWARM 数据尺寸 数据包尺寸 子包尺寸 发送间隔 TO 站点地址 发送次数 ITERATE 并行次数 ”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isParallelMultiSwarm(String input) {
		ParallelMultiSwarmParser parser = new ParallelMultiSwarmParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“GUST 数据尺寸 数据单元 FROM 站点地址 TO 站点地址”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isGust(String input) {
		GustParser parser = new GustParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“MULTI GUST 数据尺寸 数据单元 FROM 多个站点地址 TO 多个站点地址”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isMultiGust(String input) {
		MultiGustParser parser = new MultiGustParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“PARALLEL MULTI GUST 数据尺寸 数据包尺寸 子包尺寸 发送间隔 FROM 多个站点地址 TO 多个站点地址 ITERATE 并行次数 ”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isParallelMultiGust(String input) {
		ParallelMultiGustParser parser = new ParallelMultiGustParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断连通检测命令
	 * @param input 输入命令
	 * @return 返回真或者假
	 */
	public boolean isRing(String input) {
		RingParser parser = new RingParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"REFRESH SYBER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isRefreshCyber(String input) {
		RefreshCyberParser parser = new RefreshCyberParser();
		return parser.matches(input);
	}

	/**
	 * 检查是否匹配"DEPLOY USER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isDeployUser(String input) {
		DeployUserParser parser = new DeployUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"ERASE USER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isEraselUser(String input) {
		EraseUserParser parser = new EraseUserParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"SET MAX DSM REDUCE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isSetMaxDSMReduce(String input) {
		SetMaxDSMReduceParser parser = new SetMaxDSMReduceParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"DEPLOY TABLE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isDeployTable(String input) {
		DeployTableParser parser = new DeployTableParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"COPY MASTER MASS"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isCopyMasterMass(String input) {
		CopyMasterMassParser parser = new CopyMasterMassParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"RUN TUB SERVICE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isRunTubService(String input) {
		RunTubServiceParser parser = new RunTubServiceParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"STOP TUB SERVICE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isStopTubService(String input) {
		StopTubServiceParser parser = new StopTubServiceParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"PRINT TUB SERVICE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isPrintTubService(String input) {
		PrintTubServiceParser parser = new PrintTubServiceParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是否匹配“SHOW TUB CONTAINER ...”语句，显示边缘端容器实例
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isShowTubContainer(String input) {
		ShowTubContainerParser parser = new ShowTubContainerParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"CHECK TUB LISTENER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isCheckTubListener(String input) {
		CheckTubListenerParser parser = new CheckTubListenerParser();
		return parser.matches(input);
	}

	/**
	 * 检查是否匹配"SET SHADOW MODE"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isShadowMode(String input) {
		ShadowModeParser parser = new ShadowModeParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"CHECK SHADOW CONSISTENCY"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isCheckShadowConsistency(String input) {
		CheckShadowConsistencyParser parser = new CheckShadowConsistencyParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET MOST CPU 12.23% TO ALL|LOCAL|OTHER SITE...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isMostCPU(String input) {
		MostCPUParser parser = new MostCPUParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET MOST VM Memory 60.2% TO ALL|LOCAL|OTHER SITE...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isMostVMMemory(String input) {
		MostVMMemoryParser parser = new MostVMMemoryParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET LEAST MEMORY UNLIMIT | 123M | 100M”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isLeastMemory(String input) {
		LeastMemoryParser parser = new LeastMemoryParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET LEAST DISK UNLIMIT | 123M | 100M”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isLeastDisk(String input) {
		LeastDiskParser parser = new LeastDiskParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“CHECK SITE PATH TO LOCAL|ALL|other site, ...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCheckSitePath(String input) {
		CheckSitePathParser parser = new CheckSitePathParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SET DISTRIBUTED TIMEOUT 时间 TO [LOCAL|ALL|other site...]”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDistributedTimeout(String input) {
		DistributedTimeoutParser parser = new DistributedTimeoutParser();
		return parser.matches(input);
	}

	/**
	 * 判断匹配语句：“CHECK LOCAL TASK 组件名称, ...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCheckLocalTask(String input) {
		CheckLocalTaskParser parser = new CheckLocalTaskParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“CHECK REMOTE SITE ”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCheckRemoteSite(String input) {
		CheckRemoteSiteParser parser = new CheckRemoteSiteParser();
		return parser.matches(input);
	}

	/**
	 * 判断匹配语句：“CHECK JOB SITE ”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCheckJobSite(String input) {
		CheckJobSiteParser parser = new CheckJobSiteParser();
		return parser.matches(input);
	}
	
	/**
	 * 判断匹配语句：“OPEN WARNING ...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isEnableWarning(String input) {
		EnableWarningParser parser = new EnableWarningParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“OPEN FAULT ...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isEnableFault(String input) {
		EnableFaultParser parser = new EnableFaultParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“CLOSE WARNING ...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDisableWarning(String input) {
		DisableWarningParser parser = new DisableWarningParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“CLOSE FAULT ...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDisableFault(String input) {
		DisableFaultParser parser = new DisableFaultParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"SET MEMBER CYBER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isSetMemberCyber(String input) {
		SetMemberCyberParser parser = new SetMemberCyberParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"SET FRONT CYBER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isSetFrontCyber(String input) {
		SetFrontCyberParser parser = new SetFrontCyberParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查是否匹配"CHECK USER CYBER"语句
	 * @param input 输入语句
	 * @return 如果匹配返回“真”，否则“假”。
	 */
	public boolean isCheckUserCyber(String input) {
		CheckUserCyberParser parser = new CheckUserCyberParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“OPEN TIGGER COMMAND,MESSAGE,WARNING,ERROR,FATAL TO ALL|LOCAL|OTHER SITE...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isOpenTigger(String input) {
		OpenTiggerParser parser = new OpenTiggerParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“CLOSE TIGGER COMMAND,MESSAGE,WARNING,ERROR,FATAL TO ALL|LOCAL|OTHER SITE...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCloseTigger(String input) {
		CloseTiggerParser parser = new CloseTiggerParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“SEEK CLOUD WARE ALL | SEEK CLOUD WARE tasks ... scalers ... swifts ...”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isSeekCloudWare(String input) {
		SeekCloudWareParser parser = new SeekCloudWareParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“CHECK DISTRIBUTED MEMBER xxx, xxx, xxx”
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCheckDistributedMember(String input) {
		CheckDistributedMemberParser parser = new CheckDistributedMemberParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："Build Conduct Package ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isBuildConductPackage(String input) {
		BuildConductPackageParser parser = new BuildConductPackageParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："Build Establish Package ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isBuildEstablishPackage(String input) {
		BuildEstablishPackageParser parser = new BuildEstablishPackageParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："Build Contact Package ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isBuildContactPackage(String input) {
		BuildContactPackageParser parser = new BuildContactPackageParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："Deploy Conduct Package ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDeployConductPackage(String input) {
		DeployConductPackageParser parser = new DeployConductPackageParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："Deploy Establish Package ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDeployEstablishPackage(String input) {
		DeployEstablishPackageParser parser = new DeployEstablishPackageParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："Deploy Contact Package ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDeployContactPackage(String input) {
		DeployContactPackageParser parser = new DeployContactPackageParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："Drop Conduct Package ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDropConductPackage(String input) {
		DropConductPackageParser parser = new DropConductPackageParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："Drop Establish Package ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDropEstablishPackage(String input) {
		DropEstablishPackageParser parser = new DropEstablishPackageParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："Drop Contact Package ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isDropContactPackage(String input) {
		DropContactPackageParser parser = new DropContactPackageParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句："RUN DAPP ..."
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isRunTask(String input) {
		RunTaskParser parser = new RunTaskParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断匹配语句：“CHECK SYSTEM INFO TO [LOCAL|ALL|SITE,...]
	 * @param input 字符串命令
	 * @return 返回真或者假
	 */
	public boolean isCheckSystemInfo(String input) {
		CheckSystemInfoParser parser = new CheckSystemInfoParser();
		return parser.matches(true, input);
	}

	/**
	 * 检查"CHECK POCK CHANNEL"语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isCheckPockChannel(String input) {
		CheckPockChannelParser parser = new CheckPockChannelParser();
		return parser.matches(input);
	}

	/**
	 * 检查"CHECK MASSIVE MIMO"语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean isCheckMassiveMimo(String input) {
		CheckMassiveMimoParser parser = new CheckMassiveMimoParser();
		return parser.matches(true, input);
	}
	
	/**
	 * 判断是建立密钥令牌（只限WATCH站点使用）
	 * @param input 语句格式：CREATE SECURE TOKEN [参数] TO [ALL| 节点地址]
	 * @return 返回真或者假
	 */
	public boolean isCreateSecureToken(String input) {
		CreateSecureTokenParser parser = new CreateSecureTokenParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是删除密钥令牌（只限WATCH站点使用）
	 * @param input 语句格式：FLUSH SECURE TOKEN TO [ALL| 节点地址]
	 * @return 返回真或者假
	 */
	public boolean isDropSecureToken(String input) {
		DropSecureTokenParser parser = new DropSecureTokenParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是显示密钥令牌（只限WATCH站点使用）
	 * @param input 语句格式：SHOW SECURE TOKEN FROM [ALL| 节点地址]
	 * @return 返回真或者假
	 */
	public boolean isShowSecureToken(String input) {
		ShowSecureTokenParser parser = new ShowSecureTokenParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是输出密钥令牌（只限WATCH站点使用）
	 * @param input 语句格式：FLUSH SECURE TOKEN TO [ALL| 节点地址]
	 * @return 返回真或者假
	 */
	public boolean isFlushSecureToken(String input) {
		FlushSecureTokenParser parser = new FlushSecureTokenParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是设置对称密钥长度（只限WATCH站点使用）
	 * @param input 语句格式：SET SECURE SIZE -client xxx -server xxx TO [ALL| 节点地址]
	 * @return 返回真或者假
	 */
	public boolean isSetSecureSize(String input) {
		SetSecureSizeParser parser = new SetSecureSizeParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是建立云存储目录（只限FRONT节点使用）
	 * @param input 语句格式：CREATE DIRECTORY
	 * @return 返回真或者假
	 */
	public boolean isCreateCloudDirectory(String input) {
		CreateDirectoryParser parser = new CreateDirectoryParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是删除云端目录（只限FRONT节点使用）
	 * @param input 语句格式：DROP DIRECTORY
	 * @return 返回真或者假
	 */
	public boolean isDropCloudDirectory(String input) {
		DropCloudDirectoryParser parser = new DropCloudDirectoryParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是删除云端文件（只限FRONT节点使用）
	 * @param input 语句格式：DROP DIRECTORY
	 * @return 返回真或者假
	 */
	public boolean isDropCloudFile(String input) {
		DropCloudFileParser parser = new DropCloudFileParser();
		return parser.matches(true, input);
	}
	
	/**
	 * 判断是修改云端目录（只限FRONT节点使用）
	 * @param input 语句格式：RENAME CLOUD DIRECTORY
	 * @return 返回真或者假
	 */
	public boolean isRenameCloudDirectory(String input) {
		RenameCloudDirectoryParser parser = new RenameCloudDirectoryParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是修改云端文件（只限FRONT节点使用）
	 * @param input 语句格式：RENAME CLOUD DIRECTORY
	 * @return 返回真或者假
	 */
	public boolean isRenameCloudFile(String input) {
		RenameCloudFileParser parser = new RenameCloudFileParser();
		return parser.matches(true, input);
	}
	
	/**
	 * 判断是上传文件（只限FRONT节点使用）
	 * @param input 语句格式：UPLOAD FILE
	 * @return 返回真或者假
	 */
	public boolean isUploadCloudFile(String input) {
		UploadCloudFileParser parser = new UploadCloudFileParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是下载文件（只限FRONT节点使用）
	 * @param input 语句格式：DOWNLOAD FILE
	 * @return 返回真或者假
	 */
	public boolean isDownloadCloudFile(String input) {
		DownloadCloudFileParser parser = new DownloadCloudFileParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断是扫描云端磁盘（只限FRONT节点使用）
	 * @param input 语句格式：SCAN CLOUD DIRECTORY
	 * @return 返回真或者假
	 */
	public boolean isScanCloudDirectory(String input) {
		ScanCloudDirectoryParser parser = new ScanCloudDirectoryParser();
		return parser.matches(true, input);
	}

	/**
	 * 判断用户消耗资源
	 * @param input 输入命令
	 * @return 返回真或者假
	 */
	public boolean isCheckUserCost(String input) {
		CheckUserCostParser parser = new CheckUserCostParser();
		return parser.matches(true, input);
	}

}