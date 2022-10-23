/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.sign;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获取数据块签名命令 <br>
 * 
 * 此命令由CALL站点发出，目标是全部关联的DATA站点。DATA站点返回这个表下属的全部数据块签名
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public final class TakeSign extends Command {

	private static final long serialVersionUID = -8533696552394785671L;
	
	/** 数据表名 **/
	private Space space;

	/**
	 * 根据传入的获取数据块签名命令，生成它的数据副本
	 * @param that TakeSign实例
	 */
	private TakeSign(TakeSign that) {
		super(that);
		space = that.space;
	}

	/**
	 * 构造默认和私有的获取数据块签名命令。
	 */
	private TakeSign() {
		super();
	}

	/**
	 * 构造获取数据块签名命令，指定数据表名。
	 * @param space 数据表名
	 */
	public TakeSign(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析获取数据块签名命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeSign(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名，不允许空指针。
	 * @param e Space实例
	 * @throws NullPointerException
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeSign duplicate() {
		return new TakeSign(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
	}

}