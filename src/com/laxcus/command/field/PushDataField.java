/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 推送DATA站点资源元数据命令。<br>
 * 
 * DATA站点是收到HOME站点的“SelectFieldToCall”或者“FindDataField”命令通知后，发送“PushDataField”命令给CALL站点
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public final class PushDataField extends PushField {

	private static final long serialVersionUID = -5167124006660576739L;

	/** 元数据集合 **/
	private Map<Space, StubTable> tables = new TreeMap<Space, StubTable>();
	
	/**
	 * 构造默认和私有的推送DATA站点元数据命令
	 */
	private PushDataField() {
		super();
	}

	/**
	 * 根据传入的DATA站点元数据授权命令，生成它的数据副本
	 * @param that 推送DATA站点的元数据命令实例
	 */
	private PushDataField(PushDataField that) {
		super(that);
		tables.putAll(that.tables);
	}

	/**
	 * 构造推送DATA站点的元数据命令，指定它们的参数
	 * @param node 命令来源地址
	 */
	public PushDataField(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析DATA元数据命令
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public PushDataField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回站点级别
	 * @return 站点级别
	 */
	public byte getRank() {
		return getNode().getRank();
	}

	/**
	 * 判断是主站点
	 * @return 返回真或者假
	 */
	public boolean isMaster() {
		return getNode().isMaster();
	}

	/**
	 * 判断是从站点
	 * @return 返回真或者假
	 */
	public boolean isSlave() {
		return getNode().isSlave(); 
	}

	/**
	 * 保存索引表，不允许空指针
	 * @param e StubTable实例
	 * @return 返回真或者假
	 */
	public boolean add(StubTable e) {
		Laxkit.nullabled(e);

		return tables.put(e.getSpace(), e) == null;
	}

	/**
	 * 根据数据表名，查找索引表
	 * @param space 数据表名
	 * @return 返回StubTable实例
	 */
	public StubTable find(Space space) {
		return tables.get(space);
	}

	/**
	 * 输出全部索引表
	 * @return StubTable列表
	 */
	public List<StubTable> getStubTables() {
		return new ArrayList<StubTable>(tables.values());
	}

	/**
	 * 统计成员数目
	 * @return 成员数目
	 */
	public int size() {
		return tables.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PushDataField duplicate() {
		return new PushDataField(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 成员数目
		writer.writeInt(tables.size());
		// 写入成员
		for (StubTable e : tables.values()) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 数据成员
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			StubTable e = new StubTable(reader);
			tables.put(e.getSpace(), e);
		}
	}

}