/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.missing;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 许可证超时
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public class LicenceTimeout extends Command {

	private static final long serialVersionUID = -5315970963623469993L;

	/** 许可证超时站点 **/
	private Node site;
	
	/** 天数，默认是0 **/
	private int day;

	/**
	 * 构造默认和私有的许可证超时命令。
	 */
	private LicenceTimeout() {
		super();
		this.day = 0;
	}

	/**
	 * 根据传入的许可证超时命令实例，生成它的数据副本
	 * 
	 * @param that 许可证超时实例
	 */
	private LicenceTimeout(LicenceTimeout that) {
		super(that);
		site = that.site;
		this.day = that.day;
	}

	/**
	 * 从可类化数据读取器中解析推送注销站点命令
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public LicenceTimeout(ClassReader reader) {
		this();
		resolve(reader);
	}


	/**
	 * 构造许可证超时命令实例，指定许可证超时站点
	 * 
	 * @param site 许可证超时站点
	 */
	public LicenceTimeout(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 构造许可证超时命令实例，指定许可证超时站点
	 * 
	 * @param site 许可证超时站点
	 */
	public LicenceTimeout(Node site, int day) {
		this(site);
		setDay(day);
	}
	
	/**
	 * 设置许可证超时站点
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回许可证超时站点
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 设置许可证超时天数
	 * @param value int实例
	 */
	public void setDay(int value) {
		day = value;
	}

	/**
	 * 返回许可证超时天数
	 * @return int实例
	 */
	public int getDay() {
		return day;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
		writer.writeInt(day);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new Node(reader);
		this.day = reader.readInt();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public LicenceTimeout duplicate() {
		return new LicenceTimeout(this);
	}

}
