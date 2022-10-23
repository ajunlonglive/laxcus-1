/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.missing;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 磁盘空间不足
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.1
 */
public class DiskMissing extends Command {
	
	private static final long serialVersionUID = -5349739551562338910L;

	/** 磁盘所在的站点地址 **/
	private Node site;
	
	/** 磁盘路径 **/
	private String path;
	
	/** 数据表名（应用于DATA/BUILD站点） **/
	private Space table;

	/**
	 * 构造默认的磁盘空间不足命令
	 */
	public DiskMissing() {
		super();
	}

	public DiskMissing(Node site) {
		this();
		this.setSite(site);
	}
	
	/**
	 * 生成磁盘空间不足命令的数据副本
	 * @param that DiskMissing实例
	 */
	private DiskMissing(DiskMissing that) {
		super(that);
		site = that.site;
		path = that.path;
		table = that.table;
	}

	/**
	 * 构造默认的磁盘空间不足命令，指定目录
	 * @param path 磁盘目录
	 */
	public DiskMissing(String path) {
		this();
		setPath(path);
	}
	
	/**
	 * 构造默认的磁盘空间不足命令，指定表名
	 * @param space 数据表名
	 */
	public DiskMissing(Space space) {
		this();
		setTable(space);
	}

	/**
	 * 从可类化数据读取器中解析磁盘空间不足命令
	 * @param reader 可类化数据读取器
	 */
	public DiskMissing(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		site = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 设置磁盘目录
	 * @param e Path实例
	 */
	public void setPath(String e) {
		path = e;
	}

	/**
	 * 返回磁盘目录
	 * @return 字符串实例
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 设置数据表名。应用于DATA/BUILD站点
	 * @param e Space实例
	 */
	public void setTable(Space e) {
		table = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getTable() {
		return table;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DiskMissing duplicate() {
		return new DiskMissing(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		if (path != null) {
			return String.format("%s # %s", site, path);
		} else if (table != null) {
			return String.format("%s # %s", site, table);
		} else {
			return site.toString();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(site);
		writer.writeString(path);
		writer.writeInstance(table);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = reader.readInstance(Node.class);
		path = reader.readString();
		table = reader.readInstance(Space.class);
	}

}