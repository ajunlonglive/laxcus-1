/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 云磁盘
 * 
 * @author scott.liang
 * @version 1.0 10/24/2021
 * @since laxcus 1.0
 */
public final class SDisk extends SDirectory {

	private static final long serialVersionUID = 8743116005615479371L;

	/** 系统规定的磁盘容量 **/
	private long capacity;

	//	/** 子目录 **/
	//	private ArrayList<SDirectory> dirs = new ArrayList<SDirectory>();
	//
	//	/** 子文件 **/
	//	private ArrayList<SFile> files = new ArrayList<SFile>();

	/**
	 * 构造默认的云磁盘
	 */
	public SDisk() {
		super();
		path = "/";
	}

	/**
	 * 构造云磁盘，指定本地的根目录
	 * @param root 根目录
	 */
	public SDisk(File root) {
		this();
		setRoot(root);
	}

	/**
	 * 从可类化读取中解析云磁盘
	 * @param reader 可类化读取器
	 */
	public SDisk(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成云磁盘副本
	 * @param that 云磁盘
	 */
	protected SDisk(SDisk that) {
		super(that);
		//		dirs.addAll(that.dirs);
		//		files.addAll(that.files);
	}

	/**
	 * 构造磁盘容量
	 * @param l
	 */
	public void setCapacity(long l) {
		this.capacity = l;
	}

	/**
	 * 返回磁盘容量
	 * @return
	 */
	public long getCapacity() {
		return this.capacity;
	}

	//	/**
	//	 * 保存目录
	//	 * @param dir
	//	 */
	//	public void add(SDirectory dir) {
	//		Laxkit.nullabled(dir);
	//		dirs.add(dir);
	//	}
	//
	//	/**
	//	 * 保存文件
	//	 * @param file
	//	 */
	//	public void add(SFile file) {
	//		Laxkit.nullabled(file);
	//		files.add(file);
	//	}
	//
	//	public List<SDirectory> getDirectoies() {
	//		return new ArrayList<SDirectory>(dirs);
	//	}
	//
	//	public List<SFile> getFiles() {
	//		return new ArrayList<SFile>(files);
	//	}
	//
	//	public void clear() {
	//		dirs.clear();
	//		files.clear();
	//	}

	/*
	 * 覆盖上级方法，不要改变任何值
	 * @see com.laxcus.command.cloud.store.SElement#setPath(java.lang.String)
	 */
	@Override
	public void setPath(String s){

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.store.SDirectory#duplicate()
	 */
	@Override
	public SDisk duplicate() {
		return new SDisk(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.cloud.store.SElement#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(capacity);
		//		// 目录
		//		writer.writeInt(dirs.size());
		//		for(SDirectory dir : dirs) {
		//			writer.writeObject(dir);
		//		}
		//		// 文件
		//		writer.writeInt(files.size());
		//		for(SFile file : files) {
		//			writer.writeObject(file);
		//		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.cloud.store.SElement#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		this.capacity = reader.readLong();

		//		// 目录
		//		int size = reader.readInt();
		//		for (int i = 0; i < size; i++) {
		//			SDirectory dir = new SDirectory(reader);
		//			dirs.add(dir);
		//		}
		//		// 文件
		//		size = reader.readInt();
		//		for (int i = 0; i < size; i++) {
		//			SFile file = new SFile(reader);
		//			files.add(file);
		//		}
	}

}