/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得坐标范围内账号。<br>
 * HASH站点发出，ACCOUNT站点接收
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeAccountSiger extends Command {

	private static final long serialVersionUID = -5181007192017780125L;

	/** HASH站点节点坐标，从0开始，默认是-1。 **/
	private SiteAxes axes;

	/**
	 * 构造默认的获得坐标范围内账号命令
	 */
	private TakeAccountSiger() {
		super();
	}

	/**
	 * 构造获得坐标范围内账号，指定HASH站点节点坐标和全部HASH站点数目
	 * @param axes HASH站点节点坐标
	 */
	public TakeAccountSiger(SiteAxes axes) {
		this();
		setAxes(axes);
	}

	/**
	 * 生成获得坐标范围内账号的数据副本
	 * @param that 获得坐标范围内账号
	 */
	private TakeAccountSiger(TakeAccountSiger that) {
		super(that);
		axes = that.axes;
	}

	/**
	 * 从可类化数据读取器中解析获得坐标范围内账号
	 * @param reader 可类化数据读取器
	 */
	public TakeAccountSiger(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置节点坐标，不允许空指针
	 * @param e 节点坐标
	 */
	public void setAxes(SiteAxes e) {
		Laxkit.nullabled(e);
		axes = e;
	}
	
	/**
	 * 返回节点坐标
	 * @return 节点坐标
	 */
	public SiteAxes getAxes() {
		return axes;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeAccountSiger duplicate() {
		return new TakeAccountSiger(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(axes);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		axes = new SiteAxes(reader);
	}

}
