/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 收集分布站点<br>
 * 
 * 这个命令由WATCH站点发出，目标是TOP/HOME站点。TOP/HOME收集本地注册的下属站点地址，并反馈给WATCH站点。<br>
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public class AskSite extends Command {

	private static final long serialVersionUID = 1889802790655593791L;

	/**
	 * 构造默认的收集分布站点命令
	 */
	public AskSite() {
		super();
	}
	
	/**
	 * 构造默认的收集分布站点命令
	 */
	public AskSite(boolean sound) {
		this();
		setSound(sound);
	}
	
	/**
	 * 从可类化数据读取器中解析申请分布站点命令
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public AskSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入实例生成数据副本
	 * @param that AskSite实例
	 */
	private AskSite(AskSite that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AskSite duplicate() {
		return new AskSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub

	}

}