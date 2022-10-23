/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 根据SELECT语句，查找关联的数据块编号。<br>
 * 
 * 此命令由WORK站点发出，目标是DATA站点。根据检索命令（SELECT），查询匹配的数据块编号。<br>
 * 
 * @author scott.liang
 * @version 1.1 07/12/2015
 * @since laxcus 1.0
 */
public final class FilteSelectStub extends Command {

	private static final long serialVersionUID = 7444496970430166154L;

	/** SELECT语句 **/
	private Select select;
	
	/**
	 * 构造默认和私有的FilteSelectStub实例
	 */
	private FilteSelectStub() {
		super();
	}

	/**
	 * 根据传入FilteSelectStub实例，生成它的数据副本
	 * @param that FilteSelectStub实例
	 */
	private FilteSelectStub(FilteSelectStub that) {
		super(that);
		select = that.select;
	}

	/**
	 * 构造FilteSelectStub实例，指定SELECT命令
	 * @param cmd Select命令
	 */
	public FilteSelectStub(Select cmd) {
		this();
		setSelect(cmd);
	}

	/**
	 * 从可类化数据读取器中解析FilteSelectStub命令参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FilteSelectStub(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置SELECT语句实例
	 * @param e Select实例
	 */
	public void setSelect(Select e) {
		Laxkit.nullabled(e);

		select = e;
	}
	
	/**
	 * 返回SELECT语句实例
	 * @return Select实例
	 */
	public Select getSelect() {
		return select;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FilteSelectStub duplicate() {
		return new FilteSelectStub(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(select);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		select = new Select(reader);
	}

}