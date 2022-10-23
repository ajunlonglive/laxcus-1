/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 用户元数据检索结果单元
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public class SeekRegisterMetadataItem extends SeekUserSiteItem {

	private static final long serialVersionUID = 2372091939690168242L;

	/** 本地表名 **/
	private TreeSet<Space> localTables = new TreeSet<Space>();

	/** 本地阶段命名 **/
	private TreeSet<Phase> localPhases = new TreeSet<Phase>();
	
	/** 注册表名 **/
	private TreeSet<RemoteTableItem> remoteTables = new TreeSet<RemoteTableItem>();

	/** 注册阶段名 **/
	private TreeSet<RemotePhaseItem> remotePhases = new TreeSet<RemotePhaseItem>();

	/**
	 * 构造默认的用户元数据检索结果单元
	 */
	private SeekRegisterMetadataItem() {
		super();
	}
	
	/**
	 * 构造用户元数据检索结果单元，指定用户和节点
	 * @param seat 用户位置
	 */
	public SeekRegisterMetadataItem(Seat seat) {
		this();
		setSeat(seat);
	}

	/**
	 * 生成用户元数据检索结果单元的数据副本
	 * @param that 用户元数据检索结果单元
	 */
	private SeekRegisterMetadataItem(SeekRegisterMetadataItem that) {
		super(that);
		localTables.addAll(that.localTables);
		localPhases.addAll(that.localPhases);
	}

	/**
	 * 从可类化数据读取器中解析用户阶段命名检索结果单元
	 * @param reader 可类化数据读取器
	 */
	public SeekRegisterMetadataItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存本地的表名
	 * @param e
	 * @return
	 */
	public boolean addLocalTable(Space e) {
		Laxkit.nullabled(e);
		return localTables.add(e);
	}
	
	/**
	 * 输出本地表名
	 * @return
	 */
	public List<Space> getLocalTables() {
		return new ArrayList<Space>(localTables);
	}
	
	/**
	 * 保存本地的阶段命名
	 * @param e
	 * @return
	 */
	public boolean addLocalPhase(Phase e) {
		Laxkit.nullabled(e);
		return localPhases.add(e);
	}
	
	/**
	 * 输出本地阶段命名
	 * @return
	 */
	public List<Phase> getLocalPhases() {
		return new ArrayList<Phase>(localPhases);
	}
	
	/**
	 * 保存远端数据表名
	 * @param e
	 * @return
	 */
	public boolean addRemoteTable(RemoteTableItem e) {
		Laxkit.nullabled(e);
		return remoteTables.add(e);
	}
	
	/**
	 * 输出本地数据表名
	 * @return
	 */
	public List<RemoteTableItem> getRemoteTables() {
		return new ArrayList<RemoteTableItem>(remoteTables);
	}

	/**
	 * 保存远端阶段命名
	 * @param e
	 * @return
	 */
	public boolean addRemotePhase(RemotePhaseItem e) {
		Laxkit.nullabled(e);
		return remotePhases.add(e);
	}
	
	/**
	 * 输出远端阶段命名
	 * @return
	 */
	public List<RemotePhaseItem> getRemotePhases() {
		return new ArrayList<RemotePhaseItem>(remotePhases);
	}

	/**
	 * 生成数据副本
	 * @return SeekRegisterMetadataItem实例
	 */
	public SeekRegisterMetadataItem duplicate(){
		return new SeekRegisterMetadataItem(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		return duplicate();
	}
	
	/**
	 * 保存参数到可类化写入器
	 * @param writer
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(localTables.size());
		for (Space e : localTables) {
			writer.writeObject(e);
		}
		writer.writeInt(localPhases.size());
		for (Phase e : localPhases) {
			writer.writeObject(e);
		}
		
		writer.writeInt(remoteTables.size());
		for (RemoteTableItem e : remoteTables) {
			writer.writeObject(e);
		}
		writer.writeInt(remotePhases.size());
		for (RemotePhaseItem e : remotePhases) {
			writer.writeObject(e);
		}
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 表名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			localTables.add(e);
		}
		// 阶段命名
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Phase e = new Phase(reader);
			localPhases.add(e);
		}
		
		// 远端数据表名
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RemoteTableItem e = new RemoteTableItem(reader);
			remoteTables.add(e);
		}
		// 远端阶段命名
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RemotePhaseItem e = new RemotePhaseItem(reader);
			remotePhases.add(e);
		}
	}

}