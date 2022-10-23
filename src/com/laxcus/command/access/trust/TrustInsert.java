/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.trust;

import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * INSERT代理命令。<br>
 * 只在主DATA站点使用
 * 
 * @author scott.liang
 * @version 1.0 9/14/2017
 * @since laxcus 1.0
 */
public class TrustInsert extends Command {
	
	private static final long serialVersionUID = 2275793784433512821L;

	/** INSERT命令 **/
	private Insert insert;

	/**
	 * 构造默认的INSERT代理命令
	 */
	private TrustInsert() {
		super();
	}

	/**
	 * 生成INSERT代理命令的数据副本
	 * @param that TrustInsert实例
	 */
	private TrustInsert(TrustInsert that) {
		super(that);
		insert = that.insert;
	}

	/**
	 * 构造INSERT代理命令，指定INSERT命令
	 * @param insert INSERT命令
	 */
	public TrustInsert(Insert insert) {
		this();
		setInsert(insert);
	}

	/**
	 * 设置INSERT命令
	 * @param e INSERT命令实例
	 */
	public void setInsert(Insert e) {
		Laxkit.nullabled(e);
		insert = e;
	}

	/**
	 * 返回INSERT命令
	 * @return INSERT命令实例
	 */
	public Insert getInsert() {
		return insert;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TrustInsert duplicate() {
		return new TrustInsert(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(insert);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		insert = new Insert(reader);
	}

}