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
 * DELETE代理命令。<br>
 * 只在主DATA站点使用
 * 
 * @author scott.liang
 * @version 1.0 9/14/2017
 * @since laxcus 1.0
 */
public class TrustDelete extends Command {
	
	private static final long serialVersionUID = -342169679731386282L;

	/** DELETE命令 **/
	private Delete delete;

	/** 数据块编号 **/
	private long stub;

	/**
	 * 构造默认的DELETE代理命令
	 */
	private TrustDelete() {
		super();
	}

	/**
	 * 生成DELETE代理命令的数据副本
	 * @param that TrustDelete实例
	 */
	private TrustDelete(TrustDelete that) {
		super(that);
		delete = that.delete;
		stub = that.stub;
	}

	/**
	 * 构造DELETE代理命令，指定DELETE命令
	 * @param delete DELETE命令
	 * @param stub 数据块编号
	 */
	public TrustDelete(Delete delete, long stub) {
		this();
		setDelete(delete);
		setStub(stub);
	}

	/**
	 * 设置DELETE命令
	 * @param e DELETE命令实例
	 */
	public void setDelete(Delete e) {
		Laxkit.nullabled(e);
		
		delete = e;
	}

	/**
	 * 返回DELETE命令
	 * @return DELETE命令实例
	 */
	public Delete getDelete() {
		return delete;
	}

	/**
	 * 设置数据块编号
	 * @param e
	 */
	public void setStub(long e) {
		stub = e;
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return stub;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TrustDelete duplicate() {
		return new TrustDelete(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(delete);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		delete = new Delete(reader);
	}

}