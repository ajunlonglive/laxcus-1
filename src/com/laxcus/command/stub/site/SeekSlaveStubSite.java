/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.site;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询数据块编号的从站点。<br><br>
 * 
 * 这个命令由DATA主站点发出，目标是CALL站点，检查关联的数据块编号从站点。
 * 这个命令的对应的查询报告是：“SlaveStubSiteProduct”。
 * 
 * @author scott.liang
 * @version 1.1 9/22/1015
 * @since laxcus 1.0
 */
public class SeekSlaveStubSite extends Command {

	private static final long serialVersionUID = 3460020833581136981L;

	/** 数据表名 **/
	private Space space;

	/** 数据块编号 **/
	private Set<Long> stubs = new TreeSet<Long>();

	/**
	 * 构造默认的查询数据块编号的从站点命令
	 */
	public SeekSlaveStubSite() {
		super();
	}

	/**
	 * 构造查询数据块编号从站点命令
	 * @param space 数据表名
	 */
	public SeekSlaveStubSite(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 根据传入实例，生成浅层数据副本
	 * @param that SeekSlaveStubSite实例
	 */
	private SeekSlaveStubSite(SeekSlaveStubSite that) {
		super(that);
		space = that.space;
		stubs.addAll(that.stubs);
	}

	/**
	 * 从可类化数据读取器中解析查询数据块编号从站点命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SeekSlaveStubSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

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
	 * 设置数据块编号
	 * @param e 数据块编号
	 */
	public void addStub(long e) {
		stubs.add(e);
	}

	/**
	 * 输出全部数据块编号
	 * @return 数据块编号列表
	 */
	public List<Long> getStubs() {
		return new ArrayList<Long>(stubs);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekSlaveStubSite duplicate() {
		return new SeekSlaveStubSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
		// 数据块数目
		writer.writeInt(stubs.size());
		for (long stub : stubs) {
			writer.writeLong(stub);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
		// 数据块数目
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			stubs.add(stub);
		}
	}

}