/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.cast;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 投递“SQL UPDATE”命令。
 * 
 * @author scott.liang
 * @version 1.1 7/17/2015
 * @since laxcus 1.0
 */
public class CastUpdate extends CastStub {

	private static final long serialVersionUID = 3439951007997071572L;

	/** SQL UPDATE语句 **/
	private Update update;

	/** 如果snatch是“真”时，服务端需要向客户端返回被删除的数据内容 */
	private boolean snatch;

	/**
	 * 构造默认的投递“SQL UPDATE”命令
	 */
	public CastUpdate() {
		super();
		snatch = false;
	}

	/**
	 * 构造投递“SQL UPDATE”命令，指定全部参数
	 * @param cmd Update命令
	 * @param stubs 数据块编号集合
	 */
	public CastUpdate(Update cmd, List<Long> stubs) {
		this();
		setUpdate(cmd);
		addStubs(stubs);
	}

	/**
	 * 从可类化读取器中解析“POST UPDATE”命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CastUpdate(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的投递“SQL UPDATE”命令，生成它的数据副本
	 * @param that CastUpdate实例
	 */
	private CastUpdate(CastUpdate that) {
		super(that);
		snatch = that.snatch;
		update = that.update;
	}

	/**
	 * 设置snatch状态
	 * @param b snatch状态
	 */
	public void setSnatch(boolean b) {
		snatch  = b;
	}

	/**
	 * 判断是snatch状态
	 * @return 返回真或者假
	 */
	public boolean isSnatch() {
		return snatch ;
	}

	/**
	 * 设置SQL UPDATE命令
	 * @param e Update实例
	 */
	public void setUpdate(Update e) {
		update = e;
	}

	/**
	 * 返回SQL UPDATE命令
	 * @return Update实例
	 */
	public Update getUpdate() {
		return update;
	}

	/**
	 * 返回对应的数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return update.getSpace();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger siger) {
		super.setIssuer(siger);
		if (update != null) {
			update.setIssuer(siger);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CastUpdate duplicate() {
		return new CastUpdate(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.post.PostStub#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 获取标识
		writer.writeBoolean(snatch);
		// UPDATE实例
		writer.writeInstance(update);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.post.PostStub#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 获取标识
		snatch = reader.readBoolean();
		// UPDATE标识
		update = reader.readInstance(Update.class);
	}
}