/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.cast;

import java.util.*;

import com.laxcus.command.access.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 投递“SQL SELECT”命令。
 * 
 * @author scott.liang
 * @version 1.1 7/17/2015
 * @since laxcus 1.0
 */
public class CastSelect extends CastStub {

	private static final long serialVersionUID = -5568501859744295901L;

	/** SQL SELECT语句 **/
	private Select select;

	/**
	 * 根据传入的投递“SQL SELECT”命令，生成它的数据副本
	 * @param that CastSelect实例
	 */
	private CastSelect(CastSelect that) {
		super(that);
		setSelect(that.select);
	}

	/**
	 * 构造默认的投递“SQL SELECT”命令
	 */
	public CastSelect() {
		super();
	}

	/**
	 * 构造投递“SQL SELECT”命令，指定命令和数据块编号
	 * @param cmd SELECT命令
	 * @param stub 数据块编号
	 */
	public CastSelect(Select cmd, long stub) {
		this();
		setSelect(cmd);
		addStub(stub);
	}

	/**
	 * 构造投递“SQL SELECT”命令，指定全部参数
	 * @param select Select命令
	 * @param stubs 数据块编号数组
	 */
	public CastSelect(Select select, List<Long> stubs) {
		this();
		setSelect(select);
		addStubs(stubs);
	}

	/**
	 * 从可类化读取器中解析“POST SELECT”命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CastSelect(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置SQL SELECT命令
	 * @param e Select实例
	 */
	public void setSelect(Select e) {
		Laxkit.nullabled(e);

		select = e;
	}

	/**
	 * 返回SQL SELECT命令
	 * @return Select实例
	 */
	public Select getSelect() {
		return select;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger siger) {
		super.setIssuer(siger);
		if (select != null) {
			select.setIssuer(siger);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CastSelect duplicate() {
		return new CastSelect(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.post.PostStub#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInstance(select);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.post.PostStub#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		select = reader.readInstance(Select.class);
	}
}
