/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.casket;

import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据存取堆栈标识。<br>
 * 见AccessStack中的说明。
 * 
 * @author scott.liang
 * @version 1.1 8/31/2015
 * @since laxcus 1.0
 */
public final class AccessStackFlag {

	/** 处理状态 **/
	byte state;

	/** 错误码 **/
	int fault;

	/** 操作符，包括“插入、撤销、查询、删除”四种 **/
	byte operator;

	/** 操作行为辅助码 **/
	int help;

	/** 数据下标 **/
	long offset;

	/** 数据块编号 **/
	long stub;

	/** 数据块当前状态，分为未封闭/封闭两种状态 **/
	byte stubStatus;

	/** 行数 **/
	int rows;

	/** 列数 **/
	short columns;

	/** 内容长度 **/
	int contentSize;

	/** 映像长度 **/
	int reflexSize;

	/** 表名 **/
	Space space;
	
	/**
	 * 构造默认的数据存取堆栈标识
	 */
	public AccessStackFlag() {
		state = -2;
		fault = 0; // 默认无错误
		stub = 0L; // 默认是0,无效!

		operator = 0;
		help = 0;
		offset = 0L;
		rows = 0;
		columns = 0;
		contentSize = 0;
		reflexSize = 0;
	}
	
	/**
	 * 生成数据存取堆栈标识的数据副本
	 * @param that AccessStackFlag实例
	 */
	private AccessStackFlag(AccessStackFlag that) {
		this();
		state = that.state;
		fault = that.fault;
		stub = that.stub;

		operator = that.operator;
		help = that.help;
		offset = that.offset;
		rows = that.rows;
		columns = that.columns;
		contentSize = that.contentSize;
		reflexSize = that.reflexSize;
		space = that.space;
	}
	
	/**
	 * 从可类化数据读取器中解析数据存取堆栈标识
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AccessStackFlag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置处理状态
	 * 
	 * @param who 状态码
	 */
	void setState(byte who) {
		if (!AccessState.isState(who)) {
			throw new IllegalValueException("illegal state:%d", who);
		}
		state = who;
	}

	/**
	 * 返回处理状态
	 * @return 状态码
	 */
	public byte getState() {
		return state;
	}
	
	/**
	 * 处理状态的字符串描述
	 * @return 返回状态码说明
	 */
	public String getStateText() {
		return AccessState.translate(state);
	}

	/**
	 * 判断操作成功。当数据在内存或者磁盘时，操作成功。
	 * 
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isSuccessful() {
		return isMemory() || isDisk();
	}

	/**
	 * 判断数据在内存
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isMemory() {
		return AccessState.isMemory(state);
	}

	/**
	 * 判断数据在磁盘
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isDisk() {
		return AccessState.isDisk(state);
	}

	/**
	 * 判断没有找到
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isNotFound() {
		return AccessState.isNotFound(state);
	}

	/**
	 * 判断执行过程中发生错误
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isFault() {
		return AccessState.isFault(state);
	}

	/**
	 * 返回JNI错误码
	 * @return JNI错误码
	 */
	public int getFault() {
		return fault;
	}

	/**
	 * 设置JNI错误码。错误码是一个负数
	 * @param who JNI错误码
	 */
	public void setFault(int who) {
		fault = who;
	}

	/**
	 * 设置数据操作符
	 * @param who 数据操作符
	 */
	private void setOperator(byte who) {
		if (!AccessOperator.isOperator(who)) {
			throw new IllegalValueException("illegal operator:%d", who);
		}
		operator = who;
	}

	/**
	 * 返回数据操作符
	 * @return 数据操作符
	 */
	public byte getOperator() {
		return operator;
	}

	/**
	 * 判断是INSERT操作
	 * @return 返回真或者假
	 */
	public boolean isInsert() {
		return AccessOperator.isInsert(operator);
	}

	/**
	 * 判断是SELECT操作
	 * @return 返回真或者假
	 */
	public boolean isSelect() {
		return AccessOperator.isSelect(operator);
	}

	/**
	 * 判断是DELETE操作
	 * @return 返回真或者假
	 */
	public boolean isDelete() {
		return AccessOperator.isDelete(operator);
	}

	/**
	 * 判断是LEAVE操作
	 * @return 返回真或者假
	 */
	public boolean isLeave() {
		return AccessOperator.isLeave(operator);
	}

	/**
	 * 设置辅助码
	 * @param id 辅助码
	 */
	private void setHelp(int id) {
		help = id;
	}

	/**
	 * 返回辅助码
	 * @return 辅助码
	 */
	public int getHelp() {
		return help;
	}

	/**
	 * 判断INSERT填充“满”状态。
	 * @return 成立返回“真”，否则“假”。
	 */
	public boolean isInsertFull() {
		return AccessHelp.isInsertFull(help);
	}

	/**
	 * 设置数据开始下标。内存模式是0，磁盘模式是文件的某个位置
	 * @param off 数据开始下标
	 */
	private void setOffset(long off) {
		offset = off;
	}

	/**
	 * 返回数据开始下标
	 * @return 数据开始下标
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * 设置行数。来自报头
	 * @param size 行数
	 */
	private void setRows(int size) {
		rows = size;
	}

	/**
	 * 返回行数
	 * @return 行数
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * 设置列数
	 * @param size 列数
	 */
	private void setColumns(short size) {
		columns = size;
	}

	/**
	 * 返回列数
	 * @return 列数
	 */
	public short getColumns() {
		return columns;
	}

	/**
	 * 设置内容数据尺寸
	 * @param i 内容数据尺寸
	 */
	private void setContentSize(int i) {
		contentSize = i;
	}

	/**
	 * 返回内容数据尺寸
	 * @return 内容数据尺寸
	 */
	public int getContentSize() {
		return contentSize;
	}

	/**
	 * 判断是空内容
	 * @return 返回真或者假
	 */
	public boolean isEmptyContent() {
		return contentSize == 0;
	}

	/**
	 * 设置映像数据尺寸
	 * @param i 映像数据尺寸
	 */
	private void setReflexSize(int i) {
		reflexSize = i;
	}

	/**
	 * 返回映像数据尺寸
	 * @return 映像数据尺寸
	 */
	public int getReflexSize() {
		return reflexSize;
	}

	/**
	 * 判断是空映像数据
	 * @return 返回真或者假
	 */
	public boolean isEmptyReflex() {
		return reflexSize == 0;
	}

	/**
	 * 设置数据块编号。在“内存/磁盘”两种状态时有效！
	 * @param id 数据块编号
	 */
	private void setStub(long id) {
		stub = id;
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return stub;
	}

	/**
	 * 返回数据块形态（CACHE/CHUNK）
	 * @return 数据块形态，CACHE/CHUNK任意一种
	 */
	public byte getStubStatus() {
		return stubStatus;
	}

	/**
	 * 设置数据块形态（封闭/未封闭）
	 * @param who 数据块形态
	 */
	public void setStubStatus(byte who) {
		if (!MassStatus.isFamily(who)) {
			throw new IllegalValueException("illegal status:%d", who);
		}
		stubStatus = who;
	}

	/**
	 * 判断数据块是缓存状态（CACHE状态）
	 * @return 返回真或者假
	 */
	public boolean isCacheStub() {
		return MassStatus.isCache(stubStatus);
	}

	/**
	 * 判断数据块是固态状态（CHUNK状态）
	 * @return 返回真或者假
	 */
	public boolean isChunkStub() {
		return MassStatus.isChunk(stubStatus);
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	private void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 生成数据副本
	 * @return AccessStackFlag实例
	 */
	public AccessStackFlag duplicate() {
		return new AccessStackFlag(this);
	}

	/**
	 * 解析头数据
	 * @param reader 可类化读取器
	 */
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 处理状态
		setState(reader.read());
		// 操作符
		setOperator(reader.read());
		// 辅助码
		setHelp(reader.readInt());
		// 检查下标
		setOffset(reader.readLong());
		// 设置数据块编号
		setStub(reader.readLong());
		// 数据块形态（CACHE/CHUNK中的任意一种）
		setStubStatus(reader.read());
		// 设置行数
		setRows(reader.readInt());
		// 设置列数
		setColumns(reader.readShort());
		// 内容数据长度
		setContentSize(reader.readInt());
		// 映像数据长度
		setReflexSize(reader.readInt());
		// 解析和设置表名
		setSpace(new Space(reader));

		// 检查数据尺寸
		if (contentSize < 0) {
			throw new IllegalValueException("illegal content size:%d",
					contentSize);
		}
		if (reflexSize < 0) {
			throw new IllegalValueException("illegal reflex size:%d",
					reflexSize);
		}

//		System.out.printf("state is:%d\n", this.getState());
//		System.out.printf("operator is:%d\n", this.getOperator());
//		System.out.printf("help is %d\n", this.getHelp());
//		System.out.printf("offset is %d\n", this.getOffset());
//		System.out.printf("stub is %x\n", this.getStub());
//		System.out.printf("stub status is %d\n", this.getStubStatus());
//		System.out.printf("rows is %d\n", this.getRows());
//		System.out.printf("column is %d\n", this.getColumns());
//		System.out.printf("content size is %d\n", this.getContentSize());
//		System.out.printf("reflex size is %d\n", this.getReflexSize());
//		System.out.printf("space is [%s]\n", getSpace());

		// Logger.debug(this, "prefix", "act is:%d", this.getAct());
		// Logger.debug(this, "prefix", "help is %d", this.getHelp());
		// Logger.debug(this, "prefix", "offset is %d", this.getOffset());
		// Logger.debug(this, "prefix", "stub is %x", this.getStub());
		// Logger.debug(this, "prefix", "rows is %d", this.getRows());
		// Logger.debug(this, "prefix", "column is %d", this.getColumns());
		// Logger.debug(this, "prefix", "content size is %d",
		// this.getContentSize());
		// Logger.debug(this, "prefix", "reflex size is %d", this.getReflexSize());
		
		// 返回解析长度
		return reader.getSeek() - seek;
	}

}
