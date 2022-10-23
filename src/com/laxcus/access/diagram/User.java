/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.markable.*;

/**
 * 普通用户账号。<br>
 * 为保证安全，账号必须由管理员和等同管理员身份的用户建立。<br><br>
 * 
 * 普通用户账号除基本的用户名和密码，还包括以下基本参数：<br>
 * 1. 最大可用磁盘空间数。达到这个限制，用户不能再写入数据。<br>
 * 2. 最大连接数。一个账号允许有多少个客户端成员（FRONT SITE）进入系统。<br>
 * 3. 最大并行任务数。一个账号同时运行的任务数目（多个FRONT客户端在GATE的统计）。<br>
 * 4. 最大表数目。允许账号在系统上建立多少表，达到这个参数将拒绝，默认是0不限制。<br>
 * 5. 最大索引数目。用户在建表时，允许每个表的最多索引数目。默认是5，0是不限制。<br>
 * 6. 最大集群数目。每次建表时，可以分配到多少个HOME集群，默认是1。<br>
 * 7. 最大网关数目。运行过程中，可以分配到多少个CALL站点，默认是3。<br>
 * 8. WORK节点数目。<br>
 * 9. BUILD节点数目。<br>
 * 10. 最大分布式应用软件数目。默认是10个。<br>
 * 11. 最大定时优化表数目。默认3个。<br>
 * 12. 云存储空间 <br>
 * 13. 用户命令的权级，包括NONE, MIN, NORMAL, MAX, 见CommandPriority中的定义 <br>
 * <br>
 * 
 * 说明：<br>
 * 1. 通过表(Max Tables)和索引(Indexes)数目，加上数据块的尺寸（CHUNK SIZE），可以判断每个账号的最大元数据空间尺寸。这些参数为分配到CALL站点时提供必要的依据。<br><br>
 * 
 * 系统规定，普通用户进入数据操作前，必须得到管理员或者等同管理员身份的用户授权。否则系统将拒绝执行。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public final class User extends SHAUser {

	private static final long serialVersionUID = -3103094904553978877L;

	/** 用户名称的明文。默认是空 **/
	private String plainText;

	/** 允许的最大可用磁盘空间尺寸。默认是0，不限制 **/
	private long maxsize;

	/** FRONT站点最大登录数目，默认是1个。**/
	private int members;
	
	/** 最大并行任务数目，默认32。这个参数在Gate站点中实时检测。 **/
	private int jobs;
	
	/** 分布式应用软件数目 **/
	private int tasks;
	
	/** 最多定时触发工作 **/
	private int regulates;

	/** 允许建立的最多表数目。默认0，不限制 **/
	private int tables;
	
	/** 每个表的最大索引数目。默认是5，0是不限制。索引太多，底层分支要同步增加。**/
	private int indexes;

	/** 建表时的最大HOME集群数目。默认是1 **/
	private int groups;
	
	/** 网关节点（CALL节点）数目。运行过程中，HOME节点根据这个参数，分配尽可能足够的CALL节点，默认是1。**/
	private int gateways;
	
	/** WORK节点数目 **/
	private int workers;
	
	/** BUILD节点数目 **/
	private int builders;
	
	/** DATA主节点数目 **/
	private int bases;
	
	/** DATA从节点数目 **/
	private int subBases;
	
	/** 数据块尺寸。默认是64M **/
	private int chunksize;
	
	/** 用户账号建立时间，由TOP站点设置 **/
	private long createTime;
	
	/** 到期时间，默认是0 **/
	private long expireTime;
	
	/** 被强制关闭 **/
	private boolean closed;
	
	/** 中间缓存尺寸 **/
	private long middleBuffer;
	
	/** 云存储空间尺寸 **/
	private long cloudSize;
	
	/** 用户的操作权级，对应CommandPriority中的NONE, MIN, NORMAL, MAX **/
	private int priority;

	/**
	 * 根据传入的用户账号，生成它的数据副本
	 * @param that User实例
	 */
	private User(User that) {
		super(that);
		plainText = that.plainText;
		maxsize = that.maxsize;
		members = that.members;
		jobs = that.jobs;
		tasks = that.tasks;
		regulates = that.regulates;
		tables = that.tables;
		indexes = that.indexes;
		groups = that.groups;
		gateways = that.gateways;
		workers = that.workers;
		builders = that.builders;
		bases = that.bases;
		subBases = that.subBases;
		chunksize = that.chunksize;
		createTime = that.createTime;
		// 到期时间
		expireTime = that.expireTime;
		// 账号处于关闭状态
		closed = that.closed;
		// 中间缓存尺寸
		middleBuffer = that.middleBuffer;
		// 云存储空间
		cloudSize = that.cloudSize;
		// 用户权级
		priority = that.priority;
	}

	/**
	 * 生成一个空的用户账号
	 */
	public User() {
		super();
		plainText = null;
		maxsize = 0L;
		members = 1;
		jobs = 32; // 默认32个并行任务
		tasks = 10; // 分布式应用，默认10个
		regulates = 3; // 最大3个
		tables = 0;
		indexes = 5; // 默认每个表5个索引
		groups = 1;
		gateways = 1; // call 网关，默认1个
		workers = 1;	// worker，默认1个
		builders = 1;	// builder，默认1个
		bases = 1; // 默认1个
		subBases = 1; // 默认1个
		chunksize = 0x4000000;
		setCreateTime(SimpleTimestamp.currentTimeMillis());
		
		// 默认是0，没有期限
		expireTime = 0;
		// 默认是假，处于开放状态
		closed = false;
		// 默认是0，不定义
		middleBuffer = 0;
		// 云存在空间，默认是100M
		cloudSize = 1024 * 1024 * 100;
		// 用户权级
		priority = CommandPriority.NONE;
	}

	/**
	 * 生成一个用户账号，同时指定的用户名称和密码
	 * @param username 用户名称转为小写后，使用SHA256签名
	 * @param password 密码，直接SHA512签名
	 */
	public User(String username, String password) {
		this();
		setTextUsername(username);
		setTextPassword(password);
	}

	/**
	 * 生成一个用户账号，只指定用户的名称
	 * @param username 用户名称转为小写后，使用SHA256签名
	 */
	public User(String username) {
		this();
		setTextUsername(username);
	}

	/**
	 * 生成一个用户账号，只指定用户的名称
	 * @param username 用户签名，是SHA256散列码
	 */
	public User(Siger username) {
		this();
		setUsername(username);
	}
	
	/**
	 * 生成一个用户账号，同时指定的用户名称和密码
	 * @param username 用户名称，SHA256散列码
	 * @param password 密码，SHA512散列码
	 */
	public User(Siger username, SHA512Hash password) {
		this();
		setUsername(username);
		setPassword(password);
	}

	/**
	 * 从可类化数据读取器中解析普通用户账号信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public User(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出注册用户参数
	 * @param reader 标记化读取器
	 */
	public User(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置用户名称的明文，允许空指针
	 * @param e 明文
	 */
	public void setPlainText(String e) {
		plainText = e;
	}

	/**
	 * 返回用户名称的明文
	 * @return 字符串
	 */
	public String getPlainText() {
		return plainText;
	}
	
	/**
	 * 设置可使用最大空间
	 * @param i 可使用最大空间
	 */
	public void setMaxSize(long i) {
		maxsize = i;
	}

	/**
	 * 返回可使用最大空间
	 * @return 可使用最大空间
	 */
	public long getMaxSize() {
		return maxsize;
	}

	/**
	 * 设置FRONT站点最大登录数目
	 * @param max FRONT站点最大登录数目
	 */
	public void setMembers(int max) {
		if (max < 1) {
			throw new IllegalValueException("illegal %d", max);
		}
		members = max;
	}

	/**
	 * 返回FRONT站点最大登录数目
	 * @return FRONT站点最大登录数目
	 */
	public int getMembers() {
		return members;
	}

	/**
	 * 设置最大并行任务数目。<br>
	 * 一个账号，不同FRONT站点发出的命令在Gate站点汇集，检查它们的事务规则冲突，同时也检查账号的最大运行任务数目，超过则拒绝。
	 * 
	 * @param max 最大连接数
	 */
	public void setJobs(int max) {
		if (max < 1) {
			throw new IllegalValueException("illegal %d", max);
		}
		jobs = max;
	}

	/**
	 * 返回允许的最大并行工作数
	 * @return 最大并行工作数
	 */
	public int getJobs() {
		return jobs;
	}

	/**
	 * 设置最大分布式应用软件数目
	 * @param i 分布式应用软件数目
	 */
	public void setTasks(int i) {
		if (i < 0) {
			throw new IllegalValueException("illegal %d", i);
		}
		tasks = i;
	}

	/**
	 * 返回最大分布式应用软件数目
	 * @return 最大分布式应用软件数目
	 */
	public int getTasks() {
		return tasks;
	}

	/**
	 * 设置最大定时优化表数目
	 * @param i 最大定时优化表数目
	 */
	public void setRegulates(int i) {
		if (i < 0) {
			throw new IllegalValueException("illegal %d", i);
		}
		regulates = i;
	}

	/**
	 * 返回最大定时优化表数目
	 * @return 最大定时优化表数目
	 */
	public int getRegulates() {
		return regulates;
	}
	
	/**
	 * 设置最大表数目
	 * @param i 最大表数目
	 */
	public void setTables(int i) {
		if (i < 0) {
			throw new IllegalValueException("illegal %d", i);
		}
		tables = i;
	}

	/**
	 * 返回最大表数目
	 * @return 最大表数目
	 */
	public int getTables() {
		return tables;
	}
	
	
	/**
	 * 设置每个表的最大索引数
	 * @param i 每个表的最大索引数
	 */
	public void setIndexes(int i) {
		if (i < 0) {
			throw new IllegalValueException("illegal index %d", i);
		}
		indexes = i;
	}
	
	/**
	 * 返回每个表的最大索引数目
	 * @return 每个表的最大索引数
	 */
	public int getIndexes() {
		return indexes;
	}

	/**
	 * 设置最大集群数目。在建表时检查
	 * @param i 集群数目
	 */
	public void setGroups(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal groups %d", i);
		}
		groups = i;
	}

	/**
	 * 返回最大集群数目
	 * @return 集群数目
	 */
	public int getGroups() {
		return groups;
	}

	/**
	 * 设置网关数目（CALL站点）
	 * @param i 网关数目
	 */
	public void setGateways(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		gateways = i;
	}

	/***
	 * 返回网关数目（CALL站点）
	 * @return 网关数目
	 */
	public int getGateways() {
		return gateways;
	}

	/**
	 * 设置WORK节点数目
	 * @param i WORK节点数目
	 */
	public void setWorkers(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		workers = i;
	}

	/***
	 * 返回WORK节点数目
	 * @return WORK节点数目
	 */
	public int getWorkers() {
		return workers;
	}

	/**
	 * 设置BUILD节点数目
	 * @param i BUILD节点数目
	 */
	public void setBuilders(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		builders = i;
	}

	/***
	 * 返回BUILD节点数目
	 * @return BUILD节点数目
	 */
	public int getBuilders() {
		return builders;
	}

	/**
	 * 设置DATA主节点数目
	 * @param i DATA主节点数目
	 */
	public void setBases(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		bases = i;
	}

	/***
	 * 返回DATA主节点数目
	 * @return DATA主节点数目
	 */
	public int getBases() {
		return bases;
	}

	/**
	 * 设置DATA从节点数目
	 * @param i DATA从节点数目
	 */
	public void setSubBases(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		subBases = i;
	}

	/***
	 * 返回DATA从节点数目
	 * @return DATA从节点数目
	 */
	public int getSubBases() {
		return subBases;
	}

	/**
	 * 设置数据块尺寸。以字节计
	 * @param size 数据块尺寸
	 */
	public void setChunkSize(int size) {
		chunksize = size;
	}

	/**
	 * 返回数据块尺寸
	 * @return 数据块尺寸
	 */
	public int getChunkSize() {
		return chunksize;
	}

	/**
	 * 设置数据库建立时间
	 * @param i 数据库建立时间
	 */
	public void setCreateTime(long i){
		createTime = i;
	}
	
	/**
	 * 返回数据库建立时间
	 * @return 数据库建立时间
	 */
	public long getCreateTime(){
		return createTime;
	}
	
	/**
	 * 设置账号到期时间
	 * @param i 到期时间
	 */
	public void setExpireTime(long i){
		expireTime = i;
	}
	
	/**
	 * 设置账号到期时间
	 * @param d 到期时间
	 */
	public void setExpireTime(java.util.Date d) {
		Laxkit.nullabled(d);
		expireTime = SimpleTimestamp.format(d);
	}
	
	/**
	 * 返回账号到期时间
	 * @return 长整型
	 */
	public long getExpireTime() {
		return expireTime;
	}
	
	/**
	 * 返回账号到期日期
	 * @return 返回Date实例，如果没有定义，返回空指针
	 */
	public java.util.Date getExpireDate() {
		if (expireTime < 1) {
			return null;
		}
		return SimpleTimestamp.format(expireTime);
	}
	
	/**
	 * 判断账号到期
	 * @return 返回真或者假
	 */
	public boolean isExpireTime() {
		return expireTime > 0
				&& System.currentTimeMillis() >= SimpleTimestamp.format(expireTime).getTime();
	}

	/**
	 * 设置为关闭状态
	 * @param b
	 */
	public void setClosed(boolean b) {
		closed = b;
	}

	/**
	 * 判断为关闭状态。如果返回真，这个账号被封闭，不可用！
	 * @return 返回真或者假
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * 判断账号为开放状态。如果返回真，这个账号可用
	 * @return 返回真或者假
	 */
	public boolean isOpening() {
		return !isClosed();
	}
	
	/**
	 * 判断账号处于禁用状态，两种可能：
	 * 1. 账号被禁用
	 * 2. 使用时间到期。
	 * 
	 * @return 返回真或者假
	 */
	public boolean isDisabled() {
		return isClosed() || isExpireTime();
	}

	/**
	 * 判断处于有效状态
	 * @return 返回真或者假
	 */
	public boolean isEnabled() {
		return !isDisabled();
	}

	/**
	 * 设置缓存中间数据尺寸。以字节计
	 * @param size 缓存中间数据尺寸
	 */
	public void setMiddleBuffer(long size) {
		middleBuffer = size;
	}

	/**
	 * 返回缓存中间数据尺寸
	 * @return 缓存中间数据尺寸
	 */
	public long getMiddleBuffer() {
		return middleBuffer;
	}

	/**
	 * 云存储空间尺寸
	 * @param size
	 */
	public void setCloudSize(long size) {
		cloudSize = size;
	}

	/**
	 * 返回云存储空间尺寸
	 * @return
	 */
	public long getCloudSize() {
		return cloudSize;
	}

	/**
	 * 用户权级
	 * @param who
	 */
	public void setPriority(int who) {
		priority = who;
	}

	/**
	 * 返回用户权级
	 * @return
	 */
	public int getPriority() {
		return priority;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#duplicate()
	 */
	@Override
	public User duplicate() {
		return new User(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeString(plainText);
		writer.writeLong(maxsize);
		writer.writeInt(members);
		writer.writeInt(jobs);
		writer.writeInt(tasks);
		writer.writeInt(regulates);
		writer.writeInt(tables);
		writer.writeInt(indexes);
		writer.writeInt(groups);
		writer.writeInt(gateways);
		writer.writeInt(workers);
		writer.writeInt(builders);
		writer.writeInt(bases);
		writer.writeInt(subBases);
		writer.writeInt(chunksize);
		writer.writeLong(createTime);
		// 到期时间
		writer.writeLong(expireTime);
		// 关闭状态
		writer.writeBoolean(closed);
		// 中间缓存
		writer.writeLong(middleBuffer);
		// 云存储空间尺寸
		writer.writeLong(cloudSize);
		// 用户权级
		writer.writeInt(priority);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		plainText = reader.readString();
		maxsize = reader.readLong();
		members = reader.readInt();
		jobs = reader.readInt();
		tasks = reader.readInt();
		regulates = reader.readInt();
		tables = reader.readInt();
		indexes = reader.readInt();
		groups = reader.readInt();
		gateways = reader.readInt();
		workers = reader.readInt();
		builders = reader.readInt();
		bases = reader.readInt();
		subBases = reader.readInt();
		chunksize = reader.readInt();
		createTime = reader.readLong();
		// 到期时间
		expireTime = reader.readLong();
		// 关闭状态
		closed = reader.readBoolean();
		// 中间缓存尺寸
		middleBuffer = reader.readLong();
		// 云存储空间尺寸
		cloudSize = reader.readLong();
		// 用户权级
		priority = reader.readInt();
	}

}