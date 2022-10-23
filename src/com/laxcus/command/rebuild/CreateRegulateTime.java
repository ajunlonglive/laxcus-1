/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 建立数据表优化时间命令。
 * 
 * @author scott.liang
 * @version 1.1 2/2/2014
 * @since laxcus 1.0
 */
public class CreateRegulateTime extends Command {

	private static final long serialVersionUID = 967784163405927999L;

	/** 数据优化触发时间  **/
	private SwitchTime switchTime;

	/**
	 * 根据传入实例生成它的数据副本
	 * @param that RegulateTime实例
	 */
	private CreateRegulateTime(CreateRegulateTime that) {
		super(that);
		switchTime = that.switchTime;
	}

	/**
	 * 构造默认的建立数据表优化时间命令
	 */
	public CreateRegulateTime() {
		super();
	}

	/**
	 * 构造建立数据表优化时间，指定启动时间
	 * @param switchTime SwitchTime实例
	 */
	public CreateRegulateTime(SwitchTime switchTime) {
		this();
		setSwitchTime(switchTime);
	}

	/**
	 * 设置建立数据表优化时间
	 * @param e SwitchTime实例
	 */
	public void setSwitchTime(SwitchTime e) {
		Laxkit.nullabled(e);
		switchTime = e;
	}

	/**
	 * 返回建立数据表优化时间
	 * @return SwitchTime实例
	 */
	public SwitchTime getSwitchTime() {
		return switchTime;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CreateRegulateTime duplicate() {
		return new CreateRegulateTime(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(switchTime);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		switchTime = new SwitchTime(reader);
	}

}