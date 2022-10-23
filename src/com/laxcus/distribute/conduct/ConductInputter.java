/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.conduct;

import com.laxcus.distribute.*;
import com.laxcus.util.classable.*;

/**
 * CONDUCT阶段对象的参数输入器。<br>
 * 
 * 输入器中的参数来源于“图形终端/控制台/驱动程序”三个界面的用户输入，参数在“INIT”阶段使用。
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public abstract class ConductInputter extends AccessObject {

	private static final long serialVersionUID = 4527711971537630223L;

	/** 指定的DATA/WORK节点数，默认是0不指定。此参数可以操作界面中定义 **/
	private int sites;
	
	/**
	 * 构造一个默认的输入对象
	 */
	protected ConductInputter() {
		super();
		sites = 0;
	}

	/**
	 * 根据传入的输入对象，生成它的副本
	 * @param that ConductInputter实例
	 */
	protected ConductInputter(ConductInputter that) {
		super(that);
		sites = that.sites;
	}

	/**
	 * 设置目标站点数目
	 * @param i 目标站点数目
	 */
	public void setSites(int i) {
		sites = i;
	}

	/**
	 * 返回目标站点数目
	 * @return 目标站点数目
	 */
	public int getSites() {
		return sites;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.AccessObject#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 生成前缀
		super.buildSuffix(writer);
		// 写入节点数目
		writer.writeInt(sites);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.AccessObject#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀
		super.resolveSuffix(reader);
		// 节点数量
		sites = reader.readInt();
	}

}