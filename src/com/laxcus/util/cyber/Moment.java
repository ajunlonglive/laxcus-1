/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.cyber;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 节点瞬时记录
 * 
 * @author scott.liang
 * @version 1.0 10/27/2019
 * @since laxcus 1.0
 */
public class Moment implements Serializable, Classable, Cloneable {

	private static final long serialVersionUID = 8161335400352801777L;

	/** 节点承载成员数，在ACCOUNT/GATE/CALL/DATA/WORK/BUILD **/
	private PersonStamp member;

	/** FRONT在线用户，出现在CALL/GATE节点 **/
	private PersonStamp online;

	/** 虚拟机内存 **/
	private DeviceStamp vmMemory;

	/** 系统内存 **/
	private DeviceStamp sysMemory;

	/** 系统磁盘 **/
	private DeviceStamp sysDisk;

	/**
	 * 构造默认的节点瞬时记录
	 */
	public Moment() {
		super();
	}

	/**
	 * 生成节点瞬时记录副本
	 * @param that 节点瞬时记录
	 */
	private Moment(Moment that) {
		this();
		// 节点承载人数/FRONT在线人数
		member = that.member.duplicate();
		online = that.online.duplicate();
		// 虚拟机内存、系统内存、系统硬盘
		vmMemory = that.vmMemory.duplicate();
		sysMemory = that.sysMemory.duplicate();
		sysDisk = that.sysDisk.duplicate();
	}

	/**
	 * 从可类化读取器解析节点瞬时记录
	 * @param reader 可类化数据读取器
	 */
	public Moment(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置可以支持的最多用户数目。<br>
	 * 在集群里，因为计算机性能的限制，包括：内存/CPU/磁盘，每个节点只能支持有限人数。设置这个参数加以限制。<br>
	 * 这个方法针对人员注册使用的节点，包括：ACCOUNT/GATE/CALL/DATA/WORK/BUILD。<br>
	 * 
	 * @param what 用户数目
	 */
	public void setMember(PersonStamp what) {
		member = what;
	}

	/**
	 * 返回节点允许的最大用户数
	 * @return 整数
	 */
	public PersonStamp getMember() {
		return member;
	}

	/**
	 * 设置FRONT用户数目，出现在GATE/CALL节点
	 * @param what 用户数目
	 */
	public void setOnline(PersonStamp what) {
		online = what;
	}

	/**
	 * 返回FRONT用户数目，出现在GATE/CALL节点
	 * @return 用户数目
	 */
	public PersonStamp getOnline() {
		return online;
	}

	/**
	 * 设置虚拟机内存
	 * @param e Tok实例
	 */
	public void setVMMemory(DeviceStamp e) {
		vmMemory = e;
	}

	/**
	 * 返回虚拟机内存
	 * @return Tok实例
	 */
	public DeviceStamp getVMMemory() {
		return vmMemory;
	}

	/**
	 * 设置系统内存
	 * @param e Tok实例
	 */
	public void setSysMemory(DeviceStamp e) {
		sysMemory = e;
	}

	/**
	 * 返回系统内存
	 * @return Tok实例
	 */
	public DeviceStamp getSysMemory() {
		return sysMemory;
	}

	/**
	 * 设置系统磁盘
	 * @param e Tok实例
	 */
	public void setSysDisk(DeviceStamp e) {
		sysDisk = e;
	}

	/**
	 * 返回系统磁盘
	 * @return Tok实例
	 */
	public DeviceStamp getSysDisk() {
		return sysDisk;
	}

	/**
	 * 生成副本
	 * @return Moment实例
	 */
	public Moment duplicate() {
		return new Moment(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 成员数目
		writer.writeInstance(member);
		// FRONT节点数目
		writer.writeInstance(online);
		// 虚拟机内存、系统内存、系统磁盘
		writer.writeInstance(vmMemory);
		writer.writeInstance(sysMemory);
		writer.writeInstance(sysDisk);

		// 返回写入的数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 最多成员数目
		member = reader.readInstance(PersonStamp.class);
		// FRONT节点数目
		online = reader.readInstance(PersonStamp.class);
		// 虚拟机内存、系统内存、系统磁盘
		vmMemory = reader.readInstance(DeviceStamp.class);
		sysMemory = reader.readInstance(DeviceStamp.class);
		sysDisk = reader.readInstance(DeviceStamp.class);

		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}

}